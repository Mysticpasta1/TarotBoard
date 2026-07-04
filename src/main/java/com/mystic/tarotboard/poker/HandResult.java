package com.mystic.tarotboard.poker;

import java.util.List;

/**
 * The outcome of evaluating a 7-card poker hand: which {@link HandType} it makes, the data needed
 * to break ties against another player's result of the same type, and the resulting score.
 *
 * @param type                the matched hand type
 * @param primaryValueRank    the rank index used for cross-player comparison (e.g. a straight's top
 *                            card, a pair's value); higher wins ties at the same {@link HandType}
 * @param courtSetPriorities  ascending-sorted Court Set tiebreak priorities (see
 *                            {@code ThemeManager.getCourtSetPriority}) of the natural cards the hand
 *                            was built from; compared element-by-element (lower wins) if
 *                            {@code primaryValueRank} also ties
 * @param scoringCategory     the value category used to look up the score
 * @param score               the points awarded for this hand, from {@link HandType#score}
 * @param cardsUsed           the specific logical card names that made up the hand, for display
 */
public record HandResult(HandType type, double primaryValueRank, List<Integer> courtSetPriorities,
                          CardCategory scoringCategory, long score, List<String> cardsUsed) {

    /**
     * Compares this hand's strength against another. A negative result means this hand wins,
     * positive means the other hand wins, zero means a true tie (split pot).
     *
     * @param other the hand to compare against
     * @return the comparison result, per {@link java.util.Comparator} conventions
     */
    public int compareStrength(HandResult other) {
        int rankCompare = Integer.compare(this.type.rank(), other.type.rank());
        if (rankCompare != 0) return rankCompare;
        int valueCompare = Double.compare(other.primaryValueRank, this.primaryValueRank);
        if (valueCompare != 0) return valueCompare;
        int len = Math.min(this.courtSetPriorities.size(), other.courtSetPriorities.size());
        for (int i = 0; i < len; i++) {
            int c = Integer.compare(this.courtSetPriorities.get(i), other.courtSetPriorities.get(i));
            if (c != 0) return c;
        }
        return 0;
    }
}
