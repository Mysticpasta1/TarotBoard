package com.mystic.tarotboard.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provides platform-specific file system paths for application data.
 */
public class PlatformPaths {

    private static final String APP_DIR = ".tarotboard";

    private PlatformPaths() {
    }

    /**
     * Returns the application data directory path.
     *
     * @return absolute path to the .tarotboard directory
     */
    public static String getAppDataDir() {
        String home = System.getProperty("user.home");
        if (home != null && !home.isEmpty()) {
            return home + File.separator + APP_DIR;
        }
        String tmp = System.getProperty("java.io.tmpdir");
        if (tmp != null && !tmp.isEmpty()) {
            return tmp + File.separator + APP_DIR;
        }
        return System.getProperty("user.dir", ".") + File.separator + APP_DIR;
    }

    /**
     * Returns the path to the save data file.
     *
     * @return absolute path to save.dat within the app data directory
     */
    public static String getSaveFilePath() {
        return getAppDataDir() + File.separator + "save.dat";
    }

    /**
     * Returns the path to the themes' directory.
     *
     * @return Path to the themes subdirectory within app data
     */
    public static Path getThemesDir() {
        return Paths.get(getAppDataDir(), "themes");
    }
}
