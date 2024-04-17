package io.smartin.id1212.model.components;

import org.apache.logging.log4j.LogManager;

import com.google.gson.annotations.Expose;

import io.smartin.id1212.exceptions.GameException;
import io.smartin.id1212.exceptions.game.*;
import io.smartin.id1212.model.components.PlayingCard.Value;
import io.smartin.id1212.model.components.comparators.SortByValue;
import io.smartin.id1212.model.managers.TradingManager;
import io.smartin.id1212.model.managers.TrickingManager;
import io.smartin.id1212.model.managers.ScoreManager.BestHandResult;
import io.smartin.id1212.net.dto.GameCreation.OneOpenMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.smartin.id1212.config.Strings.*;

public class Round {
    @Expose
    private GamePhase phase = GamePhase.BEFORE;
    @Expose
    private final Player dealer;
    @Expose
    private Player currentPlayer;
    @Expose
    private Player chicagoTaker;
    @Expose
    private Player winner;
    @Expose
    public final List<Trick> tricks;
    @Expose
    private boolean isFinalTrade = false;

    private final TrickingManager trickingManager;
    private final TradingManager tradingManager;
    private final CardDeck deck = new CardDeck();
    private final ChicagoGame game;
    private int numAskedAboutChicago = 0;

    private final Set<PlayingCard> allCards = new CardDeck().getCards();
    private Player overriddenCurrentPlayer;

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
        for (var player : game.getPlayers()) {
            var cards = deck.drawInitial();
            var hand = new Hand(cards);
            player.setHand(hand);
        }

        if (game.getTradeEligiblePlayers().size() > 0) {
            phase = GamePhase.TRADING;
        } else if (game.getChicagoEligiblePlayers().size() > 0) {
            phase = GamePhase.CHICAGO;
        } else {
            phase = GamePhase.PLAYING;
        }

