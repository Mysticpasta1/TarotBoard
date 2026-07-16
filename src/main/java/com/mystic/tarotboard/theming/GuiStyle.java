package com.mystic.tarotboard.theming;

import com.google.gson.Gson;
import com.mystic.tarotboard.utils.PlatformPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Persistent GUI style settings for card text display.
 * Loaded from {@code gui_style.json} in the app data directory, falling back to defaults.
 */
public record GuiStyle(int cardTextSize) {
    private static GuiStyle instance;

    /**
     * Returns the singleton instance, loaded from disk or defaulted.
     *
     * @return the current GUI style
     */
    public static GuiStyle getInstance() {
        if (instance == null) instance = load();
        return instance;
    }

    private static GuiStyle defaults() {
        return new GuiStyle(15);
    }

    private static GuiStyle load() {
        try {
            Path p = Path.of(PlatformPaths.getAppDataDir(), "gui_style.json");
            if (Files.exists(p)) {
                return new Gson().fromJson(Files.readString(p), GuiStyle.class);
            }
        } catch (IOException e) {
            System.err.println("Failed to load GUI style: " + e.getMessage());
        }
        return defaults();
    }
}
