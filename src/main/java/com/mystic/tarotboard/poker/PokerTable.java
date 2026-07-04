package com.mystic.tarotboard.poker;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Turn-order and pot state for a single-betting-round Poker Mode table: seats, ante/call/raise/fold
 * actions, and an automatic showdown once betting is complete. Pure Java, no JavaFX or networking
 * dependency, so the same logic runs identically inside the embedded host ({@code TarotBoard}) and
 * the standalone {@code HeadlessServer}.
 * <p>
 * Scope, matching the approved plan: one ante, one betting round, no side pots (a seat that can't
 * cover a call/ante simply goes all-in for whatever remains of its bankroll). A fully tied showdown
 * splits the pot evenly, with any odd remainder going to the first tied seat in table order.
 */
public final class PokerTable {

    /**
     * The table's current phase.
     */
    public enum Phase {
        /** Seats can be claimed; no hand is in progress. */
        LOBBY,
        /** A hand has been dealt and players are acting in turn. */
        BETTING,
        /** The betting round is complete and the pot has been awarded. */
        SHOWDOWN
    }

    /**
     * One seat at the table: a real player or bot, its hole cards, bankroll, and per-round state.
     */
    public static final class Seat {
        private final int playerId;
        private final boolean isBot;
        private boolean active = true;
        private boolean folded;
        private boolean hasActed;
        private List<String> holeCards = List.of();
        private long bankroll;
        private long contributionThisRound;

        private Seat(int playerId, boolean isBot, long startingBankroll) {
            this.playerId = playerId;
            this.isBot = isBot;
            this.bankroll = startingBankroll;
        }

        public int playerId() {
            return playerId;
        }

        public boolean isBot() {
            return isBot;
        }

        public boolean active() {
            return active;
        }

        public boolean folded() {
            return folded;
        }

        public List<String> holeCards() {
            return holeCards;
        }

        public long bankroll() {
            return bankroll;
        }

        public long contributionThisRound() {
            return contributionThisRound;
        }
    }

    /**
     * One seat's revealed hand at showdown.
     *
     * @param playerId the seat's player id
     * @param hand     the evaluated hand result
     */
    public record ShowdownEntry(int playerId, HandResult hand) {
    }

    /**
     * The outcome of a completed hand.
     *
     * @param results         each contesting seat's revealed hand (empty if the hand ended by fold-out)
     * @param winningPlayerIds the player id(s) awarded the pot (more than one on a tie)
     * @param potWon          the total pot that was awarded
     */
    public record ShowdownResult(List<ShowdownEntry> results, List<Integer> winningPlayerIds, long potWon) {
    }

    private final List<Seat> seats = new ArrayList<>();
    private final long anteAmount;
    private final long minRaise;
    private final List<String> wilds;
    private final List<String> values;

    private int currentTurnSeatIndex = -1;
    private long potTotal;
    private long currentBet;
    private Phase phase = Phase.LOBBY;
    private ShowdownResult lastShowdown;

    /**
     * Constructs a new table.
     *
     * @param anteAmount the mandatory ante posted by every active seat at the start of each hand
     * @param minRaise   the minimum amount a raise must exceed the current bet by
     * @param wilds      the list of wild card names, used to evaluate showdown hands
     * @param values     the canonical, rank-ordered list of value names, used to evaluate showdown hands
     */
    public PokerTable(long anteAmount, long minRaise, List<String> wilds, List<String> values) {
        this.anteAmount = anteAmount;
        this.minRaise = minRaise;
        this.wilds = wilds;
        this.values = values;
    }

    /**
     * Claims a seat for a player (or bot), or returns their existing seat if already seated.
     *
     * @param playerId         the player id (a synthetic negative id for bots)
     * @param isBot            whether this seat is AI-controlled
     * @param startingBankroll the seat's starting bankroll
     * @return the seat
     */
    public Seat sitDown(int playerId, boolean isBot, long startingBankroll) {
        if (phase == Phase.BETTING) {
            throw new IllegalStateException("Cannot sit down mid-hand");
        }
        for (Seat s : seats) {
            if (s.playerId() == playerId) return s;
        }
        Seat seat = new Seat(playerId, isBot, startingBankroll);
        seats.add(seat);
        return seat;
    }

    public List<Seat> seats() {
        return List.copyOf(seats);
    }

    public Phase phase() {
        return phase;
    }

    public long potTotal() {
        return potTotal;
    }

    public long currentBet() {
        return currentBet;
    }

    public ShowdownResult lastShowdown() {
        return lastShowdown;
    }

    /**
     * Returns the player id whose turn it currently is, or {@code null} if no hand is in progress.
     *
     * @return the current turn's player id, or {@code null}
     */
    public Integer currentTurnPlayerId() {
        return currentTurnSeatIndex < 0 ? null : seats.get(currentTurnSeatIndex).playerId();
    }

