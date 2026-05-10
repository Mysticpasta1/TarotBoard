package com.mystic.tarotboard.utils;

import java.io.*;
import java.util.*;

public record SaveData(
        List<SaveData.CardState> cards,
        List<SaveData.ChipState> chips,
        List<SaveData.DieState> dice,
        boolean reshuffled,
        List<String> cardNames,
        String customCardFrontPath,
        String customCardBackPath,
        String customChipFrontPath,
        String customChipBackPath,
        String customBackgroundPath,
        String themeName // Added themeName to SaveData
) implements Serializable {

    public record CardState(
            double translateX, double translateY,
            double paneRotate,
            double backRotate, double frontRotate, double textRotate,
            boolean backVisible, boolean frontVisible, boolean textVisible
    ) implements Serializable {
    }

    public record ChipState(
            double translateX, double translateY,
            double frontRotate, double backRotate,
            boolean frontVisible, boolean backVisible,
            double red, double green, double blue, double opacity
    ) implements Serializable {
    }

    public record DieState(
            double translateX, double translateY,
            double paneRotate,
            int sides,
            int currentValue,
            double red, double green, double blue, double opacity
    ) implements Serializable {
    }
}
