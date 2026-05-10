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

    public static void makeDraggable(StackPane pane, Translate translate) {
        final double[] dragDeltaX = new double[1];
        final double[] dragDeltaY = new double[1];

        pane.getTransforms().add(translate);

        pane.setOnMousePressed(event -> {
            dragDeltaX[0] = event.getSceneX() - translate.getX();
            dragDeltaY[0] = event.getSceneY() - translate.getY();
            pane.toFront();
        });

        pane.setOnMouseDragged(event -> {
            double newTranslateX = event.getSceneX() - dragDeltaX[0];
            double newTranslateY = event.getSceneY() - dragDeltaY[0];

            if (translate.getX() != newTranslateX || translate.getY() != newTranslateY) {
                translate.setX(newTranslateX);
                translate.setY(newTranslateY);
            }
        });
    }

    public static void makeFlippableAndRotatable(Pane pane, boolean isChip) {
        pane.setOnMouseClicked(event -> {
            Node front = null;
            Node back = null;
            Node text = null;
            double currentRotation = pane.getRotate();

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
                    if (front != null && back != null && event.isStillSincePress()) {
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
                    if (front != null && back != null && event.isStillSincePress()) {
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
                    }
                    if (front != null && back != null && event.isStillSincePress()) {
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
                    if (front != null && back != null && event.isStillSincePress()) {
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
                b.setVisible(!b.isVisible());
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
