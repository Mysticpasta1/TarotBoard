package com.mystic.tarotboard;

import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class Solitaire {
    private static List<StackPane> tableauPiles;
    private static StackPane stockPile;
    private static StackPane wastePile;
    private static List<StackPane> foundationPiles;
    private static final double CARD_WIDTH = 150;
    private static final double CARD_HEIGHT = 200;
    private static List<Card> stockCards;
    private static final int NUM_CARDS = 1056;
    private static StackPane collectPile; // New pile for collecting full suits
    private static boolean gameStarted = false; // Add a flag to track game state

    // Method to create a placeholder for empty stacks (for foundation and tableau piles)
    private static Rectangle createPlaceholder() {
        Rectangle placeholder = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
        placeholder.setArcWidth(10); // Rounded corners
        placeholder.setArcHeight(10);
        placeholder.setStyle("-fx-fill: transparent; -fx-stroke: lightgray; -fx-stroke-dash-array: 5 5;"); // Dashed border
        return placeholder;
    }

    // Set up Solitaire game mode
    public static void switchToSolitaireMode(Stage primaryStage) {
        Pane solitaireRoot = new Pane();
        Scene solitaireScene = new Scene(solitaireRoot, primaryStage.getWidth(), primaryStage.getHeight());

        // Set background for the solitaire game
        Image backgroundImage = new Image(Objects.requireNonNull(Solitaire.class.getResourceAsStream("/com/mystic/tarotboard/assets/background_image.png")));
        BackgroundSize backgroundSize = new BackgroundSize(solitaireScene.getWidth(), solitaireScene.getHeight(), false, false, true, false);
        BackgroundImage bgImage = new BackgroundImage(backgroundImage, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.CENTER, backgroundSize);
        solitaireRoot.setBackground(new Background(bgImage));

        // Set up the tableau, stockpile, waste pile, and foundations
        setupSolitaireBoard(solitaireRoot);

        primaryStage.setScene(solitaireScene);
        primaryStage.setTitle("Solitaire Mode");
    }

    private static void checkAndCollectFullSuits() {
        // Iterate over all foundation piles
        for (StackPane foundation : foundationPiles) {
            // Check if the foundation pile contains a complete suit
            if (foundation.getChildren().size() == 33) { // Assuming customValues.length is the number of cards in a suit
                // Ensure that all cards in the pile belong to the same suit
                boolean isCompleteSuit = true;
                Card firstCard = (Card) foundation.getChildren().get(0).getUserData();
                String suit = firstCard.getSuit();

                for (Node node : foundation.getChildren()) {
                    if (node instanceof StackPane cardPane) {
                        Card card = (Card) cardPane.getUserData();
                        if (!card.getSuit().equals(suit)) {
                            isCompleteSuit = false;
                            break;
                        }
                    }
                }

                // If it's a complete suit, move it to the collect pile
                if (isCompleteSuit) {
                    // Move the complete suit to the collect pile
                    List<Node> completeSuit = new ArrayList<>(foundation.getChildren());
                    foundation.getChildren().clear();
                    collectPile.getChildren().addAll(completeSuit);
                }
            }
        }
    }

    private static void setupSolitaireBoard(Pane root) {
        tableauPiles = new ArrayList<>();
        stockPile = new StackPane();
        wastePile = new StackPane();
        foundationPiles = new ArrayList<>();
        collectPile = new StackPane(); // Initialize collect pile

        // Set up tableau piles
        for (int i = 0; i < 8; i++) {
            StackPane tableau = new StackPane();
            tableau.setLayoutX(100 + (i * (CARD_WIDTH + 20)));  // Adjust spacing
            tableau.setLayoutY(300);
            tableauPiles.add(tableau);

            // Add a placeholder to indicate empty stack
            Rectangle placeholder = createPlaceholder();
            tableau.getChildren().add(placeholder);

            root.getChildren().add(tableau);
        }

        // Set up stockpile
        stockPile.setLayoutX(50); // Adjusted to prevent overlapping with foundations
        stockPile.setLayoutY(50);

        // Add a placeholder for the stockpile
        Rectangle stockPlaceholder = createPlaceholder();
        stockPile.getChildren().add(stockPlaceholder);

        root.getChildren().add(stockPile);

        // Set up waste pile
        wastePile.setLayoutX(250); // Placed to the right of the stockpile
        wastePile.setLayoutY(50);

        // Add a placeholder for the waste pile
        Rectangle wastePlaceholder = createPlaceholder();
        wastePile.getChildren().add(wastePlaceholder);

        root.getChildren().add(wastePile);

        // Set up foundation piles
        for (int i = 0; i < 4; i++) {
            StackPane foundation = new StackPane();
            foundation.setLayoutX(500 + (i * (CARD_WIDTH + 20))); // Adjusted to start further right to avoid overlap
            foundation.setLayoutY(50);
            foundationPiles.add(foundation);

            // Add a placeholder to indicate empty stack
            Rectangle foundationPlaceholder = createPlaceholder();
            foundation.getChildren().add(foundationPlaceholder);

            root.getChildren().add(foundation);
        }

        // Set up collect pile (positioned to the right of foundation piles)
        collectPile.setLayoutX(1360); // Adjust this to place it properly in your layout
        collectPile.setLayoutY(50);

        // Add a placeholder for the collect pile
        Rectangle collectPlaceholder = createPlaceholder();
        collectPile.getChildren().add(collectPlaceholder);

        root.getChildren().add(collectPile);

        // Add cards to tableau and stockpile
        setupSolitaireCards();
        setupStockpileClick();

        startGame();
        // Reset the gameStarted flag after initial setup
        gameStarted = true; // Set this flag to true only after the game has been set up
    }

    private static boolean checkWinCondition() {
        // Win condition: All cards must be in the collect pile
        return collectPile.getChildren().size() == NUM_CARDS; // Assuming NUM_CARDS is the total number of cards in the game
    }

    private static boolean checkLoseCondition() {
        // Check for any possible valid moves in the tableau or waste pile
        for (StackPane tableau : tableauPiles) {
            for (Node node : tableau.getChildren()) {
                if (node instanceof StackPane cardPane) {
                    Card card = (Card) cardPane.getUserData();
                    if (isAnyValidMove(card)) {
                        return false; // Found a valid move, not a loss
                    }
                }
            }
        }
        // Check the waste pile for possible moves
        for (Node node : wastePile.getChildren()) {
            if (node instanceof StackPane cardPane) {
                Card card = (Card) cardPane.getUserData();
                if (isAnyValidMove(card)) {
                    return false; // Found a valid move, not a loss
                }
            }
        }
        return true; // No valid moves left
    }

    // Helper method to check if there is any valid move for a given card
    private static boolean isAnyValidMove(Card card) {
        // Check if the card can be moved to any tableau pile
        for (StackPane tableau : tableauPiles) {
            if (isValidMove(card, tableau)) {
                return true; // Found a valid move
            }
        }

        // Check if the card can be moved to any foundation pile
        for (StackPane foundation : foundationPiles) {
            if (isValidMove(card, foundation)) {
                return true; // Found a valid move
            }
        }
        return false; // No valid moves found
    }

    private static void handleGameOver(boolean won) {
        if (won) {
            System.out.println("Congratulations! You've won!");
            // Show a win message to the user
            // You can use a dialog, alert, or any other UI element to show a win message
        } else {
            System.out.println("No more valid moves. You lose!");
            // Show a loser message to the user
            // You can use a dialog, alert, or any other UI element to show a loser message
        }

        // Reset the game for a new round
        reshuffleAndResetGame();
        gameStarted = false;
    }

    private static void setupSolitaireCards() {
        // Use the custom suits and values
        String[] customSuits = {
                "Arcs", "Arrows", "Clouds", "Clovers", "Comets", "Crescents", "Crosses",
                "Crowns", "Diamonds", "Embers", "Eyes", "Gears", "Glyphs", "Flames", "Flowers",
                "Hearts", "Keys", "Locks", "Leaves", "Mountains", "Points", "Scrolls", "Shells",
                "Shields", "Spades", "Spirals", "Stars", "Suns", "Swords", "Tridents", "Trees", "Waves"
        };
        String[] customValues = {
                "(0) Hold", "(1) Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                "(11) Jack", "(12) Queen", "(13) King", "(14) Nomad", "(15) Prince",
                "(16) Rune", "(17) Fable", "(18) Sorceress", "(19) Utopia", "(20) Wizard",
                "(21) Titan", "(22) Baron", "(23) Illusionist", "(24) Oracle", "(25) Magician",
                "(26) Luminary", "(27) Eclipse", "(28) Celestial", "(29) Duke", "(30) Genesis",
                "(31) Zephyr", "(32) Vesper"
        };

        // Create and shuffle the custom deck
        stockCards = new ArrayList<>();
        for (String suit : customSuits) {
            for (String value : customValues) {
                Card card = new Card(value, suit);
                stockCards.add(card); // Add cards as face-down by default
            }
        }
        Collections.shuffle(stockCards);

        // Add all cards to the stockpile initially (face-down)
        for (Card card : stockCards) {
            StackPane cardPane = createCardPane(card);
            stockPile.getChildren().add(cardPane);
        }

        // Clear tableau as all cards start in the stockpile
        tableauPiles.forEach(pile -> pile.getChildren().clear());
    }

    public static void startGame() {
        // Initialize or reset the game
        reshuffleAndResetGame();
    }


    private static void reshuffleAndResetGame() {
        // Collect all existing card panes into a list for reshuffling
        List<Card> allCards = new ArrayList<>();

        // Clear tableau piles and stockpile before reshuffling
        for (StackPane tableau : tableauPiles) {
            for (Node node : tableau.getChildren()) {
                if (node instanceof StackPane cardPane) {
                    Card card = (Card) cardPane.getUserData();
                    allCards.add(card); // Collect the card for reshuffling
                }
            }
            tableau.getChildren().clear();
        }

        // Collect cards from the stockpile
        for (Node node : stockPile.getChildren()) {
            if (node instanceof StackPane cardPane) {
                Card card = (Card) cardPane.getUserData();
                allCards.add(card); // Collect the card for reshuffling
            }
        }

        stockPile.getChildren().clear();

        // Shuffle the cards
        Collections.shuffle(allCards);

        // Reset the card visuals and add them back to the stockpile
        for (Card card : allCards) {
            StackPane cardPane = createCardPane(card);
            cardPane.setTranslateX(0);
            cardPane.setTranslateY(0);
            stockPile.getChildren().add(cardPane);
        }

        // Redistribute cards to tableau
        distributeCardsToTableau();
    }

    private static void distributeCardsToTableau() {
        // Deal cards to the tableau piles (same as in setupSolitaireCards)
        int cardsPerPile = Math.min(stockCards.size() / tableauPiles.size(), 13); // Adjust this value as needed
        for (StackPane tableauPile : tableauPiles) {
            for (int j = 0; j < cardsPerPile; j++) {
                if (stockPile.getChildren().isEmpty()) break;
                StackPane cardPane = (StackPane) stockPile.getChildren().remove(stockPile.getChildren().size() - 1);
                cardPane.setTranslateY(j * 30); // Offset cards in tableau
                tableauPile.getChildren().add(cardPane);

                // Flip the last card in each pile
                if (j == cardsPerPile - 1) {
                    Card card = (Card) cardPane.getUserData();
                    card.flip();
                    cardPane.getChildren().get(1).setVisible(true); // Show front image
                    Text cardNameText = (Text) cardPane.getChildren().get(2);
                    cardNameText.setVisible(true); // Show the text overlay on the front
                }
            }
        }
    }

    private static StackPane createCardPane(Card card) {
        StackPane cardPane = new StackPane();

        // Load the custom images for the card fronts and backs
        Image cardFrontImage = new Image(Objects.requireNonNull(Solitaire.class.getResourceAsStream("/com/mystic/tarotboard/assets/card_front.png")));
        Image cardBackImage = new Image(Objects.requireNonNull(Solitaire.class.getResourceAsStream("/com/mystic/tarotboard/assets/card_back.png")));

        // Define suits that are considered "red"
        Text cardNameText = getText(card);

        // Card back
        ImageView cardBackImageView = new ImageView(cardBackImage);
        cardBackImageView.setFitWidth(CARD_WIDTH);
        cardBackImageView.setFitHeight(CARD_HEIGHT);
        cardBackImageView.setVisible(!card.isFaceUp()); // Show the back if the card is face-down

        // Card front
        ImageView cardFrontImageView = new ImageView(cardFrontImage);
        cardFrontImageView.setFitWidth(CARD_WIDTH);
        cardFrontImageView.setFitHeight(CARD_HEIGHT);
        cardFrontImageView.setVisible(card.isFaceUp()); // Show the front if the card is face-up

        // Add images and text to card pane
        cardPane.getChildren().addAll(cardBackImageView, cardFrontImageView, cardNameText);
        cardPane.setUserData(card);

        // Make the card draggable and auto-move
        makeDraggable(cardPane, card);

        return cardPane;
    }

    private static Text getText(Card card) {
        Set<String> redSuits = Card.RED_SUITS; // Add more suits as needed

        // Determine the text color based on the suit
        String textColor = redSuits.contains(card.getSuit()) ? "red" : "lightblue";

        // Create card name text
        Text cardNameText = new Text(card.getValue() + " of " + card.getSuit());
        cardNameText.setStyle("-fx-font-size: 15pt; -fx-fill: " + textColor + ";");
        cardNameText.setBoundsType(TextBoundsType.VISUAL); // Use visual bounds to get accurate text size
        cardNameText.setWrappingWidth(CARD_WIDTH); // Use the card width for centering
        cardNameText.setTextAlignment(TextAlignment.CENTER);
        cardNameText.setVisible(card.isFaceUp()); // Show the card name only if face-up
        return cardNameText;
    }

    // Method to make cards draggable with animation
    private static void makeDraggable(StackPane pane, Card card) {
        final double[] dragDeltaX = new double[1];
        final double[] dragDeltaY = new double[1];
        final double[] originalX = new double[1];
        final double[] originalY = new double[1];

        pane.setOnMousePressed(event -> {
            // Find the index of the clicked card in its tableau
            StackPane parentPane = (StackPane) pane.getParent();
            int cardIndex = parentPane.getChildren().indexOf(pane);

            // Only allow dragging if the card is face-up and forms a valid sequence
            if (!card.isFaceUp() || isNotMovable(card, parentPane)) {
                return;
            }

            // Bring the card and all cards above it to the front
            for (int i = cardIndex; i < parentPane.getChildren().size(); i++) {
                Node node = parentPane.getChildren().get(i);
                node.toFront();
            }

            dragDeltaX[0] = event.getSceneX() - pane.getTranslateX();
            dragDeltaY[0] = event.getSceneY() - pane.getTranslateY();
            pane.translateXProperty().unbind(); // Unbind translateX while dragging
            pane.toFront(); // Bring the card to the front while dragging

            if (!isNotMovable(card, (StackPane) pane.getParent())) {
                originalX[0] = pane.getTranslateX();
                originalY[0] = pane.getTranslateY();
                pane.toFront(); // Bring the card to the front while dragging
            }
        });

        pane.setOnMouseDragged(event -> {
            StackPane parentPane = (StackPane) pane.getParent();
            int cardIndex = parentPane.getChildren().indexOf(pane);

            // Only drag if the card is allowed to move
            if (!card.isFaceUp() || isNotMovable(card, parentPane)) {
                return;
            }

            // Move the card and all cards above it
            for (int i = cardIndex; i < parentPane.getChildren().size(); i++) {
                Node node = parentPane.getChildren().get(i);
                node.setTranslateX(event.getSceneX() - dragDeltaX[0]);
                node.setTranslateY(event.getSceneY() - dragDeltaY[0] + (i - cardIndex) * 30); // Maintain offset for the sequence
            }
        });

        pane.setOnMouseReleased(event -> {
            // Handle card drop logic to snap to the correct pile
            handleCardDrop(pane, card, originalX[0], originalY[0]);
        });
    }

    // Method to animate the card snapping back to its original position
    private static void animateSnapBack(StackPane cardPane, double originalX, double originalY) {
        // Create a transition to move the card back to its original position
        TranslateTransition snapBack = new TranslateTransition(Duration.millis(300), cardPane);
        snapBack.setToX(originalX); // Snap back to the original X position
        snapBack.setToY(originalY); // Snap back to the original Y position
        snapBack.play();
    }

    private static void handleCardDrop(StackPane cardPane, Card card, double originalX, double originalY) {
        StackPane parentPane = (StackPane) cardPane.getParent();
        int cardIndex = parentPane.getChildren().indexOf(cardPane);

        if(!card.isFaceUp()) {
            return;
        }

        // Ensure the card being moved is the top card or a valid sequence in the tableau
        if (isNotMovable(card, parentPane)) {
            // Invalid move; snap the card back to its original position
            animateSnapBack(cardPane, originalX, originalY);
            return;
        }

        // List to keep track of all cards being moved
        List<Node> cardsToMove = new ArrayList<>(parentPane.getChildren().subList(cardIndex, parentPane.getChildren().size()));
        boolean moveSuccessful = false;

        // Try to place the card on a tableau pile
        for (StackPane tableau : tableauPiles) {
            if (isValidMove(card, tableau)) {
                // Remove the cards from the current tableau
                parentPane.getChildren().removeAll(cardsToMove);

                // Add the cards to the new tableau pile with the correct visual offset
                double offset = tableau.getChildren().size() * 30; // Adjust this value for spacing
                for (Node node : cardsToMove) {
                    StackPane movePane = (StackPane) node;
                    movePane.setTranslateX(0);
                    movePane.setTranslateY(offset);
                    tableau.getChildren().add(movePane);
                    offset += 30; // Maintain offset for the sequence
                }
                moveSuccessful = true;

                // Flip the previous top card if uncovered
                flipTopCard(parentPane);
                break;
            }
        }

        // Try to place the card on a foundation pile if not already moved
        if (!moveSuccessful) {
            for (StackPane foundation : foundationPiles) {
                if (isValidMoveToFoundation(card, foundation)) {
                    // Only move a single card to the foundation pile (cannot move sequences here)
                    if (cardsToMove.size() == 1) {
                        parentPane.getChildren().remove(cardPane);

                        // Set the card's offset relative to the foundation
                        cardPane.setTranslateX(0);
                        cardPane.setTranslateY(0); // Cards in foundation typically stack directly on top of each other
                        foundation.getChildren().add(cardPane);
                        moveSuccessful = true;

                        // Flip the previous top card if uncovered
                        flipTopCard(parentPane);
                    }
                    break;
                }
            }
        }

        // If no valid move, return the cards to their original positions
        if (!moveSuccessful) {
            animateSnapBack(cardPane, originalX, originalY);
        } else {
            // Check and collect full suits after a successful move
            checkAndCollectFullSuits();

            // Hide placeholder of the new parent stack if it has cards
            if (parentPane.getChildren().isEmpty()) {
                togglePlaceholderVisibility(parentPane);
            }
        }

        // Check win condition after every move
        if (gameStarted) { // Only check for game-over conditions if the game has started
            if (checkWinCondition()) {
                handleGameOver(true); // Player has won
            } else if (checkLoseCondition()) {
                handleGameOver(false); // Player has lost
            }
        }
    }

    private static boolean isNotMovable(Card card, StackPane pane) {
        // Find the StackPane containing this card by comparing user data
        int cardIndex = -1;
        for (int i = 0; i < pane.getChildren().size(); i++) {
            Node child = pane.getChildren().get(i);
            if (child instanceof StackPane stackPane) {
                if (stackPane.getUserData() instanceof Card containedCard) {
                    if (containedCard == card) {
                        cardIndex = i;
                        break;
                    }
                }
            }
        }

        if (cardIndex == -1) {
            return true; // Card is not found in the pane, so it's not movable
        }

        // Check if the card is the top card in the tableau pile or the waste pile
        if (cardIndex == pane.getChildren().size() - 1) {
            // Determine if this pane is part of the tableau or waste pile
            if (isTableauPile(pane) || isWastePile(pane)) {
                return false; // The card is movable
            }
        }

        // Check if the card is part of a valid sequence that can be moved to another tableau pile
        if (isTableauPile(pane) && isPartOfValidSequence(card, pane)) {
            return false; // It's movable if it's part of a valid sequence in tableau
        }

        // Other checks for moving to foundation piles
        if (isWastePile(pane) || isTableauPile(pane)) {
            return !canMoveToFoundation(card); // Return the opposite to allow movability
        }

        // In all other cases, the card is not movable
        return true;
    }

    // Helper method to check if the pane is part of the tableau piles
    private static boolean isTableauPile(StackPane pane) {
        return tableauPiles.contains(pane);
    }

    // Helper method to check if the pane is the waste pile
    private static boolean isWastePile(StackPane pane) {
        return pane == wastePile; // Assuming wastePile is a single StackPane
    }

    // Helper method to check if the card forms a valid sequence that can be moved to another tableau pile
    private static boolean isPartOfValidSequence(Card card, StackPane parentPane) {
        // Find the index of the card in the stack
        int cardIndex = parentPane.getChildren().indexOf(card.getCardPane());
        if (cardIndex == -1) {
            return false; // Card not found in the pane
        }

        // Iterate through the cards above the clicked card to ensure they form a valid sequence
        for (int i = cardIndex; i < parentPane.getChildren().size() - 1; i++) {
            StackPane currentPane = (StackPane) parentPane.getChildren().get(i);
            StackPane nextPane = (StackPane) parentPane.getChildren().get(i + 1);

            // Ensure both are Card panes
            if (!(currentPane.getUserData() instanceof Card currentCard) || !(nextPane.getUserData() instanceof Card nextCard)) {
                return false;
            }

            // Check for alternating colors and descending rank
            if (currentCard.getRank() - 1 != nextCard.getRank() || currentCard.getColor().equals(nextCard.getColor())) {
                return false; // Invalid sequence
            }
        }

        // If all checks pass, it's a valid sequence
        return true;
    }

    // Helper method to check if a card can be moved to a foundation pile
    private static boolean canMoveToFoundation(Card card) {
        for (StackPane foundation : foundationPiles) {
            if (isValidMoveToFoundation(card, foundation)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isValidMoveToFoundation(Card card, StackPane foundationPile) {
        if (foundationPile.getChildren().isEmpty()) {
            // Only the lowest rank (e.g., "0 Hold") can be moved to an empty foundation pile
            return card.getRank() == 0;
        } else {
            // Check the top card on the foundation pile
            Node topNode = foundationPile.getChildren().get(foundationPile.getChildren().size() - 1);
            if (topNode instanceof StackPane topCardPane && topCardPane.getUserData() instanceof Card topCard) {
                // Check for the same suit and ascending order
                return topCard.getSuit().equals(card.getSuit()) && (topCard.getRank() == card.getRank() - 1);
            }
        }
        return false;
    }

    // Method to toggle the placeholder visibility based on the stack's state
    private static void togglePlaceholderVisibility(Pane stack) {
        stack.getChildren().get(0).setVisible(stack.getChildren().size() == 1); // Show placeholder
    }

    // Method to animate the card moving to the target pile
    private static void animateToPosition(StackPane cardPane, StackPane targetPile) {
        // Calculate the target position relative to the scene
        Bounds targetBounds = targetPile.localToScene(targetPile.getBoundsInLocal());
        Bounds cardBounds = cardPane.localToScene(cardPane.getBoundsInLocal());

        // Calculate the translation distances
        double translateX = targetBounds.getMinX() - cardBounds.getMinX();
        double translateY = targetBounds.getMinY() - cardBounds.getMinY();

        // Animate the card moving to the target pile
        TranslateTransition moveToPile = new TranslateTransition(Duration.millis(300), cardPane);
        moveToPile.setByX(translateX);
        moveToPile.setByY(translateY);
        moveToPile.play();

        // After the animation, set the card's position relative to the target pile
        moveToPile.setOnFinished(event -> {
            // Clear any translate transformations applied during the animation
            cardPane.setTranslateX(0);
            cardPane.setTranslateY(0);

            // Move the cardPane to the target pile
            targetPile.getChildren().add(cardPane);
        });
    }

    private static boolean isValidMove(Card card, StackPane targetPile) {
        // Moving to an empty tableau pile
        if (tableauPiles.contains(targetPile)) {
            if (targetPile.getChildren().isEmpty()) {
                // Allow any card to be moved to an empty tableau pile, usually this should be the highest rank card (e.g., "32 Vesper")
                return true; // Modify if your rules require a specific rank for the first card in the tableau
            } else {
                // Check the top card on the tableau
                Node topNode = targetPile.getChildren().get(targetPile.getChildren().size() - 1);
                if (topNode instanceof StackPane topCardPane) {
                    Card topCard = (Card) topCardPane.getUserData();

                    // Check for alternating colors and descending order
                    // topCard rank must be exactly one greater than the moving card's rank
                    return !topCard.getColor().equals(card.getColor()) && (topCard.getRank() == card.getRank() + 1);
                }
            }
        }

        // Moving to a foundation pile
        if (foundationPiles.contains(targetPile)) {
            if (targetPile.getChildren().isEmpty()) {
                // Only the lowest rank (e.g., "0 Hold") can be moved to an empty foundation pile
                return card.getRank() == 0; // Ensure that only "0 Hold" starts the foundation
            } else {
                // Check the top card on the foundation
                Node topNode = targetPile.getChildren().get(targetPile.getChildren().size() - 1);
                if (topNode instanceof StackPane topCardPane) {
                    Card topCard = (Card) topCardPane.getUserData();

                    // Check for the same suit and ascending order
                    return topCard.getSuit().equals(card.getSuit()) && (topCard.getRank() == card.getRank() - 1);
                }
            }
        }

        return false; // Invalid move to this pile
    }

    private static void flipTopCard(Pane parent) {
        if (parent.getChildren().isEmpty()) {
            return; // Nothing to flip if the pile is empty
        }

        // Get the new top card (last card in the pane)
        Node node = parent.getChildren().get(parent.getChildren().size() - 1);
        if (node instanceof StackPane topCardPane) {
            Card topCard = (Card) topCardPane.getUserData();

            // Flip the card if it's face-down
            if (!topCard.isFaceUp()) {
                topCard.flip();
                // Ensure the visual representation is updated
                topCardPane.getChildren().get(2).setVisible(true); //Show text
                topCardPane.getChildren().get(1).setVisible(true); // Show the front
                topCardPane.getChildren().get(0).setVisible(false); // Hide the back
            }
        }
    }


    private static void drawFromStock() {
        if (!stockPile.getChildren().isEmpty()) {
            // Move the top card from the stockpile to the waste pile
            StackPane cardPane = (StackPane) stockPile.getChildren().remove(stockPile.getChildren().size() - 1);
            Card card = (Card) cardPane.getUserData();

            // Flip the card face-up if it is not already
            if (!card.isFaceUp()) {
                card.flip(); // Change the card's state to face-up
                cardPane.getChildren().get(1).setVisible(true); // Show front
                cardPane.getChildren().get(0).setVisible(false); // Hide back
                Text cardNameText = (Text) cardPane.getChildren().get(2);
                cardNameText.setVisible(true); // Show the card name
            }

            // Add the card to the waste pile
            wastePile.getChildren().add(cardPane);
            cardPane.setTranslateX(0);
            cardPane.setTranslateY(0);
        } else {
            // If stockpile is empty, reset the waste pile back to the stockpile
            while (!wastePile.getChildren().isEmpty()) {
                StackPane cardPane = (StackPane) wastePile.getChildren().remove(wastePile.getChildren().size() - 1);
                Card card = (Card) cardPane.getUserData();

                // Flip the card back face-down if it was face-up
                if (card.isFaceUp()) {
                    card.flip(); // Change the card's state to face-down
                    cardPane.getChildren().get(1).setVisible(false); // Hide front
                    cardPane.getChildren().get(0).setVisible(true); // Show back
                    Text cardNameText = (Text) cardPane.getChildren().get(2);
                    cardNameText.setVisible(false); // Hide the card name
                }

                // Add back to the stockpile
                stockPile.getChildren().add(cardPane);
            }
        }
    }

    private static void setupStockpileClick() {
        stockPile.setOnMouseClicked(event -> drawFromStock());
    }
}
