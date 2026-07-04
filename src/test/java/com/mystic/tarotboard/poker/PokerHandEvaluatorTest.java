package com.mystic.tarotboard.poker;

import com.mystic.tarotboard.TarotBoard;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers all 20 hand types plus the judgment calls documented on {@link HandType} and
 * {@link PokerHandEvaluator}: wild substitution scoped to exactly 4 hand types, the
 * Five of a Kind / Prismatic Flush split, and Court-Set-based tiebreaks.
 * <p>
 * Test hands deliberately spread filler cards' value ranks far apart (large index gaps) and
 * limit filler suits to a single Court Set group where a hand's own definition doesn't already
 * fix the group count, to avoid incidentally satisfying a different (often higher-ranked) hand
 * type than the one under test.
 */
class PokerHandEvaluatorTest {

    // Mirrors TarotBoard's private wilds list (kept in sync manually; see plan's finding (h)).
    private static final List<String> WILDS = List.of(
            "Joker", "Soul", "Light", "Dark", "Judgement", "Chorus", "Life", "Death", "Wrath",
            "Pride", "Greed", "Lust", "Envy", "Gluttony", "Sloth", "Chasity", "Temperance", "Charity",
            "Diligence", "Kindness", "Patience", "Humility", "Voice", "Voices", "Mother", "Father", "Brother",
            "Sister", "Duality", "Accord", "Husband", "Wife", "Progeny", "Corridor", "Field", "Intellect", "Brawn",
            "Despair", "Past", "Present", "Future", "Gate", "Sign", "Ruin", "Snow", "Rain", "Tempest", "Lovers",
            "Discord", "Concord", "Harmony", "Dissonance", "Earth", "Fire", "Water", "Air", "Spirit",
            "Oblivion", "Obscurity", "Purgatory", "Nether", "Underworld", "Aether", "Overworld", "Limbo", "Chaos",
            "Balance", "Doom", "Peace", "Evil", "Good", "Neutral", "Hope", "Monster", "Human", "Dusk", "Dawn",
            "Paradox", "Entropy"
    );

    private static final List<String> VALUES = TarotBoard.values;

    private static HandResult evaluate(String... cards) {
        return PokerHandEvaluator.evaluate(List.of(cards), WILDS, VALUES);
    }

    @Test
    void galaxyFlush_topFiveValuesSameSuit() {
        HandResult r = evaluate(
                "Raven of Hearts", "Cipher of Hearts", "Angel of Hearts", "Knight of Hearts", "Venom of Hearts",
                "Eidolon of Stars", "Succubus of Suns");
        assertEquals(HandType.GALAXY_FLUSH, r.type());
    }

    @Test
    void realmRoyal_topFiveValuesSameCourtSetDifferentSuits() {
        HandResult r = evaluate(
                "Raven of Veils", "Cipher of Runes", "Angel of Hearts", "Knight of Spirals", "Venom of Eyes",
                "Eidolon of Stars", "Succubus of Suns");
        assertEquals(HandType.REALM_ROYAL, r.type());
    }

    @Test
    void crazyStraight_requiresWildAndSameSuit() {
        HandResult r = evaluate(
                "7 of Hearts", "8 of Hearts", "9 of Hearts", "10 of Hearts", "Joker",
                "Eidolon of Stars", "Succubus of Suns");
        assertEquals(HandType.CRAZY_STRAIGHT, r.type());
    }

    @Test
    void straightFlush_naturalSameSuitAnyWindow() {
        HandResult r = evaluate(
                "7 of Hearts", "8 of Hearts", "9 of Hearts", "10 of Hearts", "Jack of Hearts",
                "Eidolon of Stars", "Succubus of Suns");
        assertEquals(HandType.STRAIGHT_FLUSH, r.type());
    }

