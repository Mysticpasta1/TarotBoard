package com.mystic.tarotboard.scenes;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * A utility class for building the rich text and table-based content
 * displayed in the help and rules section of the application.
 */
public final class HelpContent {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private HelpContent() {
    }

    /**
     * Populates the given VBox container with all the formatted help content,
     * including headers, text blocks, and tables.
     *
     * @param container The VBox to which the help content will be added.
     */
    public static void buildContent(VBox container) {
        container.getChildren().addAll(
            h1("TarotBoard Poker — Complete Hand Ranking Guide"),
            h1("Table of Contents"),
            text("""
                 1. Default Keybinds
                 2. Overview
                 3. Deck Structure
                 4. Court Sets & Colors
                 5. Hand Rankings
                 6. Wild Cards
                 7. Tiebreakers & Special Rules
                 8. Scoring System
                 """),
            separator(),

            h2("Default Keybinds"),
            text("""
                 Cards / Chips:
                   Drag ......................... Move
                   Double-click (left) ......... Flip
                   Shift+Click (left) .......... Rotate -1°
                   Ctrl+Click (left) ........... Rotate -90°
                   Shift+Click (right) ......... Rotate +1°
                   Ctrl+Click (right) .......... Rotate +90°
                   Double-click (right) ........ Reset rotation

                 Dice:
                   Double-click ................ Roll
                   Drag ........................ Move

                 General:
                   Tab ......................... Show player list (multiplayer)
                   F ........................... Multi-flip chips while hovering
                   Drag onto ✖ zone .......... Delete item

                 Keybinds are configurable from the Settings menu on the
                 main screen."""),

            h2("Overview"),
            text("""
                 TarotBoard Poker is a mystical, cosmic twist on classic poker.
                 Featuring:

                 - 4255 unique cards
                 - 48 suits grouped into 6 thematic Court Sets
                 - 87 values per suit, spanning Negative, Neutral, and Positive cards
                 - 79 Wild Joker-type cards with unique powers
                 """),
            separator(),

            h2("Deck Structure"),
            h3("Wild Cards"),
            buildWildCardsTable(),

            h3("Suits by Court Sets"),
            buildSuitsTable(),

            h3("Values"),
            buildValuesTable(),

            h2("Court Sets & Colors"),
            buildCourtSetsTable(),
            separator(),

            h2("Hand Rankings"),
            h3("Mythic Hands (Highest Tier)"),
            buildMythicHandsTable(),

            h3("Legendary Hands"),
            buildLegendaryHandsTable(),

            h3("Core Hands (Classic + Custom)"),
            buildCoreHandsTable(),
            separator(),

            h2("Wild Cards"),
            text("""
                 - Wilds include cards such as Joker, Soul, Light, Dark, Judgment, Voice, Chaos, etc.
                 - They substitute any card to complete combos like Five of a Kind, Arcane Straight, or Double Joker Bomb.
                 - Multiple wilds enable special combos like Double Joker Bomb.
                 - Players must declare intended wild card use when played.
                 """),
            separator(),

            h2("Tiebreakers & Special Rules"),
            text("""
                 - Higher values win ties (e.g., Angel beats Phoenix, which beats Raven).
                 - If values tie, suits from Court Sets break ties using a defined hierarchy
                   (e.g., Celestial > Umbral > Infernal > Verdant > Aetheric > Expansion).
                 - Wild cards don't break ties but complete winning combos.
                 """),
            separator(),

            h2("Scoring System"),
            h3("Overview"),
            text("""
                 TarotBoard Poker uses a tiered scoring system to rank hands, taking into account the card category (positive, neutral,
                 negative), the hand type, hand value, card values, suits, and wild cards.
                 """),
            h3("Scoring Table"),
            buildScoringTable(),
            separator(),

            h2("Flavor & Gameplay Notes"),
            text("""
                 - Hands referencing Court Sets add lore-driven layers.
                 - Players can roleplay hands ("I call upon the Infernal Pact with a Crazy Straight!").
                 - Wild cards add unpredictability and excitement.
                 - Use Court colors for UI highlights and chips matching players' dominant sets.
                 """),
            separator(),

            h1("Fortune smiles upon the bold. ✨✨✨")
        );
    }

    /**
     * Creates a styled Label for a main heading (H1).
     * @param t The text content of the heading.
     * @return A new Label configured as an H1 heading.
     */
    private static Label h1(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        l.setPadding(new Insets(15, 0, 10, 0));
        return l;
    }

