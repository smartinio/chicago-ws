package io.smartin.id1212.model.components;

import com.google.gson.annotations.Expose;
import io.smartin.id1212.config.Rules;
import io.smartin.id1212.exceptions.GameException;
import io.smartin.id1212.exceptions.game.*;
import io.smartin.id1212.exceptions.key.AlreadyStartedException;
import io.smartin.id1212.model.components.Round.RoundMoveResult;
import io.smartin.id1212.model.managers.ScoreManager;
import io.smartin.id1212.model.store.GamesRepository;
import io.smartin.id1212.net.dto.GameCreation.GameRules;
import io.smartin.id1212.net.dto.Snapshot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.smartin.id1212.config.Rules.HAND_SCORES;
import static io.smartin.id1212.config.Rules.MAX_GAME_IDLE_TIME_SECONDS;
import static io.smartin.id1212.config.Strings.*;

public class ChicagoGame {
    @Expose
    private Player host;
    @Expose
    private Player dealer;
    @Expose
    private final String invitationKey;
    @Expose
    private boolean started;
    @Expose
    private boolean hasWinners = false;
    @Expose
    private Round currentRound;
    @Expose
    private final List<Player> players = new ArrayList<>();
    @Expose
    private final List<GameEvent> events = new ArrayList<>();
    @Expose
    public final OneOpen oneOpen = new OneOpen();
    @Expose
    public final ResetOthersScore resetOthersScore = new ResetOthersScore();
    @Expose
    private final GameRules rules;

    private final ScoreManager scoreManager = new ScoreManager(players);
    private final Logger logger = LogManager.getLogger("ChicagoGame");

