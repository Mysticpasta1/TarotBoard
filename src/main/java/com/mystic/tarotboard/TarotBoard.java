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
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
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

public class TarotBoard extends Application {

    private static final int NUM_CARDS = 1058;
    private static final double CARD_WIDTH = 150;
    private static final double CARD_HEIGHT = 200;
    private static final String[] colors = {"firebrick", "orange", "goldenrod", "yellow", "yellowgreen", "green", "cyan", "blue", "darkorchid", "purple", "gray", "darkgray", "white"};
    private static final int NUM_CHIPS = 200;
    private int rotationAngle = 0;
    private final Map<String, String> cardTooltips = new HashMap<>();
    private static Stage primaryStage;
    private Scene startScene;
    private static Scene gameScene;

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
        singlePlayer.setOnAction(event -> switchToGame());

        startLayout.setAlignment(Pos.CENTER);
        Button quitButton = new Button("Quit");
        quitButton.setOnAction(event -> primaryStage.close());
        startScene = new Scene(startLayout, screenBounds.getWidth(), screenBounds.getHeight());

        Button howToPlayButton = new Button("How To Play: Tarot Poker");
        howToPlayButton.setOnAction(event -> displayHowToPlayDialog());

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

        generateTooltips();

        // Create and position cards in the stack
        for (int i = 0; i < NUM_CARDS; i++) {
            // Load the custom images for the card fronts and backs
            Image cardFrontImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/mystic/tarotboard/assets/card_front.png")));
            Image cardBackImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/mystic/tarotboard/assets/card_back.png")));
            // Create a stack pane to overlay the front and back images
            StackPane cardPane = new StackPane();

            Text cardNameText = new Text(cardNames[i]);
            cardNameText.setStyle("-fx-font-size: 15pt; -fx-fill: lightblue;");
            cardNameText.setBoundsType(TextBoundsType.VISUAL); // Use visual bounds to get accurate text size
            cardNameText.setWrappingWidth(CARD_WIDTH); // Use the card width for centering
            cardNameText.setTextAlignment(TextAlignment.CENTER);
            cardNameText.setTranslateY(0);
            cardNameText.setVisible(false);

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

            makeTooltip(cardPane, cardNames[i]);

            cardPane.getChildren().addAll(cardBackImageView, cardFrontImageView, cardNameText);
            cardPane.setTranslateX(50);
            cardPane.setTranslateY(50);

            // Make the card movable
            makeDraggable(cardPane);
            makeFlippableAndRotatable(cardPane);

            // Add the card pane to the array
            cardPanes[i] = cardPane;

            // Add the card to the root pane
            gameRoot.getChildren().add(cardPane);
        }

        List<PokerChips> pokerChips = new ArrayList<>();

        for (String color : colors) {
            for (int i = 0; i < NUM_CHIPS; i++) {
                pokerChips.add(new PokerChips(color, i));
            }
        }

        double chipRadius = 50;
        double spacing = 5;
        StackPane[] chipPanes = new StackPane[pokerChips.size()];

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

                // Add the card pane to the array
                chipPanes[i * chipsOfColor.size() + j] = chipPane;

