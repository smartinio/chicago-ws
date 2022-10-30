package io.smartin.id1212.model.components;

import com.google.gson.annotations.Expose;

import io.smartin.id1212.config.Rules;
import io.smartin.id1212.exceptions.GameException;
import io.smartin.id1212.exceptions.game.*;
import io.smartin.id1212.exceptions.key.AlreadyStartedException;
import io.smartin.id1212.model.components.Round.RoundMoveResult;
import io.smartin.id1212.model.managers.ScoreManager;
import io.smartin.id1212.model.managers.ScoreManager.BestHandResult;
import io.smartin.id1212.model.store.GamesRepository;
import io.smartin.id1212.net.communication.SessionHandler;
import io.smartin.id1212.net.dto.Snapshot;
import io.smartin.id1212.net.dto.GameCreation.GameRules;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.smartin.id1212.config.Rules.*;
import static io.smartin.id1212.config.Strings.*;

public class ChicagoGame {
    @Expose
    private Player host;
    @Expose
    private Player dealer;
    @Expose
    private UUID invitationKey;
    @Expose
    private boolean started;
    @Expose
    private boolean alive = true;
    @Expose
    private boolean hasWinners = false;
    @Expose
    private Round currentRound;
    @Expose
    private final List<Player> players = new ArrayList<>();
    @Expose
    private final List<GameEvent> events = new ArrayList<>();
    @Expose
    private final OneOpen oneOpen = new OneOpen();
    @Expose
    private final GameRules rules;

    private ScoreManager scoreManager = new ScoreManager(players);
    private final Logger logger = LogManager.getLogger("ChicagoGame");

    private static void setTimeout(Runnable runnable, int delaySeconds) {
        new Thread(() -> {
            try {
                Thread.sleep(delaySeconds * 1000);
                runnable.run();
            } catch (Exception e) {
                System.err.println(e);
            }
        }).start();
    }

    private void scheduleActivityChecker() {
        var latestEvent = events.get(events.size() - 1);
        var idleTimeMillis = new Date().getTime() - latestEvent.timestamp;
        var idleTimeSeconds = Math.round(idleTimeMillis / 1000);
        var delaySeconds = 60;

        if (idleTimeSeconds >= MAX_GAME_IDLE_TIME_SECONDS) {
            logger.info("Game with key '{}' is idle. Removing.", invitationKey);
            removeGame();
        } else {
            logger.info("Game with key '{}' was active {}s ago. Checking again in {}s.", invitationKey, idleTimeSeconds, delaySeconds);
            setTimeout(this::scheduleActivityChecker, delaySeconds);
        }
    }

