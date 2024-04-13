package com.mystic.tarotboard;

public class PokerChips {
    private String color;
    private int value;

    public PokerChips(String color, int value) {
        this.color = color;
        this.value = value;
    }

    public String getColor() {
        return color;
    }

    public int getValue() {
        return value;
    }
}
