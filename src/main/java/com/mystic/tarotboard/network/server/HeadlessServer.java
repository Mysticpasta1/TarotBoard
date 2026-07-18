package com.mystic.tarotboard.network.server;

import com.mystic.tarotboard.network.NetworkMessage;
import com.mystic.tarotboard.network.NetworkMessage.Msg;
import com.mystic.tarotboard.network.ServerAddress;
import com.mystic.tarotboard.utils.CardCatalog;
import com.mystic.tarotboard.utils.PlatformPaths;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Standalone headless TarotBoard server that acts as the authoritative game host (playerId=0).
 * Manages the deck, tracks piece state, relays cursor moves, and synchronizes state with clients.
 * <p>
 * The tracked board is persisted to disk (see {@link PlatformPaths#getServerSaveFilePath()}) and
 * reloaded on startup, so a server restart resumes the game in progress. Without this, a restart
 * wiped the in-memory board and handed the next player to join a fresh deck while everyone who was
 * already playing kept the real one.
 */
public class HeadlessServer {

    private static final List<String> wilds = CardCatalog.WILDS;

    private static final List<String> suits = CardCatalog.SUITS;

    private static final List<String> values = CardCatalog.VALUES;

    private static final int NUM_CARDS = CardCatalog.NUM_CARDS;

    /** Operator password used when neither an argument nor the environment supplies one. */
    public static final String DEFAULT_OPERATOR_PASSWORD = "admin";

    /** Bumped when the on-disk save layout changes so an incompatible old file is ignored, not misread. */
    private static final long SAVE_VERSION = 1L;

    /** How often the autosave thread flushes the board to disk when it has changed. */
    private static final long AUTOSAVE_INTERVAL_MS = 3000;

    private final GameServer gameServer;
    private final List<String> cardNames = new ArrayList<>();
    private final Set<Integer> operators = new HashSet<>();
    private final String operatorPassword;

    private final Path saveFile;
    /** Guards every read and write of the tracked board so a save snapshot is internally consistent. */
    private final Object stateLock = new Object();
    private volatile boolean dirty = false;
    private volatile boolean running = true;
    private Thread autosaveThread;

    private static class TrackedChip {
        final String id;
        double x, y, rotation;
        double r, g, b, opacity;
        boolean frontVis, backVis;

        TrackedChip(String id, double x, double y, double rotation,
                    double r, double g, double b, double opacity,
                    boolean frontVis, boolean backVis) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.rotation = rotation;
            this.r = r;
            this.g = g;
            this.b = b;
            this.opacity = opacity;
            this.frontVis = frontVis;
            this.backVis = backVis;
        }
    }

    private static class TrackedDie {
        final String id;
        double x, y, rotation;
        int sides, value;
        double r, g, b, opacity;

        TrackedDie(String id, double x, double y, double rotation,
                   int sides, int value,
                   double r, double g, double b, double opacity) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.rotation = rotation;
            this.sides = sides;
            this.value = value;
            this.r = r;
            this.g = g;
            this.b = b;
            this.opacity = opacity;
        }
    }

    private final List<TrackedChip> chips = new ArrayList<>();
    private final List<TrackedDie> dice = new ArrayList<>();

    // Cards are tracked by their fixed index 0..NUM_CARDS-1 — the same index the client uses
    // as the "card:N" id and as the slot in a StateSync. Without this the server kept no memory
    // of where cards were, so it answered every SendState with all cards dumped back in the deck
    // pile face-down. A fresh joiner never noticed (that is the start-of-game layout), but a
    // player who dropped and reconnected had their whole deck reset while everyone else kept the
    // real board, so from then on nobody's cards agreed.
    private final double[] cardX = new double[NUM_CARDS];
    private final double[] cardY = new double[NUM_CARDS];
    private final double[] cardRot = new double[NUM_CARDS];
    private final boolean[] cardBackVis = new boolean[NUM_CARDS];
    private final boolean[] cardFrontVis = new boolean[NUM_CARDS];
    private final boolean[] cardTextVis = new boolean[NUM_CARDS];

    /** Serializable snapshot of the whole tracked board, written to and read from {@link #saveFile}. */
    private record ServerSave(
            long version,
            ArrayList<String> cardNames,
            double[] cardX, double[] cardY, double[] cardRot,
            boolean[] cardBackVis, boolean[] cardFrontVis, boolean[] cardTextVis,
            ArrayList<ChipSnap> chips,
            ArrayList<DieSnap> dice
    ) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    private record ChipSnap(String id, double x, double y, double rotation,
                            double r, double g, double b, double opacity,
                            boolean frontVis, boolean backVis) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    private record DieSnap(String id, double x, double y, double rotation,
                           int sides, int value,
                           double r, double g, double b, double opacity) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    /**
     * Creates a new headless server on the given port.
     *
     * @param port     the port to listen on
     * @param password the operator password (maybe empty for no auth)
     * @throws IOException if the server socket cannot be opened
     */
    public HeadlessServer(int port, String password) throws IOException {
        this.operatorPassword = password;
        this.saveFile = Path.of(PlatformPaths.getServerSaveFilePath());
        operators.add(0);
        if (!loadState()) {
            initDeck();
        }
        gameServer = new GameServer(port, "Server", 0.5, 0.5, 0.5);
        gameServer.setIsOperatorCheck(operators::contains);
        gameServer.setOnMessage(this::handleMessage);
        gameServer.setOnPortForwarded(external -> {
            if (external > 0 && external != gameServer.getPort()) {
                System.out.println("[TarotBoard] Players outside this network must connect on port " + external);
            }
        });
        gameServer.start();
        startAutosave();
        System.out.println("[TarotBoard] Server started on port " + gameServer.getPort()
                + "  (" + NUM_CARDS + " cards)"
                + (password.isEmpty() ? "" : " (operator auth enabled)"));
    }

    /**
     * Returns the port the server is listening on, which is allocated by the system when the
     * requested port was 0.
     *
     * @return the local port
     */
    public int getPort() {
        return gameServer.getPort();
    }

    /**
     * Stops the server, flushes the current board to disk, and releases its port.
     */
    public void stop() {
        running = false;
        if (autosaveThread != null) autosaveThread.interrupt();
        // A final synchronous save so the very latest board survives a clean shutdown, not just
        // whatever the last autosave tick happened to catch.
        save();
        gameServer.stop();
    }

    /**
     * Resolves the operator password: an explicit {@code --password=X} or {@code --password X}
     * argument first, then the {@code OPERATOR_PASSWORD} environment variable, which is how a
     * hosting panel can set it without putting it in a visible command line, then
     * {@link #DEFAULT_OPERATOR_PASSWORD}.
     * <p>
     * An explicitly empty value is honoured and disables operator authentication entirely.
     *
     * @param args the command-line arguments
     * @return the operator password
     */
    public static String resolvePassword(List<String> args) {
        String value = null;
        for (int i = 0; i < args.size(); i++) {
            String arg = args.get(i);
            if (arg.startsWith("--password=")) {
                value = arg.substring("--password=".length());
            } else if (arg.equals("--password") && i + 1 < args.size()) {
                value = args.get(++i);
            }
        }
        if (value == null) value = System.getenv("OPERATOR_PASSWORD");
        return value == null ? DEFAULT_OPERATOR_PASSWORD : value;
    }

    private void initDeck() {
        synchronized (stateLock) {
            cardNames.clear();
            cardNames.addAll(wilds);
            for (String suit : suits) {
                for (String value : values) {
                    cardNames.add(value + " of " + suit);
                }
            }
            Collections.shuffle(cardNames, new Random());
            resetCardsToDeck();
            chips.clear();
            dice.clear();
        }
    }

    /**
     * Starts the daemon thread that flushes the tracked board to disk whenever it has changed.
     * A restart-safe save on every single piece move would hammer the disk, so moves only set a
     * dirty flag and this thread coalesces them into one write per interval.
     */
    private void startAutosave() {
        autosaveThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(AUTOSAVE_INTERVAL_MS);
                } catch (InterruptedException e) {
                    break;
                }
                if (dirty) {
                    dirty = false;
                    save();
                }
            }
        }, "tarotboard-autosave");
        autosaveThread.setDaemon(true);
        autosaveThread.start();
    }

    private void markDirty() {
        dirty = true;
    }

    /**
     * Restores the tracked board from {@link #saveFile}.
     *
     * @return true if a compatible save was loaded, false if there was none or it could not be
     * used (a corrupt file, a different version, or a deck built from a different card list) — in
     * which case the caller starts a fresh game.
     */
    private boolean loadState() {
        if (!Files.exists(saveFile)) return false;
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(saveFile))) {
            ServerSave s = (ServerSave) ois.readObject();
            if (s.version() != SAVE_VERSION) {
                System.err.println("[TarotBoard] Ignoring saved game from an incompatible version; starting fresh.");
                return false;
            }
            if (s.cardNames() == null || s.cardNames().size() != NUM_CARDS
                    || s.cardX() == null || s.cardX().length != NUM_CARDS
                    || s.cardY().length != NUM_CARDS || s.cardRot().length != NUM_CARDS
                    || s.cardBackVis().length != NUM_CARDS || s.cardFrontVis().length != NUM_CARDS
                    || s.cardTextVis().length != NUM_CARDS) {
                System.err.println("[TarotBoard] Saved game was built from a different card list ("
                        + (s.cardNames() == null ? 0 : s.cardNames().size()) + " vs " + NUM_CARDS
                        + " cards); starting fresh.");
                return false;
            }
            synchronized (stateLock) {
                cardNames.clear();
                cardNames.addAll(s.cardNames());
                System.arraycopy(s.cardX(), 0, cardX, 0, NUM_CARDS);
                System.arraycopy(s.cardY(), 0, cardY, 0, NUM_CARDS);
                System.arraycopy(s.cardRot(), 0, cardRot, 0, NUM_CARDS);
                System.arraycopy(s.cardBackVis(), 0, cardBackVis, 0, NUM_CARDS);
                System.arraycopy(s.cardFrontVis(), 0, cardFrontVis, 0, NUM_CARDS);
                System.arraycopy(s.cardTextVis(), 0, cardTextVis, 0, NUM_CARDS);
                chips.clear();
                for (ChipSnap c : s.chips()) {
                    chips.add(new TrackedChip(c.id(), c.x(), c.y(), c.rotation(),
                            c.r(), c.g(), c.b(), c.opacity(), c.frontVis(), c.backVis()));
                }
                dice.clear();
                for (DieSnap d : s.dice()) {
                    dice.add(new TrackedDie(d.id(), d.x(), d.y(), d.rotation(),
                            d.sides(), d.value(), d.r(), d.g(), d.b(), d.opacity()));
                }
            }
            System.out.println("[TarotBoard] Resumed saved game (" + chips.size() + " chips, "
                    + dice.size() + " dice)");
            return true;
        } catch (Throwable t) {
            System.err.println("[TarotBoard] Could not load saved game: " + t + "; starting fresh.");
            return false;
        }
    }

    /**
     * Writes the current tracked board to {@link #saveFile}. The snapshot is copied under the state
     * lock and then written outside it, so a save never blocks piece updates on disk I/O, and the
     * write goes to a temp file that is atomically renamed so a crash mid-write cannot leave a
     * half-written save that would fail to load on the next start.
     */
    private void save() {
        ServerSave snapshot;
        synchronized (stateLock) {
            ArrayList<ChipSnap> chipSnaps = new ArrayList<>(chips.size());
            for (TrackedChip c : chips) {
                chipSnaps.add(new ChipSnap(c.id, c.x, c.y, c.rotation,
                        c.r, c.g, c.b, c.opacity, c.frontVis, c.backVis));
            }
            ArrayList<DieSnap> dieSnaps = new ArrayList<>(dice.size());
            for (TrackedDie d : dice) {
                dieSnaps.add(new DieSnap(d.id, d.x, d.y, d.rotation,
                        d.sides, d.value, d.r, d.g, d.b, d.opacity));
            }
            snapshot = new ServerSave(SAVE_VERSION, new ArrayList<>(cardNames),
                    cardX.clone(), cardY.clone(), cardRot.clone(),
                    cardBackVis.clone(), cardFrontVis.clone(), cardTextVis.clone(),
                    chipSnaps, dieSnaps);
        }
        try {
            Path parent = saveFile.getParent();
            if (parent != null) Files.createDirectories(parent);
            Path tmp = saveFile.resolveSibling(saveFile.getFileName() + ".tmp");
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(tmp))) {
                oos.writeObject(snapshot);
            }
            try {
                Files.move(tmp, saveFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, saveFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Throwable t) {
            System.err.println("[TarotBoard] Failed to save game: " + t);
        }
    }

    private void handleMessage(NetworkMessage msg) {
        switch (msg.data()) {
            case Msg.SendState m -> sendStateSync(m.playerId());
            case Msg.RequestOperator m -> handleRequestOperator(m);
            case Msg.ReshuffleCards m -> {
                if (isOperator(m.playerId())) handleReshuffle();
            }
            case Msg.NewGame m -> {
                if (isOperator(m.playerId())) handleNewGame();
            }
            case Msg.ResetDice m -> {
                if (isOperator(m.playerId())) {
                    synchronized (stateLock) {
                        dice.clear();
                    }
                    markDirty();
                }
            }
            case Msg.ResetChips m -> {
                if (isOperator(m.playerId())) {
                    synchronized (stateLock) {
                        chips.clear();
                    }
                    markDirty();
                }
            }
            case Msg.SpawnChip m -> {
                trackChip(m);
                markDirty();
            }
            case Msg.SpawnDie m -> {
                trackDie(m);
                markDirty();
            }
            case Msg.DeletePiece m -> {
                untrackPiece(m.pieceId());
                markDirty();
            }
            case Msg.PieceMove m -> {
                updatePiecePos(m.pieceId(), m.x(), m.y());
                markDirty();
            }
            case Msg.PieceRotate m -> {
                updatePieceRot(m.pieceId(), m.rotation());
                markDirty();
            }
            case Msg.PieceFlip m -> {
                updateCardFlip(m);
                markDirty();
            }
            default -> {
            }
        }
    }

    /** The card index carried by a {@code "card:N"} id, or -1 if the id is not an in-range card. */
    private int cardIndex(String pieceId) {
        if (pieceId == null || !pieceId.startsWith("card:")) return -1;
        try {
            int i = Integer.parseInt(pieceId.substring("card:".length()));
            return (i >= 0 && i < NUM_CARDS) ? i : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Resets every card to the face-down deck pile, the layout a new or reshuffled game starts from.
     * Callers hold {@link #stateLock}.
     */
    private void resetCardsToDeck() {
        for (int i = 0; i < NUM_CARDS; i++) {
            cardX[i] = 50;
            cardY[i] = 50;
            cardRot[i] = 0;
            cardBackVis[i] = true;
            cardFrontVis[i] = false;
            cardTextVis[i] = false;
        }
    }

    private void updateCardFlip(Msg.PieceFlip m) {
        int i = cardIndex(m.pieceId());
        if (i < 0) return;
        synchronized (stateLock) {
            // The client's StateSync convention: index 0 is the back, 1 the front, 2 the name text.
            cardBackVis[i] = m.backVisible();
            cardFrontVis[i] = m.frontVisible();
            cardTextVis[i] = m.textVisible();
        }
    }

    private boolean isOperator(int playerId) {
        if (operators.contains(playerId)) return true;
        System.out.println("[TarotBoard] Denied non-operator action from player " + playerId);
        return false;
    }

    private void handleRequestOperator(Msg.RequestOperator m) {
        boolean granted = !operatorPassword.isEmpty() && operatorPassword.equals(m.password());
        if (granted) {
            operators.add(m.playerId());
            System.out.println("[TarotBoard] Player " + m.playerId() + " is now an operator");
        } else {
            System.out.println("[TarotBoard] Operator request denied for player " + m.playerId());
        }
        gameServer.sendTo(m.playerId(),
                NetworkMessage.of(new Msg.OperatorStatus(m.playerId(), granted)));
    }

    private void sendStateSync(int playerId) {
        NetworkMessage cardNamesMsg;
        NetworkMessage syncMsg;
        // Build the whole reply under the lock so the joiner sees one coherent snapshot even while
        // other players are moving pieces, then send outside the lock so disk-slow clients cannot
        // stall board updates.
        synchronized (stateLock) {
            cardNamesMsg = NetworkMessage.of(new Msg.CardNamesSync(new ArrayList<>(cardNames)));

            int nCards = NUM_CARDS;
            int[] cardIds = new int[nCards];
            for (int i = 0; i < nCards; i++) {
                cardIds[i] = i;
            }

            int nChips = chips.size();
            String[] chipIds = new String[nChips];
            double[] chipX = new double[nChips], chipY = new double[nChips], chipRot = new double[nChips];
            boolean[] chipFrontVis = new boolean[nChips], chipBackVis = new boolean[nChips];
            double[] chipR = new double[nChips], chipG = new double[nChips], chipB = new double[nChips], chipO = new double[nChips];

            for (int i = 0; i < nChips; i++) {
                var c = chips.get(i);
                chipIds[i] = c.id;
                chipX[i] = c.x;
                chipY[i] = c.y;
                chipRot[i] = c.rotation;
                chipFrontVis[i] = c.frontVis;
                chipBackVis[i] = c.backVis;
                chipR[i] = c.r;
                chipG[i] = c.g;
                chipB[i] = c.b;
                chipO[i] = c.opacity;
            }

            int nDice = dice.size();
            String[] dieIds = new String[nDice];
            double[] dieX = new double[nDice], dieY = new double[nDice], dieRot = new double[nDice];
            int[] dieSides = new int[nDice], dieVals = new int[nDice];
            double[] dieR = new double[nDice], dieG = new double[nDice], dieB = new double[nDice], dieO = new double[nDice];

            for (int i = 0; i < nDice; i++) {
                var d = dice.get(i);
                dieIds[i] = d.id;
                dieX[i] = d.x;
                dieY[i] = d.y;
                dieRot[i] = d.rotation;
                dieSides[i] = d.sides;
                dieVals[i] = d.value;
                dieR[i] = d.r;
                dieG[i] = d.g;
                dieB[i] = d.b;
                dieO[i] = d.opacity;
            }

            var sync = new Msg.StateSync(cardIds, cardX.clone(), cardY.clone(), cardRot.clone(),
                    cardBackVis.clone(), cardFrontVis.clone(), cardTextVis.clone(),
                    chipIds, chipX, chipY, chipRot, chipFrontVis, chipBackVis,
                    chipR, chipG, chipB, chipO,
                    dieIds, dieX, dieY, dieRot, dieSides, dieVals,
                    dieR, dieG, dieB, dieO);
            syncMsg = NetworkMessage.of(sync);
        }

        gameServer.sendTo(playerId, cardNamesMsg);
        gameServer.sendTo(playerId, syncMsg);
        System.out.println("[TarotBoard] State synced to player " + playerId);
    }

    private void handleReshuffle() {
        synchronized (stateLock) {
            Collections.shuffle(cardNames, new Random());
            // The client's reshuffle handler drops every card back onto the deck pile, so the
            // tracked layout has to follow or a later reconnect would restore the pre-shuffle spread.
            resetCardsToDeck();
        }
        markDirty();
        gameServer.broadcastToAll(
                NetworkMessage.of(new Msg.ReshuffleCards(0)));
        gameServer.broadcastToAll(
                NetworkMessage.of(new Msg.CardNamesSync(new ArrayList<>(cardNames))));
        System.out.println("[TarotBoard] Cards reshuffled");
    }

    private void handleNewGame() {
        initDeck();
        markDirty();
        gameServer.broadcastToAll(
                NetworkMessage.of(new Msg.NewGame(0)));
        gameServer.broadcastToAll(
                NetworkMessage.of(new Msg.CardNamesSync(new ArrayList<>(cardNames))));
        System.out.println("[TarotBoard] New game started");
    }

    private void trackChip(Msg.SpawnChip m) {
        synchronized (stateLock) {
            chips.removeIf(c -> c.id.equals(m.pieceId()));
            chips.add(new TrackedChip(m.pieceId(), m.x(), m.y(), 0,
                    m.red(), m.green(), m.blue(), m.opacity(),
                    true, false));
        }
    }

    private void trackDie(Msg.SpawnDie m) {
        synchronized (stateLock) {
            dice.removeIf(d -> d.id.equals(m.pieceId()));
            dice.add(new TrackedDie(m.pieceId(), m.x(), m.y(), 0,
                    m.sides(), (int) m.value(),
                    m.red(), m.green(), m.blue(), m.opacity()));
        }
    }

    private void untrackPiece(String pieceId) {
        synchronized (stateLock) {
            chips.removeIf(c -> c.id.equals(pieceId));
            dice.removeIf(d -> d.id.equals(pieceId));
        }
    }

    private void updatePiecePos(String pieceId, double x, double y) {
        synchronized (stateLock) {
            int card = cardIndex(pieceId);
            if (card >= 0) {
                cardX[card] = x;
                cardY[card] = y;
                return;
            }
            for (var c : chips) {
                if (c.id.equals(pieceId)) {
                    c.x = x;
                    c.y = y;
                    return;
                }
            }
            for (var d : dice) {
                if (d.id.equals(pieceId)) {
                    d.x = x;
                    d.y = y;
                    return;
                }
            }
        }
    }

    private void updatePieceRot(String pieceId, double rot) {
        synchronized (stateLock) {
            int card = cardIndex(pieceId);
            if (card >= 0) {
                cardRot[card] = rot;
                return;
            }
            for (var c : chips) {
                if (c.id.equals(pieceId)) {
                    c.rotation = rot;
                    return;
                }
            }
            for (var d : dice) {
                if (d.id.equals(pieceId)) {
                    d.rotation = rot;
                    return;
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        HeadlessServer server = new HeadlessServer(ServerAddress.resolvePort(List.of(args)), resolvePassword(List.of(args)));
        // A dedicated server is usually stopped with Ctrl-C or a container SIGTERM rather than a
        // clean stop() call, so flush the board on the way down or the last few minutes of play
        // would be lost.
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
