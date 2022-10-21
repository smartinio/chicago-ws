package io.smartin.id1212.model.components;

import com.google.gson.annotations.Expose;
import io.smartin.id1212.exceptions.game.*;
import io.smartin.id1212.model.managers.TradingManager;
import io.smartin.id1212.model.managers.TrickingManager;
import io.smartin.id1212.model.managers.ScoreManager.BestHandResult;
import io.smartin.id1212.model.managers.TrickingManager.MoveResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.smartin.id1212.config.Strings.*;

public class Round {
    @Expose
    private GamePhase phase = GamePhase.BEFORE;
    @Expose
    private Player dealer;
    @Expose
    private Player currentPlayer;
    @Expose
    private Player chicagoTaker;
    @Expose
    private List<Trick> tricks;

    private TrickingManager trickingManager;
    private TradingManager tradingManager;
    private CardDeck deck = new CardDeck(true);
    private ChicagoGame game;
    private int numAskedAboutChicago = 0;

    private final Set<PlayingCard> allCards = new CardDeck(true).getCards();

    public ChicagoGame getGame() {
        return game;
    }

    Round(ChicagoGame game, Player dealer) {
        this.game = game;
        this.dealer = dealer;
        this.tradingManager = new TradingManager(this);
        this.trickingManager = new TrickingManager(this);
        this.tricks = this.trickingManager.getTricks();
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
        public final boolean isGuaranteedWin;
        public final List<PlayingCard> finalCards;

        public RoundMoveResult(
                Move winningMove,
                Player chicagoTaker,
                boolean isRoundOver,
                boolean isTrickDone,
                boolean isGuaranteedWin,
                List<PlayingCard> finalCards) {
            this.winningMove = winningMove;
            this.chicagoTaker = chicagoTaker;
            this.isRoundOver = isRoundOver;
            this.isTrickDone = isTrickDone;
            this.isGuaranteedWin = isGuaranteedWin;
            this.finalCards = finalCards;
        }
    }

    RoundMoveResult addMove(Move move)
            throws WaitYourTurnException, InappropriateActionException, IllegalMoveException {
        Player player = move.getPlayer();
        checkPhase(GamePhase.PLAYING);
        checkTurn(player);

        MoveResult moveResult = trickingManager.handle(move);

        Move winningMove = moveResult.winningMove;
        boolean hasMoreTricks = moveResult.hasMoreTricks;
        boolean isTrickDone = moveResult.isTrickDone;
        boolean isPlayerWinning = winningMove.getPlayer().equals(player);
        boolean chicagoWasLost = hasChicagoCalled() && !winningMove.getPlayer().equals(chicagoTaker);
        boolean playerGuaranteedToWin = isPlayerWinning && playerIsGuaranteedToWinRound(player);
        List<PlayingCard> finalCards = new ArrayList<>();

        if (hasMoreTricks && playerGuaranteedToWin) {
            finalCards.addAll(player.getHand().getCards());
            finalCards.sort((a, b) -> b.getValue().ordinal() - a.getValue().ordinal());
            finalCards.add(0, move.getCard());
            PlayingCard finalCard = finalCards.get(finalCards.size() - 1);
            winningMove = new Move(player, finalCard);
            setPhase(Round.GamePhase.AFTER);
        }

        if (!hasMoreTricks || chicagoWasLost) {
            setPhase(Round.GamePhase.AFTER);
        }

        if (isTrickDone) {
            nextTurn(winningMove.getPlayer());
        } else {
            nextTurn();
        }

        return new RoundMoveResult(
                winningMove,
                chicagoTaker,
                phase == GamePhase.AFTER,
                isTrickDone,
                playerGuaranteedToWin,
                finalCards);
    }

    private boolean playerIsGuaranteedToWinRound(Player player) {
        Set<PlayingCard> playedCards = getPlayedCards();
        Set<PlayingCard> ownCards = player.getHand().getCards();

        System.out.println("PLAYED CARDS:");
        playedCards.forEach(System.out::println);

        System.out.println("OWN CARDS FOR " + player);
        ownCards.forEach(System.out::println);

        for (PlayingCard card : ownCards) {
            Set<PlayingCard> betterCards = getBetterCards(card);

            System.out.println("BETTER CARDS THAN " + card);
            betterCards.forEach(System.out::println);

            Set<Player> potentialContenders = trickingManager.playersWithPotentialSuit(card.getSuit());
            potentialContenders.remove(player);

            System.out.println("CONTENDERS FOR " + card);
            potentialContenders.forEach(System.out::println);

            if (potentialContenders.isEmpty()) {
                continue;
            }

            for (PlayingCard betterCard : betterCards) {
                if (!ownCards.contains(betterCard) && !playedCards.contains(betterCard)) {
                    return false;
                }
            }
        }

        System.out.println("GUARANTEED PLAYED CARDS BY ALL");
        playedCards.forEach(System.out::println);

        System.out.println("GUARANTEED WIN PLAYED CARDS BY " + player);
        player.getHand().getPlayed().forEach(System.out::println);

        System.out.println("GUARANTEED WIN OWN CARDS FOR " + player);
        ownCards.forEach(System.out::println);

        return true;
    }

    private Set<PlayingCard> getBetterCards(PlayingCard card) {
        Set<PlayingCard> betterCards = new HashSet<>();

        for (PlayingCard otherCard : allCards) {
            if (otherCard.beats(card))
                betterCards.add(otherCard);
        }

        return betterCards;
    }

    private Set<PlayingCard> getPlayedCards() {
        Set<PlayingCard> playedCards = new HashSet<>();

        for (Player player : game.getPlayers()) {
            playedCards.addAll(player.getHand().getPlayed());
        }

        return playedCards;
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

    List<BestHandResult> throwCards(Player player, List<PlayingCard> cards)
            throws OutOfCardsException, WaitYourTurnException, InappropriateActionException {
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
