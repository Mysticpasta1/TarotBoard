package com.mystic.tarotboard.theming;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mystic.tarotboard.utils.PlatformPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Manages loading, registration, and access of themes, CSS, GUI configs, cursor configs,
 * and suit styles from both application resources and external user-defined files.
 */
public class ThemeManager {
    private ThemeManager() {
    }

    private static final List<ThemeConfiguration> themes = new ArrayList<>();
    private static ThemeConfiguration activeTheme;
    private static final String RESOURCE_THEMES_PATH = "/com/mystic/tarotboard/assets/configs/themes.json";
    private static final String RESOURCE_CSS_PATH = "/com/mystic/tarotboard/assets/configs/css.json";
    private static final String RESOURCE_GUI_PATH = "/com/mystic/tarotboard/assets/configs/gui.json";
    private static final String RESOURCE_CURSOR_PATH = "/com/mystic/tarotboard/assets/configs/cursor.json";
    private static final String RESOURCE_SUITS_PATH = "/com/mystic/tarotboard/assets/configs/suits.json";
    private static final String DEFAULT_THEME_NAME = "Default";
    private static final Map<String, List<String>> cssRegistry = new HashMap<>();
    private static final Map<String, ThemeConfiguration.GuiConfig> guiRegistry = new HashMap<>();
    private static final Map<String, ThemeConfiguration.CursorConfig> cursorRegistry = new HashMap<>();
    private static final Map<String, List<SuitStyle>> suitRegistry = new HashMap<>();

    private static final List<String> CELESTIAL_COURT = List.of("Stars", "Suns", "Crowns", "Quasars", "Crescents", "Sigils", "Comets", "Glyphs");
    private static final List<String> UMBRAL_DOMINION = List.of("Veils", "Runes", "Hearts", "Spirals", "Eyes", "Omens", "Diamonds", "Orbs");
    private static final List<String> INFERNAL_PACT = List.of("Arrows", "Flames", "Locks", "Arcs", "Swords", "Points", "Embers", "Gears");
    private static final List<String> VERDANT_CYCLE = List.of("Flowers", "Leaves", "Mountains", "Shells", "Clovers", "Tridents", "Trees", "Waves");
    private static final List<String> AETHERIC_LOOM = List.of("Clouds", "Crosses", "Shields", "Keys", "Spades", "Scrolls", "Looms", "Shards");
    private static final List<String> DARK_EXPANSE = List.of("Echoes", "Rifts", "Ashes", "Nulls", "Hallows", "Fluxes", "Ethers", "Grims");

    static {
        loadCssRegistry();
        loadGuiRegistry();
        loadCursorRegistry();
        loadSuitRegistry();
        loadResourceThemes();
        loadExternalThemes();
    }