    /**
     * Creates a styled Label for a subheading (H2).
     * @param t The text content of the subheading.
     * @return A new Label configured as an H2 subheading.
     */
    private static Label h2(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        l.setPadding(new Insets(12, 0, 8, 0));
        return l;
    }

    /**
     * Creates a styled Label for a minor heading (H3).
     * @param t The text content of the minor heading.
     * @return A new Label configured as an H3 minor heading.
     */
    private static Label h3(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: lightgray;");
        l.setPadding(new Insets(10, 0, 5, 0));
        return l;
    }

    /**
     * Creates a styled Label for standard body text.
     * @param t The text content.
     * @return A new Label configured for body text.
     */
    private static Label text(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        l.setWrapText(true);
        return l;
    }

    /**
     * Creates a styled Label to act as a visual separator.
     * @return A new Label configured as a separator.
     */
    private static Label separator() {
        Label l = new Label("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        l.setStyle("-fx-text-fill: gray;");
        return l;
    }

    /**
     * Creates and configures a new GridPane for displaying tabular data.
     * The grid is styled with a background and configured to create visible grid lines
     * by using the background color to fill the gaps between cells.
     *
     * @return A new, styled GridPane instance.
     */
    private static GridPane createTable() {
        GridPane grid = new GridPane();
        grid.setHgap(1);
        grid.setVgap(1);
        grid.setMaxWidth(Region.USE_PREF_SIZE);
        grid.setStyle("-fx-background-color: #888888; -fx-padding: 1;");
        return grid;
    }

    /**
     * Adds a new row of data to the specified GridPane. Each cell is a StackPane
     * containing a Label, allowing for background colors that create a grid effect.
     *
     * @param grid    The GridPane to which the row will be added.
     * @param row     The row index.
     * @param isHeader True if the row is a header, which applies a different style.
     * @param columns The string content for each column in the row.
     */
    private static void addRow(GridPane grid, int row, boolean isHeader, String... columns) {
        for (int i = 0; i < columns.length; i++) {
            Label l = new Label(columns[i]);
            l.setWrapText(true);
            l.setPadding(new Insets(5, 10, 5, 10));
            l.setMaxWidth(400);

            StackPane cell = new StackPane(l);
            cell.setStyle("-fx-background-color: #222222;");

            if (isHeader) {
                l.setStyle("-fx-font-weight: bold; -fx-text-fill: #FFCC00; -fx-font-size: 14px;");
            } else {
                l.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
            }
            grid.add(cell, i, row);
        }
    }

    /**
     * Builds and returns a GridPane displaying information about Wild Cards.
     * @return A GridPane containing the Wild Cards table.
     */
    private static GridPane buildWildCardsTable() {
        GridPane g = createTable();
        addRow(g, 0, true, "Wild Card Names", "Color (HEX)", "Color (Name)");
        addRow(g, 1, false, "Joker, Soul, Light, Dark, Judgement, Chorus, Life, Death, Wrath, Pride, Greed, Lust, Envy, Gluttony, Sloth, Chasity, Temperance, Charity, Diligence, Kindness, Patience, Humility, Voice, Voices, Mother, Father, Brother, Sister, Duality, Accord, Husband, Wife, Progeny, Corridor, Field, Intellect, Brawn, Despair, Past, Present, Future, Gate, Sign, Ruin, Snow, Rain, Tempest, Lovers, Discord, Concord, Harmony, Dissonance, Earth, Fire, Water, Air, Spirit, Oblivion, Obscurity, Purgatory, Nether, Underworld, Aether, Overworld, Limbo, Chaos, Balance, Doom, Peace, Evil, Good, Neutral, Hope, Monster, Human", "#E5E7EB", "Ghost White");
        return g;
    }

    /**
     * Builds and returns a GridPane displaying the different card suits grouped by Court Set.
     * @return A GridPane containing the suits table.
     */
    private static GridPane buildSuitsTable() {
        GridPane g = createTable();
        addRow(g, 0, true, "Court Set Name", "Suits", "Color (HEX)", "Color (Name)");
        addRow(g, 1, false, "The Celestial Court", "Stars, Suns, Crowns, Quasars, Crescents, Sigils, Comets, Glyphs", "#FFD700", "Royal Gold");
        addRow(g, 2, false, "The Umbral Dominion", "Veils, Runes, Hearts, Spirals, Eyes, Omens, Diamonds, Orbs", "#FF8800", "Fusion Orange");
        addRow(g, 3, false, "The Infernal Pact", "Arrows, Flames, Locks, Arcs, Swords, Points, Embers, Gears", "#DC143C", "Burning Crimson");
        addRow(g, 4, false, "The Verdant Cycle", "Flowers, Leaves, Mountains, Shells, Clovers, Tridents, Trees, Waves", "#228B22", "Forest Green");
        addRow(g, 5, false, "The Aetheric Loom", "Clouds, Crosses, Shields, Keys, Spades, Scrolls, Looms, Shards", "#1E90FF", "Aether Blue");
        addRow(g, 6, false, "The Dark Expanse", "Echoes, Rifts, Ashes, Nulls, Hallows, Fluxes, Ethers, Grims", "#AD03FC", "Vivid Orchid");
        return g;
    }

    /**
     * Builds and returns a GridPane detailing the card values and their categories.
     * @return A GridPane containing the card values table.
     */
    private static GridPane buildValuesTable() {
        GridPane g = createTable();
        addRow(g, 0, true, "Category", "Values", "Value Notes");
        addRow(g, 1, false, "Negative", "Devil, Shadow, Specter, Phantom, Wraith, Ghoul, Banshee, Reverent, Eidolon, Shade, Doppelganger, Hollow, Abyss, Chimera, Poltergeist, Wight, Apparition, Nightmare, Succubus, Incubus, Necromancer, Fury, Grim, Harbinger, Spectacle, Lich, Gorgon, Drake, Demon, Frost, Golem, Hydra, Inferno, Juggernaut, Kraken, Reaper, Leviathan, Manticore, Naga, Blight, Serpent", "Negative numeric values (e.g., -1 to -40)");
        addRow(g, 2, false, "Neutral", "Hold", "Neutral value 0");
        addRow(g, 3, false, "Positive", "Ace, 2, 3, 4, 5, 6, 7, 8, 9, 10, Jack, Queen, King, Nomad, Prince, Rune, Fable, Sorceress, Utopia, Wizard, Titan, Baron, Illusionist, Oracle, Magician, Luminary, Eclipse, Celestial, Duke, Genesis,Zephyr, Vesper, Umbra, Valkyrie, Warden, Zenith, Yggdrasil, Zodiac, Phoenix, Raven, Cipher, Angel", "Positive numeric values (1 to 40+)");
        return g;
    }

    /**
     * Builds and returns a GridPane describing the different Court Sets.
     * @return A GridPane containing the Court Sets table.
     */
    private static GridPane buildCourtSetsTable() {
        GridPane g = createTable();
        addRow(g, 0, true, "Set Name", "Description", "Associated Color - Name (Hex)");
        addRow(g, 1, false, "The Celestial", "Rulers of the cosmos — stars, suns, cosmic royalty", "Royal Gold (#FFD700)");
        addRow(g, 2, false, "The Umbral", "Shadow realm of secrets, spirits, occult power", "Fusion Orange (#FF8800)");
        addRow(g, 3, false, "The Infernal", "Fiery destruction, sin, and wrath", "Burning Crimson (#DC143C)");
        addRow(g, 4, false, "The Verdant", "Life, nature, cycles, and rebirth", "Forest Green (#228B22)");
        addRow(g, 5, false, "The Aetheric", "Fate, time, magic weaving threads of existence", "Aether Blue (#1E90FF)");
        addRow(g, 6, false, "The Expansion", "Void of forgotten realms, entropy, and echoes", "Vivid Orchid (#AD03FC)");
        return g;
    }

    /**
     * Builds and returns a GridPane for the Mythic tier of hand rankings.
     * @return A GridPane containing the Mythic Hands table.
     */
    private static GridPane buildMythicHandsTable() {
        GridPane g = createTable();
        addRow(g, 0, true, "Rank", "Name", "Description", "Example / Flavor", "Beats");
        addRow(g, 1, false, "1", "Galaxy Flush", "Highest 5 consecutive values (e.g. Zenith, Yggdrasil, Zodiac, Phoenix, Angel) all in the same suit", "Celestial royal straight flush", "Realm Royal and Below");
        addRow(g, 2, false, "2", "Realm Royal", "Royal Flush within the same Court Set suits", "Supreme domain flush", "Crazy Straight and Below");
        addRow(g, 3, false, "3", "Crazy Straight", "Straight flush including the value using 1 or more Wilds", "Straight with Wilds", "Straight Flush and Below");
        addRow(g, 4, false, "4", "Straight Flush", "Five consecutive values in the same suit", "Classic flush", "Five of a Kind and Below");
        addRow(g, 5, false, "5", "Five of a Kind", "Five cards of the same value, using Wilds", "Five Reapers with or without a Joker as one of them", "Hyper Flush and Below");
        addRow(g, 6, false, "6", "Hyper Flush", "Seven cards all in the same suit", "Flood of Waves", "Prismatic Flush and Below");
        addRow(g, 7, false, "7", "Prismatic Flush", "Five cards of the same value, each from different suits", "Five Shadows from different Courts", "Flush and Below");
        return g;
    }

    /**
     * Builds and returns a GridPane for the Legendary tier of hand rankings.
     * @return A GridPane containing the Legendary Hands table.
     */
    private static GridPane buildLegendaryHandsTable() {
        GridPane g = createTable();
        addRow(g, 0, true, "Rank", "Name", "Description", "Beats");
        addRow(g, 1, false, "8", "Flush", "Five cards same suit", "Four of a Kind and Below");
        addRow(g, 2, false, "9", "Four of a Kind", "Four cards of the same value", "Full House and Below");
        addRow(g, 3, false, "10", "Full House", "Three of a Kind + One Pair", "Suit Chain and Below");
        addRow(g, 4, false, "11", "Suit Chain", "Straight all from one Court Set", "Straight and Below");
        addRow(g, 5, false, "12", "Straight", "Five consecutive values", "Arcane Straight and Below");
        addRow(g, 6, false, "13", "Arcane Straight", "Straight using one or more Wild cards", "Double Joker Bomb and Below");
        addRow(g, 7, false, "14", "Double Joker Bomb", "Two or more Wild cards in hand", "Spectrum and Below");
        addRow(g, 8, false, "15", "Spectrum", "Six Cards each one from a different suit group", "Three of a Kind and Below");
        return g;
    }

    /**
     * Builds and returns a GridPane for the Core tier of hand rankings.
     * @return A GridPane containing the Core Hands table.
     */
    private static GridPane buildCoreHandsTable() {
        GridPane g = createTable();
        addRow(g, 0, true, "Rank", "Name", "Description", "Beats");
        addRow(g, 1, false, "15", "Three of a Kind", "Three cards of the same value", "Twin Realm and Below");
        addRow(g, 2, false, "16", "Twin Realm", "Two pairs from different Court Sets", "Two Pair and Below");
        addRow(g, 3, false, "17", "Two Pair", "Two pairs of same-value cards", "One Pair and Below");
        addRow(g, 4, false, "18", "One Pair", "Two cards of the same value", "High Cards");
        addRow(g, 5, false, "19", "High Cards", "Highest single card wins", "—");
        return g;
    }

    /**
     * Builds and returns a GridPane detailing the scoring for each hand type.
     * @return A GridPane containing the scoring table.
     */
    private static GridPane buildScoringTable() {
        GridPane g = createTable();
        addRow(g, 0, true, "Hand Name", "Positive", "Neutral", "Negative", "Notes");
        addRow(g, 1, false, "Galaxy Flush", "100,000", "54,750", "9,500", "Highest");
        addRow(g, 2, false, "Realm Royal", "95,000", "52,000", "9,000", "");
        addRow(g, 3, false, "Straight Inferno", "90,000", "49,250", "8,500", "");
        addRow(g, 4, false, "Straight Flush", "85,000", "46,500", "8,000", "");
        addRow(g, 5, false, "Five of a Kind", "80,000", "43,750", "7,500", "");
        addRow(g, 6, false, "Hyper Flush", "75,000", "40,000", "7,000", "");
        addRow(g, 7, false, "Prismatic Flush", "70,000", "36,250", "6,500", "");
        addRow(g, 8, false, "Four of a Kind", "65,000", "32,500", "6,000", "");
        addRow(g, 9, false, "Full House", "60,000", "28,750", "5,500", "");
        addRow(g, 10, false, "Flush", "55,000", "25,000", "5,000", "");
        addRow(g, 11, false, "Straight", "50,000", "21,250", "4,500", "");
        addRow(g, 12, false, "Suit Chain", "45,000", "20,000", "4,000", "");
        addRow(g, 13, false, "Arcane Straight", "40,000", "21,750", "3,500", "");
        addRow(g, 14, false, "Double Joker Bomb", "35,000", "19,000", "3,000", "");
        addRow(g, 15, false, "Three of a Kind", "30,000", "16,250", "2,500", "");
        addRow(g, 16, false, "Twin Realm", "25,000", "13,500", "2,000", "");
        addRow(g, 17, false, "Two Pair", "20,000", "10,750", "1,500", "");
        addRow(g, 18, false, "One Pair", "15,000", "8,000", "1,000", "");
        addRow(g, 19, false, "High Card", "10,000", "5,250", "500", "Lowest");
        return g;
    }
}
