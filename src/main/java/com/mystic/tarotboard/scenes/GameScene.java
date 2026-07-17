package com.mystic.tarotboard.scenes;

import com.mystic.tarotboard.TarotBoard;
import com.mystic.tarotboard.network.NetworkMessage;
import com.mystic.tarotboard.network.NetworkMessage.Msg;
import com.mystic.tarotboard.theming.ThemeConfiguration;
import com.mystic.tarotboard.theming.ThemeManager;
import com.mystic.tarotboard.theming.configs.KeyBindConfig;
import com.mystic.tarotboard.utils.PlatformPaths;
import com.mystic.tarotboard.utils.Styles;
import com.mystic.tarotboard.utils.UIUtils;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.util.*;

/**
 * Scene containing the game board, control panel, discard zone, cursor overlay,
 * player list overlay, and all mouse/keyboard event handlers.
 */
public class GameScene {
    private final TarotBoard tarotBoard;
    private final Scene scene;
    private final Pane gameBg;
    private final Pane gameContent;
    private final StackPane discardZone;
    private final Pane cursorOverlay;
    private final Pane playerListOverlay;
    private final double[] mouseX = new double[1];
    private final double[] mouseY = new double[1];
    /** Pinch zoom applied on top of the scale that fits the board to the window. */
    private double userZoom = 1.0;
    /** Two-finger pan of the board, in screen pixels. */
    private double panX;
    private double panY;
    /** Reapplies the board's fit scale together with {@link #userZoom} and the pan. */
    private Runnable rescale = () -> {
    };
    private final Pane gameRoot;
    /** The long-press piece menu, or null when nothing is open. */
    private Pane pieceMenu;

    /** Zoom limits, beyond which the board is either unreadable or a single card. */
    private static final double MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 4.0;
    /** How much one notch of the wheel zooms by. */
    private static final double WHEEL_ZOOM_STEP = 1.1;
    /** Arm span of the discard zone's cross, inside its 90px box. */
    private static final double DISCARD_CROSS_SIZE = 34;
    /** How far a finger may wander during a long press before it counts as a drag. */
    private static final double LONG_PRESS_SLOP = 14;
    /** Shortest gap between cursor positions sent to peers. */
    private static final long CURSOR_SEND_INTERVAL_MS = 50;
    private long lastCursorSend;
    private final Label networkStatusInGame;
    private final Button disconnectButton;
    private final PasswordField opPwInGame;
    private final Button requestOpInGame;

    // Operator-restricted buttons
    private final Button resetDiceButton;
    private final Button resetChipsButton;
    private final Button reshuffleCardsButton;
    private final Button newGameButton;