    public ChicagoGame(UUID invitationKey, Player player, GameRules rules) {
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

    private <T> void logSet(String title, Set<T> set) {
        logger.info(title);
        set.forEach(logger::info);
        logger.info("-------------------------------");
    }

    public void throwCards(Player player, Set<PlayingCard> cards, boolean isOneOpen)
            throws TradeBannedException, WaitYourTurnException, OutOfCardsException, InappropriateActionException, TooManyCardsException, UnauthorizedTradeException {
        if (!player.canTrade() && cards.size() > 0) {
            throw new TradeBannedException(TRADE_BANNED);
        }

        if (cards.size() > Rules.MAX_CARDS_PER_PLAYER) {
            throw new TooManyCardsException(TOO_MANY_CARDS);
        }

        if (!player.getHand().getCards().containsAll(cards)) {
            logger.warn("Received erroneous trade from player '{}'", player);
            logSet("Invalid trade", cards);
            throw new UnauthorizedTradeException(UNAUTHORIZED_TRADE);
        }

        if (isOneOpen && cards.size() != 1) {
            throw new UnauthorizedTradeException(UNAUTHORIZED_OPEN_TRADE);
        }

        if (isOneOpen) {
            PlayingCard openCard = currentRound.getCardForOneOpen(player);
            currentRound.throwCards(player, cards);
            oneOpen.start(player, openCard);
            logEvent(GameEvent.requestedOneOpen(player, openCard));
            return;
        }

        List<BestHandResult> playersWithBestHand = currentRound.tradeCards(player, cards);
        logEvent(GameEvent.tradedCards(player, cards.size()));

        for (BestHandResult result : playersWithBestHand) {
            logEvent(GameEvent.bestHand(result.player, result.points));
        }
    }

    public synchronized void playCard(Player player, PlayingCard card)
            throws IllegalCardException, WaitYourTurnException, InappropriateActionException, IllegalMoveException {
        if (!player.hasCard(card)) {
            throw new IllegalCardException(ILLEGAL_CARD_PLAY);
        }

        RoundMoveResult moveResult = currentRound.addMove(new Move(player, card));

        Player chicagoTaker = moveResult.chicagoTaker;
        Move winningMove = moveResult.winningMove;
        List<PlayingCard> finalCards = moveResult.finalCards;
        boolean isTrickDone = moveResult.isTrickDone;
        boolean isGuaranteedWin = moveResult.isGuaranteedWin;
        boolean isRoundOver = moveResult.isRoundOver;

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

    // Only use for server driven updates
    private void broadcastSnapshotsAsync() {
        SessionHandler.getInstance().broadcastSnapshots(this);
    }

    private void finishRound(Player chicagoTaker, Move winningMove) {
        if (chicagoTaker != null) {
            finishChicagoCalledRound(winningMove, chicagoTaker);
        } else {
            finishNormalRound(winningMove);
        }
        checkIfGameWasWon();
    }

    private void checkIfGameWasWon() {
        List<Player> winners = createWinnersIfPossible();
        if (winners.size() > 0) {
            hasWinners = true;
            for (Player winner : winners) {
                logEvent(GameEvent.wonGame(winner));
            }
            stopGame();
            return;
        }
    }

    private void showEveryonesCards() {
        for (Player player : players) {
            player.getHand().moveAllToPlayed(false);
        }
    }

    public void respondToChicago(Player player, boolean isCallingChicago)
            throws ChicagoAlreadyCalledException, WaitYourTurnException, InappropriateActionException {
        if (currentRound.hasChicagoCalled()) {
            throw new ChicagoAlreadyCalledException(CHICAGO_ALREADY_CALLED);
        }

        boolean isDoneAsking = currentRound.respondToChicago(player, isCallingChicago);

        if (isCallingChicago) {
            logEvent(GameEvent.calledChicago(player));
        }

        if (isDoneAsking) {
            // lazy reuse to get a divider in the client. can make separate event later if
            // necessary
            logEvent(GameEvent.serverTrickDone());
        }
    }

    public void finishNormalRound(Move winningMove) {
        Player winner = winningMove.getPlayer();
        if (winningMove.getCard().getValue().equals(PlayingCard.Value.TWO)) {
            winner.addPoints(WIN_WITH_TWO_SCORE);
            logEvent(GameEvent.wonRound(winningMove, WIN_WITH_TWO_SCORE));
        } else {
            winner.addPoints(ROUND_WIN_SCORE);
            logEvent(GameEvent.wonRound(winningMove, ROUND_WIN_SCORE));
        }

        List<BestHandResult> playersWithBestHand = scoreManager.givePointsForBestHand();

        for (BestHandResult result : playersWithBestHand) {
            logEvent(GameEvent.bestHand(result.player, result.points));
        }
    }

    public void finishChicagoCalledRound(Move winningMove, Player player) {
        var wonAllTricks = player.equals(winningMove.getPlayer());
        var hasBestHand = scoreManager.hasBestHand(player);
        var success = rules.chicagoBestHand ? wonAllTricks && hasBestHand : wonAllTricks;

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
        return invitationKey.toString();
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getHost() {
        return host;
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

    public void removePlayer(Player player, boolean kicked) {
        synchronized (players) {
            if (player == dealer) {
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
        if (currentRound != null && !currentRound.isOver()) {
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

    public void kickPlayer(Player player, String playerIdToKick) throws NotInGameException, UnauthorizedKickException {
        if (player != host) {
            throw new UnauthorizedKickException(UNAUTHORIZED_KICK);
        }
        Player playerToRemove = getPlayer(playerIdToKick);
        removePlayer(playerToRemove, true);
    }

    public Player getPlayer(String playerId) throws NotInGameException {
        for (Player player : players) {
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

        PlayingCard openCard = oneOpen.getCard();

        List<BestHandResult> playersWithBestHand = currentRound.completeOnOpen(player, openCard, accepted);

        oneOpen.stop();
        logEvent(GameEvent.respondedToOneOpen(player, openCard, accepted));

        for (BestHandResult result : playersWithBestHand) {
            logEvent(GameEvent.bestHand(result.player, result.points));
        }
    }

    public GameRules getRules() {
        return rules;
    }
}
