# TarotBoard Poker — Complete Hand Ranking Guide

# Table of Contents

1. [Default Keybinds](#default-keybinds)
2. [Overview](#overview)
3. [Deck Structure](#deck-structure)
4. [Court Sets & Colors](#court-sets--colors)
5. [Hand Rankings](#hand-rankings)
6. [Wild Cards](#wild-cards)
7. [Tiebreakers & Special Rules](#tiebreakers--special-rules)
8. [Scoring System](#scoring-system)

---

## Default Keybinds

            Cards / Chips:
              Drag ......................... Move
              Double-click (left) ......... Flip
              Hover + F ................... Multi-flip (chips only)
              Shift+Click (left) .......... Rotate -1°
              Ctrl+Click (left) ........... Rotate -90°
              Shift+Click (right) ......... Rotate +1°
              Ctrl+Click (right) .......... Rotate +90°
              Double-click (right) ........ Reset rotation
            
            Dice:
              Double-click ................ Roll
              Drag ........................ Move
            
            General:
              Drag onto ✖ zone ......... Delete item

## Overview

TarotBoard Poker is a mystical, cosmic twist on classic poker.  
Featuring:

- **4255 unique cards**
- **48 suits** grouped into 6 thematic Court Sets
- **87 values per suit**, spanning Negative, Neutral, and Positive cards
- **79 Wild Joker-type cards** with unique powers

---

## Deck Structure

### Wild Cards

| Wild Card Names                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            | Color (HEX) | Color (Name) |
|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------|--------------|
| Joker, Soul, Light, Dark, Judgement, Chorus, Life, Death, Wrath, Pride, Greed, Lust, Envy, Gluttony, Sloth, Chasity, Temperance, Charity, Diligence, Kindness, Patience, Humility, Voice, Voices, Mother, Father, Brother, Sister, Duality, Accord, Husband, Wife, Progeny, Corridor, Field, Intellect, Brawn, Despair, Past, Present, Future, Gate, Sign, Ruin, Snow, Rain, Tempest, Lovers, Discord, Concord, Harmony, Dissonance, Earth, Fire, Water, Air, Spirit, Oblivion, Obscurity, Purgatory, Nether, Underworld, Aether, Overworld, Limbo, Chaos, Balance, Doom, Peace, Evil, Good, Neutral, Hope, Monster, Human | #E5E7EB     | Ghost White  |

### Suits by Court Sets

| Court Set Name      | Suits                                                                | Color (HEX) | Color (Name)    |
|---------------------|----------------------------------------------------------------------|-------------|-----------------|
| The Celestial Court | Stars, Suns, Crowns, Quasars, Crescents, Sigils, Comets, Glyphs      | #FFD700     | Royal Gold      |
| The Umbral Dominion | Veils, Runes, Hearts, Spirals, Eyes, Omens, Diamonds, Orbs           | #FF8800     | Fusion Orange   |
| The Infernal Pact   | Arrows, Flames, Locks, Arcs, Swords, Points,  Embers, Gears          | #DC143C     | Burning Crimson |
| The Verdant Cycle   | Flowers, Leaves, Mountains, Shells, Clovers,  Tridents, Trees, Waves | #228B22     | Forest Green    |
| The Aetheric Loom   | Clouds, Crosses, Shields, Keys, Spades, Scrolls, Looms, Shards       | #1E90FF     | Aether Blue     |
| The Dark Expanse    | Echoes, Rifts, Ashes, Nulls, Hallows, Fluxes, Ethers, Grims          | #AD03FC     | Vivid Orchid    |

### Values

| Category | Values                                                                                                                                                                                                                                                                                                                                                                  | Value Notes                               |  
|----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------|  
| Negative | Devil, Shadow, Specter, Phantom, Wraith, Ghoul, Banshee, Reverent, Eidolon, Shade, Doppelganger, Hollow, Abyss, Chimera, Poltergeist, Wight, Apparition, Nightmare, Succubus, Incubus, Necromancer, Fury, Grim, Harbinger, Spectacle, Lich, Gorgon, Drake, Demon, Frost, Golem, Hydra, Inferno, Juggernaut, Kraken, Reaper, Leviathan, Manticore, Naga, Blight, Serpent | Negative numeric values (e.g., -1 to -40) |   
| Neutral  | Hold                                                                                                                                                                                                                                                                                                                                                                    | Neutral value 0                           |  
| Positive | Ace, 2, 3, 4, 5, 6, 7, 8, 9, 10, Jack, Queen, King, Nomad, Prince, Rune, Fable, Sorceress, Utopia, Wizard, Titan, Baron, Illusionist, Oracle, Magician, Luminary, Eclipse, Celestial, Duke, Genesis,Zephyr, Vesper, Umbra, Valkyrie, Warden, Zenith, Yggdrasil, Zodiac, Phoenix, Raven, Cipher, Angel                                                                   | Positive numeric values (1 to 40+)        |

## Court Sets & Colors

| Set Name          | Description                                        | Associated Color - Name (Hex) |  
|-------------------|----------------------------------------------------|-------------------------------|  
| **The Celestial** | Rulers of the cosmos — stars, suns, cosmic royalty | Royal Gold (#FFD700)          |  
| **The Umbral**    | Shadow realm of secrets, spirits, occult power     | Fusion Orange (#FF8800)       |  
| **The Infernal**  | Fiery destruction, sin, and wrath                  | Burning Crimson (#DC143C)     |  
| **The Verdant**   | Life, nature, cycles, and rebirth                  | Forest Green (#228B22)        |  
| **The Aetheric**  | Fate, time, magic weaving threads of existence     | Aether Blue (#1E90FF)         |  
| **The Expansion** | Void of forgotten realms, entropy, and echoes      | Vivid Orchid (#AD03FC)        |

---

## Hand Rankings

### Mythic Hands (Highest Tier)

| Rank | Name                | Description                                                                                        | Example / Flavor                                    | Beats                     |  
|------|---------------------|----------------------------------------------------------------------------------------------------|-----------------------------------------------------|---------------------------|  
| 1    | **Galaxy Flush**    | Highest 5 consecutive values (e.g. Zenith, Yggdrasil, Zodiac, Phoenix, Angel) all in the same suit | Celestial royal straight flush                      | Realm Royal and Below     |
| 2    | **Realm Royal**     | Royal Flush within the same Court Set suits                                                        | Supreme domain flush                                | Crazy Straight and Below  |  
| 3    | **Crazy Straight**  | Straight flush including the value using 1 or more Wilds                                           | Straight with Wilds                                 | Straight Flush and Below  |  
| 4    | **Straight Flush**  | Five consecutive values in the same suit                                                           | Classic flush                                       | Five of a Kind and Below  |  
| 5    | **Five of a Kind**  | Five cards of the same value, using Wilds                                                          | Five Reapers with or without a Joker as one of them | Hyper Flush and Below     |  
| 6    | **Hyper Flush**     | Seven cards all in the same suit                                                                   | Flood of Waves                                      | Prismatic Flush and Below |  
| 7    | **Prismatic Flush** | Five cards of the same value, each from different suits                                            | Five Shadows from different Courts                  | Flush and Below           |

### Legendary Hands

| Rank | Name              | Description                                    | Beats                       |  
|------|-------------------|------------------------------------------------|-----------------------------|  
| 8    | Flush             | Five cards same suit                           | Four of a Kind and Below    |
| 9    | Four of a Kind    | Four cards of the same value                   | Full House and Below        |  
| 10   | Full House        | Three of a Kind + One Pair                     | Suit Chain and Below        |
| 11   | Suit Chain        | Straight all from one Court Set                | Straight and Below          |
| 12   | Straight          | Five consecutive values                        | Arcane Straight and Below   |  
| 13   | Arcane Straight   | Straight using one or more Wild cards          | Double Joker Bomb and Below |  
| 14   | Double Joker Bomb | Two or more Wild cards in hand                 | Spectrum and Below          |
| 15   | Spectrum          | Six Cards each one from a different suit group | Three of a Kind and Below   |

### Core Hands (Classic + Custom)

| Rank | Name            | Description                         | Beats                |  
|------|-----------------|-------------------------------------|----------------------|  
| 15   | Three of a Kind | Three cards of the same value       | Twin Realm and Below |  
| 16   | Twin Realm      | Two pairs from different Court Sets | Two Pair and Below   |  
| 17   | Two Pair        | Two pairs of same-value cards       | One Pair and Below   |
| 18   | One Pair        | Two cards of the same value         | High Cards           |  
| 19   | High Cards      | Highest single card wins            | —                    |

---

## Wild Cards

- Wilds include cards such as Joker, Soul, Light, Dark, Judgment, Voice, Chaos, etc.
- They substitute any card to complete combos like Five of a Kind, Arcane Straight, or Double Joker Bomb.
- Multiple wilds enable special combos like Double Joker Bomb.
- Players must declare intended wild card use when played.

---

## Tiebreakers & Special Rules

- Higher values win ties (e.g., Angel beats Phoenix, which beats Raven).
- If values tie, suits from Court Sets break ties using a defined hierarchy  
  (e.g., Celestial > Umbral > Infernal > Verdant > Aetheric > Expansion).
- Wild cards don’t break ties but complete winning combos.

---

## Scoring System

### Overview

TarotBoard Poker uses a tiered scoring system to rank hands, taking into account the card category (positive, neutral,
negative), the hand type, card values, suits, and wild cards.

---

### Scoring Table

| Hand Name         | Positive | Neutral | Negative | Notes   |
|-------------------|----------|---------|----------|---------|
| Galaxy Flush      | 100,000  | 54,750  | 9,500    | Highest |
| Realm Royal       | 95,000   | 52,000  | 9,000    |         |
| Straight Inferno  | 90,000   | 49,250  | 8,500    |         |
| Straight Flush    | 85,000   | 46,500  | 8,000    |         |
| Five of a Kind    | 80,000   | 43,750  | 7,500    |         |
| Hyper Flush       | 75,000   | 40,000  | 7,000    |         |
| Prismatic Flush   | 70,000   | 36,250  | 6,500    |         |
| Four of a Kind    | 65,000   | 32,500  | 6,000    |         |
| Full House        | 60,000   | 28,750  | 5,500    |         |
| Flush             | 55,000   | 25,000  | 5,000    |         |
| Straight          | 50,000   | 21,250  | 4,500    |         |
| Suit Chain        | 45,000   | 20,000  | 4,000    |         |
| Arcane Straight   | 40,000   | 21,750  | 3,500    |         |
| Double Joker Bomb | 35,000   | 19,000  | 3,000    |         |
| Three of a Kind   | 30,000   | 16,250  | 2,500    |         |
| Twin Realm        | 25,000   | 13,500  | 2,000    |         |
| Two Pair          | 20,000   | 10,750  | 1,500    |         |
| One Pair          | 15,000   | 8,000   | 1,000    |         |
| High Card         | 10,000   | 5,250   | 500      | Lowest  |

---

## Flavor & Gameplay Notes

- Hands referencing Court Sets add lore-driven layers.
- Players can roleplay hands (“I call upon the Infernal Pact with a Crazy Straight!”).
- Wild cards add unpredictability and excitement.
- Use Court colors for UI highlights and chips matching players’ dominant sets.

---

# **Fortune smiles upon the bold. ✨✨✨**

