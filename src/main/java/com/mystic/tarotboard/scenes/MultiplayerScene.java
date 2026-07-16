package com.mystic.tarotboard.scenes;

import com.mystic.tarotboard.TarotBoard;
import com.mystic.tarotboard.utils.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.transform.Scale;

/**
 * Scene containing the multiplayer hub with buttons to navigate to
 * the host game scene or the join game scene.
 */
public class MultiplayerScene {
    private final Scene scene;
    private final Pane mpBg;
    private final StackPane mpContent;

    /**
     * Constructs the multiplayer hub scene with host/join navigation buttons.
     *
     * @param tarotBoard the main application instance
     * @param baseWidth  reference width for proportional scaling
     * @param baseHeight reference height for proportional scaling
     */
    public MultiplayerScene(TarotBoard tarotBoard, double baseWidth, double baseHeight) {
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setFillWidth(true);
        layout.setPadding(new Insets(30));

        Label title = new Label("— Multiplayer —");
        title.setAlignment(Pos.CENTER);
        title.setStyle(Styles.mpTitle());

        Button hostGameBtn = new Button("Host Game");
        hostGameBtn.setStyle(Styles.menuButton());
        hostGameBtn.setPrefWidth(300);
        hostGameBtn.setOnAction(event -> tarotBoard.switchToHostGame());

        Button joinGameBtn = new Button("Join Game");
        joinGameBtn.setStyle(Styles.menuButton());
        joinGameBtn.setPrefWidth(300);
        joinGameBtn.setOnAction(event -> tarotBoard.switchToJoinGame());

        Button backBtn = new Button("Back to Menu");
        backBtn.setStyle(Styles.menuButton());
        backBtn.setPrefWidth(300);
        backBtn.setOnAction(event -> tarotBoard.switchToStart());

        layout.getChildren().addAll(title, hostGameBtn, joinGameBtn, backBtn);

        StackPane root = new StackPane();
        mpBg = new Pane();
        mpContent = new StackPane();
        mpContent.setPrefSize(baseWidth, baseHeight);
        mpContent.setMaxSize(baseWidth, baseHeight);
        mpContent.setMinSize(baseWidth, baseHeight);
        mpContent.getChildren().add(layout);
        root.getChildren().addAll(mpBg, mpContent);
        scene = new Scene(root);

        Runnable scaleContent = () -> {
            double w = scene.getWidth();
            double h = scene.getHeight();
            if (w <= 0 || h <= 0) return;
            double scale = Math.min(w / baseWidth, h / baseHeight);
            scale = Math.clamp(scale, 0.3, 3.0);
            mpContent.getTransforms().setAll(new Scale(scale, scale, baseWidth / 2, baseHeight / 2));
        };
        scene.widthProperty().addListener((obs, oldV, newV) -> scaleContent.run());
        scene.heightProperty().addListener((obs, oldV, newV) -> scaleContent.run());
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
     * Returns the background pane for the multiplayer hub scene.
     *
     * @return the background pane
     */
    public Pane getMpBg() {
        return mpBg;
    }
}
