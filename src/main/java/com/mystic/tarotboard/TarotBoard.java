package com.mystic.tarotboard;

import com.sandec.mdfx.MarkdownView;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;
import javafx.scene.transform.Translate;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.html.HtmlRenderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
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
            "Balance", "Doom", "Peace", "Evil", "Good", "Neutral", "Hope", "Monster", "Human"
    );

    private static final List<String> suits = List.of(
            "Arcs", "Arrows", "Clouds", "Clovers", "Comets", "Crescents", "Crosses",
            "Crowns", "Diamonds", "Embers", "Eyes", "Gears", "Glyphs", "Flames", "Flowers",
            "Hearts", "Keys", "Locks", "Leaves", "Mountains", "Points", "Scrolls", "Shells",
            "Shields", "Spades", "Spirals", "Stars", "Suns", "Swords", "Tridents", "Trees", "Waves",
            "Quasars", "Runes", "Omens", "Sigils", "Orbs", "Veils", "Looms", "Shards"
    );

    public static final List<String> values = List.of(
            //Negative Cards
            "Devil", "Shadow", "Specter", "Phantom", "Void", "Wraith",
            "Ghoul", "Banshee", "Reverent", "Eidolon", "Shade",
            "Doppelganger", "Hollow", "Abyss", "Chimera", "Poltergeist",
            "Wight", "Apparition", "Nightmare", "Succubus", "Incubus",
            "Necromancer", "Fury", "Grim", "Harbinger", "Spectacle",
            "Lich", "Gorgon", "Drake", "Demon", "Frost",
            "Golem", "Hydra", "Inferno", "Juggernaut", "Kraken", "Reaper",
            "Leviathan", "Manticore", "Naga", "Blight", "Serpent",

            //Neutral Card
            "Hold",

            //Positive Cards
            "Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10",
            "Jack", "Queen", "King", "Nomad", "Prince",
            "Rune", "Fable", "Sorceress", "Utopia", "Wizard",
            "Titan", "Baron", "Illusionist", "Oracle", "Magician",
            "Luminary", "Eclipse", "Celestial", "Duke", "Genesis",
            "Zephyr", "Vesper", "Umbra", "Valkyrie", "Warden",
            "Zenith", "Yggdrasil", "Zodiac", "Phoenix", "Raven",
            "Cipher", "Angel"
    );

    private static final Pattern CARD_PATTERN = Pattern.compile("^(?<value>[\\d,a-z,A-Z]+) of (?<suit>[a-z,A-Z]+)$");
    private static final int NUM_CARDS = (suits.size() * values.size()) + wilds.size();
    private static final double CARD_WIDTH = 150;
    private static final double CARD_HEIGHT = 200;
    private static final String[] colors = {"firebrick", "orange", "goldenrod", "yellow", "yellowgreen", "green", "cyan", "lightblue", "blue", "darkorchid", "purple", "gray", "darkgray", "white"};
    private static final int NUM_CHIPS = 250;
    private static final int TOTAL_CHIPS = colors.length * NUM_CHIPS;
    private static int rotationAngle = 0;
    private static final Map<String, String> cardTooltips = new HashMap<>();
    private final Map<String, String> chipTooltips = new HashMap<>();
    private static Stage primaryStage;
    private Scene startScene;
    private static Scene gameScene;
    private final List<PokerChips> pokerChips = new ArrayList<>();
    private final StackPane[] chipPanes = new StackPane[TOTAL_CHIPS];
    private static boolean reshuffled = false;
    private static final ObservableList<String> cardNames = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        TarotBoard.primaryStage = primaryStage;
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        Pane gameRoot = new Pane();
        gameScene = new Scene(gameRoot, screenBounds.getWidth(), screenBounds.getHeight());

        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/mystic/tarotboard/assets/background_image.png")));
        BackgroundSize backgroundSize = new BackgroundSize(gameScene.getWidth(), gameScene.getHeight(), false, false, true, false);
        BackgroundImage backgroundImage = new BackgroundImage(image, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.CENTER, backgroundSize);
        Background background = new Background(backgroundImage);
        gameRoot.setBackground(background);

        addCardNames();
        generateShuffledCardNames();
        StackPane[] cardPanes = new StackPane[NUM_CARDS]; // Array to store card panes

        // Preload card images only once
        Image cardFrontImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/mystic/tarotboard/assets/card_front.png")));
        Image cardBackImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/mystic/tarotboard/assets/card_back.png")));

        System.out.println("Adding " + NUM_CARDS + " cards to the board");
        System.out.println(suits.size() + " Suits, " + values.size() + " Values per suit, and " + wilds.size() + " Wilds");

        for (int i = 0; i < NUM_CARDS; i++) {
            Text cardNameText;

            Matcher matcher = CARD_PATTERN.matcher(cardNames.get(i));
            if (matcher.matches() && !wilds.contains(cardNames.get(i))) {
                // Process normal cards
                String value = matcher.group("value");
                String suit = matcher.group("suit");
                Card card = new Card(cardNames.get(i), value, suit, CARD_WIDTH, CARD_HEIGHT, cardFrontImage, cardBackImage);
                cardNameText = card.getCardName();
                cardPanes[i] = card.getCardPane();
            } else {
                StackPane cardPane = new StackPane();

                // Process wild cards
                cardNameText = getWildCardName(new Text(cardNames.get(i) + "\n \n" + "(Wild)"));

                // Set up the images (no need to create them multiple times)
                ImageView cardBackImageView = new ImageView(cardBackImage);
                cardBackImageView.setFitWidth(CARD_WIDTH);
                cardBackImageView.setFitHeight(CARD_HEIGHT);
                cardBackImageView.setVisible(true);

                ImageView cardFrontImageView = new ImageView(cardFrontImage);
                cardFrontImageView.setFitWidth(CARD_WIDTH);
                cardFrontImageView.setFitHeight(CARD_HEIGHT);
                cardFrontImageView.setVisible(false);

                cardPane.getChildren().addAll(cardBackImageView, cardFrontImageView, cardNameText);

                cardPanes[i] = cardPane;
            }

            // Set initial positions and make draggable and flippable
            cardPanes[i].setTranslateX(50);
            cardPanes[i].setTranslateY(50);
            makeDraggable(cardPanes[i], new Translate());
            makeFlippableAndRotatable(cardPanes[i]);

            // Tooltip setup
            reshuffled = false;
            generateCardTooltips(cardNameText.getText());
            makeCardTooltip(cardPanes[i], cardNameText.getText(), reshuffled); //Set to 0 since reshuffled is false here!

            // Add the card to the root pane
            gameRoot.getChildren().add(cardPanes[i]);
        }

        for (String color : colors) {
            for (int i = 0; i < NUM_CHIPS; i++) {
                pokerChips.add(new PokerChips(color, i));
            }
        }

        System.out.println("Adding " + (NUM_CHIPS * colors.length));
        System.out.println(NUM_CHIPS + " Chips per " + colors.length + " Colors");

        generateChipTooltips();

        // Preload poker chip images only once
        Image bwFrontImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/mystic/tarotboard/assets/front_poker_chips.png")));
        Image bwBackImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/mystic/tarotboard/assets/back_poker_chips.png")));

        // Define the chip color adjustment blend
        double chipRadius = 50;
        double spacing = 5;

        for (int i = 0; i < colors.length; i++) {
            String color = colors[i];
            List<PokerChips> chipsOfColor = pokerChips.stream().filter(chip -> chip.color().equals(color)).toList();

            for (int j = 0; j < chipsOfColor.size(); j++) {
                PokerChips chip = chipsOfColor.get(j);
                Color chipColor = Color.valueOf(chip.color());

                // Create a Circle clip for the chip
                Circle circleClip = new Circle(chipRadius / 2);
                circleClip.setCenterX(chipRadius / 2);
                circleClip.setCenterY(chipRadius / 2);

                // Create StackPane for chip
                StackPane chipPane = new StackPane();

                // Front image with color applied using a Blend effect
                ImageView chipFrontImageView = createChipImageView(bwFrontImage, chipRadius, chipColor);

                // Back image with color applied
                ImageView chipBackImageView = createChipImageView(bwBackImage, chipRadius, chipColor);
                chipBackImageView.setVisible(false); // Initially invisible

                chipPane.getChildren().addAll(chipFrontImageView, chipBackImageView);
                chipPane.setClip(circleClip);

                // Set initial positions
                chipPane.translateXProperty().bind(gameScene.widthProperty().subtract(200).subtract(j));
                chipPane.setTranslateY((gameScene.getHeight() / 8) + (chipRadius + spacing) * i - 75);

                // Add interactivity
                makeDraggable(chipPane, new Translate());
                makeFlippableAndRotatable(chipPane);
                makeChipTooltip(chipPane, i, j, chipsOfColor);

                // Store and display the chip
                chipPanes[i * chipsOfColor.size() + j] = chipPane;
                gameRoot.getChildren().add(chipPane);
            }
        }

        Button resetChips = getResetChipsButton(chipRadius, spacing);
        resetChips.setStyle("-fx-font-size: 15pt;");
        resetChips.layoutXProperty().bind(gameScene.widthProperty().subtract(resetChips.widthProperty()).subtract(50));
        resetChips.layoutYProperty().bind(gameScene.heightProperty().subtract(resetChips.heightProperty()).subtract(200));

        Button reshuffleCards = getReshuffleCardsButton(cardPanes);
        reshuffleCards.setStyle("-fx-font-size: 15pt;");
        reshuffleCards.layoutXProperty().bind(gameScene.widthProperty().subtract(reshuffleCards.widthProperty()).subtract(50));
        reshuffleCards.layoutYProperty().bind(gameScene.heightProperty().subtract(reshuffleCards.heightProperty()).subtract(150));

        // Create the start scene
        VBox startLayout = new VBox(10);

        Button singlePlayer = new Button("Single Player");
        singlePlayer.setStyle("-fx-font-size: 20pt;");
        singlePlayer.setOnAction(_ -> switchToGame(cardPanes));

        Button continueButton = new Button("Continue");
        continueButton.setStyle("-fx-font-size: 20pt;");
        continueButton.setOnAction(_ -> continueGame());

        Button helpButton = createHelpButton();
        helpButton.setStyle("-fx-font-size: 20pt;");

        Button quitButton = new Button("Quit");
        quitButton.setStyle("-fx-font-size: 20pt;");
        quitButton.setOnAction(_ -> primaryStage.close());

        startLayout.setAlignment(Pos.CENTER);
        startScene = new Scene(startLayout, screenBounds.getWidth(), screenBounds.getHeight());
        startLayout.getChildren().addAll(singlePlayer, continueButton, helpButton, quitButton);
        // Create the game scene

        Button helpButton2 = createHelpButton();
        helpButton2.setStyle("-fx-font-size: 15pt;");
        helpButton2.layoutXProperty().bind(gameScene.widthProperty().subtract(helpButton2.widthProperty()).subtract(50));
        helpButton2.layoutYProperty().bind(gameScene.heightProperty().subtract(helpButton2.heightProperty()).subtract(100));

        Button backButton3 = new Button("Back to Start");
        backButton3.setOnAction(_ -> switchToStart());
        backButton3.setStyle("-fx-font-size: 15pt;");
        backButton3.layoutXProperty().bind(gameScene.widthProperty().subtract(backButton3.widthProperty()).subtract(50));
        backButton3.layoutYProperty().bind(gameScene.heightProperty().subtract(backButton3.heightProperty()).subtract(50));
        startLayout.setBackground(background);

        gameRoot.getChildren().addAll(resetChips, reshuffleCards, helpButton2, backButton3);

        primaryStage.setScene(startScene);
        primaryStage.setX(screenBounds.getMinX());
        primaryStage.setY(screenBounds.getMinY());
        primaryStage.setWidth(screenBounds.getWidth());
        primaryStage.setHeight(screenBounds.getHeight());
        primaryStage.show();
    }

    private static Text getWildCardName(Text cardNameText) {
        cardNameText.setStyle("-fx-font-size: 15pt; -fx-fill: white;");
        cardNameText.setBoundsType(TextBoundsType.VISUAL); // Use visual bounds to get accurate text size
        cardNameText.setWrappingWidth(CARD_WIDTH); // Use the card width for centering
        cardNameText.setTextAlignment(TextAlignment.CENTER);
        cardNameText.setTranslateY(0);
        cardNameText.setVisible(false);
        return cardNameText;
    }

    // Helper method to create chip images with color applied
    private ImageView createChipImageView(Image image, double radius, Color chipColor) {
        // Create an ImageView for the chip
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(radius);
        imageView.setFitHeight(radius);

        // Apply color to the chip using Blend effect
        ColorInput colorInput = new ColorInput(0, 0, radius, radius, chipColor);
        Blend blendEffect = new Blend(BlendMode.MULTIPLY, colorInput, null);
        imageView.setEffect(blendEffect);  // Apply the blend effect to the ImageView

        return imageView;
    }

    private Button getResetChipsButton(double chipRadius, double spacing) {
        Button resetChips = new Button("Reset Chips");
        resetChips.setOnAction(_ -> {
            for (int i = 0; i < colors.length; i++) {
                String color = colors[i];
                List<PokerChips> chipsOfColor = pokerChips.stream().filter(chip -> chip.color().equals(color)).toList();

                for (int j = 0; j < chipsOfColor.size(); j++) {
                    StackPane pane = chipPanes[i * chipsOfColor.size() + j];
                    ImageView chipBackImageView = (ImageView) pane.getChildren().get(0);
                    ImageView chipFrontImageView = (ImageView) pane.getChildren().get(1);
                    chipBackImageView.setRotate(0);
                    chipFrontImageView.setRotate(0);
                    chipBackImageView.setVisible(true);
                    chipFrontImageView.setVisible(false);

                    pane.getTransforms().clear();
                    pane.translateXProperty().bind(gameScene.widthProperty().subtract(200).subtract(j));
                    pane.setTranslateY(((gameScene.getHeight() / 8) + (chipRadius + spacing) * i) - 75);
                    makeDraggable(pane, new Translate());

                    // Ensure the chip is on top of other elements
                    pane.toFront();
                }
            }
        });

        return resetChips;
    }

    public static void makeDraggable(StackPane pane, Translate translate) {
        final double[] dragDeltaX = new double[1];
        final double[] dragDeltaY = new double[1];

        // Use a Translate object to handle smooth dragging
        pane.getTransforms().add(translate);

        // When the mouse is pressed, record the starting position
        pane.setOnMousePressed(event -> {
            // Record the delta of mouse position relative to the node's current translation
            dragDeltaX[0] = event.getSceneX() - translate.getX();
            dragDeltaY[0] = event.getSceneY() - translate.getY();

            pane.toFront();
        });

        // When the mouse is dragged, update the translation of the pane
        pane.setOnMouseDragged(event -> {
            // Calculate the new translation values
            double newTranslateX = event.getSceneX() - dragDeltaX[0];
            double newTranslateY = event.getSceneY() - dragDeltaY[0];

            // Update the translation values only if necessary (avoid redundant calls)
            if (translate.getX() != newTranslateX || translate.getY() != newTranslateY) {
                translate.setX(newTranslateX);
                translate.setY(newTranslateY);
            }
        });
    }

    private Button createHelpButton() {
        Button helpButton = new Button("Help");
        helpButton.setOnAction(event -> {
            Stage helpStage = new Stage();
            helpStage.setTitle("TarotBoard Poker — Help");
            helpStage.initModality(Modality.APPLICATION_MODAL);
            helpStage.initOwner(primaryStage);

            MarkdownView mdView = new MarkdownView();
            mdView.setPrefWidth(1800);  // slightly smaller than 1920 for padding

            // Wrap in a ScrollPane for scrolling
            ScrollPane scrollPane = new ScrollPane(mdView);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setPrefViewportWidth(1800);
            scrollPane.setPrefViewportHeight(980); // leave space for button

            // Load markdown text
            try (InputStream is = getClass().getResourceAsStream("/com/mystic/tarotboard/assets/TarotBoard_Poker_Rules.md")) {
                if (is != null) {
                    String markdown = new String(is.readAllBytes());
                    mdView.setMdString(markdown);
                } else {
                    mdView.setMdString("Could not find help file.");
                }
            } catch (IOException e) {
                mdView.setMdString("Could not load help file.");
            }

            Button closeButton = new Button("Back");
            closeButton.setStyle("-fx-font-size: 16pt;");
            closeButton.setOnAction(e -> helpStage.close());

            VBox helpLayout = new VBox(10, scrollPane, closeButton);
            helpLayout.setPrefSize(1920, 1080);
            helpLayout.setSpacing(20);
            helpLayout.setStyle("-fx-padding: 20;");
            helpLayout.setAlignment(javafx.geometry.Pos.TOP_CENTER);

            helpStage.setScene(new Scene(helpLayout, 1920, 1080));
            helpStage.centerOnScreen();
            helpStage.showAndWait();
        });

        return helpButton;
    }

    private Button getReshuffleCardsButton(StackPane[] cardPanes) {
        Button reshuffleCards = new Button("Reshuffle Cards");
        reshuffleCards.setOnAction(_ -> {
            for (StackPane pane : cardPanes) {
                ImageView cardBackImageView = (ImageView) pane.getChildren().get(0);
                ImageView cardFrontImageView = (ImageView) pane.getChildren().get(1);
                Text cardNameText = (Text) pane.getChildren().get(2);
                cardNameText.setRotate(0);
                cardBackImageView.setRotate(0);
                cardFrontImageView.setRotate(0);
                cardNameText.setVisible(false);
                cardBackImageView.setVisible(true);
                cardFrontImageView.setVisible(false);
                rotationAngle = 0;

                // Reset the translation positions after binding
                pane.getTransforms().clear();
                pane.setTranslateX(50);
                pane.setTranslateY(50);
                makeDraggable(pane, new Translate());
            }

            // Shuffle and update card names
            generateShuffledCardNames();
            reshuffled = true;

            // Update the card names
            for (int a = 0; a < NUM_CARDS; a++) {
                if (cardPanes[a].getChildren().get(2) instanceof Text cardNameText) {
                    Matcher matcher = CARD_PATTERN.matcher(cardNames.get(a));
                    if (matcher.matches() && !wilds.contains(cardNames.get(a))) {
                        String value = matcher.group("value");
                        String suit = matcher.group("suit");
                        cardNameText.setText(Card.getStyle(cardNames.get(a), value, suit).getText());
                        cardNameText.setStyle(Card.getStyle(cardNames.get(a), value, suit).getStyle());
                    } else {
                        Text text = new Text(cardNames.get(a));
                        cardNameText.setText(getWildCardName(text).getText() + "\n \n" + "(Wild)");
                        cardNameText.setStyle(getWildCardName(text).getStyle());
                    }
                    makeCardTooltip(cardPanes[a], cardNameText.getText(), reshuffled);
                }
            }
        });

        reshuffled = false;
        return reshuffleCards;
    }

    private static void makeCardTooltip(StackPane pane, String text, boolean reshuffled) {
        String hoverText = cardTooltips.get(text);
        if (hoverText != null) {
            Tooltip tooltip = new Tooltip(hoverText);
            tooltip.setStyle("-fx-font-size: 18pt;");

            if (reshuffled) {
                Tooltip.uninstall(pane, tooltip);
            }

            Tooltip.install(pane, tooltip);

            // Show tooltip on mouse enter
            pane.setOnMouseEntered(event -> {
                if (pane.getChildren().get(1).isVisible()) {
                    tooltip.setText(hoverText);
                    tooltip.show(pane, event.getScreenX(), event.getScreenY() + 5); // Offset tooltip position
                } else {
                    tooltip.setText("Unknown");
                }
            });

            // Hide tooltip on mouse exit
            pane.setOnMouseExited(_ -> tooltip.hide());
        }
    }

    private void makeChipTooltip(Pane pane, int i, int j, List<PokerChips> colorOfChips) {
        String hoverText = chipTooltips.get(Integer.toString(i * colorOfChips.size() + j));
        Tooltip tooltip = new Tooltip(hoverText);
        tooltip.setStyle("-fx-font-size: 18pt;");
        Tooltip.install(pane, tooltip);

        // Show tooltip on mouse enter
        pane.setOnMouseEntered(event -> {
            if (pane.getChildren().getFirst().isVisible()) {
                tooltip.setText(hoverText);
                tooltip.show(pane, event.getScreenX(), event.getScreenY() + 5); // Offset tooltip position
            } else {
                tooltip.setText("-" + hoverText);
            }
        });

        // Hide tooltip on mouse exit
        pane.setOnMouseExited(_ -> tooltip.hide());
    }

    private void generateChipTooltips() {
        String points;
        for (int i = 0; i < colors.length; i++) {
            String color = colors[i];
            List<PokerChips> chipsOfColor = pokerChips.stream().filter(chip -> chip.color().equals(color)).toList();
            int num = i * 5;
            if (i * 5 == 0) {
                points = Integer.toString((1));
            } else {
                points = Integer.toString(num);
            }
            for (int j = 0; j < chipsOfColor.size(); j++) {
                chipTooltips.put(Integer.toString(i * chipsOfColor.size() + j), points);
            }
        }
    }

    private void continueGame() {
        primaryStage.setScene(gameScene);
        primaryStage.setTitle("Game Scene");
    }

    private static void switchToGame(StackPane[] cardPanes) {
        // Loop through each card pane
        for (StackPane pane : cardPanes) {
            ImageView cardBackImageView = (ImageView) pane.getChildren().get(0);
            ImageView cardFrontImageView = (ImageView) pane.getChildren().get(1);
            Text cardNameText = (Text) pane.getChildren().get(2);

            // Reset rotations
            cardNameText.setRotate(0);
            cardBackImageView.setRotate(0);
            cardFrontImageView.setRotate(0);

            // Reset visibility of front and back images, and name text
            cardNameText.setVisible(false);
            cardBackImageView.setVisible(true);
            cardFrontImageView.setVisible(false);

            // Reset the rotation angle
            rotationAngle = 0;
        }

        generateShuffledCardNames();
        reshuffled = true;

        // Update the card names and setup tooltips
        for (int a = 0; a < NUM_CARDS; a++) {
            if (cardPanes[a].getChildren().get(2) instanceof Text cardNameText) {
                Matcher matcher = CARD_PATTERN.matcher(cardNames.get(a));
                if (matcher.matches() && !wilds.contains(cardNames.get(a))) {
                    String value = matcher.group("value");
                    String suit = matcher.group("suit");
                    cardNameText.setText(Card.getStyle(cardNames.get(a), value, suit).getText());
                    cardNameText.setStyle(Card.getStyle(cardNames.get(a), value, suit).getStyle());
                } else {
                    Text text = new Text(cardNames.get(a));
                    cardNameText.setText(getWildCardName(text).getText() + "\n \n" + "(Wild)");
                    cardNameText.setStyle(getWildCardName(text).getStyle());
                }
                makeCardTooltip(cardPanes[a], cardNameText.getText(), reshuffled);
            }
        }

        reshuffled = false;
        primaryStage.setScene(gameScene);
        primaryStage.setTitle("Game Scene");
    }

    private void switchToStart() {
        primaryStage.setScene(startScene);
        primaryStage.setTitle("Start Scene");
    }

    private void makeFlippableAndRotatable(Pane pane) {
        pane.setOnMouseClicked(event -> {
            Node front = null;
            Node back = null;
            Node text = null;
            if (event.getButton() == MouseButton.PRIMARY) {
                if (event.isShiftDown()) {
                    for (Node node : pane.getChildren()) {
                        if (node instanceof ImageView imageView) {
                            if (imageView.isVisible()) {
                                front = imageView;
                            } else {
                                back = imageView;
                            }
                        }

                        if (node instanceof Text text1) {
                            text = text1;
                        }
                    }
                    if (front != null && back != null && event.isStillSincePress()) {
                        front.setRotate(rotationAngle--);
                        back.setRotate(rotationAngle--);
                        pane.toFront();
                    }

                    if (text != null && event.isStillSincePress()) {
                        text.setRotate(rotationAngle--);
                    }
                } else if (event.isControlDown()) {
                    for (Node node : pane.getChildren()) {
                        if (node instanceof ImageView imageView) {
                            if (imageView.isVisible()) {
                                front = imageView;
                            } else {
                                back = imageView;
                            }
                        }

                        if (node instanceof Text text1) {
                            text = text1;
                        }
                    }
                    if (front != null && back != null && event.isStillSincePress()) {
                        front.setRotate(rotationAngle);
                        back.setRotate(rotationAngle);
                        pane.toFront();
                    }

                    if (text != null && event.isStillSincePress()) {
                        text.setRotate(rotationAngle);
                    }
                    rotationAngle = rotationAngle - 90;
                } else {
                    if (event.getClickCount() == 2) {
                        for (Node node : pane.getChildren()) {
                            if (node instanceof ImageView imageView) {
                                if (imageView.isVisible()) {
                                    front = imageView;
                                } else {
                                    back = imageView;
                                }
                            }

                            if (node instanceof Text text1) {
                                text = text1;
                            }
                        }
                        if (front != null && back != null && event.isStillSincePress()) {
                            front.setVisible(!front.isVisible());
                            back.setVisible(!back.isVisible());
                            pane.toFront();
                            if (text != null) {
                                text.setVisible(!front.isVisible() && !text.isVisible());
                            }
                        }
                    }
                }
            } else if (event.getButton() == MouseButton.SECONDARY) {
                if (event.isShiftDown()) {
                    for (Node node : pane.getChildren()) {
                        if (node instanceof ImageView imageView) {
                            if (imageView.isVisible()) {
                                front = imageView;
                            } else {
                                back = imageView;
                            }
                        }

                        if (node instanceof Text text1) {
                            text = text1;
                        }
                    }
                    if (front != null && back != null && event.isStillSincePress()) {
                        front.setRotate(rotationAngle++);
                        back.setRotate(rotationAngle++);
                        pane.toFront();
                    }

                    if (text != null && event.isStillSincePress()) {
                        text.setRotate(rotationAngle++);
                    }
                } else if (event.isControlDown()) {
                    for (Node node : pane.getChildren()) {
                        if (node instanceof ImageView imageView) {
                            if (imageView.isVisible()) {
                                front = imageView;
                            } else {
                                back = imageView;
                            }
                        }

                        if (node instanceof Text text1) {
                            text = text1;
                        }
                    }
                    if (front != null && back != null && event.isStillSincePress()) {
                        front.setRotate(rotationAngle);
                        back.setRotate(rotationAngle);
                        pane.toFront();
                    }

                    if (text != null && event.isStillSincePress()) {
                        text.setRotate(rotationAngle);
                    }
                    rotationAngle = rotationAngle + 90;
                } else {
                    if (event.getClickCount() == 2) {
                        rotationAngle = 0;
                        for (Node node : pane.getChildren()) {
                            if (node instanceof ImageView imageView) {
                                if (imageView.isVisible()) {
                                    front = imageView;
                                } else {
                                    back = imageView;
                                }
                            }

                            if (node instanceof Text text1) {
                                text = text1;
                            }
                        }
                        if (front != null && back != null && event.isStillSincePress()) {
                            front.setRotate(rotationAngle);
                            back.setRotate(rotationAngle);
                            pane.toFront();
                        }

                        if (text != null && event.isStillSincePress()) {
                            text.setRotate(rotationAngle);
                        }
                    }
                }
            }
        });
    }

    public static void generateCardTooltips(String name) {
        if (!cardTooltips.containsValue(name)) {
            if (!wilds.contains(name)) {
                cardTooltips.put(name,
                        name.replaceAll("of", "\n"));
            } else {
                cardTooltips.put(name, name);
            }
        }
    }

    private void addCardNames() {
        cardNames.addAll(wilds); //Add all wilds

        for (String suit : suits) {
            for (String value : values) {
                cardNames.add(value + " of " + suit); //build and add cards with suit and rank
            }
        }
    }

    private static void generateShuffledCardNames() {
        // Shuffle the cards
        Collections.shuffle(cardNames);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public record PokerChips(String color, int value) {
    }
}