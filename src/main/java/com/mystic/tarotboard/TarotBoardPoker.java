package com.mystic.tarotboard;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class TarotBoardPoker {

    // --- Court Sets ---

    public enum CourtSet {
        CELESTIAL("#FFD700", 5), // Gold, highest rank
        UMBRAL("#4B0082", 4),
        INFERNAL("#DC143C", 3),
        VERDANT("#228B22", 2),
        AETHERIC("#1E90FF", 1);

        private final String colorHex;
        private final int rank; // For tiebreaks: higher wins

        CourtSet(String colorHex, int rank) {
            this.colorHex = colorHex;
            this.rank = rank;
        }

        public String getColorHex() {
            return colorHex;
        }

        public int getRank() {
            return rank;
        }
    }

    // --- Suits ---

    public enum Suit {
        // Celestial Court
        STARS(CourtSet.CELESTIAL),
        SUNS(CourtSet.CELESTIAL),
        CROWNS(CourtSet.CELESTIAL),
        QUASARS(CourtSet.CELESTIAL),
        CRESCENTS(CourtSet.CELESTIAL),
        SIGILS(CourtSet.CELESTIAL),
        COMETS(CourtSet.CELESTIAL),
        GLYPHS(CourtSet.CELESTIAL),

        // Umbral Dominion
        VEILS(CourtSet.UMBRAL),
        RUNES(CourtSet.UMBRAL),
        HEARTS(CourtSet.UMBRAL),
        SPIRALS(CourtSet.UMBRAL),
        EYES(CourtSet.UMBRAL),
        OMENS(CourtSet.UMBRAL),
        DIAMONDS(CourtSet.UMBRAL),
        ORBS(CourtSet.UMBRAL),

        // Infernal Pact
        ARROWS(CourtSet.INFERNAL),
        FLAMES(CourtSet.INFERNAL),
        LOCKS(CourtSet.INFERNAL),
        ARCS(CourtSet.INFERNAL),
        SWORDS(CourtSet.INFERNAL),
        POINTS(CourtSet.INFERNAL),
        EMBERS(CourtSet.INFERNAL),
        GEARS(CourtSet.INFERNAL),

        // Verdant Cycle
        FLOWERS(CourtSet.VERDANT),
        LEAVES(CourtSet.VERDANT),
        MOUNTAINS(CourtSet.VERDANT),
        SHELLS(CourtSet.VERDANT),
        CLOVERS(CourtSet.VERDANT),
        TRIDENTS(CourtSet.VERDANT),
        TREES(CourtSet.VERDANT),
        WAVES(CourtSet.VERDANT),

        // Aetheric Loom
        CLOUDS(CourtSet.AETHERIC),
        CROSSES(CourtSet.AETHERIC),
        SHIELDS(CourtSet.AETHERIC),
        KEYS(CourtSet.AETHERIC),
        SPADES(CourtSet.AETHERIC),
        SCROLLS(CourtSet.AETHERIC),
        LOOMS(CourtSet.AETHERIC),
        SHARDS(CourtSet.AETHERIC);

        private final CourtSet courtSet;

        Suit(CourtSet courtSet) {
            this.courtSet = courtSet;
        }

        public CourtSet getCourtSet() {
            return courtSet;
        }
    }

    // --- Value Categories ---

    public enum ValueCategory {
        NEGATIVE, NEUTRAL, POSITIVE, WILD
    }

    // --- Card Values ---

    public enum Value {
        // Negative values with assigned negative numeric rank (e.g., -40 to -1)
        DEVIL(ValueCategory.NEGATIVE, -40),
        SHADOW(ValueCategory.NEGATIVE, -39),
        SPECTER(ValueCategory.NEGATIVE, -38),
        PHANTOM(ValueCategory.NEGATIVE, -37),
        WRAITH(ValueCategory.NEGATIVE, -36),
        GHOUL(ValueCategory.NEGATIVE, -35),
        BANSHEE(ValueCategory.NEGATIVE, -34),
        REVERENT(ValueCategory.NEGATIVE, -33),
        EIDOLON(ValueCategory.NEGATIVE, -32),
        SHADE(ValueCategory.NEGATIVE, -31),
        DOPPELGANGER(ValueCategory.NEGATIVE, -30),
        HOLLOW(ValueCategory.NEGATIVE, -29),
        ABYSS(ValueCategory.NEGATIVE, -28),
        CHIMERA(ValueCategory.NEGATIVE, -27),
        POLTERGEIST(ValueCategory.NEGATIVE, -26),
        WIGHT(ValueCategory.NEGATIVE, -25),
        APPARITION(ValueCategory.NEGATIVE, -24),
        NIGHTMARE(ValueCategory.NEGATIVE, -23),
        SUCCUBUS(ValueCategory.NEGATIVE, -22),
        INCUBUS(ValueCategory.NEGATIVE, -21),
        NECROMANCER(ValueCategory.NEGATIVE, -20),
        FURY(ValueCategory.NEGATIVE, -19),
        GRIM(ValueCategory.NEGATIVE, -18),
        HARBINGER(ValueCategory.NEGATIVE, -17),
        SPECTACLE(ValueCategory.NEGATIVE, -16),
        LICH(ValueCategory.NEGATIVE, -15),
        GORGON(ValueCategory.NEGATIVE, -14),
        DRAKE(ValueCategory.NEGATIVE, -13),
        DEMON(ValueCategory.NEGATIVE, -12),
        FROST(ValueCategory.NEGATIVE, -11),
        GOLEM(ValueCategory.NEGATIVE, -10),
        HYDRA(ValueCategory.NEGATIVE, -9),
        INFERNO(ValueCategory.NEGATIVE, -8),
        JUGGERNAUT(ValueCategory.NEGATIVE, -7),
        KRAKEN(ValueCategory.NEGATIVE, -6),
        REAPER(ValueCategory.NEGATIVE, -5),
        LEVIATHAN(ValueCategory.NEGATIVE, -4),
        MANTICORE(ValueCategory.NEGATIVE, -3),
        NAGA(ValueCategory.NEGATIVE, -2),
        BLIGHT(ValueCategory.NEGATIVE, -1),
        SERPENT(ValueCategory.NEGATIVE, 0), // Let's treat as -0 for tie-breaks

        // Neutral
        HOLD(ValueCategory.NEUTRAL, 0),

        // Positive values 1 to 40+
        ACE(ValueCategory.POSITIVE, 1),
        TWO(ValueCategory.POSITIVE, 2),
        THREE(ValueCategory.POSITIVE, 3),
        FOUR(ValueCategory.POSITIVE, 4),
        FIVE(ValueCategory.POSITIVE, 5),
        SIX(ValueCategory.POSITIVE, 6),
        SEVEN(ValueCategory.POSITIVE, 7),
        EIGHT(ValueCategory.POSITIVE, 8),
        NINE(ValueCategory.POSITIVE, 9),
        TEN(ValueCategory.POSITIVE, 10),
        JACK(ValueCategory.POSITIVE, 11),
        QUEEN(ValueCategory.POSITIVE, 12),
        KING(ValueCategory.POSITIVE, 13),
        NOMAD(ValueCategory.POSITIVE, 14),
        PRINCE(ValueCategory.POSITIVE, 15),
        RUNE(ValueCategory.POSITIVE, 16),
        FABLE(ValueCategory.POSITIVE, 17),
        SORCERESS(ValueCategory.POSITIVE, 18),
        UTOPIA(ValueCategory.POSITIVE, 19),
        WIZARD(ValueCategory.POSITIVE, 20),
        TITAN(ValueCategory.POSITIVE, 21),
        BARON(ValueCategory.POSITIVE, 22),
        ILLUSIONIST(ValueCategory.POSITIVE, 23),
        ORACLE(ValueCategory.POSITIVE, 24),
        MAGICIAN(ValueCategory.POSITIVE, 25),
        LUMINARY(ValueCategory.POSITIVE, 26),
        ECLIPSE(ValueCategory.POSITIVE, 27),
        CELESTIAL(ValueCategory.POSITIVE, 28),
        DUKE(ValueCategory.POSITIVE, 29),
        GENESIS(ValueCategory.POSITIVE, 30),
        ZEPHYR(ValueCategory.POSITIVE, 31),
        VESPER(ValueCategory.POSITIVE, 32),
        UMBRA(ValueCategory.POSITIVE, 33),
        VALKYRIE(ValueCategory.POSITIVE, 34),
        WARDEN(ValueCategory.POSITIVE, 35),
        ZENITH(ValueCategory.POSITIVE, 36),
        YGGDRASIL(ValueCategory.POSITIVE, 37),
        ZODIAC(ValueCategory.POSITIVE, 38),
        PHOENIX(ValueCategory.POSITIVE, 39),
        RAVEN(ValueCategory.POSITIVE, 40),
        CIPHER(ValueCategory.POSITIVE, 41),
        ANGEL(ValueCategory.POSITIVE, 42),

        // Wild cards (special)
        JOKER(ValueCategory.WILD, 1000),
        SOUL(ValueCategory.WILD, 1001),
        LIGHT(ValueCategory.WILD, 1002),
        DARK(ValueCategory.WILD, 1003),
        JUDGEMENT(ValueCategory.WILD, 1004),
        CHORUS(ValueCategory.WILD, 1005),
        LIFE(ValueCategory.WILD, 1006),
        DEATH(ValueCategory.WILD, 1007),
        WRATH(ValueCategory.WILD, 1008),
        PRIDE(ValueCategory.WILD, 1009),
        GREED(ValueCategory.WILD, 1010),
        LUST(ValueCategory.WILD, 1011),
        ENVY(ValueCategory.WILD, 1012),
        GLUTTONY(ValueCategory.WILD, 1013),
        SLOTH(ValueCategory.WILD, 1014),
        CHASITY(ValueCategory.WILD, 1015),
        TEMPERANCE(ValueCategory.WILD, 1016),
        CHARITY(ValueCategory.WILD, 1017),
        DILIGENCE(ValueCategory.WILD, 1018),
        KINDNESS(ValueCategory.WILD, 1019),
        PATIENCE(ValueCategory.WILD, 1020),
        HUMILITY(ValueCategory.WILD, 1021),
        VOICE(ValueCategory.WILD, 1022),
        VOICES(ValueCategory.WILD, 1023),
        MOTHER(ValueCategory.WILD, 1024),
        FATHER(ValueCategory.WILD, 1025),
        BROTHER(ValueCategory.WILD, 1026),
        SISTER(ValueCategory.WILD, 1027),
        DUALITY(ValueCategory.WILD, 1028),
        ACCORD(ValueCategory.WILD, 1029),
        HUSBAND(ValueCategory.WILD, 1030),
        WIFE(ValueCategory.WILD, 1031),
        PROGENY(ValueCategory.WILD, 1032),
        CORRIDOR(ValueCategory.WILD, 1033),
        FIELD(ValueCategory.WILD, 1034),
        INTELLECT(ValueCategory.WILD, 1035),
        BRAWN(ValueCategory.WILD, 1036),
        DESPAIR(ValueCategory.WILD, 1037),
        PAST(ValueCategory.WILD, 1038),
        PRESENT(ValueCategory.WILD, 1039),
        FUTURE(ValueCategory.WILD, 1040),
        GATE(ValueCategory.WILD, 1041),
        SIGN(ValueCategory.WILD, 1042),
        RUIN(ValueCategory.WILD, 1043),
        SNOW(ValueCategory.WILD, 1044),
        RAIN(ValueCategory.WILD, 1045),
        TEMPEST(ValueCategory.WILD, 1046),
        LOVERS(ValueCategory.WILD, 1047),
        DISCORD(ValueCategory.WILD, 1048),
        CONCORD(ValueCategory.WILD, 1049),
        HARMONY(ValueCategory.WILD, 1050),
        DISSONANCE(ValueCategory.WILD, 1051),
        EARTH(ValueCategory.WILD, 1052),
        FIRE(ValueCategory.WILD, 1053),
        WATER(ValueCategory.WILD, 1054),
        AIR(ValueCategory.WILD, 1055),
        SPIRIT(ValueCategory.WILD, 1056),
        OBLIVION(ValueCategory.WILD, 1057),
        OBSCURITY(ValueCategory.WILD, 1058),
        PURGATORY(ValueCategory.WILD, 1059),
        NETHER(ValueCategory.WILD, 1060),
        UNDERWORLD(ValueCategory.WILD, 1061),
        AETHER(ValueCategory.WILD, 1062),
        OVERWORLD(ValueCategory.WILD, 1063),
        LIMBO(ValueCategory.WILD, 1064),
        CHAOS(ValueCategory.WILD, 1065),
        BALANCE(ValueCategory.WILD, 1066),
        DOOM(ValueCategory.WILD, 1067),
        PEACE(ValueCategory.WILD, 1068),
        EVIL(ValueCategory.WILD, 1069),
        GOOD(ValueCategory.WILD, 1070),
        NEUTRAL(ValueCategory.WILD, 1071),
        HOPE(ValueCategory.WILD, 1072),
        MONSTER(ValueCategory.WILD, 1073),
        HUMAN(ValueCategory.WILD, 1074);

        private final ValueCategory category;
        private final int rankValue; // Used to compare cards internally

        Value(ValueCategory category, int rankValue) {
            this.category = category;
            this.rankValue = rankValue;
        }

        public ValueCategory getCategory() {
            return category;
        }

        public int getRankValue() {
            return rankValue;
        }

        public boolean isWild() {
            return category == ValueCategory.WILD;
        }

        public boolean isNegative() {
            return category == ValueCategory.NEGATIVE;
        }

        public boolean isPositive() {
            return category == ValueCategory.POSITIVE;
        }

        public boolean isNeutral() {
            return category == ValueCategory.NEUTRAL;
        }
    }

    // --- Card Class ---

    public static class Card {
        private final Suit suit;  // null for wild cards with no suit or face-down cards
        private final Value value; // null for face-down cards

        private final boolean faceDown;

        public Card(Suit suit, Value value, boolean faceDown) {
            this.faceDown = faceDown;
            if (!faceDown) {  // only check these if NOT face down
                if (value == null)
                    throw new IllegalArgumentException("Card value cannot be null");
                if (value.isWild() && suit != null)
                    throw new IllegalArgumentException("Wild cards do not have suits");
                if (!value.isWild() && suit == null)
                    throw new IllegalArgumentException("Non-wild cards must have a suit");
            }

            this.suit = suit;
            this.value = value;
        }

        public static Card createFaceDown() {
            return new Card(null, null, true);
        }

        public boolean isFaceDown() {
            return faceDown;
        }

        public Suit getSuit() {
            return suit;
        }

        public CourtSet getCourtSet() {
            return suit == null ? null : suit.getCourtSet();
        }

        public Value getValue() {
            return value;
        }

        public boolean isWild() {
            if (faceDown) return false;  // Face down cards are never wild
            return value.isWild();
        }

        @Override
        public String toString() {
            if (faceDown) return "[Face Down]";
            if (isWild())
                return value.name();
            else
                return value.name() + " of " + suit.name();
        }
    }

    // --- Hand Types / Rankings (Enum) ---

    public enum HandRank {
        GALAXY_FLUSH(1, "Galaxy Flush", 100000, 54750, 9500),
        REALM_ROYAL(2, "Realm Royal", 95000, 52000, 9000),
        STRAIGHT_INFERNO(3, "Straight Inferno", 90000, 49250, 8500),
        STRAIGHT_FLUSH(4, "Straight Flush", 85000, 46500, 8000),
        FIVE_OF_A_KIND(5, "Five of a Kind", 80000, 43750, 7500),
        HYPER_FLUSH(6, "Hyper Flush", 75000, 40000, 7000),
        PRISMATIC_FLUSH(7, "Prismatic Flush", 70000, 36250, 6500),
        FOUR_OF_A_KIND(8, "Four of a Kind", 65000, 32500, 6000),
        FULL_HOUSE(9, "Full House", 60000, 28750, 5500),
        FLUSH(10, "Flush", 55000, 25000, 5000),
        STRAIGHT(11, "Straight", 50000, 21250, 4500),
        SUIT_CHAIN(12, "Suit Chain", 45000, 20000, 4000),
        ARCANE_STRAIGHT(13, "Arcane Straight", 40000, 21750, 3500),
        DOUBLE_JOKER_BOMB(14, "Double Joker Bomb", 35000, 19000, 3000),
        THREE_OF_A_KIND(15, "Three of a Kind", 30000, 16250, 2500),
        TWIN_REALM(16, "Twin Realm", 25000, 13500, 2000),
        TWO_PAIR(17, "Two Pair", 20000, 10750, 1500),
        ONE_PAIR(18, "One Pair", 15000, 8000, 1000),
        HIGH_CARD(19, "High Card", 10000, 5250, 500);

        private final int rankOrder;
        private final String name;
        private final int positiveScore;
        private final int neutralScore;
        private final int negativeScore;

        HandRank(int rankOrder, String name, int positiveScore, int neutralScore, int negativeScore) {
            this.rankOrder = rankOrder;
            this.name = name;
            this.positiveScore = positiveScore;
            this.neutralScore = neutralScore;
            this.negativeScore = negativeScore;
        }

        public int getRankOrder() {
            return rankOrder;
        }

        public String getName() {
            return name;
        }

        public int getScore(ValueCategory category) {
            return switch (category) {
                case POSITIVE -> positiveScore;
                case NEUTRAL -> neutralScore;
                case NEGATIVE -> negativeScore;
                default -> 0;
            };
        }
    }

    // --- Poker Hand ---

    public static class Hand implements Comparable<Hand> {
        private final List<Card> cards;

        // Calculated fields
        private HandRank handRank;
        private int score;
        private ValueCategory category; // dominant category of cards

        public Hand(List<Card> cards) {
            if (cards == null || cards.size() < 5)
                throw new IllegalArgumentException("Hand must have at least 5 cards");
            this.cards = cards;
        }

        public List<Card> getCards() {
            return cards;
        }

        public HandRank getHandRank() {
            return handRank;
        }

        public int getScore() {
            return score;
        }

        public ValueCategory getCategory() {
            return category;
        }

        @Override
        public String toString() {
            return handRank.getName() + " (" + cards.stream().map(Card::toString).collect(Collectors.joining(", ")) + ")";
        }

        @Override
        public int compareTo(@NotNull TarotBoardPoker.Hand o) {
            return Integer.compare(this.getScore(), o.getScore());
        }
    }

    // --- Hand Evaluator ---

    public static class HandEvaluator {

        private static final int HAND_SIZE = 7;

        private static final List<Value> WILD_VALUES = Arrays.stream(Value.values())
                .filter(Value::isWild)
                .toList();

        /**
         * Evaluate the given hand and assign the best possible HandRank and score
         */
        public static Hand evaluate(List<Card> cards) {
            if (cards.size() < HAND_SIZE)
                throw new IllegalArgumentException("Need at least " + HAND_SIZE + " cards");

            Hand hand = new Hand(cards);

            // First, classify cards by wild and non-wild
            List<Card> wildCards = cards.stream().filter(Card::isWild).toList();
            List<Card> nonWildCards = cards.stream().filter(c -> !c.isWild()).toList();

            // Dominant category based on majority cards (excluding wilds)
            ValueCategory dominantCategory = determineDominantCategory(cards);

            // Try to identify the best hand rank in order (from highest to lowest)
            // Implementation will check from highest to lowest following the rank order

            // 1. Galaxy Flush
            if (isGalaxyFlush(cards)) {
                hand.handRank = HandRank.GALAXY_FLUSH;
                hand.category = dominantCategory;
                hand.score = hand.handRank.getScore(dominantCategory);
                return hand;
            }

            // 2. Realm Royal
            if (isRealmRoyal(cards)) {
                hand.handRank = HandRank.REALM_ROYAL;
                hand.category = dominantCategory;
                hand.score = hand.handRank.getScore(dominantCategory);
                return hand;
            }

            // 3. Straight Inferno
            if (isStraightInferno(cards)) {
                hand.handRank = HandRank.STRAIGHT_INFERNO;
                hand.category = dominantCategory;
                hand.score = hand.handRank.getScore(dominantCategory);
                return hand;
            }

            // 4. Straight Flush
            if (isStraightFlush(cards)) {
                hand.handRank = HandRank.STRAIGHT_FLUSH;
                hand.category = dominantCategory;
                hand.score = hand.handRank.getScore(dominantCategory);
                return hand;
            }

            // 5. Five of a Kind
            if (isFiveOfAKind(cards)) {
                hand.handRank = HandRank.FIVE_OF_A_KIND;
                hand.category = dominantCategory;
                hand.score = hand.handRank.getScore(dominantCategory);
                return hand;
            }

            // 6. Hyper Flush
            if (isHyperFlush(cards)) {
                hand.handRank = HandRank.HYPER_FLUSH;
                hand.category = dominantCategory;
                hand.score = hand.handRank.getScore(dominantCategory);
                return hand;
            }

            // 7. Prismatic Flush
            if (isPrismaticFlush(cards)) {
                hand.handRank = HandRank.PRISMATIC_FLUSH;
                hand.category = dominantCategory;
                hand.score = hand.handRank.getScore(dominantCategory);
                return hand;
            }

            // 8. Four of a Kind
            if (isFourOfAKind(cards)) {
                hand.handRank = HandRank.FOUR_OF_A_KIND;
                hand.category = dominantCategory;

                Map<Value, Long> counts = countValues(cards);
                long wildCount = cards.stream().filter(Card::isWild).count();

                Value quadValue = counts.entrySet().stream()
                        .filter(e -> e.getValue() + wildCount >= 4)
                        .max(Map.Entry.comparingByKey(Comparator.comparingInt(Value::getRankValue)))
                        .map(Map.Entry::getKey)
                        .orElse(null);

                int quadNum = quadValue != null ? quadValue.getRankValue() : 0;

                // Best kicker
                int kicker = cards.stream()
                        .filter(c -> !c.isWild() && !c.getValue().equals(quadValue))
                        .mapToInt(c -> c.getValue().getRankValue())
                        .max().orElse(0);

                hand.score = hand.handRank.getScore(dominantCategory) + quadNum * 10 + kicker;
                return hand;
            }

            // 9. Full House
            if (isFullHouse(cards)) {
                hand.handRank = HandRank.FULL_HOUSE;
                hand.category = dominantCategory;

                Map<Value, Long> counts = countValues(cards);

                Value trips = counts.entrySet().stream()
                        .filter(e -> e.getValue() >= 3)
                        .map(Map.Entry::getKey)
                        .max(Comparator.comparingInt(Value::getRankValue))
                        .orElse(null);

                Value pair = counts.entrySet().stream()
                        .filter(e -> !e.getKey().equals(trips) && e.getValue() >= 2)
                        .map(Map.Entry::getKey)
                        .max(Comparator.comparingInt(Value::getRankValue))
                        .orElse(null);

                int tripsNum = trips != null ? trips.getRankValue() : 0;
                int pairNum = pair != null ? pair.getRankValue() : 0;

                hand.score = hand.handRank.getScore(dominantCategory) + tripsNum * 10 + pairNum * 5;
                return hand;
            }

            // 10. Flush
            if (isFlush(cards)) {
                hand.handRank = HandRank.FLUSH;
                hand.category = dominantCategory;
                hand.score = hand.handRank.getScore(dominantCategory);
                return hand;
            }

            // 11. Straight
            if (isStraight(cards)) {
                hand.handRank = HandRank.STRAIGHT;
                hand.category = dominantCategory;
                hand.score = hand.handRank.getScore(dominantCategory);
                return hand;
            }

            // 12. Suit Chain
            if (isSuitChain(cards)) {
                hand.handRank = HandRank.SUIT_CHAIN;
                hand.category = dominantCategory;
                hand.score = hand.handRank.getScore(dominantCategory);
                return hand;
            }

            // 13. Arcane Straight
            if (isArcaneStraight(cards)) {
                hand.handRank = HandRank.ARCANE_STRAIGHT;
                hand.category = dominantCategory;
                hand.score = hand.handRank.getScore(dominantCategory);
                return hand;
            }

            // 14. Double Joker Bomb
            if (isDoubleJokerBomb(cards)) {
                hand.handRank = HandRank.DOUBLE_JOKER_BOMB;
                hand.category = dominantCategory;
                hand.score = hand.handRank.getScore(dominantCategory);
                return hand;
            }

            // 15. Three of a Kind
            if (isThreeOfAKind(cards)) {
                hand.handRank = HandRank.THREE_OF_A_KIND;
                hand.category = dominantCategory;

                Map<Value, Long> counts = countValues(cards);

                Value trips = counts.entrySet().stream()
                        .filter(e -> e.getValue() >= 3)
                        .map(Map.Entry::getKey)
                        .max(Comparator.comparingInt(Value::getRankValue))
                        .orElse(null);

                int tripsNum = trips != null ? trips.getRankValue() : 0;

                // Best kicker
                int kicker = cards.stream()
                        .filter(c -> !c.isWild() && !c.getValue().equals(trips))
                        .mapToInt(c -> c.getValue().getRankValue())
                        .max().orElse(0);

                hand.score = hand.handRank.getScore(dominantCategory) + tripsNum * 10 + kicker;
                return hand;
            }

            // 16. Twin Realm (same as Two Pair but across different CourtSets)
            if (isTwinRealm(cards)) {
                hand.handRank = HandRank.TWIN_REALM;
                hand.category = dominantCategory;

                Map<Value, List<Card>> pairs = findPairs(cards);

                List<Integer> pairValues = pairs.keySet().stream()
                        .map(Value::getRankValue)
                        .sorted(Comparator.reverseOrder())
                        .limit(2)
                        .toList();

                int highPair = pairValues.size() > 0 ? pairValues.get(0) : 0;
                int lowPair = pairValues.size() > 1 ? pairValues.get(1) : 0;

                // Best kicker
                int kicker = cards.stream()
                        .filter(c -> !c.isWild() && !pairValues.contains(c.getValue().getRankValue()))
                        .mapToInt(c -> c.getValue().getRankValue())
                        .max().orElse(0);

                hand.score = hand.handRank.getScore(dominantCategory) + highPair * 10 + lowPair * 5 + kicker;
                return hand;
            }

            // 17. Two Pair
            if (isTwoPair(cards)) {
                hand.handRank = HandRank.TWO_PAIR;
                hand.category = dominantCategory;

                Map<Value, List<Card>> pairs = findPairs(cards);

                List<Integer> pairValues = pairs.keySet().stream()
                        .map(Value::getRankValue)
                        .sorted(Comparator.reverseOrder())
                        .limit(2)
                        .toList();

                int highPair = pairValues.size() > 0 ? pairValues.get(0) : 0;
                int lowPair = pairValues.size() > 1 ? pairValues.get(1) : 0;

                // Best kicker
                int kicker = cards.stream()
                        .filter(c -> !c.isWild() && !pairValues.contains(c.getValue().getRankValue()))
                        .mapToInt(c -> c.getValue().getRankValue())
                        .max().orElse(0);

                hand.score = hand.handRank.getScore(dominantCategory) + highPair * 10 + lowPair * 5 + kicker;
                return hand;
            }

            // 18. One Pair
            // Example: One Pair
            if (isOnePair(cards)) {
                hand.handRank = HandRank.ONE_PAIR;
                hand.category = dominantCategory;

                Map<Value, List<Card>> pairs = findPairs(cards);

                Value pairValue = pairs.keySet().stream()
                        .max(Comparator.comparingInt(Value::getRankValue))
                        .orElse(null);

                int pairNumeric = pairValue.getRankValue();

                // Find highest kicker outside pair
                List<Value> remaining = cards.stream()
                        .filter(c -> !c.isWild() && c.getValue() != pairValue)
                        .map(Card::getValue)
                        .sorted(Comparator.comparingInt(Value::getRankValue).reversed())
                        .collect(Collectors.toList());

                int kicker = remaining.isEmpty() ? 0 : remaining.get(0).getRankValue();

                hand.score = hand.handRank.getScore(dominantCategory)
                        + pairNumeric * 10  // pair multiplier
                        + kicker;

                return hand;
            }


            // 19. High Card
            // 19. High Card
            hand.handRank = HandRank.HIGH_CARD;
            hand.category = dominantCategory;

            // Find highest value among non-wild cards
            OptionalInt highestValue = nonWildCards.stream()
                    .mapToInt(c -> c.value.getRankValue()) // assume your Value enum has getNumericValue()
                    .max();

            int highCardValue = highestValue.orElse(0);

            // Combine base rank score with kicker
            hand.score = hand.handRank.getScore(dominantCategory) + highCardValue;

            return hand;
        }

        private static ValueCategory determineDominantCategory(List<Card> cards) {
            // Count number of cards per category (ignoring wilds)
            Map<ValueCategory, Long> counts = cards.stream()
                    .filter(c -> !c.isWild())
                    .collect(Collectors.groupingBy(c -> c.getValue().getCategory(), Collectors.counting()));

            // Default neutral if no cards (wild only?)
            if (counts.isEmpty())
                return ValueCategory.NEUTRAL;

            return counts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(ValueCategory.NEUTRAL);
        }

        // The following methods implement checks for each hand rank type,
        // respecting wild cards and special rules.
        // For brevity here, only a few representative examples:

        private static boolean isGalaxyFlush(List<Card> cards) {
            // Highest 5 consecutive positive values (e.g. Zenith, Yggdrasil, Zodiac, Phoenix, Angel) all same suit
            // Cards must be positive, same suit, and specific top values

            // Filter non-wild cards by same suit
            Set<Suit> suits = cards.stream()
                    .filter(c -> !c.isWild())
                    .map(Card::getSuit)
                    .collect(Collectors.toSet());
            if (suits.size() != 1)
                return false; // Must be same suit

            Suit suit = suits.iterator().next();

            List<Value> needed = List.of(Value.ZENITH, Value.YGGDRASIL, Value.ZODIAC, Value.PHOENIX, Value.ANGEL);
            Set<Value> cardValues = cards.stream()
                    .filter(c -> !c.isWild())
                    .map(Card::getValue)
                    .collect(Collectors.toSet());

            // Wild cards can substitute missing cards
            long wildCount = cards.stream().filter(Card::isWild).count();

            long missing = needed.stream().filter(v -> !cardValues.contains(v)).count();

            return missing <= wildCount && cards.size() >= 5;
        }

        private static boolean isRealmRoyal(List<Card> cards) {
            // Royal flush within the same CourtSet suits (not necessarily same suit)
            // Must be highest values in a CourtSet suits

            CourtSet courtSet = null;
            for (Card c : cards) {
                if (c.isWild())
                    continue;
                if (courtSet == null)
                    courtSet = c.getCourtSet();
                else if (c.getCourtSet() != courtSet)
                    return false; // Must be all same court set
            }
            if (courtSet == null)
                return false;

            // Must have royal values (e.g. King, Queen, Prince, etc). Here assume top 5 positive values?
            List<Value> royalValues = List.of(Value.ANGEL, Value.CIPHER, Value.RAVEN, Value.PHOENIX, Value.ZODIAC);
            Set<Value> cardValues = cards.stream()
                    .filter(c -> !c.isWild())
                    .map(Card::getValue)
                    .collect(Collectors.toSet());

            long wildCount = cards.stream().filter(Card::isWild).count();
            long missing = royalValues.stream().filter(v -> !cardValues.contains(v)).count();

            return missing <= wildCount && cards.size() >= 5;
        }

        // Implement similar logic for all other hands...

        private static boolean isStraightInferno(List<Card> cards) {
            // Straight flush including the value "Inferno"
            return isStraightFlush(cards) && cards.stream().anyMatch(c -> c.getValue() == Value.INFERNO);
        }

        private static boolean isStraightFlush(List<Card> cards) {
            // Five consecutive values same suit, wilds can substitute
            return checkStraightFlush(cards);
        }

        private static boolean isFiveOfAKind(List<Card> cards) {
            // Five cards same value, wilds can substitute missing cards of that value
            return checkOfAKind(cards, 5);
        }

        private static boolean isHyperFlush(List<Card> cards) {
            // Seven cards all in the same suit
            if (cards.size() < 7)
                return false;
            Suit firstSuit = null;
            for (Card c : cards) {
                if (c.isWild())
                    continue;
                if (firstSuit == null)
                    firstSuit = c.getSuit();
                else if (c.getSuit() != firstSuit)
                    return false;
            }
            return true;
        }

        private static boolean isPrismaticFlush(List<Card> cards) {
            // Five cards of same value, each from different suits (across courts)
            Map<Value, Set<Suit>> valueSuitMap = new HashMap<>();
            for (Card c : cards) {
                if (c.isWild())
                    continue;
                valueSuitMap.computeIfAbsent(c.getValue(), k -> new HashSet<>()).add(c.getSuit());
            }

            for (Map.Entry<Value, Set<Suit>> entry : valueSuitMap.entrySet()) {
                if (entry.getValue().size() >= 5)
                    return true;
            }
            return false;
        }

        private static boolean isFourOfAKind(List<Card> cards) {
            return checkOfAKind(cards, 4);
        }

        private static boolean isFullHouse(List<Card> cards) {
            Map<Value, Long> counts = countValues(cards);

            boolean hasThree = counts.values().stream().anyMatch(c -> c >= 3);
            boolean hasPair = counts.values().stream().anyMatch(c -> c >= 2 && c < 3);

            return hasThree && hasPair;
        }

        private static boolean isFlush(List<Card> cards) {
            Set<Suit> suits = cards.stream().filter(c -> !c.isWild()).map(Card::getSuit).collect(Collectors.toSet());
            return suits.size() == 1;
        }

        private static boolean isStraight(List<Card> cards) {
            return checkStraight(cards);
        }

        private static boolean isSuitChain(List<Card> cards) {
            // Straight all from one Court Set (different suits allowed)
            CourtSet cs = null;
            for (Card c : cards) {
                if (c.isWild())
                    continue;
                if (cs == null)
                    cs = c.getCourtSet();
                else if (c.getCourtSet() != cs)
                    return false;
            }
            return checkStraight(cards);
        }

        private static boolean isArcaneStraight(List<Card> cards) {
            // Straight using wild cards as substitutes
            return checkStraight(cards);
        }

        private static boolean isDoubleJokerBomb(List<Card> cards) {
            long wildCount = cards.stream().filter(Card::isWild).count();
            return wildCount >= 2;
        }

        private static boolean isThreeOfAKind(List<Card> cards) {
            return checkOfAKind(cards, 3);
        }

        private static boolean isTwinRealm(List<Card> cards) {
            // Two pairs from different Court Sets
            Map<Value, List<Card>> pairs = findPairs(cards);

            if (pairs.size() < 2)
                return false;

            List<Card> firstPair = pairs.values().iterator().next();
            List<Card> secondPair = pairs.values().stream().skip(1).findFirst().orElse(null);
            if (secondPair == null)
                return false;

            CourtSet firstCS = firstPair.get(0).getCourtSet();
            CourtSet secondCS = secondPair.get(0).getCourtSet();

            return firstCS != null && secondCS != null && firstCS != secondCS;
        }

        private static boolean isTwoPair(List<Card> cards) {
            Map<Value, List<Card>> pairs = findPairs(cards);
            return pairs.size() >= 2;
        }

        private static boolean isOnePair(List<Card> cards) {
            Map<Value, List<Card>> pairs = findPairs(cards);
            return pairs.size() >= 1;
        }

        // --- Helpers ---

        private static Map<Value, Long> countValues(List<Card> cards) {
            Map<Value, Long> counts = new HashMap<>();
            for (Card c : cards) {
                if (c.isWild())
                    continue;
                counts.put(c.getValue(), counts.getOrDefault(c.getValue(), 0L) + 1);
            }
            return counts;
        }

        private static Map<Value, List<Card>> findPairs(List<Card> cards) {
            Map<Value, List<Card>> pairs = new HashMap<>();
            Map<Value, Long> counts = countValues(cards);
            for (Map.Entry<Value, Long> entry : counts.entrySet()) {
                if (entry.getValue() >= 2) {
                    List<Card> pairCards = cards.stream()
                            .filter(c -> !c.isWild() && c.getValue() == entry.getKey())
                            .collect(Collectors.toList());
                    pairs.put(entry.getKey(), pairCards);
                }
            }
            return pairs;
        }

        // Check for "of a kind" (with wild substitution)
        private static boolean checkOfAKind(List<Card> cards, int countNeeded) {
            Map<Value, Long> counts = countValues(cards);
            long wildCount = cards.stream().filter(Card::isWild).count();

            for (Map.Entry<Value, Long> entry : counts.entrySet()) {
                if (entry.getValue() + wildCount >= countNeeded) {
                    return true;
                }
            }
            return false;
        }

        // Check straight, accounting for wild cards substituting missing values
        private static boolean checkStraight(List<Card> cards) {
            // Extract non-wild values, sort by rank
            List<Integer> ranks = cards.stream()
                    .filter(c -> !c.isWild())
                    .map(c -> c.getValue().getRankValue())
                    .sorted()
                    .distinct()
                    .collect(Collectors.toList());

            long wildCount = cards.stream().filter(Card::isWild).count();

            if (ranks.size() + wildCount < 5)
                return false; // Not enough cards

            // Attempt to find a 5-card consecutive sequence, allowing wilds to fill gaps
            for (int startIdx = 0; startIdx <= ranks.size() - 1; startIdx++) {
                int startRank = ranks.get(startIdx);

                int neededWilds = 0;
                int length = 1;
                int lastRank = startRank;

                for (int next = startIdx + 1; next < ranks.size(); next++) {
                    int diff = ranks.get(next) - lastRank;
                    if (diff == 1) {
                        length++;
                        lastRank = ranks.get(next);
                    } else if (diff > 1) {
                        neededWilds += (diff - 1);
                        if (neededWilds > wildCount)
                            break;
                        length++;
                        lastRank = ranks.get(next);
                    }
                    if (length >= 5) {
                        return true;
                    }
                }

                // Check if we can add wilds at the end to reach 5 length
                if (length + (wildCount - neededWilds) >= 5)
                    return true;
            }

            // Special case: low-Ace straight (if you have Ace=1 in your system, not defined here)
            // Could add special logic if needed

            return false;
        }

        private static boolean checkStraightFlush(List<Card> cards) {
            // Check if flush and straight simultaneously with wilds
            Set<Suit> suits = cards.stream().filter(c -> !c.isWild()).map(Card::getSuit).collect(Collectors.toSet());
            if (suits.size() > 1)
                return false;

            return checkStraight(cards);
        }
    }
}
