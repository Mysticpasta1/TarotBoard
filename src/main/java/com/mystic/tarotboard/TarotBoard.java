package com.mystic.tarotboard;

import com.mystic.tarotboard.items.Cards;
import com.mystic.tarotboard.items.Chips;
import com.mystic.tarotboard.items.Dice;
import com.mystic.tarotboard.network.NetworkMessage;
import com.mystic.tarotboard.network.NetworkMessage.Msg;
import com.mystic.tarotboard.network.UpdateManager;
import com.mystic.tarotboard.network.client.GameClient;
import com.mystic.tarotboard.network.server.GameServer;
import com.mystic.tarotboard.poker.CardCategory;
import com.mystic.tarotboard.poker.HandResult;
import com.mystic.tarotboard.poker.HandType;
import com.mystic.tarotboard.poker.PokerBotAI;
import com.mystic.tarotboard.poker.PokerClientState;
import com.mystic.tarotboard.poker.PokerTable;
import com.mystic.tarotboard.scenes.GameScene;
import com.mystic.tarotboard.scenes.HostGameScene;
import com.mystic.tarotboard.scenes.JoinGameScene;
import com.mystic.tarotboard.scenes.MultiplayerScene;
import com.mystic.tarotboard.scenes.PokerScene;
import com.mystic.tarotboard.scenes.StartScene;
import com.mystic.tarotboard.theming.RemoteCursor;
import com.mystic.tarotboard.theming.ThemeConfiguration;
import com.mystic.tarotboard.theming.ThemeManager;
import com.mystic.tarotboard.utils.CardDataHelper;
import com.mystic.tarotboard.utils.LogWindow;
import com.mystic.tarotboard.utils.PlatformPaths;
import com.mystic.tarotboard.utils.SaveData;
import com.mystic.tarotboard.utils.Styles;
import com.mystic.tarotboard.utils.UIUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main application class for TarotBoard.
 * Manages the primary game lifecycle including UI setup, card/chip/dice
 * management, multiplayer networking, theme management, and save/load.
 */
public class TarotBoard extends Application {
    /**
     * Constructs a new TarotBoard instance.
     */
    public TarotBoard() {
    }

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

    /**
     * All possible card values in the deck, including numbered ranks,
     * face cards, and supernatural entity names.
     */
    public static final List<String> values = List.of(
            "Fugitive", "Devil", "Shadow", "Specter", "Phantom", "Void", "Wraith",
            "Ghoul", "Banshee", "Reverent", "Eidolon", "Shade",
            "Doppelganger", "Hollow", "Abyss", "Chimera", "Poltergeist",
            "Wight", "Apparition", "Nightmare", "Succubus", "Incubus", "Unknown",
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
            "Cipher", "Angel", "Knight", "Venom"
    );

    private static final Pattern CARD_PATTERN = Pattern.compile("^(?<value>[\\d,a-z,A-Z]+) of (?<suit>[a-z,A-Z]+)$");
    private static final int NUM_CARDS = (suits.size() * values.size()) + wilds.size();
    public static final double CARD_WIDTH = 150;
    private static final double CARD_HEIGHT = 200;
    private static Stage primaryStage;
    private GameScene gameScene;
    private PokerScene pokerScene;
    private StartScene startScene;
    private MultiplayerScene multiplayerScene;
    private HostGameScene hostGameScene;
    private JoinGameScene joinGameScene;
    private final List<Chips> chips = new ArrayList<>();
    private Image bwFrontImage;
    private Image bwBackImage;
    private Cards[] cards;
    private final List<Dice> dice = new ArrayList<>();
    private static boolean reshuffled = false;
    private static final ObservableList<String> cardNames = FXCollections.observableArrayList();
    private Color currentColor = Color.WHITE;
    private ThemeConfiguration currentCardTheme = initDefaultTheme();

    private static ThemeConfiguration initDefaultTheme() {
        var t = ThemeManager.getThemeByName("Default");
        ThemeManager.setActiveTheme(t);
        return t;
    }

    private String customCardFrontPath = null;
    private String customCardBackPath = null;
    private String customChipFrontPath = null;
    private String customChipBackPath = null;
    private String customBackgroundPath = null;

    private static final double DEFAULT_DECK_X = 50;
    private static final double DEFAULT_DECK_Y = 50;

    private GameServer gameServer;
    private GameClient gameClient;
    private boolean isMultiplayer;
    private int myPlayerId;
    private boolean isHost;
    private boolean isOperator;
    private byte[] myCursorImage;
    private final Map<Integer, RemoteCursor> remoteCursors = new HashMap<>();
    private final Map<String, StackPane> pieceMap = new HashMap<>();
    private final List<NetworkMessage.PlayerInfo> playerList = new ArrayList<>();
    private final Map<String, Long> lastPieceMoveTime = new HashMap<>();

    private final Set<Integer> operators = new HashSet<>();
    private String hostOperatorPassword = "admin";

    private static final long POKER_ANTE = 10;
    private static final long POKER_MIN_RAISE = 10;
    private static final long POKER_STARTING_BANKROLL = 1000;
    private static final int POKER_BOT_DELAY_SECONDS = 2;
    /** Point value of a single physical chip piece staked in Poker Mode; chip color stays purely cosmetic. */
    public static final long POKER_CHIP_VALUE = 10;

