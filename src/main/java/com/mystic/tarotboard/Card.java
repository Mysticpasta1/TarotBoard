package com.mystic.tarotboard;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.util.Objects;
import java.util.Set;

public class Card {
    /*TODO
       Make negative value cards with a black or darker color background to normal cards!
       There would be 32 negative values in each suit!
       Suit text color will still be Blue, Red, Yellow, Green, possibly more in the future!
       Color of text will depend on color of card in Minecraft Mods variation!
     */

    private final String value;
    private final String suit;
    private final StackPane cardPane;
    private final Text cardName; // The visual representation of the card

    // List of suits categorized by color
    public static final Set<String> BLUE_SUITS = Set.of(
            "Arcs", "Spades", "Clouds", "Clovers", "Comets", "Crescents", "Crosses", "Crowns"
    );

    public static final Set<String> RED_SUITS = Set.of(
            "Diamonds", "Embers", "Eyes", "Gears", "Glyphs", "Flames", "Flowers", "Hearts"
    );

    public static final Set<String> YELLOW_SUITS = Set.of(
            "Arrows", "Keys", "Locks", "Leaves", "Mountains", "Points", "Scrolls", "Shells"
    );

    public static final Set<String> GREEN_SUITS = Set.of(
            "Shields", "Spirals", "Stars", "Suns", "Swords", "Tridents", "Trees", "Waves"
    );

    public Card(String value, String suit, double width, double height) {
        this.value = value;
        this.suit = suit;

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
        cardFrontImageView.setFitWidth(width); // Use your CARD_WIDTH
        cardFrontImageView.setFitHeight(height); // Use your CARD_HEIGHT
        cardBackImageView.setFitWidth(width);
        cardBackImageView.setFitHeight(height);

        // Add text for card name
        this.cardName = new Text(value + " of " + suit);

        if (RED_SUITS.contains(suit)) {
            this.cardName.setStyle("-fx-font-size: 15pt; -fx-fill: " + "firebrick" + ";");
        } else if (BLUE_SUITS.contains(suit)) {
            cardName.setStyle("-fx-font-size: 15pt; -fx-fill: " + "light-blue" + ";");
        } else if (YELLOW_SUITS.contains(suit)) {
            cardName.setStyle("-fx-font-size: 15pt; -fx-fill: " + "yellow" + ";");
        } else if (GREEN_SUITS.contains(suit)) {
            cardName.setStyle("-fx-font-size: 15pt; -fx-fill: " + "green" + ";");
        }

        cardName.setVisible(false);

        // Add images and text to cardPane
        cardPane.getChildren().addAll(cardBackImageView, cardFrontImageView, cardName);
    }

    public String getValue() {
        return value;
    }

    public String getSuit() {
        return suit;
    }

    public Text getCardName() {
        return cardName;
    }

    //I want to use negative card value for later so using PI for an unknown value
    public double getRank() {
        return switch (value) {
            case "Hold" -> 0;
            case "Ace" -> 1;
            case "2" -> 2;
            case "3" -> 3;
            case "4" -> 4;
            case "5" -> 5;
            case "6" -> 6;
            case "7" -> 7;
            case "8" -> 8;
            case "9" -> 9;
            case "10" -> 10;
            case "Jack" -> 11;
            case "Queen" -> 12;
            case "King" -> 13;
            case "Nomad" -> 14;
            case "Prince" -> 15;
            case "Rune" -> 16;
            case "Fable" -> 17;
            case "Sorceress" -> 18;
            case "Utopia" -> 19;
            case "Wizard" -> 20;
            case "Titan" -> 21;
            case "Baron" -> 22;
            case "Illusionist" -> 23;
            case "Oracle" -> 24;
            case "Magician" -> 25;
            case "Luminary" -> 26;
            case "Eclipse" -> 27;
            case "Celestial" -> 28;
            case "Duke" -> 29;
            case "Genesis" -> 30;
            case "Zephyr" -> 31;
            case "Vesper" -> 32;
            default -> Math.PI; // Invalid or unknown value
        };
    }

    // Getter for the cardPane
    public StackPane getCardPane() {
        return cardPane;
    }
}
