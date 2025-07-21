package com.mystic.tarotboard;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.util.*;

public class GameUI {
    private final Stage stage;
    private final BorderPane root;
    private final VBox playersBox;
    private final VBox messagesBox;
    private ClientNetwork clientNetwork;
    private String currentTurn = "";

    // Map player names to their card display
    private final Map<String, HBox> playerHandsMap = new HashMap<>();

    // Action buttons for betting
    private Button btnBet;
    private Button btnRaise;
    private Button btnCall;
    private Button btnFold;
    private Button btnCheck;
    private TextField betAmountField;  // input for bet/raise amount

    // For displaying community cards
    private final HBox communityCardsBox = new HBox(5);

    // For chip counts display
    private final Map<String, Integer> playerChips = new HashMap<>();
    private final Map<String, HBox> playerChipBoxes = new HashMap<>();

    // Pot display label
    private Label potLabel = new Label("Pot: 0");

    public GameUI(Stage stage) {
        this.stage = stage;
        root = new BorderPane();

        playersBox = new VBox(10);
        playersBox.setPadding(new Insets(10));
        playersBox.setPrefHeight(200);
        playersBox.setStyle("-fx-background-color: #2b2b2b;");
        root.setTop(playersBox);

        messagesBox = new VBox(5);
        messagesBox.setPadding(new Insets(10));
        ScrollPane scrollPane = new ScrollPane(messagesBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);

        // Add community cards box to center
        communityCardsBox.setPadding(new Insets(10));
        communityCardsBox.setStyle("-fx-background-color: #222;");
        root.setCenter(communityCardsBox);

        // Pot label styling
        potLabel.setStyle("-fx-text-fill: gold; -fx-font-weight: bold; -fx-font-size: 16px;");

        // Create buttons
        btnBet = new Button("Bet");
        btnRaise = new Button("Raise");
        btnCall = new Button("Call");
        btnFold = new Button("Fold");
        btnCheck = new Button("Check");

        // Create TextField for entering bet/raise amount
        betAmountField = new TextField();
        betAmountField.setPrefWidth(60);
        betAmountField.setPromptText("Amount");

        // Initially disable action buttons and input
        betAmountField.setDisable(true);
        btnBet.setDisable(true);
        btnRaise.setDisable(true);
        btnCall.setDisable(true);
        btnFold.setDisable(true);
        btnCheck.setDisable(true);

        // Controls container includes the bet amount field + all buttons
        HBox controls = new HBox(10, betAmountField, btnBet, btnRaise, btnCall, btnFold, btnCheck);
        controls.setPadding(new Insets(10));
        controls.setAlignment(Pos.CENTER);

        VBox bottomBox = new VBox(5, potLabel, controls, scrollPane);
        root.setBottom(bottomBox);

        // Button event handlers
        btnBet.setOnAction(e -> {
            if (clientNetwork != null) {
                try {
                    int amount = Integer.parseInt(betAmountField.getText().trim());
                    if (amount <= 0) {
                        showMessage("Bet amount must be positive.");
                        return;
                    }
                    clientNetwork.send("BET " + getPlayerName() + " " + amount);
                } catch (NumberFormatException ex) {
                    showMessage("Invalid bet amount.");
                }
            }
        });

        btnRaise.setOnAction(e -> {
            if (clientNetwork != null) {
                try {
                    int amount = Integer.parseInt(betAmountField.getText().trim());
                    if (amount <= 0) {
                        showMessage("Raise amount must be positive.");
                        return;
                    }
                    clientNetwork.send("RAISE " + getPlayerName() + " " + amount);
                } catch (NumberFormatException ex) {
                    showMessage("Invalid raise amount.");
                }
            }
        });

        btnCall.setOnAction(e -> {
            if (clientNetwork != null) {
                clientNetwork.send("CALL " + getPlayerName());
            }
        });

        btnFold.setOnAction(e -> {
            if (clientNetwork != null) {
                clientNetwork.send("FOLD " + getPlayerName());
            }
        });

        btnCheck.setOnAction(e -> {
            if (clientNetwork != null) {
                clientNetwork.send("CHECK " + getPlayerName());
            }
        });

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/com/mystic/tarotboard/assets/styles.css").toExternalForm());

        this.stage.setScene(scene);
        this.stage.setTitle("TarotBoard Poker");
    }

    public void setClientNetwork(ClientNetwork clientNetwork) {
        this.clientNetwork = clientNetwork;
    }

    public Scene getScene() {
        return stage.getScene();
    }

