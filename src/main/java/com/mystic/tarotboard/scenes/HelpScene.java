package com.mystic.tarotboard.scenes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.mystic.tarotboard.utils.Styles;

public class HelpScene {

    public static void show(Stage stage) {
        Scene previousScene = stage.getScene();
        VBox contentContainer = new VBox(12);
        contentContainer.setPadding(new Insets(20));
        contentContainer.setStyle("-fx-background-color: black;");
        
        HelpContent.buildContent(contentContainer);
        
        ScrollPane scrollPane = new ScrollPane(contentContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: gray;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Button backButton = new Button("Back");
        backButton.setStyle(Styles.helpBtn());
        backButton.setOnAction(event -> stage.setScene(previousScene));

        HBox buttonBox = new HBox(backButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15));

        VBox layout = new VBox(scrollPane, buttonBox);
        layout.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(layout, 450, 750);
        Styles.applyBackgroundImage(layout);
        stage.setScene(scene);
    }
}