package io.smartin.id1212.model.components;

import com.google.gson.annotations.Expose;

public class Move {
    @Expose
    private Player player;
    @Expose
    private PlayingCard card;

    public Move(Player player, PlayingCard card) {
        this.player = player;
        this.card = card;
    }

    public Move() {
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public PlayingCard getCard() {
        return card;
    }

    public void setCard(PlayingCard card) {
        this.card = card;
    }

    public boolean beats(Move startingMove) {
        return (card.getSuit().equals(startingMove.getCard().getSuit()) &&
                card.getValue().ordinal() > startingMove.getCard().getValue().ordinal());
    }
}
