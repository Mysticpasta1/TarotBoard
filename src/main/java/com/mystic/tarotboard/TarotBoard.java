package com.mystic.tarotboard;

import com.traneptora.jxlatte.JXLDecoder;
import com.traneptora.jxlatte.JXLOptions;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import javafx.stage.Screen;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TarotBoard extends Application {

    private static final int NUM_CARDS = 1024;
    private static final double CARD_WIDTH = 150;
    private static final double CARD_HEIGHT = 200;
    private static final String[] colors = {"firebrick", "orange", "goldenrod", "yellow", "yellowgreen", "green", "cyan", "blue", "darkorchid", "purple", "gray", "darkgray", "white"};
    private static final int NUM_CHIPS = 200;
    private int rotationAngle = 0;

    @Override
    public void start(Stage primaryStage) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        Pane root = new Pane();
        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
        String[] cardNames = generateShuffledCardNames();

        Image backgroundImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/background_image.png")));
        BackgroundSize backgroundSize = new BackgroundSize(scene.getWidth(), scene.getHeight(), false, false, true, false);
        BackgroundImage background = new BackgroundImage(backgroundImage, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.CENTER, backgroundSize);
        root.setBackground(new Background(background));

        StackPane[] cardPanes = new StackPane[NUM_CARDS]; // Array to store card panes

        // Create and position cards in the stack
        for (int i = 0; i < NUM_CARDS; i++) {
            // Load the custom images for the card fronts and backs
            Image cardFrontImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/card_front.png")));
            Image cardBackImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/card_back.png")));
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

            cardPane.getChildren().addAll(cardBackImageView, cardFrontImageView, cardNameText);
            cardPane.setTranslateX(50);
            cardPane.setTranslateY(50);

            // Make the card movable
            makeDraggable(cardPane);
            makeFlippableAndRotatable(cardPane);

            // Add the card pane to the array
            cardPanes[i] = cardPane;

            // Add the card to the root pane
            root.getChildren().add(cardPane);
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
            List<PokerChips> chipsOfColor = pokerChips.stream().filter(chip -> chip.getColor().equals(color)).toList();

            BufferedImage bwFrontImage = loadImage("front_poker_chips.jxl");
            BufferedImage bwBackImage = loadImage("back_poker_chips.jxl");

            ColorAdjust colorAdjust = new ColorAdjust(0, 0, 0.5, 0);

            for (int j = 0; j < chipsOfColor.size(); j++) {
                PokerChips chip = chipsOfColor.get(j);
                Color color1 = Color.valueOf(chip.getColor());

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
                chipPane.translateXProperty().bind(scene.widthProperty().subtract(200).subtract(j));
                chipPane.setTranslateY(((scene.getHeight() / 8) + (chipRadius + spacing) * i) - 75);

                makeDraggable(chipPane);
                makeFlippableAndRotatable(chipPane);

                // Add the card pane to the array
                chipPanes[i * chipsOfColor.size() + j] = chipPane;

                // Add the card to the root pane
                root.getChildren().add(chipPane);
            }
        }

        Button reshuffleButton = new Button("Reshuffle");
        reshuffleButton.setOnAction(event -> {
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

            for (int i = 0; i < colors.length; i++) {
                String color = colors[i];
                List<PokerChips> chipsOfColor = pokerChips.stream().filter(chip -> chip.getColor().equals(color)).toList();
                for (int j = 0; j < chipsOfColor.size(); j++) {
                    StackPane pane1 = chipPanes[i * chipsOfColor.size() + j];
                    ImageView chipBackImageView = (ImageView) pane1.getChildren().get(0);
                    ImageView chipFrontImageView = (ImageView) pane1.getChildren().get(1);
                    chipBackImageView.setRotate(0);
                    chipFrontImageView.setRotate(0);
                    chipBackImageView.setVisible(true);
                    chipFrontImageView.setVisible(false);
                    pane1.translateXProperty().bind(scene.widthProperty().subtract(200).subtract(j));
                    pane1.setTranslateY(((scene.getHeight() / 8) + (chipRadius + spacing) * i) - 75);
                    pane1.toFront();
                }
            }

            String[] cardNames2 = generateShuffledCardNames();

            // Update the card names
            for (int a = 0; a < NUM_CARDS; a++) {
                Text cardNameText = (Text) cardPanes[a].getChildren().get(2);
                cardNameText.setText(cardNames2[a]);
            }
        });

        reshuffleButton.layoutXProperty().bind(scene.widthProperty().subtract(reshuffleButton.widthProperty()).subtract(50));
        reshuffleButton.layoutYProperty().bind(scene.heightProperty().subtract(reshuffleButton.heightProperty()).subtract(50));

        root.getChildren().add(reshuffleButton);

        primaryStage.setScene(scene);
        primaryStage.setX(screenBounds.getMinX());
        primaryStage.setY(screenBounds.getMinY());
        primaryStage.setWidth(screenBounds.getWidth());
        primaryStage.setHeight(screenBounds.getHeight());
        primaryStage.show();
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
        if(name.endsWith(".jxl")) {
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

    private String[] generateShuffledCardNames() {
        ObservableList<String> cardNames = FXCollections.observableArrayList();
        String[] suits = {
                "Arcs", "Arrows", "Clouds", "Clovers", "Comets", "Crescents", "Crosses", "Crowns", "Diamonds", "Embers", "Eyes",
                "Gears", "Glyphs", "Flames", "Flowers", "Hearts", "Keys", "Locks", "Leaves", "Mountains", "Points", "Scrolls",
                "Shells", "Shields", "Spades", "Spirals", "Stars", "Suns", "Swords", "Tridents", "Trees", "Waves"
        };
        String[] values = {
                "(1) Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "(11) Jack", "(12) Queen", "(13) King", "(14) Nomad",
                "(15) Prince", "(16) Rune", "(17) Fable", "(18) Sorceress", "(19) Utopia", "(20) Wizard", "(21) Titan",
                "(22) Baron", "(23) Illusionist", "(24) Oracle", "(25) Magician", "(26) Luminary", "(27) Eclipse",
                "(28) Celestial", "(29) Duke", "(30) Genesis", "(31) Zephyr", "(32) Vesper"
        };

        for (String suit : suits) {
            for (String value : values) {
                cardNames.add(value + " of " + suit);
            }
        }

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
}
