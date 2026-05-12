package com.mystic.tarotboard.theming.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mystic.tarotboard.utils.PlatformPaths;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.UnaryOperator;

public record KeyBindConfig(
        KeyCode togglePlayerList,
        KeyCode multiFlip,
        boolean shiftLeftRotate,
        boolean ctrlLeftRotate,
        boolean shiftRightRotate,
        boolean ctrlRightRotate,
        boolean doubleLeftFlip,
        boolean doubleRightReset,
        MouseButton shiftLeftRotateButton,
        MouseButton ctrlLeftRotateButton,
        MouseButton shiftRightRotateButton,
        MouseButton ctrlRightRotateButton,
        MouseButton flipButton,
        MouseButton resetButton,
        KeyCode shiftLeftRotateKey,
        KeyCode ctrlLeftRotateKey,
        KeyCode shiftRightRotateKey,
        KeyCode ctrlRightRotateKey,
        KeyCode flipKey,
        KeyCode resetKey,
        KeyCode moveUp,
        KeyCode moveDown,
        KeyCode moveLeft,
        KeyCode moveRight,
        int moveSpeed
) {
    private static KeyBindConfig instance;

    public static KeyBindConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public static KeyBindConfig defaults() {
        return new KeyBindConfig(
                KeyCode.TAB, KeyCode.F,
                true, true, true, true, true, true,
                MouseButton.PRIMARY, MouseButton.PRIMARY, MouseButton.SECONDARY, MouseButton.SECONDARY,
                MouseButton.PRIMARY, MouseButton.SECONDARY,
                KeyCode.UNDEFINED, KeyCode.UNDEFINED, KeyCode.UNDEFINED, KeyCode.UNDEFINED,
                KeyCode.UNDEFINED, KeyCode.UNDEFINED,
                KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT,
                10
        );
    }

    public static void update(UnaryOperator<KeyBindConfig> fn) {
        instance = fn.apply(getInstance());
        instance.save();
    }

    public KeyBindConfig withTogglePlayerList(KeyCode v) {
        return new KeyBindConfig(v, multiFlip, shiftLeftRotate, ctrlLeftRotate, shiftRightRotate, ctrlRightRotate, doubleLeftFlip, doubleRightReset, shiftLeftRotateButton, ctrlLeftRotateButton, shiftRightRotateButton, ctrlRightRotateButton, flipButton, resetButton, shiftLeftRotateKey, ctrlLeftRotateKey, shiftRightRotateKey, ctrlRightRotateKey, flipKey, resetKey, moveUp, moveDown, moveLeft, moveRight, moveSpeed);
    }

    public KeyBindConfig withMultiFlip(KeyCode v) {
        return new KeyBindConfig(togglePlayerList, v, shiftLeftRotate, ctrlLeftRotate, shiftRightRotate, ctrlRightRotate, doubleLeftFlip, doubleRightReset, shiftLeftRotateButton, ctrlLeftRotateButton, shiftRightRotateButton, ctrlRightRotateButton, flipButton, resetButton, shiftLeftRotateKey, ctrlLeftRotateKey, shiftRightRotateKey, ctrlRightRotateKey, flipKey, resetKey, moveUp, moveDown, moveLeft, moveRight, moveSpeed);
    }

    public KeyBindConfig withShiftLeftRotate(boolean v) {
        return new KeyBindConfig(togglePlayerList, multiFlip, v, ctrlLeftRotate, shiftRightRotate, ctrlRightRotate, doubleLeftFlip, doubleRightReset, shiftLeftRotateButton, ctrlLeftRotateButton, shiftRightRotateButton, ctrlRightRotateButton, flipButton, resetButton, shiftLeftRotateKey, ctrlLeftRotateKey, shiftRightRotateKey, ctrlRightRotateKey, flipKey, resetKey, moveUp, moveDown, moveLeft, moveRight, moveSpeed);
    }

    public KeyBindConfig withCtrlLeftRotate(boolean v) {
        return new KeyBindConfig(togglePlayerList, multiFlip, shiftLeftRotate, v, shiftRightRotate, ctrlRightRotate, doubleLeftFlip, doubleRightReset, shiftLeftRotateButton, ctrlLeftRotateButton, shiftRightRotateButton, ctrlRightRotateButton, flipButton, resetButton, shiftLeftRotateKey, ctrlLeftRotateKey, shiftRightRotateKey, ctrlRightRotateKey, flipKey, resetKey, moveUp, moveDown, moveLeft, moveRight, moveSpeed);
    }

    public KeyBindConfig withShiftRightRotate(boolean v) {
        return new KeyBindConfig(togglePlayerList, multiFlip, shiftLeftRotate, ctrlLeftRotate, v, ctrlRightRotate, doubleLeftFlip, doubleRightReset, shiftLeftRotateButton, ctrlLeftRotateButton, shiftRightRotateButton, ctrlRightRotateButton, flipButton, resetButton, shiftLeftRotateKey, ctrlLeftRotateKey, shiftRightRotateKey, ctrlRightRotateKey, flipKey, resetKey, moveUp, moveDown, moveLeft, moveRight, moveSpeed);
    }

    public KeyBindConfig withCtrlRightRotate(boolean v) {
        return new KeyBindConfig(togglePlayerList, multiFlip, shiftLeftRotate, ctrlLeftRotate, shiftRightRotate, v, doubleLeftFlip, doubleRightReset, shiftLeftRotateButton, ctrlLeftRotateButton, shiftRightRotateButton, ctrlRightRotateButton, flipButton, resetButton, shiftLeftRotateKey, ctrlLeftRotateKey, shiftRightRotateKey, ctrlRightRotateKey, flipKey, resetKey, moveUp, moveDown, moveLeft, moveRight, moveSpeed);
    }

    public KeyBindConfig withDoubleLeftFlip(boolean v) {
        return new KeyBindConfig(togglePlayerList, multiFlip, shiftLeftRotate, ctrlLeftRotate, shiftRightRotate, ctrlRightRotate, v, doubleRightReset, shiftLeftRotateButton, ctrlLeftRotateButton, shiftRightRotateButton, ctrlRightRotateButton, flipButton, resetButton, shiftLeftRotateKey, ctrlLeftRotateKey, shiftRightRotateKey, ctrlRightRotateKey, flipKey, resetKey, moveUp, moveDown, moveLeft, moveRight, moveSpeed);
    }

    public KeyBindConfig withDoubleRightReset(boolean v) {
        return new KeyBindConfig(togglePlayerList, multiFlip, shiftLeftRotate, ctrlLeftRotate, shiftRightRotate, ctrlRightRotate, doubleLeftFlip, v, shiftLeftRotateButton, ctrlLeftRotateButton, shiftRightRotateButton, ctrlRightRotateButton, flipButton, resetButton, shiftLeftRotateKey, ctrlLeftRotateKey, shiftRightRotateKey, ctrlRightRotateKey, flipKey, resetKey, moveUp, moveDown, moveLeft, moveRight, moveSpeed);
    }

    public KeyBindConfig withShiftLeftRotateButton(MouseButton v) {
        return new KeyBindConfig(togglePlayerList, multiFlip, shiftLeftRotate, ctrlLeftRotate, shiftRightRotate, ctrlRightRotate, doubleLeftFlip, doubleRightReset, v, ctrlLeftRotateButton, shiftRightRotateButton, ctrlRightRotateButton, flipButton, resetButton, shiftLeftRotateKey, ctrlLeftRotateKey, shiftRightRotateKey, ctrlRightRotateKey, flipKey, resetKey, moveUp, moveDown, moveLeft, moveRight, moveSpeed);
    }

    public KeyBindConfig withCtrlLeftRotateButton(MouseButton v) {
        return new KeyBindConfig(togglePlayerList, multiFlip, shiftLeftRotate, ctrlLeftRotate, shiftRightRotate, ctrlRightRotate, doubleLeftFlip, doubleRightReset, shiftLeftRotateButton, v, shiftRightRotateButton, ctrlRightRotateButton, flipButton, resetButton, shiftLeftRotateKey, ctrlLeftRotateKey, shiftRightRotateKey, ctrlRightRotateKey, flipKey, resetKey, moveUp, moveDown, moveLeft, moveRight, moveSpeed);
    }

    public KeyBindConfig withShiftRightRotateButton(MouseButton v) {
        return new KeyBindConfig(togglePlayerList, multiFlip, shiftLeftRotate, ctrlLeftRotate, shiftRightRotate, ctrlRightRotate, doubleLeftFlip, doubleRightReset, shiftLeftRotateButton, ctrlLeftRotateButton, v, ctrlRightRotateButton, flipButton, resetButton, shiftLeftRotateKey, ctrlLeftRotateKey, shiftRightRotateKey, ctrlRightRotateKey, flipKey, resetKey, moveUp, moveDown, moveLeft, moveRight, moveSpeed);
    }

    public KeyBindConfig withCtrlRightRotateButton(MouseButton v) {
        return new KeyBindConfig(togglePlayerList, multiFlip, shiftLeftRotate, ctrlLeftRotate, shiftRightRotate, ctrlRightRotate, doubleLeftFlip, doubleRightReset, shiftLeftRotateButton, ctrlLeftRotateButton, shiftRightRotateButton, v, flipButton, resetButton, shiftLeftRotateKey, ctrlLeftRotateKey, shiftRightRotateKey, ctrlRightRotateKey, flipKey, resetKey, moveUp, moveDown, moveLeft, moveRight, moveSpeed);
    }

    public KeyBindConfig withFlipButton(MouseButton v) {
        return new KeyBindConfig(togglePlayerList, multiFlip, shiftLeftRotate, ctrlLeftRotate, shiftRightRotate, ctrlRightRotate, doubleLeftFlip, doubleRightReset, shiftLeftRotateButton, ctrlLeftRotateButton, shiftRightRotateButton, ctrlRightRotateButton, v, resetButton, shiftLeftRotateKey, ctrlLeftRotateKey, shiftRightRotateKey, ctrlRightRotateKey, flipKey, resetKey, moveUp, moveDown, moveLeft, moveRight, moveSpeed);
    }

    public KeyBindConfig withResetButton(MouseButton v) {
        return new KeyBindConfig(togglePlayerList, multiFlip, shiftLeftRotate, ctrlLeftRotate, shiftRightRotate, ctrlRightRotate, doubleLeftFlip, doubleRightReset, shiftLeftRotateButton, ctrlLeftRotateButton, shiftRightRotateButton, ctrlRightRotateButton, flipButton, v, shiftLeftRotateKey, ctrlLeftRotateKey, shiftRightRotateKey, ctrlRightRotateKey, flipKey, resetKey, moveUp, moveDown, moveLeft, moveRight, moveSpeed);
    }

    public KeyBindConfig withShiftLeftRotateKey(KeyCode v) {
        return new KeyBindConfig(togglePlayerList, multiFlip, shiftLeftRotate, ctrlLeftRotate, shiftRightRotate, ctrlRightRotate, doubleLeftFlip, doubleRightReset, shiftLeftRotateButton, ctrlLeftRotateButton, shiftRightRotateButton, ctrlRightRotateButton, flipButton, resetButton, v, ctrlLeftRotateKey, shiftRightRotateKey, ctrlRightRotateKey, flipKey, resetKey, moveUp, moveDown, moveLeft, moveRight, moveSpeed);
    }

    public KeyBindConfig withCtrlLeftRotateKey(KeyCode v) {
        return new KeyBindConfig(togglePlayerList, multiFlip, shiftLeftRotate, ctrlLeftRotate, shiftRightRotate, ctrlRightRotate, doubleLeftFlip, doubleRightReset, shiftLeftRotateButton, ctrlLeftRotateButton, shiftRightRotateButton, ctrlRightRotateButton, flipButton, resetButton, shiftLeftRotateKey, v, shiftRightRotateKey, ctrlRightRotateKey, flipKey, resetKey, moveUp, moveDown, moveLeft, moveRight, moveSpeed);
    }

    public KeyBindConfig withShiftRightRotateKey(KeyCode v) {
        return new KeyBindConfig(togglePlayerList, multiFlip, shiftLeftRotate, ctrlLeftRotate, shiftRightRotate, ctrlRightRotate, doubleLeftFlip, doubleRightReset, shiftLeftRotateButton, ctrlLeftRotateButton, shiftRightRotateButton, ctrlRightRotateButton, flipButton, resetButton, shiftLeftRotateKey, ctrlLeftRotateKey, v, ctrlRightRotateKey, flipKey, resetKey, moveUp, moveDown, moveLeft, moveRight, moveSpeed);
    }

    public KeyBindConfig withCtrlRightRotateKey(KeyCode v) {
        return new KeyBindConfig(togglePlayerList, multiFlip, shiftLeftRotate, ctrlLeftRotate, shiftRightRotate, ctrlRightRotate, doubleLeftFlip, doubleRightReset, shiftLeftRotateButton, ctrlLeftRotateButton, shiftRightRotateButton, ctrlRightRotateButton, flipButton, resetButton, shiftLeftRotateKey, ctrlLeftRotateKey, shiftRightRotateKey, v, flipKey, resetKey, moveUp, moveDown, moveLeft, moveRight, moveSpeed);
    }

    public KeyBindConfig withFlipKey(KeyCode v) {
        return new KeyBindConfig(togglePlayerList, multiFlip, shiftLeftRotate, ctrlLeftRotate, shiftRightRotate, ctrlRightRotate, doubleLeftFlip, doubleRightReset, shiftLeftRotateButton, ctrlLeftRotateButton, shiftRightRotateButton, ctrlRightRotateButton, flipButton, resetButton, shiftLeftRotateKey, ctrlLeftRotateKey, shiftRightRotateKey, ctrlRightRotateKey, v, resetKey, moveUp, moveDown, moveLeft, moveRight, moveSpeed);
    }

    public KeyBindConfig withResetKey(KeyCode v) {
        return new KeyBindConfig(togglePlayerList, multiFlip, shiftLeftRotate, ctrlLeftRotate, shiftRightRotate, ctrlRightRotate, doubleLeftFlip, doubleRightReset, shiftLeftRotateButton, ctrlLeftRotateButton, shiftRightRotateButton, ctrlRightRotateButton, flipButton, resetButton, shiftLeftRotateKey, ctrlLeftRotateKey, shiftRightRotateKey, ctrlRightRotateKey, flipKey, v, moveUp, moveDown, moveLeft, moveRight, moveSpeed);
    }

    public KeyBindConfig withMoveUp(KeyCode v) {
        return new KeyBindConfig(togglePlayerList, multiFlip, shiftLeftRotate, ctrlLeftRotate, shiftRightRotate, ctrlRightRotate, doubleLeftFlip, doubleRightReset, shiftLeftRotateButton, ctrlLeftRotateButton, shiftRightRotateButton, ctrlRightRotateButton, flipButton, resetButton, shiftLeftRotateKey, ctrlLeftRotateKey, shiftRightRotateKey, ctrlRightRotateKey, flipKey, resetKey, v, moveDown, moveLeft, moveRight, moveSpeed);
    }

    public KeyBindConfig withMoveDown(KeyCode v) {
        return new KeyBindConfig(togglePlayerList, multiFlip, shiftLeftRotate, ctrlLeftRotate, shiftRightRotate, ctrlRightRotate, doubleLeftFlip, doubleRightReset, shiftLeftRotateButton, ctrlLeftRotateButton, shiftRightRotateButton, ctrlRightRotateButton, flipButton, resetButton, shiftLeftRotateKey, ctrlLeftRotateKey, shiftRightRotateKey, ctrlRightRotateKey, flipKey, resetKey, moveUp, v, moveLeft, moveRight, moveSpeed);
    }

    public KeyBindConfig withMoveLeft(KeyCode v) {
        return new KeyBindConfig(togglePlayerList, multiFlip, shiftLeftRotate, ctrlLeftRotate, shiftRightRotate, ctrlRightRotate, doubleLeftFlip, doubleRightReset, shiftLeftRotateButton, ctrlLeftRotateButton, shiftRightRotateButton, ctrlRightRotateButton, flipButton, resetButton, shiftLeftRotateKey, ctrlLeftRotateKey, shiftRightRotateKey, ctrlRightRotateKey, flipKey, resetKey, moveUp, moveDown, v, moveRight, moveSpeed);
    }

    public KeyBindConfig withMoveRight(KeyCode v) {
        return new KeyBindConfig(togglePlayerList, multiFlip, shiftLeftRotate, ctrlLeftRotate, shiftRightRotate, ctrlRightRotate, doubleLeftFlip, doubleRightReset, shiftLeftRotateButton, ctrlLeftRotateButton, shiftRightRotateButton, ctrlRightRotateButton, flipButton, resetButton, shiftLeftRotateKey, ctrlLeftRotateKey, shiftRightRotateKey, ctrlRightRotateKey, flipKey, resetKey, moveUp, moveDown, moveLeft, v, moveSpeed);
    }

    public KeyBindConfig withMoveSpeed(int v) {
        return new KeyBindConfig(togglePlayerList, multiFlip, shiftLeftRotate, ctrlLeftRotate, shiftRightRotate, ctrlRightRotate, doubleLeftFlip, doubleRightReset, shiftLeftRotateButton, ctrlLeftRotateButton, shiftRightRotateButton, ctrlRightRotateButton, flipButton, resetButton, shiftLeftRotateKey, ctrlLeftRotateKey, shiftRightRotateKey, ctrlRightRotateKey, flipKey, resetKey, moveUp, moveDown, moveLeft, moveRight, v);
    }

    public void save() {
        try {
            Path cfg = Path.of(PlatformPaths.getAppDataDir(), "keybinds.json");
            Files.createDirectories(cfg.getParent());
            String json = new GsonBuilder().setPrettyPrinting().create().toJson(this);
            Files.writeString(cfg, json);
        } catch (IOException e) {
            System.err.println("Failed to save keybinds: " + e.getMessage());
        }
    }

    private static KeyBindConfig load() {
        try {
            Path cfg = Path.of(PlatformPaths.getAppDataDir(), "keybinds.json");
            if (Files.exists(cfg)) {
                String json = Files.readString(cfg);
                return new Gson().fromJson(json, KeyBindConfig.class);
            }
        } catch (IOException e) {
            System.err.println("Failed to load keybinds: " + e.getMessage());
        }
        return defaults();
    }
}
