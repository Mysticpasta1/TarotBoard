package com.mystic.tarotboard.scenes;

import com.mystic.tarotboard.TarotBoard;
import com.mystic.tarotboard.utils.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

/**
 * Scene containing the host multiplayer setup UI: player name/color,
 * port selection, operator password, and cursor image picker.
 */
public class HostGameScene {
    private final Scene scene;
    private final Pane mpBg;
    private final StackPane mpContent;
    private final TextField playerNameField;
    private final ColorPicker playerColorPicker;
    private final TextField hostPortField;
    private final PasswordField hostOpPasswordField;
    private final Label cursorStatusLabel;
    private final Label networkStatusLabel;

    /**
     * Constructs the host game scene with all host-specific controls.
     *
     * @param tarotBoard the main application instance
     * @param baseWidth  reference width for proportional scaling
     * @param baseHeight reference height for proportional scaling
     */
    public HostGameScene(TarotBoard tarotBoard, double baseWidth, double baseHeight) {
        playerNameField = new TextField("Player");
        playerNameField.setStyle(Styles.mpField());
        playerNameField.setMaxWidth(200);
        playerColorPicker = new ColorPicker(Color.color(0.2, 0.5, 1.0));
        playerColorPicker.setStyle("-fx-background-color: #2d2d44; -fx-font-size: 12pt;");
        hostPortField = new TextField("5555");
        hostPortField.setStyle(Styles.mpField());
        hostPortField.setMaxWidth(100);
        Button hostGameButton = new Button("Host Game");
        hostGameButton.setStyle(Styles.mpBtn());
        hostGameButton.setOnAction(event -> tarotBoard.hostGame());

        hostOpPasswordField = new PasswordField();
        hostOpPasswordField.setPromptText("Host operator password");
        hostOpPasswordField.setText("admin");
        hostOpPasswordField.setStyle(Styles.mpField());
        hostOpPasswordField.setMaxWidth(200);

        cursorStatusLabel = new Label("Default cursor");
        cursorStatusLabel.setStyle(Styles.mpSmallLabel());
        Button chooseCursorBtn = new Button("Choose Cursor Image");
        chooseCursorBtn.setStyle(Styles.menuSmallBtn());
        chooseCursorBtn.setOnAction(event -> tarotBoard.chooseCursorImage());

        networkStatusLabel = new Label("Offline");
        networkStatusLabel.setStyle(Styles.mpLabel());
        networkStatusLabel.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setFillWidth(true);
        layout.setPadding(new Insets(30));

        Label title = new Label("— Host Game —");
        title.setAlignment(Pos.CENTER);
        title.setStyle(Styles.mpTitle());
        Label nameLbl = new Label("Name:");
        nameLbl.setStyle(Styles.mpLabel());
        Label colorLbl = new Label("Color:");
        colorLbl.setStyle(Styles.mpLabel());
        HBox nameRow = new HBox(10, nameLbl, playerNameField, colorLbl, playerColorPicker);
        nameRow.setAlignment(Pos.CENTER);
        nameRow.setMaxWidth(Double.MAX_VALUE);

        Label portLbl = new Label("Port:");
        portLbl.setStyle(Styles.mpLabel());
        HBox hostRow = new HBox(10, portLbl, hostPortField, hostGameButton);
        hostRow.setAlignment(Pos.CENTER);
        hostRow.setMaxWidth(Double.MAX_VALUE);

        Label hostOpLbl = new Label("Host Op Password:");
        hostOpLbl.setStyle(Styles.mpLabel());
        HBox hostOpRow = new HBox(10, hostOpLbl, hostOpPasswordField);
        hostOpRow.setAlignment(Pos.CENTER);
        hostOpRow.setMaxWidth(Double.MAX_VALUE);

        Label cursorLbl = new Label("Cursor:");
        cursorLbl.setStyle(Styles.mpLabel());
        HBox cursorRow = new HBox(10, cursorLbl, cursorStatusLabel, chooseCursorBtn);
        cursorRow.setAlignment(Pos.CENTER);
        cursorRow.setMaxWidth(Double.MAX_VALUE);

        Button backToStartBtn = new Button("Back to Menu");
        backToStartBtn.setStyle(Styles.mpBtn());
        backToStartBtn.setOnAction(event -> tarotBoard.switchToStart());

        layout.getChildren().addAll(title, nameRow, hostRow, hostOpRow, cursorRow, networkStatusLabel, backToStartBtn);

        StackPane root = new StackPane();
        mpBg = new Pane();
        mpContent = new StackPane();
        mpContent.setPrefSize(baseWidth, baseHeight);
        mpContent.setMaxSize(baseWidth, baseHeight);
        mpContent.setMinSize(baseWidth, baseHeight);
        mpContent.getChildren().add(layout);
        root.getChildren().addAll(mpBg, mpContent);
        scene = new Scene(root);
        addColorPickerCss(scene);

        Runnable scaleContent = () -> {
            double w = scene.getWidth();
            double h = scene.getHeight();
            if (w <= 0 || h <= 0) return;
            double scale = Math.min(w / baseWidth, h / baseHeight);
            scale = Math.clamp(scale, 0.3, 3.0);
            mpContent.getTransforms().setAll(new Scale(scale, scale, baseWidth / 2, baseHeight / 2));
        };
        scene.widthProperty().addListener((observableValue, number1, number2) -> scaleContent.run());
        scene.heightProperty().addListener((observableValue, number1, number2) -> scaleContent.run());
    }

    /**
     * Returns the JavaFX {@link Scene} object.
     *
     * @return the scene
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * Returns the background pane for the host game scene.
     *
     * @return the background pane
     */
    public Pane getMpBg() {
        return mpBg;
    }

    /**
     * Returns the player name text field.
     *
     * @return the player name field
     */
    public TextField getPlayerNameField() {
        return playerNameField;
    }

    /**
     * Returns the player color picker.
     *
     * @return the color picker
     */
    public ColorPicker getPlayerColorPicker() {
        return playerColorPicker;
    }

    /**
     * Returns the host port text field.
     *
     * @return the host port field
     */
    public TextField getHostPortField() {
        return hostPortField;
    }

    /**
     * Returns the host operator password field.
     *
     * @return the operator password field
     */
    public PasswordField getHostOpPasswordField() {
        return hostOpPasswordField;
    }

    /**
     * Returns the label showing the current cursor image filename.
     *
     * @return the cursor status label
     */
    public Label getCursorStatusLabel() {
        return cursorStatusLabel;
    }

    /**
     * Returns the label used for network connection status messages.
     *
     * @return the network status label
     */
    public Label getNetworkStatusLabel() {
        return networkStatusLabel;
    }

    private void addColorPickerCss(Scene s) {
        var url = getClass().getResource("/com/mystic/tarotboard/assets/colorpicker.css");
        if (url != null) s.getStylesheets().add(url.toExternalForm());
    }
}
