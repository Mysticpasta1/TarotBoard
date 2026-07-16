package com.mystic.tarotboard.items;

import com.mystic.tarotboard.TarotBoard;
import com.mystic.tarotboard.theming.SuitStyle;
import com.mystic.tarotboard.theming.ThemeConfiguration;
import com.mystic.tarotboard.theming.ThemeManager;
import com.mystic.tarotboard.theming.GuiStyle;
import com.mystic.tarotboard.utils.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
 * a name label, and flip behavior for TarotBoard cards.
 *
 * <p>The face is laid out like a printed playing card: the value and suit pips in the
 * top-left corner and repeated upside down in the bottom-right, with the value/suit
 * names stacked in the middle.</p>
 */
public class Cards {
    private static final Pattern NAME_PATTERN = Pattern.compile("^(?<value>[\\d,a-z,A-Z]+) of (?<suit>[a-z,A-Z]+)$");
    private static final String SYMBOL_ASSET_ROOT = "/com/mystic/tarotboard/assets/";
    private static final Map<String, Image> SYMBOL_CACHE = new HashMap<>();

    /** Card geometry, expressed as fractions of the card's width or height. */
    private static final double CORNER_RADIUS_RATIO = 0.05;
    private static final double PIP_SIZE_RATIO = 0.15;
    private static final double PIP_INSET_X_RATIO = 0.055;
    private static final double PIP_INSET_Y_RATIO = 0.04;
    private static final double NAME_WIDTH_RATIO = 0.9;

    /** Name block font sizes, as multiples of the configured card text size. */
    private static final double VALUE_FONT_RATIO = 1.75;
    private static final double SUIT_FONT_RATIO = 1.15;
    private static final double NOTE_FONT_RATIO = 0.85;
    private static final double MIN_FONT_SIZE = 6;

    /** Scratch node for measuring label widths; only ever touched on the FX thread. */
    private static final Text MEASURE_NODE = new Text();

    private final StackPane cardPane;
    private final ImageView topValueSymbol = new ImageView();
    private final ImageView topSuitSymbol = new ImageView();
    private final ImageView bottomValueSymbol = new ImageView();
    private final ImageView bottomSuitSymbol = new ImageView();
    private final Text valueLabel = new Text();
    private final Text suitLabel = new Text();
    private final Text noteLabel = new Text();
    private final double nameWidth;
    private final ThemeConfiguration fallbackTheme;
    private final List<String> wilds;

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
        nameWidth = width * NAME_WIDTH_RATIO;
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
        roundCorners(cardFrontImageView, width, height);
        roundCorners(cardBackImageView, width, height);
        Text cardNameText = getStyle(text, value, suit, themeConfiguration);
        cardNameText.setBoundsType(TextBoundsType.VISUAL);
        cardNameText.setWrappingWidth(width);
        cardNameText.setTextAlignment(TextAlignment.CENTER);
        cardNameText.setTranslateY(0);
        cardNameText.setVisible(false);
        // The name Text stays the card's identity and its visible/rotate state, which the
        // rest of the app reads and writes as the pane's third child. The face itself is
        // drawn by facePane below and mirrors this node, so the raw string is not painted.
        cardNameText.setOpacity(0);
        cardPane.getChildren().addAll(cardBackImageView, cardFrontImageView, cardNameText);

        // Wrapped in a nested pane so the instanceof-ImageView and instanceof-Text scans
        // that UIUtils runs over the card pane's direct children never mistake the face's
        // pips or labels for the card images and the name node.
        cardPane.getChildren().add(buildFace(width, height, cardNameText));

        cardNameText.textProperty().addListener((obs, oldV, newText) -> applyFace(newText));
        cardNameText.styleProperty().addListener((obs, oldV, newV) -> applyFace(getCardName().getText()));
        applyFace(cardNameText.getText());
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

    /**
     * Builds the face overlay: both corner pip stacks and the centered name block. It
     * follows the name node's visibility and rotation so a flip or a rotated card
     * carries the whole face with it.
     */
    private StackPane buildFace(double width, double height, Text nameText) {
        double pip = width * PIP_SIZE_RATIO;
        for (ImageView view : List.of(topValueSymbol, topSuitSymbol, bottomValueSymbol, bottomSuitSymbol)) {
            configureSymbolView(view, pip);
        }

        VBox topPips = pipStack(topValueSymbol, topSuitSymbol);
        VBox bottomPips = pipStack(bottomValueSymbol, bottomSuitSymbol);
        bottomPips.setRotate(180);

        for (Text label : List.of(valueLabel, suitLabel, noteLabel)) {
            label.setWrappingWidth(nameWidth);
            label.setTextAlignment(TextAlignment.CENTER);
            label.setEffect(new DropShadow(width * 0.03, 0, 1, Color.rgb(0, 0, 0, 0.85)));
        }
        suitLabel.setOpacity(0.85);
        noteLabel.setOpacity(0.7);
        VBox nameBlock = new VBox(valueLabel, suitLabel, noteLabel);
        nameBlock.setAlignment(Pos.CENTER);
        nameBlock.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        StackPane facePane = new StackPane(topPips, bottomPips, nameBlock);
        StackPane.setAlignment(topPips, Pos.TOP_LEFT);
        StackPane.setAlignment(bottomPips, Pos.BOTTOM_RIGHT);
        Insets pipInset = new Insets(height * PIP_INSET_Y_RATIO, width * PIP_INSET_X_RATIO,
                height * PIP_INSET_Y_RATIO, width * PIP_INSET_X_RATIO);
        StackPane.setMargin(topPips, pipInset);
        StackPane.setMargin(bottomPips, pipInset);
        facePane.setMaxSize(width, height);
        facePane.setMouseTransparent(true);
        facePane.visibleProperty().bind(nameText.visibleProperty());
        facePane.rotateProperty().bind(nameText.rotateProperty());
        return facePane;
    }

