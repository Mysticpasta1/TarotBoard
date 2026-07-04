package com.mystic.tarotboard.poker;

import java.util.List;
import java.util.Random;

/**
 * A simple call/fold/raise heuristic for AI-controlled seats, used to fill empty seats in
 * single-player Poker Mode. No deep strategy: hand strength is derived directly from the
 * {@link PokerHandEvaluator}'s rank, with a small amount of random jitter so bots aren't fully
 * deterministic/exploitable.
 */
public final class PokerBotAI {

    private PokerBotAI() {
    }

    /**
     * A bot's chosen action and the amount to commit (for CALL/RAISE; ignored for FOLD).
     *
     * @param action one of {@code "CALL"}, {@code "RAISE"}, or {@code "FOLD"}
     * @param amount the additional amount to commit this action
     */
    public record BotDecision(String action, long amount) {
    }

    /**
     * Decides a bot's action for its turn.
     *
     * @param holeCards          the bot's 7 hole cards
     * @param wilds              the list of wild card names
     * @param values             the canonical, rank-ordered list of value names
     * @param currentBet         the table's current bet to match
     * @param myContribution     how much this seat has already committed this betting round
     * @param bankroll           this seat's remaining bankroll
     * @param minRaise           the table's minimum raise increment
     * @param random             a source of randomness for jitter (inject for deterministic tests)
     * @return the bot's decision
     */
    public static BotDecision decide(List<String> holeCards, List<String> wilds, List<String> values,
                                      long currentBet, long myContribution, long bankroll, long minRaise,
                                      Random random) {
        HandResult hand = PokerHandEvaluator.evaluate(holeCards, wilds, values);
        long required = Math.min(Math.max(0, currentBet - myContribution), bankroll);

        double strength = (21 - hand.type().rank()) / 20.0;
        double jitter = (random.nextDouble() - 0.5) * 0.3;
        double effective = Math.min(1.0, Math.max(0.0, strength + jitter));

        if (effective >= 0.75) {
            long raiseAmount = required + minRaise;
            if (bankroll >= raiseAmount && minRaise > 0) {
                return new BotDecision("RAISE", raiseAmount);
            }
            return new BotDecision("CALL", required);
        }
        if (effective >= 0.45) {
            boolean affordable = required == 0 || bankroll == 0 || required <= bankroll * 0.3;
            if (affordable) {
                return new BotDecision("CALL", required);
            }
            return new BotDecision("FOLD", 0);
        }
        if (required == 0) {
            return new BotDecision("CALL", 0);
        }
        return new BotDecision("FOLD", 0);
    }
}
