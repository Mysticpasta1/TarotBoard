package com.mystic.tarotboard.items;

import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Random;

/**
 * Represents a die in the TarotBoard UI, supporting configurable side count,
 * color, random rolling, and double-click interaction.
 */
public class Dice {
    private static final Random RNG = new Random();
    private static final double SIZE = 60;
    private static int dieCounter = 0;
    private final int sides;
    private final StackPane pane;
    private int currentValue;
    private final Text valueText;
    private final Color dieColor;
    private final String pieceId;

    /**
     * Constructs a die item with an auto-generated piece ID.
     *
     * @param sides    the number of sides on the die
     * @param dieColor the background color of the die
     */
    public Dice(int sides, Color dieColor) {
        this(sides, dieColor, "die:" + (dieCounter++));
    }

    /**
     * Constructs a die item with a specified piece ID.
     *
     * @param sides    the number of sides on the die
     * @param dieColor the background color of the die
     * @param pieceId  the unique identifier for this die piece
     */
    public Dice(int sides, Color dieColor, String pieceId) {
        this.sides = sides;
        this.dieColor = dieColor;
        this.pieceId = pieceId;
        this.currentValue = RNG.nextInt(sides) + 1;

        pane = new StackPane();

        Rectangle bg = new Rectangle(SIZE, SIZE);
        bg.setArcWidth(12);
        bg.setArcHeight(12);
        bg.setFill(dieColor);
        bg.setStroke(Color.BLACK);
        bg.setStrokeWidth(2);

        valueText = new Text(Integer.toString(currentValue));
        valueText.setFont(Font.font(28));
        valueText.setFill(dieColor.invert());

        pane.getChildren().addAll(bg, valueText);

        pane.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2
                    && !event.isShiftDown() && !event.isControlDown()) {
                roll();
            }
        });
    }

    /**
     * Rolls the die, setting its current value to a random number between 1 and sides.
     */
    public void roll() {
        currentValue = RNG.nextInt(sides) + 1;
        valueText.setText(Integer.toString(currentValue));
    }

    /**
     * Sets the die to a specific value and updates the display.
     *
     * @param value the value to set (must be between 1 and sides)
     */
    public void setCurrentValue(int value) {
        this.currentValue = value;
        valueText.setText(Integer.toString(this.currentValue));
    }

    /**
     * Returns the StackPane containing the die visuals.
     *
     * @return the die pane
     */
    public StackPane getPane() {
        return pane;
    }

    /**
     * Returns the number of sides on this die.
     *
     * @return the side count
     */
    public int getSides() {
        return sides;
    }

    /**
     * Returns the current face-up value of the die.
     *
     * @return the current value
     */
    public int getCurrentValue() {
        return currentValue;
    }

    /**
     * Returns the background color of the die.
     *
     * @return the die color
     */
    public Color getDieColor() {
        return dieColor;
    }

    /**
     * Returns the unique piece identifier for this die.
     *
     * @return the piece ID
     */
    public String getPieceId() {
        return pieceId;
    }
}
