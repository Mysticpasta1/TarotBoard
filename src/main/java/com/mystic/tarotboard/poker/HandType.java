package com.mystic.tarotboard.poker;

/**
 * The 20 ranked poker hand types from {@code TarotBoard_Poker_Rules.md}, ordered from strongest
 * (rank 1) to weakest (rank 20), each carrying its scoring-table point values per card category.
 * <p>
 * The source document's own tables are internally inconsistent (a duplicated rank-15 entry, a
 * "Straight Inferno" scoring-table row that has no matching entry in the hand-rankings table, and
 * a hand ordering in the scoring table that disagrees with the rankings table's {@code Beats}
 * column). This enum treats the rankings table's {@code Beats} chain as authoritative for
 * comparison order, and "Straight Inferno" as a synonym for {@link #CRAZY_STRAIGHT} (same table
 * position). {@link #SPECTRUM} has no scoring-table row at all; its point values are linearly
 * interpolated between its neighbors, {@link #DOUBLE_JOKER_BOMB} and {@link #THREE_OF_A_KIND}.
 */
public enum HandType {
    GALAXY_FLUSH(1, 100_000, 54_750, 9_500),
    REALM_ROYAL(2, 95_000, 52_000, 9_000),
    CRAZY_STRAIGHT(3, 90_000, 49_250, 8_500),
    STRAIGHT_FLUSH(4, 85_000, 46_500, 8_000),
    FIVE_OF_A_KIND(5, 80_000, 43_750, 7_500),
    HYPER_FLUSH(6, 75_000, 40_000, 7_000),
    PRISMATIC_FLUSH(7, 70_000, 36_250, 6_500),
    FLUSH(8, 55_000, 25_000, 5_000),
    FOUR_OF_A_KIND(9, 65_000, 32_500, 6_000),
    FULL_HOUSE(10, 60_000, 28_750, 5_500),
    SUIT_CHAIN(11, 45_000, 20_000, 4_000),
    STRAIGHT(12, 50_000, 21_250, 4_500),
    ARCANE_STRAIGHT(13, 40_000, 21_750, 3_500),
    DOUBLE_JOKER_BOMB(14, 35_000, 19_000, 3_000),
    SPECTRUM(15, 32_500, 17_625, 2_750),
    THREE_OF_A_KIND(16, 30_000, 16_250, 2_500),
    TWIN_REALM(17, 25_000, 13_500, 2_000),
    TWO_PAIR(18, 20_000, 10_750, 1_500),
    ONE_PAIR(19, 15_000, 8_000, 1_000),
    HIGH_CARD(20, 10_000, 5_250, 500);

    private final int rank;
    private final long positivePoints;
    private final long neutralPoints;
    private final long negativePoints;

    HandType(int rank, long positivePoints, long neutralPoints, long negativePoints) {
        this.rank = rank;
        this.positivePoints = positivePoints;
        this.neutralPoints = neutralPoints;
        this.negativePoints = negativePoints;
    }

    /**
     * Returns the 1-20 comparison rank, where 1 is the strongest hand.
     *
     * @return the rank
     */
    public int rank() {
        return rank;
    }

    /**
     * Returns the score for this hand type at the given card category.
     *
     * @param category the scoring category
     * @return the point value
     */
    public long score(CardCategory category) {
        return switch (category) {
            case POSITIVE -> positivePoints;
            case NEUTRAL -> neutralPoints;
            case NEGATIVE -> negativePoints;
        };
    }
}
