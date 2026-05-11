package com.mystic.tarotboard.gameitems;

import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.control.Tooltip;

public class Chip {
    private final StackPane chipPane;
    private final Color chipColor;
    public static final double CHIP_RADIUS = 50;

    public Chip(Color chipColor, Image bwFrontImage, Image bwBackImage) {
        this.chipColor = chipColor;
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
        imageView.setFitWidth(Chip.CHIP_RADIUS);
        imageView.setFitHeight(Chip.CHIP_RADIUS);

        ColorInput colorInput = new ColorInput(0, 0, Chip.CHIP_RADIUS, Chip.CHIP_RADIUS, chipColor);
        Blend blendEffect = new Blend(BlendMode.MULTIPLY, colorInput, null);
        imageView.setEffect(blendEffect);

        return imageView;
    }

    public StackPane getChipPane() {
        return chipPane;
    }

    public Color getColor() {
        return chipColor;
    }

    // New method to update chip images
    public void updateImages(Image newFrontImage, Image newBackImage) {
        if (chipPane.getChildren().getFirst() instanceof ImageView) {
            ((ImageView) chipPane.getChildren().getFirst()).setImage(newFrontImage);
        }
        if (chipPane.getChildren().get(1) instanceof ImageView) {
            ((ImageView) chipPane.getChildren().get(1)).setImage(newBackImage);
        }
    }
}
