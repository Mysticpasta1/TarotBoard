package com.mystic.tarotboard.scenes;

import com.mystic.tarotboard.theming.ThemeConfiguration;
import com.mystic.tarotboard.theming.ThemeManager;
import com.mystic.tarotboard.theming.configs.KeyBindConfig;
import com.mystic.tarotboard.utils.Styles;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class SettingsScene {
    private SettingsScene() {
    }

    public static void show(Stage stage) {
        show(stage, null);
    }

    private static void show(Stage stage, Scene previousScene) {
        if (previousScene == null) {
            previousScene = stage.getScene();
        }

        ThemeConfiguration.GuiConfig gui = ThemeManager.getActiveTheme().getGui();

        TabPane tabs = new TabPane();
        tabs.setStyle("-fx-background: transparent; -fx-text-fill: " + gui.settingsTextColor + ";");

        Tab keysTab = new Tab("Key Bindings");
        keysTab.setClosable(false);
        keysTab.setContent(createKeyBindsTab(stage, previousScene, gui));

        tabs.getTabs().addAll(keysTab);

        VBox root = new VBox(tabs);
        root.setStyle("-fx-padding: 24;");

        Scene scene = new Scene(root);
        Styles.applyBackgroundImage(root);
        stage.setScene(scene);
    }

    private static VBox createKeyBindsTab(Stage stage, Scene previousScene, ThemeConfiguration.GuiConfig gui) {
        KeyBindConfig config = KeyBindConfig.getInstance();

        VBox bindsBox = new VBox(8);
        bindsBox.setAlignment(Pos.CENTER_LEFT);
        bindsBox.setStyle("-fx-padding: 20;");

        Label title = new Label("Key Bindings");
        title.setStyle("-fx-font-size: 16pt; -fx-font-weight: bold; -fx-text-fill: " + gui.settingsTextColor + ";");
        bindsBox.getChildren().add(title);

        bindsBox.getChildren().add(createBindRow("Toggle Player List", config.togglePlayerList(), stage));

        bindsBox.getChildren().add(createBindRow("Multi-Flip Chips", config.multiFlip(), stage));

        bindsBox.getChildren().add(createMouseActionRow("Rotate Left (-1°)", config.shiftLeftRotateButton(), v -> KeyBindConfig.update(c -> c.withShiftLeftRotateButton(v)), config.shiftLeftRotateKey(), v -> KeyBindConfig.update(c -> c.withShiftLeftRotateKey(v)), config.shiftLeftRotate(), v -> KeyBindConfig.update(c -> c.withShiftLeftRotate(v)), stage));

        bindsBox.getChildren().add(createMouseActionRow("Rotate Left (-90°)", config.ctrlLeftRotateButton(), v -> KeyBindConfig.update(c -> c.withCtrlLeftRotateButton(v)), config.ctrlLeftRotateKey(), v -> KeyBindConfig.update(c -> c.withCtrlLeftRotateKey(v)), config.ctrlLeftRotate(), v -> KeyBindConfig.update(c -> c.withCtrlLeftRotate(v)), stage));

        bindsBox.getChildren().add(createMouseActionRow("Rotate Right (+1°)", config.shiftRightRotateButton(), v -> KeyBindConfig.update(c -> c.withShiftRightRotateButton(v)), config.shiftRightRotateKey(), v -> KeyBindConfig.update(c -> c.withShiftRightRotateKey(v)), config.shiftRightRotate(), v -> KeyBindConfig.update(c -> c.withShiftRightRotate(v)), stage));

        bindsBox.getChildren().add(createMouseActionRow("Rotate Right (+90°)", config.ctrlRightRotateButton(), v -> KeyBindConfig.update(c -> c.withCtrlRightRotateButton(v)), config.ctrlRightRotateKey(), v -> KeyBindConfig.update(c -> c.withCtrlRightRotateKey(v)), config.ctrlRightRotate(), v -> KeyBindConfig.update(c -> c.withCtrlRightRotate(v)), stage));

        bindsBox.getChildren().add(createMouseActionRow("Flip", config.flipButton(), v -> KeyBindConfig.update(c -> c.withFlipButton(v)), config.flipKey(), v -> KeyBindConfig.update(c -> c.withFlipKey(v)), config.doubleLeftFlip(), v -> KeyBindConfig.update(c -> c.withDoubleLeftFlip(v)), stage));

        bindsBox.getChildren().add(createMouseActionRow("Reset Rotation", config.resetButton(), v -> KeyBindConfig.update(c -> c.withResetButton(v)), config.resetKey(), v -> KeyBindConfig.update(c -> c.withResetKey(v)), config.doubleRightReset(), v -> KeyBindConfig.update(c -> c.withDoubleRightReset(v)), stage));

        bindsBox.getChildren().add(createBindRow("Move Up", config.moveUp(), stage));

        bindsBox.getChildren().add(createBindRow("Move Down", config.moveDown(), stage));

        bindsBox.getChildren().add(createBindRow("Move Left", config.moveLeft(), stage));

        bindsBox.getChildren().add(createBindRow("Move Right", config.moveRight(), stage));

        bindsBox.getChildren().add(createSpeedRow(config.moveSpeed(), v -> KeyBindConfig.update(c -> c.withMoveSpeed(v))));

        Button resetBtn = new Button("Reset to Defaults");
        resetBtn.setStyle(Styles.settingsResetBtn());
        resetBtn.setOnAction(_ -> {
            KeyBindConfig.update(_ -> KeyBindConfig.defaults());
            show(stage, previousScene);
        });

        Button backBtn = new Button("Back");
        backBtn.setStyle(Styles.settingsCloseBtn());
        backBtn.setOnAction(_ -> stage.setScene(previousScene));

        HBox btnRow = new HBox(10, resetBtn, backBtn);
        btnRow.setAlignment(Pos.CENTER);
        btnRow.setStyle("-fx-padding: 10 0 0 0;");

        bindsBox.getChildren().add(btnRow);
        return bindsBox;
    }

    private static HBox createBindRow(String actionName, KeyCode current, Stage parent) {
        Circle dot = new Circle(5, Color.WHITE);
        Label nameLabel = new Label(actionName);
        nameLabel.setStyle(Styles.settingsBindName());

        Button keyBtn = new Button(formatKeyName(current));
        keyBtn.setStyle(Styles.settingsBindKey());
        keyBtn.setOnAction(_ -> {
            keyBtn.setText("Press a key...");
            keyBtn.setStyle(Styles.settingsBindKeyRecording());
            parent.addEventFilter(KeyEvent.KEY_PRESSED, new javafx.event.EventHandler<>() {
                @Override
                public void handle(KeyEvent e) {
                    KeyCode code = e.getCode();
                    if (code.isModifierKey()) return;
                    KeyCode bind = code == KeyCode.ESCAPE ? KeyCode.UNDEFINED : code;
                    parent.removeEventFilter(KeyEvent.KEY_PRESSED, this);
                    switch (actionName) {
                        case "Toggle Player List" -> KeyBindConfig.update(c -> c.withTogglePlayerList(bind));
                        case "Multi-Flip Chips" -> KeyBindConfig.update(c -> c.withMultiFlip(bind));
                        case "Move Up" -> KeyBindConfig.update(c -> c.withMoveUp(bind));
                        case "Move Down" -> KeyBindConfig.update(c -> c.withMoveDown(bind));
                        case "Move Left" -> KeyBindConfig.update(c -> c.withMoveLeft(bind));
                        case "Move Right" -> KeyBindConfig.update(c -> c.withMoveRight(bind));
                    }
                    keyBtn.setText(formatKeyName(bind));
                    keyBtn.setStyle(Styles.settingsBindKey());
                    e.consume();
                }
            });
        });

        HBox row = new HBox(12, dot, nameLabel, keyBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static HBox createMouseActionRow(String action, MouseButton button, java.util.function.Consumer<MouseButton> onButton, KeyCode keyCode, java.util.function.Consumer<KeyCode> onKeyCode, boolean enabled, java.util.function.Consumer<Boolean> onToggle, Stage parent) {
        Label actionLabel = new Label(action);
        actionLabel.setStyle("-fx-font-size: 12pt; -fx-text-fill: white; -fx-min-width: 160;");
        ComboBox<MouseButton> btnCombo = new ComboBox<>();
        btnCombo.getItems().addAll(MouseButton.PRIMARY, MouseButton.SECONDARY, MouseButton.MIDDLE);
        btnCombo.setValue(button);
        btnCombo.setOnAction(_ -> onButton.accept(btnCombo.getValue()));
        Button keyBtn = new Button(formatKeyName(keyCode));
        keyBtn.setStyle(Styles.settingsBindKey());
        keyBtn.setOnAction(_ -> {
            keyBtn.setText("Press a key...");
            keyBtn.setStyle(Styles.settingsBindKeyRecording());
            parent.addEventFilter(KeyEvent.KEY_PRESSED, new javafx.event.EventHandler<>() {
                @Override
                public void handle(KeyEvent e) {
                    KeyCode code = e.getCode();
                    if (code.isModifierKey()) return;
                    KeyCode bind = code == KeyCode.ESCAPE ? KeyCode.UNDEFINED : code;
                    parent.removeEventFilter(KeyEvent.KEY_PRESSED, this);
                    onKeyCode.accept(bind);
                    keyBtn.setText(formatKeyName(bind));
                    keyBtn.setStyle(Styles.settingsBindKey());
                    e.consume();
                }
            });
        });
        CheckBox cb = new CheckBox("On");
        cb.setSelected(enabled);
        cb.selectedProperty().addListener((_, _, n) -> onToggle.accept(n));
        HBox row = new HBox(10, actionLabel, btnCombo, keyBtn, cb);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static HBox createSpeedRow(int value, Consumer<Integer> onUpdate) {
        Label l = new Label("Move Speed (px)");
        l.setStyle("-fx-font-size: 12pt; -fx-text-fill: white; -fx-min-width: 160;");
        Spinner<Integer> spin = new Spinner<>(1, 100, value);
        spin.setStyle("-fx-pref-width: 80;");
        spin.valueProperty().addListener((_, _, n) -> onUpdate.accept(n));
        HBox row = new HBox(10, l, spin);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static String formatKeyName(KeyCode code) {
        return switch (code) {
            case UNDEFINED -> "None";
            case TAB -> "Tab";
            case CONTROL -> "Ctrl";
            case SHIFT -> "Shift";
            case ALT -> "Alt";
            case ENTER -> "Enter";
            case ESCAPE -> "Esc";
            case SPACE -> "Space";
            case BACK_SPACE -> "Backspace";
            case DELETE -> "Del";
            case OPEN_BRACKET -> "[";
            case CLOSE_BRACKET -> "]";
            case COMMA -> ",";
            case PERIOD -> ".";
            case SEMICOLON -> ";";
            case QUOTE -> "'";
            case SLASH -> "/";
            case BACK_SLASH -> "\\";
            case MINUS -> "-";
            case EQUALS -> "=";
            default -> code.getName();
        };
    }
}
