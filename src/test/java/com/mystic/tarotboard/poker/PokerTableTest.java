package com.mystic.tarotboard.poker;

import com.mystic.tarotboard.TarotBoard;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives full hands through {@link PokerTable} end-to-end (ante, betting, showdown/fold-out)
 * without any UI or networking, per the plan's Phase B "hotseat testable" goal.
 */
class PokerTableTest {

    private static final List<String> WILDS = List.of("Joker", "Soul");

    private static Deque<String> deck(String... cards) {
        return new ArrayDeque<>(List.of(cards));
    }

    @Test
    void startHand_requiresAtLeastTwoSeatedPlayers() {
        PokerTable table = new PokerTable(10, 10, WILDS, TarotBoard.values);
        table.sitDown(1, false, 1000);
        assertThrows(IllegalStateException.class, () -> table.startHand(deck()));
    }

    @Test
    void fullHand_bothCall_strongerHandWinsShowdown() {
        PokerTable table = new PokerTable(10, 10, WILDS, TarotBoard.values);
        table.sitDown(1, false, 1000);
        table.sitDown(2, false, 1000);

        Deque<String> deck = deck(
                // Seat 1: Straight Flush
                "7 of Hearts", "8 of Hearts", "9 of Hearts", "10 of Hearts", "Jack of Hearts",
                "2 of Stars", "3 of Suns",
                // Seat 2: High Card
                "Eidolon of Stars", "Succubus of Suns", "Drake of Crowns", "Manticore of Quasars",
                "6 of Crescents", "Titan of Sigils", "Zephyr of Comets");
        table.startHand(deck);

        assertEquals(PokerTable.Phase.BETTING, table.phase());
        assertEquals(20, table.potTotal());
        assertEquals(10, table.currentBet());
        assertEquals(1, table.currentTurnPlayerId());

        assertTrue(table.applyAction(1, "CALL", 0));
        assertEquals(2, table.currentTurnPlayerId());
        assertTrue(table.applyAction(2, "CALL", 0));

        assertEquals(PokerTable.Phase.SHOWDOWN, table.phase());
        assertNull(table.currentTurnPlayerId());
        assertEquals(0, table.potTotal());
        assertEquals(List.of(1), table.lastShowdown().winningPlayerIds());
        assertEquals(20, table.lastShowdown().potWon());
        assertEquals(2, table.lastShowdown().results().size());

        assertSeatBankrolls(table, 1010, 990);
    }

    @Test
    void fold_endsHandImmediatelyWithoutShowdown() {
        PokerTable table = new PokerTable(10, 10, WILDS, TarotBoard.values);
        table.sitDown(1, false, 1000);
        table.sitDown(2, false, 1000);
        table.startHand(deck(
                "7 of Hearts", "8 of Hearts", "9 of Hearts", "10 of Hearts", "Jack of Hearts", "2 of Stars", "3 of Suns",
                "Eidolon of Stars", "Succubus of Suns", "Drake of Crowns", "Manticore of Quasars",
                "6 of Crescents", "Titan of Sigils", "Zephyr of Comets"));

        assertTrue(table.applyAction(1, "RAISE", 20));
        assertEquals(30, table.currentBet());
        assertEquals(2, table.currentTurnPlayerId());

        assertTrue(table.applyAction(2, "FOLD", 0));

        assertEquals(PokerTable.Phase.SHOWDOWN, table.phase());
        assertEquals(List.of(1), table.lastShowdown().winningPlayerIds());
        assertTrue(table.lastShowdown().results().isEmpty());
        assertEquals(40, table.lastShowdown().potWon());

        assertSeatBankrolls(table, 1010, 990);
    }

    @Test
    void applyAction_rejectsOutOfTurnAction() {
        PokerTable table = new PokerTable(10, 10, WILDS, TarotBoard.values);
        table.sitDown(1, false, 1000);
        table.sitDown(2, false, 1000);
        table.startHand(deck(
                "7 of Hearts", "8 of Hearts", "9 of Hearts", "10 of Hearts", "Jack of Hearts", "2 of Stars", "3 of Suns",
                "Eidolon of Stars", "Succubus of Suns", "Drake of Crowns", "Manticore of Quasars",
                "6 of Crescents", "Titan of Sigils", "Zephyr of Comets"));

        assertFalse(table.applyAction(2, "CALL", 0));
        assertEquals(1, table.currentTurnPlayerId());
    }

    @Test
    void applyAction_rejectsShortCallAndShortRaise() {
        PokerTable table = new PokerTable(10, 10, WILDS, TarotBoard.values);
        table.sitDown(1, false, 1000);
        table.sitDown(2, false, 1000);
        table.startHand(deck(
                "7 of Hearts", "8 of Hearts", "9 of Hearts", "10 of Hearts", "Jack of Hearts", "2 of Stars", "3 of Suns",
                "Eidolon of Stars", "Succubus of Suns", "Drake of Crowns", "Manticore of Quasars",
                "6 of Crescents", "Titan of Sigils", "Zephyr of Comets"));

        // Currently owes 0 to call (ante already matches currentBet); a raise of only 5 is below minRaise 10.
        assertFalse(table.applyAction(1, "RAISE", 5));
        assertEquals(1, table.currentTurnPlayerId());
        assertEquals(10, table.currentBet());
    }

    @Test
    void threeWayHand_tiedShowdownSplitsPotWithRemainderToFirstSeat() {
        PokerTable table = new PokerTable(10, 10, WILDS, TarotBoard.values);
        table.sitDown(1, false, 1000);
        table.sitDown(2, false, 1000);
        table.sitDown(3, false, 1000);

        // All three seats get an identical High Card hand (same top card, same suit/court set even)
        // so the showdown is a genuine 3-way tie: pot 30 split 10/10/10, no remainder to distribute.
        String[] hand = {"Eidolon of Stars", "Succubus of Suns", "Drake of Crowns", "Manticore of Quasars",
                "6 of Crescents", "Titan of Sigils", "Zephyr of Comets"};
        Deque<String> deck = new ArrayDeque<>();
        for (int i = 0; i < 3; i++) deck.addAll(List.of(hand));
        table.startHand(deck);

        assertTrue(table.applyAction(1, "CALL", 0));
        assertTrue(table.applyAction(2, "CALL", 0));
        assertTrue(table.applyAction(3, "CALL", 0));

        assertEquals(PokerTable.Phase.SHOWDOWN, table.phase());
        assertEquals(List.of(1, 2, 3), table.lastShowdown().winningPlayerIds());
        assertEquals(30, table.lastShowdown().potWon());
        for (PokerTable.Seat seat : table.seats()) {
            assertEquals(1000, seat.bankroll());
        }
    }

    private static void assertSeatBankrolls(PokerTable table, long seat1, long seat2) {
        for (PokerTable.Seat seat : table.seats()) {
            if (seat.playerId() == 1) assertEquals(seat1, seat.bankroll());
            if (seat.playerId() == 2) assertEquals(seat2, seat.bankroll());
        }
    }
}
