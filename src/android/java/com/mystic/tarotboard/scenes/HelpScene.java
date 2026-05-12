package com.mystic.tarotboard.scenes;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.mystic.tarotboard.theming.ThemeConfiguration;
import com.mystic.tarotboard.theming.ThemeManager;
import com.mystic.tarotboard.utils.Styles;

import java.util.List;

public class HelpScene {
    private HelpScene() {
    }

    private static String stripHtml(String html) {
        return html
                .replaceAll("<[^>]*>", "")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&nbsp;", " ")
                .replaceAll("\\s*\n\\s*", "\n")
                .replaceAll("\n{3,}", "\n\n")
                .strip();
    }

    public static void show(Stage stage) {
        Scene previousScene = stage.getScene();

        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, List.of(TablesExtension.create()));

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();
        String htmlContent = renderer.render(parser.parse(HelpContent.MARKDOWN));
        String plainText = stripHtml(htmlContent);

        TextArea textArea = new TextArea(plainText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        ThemeConfiguration.GuiConfig gui = ThemeManager.getActiveTheme().getGui();
        String cssLight = ThemeManager.getCss(gui.helpCssLight);
        String cssDark = ThemeManager.getCss(gui.helpCssDark);
        boolean useThemeCss = !cssLight.isEmpty() && !cssDark.isEmpty();

        Button backButton = new Button("Back");
        backButton.setStyle(Styles.helpBtn());
        backButton.setOnAction(_ -> stage.setScene(previousScene));

        Button toggleThemeButton = new Button("Toggle Theme");
        toggleThemeButton.setStyle(Styles.helpBtn());
        toggleThemeButton.setOnAction(_ -> {
            if (useThemeCss) {
                boolean isDark = textArea.getScene() != null
                        && textArea.getScene().getRoot().getStyleClass().contains("dark");
                textArea.getScene().getRoot().getStyleClass().removeAll("dark", "light");
                textArea.getScene().getRoot().getStyleClass().add(isDark ? "light" : "dark");
            }
        });

        VBox.setVgrow(textArea, Priority.ALWAYS);

        HBox buttonBox = new HBox(10, toggleThemeButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox helpLayout = new VBox(10, textArea, buttonBox);
        helpLayout.setStyle("-fx-padding: 20;");
        helpLayout.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(helpLayout);
        Styles.applyBackgroundImage(helpLayout);
        stage.setScene(scene);
    }
}
