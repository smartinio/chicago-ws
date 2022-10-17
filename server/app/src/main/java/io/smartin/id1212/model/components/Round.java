package io.smartin.id1212.model.components;

import com.google.gson.annotations.Expose;
import io.smartin.id1212.exceptions.game.*;
import io.smartin.id1212.model.managers.TradingManager;
import io.smartin.id1212.model.managers.TrickingManager;
import io.smartin.id1212.model.managers.ScoreManager.BestHandResult;
import io.smartin.id1212.model.managers.TrickingManager.MoveResult;

import java.util.List;
import java.util.Set;

import static io.smartin.id1212.config.Strings.*;

public class Round {
    private TrickingManager trickingManager = new TrickingManager(this);
    @Expose
    private GamePhase phase = GamePhase.BEFORE;
    @Expose
    private Player dealer;
    @Expose
    private Player currentPlayer;
    @Expose
    private Player chicagoTaker;
    @Expose
    private List<Trick> tricks = trickingManager.getTricks();
    private TradingManager tradingManager = new TradingManager(this);
    private CardDeck deck = new CardDeck(true);
    private ChicagoGame game;
    private int numAskedAboutChicago = 0;

    public ChicagoGame getGame() {
        return game;
    }

    Round(ChicagoGame game, Player dealer) {
        this.game = game;
        this.dealer = dealer;
    }

    void start() {
        for (Player player : game.getPlayers()) {
            Set<PlayingCard> cards = deck.drawInitial();
            Hand hand = new Hand(cards);
            player.setHand(hand);
        }
        currentPlayer = dealer;
        phase = GamePhase.TRADING;
        nextTurn();
    }

    public class RoundMoveResult {
        public final Move winningMove;
        public final Player chicagoTaker;
        public final boolean isRoundOver;
        public final boolean isTrickDone;

        public RoundMoveResult(Move winningMove, Player chicagoTaker, boolean isRoundOver, boolean isTrickDone) {
            this.winningMove = winningMove;
            this.chicagoTaker = chicagoTaker;
            this.isRoundOver = isRoundOver;
            this.isTrickDone = isTrickDone;
        }
    }

    RoundMoveResult addMove(Move move) throws WaitYourTurnException, InappropriateActionException, IllegalMoveException {
        checkPhase(GamePhase.PLAYING);
        checkTurn(move.getPlayer());
        MoveResult moveResult = trickingManager.handle(move);
        Move winningMove = moveResult.winningMove;
        boolean hasMoreTricks = moveResult.hasMoreTricks;
        boolean isTrickDone = moveResult.isTrickDone;
        boolean chicagoWasLost = hasChicagoCalled() && !winningMove.getPlayer().equals(chicagoTaker);

        if (!hasMoreTricks || chicagoWasLost) {
            setPhase(Round.GamePhase.AFTER);
        }

        if (isTrickDone) {
            nextTurn(winningMove.getPlayer());
        } else {
            nextTurn();
        }

        return new RoundMoveResult(winningMove, chicagoTaker, phase == GamePhase.AFTER, isTrickDone);
    }

    void setChicagoTaker(Player player, boolean yolo) throws WaitYourTurnException, InappropriateActionException {
        checkPhase(GamePhase.CHICAGO);
        checkTurn(player);
        numAskedAboutChicago++;
        if (yolo) {
            chicagoTaker = player;
            phase = GamePhase.PLAYING;
            return;
        }
        if (numAskedAboutChicago == game.getPlayers().size()) {
            phase = GamePhase.PLAYING;
        }
        nextTurn();
    }

    List<BestHandResult> throwCards(Player player, List<PlayingCard> cards) throws OutOfCardsException, WaitYourTurnException, InappropriateActionException {
        checkPhase(GamePhase.TRADING);
        checkTurn(player);
        List<BestHandResult> result = tradingManager.handle(player, cards);
        nextTurn();
        return result;
    }

    private void checkPhase(GamePhase gamePhase) throws InappropriateActionException {
        if (!phase.equals(gamePhase)) {
            throw new InappropriateActionException(INAPPROPRIATE_ACTION);
        }
    }

    private void checkTurn(Player player) throws WaitYourTurnException {
        if (!player.equals(currentPlayer)) {
            throw new WaitYourTurnException(WAIT_YOUR_TURN);
        }
    }

    private void nextTurn() {
        List<Player> p = game.getPlayers();
        for (int i = 0; i < p.size(); i++) {
            if (p.get(i).equals(currentPlayer)) {
                currentPlayer = p.get((i + 1) % p.size());
                break;
            }
        }
    }

    private void nextTurn(Player winner) {
        if (winner == null) {
            nextTurn();
            return;
        }
        currentPlayer = winner;
    }

    public boolean hasChicagoCalled() {
        return chicagoTaker != null;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public CardDeck getDeck() {
        return deck;
    }

    public void endTradingPhase() {
        phase = GamePhase.CHICAGO;
    }

    public Trick getFinalTrick() {
        return trickingManager.currentTrick();
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public Player getChicagoTaker() {
        return chicagoTaker;
    }

    public enum GamePhase {
        BEFORE,
        TRADING,
        CHICAGO,
        PLAYING,
        AFTER
    }
}
