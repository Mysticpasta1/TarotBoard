package com.mystic.tarotboard;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;

import java.util.List;

public class Card {
    private final StackPane cardPane;
    private final Text cardName; // The visual representation of the card

    // List of suits categorized by color
    public static final List<String> BLUE_SUITS = List.of(
            "Arcs", "Spades", "Clouds", "Clovers", "Comets", "Crescents", "Crosses", "Crowns"
    );

    public static final List<String> RED_SUITS = List.of(
            "Diamonds", "Embers", "Eyes", "Gears", "Glyphs", "Flames", "Flowers", "Hearts"
    );

    public static final List<String> YELLOW_SUITS = List.of(
            "Arrows", "Keys", "Locks", "Leaves", "Mountains", "Points", "Scrolls", "Shells"
    );

    public static final List<String> GREEN_SUITS = List.of(
            "Shields", "Spirals", "Stars", "Suns", "Swords", "Tridents", "Trees", "Waves"
    );

    public static final List<String> PURPLE_SUITS = List.of(
            "Quasars", "Runes", "Omens", "Sigils", "Orbs", "Veils", "Looms", "Shards"
    );

    public Card(String text, String value, String suit, double width, double height, Image cardFrontImage, Image cardBackImage) {
        // Initialize the cardPane
        cardPane = new StackPane();

        // Create ImageViews for front and back of the card
        ImageView cardFrontImageView = new ImageView(cardFrontImage);
        ImageView cardBackImageView = new ImageView(cardBackImage);

        // Set initial visibility (face-down initially)
        cardFrontImageView.setVisible(false);
        cardBackImageView.setVisible(true);

        // Adjust image view size (match your card size)
        cardFrontImageView.setFitWidth(width); // Use your CARD_WIDTH
        cardFrontImageView.setFitHeight(height); // Use your CARD_HEIGHT
        cardBackImageView.setFitWidth(width);
        cardBackImageView.setFitHeight(height);

        // Add text for card name
        Text cardNameText = getStyle(text, value, suit);

        cardNameText.setBoundsType(TextBoundsType.VISUAL); // Use visual bounds to get accurate text size
        cardNameText.setWrappingWidth(width); // Use the card width for centering
        cardNameText.setTextAlignment(TextAlignment.CENTER);
        cardNameText.setTranslateY(0);
        cardNameText.setVisible(false);

        // Add images and text to cardPane
        cardPane.getChildren().addAll(cardBackImageView, cardFrontImageView, cardNameText);

        this.cardName = cardNameText;
    }

    public static Text getStyle(String cardText, String value, String suit) {
        Text cardNameText = new Text(cardText + "\n \n (" + Math.round(getRank(value)) + ")");

        if (RED_SUITS.contains(suit)) {
            cardNameText.setStyle("-fx-font-size: 15pt; -fx-fill: firebrick;");
        } else if (BLUE_SUITS.contains(suit)) {
            cardNameText.setStyle("-fx-font-size: 15pt; -fx-fill: blue;");
        } else if (YELLOW_SUITS.contains(suit)) {
            cardNameText.setStyle("-fx-font-size: 15pt; -fx-fill: yellow;");
        } else if (GREEN_SUITS.contains(suit)) {
            cardNameText.setStyle("-fx-font-size: 15pt; -fx-fill: green;");
        } else if (PURPLE_SUITS.contains(suit)) {
            cardNameText.setStyle("-fx-font-size: 15pt; -fx-fill: purple;");
        }

        return cardNameText;
    }

    public Text getCardName() {
        return cardName;
    }

    public static double getRank(String value) {
        int index = TarotBoard.values.indexOf(value);
        if (index == -1) {
            return Math.PI;
        }
        return index - 41;
    }

    // Getter for the cardPane
    public StackPane getCardPane() {
        return cardPane;
    }
}