    /**
     * Stacks a value pip over a suit pip. The box is held to its preferred size so the
     * face's StackPane parks it in a corner instead of stretching it across the card.
     */
    private static VBox pipStack(ImageView valueSymbol, ImageView suitSymbol) {
        VBox pips = new VBox(valueSymbol, suitSymbol);
        pips.setAlignment(Pos.CENTER);
        pips.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        return pips;
    }

    private static void roundCorners(ImageView view, double width, double height) {
        Rectangle clip = new Rectangle(width, height);
        clip.setArcWidth(width * CORNER_RADIUS_RATIO * 2);
        clip.setArcHeight(width * CORNER_RADIUS_RATIO * 2);
        view.setClip(clip);
    }

    private static void configureSymbolView(ImageView view, double size) {
        view.setFitWidth(size);
        view.setFitHeight(size);
        view.setPreserveRatio(true);
        view.setMouseTransparent(true);
    }

    /**
     * Rebuilds the face from the card's display text, whose first line is the logical
     * card name and whose last line is the rank or wild note, tinting everything with
     * the active theme's suit color.
     */
    private void applyFace(String displayText) {
        String[] lines = displayText == null ? new String[0] : displayText.split("\n");
        String logicalName = lines.length > 0 ? lines[0].trim() : "";
        String note = lines.length > 1 ? lines[lines.length - 1].trim() : "";
        List<SuitStyle> styles = activeSuitStyles();

        String value = logicalName;
        String suit = "";
        Image valueImage = null;
        Image suitImage = null;
        String tint = null;

        Matcher matcher = NAME_PATTERN.matcher(logicalName);
        if (wilds.contains(logicalName)) {
            valueImage = loadSymbol("wilds", logicalName);
            tint = styles.isEmpty() ? "white" : styles.getLast().getColorHex();
        } else if (matcher.matches() && !styles.isEmpty()) {
            value = matcher.group("value");
            suit = matcher.group("suit");
            valueImage = loadSymbol("values", value);
            suitImage = loadSymbol("symbols", suit);
            tint = ThemeManager.getSuitColor(suit, styles);
        }

        setSymbol(topValueSymbol, valueImage, tint);
        setSymbol(bottomValueSymbol, valueImage, tint);
        setSymbol(topSuitSymbol, suitImage, tint);
        setSymbol(bottomSuitSymbol, suitImage, tint);

        Color color = webColor(tint);
        double base = GuiStyle.getInstance().cardTextSize();
        setLabel(valueLabel, value.toUpperCase(Locale.ROOT), base * VALUE_FONT_RATIO, true, color);
        setLabel(suitLabel, suit.toUpperCase(Locale.ROOT), base * SUIT_FONT_RATIO, false, color);
        setLabel(noteLabel, note, base * NOTE_FONT_RATIO, false, color);
    }

    private void setLabel(Text label, String content, double preferredSize, boolean bold, Color color) {
        label.setText(content);
        label.setFont(fittedFont(content, bold, preferredSize, nameWidth));
        label.setFill(color);
        // An empty line must not reserve height, or the block drifts off centre.
        label.setVisible(!content.isEmpty());
        label.setManaged(!content.isEmpty());
    }

    /**
     * Returns the requested font, shrunk just enough that the label spans no more than
     * {@code maxWidth}, so long names stay on one line whatever card text size is
     * configured. Rendered width scales roughly with font size, so the measured overflow
     * ratio gives a close first guess; metrics are not perfectly linear, hence the
     * confirming step down until it truly fits.
     */
    private static Font fittedFont(String label, boolean bold, double preferredSize, double maxWidth) {
        FontWeight weight = bold ? FontWeight.BOLD : FontWeight.NORMAL;
        if (label.isEmpty()) {
            return Font.font(null, weight, preferredSize);
        }
        double size = preferredSize;
        double width = measureWidth(label, weight, size);
        if (width > maxWidth) {
            size = Math.max(MIN_FONT_SIZE, size * maxWidth / width);
            while (size > MIN_FONT_SIZE && measureWidth(label, weight, size) > maxWidth) {
                size -= 0.25;
            }
        }
        return Font.font(null, weight, size);
    }

    private static double measureWidth(String label, FontWeight weight, double size) {
        MEASURE_NODE.setText(label);
        MEASURE_NODE.setFont(Font.font(null, weight, size));
        return MEASURE_NODE.getLayoutBounds().getWidth();
    }

    private List<SuitStyle> activeSuitStyles() {
        ThemeConfiguration theme = ThemeManager.getActiveTheme();
        if (theme == null) {
            theme = fallbackTheme;
        }
        return theme != null && theme.getSuitStyles() != null ? theme.getSuitStyles() : List.of();
    }

    private static Color webColor(String tint) {
        if (tint == null) {
            return Color.WHITE;
        }
        try {
            return Color.web(tint);
        } catch (IllegalArgumentException e) {
            return Color.WHITE;
        }
    }

    private static void setSymbol(ImageView view, Image image, String tint) {
        view.setImage(image);
        if (image == null || tint == null) {
            view.setEffect(null);
            return;
        }
        view.setEffect(new Blend(BlendMode.SRC_ATOP, null,
                new ColorInput(0, 0, view.getFitWidth(), view.getFitHeight(), webColor(tint))));
    }

    private static Image loadSymbol(String folder, String name) {
        String key = folder + "/" + name.toLowerCase(Locale.ROOT) + ".png";
        return SYMBOL_CACHE.computeIfAbsent(key, k -> {
            URL url = Cards.class.getResource(SYMBOL_ASSET_ROOT + k);
            return url == null ? null : new Image(url.toExternalForm());
        });
    }
}
