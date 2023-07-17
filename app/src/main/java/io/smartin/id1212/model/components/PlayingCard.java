package io.smartin.id1212.model.components;

import com.google.gson.annotations.Expose;

public class PlayingCard {
    @Expose
    private Suit suit;
    @Expose
    private Value value;
    @Expose
    private String shortValue;

    public PlayingCard(Suit suit, Value value) {
        this.suit = suit;
        this.value = value;

        int normalizedOrdinal = this.value.ordinal() + 2;

        if (normalizedOrdinal <= 10) {
            this.shortValue = String.valueOf(normalizedOrdinal);
        } else {
            this.shortValue = this.value.name().substring(0, 1);
        }
    }

    public Suit getSuit() {
        return suit;
    }

    public void setSuit(String suit) {
        this.suit = Suit.valueOf(suit);
    }

    public Value getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = Value.valueOf(value);
    }

    public boolean beats(PlayingCard otherCard) {
        return this.getSuit() == otherCard.getSuit() &&
                this.getValue().ordinal() > otherCard.getValue().ordinal();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        PlayingCard that = (PlayingCard) o;

        if (suit != that.suit)
            return false;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        int result = suit.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return this.suit + ":" + this.value;
    }

    public enum Suit {
        HEARTS,
        SPADES,
        DIAMONDS,
        CLUBS
    }

    public enum Value {
        TWO,
        THREE,
        FOUR,
        FIVE,
        SIX,
        SEVEN,
        EIGHT,
        NINE,
        TEN,
        JACK,
        QUEEN,
        KING,
        ACE
    }
}
