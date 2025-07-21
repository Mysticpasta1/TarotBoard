package com.mystic.tarotboard;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.util.Objects;

public class CardView extends StackPane {
    private final TarotBoardPoker.Card card;
    private final ImageView frontImageView;
    private final ImageView backImageView;
    private final Label cardName;

    private boolean frontVisible = true;

    public CardView(TarotBoardPoker.Card card) {
        this.card = card;

        this.frontImageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/mystic/tarotboard/assets/card_front.png"))));
        this.backImageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/mystic/tarotboard/assets/card_back.png"))));

        this.cardName = new Label(card.toString());

        frontImageView.setVisible(true);
        backImageView.setVisible(false);
        cardName.setVisible(true);

        getChildren().addAll(backImageView, frontImageView, cardName);

        setPrefSize(150, 200); // adjust size as needed
    }

    public TarotBoardPoker.Card getCard() {
        return card;
    }

    public ImageView getFrontImageView() {
        return frontImageView;
    }

    public ImageView getBackImageView() {
        return backImageView;
    }

    public Label getCardName() {
        return cardName;
    }

    public void flip() {
        frontVisible = !frontVisible;
        frontImageView.setVisible(frontVisible);
        backImageView.setVisible(!frontVisible);
        cardName.setVisible(frontVisible);
    }
}
