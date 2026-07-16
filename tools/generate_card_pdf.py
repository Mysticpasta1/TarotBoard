#!/usr/bin/env python3
"""Generate a printable PDF of all TarotBoard cards in mini playing-card size."""

import os
import io
from PIL import Image
from reportlab.lib.units import inch
from reportlab.lib.pagesizes import letter
from reportlab.lib.colors import HexColor, white, black, Color
from reportlab.pdfgen import canvas
from reportlab.lib.utils import ImageReader

# --- Asset paths ---
ASSETS_DIR = os.path.join(os.path.dirname(__file__), "..",
    "src", "main", "resources", "com", "mystic", "tarotboard", "assets")
SYMBOLS_DIR = os.path.join(ASSETS_DIR, "symbols")
VALUES_DIR = os.path.join(ASSETS_DIR, "values")
WILDS_DIR = os.path.join(ASSETS_DIR, "wilds")

# --- Card data (mirrors TarotBoard.java / ThemeManager.java) ---

SUIT_GROUPS = {
    "CELESTIAL_COURT": ("#FFD700", ["Stars", "Suns", "Crowns", "Quasars", "Crescents", "Sigils", "Comets", "Glyphs"]),
    "UMBRAL_DOMINION": ("#FF8800", ["Veils", "Runes", "Hearts", "Spirals", "Eyes", "Omens", "Diamonds", "Orbs"]),
    "INFERNAL_PACT":  ("#DC143C", ["Arrows", "Flames", "Locks", "Arcs", "Swords", "Points", "Embers", "Gears"]),
    "VERDANT_CYCLE":  ("#228B22", ["Flowers", "Leaves", "Mountains", "Shells", "Clovers", "Tridents", "Trees", "Waves"]),
    "AETHERIC_LOOM":  ("#1E90FF", ["Clouds", "Crosses", "Shields", "Keys", "Spades", "Scrolls", "Looms", "Shards"]),
    "DARK_EXPANSE":   ("#AD03FC", ["Echoes", "Rifts", "Ashes", "Nulls", "Hallows", "Fluxes", "Ethers", "Grims"]),
}

VALUES = [
    "Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10",
    "Jack", "Queen", "King",
    "Fugitive", "Devil", "Shadow", "Specter", "Phantom", "Void", "Wraith", "Ghoul",
    "Banshee", "Reverent", "Eidolon", "Shade", "Doppelganger", "Hollow", "Abyss",
    "Chimera", "Poltergeist", "Wight", "Apparition", "Nightmare", "Succubus",
    "Incubus", "Unknown", "Necromancer", "Fury", "Grim", "Harbinger", "Spectacle",
    "Lich", "Gorgon", "Drake", "Demon", "Frost", "Golem", "Hydra", "Inferno",
    "Juggernaut", "Kraken", "Reaper", "Leviathan", "Manticore", "Naga", "Blight",
    "Serpent", "Hold", "Nomad", "Prince", "Rune", "Fable", "Sorceress", "Utopia",
    "Wizard", "Titan", "Baron", "Illusionist", "Oracle", "Magician", "Luminary",
    "Eclipse", "Celestial", "Duke", "Genesis", "Zephyr", "Vesper", "Umbra",
    "Valkyrie", "Warden", "Zenith", "Yggdrasil", "Zodiac", "Phoenix", "Raven",
    "Cipher", "Angel", "Knight", "Venom",
]

WILDS = [
    "Joker", "Soul", "Light", "Dark", "Judgement", "Chorus", "Life", "Death",
    "Wrath", "Pride", "Greed", "Lust", "Envy", "Gluttony", "Sloth", "Chasity",
    "Temperance", "Charity", "Diligence", "Kindness", "Patience", "Humility",
    "Voice", "Voices", "Mother", "Father", "Brother", "Sister", "Duality",
    "Accord", "Husband", "Wife", "Progeny", "Corridor", "Field", "Intellect",
    "Brawn", "Despair", "Past", "Present", "Future", "Gate", "Sign", "Ruin",
    "Snow", "Rain", "Tempest", "Lovers", "Discord", "Concord", "Harmony",
    "Dissonance", "Earth", "Fire", "Water", "Air", "Spirit", "Oblivion",
    "Obscurity", "Purgatory", "Nether", "Underworld", "Aether", "Overworld",
    "Limbo", "Chaos", "Balance", "Doom", "Peace", "Evil", "Good", "Neutral",
    "Hope", "Monster", "Human", "Dusk", "Dawn", "Paradox", "Entropy",
]

WILD_COLOR = "#333333"

# --- Image caches ---
_raw_cache = {}    # name -> PIL RGBA image
_tinted_cache = {} # (name, hex_color) -> reportlab ImageReader

