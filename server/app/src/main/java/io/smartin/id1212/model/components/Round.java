package io.smartin.id1212.model.components;

import io.smartin.id1212.config.Rules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.annotations.Expose;

import io.smartin.id1212.exceptions.GameException;
import io.smartin.id1212.exceptions.game.*;
import io.smartin.id1212.model.components.PlayingCard.Value;
import io.smartin.id1212.model.components.comparators.SortByValue;
import io.smartin.id1212.model.managers.TradingManager;
import io.smartin.id1212.model.managers.TrickingManager;
import io.smartin.id1212.model.managers.ScoreManager.BestHandResult;
import io.smartin.id1212.model.managers.TrickingManager.MoveResult;
import io.smartin.id1212.net.dto.GameCreation.OneOpenMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    @Expose
    private boolean isFinalTrade = false;

    private TrickingManager trickingManager;
    private TradingManager tradingManager;
    private CardDeck deck = new CardDeck();
    private ChicagoGame game;
    private int numAskedAboutChicago = 0;

    private final Set<PlayingCard> allCards = new CardDeck().getCards();

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

        if (game.getTradeEligiblePlayers().size() == 0) {
            phase = GamePhase.CHICAGO;
        } else {
            phase = GamePhase.TRADING;
        }

        currentPlayer = dealer;
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
        List<PlayingCard> finalCards = new ArrayList<>(player.getHand().getCards());
        boolean hasMoreTricks = moveResult.hasMoreTricks;
        boolean isTrickDone = moveResult.isTrickDone;
        boolean isPlayerWinning = winningMove.getPlayer().equals(player);
        boolean chicagoWasLost = hasChicagoCalled() && !winningMove.getPlayer().equals(chicagoTaker);
        boolean playerGuaranteedToWin = isPlayerWinning && playerIsGuaranteedToWinRound(player, winningMove);

        if (hasMoreTricks && playerGuaranteedToWin) {
            // sort final cards descending (as one would play irl)
            finalCards.sort(new SortByValue(false));

            // add the winning move to the start so client reads: "..made it rain with X, Y, Z" where X is winning move and Y, Z are the finalCards sorted descending
            finalCards.add(0, winningMove.getCard());

            // move remaining cards to played so hand is shown in client (same sorting as above)
            player.getHand().moveAllToPlayed(true);

            // use the final card to get correct score (card will be a 2 if present due to sort order)
            PlayingCard finalCard = player.getHand().getLastPlayedCard();
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
        Logger logSetLogger = LogManager.getLogger("logSet");
        logSetLogger.info(title);
        set.forEach(logSetLogger::info);
        logSetLogger.info("-------------------------------");
    }

    private boolean someoneElseCouldBeatCard(PlayingCard card, Player currentPlayer, Set<PlayingCard> playedCards,
            Set<Player> eligiblePlayers) {
        Logger methodLogger = LogManager.getLogger("someoneElseCouldBeatCard:" + card);

        if (card.getValue() == Value.ACE) {
            return false;
        }

        Set<Player> potentialContenders = trickingManager.playersWithPotentialSuit(card.getSuit());

        logSet("These can still play this trick: " + card, eligiblePlayers);
        logSet("These players might still have suit: " + card.getSuit(), potentialContenders);

        potentialContenders.removeIf(p -> !eligiblePlayers.contains(p));
        potentialContenders.remove(currentPlayer);

        logSet("These players might still win this trick: " + card, potentialContenders);

        if (potentialContenders.isEmpty()) {
            methodLogger.info("Potential contenders empty for {}", card);
            return false;
        }

        Set<PlayingCard> betterCards = getBetterCards(card);
        Set<PlayingCard> ownCards = currentPlayer.getHand().getCards();

        logSet("These cards are better than " + card, betterCards);

        for (PlayingCard betterCard : betterCards) {
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
        Set<PlayingCard> betterCards = new HashSet<>();

        for (PlayingCard otherCard : allCards) {
            if (otherCard.beats(card)) {
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

    List<BestHandResult> tradeCards(Player player, Set<PlayingCard> cards)
            throws OutOfCardsException, WaitYourTurnException, InappropriateActionException, UnauthorizedTradeException {
        checkPhase(GamePhase.TRADING);
        checkTurn(player);
        tradingManager.throwCards(player, cards);
        tradingManager.drawCards(player, cards);
        List<BestHandResult> result =  tradingManager.completeTrade();
        nextTurn();
        return result;
    }

    public void throwCards(Player player, Set<PlayingCard> cards) throws WaitYourTurnException, InappropriateActionException, OutOfCardsException {
        checkPhase(GamePhase.TRADING);
        checkTurn(player);
        tradingManager.throwCards(player, cards);
    }

    public PlayingCard getCardForOneOpen(Player player) throws InappropriateActionException, WaitYourTurnException, OutOfCardsException, UnauthorizedTradeException {
        checkPhase(GamePhase.TRADING);
        checkTurn(player);

        var isFinalTrade = tradingManager.maxTradingCyclesReached();
        if (game.getRules().oneOpen == OneOpenMode.FINAL && !isFinalTrade) {
            throw new UnauthorizedTradeException(NOT_FINAL_TRADE);
        }

        return deck.draw(1).iterator().next();
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
        // hack to make sure isFinalTrade is updated before each turn
        isFinalTrade = tradingManager.maxTradingCyclesReached();
        List<Player> p = game.getPlayers();
        int currentPlayerIndex = p.indexOf(currentPlayer);
        int offset = 1;

        while (offset <= p.size()) {
            Player candidate = p.get((currentPlayerIndex + offset) % p.size());

            if (phase == GamePhase.TRADING && !candidate.canTrade()) {
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

    public void abort() {
        setPhase(GamePhase.AFTER);
    }

    public List<BestHandResult> completeOnOpen(Player player, PlayingCard openCard, boolean accepted) throws GameException {
        PlayingCard finalCard = accepted ? openCard : getCardForOneOpen(player);
        player.giveCards(Set.of(finalCard));
        List<BestHandResult> result =  tradingManager.completeTrade();
        nextTurn();
        return result;
    }
}
