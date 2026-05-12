package com.mystic.tarotboard.scenes;

import com.mystic.tarotboard.utils.MarkdownToGui;
import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
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

import java.util.List;

public class HelpScene {

    public static void show(Stage stage) {
        Scene previousScene = stage.getScene();
        VBox contentContainer = new VBox(12);
        contentContainer.setPadding(new Insets(20));
        contentContainer.setStyle("-fx-background-color: black;");
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, List.of(TablesExtension.create()));
        Parser parser = Parser.builder(options).build();
        Node document = parser.parse(HelpContent.MARKDOWN);
        ScrollPane scrollPane = new ScrollPane(contentContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: gray;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        MarkdownToGui.render(document, contentContainer, scrollPane);
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