    /**
     * Starts a new hand: resets per-round state, posts antes for every active seat, deals 7 cards
     * to each from the front of {@code shuffledDeck}, and opens betting with the first active seat.
     *
     * @param shuffledDeck the shuffled deck to deal from; cards are consumed from the front
     */
    public void startHand(Deque<String> shuffledDeck) {
        List<Seat> active = seats.stream().filter(Seat::active).toList();
        if (active.size() < 2) {
            throw new IllegalStateException("Poker Mode needs at least 2 seated players");
        }
        for (Seat s : seats) {
            s.folded = false;
            s.hasActed = false;
            s.contributionThisRound = 0;
            s.holeCards = List.of();
        }
        potTotal = 0;
        currentBet = anteAmount;
        lastShowdown = null;

        for (Seat s : active) {
            long ante = Math.min(anteAmount, s.bankroll);
            s.bankroll -= ante;
            s.contributionThisRound = ante;
            potTotal += ante;

            List<String> hand = new ArrayList<>(7);
            for (int i = 0; i < 7; i++) {
                String card = shuffledDeck.poll();
                if (card != null) hand.add(card);
            }
            s.holeCards = hand;
        }

        phase = Phase.BETTING;
        currentTurnSeatIndex = seats.indexOf(active.getFirst());
    }

    /**
     * Applies a betting action for the seat whose turn it currently is.
     *
     * @param playerId the acting player's id; must match whose turn it is
     * @param action   one of {@code "CALL"}, {@code "RAISE"}, or {@code "FOLD"}
     * @param amount   for CALL/RAISE, the additional amount the seat commits this action
     * @return {@code true} if the action was valid and applied, {@code false} otherwise
     */
    public boolean applyAction(int playerId, String action, long amount) {
        if (phase != Phase.BETTING) return false;
        Seat seat = currentTurnSeatIndex < 0 ? null : seats.get(currentTurnSeatIndex);
        if (seat == null || seat.playerId() != playerId || seat.folded()) return false;

        switch (action) {
            case "FOLD" -> seat.folded = true;
            case "CALL" -> {
                long required = currentBet - seat.contributionThisRound;
                if (amount < required) return false;
                commit(seat, amount);
            }
            case "RAISE" -> {
                long newContribution = seat.contributionThisRound + amount;
                if (newContribution - currentBet < minRaise) return false;
                commit(seat, amount);
                currentBet = seat.contributionThisRound;
                resetActedExcept(seat);
            }
            default -> {
                return false;
            }
        }
        seat.hasActed = true;

        List<Seat> stillIn = seats.stream().filter(s -> s.active() && !s.folded()).toList();
        if (stillIn.size() == 1 || stillIn.stream().allMatch(s -> s.hasActed && s.contributionThisRound == currentBet)) {
            concludeHand(stillIn);
        } else {
            advanceTurn();
        }
        return true;
    }

    private void commit(Seat seat, long amount) {
        long spend = Math.min(amount, seat.bankroll);
        seat.bankroll -= spend;
        seat.contributionThisRound += spend;
        potTotal += spend;
    }

    private void resetActedExcept(Seat raiser) {
        for (Seat s : seats) {
            if (s != raiser && s.active() && !s.folded()) s.hasActed = false;
        }
    }

    private void advanceTurn() {
        int size = seats.size();
        for (int step = 1; step <= size; step++) {
            int idx = (currentTurnSeatIndex + step) % size;
            Seat s = seats.get(idx);
            if (s.active() && !s.folded()) {
                currentTurnSeatIndex = idx;
                return;
            }
        }
    }

    private void concludeHand(List<Seat> stillIn) {
        phase = Phase.SHOWDOWN;
        currentTurnSeatIndex = -1;

        if (stillIn.size() == 1) {
            Seat winner = stillIn.getFirst();
            winner.bankroll += potTotal;
            lastShowdown = new ShowdownResult(List.of(), List.of(winner.playerId()), potTotal);
            potTotal = 0;
            return;
        }

        List<ShowdownEntry> entries = new ArrayList<>();
        for (Seat s : stillIn) {
            entries.add(new ShowdownEntry(s.playerId(), PokerHandEvaluator.evaluate(s.holeCards(), wilds, values)));
        }

        List<ShowdownEntry> winners = new ArrayList<>();
        winners.add(entries.getFirst());
        for (int i = 1; i < entries.size(); i++) {
            ShowdownEntry e = entries.get(i);
            int cmp = e.hand().compareStrength(winners.getFirst().hand());
            if (cmp < 0) {
                winners.clear();
                winners.add(e);
            } else if (cmp == 0) {
                winners.add(e);
            }
        }

        long share = potTotal / winners.size();
        long remainder = potTotal % winners.size();
        for (int i = 0; i < winners.size(); i++) {
            Seat seat = findSeat(winners.get(i).playerId());
            seat.bankroll += share + (i == 0 ? remainder : 0);
        }

        lastShowdown = new ShowdownResult(entries, winners.stream().map(ShowdownEntry::playerId).toList(), potTotal);
        potTotal = 0;
    }

    private Seat findSeat(int playerId) {
        for (Seat s : seats) {
            if (s.playerId() == playerId) return s;
        }
        throw new IllegalStateException("Unknown seat: " + playerId);
    }
}
