package com.mystic.tarotboard.utils;

import com.mystic.tarotboard.TarotBoard;
import com.mystic.tarotboard.items.Cards;
import com.mystic.tarotboard.theming.configs.KeyBindConfig;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility methods for interactive UI behaviors such as dragging, flipping, and rotating.
 */
public class UIUtils {

    /**
     * Callback invoked when a drag operation ends.
     */
    @FunctionalInterface
    public interface DragEndCallback {
        void onDragEnd(double translateX, double translateY);
    }

    /**
     * Callback invoked during a drag operation.
     */
    @FunctionalInterface
    public interface DragMoveCallback {
        void onDragMove(double translateX, double translateY);
    }

    /**
     * Callback invoked during a pile drag operation.
     */
    @FunctionalInterface
    public interface PileDragCallback {
        /**
         * @param isFinal true when the drag has ended, so the position is the resting one
         *                and must be sent to peers rather than dropped by move throttling
         */
        void onPileDrag(List<StackPane> pile, double newTranslateX, double newTranslateY, boolean isFinal);
    }

    /**
     * Callback invoked when a transform (flip or rotate) occurs.
     */
    @FunctionalInterface
    public interface TransformCallback {
        void onTransform(String type, double value);
    }

    /**
     * Callback invoked when an element is brought to the front.
     */
    @FunctionalInterface
    public interface ToFrontCallback {
        void onToFront();
    }

