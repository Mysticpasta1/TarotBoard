package com.mystic.tarotboard.poker;

import com.mystic.tarotboard.theming.ThemeManager;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A single parsed poker card: either a wild (no value/suit) or a natural card with a value,
 * suit, and the Court Set the suit belongs to.
 *
 * @param raw        the original logical card name, e.g. {@code "7 of Hearts"} or {@code "Joker"}
 * @param isWild     whether this card is a wild card
 * @param valueName  the value name, or {@code null} if wild
 * @param suit       the suit name, or {@code null} if wild
 * @param courtSet   the Court Set group name the suit belongs to, or {@code null} if wild/unknown
 * @param rankIndex  the index of {@code valueName} within the canonical values list, or -1 if wild
 * @param category   the value category, used for scoring; wilds are always {@link CardCategory#NEUTRAL}
 */
public record Card(String raw, boolean isWild, String valueName, String suit, String courtSet,
                    int rankIndex, CardCategory category) {

    private static final Pattern CARD_PATTERN = Pattern.compile("^(?<value>[\\d,a-zA-Z]+) of (?<suit>[a-zA-Z]+)$");

    /**
     * Parses a logical card name into a {@link Card}.
     *
     * @param logicalName the card's logical name, e.g. {@code "7 of Hearts"} or a wild name
     * @param wilds        the list of wild card names
     * @param values       the canonical, rank-ordered list of value names
     * @return the parsed card
     */
    public static Card parse(String logicalName, List<String> wilds, List<String> values) {
        if (wilds.contains(logicalName)) {
            return new Card(logicalName, true, null, null, null, -1, CardCategory.NEUTRAL);
        }
        Matcher matcher = CARD_PATTERN.matcher(logicalName);
        if (!matcher.matches()) {
            return new Card(logicalName, true, null, null, null, -1, CardCategory.NEUTRAL);
        }
        String value = matcher.group("value");
        String suit = matcher.group("suit");
        int rankIndex = values.indexOf(value);
        int holdIndex = values.indexOf("Hold");
        CardCategory category;
        if (rankIndex < 0 || holdIndex < 0) {
            category = CardCategory.NEUTRAL;
        } else if (rankIndex < holdIndex) {
            category = CardCategory.NEGATIVE;
        } else if (rankIndex == holdIndex) {
            category = CardCategory.NEUTRAL;
        } else {
            category = CardCategory.POSITIVE;
        }
        String courtSet = ThemeManager.getCourtSetName(suit);
        return new Card(logicalName, false, value, suit, courtSet, rankIndex, category);
    }
}