    /**
     * Constructs the game scene and wires all UI controls and event handlers.
     *
     * @param tarotBoard   the main application instance
     * @param primaryStage the primary stage for scene transitions
     * @param baseWidth    reference width for proportional scaling
     * @param baseHeight   reference height for proportional scaling
     */
    public GameScene(TarotBoard tarotBoard, Stage primaryStage, double baseWidth, double baseHeight) {
        this.tarotBoard = tarotBoard;

        gameRoot = new Pane();
        gameBg = new Pane();
        gameBg.prefWidthProperty().bind(gameRoot.widthProperty());
        gameBg.prefHeightProperty().bind(gameRoot.heightProperty());

        gameContent = new Pane();
        gameContent.setPrefSize(baseWidth, baseHeight);
        gameContent.layoutXProperty().bind(gameRoot.widthProperty().subtract(baseWidth).divide(2));
        gameContent.layoutYProperty().bind(gameRoot.heightProperty().subtract(baseHeight).divide(2));

        gameRoot.getChildren().addAll(gameBg, gameContent);

        scene = new Scene(gameRoot);
        addColorPickerCss(scene);

        Runnable scaleGameContent = () -> {
            double w = scene.getWidth();
            double h = scene.getHeight();
            if (w <= 0 || h <= 0) return;
            double scale = Math.min(w / baseWidth, h / baseHeight);
            scale = Math.clamp(scale, 0.3, 3.0);
            // The pan runs before the scale so it stays in screen pixels: a finger that
            // travels an inch moves the board an inch, whatever the board is zoomed to.
            gameContent.getTransforms().setAll(
                    new Translate(panX, panY),
                    new Scale(scale * userZoom, scale * userZoom, baseWidth / 2, baseHeight / 2));
        };
        this.rescale = scaleGameContent;
        scene.widthProperty().addListener((obs, oldV, newV) -> scaleGameContent.run());
        scene.heightProperty().addListener((obs, oldV, newV) -> scaleGameContent.run());
        installTouchControls(scene);
        installWheelZoom();
        installBoardPan();

        VBox controlPanelRight = new VBox(10);
        controlPanelRight.setAlignment(Pos.TOP_RIGHT);
        controlPanelRight.setStyle(Styles.panelBg());
        controlPanelRight.setPrefWidth(200);
        controlPanelRight.setMaxWidth(200);

        ColorPicker colorPicker = new ColorPicker(tarotBoard.getCurrentColor());
        colorPicker.setOnAction(event -> tarotBoard.setCurrentColor(colorPicker.getValue()));
        colorPicker.setMaxWidth(Double.MAX_VALUE);
        colorPicker.setStyle("-fx-background-color: #2d2d44; -fx-font-size: 11pt;");

        Button spawnChipButton = new Button("Spawn Chip");
        spawnChipButton.setStyle(Styles.panelBtn());
        spawnChipButton.setMaxWidth(Double.MAX_VALUE);
        // The board's current colour rather than the picker's own value: the picker sets
        // that colour on desktop, and on Android it cannot be opened at all, so the touch
        // swatches below are what set it there.
        spawnChipButton.setOnAction(event -> tarotBoard.spawnChip(tarotBoard.getCurrentColor()));

        TextField diceSidesInput = new TextField("20");
        diceSidesInput.setPrefWidth(60);
        diceSidesInput.setAlignment(Pos.CENTER);
        diceSidesInput.setStyle(Styles.panelBtn());
        diceSidesInput.textProperty().addListener((obs, oldV, n) -> {
            if (!n.matches("\\d*")) {
                diceSidesInput.setText(n.replaceAll("\\D", ""));
                return;
            }
            if (n.length() > 4) {
                diceSidesInput.setText(n.substring(0, 4));
                return;
            }
            if (!n.isEmpty() && Integer.parseInt(n) > 9999) {
                diceSidesInput.setText("9999");
            }
        });

        Button spawnDieButton = new Button("Spawn Dice");
        spawnDieButton.setStyle(Styles.panelBtn());
        spawnDieButton.setMaxWidth(Double.MAX_VALUE);
        spawnDieButton.setOnAction(event -> {
            try {
                int sides = Integer.parseInt(diceSidesInput.getText());
                if (sides > 0) {
                    tarotBoard.spawnDie(sides, tarotBoard.getCurrentColor());
                } else {
                    System.err.println("Number of sides for die must be positive.");
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid number of sides for die: " + diceSidesInput.getText());
            }
        });

        HBox diceInputGroup = new HBox(5, diceSidesInput, spawnDieButton);
        diceInputGroup.setAlignment(Pos.CENTER_LEFT);
        diceInputGroup.setMaxWidth(Double.MAX_VALUE);

        resetDiceButton = new Button("Reset Dice");
        resetDiceButton.setStyle(Styles.panelBtn());
        resetDiceButton.setMaxWidth(Double.MAX_VALUE);
        resetDiceButton.setOnAction(event -> tarotBoard.resetDice());

        ComboBox<ThemeConfiguration> themeSelector = new ComboBox<>(FXCollections.observableArrayList(ThemeManager.getThemes()));
        themeSelector.setValue(tarotBoard.getCurrentCardTheme());
        themeSelector.setMaxWidth(Double.MAX_VALUE);
        themeSelector.setConverter(new StringConverter<>() {
            @Override
            public String toString(ThemeConfiguration theme) {
                return theme != null ? theme.getThemeName() : "";
            }

            @Override
            public ThemeConfiguration fromString(String string) {
                return ThemeManager.getThemeByName(string);
            }
        });
        themeSelector.setOnAction(event -> tarotBoard.applyCurrentTheme(themeSelector.getValue()));

        resetChipsButton = new Button("Reset Chips");
        resetChipsButton.setStyle(Styles.panelBtn());
        resetChipsButton.setMaxWidth(Double.MAX_VALUE);
        resetChipsButton.setOnAction(event -> tarotBoard.resetChips());

        reshuffleCardsButton = new Button("Reshuffle Cards");
        reshuffleCardsButton.setStyle(Styles.panelBtn());
        reshuffleCardsButton.setMaxWidth(Double.MAX_VALUE);
        reshuffleCardsButton.setOnAction(event -> tarotBoard.reshuffleCards());

        newGameButton = new Button("New Game");
        newGameButton.setStyle(Styles.panelBtn());
        newGameButton.setMaxWidth(Double.MAX_VALUE);
        newGameButton.setOnAction(event -> tarotBoard.newGame());

        Button helpButton2 = new Button("Help");
        helpButton2.setStyle(Styles.panelBtn());
        helpButton2.setMaxWidth(Double.MAX_VALUE);
        helpButton2.setOnAction(event -> HelpScene.show(primaryStage));

        Button backButton3 = new Button("Back to Start");
        backButton3.setStyle(Styles.panelBtn());
        backButton3.setMaxWidth(Double.MAX_VALUE);
        backButton3.setOnAction(event -> {
            tarotBoard.leaveGame();
            tarotBoard.switchToStart();
        });

        disconnectButton = new Button("Disconnect");
        disconnectButton.setStyle(Styles.panelBtn());
        disconnectButton.setMaxWidth(Double.MAX_VALUE);
        disconnectButton.setOnAction(event -> {
            tarotBoard.leaveGame();
            tarotBoard.switchToStart();
        });
        disconnectButton.setVisible(false);

        networkStatusInGame = new Label("");
        networkStatusInGame.setStyle(Styles.panelLabel());
        networkStatusInGame.setMaxWidth(Double.MAX_VALUE);
        networkStatusInGame.setWrapText(true);

        opPwInGame = new PasswordField();
        opPwInGame.setPromptText("Op password");
        opPwInGame.setStyle(Styles.panelSmall());
        opPwInGame.setMaxWidth(Double.MAX_VALUE);
        opPwInGame.setVisible(false);
        requestOpInGame = new Button("Request Operator");
        requestOpInGame.setStyle(Styles.panelSmall());
        requestOpInGame.setMaxWidth(Double.MAX_VALUE);
        requestOpInGame.setVisible(false);
        requestOpInGame.setOnAction(event -> {
            String pw = opPwInGame.getText();
            if (!pw.isEmpty() && tarotBoard.isClientConnected()) {
                tarotBoard.sendNetworkMessage(NetworkMessage.of(new Msg.RequestOperator(tarotBoard.getMyPlayerId(), pw)));
                opPwInGame.clear();
            }
        });

        Button chooseCursorInGame = new Button("Cursor Image");
        chooseCursorInGame.setStyle(Styles.panelSmall());
        chooseCursorInGame.setMaxWidth(Double.MAX_VALUE);
        chooseCursorInGame.setOnAction(event -> tarotBoard.chooseCursorImage());

        Button settingsInGameBtn = new Button("Settings");
        settingsInGameBtn.setStyle(Styles.panelBtn());
        settingsInGameBtn.setMaxWidth(Double.MAX_VALUE);
        settingsInGameBtn.setOnAction(event -> SettingsScene.show(primaryStage));

        // Zoom and pan have no scrollbars to show where the board went, so there has to be
        // a way back to a known view on every platform that can move it.
        Button resetViewBtn = new Button("Reset View");
        resetViewBtn.setStyle(Styles.panelSmall());
        resetViewBtn.setMaxWidth(Double.MAX_VALUE);
        resetViewBtn.setOnAction(event -> resetView());

        // Split deck, move wilds and the player list are keystrokes everywhere else, which
        // a tablet has no way to send. Filled in further down, once playerListOverlay
        // exists for the Players button to toggle.
        VBox touchActions = new VBox(10);
        touchActions.setVisible(PlatformPaths.isAndroid());
        touchActions.setManaged(PlatformPaths.isAndroid());

        controlPanelRight.getChildren().addAll(
                colorPicker,
                chooseCursorInGame,
                spawnChipButton,
                diceInputGroup,
                resetDiceButton,
                resetChipsButton,
                reshuffleCardsButton,
                newGameButton,
                resetViewBtn,
                touchActions,
                networkStatusInGame,
                opPwInGame,
                requestOpInGame,
                disconnectButton,
                settingsInGameBtn,
                helpButton2,
                backButton3,
                themeSelector
        );

        // The panel is taller than a tablet screen, so its lower half — disconnect, settings,
        // back, theme — simply had no way to be reached. A ScrollPane rather than a shorter
        // panel because every one of those controls has to stay available.
        ScrollPane controlPanelScroll = new ScrollPane(controlPanelRight);
        controlPanelScroll.setFitToWidth(true);
        controlPanelScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        controlPanelScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        controlPanelScroll.setPrefWidth(216);
        controlPanelScroll.setMaxWidth(216);
        // Transparent so the panel's own background stays the thing that shows.
        controlPanelScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        // A drag inside the panel should scroll it rather than fling the board underneath.
        controlPanelScroll.setPannable(true);
        controlPanelScroll.layoutXProperty().bind(
                scene.widthProperty().subtract(controlPanelScroll.widthProperty()).subtract(10));
        controlPanelScroll.setLayoutY(10);
        // Leaves a strip of board visible above and below rather than running edge to edge.
        controlPanelScroll.prefHeightProperty().bind(scene.heightProperty().subtract(20));
        gameRoot.getChildren().add(controlPanelScroll);

        discardZone = new StackPane();
        discardZone.setStyle(Styles.discardZone());
        discardZone.setPrefSize(90, 90);
        discardZone.getChildren().add(discardCross());
        discardZone.setLayoutX(10);
        discardZone.layoutYProperty().bind(scene.heightProperty().subtract(100));
        gameRoot.getChildren().add(discardZone);

        cursorOverlay = new Pane();
        cursorOverlay.setMouseTransparent(true);
        gameRoot.getChildren().add(cursorOverlay);

        StackPane overlayRoot = new StackPane();
        overlayRoot.setMouseTransparent(true);
        overlayRoot.setVisible(false);
        overlayRoot.prefWidthProperty().bind(scene.widthProperty());
        overlayRoot.prefHeightProperty().bind(scene.heightProperty());
        overlayRoot.setStyle(Styles.overlayBg());
        VBox overlayContent = new VBox(10);
        overlayContent.setAlignment(Pos.TOP_CENTER);
        overlayContent.setStyle(Styles.overlayContent());
        overlayContent.setMaxWidth(480);
        overlayContent.setMaxHeight(600);
        VBox overlayPlayersBox = new VBox(6);
        overlayPlayersBox.setAlignment(Pos.CENTER_LEFT);
        overlayContent.getChildren().addAll(overlayPlayersBox);
        overlayRoot.getChildren().add(overlayContent);
        playerListOverlay = overlayRoot;

        if (PlatformPaths.isAndroid()) {
            Button splitDeckBtn = new Button("Split Deck");
            splitDeckBtn.setStyle(Styles.panelBtn());
            splitDeckBtn.setMaxWidth(Double.MAX_VALUE);
            splitDeckBtn.setOnAction(event -> tarotBoard.splitDeck());

            Button moveWildsBtn = new Button("Move Wilds");
            moveWildsBtn.setStyle(Styles.panelBtn());
            moveWildsBtn.setMaxWidth(Double.MAX_VALUE);
            moveWildsBtn.setOnAction(event -> tarotBoard.moveWildsToPile());

            // Desktop shows the list only while the key is held; a finger has nothing to
            // hold, so this toggles.
            Button playersBtn = new Button("Players");
            playersBtn.setStyle(Styles.panelBtn());
            playersBtn.setMaxWidth(Double.MAX_VALUE);
            playersBtn.setOnAction(event -> {
                if (!tarotBoard.isMultiplayer()) return;
                if (playerListOverlay.isVisible()) {
                    playerListOverlay.setVisible(false);
                } else {
                    rebuildPlayerListOverlay();
                    playerListOverlay.setVisible(true);
                }
            });

            // A drag carries the pile while this is latched on, which is the one way to
            // reach pile drag without a modifier key to hold.
            ToggleButton pileDragBtn = new ToggleButton("Drag Pile: Off");
            pileDragBtn.setStyle(Styles.panelBtn());
            pileDragBtn.setMaxWidth(Double.MAX_VALUE);
            pileDragBtn.setOnAction(event -> {
                boolean on = pileDragBtn.isSelected();
                UIUtils.setTouchPileDrag(on);
                pileDragBtn.setText(on ? "Drag Pile: On" : "Drag Pile: Off");
            });

            touchActions.getChildren().addAll(splitDeckBtn, moveWildsBtn, playersBtn,
                    pileDragBtn, touchColourSwatches(), touchDiePresets(diceSidesInput));
        }

        var keybinds = KeyBindConfig.getInstance();
        // MOUSE_MOVED stops arriving the moment a button goes down, so tracking it alone
        // left the pointer position frozen wherever a drag began: keyboard actions then
        // acted on whatever piece sat at the old spot, and peers watched a stuck cursor.
        // MOUSE_DRAGGED carries the rest of the gesture.
        scene.addEventFilter(MouseEvent.MOUSE_MOVED, this::trackMouse);
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::trackMouse);
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, this::trackMouse);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (isTypingInTextField()) return;
            var code = event.getCode();
            if (code == keybinds.togglePlayerList()) {
                if (tarotBoard.isMultiplayer()) {
                    rebuildPlayerListOverlay();
                    playerListOverlay.setVisible(true);
                }
                event.consume();
                return;
            }
            if (code == keybinds.multiFlip()) {
                // Topmost piece, not the first chip that happens to sit under the cursor,
                // so a stacked chip flips the one the player can actually see.
                StackPane piece = tarotBoard.findPieceAtMouse(mouseX[0], mouseY[0]);
                if (piece != null && tarotBoard.isChip(piece)) {
                    UIUtils.multiFlip(piece);
                }
                event.consume();
                return;
            }
            if (code == keybinds.splitDeck()) {
                tarotBoard.splitDeck();
                event.consume();
                return;
            }
            if (code == keybinds.moveWilds()) {
                tarotBoard.moveWildsToPile();
                event.consume();
                return;
            }
            StackPane piece = tarotBoard.findPieceAtMouse(mouseX[0], mouseY[0]);
            if (piece != null) {
                UIUtils.handlePieceKeyPress(piece, code);
                int speed = keybinds.moveSpeed();
                int dx = 0, dy = 0;
                if (code == keybinds.moveUp()) dy = -speed;
                else if (code == keybinds.moveDown()) dy = speed;
                else if (code == keybinds.moveLeft()) dx = -speed;
                else if (code == keybinds.moveRight()) dx = speed;
                if (dx != 0 || dy != 0) {
                    piece.setTranslateX(piece.getTranslateX() + dx);
                    piece.setTranslateY(piece.getTranslateY() + dy);
                }
            }
        });
        scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (isTypingInTextField()) return;
            if (event.getCode() == keybinds.togglePlayerList()) {
                playerListOverlay.setVisible(false);
                event.consume();
            }
        });
        scene.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            bringCursorOverlayToFront();
            // The click's own coordinates, never the tracked ones: a click carries where it
            // happened, so there is no way for it to act on a piece somewhere else.
            StackPane piece = tarotBoard.findPieceAtMouse(event.getSceneX(), event.getSceneY());
            if (piece != null) {
                UIUtils.handlePieceClick(piece, event);
            }
        });
    }

    /**
     * Whether a text field currently holds focus, in which case its keystrokes are text
     * and not board keybinds. The board's keybinds are scene-wide filters, so without
     * this an operator password would split piles and lose the letters it fired on.
     */
    private boolean isTypingInTextField() {
        return scene.getFocusOwner() instanceof TextInputControl;
    }

    /**
     * Records the pointer position for the keyboard actions that act on whatever piece is
     * under the cursor, and reports it to peers so they can follow this player's cursor.
     */
    private void trackMouse(MouseEvent event) {
        mouseX[0] = event.getSceneX();
        mouseY[0] = event.getSceneY();
        if (!tarotBoard.isMultiplayer()) return;
        // Throttled to match the piece moves this rides alongside. A pointer reports every
        // pixel it crosses, so sending each one put a serialise-and-write on the FX thread
        // for every event of a drag — the drag being exactly when the thread has the least
        // to spare. Peers cannot see a cursor updated faster than their own frames anyway.
        long now = System.currentTimeMillis();
        if (now - lastCursorSend < CURSOR_SEND_INTERVAL_MS) return;
        lastCursorSend = now;
        tarotBoard.sendNetworkMessage(NetworkMessage.of(
                new Msg.CursorMove(tarotBoard.getMyPlayerId(), event.getSceneX(), event.getSceneY())));
    }

    /**
     * Builds the discard zone's cross out of two strokes.
     *
     * <p>This was a "✖" (U+2716), a Dingbats character that Android's fonts do not carry
     * and that Gluon's cut-down font set has nothing to fall back to, so the zone came up
     * as an empty box on a tablet while looking right on desktop. A shape has no font to
     * miss and draws the same everywhere.</p>
     */
    private Node discardCross() {
        Line down = new Line(0, 0, DISCARD_CROSS_SIZE, DISCARD_CROSS_SIZE);
        Line up = new Line(0, DISCARD_CROSS_SIZE, DISCARD_CROSS_SIZE, 0);
        for (Line stroke : List.of(down, up)) {
            stroke.setStroke(Color.web("#cc0000"));
            stroke.setStrokeWidth(5);
            // Rounded ends, so the arms read as a drawn mark rather than as cut bars.
            stroke.setStrokeLineCap(StrokeLineCap.ROUND);
        }
        return new Group(down, up);
    }

    /**
     * Returns the board to a known view: unzoomed, unpanned, fitted to the window.
     */
    private void resetView() {
        userZoom = 1.0;
        panX = 0;
        panY = 0;
        rescale.run();
    }

    /**
     * Whether a press landed on the board itself rather than on a piece or a control.
     *
     * <p>The board's own panes are the only things this accepts, which is what separates a
     * pan from a piece drag: a press on a card reports the card's image or its pane, never
     * the board underneath it. The control panel and the discard zone are children of the
     * scene root and are likewise not the board.</p>
     */
    private boolean isBoardBackground(Object target) {
        return target == gameBg || target == gameContent || target == gameRoot;
    }

    /**
     * Zooms the board with the mouse wheel, the desktop counterpart to the pinch gesture.
     *
     * <p>A handler rather than a filter, and hung off the scene root rather than the scene,
     * so a control that has its own use for a wheel — a dropdown's list, a scrolling pane —
     * still gets it first and this only sees what nothing else wanted. Android is excluded
     * because a scroll there is two fingers panning, which {@link #installTouchControls}
     * already claims.</p>
     */
    private void installWheelZoom() {
        if (PlatformPaths.isAndroid()) return;
        gameRoot.addEventHandler(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() == 0) return;
            // A notch is a fixed step rather than a multiple of the reported delta, which
            // varies wildly between a wheel, a trackpad and a free-spinning wheel.
            double step = event.getDeltaY() > 0 ? WHEEL_ZOOM_STEP : 1 / WHEEL_ZOOM_STEP;
            userZoom = Math.clamp(userZoom * step, MIN_ZOOM, MAX_ZOOM);
            rescale.run();
            event.consume();
        });
    }

    /**
     * Drags the whole board with the left button held on empty board.
     *
     * <p>The press decides whether the gesture is a pan, and the drag only follows through
     * on that decision: a card dragged out from under the pointer would otherwise hand the
     * board the rest of the gesture and slide it along underneath the card. Filters, so the
     * decision is made before a piece's own handlers see the press.</p>
     *
     * <p>Desktop only. Android pans with two fingers, and a touch there arrives as a
     * synthesised mouse drag as well, so this would pan a second time on top of the
     * gesture that already did.</p>
     */
    private void installBoardPan() {
        if (PlatformPaths.isAndroid()) return;
        double[] last = new double[2];
        boolean[] panning = {false};

        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            panning[0] = event.getButton() == MouseButton.PRIMARY && isBoardBackground(event.getTarget());
            last[0] = event.getSceneX();
            last[1] = event.getSceneY();
        });
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            if (!panning[0]) return;
            // Screen pixels, matching the Translate that runs before the board's scale: the
            // board keeps up with the pointer whatever it is zoomed to.
            panX += event.getSceneX() - last[0];
            panY += event.getSceneY() - last[1];
            last[0] = event.getSceneX();
            last[1] = event.getSceneY();
            rescale.run();
        });
        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> panning[0] = false);
    }

    /**
     * Wires up the touch-only controls.
     *
     * <p>Only the actions that a touch screen cannot otherwise reach are rebuilt here.
     * Dragging a piece and double-tapping to flip already work, because a single touch
     * arrives as a synthesised mouse event; everything else on a piece is behind a
     * modifier key or the right mouse button, so it moves to a long-press menu. Zoom and
     * pan are new to touch — a phone cannot show a 1920x1080 board legibly at once.</p>
     *
     * <p>Android only: on desktop these same events come from a trackpad and the board is
     * already reachable with keys and buttons.</p>
     */
    private void installTouchControls(Scene scene) {
        if (!PlatformPaths.isAndroid()) return;

        scene.addEventFilter(ZoomEvent.ZOOM, event -> {
            userZoom = Math.clamp(userZoom * event.getZoomFactor(), MIN_ZOOM, MAX_ZOOM);
            rescale.run();
            event.consume();
        });

        // Two fingers only. A one-finger drag has to stay free to move a piece, and with
        // scroll recognition on it would otherwise pan the board underneath the piece at
        // the same time.
        scene.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getTouchCount() < 2) return;
            panX += event.getDeltaX();
            panY += event.getDeltaY();
            rescale.run();
            event.consume();
        });

        PauseTransition longPress = new PauseTransition(Duration.millis(450));
        double[] pressScene = new double[2];

        longPress.setOnFinished(event -> {
            StackPane piece = tarotBoard.findPieceAtMouse(pressScene[0], pressScene[1]);
            if (piece != null) showPieceMenu(piece, pressScene[0], pressScene[1]);
        });

        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            // While the menu is up, a press belongs to the menu. Starting the timer again
            // would rebuild the menu underneath the finger mid-tap, swapping out the very
            // button being pressed so its release lands on nothing and the tap is lost.
            if (pieceMenu != null) return;
            pressScene[0] = event.getSceneX();
            pressScene[1] = event.getSceneY();
            longPress.playFromStart();
        });
        // A press that turns into a drag is a move, not a long press. The slop is
        // deliberately generous: a finger resting on glass wanders a few pixels.
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            if (Math.hypot(event.getSceneX() - pressScene[0], event.getSceneY() - pressScene[1]) > LONG_PRESS_SLOP) {
                longPress.stop();
            }
        });
        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> longPress.stop());
    }

    /**
     * Shows the touch menu of everything that can be done to a single piece.
     *
     * <p>Built as a node inside the scene rather than a {@link ContextMenu}, because a
     * popup is a second window and Android's toolkit drives a single window: a popup menu
     * does render there, but no touch ever reaches it, so every entry is dead. This also
     * has to hang off {@code gameRoot} rather than {@code gameContent}, or it would be
     * scaled and panned along with the board it is acting on.</p>
     *
     * @param piece  the piece the finger went down on
     * @param sceneX scene X to open the menu at
     * @param sceneY scene Y to open the menu at
     */
    private void showPieceMenu(StackPane piece, double sceneX, double sceneY) {
        hidePieceMenu();

        VBox panel = new VBox(4);
        panel.setStyle(Styles.touchMenu());
        panel.getChildren().addAll(
                touchItem("Flip", () -> UIUtils.flipPiece(piece)),
                touchItem("Rotate 90 left", () -> UIUtils.rotatePiece(piece, -90)),
                touchItem("Rotate 90 right", () -> UIUtils.rotatePiece(piece, 90)),
                touchItem("Nudge 1 left", () -> UIUtils.rotatePiece(piece, -1)),
                touchItem("Nudge 1 right", () -> UIUtils.rotatePiece(piece, 1)),
                touchItem("Reset rotation", () -> UIUtils.resetPieceRotation(piece)),
                touchItem("Bring to front", piece::toFront));

        if (tarotBoard.isChip(piece)) {
            panel.getChildren().add(touchItem("Flip whole stack", () -> UIUtils.multiFlip(piece)));
        }

        // Catches the tap that dismisses the menu. Without it that tap would fall through
        // to the board and move whatever piece happened to be underneath.
        Pane scrim = new Pane();
        scrim.prefWidthProperty().bind(scene.widthProperty());
        scrim.prefHeightProperty().bind(scene.heightProperty());
        // An empty Pane paints nothing and so has nothing to hit-test against; without
        // this the dismiss tap sails straight through to the board behind it.
        scrim.setPickOnBounds(true);
        scrim.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            hidePieceMenu();
            event.consume();
        });

        Pane overlay = new Pane(scrim, panel);
        pieceMenu = overlay;
        gameRoot.getChildren().add(overlay);

        // Measure before placing, so a piece near an edge opens a menu that is still
        // wholly on screen.
        panel.applyCss();
        panel.layout();
        double w = panel.prefWidth(-1);
        double h = panel.prefHeight(-1);
        panel.setLayoutX(Math.max(0, Math.min(sceneX, scene.getWidth() - w)));
        panel.setLayoutY(Math.max(0, Math.min(sceneY, scene.getHeight() - h)));
    }

    /**
     * Builds a menu entry sized for a fingertip rather than a mouse pointer.
     */
    private Button touchItem(String text, Runnable action) {
        Button item = new Button(text);
        item.setStyle(Styles.touchMenuItem());
        item.setMaxWidth(Double.MAX_VALUE);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setOnAction(event -> {
            action.run();
            hidePieceMenu();
        });
        return item;
    }

    private void hidePieceMenu() {
        if (pieceMenu != null) {
            gameRoot.getChildren().remove(pieceMenu);
            pieceMenu = null;
        }
    }

    /** The colours a chip or die can be spawned in by touch. */
    private static final List<Color> TOUCH_COLOURS = List.of(
            Color.web("#e6194b"), Color.web("#3cb44b"), Color.web("#4363d8"), Color.web("#f58231"),
            Color.web("#911eb4"), Color.web("#42d4f4"), Color.web("#f032e6"), Color.web("#ffe119"),
            Color.web("#ffffff"), Color.web("#000000"));

    /**
     * Builds the touch replacement for the colour picker.
     *
     * <p>A {@link ColorPicker} drops its palette into a popup, and a popup on Android
     * renders but never receives a touch, so the picker is not merely awkward there but
     * inert: every chip and die came out the one colour the picker happened to start on.
     * These are ordinary nodes in the scene, so they are reachable. A fixed palette rather
     * than a full colour space, because a fingertip cannot work a saturation square.</p>
     */
    private Pane touchColourSwatches() {
        FlowPane swatches = new FlowPane(6, 6);
        swatches.setMaxWidth(Double.MAX_VALUE);
        for (Color colour : TOUCH_COLOURS) {
            Button swatch = new Button();
            swatch.setPrefSize(44, 44);
            swatch.setMinSize(44, 44);
            // No -fx-effect anywhere in here: an effect on Android takes the renderer down.
            swatch.setStyle("-fx-background-color: " + toHex(colour)
                    + "; -fx-border-color: #dddddd; -fx-border-width: 2; -fx-background-radius: 4;");
            swatch.setOnAction(event -> {
                tarotBoard.setCurrentColor(colour);
                markSelectedSwatch(swatches, swatch);
            });
            swatches.getChildren().add(swatch);
        }
        // Start with the board's colour ringed, so the swatches agree with what the next
        // chip will actually be rather than showing nothing chosen.
        int current = TOUCH_COLOURS.indexOf(tarotBoard.getCurrentColor());
        markSelectedSwatch(swatches, current < 0 ? null : (Button) swatches.getChildren().get(current));
        return swatches;
    }

    /** Rings the chosen swatch, so the colour the next piece will use is visible. */
    private void markSelectedSwatch(FlowPane swatches, Button chosen) {
        for (int i = 0; i < swatches.getChildren().size(); i++) {
            Button swatch = (Button) swatches.getChildren().get(i);
            boolean on = swatch == chosen;
            swatch.setStyle("-fx-background-color: " + toHex(TOUCH_COLOURS.get(i))
                    + "; -fx-border-color: " + (on ? "#00d0ff" : "#dddddd")
                    + "; -fx-border-width: " + (on ? 4 : 2) + "; -fx-background-radius: 4;");
        }
    }

    /**
     * Builds the touch replacement for typing a die's side count.
     *
     * <p>The side count is a {@link TextField}, which on a tablet needs a soft keyboard
     * raised over the app to change — so the die was stuck on whatever the field was left
     * at. These set the field instead, leaving Spawn Dice as the one thing that spawns.</p>
     */
    private Pane touchDiePresets(TextField diceSidesInput) {
        FlowPane presets = new FlowPane(6, 6);
        presets.setMaxWidth(Double.MAX_VALUE);
        for (int sides : new int[]{4, 6, 8, 10, 12, 20, 100}) {
            Button preset = new Button("d" + sides);
            preset.setStyle(Styles.panelSmall());
            preset.setMinWidth(52);
            preset.setOnAction(event -> diceSidesInput.setText(Integer.toString(sides)));
            presets.getChildren().add(preset);
        }
        return presets;
    }

    /** JavaFX colours have no hex form of their own, and inline styles want one. */
    private static String toHex(Color c) {
        return String.format("#%02X%02X%02X",
                Math.round(c.getRed() * 255), Math.round(c.getGreen() * 255), Math.round(c.getBlue() * 255));
    }

    /**
     * Returns the JavaFX {@link Scene} object.
     *
     * @return the scene
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * Returns the middle of what the player can currently see, in board coordinates.
     *
     * <p>A spawned piece is positioned by its translation, which is read in the board's
     * own space — not the scene's. The two only coincide on a window that happens to be
     * exactly the design size and has never been panned or zoomed, which is why spawning
     * looked right on a desktop and dropped pieces off the edge of a tablet. Asking the
     * board to convert the scene's centre folds in its centring offset, the fit-to-window
     * scale, and the pan and zoom, so a piece lands where the player is looking.</p>
     */
    public Point2D visibleBoardCentre() {
        return gameContent.sceneToLocal(scene.getWidth() / 2, scene.getHeight() / 2);
    }

    /**
     * Returns the background pane for the game board.
     *
     * @return the game background pane
     */
    public Pane getGameBg() {
        return gameBg;
    }

    /**
     * Returns the content pane where game pieces (cards, chips, dice) are placed.
     *
     * @return the game content pane
     */
    public Pane getGameContent() {
        return gameContent;
    }

    /**
     * Returns the discard zone stack pane.
     *
     * @return the discard zone pane
     */
    public StackPane getDiscardZone() {
        return discardZone;
    }

    /**
     * Returns the overlay pane for remote cursor indicators.
     *
     * @return the cursor overlay pane
     */
    public Pane getCursorOverlay() {
        return cursorOverlay;
    }

    /**
     * Returns the label used for in-game network status messages.
     *
     * @return the network status label
     */
    public Label getNetworkStatusInGame() {
        return networkStatusInGame;
    }

    /**
     * Shows or hides multiplayer-specific controls (operator password,
     * request operator button, and disconnect button).
     *
     * @param visible true to show multiplayer controls, false to hide
     */
    public void setMultiplayerControlsVisible(boolean visible) {
        opPwInGame.setVisible(visible);
        opPwInGame.setManaged(visible);
        requestOpInGame.setVisible(visible);
        requestOpInGame.setManaged(visible);
        disconnectButton.setVisible(visible);
        disconnectButton.setManaged(visible);
    }

    /**
     * Updates the visibility of operator-only buttons based on current permissions.
     */
    public void updateOperatorButtonsVisibility() {
        boolean hasPerms = tarotBoard.isHost() || tarotBoard.isOperator() || !tarotBoard.isMultiplayer();
        
        // Use setVisible instead of setDisable
        resetDiceButton.setVisible(hasPerms);
        resetDiceButton.setManaged(hasPerms);
        resetChipsButton.setVisible(hasPerms);
        resetChipsButton.setManaged(hasPerms);
        reshuffleCardsButton.setVisible(hasPerms);
        reshuffleCardsButton.setManaged(hasPerms);
        newGameButton.setVisible(hasPerms);
        newGameButton.setManaged(hasPerms);
        
        // Hide the request operator stuff if they are already an operator or host
        boolean showRequestOp = tarotBoard.isMultiplayer() && !tarotBoard.isHost() && !tarotBoard.isOperator();
        opPwInGame.setVisible(showRequestOp);
        opPwInGame.setManaged(showRequestOp);
        requestOpInGame.setVisible(showRequestOp);
        requestOpInGame.setManaged(showRequestOp);
    }

    /**
     * Brings the cursor overlay and player list overlay to the front of the scene
     * so they remain visible above game content and UI panels.
     */
    public void bringCursorOverlayToFront() {
        if (cursorOverlay != null && cursorOverlay.getParent() != null) {
            cursorOverlay.toFront();
        }
        if (playerListOverlay != null && playerListOverlay.getParent() != null) {
            playerListOverlay.toFront();
        }
    }

    /**
     * Rebuilds the player list overlay content from the current network player list.
     * The local player is sorted first, followed by others by ID.
     */
    public void rebuildPlayerListOverlay() {
        if (!(playerListOverlay instanceof StackPane overlay)) return;
        if (overlay.getChildren().isEmpty()) return;
        VBox content = (VBox) overlay.getChildren().getFirst();
        VBox playersBox = (VBox) content.getChildren().getFirst();
        playersBox.getChildren().clear();

        List<com.mystic.tarotboard.network.NetworkMessage.PlayerInfo> allPlayers = new ArrayList<>(tarotBoard.getPlayerList());
        if (tarotBoard.isHost() && tarotBoard.getGameServer() != null) {
            for (var pi : tarotBoard.getGameServer().getPlayers()) {
                boolean found = false;
                for (var existing : allPlayers) {
                    if (existing.id() == pi.id()) {
                        found = true;
                        break;
                    }
                }
                if (!found) allPlayers.add(pi);
            }
        }

        allPlayers.sort((a, b) -> {
            if (a.id() == tarotBoard.getMyPlayerId()) return -1;
            if (b.id() == tarotBoard.getMyPlayerId()) return 1;
            return Integer.compare(a.id(), b.id());
        });

        for (var pi : allPlayers) {
            boolean isMe = pi.id() == tarotBoard.getMyPlayerId();
            playersBox.getChildren().add(createPlayerRow(pi.name(), Color.color(pi.r(), pi.g(), pi.b()), isMe));
        }
        overlay.toFront();
    }

    private void addColorPickerCss(Scene s) {
        var url = getClass().getResource("/com/mystic/tarotboard/assets/colorpicker.css");
        if (url != null) {
            s.getStylesheets().add(url.toExternalForm());
        }
    }

    private HBox createPlayerRow(String name, Color color, boolean isMe) {
        Circle dot = new Circle(7);
        dot.setFill(color);
        dot.setStroke(Color.WHITE);
        dot.setStrokeWidth(1.5);

        Label nameLabel = new Label(name);
        nameLabel.setStyle(Styles.playerName(isMe));

        StringBuilder tagText = new StringBuilder();
        if (isMe) tagText.append("[You]");
        if (tarotBoard.isHost() && isMe) tagText.append(" [Host]");
        if (tarotBoard.isOperator() && isMe) tagText.append(" [Op]");
        if (!isMe && tarotBoard.isHost()) tagText.append(" [Guest]");

        Label tagLabel = new Label(tagText.toString());
        tagLabel.setStyle(Styles.playerTag(isMe));

        HBox row = new HBox(12, dot, nameLabel, tagLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
}