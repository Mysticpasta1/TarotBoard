package com.mystic.tarotboard.items;

import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Represents a poker chip in the TarotBoard UI, displaying front/back images
 * tinted with a configurable color and clipped to a circular shape.
 */
public class Chips {
    private final StackPane chipPane;
    private final Color chipColor;
    private final String pieceId;
    private static int chipCounter = 0;

    /**
     * The fixed radius used for all chip circles.
     */
    public static final double CHIP_RADIUS = 50;

    /**
     * Constructs a Chips item with an auto-generated piece ID.
     *
     * @param chipColor    the tint color for the chip images
     * @param bwFrontImage the front face image (grayscale)
     * @param bwBackImage  the back face image (grayscale)
     */
    public Chips(Color chipColor, Image bwFrontImage, Image bwBackImage) {
        this(chipColor, bwFrontImage, bwBackImage, "chip:" + (chipCounter++));
    }

    /**
     * Constructs a Chips item with a specified piece ID.
     *
     * @param chipColor    the tint color for the chip images
     * @param bwFrontImage the front face image (grayscale)
     * @param bwBackImage  the back face image (grayscale)
     * @param pieceId      the unique identifier for this chip piece
     */
    public Chips(Color chipColor, Image bwFrontImage, Image bwBackImage, String pieceId) {
        this.chipColor = chipColor;
        this.pieceId = pieceId;
        this.chipPane = new StackPane();

        Circle circleClip = new Circle(CHIP_RADIUS / 2);
        circleClip.setCenterX(CHIP_RADIUS / 2);
        circleClip.setCenterY(CHIP_RADIUS / 2);

        ImageView chipFrontImageView = createChipImageView(bwFrontImage, chipColor);
        ImageView chipBackImageView = createChipImageView(bwBackImage, chipColor);
        chipBackImageView.setVisible(false);

        chipPane.getChildren().addAll(chipFrontImageView, chipBackImageView);
        chipPane.setClip(circleClip);
    }

    private ImageView createChipImageView(Image image, Color chipColor) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(Chips.CHIP_RADIUS);
        imageView.setFitHeight(Chips.CHIP_RADIUS);

        ColorInput colorInput = new ColorInput(0, 0, Chips.CHIP_RADIUS, Chips.CHIP_RADIUS, chipColor);
        Blend blendEffect = new Blend(BlendMode.MULTIPLY, colorInput, null);
        imageView.setEffect(blendEffect);

        return imageView;
    }

    /**
     * Returns the StackPane containing the chip visuals.
     *
     * @return the chip pane
     */
    public StackPane getChipPane() {
        return chipPane;
    }

    /**
     * Returns the tint color used for this chip.
     *
     * @return the chip color
     */
    public Color getColor() {
        return chipColor;
    }

    /**
     * Returns the unique piece identifier for this chip.
     *
     * @return the piece ID
     */
    public String getPieceId() {
        return pieceId;
    }

    /**
     * Updates the front and back chip images.
     *
     * @param newFrontImage the new front face image
     * @param newBackImage  the new back face image
     */
    public void updateImages(Image newFrontImage, Image newBackImage) {
        if (chipPane.getChildren().getFirst() instanceof ImageView) {
            ((ImageView) chipPane.getChildren().getFirst()).setImage(newFrontImage);
        }
        if (chipPane.getChildren().get(1) instanceof ImageView) {
            ((ImageView) chipPane.getChildren().get(1)).setImage(newBackImage);
        }
    }
}
