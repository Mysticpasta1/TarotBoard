package com.mystic.tarotboard.network.server;

import com.mystic.tarotboard.network.NetworkMessage;
import com.mystic.tarotboard.network.NetworkMessage.Msg;
import com.mystic.tarotboard.poker.PokerBotAI;
import com.mystic.tarotboard.poker.PokerTable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Standalone headless TarotBoard server that acts as the authoritative game host (playerId=0).
 * Manages the deck, tracks piece state, relays cursor moves, and synchronizes state with clients.
 */
public class HeadlessServer {

    private static final List<String> wilds = List.of(
            "Joker", "Soul", "Light", "Dark", "Judgement", "Chorus", "Life", "Death", "Wrath",
            "Pride", "Greed", "Lust", "Envy", "Gluttony", "Sloth", "Chasity", "Temperance", "Charity",
            "Diligence", "Kindness", "Patience", "Humility", "Voice", "Voices", "Mother", "Father", "Brother",
            "Sister", "Duality", "Accord", "Husband", "Wife", "Progeny", "Corridor", "Field", "Intellect", "Brawn",
            "Despair", "Past", "Present", "Future", "Gate", "Sign", "Ruin", "Snow", "Rain", "Tempest", "Lovers",
            "Discord", "Concord", "Harmony", "Dissonance", "Earth", "Fire", "Water", "Air", "Spirit",
            "Oblivion", "Obscurity", "Purgatory", "Nether", "Underworld", "Aether", "Overworld", "Limbo", "Chaos",
            "Balance", "Doom", "Peace", "Evil", "Good", "Neutral", "Hope", "Monster", "Human", "Dusk", "Dawn",
            "Paradox", "Entropy"
    );

    private static final List<String> suits = List.of(
            "Arcs", "Arrows", "Clouds", "Clovers", "Comets", "Crescents", "Crosses",
            "Crowns", "Diamonds", "Embers", "Eyes", "Gears", "Glyphs", "Flames", "Flowers",
            "Hearts", "Keys", "Locks", "Leaves", "Mountains", "Points", "Scrolls", "Shells",
            "Shields", "Spades", "Spirals", "Stars", "Suns", "Swords", "Tridents", "Trees", "Waves",
            "Quasars", "Runes", "Omens", "Sigils", "Orbs", "Veils", "Looms", "Shards", "Echoes",
            "Rifts", "Ashes", "Nulls", "Hallows", "Fluxes", "Ethers", "Grims"
    );

    private static final List<String> values = List.of(
            "Fugitive", "Devil", "Shadow", "Specter", "Phantom", "Void", "Wraith",
            "Ghoul", "Banshee", "Reverent", "Eidolon", "Shade",
            "Doppelganger", "Hollow", "Abyss", "Chimera", "Poltergeist",
            "Wight", "Apparition", "Nightmare", "Succubus", "Incubus",
            "Necromancer", "Fury", "Grim", "Harbinger", "Spectacle",
            "Lich", "Gorgon", "Drake", "Demon", "Frost",
            "Golem", "Hydra", "Inferno", "Juggernaut", "Kraken", "Reaper",
            "Leviathan", "Manticore", "Naga", "Blight", "Serpent",
            "Hold",
            "Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10",
            "Jack", "Queen", "King", "Nomad", "Prince",
            "Rune", "Fable", "Sorceress", "Utopia", "Wizard",
            "Titan", "Baron", "Illusionist", "Oracle", "Magician",
            "Luminary", "Eclipse", "Celestial", "Duke", "Genesis",
            "Zephyr", "Vesper", "Umbra", "Valkyrie", "Warden",
            "Zenith", "Yggdrasil", "Zodiac", "Phoenix", "Raven",
            "Cipher", "Angel", "Knight"
    );

    private static final int NUM_CARDS = (suits.size() * values.size()) + wilds.size();

    private static final long POKER_ANTE = 10;
    private static final long POKER_MIN_RAISE = 10;
    private static final long POKER_STARTING_BANKROLL = 1000;
    private static final int POKER_BOT_DELAY_SECONDS = 2;

