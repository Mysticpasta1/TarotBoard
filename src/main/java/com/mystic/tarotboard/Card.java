package com.mystic.tarotboard;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;

import java.util.List;

public class Card {
    private final StackPane cardPane;
    private final Text cardName; // The visual representation of the card

    public static final Color CELESTIAL_COURT = Color.web("#FFD700"); // Gold
    public static final Color UMBRAL_DOMINION = Color.web("#4B0082"); // Indigo
    public static final Color INFERNAL_PACT = Color.web("#DC143C");    // Crimson
    public static final Color VERDANT_CYCLE = Color.web("#228B22");    // ForestGreen
    public static final Color AETHERIC_LOOM = Color.web("#1E90FF");    // DodgerBlue

    // List of suits categorized by color
    public static final List<String> CELESTIAL_COURT_SUITS = List.of(
            "Stars", "Suns", "Crowns", "Quasars", "Crescents", "Sigils", "Comets", "Glyphs"
    );

    public static final List<String> UMBRAL_DOMINION_SUITS = List.of(
            "Veils", "Runes", "Hearts", "Spirals", "Eyes", "Omens", "Diamonds", "Orbs"
    );

    public static final List<String> INFERNAL_PACT_SUITS = List.of(
            "Arrows", "Flames", "Locks", "Arcs", "Swords", "Points", "Embers", "Gears"
    );

    public static final List<String> VERDANT_CYCLE_SUITS = List.of(
            "Flowers", "Leaves", "Mountains", "Shells", "Clovers", "Tridents", "Trees", "Waves"
    );

    public static final List<String> AETHERIC_LOOM_SUITS = List.of(
            "Clouds", "Crosses", "Shields", "Keys", "Spades", "Scrolls", "Looms", "Shards"
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

        if (CELESTIAL_COURT_SUITS.contains(suit)) {
            cardNameText.setStyle("-fx-font-size: 15pt;");
            cardNameText.setFill(CELESTIAL_COURT);
        } else if (UMBRAL_DOMINION_SUITS.contains(suit)) {
            cardNameText.setStyle("-fx-font-size: 15pt;");
            cardNameText.setFill(UMBRAL_DOMINION);
        } else if (INFERNAL_PACT_SUITS.contains(suit)) {
            cardNameText.setStyle("-fx-font-size: 15pt;");
            cardNameText.setFill(INFERNAL_PACT);
        } else if (VERDANT_CYCLE_SUITS.contains(suit)) {
            cardNameText.setStyle("-fx-font-size: 15pt;");
            cardNameText.setFill(VERDANT_CYCLE);
        } else if (AETHERIC_LOOM_SUITS.contains(suit)) {
            cardNameText.setStyle("-fx-font-size: 15pt;");
            cardNameText.setFill(AETHERIC_LOOM);
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
        return index - (TarotBoard.values.size() - 1) / 2;
    }

    // Getter for the cardPane
    public StackPane getCardPane() {
        return cardPane;
    }
}
