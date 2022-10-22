package io.smartin.id1212.model.components;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    boolean isOver() {
        return phase == GamePhase.AFTER;
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
        boolean playerGuaranteedToWin = isPlayerWinning && playerIsGuaranteedToWinRound(player, winningMove);
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

    private boolean playerIsGuaranteedToWinRound(Player currentPlayer, Move currentMove) {
        Logger playerLogger = LogManager.getLogger("gtw:" + currentPlayer.getName());
        Logger gtwLogger = LogManager.getLogger("gtw");

        Set<PlayingCard> playedCards = getPlayedCards();
        List<PlayingCard> playerPlayedCards = currentPlayer.getHand().getPlayed();
        Set<PlayingCard> ownCards = currentPlayer.getHand().getCards();
        PlayingCard currentCard = currentMove.getCard();
        Set<Player> remainingPlayers = trickingManager.currentTrick().getRemainingPlayers();
        Set<Player> allPlayers = new HashSet<>(game.getPlayers());

        if (someoneElseCouldBeatCard(currentCard, currentPlayer, playedCards, remainingPlayers)) {
            return false;
        }

        for (PlayingCard card : ownCards) {
            if (someoneElseCouldBeatCard(card, currentPlayer, playedCards, allPlayers)) {
                return false;
            }
        }

        gtwLogger.info("PLAYED CARDS (ALL):");
        playedCards.forEach(gtwLogger::info);

        playerLogger.info("PLAYED CARDS:");
        playerPlayedCards.forEach(playerLogger::info);

        playerLogger.info("REMAINING CARDS ON HAND:");
        ownCards.forEach(playerLogger::info);

        return true;
    }

    private boolean someoneElseCouldBeatCard(PlayingCard card, Player currentPlayer, Set<PlayingCard> playedCards, Set<Player> eligiblePlayers) {
        Logger methodLogger = LogManager.getLogger("someoneElseCouldBeatCard:" + card);

        Set<PlayingCard> ownCards = currentPlayer.getHand().getCards();
        Set<PlayingCard> betterCards = getBetterCards(card);
        Set<Player> potentialContenders = trickingManager.playersWithPotentialSuit(card.getSuit());

        methodLogger.info("Eligible players for {}", card);
        eligiblePlayers.forEach(methodLogger::info);
        methodLogger.info("Potential contenders for {}", card);
        potentialContenders.forEach(methodLogger::info);

        potentialContenders.removeIf(p -> !eligiblePlayers.contains(p));

        if (potentialContenders.isEmpty()) {
            methodLogger.info("Potential contenders empty for {}", card);
            return false;
        }

        for (PlayingCard betterCard : betterCards) {
            boolean nobodyCanBeatCard = ownCards.contains(betterCard) || playedCards.contains(betterCard);
            boolean someoneElseMightHaveThisBetterCard = !nobodyCanBeatCard;

            if (someoneElseMightHaveThisBetterCard) {
                return true;
            }
        }

        methodLogger.info("Nobody else could possibly beat this card");
        methodLogger.info("All better cards are self-owned/already played:");
        betterCards.forEach(methodLogger::info);

        return false;
    }

    private Set<PlayingCard> getBetterCards(PlayingCard card) {
        Logger betterLogger = LogManager.getLogger("getBetterCards");
        Set<PlayingCard> betterCards = new HashSet<>();

        for (PlayingCard otherCard : allCards) {
            if (otherCard.beats(card)) {
                betterLogger.info("{} beats {}", otherCard, card);
                betterCards.add(otherCard);
            }
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

    boolean respondToChicago(Player player, boolean yolo) throws WaitYourTurnException, InappropriateActionException {
        checkPhase(GamePhase.CHICAGO);
        checkTurn(player);
        numAskedAboutChicago++;
        if (yolo) {
            chicagoTaker = player;
            phase = GamePhase.PLAYING;
            return true;
        }
        if (numAskedAboutChicago == game.getPlayers().size()) {
            phase = GamePhase.PLAYING;
        }

        nextTurn();
        return phase == GamePhase.PLAYING;
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
