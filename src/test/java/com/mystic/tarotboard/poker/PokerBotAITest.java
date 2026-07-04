package com.mystic.tarotboard.poker;

import com.mystic.tarotboard.TarotBoard;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PokerBotAITest {

    private static final List<String> WILDS = List.of("Joker", "Soul");

    @Test
    void strongHand_raisesWhenAffordable() {
        List<String> straightFlush = List.of(
                "7 of Hearts", "8 of Hearts", "9 of Hearts", "10 of Hearts", "Jack of Hearts",
                "2 of Stars", "3 of Suns");
        PokerBotAI.BotDecision decision = PokerBotAI.decide(straightFlush, WILDS, TarotBoard.values,
                10, 10, 1000, 10, new Random(1));
        assertEquals("RAISE", decision.action());
        assertEquals(10, decision.amount());
    }

    @Test
    void weakHand_foldsWhenCallIsNonzero() {
        List<String> highCard = List.of(
                "Eidolon of Stars", "Succubus of Suns", "Drake of Crowns", "Manticore of Quasars",
                "6 of Crescents", "Titan of Sigils", "Zephyr of Comets");
        PokerBotAI.BotDecision decision = PokerBotAI.decide(highCard, WILDS, TarotBoard.values,
                50, 10, 1000, 10, new Random(1));
        assertEquals("FOLD", decision.action());
    }

    @Test
    void weakHand_callsWhenNothingOwed() {
        List<String> highCard = List.of(
                "Eidolon of Stars", "Succubus of Suns", "Drake of Crowns", "Manticore of Quasars",
                "6 of Crescents", "Titan of Sigils", "Zephyr of Comets");
        PokerBotAI.BotDecision decision = PokerBotAI.decide(highCard, WILDS, TarotBoard.values,
                10, 10, 1000, 10, new Random(1));
        assertEquals("CALL", decision.action());
        assertEquals(0, decision.amount());
    }
}