                // Add the card to the root pane
                gameRoot.getChildren().add(chipPane);
            }
        }

        Button reshuffleCards = getReshuffleCards(cardPanes);

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

        HBox handCardPane = new HBox(10); // Horizontal box for the hand
        handCardPane.setPadding(new Insets(10));

        HBox handChipPane = new HBox(10); // Horizontal box for the hand
        handChipPane.setPadding(new Insets(10));


        for (int i = 0; i < NUM_CARDS; i++) {
            Pane cardPane = cardPanes[i]; // Create a pane for each card
            cardPane.setOnDragDetected(event -> {
                Dragboard dragboard = cardPane.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString("Cards"); // You can put any string here to identify the card
                dragboard.setContent(content);
                event.consume();
            });
            handCardPane.getChildren().add(cardPane); // Add the card pane to the hand
        }

        for (int i = 0; i < (NUM_CHIPS * colors.length); i++) {
            Pane chipPane = chipPanes[i];
            chipPane.setOnDragDetected(event -> {
                Dragboard dragboard = chipPane.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString("Chips"); // You can put any string here to identify the card
                dragboard.setContent(content);
                event.consume();
            });
            handChipPane.getChildren().add(chipPane);
        }

        Pane pane = new Pane();
        pane.getChildren().addAll(handCardPane, handChipPane);
        pane.layoutXProperty().bind(gameScene.widthProperty().subtract(pane.widthProperty()).subtract(500));
        pane.layoutYProperty().bind(gameScene.heightProperty().subtract(pane.heightProperty()).subtract(50));
        gameRoot.getChildren().addAll(pane);


        resetChips.layoutXProperty().bind(gameScene.widthProperty().subtract(resetChips.widthProperty()).subtract(50));
        resetChips.layoutYProperty().bind(gameScene.heightProperty().subtract(resetChips.heightProperty()).subtract(150));

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

            // Update the card names
            for (int a = 0; a < NUM_CARDS; a++) {
                Text cardNameText = (Text) cardPanes[a].getChildren().get(2);
                cardNameText.setText(cardNames2[a]);
            }
        });
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

    private void makeTooltip(Pane pane, String cardName) {
        String hoverText = cardTooltips.get(cardName);
        if (hoverText != null) {
            Tooltip tooltip = new Tooltip(hoverText);
            pane.setOnMouseEntered(event -> Tooltip.install(pane, tooltip));
            pane.setOnMouseExited(event -> Tooltip.uninstall(pane, tooltip));
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
        Text introText = new Text("How To Play: Tarot Poker:\n\n");
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

    private void generateTooltips() {
        cardTooltips.put("Joker (Will)", "Wild Card, Can Be Matched With Anything Or, Added On Top Of Anything");
        cardTooltips.put("Blessings of Heart (Will)", "Add 5 Red Chips To Your Hand");
        cardTooltips.put("Follow of Soul (Will)", "Add 5 Blue Chips To Your Hand");
        cardTooltips.put("Call of Light (Will))", "Add 2 Yellow Chips To Your Hand");
        cardTooltips.put("Whisper of Dark (Will))", "Add 3 Dark Gray Chips To Your Hand");
        cardTooltips.put("Judgement (Will)", "Pull Three Cards, Choose 1 For You, The Others Discards");
        cardTooltips.put("Chorus (Will)", "Add 4 Purple Chips To Your Hand");
        cardTooltips.put("Dawn of Death (Will)", "Remove All Chips From 1 Person");
        cardTooltips.put("Night of Wrath (Will)", "Remove 2 Cards From Each Hand And The Draw Pile, Discard All");
        cardTooltips.put("Voice (Will)", "Add A Card To Your Hand");
        cardTooltips.put("Voices (Will)", "Add A Card To Everyone But Your Hand");
        cardTooltips.put("Mother (Will)", "Add 5 Red Chips To Everyone's Hand");
        cardTooltips.put("Father (Will)", "Add 5 Red Chips To Everyone's Hand");
        cardTooltips.put("Brother (Will)", "Add 5 Red Chips To Everyone's Hand");
        cardTooltips.put("Sister (Will)", "Add 5 Red Chips To Everyone's Hand");
        cardTooltips.put("Duality (Will)", "Pull Two Card, Give Yourself One And One To A Friend Or Foe");
        cardTooltips.put("Husband (Will)", "Remove 5 Red Chips From Everyone's Hand");
        cardTooltips.put("Wife (Will)", "Remove 5 Red Chips To Another Player's Hand");
        cardTooltips.put("Progeny (Will)", "Remove 15 Red Chips From Your Hand");
        cardTooltips.put("Corridor (Will)", "Draw 10, Discard 5");
        cardTooltips.put("Field (Will)", "Draw 20, Discard 10");
        cardTooltips.put("Intellect (Will)", "Discard 10");
        cardTooltips.put("Brawn (Will)", "Discard 20");
        cardTooltips.put("Hope (Will)", "Add One Extra Card To Your Hand and One Face Up In The Middle");
        cardTooltips.put("Despair (Will)", "Remove 3 Cards From A Person Of Your Choosing");
        cardTooltips.put("Past (Will)", "Add 3 Cards From The Draw To Your Hand");
        cardTooltips.put("Present (Will)", "Add 2 Cards From The Draw To Your Hand");
        cardTooltips.put("Future (Will)", "Remove 3 Cards From Your Hand And Discard");
        cardTooltips.put("Gate (Will)", "Remove 3 Red Chips (If You Have Some Chips) From Your Hand");
        cardTooltips.put("Sign (Will)", "Draw 2 Cards And Discard 2 Other Cards");
        cardTooltips.put("Ruin (Will)", "Reshuffle All Cards, All Player Draw 5");
        cardTooltips.put("Snow (Will)", "Add 3 White Chips To Everyone's Hand");
        cardTooltips.put("Rain (Will)", "Add 3 Cyan Chips To Everyone's Hand");
        cardTooltips.put("Tempest (Will)", "Remove 2 Cards And 10 Red Chips From Your Hand");
        cardTooltips.put("Lovers (Will)", "Add 2 Cards And 10 Red Chips To Your Hand");
    }

    private String[] generateShuffledCardNames() {
        ObservableList<String> cardNames = FXCollections.observableArrayList();
        String[] suits = {
                "Arcs", "Arrows", "Clouds", "Clovers", "Comets", "Crescents", "Crosses", "Crowns", "Diamonds", "Embers", "Eyes",
                "Gears", "Glyphs", "Flames", "Flowers", "Hearts", "Keys", "Locks", "Leaves", "Mountains", "Points", "Scrolls",
                "Shells", "Shields", "Spades", "Spirals", "Stars", "Suns", "Swords", "Tridents", "Trees", "Waves"
        };
        String[] values = {
                "(0) Hold, (1) Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "(11) Jack", "(12) Queen", "(13) King", "(14) Nomad",
                "(15) Prince", "(16) Rune", "(17) Fable", "(18) Sorceress", "(19) Utopia", "(20) Wizard", "(21) Titan",
                "(22) Baron", "(23) Illusionist", "(24) Oracle", "(25) Magician", "(26) Luminary", "(27) Eclipse",
                "(28) Celestial", "(29) Duke", "(30) Genesis", "(31) Zephyr", "(32) Vesper"
        };

        for (String suit : suits) {
            for (String value : values) {
                cardNames.add(value + " of " + suit);
            }
        }

        cardNames.add("Joker (Will)");
        cardNames.add("Blessings of Heart (Will)");
        cardNames.add("Follow of Soul (Will)");
        cardNames.add("Call of Light (Will))");
        cardNames.add("Whisper of Dark (Will))");
        cardNames.add("Judgement (Will)");
        cardNames.add("Chorus (Will)");
        cardNames.add("Dawn of Death (Will)");
        cardNames.add("Night of Wrath (Will)");
        cardNames.add("Voice (Will)");
        cardNames.add("Voices (Will)");
        cardNames.add("Mother (Will)");
        cardNames.add("Father (Will)");
        cardNames.add("Brother (Will)");
        cardNames.add("Sister (Will)");
        cardNames.add("Duality (Will)");
        cardNames.add("Husband (Will)");
        cardNames.add("Wife (Will)");
        cardNames.add("Progeny (Will)");
        cardNames.add("Corridor (Will)");
        cardNames.add("Field (Will)");
        cardNames.add("Intellect (Will)");
        cardNames.add("Brawn (Will)");
        cardNames.add("Hope (Will)");
        cardNames.add("Despair (Will)");
        cardNames.add("Past (Will)");
        cardNames.add("Present (Will)");
        cardNames.add("Future (Will)");
        cardNames.add("Gate (Will)");
        cardNames.add("Sign (Will)");
        cardNames.add("Ruin (Will)");
        cardNames.add("Snow (Will)");
        cardNames.add("Rain (Will)");
        cardNames.add("Tempest (Will)");
        cardNames.add("Lovers (Will)");

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