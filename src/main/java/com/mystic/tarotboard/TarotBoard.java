package com.mystic.tarotboard;

import com.traneptora.jxlatte.JXLDecoder;
import com.traneptora.jxlatte.JXLOptions;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TarotBoard extends Application {

    private static final Pattern CARD_PATTERN = Pattern.compile("^(?<value>[^ ]+) of (?<suit>.+)$");
    private static final int NUM_CARDS = 1091;
    private static final double CARD_WIDTH = 150;
    private static final double CARD_HEIGHT = 200;
    private static final String[] colors = {"firebrick", "orange", "goldenrod", "yellow", "yellowgreen", "green", "cyan", "lightblue", "blue", "darkorchid", "purple", "gray", "darkgray", "white"};
    private static final int NUM_CHIPS = 250;
    private static final int TOTAL_CHIPS = colors.length * NUM_CHIPS;
    private int rotationAngle = 0;
    private final Map<String, String> cardTooltips = new HashMap<>();
    private final Map<String, String> chipTooltips = new HashMap<>();
    private static Stage primaryStage;
    private Scene startScene;
    private static Scene gameScene;
    private final List<PokerChips> pokerChips = new ArrayList<>();
    private final StackPane[] chipPanes = new StackPane[TOTAL_CHIPS];
    private boolean reshuffled = false;
    private final String[] suits = {
            "Arcs", "Arrows", "Clouds", "Clovers", "Comets", "Crescents", "Crosses",
            "Crowns", "Diamonds", "Embers", "Eyes", "Gears", "Glyphs", "Flames", "Flowers",
            "Hearts", "Keys", "Locks", "Leaves", "Mountains", "Points", "Scrolls", "Shells",
            "Shields", "Spades", "Spirals", "Stars", "Suns", "Swords", "Tridents", "Trees", "Waves"
    };
    private final String[] values = {
            "Hold", "Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10",
            "Jack", "Queen", "King", "Nomad", "Prince",
            "Rune", "Fable", "Sorceress", "Utopia", "Wizard",
            "Titan", "Baron", "Illusionist", "Oracle", "Magician",
            "Luminary", "Eclipse", "Celestial", "Duke", "Genesis",
            "Zephyr", "Vesper"
    };

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

        // Create the start scene
        VBox startLayout = new VBox(10);
        Button singlePlayer = new Button("Single Player");
        singlePlayer.setStyle("-fx-font-size: 20pt;");
        singlePlayer.setOnAction(event -> switchToGame());
        Button quitButton = new Button("Quit");
        quitButton.setStyle("-fx-font-size: 20pt;");
        quitButton.setOnAction(event -> primaryStage.close());
        Button howToPlayButton = new Button("How To Play: Tarot Poker IRL");
        howToPlayButton.setStyle("-fx-font-size: 20pt;");
        howToPlayButton.setOnAction(event -> displayHowToPlayDialog());
        startLayout.setAlignment(Pos.CENTER);
        startScene = new Scene(startLayout, screenBounds.getWidth(), screenBounds.getHeight());
        startLayout.getChildren().addAll(singlePlayer, howToPlayButton, quitButton);
        // Create the game scene
        Button backButton3 = new Button("Back to Start");
        backButton3.setOnAction(event -> switchToStart());
        // Position the button as needed
        backButton3.layoutXProperty().bind(gameScene.widthProperty().subtract(backButton3.widthProperty()).subtract(50));
        backButton3.layoutYProperty().bind(gameScene.heightProperty().subtract(backButton3.heightProperty()).subtract(50));

        String[] cardNames = generateShuffledCardNames();
        StackPane[] cardPanes = new StackPane[NUM_CARDS]; // Array to store card panes

        startLayout.setBackground(background);

        // Create and position cards in the stack
        for (int i = 0; i < NUM_CARDS; i++) {
            Text cardNameText = new Text("");

            //I want to use negative card value for later so using PI for an unknown value
            double rank = Math.PI;

            // Load the custom images for the card fronts and backs
            Image cardFrontImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/mystic/tarotboard/assets/card_front.png")));
            Image cardBackImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/mystic/tarotboard/assets/card_back.png")));
            // Create a stack pane to overlay the front and back images
            StackPane cardPane = new StackPane();

            Matcher matcher = CARD_PATTERN.matcher(cardNames[i]);
            if (matcher.matches()) {
                String value = matcher.group("value");
                String suit = matcher.group("suit");
                Card card = new Card(value, suit, CARD_WIDTH, CARD_HEIGHT);

                rank = card.getRank();

                cardNameText = card.getCardName();

                StackPane pane = card.getCardPane();

                pane.setTranslateX(50);
                pane.setTranslateY(50);

                // Make the card movable
                makeDraggable(pane);
                makeFlippableAndRotatable(pane);

                cardPanes[i] = pane;
            } else {
                cardNameText = getWildCardName(cardNames[i]);

                // Initially show the back of the card
                ImageView cardBackImageView = new ImageView(cardBackImage);
                cardBackImageView.setFitWidth(CARD_WIDTH);
                cardBackImageView.setFitHeight(CARD_HEIGHT);
                cardBackImageView.setVisible(true);

                // Create an image view for the front of the card (hidden initially)
                ImageView cardFrontImageView = new ImageView(cardFrontImage);
                cardFrontImageView.setFitWidth(CARD_WIDTH);
                cardFrontImageView.setFitHeight(CARD_HEIGHT);
                cardFrontImageView.setVisible(false);

                cardPane.getChildren().addAll(cardBackImageView, cardFrontImageView, cardNameText);

                cardPane.setTranslateX(50);
                cardPane.setTranslateY(50);

                // Make the card movable
                makeDraggable(cardPane);
                makeFlippableAndRotatable(cardPane);

                cardPanes[i] = cardPane;
            }

            // Add the card pane to the array
            reshuffled = false;
            generateCardTooltips(cardNameText.getText(), rank);
            makeCardTooltip(cardPane, cardNameText.getText(), reshuffled);
            // Add the card to the root pane
            gameRoot.getChildren().add(cardPane);
        }


        for (String color : colors) {
            for (int i = 0; i < NUM_CHIPS; i++) {
                pokerChips.add(new PokerChips(color, i));
            }
        }

        generateChipTooltips();

        double chipRadius = 50;
        double spacing = 5;

        for (int i = 0; i < colors.length; i++) {
            String color = colors[i];
            List<PokerChips> chipsOfColor = pokerChips.stream().filter(chip -> chip.color().equals(color)).toList();

            BufferedImage bwFrontImage = loadImage("com/mystic/tarotboard/assets/front_poker_chips.jxl");
            BufferedImage bwBackImage = loadImage("com/mystic/tarotboard/assets/back_poker_chips.jxl");

            ColorAdjust colorAdjust = new ColorAdjust(0, 0, 0.5, 0);

            for (int j = 0; j < chipsOfColor.size(); j++) {
                PokerChips chip = chipsOfColor.get(j);
                Color color1 = Color.valueOf(chip.color());

                Circle circle = new Circle(chipRadius / 2);
                circle.setCenterX(25.0);
                circle.setCenterY(25.0);

                // Apply color adjustment
                Blend blend = new Blend(BlendMode.MULTIPLY);
                blend.setBottomInput(new ColorInput(0, 0, chipRadius, chipRadius, color1));
                blend.setTopInput(colorAdjust);

                StackPane chipPane = new StackPane();

                // Create a new ImageView for each chip to apply different colors
                BufferedImage resizedBackImage = resizeImage(bwBackImage);
                ImageView chipBackImageView = new ImageView(SwingFXUtils.toFXImage(resizedBackImage, null));
                chipBackImageView.setFitWidth(chipRadius);
                chipBackImageView.setFitHeight(chipRadius);
                chipBackImageView.setVisible(false);
                chipBackImageView.setEffect(blend);

                // Create an image view for the front of the chip
                BufferedImage resizedFrontImage = resizeImage(bwFrontImage);
                ImageView chipFrontImageView = new ImageView(SwingFXUtils.toFXImage(resizedFrontImage, null));
                chipFrontImageView.setFitWidth(chipRadius);
                chipFrontImageView.setFitHeight(chipRadius);
                chipFrontImageView.setVisible(true);
                chipFrontImageView.setEffect(blend);

                chipPane.getChildren().addAll(chipFrontImageView, chipBackImageView);

                chipPane.setClip(circle);
                chipPane.translateXProperty().bind(gameScene.widthProperty().subtract(200).subtract(j));
                chipPane.setTranslateY(((gameScene.getHeight() / 8) + (chipRadius + spacing) * i) - 75);

                makeDraggable(chipPane);
                makeFlippableAndRotatable(chipPane);
                makeChipTooltip(chipPane, i, j, chipsOfColor);

                // Add the card pane to the array
                chipPanes[i * chipsOfColor.size() + j] = chipPane;

                // Add the card to the root pane
                gameRoot.getChildren().add(chipPane);
            }
        }

        Button resetChips = getResetButton(chipRadius, spacing);
        resetChips.layoutXProperty().bind(gameScene.widthProperty().subtract(resetChips.widthProperty()).subtract(50));
        resetChips.layoutYProperty().bind(gameScene.heightProperty().subtract(resetChips.heightProperty()).subtract(150));

        Button reshuffleCards = getReshuffleCards(cardPanes);
        reshuffleCards.layoutXProperty().bind(gameScene.widthProperty().subtract(reshuffleCards.widthProperty()).subtract(50));
        reshuffleCards.layoutYProperty().bind(gameScene.heightProperty().subtract(reshuffleCards.heightProperty()).subtract(100));

        gameRoot.getChildren().addAll(resetChips, reshuffleCards, backButton3);

        primaryStage.setScene(startScene);
        primaryStage.setX(screenBounds.getMinX());
        primaryStage.setY(screenBounds.getMinY());
        primaryStage.setWidth(screenBounds.getWidth());
        primaryStage.setHeight(screenBounds.getHeight());
        primaryStage.show();
    }

    private static Text getWildCardName(String cardNames) {
        Text cardNameText = new Text(cardNames);
        cardNameText.setStyle("-fx-font-size: 15pt; -fx-fill: purple;");
        cardNameText.setBoundsType(TextBoundsType.VISUAL); // Use visual bounds to get accurate text size
        cardNameText.setWrappingWidth(CARD_WIDTH); // Use the card width for centering
        cardNameText.setTextAlignment(TextAlignment.CENTER);
        cardNameText.setTranslateY(0);
        cardNameText.setVisible(false);
        return cardNameText;
    }

    private Button getResetButton(double chipRadius, double spacing) {
        Button resetChips = new Button("Reset Chips");
        resetChips.setOnAction(event -> {
            for (int i = 0; i < colors.length; i++) {
                String color = colors[i];
                List<PokerChips> chipsOfColor = pokerChips.stream().filter(chip -> chip.color().equals(color)).toList();
                for (int j = 0; j < chipsOfColor.size(); j++) {
                    StackPane pane1 = chipPanes[i * chipsOfColor.size() + j];
                    ImageView chipBackImageView = (ImageView) pane1.getChildren().get(0);
                    ImageView chipFrontImageView = (ImageView) pane1.getChildren().get(1);
                    chipBackImageView.setRotate(0);
                    chipFrontImageView.setRotate(0);
                    chipBackImageView.setVisible(true);
                    chipFrontImageView.setVisible(false);
                    pane1.translateXProperty().bind(gameScene.widthProperty().subtract(200).subtract(j));
                    pane1.setTranslateY(((gameScene.getHeight() / 8) + (chipRadius + spacing) * i) - 75);
                    pane1.toFront();
                }
            }
        });
        return resetChips;
    }

    private void makeDraggable(StackPane pane) {
        final double[] dragDeltaX = new double[1];
        final double[] dragDeltaY = new double[1];

        pane.setOnMousePressed(event -> {
            dragDeltaX[0] = event.getSceneX() - pane.getTranslateX();
            dragDeltaY[0] = event.getSceneY() - pane.getTranslateY();
            pane.translateXProperty().unbind(); // Unbind translateX while dragging
        });

        pane.setOnMouseDragged(event -> {
            pane.setTranslateX(event.getSceneX() - dragDeltaX[0]);
            pane.setTranslateY(event.getSceneY() - dragDeltaY[0]);
            pane.toFront();
        });
    }

    private Button getReshuffleCards(StackPane[] cardPanes) {
        Button reshuffleCards = new Button("Reshuffle Cards");
        reshuffleCards.setOnAction(event -> {
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
                pane.setTranslateY(50);
                pane.setTranslateX(50);
            }

            String[] cardNames2 = generateShuffledCardNames();
            reshuffled = true;

            // Update the card names
            for (int a = 0; a < NUM_CARDS; a++) {
                Text cardNameText = (Text) cardPanes[a].getChildren().get(2);
                cardNameText.setText(cardNames2[a]);
                makeCardTooltip(cardPanes[a], cardNameText.getText(), reshuffled);
            }
        });
        reshuffled = false;
        return reshuffleCards;
    }

    private BufferedImage resizeImage(BufferedImage originalImage) {
        BufferedImage resizedImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, 50, 50, null);
        graphics2D.dispose();
        return resizedImage;
    }

    private BufferedImage loadImage(String name) {
        BufferedImage temp;
        if (name.endsWith(".jxl")) {
            var is = Objects.requireNonNull(getClass().getResourceAsStream("/" + name));
            var options = new JXLOptions();
            options.hdr = JXLOptions.HDR_OFF;
            options.threads = 2;
            try {
                temp = new JXLDecoder((is), options).decode().asBufferedImage();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                temp = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/" + name)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return temp;
    }

    private void makeCardTooltip(StackPane pane, String text, boolean reshuffled) {
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
            pane.setOnMouseExited(event -> tooltip.hide());
        }
    }

    private void makeChipTooltip(Pane pane, int i, int j, List<PokerChips> colorOfChips) {
        String hoverText = chipTooltips.get(Integer.toString(i * colorOfChips.size() + j));
        Tooltip tooltip = new Tooltip(hoverText);
        tooltip.setStyle("-fx-font-size: 18pt;");
        Tooltip.install(pane, tooltip);

        // Show tooltip on mouse enter
        pane.setOnMouseEntered(event -> {
            if (pane.getChildren().get(0).isVisible()) {
                tooltip.setText(hoverText);
                tooltip.show(pane, event.getScreenX(), event.getScreenY() + 5); // Offset tooltip position
            } else {
                tooltip.setText("-" + hoverText);
            }
        });

        // Hide tooltip on mouse exit
        pane.setOnMouseExited(event -> tooltip.hide());
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

    private static void switchToGame() {
        primaryStage.setScene(gameScene);
        primaryStage.setTitle("Game Scene");
    }

    private void switchToStart() {
        primaryStage.setScene(startScene);
        primaryStage.setTitle("Start Scene");
    }

    private void displayHowToPlayDialog() {
        // Create a new stage for the instructions dialog
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("How To Play");

        // Create a TextFlow to hold the instructions text
        TextFlow instructionsText = new TextFlow();
        instructionsText.setPrefWidth(300);

        // Add the instructions text to the TextFlow
        Text introText = new Text("How To Play: Tarot Poker IRL\n\n");
        introText.setStyle("-fx-font-weight: bold;");
        Text bodyText = new Text("""
                Everyone Starts With 5 Cards and 5 Red Chips
                Place Cards In The Middle Like Uno
                Every Card Discarded Is Worth 1 Red Token
                If You Run Out Of Cards, Get 3 More
                Play Until There Are No More Cards
                Follow All 'Will' Cards Instructions Via The Tooltip
                Each Color Goes Up By 10 In Order Of Color (i.e. 1 Orange Chips Equals 10 Red Chips
                Player With The Most Chips Wins!""");
        instructionsText.getChildren().addAll(introText, bodyText);

        // Create a button to close the dialog
        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> dialogStage.close());

        // Add the TextFlow and the Close button to a VBox
        VBox dialogContent = new VBox(10);
        dialogContent.getChildren().addAll(instructionsText, closeButton);
        dialogContent.setPadding(new Insets(10, 10, 10, 10));

        // Create a scene for the dialog with the VBox as its root
        Scene dialogScene = new Scene(dialogContent);
        dialogStage.setScene(dialogScene);

        // Show the dialog
        dialogStage.show();
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

    private void generateCardTooltips(String name, double rank) {
        cardTooltips.put("Joker", "Wild Card");
        cardTooltips.put("Blessings of Heart", "Wild Card");
        cardTooltips.put("Follow of Soul", "Wild Card");
        cardTooltips.put("Call of Light", "Wild Card");
        cardTooltips.put("Whisper of Dark", "Wild Card");
        cardTooltips.put("Judgement", "Wild Card");
        cardTooltips.put("Chorus", "Wild Card");
        cardTooltips.put("Dawn of Death", "Wild Card");
        cardTooltips.put("Night of Wrath", "Wild Card");
        cardTooltips.put("Voice", "Wild Card");
        cardTooltips.put("Voices", "Wild Card");
        cardTooltips.put("Mother", "Wild Card");
        cardTooltips.put("Father", "Wild Card");
        cardTooltips.put("Brother", "Wild Card");
        cardTooltips.put("Sister", "Wild Card");
        cardTooltips.put("Duality", "Wild Card");
        cardTooltips.put("Husband", "Wild Card");
        cardTooltips.put("Wife", "Wild Card");
        cardTooltips.put("Progeny", "Wild Card");
        cardTooltips.put("Corridor", "Wild Card");
        cardTooltips.put("Field", "Wild Card");
        cardTooltips.put("Intellect", "Wild Card");
        cardTooltips.put("Brawn", "Wild Card");
        cardTooltips.put("Hope", "Wild Card");
        cardTooltips.put("Despair", "Wild Card");
        cardTooltips.put("Past", "Wild Card");
        cardTooltips.put("Present", "Wild Card");
        cardTooltips.put("Future", "Wild Card");
        cardTooltips.put("Gate", "Wild Card");
        cardTooltips.put("Sign", "Wild Card");
        cardTooltips.put("Ruin", "Wild Card");
        cardTooltips.put("Snow", "Wild Card");
        cardTooltips.put("Rain", "Wild Card");
        cardTooltips.put("Tempest", "Wild Card");
        cardTooltips.put("Lovers", "Wild Card");

        if (!cardTooltips.containsValue(name)) {
            if(rank != Math.PI) {
                cardTooltips.put(name,
                        name.replaceAll("of", "\n") +
                                "\n" + rank);
            } else {
                cardTooltips.put(name,
                        name.replaceAll("of", "\n"));
            }
        }
    }

    private String[] generateShuffledCardNames() {
        ObservableList<String> cardNames = FXCollections.observableArrayList();

        for (String suit : suits) {
            for (String value : values) {
                cardNames.add(value + " of " + suit);
            }
        }

        cardNames.add("Joker");
        cardNames.add("Blessings of Heart");
        cardNames.add("Follow of Soul");
        cardNames.add("Call of Light ");
        cardNames.add("Whisper of Dark ");
        cardNames.add("Judgement");
        cardNames.add("Chorus");
        cardNames.add("Dawn of Death");
        cardNames.add("Night of Wrath");
        cardNames.add("Voice");
        cardNames.add("Voices");
        cardNames.add("Mother");
        cardNames.add("Father");
        cardNames.add("Brother");
        cardNames.add("Sister");
        cardNames.add("Duality");
        cardNames.add("Husband");
        cardNames.add("Wife");
        cardNames.add("Progeny");
        cardNames.add("Corridor");
        cardNames.add("Field");
        cardNames.add("Intellect");
        cardNames.add("Brawn");
        cardNames.add("Hope");
        cardNames.add("Despair");
        cardNames.add("Past");
        cardNames.add("Present");
        cardNames.add("Future");
        cardNames.add("Gate");
        cardNames.add("Sign");
        cardNames.add("Ruin");
        cardNames.add("Snow");
        cardNames.add("Rain");
        cardNames.add("Tempest");
        cardNames.add("Lovers");

        // Shuffle the cards
        Collections.shuffle(cardNames);

        return cardNames.toArray(new String[0]);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public record PokerChips(String color, int value) {
    }
}