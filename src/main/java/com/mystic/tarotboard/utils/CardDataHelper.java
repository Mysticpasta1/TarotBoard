package com.mystic.tarotboard.utils;

import javafx.collections.ObservableList;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;

import java.util.Collections;
import java.util.List;

/**
 * Helper methods for generating and managing card name data.
 */
public class CardDataHelper {

    private static final double CARD_WIDTH = 150;

    /**
     * Populates the given list with card names formed from wilds, suits, and values.
     *
     * @param cardNames the list to populate
     * @param wilds     wild card names to add first
     * @param suits     suit names to combine with values
     * @param values    value names to combine with suits
     */
    public static void addCardNames(ObservableList<String> cardNames, List<String> wilds, List<String> suits, List<String> values) {
        cardNames.addAll(wilds);

        for (String suit : suits) {
            for (String value : values) {
                cardNames.add(value + " of " + suit);
            }
        }
    }

    /**
     * Randomly shuffles the given card name list in place.
     *
     * @param cardNames the list to shuffle
     */
    public static void generateShuffledCardNames(ObservableList<String> cardNames) {
        Collections.shuffle(cardNames);
    }

    /**
     * Configures a Text node for displaying a wild card name with appropriate styling.
     *
     * @param cardNameText the Text node to configure
     * @return the configured Text node
     */
    public static Text getWildCardName(Text cardNameText) {
        cardNameText.setStyle(Styles.wildCardText());
        cardNameText.setBoundsType(TextBoundsType.VISUAL);
        cardNameText.setWrappingWidth(CARD_WIDTH);
        cardNameText.setTextAlignment(TextAlignment.CENTER);
        cardNameText.setTranslateY(0);
        cardNameText.setVisible(false);
        return cardNameText;
    }
}
