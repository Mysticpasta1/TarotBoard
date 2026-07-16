package com.mystic.tarotboard.items;

import com.mystic.tarotboard.TarotBoard;
import com.mystic.tarotboard.theming.SuitStyle;
import com.mystic.tarotboard.theming.ThemeConfiguration;
import com.mystic.tarotboard.theming.ThemeManager;
import com.mystic.tarotboard.theming.GuiStyle;
import com.mystic.tarotboard.utils.Styles;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a set of playing card visuals in the UI, managing front/back images,
 * a name label, hover tooltips, and flip behavior for TarotBoard cards.
 */
public class Cards {
    private static final Pattern NAME_PATTERN = Pattern.compile("^(?<value>[\\d,a-z,A-Z]+) of (?<suit>[a-z,A-Z]+)$");
    private static final String SYMBOL_ASSET_ROOT = "/com/mystic/tarotboard/assets/";
    private static final Map<String, Image> SYMBOL_CACHE = new HashMap<>();

    private final StackPane cardPane;
    private final Tooltip cardTooltip;
    private final ImageView valueSymbolView = new ImageView();
    private final ImageView suitSymbolView = new ImageView();
    private final ThemeConfiguration fallbackTheme;
    private final List<String> wilds;
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
        fallbackTheme = themeConfiguration;
        this.wilds = wilds;
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

        configureSymbolView(valueSymbolView, width * 0.32, -height * 0.28);
        configureSymbolView(suitSymbolView, width * 0.28, height * 0.28);
        // Wrapped in a nested pane so UIUtils' instanceof-ImageView scans over the
        // card pane's direct children never mistake the symbols for the card faces.
        StackPane symbolPane = new StackPane(valueSymbolView, suitSymbolView);
        symbolPane.setMouseTransparent(true);
        symbolPane.visibleProperty().bind(cardNameText.visibleProperty());
        cardPane.getChildren().add(symbolPane);

        cardNameText.textProperty().addListener((_, _, newText) -> applySymbols(newText));
        cardNameText.styleProperty().addListener((_, _, _) -> applySymbols(getCardName().getText()));
        applySymbols(text);

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

    private static void configureSymbolView(ImageView view, double size, double offsetY) {
        view.setFitWidth(size);
        view.setFitHeight(size);
        view.setPreserveRatio(true);
        view.setTranslateY(offsetY);
        view.setMouseTransparent(true);
    }

    /**
     * Resolves and displays the suit/value (or wild) symbol images for the card
     * identified by the first line of the given display text, tinted with the
     * active theme's suit color.
     */
    private void applySymbols(String displayText) {
        String logicalName = displayText == null ? "" : displayText.split("\n", 2)[0].trim();
        List<SuitStyle> styles = activeSuitStyles();

        Image valueImage = null;
        Image suitImage = null;
        String tint = null;

        Matcher matcher = NAME_PATTERN.matcher(logicalName);
        if (wilds.contains(logicalName)) {
            valueImage = loadSymbol("wilds", logicalName);
            tint = styles.isEmpty() ? "white" : styles.getLast().getColorHex();
        } else if (matcher.matches() && !styles.isEmpty()) {
            valueImage = loadSymbol("values", matcher.group("value"));
            suitImage = loadSymbol("symbols", matcher.group("suit"));
            tint = ThemeManager.getSuitColor(matcher.group("suit"), styles);
        }

        setSymbol(valueSymbolView, valueImage, tint);
        setSymbol(suitSymbolView, suitImage, tint);
    }

    private List<SuitStyle> activeSuitStyles() {
        ThemeConfiguration theme = ThemeManager.getActiveTheme();
        if (theme == null) {
            theme = fallbackTheme;
        }
        return theme != null && theme.getSuitStyles() != null ? theme.getSuitStyles() : List.of();
    }

    private static void setSymbol(ImageView view, Image image, String tint) {
        view.setImage(image);
        if (image == null || tint == null) {
            view.setEffect(null);
            return;
        }
        Color color;
        try {
            color = Color.web(tint);
        } catch (IllegalArgumentException e) {
            color = Color.WHITE;
        }
        view.setEffect(new Blend(BlendMode.SRC_ATOP, null,
                new ColorInput(0, 0, view.getFitWidth(), view.getFitHeight(), color)));
    }

    private static Image loadSymbol(String folder, String name) {
        String key = folder + "/" + name.toLowerCase(Locale.ROOT) + ".png";
        return SYMBOL_CACHE.computeIfAbsent(key, k -> {
            URL url = Cards.class.getResource(SYMBOL_ASSET_ROOT + k);
            return url == null ? null : new Image(url.toExternalForm());
        });
    }
}