def _load_raw(name, folder):
    """Load raw PNG as PIL RGBA, cached."""
    key = f"{folder}/{name.lower()}"
    if key in _raw_cache:
        return _raw_cache[key]
    path = os.path.join(ASSETS_DIR, folder, f"{name.lower()}.png")
    try:
        img = Image.open(path).convert("RGBA")
        _raw_cache[key] = img
        return img
    except Exception:
        _raw_cache[key] = None
        return None

import numpy as np

def tint_symbol(name, folder, hex_color):
    """Load a symbol, tint its visible pixels to hex_color, return ImageReader."""
    cache_key = (f"{folder}/{name.lower()}", hex_color)
    if cache_key in _tinted_cache:
        return _tinted_cache[cache_key]

    raw = _load_raw(name, folder)
    if raw is None:
        _tinted_cache[cache_key] = None
        return None

    tc = HexColor(hex_color)
    r, g, b = tc.red, tc.green, tc.blue

    arr = np.array(raw, dtype=np.float32)
    alpha = arr[:, :, 3:4] / 255.0
    lum = (arr[:, :, 0:1] + arr[:, :, 1:2] + arr[:, :, 2:3]) / (3.0 * 255.0)
    rgb = np.array([r, g, b], dtype=np.float32).reshape(1, 1, 3)
    tinted_rgb = lum * rgb * 255.0
    result = np.concatenate([tinted_rgb, arr[:, :, 3:4]], axis=2)
    result = np.clip(result, 0, 255).astype(np.uint8)

    out = Image.fromarray(result, "RGBA")
    buf = io.BytesIO()
    out.save(buf, format="PNG")
    buf.seek(0)
    reader = ImageReader(buf)
    _tinted_cache[cache_key] = reader
    return reader

# --- Card sizing ---
CARD_W = 1.75 * inch
CARD_H = 2.5 * inch
CORNER_R = 6
MARGIN_X = 0.5 * inch
MARGIN_Y = 0.5 * inch
CORNER_SYM_SIZE = 14  # points

PAGE_W, PAGE_H = letter
COLS = int((PAGE_W - 2 * MARGIN_X) / CARD_W)
ROWS = int((PAGE_H - 2 * MARGIN_Y) / CARD_H)
CARDS_PER_PAGE = COLS * ROWS

def darken(hex_color, factor=0.3):
    c = HexColor(hex_color)
    return Color(c.red * factor, c.green * factor, c.blue * factor)

def draw_rounded_rect(c, x, y, w, h, r, fill_color, stroke_color):
    c.saveState()
    c.setFillColor(fill_color)
    c.setStrokeColor(stroke_color)
    c.setLineWidth(1.5)
    p = c.beginPath()
    p.roundRect(x, y, w, h, r)
    c.drawPath(p, fill=1, stroke=1)
    c.restoreState()

