package com.mystic.tarotboard.gameitems;

import com.mystic.tarotboard.TarotBoard;
import com.mystic.tarotboard.theming.ThemeConfiguration;
import com.mystic.tarotboard.theming.ThemeManager;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;

import java.util.List;

public class Card {
    private final StackPane cardPane;
    private Tooltip cardTooltip;
    private String currentHoverText;

    public Card(String text, String value, String suit, double width, double height, Image cardFrontImage, Image cardBackImage, ThemeConfiguration themeConfiguration, List<String> wilds) {
        cardPane = new StackPane();
        ImageView cardFrontImageView = new ImageView(cardFrontImage);
        ImageView cardBackImageView = new ImageView(cardBackImage);
        cardFrontImageView.setVisible(false);
        cardBackImageView.setVisible(true);
        cardFrontImageView.setFitWidth(width);
        cardFrontImageView.setFitHeight(height);
        cardBackImageView.setFitWidth(width);
        cardBackImageView.setFitHeight(height);
        Text cardNameText = getStyle(text, value, suit, themeConfiguration);
        cardNameText.setBoundsType(TextBoundsType.VISUAL);
        cardNameText.setWrappingWidth(width);
        cardNameText.setTextAlignment(TextAlignment.CENTER);
        cardNameText.setTranslateY(0);
        cardNameText.setVisible(false);
        cardPane.getChildren().addAll(cardBackImageView, cardFrontImageView, cardNameText);

        // Tooltip initialization
        cardTooltip = new Tooltip();
        cardTooltip.setStyle("-fx-font-size: 18pt;");
        Tooltip.install(cardPane, cardTooltip);

        updateHoverTextInternal(text, wilds); // Set initial hover text

        cardPane.setOnMouseEntered(event -> {
            // Check if the card name text is visible (meaning card is face up)
            // The Text object is at index 2 in cardPane's children
            if (getCardName().isVisible()) {
                cardTooltip.setText(currentHoverText);
                cardTooltip.show(cardPane, event.getScreenX(), event.getScreenY() + 5);
            } else {
                cardTooltip.setText("Unknown"); // Or some other placeholder for face-down cards
            }
        });

        cardPane.setOnMouseExited(_ -> cardTooltip.hide());
    }

    public static Text getStyle(String cardText, String value, String suit, ThemeConfiguration themeConfiguration) {
        Text cardNameText = new Text(cardText + "\n \n (" + Math.round(getRank(value)) + ")");
        String color = ThemeManager.getSuitColor(suit, themeConfiguration.getSuitStyles());
        String style = "-fx-font-size: 15pt; -fx-fill: " + color + ";";
        cardNameText.setStyle(style);
        return cardNameText;
    }

    public Text getCardName() {
        // Assuming the Text object is always the third child (index 2)
        return (Text) cardPane.getChildren().get(2);
    }

    public static double getRank(String value) {
        int index = TarotBoard.values.indexOf(value);
        if (index == -1) {
            return Math.PI;
        }
        return index - (double) (TarotBoard.values.size() - 1) / 2;
    }

    public StackPane getCardPane() {
        return cardPane;
    }

    // New method to update card images
    public void updateImages(Image newFrontImage, Image newBackImage) {
        if (cardPane.getChildren().getFirst() instanceof ImageView) {
            ((ImageView) cardPane.getChildren().getFirst()).setImage(newBackImage);
        }
        if (cardPane.getChildren().get(1) instanceof ImageView) {
            ((ImageView) cardPane.getChildren().get(1)).setImage(newFrontImage);
        }
    }

    // Private helper to set the hover text
    private void updateHoverTextInternal(String cardLogicalName, List<String> wilds) {
        if (!wilds.contains(cardLogicalName)) {
            currentHoverText = cardLogicalName.replace("of", "\n");
        } else {
            currentHoverText = cardLogicalName;
        }
        // Update the tooltip's text immediately if it's already created
        if (cardTooltip != null) {
            cardTooltip.setText(currentHoverText);
        }
    }

    // Public method to refresh the tooltip content when card data changes
    public void refreshTooltipContent(String newCardLogicalName, List<String> wilds) {
        updateHoverTextInternal(newCardLogicalName, wilds);
    }
}
