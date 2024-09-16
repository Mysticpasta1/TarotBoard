package com.mystic.tarotboard;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.util.Objects;
import java.util.Set;

public class Card {
    private final String value;
    private final String suit;
    private boolean faceUp;
    private final StackPane cardPane; // The visual representation of the card

    // List of suits categorized by color
    public static final Set<String> RED_SUITS = Set.of(
            "Arcs", "Arrows", "Clouds", "Clovers", "Comets", "Crescents", "Crosses",
            "Crowns", "Diamonds", "Embers", "Eyes", "Gears", "Glyphs", "Flames", "Flowers", "Hearts"
    );

    public Card(String value, String suit) {
        this.value = value;
        this.suit = suit;
        this.faceUp = false; // Initially face down

        // Initialize the cardPane
        cardPane = new StackPane();

        // Load card images (ensure paths are correct)
        Image cardFrontImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/mystic/tarotboard/assets/card_front.png")));
        Image cardBackImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/mystic/tarotboard/assets/card_back.png")));

        // Create ImageViews for front and back of the card
        ImageView cardFrontImageView = new ImageView(cardFrontImage);
        ImageView cardBackImageView = new ImageView(cardBackImage);

        // Set initial visibility (face-down initially)
        cardFrontImageView.setVisible(false);
        cardBackImageView.setVisible(true);

        // Adjust image view size (match your card size)
        cardFrontImageView.setFitWidth(150); // Use your CARD_WIDTH
        cardFrontImageView.setFitHeight(200); // Use your CARD_HEIGHT
        cardBackImageView.setFitWidth(150);
        cardBackImageView.setFitHeight(200);

        // Add text for card name
        Text cardNameText = new Text(value + " of " + suit);
        cardNameText.setStyle("-fx-font-size: 15pt; -fx-fill: " + (getColor().equals("red") ? "red" : "lightblue") + ";");
        cardNameText.setVisible(false);

        // Add images and text to cardPane
        cardPane.getChildren().addAll(cardBackImageView, cardFrontImageView, cardNameText);
    }

    public String getValue() {
        return value;
    }

    public String getSuit() {
        return suit;
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    public void flip() {
        faceUp = !faceUp;
        // Update the visual representation
        cardPane.getChildren().get(0).setVisible(!faceUp); // cardBackImageView
        cardPane.getChildren().get(1).setVisible(faceUp); // cardFrontImageView
        cardPane.getChildren().get(2).setVisible(faceUp); // cardNameText
    }

    // Utility method to get the color of the card (red or black)
    public String getColor() {
        return RED_SUITS.contains(suit) ? "red" : "black";
    }

    public int getRank() {
        return switch (value) {
            case "(0) Hold" -> 0;
            case "(1) Ace" -> 1;
            case "2" -> 2;
            case "3" -> 3;
            case "4" -> 4;
            case "5" -> 5;
            case "6" -> 6;
            case "7" -> 7;
            case "8" -> 8;
            case "9" -> 9;
            case "10" -> 10;
            case "(11) Jack" -> 11;
            case "(12) Queen" -> 12;
            case "(13) King" -> 13;
            case "(14) Nomad" -> 14;
            case "(15) Prince" -> 15;
            case "(16) Rune" -> 16;
            case "(17) Fable" -> 17;
            case "(18) Sorceress" -> 18;
            case "(19) Utopia" -> 19;
            case "(20) Wizard" -> 20;
            case "(21) Titan" -> 21;
            case "(22) Baron" -> 22;
            case "(23) Illusionist" -> 23;
            case "(24) Oracle" -> 24;
            case "(25) Magician" -> 25;
            case "(26) Luminary" -> 26;
            case "(27) Eclipse" -> 27;
            case "(28) Celestial" -> 28;
            case "(29) Duke" -> 29;
            case "(30) Genesis" -> 30;
            case "(31) Zephyr" -> 31;
            case "(32) Vesper" -> 32;
            default -> -1; // Invalid or unknown value
        };
    }

    // Getter for the cardPane
    public StackPane getCardPane() {
        return cardPane;
    }
}
