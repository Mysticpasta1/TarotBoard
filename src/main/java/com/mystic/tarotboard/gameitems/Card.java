package com.mystic.tarotboard.gameitems;

import com.mystic.tarotboard.TarotBoard;
import com.mystic.tarotboard.theming.ThemeConfiguration;
import com.mystic.tarotboard.theming.ThemeManager;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;

public class Card {
    private final StackPane cardPane;
    private final Text cardName;

    public Card(String text, String value, String suit, double width, double height, Image cardFrontImage, Image cardBackImage, ThemeConfiguration themeConfiguration) {
        cardPane = new StackPane();
        ImageView cardFrontImageView = new ImageView(cardFrontImage);
        ImageView cardBackImageView = new ImageView(cardBackImage);
        cardFrontImageView.setVisible(false);
        cardBackImageView.setVisible(true);
        cardFrontImageView.setFitWidth(width);
        cardFrontImageView.setFitHeight(height);
        cardBackImageView.setFitWidth(width);
        cardBackImageView.setFitHeight(height);
        Text cardNameText = getStyle(text, value, suit, themeConfiguration);
        cardNameText.setBoundsType(TextBoundsType.VISUAL);
        cardNameText.setWrappingWidth(width);
        cardNameText.setTextAlignment(TextAlignment.CENTER);
        cardNameText.setTranslateY(0);
        cardNameText.setVisible(false);
        cardPane.getChildren().addAll(cardBackImageView, cardFrontImageView, cardNameText);
        this.cardName = cardNameText;
    }

    public static Text getStyle(String cardText, String value, String suit, ThemeConfiguration themeConfiguration) {
        Text cardNameText = new Text(cardText + "\n \n (" + Math.round(getRank(value)) + ")");
        String color = ThemeManager.getSuitColor(suit, themeConfiguration.getSuitStyles());
        String style = "-fx-font-size: 15pt; -fx-fill: " + color + ";";
        cardNameText.setStyle(style);
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
        return index - (double) (TarotBoard.values.size() - 1) / 2;
    }

    public StackPane getCardPane() {
        return cardPane;
    }
}