    /**
     * Makes a StackPane draggable with mouse press and drag events.
     *
     * @param pane           the pane to make draggable
     * @param tarotBoard     the main application instance
     * @param onDragEnd      callback invoked when dragging ends, or null
     * @param onDragMove     callback invoked while dragging, or null
     * @param onPileDrag     callback for dragging a pile of cards, or null
     */
    public static void makeDraggable(StackPane pane, TarotBoard tarotBoard, DragEndCallback onDragEnd, DragMoveCallback onDragMove, PileDragCallback onPileDrag) {
        final double[] dragDeltaX = new double[1];
        final double[] dragDeltaY = new double[1];
        final boolean[] isDragging = new boolean[1];
        final boolean[] wasDragged = new boolean[1];
        final double[] pressX = new double[1];
        final double[] pressY = new double[1];
        final boolean[] pileDragActive = new boolean[1];
        @SuppressWarnings("unchecked")
        final List<StackPane>[] cachedPile = new List[1];

        pane.setOnMousePressed(event -> {
            if (!event.isShiftDown()) {
                var parent = pane.getParent();
                if (parent != null) {
                    var local = parent.sceneToLocal(event.getSceneX(), event.getSceneY());
                    dragDeltaX[0] = local.getX() - pane.getTranslateX();
                    dragDeltaY[0] = local.getY() - pane.getTranslateY();
                } else {
                    dragDeltaX[0] = event.getSceneX() - pane.getTranslateX();
                    dragDeltaY[0] = event.getSceneY() - pane.getTranslateY();
                }
                pressX[0] = pane.getTranslateX();
                pressY[0] = pane.getTranslateY();
                pileDragActive[0] = false;
                cachedPile[0] = null;
                pane.toFront();
                isDragging[0] = true;
                wasDragged[0] = false;
            } else {
                isDragging[0] = false;
            }
        });

        pane.setOnMouseDragged(event -> {
            if (isDragging[0]) {
                double newTranslateX, newTranslateY;
                var parent = pane.getParent();
                if (parent != null) {
                    var local = parent.sceneToLocal(event.getSceneX(), event.getSceneY());
                    newTranslateX = local.getX() - dragDeltaX[0];
                    newTranslateY = local.getY() - dragDeltaY[0];
                } else {
                    newTranslateX = event.getSceneX() - dragDeltaX[0];
                    newTranslateY = event.getSceneY() - dragDeltaY[0];
                }

                KeyBindConfig kb = KeyBindConfig.getInstance();
                if (event.isAltDown()) {
                    if (!pileDragActive[0]) {
                        cachedPile[0] = findCardPileAt(tarotBoard, pressX[0], pressY[0], pane, true);
                        pileDragActive[0] = cachedPile[0].size() > 1;
                    }
                    if (pileDragActive[0] && onPileDrag != null) {
                        onPileDrag.onPileDrag(cachedPile[0], newTranslateX, newTranslateY, false);
                    } else {
                        pane.setTranslateX(newTranslateX);
                        pane.setTranslateY(newTranslateY);
                        if (onDragMove != null) onDragMove.onDragMove(newTranslateX, newTranslateY);
                    }
                } else if (event.isControlDown() && kb.pileDrag() == KeyCode.CONTROL) {
                    if (!pileDragActive[0]) {
                        cachedPile[0] = findCardPileAt(tarotBoard, pressX[0], pressY[0], pane, false);
                        pileDragActive[0] = cachedPile[0].size() > 1;
                    }
                    if (pileDragActive[0] && onPileDrag != null) {
                        onPileDrag.onPileDrag(cachedPile[0], newTranslateX, newTranslateY, false);
                    } else {
                        pane.setTranslateX(newTranslateX);
                        pane.setTranslateY(newTranslateY);
                        if (onDragMove != null) onDragMove.onDragMove(newTranslateX, newTranslateY);
                    }
                } else {
                    pileDragActive[0] = false;
                    cachedPile[0] = null;
                    pane.setTranslateX(newTranslateX);
                    pane.setTranslateY(newTranslateY);
                    if (onDragMove != null) onDragMove.onDragMove(newTranslateX, newTranslateY);
                }
                wasDragged[0] = true;
            }
        });

        pane.setOnMouseReleased(event -> {
            if (isDragging[0] && wasDragged[0]) {
                KeyBindConfig kb = KeyBindConfig.getInstance();
                boolean wildsOnly = event.isAltDown();
                boolean pileMode = wildsOnly || (event.isControlDown() && kb.pileDrag() == KeyCode.CONTROL);
                if (pileMode) {
                    if (!pileDragActive[0]) {
                        cachedPile[0] = findCardPileAt(tarotBoard, pressX[0], pressY[0], pane, wildsOnly);
                        pileDragActive[0] = cachedPile[0].size() > 1;
                    }
                    if (pileDragActive[0]) {
                        // onDragEnd only ever reports the dragged pane's own id, so routing
                        // the whole pile through it told peers one card moved N times and
                        // left the rest of the pile wherever throttling last put them.
                        if (onPileDrag != null) {
                            onPileDrag.onPileDrag(cachedPile[0], pane.getTranslateX(), pane.getTranslateY(), true);
                        }
                    } else if (onDragEnd != null) {
                        onDragEnd.onDragEnd(pane.getTranslateX(), pane.getTranslateY());
                    }
                } else if (onDragEnd != null) {
                    onDragEnd.onDragEnd(pane.getTranslateX(), pane.getTranslateY());
                }
            }
            isDragging[0] = false;
        });

        pane.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if (wasDragged[0] && !pileDragActive[0]) {
                snapToNearestCard(pane, tarotBoard);
            }
        });
    }

    private static List<StackPane> findCardPileAt(TarotBoard tarotBoard, double x, double y, StackPane draggedPane, boolean wildsOnly) {
        List<StackPane> pile = new ArrayList<>();
        for (Cards card : tarotBoard.getCards()) {
            if (card == null) continue;
            StackPane cardPane = card.getCardPane();
            if (cardPane == null || cardPane == draggedPane) continue;
            if (Math.abs(cardPane.getTranslateX() - x) < 5.0 && Math.abs(cardPane.getTranslateY() - y) < 5.0) {
                if (wildsOnly && !tarotBoard.isWildCard(cardPane)) continue;
                pile.add(cardPane);
            }
        }
        if (!wildsOnly || tarotBoard.isWildCard(draggedPane)) {
            pile.add(draggedPane);
        }
        return pile;
    }

    private static void snapToNearestCard(StackPane pane, TarotBoard tarotBoard) {
        double px = pane.getTranslateX();
        double py = pane.getTranslateY();
        double snapDistance = 50.0;

        StackPane nearest = null;
        double bestDist = snapDistance;

        for (Cards card : tarotBoard.getCards()) {
            if (card == null) continue;
            StackPane cp = card.getCardPane();
            if (cp == null || cp == pane) continue;
            double dx = cp.getTranslateX() - px;
            double dy = cp.getTranslateY() - py;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < bestDist) {
                bestDist = dist;
                nearest = cp;
            }
        }

        if (nearest != null) {
            pane.setTranslateX(nearest.getTranslateX());
            pane.setTranslateY(nearest.getTranslateY());
        }
    }

    /**
     * Makes a Pane flippable and rotatable via mouse clicks.
     * <ul>
     *   <li>Left-click double-click: flips the card/chip</li>
     *   <li>Shift+click: rotate by 1 degree</li>
     *   <li>Ctrl+click: rotate by 90 degrees</li>
     *   <li>Right-click double-click: reset rotation</li>
     * </ul>
     *
     * @param pane        the pane to make interactive
     * @param isChip      true if the pane represents a chip (dual ImageView flip)
     * @param onTransform callback invoked on flip/rotate, or null
     * @param onToFront   callback invoked when brought to front, or null
     */
    public static void makeFlippableAndRotatable(Pane pane, boolean isChip, TransformCallback onTransform, ToFrontCallback onToFront) {
        pane.getProperties().put("tb_isChip", isChip);
        if (onTransform != null) pane.getProperties().put("tb_onTransform", onTransform);
        if (onToFront != null) pane.getProperties().put("tb_onToFront", onToFront);
    }

    /**
     * Rotates a piece by {@code delta} degrees and raises it.
     *
     * <p>Does nothing unless the piece has both a visible and a hidden face, matching the
     * guard the click and key handlers use: a piece mid-deal has no back to rotate.</p>
     *
     * @param pane  the piece to rotate
     * @param delta the rotation to apply, in degrees
     */
    public static void rotatePiece(Pane pane, double delta) {
        Node front = null, back = null;
        for (Node node : pane.getChildren()) {
            if (node instanceof ImageView imageView) {
                if (imageView.isVisible()) front = imageView;
                else back = imageView;
            }
        }
        if (front == null || back == null) return;
        pane.setRotate(pane.getRotate() + delta);
        pane.toFront();
        fireTransform(pane, "rotate", pane.getRotate());
    }

    /**
     * Turns a piece over, showing whichever face is currently hidden.
     *
     * @param pane the piece to flip
     */
    public static void flipPiece(Pane pane) {
        boolean isChip = Boolean.TRUE.equals(pane.getProperties().get("tb_isChip"));
        if (isChip) {
            ImageView chipFront = (ImageView) pane.getChildren().get(0);
            ImageView chipBack = (ImageView) pane.getChildren().get(1);
            chipFront.setVisible(!chipFront.isVisible());
            chipBack.setVisible(!chipBack.isVisible());
            pane.toFront();
            fireTransform(pane, "flip", chipFront.isVisible() ? 1 : 0);
            return;
        }
        Node front = null, back = null, text = null;
        for (Node node : pane.getChildren()) {
            if (node instanceof ImageView imageView) {
                if (imageView.isVisible()) front = imageView;
                else back = imageView;
            }
            if (node instanceof Text t) text = t;
        }
        if (front == null || back == null) return;
        front.setVisible(!front.isVisible());
        back.setVisible(!back.isVisible());
        pane.toFront();
        if (text != null) text.setVisible(!front.isVisible() && !text.isVisible());
        fireTransform(pane, "flip", front.isVisible() ? 1 : 0);
    }

    /**
     * Returns a piece to its unrotated orientation and raises it.
     *
     * @param pane the piece to straighten
     */
    public static void resetPieceRotation(Pane pane) {
        pane.setRotate(0);
        pane.toFront();
        fireTransform(pane, "rotate", 0);
    }

    /**
     * Reports a change to the callbacks a piece was registered with, so that multiplayer
     * peers and the z-order stay in step regardless of which input triggered it.
     */
    private static void fireTransform(Pane pane, String kind, double value) {
        TransformCallback onTransform = (TransformCallback) pane.getProperties().get("tb_onTransform");
        ToFrontCallback onToFront = (ToFrontCallback) pane.getProperties().get("tb_onToFront");
        if (onTransform != null) onTransform.onTransform(kind, value);
        if (onToFront != null) onToFront.onToFront();
    }

    public static void handlePieceClick(Pane pane, MouseEvent event) {
        if (!event.isStillSincePress()) return;

        var kb = KeyBindConfig.getInstance();
        boolean isChip = Boolean.TRUE.equals(pane.getProperties().get("tb_isChip"));
        TransformCallback onTransform = (TransformCallback) pane.getProperties().get("tb_onTransform");
        ToFrontCallback onToFront = (ToFrontCallback) pane.getProperties().get("tb_onToFront");

        Node front = null, back = null, text = null;
        double currentRotation = pane.getRotate();
        MouseButton btn = event.getButton();
        int clicks = event.getClickCount();

        if (btn == kb.shiftLeftRotateButton() && event.isShiftDown() && !event.isControlDown() && kb.shiftLeftRotate()) {
            for (Node node : pane.getChildren()) {
                if (node instanceof ImageView imageView) {
                    if (imageView.isVisible()) front = imageView;
                    else back = imageView;
                }
            }
            if (front != null && back != null) {
                pane.setRotate(currentRotation - 1);
                pane.toFront();
                if (onTransform != null) onTransform.onTransform("rotate", pane.getRotate());
                if (onToFront != null) onToFront.onToFront();
            }
            return;
        }

        if (btn == kb.ctrlLeftRotateButton() && event.isControlDown() && !event.isShiftDown() && kb.ctrlLeftRotate()) {
            for (Node node : pane.getChildren()) {
                if (node instanceof ImageView imageView) {
                    if (imageView.isVisible()) front = imageView;
                    else back = imageView;
                }
            }
            if (front != null && back != null) {
                pane.setRotate(currentRotation - 90);
                pane.toFront();
                if (onTransform != null) onTransform.onTransform("rotate", pane.getRotate());
                if (onToFront != null) onToFront.onToFront();
            }
            return;
        }

        if (btn == kb.shiftRightRotateButton() && event.isShiftDown() && !event.isControlDown() && kb.shiftRightRotate()) {
            for (Node node : pane.getChildren()) {
                if (node instanceof ImageView imageView) {
                    if (imageView.isVisible()) front = imageView;
                    else back = imageView;
                }
            }
            if (front != null && back != null) {
                pane.setRotate(currentRotation + 1);
                pane.toFront();
                if (onTransform != null) onTransform.onTransform("rotate", pane.getRotate());
                if (onToFront != null) onToFront.onToFront();
            }
            return;
        }

        if (btn == kb.ctrlRightRotateButton() && event.isControlDown() && !event.isShiftDown() && kb.ctrlRightRotate()) {
            for (Node node : pane.getChildren()) {
                if (node instanceof ImageView imageView) {
                    if (imageView.isVisible()) front = imageView;
                    else back = imageView;
                }
            }
            if (front != null && back != null) {
                pane.setRotate(currentRotation + 90);
                pane.toFront();
                if (onTransform != null) onTransform.onTransform("rotate", pane.getRotate());
                if (onToFront != null) onToFront.onToFront();
            }
            return;
        }

        if (clicks == 2 && btn == kb.flipButton() && kb.doubleLeftFlip()) {
            if (isChip) {
                ImageView chipFront = (ImageView) pane.getChildren().get(0);
                ImageView chipBack = (ImageView) pane.getChildren().get(1);
                chipFront.setVisible(!chipFront.isVisible());
                chipBack.setVisible(!chipBack.isVisible());
                pane.toFront();
                if (onTransform != null) onTransform.onTransform("flip", chipFront.isVisible() ? 1 : 0);
                if (onToFront != null) onToFront.onToFront();
            } else {
                for (Node node : pane.getChildren()) {
                    if (node instanceof ImageView imageView) {
                        if (imageView.isVisible()) front = imageView;
                        else back = imageView;
                    }
                    if (node instanceof Text t) text = t;
                }
                if (front != null && back != null) {
                    front.setVisible(!front.isVisible());
                    back.setVisible(!back.isVisible());
                    pane.toFront();
                    if (text != null) {
                        text.setVisible(!front.isVisible() && !text.isVisible());
                    }
                    if (onTransform != null) onTransform.onTransform("flip", front.isVisible() ? 1 : 0);
                    if (onToFront != null) onToFront.onToFront();
                }
            }
            return;
        }

        if (clicks == 2 && btn == kb.resetButton() && kb.doubleRightReset()) {
            pane.setRotate(0);
            pane.toFront();
            if (onTransform != null) onTransform.onTransform("rotate", 0);
            if (onToFront != null) onToFront.onToFront();
        }
    }

    public static void handlePieceKeyPress(Pane pane, KeyCode code) {
        var kb = KeyBindConfig.getInstance();
        boolean isChip = Boolean.TRUE.equals(pane.getProperties().get("tb_isChip"));
        TransformCallback onTransform = (TransformCallback) pane.getProperties().get("tb_onTransform");
        ToFrontCallback onToFront = (ToFrontCallback) pane.getProperties().get("tb_onToFront");

        Node front = null, back = null, text = null;
        double currentRotation = pane.getRotate();

        if (code == kb.shiftLeftRotateKey() && kb.shiftLeftRotate()) {
            for (Node node : pane.getChildren()) {
                if (node instanceof ImageView iv) {
                    if (iv.isVisible()) front = iv;
                    else back = iv;
                }
            }
            if (front != null && back != null) {
                pane.setRotate(currentRotation - 1);
                pane.toFront();
                if (onTransform != null) onTransform.onTransform("rotate", pane.getRotate());
                if (onToFront != null) onToFront.onToFront();
            }
            return;
        }

        if (code == kb.ctrlLeftRotateKey() && kb.ctrlLeftRotate()) {
            for (Node node : pane.getChildren()) {
                if (node instanceof ImageView iv) {
                    if (iv.isVisible()) front = iv;
                    else back = iv;
                }
            }
            if (front != null && back != null) {
                pane.setRotate(currentRotation - 90);
                pane.toFront();
                if (onTransform != null) onTransform.onTransform("rotate", pane.getRotate());
                if (onToFront != null) onToFront.onToFront();
            }
            return;
        }

        if (code == kb.shiftRightRotateKey() && kb.shiftRightRotate()) {
            for (Node node : pane.getChildren()) {
                if (node instanceof ImageView iv) {
                    if (iv.isVisible()) front = iv;
                    else back = iv;
                }
            }
            if (front != null && back != null) {
                pane.setRotate(currentRotation + 1);
                pane.toFront();
                if (onTransform != null) onTransform.onTransform("rotate", pane.getRotate());
                if (onToFront != null) onToFront.onToFront();
            }
            return;
        }

        if (code == kb.ctrlRightRotateKey() && kb.ctrlRightRotate()) {
            for (Node node : pane.getChildren()) {
                if (node instanceof ImageView iv) {
                    if (iv.isVisible()) front = iv;
                    else back = iv;
                }
            }
            if (front != null && back != null) {
                pane.setRotate(currentRotation + 90);
                pane.toFront();
                if (onTransform != null) onTransform.onTransform("rotate", pane.getRotate());
                if (onToFront != null) onToFront.onToFront();
            }
            return;
        }

        if (code == kb.flipKey() && kb.doubleLeftFlip()) {
            if (isChip) {
                ImageView chipFront = (ImageView) pane.getChildren().get(0);
                ImageView chipBack = (ImageView) pane.getChildren().get(1);
                chipFront.setVisible(!chipFront.isVisible());
                chipBack.setVisible(!chipBack.isVisible());
                pane.toFront();
                if (onTransform != null) onTransform.onTransform("flip", chipFront.isVisible() ? 1 : 0);
                if (onToFront != null) onToFront.onToFront();
            } else {
                for (Node node : pane.getChildren()) {
                    if (node instanceof ImageView iv) {
                        if (iv.isVisible()) front = iv;
                        else back = iv;
                    }
                    if (node instanceof Text t) text = t;
                }
                if (front != null && back != null) {
                    front.setVisible(!front.isVisible());
                    back.setVisible(!back.isVisible());
                    pane.toFront();
                    if (text != null) {
                        text.setVisible(!front.isVisible() && !text.isVisible());
                    }
                    if (onTransform != null) onTransform.onTransform("flip", front.isVisible() ? 1 : 0);
                    if (onToFront != null) onToFront.onToFront();
                }
            }
            return;
        }

        if (code == kb.resetKey() && kb.doubleRightReset()) {
            pane.setRotate(0);
            pane.toFront();
            if (onTransform != null) onTransform.onTransform("rotate", 0);
            if (onToFront != null) onToFront.onToFront();
        }
    }

    /**
     * Plays an animated multi-flip effect on the given pane's ImageView children.
     *
     * @param pane the pane containing front/back ImageView nodes
     */
    public static void multiFlip(Pane pane) {
        ImageView front = null, back = null;
        for (Node node : pane.getChildren()) {
            if (node instanceof ImageView iv) {
                if (iv.isVisible()) front = iv;
                else back = iv;
            }
        }
        if (front == null || back == null) return;

        ImageView f = front, b = back;
        int flips = 5;
        Timeline timeline = new Timeline();
        for (int i = 0; i < flips; i++) {
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(60 * i), event -> {
                f.setVisible(!f.isVisible());
                b.setVisible(!f.isVisible());
            }));
        }
        TransformCallback onTransform = (TransformCallback) pane.getProperties().get("tb_onTransform");
        ToFrontCallback onToFront = (ToFrontCallback) pane.getProperties().get("tb_onToFront");
        timeline.setOnFinished(event -> {
            boolean showFront = new Random().nextBoolean();
            f.setVisible(showFront);
            b.setVisible(!showFront);
            pane.toFront();
            // The landing face is random, so peers cannot infer it: tell them how it
            // settled or their copy of this chip stays wrong for the rest of the game.
            if (onTransform != null) onTransform.onTransform("flip", showFront ? 1 : 0);
            if (onToFront != null) onToFront.onToFront();
        });
        timeline.play();
    }
}