    private static void loadCssRegistry() {
        Gson gson = new Gson();
        try (InputStream is = ThemeManager.class.getResourceAsStream(RESOURCE_CSS_PATH)) {
            if (is != null) {
                Type mapType = new TypeToken<Map<String, List<String>>>() {
                }.getType();
                Map<String, List<String>> loaded = gson.fromJson(new InputStreamReader(is), mapType);
                if (loaded != null) {
                    cssRegistry.putAll(loaded);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading CSS registry: " + e.getMessage());
        }
    }

    /**
     * Returns the CSS string for the given key from the registry.
     *
     * @param key the CSS registry key
     * @return the joined CSS string, or empty if not found
     */
    public static String getCss(String key) {
        List<String> lines = cssRegistry.get(key);
        return lines != null ? String.join("\n", lines) : "";
    }

    private static void loadGuiRegistry() {
        Gson gson = new Gson();
        try (InputStream is = ThemeManager.class.getResourceAsStream(RESOURCE_GUI_PATH)) {
            if (is != null) {
                Type mapType = new TypeToken<Map<String, ThemeConfiguration.GuiConfig>>() {
                }.getType();
                Map<String, ThemeConfiguration.GuiConfig> loaded = gson.fromJson(new InputStreamReader(is), mapType);
                if (loaded != null) {
                    guiRegistry.putAll(loaded);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading GUI registry: " + e.getMessage());
        }
    }

    /**
     * Returns the GUI configuration for the given key from the registry.
     *
     * @param key the GUI config registry key
     * @return the GuiConfig, or null if not found
     */
    public static ThemeConfiguration.GuiConfig getGuiConfig(String key) {
        return guiRegistry.get(key);
    }

    private static void loadCursorRegistry() {
        Gson gson = new Gson();
        try (InputStream is = ThemeManager.class.getResourceAsStream(RESOURCE_CURSOR_PATH)) {
            if (is != null) {
                Type mapType = new TypeToken<Map<String, ThemeConfiguration.CursorConfig>>() {
                }.getType();
                Map<String, ThemeConfiguration.CursorConfig> loaded = gson.fromJson(new InputStreamReader(is), mapType);
                if (loaded != null) {
                    cursorRegistry.putAll(loaded);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading cursor registry: " + e.getMessage());
        }
    }

    /**
     * Returns the cursor configuration for the given key from the registry.
     *
     * @param key the cursor config registry key
     * @return the CursorConfig, or null if not found
     */
    public static ThemeConfiguration.CursorConfig getCursorConfig(String key) {
        return cursorRegistry.get(key);
    }

    private static void loadSuitRegistry() {
        Gson gson = new Gson();
        try (InputStream is = ThemeManager.class.getResourceAsStream(RESOURCE_SUITS_PATH)) {
            if (is != null) {
                Type mapType = new TypeToken<Map<String, List<SuitStyle>>>() {
                }.getType();
                Map<String, List<SuitStyle>> loaded = gson.fromJson(new InputStreamReader(is), mapType);
                if (loaded != null) {
                    suitRegistry.putAll(loaded);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading suit registry: " + e.getMessage());
        }
    }

    /**
     * Returns the suit styles for the given key from the registry.
     *
     * @param key the suit style registry key
     * @return list of SuitStyle, or empty list if not found
     */
    public static List<SuitStyle> getSuitStyles(String key) {
        List<SuitStyle> styles = suitRegistry.get(key);
        return styles != null ? styles : new ArrayList<>();
    }

    private static void loadResourceThemes() {
        Gson gson = new Gson();
        try (InputStream is = ThemeManager.class.getResourceAsStream(RESOURCE_THEMES_PATH)) {
            if (is != null) {
                Type listType = new TypeToken<ArrayList<ThemeConfiguration>>() {
                }.getType();
                List<ThemeConfiguration> loadedResourceThemes = gson.fromJson(new InputStreamReader(is), listType);
                if (loadedResourceThemes != null && !loadedResourceThemes.isEmpty()) {
                    loadedResourceThemes.forEach(theme -> {
                        theme.setSuitStyleKey(theme.getSuitStyleKey());
                        theme.setGuiKey(theme.getGuiKey());
                        theme.setCursorKey(theme.getCursorKey());
                    });
                    themes.addAll(loadedResourceThemes);
                    System.out.println("Successfully loaded themes from resource: " + RESOURCE_THEMES_PATH);
                }
            } else {
                System.err.println("Resource theme JSON file not found: " + RESOURCE_THEMES_PATH + ". No default themes loaded from resources.");
                if (themes.isEmpty()) {
                    addHardcodedDefaultTheme();
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading themes from resource JSON: " + e.getMessage() + ". Attempting to use hardcoded default.");
            if (themes.isEmpty()) {
                addHardcodedDefaultTheme();
            }
        }
    }

    private static void addHardcodedDefaultTheme() {
        List<SuitStyle> defaultSuitStyles = new ArrayList<>();
        defaultSuitStyles.add(new SuitStyle("#FFD700"));
        defaultSuitStyles.add(new SuitStyle("#FF8800"));
        defaultSuitStyles.add(new SuitStyle("#DC143C"));
        defaultSuitStyles.add(new SuitStyle("#228B22"));
        defaultSuitStyles.add(new SuitStyle("#1E90FF"));
        defaultSuitStyles.add(new SuitStyle("#AD03FC"));
        defaultSuitStyles.add(new SuitStyle("white"));

        ThemeConfiguration defaultTheme = new ThemeConfiguration(
                DEFAULT_THEME_NAME,
                "card_front.png",
                "card_back.png",
                "front_poker_chips.png",
                "back_poker_chips.png",
                "background_image.png",
                defaultSuitStyles,
                "./com/mystic/tarotboard/assets/"
        );
        themes.add(defaultTheme);
    }

    private static void loadExternalThemes() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Type listType = new TypeToken<ArrayList<ThemeConfiguration>>() {
        }.getType();
        Path themesDir = PlatformPaths.getThemesDir();

        try {
            Files.createDirectories(themesDir);

            try (Stream<Path> paths = Files.list(themesDir)) {
                paths.filter(Files::isDirectory)
                        .forEach(dir -> {
                            try (Stream<Path> subPaths = Files.list(dir)) {
                                subPaths.filter(Files::isDirectory)
                                        .forEach(subDir -> {
                                            try (Stream<Path> jsonPaths = Files.list(subDir)) {
                                                if (subDir.getFileName().endsWith("/configs")) {
                                                    jsonPaths.filter(path -> path.toString().endsWith(".json"))
                                                            .forEach(jsonFile -> {
                                                                try (Reader reader = Files.newBufferedReader(jsonFile)) {
                                                                    List<ThemeConfiguration> loadedExternalThemes = gson.fromJson(reader, listType);
                                                                    if (loadedExternalThemes != null) {
                                                                        String basePath = jsonFile.getParent().toString();
                                                                        for (ThemeConfiguration externalTheme : loadedExternalThemes) {
                                                                            externalTheme.setBasePath(basePath);
                                                                            externalTheme.setSuitStyleKey(externalTheme.getSuitStyleKey());
                                                                            externalTheme.setGuiKey(externalTheme.getGuiKey());
                                                                            externalTheme.setCursorKey(externalTheme.getCursorKey());
                                                                            boolean replaced = false;
                                                                            for (int i = 0; i < themes.size(); i++) {
                                                                                if (themes.get(i).getThemeName().equalsIgnoreCase(externalTheme.getThemeName())) {
                                                                                    themes.set(i, externalTheme);
                                                                                    replaced = true;
                                                                                    break;
                                                                                }
                                                                            }
                                                                            if (!replaced) {
                                                                                themes.add(externalTheme);
                                                                            }
                                                                        }
                                                                        System.out.println("Successfully loaded themes from external file: " + jsonFile.getFileName());
                                                                    }
                                                                } catch (JsonSyntaxException e) {
                                                                    System.err.println("Error parsing JSON from " + jsonFile.getFileName() + ": " + e.getMessage());
                                                                } catch (IOException e) {
                                                                    System.err.println("Error reading file " + jsonFile.getFileName() + ": " + e.getMessage());
                                                                }
                                                            });
                                                } else {
                                                    System.err.println("Jsons must be in a configs directory: " + subDir.getFileName());
                                                }
                                            } catch (IOException e) {
                                                System.err.println("Error accessing directory " + subDir.getFileName() + ": " + e.getMessage());
                                            }
                                        });
                            } catch (IOException e) {
                                System.err.println("Error accessing directory " + dir.getFileName() + ": " + e.getMessage());
                            }
                        });
            }
        } catch (IOException e) {
            System.err.println("Error accessing user themes directory " + themesDir + ": " + e.getMessage());
        }
    }

    /**
     * Returns a defensive copy of all loaded themes.
     *
     * @return list of all theme configurations
     */
    public static List<ThemeConfiguration> getThemes() {
        return new ArrayList<>(themes);
    }

    /**
     * Finds a theme by name (case-insensitive). Falls back to the first available theme if not found.
     *
     * @param name the theme name to search for
     * @return the matching ThemeConfiguration, or the first available theme as fallback
     */
    public static ThemeConfiguration getThemeByName(String name) {
        for (ThemeConfiguration theme : themes) {
            if (theme.getThemeName().equalsIgnoreCase(name)) {
                return theme;
            }
        }
        System.err.println("Theme '" + name + "' not found. Falling back to default theme.");
        if (themes.isEmpty()) {
            addHardcodedDefaultTheme();
        }
        return themes.getFirst();
    }

    /**
     * Returns the hex color for a given suit name by looking up its associated suit style index.
     *
     * @param suit       the suit name
     * @param suitStyles the ordered list of suit styles to index into
     * @return the hex color string for the suit
     */
    public static String getSuitColor(String suit, List<SuitStyle> suitStyles) {
        if (CELESTIAL_COURT.contains(suit)) {
            return suitStyles.getFirst().getColorHex();
        } else if (UMBRAL_DOMINION.contains(suit)) {
            return suitStyles.get(1).getColorHex();
        } else if (INFERNAL_PACT.contains(suit)) {
            return suitStyles.get(2).getColorHex();
        } else if (VERDANT_CYCLE.contains(suit)) {
            return suitStyles.get(3).getColorHex();
        } else if (AETHERIC_LOOM.contains(suit)) {
            return suitStyles.get(4).getColorHex();
        } else if (DARK_EXPANSE.contains(suit)) {
            return suitStyles.get(5).getColorHex();
        }

        return suitStyles.getLast().getColorHex();
    }

    /**
     * Returns the currently active theme, defaulting to "Default" if none is set.
     *
     * @return the active ThemeConfiguration
     */
    public static ThemeConfiguration getActiveTheme() {
        if (activeTheme == null) {
            activeTheme = getThemeByName("Default");
        }
        return activeTheme;
    }

    /**
     * Sets the active theme.
     *
     * @param theme the theme to activate
     */
    public static void setActiveTheme(ThemeConfiguration theme) {
        activeTheme = theme;
    }
}