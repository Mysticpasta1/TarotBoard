package com.mystic.tarotboard.scenes;

import com.mystic.tarotboard.TarotBoard;
import com.mystic.tarotboard.utils.PlatformPaths;
import com.mystic.tarotboard.utils.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

import java.io.File;
import java.util.Objects;

/**
 * Scene containing the main menu with navigation buttons, logo, and update status label.
 */
public class StartScene {
    private final Scene scene;
    private final Pane startBg;
    private final StackPane startContent;

    /**
     * Constructs the start menu scene with all navigation buttons and auto-update check.
     *
     * @param tarotBoard   the main application instance
     * @param primaryStage the primary stage for scene transitions
     * @param baseWidth    reference width for proportional scaling
     * @param baseHeight   reference height for proportional scaling
     */
    public StartScene(TarotBoard tarotBoard, Stage primaryStage, double baseWidth, double baseHeight) {

        VBox startLayout = new VBox(10);
        startLayout.setAlignment(Pos.CENTER);

        Button singlePlayerBtn = new Button("Single Player");
        singlePlayerBtn.setStyle(Styles.menuButton());
        singlePlayerBtn.setPrefWidth(300);
        singlePlayerBtn.setOnAction(event -> {
            tarotBoard.newGame();
            primaryStage.setScene(tarotBoard.getGameScene().getScene());
            primaryStage.setTitle("Game Scene");
        });

        Button multiplayerBtn = new Button("Multiplayer");
        multiplayerBtn.setStyle(Styles.menuButton());
        multiplayerBtn.setPrefWidth(300);
        multiplayerBtn.setOnAction(event -> tarotBoard.switchToMultiplayer());

        Button continueButton = new Button("Continue");
        continueButton.setStyle(Styles.menuButton());
        continueButton.setPrefWidth(300);
        continueButton.setDisable(!new File(PlatformPaths.getSaveFilePath()).exists());
        continueButton.setOnAction(event -> tarotBoard.continueGame());

        Button helpButton = new Button("Help");
        helpButton.setStyle(Styles.menuButton());
        helpButton.setPrefWidth(300);
        helpButton.setOnAction(event -> HelpScene.show(primaryStage));

        Button quitButton = new Button("Quit");
        quitButton.setStyle(Styles.menuButton());
        quitButton.setPrefWidth(300);
        quitButton.setOnAction(event -> primaryStage.close());

        Button settingsBtn = new Button("Settings");
        settingsBtn.setStyle(Styles.menuButton());
        settingsBtn.setPrefWidth(300);
        settingsBtn.setOnAction(event -> SettingsScene.show(primaryStage));

        Label updateStatusLabel = new Label();
        updateStatusLabel.setStyle("-fx-text-fill: white; -fx-font-size: 40; -fx-font-weight: bold;");
        updateStatusLabel.setVisible(false);
        StackPane.setAlignment(updateStatusLabel, Pos.BOTTOM_CENTER);
        StackPane.setMargin(updateStatusLabel, new Insets(0, 0, 30, 0));

        tarotBoard.checkForUpdates(updateStatusLabel);

        ImageView logoView = new ImageView(new Image(Objects.requireNonNull(TarotBoard.class.getResourceAsStream("/com/mystic/tarotboard/assets/logo.png"))));
        logoView.setPreserveRatio(true);
        logoView.setFitWidth(1200);
        logoView.setMouseTransparent(true);
        StackPane.setAlignment(logoView, Pos.TOP_CENTER);
        StackPane.setMargin(logoView, new Insets(100, 0, 0, 0));

        startLayout.getChildren().addAll(singlePlayerBtn, multiplayerBtn, continueButton, settingsBtn, helpButton, quitButton);

        StackPane startRoot = new StackPane();
        startBg = new Pane();
        startContent = new StackPane();
        startContent.setPrefSize(baseWidth, baseHeight);
        startContent.setMaxSize(baseWidth, baseHeight);
        startContent.setMinSize(baseWidth, baseHeight);
        startContent.getChildren().addAll(startLayout, logoView, updateStatusLabel);
        StackPane.setAlignment(logoView, Pos.TOP_CENTER);
        StackPane.setMargin(logoView, new Insets(100, 0, 0, 0));
        startRoot.getChildren().addAll(startBg, startContent);

        scene = new Scene(startRoot);

        Runnable scaleStartContent = () -> {
            double w = scene.getWidth();
            double h = scene.getHeight();
            if (w <= 0 || h <= 0) return;
            double scale = Math.min(w / baseWidth, h / baseHeight);
            scale = Math.clamp(scale, 0.3, 3.0);
            startContent.getTransforms().setAll(new Scale(scale, scale, baseWidth / 2, baseHeight / 2));
        };
        scene.widthProperty().addListener((observableValue, number1, number2) -> scaleStartContent.run());
        scene.heightProperty().addListener((observableValue, number1, number2) -> scaleStartContent.run());
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
     * Returns the background pane for the start menu.
     *
     * @return the start background pane
     */
    public Pane getStartBg() {
        return startBg;
    }
}
