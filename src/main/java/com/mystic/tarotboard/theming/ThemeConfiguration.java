package com.mystic.tarotboard.theming;

import java.io.Serializable;
import java.util.List;

public class ThemeConfiguration implements Serializable {
    private String themeName;
    private String cardFrontPath;
    private String cardBackPath;
    private String chipFrontPath;
    private String chipBackPath;
    private String backgroundPath;
    private List<SuitStyle> suitStyles;

    // Default constructor for serialization
    public ThemeConfiguration() {}

    public ThemeConfiguration(String themeName, String cardFrontPath, String cardBackPath, String chipFrontPath, String chipBackPath, String backgroundPath, List<SuitStyle> suitStyles) {
        this.themeName = themeName;
        this.cardFrontPath = cardFrontPath;
        this.cardBackPath = cardBackPath;
        this.chipFrontPath = chipFrontPath;
        this.chipBackPath = chipBackPath;
        this.backgroundPath = backgroundPath;
        this.suitStyles = suitStyles;
    }

    public String getThemeName() {
        return themeName;
    }

    public String getCardFrontPath() {
        return cardFrontPath;
    }

    public String getCardBackPath() {
        return cardBackPath;
    }

    public String getChipFrontPath() {
        return chipFrontPath;
    }

    public String getChipBackPath() {
        return chipBackPath;
    }

    public String getBackgroundPath() {
        return backgroundPath;
    }

    public List<SuitStyle> getSuitStyles() {
        return suitStyles;
    }

    // Setters for serialization
    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }

    public void setCardFrontPath(String cardFrontPath) {
        this.cardFrontPath = cardFrontPath;
    }

    public void setCardBackPath(String cardBackPath) {
        this.cardBackPath = cardBackPath;
    }

    public void setChipFrontPath(String chipFrontPath) {
        this.chipFrontPath = chipFrontPath;
    }

    public void setChipBackPath(String chipBackPath) {
        this.chipBackPath = chipBackPath;
    }

    public void setBackgroundPath(String backgroundPath) {
        this.backgroundPath = backgroundPath;
    }

    public void setSuitStyles(List<SuitStyle> suitStyles) {
        this.suitStyles = suitStyles;
    }
}
