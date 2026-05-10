package com.mystic.tarotboard.gameitems;

import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.util.Random;

public class Die {
    private static final Random RNG = new Random();
    private static final double SIZE = 60;
    private final int sides;
    private final StackPane pane;
    private int currentValue;
    private final Text valueText;
    private final Color dieColor;

    public Die(int sides, Color dieColor) {
        this.sides = sides;
        this.dieColor = dieColor;
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

    public void roll() {
        currentValue = RNG.nextInt(sides) + 1;
        valueText.setText(Integer.toString(currentValue));
    }

    public void setCurrentValue(int value) {
        this.currentValue = value;
        valueText.setText(Integer.toString(this.currentValue));
    }

    public StackPane getPane() { return pane; }

    public int getSides() { return sides; }

    public int getCurrentValue() { return currentValue; }

    public Color getDieColor() { return dieColor; }
}
