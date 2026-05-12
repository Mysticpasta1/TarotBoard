package com.mystic.tarotboard.theming;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializable configuration for a theme, including card and chip image paths,
 * suit styles, GUI display properties, and remote cursor settings.
 */
public class ThemeConfiguration implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String themeName;
    private String cardFrontPath;
    private String cardBackPath;
    private String chipFrontPath;
    private String chipBackPath;
    private String backgroundPath;
    private ArrayList<SuitStyle> suitStyles;
    private String suitStyleKey;
    private String basePath;
    private String guiKey;
    private String cursorKey;
    private GuiConfig gui;
    private CursorConfig cursor;

    /**
     * Creates an empty ThemeConfiguration.
     */
    public ThemeConfiguration() {
    }

    /**
     * Creates a ThemeConfiguration with all properties.
     *
     * @param themeName      the display name of the theme
     * @param cardFrontPath  path to the card front image
     * @param cardBackPath   path to the card back image
     * @param chipFrontPath  path to the chip front image
     * @param chipBackPath   path to the chip back image
     * @param backgroundPath path to the background image
     * @param suitStyles     list of suit style definitions
     */
    public ThemeConfiguration(String themeName, String cardFrontPath, String cardBackPath, String chipFrontPath, String chipBackPath, String backgroundPath, List<SuitStyle> suitStyles) {
        this.themeName = themeName;
        this.cardFrontPath = cardFrontPath;
        this.cardBackPath = cardBackPath;
        this.chipFrontPath = chipFrontPath;
        this.chipBackPath = chipBackPath;
        this.backgroundPath = backgroundPath;
        this.suitStyles = new ArrayList<>(suitStyles);
        this.basePath = null;
    }

    /**
     * Creates a ThemeConfiguration with all properties including a base path for relative image resolution.
     *
     * @param themeName      the display name of the theme
     * @param cardFrontPath  path to the card front image
     * @param cardBackPath   path to the card back image
     * @param chipFrontPath  path to the chip front image
     * @param chipBackPath   path to the chip back image
     * @param backgroundPath path to the background image
     * @param suitStyles     list of suit style definitions
     * @param basePath       base directory for resolving relative image paths
     */
    public ThemeConfiguration(String themeName, String cardFrontPath, String cardBackPath, String chipFrontPath, String chipBackPath, String backgroundPath, List<SuitStyle> suitStyles, String basePath) {
        this(themeName, cardFrontPath, cardBackPath, chipFrontPath, chipBackPath, backgroundPath, suitStyles);
        this.basePath = basePath;
    }

    /**
     * @return the theme display name
     */
    public String getThemeName() {
        return themeName;
    }

    /**
     * @return path to the card front image
     */
    public String getCardFrontPath() {
        return cardFrontPath;
    }

    /**
     * @return path to the card back image
     */
    public String getCardBackPath() {
        return cardBackPath;
    }

    /**
     * @return path to the chip front image
     */
    public String getChipFrontPath() {
        return chipFrontPath;
    }

    /**
     * @return path to the chip back image
     */
    public String getChipBackPath() {
        return chipBackPath;
    }

    /**
     * @return path to the background image
     */
    public String getBackgroundPath() {
        return backgroundPath;
    }

    /**
     * Returns the suit styles, resolving from a registered suit key if not already loaded.
     *
     * @return list of suit style definitions
     */
    public ArrayList<SuitStyle> getSuitStyles() {
        if (suitStyles == null) {
            if (suitStyleKey != null && !suitStyleKey.isEmpty()) {
                List<SuitStyle> resolved = ThemeManager.getSuitStyles(suitStyleKey);
                suitStyles = new ArrayList<>(resolved);
            }
            if (suitStyles == null) {
                suitStyles = new ArrayList<>();
            }
        }
        return suitStyles;
    }

    /**
     * @return base directory for resolving relative paths, or null
     */
    public String getBasePath() {
        return basePath;
    }

    /**
     * Returns the GUI configuration, resolving from a registered GUI key if not already loaded.
     *
     * @return the GUI configuration
     */
    public GuiConfig getGui() {
        if (gui == null) {
            if (guiKey != null && !guiKey.isEmpty()) {
                gui = ThemeManager.getGuiConfig(guiKey);
            }
            if (gui == null) {
                gui = new GuiConfig();
            }
        }
        return gui;
    }

    /**
     * Returns the cursor configuration, resolving from a registered cursor key if not already loaded.
     *
     * @return the cursor configuration
     */
    public CursorConfig getCursor() {
        if (cursor == null) {
            if (cursorKey != null && !cursorKey.isEmpty()) {
                cursor = ThemeManager.getCursorConfig(cursorKey);
            }
            if (cursor == null) {
                cursor = new CursorConfig();
            }
        }
        return cursor;
    }

    /**
     * Sets the base directory for resolving relative image paths.
     *
     * @param v the base path
     */
    public void setBasePath(String v) {
        basePath = v;
    }

    /**
     * @return the suit style registry key
     */
    public String getSuitStyleKey() {
        return suitStyleKey;
    }

    /**
     * @return the GUI config registry key
     */
    public String getGuiKey() {
        return guiKey;
    }

    /**
     * @return the cursor config registry key
     */
    public String getCursorKey() {
        return cursorKey;
    }

    /**
     * Sets the suit style registry key for lazy resolution.
     *
     * @param v the suit style key
     */
    public void setSuitStyleKey(String v) {
        suitStyleKey = v;
    }

    /**
     * Sets the GUI config registry key for lazy resolution.
     *
     * @param v the GUI key
     */
    public void setGuiKey(String v) {
        guiKey = v;
    }

    /**
     * Sets the cursor config registry key for lazy resolution.
     *
     * @param v the cursor key
     */
    public void setCursorKey(String v) {
        cursorKey = v;
    }

    /**
     * Holds GUI style values embedded in the theme configuration, such as font sizes,
     * colors, padding, and overlay settings.
     */
    public static class GuiConfig implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        /**
         * Font size for menu buttons
         */
        public int menuButtonFontSize = 20;
        /**
         * Width of menu buttons
         */
        public int menuButtonWidth = 300;
        /**
         * Font size for small menu elements
         */
        public int menuSmallBtnSize = 12;

        /**
         * Background color for menu buttons
         */
        public String menuBtnBg = "#2d2d44";
        /**
         * Text color for menu buttons
         */
        public String menuBtnFg = "#e8e8e8";
        /**
         * Background color for small menu buttons
         */
        public String menuSmallBtnBg = "#2d2d44";
        /**
         * Text color for small menu buttons
         */
        public String menuSmallBtnFg = "#e8e8e8";

        /**
         * Font size for the title in multiplayer screens
         */
        public int mpTitleFontSize = 18;
        /**
         * Font size for input fields in multiplayer screens
         */
        public int mpFieldFontSize = 14;
        /**
         * Font size for buttons in multiplayer screens
         */
        public int mpBtnFontSize = 14;
        /**
         * Background color for multiplayer buttons
         */
        public String mpBtnBg = "#2d2d44";
        /**
         * Text color for multiplayer buttons
         */
        public String mpBtnFg = "#e8e8e8";
        /**
         * Font size for labels in multiplayer screens
         */
        public int mpLabelFontSize = 14;
        /**
         * Font size for small text in multiplayer screens
         */
        public int mpSmallFontSize = 12;
        /**
         * Background color of multiplayer labels
         */
        public String mpLabelBg = "rgba(0,0,0,0.25)";
        /**
         * Color of multiplayer labels
         */
        public String mpLabelColor = "#888";
        /**
         * Color indicating OK status
         */
        public String mpStatusOk = "#4c4";
        /**
         * Color indicating error status
         */
        public String mpStatusErr = "#c44";
        /**
         * Background color of panels
         */
        public String panelBg = "rgba(0,0,0,0.4)";
        /**
         * Padding within panels
         */
        public int panelPadding = 10;
        /**
         * Font size for panel buttons
         */
        public int panelBtnSize = 11;
        /**
         * Font size for small panel elements
         */
        public int panelSmallSize = 10;
        /**
         * Background color for panel buttons
         */
        public String panelBtnBg = "#2d2d44";
        /**
         * Text color for panel buttons
         */
        public String panelBtnFg = "#e8e8e8";
        /**
         * Background color for small panel buttons
         */
        public String panelSmallBg = "#2d2d44";
        /**
         * Text color for small panel buttons
         */
        public String panelSmallFg = "#e8e8e8";
        /**
         * Color of panel labels
         */
        public String panelLabelColor = "#aaa";

        /**
         * Background color of the discard zone
         */
        public String discardBg = "rgba(200,0,0,0.25)";
        /**
         * Border color of the discard zone
         */
        public String discardBorderColor = "#cc0000";
        /**
         * Border width of the discard zone
         */
        public int discardBorderWidth = 2;
        /**
         * Overlay background color
         */
        public String overlayBg = "rgba(0,0,0,0.55)";
        /**
         * Overlay content area background color
         */
        public String overlayContentBg = "rgba(0,0,0,0.8)";
        /**
         * Padding within overlays
         */
        public int overlayPadding = 24;
        /**
         * Border radius of overlays
         */
        public int overlayRadius = 14;
        /**
         * Font size for player names
         */
        public int plyNameSize = 15;
        /**
         * Font size for player tags
         */
        public int plyTagSize = 11;
        /**
         * Color indicating the local player
         */
        public String plyYouColor = "#6c6";
        /**
         * Color indicating guest players
         */
        public String plyGuestColor = "#aaa";

        /**
         * Font size for card text
         */
        public int cardTextSize = 15;

        /**
         * Color for settings window text (titles, labels, buttons)
         */
        public String settingsTextColor = "white";
        /**
         * Font size for help button
         */
        public int helpBtnSize = 16;
        /**
         * Background color for help buttons
         */
        public String helpBtnBg = "#2d2d44";
        /**
         * Text color for help buttons
         */
        public String helpBtnFg = "#e8e8e8";
        /**
         * Background color for settings buttons
         */
        public String settingsBtnBg = "#2d2d44";
        /**
         * Text color for settings buttons
         */
        public String settingsBtnFg = "#e8e8e8";
        /**
         * Background color of the reset button
         */
        public String resetBtnBg = "#c44";
    }

    /**
     * Holds remote cursor display values embedded in the theme configuration,
     * such as cursor size, dot appearance, label styling, and optional cursor image.
     */
    public static class CursorConfig implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        /**
         * Size of the remote cursor in pixels
         */
        public double cursorSize = 64;
        /**
         * Radius of the cursor dot
         */
        public double dotRadius = 8;
        /**
         * Width of the dot stroke outline
         */
        public double dotStrokeWidth = 1.5;
        /**
         * Color of the dot stroke outline
         */
        public String dotStrokeColor = "white";
        /**
         * Font size of the cursor label
         */
        public int labelFontSize = 10;
        /**
         * Color of the cursor label
         */
        public String labelColor = "white";
        /**
         * Optional path to a custom cursor image
         */
        public String cursorImagePath = "";
    }
}
