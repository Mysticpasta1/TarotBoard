package com.mystic.tarotboard.theming;

import java.io.Serial;
import java.io.Serializable;

/**
 * An immutable record associating a suit with its display color hex value.
 */
public record SuitStyle(String colorHex) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Returns the hex color string for this suit style.
     *
     * @return the color hex string (e.g. "#FFD700")
     */
    public String getColorHex() {
        return colorHex;
    }
}
