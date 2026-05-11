package com.mystic.tarotboard.utils;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import java.util.Random;

public class UIUtils {

    public static void makeDraggable(StackPane pane) { // Removed Translate parameter
        final double[] dragDeltaX = new double[1];
        final double[] dragDeltaY = new double[1];
        final boolean[] isDragging = new boolean[1]; // Flag to indicate if dragging is active

        pane.setOnMousePressed(event -> {
            // Only start dragging if no modifier keys (Shift or Control) are pressed
            if (!event.isShiftDown() && !event.isControlDown()) {
                dragDeltaX[0] = event.getSceneX() - pane.getTranslateX();
                dragDeltaY[0] = event.getSceneY() - pane.getTranslateY();
                pane.toFront();
                isDragging[0] = true; // Set dragging flag to true
            } else {
                isDragging[0] = false; // Not dragging if modifier key is pressed
            }
        });

        pane.setOnMouseDragged(event -> {
            if (isDragging[0]) { // Only drag if the dragging flag is true
                double newTranslateX = event.getSceneX() - dragDeltaX[0];
                double newTranslateY = event.getSceneY() - dragDeltaY[0];

                // Apply directly to pane's translate properties
                pane.setTranslateX(newTranslateX);
                pane.setTranslateY(newTranslateY);
            }
        });

        pane.setOnMouseReleased(_ -> {
            isDragging[0] = false; // Reset dragging flag on mouse release
        });
    }

    public static void makeFlippableAndRotatable(Pane pane, boolean isChip) {
        pane.setOnMouseClicked(event -> {
            Node front = null;
            Node back = null;
            Node text = null;
            double currentRotation = pane.getRotate();

            // Prevent rotation/flip if a drag was initiated (though makeDraggable should prevent this now)
            // The isStillSincePress() check is crucial here to distinguish clicks from drags
            if (!event.isStillSincePress()) {
                return; // If mouse moved significantly, it was a drag, not a click for rotation/flip
            }

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
                    }
                    if (front != null && back != null) { // Removed event.isStillSincePress() as it's checked above
                        pane.setRotate(currentRotation - 1);
                        pane.toFront();
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
                    }
                    if (front != null && back != null) { // Removed event.isStillSincePress() as it's checked above
                        pane.setRotate(currentRotation - 90);
                        pane.toFront();
                    }
                } else {
                    if (isChip && event.getClickCount() == 2) {
                        ImageView chipFront = (ImageView) pane.getChildren().get(0);
                        ImageView chipBack = (ImageView) pane.getChildren().get(1);
                        chipFront.setVisible(!chipFront.isVisible());
                        chipBack.setVisible(!chipBack.isVisible());
                        pane.toFront();
                    } else if (event.getClickCount() == 2) {
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
                        if (front != null && back != null) { // Removed event.isStillSincePress() as it's checked above
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
                    }
                    if (front != null && back != null) { // Removed event.isStillSincePress() as it's checked above
                        pane.setRotate(currentRotation + 1);
                        pane.toFront();
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
                    }
                    if (front != null && back != null) { // Removed event.isStillSincePress() as it's checked above
                        pane.setRotate(currentRotation + 90);
                        pane.toFront();
                    }
                } else {
                    if (event.getClickCount() == 2) {
                        pane.setRotate(0);
                        pane.toFront();
                    }
                }
            }
        });
    }

    public static void multiFlip(Pane pane) {
        ImageView front = null, back = null;
        for (Node node : pane.getChildren()) {
            if (node instanceof ImageView iv) {
                if (iv.isVisible()) front = iv; else back = iv;
            }
        }
        if (front == null || back == null) return;

        ImageView f = front, b = back;
        int flips = 5;
        Timeline timeline = new Timeline();
        for (int i = 0; i < flips; i++) {
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(60 * i), _ -> {
                f.setVisible(!f.isVisible());
                b.setVisible(!f.isVisible()); // Corrected: b.setVisible(!f.isVisible())
            }));
        }
        timeline.setOnFinished(_ -> {
            boolean showFront = new Random().nextBoolean();
            f.setVisible(showFront);
            b.setVisible(!showFront);
            pane.toFront();
        });
        timeline.play();
    }
}
