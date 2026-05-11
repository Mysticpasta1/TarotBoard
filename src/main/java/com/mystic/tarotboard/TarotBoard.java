package com.mystic.tarotboard;

import com.mystic.tarotboard.gameitems.Card;
import com.mystic.tarotboard.gameitems.Chip;
import com.mystic.tarotboard.gameitems.Die;
import com.mystic.tarotboard.theming.ThemeConfiguration;
import com.mystic.tarotboard.theming.ThemeManager;
import com.mystic.tarotboard.utils.CardDataHelper;
import com.mystic.tarotboard.utils.SaveData;
import com.mystic.tarotboard.utils.UIUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.transform.Translate;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TarotBoard extends Application {

    private static final List<String> wilds = List.of(
            "Joker", "Soul", "Light", "Dark", "Judgement", "Chorus", "Life", "Death", "Wrath",
            "Pride", "Greed", "Lust", "Envy", "Gluttony", "Sloth", "Chasity", "Temperance", "Charity",
            "Diligence", "Kindness", "Patience", "Humility", "Voice", "Voices", "Mother", "Father", "Brother",
            "Sister", "Duality", "Accord", "Husband", "Wife", "Progeny", "Corridor", "Field", "Intellect", "Brawn",
            "Despair", "Past", "Present", "Future", "Gate", "Sign", "Ruin", "Snow", "Rain", "Tempest", "Lovers",
            "Discord", "Concord", "Harmony", "Dissonance", "Earth", "Fire", "Water", "Air", "Spirit",
            "Oblivion", "Obscurity", "Purgatory", "Nether", "Underworld", "Aether", "Overworld", "Limbo", "Chaos",
            "Balance", "Doom", "Peace", "Evil", "Good", "Neutral", "Hope", "Monster", "Human", "Dusk", "Dawn",
            "Paradox", "Entropy"
    );

    private static final List<String> suits = List.of(
            "Arcs", "Arrows", "Clouds", "Clovers", "Comets", "Crescents", "Crosses",
            "Crowns", "Diamonds", "Embers", "Eyes", "Gears", "Glyphs", "Flames", "Flowers",
            "Hearts", "Keys", "Locks", "Leaves", "Mountains", "Points", "Scrolls", "Shells",
            "Shields", "Spades", "Spirals", "Stars", "Suns", "Swords", "Tridents", "Trees", "Waves",
            "Quasars", "Runes", "Omens", "Sigils", "Orbs", "Veils", "Looms", "Shards", "Echoes",
            "Rifts", "Ashes", "Nulls", "Hallows", "Fluxes", "Ethers", "Grims"
    );

    public static final List<String> values = List.of(
            "Fugitive", "Devil", "Shadow", "Specter", "Phantom", "Void", "Wraith",
            "Ghoul", "Banshee", "Reverent", "Eidolon", "Shade",
            "Doppelganger", "Hollow", "Abyss", "Chimera", "Poltergeist",
            "Wight", "Apparition", "Nightmare", "Succubus", "Incubus",
            "Necromancer", "Fury", "Grim", "Harbinger", "Spectacle",
            "Lich", "Gorgon", "Drake", "Demon", "Frost",
            "Golem", "Hydra", "Inferno", "Juggernaut", "Kraken", "Reaper",
            "Leviathan", "Manticore", "Naga", "Blight", "Serpent",

            "Hold",

            "Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10",
            "Jack", "Queen", "King", "Nomad", "Prince",
            "Rune", "Fable", "Sorceress", "Utopia", "Wizard",
            "Titan", "Baron", "Illusionist", "Oracle", "Magician",
            "Luminary", "Eclipse", "Celestial", "Duke", "Genesis",
            "Zephyr", "Vesper", "Umbra", "Valkyrie", "Warden",
            "Zenith", "Yggdrasil", "Zodiac", "Phoenix", "Raven",
            "Cipher", "Angel", "Knight"
    );

    private static final Pattern CARD_PATTERN = Pattern.compile("^(?<value>[\\d,a-z,A-Z]+) of (?<suit>[a-z,A-Z]+)$");
    private static final int NUM_CARDS = (suits.size() * values.size()) + wilds.size();
    private static final double CARD_WIDTH = 150;
    private static final double CARD_HEIGHT = 200;
    private static final String SAVE_FILE = System.getProperty("user.home") + File.separator + ".tarotboard" + File.separator + "save.dat";
    private static Stage primaryStage;
    private Scene startScene;
    private static Scene gameScene;
    private final List<Chip> chips = new ArrayList<>();
    private StackPane discardZone;
    private Image bwFrontImage;
    private Image bwBackImage;
    private Card[] cards; // Changed from StackPane[] cardPanes
    private final List<Die> dice = new ArrayList<>();
    private static boolean reshuffled = false;
    private static final ObservableList<String> cardNames = FXCollections.observableArrayList();
    private Color currentColor = Color.WHITE; // New field for selected color
    private ThemeConfiguration currentCardTheme = ThemeManager.getThemeByName("Default"); // Use ThemeConfiguration

    // Custom image paths
    private String customCardFrontPath = null;
    private String customCardBackPath = null;
    private String customChipFrontPath = null;
    private String customChipBackPath = null;
    private String customBackgroundPath = null;

    // Declare startLayout as a class field
    private VBox startLayout;

    @Override
    public void start(Stage primaryStage) {
        TarotBoard.primaryStage = primaryStage;
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        Pane gameRoot = new Pane();
        gameScene = new Scene(gameRoot, screenBounds.getWidth(), screenBounds.getHeight());

        // Load background image
        updateBackground(gameRoot);

        CardDataHelper.addCardNames(cardNames, wilds, suits, values);
        CardDataHelper.generateShuffledCardNames(cardNames);
        cards = new Card[NUM_CARDS]; // Initialize cards array

        loadAndCreateCards(gameRoot);

        // Load chip images
        bwFrontImage = loadImage(customChipFrontPath, currentCardTheme.getChipFrontPath(), currentCardTheme);
        bwBackImage = loadImage(customChipBackPath, currentCardTheme.getChipBackPath(), currentCardTheme);

        // Create buttons that will be moved to the control panel
        Button resetChips = getResetChipsButton(gameRoot);
        Button reshuffleCards = new Button("Reshuffle Cards");
        reshuffleCards.setOnAction(_ -> getReshuffleCardsButton());
        Button newGameButton = new Button("New Game");
        newGameButton.setOnAction(_ -> newGame(gameRoot));
        Button helpButton2 = createHelpButton();
        Button backButton3 = new Button("Back to Start");
        backButton3.setOnAction(_ -> switchToStart());


        // Buttons for the start scene
        Button newGameBtn = new Button("New Game");
        newGameBtn.setStyle("-fx-font-size: 20pt;");
        newGameBtn.setOnAction(_ -> {
            newGame(gameRoot);
            primaryStage.setScene(gameScene);
            primaryStage.setTitle("Game Scene");
        });

        Button continueButton = new Button("Continue");
        continueButton.setStyle("-fx-font-size: 20pt;");
        continueButton.setDisable(!new File(SAVE_FILE).exists());
        continueButton.setOnAction(_ -> continueGame());

        Button helpButton = createHelpButton();
        helpButton.setStyle("-fx-font-size: 20pt;");


        Button quitButton = new Button("Quit");
        quitButton.setStyle("-fx-font-size: 20pt;");
        quitButton.setOnAction(_ -> primaryStage.close());

        startLayout = new VBox(10); // Initialize the class field
        startLayout.setAlignment(Pos.CENTER);
        // Add buttons to startLayout BEFORE creating the scene
        startLayout.getChildren().addAll(newGameBtn, continueButton, helpButton, quitButton);
        startScene = new Scene(startLayout, screenBounds.getWidth(), screenBounds.getHeight());

        // Set background for startLayout
        updateStartSceneBackground();

        VBox controlPanelRight = new VBox(10);
        controlPanelRight.setAlignment(Pos.TOP_RIGHT);
        controlPanelRight.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-padding: 10; -fx-background-radius: 8;");
        controlPanelRight.setPrefWidth(200);
        controlPanelRight.setMaxWidth(200);

        ColorPicker colorPicker = new ColorPicker(currentColor);
        colorPicker.setOnAction(_ -> currentColor = colorPicker.getValue());
        colorPicker.setMaxWidth(Double.MAX_VALUE);

        Button spawnChipButton = new Button("Spawn Chip");
        spawnChipButton.setStyle("-fx-font-size: 11pt;");
        spawnChipButton.setMaxWidth(Double.MAX_VALUE);
        spawnChipButton.setOnAction(_ -> spawnChip(gameRoot, currentColor));

        TextField diceSidesInput = new TextField("20");
        diceSidesInput.setPrefWidth(60);
        diceSidesInput.setAlignment(Pos.CENTER);
        diceSidesInput.setStyle("-fx-font-size: 11pt;");

        Button spawnDieButton = new Button("Spawn Dice");
        spawnDieButton.setStyle("-fx-font-size: 11pt;");
        spawnDieButton.setMaxWidth(Double.MAX_VALUE);
        spawnDieButton.setOnAction(_ -> {
            try {
                int sides = Integer.parseInt(diceSidesInput.getText());
                if (sides > 0) {
                    spawnDie(gameRoot, sides, currentColor);
                } else {
                    System.err.println("Number of sides for die must be positive.");
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid number of sides for die: " + diceSidesInput.getText());
            }
        });

        HBox diceInputGroup = new HBox(5, diceSidesInput, spawnDieButton);

        diceInputGroup.setAlignment(Pos.CENTER_LEFT);
        diceInputGroup.setMaxWidth(Double.MAX_VALUE);

        Button resetDiceButton = new Button("Reset Dice");
        resetDiceButton.setStyle("-fx-font-size: 11pt;");
        resetDiceButton.setMaxWidth(Double.MAX_VALUE);
        resetDiceButton.setOnAction(_ -> {
            for (Die die : dice) {
                gameRoot.getChildren().remove(die.getPane());
            }
            dice.clear();
        });

        // Card Theme Selector
        ComboBox<ThemeConfiguration> themeSelector = new ComboBox<>(FXCollections.observableArrayList(ThemeManager.getThemes()));
        themeSelector.setValue(currentCardTheme);
        themeSelector.setMaxWidth(Double.MAX_VALUE);
        themeSelector.setConverter(new StringConverter<>() {
            @Override
            public String toString(ThemeConfiguration theme) {
                return theme != null ? theme.getThemeName() : "";
            }

            @Override
            public ThemeConfiguration fromString(String string) {
                return ThemeManager.getThemeByName(string);
            }
        });
        themeSelector.setOnAction(_ -> {
            currentCardTheme = themeSelector.getValue();
            // Reset custom card paths when a theme is selected
            customCardFrontPath = null;
            customCardBackPath = null;
            customChipFrontPath = null; // Clear custom chip paths too
            customChipBackPath = null;  // Clear custom chip paths too
            customBackgroundPath = null; // Clear custom background path too
            applyCurrentTheme(gameRoot); // Call new method to apply theme without new game
        });

        resetChips.setStyle("-fx-font-size: 11pt;");
        resetChips.setMaxWidth(Double.MAX_VALUE);
        reshuffleCards.setStyle("-fx-font-size: 11pt;");
        reshuffleCards.setMaxWidth(Double.MAX_VALUE);
        newGameButton.setStyle("-fx-font-size: 11pt;");
        newGameButton.setMaxWidth(Double.MAX_VALUE);
        helpButton2.setStyle("-fx-font-size: 11pt;");
        helpButton2.setMaxWidth(Double.MAX_VALUE);
        backButton3.setStyle("-fx-font-size: 11pt;");
        backButton3.setMaxWidth(Double.MAX_VALUE);


        controlPanelRight.getChildren().addAll(
                colorPicker,
                spawnChipButton,
                diceInputGroup,
                resetDiceButton,
                resetChips,
                reshuffleCards,
                newGameButton,
                helpButton2,
                backButton3,
                themeSelector
        );

        controlPanelRight.layoutXProperty().bind(gameScene.widthProperty().subtract(controlPanelRight.widthProperty()).subtract(10));
        controlPanelRight.setLayoutY(10);
        gameRoot.getChildren().add(controlPanelRight);

        discardZone = new StackPane();
        discardZone.setStyle("-fx-background-color: rgba(200,0,0,0.25); -fx-border-color: #cc0000; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;");
        discardZone.setPrefSize(90, 90);
        Text trashText = new Text("✖");
        trashText.setFont(javafx.scene.text.Font.font(36));
        trashText.setFill(Color.web("#cc0000"));
        discardZone.getChildren().add(trashText);
        discardZone.setLayoutX(10);
        discardZone.layoutYProperty().bind(gameScene.heightProperty().subtract(100));
        gameRoot.getChildren().add(discardZone);

        double[] mouseX = new double[1];
        double[] mouseY = new double[1];
        gameScene.setOnMouseMoved(event -> {
            mouseX[0] = event.getSceneX();
            mouseY[0] = event.getSceneY();
        });
        gameScene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() != javafx.scene.input.KeyCode.F) return;
            for (Chip chip : chips) {
                StackPane pane = chip.getChipPane();
                var pt = pane.sceneToLocal(mouseX[0], mouseY[0]);
                if (pane.contains(pt)) {
                    UIUtils.multiFlip(pane);
                    break;
                }
            }
        });

        primaryStage.setScene(startScene);
        primaryStage.setX(screenBounds.getMinX());
        primaryStage.setY(screenBounds.getMinY());
        primaryStage.setWidth(screenBounds.getWidth());
        primaryStage.setHeight(screenBounds.getHeight());
        primaryStage.show();
    }

    private Image loadImage(String customPath, String themeDefinedPath, ThemeConfiguration theme) {
        Image originalImage = null;
        String finalPath;

        // Try to load from customPath first (always treated as a file path)
        if (customPath != null && !customPath.trim().isEmpty()) {
            finalPath = customPath;
            try {
                File file = new File(finalPath);
                if (file.exists()) {
                    originalImage = new Image(file.toURI().toString());
                } else {
                    System.err.println("ERROR: Custom image file not found: " + finalPath);
                }
            } catch (Exception e) {
                System.err.println("ERROR: Failed to load custom image from file: " + finalPath + ". Falling back to theme-defined path. Error: " + e.getMessage());
            }
        }

        // Fallback to themeDefinedPath if customPath failed or was not provided
        if (originalImage == null && themeDefinedPath != null && !themeDefinedPath.trim().isEmpty()) {
            if (themeDefinedPath.startsWith("/")) {
                // Treat as an internal resource path
                finalPath = themeDefinedPath;
                try {
                    InputStream is = getClass().getResourceAsStream(finalPath);
                    if (is != null) {
                        originalImage = new Image(is);
                    } else {
                        System.err.println("ERROR: Theme-defined resource not found: " + finalPath);
                    }
                } catch (Exception e) {
                    System.err.println("ERROR: Failed to load theme-defined image from resource: " + finalPath + ". Error: " + e.getMessage());
                }
            } else {
                // Treat as an external file system path, potentially relative to basePath
                if (theme.getBasePath() != null) {
                    finalPath = Paths.get(theme.getBasePath(), themeDefinedPath).toString();
                } else {
                    finalPath = themeDefinedPath; // Assume it's an absolute path or relative to current working directory
                }

                try {
                    File file = new File(finalPath);
                    if (file.exists()) {
                        originalImage = new Image(file.toURI().toString());
                    } else {
                        System.err.println("ERROR: Theme-defined file not found: " + finalPath);
                    }
                } catch (Exception e) {
                    System.err.println("ERROR: Failed to load theme-defined image from file system: " + finalPath + ". Error: " + e.getMessage());
                }
            }
        }

        if (originalImage == null) {
            System.err.println("ERROR: No image could be loaded. Custom path: " + customPath + ", Theme-defined path: " + themeDefinedPath);
            return null; // Return null if no image could be loaded
        }

        return originalImage;
    }

    private void updateBackground(Pane gameRoot) {
        Image backgroundImage = loadImage(customBackgroundPath, currentCardTheme.getBackgroundPath(), currentCardTheme);
        if (backgroundImage != null) {
            BackgroundSize backgroundSize = new BackgroundSize(gameScene.getWidth(), gameScene.getHeight(), false, false, true, false);
            gameRoot.setBackground(new Background(new BackgroundImage(backgroundImage, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.CENTER, backgroundSize)));
        } else {
            System.err.println("WARNING: Background image could not be loaded. Using default/no background.");
            gameRoot.setBackground(Background.EMPTY); // Or set a solid color background
        }
    }

    private void updateStartSceneBackground() {
        if (startLayout != null && startScene != null) {
            Image backgroundImage = loadImage(customBackgroundPath, currentCardTheme.getBackgroundPath(), currentCardTheme);
            if (backgroundImage != null) {
                BackgroundSize backgroundSize = new BackgroundSize(startScene.getWidth(), startScene.getHeight(), false, false, true, false);
                startLayout.setBackground(new Background(new BackgroundImage(backgroundImage, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.CENTER, backgroundSize)));
            } else {
                System.err.println("WARNING: Start scene background image could not be loaded. Using default/no background.");
                startLayout.setBackground(Background.EMPTY); // Or set a solid color background
            }
        }
    }

    private void loadAndCreateCards(Pane gameRoot) {
        // Clear existing cards from gameRoot if any
        if (cards != null) {
            for (Card card : cards) {
                if (card != null) {
                    gameRoot.getChildren().remove(card.getCardPane());
                }
            }
        }

        Image cardFrontImage = loadImage(customCardFrontPath, currentCardTheme.getCardFrontPath(), currentCardTheme);
        Image cardBackImage = loadImage(customCardBackPath, currentCardTheme.getCardBackPath(), currentCardTheme);

        // Handle case where images might not load
        if (cardFrontImage == null || cardBackImage == null) {
            System.err.println("ERROR: Card images could not be loaded. Cannot create cards.");
            // Potentially display an error to the user or use placeholder images
            return;
        }

        System.out.println("Adding " + NUM_CARDS + " cards to the board");
        System.out.println(suits.size() + " Suits, " + values.size() + " Values per suit, and " + wilds.size() + " Wilds");

        for (int i = 0; i < NUM_CARDS; i++) {
            Card card;
            String cardLogicalName = cardNames.get(i);

            Matcher matcher = CARD_PATTERN.matcher(cardLogicalName);
            if (matcher.matches() && !wilds.contains(cardLogicalName)) {
                String value = matcher.group("value");
                String suit = matcher.group("suit");
                card = new Card(cardLogicalName, value, suit, CARD_WIDTH, CARD_HEIGHT, cardFrontImage, cardBackImage, currentCardTheme, wilds);
            } else {
                card = new Card(cardLogicalName, "", "", CARD_WIDTH, CARD_HEIGHT, cardFrontImage, cardBackImage, currentCardTheme, wilds); // Pass empty strings for value/suit for wild cards
                Text cardNameText = CardDataHelper.getWildCardName(new Text(cardLogicalName + "\n \n" + "(Wild)"));
                // Manually set text and style for wild cards as Card constructor doesn't handle it directly
                ((Text) card.getCardPane().getChildren().get(2)).setText(cardNameText.getText());
                card.getCardPane().getChildren().get(2).setStyle(cardNameText.getStyle());
            }
            cards[i] = card; // Store Card object

            cards[i].getCardPane().setTranslateX(50);
            cards[i].getCardPane().setTranslateY(50);
            UIUtils.makeDraggable(cards[i].getCardPane(), new Translate());
            UIUtils.makeFlippableAndRotatable(cards[i].getCardPane(), false);
            gameRoot.getChildren().add(cards[i].getCardPane());
        }
    }

    private Button getResetChipsButton(Pane gameRoot) {
        Button resetChips = new Button("Reset Chips");
        resetChips.setOnAction(_ -> {
            for (Chip chip : chips) {
                gameRoot.getChildren().remove(chip.getChipPane());
            }
            chips.clear();
        });

        return resetChips;
    }

    private void makeDiscardable(Pane pane, Pane gameRoot, StackPane discardZone) {
        pane.setOnMouseReleased(event -> {
            double sx = event.getSceneX();
            double sy = event.getSceneY();
            var bounds = discardZone.localToScene(discardZone.getBoundsInLocal());
            if (bounds.contains(sx, sy)) {
                for (int i = 0; i < dice.size(); i++) {
                    if (dice.get(i).getPane() == pane) {
                        gameRoot.getChildren().remove(pane);
                        dice.remove(i);
                        return;
                    }
                }

                int idx = -1;
                for (int i = 0; i < chips.size(); i++) {
                    if (chips.get(i).getChipPane() == pane) {
                        idx = i;
                        break;
                    }
                }
                if (idx >= 0) {
                    gameRoot.getChildren().remove(pane);
                    chips.remove(idx);
                }
            }
        });
    }

    private Button createHelpButton() {
        Button helpButton = new Button("Help");
        helpButton.setOnAction(_ -> HelpWindow.show(primaryStage));
        return helpButton;
    }

    private void getReshuffleCardsButton() {
        CardDataHelper.generateShuffledCardNames(cardNames);
        reshuffled = true;

        // Re-apply styles and tooltips for the new card order to existing cards
        for (int a = 0; a < NUM_CARDS; a++) {
            if (cards[a] != null) { // Ensure the card object exists
                StackPane cardPane = cards[a].getCardPane();
                Text cardNameText = cards[a].getCardName(); // Get the Text object from the existing Card
                String cardLogicalName = cardNames.get(a);
                Matcher matcher = CARD_PATTERN.matcher(cardLogicalName);

                // Reset position and rotation
                cardPane.setTranslateX(50);
                cardPane.setTranslateY(50);
                cardPane.getTransforms().removeAll(cardPane.getTransforms());
                cardPane.setRotate(0);

                // Reset flip state (face down)
                ImageView backView = (ImageView) cardPane.getChildren().get(0);
                ImageView frontView = (ImageView) cardPane.getChildren().get(1);
                Node textNode = cardPane.getChildren().get(2);
                backView.setVisible(true);
                frontView.setVisible(false);
                textNode.setVisible(false);

                if (matcher.matches() && !wilds.contains(cardLogicalName)) {
                    String value = matcher.group("value");
                    String suit = matcher.group("suit");
                    cardNameText.setText(Card.getStyle(cardLogicalName, value, suit, currentCardTheme).getText());
                    cardNameText.setStyle(Card.getStyle(cardLogicalName, value, suit, currentCardTheme).getStyle());
                } else {
                    Text text = new Text(cardLogicalName);
                    cardNameText.setText(CardDataHelper.getWildCardName(text).getText() + "\n \n" + "(Wild)");
                    cardNameText.setStyle(CardDataHelper.getWildCardName(text).getStyle());
                }
                cards[a].refreshTooltipContent(cardLogicalName, wilds); // Refresh the tooltip content

                UIUtils.makeDraggable(cardPane, new Translate());
                UIUtils.makeFlippableAndRotatable(cardPane, false);
            }
        }
        reshuffled = false;
    }

    private void continueGame() {
        loadGame();
        primaryStage.setScene(gameScene);
        primaryStage.setTitle("Game Scene");
    }

    private void switchToStart() {
        primaryStage.setScene(startScene);
        primaryStage.setTitle("Start Scene");
    }

    private void spawnDie(Pane gameRoot, int sides, Color dieColor) {
        Die die = new Die(sides, dieColor);
        StackPane diePane = die.getPane();
        UIUtils.makeDraggable(diePane, new Translate());
        this.makeDiscardable(diePane, gameRoot, discardZone);

        diePane.setTranslateX(gameScene.getWidth() / 2);
        diePane.setTranslateY(gameScene.getHeight() / 2);
        gameRoot.getChildren().add(diePane);
        dice.add(die);
    }

    private void spawnChip(Pane gameRoot, Color chipColor) {
        Chip chip = new Chip(chipColor, bwFrontImage, bwBackImage);
        StackPane chipPane = chip.getChipPane();

        chipPane.setTranslateX(gameScene.getWidth() / 2);
        chipPane.setTranslateY(gameScene.getHeight() / 2);

        UIUtils.makeDraggable(chipPane, new Translate());
        this.makeDiscardable(chipPane, gameRoot, discardZone);
        UIUtils.makeFlippableAndRotatable(chipPane, true);

        chips.add(chip);
        gameRoot.getChildren().add(chipPane);
    }

    private void newGame(Pane gameRoot) {
        // Clear existing cards from gameRoot
        if (cards != null) {
            for (Card card : cards) {
                if (card != null) {
                    gameRoot.getChildren().remove(card.getCardPane());
                }
            }
        }
        // Clear existing chips from gameRoot
        for (Chip chip : chips) {
            gameRoot.getChildren().remove(chip.getChipPane());
        }
        chips.clear();

        // Clear existing dice from gameRoot
        for (Die die : dice) {
            gameRoot.getChildren().remove(die.getPane());
        }
        dice.clear();

        // Re-initialize cards array
        cards = new Card[NUM_CARDS];
        loadAndCreateCards(gameRoot); // Load cards with the current theme

        CardDataHelper.generateShuffledCardNames(cardNames);
        reshuffled = true;

        // Re-apply styles and tooltips for the new card order
        for (int a = 0; a < NUM_CARDS; a++) {
            if (cards[a] != null) { // Ensure the card object exists
                Text cardNameText = cards[a].getCardName();
                String cardLogicalName = cardNames.get(a);
                Matcher matcher = CARD_PATTERN.matcher(cardLogicalName);
                if (matcher.matches() && !wilds.contains(cardLogicalName)) {
                    String value = matcher.group("value");
                    String suit = matcher.group("suit");
                    cardNameText.setText(Card.getStyle(cardLogicalName, value, suit, currentCardTheme).getText());
                    cardNameText.setStyle(Card.getStyle(cardLogicalName, value, suit, currentCardTheme).getStyle());
                } else {
                    Text text = new Text(cardLogicalName);
                    cardNameText.setText(CardDataHelper.getWildCardName(text).getText() + "\n \n" + "(Wild)");
                    cardNameText.setStyle(CardDataHelper.getWildCardName(text).getStyle());
                }
                cards[a].refreshTooltipContent(cardLogicalName, wilds); // Refresh the tooltip content
            }
        }
        reshuffled = false;
        applyCurrentTheme(gameRoot); // Apply theme after new game setup
    }

    // New method to apply the current theme to existing game items
    private void applyCurrentTheme(Pane gameRoot) {
        // Update background
        updateBackground(gameRoot);
        updateStartSceneBackground();

        // Reload chip images
        bwFrontImage = loadImage(customChipFrontPath, currentCardTheme.getChipFrontPath(), currentCardTheme);
        bwBackImage = loadImage(customChipBackPath, currentCardTheme.getChipBackPath(), currentCardTheme);

        // Update existing chips
        for (Chip chip : chips) {
            chip.updateImages(bwFrontImage, bwBackImage);
        }

        // Reload card images
        Image cardFrontImage = loadImage(customCardFrontPath, currentCardTheme.getCardFrontPath(), currentCardTheme);
        Image cardBackImage = loadImage(customCardBackPath, currentCardTheme.getCardBackPath(), currentCardTheme);

        // Update existing cards
        if (cards != null) {
            for (Card card : cards) {
                if (card != null) {
                    card.updateImages(cardFrontImage, cardBackImage);
                    // Also update card text style if needed (e.g., color changes with theme)
                    Text cardNameText = card.getCardName();
                    String cardName = cardNameText.getText().split("\n")[0].trim(); // Extract actual card name
                    Matcher matcher = CARD_PATTERN.matcher(cardName);
                    if (matcher.matches() && !wilds.contains(cardName)) {
                        String value = matcher.group("value");
                        String suit = matcher.group("suit");
                        cardNameText.setStyle(Card.getStyle(cardName, value, suit, currentCardTheme).getStyle());
                    } else {
                        // For wild cards, re-apply wild card style
                        Text tempText = new Text(cardName);
                        cardNameText.setStyle(CardDataHelper.getWildCardName(tempText).getStyle());
                    }
                    card.refreshTooltipContent(cardName, wilds); // Refresh the tooltip content
                }
            }
        }
    }


    @Override
    public void stop() {
        saveGame();
    }

    private void saveGame() {
        if (cards == null) return; // Changed from cardPanes

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(getSaveData(new ArrayList<>())); // Pass an empty list, as cardStates are generated in getSaveData
        } catch (IOException e) {
            System.err.println("Error saving game: " + e.getMessage());
        }
    }

    private SaveData getSaveData(List<SaveData.CardState> cardStates) {
        // Populate cardStates here, as it's needed for the SaveData constructor
        for (Card card : cards) { // Iterate through Card objects
            if (card == null) continue;
            StackPane pane = card.getCardPane(); // Get the pane from the Card object
            double tx = pane.getTranslateX();
            double ty = pane.getTranslateY();
            for (var t : pane.getTransforms()) {
                if (t instanceof Translate tr) {
                    tx += tr.getX();
                    ty += tr.getY();
                }
            }

            ImageView backView = (ImageView) pane.getChildren().get(0);
            ImageView frontView = (ImageView) pane.getChildren().get(1);
            Text text = (Text) pane.getChildren().get(2);

            cardStates.add(new SaveData.CardState(
                    tx, ty,
                    pane.getRotate(),
                    backView.getRotate(), frontView.getRotate(), text.getRotate(),
                    backView.isVisible(), frontView.isVisible(), text.isVisible()
            ));
        }

        List<SaveData.ChipState> chipStates = getChipStates();
        List<SaveData.DieState> dieStates = new ArrayList<>();
        for (Die die : dice) {
            StackPane diePane = die.getPane();
            double tx = diePane.getTranslateX();
            double ty = diePane.getTranslateY();
            for (var t : diePane.getTransforms()) {
                if (t instanceof Translate tr) {
                    tx += tr.getX();
                    ty += tr.getY();
                }
            }
            Color dieColor = die.getDieColor();

            dieStates.add(new SaveData.DieState(
                    tx, ty,
                    diePane.getRotate(),
                    die.getSides(),
                    die.getCurrentValue(),
                    dieColor.getRed(), dieColor.getGreen(), dieColor.getBlue(), dieColor.getOpacity()
            ));
        }

        return new SaveData(cardStates, chipStates, dieStates, reshuffled, new ArrayList<>(cardNames),
                customCardFrontPath, customCardBackPath, customChipFrontPath, customChipBackPath, customBackgroundPath,
                currentCardTheme.getThemeName()); // Save the current theme name
    }

    private List<SaveData.ChipState> getChipStates() {
        List<SaveData.ChipState> chipStates = new ArrayList<>();
        for (Chip chip : chips) {
            StackPane chipPane = chip.getChipPane();
            double tx = chipPane.getTranslateX();
            double ty = chipPane.getTranslateY();
            for (var t : chipPane.getTransforms()) {
                if (t instanceof Translate tr) {
                    tx += tr.getX();
                    ty += tr.getY();
                }
            }

            ImageView frontView = (ImageView) chipPane.getChildren().get(0);
            ImageView backView = (ImageView) chipPane.getChildren().get(1);
            Color chipColor = chip.getColor();

            chipStates.add(new SaveData.ChipState(
                    tx, ty,
                    frontView.getRotate(), backView.getRotate(),
                    frontView.isVisible(), backView.isVisible(),
                    chipColor.getRed(), chipColor.getGreen(), chipColor.getBlue(), chipColor.getOpacity()
            ));
        }
        return chipStates;
    }

    private void loadGame() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) {
            System.out.println("No save file found. Starting new game.");
            return;
        }

        SaveData save;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
            save = (SaveData) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading game: " + e.getMessage());
            return;
        }

        reshuffled = save.reshuffled();
        cardNames.setAll(save.cardNames());

        // Load custom image paths from save data
        customCardFrontPath = save.customCardFrontPath();
        customCardBackPath = save.customCardBackPath();
        customChipFrontPath = save.customChipFrontPath();
        customChipBackPath = save.customChipBackPath();
        customBackgroundPath = save.customBackgroundPath();
        currentCardTheme = ThemeManager.getThemeByName(save.themeName()); // Load theme from save

        // Remove existing cards from gameRoot before loading new ones
        Pane gameRoot = (Pane) gameScene.getRoot();
        if (cards != null) { // Changed from cardPanes
            for (Card card : cards) { // Iterate through Card objects
                if (card != null) {
                    gameRoot.getChildren().remove(card.getCardPane());
                }
            }
        }
        cards = new Card[NUM_CARDS]; // Re-initialize cards array

        // Load images for the current theme (assuming theme is saved or default)
        Image cardFrontImage = loadImage(customCardFrontPath, currentCardTheme.getCardFrontPath(), currentCardTheme);
        Image cardBackImage = loadImage(customCardBackPath, currentCardTheme.getCardBackPath(), currentCardTheme);

        // Update background and chip images based on loaded custom paths
        updateBackground(gameRoot);
        updateStartSceneBackground(); // Update start scene background
        bwFrontImage = loadImage(customChipFrontPath, currentCardTheme.getChipFrontPath(), currentCardTheme);
        bwBackImage = loadImage(customChipBackPath, currentCardTheme.getChipBackPath(), currentCardTheme);

        int idx = 0;
        for (var cs : save.cards()) {
            Card card; // Declare Card object
            String cardLogicalName = cardNames.get(idx);

            Matcher matcher = CARD_PATTERN.matcher(cardLogicalName);
            if (matcher.matches() && !wilds.contains(cardLogicalName)) {
                String value = matcher.group("value");
                String suit = matcher.group("suit");
                card = new Card(cardLogicalName, value, suit, CARD_WIDTH, CARD_HEIGHT, cardFrontImage, cardBackImage, currentCardTheme, wilds);
            } else {
                card = new Card(cardLogicalName, "", "", CARD_WIDTH, CARD_HEIGHT, cardFrontImage, cardBackImage, currentCardTheme, wilds); // Pass empty strings for value/suit for wild cards
                Text cardNameText = CardDataHelper.getWildCardName(new Text(cardLogicalName + "\n \n" + "(Wild)"));
                // Manually set text and style for wild cards as Card constructor doesn't handle it directly
                ((Text) card.getCardPane().getChildren().get(2)).setText(cardNameText.getText());
                card.getCardPane().getChildren().get(2).setStyle(cardNameText.getStyle());
            }
            cards[idx] = card; // Store Card object

            StackPane pane = cards[idx].getCardPane(); // Get pane from Card object

            ImageView backView = (ImageView) pane.getChildren().get(0);
            ImageView frontView = (ImageView) pane.getChildren().get(1);
            Text text = (Text) pane.getChildren().get(2);

            pane.getTransforms().clear();
            UIUtils.makeDraggable(pane, new Translate());

            pane.setTranslateX(cs.translateX());
            pane.setTranslateY(cs.translateY());
            pane.setRotate(cs.paneRotate());

            backView.setRotate(cs.backRotate());
            frontView.setRotate(cs.frontRotate());
            text.setRotate(cs.textRotate());
            backView.setVisible(cs.backVisible());
            frontView.setVisible(cs.frontVisible());
            text.setVisible(cs.textVisible());

            if (!wilds.contains(cardLogicalName)) {
                Matcher matcher1 = CARD_PATTERN.matcher(cardLogicalName);
                if (matcher1.matches()) {
                    String value = matcher1.group("value");
                    String suit = matcher1.group("suit");
                    var styled = Card.getStyle(cardLogicalName, value, suit, currentCardTheme);
                    text.setText(styled.getText());
                    text.setStyle(styled.getStyle());
                }
            }
            cards[idx].refreshTooltipContent(cardLogicalName, wilds); // Refresh the tooltip content
            gameRoot.getChildren().add(pane); // Add the card to the gameRoot
            idx++;
        }

        for (var ds : save.dice()) { // Corrected loop to iterate over save.dice()
            Color loadedColor = new Color(ds.red(), ds.green(), ds.blue(), ds.opacity());
            Die die = new Die(ds.sides(), loadedColor);
            StackPane diePane = die.getPane();

            diePane.translateXProperty().unbind();
            diePane.getTransforms().clear();
            UIUtils.makeDraggable(diePane, new Translate());
            this.makeDiscardable(diePane, gameRoot, discardZone);

            diePane.setTranslateX(ds.translateX());
            diePane.setTranslateY(ds.translateY());
            diePane.setRotate(ds.paneRotate());

            die.setCurrentValue(ds.currentValue());

            dice.add(die);
            gameRoot.getChildren().add(diePane);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
