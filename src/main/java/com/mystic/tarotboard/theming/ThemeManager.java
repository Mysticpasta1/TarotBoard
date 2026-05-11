package com.mystic.tarotboard.theming;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ThemeManager {

    private static final List<ThemeConfiguration> themes = new ArrayList<>();
    private static final Path USER_THEMES_DIR = Paths.get(System.getProperty("user.home"), ".tarotboard", "themes");
    private static final String RESOURCE_THEMES_PATH = "/com/mystic/tarotboard/assets/themes.json";
    private static final String DEFAULT_THEME_NAME = "Default";

    private static final List<String> CELESTIAL_COURT = List.of("Stars", "Suns", "Crowns", "Quasars", "Crescents", "Sigils", "Comets", "Glyphs");
    private static final List<String> UMBRAL_DOMINION = List.of("Veils", "Runes", "Hearts", "Spirals", "Eyes", "Omens", "Diamonds", "Orbs");
    private static final List<String> INFERNAL_PACT = List.of("Arrows", "Flames", "Locks", "Arcs", "Swords", "Points", "Embers", "Gears");
    private static final List<String> VERDANT_CYCLE = List.of("Flowers", "Leaves", "Mountains", "Shells", "Clovers", "Tridents", "Trees", "Waves");
    private static final List<String> AETHERIC_LOOM = List.of("Clouds", "Crosses", "Shields", "Keys", "Spades", "Scrolls", "Looms", "Shards");
    private static final List<String> DARK_EXPANSE = List.of("Echoes", "Rifts", "Ashes", "Nulls", "Hallows", "Fluxes", "Ethers", "Grims");

    static {
        // Load themes from application resources first
        loadResourceThemes();
        // Then load external themes, which can override or add to the resource themes
        loadExternalThemes();
    }

    private static void loadResourceThemes() {
        Gson gson = new Gson();
        try (InputStream is = ThemeManager.class.getResourceAsStream(RESOURCE_THEMES_PATH)) {
            if (is != null) {
                Type listType = new TypeToken<ArrayList<ThemeConfiguration>>() {
                }.getType();
                List<ThemeConfiguration> loadedResourceThemes = gson.fromJson(new InputStreamReader(is), listType);
                if (loadedResourceThemes != null && !loadedResourceThemes.isEmpty()) {
                    // For resource themes, basePath is null
                    loadedResourceThemes.forEach(theme -> theme.setBasePath(null));
                    themes.addAll(loadedResourceThemes);
                    System.out.println("Successfully loaded themes from resource: " + RESOURCE_THEMES_PATH);
                }
            } else {
                System.err.println("Resource theme JSON file not found: " + RESOURCE_THEMES_PATH + ". No default themes loaded from resources.");
                // If resource themes.json is not found, ensure a basic default is still present
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
        defaultSuitStyles.add(new SuitStyle("CELESTIAL_COURT", "#FFD700"));
        defaultSuitStyles.add(new SuitStyle("UMBRAL_DOMINION", "#FF8800"));
        defaultSuitStyles.add(new SuitStyle("INFERNAL_PACT", "#DC143C"));
        defaultSuitStyles.add(new SuitStyle("VERDANT_CYCLE", "#228B22"));
        defaultSuitStyles.add(new SuitStyle("AETHERIC_LOOM", "#1E90FF"));
        defaultSuitStyles.add(new SuitStyle("DARK_EXPANSE", "#AD03FC"));
        defaultSuitStyles.add(new SuitStyle("WILDS", "#E5E7EB"));

        ThemeConfiguration defaultTheme = new ThemeConfiguration(
                DEFAULT_THEME_NAME,
                "/com/mystic/tarotboard/assets/card_front.png",
                "/com/mystic/tarotboard/assets/card_back.png",
                "/com/mystic/tarotboard/assets/front_poker_chips.png",
                "/com/mystic/tarotboard/assets/back_poker_chips.png",
                "/com/mystic/tarotboard/assets/background_image.png",
                defaultSuitStyles,
                null // basePath is null for hardcoded resource themes
        );
        themes.add(defaultTheme);
    }

    private static void loadExternalThemes() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Type listType = new TypeToken<ArrayList<ThemeConfiguration>>() {
        }.getType();

        try {
            Files.createDirectories(USER_THEMES_DIR);

            try (Stream<Path> paths = Files.list(USER_THEMES_DIR)) {
                paths.filter(Files::isDirectory)
                        .forEach(dir -> {
                            try (Stream<Path> subPaths = Files.list(dir)) {
                                subPaths.filter(Files::isRegularFile)
                                        .filter(path -> path.toString().endsWith(".json"))
                                        .forEach(jsonFile -> {
                                            try (Reader reader = Files.newBufferedReader(jsonFile)) {
                                                List<ThemeConfiguration> loadedExternalThemes = gson.fromJson(reader, listType);
                                                if (loadedExternalThemes != null) {
                                                    String basePath = jsonFile.getParent().toString(); // Get the directory of the JSON file
                                                    for (ThemeConfiguration externalTheme : loadedExternalThemes) {
                                                        externalTheme.setBasePath(basePath); // Set the base path for relative image resolution
                                                        // Check if a theme with the same name already exists (from resources)
                                                        boolean replaced = false;
                                                        for (int i = 0; i < themes.size(); i++) {
                                                            if (themes.get(i).getThemeName().equalsIgnoreCase(externalTheme.getThemeName())) {
                                                                themes.set(i, externalTheme); // Replace existing theme
                                                                replaced = true;
                                                                break;
                                                            }
                                                        }
                                                        if (!replaced) {
                                                            themes.add(externalTheme); // Add new external theme
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
                            } catch (IOException e) {
                                System.err.println("Error accessing directory " + dir.getFileName() + ": " + e.getMessage());
                            }
                        });
            }
        } catch (IOException e) {
            System.err.println("Error accessing user themes directory " + USER_THEMES_DIR + ": " + e.getMessage());
        }
    }

    public static List<ThemeConfiguration> getThemes() {
        return new ArrayList<>(themes); // Return a copy to prevent external modification
    }

    public static ThemeConfiguration getThemeByName(String name) {
        for (ThemeConfiguration theme : themes) {
            if (theme.getThemeName().equalsIgnoreCase(name)) {
                return theme;
            }
        }
        System.err.println("Theme '" + name + "' not found. Falling back to default theme.");
        // Ensure there's always at least one theme, even if loading failed
        if (themes.isEmpty()) {
            addHardcodedDefaultTheme(); // Fallback to hardcoded if everything else failed
        }
        return themes.getFirst(); // Fallback to the first available theme (should be default)
    }

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

        return suitStyles.getLast().getColorHex(); // Default Ghost White
    }
}