        currentPlayer = dealer;
        nextTurn();
    }

    public void setWinner(Player player) {
        winner = player;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public record RoundMoveResult(
            Move winningMove,
            Player chicagoTaker,
            boolean isRoundOver,
            boolean isTrickDone,
            boolean isGuaranteedWin,
            List<PlayingCard> finalCards
    ) { }

    RoundMoveResult addMove(Move move)
            throws WaitYourTurnException, InappropriateActionException, IllegalMoveException {
        var player = move.getPlayer();
        checkPhase(GamePhase.PLAYING);
        checkTurn(player);

        var moveResult = trickingManager.handle(move);

        var winningMove = moveResult.winningMove();
        var finalCards = new ArrayList<>(player.getHand().getCards());
        var hasMoreTricks = moveResult.hasMoreTricks();
        var isTrickDone = moveResult.isTrickDone();
        var isPlayerWinning = winningMove.getPlayer().equals(player);
        var chicagoWasLost = hasChicagoCalled() && !winningMove.getPlayer().equals(chicagoTaker);
        var playerGuaranteedToWin = isPlayerWinning && playerIsGuaranteedToWinRound(player, winningMove);

        if (hasMoreTricks && playerGuaranteedToWin) {
            // sort final cards descending (as one would play irl)
            finalCards.sort(new SortByValue(false));

            // add the winning move to the start so client reads: "...made it rain with X, Y, Z" where X is winning move and Y, Z are the finalCards sorted descending
            finalCards.add(0, winningMove.getCard());

            // move remaining cards to played so hand is shown in client (same sorting as above)
            player.getHand().moveAllToPlayed(true);

            // use the final card to get correct score (card will be a 2 if present due to sort order)
            var finalCard = player.getHand().getLastPlayedCard();
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
        var playerLogger = LogManager.getLogger("gtw:" + currentPlayer.getName());
        var gtwLogger = LogManager.getLogger("gtw");

        var playedCards = getPlayedCards();
        var playerPlayedCards = currentPlayer.getHand().getPlayed();
        var ownCards = currentPlayer.getHand().getCards();
        var currentCard = currentMove.getCard();
        var remainingPlayers = trickingManager.currentTrick().getRemainingPlayers();
        var allPlayers = new HashSet<>(game.getPlayers());

        if (someoneElseCouldBeatCard(currentCard, currentPlayer, playedCards, remainingPlayers)) {
            return false;
        }

        for (var card : ownCards) {
            if (someoneElseCouldBeatCard(card, currentPlayer, playedCards, allPlayers)) {
                return false;
            }
        }

        gtwLogger.info("PLAYED CARDS (ALL):");
        playedCards.forEach(gtwLogger::info);
        gtwLogger.info("-------------------------------");

        playerLogger.info("PLAYED CARDS:");
        playerPlayedCards.forEach(playerLogger::info);
        playerLogger.info("-------------------------------");

        playerLogger.info("REMAINING CARDS ON HAND:");
        ownCards.forEach(playerLogger::info);
        playerLogger.info("-------------------------------");

        return true;
    }

    private <T> void logSet(String title, Set<T> set) {
        var logSetLogger = LogManager.getLogger("logSet");
        logSetLogger.info(title);
        set.forEach(logSetLogger::info);
        logSetLogger.info("-------------------------------");
    }

    private boolean someoneElseCouldBeatCard(PlayingCard card, Player currentPlayer, Set<PlayingCard> playedCards,
            Set<Player> eligiblePlayers) {
        var methodLogger = LogManager.getLogger("someoneElseCouldBeatCard:" + card);

        if (card.getValue() == Value.ACE) {
            return false;
        }

        var potentialContenders = trickingManager.playersWithPotentialSuit(card.getSuit());

        logSet("These can still play this trick: " + card, eligiblePlayers);
        logSet("These players might still have suit: " + card.getSuit(), potentialContenders);

        potentialContenders.removeIf(p -> !eligiblePlayers.contains(p));
        potentialContenders.remove(currentPlayer);

        logSet("These players might still win this trick: " + card, potentialContenders);

        if (potentialContenders.isEmpty()) {
            methodLogger.info("Potential contenders empty for {}", card);
            return false;
        }

        var betterCards = getBetterCards(card);
        var ownCards = currentPlayer.getHand().getCards();

        logSet("These cards are better than " + card, betterCards);

        for (var betterCard : betterCards) {
            boolean nobodyCanBeatCard = ownCards.contains(betterCard) || playedCards.contains(betterCard);
            boolean someoneElseMightHaveThisBetterCard = !nobodyCanBeatCard;

            if (someoneElseMightHaveThisBetterCard) {
                methodLogger.info("No guaranteed win. Someone could have {}", betterCard);
                methodLogger.info("Why? Player does not have {} and it has not been played", betterCard);
                return true;
            }
        }

        methodLogger.info("Nobody else could possibly beat {}", card);

        return false;
    }

    private Set<PlayingCard> getBetterCards(PlayingCard card) {
        var betterCards = new HashSet<PlayingCard>();

        for (var otherCard : allCards) {
            if (otherCard.beats(card)) {
                betterCards.add(otherCard);
            }
        }

        return betterCards;
    }

    private Set<PlayingCard> getPlayedCards() {
        var playedCards = new HashSet<PlayingCard>();

        for (var player : game.getPlayers()) {
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
        if (numAskedAboutChicago == game.getChicagoEligiblePlayers().size()) {
            phase = GamePhase.PLAYING;
        }

        nextTurn();
        return phase == GamePhase.PLAYING;
    }

    List<BestHandResult> tradeCards(Player player, Set<PlayingCard> cards)
            throws OutOfCardsException, WaitYourTurnException, InappropriateActionException {
        checkPhase(GamePhase.TRADING);
        checkTurn(player);
        tradingManager.throwCards(player, cards);
        tradingManager.drawCards(player, cards);
        var result =  tradingManager.completeTrade();

        if (result.stream().anyMatch(BestHandResult::isFourOfAKindPending)) {
            System.out.println("overriding current player with " + result.getFirst().player());
            overrideCurrentPlayer(result.getFirst().player());  // can only be 1 best 4 of a kind
        } else {
            nextTurn();
        }

        return result;
    }

    public void overrideCurrentPlayer(Player player) {
        overriddenCurrentPlayer = currentPlayer;
        currentPlayer = player;
    }

    public void throwCards(Player player, Set<PlayingCard> cards) throws WaitYourTurnException, InappropriateActionException {
        checkPhase(GamePhase.TRADING);
        checkTurn(player);
        tradingManager.throwCards(player, cards);
    }

    public PlayingCard getCardForOneOpen(Player player) throws InappropriateActionException, WaitYourTurnException, OutOfCardsException, UnauthorizedTradeException {
        checkPhase(GamePhase.TRADING);
        checkTurn(player);

        if (!isOneOpenAvailable()) {
            throw new UnauthorizedTradeException(NOT_FINAL_TRADE);
        }

        return deck.draw(1).iterator().next();
    }

    public boolean isOneOpenAvailable() {
        var isFinalTrade = tradingManager.maxTradingCyclesReached();
        return game.getRules().oneOpen() != OneOpenMode.FINAL || isFinalTrade;
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
        // used for 4 of a kind decision so the asked player can respond
        // should be discarded as soon as it's the next persons turn
        if (overriddenCurrentPlayer != null) {
            currentPlayer = overriddenCurrentPlayer;
            overriddenCurrentPlayer = null;
        }

        // hack to make sure isFinalTrade is updated before each turn
        isFinalTrade = tradingManager.maxTradingCyclesReached();
        var p = game.getPlayers();
        var currentPlayerIndex = p.indexOf(currentPlayer);
        var offset = 1;

        while (offset <= p.size()) {
            var candidate = p.get((currentPlayerIndex + offset) % p.size());

            if (phase == GamePhase.TRADING && !candidate.canTrade()) {
                offset++;
                continue;
            } else if (phase == GamePhase.CHICAGO && !candidate.canCallChicago()) {
                offset++;
                continue;
            }

            currentPlayer = candidate;
            break;
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
        if (game.getChicagoEligiblePlayers().size() > 0) {
            phase = GamePhase.CHICAGO;
        } else {
            phase = GamePhase.PLAYING;
        }
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public enum GamePhase {
        BEFORE,
        TRADING,
        CHICAGO,
        PLAYING,
        AFTER
    }

    public void abort() {
        setPhase(GamePhase.AFTER);
    }

    public List<BestHandResult> completeOnOpen(Player player, PlayingCard openCard, boolean accepted) throws GameException {
        var finalCard = accepted ? openCard : getCardForOneOpen(player);
        player.giveCards(Set.of(finalCard));
        var result =  tradingManager.completeTrade();

        if (result.stream().anyMatch(BestHandResult::isFourOfAKindPending)) {
            overrideCurrentPlayer(result.getFirst().player()); // can only be 1 best 4 of a kind
        } else {
            nextTurn();
        }

        return result;
    }

    public void completeResetOthersScoreDecision() {
        nextTurn();
    }
}
