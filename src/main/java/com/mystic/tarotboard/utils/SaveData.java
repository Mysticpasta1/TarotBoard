package com.mystic.tarotboard.utils;

import java.io.*;
import java.util.*;

/**
 * Serializable record holding the complete game state for save/load operations.
 *
 * @param cards                list of card states
 * @param chips                list of chip states
 * @param dice                 list of die states
 * @param reshuffled           whether the deck has been reshuffled
 * @param cardNames            ordered list of card names
 * @param customCardFrontPath  path to custom card front image
 * @param customCardBackPath   path to custom card back image
 * @param customChipFrontPath  path to custom chip front image
 * @param customChipBackPath   path to custom chip back image
 * @param customBackgroundPath path to custom background image
 * @param themeName            active theme name
 * @param isMultiplayer        whether the session is multiplayer
 * @param serverIp             multiplayer server IP address
 * @param serverPort           multiplayer server port
 */
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
        String themeName,
        boolean isMultiplayer,
        String serverIp,
        int serverPort
) implements Serializable {

    /**
     * Serializable state for a single card.
     *
     * @param translateX   X translation offset
     * @param translateY   Y translation offset
     * @param paneRotate   rotation of the pane
     * @param backRotate   rotation of the back image
     * @param frontRotate  rotation of the front image
     * @param textRotate   rotation of the text label
     * @param backVisible  whether the back image is visible
     * @param frontVisible whether the front image is visible
     * @param textVisible  whether the text label is visible
     */
    public record CardState(
            double translateX, double translateY,
            double paneRotate,
            double backRotate, double frontRotate, double textRotate,
            boolean backVisible, boolean frontVisible, boolean textVisible
    ) implements Serializable {
    }

    /**
     * Serializable state for a single chip.
     *
     * @param translateX   X translation offset
     * @param translateY   Y translation offset
     * @param frontRotate  rotation of the front image
     * @param backRotate   rotation of the back image
     * @param frontVisible whether the front is visible
     * @param backVisible  whether the back is visible
     * @param red          red color component
     * @param green        green color component
     * @param blue         blue color component
     * @param opacity      opacity value
     */
    public record ChipState(
            double translateX, double translateY,
            double frontRotate, double backRotate,
            boolean frontVisible, boolean backVisible,
            double red, double green, double blue, double opacity
    ) implements Serializable {
    }

    /**
     * Serializable state for a single die.
     *
     * @param translateX   X translation offset
     * @param translateY   Y translation offset
     * @param paneRotate   rotation of the pane
     * @param sides        number of sides on the die
     * @param currentValue current face-up value
     * @param red          red color component
     * @param green        green color component
     * @param blue         blue color component
     * @param opacity      opacity value
     */
    public record DieState(
            double translateX, double translateY,
            double paneRotate,
            int sides,
            int currentValue,
            double red, double green, double blue, double opacity
    ) implements Serializable {
    }
}
