package com.mystic.pcg;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Collections;
import java.util.Objects;

public class SolitaireApplication extends Application {

    private static final int NUM_CARDS = 1024;
    private static final double CARD_WIDTH = 200;
    private static final double CARD_HEIGHT = 300;
    private static final int NUM_WIN_PILES = 32;
    private static final int NUM_ROWS = 8;
    private static boolean moving = false;

    @Override
    public void start(Stage primaryStage) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        Pane root = new Pane();
        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
        String[] cardNames = generateShuffledCardNames();

        // Create a single pane for the card stack
        Pane cardStack = new Pane();
        root.getChildren().add(cardStack);

        Image backgroundImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/background_image.png")));
        BackgroundSize backgroundSize = new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false);
        BackgroundImage background = new BackgroundImage(backgroundImage, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT, backgroundSize);
        root.setBackground(new Background(background));

        // Create and position cards in the stack
        for (int i = 0; i < NUM_CARDS; i++) {
            // Load the custom images for the card fronts and backs
            Image cardFrontImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/card_front.png")));
            Image cardBackImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/card_back.png")));

            // Create a stack pane to overlay the front and back images
            StackPane cardPane = new StackPane();

            // Create a text node for the card name
            Text cardNameText = new Text(cardNames[i]);
            cardNameText.setStyle("-fx-font-size: 30pt; -fx-fill: lightblue;");
            cardNameText.setBoundsType(TextBoundsType.VISUAL); // Use visual bounds to get accurate text size

            // Set the text node size to match the card size
            cardNameText.setWrappingWidth(CARD_WIDTH); // Use the card width for centering
            cardNameText.setTextAlignment(TextAlignment.CENTER);
            cardNameText.setTranslateY(0);
            cardNameText.setVisible(false);

            // Initially show the back of the card
            ImageView cardBackImageView = new ImageView(cardBackImage);
            cardBackImageView.setFitWidth(CARD_WIDTH);
            cardBackImageView.setFitHeight(CARD_HEIGHT);
            cardBackImageView.setVisible(true);

            // Create an image view for the front of the card (hidden initially)
            ImageView cardFrontImageView = new ImageView(cardFrontImage);
            cardFrontImageView.setFitWidth(CARD_WIDTH);
            cardFrontImageView.setFitHeight(CARD_HEIGHT);
            cardFrontImageView.setVisible(false);

            cardPane.getChildren().addAll(cardBackImageView, cardFrontImageView, cardNameText);

            // Make the card movable
            makeDraggable(cardPane);
            makeFlippable(cardPane);

            // Add the card to the root pane
            root.getChildren().add(cardPane);
            primaryStage.setScene(scene);
            primaryStage.setX(screenBounds.getMinX());
            primaryStage.setY(screenBounds.getMinY());
            primaryStage.setWidth(screenBounds.getWidth());
            primaryStage.setHeight(screenBounds.getHeight());

            primaryStage.show();
        }
    }

    private void makeFlippable(Pane pane) {
        pane.setOnMouseClicked(event -> {
            Node front = null;
            Node back = null;
            Node text = null;
            for (Node node : pane.getChildren()) {
                if (node instanceof ImageView) {
                    ImageView imageView = (ImageView) node;
                    if (imageView.isVisible()) {
                        front = imageView;
                    } else {
                        back = imageView;
                    }
                }

                if (node instanceof Text text1) {
                    text = text1;
                }
            }
            if (front != null && back != null && text != null && event.isStillSincePress()) {
                front.setVisible(!front.isVisible());
                text.setVisible(!front.isVisible() && !text.isVisible());
                back.setVisible(!back.isVisible());
                pane.toFront();
            }
        });
    }

    private void makeDraggable(Pane pane) {
        final double[] dragDeltaX = new double[1];
        final double[] dragDeltaY = new double[1];

        pane.setOnMousePressed(event -> {
            dragDeltaX[0] = event.getSceneX() - pane.getTranslateX();
            dragDeltaY[0] = event.getSceneY() - pane.getTranslateY();
        });

        pane.setOnMouseDragged(event -> {
            pane.setTranslateX(event.getSceneX() - dragDeltaX[0]);
            pane.setTranslateY(event.getSceneY() - dragDeltaY[0]);
            pane.toFront();
        });
    }

    private String[] generateShuffledCardNames() {
        ObservableList<String> cardNames = FXCollections.observableArrayList();
        String[] suits = {
                "Arcs", "Arrows", "Clouds", "Clovers", "Comets", "Crescents", "Crosses", "Crowns", "Diamonds", "Embers", "Eyes",
                "Gears", "Glyphs", "Flames", "Flowers", "Hearts", "Keys", "Locks", "Leaves", "Mountains", "Points", "Scrolls",
                "Shells", "Shields", "Spades", "Spirals", "Stars", "Suns", "Swords", "Tridents", "Trees", "Waves"
        };
        String[] values = {
                "(1) Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "(11) Jack", "(12) Queen", "(13) King", "(14) Nomad",
                "(15) Prince", "(16) Rune", "(17) Fable", "(18) Sorceress", "(19) Utopia", "(20) Wizard", "(21) Titan",
                "(22) Baron", "(23) Illusionist", "(24) Oracle", "(25) Magician", "(26) Luminary", "(27) Eclipse",
                "(28) Celestial", "(29) Duke", "(30) Genesis", "(31) Zephyr", "(32) Vesper"
        };

        for (String suit : suits) {
            for (String value : values) {
                cardNames.add(value + " of " + suit);
            }
        }

        // Shuffle the cards
        Collections.shuffle(cardNames);

        return cardNames.toArray(new String[0]);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
