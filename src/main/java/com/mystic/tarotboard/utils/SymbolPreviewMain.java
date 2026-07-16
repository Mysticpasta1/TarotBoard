package com.mystic.tarotboard.utils;

import com.mystic.tarotboard.items.Cards;
import com.mystic.tarotboard.theming.ThemeManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * Temporary verification harness: renders a few Cards face-up and writes a PPM
 * snapshot so the suit/value/wild symbol integration can be checked visually.
 */
public final class SymbolPreviewMain {
    private SymbolPreviewMain() {
    }

    public static void main(String[] args) {
        String out = args.length > 0 ? args[0] : "symbol_preview.ppm";
        Platform.startup(() -> {
            try {
                String base = "/com/mystic/tarotboard/assets/";
                Image front = new Image(Objects.requireNonNull(SymbolPreviewMain.class.getResource(base + "card_front.png")).toExternalForm());
                Image back = new Image(Objects.requireNonNull(SymbolPreviewMain.class.getResource(base + "card_back.png")).toExternalForm());
                var theme = ThemeManager.getActiveTheme();
                List<String> wilds = List.of("Joker", "Entropy");

                Cards c1 = new Cards("King of Hearts", "King", "Hearts", 150, 200, front, back, theme, wilds);
                Cards c2 = new Cards("Reaper of Grims", "Reaper", "Grims", 150, 200, front, back, theme, wilds);
                Cards c3 = new Cards("7 of Waves", "7", "Waves", 150, 200, front, back, theme, wilds);
                Cards c4 = new Cards("Joker", "", "", 150, 200, front, back, theme, wilds);
                Cards faceDown = new Cards("Ace of Stars", "Ace", "Stars", 150, 200, front, back, theme, wilds);

                for (Cards c : List.of(c1, c2, c3, c4)) {
                    var pane = c.getCardPane();
                    pane.getChildren().get(0).setVisible(false);
                    pane.getChildren().get(1).setVisible(true);
                    pane.getChildren().get(2).setVisible(true);
                }

                HBox box = new HBox(20, c1.getCardPane(), c2.getCardPane(), c3.getCardPane(), c4.getCardPane(), faceDown.getCardPane());
                box.setPadding(new Insets(20));
                box.setStyle("-fx-background-color: #1a1a2e;");
                new Scene(box);
                WritableImage img = box.snapshot(null, null);
                writePpm(img, out);
                System.out.println("snapshot written: " + out);
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                Platform.exit();
            }
        });
    }

    private static void writePpm(WritableImage img, String path) throws Exception {
        int w = (int) img.getWidth();
        int h = (int) img.getHeight();
        var reader = img.getPixelReader();
        try (var os = new BufferedOutputStream(new FileOutputStream(path))) {
            os.write(("P6\n" + w + " " + h + "\n255\n").getBytes(StandardCharsets.US_ASCII));
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int argb = reader.getArgb(x, y);
                    os.write((argb >> 16) & 0xFF);
                    os.write((argb >> 8) & 0xFF);
                    os.write(argb & 0xFF);
                }
            }
        }
    }
}
