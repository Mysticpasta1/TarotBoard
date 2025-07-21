package com.mystic.tarotboard;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.Objects;

public class CardView extends StackPane {

    public CardView(TarotBoardPoker.Card card) {
        ImageView frontImageView = new ImageView(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/mystic/tarotboard/assets/card_front.png")))
        );
        ImageView backImageView = new ImageView(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/mystic/tarotboard/assets/card_back.png")))
        );

        String textContent = card.isFaceDown() ? "[Face Down]" : card.toString();
        String wrappedText = wrapTextAtSpaces(textContent, 1);
        Label cardLabel = new Label(wrappedText);
        cardLabel.setWrapText(true);
        cardLabel.getStyleClass().add("label");
        cardLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        cardLabel.setTextAlignment(TextAlignment.CENTER);
        cardLabel.setAlignment(Pos.CENTER);
        cardLabel.setMaxWidth(130);

        StackPane.setAlignment(cardLabel, Pos.CENTER);

        // Suit-based color/style classes
        if (card.isFaceDown()) {
            cardLabel.getStyleClass().add("card-facedown");
        } else if (card.isWild()) {
            cardLabel.getStyleClass().add("card-wild");
        } else {
            TarotBoardPoker.CourtSet courtSet = card.getSuit() != null ? card.getSuit().getCourtSet() : null;
            if (courtSet != null) {
                switch (courtSet) {
                    case CELESTIAL -> cardLabel.getStyleClass().add("card-celestial");
                    case UMBRAL    -> cardLabel.getStyleClass().add("card-umbral");
                    case INFERNAL  -> cardLabel.getStyleClass().add("card-infernal");
                    case VERDANT   -> cardLabel.getStyleClass().add("card-verdant");
                    case AETHERIC  -> cardLabel.getStyleClass().add("card-aetheric");
                    default        -> cardLabel.getStyleClass().add("label");
                }
            } else {
                cardLabel.getStyleClass().add("label");
            }
        }

        boolean frontVisible = !card.isFaceDown();
        frontImageView.setVisible(frontVisible);
        backImageView.setVisible(!frontVisible);
        cardLabel.setVisible(frontVisible);

        getChildren().addAll(backImageView, frontImageView, cardLabel);
        setPrefSize(100, 140);
    }

    private String wrapTextAtSpaces(String text, int wordsPerLine) {
        String[] words = text.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            sb.append(words[i]);
            if ((i + 1) % wordsPerLine == 0 && i < words.length - 1) {
                sb.append("\n");
            } else if (i < words.length - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}