    private static void setTimeout(Runnable runnable, int delaySeconds) {
        new Thread(() -> {
            try {
                Thread.sleep(delaySeconds * 1000L);
                runnable.run();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void scheduleActivityChecker() {
        var latestEvent = events.get(events.size() - 1);
        var idleTimeMillis = new Date().getTime() - latestEvent.timestamp;
        var idleTimeSeconds = idleTimeMillis / 1000;
        var delaySeconds = 60;

        if (idleTimeSeconds >= MAX_GAME_IDLE_TIME_SECONDS) {
            logger.info("Game with key '{}' is idle. Removing.", invitationKey);
            removeGame();
        } else {
            logger.info("Game with key '{}' was active {}s ago. Checking again in {}s.", invitationKey, idleTimeSeconds,
                    delaySeconds);
            setTimeout(this::scheduleActivityChecker, delaySeconds);
        }
    }

    public ChicagoGame(String invitationKey, Player player, GameRules rules) {
        this.invitationKey = invitationKey;
        this.players.add(player);
        this.host = player;
        this.rules = rules;
        logEvent(GameEvent.createdGame(player));
        scheduleActivityChecker();
    }

    public void addParticipant(Player player) throws AlreadyStartedException {
        if (started)
            throw new AlreadyStartedException("That game has already started");
        synchronized (players) {
            players.add(player);
            logEvent(GameEvent.joinedGame(player));
        }
    }

    private void newHost(Player host) {
        this.host = host;
        logEvent(GameEvent.becameHost(host));
    }

    private void newRound() {
        logEvent(GameEvent.serverNewRound());
        currentRound = new Round(this, dealer);
    }

    private void logEvent(GameEvent event) {
        synchronized (events) {
            events.add(event);
        }
    }

    void restart(Player player) throws TooFewPlayersException, UnauthorizedStartException {
        synchronized (players) {
            for (Player p : players) {
                p.reset();
            }
        }
        hasWinners = false;
        start(player);
    }

    public void start(Player player) throws UnauthorizedStartException, TooFewPlayersException {
        if (!player.equals(host)) {
            throw new UnauthorizedStartException(UNAUTHORIZED_START);
        }
        if (players.size() < 2) {
            throw new TooFewPlayersException(TOO_FEW_PLAYERS);
        }
        dealer = players.get(1);
        newRound();
        currentRound.start();
        started = true;
    }

    public void throwCards(Player player, Set<PlayingCard> cards, boolean isOneOpen)
            throws TradeBannedException, WaitYourTurnException, OutOfCardsException, InappropriateActionException,
            TooManyCardsException, UnauthorizedTradeException {
        if (!player.canTrade() && cards.size() > 0) {
            throw new TradeBannedException(TRADE_BANNED);
        }

        if (cards.size() > Rules.MAX_CARDS_PER_PLAYER) {
            throw new TooManyCardsException(TOO_MANY_CARDS);
        }

        if (!player.getHand().getCards().containsAll(cards)) {
            logger.warn("Received erroneous trade from player '{}'", player);
            throw new UnauthorizedTradeException(UNAUTHORIZED_TRADE);
        }

        if (isOneOpen && cards.size() != 1) {
            throw new UnauthorizedTradeException(UNAUTHORIZED_OPEN_TRADE);
        }

        if (isOneOpen) {
            var openCard = currentRound.getCardForOneOpen(player);
            currentRound.throwCards(player, cards);
            oneOpen.start(player, openCard);
            logEvent(GameEvent.requestedOneOpen(player, openCard));
            return;
        }

        var playersWithBestHand = currentRound.tradeCards(player, cards);
        logEvent(GameEvent.tradedCards(player, cards.size()));

        if (playersWithBestHand.stream().anyMatch(ScoreManager.BestHandResult::isFourOfAKindPending)) {
            // only 1 player can have the best four of a kind at one time
            var deciding = playersWithBestHand.getFirst();
            resetOthersScore.start(deciding.player(), deciding.points());
            logEvent(GameEvent.decidingResetOthersScore(deciding.player(), deciding.points()));
            return;
        }

        for (var result : playersWithBestHand) {
            logEvent(GameEvent.bestHand(result.player(), result.points()));
        }
    }

    public synchronized void playCard(Player player, PlayingCard card)
            throws IllegalCardException, WaitYourTurnException, InappropriateActionException, IllegalMoveException {
        if (!player.hasCard(card)) {
            throw new IllegalCardException(ILLEGAL_CARD_PLAY);
        }

        RoundMoveResult moveResult = currentRound.addMove(new Move(player, card));

        var chicagoTaker = moveResult.chicagoTaker();
        var winningMove = moveResult.winningMove();
        var finalCards = moveResult.finalCards();
        var isTrickDone = moveResult.isTrickDone();
        var isGuaranteedWin = moveResult.isGuaranteedWin();
        var isRoundOver = moveResult.isRoundOver();

        if (isGuaranteedWin) {
            logEvent(GameEvent.wonRoundGuaranteed(player, finalCards));
            showEveryonesCards();
        } else {
            logEvent(GameEvent.playedCard(player, card));
            if (isTrickDone) {
                logEvent(GameEvent.wonTrick(winningMove));
                if (!isRoundOver) {
                    logEvent(GameEvent.serverTrickDone());
                }
            }
        }

        if (isRoundOver) {
            finishRound(chicagoTaker, winningMove);
            newDealer();
        }
    }

    private void finishRound(Player chicagoTaker, Move winningMove) {
        currentRound.setWinner(winningMove.getPlayer());

        if (chicagoTaker != null) {
            finishChicagoCalledRound(winningMove, chicagoTaker);
        } else {
            var result = finishNormalRound(winningMove);
            if (result.isFourOfAKindPending()) return;
        }
        checkIfGameWasWon();
    }

    private void checkIfGameWasWon() {
        var winners = createWinnersIfPossible();
        if (winners.size() > 0) {
            hasWinners = true;
            for (var winner : winners) {
                logEvent(GameEvent.wonGame(winner));
            }
            stopGame();
        }
    }

    private void showEveryonesCards() {
        for (var player : players) {
            player.getHand().moveAllToPlayed(false);
        }
    }

    public void respondToChicago(Player player, boolean isCallingChicago)
            throws ChicagoAlreadyCalledException, WaitYourTurnException, InappropriateActionException {
        if (currentRound.hasChicagoCalled()) {
            throw new ChicagoAlreadyCalledException(CHICAGO_ALREADY_CALLED);
        }

        if (!player.canCallChicago()) {
            throw new InappropriateActionException(CANNOT_CALL_CHICAGO);
        }

        var isDoneAsking = currentRound.respondToChicago(player, isCallingChicago);

        if (isCallingChicago) {
            logEvent(GameEvent.calledChicago(player));
        }

        if (isDoneAsking) {
            // lazy reuse to get a divider in the client. can make separate event later if
            // necessary
            logEvent(GameEvent.serverTrickDone());
        }
    }

    public record FinishRoundResult(boolean isFourOfAKindPending) {}

    public FinishRoundResult finishNormalRound(Move winningMove) {
        var winner = winningMove.getPlayer();
        if (winningMove.getCard().getValue().equals(PlayingCard.Value.TWO)) {
            winner.addPoints(rules.winWithTwoScore());
            logEvent(GameEvent.wonRound(winningMove, rules.winWithTwoScore()));
        } else {
            winner.addPoints(rules.roundWinScore());
            logEvent(GameEvent.wonRound(winningMove, rules.roundWinScore()));
        }

        var preview = scoreManager.previewBestHandResults();

        if (preview.stream().anyMatch(ScoreManager.BestHandResult::isFourOfAKindPending)) {
            var deciding = preview.getFirst();
            resetOthersScore.start(deciding.player(), deciding.points());
            currentRound.overrideCurrentPlayer(deciding.player());
            logEvent(GameEvent.decidingResetOthersScore(deciding.player(), deciding.points()));
            return new FinishRoundResult(true);
        }

        var playersWithBestHand = scoreManager.givePointsForBestHand();

        for (var result : playersWithBestHand) {
            logEvent(GameEvent.bestHand(result.player(), result.points()));
        }

        return new FinishRoundResult(false);
    }

    public void finishChicagoCalledRound(Move winningMove, Player player) {
        var wonAllTricks = player.equals(winningMove.getPlayer());
        var hasBestHand = scoreManager.hasBestHand(player);
        var needsBestHand = rules.chicagoBestHand() && scoreManager.someoneHasBestHand();
        var success = needsBestHand ? wonAllTricks && hasBestHand : wonAllTricks;

        if (success) {
            logEvent(GameEvent.wonChicago(player));
        } else if (!wonAllTricks) {
            var trickWinner = winningMove.getPlayer();
            logEvent(GameEvent.lostChicago(player, trickWinner, "TRICKS"));
        } else {
            var handWinner = scoreManager.getBestHandWinner();
            logEvent(GameEvent.lostChicago(player, handWinner, "HAND"));
        }

        scoreManager.handleChicagoResult(success, player);
    }

    private void newDealer() {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).equals(dealer)) {
                dealer = players.get((i + 1) % players.size());
                break;
            }
        }
    }

    public Snapshot snapshot(Player player) {
        return new Snapshot(player, this);
    }

    public String getInvitationKey() {
        return invitationKey;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getDealer() {
        return dealer;
    }

    public boolean getStarted() {
        return started;
    }

    public Round getCurrentRound() {
        return currentRound;
    }

    public List<Player> getTradeEligiblePlayers() {
        return getFilteredPlayers(Player::canTrade);
    }

    public List<Player> getChicagoEligiblePlayers() {
        return getFilteredPlayers(Player::canCallChicago);
    }

    public List<Player> getFilteredPlayers(Predicate<Player> predicate) {
        return getPlayers().stream().filter(predicate).collect(Collectors.toList());
    }

    public void removePlayer(Player player, boolean kicked) {
        synchronized (players) {
            if (player.equals(dealer)) {
                newDealer();
            }

            players.remove(player);
            logEvent(GameEvent.leftGame(player, kicked));

            if (players.size() < 1) {
                removeGame();
                return;
            }
            if (player.equals(host)) {
                newHost(players.get(0));
            }
            if (players.size() < 2) {
                stopGame();
                return;
            }
            abortRound();
        }
    }

    private void abortRound() {
        if (currentRound != null && !currentRound.isOver()) {
            currentRound.abort();
        }
    }

    private void removeGame() {
        GamesRepository.getInstance().removeGame(this.invitationKey);
    }

    private void stopGame() {
        this.started = false;
        this.currentRound = null;
    }

    public ScoreManager getScoreManager() {
        return scoreManager;
    }

    public boolean hasPlayerWithName(String name) {
        for (Player player : players) {
            if (player.getName().equals(name))
                return true;
        }
        return false;
    }

    public boolean hasPlayerWithId(String playerId) {
        for (Player player : players) {
            if (player.getId().equals(playerId))
                return true;
        }
        return false;
    }

    private List<Player> createWinnersIfPossible() {
        List<Player> winners = new ArrayList<>();
        for (Player player : players) {
            if (player.hasWon()) {
                player.setWinner(true);
                winners.add(player);
            }
        }
        return winners;
    }

    public void dealCards(Player player)
            throws GameOverException, RoundNotFinishedException, UnauthorizedDealerException {
        if (!player.equals(dealer)) {
            throw new UnauthorizedDealerException(UNAUTHORIZED_DEALER);
        }
        if (currentRound != null && !currentRound.isOver() || resetOthersScore.isPending()) {
            throw new RoundNotFinishedException(ROUND_NOT_FINISHED);
        }
        if (hasWinners) {
            throw new GameOverException(GAME_OVER);
        }
        newRound();
        currentRound.start();
    }

    public void sendChatMessage(Player player, String message) throws NotInGameException {
        if (!players.contains(player)) {
            throw new NotInGameException(INVALID_PLAYER);
        }
        logEvent(GameEvent.chatMessage(player, message));
    }

    public Player kickPlayer(Player player, String playerIdToKick) throws NotInGameException, UnauthorizedKickException {
        if (player != host) {
            throw new UnauthorizedKickException(UNAUTHORIZED_KICK);
        }
        var playerToRemove = getPlayer(playerIdToKick);
        removePlayer(playerToRemove, true);
        return playerToRemove;
    }

    public Player getPlayer(String playerId) throws NotInGameException {
        for (var player : players) {
            if (player.getId().equals(playerId)) {
                return player;
            }

        }
        throw new NotInGameException(INVALID_PLAYER);
    }

    public void throwOneOpen(Player player, PlayingCard cardToThrow) throws GameException {
        throwCards(player, Set.of(cardToThrow), true);
    }

    public void respondToOneOpen(Player player, boolean accepted) throws GameException {
        if (!oneOpen.isOpenFor(player)) {
            throw new UnauthorizedTradeException(WAIT_YOUR_TURN);
        }

        var openCard = oneOpen.getCard();

        var playersWithBestHand = currentRound.completeOnOpen(player, openCard, accepted);

        oneOpen.stop();
        logEvent(GameEvent.respondedToOneOpen(player, openCard, accepted));

        if (playersWithBestHand.stream().anyMatch(ScoreManager.BestHandResult::isFourOfAKindPending)) {
            // only 1 player can have the best four of a kind at one time
            var deciding = playersWithBestHand.getFirst();
            resetOthersScore.start(deciding.player(), deciding.points());
            logEvent(GameEvent.decidingResetOthersScore(deciding.player(), deciding.points()));
            return;
        }

        for (var result : playersWithBestHand) {
            logEvent(GameEvent.bestHand(result.player(), result.points()));
        }
    }

    public void respondToResetOthersScore(Player player, boolean accepted) throws InappropriateActionException {
        if (!resetOthersScore.isPendingFor(player)) {
            throw new InappropriateActionException(INAPPROPRIATE_ACTION);
        }

        resetOthersScore.stop();
        logEvent(GameEvent.respondedToResetOthersScore(player, accepted));

        if (accepted) {
            scoreManager.resetOthersScore(player);
        } else {
            var points = HAND_SCORES.get(Hand.HandType.FOUR_OF_A_KIND);
            player.addPoints(points);
            logEvent(GameEvent.bestHand(player, points));

            if (currentRound.isOver()) {
                checkIfGameWasWon();
            }
        }

        currentRound.completeResetOthersScoreDecision();
    }

    public GameRules getRules() {
        return rules;
    }
}