    @Test
    void fiveOfAKind_requiresWildToCompleteFive() {
        HandResult r = evaluate(
                "9 of Hearts", "9 of Arrows", "9 of Flowers", "9 of Clouds", "Joker",
                "Eidolon of Stars", "Succubus of Suns");
        assertEquals(HandType.FIVE_OF_A_KIND, r.type());
    }

    @Test
    void hyperFlush_allSevenNaturalCardsSameSuitNonConsecutive() {
        HandResult r = evaluate(
                "Eidolon of Stars", "Succubus of Stars", "Drake of Stars", "Manticore of Stars",
                "6 of Stars", "Titan of Stars", "Zephyr of Stars");
        assertEquals(HandType.HYPER_FLUSH, r.type());
    }

    @Test
    void prismaticFlush_fiveNaturalSameValueDifferentSuitsNoWild() {
        HandResult r = evaluate(
                "7 of Hearts", "7 of Arcs", "7 of Flowers", "7 of Clouds", "7 of Echoes",
                "2 of Stars", "4 of Suns");
        assertEquals(HandType.PRISMATIC_FLUSH, r.type());
    }

    @Test
    void flush_fiveOfSevenSameSuitNonConsecutive() {
        HandResult r = evaluate(
                "Eidolon of Stars", "Succubus of Stars", "Drake of Stars", "Manticore of Stars",
                "6 of Stars", "Titan of Suns", "Zephyr of Crowns");
        assertEquals(HandType.FLUSH, r.type());
    }

    @Test
    void fourOfAKind_fourNaturalSameValue() {
        HandResult r = evaluate(
                "8 of Hearts", "8 of Arrows", "8 of Flowers", "8 of Clouds",
                "Eidolon of Stars", "Succubus of Suns", "Drake of Crowns");
        assertEquals(HandType.FOUR_OF_A_KIND, r.type());
    }

    @Test
    void fullHouse_tripPlusPair() {
        HandResult r = evaluate(
                "9 of Hearts", "9 of Arrows", "9 of Flowers", "10 of Clouds", "10 of Echoes",
                "3 of Stars", "5 of Suns");
        assertEquals(HandType.FULL_HOUSE, r.type());
    }

    @Test
    void suitChain_straightAllSameCourtSetDifferentSuits() {
        HandResult r = evaluate(
                "7 of Arrows", "8 of Flames", "9 of Locks", "10 of Arcs", "Jack of Swords",
                "2 of Stars", "3 of Suns");
        assertEquals(HandType.SUIT_CHAIN, r.type());
    }

    @Test
    void straight_naturalAnyWindowDifferentCourtSets() {
        HandResult r = evaluate(
                "7 of Hearts", "8 of Arrows", "9 of Flowers", "10 of Clouds", "Jack of Echoes",
                "2 of Stars", "3 of Suns");
        assertEquals(HandType.STRAIGHT, r.type());
    }

    @Test
    void arcaneStraight_wildFillsMissingRankDifferentSuits() {
        HandResult r = evaluate(
                "7 of Hearts", "8 of Arrows", "9 of Flowers", "10 of Clouds", "Joker",
                "2 of Stars", "3 of Suns");
        assertEquals(HandType.ARCANE_STRAIGHT, r.type());
    }

    @Test
    void doubleJokerBomb_twoWildsWithNoOtherQualifyingCombo() {
        HandResult r = evaluate(
                "Joker", "Soul",
                "Eidolon of Stars", "Succubus of Suns", "Drake of Crowns", "Manticore of Quasars", "Rune of Crescents");
        assertEquals(HandType.DOUBLE_JOKER_BOMB, r.type());
    }

    @Test
    void spectrum_sixDistinctCourtSetsPlusWild() {
        HandResult r = evaluate(
                "Eidolon of Stars", "Succubus of Hearts", "Drake of Arrows",
                "Manticore of Flowers", "Rune of Clouds", "Titan of Echoes",
                "Joker");
        assertEquals(HandType.SPECTRUM, r.type());
    }

