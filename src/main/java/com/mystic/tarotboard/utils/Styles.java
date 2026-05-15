package com.mystic.tarotboard.utils;

import com.mystic.tarotboard.theming.ThemeConfiguration;
import com.mystic.tarotboard.theming.ThemeManager;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;

/**
 * Provides static CSS style strings derived from the active theme configuration.
 */
public record Styles() {
    private static ThemeConfiguration.GuiConfig g() {
        return ThemeManager.getActiveTheme().getGui();
    }

    /**
     * Style for menu buttons.
     *
     * @return CSS string with font size and preferred width
     */
    public static String menuButton() {
        int w = g().menuButtonWidth;
        int fs = g().menuButtonFontSize;
        int h = g().menuButtonHeight;
        return "-fx-font-size: " + fs + "pt; -fx-pref-width: " + w + "; -fx-min-width: " + w + "; -fx-max-width: " + w + "; -fx-min-height: " + h + "; -fx-max-height: " + h + "; -fx-background-color: " + g().menuBtnBg + "; -fx-text-fill: " + g().menuBtnFg + ";";
    }

    /**
     * Style for small menu buttons.
     *
     * @return CSS string with font size
     */
    public static String menuSmallBtn() {
        return "-fx-font-size: " + g().menuSmallBtnSize + "pt; -fx-background-color: " + g().menuSmallBtnBg + "; -fx-text-fill: " + g().menuSmallBtnFg + ";";
    }

    /**
     * Style for multiplayer scene title text.
     *
     * @return CSS string with bold font size
     */
    public static String mpTitle() {
        return "-fx-font-size: " + g().mpTitleFontSize + "pt; -fx-font-weight: bold; -fx-text-fill: white;";
    }

    /**
     * Style for multiplayer scene input fields.
     *
     * @return CSS string with font size
     */
    public static String mpField() {
        return "-fx-font-size: " + g().mpFieldFontSize + "pt;";
    }

    /**
     * Style for multiplayer scene buttons.
     *
     * @return CSS string with font size
     */
    public static String mpBtn() {
        return "-fx-font-size: " + g().mpBtnFontSize + "pt; -fx-background-color: " + g().mpBtnBg + "; -fx-text-fill: " + g().mpBtnFg + ";";
    }

