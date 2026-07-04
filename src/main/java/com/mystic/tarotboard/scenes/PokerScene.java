package com.mystic.tarotboard.scenes;

import com.mystic.tarotboard.TarotBoard;
import com.mystic.tarotboard.items.Chips;
import com.mystic.tarotboard.poker.PokerClientState;
import com.mystic.tarotboard.poker.PokerTable;
import com.mystic.tarotboard.utils.Styles;
import com.mystic.tarotboard.utils.UIUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Dedicated scene for Poker Mode: seat/table controls, a local chip-staking area for ante/call/
 * raise bets, and the showdown result overlay. Distinct from {@link GameScene}'s free-form sandbox
 * board — chips staked here are purely local visual tokens (not networked piece state); only the
 * resulting bet amount, submitted via {@link TarotBoard#pokerAction}, is communicated to the table.
 */
public class PokerScene {
    private final TarotBoard tarotBoard;
    private final Scene scene;
    private final Pane pokerContent;
    private final List<Chips> stakeChips = new ArrayList<>();

    private final Button pokerSitButton;
    private final Button pokerAddBotButton;
    private final Button pokerStartHandButton;
    private final Button pokerCallButton;
    private final Button pokerRaiseButton;
    private final Button pokerFoldButton;
    private final Label pokerStatusLabel;
    private final Label pokerHandLabel;
    private final Label pokerStagedLabel;
    private final StackPane pokerStakeZone;
    private final StackPane pokerPotZone;
    private final StackPane pokerShowdownOverlay;
    private final VBox pokerShowdownContent;

    /**
     * Constructs the Poker Mode scene and wires all controls and event handlers.
     *
     * @param tarotBoard   the main application instance
     * @param primaryStage the primary stage (unused directly, kept for constructor symmetry with other scenes)
     * @param baseWidth    reference width for the poker board area
     * @param baseHeight   reference height for the poker board area
     */
    public PokerScene(TarotBoard tarotBoard, Stage primaryStage, double baseWidth, double baseHeight) {
        this.tarotBoard = tarotBoard;

        Pane root = new Pane();
        root.setStyle("-fx-background-color: #14141f;");

        pokerContent = new Pane();
        pokerContent.setPrefSize(baseWidth, baseHeight);
        root.getChildren().add(pokerContent);

        scene = new Scene(root);

        VBox pokerPanel = new VBox(8);
        pokerPanel.setAlignment(Pos.TOP_LEFT);
        pokerPanel.setStyle(Styles.panelBg());
        pokerPanel.setPrefWidth(240);
        pokerPanel.setMaxWidth(240);
        pokerPanel.setLayoutX(10);
        pokerPanel.setLayoutY(10);

        Button backButton = new Button("Back to Board");
        backButton.setStyle(Styles.panelBtn());
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setOnAction(_ -> tarotBoard.switchToGameBoard());

        pokerSitButton = new Button("Sit Down");
        pokerSitButton.setStyle(Styles.panelBtn());
        pokerSitButton.setMaxWidth(Double.MAX_VALUE);
        pokerSitButton.setOnAction(_ -> tarotBoard.pokerSitDown());

        pokerAddBotButton = new Button("Add Bot");
        pokerAddBotButton.setStyle(Styles.panelBtn());
        pokerAddBotButton.setMaxWidth(Double.MAX_VALUE);
        pokerAddBotButton.setOnAction(_ -> tarotBoard.pokerAddBot());

        pokerStartHandButton = new Button("Start Hand");
        pokerStartHandButton.setStyle(Styles.panelBtn());
        pokerStartHandButton.setMaxWidth(Double.MAX_VALUE);
        pokerStartHandButton.setOnAction(_ -> tarotBoard.pokerStartHand());

        pokerStatusLabel = new Label("Poker Mode is not active yet.");
        pokerStatusLabel.setStyle(Styles.panelLabel());
        pokerStatusLabel.setWrapText(true);

        pokerHandLabel = new Label("");
        pokerHandLabel.setStyle(Styles.panelLabel());
        pokerHandLabel.setWrapText(true);

        pokerStagedLabel = new Label("Staked: 0 (0 pts)");
        pokerStagedLabel.setStyle(Styles.panelLabel());

        pokerCallButton = new Button("Call");
        pokerCallButton.setStyle(Styles.panelBtn());
        pokerCallButton.setMaxWidth(Double.MAX_VALUE);
        pokerCallButton.setOnAction(_ -> confirmAction("CALL"));

        pokerRaiseButton = new Button("Raise");
        pokerRaiseButton.setStyle(Styles.panelBtn());
        pokerRaiseButton.setMaxWidth(Double.MAX_VALUE);
        pokerRaiseButton.setOnAction(_ -> confirmAction("RAISE"));

        pokerFoldButton = new Button("Fold");
        pokerFoldButton.setStyle(Styles.panelBtn());
        pokerFoldButton.setMaxWidth(Double.MAX_VALUE);
        pokerFoldButton.setOnAction(_ -> {
            tarotBoard.pokerAction("FOLD", 0);
            refreshPokerPanel();
        });

        HBox pokerActionButtons = new HBox(6, pokerCallButton, pokerRaiseButton, pokerFoldButton);

        ColorPicker stakeChipColorPicker = new ColorPicker(Color.GOLD);
        stakeChipColorPicker.setMaxWidth(Double.MAX_VALUE);
        stakeChipColorPicker.setStyle("-fx-background-color: #2d2d44; -fx-font-size: 11pt;");

        Button spawnStakeChipButton = new Button("Spawn Chip (" + TarotBoard.POKER_CHIP_VALUE + " pts)");
        spawnStakeChipButton.setStyle(Styles.panelBtn());
        spawnStakeChipButton.setMaxWidth(Double.MAX_VALUE);
        spawnStakeChipButton.setOnAction(_ -> spawnStakeChip(stakeChipColorPicker.getValue()));

        pokerPanel.getChildren().addAll(backButton, pokerSitButton, pokerAddBotButton, pokerStartHandButton,
                pokerStatusLabel, pokerHandLabel, stakeChipColorPicker, spawnStakeChipButton,
                pokerStagedLabel, pokerActionButtons);
        root.getChildren().add(pokerPanel);

        pokerStakeZone = new StackPane();
        pokerStakeZone.setStyle(Styles.discardZone());
        pokerStakeZone.setPrefSize(160, 120);
        Label stakeLabel = new Label("Your Stake\n(drag chips here)");
        stakeLabel.setTextAlignment(TextAlignment.CENTER);
        stakeLabel.setStyle(Styles.panelLabel());
        pokerStakeZone.getChildren().add(stakeLabel);
        pokerStakeZone.layoutXProperty().bind(scene.widthProperty().divide(2).subtract(180));
        pokerStakeZone.layoutYProperty().bind(scene.heightProperty().divide(2).add(60));
        root.getChildren().add(pokerStakeZone);

        pokerPotZone = new StackPane();
        pokerPotZone.setStyle(Styles.discardZone());
        pokerPotZone.setPrefSize(160, 120);
        Label potLabel = new Label("Pot");
        potLabel.setStyle(Styles.panelLabel());
        pokerPotZone.getChildren().add(potLabel);
        pokerPotZone.layoutXProperty().bind(scene.widthProperty().divide(2).add(20));
        pokerPotZone.layoutYProperty().bind(scene.heightProperty().divide(2).add(60));
        root.getChildren().add(pokerPotZone);

        pokerShowdownContent = new VBox(10);
        pokerShowdownContent.setAlignment(Pos.TOP_CENTER);
        pokerShowdownContent.setStyle(Styles.overlayContent());
        pokerShowdownContent.setMaxWidth(520);
        pokerShowdownContent.setMaxHeight(640);
        Button closeShowdownButton = new Button("Close");
        closeShowdownButton.setStyle(Styles.panelBtn());

        pokerShowdownOverlay = new StackPane();
        pokerShowdownOverlay.setVisible(false);
        pokerShowdownOverlay.prefWidthProperty().bind(scene.widthProperty());
        pokerShowdownOverlay.prefHeightProperty().bind(scene.heightProperty());
        pokerShowdownOverlay.setStyle(Styles.overlayBg());
        VBox showdownWrapper = new VBox(10, pokerShowdownContent, closeShowdownButton);
        showdownWrapper.setAlignment(Pos.CENTER);
        pokerShowdownOverlay.getChildren().add(showdownWrapper);
        root.getChildren().add(pokerShowdownOverlay);

        closeShowdownButton.setOnAction(_ -> pokerShowdownOverlay.setVisible(false));

        pokerSitButton.setVisible(false);
        pokerSitButton.setManaged(false);
        pokerAddBotButton.setVisible(false);
        pokerAddBotButton.setManaged(false);
        pokerStartHandButton.setVisible(false);
        pokerStartHandButton.setManaged(false);
        pokerCallButton.setVisible(false);
        pokerCallButton.setManaged(false);
        pokerRaiseButton.setVisible(false);
        pokerRaiseButton.setManaged(false);
        pokerFoldButton.setVisible(false);
        pokerFoldButton.setManaged(false);
        pokerStagedLabel.setVisible(false);
        pokerStagedLabel.setManaged(false);

        Timeline stakeTicker = new Timeline(new KeyFrame(Duration.millis(400), _ -> updateStagedLabel()));
        stakeTicker.setCycleCount(Timeline.INDEFINITE);
        stakeTicker.play();
    }

    /**
     * Returns the JavaFX {@link Scene} object.
     *
     * @return the scene
     */
    public Scene getScene() {
        return scene;
    }

    private void spawnStakeChip(Color color) {
        Chips chip = new Chips(color, tarotBoard.getChipFrontImage(), tarotBoard.getChipBackImage());
        StackPane pane = chip.getChipPane();
        pane.getChildren().get(0).setVisible(true);
        pane.getChildren().get(1).setVisible(false);
        pane.setTranslateX(pokerStakeZone.getLayoutX() + 20 + (stakeChips.size() % 8) * 8);
        pane.setTranslateY(pokerStakeZone.getLayoutY() + 40 + (stakeChips.size() % 8) * 2);
        UIUtils.makeDraggable(pane, tarotBoard, null, null, null);
        stakeChips.add(chip);
        pokerContent.getChildren().add(pane);
    }

    private void confirmAction(String action) {
        long stagedChips = countStakedChips();
        long stagedValue = stagedChips * TarotBoard.POKER_CHIP_VALUE;
        if (stagedChips > 0) {
            moveStakedChipsToPot();
        }
        tarotBoard.pokerAction(action, stagedValue);
        refreshPokerPanel();
    }

    private long countStakedChips() {
        Bounds zoneBounds = pokerStakeZone.localToScene(pokerStakeZone.getBoundsInLocal());
        long count = 0;
        for (Chips chip : stakeChips) {
            Bounds chipBounds = chip.getChipPane().localToScene(chip.getChipPane().getBoundsInLocal());
            if (zoneBounds.intersects(chipBounds)) count++;
        }
        return count;
    }

    private void moveStakedChipsToPot() {
        Bounds zoneBounds = pokerStakeZone.localToScene(pokerStakeZone.getBoundsInLocal());
        Bounds potBounds = pokerPotZone.localToScene(pokerPotZone.getBoundsInLocal());
        var potCenter = pokerContent.sceneToLocal(potBounds.getMinX() + potBounds.getWidth() / 2,
                potBounds.getMinY() + potBounds.getHeight() / 2);
        int i = 0;
        for (Chips chip : stakeChips) {
            StackPane pane = chip.getChipPane();
            Bounds chipBounds = pane.localToScene(pane.getBoundsInLocal());
            if (zoneBounds.intersects(chipBounds)) {
                pane.setTranslateX(potCenter.getX() + (i % 6) * 12);
                pane.setTranslateY(potCenter.getY() + (i / 6) * 12);
                i++;
            }
        }
    }

    private void updateStagedLabel() {
        if (!pokerCallButton.isVisible()) return;
        long chips = countStakedChips();
        pokerStagedLabel.setText("Staked: " + chips + " chip(s) (" + (chips * TarotBoard.POKER_CHIP_VALUE) + " pts)");
    }

    /**
     * Refreshes all poker panel content (buttons, labels) from the current
     * {@link PokerClientState}. Safe to call at any time, including before Poker Mode is active.
     */
    public void refreshPokerPanel() {
        PokerClientState state = tarotBoard.getPokerClientState();
        boolean hasPerms = tarotBoard.isHost() || tarotBoard.isOperator() || !tarotBoard.isMultiplayer();
        boolean active = state.pokerModeActive;

        boolean iAmSeated = state.seats.stream().anyMatch(s -> s.playerId() == tarotBoard.getMyPlayerId());
        pokerSitButton.setVisible(active && !iAmSeated);
        pokerSitButton.setManaged(active && !iAmSeated);

        pokerAddBotButton.setVisible(active && hasPerms);
        pokerAddBotButton.setManaged(active && hasPerms);

        boolean canStartHand = active && hasPerms && state.phase != PokerTable.Phase.BETTING;
        pokerStartHandButton.setVisible(canStartHand);
        pokerStartHandButton.setManaged(canStartHand);

        boolean myTurn = active && state.phase == PokerTable.Phase.BETTING
                && state.currentTurnPlayerId == tarotBoard.getMyPlayerId();
        pokerCallButton.setVisible(myTurn);
        pokerCallButton.setManaged(myTurn);
        pokerRaiseButton.setVisible(myTurn);
        pokerRaiseButton.setManaged(myTurn);
        pokerFoldButton.setVisible(myTurn);
        pokerFoldButton.setManaged(myTurn);
        pokerStagedLabel.setVisible(myTurn);
        pokerStagedLabel.setManaged(myTurn);

        if (!active) {
            pokerStatusLabel.setText("Poker Mode is not active yet.");
            pokerHandLabel.setText("");
            return;
        }

        long myContribution = state.seats.stream()
                .filter(s -> s.playerId() == tarotBoard.getMyPlayerId())
                .mapToLong(PokerClientState.SeatView::contributionThisRound)
                .findFirst().orElse(0);
        long required = Math.max(0, state.currentBet - myContribution);

        StringBuilder sb = new StringBuilder();
        sb.append("Phase: ").append(state.phase).append("\n");
        sb.append("Pot: ").append(state.potTotal).append(" pts | Current bet: ").append(state.currentBet).append(" pts\n");
        sb.append("Seats: ").append(state.seats.size());
        if (myTurn) {
            sb.append("\nYour turn - to call you owe ").append(required).append(" pts");
        } else if (state.phase == PokerTable.Phase.BETTING && state.currentTurnPlayerId >= 0) {
            sb.append("\nWaiting on player ").append(state.currentTurnPlayerId);
        }
        pokerStatusLabel.setText(sb.toString());

        pokerHandLabel.setText(state.myHoleCards.isEmpty() ? "" : "Your hand: " + String.join(", ", state.myHoleCards));
    }

    /**
     * Shows the showdown overlay for a completed hand's results.
     *
     * @param result the showdown result, or {@code null} to leave the overlay untouched
     */
    public void showPokerShowdown(PokerTable.ShowdownResult result) {
        if (result == null) return;
        pokerShowdownContent.getChildren().clear();
        Label title = new Label(result.results().isEmpty() ? "Hand won by fold" : "Showdown");
        title.setStyle(Styles.panelLabel());
        pokerShowdownContent.getChildren().add(title);
        for (var entry : result.results()) {
            boolean isWinner = result.winningPlayerIds().contains(entry.playerId());
            Label row = new Label((isWinner ? "★ " : "") + "Player " + entry.playerId() + ": "
                    + entry.hand().type().name() + " (rank " + entry.hand().type().rank() + ") - "
                    + String.join(", ", entry.hand().cardsUsed()));
            row.setStyle(Styles.panelLabel());
            row.setWrapText(true);
            pokerShowdownContent.getChildren().add(row);
        }
        Label winnerLabel = new Label("Pot of " + result.potWon() + " pts awarded to player(s): "
                + result.winningPlayerIds());
        winnerLabel.setStyle(Styles.panelLabel());
        pokerShowdownContent.getChildren().add(winnerLabel);
        pokerShowdownOverlay.setVisible(true);
        pokerShowdownOverlay.toFront();
    }
}