    @Test
    void threeOfAKind_threeNaturalSameValue() {
        HandResult r = evaluate(
                "9 of Hearts", "9 of Arrows", "9 of Flowers",
                "Eidolon of Stars", "Succubus of Suns", "Drake of Crowns", "Manticore of Quasars");
        assertEquals(HandType.THREE_OF_A_KIND, r.type());
    }

    @Test
    void twinRealm_twoMonoCourtSetPairsDifferentGroups() {
        HandResult r = evaluate(
                "9 of Hearts", "9 of Veils", "Rune of Arrows", "Rune of Flames",
                "Eidolon of Stars", "Succubus of Suns", "Drake of Crowns");
        assertEquals(HandType.TWIN_REALM, r.type());
    }

    @Test
    void twoPair_pairsNotMonoCourtSetDegradesFromTwinRealm() {
        HandResult r = evaluate(
                "9 of Hearts", "9 of Arrows", "Rune of Stars", "Rune of Flowers",
                "Eidolon of Suns", "Succubus of Crowns", "Drake of Quasars");
        assertEquals(HandType.TWO_PAIR, r.type());
    }

    @Test
    void onePair_singlePair() {
        HandResult r = evaluate(
                "9 of Hearts", "9 of Arrows",
                "Eidolon of Stars", "Succubus of Suns", "Drake of Crowns", "Manticore of Quasars", "Rune of Crescents");
        assertEquals(HandType.ONE_PAIR, r.type());
    }

    @Test
    void highCard_noCombosAtAll() {
        HandResult r = evaluate(
                "Eidolon of Stars", "Succubus of Suns", "Drake of Crowns", "Manticore of Quasars",
                "6 of Crescents", "Titan of Sigils", "Zephyr of Comets");
        assertEquals(HandType.HIGH_CARD, r.type());
    }

    @Test
    void card_categoryClassification_negativeNeutralPositive() {
        assertEquals(CardCategory.NEGATIVE, Card.parse("Devil of Hearts", WILDS, VALUES).category());
        assertEquals(CardCategory.NEUTRAL, Card.parse("Hold of Hearts", WILDS, VALUES).category());
        assertEquals(CardCategory.POSITIVE, Card.parse("Ace of Hearts", WILDS, VALUES).category());
    }

    @Test
    void card_wildParsesWithNoValueOrSuit() {
        Card c = Card.parse("Joker", WILDS, VALUES);
        assertTrue(c.isWild());
        assertEquals(CardCategory.NEUTRAL, c.category());
    }

    @Test
    void handResult_tiebreakByCourtSetPriority() {
        // Same hand type and same primary value: the hand built from the higher-priority
        // (lower-ordinal) Court Set must win, per the doc's Celestial > ... > Dark Expanse hierarchy.
        HandResult higherPriority = new HandResult(HandType.STRAIGHT, 54, List.of(0), CardCategory.POSITIVE,
                HandType.STRAIGHT.score(CardCategory.POSITIVE), List.of());
        HandResult lowerPriority = new HandResult(HandType.STRAIGHT, 54, List.of(5), CardCategory.POSITIVE,
                HandType.STRAIGHT.score(CardCategory.POSITIVE), List.of());
        assertTrue(higherPriority.compareStrength(lowerPriority) < 0);
        assertTrue(lowerPriority.compareStrength(higherPriority) > 0);
    }

    @Test
    void handResult_strongerHandTypeWinsRegardlessOfValue() {
        HandResult straightFlush = new HandResult(HandType.STRAIGHT_FLUSH, 10, List.of(), CardCategory.NEGATIVE,
                HandType.STRAIGHT_FLUSH.score(CardCategory.NEGATIVE), List.of());
        HandResult galaxyFlushValueLower = new HandResult(HandType.GALAXY_FLUSH, 5, List.of(), CardCategory.NEGATIVE,
                HandType.GALAXY_FLUSH.score(CardCategory.NEGATIVE), List.of());
        assertTrue(galaxyFlushValueLower.compareStrength(straightFlush) < 0);
    }
}
