package com.mystic.tarotboard.utils;

import java.util.List;

/**
 * The one definition of what a deck contains.
 *
 * <p>The client and the dedicated server each used to carry their own copy of these
 * lists, and they drifted: the server's values were missing "Unknown" and "Venom", so it
 * dealt 4255 cards against the client's 4351. Nothing failed loudly — the client drops a
 * {@code CardNamesSync} whose length does not match its own deck — so every player simply
 * kept a private shuffle and saw different cards from everyone else, and a reshuffle
 * looked like it did nothing.
 *
 * <p>Deliberately free of JavaFX so the headless server, whose jar excludes the JavaFX
 * modules entirely, can share it rather than keep a second copy.
 */
public final class CardCatalog {

    private CardCatalog() {
    }

    /** Cards that stand outside the suits, carrying only a name. */
    public static final List<String> WILDS = List.of(
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

    /** Every suit a valued card can belong to. */
    public static final List<String> SUITS = List.of(
            "Arcs", "Arrows", "Clouds", "Clovers", "Comets", "Crescents", "Crosses",
            "Crowns", "Diamonds", "Embers", "Eyes", "Gears", "Glyphs", "Flames", "Flowers",
            "Hearts", "Keys", "Locks", "Leaves", "Mountains", "Points", "Scrolls", "Shells",
            "Shields", "Spades", "Spirals", "Stars", "Suns", "Swords", "Tridents", "Trees", "Waves",
            "Quasars", "Runes", "Omens", "Sigils", "Orbs", "Veils", "Looms", "Shards", "Echoes",
            "Rifts", "Ashes", "Nulls", "Hallows", "Fluxes", "Ethers", "Grims"
    );

    /**
     * All possible card values in the deck, including numbered ranks, face cards, and
     * supernatural entity names.
     *
     * <p>The order is meaningful: {@link com.mystic.tarotboard.items.Cards} takes a card's
     * index here as its rank, so inserting a value renumbers everything after it.
     */
    public static final List<String> VALUES = List.of(
            "Fugitive", "Devil", "Shadow", "Specter", "Phantom", "Void", "Wraith",
            "Ghoul", "Banshee", "Reverent", "Eidolon", "Shade",
            "Doppelganger", "Hollow", "Abyss", "Chimera", "Poltergeist",
            "Wight", "Apparition", "Nightmare", "Succubus", "Incubus", "Unknown",
            "Necromancer", "Fury", "Grim", "Harbinger", "Spectacle",
            "Lich", "Gorgon", "Drake", "Demon", "Frost",
            "Golem", "Hydra", "Inferno", "Juggernaut", "Kraken", "Reaper",
            "Leviathan", "Manticore", "Naga", "Blight", "Serpent",

            "Hold",

            "Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10",
            "Jack", "Queen", "King", "Nomad", "Prince",
            "Rune", "Fable", "Sorceress", "Utopia", "Wizard",
            "Titan", "Baron", "Illusionist", "Oracle", "Magician",
            "Luminary", "Eclipse", "Celestial", "Duke", "Genesis",
            "Zephyr", "Vesper", "Umbra", "Valkyrie", "Warden",
            "Zenith", "Yggdrasil", "Zodiac", "Phoenix", "Raven",
            "Cipher", "Angel", "Knight", "Venom"
    );

    /** How many cards a full deck holds: every suit crossed with every value, plus the wilds. */
    public static final int NUM_CARDS = (SUITS.size() * VALUES.size()) + WILDS.size();
}
