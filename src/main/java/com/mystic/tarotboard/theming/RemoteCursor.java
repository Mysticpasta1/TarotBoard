package com.mystic.tarotboard.theming;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.ByteArrayInputStream;
import java.io.File;

/**
 * A visual remote cursor displayed on the game board, rendered as a colored dot
 * with an optional player label and custom image overlay.
 */
public class RemoteCursor {
    private final StackPane root;
    private final Circle dot;
    private final ImageView imageView;

    /**
     * Creates a remote cursor for the given player with the specified color.
     *
     * @param playerName the player's display name (abbreviated to 2 chars on the label)
     * @param color      the color of the cursor dot
     */
    public RemoteCursor(String playerName, Color color) {
        var cc = ThemeManager.getActiveTheme().getCursor();
        dot = new Circle(cc.dotRadius);
        dot.setFill(color);
        dot.setStroke(Color.web(cc.dotStrokeColor));
        dot.setStrokeWidth(cc.dotStrokeWidth);
        imageView = new ImageView();
        imageView.setFitWidth(cc.cursorSize);
        imageView.setFitHeight(cc.cursorSize);
        imageView.setPreserveRatio(true);
        imageView.setMouseTransparent(true);
        if (cc.cursorImagePath != null && !cc.cursorImagePath.isEmpty()) {
            try {
                Image img = new Image(new File(cc.cursorImagePath).toURI().toString(), cc.cursorSize, cc.cursorSize, true, true);
                imageView.setImage(img);
                imageView.setVisible(true);
                dot.setVisible(false);
            } catch (Exception e) {
                imageView.setVisible(false);
            }
        } else {
            imageView.setVisible(false);
        }
        Text label = new Text(playerName.length() > 2 ? playerName.substring(0, 2) : playerName);
        label.setFont(Font.font(cc.labelFontSize));
        label.setFill(Color.web(cc.labelColor));
        label.setMouseTransparent(true);
        root = new StackPane(dot, imageView, label);
        root.setMouseTransparent(true);
        root.setVisible(false);
    }

    /**
     * Sets the cursor image from raw byte data. When an image is set the dot is hidden.
     *
     * @param imageData raw image bytes, or null/empty to fall back to the dot
     */
    public void setImage(byte[] imageData) {
        double cs = ThemeManager.getActiveTheme().getCursor().cursorSize;
        if (imageData != null && imageData.length > 0) {
            Image img = new Image(new ByteArrayInputStream(imageData), cs, cs, true, true);
            imageView.setImage(img);
            imageView.setVisible(true);
            dot.setVisible(false);
        } else {
            imageView.setImage(null);
            imageView.setVisible(false);
            dot.setVisible(true);
        }
    }

    /**
     * Moves this cursor to the given board coordinates and makes it visible.
     *
     * @param x the x-coordinate on the board
     * @param y the y-coordinate on the board
     */
    public void setPosition(double x, double y) {
        double cs = ThemeManager.getActiveTheme().getCursor().cursorSize;
        root.setTranslateX(x - cs / 2);
        root.setTranslateY(y - cs / 2);
        root.setVisible(true);
    }

    /**
     * Adds this cursor to the specified parent pane.
     *
     * @param parent the pane to add this cursor to
     */
    public void addTo(Pane parent) {
        parent.getChildren().add(root);
    }

    /**
     * Removes this cursor from the specified parent pane.
     *
     * @param parent the pane to remove this cursor from
     */
    public void removeFrom(Pane parent) {
        parent.getChildren().remove(root);
    }
}
