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
     * Returns whether the application is running on Android.
     *
     * <p>Substrate's Android launcher starts the VM with
     * {@code -Djavafx.platform=android}, which is readable before the JavaFX toolkit
     * starts and so is usable from the very top of {@code start()}.</p>
     *
     * @return true when running on Android
     */
    public static boolean isAndroid() {
        return "android".equalsIgnoreCase(System.getProperty("javafx.platform"));
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
     * Returns the path to the dedicated server's persisted game file. This is kept separate from
     * {@link #getSaveFilePath()} so a machine that both hosts a dedicated server and plays locally
     * does not have the two clobber each other.
     *
     * @return absolute path to server-save.dat within the app data directory
     */
    public static String getServerSaveFilePath() {
        return getAppDataDir() + File.separator + "server-save.dat";
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
