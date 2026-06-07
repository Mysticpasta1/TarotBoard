package com.mystic.tarotboard.items;

import com.mystic.tarotboard.TarotBoard;
import com.mystic.tarotboard.theming.ThemeConfiguration;
import com.mystic.tarotboard.theming.ThemeManager;
import com.mystic.tarotboard.theming.GuiStyle;
import com.mystic.tarotboard.utils.Styles;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;

import java.util.List;

/**
 * Represents a set of playing card visuals in the UI, managing front/back images,
 * a name label, hover tooltips, and flip behavior for TarotBoard cards.
 */
public class Cards {
    private final StackPane cardPane;
    private final Tooltip cardTooltip;
    private String currentHoverText;

    /**
     * Constructs a Cards display with given text, value, suit, dimensions, images, and wilds.
     *
     * @param text               the display text for the card
     * @param value              the card's rank value
     * @param suit               the card's suit
     * @param width              the rendered width of the card
     * @param height             the rendered height of the card
     * @param cardFrontImage     the image for the card's face
     * @param cardBackImage      the image for the card's back
     * @param themeConfiguration the active theme configuration
     * @param wilds              the list of wild card identifiers
     */
    public Cards(String text, String value, String suit, double width, double height, Image cardFrontImage, Image cardBackImage, ThemeConfiguration themeConfiguration, List<String> wilds) {
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

        cardTooltip = new Tooltip();
        cardTooltip.setStyle("-fx-font-size: " + GuiStyle.getInstance().cardTextSize() + "pt;");
        Tooltip.install(cardPane, cardTooltip);

        updateHoverTextInternal(text, wilds);

        cardPane.setOnMouseEntered(event -> {
            if (GuiStyle.getInstance().showCardTooltips()) {
                if (getCardName().isVisible()) {
                    cardTooltip.setText(currentHoverText);
                    cardTooltip.show(cardPane, event.getScreenX(), event.getScreenY() + 5);
                } else {
                    cardTooltip.setText("Unknown");
                }
            } else {
                cardTooltip.setText("");
                cardTooltip.hide();
            }
        });

        cardPane.setOnMouseExited(_ -> cardTooltip.hide());
    }

    /**
     * Creates and styles a {@link Text} node for the card's display name and rank.
     *
     * @param cardText           the card display text
     * @param value              the card's rank value
     * @param suit               the card's suit
     * @param themeConfiguration the active theme configuration
     * @return a styled Text node
     */
    public static Text getStyle(String cardText, String value, String suit, ThemeConfiguration themeConfiguration) {
        Text cardNameText = new Text(cardText + "\n \n (" + Math.round(getRank(value)) + ")");
        String color = ThemeManager.getSuitColor(suit, themeConfiguration.getSuitStyles());
        cardNameText.setStyle(Styles.cardText(color));
        return cardNameText;
    }

    /**
     * Returns the Text node used for the card name, which is always the third child (index 2).
     *
     * @return the card name Text node
     */
    public Text getCardName() {
        return (Text) cardPane.getChildren().get(2);
    }

    /**
     * Calculates a numeric rank from the card's value string.
     *
     * @param value the card value string
     * @return the numeric rank offset from the center of the value list
     */
    public static double getRank(String value) {
        int index = TarotBoard.values.indexOf(value);
        if (index == -1) {
            return Math.PI;
        }
        return index - (double) (TarotBoard.values.size() - 1) / 2;
    }

    /**
     * Returns the StackPane containing all card visuals.
     *
     * @return the card pane
     */
    public StackPane getCardPane() {
        return cardPane;
    }

    /**
     * Updates the front and back card images.
     *
     * @param newFrontImage the new front face image
     * @param newBackImage  the new back face image
     */
    public void updateImages(Image newFrontImage, Image newBackImage) {
        if (cardPane.getChildren().getFirst() instanceof ImageView) {
            ((ImageView) cardPane.getChildren().getFirst()).setImage(newBackImage);
        }
        if (cardPane.getChildren().get(1) instanceof ImageView) {
            ((ImageView) cardPane.getChildren().get(1)).setImage(newFrontImage);
        }
    }

    private void updateHoverTextInternal(String cardLogicalName, List<String> wilds) {
        if (!wilds.contains(cardLogicalName)) {
            currentHoverText = cardLogicalName.replace("of", "\n");
        } else {
            currentHoverText = cardLogicalName;
        }
        if (cardTooltip != null) {
            cardTooltip.setText(currentHoverText);
        }
    }

    /**
     * Refreshes the tooltip content with a new card logical name and wilds list.
     *
     * @param newCardLogicalName the new logical name for the card
     * @param wilds              the list of wild card identifiers
     */
    public void refreshTooltipContent(String newCardLogicalName, List<String> wilds) {
        updateHoverTextInternal(newCardLogicalName, wilds);
    }
}