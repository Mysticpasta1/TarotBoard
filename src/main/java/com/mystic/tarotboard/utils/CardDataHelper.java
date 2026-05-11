package com.mystic.tarotboard.utils;

import javafx.collections.ObservableList;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;

import java.util.Collections;
import java.util.List;

public class CardDataHelper {

    private static final double CARD_WIDTH = 150;

    public static void addCardNames(ObservableList<String> cardNames, List<String> wilds, List<String> suits, List<String> values) {
        cardNames.addAll(wilds);

        for (String suit : suits) {
            for (String value : values) {
                cardNames.add(value + " of " + suit);
            }
        }
    }

    public static void generateShuffledCardNames(ObservableList<String> cardNames) {
        Collections.shuffle(cardNames);
    }

    public static Text getWildCardName(Text cardNameText) {
        cardNameText.setStyle("-fx-font-size: 15pt; -fx-fill: white;");
        cardNameText.setBoundsType(TextBoundsType.VISUAL);
        cardNameText.setWrappingWidth(CARD_WIDTH);
        cardNameText.setTextAlignment(TextAlignment.CENTER);
        cardNameText.setTranslateY(0);
        cardNameText.setVisible(false);
        return cardNameText;
    }
}
