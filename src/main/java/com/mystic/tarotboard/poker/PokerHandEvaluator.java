package com.mystic.tarotboard.poker;

import com.mystic.tarotboard.theming.ThemeManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Evaluates a 7-card poker hand against the 20 hand types defined in
 * {@code TarotBoard_Poker_Rules.md}, from strongest ({@link HandType#GALAXY_FLUSH}) to weakest
 * ({@link HandType#HIGH_CARD}). See {@link HandType}'s javadoc for how the source document's
 * internal inconsistencies were resolved.
 * <p>
 * Checks run in strength order and the first match wins, so hand definitions don't need explicit
 * "and not a stronger hand" exclusion clauses.
 * <p>
 * <b>Wild substitution</b> is scoped to exactly four hand types (per the rules doc's Wild Cards
 * section): {@link HandType#FIVE_OF_A_KIND}, {@link HandType#CRAZY_STRAIGHT},
 * {@link HandType#ARCANE_STRAIGHT}, and {@link HandType#DOUBLE_JOKER_BOMB}. Each of these
 * <i>requires</i> at least one wild to be used — a hand achievable purely naturally always matches
 * its natural counterpart instead (e.g. a natural straight flush is a {@code STRAIGHT_FLUSH}, never
 * a {@code CRAZY_STRAIGHT}), since {@link HandType#FIVE_OF_A_KIND} and
 * {@link HandType#PRISMATIC_FLUSH} would otherwise be indistinguishable: because the deck has only
 * one card per suit+value combination, five natural cards of the same value are, by construction,
 * always from five different suits. {@code FIVE_OF_A_KIND} is therefore defined here as the
 * wild-assisted version and {@code PRISMATIC_FLUSH} as the natural-only version.
 */
public final class PokerHandEvaluator {

    private PokerHandEvaluator() {
    }

    /**
     * Evaluates a 7-card hand of logical card names.
     *
     * @param sevenCardNames the seven logical card names (wild names or {@code "<value> of <suit>"})
     * @param wilds          the list of wild card names
     * @param values         the canonical, rank-ordered list of value names
     * @return the best-matching hand result
     */
    public static HandResult evaluate(List<String> sevenCardNames, List<String> wilds, List<String> values) {
        List<Card> cards = sevenCardNames.stream().map(name -> Card.parse(name, wilds, values)).toList();
        return evaluate(cards, values.size());
    }

    /**
     * Evaluates a 7-card hand of already-parsed cards.
     *
     * @param cards      the seven parsed cards
     * @param valueCount the size of the canonical values list, used to locate the top rank window
     * @return the best-matching hand result
     */
    public static HandResult evaluate(List<Card> cards, int valueCount) {
        List<Card> naturals = cards.stream().filter(c -> !c.isWild()).toList();
        int wildCount = (int) cards.stream().filter(Card::isWild).count();

        HandResult result;
        if ((result = checkGalaxyFlush(naturals, valueCount)) != null) return result;
        if ((result = checkRealmRoyal(naturals, valueCount)) != null) return result;
        if ((result = checkCrazyStraight(naturals, wildCount, valueCount)) != null) return result;
        if ((result = checkStraightFlush(naturals, valueCount)) != null) return result;
        if ((result = checkFiveOfAKind(naturals, wildCount)) != null) return result;
        if ((result = checkHyperFlush(naturals, wildCount)) != null) return result;
        if ((result = checkPrismaticFlush(naturals)) != null) return result;
        if ((result = checkFlush(naturals)) != null) return result;
        if ((result = checkFourOfAKind(naturals)) != null) return result;
        if ((result = checkFullHouse(naturals)) != null) return result;
        if ((result = checkSuitChain(naturals, valueCount)) != null) return result;
        if ((result = checkStraight(naturals, valueCount)) != null) return result;
        if ((result = checkArcaneStraight(naturals, wildCount, valueCount)) != null) return result;
        if ((result = checkDoubleJokerBomb(cards, wildCount)) != null) return result;
        if ((result = checkSpectrum(naturals)) != null) return result;
        if ((result = checkThreeOfAKind(naturals)) != null) return result;
        if ((result = checkTwinRealm(naturals)) != null) return result;
        if ((result = checkTwoPair(naturals)) != null) return result;
        if ((result = checkOnePair(naturals)) != null) return result;
        return checkHighCard(naturals);
    }

    // --- Rank 1-2: top-window-only flushes ---

    private static HandResult checkGalaxyFlush(List<Card> naturals, int valueCount) {
        int windowStart = valueCount - 5;
        for (var entry : ranksBySuit(naturals).entrySet()) {
            if (containsFullWindow(entry.getValue(), windowStart)) {
                String suit = entry.getKey();
                List<Card> used = collectWindowCards(naturals, windowStart, c -> suit.equals(c.suit()));
                return buildResult(HandType.GALAXY_FLUSH, valueCount - 1, used);
            }
        }
        return null;
    }

    private static HandResult checkRealmRoyal(List<Card> naturals, int valueCount) {
        int windowStart = valueCount - 5;
        for (var entry : ranksByCourtSet(naturals).entrySet()) {
            if (containsFullWindow(entry.getValue(), windowStart)) {
                String group = entry.getKey();
                List<Card> used = collectWindowCards(naturals, windowStart, c -> group.equals(c.courtSet()));
                return buildResult(HandType.REALM_ROYAL, valueCount - 1, used);
            }
        }
        return null;
    }

    // --- Rank 3-4: any-window flushes ---

    private static HandResult checkCrazyStraight(List<Card> naturals, int wildCount, int valueCount) {
        if (wildCount < 1) return null;
        int bestTop = -1;
        String bestSuit = null;
        for (var entry : ranksBySuit(naturals).entrySet()) {
            int top = findBestWindowTop(entry.getValue(), valueCount, 1, wildCount);
            if (top > bestTop) {
                bestTop = top;
                bestSuit = entry.getKey();
            }
        }
        if (bestTop < 0) return null;
        String suit = bestSuit;
        List<Card> used = collectPartialWindowCards(naturals, bestTop - 4, c -> suit.equals(c.suit()));
        return buildResult(HandType.CRAZY_STRAIGHT, bestTop, used);
    }

    private static HandResult checkStraightFlush(List<Card> naturals, int valueCount) {
        int bestTop = -1;
        String bestSuit = null;
        for (var entry : ranksBySuit(naturals).entrySet()) {
            int top = findBestWindowTop(entry.getValue(), valueCount, 0, 0);
            if (top > bestTop) {
                bestTop = top;
                bestSuit = entry.getKey();
            }
        }
        if (bestTop < 0) return null;
        String suit = bestSuit;
        List<Card> used = collectWindowCards(naturals, bestTop - 4, c -> suit.equals(c.suit()));
        return buildResult(HandType.STRAIGHT_FLUSH, bestTop, used);
    }

    // --- Rank 5: wild-assisted five of a kind ---

    private static HandResult checkFiveOfAKind(List<Card> naturals, int wildCount) {
        if (wildCount < 1) return null;
        int bestRank = -1;
        List<Card> bestCards = null;
        for (var entry : groupByRank(naturals).entrySet()) {
            int naturalCount = entry.getValue().size();
            int wildsNeeded = 5 - naturalCount;
            if (naturalCount >= 1 && naturalCount <= 4 && wildsNeeded <= wildCount && entry.getKey() > bestRank) {
                bestRank = entry.getKey();
                bestCards = entry.getValue();
            }
        }
        if (bestRank < 0) return null;
        return buildResult(HandType.FIVE_OF_A_KIND, bestRank, bestCards);
    }

    // --- Rank 6: whole-hand single-suit flush ---

    private static HandResult checkHyperFlush(List<Card> naturals, int wildCount) {
        if (wildCount != 0 || naturals.size() != 7) return null;
        String suit = naturals.getFirst().suit();
        for (Card c : naturals) {
            if (!suit.equals(c.suit())) return null;
        }
        return buildResult(HandType.HYPER_FLUSH, topRank(naturals), naturals);
    }

    // --- Rank 7: natural-only five of a kind ---

    private static HandResult checkPrismaticFlush(List<Card> naturals) {
        int bestRank = -1;
        List<Card> bestCards = null;
        for (var entry : groupByRank(naturals).entrySet()) {
            if (entry.getValue().size() >= 5 && entry.getKey() > bestRank) {
                bestRank = entry.getKey();
                bestCards = entry.getValue().subList(0, 5);
            }
        }
        if (bestRank < 0) return null;
        return buildResult(HandType.PRISMATIC_FLUSH, bestRank, bestCards);
    }

    // --- Rank 8: ordinary flush ---

    private static HandResult checkFlush(List<Card> naturals) {
        int bestTop = -1;
        List<Card> bestCards = null;
        for (var entry : groupBySuit(naturals).entrySet()) {
            if (entry.getValue().size() < 5) continue;
            List<Card> sorted = entry.getValue().stream()
                    .sorted(Comparator.comparingInt(Card::rankIndex).reversed()).toList();
            List<Card> top5 = sorted.subList(0, 5);
            int top = top5.getFirst().rankIndex();
            if (top > bestTop) {
                bestTop = top;
                bestCards = top5;
            }
        }
        if (bestTop < 0) return null;
        return buildResult(HandType.FLUSH, bestTop, bestCards);
    }

    // --- Rank 9-10: natural groupings ---

    private static HandResult checkFourOfAKind(List<Card> naturals) {
        int bestRank = -1;
        List<Card> bestCards = null;
        for (var entry : groupByRank(naturals).entrySet()) {
            if (entry.getValue().size() >= 4 && entry.getKey() > bestRank) {
                bestRank = entry.getKey();
                bestCards = entry.getValue().subList(0, 4);
            }
        }
        if (bestRank < 0) return null;
        return buildResult(HandType.FOUR_OF_A_KIND, bestRank, bestCards);
    }

    private static HandResult checkFullHouse(List<Card> naturals) {
        Map<Integer, List<Card>> byRank = groupByRank(naturals);
        int bestTripRank = -1;
        List<Card> tripCards = null;
        for (var entry : byRank.entrySet()) {
            if (entry.getValue().size() >= 3 && entry.getKey() > bestTripRank) {
                bestTripRank = entry.getKey();
                tripCards = entry.getValue().subList(0, 3);
            }
        }
        if (bestTripRank < 0) return null;
        int bestPairRank = -1;
        List<Card> pairCards = null;
        for (var entry : byRank.entrySet()) {
            if (entry.getKey() == bestTripRank || entry.getValue().size() < 2) continue;
            if (entry.getKey() > bestPairRank) {
                bestPairRank = entry.getKey();
                pairCards = entry.getValue().subList(0, 2);
            }
        }
        if (bestPairRank < 0) return null;
        List<Card> used = new ArrayList<>(tripCards);
        used.addAll(pairCards);
        return buildResult(HandType.FULL_HOUSE, bestTripRank, used);
    }

    // --- Rank 11-12: any-window straights ---

    private static HandResult checkSuitChain(List<Card> naturals, int valueCount) {
        int bestTop = -1;
        String bestGroup = null;
        for (var entry : ranksByCourtSet(naturals).entrySet()) {
            int top = findBestWindowTop(entry.getValue(), valueCount, 0, 0);
            if (top > bestTop) {
                bestTop = top;
                bestGroup = entry.getKey();
            }
        }
        if (bestTop < 0) return null;
        String group = bestGroup;
        List<Card> used = collectWindowCards(naturals, bestTop - 4, c -> group.equals(c.courtSet()));
        return buildResult(HandType.SUIT_CHAIN, bestTop, used);
    }

    private static HandResult checkStraight(List<Card> naturals, int valueCount) {
        Set<Integer> ranks = allRanks(naturals);
        int top = findBestWindowTop(ranks, valueCount, 0, 0);
        if (top < 0) return null;
        List<Card> used = collectWindowCards(naturals, top - 4, c -> true);
        return buildResult(HandType.STRAIGHT, top, used);
    }

    // --- Rank 13: wild-assisted straight ---

    private static HandResult checkArcaneStraight(List<Card> naturals, int wildCount, int valueCount) {
        if (wildCount < 1) return null;
        Set<Integer> ranks = allRanks(naturals);
        int top = findBestWindowTop(ranks, valueCount, 1, wildCount);
        if (top < 0) return null;
        List<Card> used = collectPartialWindowCards(naturals, top - 4, c -> true);
        return buildResult(HandType.ARCANE_STRAIGHT, top, used);
    }

    // --- Rank 14: two or more wilds ---

    private static HandResult checkDoubleJokerBomb(List<Card> cards, int wildCount) {
        if (wildCount < 2) return null;
        List<String> used = cards.stream().filter(Card::isWild).map(Card::raw).toList();
        return new HandResult(HandType.DOUBLE_JOKER_BOMB, 0, List.of(), CardCategory.NEUTRAL,
                HandType.DOUBLE_JOKER_BOMB.score(CardCategory.NEUTRAL), used);
    }

    // --- Rank 15: six cards, six distinct Court Sets ---

    private static HandResult checkSpectrum(List<Card> naturals) {
        Map<String, Card> representative = new LinkedHashMap<>();
        for (Card c : naturals) {
            if (c.courtSet() != null) representative.putIfAbsent(c.courtSet(), c);
        }
        if (representative.size() < 6) return null;
        List<Card> used = new ArrayList<>(representative.values());
        return buildResult(HandType.SPECTRUM, topRank(used), used);
    }

    // --- Rank 16: natural three of a kind ---

    private static HandResult checkThreeOfAKind(List<Card> naturals) {
        int bestRank = -1;
        List<Card> bestCards = null;
        for (var entry : groupByRank(naturals).entrySet()) {
            if (entry.getValue().size() >= 3 && entry.getKey() > bestRank) {
                bestRank = entry.getKey();
                bestCards = entry.getValue().subList(0, 3);
            }
        }
        if (bestRank < 0) return null;
        return buildResult(HandType.THREE_OF_A_KIND, bestRank, bestCards);
    }

    // --- Rank 17: two pairs, each mono-Court-Set, differing Court Sets ---

    private record GroupedPair(int rank, String group, List<Card> cards) {
    }

    private static HandResult checkTwinRealm(List<Card> naturals) {
        List<GroupedPair> candidates = new ArrayList<>();
        for (var entry : groupByRank(naturals).entrySet()) {
            Map<String, List<Card>> byGroup = new HashMap<>();
            for (Card c : entry.getValue()) {
                if (c.courtSet() == null) continue;
                byGroup.computeIfAbsent(c.courtSet(), k -> new ArrayList<>()).add(c);
            }
            for (var g : byGroup.entrySet()) {
                if (g.getValue().size() >= 2) {
                    candidates.add(new GroupedPair(entry.getKey(), g.getKey(), g.getValue().subList(0, 2)));
                }
            }
        }
        if (candidates.size() < 2) return null;
        candidates.sort((a, b) -> Integer.compare(b.rank(), a.rank()));
        GroupedPair first = candidates.getFirst();
        for (int i = 1; i < candidates.size(); i++) {
            GroupedPair second = candidates.get(i);
            if (!second.group().equals(first.group())) {
                List<Card> used = new ArrayList<>(first.cards());
                used.addAll(second.cards());
                return buildResult(HandType.TWIN_REALM, first.rank(), used);
            }
        }
        return null;
    }

    // --- Rank 18-19: ordinary pairs ---

    private static HandResult checkTwoPair(List<Card> naturals) {
        Map<Integer, List<Card>> byRank = groupByRank(naturals);
        List<Integer> pairRanks = byRank.entrySet().stream()
                .filter(e -> e.getValue().size() >= 2).map(Map.Entry::getKey)
                .sorted(Collections.reverseOrder()).toList();
        if (pairRanks.size() < 2) return null;
        int r1 = pairRanks.get(0);
        int r2 = pairRanks.get(1);
        List<Card> used = new ArrayList<>(byRank.get(r1).subList(0, 2));
        used.addAll(byRank.get(r2).subList(0, 2));
        return buildResult(HandType.TWO_PAIR, r1, used);
    }

    private static HandResult checkOnePair(List<Card> naturals) {
        int bestRank = -1;
        List<Card> bestCards = null;
        for (var entry : groupByRank(naturals).entrySet()) {
            if (entry.getValue().size() >= 2 && entry.getKey() > bestRank) {
                bestRank = entry.getKey();
                bestCards = entry.getValue().subList(0, 2);
            }
        }
        if (bestRank < 0) return null;
        return buildResult(HandType.ONE_PAIR, bestRank, bestCards);
    }

    // --- Rank 20: fallback ---

    private static HandResult checkHighCard(List<Card> naturals) {
        if (naturals.isEmpty()) {
            return new HandResult(HandType.HIGH_CARD, -1, List.of(), CardCategory.NEUTRAL,
                    HandType.HIGH_CARD.score(CardCategory.NEUTRAL), List.of());
        }
        Card top = naturals.stream().max(Comparator.comparingInt(Card::rankIndex)).orElseThrow();
        return buildResult(HandType.HIGH_CARD, top.rankIndex(), List.of(top));
    }

    // --- shared helpers ---

    private static HandResult buildResult(HandType type, double primaryValueRank, List<Card> used) {
        Card categoryCard = used.stream().max(Comparator.comparingInt(Card::rankIndex)).orElse(null);
        CardCategory category = categoryCard != null ? categoryCard.category() : CardCategory.NEUTRAL;
        List<Integer> priorities = used.stream()
                .filter(c -> c.courtSet() != null)
                .map(c -> ThemeManager.getCourtSetPriority(c.courtSet()))
                .sorted()
                .collect(Collectors.toList());
        List<String> cardsUsed = used.stream().map(Card::raw).toList();
        return new HandResult(type, primaryValueRank, priorities, category, type.score(category), cardsUsed);
    }

    private static int topRank(List<Card> cards) {
        return cards.stream().mapToInt(Card::rankIndex).max().orElse(-1);
    }

    private static Map<Integer, List<Card>> groupByRank(List<Card> naturals) {
        Map<Integer, List<Card>> map = new LinkedHashMap<>();
        for (Card c : naturals) {
            map.computeIfAbsent(c.rankIndex(), k -> new ArrayList<>()).add(c);
        }
        return map;
    }

    private static Map<String, List<Card>> groupBySuit(List<Card> naturals) {
        Map<String, List<Card>> map = new LinkedHashMap<>();
        for (Card c : naturals) {
            map.computeIfAbsent(c.suit(), k -> new ArrayList<>()).add(c);
        }
        return map;
    }

    private static Map<String, Set<Integer>> ranksBySuit(List<Card> naturals) {
        Map<String, Set<Integer>> map = new HashMap<>();
        for (Card c : naturals) {
            map.computeIfAbsent(c.suit(), k -> new java.util.HashSet<>()).add(c.rankIndex());
        }
        return map;
    }

    private static Map<String, Set<Integer>> ranksByCourtSet(List<Card> naturals) {
        Map<String, Set<Integer>> map = new HashMap<>();
        for (Card c : naturals) {
            if (c.courtSet() == null) continue;
            map.computeIfAbsent(c.courtSet(), k -> new java.util.HashSet<>()).add(c.rankIndex());
        }
        return map;
    }

    private static Set<Integer> allRanks(List<Card> naturals) {
        Set<Integer> ranks = new java.util.HashSet<>();
        for (Card c : naturals) {
            ranks.add(c.rankIndex());
        }
        return ranks;
    }

    private static boolean containsFullWindow(Set<Integer> presentRanks, int windowStart) {
        for (int r = windowStart; r < windowStart + 5; r++) {
            if (!presentRanks.contains(r)) return false;
        }
        return true;
    }

    /**
     * Scans 5-value windows from the top of the value range downward, returning the top rank of
     * the highest window whose count of missing ranks falls within {@code [minMissing, maxMissing]}.
     *
     * @return the window's top rank, or -1 if no qualifying window exists
     */
    private static int findBestWindowTop(Set<Integer> presentRanks, int valueCount, int minMissing, int maxMissing) {
        for (int start = valueCount - 5; start >= 0; start--) {
            int missing = 0;
            for (int r = start; r < start + 5; r++) {
                if (!presentRanks.contains(r)) missing++;
            }
            if (missing >= minMissing && missing <= maxMissing) {
                return start + 4;
            }
        }
        return -1;
    }

    private static List<Card> collectWindowCards(List<Card> naturals, int windowStart, Predicate<Card> filter) {
        List<Card> used = new ArrayList<>();
        for (int r = windowStart; r < windowStart + 5; r++) {
            int rank = r;
            naturals.stream().filter(c -> c.rankIndex() == rank).filter(filter).findFirst().ifPresent(used::add);
        }
        return used;
    }

    private static List<Card> collectPartialWindowCards(List<Card> naturals, int windowStart, Predicate<Card> filter) {
        return collectWindowCards(naturals, windowStart, filter);
    }
}