    // MARK PLAYER AS FOLDED — Grey out their hand and add a folded label
    public void markPlayerFolded(String playerName) {
        HBox handBox = playerHandsMap.get(playerName);
        if (handBox == null) return;

        handBox.setStyle("-fx-background-color: #555555; -fx-opacity: 0.5; -fx-border-color: darkred; -fx-border-width: 2;");

        for (Node parent : playersBox.getChildren()) {
            if (parent instanceof VBox vbox) {
                if (!vbox.getChildren().isEmpty()) {
                    Node firstChild = vbox.getChildren().get(0);
                    if (firstChild instanceof HBox infoBox) {
                        for (Node node : infoBox.getChildren()) {
                            if (node instanceof Label label && label.getText().equals(playerName)) {
                                Label foldedLabel = null;
                                for (Node n : infoBox.getChildren()) {
                                    if ("foldedLabel".equals(n.getId())) {
                                        foldedLabel = (Label) n;
                                        break;
                                    }
                                }
                                if (foldedLabel == null) {
                                    foldedLabel = new Label(" (Folded)");
                                    foldedLabel.setId("foldedLabel");
                                    foldedLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                                    infoBox.getChildren().add(foldedLabel);
                                }
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    // UPDATE PLAYERS — Reset UI for players list
    public void updatePlayers(List<String> players) {
        playersBox.getChildren().clear();
        playerHandsMap.clear();
        playerChipBoxes.clear();
        playerChips.clear();

        for (String player : players) {
            Label nameLabel = new Label(player);
            nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

            Label chipCountLabel = new Label("Chips: 1000"); // default chips
            chipCountLabel.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");

            HBox chipStack = new HBox(3);
            chipStack.setAlignment(Pos.CENTER_LEFT);

            playerChipBoxes.put(player, chipStack);

            HBox playerInfo = new HBox(10, nameLabel, chipCountLabel, chipStack);
            playerInfo.setAlignment(Pos.CENTER_LEFT);

            HBox handBox = new HBox(5);
            handBox.setAlignment(Pos.CENTER_LEFT);
            handBox.setPadding(new Insets(5));
            handBox.setStyle("-fx-background-color: #444; -fx-border-color: black;");

            VBox playerBox = new VBox(5, playerInfo, handBox);
            playerBox.setPadding(new Insets(5));
            playerBox.setStyle("-fx-border-color: gray; -fx-border-width: 1;");

            playersBox.getChildren().add(playerBox);
            playerHandsMap.put(player, handBox);

            playerChips.put(player, 1000);
            updateChipDisplay(player);
        }
        showMessage("Players updated: " + String.join(", ", players));
    }

    // UPDATE CHIP DISPLAY FOR PLAYER
    public void updateChipDisplay(String playerName) {
        HBox chipStack = playerChipBoxes.get(playerName);
        if (chipStack == null) return;
        chipStack.getChildren().clear();

        int totalChips = playerChips.getOrDefault(playerName, 0);

        int[] chipValues = {100, 25, 10, 5, 1};

        for (int value : chipValues) {
            int count = totalChips / value;
            totalChips %= value;

            for (int i = 0; i < count; i++) {
                StackPane chipPane = createChipPane(value);
                chipStack.getChildren().add(chipPane);
            }
        }
    }

    private StackPane createChipPane(int value) {
        double chipRadius = 15;

        Circle circle = new Circle(chipRadius);
        circle.setFill(Color.DARKRED);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(1);

        Text valueText = new Text(String.valueOf(value));
        valueText.setFill(Color.WHITE);
        valueText.setFont(Font.font("Arial", 12));
        valueText.setMouseTransparent(true);

        StackPane chipPane = new StackPane(circle, valueText);
        chipPane.setPrefSize(chipRadius * 2, chipRadius * 2);

        Translate translate = new Translate();
        chipPane.getTransforms().add(translate);

        return chipPane;
    }

    // UPDATE PLAYER'S HAND DISPLAY
    public void updatePlayerHand(String playerName, List<TarotBoardPoker.Card> cards) {
        HBox handBox = playerHandsMap.get(playerName);
        if (handBox == null) return;
        handBox.getChildren().clear();

        for (TarotBoardPoker.Card card : cards) {
            CardView cardView = new CardView(card);
            cardView.setUserData(card);

            handBox.getChildren().add(cardView);
        }
    }

    // SET CURRENT TURN AND HIGHLIGHT
    public void setCurrentTurn(String playerName) {
        currentTurn = playerName;
        showMessage("It's " + playerName + "'s turn.");

        for (Map.Entry<String, HBox> entry : playerHandsMap.entrySet()) {
            String player = entry.getKey();
            HBox handBox = entry.getValue();

            if (player.equals(playerName)) {
                handBox.setStyle("-fx-background-color: #6666ff; -fx-border-color: gold; -fx-border-width: 2;");
            } else {
                handBox.setStyle("-fx-background-color: #444; -fx-border-color: black;");
            }
        }

        boolean isMyTurn = getPlayerName().equals(playerName);
        betAmountField.setDisable(!isMyTurn);
        btnBet.setDisable(!isMyTurn);
        btnRaise.setDisable(!isMyTurn);
        btnCall.setDisable(!isMyTurn);
        btnFold.setDisable(!isMyTurn);
        btnCheck.setDisable(!isMyTurn);
    }

    // UPDATE PLAYER BET AND CHIPS
    public void updatePlayerBet(String playerName, int amount) {
        showMessage(playerName + " bets " + amount);
        int currentChips = playerChips.getOrDefault(playerName, 0);
        playerChips.put(playerName, Math.max(0, currentChips - amount));
        updateChipDisplay(playerName);
    }

    // SHOW MESSAGE IN MESSAGE BOX
    public void showMessage(String msg) {
        Text text = new Text(msg);
        text.setStyle("-fx-fill: white;");
        messagesBox.getChildren().add(text);

        if (messagesBox.getParent() instanceof ScrollPane sp) {
            sp.layout();
            sp.setVvalue(1.0);
        }
        System.out.println(msg);
    }

    public void updateCommunityCards(List<TarotBoardPoker.Card> cards) {
        Platform.runLater(() -> {
            communityCardsBox.getChildren().clear();
            for (TarotBoardPoker.Card card : cards) {
                CardView cardView = new CardView(card);
                communityCardsBox.getChildren().add(cardView);
            }
        });
    }

    // Helper to get the player's own name from window title
    private String getPlayerName() {
        return stage.getTitle().replace("TarotBoard Poker - ", "");
    }

    // ======= New methods you asked for =======

    /**
     * Called to start a new round:
     * Clears community cards and resets player hands display.
     *
     * @param playerOrder list of players in the round
     */
    public void startNewRound(List<String> playerOrder) {
        Platform.runLater(() -> {
            // Clear community cards
            communityCardsBox.getChildren().clear();

            // Reset player hands display (clear cards)
            for (String player : playerOrder) {
                HBox handBox = playerHandsMap.get(player);
                if (handBox != null) {
                    handBox.getChildren().clear();
                    handBox.setStyle("-fx-background-color: #444; -fx-border-color: black;");
                }
            }

            // Remove folded labels and restore opacity for all players
            for (Node playerNode : playersBox.getChildren()) {
                if (playerNode instanceof VBox vbox && !vbox.getChildren().isEmpty()) {
                    Node infoNode = vbox.getChildren().get(0);
                    if (infoNode instanceof HBox infoBox) {
                        infoBox.getChildren().removeIf(n -> "foldedLabel".equals(n.getId()));
                    }
                }
            }

            updatePot(0);
            showMessage("New round started.");
        });
    }

    /**
     * Enables or disables all action buttons (Bet, Raise, Call, Fold, Check) and bet input
     *
     * @param enabled true to enable, false to disable
     */
    public void enableActions(boolean enabled) {
        Platform.runLater(() -> {
            betAmountField.setDisable(!enabled);
            btnBet.setDisable(!enabled);
            btnRaise.setDisable(!enabled);
            btnCall.setDisable(!enabled);
            btnFold.setDisable(!enabled);
            btnCheck.setDisable(!enabled);
        });
    }

    /**
     * Updates UI to show a player's last action (bet, raise, fold, etc.)
     * Displays a message and highlights the player's hand briefly.
     *
     * @param playerName player's name
     * @param action     action string, e.g. "raises", "folds", "calls"
     */
    public void updatePlayerAction(String playerName, String action) {
        Platform.runLater(() -> {
            showMessage(playerName + " " + action + ".");

            HBox handBox = playerHandsMap.get(playerName);
            if (handBox != null) {
                String originalStyle = handBox.getStyle();
                handBox.setStyle("-fx-background-color: #ffcc00; -fx-border-color: gold; -fx-border-width: 3;");
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                    Platform.runLater(() -> handBox.setStyle(originalStyle));
                }).start();
            }
        });
    }

    /**
     * Update the displayed pot amount.
     *
     * @param potAmount the current pot value
     */
    public void updatePot(int potAmount) {
        Platform.runLater(() -> potLabel.setText("Pot: " + potAmount));
    }

}
