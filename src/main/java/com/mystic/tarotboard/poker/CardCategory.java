package com.mystic.tarotboard.poker;

/**
 * The three value categories a poker hand's cards fall into, used for scoring-table lookup.
 * Wild cards are always {@link #NEUTRAL} since they carry no inherent value of their own.
 */
public enum CardCategory {
    NEGATIVE,
    NEUTRAL,
    POSITIVE
}
