package com.mystic.tarotboard.utils;

import javafx.collections.ObservableList;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CardDataHelper {

    private static final double CARD_WIDTH = 150;

    public static void addCardNames(ObservableList<String> cardNames, List<String> wilds, List<String> suits, List<String> values) {
        cardNames.addAll(wilds);

        for (String suit : suits) {
            for (String value : values) {
                cardNames.add(value + " of " + suit);
            }
        }
    }

    public static void generateShuffledCardNames(ObservableList<String> cardNames) {
        Collections.shuffle(cardNames);
    }

    public static void generateCardTooltips(String name, Map<String, String> cardTooltips, List<String> wilds) {
        if (!cardTooltips.containsKey(name)) {
            if (!wilds.contains(name)) {
                cardTooltips.put(name, name.replace("of", "\n"));
            } else {
                cardTooltips.put(name, name);
            }
        }
    }

    public static void makeCardTooltip(StackPane pane, String text, boolean reshuffled, Map<String, String> cardTooltips) {
        String hoverText = cardTooltips.get(text);
        if (hoverText != null) {
            Tooltip tooltip = new Tooltip(hoverText);
            tooltip.setStyle("-fx-font-size: 18pt;");
            if (reshuffled) {
                Tooltip.uninstall(pane, tooltip);
            }

            Tooltip.install(pane, tooltip);
            pane.setOnMouseEntered(event -> {
                if (pane.getChildren().size() > 1 && pane.getChildren().get(1).isVisible()) {
                    tooltip.setText(hoverText);
                    tooltip.show(pane, event.getScreenX(), event.getScreenY() + 5);
                } else {
                    tooltip.setText("Unknown");
                }
            });

            pane.setOnMouseExited(_ -> tooltip.hide());
        }
    }

    public static Text getWildCardName(Text cardNameText) {
        cardNameText.setStyle("-fx-font-size: 15pt; -fx-fill: white;");
        cardNameText.setBoundsType(TextBoundsType.VISUAL);
        cardNameText.setWrappingWidth(CARD_WIDTH);
        cardNameText.setTextAlignment(TextAlignment.CENTER);
        cardNameText.setTranslateY(0);
        cardNameText.setVisible(false);
        return cardNameText;
    }
}
