package com.mystic.tarotboard.poker;

import java.util.List;

/**
 * A read-only display mirror of the poker table, used to drive the UI on every client (host and
 * non-host alike). The authority ({@code TarotBoard} when hosting/single-player, or
 * {@code HeadlessServer}) refreshes this directly from its own {@link PokerTable} after every
 * mutation; a non-authoritative client refreshes it from an incoming {@code PokerStateSync}
 * network message. Either way, UI code only ever reads this one shape.
 */
public final class PokerClientState {

    /**
     * One seat's publicly-visible state (hole cards themselves are never included here).
     *
     * @param playerId           the seat's player id
     * @param isBot               whether this seat is AI-controlled
     * @param folded             whether this seat has folded this hand
     * @param active             whether this seat is currently occupied
     * @param holeCardCount      how many hole cards this seat holds (0 before a hand is dealt)
     * @param bankroll           this seat's remaining bankroll
     * @param contributionThisRound how much this seat has committed to the pot this betting round
     */
    public record SeatView(int playerId, boolean isBot, boolean folded, boolean active,
                            int holeCardCount, long bankroll, long contributionThisRound) {
    }

    public boolean pokerModeActive;
    public List<SeatView> seats = List.of();
    public int currentTurnPlayerId = -1;
    public long potTotal;
    public long currentBet;
    public PokerTable.Phase phase = PokerTable.Phase.LOBBY;
    public List<String> myHoleCards = List.of();
    public PokerTable.ShowdownResult lastShowdown;
}