    private PokerTable pokerTable;
    private int pokerNextBotId = -1;
    private final PokerClientState pokerClientState = new PokerClientState();
    private final ScheduledExecutorService pokerBotScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "poker-bot-scheduler");
        t.setDaemon(true);
        return t;
    });

    /**
     * Returns the game scene instance.
     *
     * @return the game scene
     */
    public GameScene getGameScene() {
        return gameScene;
    }

    /**
     * Returns the current player color used for new chips.
     *
     * @return the current color
     */
    public Color getCurrentColor() {
        return currentColor;
    }

    /**
     * Sets the current player color for new chips.
     *
     * @param c the new color
     */
    public void setCurrentColor(Color c) {
        currentColor = c;
    }

    /**
     * Returns the currently active card theme configuration.
     *
     * @return the current card theme
     */
    public ThemeConfiguration getCurrentCardTheme() {
        return currentCardTheme;
    }

    /**
     * Returns the currently loaded chip front image, used to render chip pieces.
     *
     * @return the chip front image
     */
    public Image getChipFrontImage() {
        return bwFrontImage;
    }

    /**
     * Returns the currently loaded chip back image, used to render chip pieces.
     *
     * @return the chip back image
     */
    public Image getChipBackImage() {
        return bwBackImage;
    }

    /**
     * Returns the list of chip items on the board.
     *
     * @return the chips list
     */
    public List<Chips> getChips() {
        return chips;
    }

    /**
     * Returns the array of card items on the board.
     *
     * @return the cards array
     */
    public Cards[] getCards() {
        return cards;
    }

    /**
     * Returns the local player's network ID.
     *
     * @return the player ID
     */
    public int getMyPlayerId() {
        return myPlayerId;
    }

    /**
     * Returns whether this session is a multiplayer game.
     *
     * @return true if multiplayer
     */
    public boolean isMultiplayer() {
        return isMultiplayer;
    }

    /**
     * Returns whether this peer is the host of the multiplayer session.
     *
     * @return true if this peer is the host
     */
    public boolean isHost() {
        return isHost;
    }

    /**
     * Returns whether this peer has operator privileges.
     *
     * @return true if this peer is an operator
     */
    public boolean isOperator() {
        return isOperator;
    }

    /**
     * Returns whether the game client is currently connected to a server.
     *
     * @return true if connected
     */
    public boolean isClientConnected() {
        return gameClient != null && gameClient.isConnected();
    }

    /**
     * Returns the game server instance, or null if not hosting.
     *
     * @return the game server, or null
     */
    public GameServer getGameServer() {
        return gameServer;
    }

    /**
     * Returns the current list of connected players.
     *
     * @return the player list
     */
    public List<NetworkMessage.PlayerInfo> getPlayerList() {
        return playerList;
    }

    /**
     * Returns the read-only poker display state used to render the Poker Mode panel, kept in sync
     * whether this process is the poker authority or a non-authoritative client.
     *
     * @return the poker client display state
     */
    public PokerClientState getPokerClientState() {
        return pokerClientState;
    }

    /**
     * Returns whether this process owns the authoritative poker table (host or single-player).
     *
     * @return true if this process is the poker authority
     */
    public boolean isPokerAuthority() {
        return isHost || !isMultiplayer;
    }

    /**
     * Refreshes both the Poker Mode nav button on the game board and the dedicated
     * {@link PokerScene}'s panel, keeping both in sync with the current poker display state.
     */
    private void refreshPokerUI() {
        gameScene.updatePokerNavButton();
        if (pokerScene != null) {
            pokerScene.refreshPokerPanel();
        }
    }

    /**
     * Enters Poker Mode: starts a fresh table if one isn't already active, then switches the
     * primary stage to the dedicated {@link PokerScene}.
     */
    public void enterPokerMode() {
        if (!pokerClientState.pokerModeActive) {
            startPokerMode();
        }
        primaryStage.setScene(pokerScene.getScene());
        primaryStage.setTitle("Poker Mode");
        refreshPokerUI();
    }

    /**
     * Switches the primary stage back to the sandbox game board, leaving any active poker
     * session running in the background.
     */
    public void switchToGameBoard() {
        primaryStage.setScene(gameScene.getScene());
        primaryStage.setTitle("Game Scene");
    }

    /**
     * Initializes and displays the primary stage with all game scenes,
     * including the main menu, multiplayer setup, and the game board.
     *
     * @param primaryStage the primary stage for this JavaFX application
     */
    @Override
    public void start(Stage primaryStage) {
        LogWindow logWindow = new LogWindow();
        logWindow.setTitle("TarotBoard Console Log");
        logWindow.show();

        TarotBoard.primaryStage = primaryStage;
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double baseWidth = screenBounds.getWidth();
        double baseHeight = screenBounds.getHeight();

        gameScene = new GameScene(this, primaryStage, baseWidth, baseHeight);
        pokerScene = new PokerScene(this, primaryStage, baseWidth, baseHeight);
        startScene = new StartScene(this, primaryStage, baseWidth, baseHeight);
        multiplayerScene = new MultiplayerScene(this, baseWidth, baseHeight);
        hostGameScene = new HostGameScene(this, baseWidth, baseHeight);
        joinGameScene = new JoinGameScene(this, baseWidth, baseHeight);

        updateBackground(gameScene.getGameBg());
        updateStartSceneBackground();

        CardDataHelper.addCardNames(cardNames, wilds, suits, values);
        CardDataHelper.generateShuffledCardNames(cardNames);
        cards = new Cards[NUM_CARDS];

        loadAndCreateCards();

        bwFrontImage = loadImage(customChipFrontPath, currentCardTheme.getChipFrontPath(), currentCardTheme);
        bwBackImage = loadImage(customChipBackPath, currentCardTheme.getChipBackPath(), currentCardTheme);

        gameScene.updateOperatorButtonsVisibility();
        refreshPokerUI();

        primaryStage.setScene(startScene.getScene());
        primaryStage.setX(screenBounds.getMinX());
        primaryStage.setY(screenBounds.getMinY());
        primaryStage.setWidth(screenBounds.getWidth());
        primaryStage.setHeight(screenBounds.getHeight());
        primaryStage.show();
    }

    /**
     * Finds a game piece pane at the given scene coordinates.
     *
     * @param mouseX the scene X coordinate
     * @param mouseY the scene Y coordinate
     * @return the piece pane at the coordinates, or null if none found
     */
    public StackPane findPieceAtMouse(double mouseX, double mouseY) {
        for (StackPane pane : pieceMap.values()) {
            var pt = pane.sceneToLocal(mouseX, mouseY);
            if (pane.contains(pt)) return pane;
        }
        return null;
    }

    /**
     * Starts a new multiplayer game server on the configured port and switches to the game scene.
     */
    public void hostGame() {
        if (gameServer != null) leaveGame();
        String name = hostGameScene.getPlayerNameField().getText().trim();
        if (name.isEmpty()) name = "Host";
        int port;
        try {
            port = Integer.parseInt(hostGameScene.getHostPortField().getText().trim());
        } catch (NumberFormatException e) {
            hostGameScene.getNetworkStatusLabel().setText("Invalid port");
            return;
        }
        try {
            Color c = hostGameScene.getPlayerColorPicker().getValue();
            gameServer = new GameServer(port, name, c.getRed(), c.getGreen(), c.getBlue());
            myPlayerId = gameServer.getHostPlayerId();
            isHost = true;
            isMultiplayer = true;
            operators.clear();
            operators.add(myPlayerId);
            gameServer.setIsOperatorCheck(operators::contains);
            hostOperatorPassword = hostGameScene.getHostOpPasswordField().getText();
            playerList.clear();
            playerList.addAll(gameServer.getPlayers());

            gameServer.setOnMessage(msg -> Platform.runLater(() -> handleNetworkMessage(msg)));
            gameServer.start();

            hostGameScene.getNetworkStatusLabel().setText("Hosting on port " + port + " (ID: " + myPlayerId + ")");
            hostGameScene.getNetworkStatusLabel().setStyle(Styles.mpStatusOk());
            gameScene.getNetworkStatusInGame().setText("Hosting on port " + port);
            gameScene.getNetworkStatusInGame().setStyle(Styles.panelLabel());
            gameScene.setMultiplayerControlsVisible(true);
            gameScene.updateOperatorButtonsVisibility(); // Update when hosting

            primaryStage.setScene(gameScene.getScene());
            primaryStage.setTitle("Game Scene - Hosting on port " + port);
        } catch (IOException e) {
            hostGameScene.getNetworkStatusLabel().setText("Failed to host: " + e.getMessage());
            hostGameScene.getNetworkStatusLabel().setStyle(Styles.mpStatusErr());
            isMultiplayer = false;
            isHost = false;
            gameScene.updateOperatorButtonsVisibility();
        }
    }

    public void joinGame() {
        if (gameClient != null && gameClient.isConnected()) leaveGame();
        String name = joinGameScene.getPlayerNameField().getText().trim();
        if (name.isEmpty()) name = "Player";
        String ip = joinGameScene.getJoinIpField().getText().trim();
        int port;
        try {
            port = Integer.parseInt(joinGameScene.getJoinPortField().getText().trim());
        } catch (NumberFormatException e) {
            joinGameScene.getNetworkStatusLabel().setText("Invalid port");
            return;
        }
        try {
            gameClient = new GameClient(ip, port);
            isHost = false;
            isMultiplayer = true;

            gameClient.setOnMessage(msg -> Platform.runLater(() -> handleNetworkMessage(msg)));
            gameClient.start();

            Color pc = joinGameScene.getPlayerColorPicker().getValue();
            gameClient.send(NetworkMessage.of(new Msg.PlayerJoin(name, pc.getRed(), pc.getGreen(), pc.getBlue())));

            gameClient.send(NetworkMessage.of(new Msg.SendState(myPlayerId)));

            joinGameScene.getNetworkStatusLabel().setText("Connected to " + ip + ":" + port);
            joinGameScene.getNetworkStatusLabel().setStyle(Styles.mpStatusOk());
            gameScene.getNetworkStatusInGame().setText("Connected to " + ip + ":" + port);
            gameScene.getNetworkStatusInGame().setStyle(Styles.panelLabel());
            gameScene.setMultiplayerControlsVisible(true);
            gameScene.updateOperatorButtonsVisibility(); // Update when joining

            primaryStage.setScene(gameScene.getScene());
            primaryStage.setTitle("Game Scene - Connected to " + ip + ":" + port);
        } catch (IOException e) {
            joinGameScene.getNetworkStatusLabel().setText("Failed to connect: " + e.getMessage());
            joinGameScene.getNetworkStatusLabel().setStyle(Styles.mpStatusErr());
            isMultiplayer = false;
            gameScene.updateOperatorButtonsVisibility();
        }
    }

    /**
     * Disconnects from any active multiplayer session and cleans up remote cursors and player state.
     */
    public void leaveGame() {
        if (gameClient != null) {
            gameClient.disconnect();
            gameClient = null;
        }
        if (gameServer != null) {
            gameServer.stop();
            gameServer = null;
        }
        isMultiplayer = false;
        isHost = false;
        isOperator = false;
        myPlayerId = -1;
        operators.clear();
        for (var cursor : remoteCursors.values()) {
            cursor.removeFrom(gameScene.getCursorOverlay());
        }
        remoteCursors.clear();
        playerList.clear();
        pokerTable = null;
        pokerClientState.pokerModeActive = false;
        pokerClientState.seats = List.of();
        pokerClientState.myHoleCards = List.of();
        pokerClientState.lastShowdown = null;
        if (hostGameScene != null) {
            hostGameScene.getNetworkStatusLabel().setText("Offline");
            hostGameScene.getNetworkStatusLabel().setStyle(Styles.mpLabel());
        }
        if (joinGameScene != null) {
            joinGameScene.getNetworkStatusLabel().setText("Offline");
            joinGameScene.getNetworkStatusLabel().setStyle(Styles.mpLabel());
            joinGameScene.getOperatorStatusLabel().setText("");
        }
        if (gameScene != null) {
            gameScene.getNetworkStatusInGame().setText("");
            gameScene.setMultiplayerControlsVisible(false);
            gameScene.updateOperatorButtonsVisibility(); // Reset to single player view
            refreshPokerUI();
        }
        if (primaryStage != null) {
            primaryStage.setTitle("TarotBoard");
        }
    }

    /**
     * Sends an operator access request to the host using the password entered in the multiplayer UI.
     */
    public void requestOperatorAccess() {
        String password = joinGameScene.getOperatorPasswordField().getText();
        if (password.isEmpty()) {
            joinGameScene.getOperatorStatusLabel().setText("Enter a password");
            joinGameScene.getOperatorStatusLabel().setStyle("-fx-font-size: 12pt; -fx-text-fill: #c44;");
            return;
        }
        if (gameClient != null && gameClient.isConnected()) {
            gameClient.send(NetworkMessage.of(new Msg.RequestOperator(myPlayerId, password)));
            joinGameScene.getOperatorStatusLabel().setText("Requesting operator access...");
            joinGameScene.getOperatorStatusLabel().setStyle("-fx-font-size: 12pt; -fx-text-fill: #FFA500;");
        } else {
            joinGameScene.getOperatorStatusLabel().setText("Not connected to a server");
            joinGameScene.getOperatorStatusLabel().setStyle("-fx-font-size: 12pt; -fx-text-fill: #c44;");
        }
    }

    /**
     * Opens a file chooser to select a custom cursor image and sends it to connected peers.
     */
    public void chooseCursorImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose Cursor Image");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        java.io.File file = fc.showOpenDialog(primaryStage);
        if (file == null) return;
        try {
            myCursorImage = java.nio.file.Files.readAllBytes(file.toPath());
            if (hostGameScene != null && hostGameScene.getCursorStatusLabel() != null) {
                hostGameScene.getCursorStatusLabel().setText(file.getName());
                hostGameScene.getCursorStatusLabel().setStyle(Styles.mpSmallLabel());
            }
            if (joinGameScene != null && joinGameScene.getCursorStatusLabel() != null) {
                joinGameScene.getCursorStatusLabel().setText(file.getName());
                joinGameScene.getCursorStatusLabel().setStyle(Styles.mpSmallLabel());
            }
            if (gameScene != null) {
                Image cursorImg = new Image(new ByteArrayInputStream(myCursorImage));
                if (!cursorImg.isError()) {
                    gameScene.getScene().setCursor(new ImageCursor(cursorImg, cursorImg.getWidth() / 2, cursorImg.getHeight() / 2));
                }
            }
            if (isMultiplayer) {
                sendNetworkMessage(NetworkMessage.of(new Msg.CursorImage(myPlayerId, myCursorImage)));
            }
        } catch (IOException e) {
            System.err.println("Failed to load cursor image: " + e.getMessage());
        }
    }

    /**
     * Sends a network message to the connected client or broadcasts to all peers if hosting.
     *
     * @param msg the message to send
     */
    public void sendNetworkMessage(NetworkMessage msg) {
        if (gameClient != null && gameClient.isConnected()) {
            gameClient.send(msg);
        }
        if (gameServer != null && isHost) {
            gameServer.broadcast(msg, myPlayerId);
        }
    }

    private void handleNetworkMessage(NetworkMessage msg) {
        switch (msg.data()) {
            case Msg.YourId y -> myPlayerId = y.playerId();
            case Msg.OperatorStatus o -> handleOperatorStatus(o);
            case Msg.PlayerList p -> handlePlayerList(p);
            case Msg.PlayerLeave l -> handlePlayerLeave(l);
            case Msg.CardNamesSync c -> handleCardNamesSync(c);
            case Msg.CursorMove c -> handleCursorMove(c);
            case Msg.CursorImage c -> handleCursorImage(c);
            case Msg.PieceMove m -> handlePieceMove(m);
            case Msg.PieceRotate m -> handlePieceRotate(m);
            case Msg.PieceFlip m -> handlePieceFlip(m);
            case Msg.PieceToFront m -> handlePieceToFront(m);
            case Msg.SpawnChip m -> handleSpawnChip(m);
            case Msg.SpawnDie m -> handleSpawnDie(m);
            case Msg.DeletePiece m -> handleDeletePiece(m);
            case Msg.DieRoll m -> handleDieRoll(m);
            case Msg.RequestOperator m -> handleRequestOperator(m);
            case Msg.ReshuffleCards m -> handleReshuffleCards(m);
            case Msg.ResetDice m -> handleResetDice(m);
            case Msg.ResetChips m -> handleResetChips(m);
            case Msg.NewGame m -> handleNewGame(m);
            case Msg.SendState m -> handleSendState(m);
            case Msg.StateSync s -> handleStateSync(s);
            case Msg.SetupGame m -> handleSetupGame(m);
            case Msg.PokerSitDown m -> handlePokerSitDown(m);
            case Msg.PokerAddBot m -> handlePokerAddBot(m);
            case Msg.PokerStartHand m -> handlePokerStartHand(m);
            case Msg.PokerAction m -> handlePokerAction(m);
            case Msg.PokerStateSync s -> handlePokerStateSync(s);
            case Msg.PokerDealPrivate m -> handlePokerDealPrivate(m);
            case Msg.PokerShowdownResult m -> handlePokerShowdownResult(m);
            default -> {
            }
        }
    }

    private void handleRequestOperator(Msg.RequestOperator m) {
        boolean granted = !hostOperatorPassword.isEmpty() && hostOperatorPassword.equals(m.password());
        if (granted) {
            operators.add(m.playerId());
            System.out.println("[TarotBoard] Player " + m.playerId() + " is now an operator");
        } else {
            System.out.println("[TarotBoard] Operator request denied for player " + m.playerId());
        }
        if (gameServer != null) {
            gameServer.sendTo(m.playerId(),
                    NetworkMessage.of(new Msg.OperatorStatus(m.playerId(), granted)));
        }
    }

    private boolean isNotOperator(int playerId) {
        if (operators.contains(playerId)) return false;
        System.out.println("[TarotBoard] Denied non-operator action from player " + playerId);
        return true;
    }

    private void handlePlayerList(Msg.PlayerList p) {
        playerList.clear();
        playerList.addAll(p.players());
        Set<Integer> activeIds = new HashSet<>();
        for (var pi : p.players()) {
            activeIds.add(pi.id());
            if (pi.id() == myPlayerId) continue;
            if (!remoteCursors.containsKey(pi.id())) {
                RemoteCursor rc = new RemoteCursor(pi.name(),
                        Color.color(pi.r(), pi.g(), pi.b()));
                rc.addTo(gameScene.getCursorOverlay());
                remoteCursors.put(pi.id(), rc);
            }
        }
        var iter = remoteCursors.entrySet().iterator();
        while (iter.hasNext()) {
            var entry = iter.next();
            if (!activeIds.contains(entry.getKey())) {
                entry.getValue().removeFrom(gameScene.getCursorOverlay());
                iter.remove();
            }
        }
        if (myCursorImage != null && isMultiplayer) {
            sendNetworkMessage(NetworkMessage.of(new Msg.CursorImage(myPlayerId, myCursorImage)));
        }
    }

    private void handlePlayerLeave(Msg.PlayerLeave l) {
        var cursor = remoteCursors.remove(l.playerId());
        if (cursor != null) cursor.removeFrom(gameScene.getCursorOverlay());
    }

    private void handleOperatorStatus(Msg.OperatorStatus o) {
        if (o.playerId() != myPlayerId) return; // Ensure this is for the current player
        isOperator = o.isOperator();
        if (joinGameScene != null) {
            if (isOperator) {
                joinGameScene.getOperatorStatusLabel().setText("Operator ✓");
                joinGameScene.getOperatorStatusLabel().setStyle(Styles.mpSmallLabel());
            } else {
                joinGameScene.getOperatorStatusLabel().setText("Access denied");
                joinGameScene.getOperatorStatusLabel().setStyle("-fx-font-size: 12pt; -fx-text-fill: #c44;");
            }
        }
        if (gameScene != null) {
            Label inGame = gameScene.getNetworkStatusInGame();
            String current = inGame.getText();
            if (isOperator) {
                if (!current.contains(" [Operator]")) {
                    inGame.setText(current + " [Operator]");
                }
            } else {
                inGame.setText(current.replace(" [Operator]", ""));
            }
            inGame.setStyle(Styles.panelLabel());
            gameScene.updateOperatorButtonsVisibility(); // Update UI when status changes
        }
    }

    private void handleCursorMove(Msg.CursorMove m) {
        if (m.playerId() == myPlayerId) return;
        var cursor = remoteCursors.get(m.playerId());
        if (cursor != null) {
            cursor.setPosition(m.x(), m.y());
        }
    }

    private void handleCursorImage(Msg.CursorImage m) {
        if (m.playerId() == myPlayerId) return;
        var cursor = remoteCursors.get(m.playerId());
        if (cursor != null) {
            cursor.setImage(m.imageData());
        }
    }

    private void handlePieceMove(Msg.PieceMove m) {
        if (m.playerId() == myPlayerId) return;
        var pane = pieceMap.get(m.pieceId());
        if (pane != null) {
            pane.setTranslateX(m.x());
            pane.setTranslateY(m.y());
            pane.toFront();
        }
    }

    private void handlePieceRotate(Msg.PieceRotate m) {
        if (m.playerId() == myPlayerId) return;
        var pane = pieceMap.get(m.pieceId());
        if (pane != null) {
            pane.setRotate(m.rotation());
            pane.toFront();
        }
    }

    private void handlePieceFlip(Msg.PieceFlip m) {
        if (m.playerId() == myPlayerId) return;
        var pane = pieceMap.get(m.pieceId());
        if (pane != null && pane.getChildren().size() >= 3) {
            Node f = pane.getChildren().get(1);
            Node b = pane.getChildren().get(0);
            f.setVisible(m.frontVisible());
            b.setVisible(m.backVisible());
            if (pane.getChildren().size() > 2) {
                Node t = pane.getChildren().get(2);
                t.setVisible(m.textVisible());
            }
            pane.toFront();
        }
    }

    private void handlePieceToFront(Msg.PieceToFront m) {
        if (m.playerId() == myPlayerId) return;
        var pane = pieceMap.get(m.pieceId());
        if (pane != null) pane.toFront();
    }

    private void handleSpawnChip(Msg.SpawnChip m) {
        if (m.playerId() == myPlayerId) return;
        Color color = Color.color(m.red(), m.green(), m.blue(), m.opacity());
        Chips chip = new Chips(color, bwFrontImage, bwBackImage, m.pieceId());
        StackPane chipPane = chip.getChipPane();
        chipPane.setTranslateX(m.x());
        chipPane.setTranslateY(m.y());
        setupPieceInteractions(chipPane, chip.getPieceId(), true);
        chip.getChipPane().getChildren().get(0).setVisible(true);
        chip.getChipPane().getChildren().get(1).setVisible(false);
        chips.add(chip);
        gameScene.getGameContent().getChildren().add(chipPane);
        pieceMap.put(chip.getPieceId(), chipPane);
        gameScene.bringCursorOverlayToFront();
    }

    private void handleSpawnDie(Msg.SpawnDie m) {
        if (m.playerId() == myPlayerId) return;
        Color color = Color.color(m.red(), m.green(), m.blue(), m.opacity());
        Dice die = new Dice(m.sides(), color, m.pieceId());
        die.setCurrentValue((int) m.value());
        StackPane diePane = die.getPane();
        diePane.setTranslateX(m.x());
        diePane.setTranslateY(m.y());
        setupPieceInteractions(diePane, die.getPieceId(), false);
        dice.add(die);
        gameScene.getGameContent().getChildren().add(diePane);
        pieceMap.put(die.getPieceId(), diePane);
        gameScene.bringCursorOverlayToFront();
    }

    private void handleDeletePiece(Msg.DeletePiece m) {
        if (m.playerId() == myPlayerId) return;
        var pane = pieceMap.remove(m.pieceId());
        if (pane == null) return;
        gameScene.getGameContent().getChildren().remove(pane);
        chips.removeIf(c -> c.getPieceId().equals(m.pieceId()));
        dice.removeIf(d -> d.getPieceId().equals(m.pieceId()));
    }

    private void handleDieRoll(Msg.DieRoll m) {
        if (m.playerId() == myPlayerId) return;
        for (Dice die : dice) {
            if (die.getPieceId().equals(m.pieceId())) {
                die.setCurrentValue(m.value());
                break;
            }
        }
    }

    private void handleReshuffleCards(Msg.ReshuffleCards m) {
        if (m.playerId() == myPlayerId) return;
        if (isNotOperator(m.playerId())) return;
        for (int a = 0; a < NUM_CARDS; a++) {
            if (cards[a] != null) {
                StackPane cardPane = cards[a].getCardPane();
                cardPane.setTranslateX(DEFAULT_DECK_X);
                cardPane.setTranslateY(DEFAULT_DECK_Y);
                cardPane.setRotate(0);
                ImageView backView = (ImageView) cardPane.getChildren().get(0);
                ImageView frontView = (ImageView) cardPane.getChildren().get(1);
                Node textNode = cardPane.getChildren().get(2);
                backView.setVisible(true);
                frontView.setVisible(false);
                textNode.setVisible(false);
            }
        }
    }

    private void handleResetDice(Msg.ResetDice m) {
        if (m.playerId() == myPlayerId) return;
        if (isNotOperator(m.playerId())) return;
        for (Dice die : dice) {
            gameScene.getGameContent().getChildren().remove(die.getPane());
            pieceMap.remove(die.getPieceId());
        }
        dice.clear();
    }

    private void handleResetChips(Msg.ResetChips m) {
        if (m.playerId() == myPlayerId) return;
        if (isNotOperator(m.playerId())) return;
        for (Chips chip : chips) {
            gameScene.getGameContent().getChildren().remove(chip.getChipPane());
            pieceMap.remove(chip.getPieceId());
        }
        chips.clear();
    }

    private void handleNewGame(Msg.NewGame m) {
        if (m.playerId() == myPlayerId) return;
        if (isNotOperator(m.playerId())) return;
        if (isHost) {
            newGame();
        } else {
            for (Chips chip : chips) {
                gameScene.getGameContent().getChildren().remove(chip.getChipPane());
                pieceMap.remove(chip.getPieceId());
            }
            chips.clear();
            for (Dice die : dice) {
                gameScene.getGameContent().getChildren().remove(die.getPane());
                pieceMap.remove(die.getPieceId());
            }
            dice.clear();
            pieceMap.clear();
            for (int a = 0; a < NUM_CARDS; a++) {
                if (cards[a] != null) {
                    StackPane pane = cards[a].getCardPane();
                    pane.setTranslateX(DEFAULT_DECK_X);
                    pane.setTranslateY(DEFAULT_DECK_Y);
                    pane.setRotate(0);
                    pane.getChildren().get(0).setVisible(true);
                    pane.getChildren().get(1).setVisible(false);
                    if (pane.getChildren().size() > 2)
                        pane.getChildren().get(2).setVisible(false);
                }
            }
        }
    }

    private void handleCardNamesSync(Msg.CardNamesSync m) {
        var names = m.cardNames();
        if (names.size() == cardNames.size()) {
            cardNames.setAll(names);
            for (int i = 0; i < NUM_CARDS; i++) {
                if (cards[i] != null) {
                    String logicalName = cardNames.get(i);
                    Text text = cards[i].getCardName();
                    Matcher matcher = CARD_PATTERN.matcher(logicalName);
                    if (matcher.matches() && !wilds.contains(logicalName)) {
                        String value = matcher.group("value");
                        String suit = matcher.group("suit");
                        text.setText(Cards.getStyle(logicalName, value, suit, currentCardTheme).getText());
                        text.setStyle(Cards.getStyle(logicalName, value, suit, currentCardTheme).getStyle());
                    } else {
                        Text wildText = CardDataHelper.getWildCardName(new Text(logicalName + "\n \n" + "(Wild)"));
                        text.setText(wildText.getText());
                        text.setStyle(wildText.getStyle());
                    }
                    cards[i].refreshTooltipContent(logicalName, wilds);
                }
            }
        }
    }

    private void handleSendState(Msg.SendState m) {
        if (!isHost) return;
        gameServer.sendTo(m.playerId(), NetworkMessage.of(new Msg.CardNamesSync(new ArrayList<>(cardNames))));
        int nCards = cards.length;
        int[] cardIds = new int[nCards];
        double[] cardX = new double[nCards];
        double[] cardY = new double[nCards];
        double[] cardRot = new double[nCards];
        boolean[] cardBackVis = new boolean[nCards];
        boolean[] cardFrontVis = new boolean[nCards];
        boolean[] cardTextVis = new boolean[nCards];

        for (int i = 0; i < nCards; i++) {
            if (cards[i] == null) continue;
            var pane = cards[i].getCardPane();
            cardIds[i] = i;
            cardX[i] = pane.getTranslateX();
            cardY[i] = pane.getTranslateY();
            cardRot[i] = pane.getRotate();
            cardBackVis[i] = pane.getChildren().get(0).isVisible();
            cardFrontVis[i] = pane.getChildren().get(1).isVisible();
            cardTextVis[i] = pane.getChildren().size() > 2 && pane.getChildren().get(2).isVisible();
        }

        int nChips = chips.size();
        String[] chipIds = new String[nChips];
        double[] chipX = new double[nChips];
        double[] chipY = new double[nChips];
        double[] chipRot = new double[nChips];
        boolean[] chipFrontVis = new boolean[nChips];
        boolean[] chipBackVis = new boolean[nChips];
        double[] chipR = new double[nChips];
        double[] chipG = new double[nChips];
        double[] chipB = new double[nChips];
        double[] chipO = new double[nChips];

        for (int i = 0; i < nChips; i++) {
            var chip = chips.get(i);
            var pane = chip.getChipPane();
            chipIds[i] = chip.getPieceId();
            chipX[i] = pane.getTranslateX();
            chipY[i] = pane.getTranslateY();
            chipRot[i] = pane.getRotate();
            chipFrontVis[i] = pane.getChildren().get(0).isVisible();
            chipBackVis[i] = pane.getChildren().get(1).isVisible();
            var c = chip.getColor();
            chipR[i] = c.getRed();
            chipG[i] = c.getGreen();
            chipB[i] = c.getBlue();
            chipO[i] = c.getOpacity();
        }

        int nDice = dice.size();
        String[] dieIds = new String[nDice];
        double[] dieX = new double[nDice];
        double[] dieY = new double[nDice];
        double[] dieRot = new double[nDice];
        int[] dieSides = new int[nDice];
        int[] dieVals = new int[nDice];
        double[] dieR = new double[nDice];
        double[] dieG = new double[nDice];
        double[] dieB = new double[nDice];
        double[] dieO = new double[nDice];

        for (int i = 0; i < nDice; i++) {
            var die = dice.get(i);
            var pane = die.getPane();
            dieIds[i] = die.getPieceId();
            dieX[i] = pane.getTranslateX();
            dieY[i] = pane.getTranslateY();
            dieRot[i] = pane.getRotate();
            dieSides[i] = die.getSides();
            dieVals[i] = die.getCurrentValue();
            var c = die.getDieColor();
            dieR[i] = c.getRed();
            dieG[i] = c.getGreen();
            dieB[i] = c.getBlue();
            dieO[i] = c.getOpacity();
        }

        var sync = new Msg.StateSync(cardIds, cardX, cardY, cardRot, cardBackVis, cardFrontVis, cardTextVis,
                chipIds, chipX, chipY, chipRot, chipFrontVis, chipBackVis,
                chipR, chipG, chipB, chipO,
                dieIds, dieX, dieY, dieRot, dieSides, dieVals,
                dieR, dieG, dieB, dieO);

        if (gameServer != null) {
            gameServer.sendTo(m.playerId(), NetworkMessage.of(sync));
        }

        if (pokerTable != null) {
            sendPokerStateTo(m.playerId());
        }
    }

    private void handleStateSync(Msg.StateSync s) {
        for (Chips chip : chips) {
            gameScene.getGameContent().getChildren().remove(chip.getChipPane());
            pieceMap.remove(chip.getPieceId());
        }
        chips.clear();
        for (Dice die : dice) {
            gameScene.getGameContent().getChildren().remove(die.getPane());
            pieceMap.remove(die.getPieceId());
        }
        dice.clear();

        for (int i = 0; i < s.cardIds().length && i < cards.length; i++) {
            if (cards[i] == null) continue;
            var pane = cards[i].getCardPane();
            pane.setTranslateX(s.cardX()[i]);
            pane.setTranslateY(s.cardY()[i]);
            pane.setRotate(s.cardRot()[i]);
            if (pane.getChildren().size() >= 2) {
                pane.getChildren().get(0).setVisible(s.cardBackVis()[i]);
                pane.getChildren().get(1).setVisible(s.cardFrontVis()[i]);
            }
            if (pane.getChildren().size() > 2) {
                pane.getChildren().get(2).setVisible(s.cardTextVis()[i]);
            }
        }

        for (int i = 0; i < s.chipIds().length; i++) {
            Color color = Color.color(s.chipR()[i], s.chipG()[i], s.chipB()[i], s.chipO()[i]);
            Chips chip = new Chips(color, bwFrontImage, bwBackImage, s.chipIds()[i]);
            StackPane chipPane = chip.getChipPane();
            chipPane.setTranslateX(s.chipX()[i]);
            chipPane.setTranslateY(s.chipY()[i]);
            chipPane.setRotate(s.chipRot()[i]);
            chipPane.getChildren().get(0).setVisible(s.chipFrontVis()[i]);
            chipPane.getChildren().get(1).setVisible(s.chipBackVis()[i]);
            setupPieceInteractions(chipPane, chip.getPieceId(), true);
            chips.add(chip);
            gameScene.getGameContent().getChildren().add(chipPane);
            pieceMap.put(chip.getPieceId(), chipPane);
        }

        for (int i = 0; i < s.dieIds().length; i++) {
            Color color = Color.color(s.dieR()[i], s.dieG()[i], s.dieB()[i], s.dieO()[i]);
            Dice die = new Dice(s.dieSides()[i], color, s.dieIds()[i]);
            die.setCurrentValue(s.dieVals()[i]);
            StackPane diePane = die.getPane();
            diePane.setTranslateX(s.dieX()[i]);
            diePane.setTranslateY(s.dieY()[i]);
            diePane.setRotate(s.dieRot()[i]);
            setupPieceInteractions(diePane, die.getPieceId(), false);
            dice.add(die);
            gameScene.getGameContent().getChildren().add(diePane);
            pieceMap.put(die.getPieceId(), diePane);
        }

        gameScene.bringCursorOverlayToFront();
    }

    private void setupPieceInteractions(StackPane pane, String pieceId, boolean isChipOrCard) {
        UIUtils.makeDraggable(pane, this,
                (x, y) -> sendPieceMove(pieceId, x, y),
                (x, y) -> sendPieceMoveThrottled(pieceId, x, y),
                (pile, x, y) -> {
                    for (StackPane p : pile) {
                        p.setTranslateX(x);
                        p.setTranslateY(y);
                        sendPieceMoveThrottled(getCardId(p), x, y);
                    }
                }
        );

        if (isChipOrCard) {
            boolean isCard = pieceId.startsWith("card:");
            UIUtils.makeFlippableAndRotatable(pane, !isCard,
                    (type, val) -> {
                        if ("rotate".equals(type)) sendPieceRotate(pieceId, val);
                        else if ("flip".equals(type)) {
                            int frontIdx = isCard ? 1 : 0;
                            int backIdx = isCard ? 0 : 1;
                            sendPieceFlip(pieceId,
                                    pane.getChildren().get(frontIdx).isVisible(),
                                    pane.getChildren().get(backIdx).isVisible(),
                                    pane.getChildren().size() > 2 && pane.getChildren().get(2).isVisible());
                        }
                    },
                    () -> sendPieceToFront(pieceId)
            );
        }
        this.makeDiscardable(pane, gameScene.getDiscardZone(), pieceId);
    }

    public void sendPieceMove(String pieceId, double x, double y) {
        if (!isMultiplayer) return;
        sendNetworkMessage(NetworkMessage.of(new Msg.PieceMove(myPlayerId, pieceId, x, y)));
    }

    private void sendPieceMoveThrottled(String pieceId, double x, double y) {
        if (!isMultiplayer) return;
        long now = System.currentTimeMillis();
        Long last = lastPieceMoveTime.get(pieceId);
        if (last == null || now - last > 50) {
            lastPieceMoveTime.put(pieceId, now);
            sendNetworkMessage(NetworkMessage.of(new Msg.PieceMove(myPlayerId, pieceId, x, y)));
        }
    }

    private void sendPieceRotate(String pieceId, double rotation) {
        if (!isMultiplayer) return;
        sendNetworkMessage(NetworkMessage.of(new Msg.PieceRotate(myPlayerId, pieceId, rotation)));
    }

    public void sendPieceFlip(String pieceId, boolean frontVis, boolean backVis, boolean textVis) {
        if (!isMultiplayer) return;
        sendNetworkMessage(NetworkMessage.of(new Msg.PieceFlip(myPlayerId, pieceId, frontVis, backVis, textVis)));
    }

    private void sendPieceToFront(String pieceId) {
        if (!isMultiplayer) return;
        sendNetworkMessage(NetworkMessage.of(new Msg.PieceToFront(myPlayerId, pieceId)));
    }

    private Image loadImage(String customPath, String themeDefinedPath, ThemeConfiguration theme) {
        Image originalImage = null;
        String finalPath;

        if (customPath != null && !customPath.trim().isEmpty()) {
            finalPath = customPath;
            try {
                File file = new File(finalPath);
                if (file.exists()) {
                    originalImage = new Image(file.toURI().toString());
                } else {
                    System.err.println("ERROR: Custom image file not found: " + finalPath);
                }
            } catch (Exception e) {
                System.err.println("ERROR: Failed to load custom image from file: " + finalPath + ". Falling back to theme-defined path. Error: " + e.getMessage());
            }
        }

        if (originalImage == null && themeDefinedPath != null && !themeDefinedPath.trim().isEmpty()) {
            if (themeDefinedPath.startsWith("/")) {
                finalPath = themeDefinedPath;
                try {
                    InputStream is = getClass().getResourceAsStream(finalPath);
                    if (is != null) {
                        originalImage = new Image(is);
                    } else {
                        System.err.println("ERROR: Theme-defined resource not found: " + finalPath);
                    }
                } catch (Exception e) {
                    System.err.println("ERROR: Failed to load theme-defined image from resource: " + finalPath + ". Error: " + e.getMessage());
                }
            } else {
                String bp = theme.getBasePath();
                if (bp != null && bp.startsWith("./")) {
                    finalPath = "/" + bp.substring(2) + themeDefinedPath;
                    try {
                        InputStream is = getClass().getResourceAsStream(finalPath);
                        if (is != null) {
                            originalImage = new Image(is);
                        } else {
                            System.err.println("ERROR: Theme-defined resource not found: " + finalPath);
                        }
                    } catch (Exception e) {
                        System.err.println("ERROR: Failed to load theme-defined image from resource: " + finalPath + ". Error: " + e.getMessage());
                    }
                } else {
                    if (bp != null) {
                        finalPath = Paths.get(bp, themeDefinedPath).toString();
                    } else {
                        finalPath = themeDefinedPath;
                    }

                    try {
                        File file = new File(finalPath);
                        if (file.exists()) {
                            originalImage = new Image(file.toURI().toString());
                        } else {
                            System.err.println("ERROR: Theme-defined file not found: " + finalPath);
                        }
                    } catch (Exception e) {
                        System.err.println("ERROR: Failed to load theme-defined image from file system: " + finalPath + ". Error: " + e.getMessage());
                    }
                }
            }
        }

        if (originalImage == null) {
            System.err.println("ERROR: No image could be loaded. Custom path: " + customPath + ", Theme-defined path: " + themeDefinedPath);
            return null;
        }

        return originalImage;
    }

    private void updateBackground(Pane bg) {
        Image backgroundImage = loadImage(customBackgroundPath, currentCardTheme.getBackgroundPath(), currentCardTheme);
        if (backgroundImage != null) {
            bg.setBackground(new Background(new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(1.0, 1.0, true, true, false, false))));
        } else {
            System.err.println("WARNING: Background image could not be loaded. Using default/no background.");
            bg.setBackground(Background.EMPTY);
        }
    }

    private void updateStartSceneBackground() {
        setLayoutBackground(startScene.getStartBg(), startScene.getScene());
    }

    private void updateMultiplayerSceneBackground() {
        setLayoutBackground(multiplayerScene.getMpBg(), multiplayerScene.getScene());
    }

    private void updateHostGameSceneBackground() {
        hostGameScene.updateOnlineStatus();
        setLayoutBackground(hostGameScene.getMpBg(), hostGameScene.getScene());
    }

    private void updateJoinGameSceneBackground() {
        joinGameScene.updateOnlineStatus();
        setLayoutBackground(joinGameScene.getMpBg(), joinGameScene.getScene());
    }

    private void setLayoutBackground(Pane layout, Scene scene) {
        if (layout != null && scene != null) {
            Image backgroundImage = loadImage(customBackgroundPath, currentCardTheme.getBackgroundPath(), currentCardTheme);
            if (backgroundImage != null) {
                layout.setBackground(new Background(new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(1.0, 1.0, true, true, false, false))));
            } else {
                System.err.println("WARNING: Scene background image could not be loaded.");
                layout.setBackground(Background.EMPTY);
            }
        }
    }

    private void loadAndCreateCards() {
        if (cards != null) {
            for (Cards card : cards) {
                if (card != null) {
                    gameScene.getGameContent().getChildren().remove(card.getCardPane());
                }
            }
        }

        Image cardFrontImage = loadImage(customCardFrontPath, currentCardTheme.getCardFrontPath(), currentCardTheme);
        Image cardBackImage = loadImage(customCardBackPath, currentCardTheme.getCardBackPath(), currentCardTheme);

        if (cardFrontImage == null || cardBackImage == null) {
            System.err.println("ERROR: Card images could not be loaded. Cannot create cards.");
            return;
        }

        System.out.println("Adding " + NUM_CARDS + " cards to the board");
        System.out.println(suits.size() + " Suits, " + values.size() + " Values per suit, and " + wilds.size() + " Wilds");

        for (int i = 0; i < NUM_CARDS; i++) {
            Cards card;
            String cardLogicalName = cardNames.get(i);

            Matcher matcher = CARD_PATTERN.matcher(cardLogicalName);
            if (matcher.matches() && !wilds.contains(cardLogicalName)) {
                String value = matcher.group("value");
                String suit = matcher.group("suit");
                card = new Cards(cardLogicalName, value, suit, CARD_WIDTH, CARD_HEIGHT, cardFrontImage, cardBackImage, currentCardTheme, wilds);
            } else {
                card = new Cards(cardLogicalName, "", "", CARD_WIDTH, CARD_HEIGHT, cardFrontImage, cardBackImage, currentCardTheme, wilds);
            }

            Text cardNameText = CardDataHelper.getWildCardName(new Text(cardLogicalName + "\n \n" + "(Wild)"));
            ((Text) card.getCardPane().getChildren().get(2)).setText(cardNameText.getText());
            card.getCardPane().getChildren().get(2).setStyle(cardNameText.getStyle());
            card.refreshTooltipContent(cardLogicalName, wilds);

            String pieceId = "card:" + i;
            cards[i] = card;

            cards[i].getCardPane().setTranslateX(DEFAULT_DECK_X);
            cards[i].getCardPane().setTranslateY(DEFAULT_DECK_Y);
            setupPieceInteractions(cards[i].getCardPane(), pieceId, true);
            gameScene.getGameContent().getChildren().add(cards[i].getCardPane());
            pieceMap.put(pieceId, cards[i].getCardPane());
        }
    }

    /**
     * Removes all chips from the board and notifies connected peers.
     */
    public void resetChips() {
        for (Chips chip : chips) {
            gameScene.getGameContent().getChildren().remove(chip.getChipPane());
            pieceMap.remove(chip.getPieceId());
        }
        chips.clear();
        sendNetworkMessage(NetworkMessage.of(new Msg.ResetChips(myPlayerId)));
    }

    /**
     * Removes all dice from the board and notifies connected peers.
     */
    public void resetDice() {
        for (Dice die : dice) {
            gameScene.getGameContent().getChildren().remove(die.getPane());
            pieceMap.remove(die.getPieceId());
        }
        dice.clear();
        sendNetworkMessage(NetworkMessage.of(new Msg.ResetDice(myPlayerId)));
    }

    private void makeDiscardable(Pane pane, StackPane discardZone, String pieceId) {
        pane.setOnMouseReleased(event -> {
            double sx = event.getSceneX();
            double sy = event.getSceneY();
            var bounds = discardZone.localToScene(discardZone.getBoundsInLocal());

            if (isMultiplayer && pieceId != null) {
                sendPieceMove(pieceId, pane.getTranslateX(), pane.getTranslateY());
            }

            if (bounds.contains(sx, sy)) {
                boolean isCard = false;
                for (Cards card : cards) {
                    if (card != null && card.getCardPane() == pane) {
                        pane.setTranslateX(DEFAULT_DECK_X);
                        pane.setTranslateY(DEFAULT_DECK_Y);
                        pane.setRotate(0);
                        ImageView backView = (ImageView) pane.getChildren().get(0);
                        ImageView frontView = (ImageView) pane.getChildren().get(1);
                        Node textNode = pane.getChildren().size() > 2 ? pane.getChildren().get(2) : null;
                        backView.setVisible(true);
                        frontView.setVisible(false);
                        if (textNode != null) {
                            textNode.setVisible(false);
                        }
                        pane.toBack();
                        isCard = true;
                        if (isMultiplayer && pieceId != null) {
                            sendNetworkMessage(NetworkMessage.of(new Msg.PieceMove(myPlayerId, pieceId, DEFAULT_DECK_X, DEFAULT_DECK_Y)));
                            sendPieceFlip(pieceId, false, true, false);
                            sendPieceRotate(pieceId, 0);
                        }
                        break;
                    }
                }
                if (isCard) return;

                for (int i = 0; i < dice.size(); i++) {
                    if (dice.get(i).getPane() == pane) {
                        gameScene.getGameContent().getChildren().remove(pane);
                        pieceMap.remove(dice.get(i).getPieceId());
                        sendNetworkMessage(NetworkMessage.of(new Msg.DeletePiece(myPlayerId, dice.get(i).getPieceId())));
                        dice.remove(i);
                        return;
                    }
                }

                int idx = -1;
                for (int i = 0; i < chips.size(); i++) {
                    if (chips.get(i).getChipPane() == pane) {
                        idx = i;
                        break;
                    }
                }
                if (idx >= 0) {
                    gameScene.getGameContent().getChildren().remove(pane);
                    pieceMap.remove(chips.get(idx).getPieceId());
                    sendNetworkMessage(NetworkMessage.of(new Msg.DeletePiece(myPlayerId, chips.get(idx).getPieceId())));
                    chips.remove(idx);
                }
            }
        });
    }

    /**
     * Reshuffles all card names back into the deck and resets card positions.
     * Only the host or a single-player session may perform the reshuffle.
     */
    public void reshuffleCards() {
        if (isHost || !isMultiplayer) {
            CardDataHelper.generateShuffledCardNames(cardNames);
            reshuffled = true;

            for (int a = 0; a < NUM_CARDS; a++) {
                if (cards[a] != null) {
                    StackPane cardPane = cards[a].getCardPane();
                    Text cardNameText = cards[a].getCardName();
                    String cardLogicalName = cardNames.get(a);
                    Matcher matcher = CARD_PATTERN.matcher(cardLogicalName);

                    cardPane.setTranslateX(DEFAULT_DECK_X);
                    cardPane.setTranslateY(DEFAULT_DECK_Y);
                    cardPane.getTransforms().removeAll(cardPane.getTransforms());
                    cardPane.setRotate(0);

                    ImageView backView = (ImageView) cardPane.getChildren().get(0);
                    ImageView frontView = (ImageView) cardPane.getChildren().get(1);
                    Node textNode = cardPane.getChildren().get(2);
                    backView.setVisible(true);
                    frontView.setVisible(false);
                    textNode.setVisible(false);

                    if (matcher.matches() && !wilds.contains(cardLogicalName)) {
                        String value = matcher.group("value");
                        String suit = matcher.group("suit");
                        cardNameText.setText(Cards.getStyle(cardLogicalName, value, suit, currentCardTheme).getText());
                        cardNameText.setStyle(Cards.getStyle(cardLogicalName, value, suit, currentCardTheme).getStyle());
                    } else {
                        Text text = new Text(cardLogicalName);
                        cardNameText.setText(CardDataHelper.getWildCardName(text).getText() + "\n \n" + "(Wild)");
                        cardNameText.setStyle(CardDataHelper.getWildCardName(text).getStyle());
                    }

                    cards[a].refreshTooltipContent(cardLogicalName, wilds);

                    String pieceId = "card:" + a;
                    setupPieceInteractions(cards[a].getCardPane(), pieceId, true);
                }
            }
            reshuffled = false;
            sendNetworkMessage(NetworkMessage.of(new Msg.ReshuffleCards(myPlayerId)));
            sendNetworkMessage(NetworkMessage.of(new Msg.CardNamesSync(new ArrayList<>(cardNames))));
        } else {
            sendNetworkMessage(NetworkMessage.of(new Msg.ReshuffleCards(myPlayerId)));
        }
    }

    /**
     * Loads a saved game from disk and restores the board state or re-enters multiplayer setup.
     */
    public void continueGame() {
        String saveFile = PlatformPaths.getSaveFilePath();
        File file = new File(saveFile);
        if (!file.exists()) return;

        SaveData save;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFile))) {
            save = (SaveData) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading save: " + e.getMessage());
            return;
        }

        if (save.isMultiplayer()) {
            hostGameScene.getHostPortField().setText(String.valueOf(save.serverPort()));
            if (save.serverIp() != null && !save.serverIp().isEmpty()) {
                joinGameScene.getJoinIpField().setText(save.serverIp());
                joinGameScene.getJoinPortField().setText(String.valueOf(save.serverPort()));
                joinGameScene.getNetworkStatusLabel().setText("Saved multiplayer session - re-join");
                joinGameScene.getNetworkStatusLabel().setStyle("-fx-font-size: 14pt; -fx-text-fill: #FFA500;");
                switchToJoinGame();
            } else {
                hostGameScene.getNetworkStatusLabel().setText("Saved multiplayer session - re-host");
                hostGameScene.getNetworkStatusLabel().setStyle("-fx-font-size: 14pt; -fx-text-fill: #FFA500;");
                switchToHostGame();
            }
        } else {
            loadGame(save);
            primaryStage.setScene(gameScene.getScene());
            primaryStage.setTitle("Game Scene");
        }
    }

    /**
     * Switches the scene to the multiplayer setup screen.
     */
    public void switchToMultiplayer() {
        updateMultiplayerSceneBackground();
        primaryStage.setScene(multiplayerScene.getScene());
        primaryStage.setTitle("Multiplayer");
    }

    public void switchToHostGame() {
        updateHostGameSceneBackground();
        hostGameScene.updateOnlineStatus();
        primaryStage.setScene(hostGameScene.getScene());
        primaryStage.setTitle("Host Game");
    }

    public void switchToJoinGame() {
        updateJoinGameSceneBackground();
        joinGameScene.updateOnlineStatus();
        primaryStage.setScene(joinGameScene.getScene());
        primaryStage.setTitle("Join Game");
    }

    public void switchToStart() {
        if (hostGameScene != null) {
            hostGameScene.getNetworkStatusLabel().setText("Offline");
            hostGameScene.getNetworkStatusLabel().setStyle(Styles.mpLabel());
        }
        if (joinGameScene != null) {
            joinGameScene.getNetworkStatusLabel().setText("Offline");
            joinGameScene.getNetworkStatusLabel().setStyle(Styles.mpLabel());
        }
        primaryStage.setScene(startScene.getScene());
        primaryStage.setTitle("TarotBoard");
    }

    /**
     * Spawns a new die with the given number of sides and color at the center of the board.
     *
     * @param sides    the number of die faces
     * @param dieColor the color of the die
     */
    public void spawnDie(int sides, Color dieColor) {
        Dice die = new Dice(sides, dieColor);
        StackPane diePane = die.getPane();
        setupPieceInteractions(diePane, die.getPieceId(), false);

        int randomX = new Random().nextInt(-5, 5);
        int randomY = new Random().nextInt(-5, 5);
        diePane.setTranslateX((gameScene.getScene().getWidth() / 2) + randomX);
        diePane.setTranslateY((gameScene.getScene().getHeight() / 2) + randomY);
        gameScene.getGameContent().getChildren().add(diePane);
        dice.add(die);
        pieceMap.put(die.getPieceId(), diePane);
        gameScene.bringCursorOverlayToFront();

        sendNetworkMessage(NetworkMessage.of(new Msg.SpawnDie(myPlayerId, die.getPieceId(),
                diePane.getTranslateX(), diePane.getTranslateY(),
                sides, die.getCurrentValue(),
                dieColor.getRed(), dieColor.getGreen(), dieColor.getBlue(), dieColor.getOpacity())));
    }

    /**
     * Spawns a new chip with the given color at a random offset near the center of the board.
     *
     * @param chipColor the color of the chip
     */
    public void spawnChip(Color chipColor) {
        Chips chip = new Chips(chipColor, bwFrontImage, bwBackImage);
        StackPane chipPane = chip.getChipPane();

        int randomX = new Random().nextInt(-5, 5);
        int randomY = new Random().nextInt(-5, 5);
        chipPane.setTranslateX((gameScene.getScene().getWidth() / 2) + randomX);
        chipPane.setTranslateY((gameScene.getScene().getHeight() / 2) + randomY);

        setupPieceInteractions(chipPane, chip.getPieceId(), true);

        chips.add(chip);
        gameScene.getGameContent().getChildren().add(chipPane);
        pieceMap.put(chip.getPieceId(), chipPane);
        gameScene.bringCursorOverlayToFront();

        sendNetworkMessage(NetworkMessage.of(new Msg.SpawnChip(myPlayerId, chip.getPieceId(),
                chipPane.getTranslateX(), chipPane.getTranslateY(),
                chipColor.getRed(), chipColor.getGreen(), chipColor.getBlue(), chipColor.getOpacity())));
    }

    /**
     * Clears all pieces from the board, generates a fresh deck of cards, and applies
     * the current theme. Only the host or a single-player session may start a new game.
     */
    public void newGame() {
        if (isHost || !isMultiplayer) {
            if (cards != null) {
                for (Cards card : cards) {
                    if (card != null) {
                        gameScene.getGameContent().getChildren().remove(card.getCardPane());
                    }
                }
            }
            for (Chips chip : chips) {
                gameScene.getGameContent().getChildren().remove(chip.getChipPane());
            }
            chips.clear();

            for (Dice die : dice) {
                gameScene.getGameContent().getChildren().remove(die.getPane());
            }
            dice.clear();
            pieceMap.clear();

            cards = new Cards[NUM_CARDS];
            loadAndCreateCards();

            CardDataHelper.generateShuffledCardNames(cardNames);
            reshuffled = true;

            for (int a = 0; a < NUM_CARDS; a++) {
                if (cards[a] != null) {
                    Text cardNameText = cards[a].getCardName();
                    String cardLogicalName = cardNames.get(a);
                    Matcher matcher = CARD_PATTERN.matcher(cardLogicalName);
                    if (matcher.matches() && !wilds.contains(cardLogicalName)) {
                        String value = matcher.group("value");
                        String suit = matcher.group("suit");
                        cardNameText.setText(Cards.getStyle(cardLogicalName, value, suit, currentCardTheme).getText());
                        cardNameText.setStyle(Cards.getStyle(cardLogicalName, value, suit, currentCardTheme).getStyle());
                    } else {
                        Text text = new Text(cardLogicalName);
                        cardNameText.setText(CardDataHelper.getWildCardName(text).getText() + "\n \n" + "(Wild)");
                        cardNameText.setStyle(CardDataHelper.getWildCardName(text).getStyle());
                    }
                    cards[a].refreshTooltipContent(cardLogicalName, wilds);
                }
            }

            reshuffled = false;
            applyCurrentTheme();
            sendNetworkMessage(NetworkMessage.of(new Msg.NewGame(myPlayerId)));
        } else {
            sendNetworkMessage(NetworkMessage.of(new Msg.NewGame(myPlayerId)));
        }
    }

    // --- Poker Mode ---

    /**
     * Enters Poker Mode: creates a fresh poker table if this process is the authority
     * (host or single-player), or requests the host to do so otherwise. The existing sandbox
     * cards/chips/dice are left untouched.
     */
    public void startPokerMode() {
        if (isPokerAuthority()) {
            pokerTable = new PokerTable(POKER_ANTE, POKER_MIN_RAISE, wilds, values);
            pokerNextBotId = -1;
            refreshPokerDisplayFromTable();
            broadcastPokerState();
        }
        sendNetworkMessage(NetworkMessage.of(new Msg.SetupGame(myPlayerId, "POKER")));
        refreshPokerUI();
    }

    /**
     * Claims a poker seat for the local player.
     */
    public void pokerSitDown() {
        if (isPokerAuthority()) {
            if (pokerTable == null) return;
            pokerTable.sitDown(myPlayerId, false, POKER_STARTING_BANKROLL);
            refreshPokerDisplayFromTable();
            broadcastPokerState();
        } else {
            sendNetworkMessage(NetworkMessage.of(new Msg.PokerSitDown(myPlayerId)));
        }
        refreshPokerUI();
    }

    /**
     * Adds an AI-controlled seat. Only the poker authority or an operator may do this.
     */
    public void pokerAddBot() {
        if (isPokerAuthority()) {
            if (pokerTable == null) return;
            pokerTable.sitDown(pokerNextBotId--, true, POKER_STARTING_BANKROLL);
            refreshPokerDisplayFromTable();
            broadcastPokerState();
        } else {
            sendNetworkMessage(NetworkMessage.of(new Msg.PokerAddBot(myPlayerId)));
        }
        refreshPokerUI();
    }

    /**
     * Deals a new poker hand. Only the poker authority or an operator may do this.
     */
    public void pokerStartHand() {
        if (isPokerAuthority()) {
            startPokerHandAuthoritative();
        } else {
            sendNetworkMessage(NetworkMessage.of(new Msg.PokerStartHand(myPlayerId)));
        }
    }

    /**
     * Submits a betting action for the local player's turn.
     *
     * @param action one of {@code "CALL"}, {@code "RAISE"}, or {@code "FOLD"}
     * @param amount the additional amount to commit this action (ignored for FOLD)
     */
    public void pokerAction(String action, long amount) {
        if (isPokerAuthority()) {
            applyPokerActionAuthoritative(myPlayerId, action, amount);
        } else {
            sendNetworkMessage(NetworkMessage.of(new Msg.PokerAction(myPlayerId, action, amount)));
        }
    }

    private void startPokerHandAuthoritative() {
        if (pokerTable == null) return;
        try {
            pokerTable.startHand(buildPokerDeck());
        } catch (IllegalStateException e) {
            System.out.println("[TarotBoard] Cannot start poker hand: " + e.getMessage());
            return;
        }
        for (PokerTable.Seat seat : pokerTable.seats()) {
            if (seat.playerId() == myPlayerId) {
                pokerClientState.myHoleCards = seat.holeCards();
            } else if (!seat.isBot() && seat.active() && isHost) {
                sendPokerDealPrivateTo(seat.playerId(), seat.holeCards());
            }
        }
        refreshPokerDisplayFromTable();
        broadcastPokerState();
        refreshPokerUI();
        schedulePokerBotTurnIfNeeded();
    }

    private Deque<String> buildPokerDeck() {
        List<String> deck = new ArrayList<>(wilds);
        for (String suit : suits) {
            for (String value : values) {
                deck.add(value + " of " + suit);
            }
        }
        Collections.shuffle(deck);
        return new ArrayDeque<>(deck);
    }

    private void sendPokerDealPrivateTo(int playerId, List<String> holeCards) {
        if (gameServer != null) {
            gameServer.sendTo(playerId,
                    NetworkMessage.of(new Msg.PokerDealPrivate(playerId, new ArrayList<>(holeCards))));
        }
    }

    private void applyPokerActionAuthoritative(int playerId, String action, long amount) {
        if (pokerTable == null || pokerTable.phase() != PokerTable.Phase.BETTING) return;
        if (!pokerTable.applyAction(playerId, action, amount)) {
            System.out.println("[TarotBoard] Rejected poker action " + action + " from player " + playerId);
            return;
        }
        refreshPokerDisplayFromTable();
        broadcastPokerState();
        refreshPokerUI();
        if (pokerTable.phase() == PokerTable.Phase.SHOWDOWN) {
            broadcastPokerShowdown();
            pokerScene.showPokerShowdown(pokerClientState.lastShowdown);
        } else {
            schedulePokerBotTurnIfNeeded();
        }
    }

    private void schedulePokerBotTurnIfNeeded() {
        if (pokerTable == null || pokerTable.phase() != PokerTable.Phase.BETTING) return;
        Integer turnPlayerId = pokerTable.currentTurnPlayerId();
        if (turnPlayerId == null) return;
        PokerTable.Seat seat = pokerTable.seats().stream()
                .filter(s -> s.playerId() == turnPlayerId).findFirst().orElse(null);
        if (seat == null || !seat.isBot()) return;
        pokerBotScheduler.schedule(() -> Platform.runLater(() -> {
            if (pokerTable == null || pokerTable.phase() != PokerTable.Phase.BETTING) return;
            Integer current = pokerTable.currentTurnPlayerId();
            if (current == null || current != seat.playerId()) return;
            PokerBotAI.BotDecision decision = PokerBotAI.decide(seat.holeCards(), wilds, values,
                    pokerTable.currentBet(), seat.contributionThisRound(), seat.bankroll(), POKER_MIN_RAISE,
                    new Random());
            applyPokerActionAuthoritative(seat.playerId(), decision.action(), decision.amount());
        }), POKER_BOT_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    private void refreshPokerDisplayFromTable() {
        if (pokerTable == null) return;
        pokerClientState.pokerModeActive = true;
        List<PokerClientState.SeatView> views = new ArrayList<>();
        for (PokerTable.Seat s : pokerTable.seats()) {
            views.add(new PokerClientState.SeatView(s.playerId(), s.isBot(), s.folded(), s.active(),
                    s.holeCards().size(), s.bankroll(), s.contributionThisRound()));
        }
        pokerClientState.seats = views;
        Integer turn = pokerTable.currentTurnPlayerId();
        pokerClientState.currentTurnPlayerId = turn == null ? -1 : turn;
        pokerClientState.potTotal = pokerTable.potTotal();
        pokerClientState.currentBet = pokerTable.currentBet();
        pokerClientState.phase = pokerTable.phase();
        pokerClientState.lastShowdown = pokerTable.lastShowdown();
    }

    private void broadcastPokerState() {
        if (pokerTable == null) return;
        sendPokerStateTo(null);
    }

    private void sendPokerStateTo(Integer onlyToPlayerId) {
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
        if (onlyToPlayerId != null) {
            if (gameServer != null) gameServer.sendTo(onlyToPlayerId, NetworkMessage.of(sync));
        } else {
            sendNetworkMessage(NetworkMessage.of(sync));
        }
    }

    private void broadcastPokerShowdown() {
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
        sendNetworkMessage(NetworkMessage.of(msg));
    }

    private void handleSetupGame(Msg.SetupGame m) {
        if (!"POKER".equals(m.gameType()) || m.playerId() == myPlayerId) return;
        if (isHost) {
            if (isNotOperator(m.playerId())) return;
            pokerTable = new PokerTable(POKER_ANTE, POKER_MIN_RAISE, wilds, values);
            pokerNextBotId = -1;
            refreshPokerDisplayFromTable();
            broadcastPokerState();
        }
        refreshPokerUI();
    }

    private void handlePokerSitDown(Msg.PokerSitDown m) {
        if (!isHost || pokerTable == null) return;
        pokerTable.sitDown(m.playerId(), false, POKER_STARTING_BANKROLL);
        refreshPokerDisplayFromTable();
        broadcastPokerState();
    }

    private void handlePokerAddBot(Msg.PokerAddBot m) {
        if (!isHost || pokerTable == null) return;
        if (isNotOperator(m.playerId())) return;
        pokerTable.sitDown(pokerNextBotId--, true, POKER_STARTING_BANKROLL);
        refreshPokerDisplayFromTable();
        broadcastPokerState();
    }

    private void handlePokerStartHand(Msg.PokerStartHand m) {
        if (!isHost) return;
        if (isNotOperator(m.playerId())) return;
        startPokerHandAuthoritative();
    }

    private void handlePokerAction(Msg.PokerAction m) {
        if (!isHost) return;
        applyPokerActionAuthoritative(m.playerId(), m.action(), m.amount());
    }

    private void handlePokerStateSync(Msg.PokerStateSync s) {
        pokerClientState.pokerModeActive = true;
        List<PokerClientState.SeatView> views = new ArrayList<>();
        for (int i = 0; i < s.seatPlayerIds().length; i++) {
            views.add(new PokerClientState.SeatView(s.seatPlayerIds()[i], s.seatIsBot()[i], s.seatFolded()[i],
                    s.seatActive()[i], s.seatHoleCardCount()[i], s.seatBankroll()[i], s.seatContribution()[i]));
        }
        pokerClientState.seats = views;
        pokerClientState.currentTurnPlayerId = s.currentTurnPlayerId();
        pokerClientState.potTotal = s.potTotal();
        pokerClientState.currentBet = s.currentBet();
        pokerClientState.phase = PokerTable.Phase.valueOf(s.phase());
        refreshPokerUI();
    }

    private void handlePokerDealPrivate(Msg.PokerDealPrivate m) {
        if (m.playerId() != myPlayerId) return;
        pokerClientState.myHoleCards = new ArrayList<>(m.holeCards());
        refreshPokerUI();
    }

    private void handlePokerShowdownResult(Msg.PokerShowdownResult m) {
        List<PokerTable.ShowdownEntry> entries = new ArrayList<>();
        for (int i = 0; i < m.seatPlayerIds().length; i++) {
            HandType type = HandType.valueOf(m.handTypeNames().get(i));
            HandResult hand = new HandResult(type, 0, List.of(), CardCategory.NEUTRAL, m.scoresAwarded()[i],
                    new ArrayList<>(m.revealedHoleCards().get(i)));
            entries.add(new PokerTable.ShowdownEntry(m.seatPlayerIds()[i], hand));
        }
        List<Integer> winningIds = Arrays.stream(m.winningPlayerIds()).boxed().toList();
        pokerClientState.lastShowdown = new PokerTable.ShowdownResult(entries, winningIds, m.potWon());
        pokerScene.showPokerShowdown(pokerClientState.lastShowdown);
    }

    /**
     * Applies the given theme configuration to the board, updating card art,
     * chip images, and all scene backgrounds.
     *
     * @param theme the theme to apply
     */
    public void applyCurrentTheme(ThemeConfiguration theme) {
        currentCardTheme = theme;
        ThemeManager.setActiveTheme(theme);
        customCardFrontPath = null;
        customCardBackPath = null;
        customChipFrontPath = null;
        customChipBackPath = null;
        customBackgroundPath = null;
        applyCurrentTheme();
    }

    /**
     * Re-applies the currently active theme to all scenes, chip images, and card images.
     */
    public void applyCurrentTheme() {
        updateBackground(gameScene.getGameBg());
        updateStartSceneBackground();
        updateMultiplayerSceneBackground();
        updateHostGameSceneBackground();
        updateJoinGameSceneBackground();

        bwFrontImage = loadImage(customChipFrontPath, currentCardTheme.getChipFrontPath(), currentCardTheme);
        bwBackImage = loadImage(customChipBackPath, currentCardTheme.getChipBackPath(), currentCardTheme);

        for (Chips chip : chips) {
            chip.updateImages(bwFrontImage, bwBackImage);
        }

        Image cardFrontImage = loadImage(customCardFrontPath, currentCardTheme.getCardFrontPath(), currentCardTheme);
        Image cardBackImage = loadImage(customCardBackPath, currentCardTheme.getCardBackPath(), currentCardTheme);

        if (cards != null) {
            for (Cards card : cards) {
                if (card != null) {
                    card.updateImages(cardFrontImage, cardBackImage);
                    String cardName = card.getCardName().getText();
                    card.refreshTooltipContent(cardName, wilds);
                }
            }
        }
    }

    /**
     * Called when the application is shutting down.
     * Saves the current game state and cleans up network connections.
     */
    @Override
    public void stop() {
        saveGame();
        leaveGame();
    }

    /**
     * Saves the current game state (card positions, chip/die states, theme, multiplayer info)
     * to the application's saved game file.
     */
    private void saveGame() {
        if (cards == null) return;

        String saveFile = PlatformPaths.getSaveFilePath();
        try {
            Files.createDirectories(Path.of(PlatformPaths.getAppDataDir()));
        } catch (IOException e) {
            System.err.println("Error creating save directory: " + e.getMessage());
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile))) {
            oos.writeObject(getSaveData(new ArrayList<>()));
        } catch (IOException e) {
            System.err.println("Error saving game: " + e.getMessage());
        }
    }

    /**
     * Builds a {@link SaveData} object from the current board state, including card,
     * chip, and die positions, multiplayer connection info, and theme settings.
     *
     * @param cardStates an initially empty list to populate with card states
     * @return the complete save data snapshot
     */
    private SaveData getSaveData(List<SaveData.CardState> cardStates) {
        for (Cards card : cards) {
            if (card == null) continue;
            StackPane pane = card.getCardPane();
            double tx = pane.getTranslateX();
            double ty = pane.getTranslateY();

            ImageView backView = (ImageView) pane.getChildren().get(0);
            ImageView frontView = (ImageView) pane.getChildren().get(1);
            Text text = (Text) pane.getChildren().get(2);

            cardStates.add(new SaveData.CardState(
                    tx, ty,
                    pane.getRotate(),
                    backView.getRotate(), frontView.getRotate(), text.getRotate(),
                    backView.isVisible(), frontView.isVisible(), text.isVisible()
            ));
        }

        List<SaveData.ChipState> chipStates = getChipStates();
        List<SaveData.DieState> dieStates = new ArrayList<>();
        for (Dice die : dice) {
            StackPane diePane = die.getPane();
            double tx = diePane.getTranslateX();
            double ty = diePane.getTranslateY();
            Color dieColor = die.getDieColor();

            dieStates.add(new SaveData.DieState(
                    tx, ty,
                    diePane.getRotate(),
                    die.getSides(),
                    die.getCurrentValue(),
                    dieColor.getRed(), dieColor.getGreen(), dieColor.getBlue(), dieColor.getOpacity()
            ));
        }

        String mpIp = null;
        int mpPort = 0;
        if (isMultiplayer) {
            if (isHost && gameServer != null) {
                mpIp = "HOST";
                mpPort = gameServer.getPort();
            } else if (gameClient != null && gameClient.isConnected()) {
                mpIp = joinGameScene.getJoinIpField().getText().trim();
                try {
                    mpPort = Integer.parseInt(joinGameScene.getJoinPortField().getText().trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return new SaveData(cardStates, chipStates, dieStates, reshuffled, new ArrayList<>(cardNames),
                customCardFrontPath, customCardBackPath, customChipFrontPath, customChipBackPath, customBackgroundPath,
                currentCardTheme.getThemeName(),
                isMultiplayer, mpIp, mpPort);
    }

    /**
     * Collects the current position, rotation, and color state of all chips on the board.
     *
     * @return a list of chip state snapshots
     */
    private List<SaveData.ChipState> getChipStates() {
        List<SaveData.ChipState> chipStates = new ArrayList<>();
        for (Chips chip : chips) {
            StackPane chipPane = chip.getChipPane();
            double tx = chipPane.getTranslateX();
            double ty = chipPane.getTranslateY();

            ImageView frontView = (ImageView) chipPane.getChildren().get(0);
            ImageView backView = (ImageView) chipPane.getChildren().get(1);
            Color chipColor = chip.getColor();

            chipStates.add(new SaveData.ChipState(
                    tx, ty,
                    frontView.getRotate(), backView.getRotate(),
                    frontView.isVisible(), backView.isVisible(),
                    chipColor.getRed(), chipColor.getGreen(), chipColor.getBlue(), chipColor.getOpacity()
            ));
        }
        return chipStates;
    }

    /**
     * Restores the board state from a {@link SaveData} object, including card names,
     * piece positions, custom asset paths, theme, and multiplayer info.
     *
     * @param save the save data to restore
     */
    private void loadGame(SaveData save) {
        reshuffled = save.reshuffled();
        cardNames.setAll(save.cardNames());

        customCardFrontPath = save.customCardFrontPath();
        customCardBackPath = save.customCardBackPath();
        customChipFrontPath = save.customChipFrontPath();
        customChipBackPath = save.customChipBackPath();
        customBackgroundPath = save.customBackgroundPath();
        currentCardTheme = ThemeManager.getThemeByName(save.themeName());
        ThemeManager.setActiveTheme(currentCardTheme);

        if (cards != null) {
            for (Cards card : cards) {
                if (card != null) {
                    gameScene.getGameContent().getChildren().remove(card.getCardPane());
                }
            }
        }
        cards = new Cards[NUM_CARDS];
        pieceMap.clear();

        Image cardFrontImage = loadImage(customCardFrontPath, currentCardTheme.getCardFrontPath(), currentCardTheme);
        Image cardBackImage = loadImage(customCardBackPath, currentCardTheme.getCardBackPath(), currentCardTheme);

        updateBackground(gameScene.getGameBg());
        updateStartSceneBackground();
        bwFrontImage = loadImage(customChipFrontPath, currentCardTheme.getChipFrontPath(), currentCardTheme);
        bwBackImage = loadImage(customChipBackPath, currentCardTheme.getChipBackPath(), currentCardTheme);

        int idx = 0;
        for (var cs : save.cards()) {
            Cards card;
            String cardLogicalName = cardNames.get(idx);

            Matcher matcher = CARD_PATTERN.matcher(cardLogicalName);
            if (matcher.matches() && !wilds.contains(cardLogicalName)) {
                String value = matcher.group("value");
                String suit = matcher.group("suit");
                card = new Cards(cardLogicalName, value, suit, CARD_WIDTH, CARD_HEIGHT, cardFrontImage, cardBackImage, currentCardTheme, wilds);
            } else {
                card = new Cards(cardLogicalName, "", "", CARD_WIDTH, CARD_HEIGHT, cardFrontImage, cardBackImage, currentCardTheme, wilds);
                Text cardNameText = CardDataHelper.getWildCardName(new Text(cardLogicalName + "\n \n" + "(Wild)"));
                ((Text) card.getCardPane().getChildren().get(2)).setText(cardNameText.getText());
                card.getCardPane().getChildren().get(2).setStyle(cardNameText.getStyle());
            }
            String pieceId = "card:" + idx;
            cards[idx] = card;

            StackPane pane = cards[idx].getCardPane();

            ImageView backView = (ImageView) pane.getChildren().get(0);
            ImageView frontView = (ImageView) pane.getChildren().get(1);
            Text text = (Text) pane.getChildren().get(2);

            pane.getTransforms().clear();
            setupPieceInteractions(pane, pieceId, true);

            pane.setTranslateX(cs.translateX());
            pane.setTranslateY(cs.translateY());
            pane.setRotate(cs.paneRotate());

            backView.setRotate(cs.backRotate());
            frontView.setRotate(cs.frontRotate());
            text.setRotate(cs.textRotate());
            backView.setVisible(cs.backVisible());
            frontView.setVisible(cs.frontVisible());
            text.setVisible(cs.textVisible());

            if (!wilds.contains(cardLogicalName)) {
                Matcher matcher1 = CARD_PATTERN.matcher(cardLogicalName);
                if (matcher1.matches()) {
                    String value = matcher1.group("value");
                    String suit = matcher1.group("suit");
                    var styled = Cards.getStyle(cardLogicalName, value, suit, currentCardTheme);
                    text.setText(styled.getText());
                    text.setStyle(styled.getStyle());
                }
            }

            cards[idx].refreshTooltipContent(cardLogicalName, wilds);
            gameScene.getGameContent().getChildren().add(pane);
            pieceMap.put(pieceId, pane);
            idx++;
        }

        for (var ds : save.dice()) {
            Color loadedColor = new Color(ds.red(), ds.green(), ds.blue(), ds.opacity());
            Dice die = new Dice(ds.sides(), loadedColor);
            StackPane diePane = die.getPane();

            diePane.translateXProperty().unbind();
            diePane.getTransforms().clear();
            setupPieceInteractions(diePane, die.getPieceId(), false);

            diePane.setTranslateX(ds.translateX());
            diePane.setTranslateY(ds.translateY());
            diePane.setRotate(ds.paneRotate());

            die.setCurrentValue(ds.currentValue());

            dice.add(die);
            gameScene.getGameContent().getChildren().add(diePane);
            pieceMap.put(die.getPieceId(), diePane);
        }
        gameScene.bringCursorOverlayToFront();
    }

    /**
     * Checks for a newer application release in a background thread and updates
     * the provided label with the result. The label is shown only when an update
     * is available and is made clickable to start the download.
     *
     * @param statusLabel the label to update with check status
     */
    public void checkForUpdates(Label statusLabel) {
        new Thread(() -> {
            try {
                UpdateManager.ReleaseInfo release = UpdateManager.checkForUpdate();
                Platform.runLater(() -> {
                    if (release != null && UpdateManager.isNewerVersion(release.version())) {
                        statusLabel.setText("Update v" + release.version() + " available (click to download)");
                        statusLabel.setStyle("-fx-text-fill: white; -fx-font-size: 40; -fx-font-weight: bold; -fx-underline: true; -fx-cursor: hand;");
                        statusLabel.setOnMouseClicked(_ -> downloadAndInstallUpdate(release));
                        statusLabel.setVisible(true);
                    }
                });
            } catch (Exception ignored) {
            }
        }).start();
    }

    /**
     * Shows simple information alert dialog and waits for the user to dismiss it.
     *
     * @param title   the alert window title
     * @param content the alert message text
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Downloads the given release MSI with a progress dialog, then launches the installer
     * and exits the application.
     *
     * @param release the release to download and install
     */
    private void downloadAndInstallUpdate(UpdateManager.ReleaseInfo release) {
        Stage progressStage = new Stage();
        progressStage.setTitle("Downloading Update");
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-padding: 20;");
        Label statusLabel = new Label("Downloading version " + release.version() + "...");
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        vbox.getChildren().addAll(statusLabel, progressBar);
        Scene scene = new Scene(vbox);
        progressStage.setScene(scene);
        progressStage.setWidth(380);
        progressStage.setHeight(130);
        progressStage.show();

        new Thread(() -> {
            try {
                java.nio.file.Path msiPath = UpdateManager.downloadUpdate(release.downloadUrl(),
                        fraction -> Platform.runLater(() -> progressBar.setProgress(fraction)));
                Platform.runLater(() -> {
                    progressStage.close();
                    try {
                        UpdateManager.installUpdate(msiPath);
                        showAlert("Update Downloaded", "The update has been downloaded.\nThe application will now close to complete the installation.");
                        Platform.exit();
                    } catch (IOException e) {
                        showAlert("Install Failed", "Could not launch installer: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressStage.close();
                    showAlert("Download Failed", "Could not download update: " + e.getMessage());
                });
            }
        }).start();
    }


    private static boolean launched;

    /**
     * Application entry point. Handles {@code --version} and {@code --help}
     * flags before launching the JavaFX application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "--version" -> {
                    System.out.println("TarotBoard v" + UpdateManager.CURRENT_VERSION);
                    return;
                }
                case "--help" -> {
                    System.out.println("Usage: TarotBoard [--version] [--help]");
                    return;
                }
            }
        }
        if (!launched) {
            launched = true;
            launch(args);
        }
    }

    /**
     * JavaFX application initializer. Re-enters {@link #main(String[])}
     * with the raw launch arguments so that {@code main} is the sole CLI
     * handler and is visibly referenced from code.
     */
    @Override
    public void init() {
        main(getParameters().getRaw().toArray(String[]::new));
    }

    public String getCardId(StackPane pane) {
        for (int i = 0; i < cards.length; i++) {
            if (cards[i] != null && cards[i].getCardPane() == pane) {
                return "card:" + i;
            }
        }
        return null;
    }

    public boolean isWildCard(StackPane pane) {
        for (int i = 0; i < cards.length; i++) {
            if (cards[i] != null && cards[i].getCardPane() == pane) {
                return i < cardNames.size() && wilds.contains(cardNames.get(i));
            }
        }
        return false;
    }

    public void splitDeck() {
        TextInputDialog dialog = new TextInputDialog("10");
        dialog.setTitle("Split Deck");
        dialog.setHeaderText("How many cards to split into a new pile?");
        dialog.setContentText("Cards:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(value -> {
            try {
                int numToSplit = Integer.parseInt(value);
                if (numToSplit <= 0) return;

                List<Cards> deckCards = new ArrayList<>();
                for (Cards card : cards) {
                    if (card.getCardPane().getTranslateX() == DEFAULT_DECK_X &&
                            card.getCardPane().getTranslateY() == DEFAULT_DECK_Y) {
                        deckCards.add(card);
                    }
                }

                if (numToSplit > deckCards.size()) {
                    numToSplit = deckCards.size();
                }

                double newX = DEFAULT_DECK_X + CARD_WIDTH + 20;
                double newY = DEFAULT_DECK_Y;

                for (int i = 0; i < numToSplit; i++) {
                    Cards card = deckCards.get(i);
                    StackPane cardPane = card.getCardPane();
                    cardPane.setTranslateX(newX);
                    cardPane.setTranslateY(newY);
                    sendPieceMove(getCardId(cardPane), newX, newY);
                }
            } catch (NumberFormatException e) {
                // Ignore invalid input
            }
        });
    }

    public void moveWildsToPile() {
        double newX = gameScene.getScene().getWidth() - CARD_WIDTH - 50;
        double newY = 100;
        int i = 0;
        for (int j = 0; j < cards.length; j++) {
            if (cards[j] != null && j < cardNames.size() && wilds.contains(cardNames.get(j))) {
                StackPane cardPane = cards[j].getCardPane();
                cardPane.setTranslateX(newX);
                cardPane.setTranslateY(newY + i * 5);
                sendPieceMove(getCardId(cardPane), cardPane.getTranslateX(), cardPane.getTranslateY());
                i++;
            }
        }
    }
}