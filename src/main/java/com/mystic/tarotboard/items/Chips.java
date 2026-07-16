package com.mystic.tarotboard.items;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a poker chip in the TarotBoard UI, displaying front/back images
 * tinted with a configurable color and clipped to a circular shape.
 */
public class Chips {
    private final StackPane chipPane;
    private final Color chipColor;
    private final String pieceId;
    private static int chipCounter = 0;

    /** Tinted chip faces, keyed by source image and colour. See {@code tinted}. */
    private static final Map<String, Image> TINT_CACHE = new HashMap<>();

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
        ImageView imageView = new ImageView(tinted(image, chipColor));
        imageView.setFitWidth(Chips.CHIP_RADIUS);
        imageView.setFitHeight(Chips.CHIP_RADIUS);
        return imageView;
    }

    /**
     * Multiplies a greyscale chip face by its colour, once, into a plain image.
     *
     * <p>This was a {@link javafx.scene.effect.Blend} of a {@code ColorInput} over the
     * face, which is the same arithmetic but asks the renderer to redo it every frame,
     * into an offscreen buffer per chip per face. Android's GL pipeline is the reason it
     * cannot stay: it has no effect peers to build, which is already why the card labels
     * drop their shadow there, and the cost grows with every chip spawned, so a table
     * that has been played on a while is the slowest one. Two chips of one colour share
     * a tinted face, so the cache holds one image per colour rather than per chip.</p>
     */
    private static Image tinted(Image image, Color color) {
        String key = System.identityHashCode(image) + "@" + color;
        return TINT_CACHE.computeIfAbsent(key, k -> {
            int w = (int) image.getWidth();
            int h = (int) image.getHeight();
            PixelReader in = image.getPixelReader();
            if (w <= 0 || h <= 0 || in == null) return image;
            WritableImage out = new WritableImage(w, h);
            PixelWriter writer = out.getPixelWriter();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    Color src = in.getColor(x, y);
                    // MULTIPLY, matching the Blend this replaces: the greyscale artwork
                    // shades the colour, and the face's own alpha is what keeps its edge.
                    writer.setColor(x, y, new Color(
                            src.getRed() * color.getRed(),
                            src.getGreen() * color.getGreen(),
                            src.getBlue() * color.getBlue(),
                            src.getOpacity() * color.getOpacity()));
                }
            }
            return out;
        });
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
