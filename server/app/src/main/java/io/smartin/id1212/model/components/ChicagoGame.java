package io.smartin.id1212.model.components;

import com.google.gson.annotations.Expose;

import io.smartin.id1212.exceptions.game.*;
import io.smartin.id1212.exceptions.key.AlreadyStartedException;
import io.smartin.id1212.model.components.Round.RoundMoveResult;
import io.smartin.id1212.model.managers.ScoreManager;
import io.smartin.id1212.model.managers.ScoreManager.BestHandResult;
import io.smartin.id1212.model.store.GamesRepository;
import io.smartin.id1212.net.communication.SessionHandler;
import io.smartin.id1212.net.dto.Snapshot;

import java.util.*;

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

    private ScoreManager scoreManager = new ScoreManager(players);

    private static void setTimeout(Runnable runnable, int delay) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            } catch (Exception e) {
                System.err.println(e);
            }
        }).start();
    }

    public ChicagoGame(UUID invitationKey) {
        this.invitationKey = invitationKey;
    }

    public void addParticipant(Player player) throws AlreadyStartedException {
        if (started)
            throw new AlreadyStartedException("That game has already started");
        synchronized (players) {
            players.add(player);
        }
    }

    private void setHost(Player host) {
        this.host = host;
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

    public void throwCards(Player player, List<PlayingCard> cards)
            throws TradeBannedException, WaitYourTurnException, OutOfCardsException, InappropriateActionException {
        if (!player.canTrade() && cards.size() > 0) {
            throw new TradeBannedException(TRADE_BANNED);
        }

        List<BestHandResult> playersWithBestHand = currentRound.throwCards(player, cards);
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
            boolean success = chicagoTaker.equals(winningMove.getPlayer());
            finishChicagoCalledRound(success, chicagoTaker);
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

    public void finishChicagoCalledRound(boolean success, Player player) {
        scoreManager.handleChicagoResult(success, player);
        logEvent(success ? GameEvent.wonChicago(player) : GameEvent.lostChicago(player));
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

    public void removePlayer(Player player) {
        synchronized (players) {
            players.remove(player);
            if (players.size() < 1) {
                removeGame();
                return;
            }
            if (player.equals(host)) {
                setHost(players.get(0));
            }
            if (players.size() < 2) {
                stopGame();
                return;
            }
            newRound();
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

    public void setInitialPlayer(Player player) {
        players.add(player);
        setHost(player);
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
}
