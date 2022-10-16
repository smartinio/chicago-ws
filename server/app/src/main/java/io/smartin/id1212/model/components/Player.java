package io.smartin.id1212.model.components;

import com.google.gson.annotations.Expose;
import io.smartin.id1212.exceptions.game.*;

import java.util.List;
import java.util.Set;

import static io.smartin.id1212.config.Rules.MAX_GAME_SCORE;
import static io.smartin.id1212.config.Rules.TRADE_BAN_SCORE;

public class Player {
    private ChicagoGame game;
    @Expose
    private final String id;
    @Expose
    private String name;
    @Expose
    private Hand hand;
    @Expose
    private int score;
    @Expose
    private boolean hasTakenChicago;
    @Expose
    private boolean winner = false;

    public Player(String id) {
        this.game = game;
        this.id = id;
    }

    public ChicagoGame getGame() {
        return game;
    }

    public void startGame() throws TooFewPlayersException, UnauthorizedStartException {
        game.start(this);
    }

    public void restartGame() throws TooFewPlayersException, UnauthorizedStartException {
        game.restart(this);
    }

    public void throwCards(List<PlayingCard> cards) throws TradeBannedException, WaitYourTurnException, OutOfCardsException, InappropriateActionException {
        game.throwCards(this, cards);
    }

    public void callChicago(boolean answer) throws ChicagoAlreadyCalledException, WaitYourTurnException, InappropriateActionException {
        game.callChicago(this, answer);
    }

    public void playCard(PlayingCard card) throws IllegalCardException, WaitYourTurnException, InappropriateActionException, IllegalMoveException {
        game.playCard(this, card);
    }

    void setHand(Hand hand) {
        this.hand = hand;
    }

    public boolean hasCard(PlayingCard card) {
        return hand.contains(card);
    }

    public void removeCard(PlayingCard card) {
        hand.remove(card);
    }

    public void addPoints(int points) {
            score += points;
    }

    public void removePoints(int points) {
        score -= points;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean getHasTakenChicago() {
        return hasTakenChicago;
    }

    public void setHasTakenChicago(boolean chicago) {
        this.hasTakenChicago = chicago;
    }

    public boolean canTrade() {
        return score < TRADE_BAN_SCORE;
    }

    public void removeCards(List<PlayingCard> cards) {
        hand.removeAll(cards);
    }

    public void giveCards(Set<PlayingCard> draw) {
        hand.addAll(draw);
    }

    public Hand getHand() {
        return hand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        return id.equals(player.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public boolean hasCardOfSuit(PlayingCard.Suit suit) {
        for (PlayingCard playingCard : hand.getCards()) {
            if (playingCard.getSuit().equals(suit)) {
                return true;
            }
        }
        return false;
    }

    public void leaveGame() {
        game.removePlayer(this);
    }

    public void setGame(ChicagoGame game) {
        this.game = game;
    }

    public boolean isInGame() {
        return game != null;
    }

    public boolean hasWon() {
        return score >= MAX_GAME_SCORE;
    }

    public void setWinner(boolean winner) {
        this.winner = winner;
    }

    public void reset() {
        score = 0;
        hasTakenChicago = false;
        winner = false;
    }
}