    private final GameServer gameServer;
    private final List<String> cardNames = new ArrayList<>();
    private final Set<Integer> operators = new HashSet<>();
    private final String operatorPassword;
    private final ScheduledExecutorService botScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "poker-bot-scheduler");
        t.setDaemon(true);
        return t;
    });
    private final Random random = new Random();
    private PokerTable pokerTable;
    private int nextBotId = -1;

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

    /**
     * Creates a new headless server on the given port.
     *
     * @param port     the port to listen on
     * @param password the operator password (maybe empty for no auth)
     * @throws IOException if the server socket cannot be opened
     */
    public HeadlessServer(int port, String password) throws IOException {
        this.operatorPassword = password;
        operators.add(0);
        initDeck();
        gameServer = new GameServer(port, "Server", 0.5, 0.5, 0.5);
        gameServer.setIsOperatorCheck(operators::contains);
        gameServer.setOnMessage(this::handleMessage);
        gameServer.start();
        System.out.println("[TarotBoard] Server started on port " + port
                + "  (" + NUM_CARDS + " cards)"
                + (password.isEmpty() ? "" : " (operator auth enabled)"));
    }

    private void initDeck() {
        cardNames.clear();
        cardNames.addAll(wilds);
        for (String suit : suits) {
            for (String value : values) {
                cardNames.add(value + " of " + suit);
            }
        }
        Collections.shuffle(cardNames, new Random());
        chips.clear();
        dice.clear();
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
                if (isOperator(m.playerId())) dice.clear();
            }
            case Msg.ResetChips m -> {
                if (isOperator(m.playerId())) chips.clear();
            }
            case Msg.SpawnChip m -> trackChip(m);
            case Msg.SpawnDie m -> trackDie(m);
            case Msg.DeletePiece m -> untrackPiece(m.pieceId());
            case Msg.PieceMove m -> updatePiecePos(m.pieceId(), m.x(), m.y());
            case Msg.PieceRotate m -> updatePieceRot(m.pieceId(), m.rotation());
            case Msg.SetupGame m -> {
                if (isOperator(m.playerId()) && "POKER".equals(m.gameType())) startPokerMode();
            }
            case Msg.PokerSitDown m -> handlePokerSitDown(m);
            case Msg.PokerAddBot m -> {
                if (isOperator(m.playerId())) addPokerBot();
            }
            case Msg.PokerStartHand m -> {
                if (isOperator(m.playerId())) startPokerHand();
            }
            case Msg.PokerAction m -> applyPokerAction(m.playerId(), m.action(), m.amount());
            default -> {
            }
        }
    }

    private void startPokerMode() {
        pokerTable = new PokerTable(POKER_ANTE, POKER_MIN_RAISE, wilds, values);
        nextBotId = -1;
        System.out.println("[TarotBoard] Poker Mode started");
        broadcastPokerState();
    }

    private void handlePokerSitDown(Msg.PokerSitDown m) {
        if (pokerTable == null) return;
        pokerTable.sitDown(m.playerId(), false, POKER_STARTING_BANKROLL);
        broadcastPokerState();
    }

    private void addPokerBot() {
        if (pokerTable == null) return;
        pokerTable.sitDown(nextBotId--, true, POKER_STARTING_BANKROLL);
        broadcastPokerState();
    }

    private void startPokerHand() {
        if (pokerTable == null) return;
        try {
            pokerTable.startHand(buildShuffledDeck());
        } catch (IllegalStateException e) {
            System.out.println("[TarotBoard] Cannot start poker hand: " + e.getMessage());
            return;
        }
        for (PokerTable.Seat seat : pokerTable.seats()) {
            if (!seat.isBot() && seat.active()) {
                gameServer.sendTo(seat.playerId(),
                        NetworkMessage.of(new Msg.PokerDealPrivate(seat.playerId(), new ArrayList<>(seat.holeCards()))));
            }
        }
        broadcastPokerState();
        scheduleBotTurnIfNeeded();
    }

    private Deque<String> buildShuffledDeck() {
        List<String> deck = new ArrayList<>(wilds);
        for (String suit : suits) {
            for (String value : values) {
                deck.add(value + " of " + suit);
            }
        }
        Collections.shuffle(deck, random);
        return new ArrayDeque<>(deck);
    }

    private void applyPokerAction(int playerId, String action, long amount) {
        if (pokerTable == null || pokerTable.phase() != PokerTable.Phase.BETTING) return;
        if (!pokerTable.applyAction(playerId, action, amount)) {
            System.out.println("[TarotBoard] Rejected poker action " + action + " from player " + playerId);
            return;
        }
        broadcastPokerState();
        if (pokerTable.phase() == PokerTable.Phase.SHOWDOWN) {
            broadcastShowdown();
        } else {
            scheduleBotTurnIfNeeded();
        }
    }

    private void scheduleBotTurnIfNeeded() {
        if (pokerTable == null || pokerTable.phase() != PokerTable.Phase.BETTING) return;
        Integer turnPlayerId = pokerTable.currentTurnPlayerId();
        if (turnPlayerId == null) return;
        PokerTable.Seat seat = pokerTable.seats().stream()
                .filter(s -> s.playerId() == turnPlayerId).findFirst().orElse(null);
        if (seat == null || !seat.isBot()) return;
        botScheduler.schedule(() -> {
            if (pokerTable == null || pokerTable.phase() != PokerTable.Phase.BETTING) return;
            Integer current = pokerTable.currentTurnPlayerId();
            if (current == null || current != seat.playerId()) return;
            PokerBotAI.BotDecision decision = PokerBotAI.decide(seat.holeCards(), wilds, values,
                    pokerTable.currentBet(), seat.contributionThisRound(), seat.bankroll(), POKER_MIN_RAISE, random);
            applyPokerAction(seat.playerId(), decision.action(), decision.amount());
        }, POKER_BOT_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    private void broadcastPokerState() {
        if (pokerTable == null) return;
        List<PokerTable.Seat> seats = pokerTable.seats();
        int n = seats.size();
        int[] seatPlayerIds = new int[n];
        boolean[] seatIsBot = new boolean[n];
        boolean[] seatFolded = new boolean[n];
        boolean[] seatActive = new boolean[n];
        int[] seatHoleCardCount = new int[n];
        long[] seatBankroll = new long[n];
        long[] seatContribution = new long[n];
        for (int i = 0; i < n; i++) {
            PokerTable.Seat s = seats.get(i);
            seatPlayerIds[i] = s.playerId();
            seatIsBot[i] = s.isBot();
            seatFolded[i] = s.folded();
            seatActive[i] = s.active();
            seatHoleCardCount[i] = s.holeCards().size();
            seatBankroll[i] = s.bankroll();
            seatContribution[i] = s.contributionThisRound();
        }
        Integer turn = pokerTable.currentTurnPlayerId();
        var sync = new Msg.PokerStateSync(seatPlayerIds, seatIsBot, seatFolded, seatActive,
                seatHoleCardCount, seatBankroll, seatContribution,
                turn == null ? -1 : turn, pokerTable.potTotal(), pokerTable.currentBet(),
                pokerTable.phase().name());
        gameServer.broadcastToAll(NetworkMessage.of(sync));
    }

    private void broadcastShowdown() {
        PokerTable.ShowdownResult result = pokerTable.lastShowdown();
        if (result == null) return;
        int n = result.results().size();
        int[] seatIds = new int[n];
        ArrayList<ArrayList<String>> revealed = new ArrayList<>();
        ArrayList<String> handTypeNames = new ArrayList<>();
        int[] handRanks = new int[n];
        long[] scores = new long[n];
        for (int i = 0; i < n; i++) {
            var entry = result.results().get(i);
            seatIds[i] = entry.playerId();
            revealed.add(new ArrayList<>(entry.hand().cardsUsed()));
            handTypeNames.add(entry.hand().type().name());
            handRanks[i] = entry.hand().type().rank();
            scores[i] = entry.hand().score();
        }
        int[] winningIds = result.winningPlayerIds().stream().mapToInt(Integer::intValue).toArray();
        var msg = new Msg.PokerShowdownResult(seatIds, revealed, handTypeNames, handRanks, scores,
                winningIds, result.potWon());
        gameServer.broadcastToAll(NetworkMessage.of(msg));
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
        gameServer.sendTo(playerId,
                NetworkMessage.of(new Msg.CardNamesSync(new ArrayList<>(cardNames))));

        int nCards = NUM_CARDS;
        int[] cardIds = new int[nCards];
        double[] cardX = new double[nCards];
        double[] cardY = new double[nCards];
        double[] cardRot = new double[nCards];
        boolean[] cardBackVis = new boolean[nCards];
        boolean[] cardFrontVis = new boolean[nCards];
        boolean[] cardTextVis = new boolean[nCards];

        for (int i = 0; i < nCards; i++) {
            cardIds[i] = i;
            cardX[i] = 50;
            cardY[i] = 50;
            cardRot[i] = 0;
            cardBackVis[i] = true;
            cardFrontVis[i] = false;
            cardTextVis[i] = false;
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

        var sync = new Msg.StateSync(cardIds, cardX, cardY, cardRot,
                cardBackVis, cardFrontVis, cardTextVis,
                chipIds, chipX, chipY, chipRot, chipFrontVis, chipBackVis,
                chipR, chipG, chipB, chipO,
                dieIds, dieX, dieY, dieRot, dieSides, dieVals,
                dieR, dieG, dieB, dieO);

        gameServer.sendTo(playerId, NetworkMessage.of(sync));
        System.out.println("[TarotBoard] State synced to player " + playerId);
    }

    private void handleReshuffle() {
        Collections.shuffle(cardNames, new Random());
        gameServer.broadcastToAll(
                NetworkMessage.of(new Msg.ReshuffleCards(0)));
        gameServer.broadcastToAll(
                NetworkMessage.of(new Msg.CardNamesSync(new ArrayList<>(cardNames))));
        System.out.println("[TarotBoard] Cards reshuffled");
    }

    private void handleNewGame() {
        initDeck();
        gameServer.broadcastToAll(
                NetworkMessage.of(new Msg.NewGame(0)));
        gameServer.broadcastToAll(
                NetworkMessage.of(new Msg.CardNamesSync(new ArrayList<>(cardNames))));
        chips.clear();
        dice.clear();
        System.out.println("[TarotBoard] New game started");
    }

    private void trackChip(Msg.SpawnChip m) {
        chips.removeIf(c -> c.id.equals(m.pieceId()));
        chips.add(new TrackedChip(m.pieceId(), m.x(), m.y(), 0,
                m.red(), m.green(), m.blue(), m.opacity(),
                true, false));
    }

    private void trackDie(Msg.SpawnDie m) {
        dice.removeIf(d -> d.id.equals(m.pieceId()));
        dice.add(new TrackedDie(m.pieceId(), m.x(), m.y(), 0,
                m.sides(), (int) m.value(),
                m.red(), m.green(), m.blue(), m.opacity()));
    }

    private void untrackPiece(String pieceId) {
        chips.removeIf(c -> c.id.equals(pieceId));
        dice.removeIf(d -> d.id.equals(pieceId));
    }

    private void updatePiecePos(String pieceId, double x, double y) {
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

    private void updatePieceRot(String pieceId, double rot) {
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

    static void main(String[] args) throws IOException {
        int port = 5555;
        String password = "admin";
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--port" -> {
                    if (i + 1 < args.length) port = Integer.parseInt(args[++i]);
                }
                case "--password" -> {
                    if (i + 1 < args.length) password = args[++i];
                }
                default -> {
                    if (args[i].chars().allMatch(Character::isDigit)) {
                        port = Integer.parseInt(args[i]);
                    }
                }
            }
        }

        new HeadlessServer(port, password);

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}