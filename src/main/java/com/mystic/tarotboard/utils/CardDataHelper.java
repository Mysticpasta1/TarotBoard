package com.mystic.tarotboard.utils;

import javafx.collections.ObservableList;
import java.util.Collections;
import java.util.List;

/**
 * Helper methods for generating and managing card name data.
 */
public class CardDataHelper {

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
}