def draw_card(c, x, y, value, suit, color_hex, is_wild=False):
    """Draw a single mini playing card at (x, y) which is bottom-left corner."""
    bg = HexColor(color_hex)
    border = darken(color_hex, 0.4)

    # Card background
    draw_rounded_rect(c, x, y, CARD_W, CARD_H, CORNER_R, bg, border)

    # Inner white rectangle for text area
    inset = 4
    inner_x = x + inset
    inner_y = y + inset
    inner_w = CARD_W - 2 * inset
    inner_h = CARD_H - 2 * inset
    c.saveState()
    c.setFillColor(Color(1, 1, 1, alpha=0.85))
    p = c.beginPath()
    p.roundRect(inner_x, inner_y, inner_w, inner_h, CORNER_R - 2)
    c.drawPath(p, fill=1, stroke=0)
    c.restoreState()

    # --- Corner symbols ---
    # Top-left corner: value symbol on top, suit/wild symbol underneath
    # Bottom-right corner: same but rotated 180
    sz = CORNER_SYM_SIZE
    half = sz / 2.0
    gap = 1  # vertical gap between the two symbols

    tl_cx = x + CARD_W * 0.2
    tl_val_cy = y + CARD_H - 12
    tl_suit_cy = tl_val_cy - sz - gap

    br_cx = x + CARD_W * 0.8
    br_val_cy = y + 12
    br_suit_cy = br_val_cy + sz + gap

    if is_wild:
        img = tint_symbol(value, "wilds", color_hex)
        if img:
            # Top-left
            c.drawImage(img, tl_cx - half, tl_val_cy - half, width=sz, height=sz, mask='auto')
            # Bottom-right (rotated 180)
            c.saveState()
            c.translate(br_cx, br_val_cy)
            c.rotate(180)
            c.drawImage(img, -half, -half, width=sz, height=sz, mask='auto')
            c.restoreState()
        else:
            c.saveState()
            c.setFillColor(border)
            c.setFont("Helvetica-Bold", 7)
            label = value if len(value) <= 5 else value[:4] + "."
            c.drawCentredString(tl_cx, tl_val_cy - 3, label)
            c.translate(br_cx, br_val_cy)
            c.rotate(180)
            c.drawCentredString(0, -3, label)
            c.restoreState()
    else:
        val_img = tint_symbol(value, "values", color_hex)
        suit_img = tint_symbol(suit, "symbols", color_hex)

        # Top-left corner
        if val_img:
            c.drawImage(val_img, tl_cx - half, tl_val_cy - half, width=sz, height=sz, mask='auto')
        else:
            c.saveState()
            c.setFillColor(border)
            c.setFont("Helvetica-Bold", 7)
            label = value if len(value) <= 5 else value[:4] + "."
            c.drawCentredString(tl_cx, tl_val_cy - 3, label)
            c.restoreState()

        if suit_img:
            c.drawImage(suit_img, tl_cx - half, tl_suit_cy - half, width=sz, height=sz, mask='auto')
        else:
            c.saveState()
            c.setFillColor(border)
            c.setFont("Helvetica", 5)
            c.drawCentredString(tl_cx, tl_suit_cy - 3, suit[:6])
            c.restoreState()

        # Bottom-right corner (rotated 180)
        if val_img:
            c.saveState()
            c.translate(br_cx, br_val_cy)
            c.rotate(180)
            c.drawImage(val_img, -half, -half, width=sz, height=sz, mask='auto')
            c.restoreState()

        if suit_img:
            c.saveState()
            c.translate(br_cx, br_suit_cy)
            c.rotate(180)
            c.drawImage(suit_img, -half, -half, width=sz, height=sz, mask='auto')
            c.restoreState()

    # --- Center text ---
    cx = x + CARD_W / 2
    cy = y + CARD_H / 2

    if is_wild:
        font_size = 9
        if len(value) > 12:
            font_size = 7
        elif len(value) > 9:
            font_size = 8
        c.saveState()
        c.setFillColor(border)
        c.setFont("Helvetica-Bold", font_size)
        c.drawCentredString(cx, cy + 4, value.upper())
        c.setFont("Helvetica", 5.5)
        c.setFillColor(HexColor("#666666"))
        c.drawCentredString(cx, cy - 6, value.upper())
        c.restoreState()
    else:
        val_size = 11
        if len(value) > 8:
            val_size = 8
        elif len(value) > 5:
            val_size = 9

        c.saveState()
        c.setFillColor(border)
        c.setFont("Helvetica-Bold", val_size)
        c.drawCentredString(cx, cy + 6, value.upper())

        suit_size = 7
        if len(suit) > 8:
            suit_size = 5.5
        elif len(suit) > 6:
            suit_size = 6
        c.setFont("Helvetica", suit_size)
        c.setFillColor(HexColor("#555555"))
        c.drawCentredString(cx, cy - 6, suit.upper())
        c.restoreState()

def generate_pdf(output_path):
    pdf = canvas.Canvas(output_path, pagesize=letter)
    pdf.setTitle("TarotBoard Mini Cards")

    cards = []

    # Wild cards first
    for w in WILDS:
        cards.append((w, "Wild", WILD_COLOR, True))

    # Suited cards
    for group_name, (color_hex, suits) in sorted(SUIT_GROUPS.items()):
        for suit in suits:
            for value in VALUES:
                cards.append((value, suit, color_hex, False))

    total = len(cards)
    total_pages = (total + CARDS_PER_PAGE - 1) // CARDS_PER_PAGE
    print(f"Generating {total} cards across {total_pages} pages...")

    for i, (value, suit, color_hex, is_wild) in enumerate(cards):
        pos_on_page = i % CARDS_PER_PAGE

        if pos_on_page == 0 and i > 0:
            pdf.showPage()

        col = pos_on_page % COLS
        row = pos_on_page // COLS

        x = MARGIN_X + col * CARD_W
        y = PAGE_H - MARGIN_Y - (row + 1) * CARD_H

        draw_card(pdf, x, y, value, suit, color_hex, is_wild)

        if (i + 1) % 500 == 0:
            print(f"  ...{i + 1}/{total} cards")

    pdf.save()
    print(f"Done! Saved to {output_path}")
    print(f"Total cards: {total}")
    print(f"Total pages: {total_pages}")

if __name__ == "__main__":
    out = os.path.join(os.path.dirname(__file__), "..", "TarotBoard_Mini_Cards.pdf")
    generate_pdf(out)
