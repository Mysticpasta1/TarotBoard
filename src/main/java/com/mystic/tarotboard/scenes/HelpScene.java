package com.mystic.tarotboard.scenes;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import com.mystic.tarotboard.theming.ThemeConfiguration;
import com.mystic.tarotboard.theming.ThemeManager;
import com.mystic.tarotboard.utils.Styles;

import java.util.List;

public class HelpScene {
    private HelpScene() {
    }

    public static void show(Stage stage) {
        Scene previousScene = stage.getScene();

        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, List.of(TablesExtension.create()));

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();
        String htmlContent = renderer.render(parser.parse(HelpContent.MARKDOWN));

        WebView webView = new WebView();

        ThemeConfiguration.GuiConfig gui = ThemeManager.getActiveTheme().getGui();
        String cssLight = ThemeManager.getCss(gui.helpCssLight);
        String cssDark = ThemeManager.getCss(gui.helpCssDark);
        boolean useThemeCss = !cssLight.isEmpty() && !cssDark.isEmpty();

        boolean[] isDarkMode = {true};

        java.util.function.BiFunction<String, Boolean, String> buildHtml = (css, dark) -> {
            String cls = useThemeCss ? "" : (dark ? "dark-mode" : "");
            return """
                    <html>
                    <head>
                        <style>
                            %s
                        </style>
                    </head>
                    <body class="%s" style="font-family: system-ui; font-size: 13pt; padding: 16px;">
                    %s
                    </body>
                    </html>
                    """.formatted(css, cls, htmlContent);
        };

        webView.getEngine().loadContent(buildHtml.apply(cssDark, true));

        Button backButton = new Button("Back");
        backButton.setStyle(Styles.helpBtn());
        backButton.setOnAction(event -> stage.setScene(previousScene));

        Button toggleThemeButton = new Button("Toggle Theme");
        toggleThemeButton.setStyle(Styles.helpBtn());
        toggleThemeButton.setOnAction(event -> {
            isDarkMode[0] = !isDarkMode[0];
            String nextCss = useThemeCss
                    ? (isDarkMode[0] ? cssDark : cssLight)
                    : cssLight;
            webView.getEngine().loadContent(buildHtml.apply(nextCss, isDarkMode[0]));
        });

        HBox buttonBox = new HBox(10, toggleThemeButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox helpLayout = new VBox(10, webView, buttonBox);
        helpLayout.setSpacing(15);
        helpLayout.setStyle("-fx-padding: 20;");
        helpLayout.setAlignment(Pos.TOP_CENTER);

        webView.prefWidthProperty().bind(helpLayout.widthProperty().subtract(40));
        VBox.setVgrow(webView, Priority.ALWAYS);

        Scene scene = new Scene(helpLayout);
        Styles.applyBackgroundImage(helpLayout);
        stage.setScene(scene);
    }
}