    /**
     * Style for multiplayer scene labels.
     *
     * @return CSS string with font size and text color
     */
    public static String mpLabel() {
        return "-fx-font-size: " + g().mpLabelFontSize + "pt; -fx-font-weight: bold; -fx-background-color: " + g().mpLabelBg + "; -fx-text-fill: " + g().mpLabelColor + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 2, 0.5, 0, 1);";
    }

    /**
     * Style for small multiplayer labels.
     *
     * @return CSS string with font size and label color
     */
    public static String mpSmallLabel() {
        return "-fx-font-size: " + g().mpSmallFontSize + "pt; -fx-font-weight: bold; -fx-background-color: " + g().mpLabelBg + "; -fx-text-fill: " + g().mpLabelColor + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 2, 0.5, 0, 1);";
    }

    /**
     * Style for multiplayer status OK indicator.
     *
     * @return CSS string with font size and OK status color
     */
    public static String mpStatusOk() {
        return "-fx-font-size: " + g().mpLabelFontSize + "pt; -fx-text-fill: " + g().mpStatusOk + ";";
    }

    /**
     * Style for multiplayer status error indicator.
     *
     * @return CSS string with font size and error status color
     */
    public static String mpStatusErr() {
        return "-fx-font-size: " + g().mpLabelFontSize + "pt; -fx-text-fill: " + g().mpStatusErr + ";";
    }

    /**
     * Style for the in-game control panel background.
     *
     * @return CSS string with background color, padding, and border radius
     */
    public static String panelBg() {
        return "-fx-background-color: " + g().panelBg + "; -fx-padding: " + g().panelPadding + "; -fx-background-radius: 8;";
    }

    /**
     * Style for in-game panel buttons.
     *
     * @return CSS string with font size
     */
    public static String panelBtn() {
        return "-fx-font-size: " + g().panelBtnSize + "pt; -fx-background-color: " + g().panelBtnBg + "; -fx-text-fill: " + g().panelBtnFg + ";";
    }

    /**
     * Style for small panel text.
     *
     * @return CSS string with font size
     */
    public static String panelSmall() {
        return "-fx-font-size: " + g().panelSmallSize + "pt; -fx-background-color: " + g().panelSmallBg + "; -fx-text-fill: " + g().panelSmallFg + ";";
    }

    /**
     * Style for panel labels.
     *
     * @return CSS string with font size and label color
     */
    public static String panelLabel() {
        return "-fx-font-size: " + g().panelSmallSize + "pt; -fx-text-fill: " + g().panelLabelColor + ";";
    }

    /**
     * Style for the discard zone.
     *
     * @return CSS string with background, border, and radius
     */
    public static String discardZone() {
        return "-fx-background-color: " + g().discardBg + "; -fx-border-color: " + g().discardBorderColor
                + "; -fx-border-width: " + g().discardBorderWidth + "; -fx-background-radius: 10; -fx-border-radius: 10;";
    }

    /**
     * Style for the player list overlay background.
     *
     * @return CSS string with background color
     */
    public static String overlayBg() {
        return "-fx-background-color: " + g().overlayBg + ";";
    }

    /**
     * Style for the player list overlay content panel.
     *
     * @return CSS string with background, padding, radius, and border
     */
    public static String overlayContent() {
        return "-fx-background-color: " + g().overlayContentBg + "; -fx-padding: " + g().overlayPadding
                + "; -fx-background-radius: " + g().overlayRadius + "; -fx-border-radius: " + g().overlayRadius
                + "; -fx-border-color: rgba(255,255,255,0.15); -fx-border-width: 1;";
    }

    /**
     * Style for a player name in the overlay.
     *
     * @param isMe whether the player is the local user
     * @return CSS string with font size and optional bold weight
     */
    public static String playerName(boolean isMe) {
        return "-fx-font-size: " + g().plyNameSize + "pt; -fx-text-fill: white;"
                + (isMe ? " -fx-font-weight: bold;" : "");
    }

    /**
     * Style for a player tag in the overlay.
     *
     * @param isMe whether the player is the local user
     * @return CSS string with font size and color for you or guest
     */
    public static String playerTag(boolean isMe) {
        return "-fx-font-size: " + g().plyTagSize + "pt; -fx-text-fill: "
                + (isMe ? g().plyYouColor : g().plyGuestColor) + ";";
    }

    /**
     * Style for card text with a custom color.
     *
     * @param colorHex the hex color for the text fill
     * @return CSS string with font size and fill color
     */
    public static String cardText(String colorHex) {
        return "-fx-font-size: " + g().cardTextSize + "pt; -fx-fill: " + colorHex + ";";
    }

    /**
     * Style for wild card text.
     *
     * @return CSS string with font size and wild card color
     */
    public static String wildCardText() {
        return "-fx-font-size: " + g().cardTextSize + "pt; -fx-fill: " + ThemeManager.getActiveTheme().getSuitStyles().getLast().colorHex() + ";";
    }

    /**
     * Style for the help button.
     *
     * @return CSS string with font size
     */
    public static String helpBtn() {
        return "-fx-font-size: " + g().helpBtnSize + "pt; -fx-background-color: " + g().helpBtnBg + "; -fx-text-fill: " + g().helpBtnFg + ";";
    }

    /**
     * Style for the settings reset button.
     *
     * @return CSS string with font size, background, and text color
     */
    public static String settingsResetBtn() {
        return "-fx-font-size: 16pt; -fx-background-color: " + g().resetBtnBg + "; -fx-text-fill: " + g().settingsBtnFg + ";";
    }

    /**
     * Style for the settings close button.
     *
     * @return CSS string with font size
     */
    public static String settingsCloseBtn() {
        return "-fx-font-size: 16pt; -fx-background-color: " + g().settingsBtnBg + "; -fx-text-fill: " + g().settingsBtnFg + ";";
    }

    /**
     * Style for a settings key bind name label.
     *
     * @return CSS string with font size and text color
     */
    public static String settingsBindName() {
        return "-fx-font-size: 16pt; -fx-text-fill: " + g().settingsTextColor + ";";
    }

    /**
     * Style for a settings key bind button.
     *
     * @return CSS string with font size and minimum width
     */
    public static String settingsBindKey() {
        return "-fx-font-size: 16pt; -fx-min-width: 150;";
    }

    /**
     * Style for a settings key bind button while recording.
     *
     * @return CSS string with font size, minimum width, and orange background
     */
    public static String settingsBindKeyRecording() {
        return "-fx-font-size: 16pt; -fx-min-width: 150; -fx-background-color: #ffa500; -fx-text-fill: black;";
    }

    /**
     * Applies the active theme's background image to the given pane.
     *
     * @param pane the target pane
     */
    public static void applyBackgroundImage(Pane pane) {
        ThemeConfiguration theme = ThemeManager.getActiveTheme();
        String bgPath = theme.getBackgroundPath();
        if (bgPath == null || bgPath.trim().isEmpty()) return;
        String bp = theme.getBasePath();
        Image img = null;
        if (bp != null && bp.startsWith("./")) {
            String resPath = "/" + bp.substring(2) + bgPath;
            try (InputStream is = Styles.class.getResourceAsStream(resPath)) {
                if (is != null) img = new Image(is);
            } catch (Exception e) {
                System.err.println("Failed to load background from resource: " + resPath + ". Error: " + e.getMessage());
            }
        } else {
            String fp = bp != null ? Paths.get(bp, bgPath).toString() : bgPath;
            try {
                File f = new File(fp);
                if (f.exists()) img = new Image(f.toURI().toString());
            } catch (Exception e) {
                System.err.println("Failed to load background from file: " + fp + ". Error: " + e.getMessage());
            }
        }
        if (img != null) {
            pane.setBackground(new Background(new BackgroundImage(img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(1.0, 1.0, true, true, false, false))));
        }
    }
}