package io.smartin.id1212.model.components;

import com.google.gson.annotations.Expose;
import io.smartin.id1212.exceptions.game.*;
import io.smartin.id1212.exceptions.key.AlreadyStartedException;
import io.smartin.id1212.model.managers.ScoreManager;
import io.smartin.id1212.model.store.GamesRepository;
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
    private ScoreManager scoreManager = new ScoreManager(players);

    public ChicagoGame(UUID invitationKey) {
        this.invitationKey = invitationKey;
        this.host = host;
    }

    public void addParticipant(Player player) throws AlreadyStartedException {
        if (started) throw new AlreadyStartedException("That game has already started");
        synchronized (players) {
            players.add(player);
        }
    }

    private void setHost(Player host) {
        this.host = host;
    }

    private void newRound() {
        currentRound = new Round(this, dealer);
        currentRound.start();
    }

    void restart (Player player) throws TooFewPlayersException, UnauthorizedStartException {
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
        started = true;
    }

    public void throwCards(Player player, List<PlayingCard> cards) throws TradeBannedException, WaitYourTurnException, OutOfCardsException, InappropriateActionException {
        if (!player.canTrade() && cards.size() > 0) {
            throw new TradeBannedException(TRADE_BANNED);
        }
        currentRound.throwCards(player, cards);
    }

    public synchronized void playCard(Player player, PlayingCard card) throws IllegalCardException, WaitYourTurnException, InappropriateActionException, IllegalMoveException {
        if (!player.hasCard(card)) {
            throw new IllegalCardException(ILLEGAL_CARD_PLAY);
        }
        currentRound.addMove(new Move(player, card));
    }

    public void callChicago(Player player, boolean answer) throws ChicagoAlreadyCalledException, WaitYourTurnException, InappropriateActionException {
        if (currentRound.hasChicagoCalled()) {
            throw new ChicagoAlreadyCalledException(CHICAGO_ALREADY_CALLED);
        }
        currentRound.setChicagoTaker(player, answer);
    }

    public void finishNormalRound() {
        Move winningMove = currentRound.getFinalTrick().getWinningMove();
        Player winner = winningMove.getPlayer();
        if (winningMove.getCard().getValue().equals(PlayingCard.Value.TWO)) {
            winner.addPoints(WIN_WITH_TWO_SCORE);
        } else {
            winner.addPoints(ROUND_WIN_SCORE);
        }
        scoreManager.givePointsForBestHand();
        playAgainIfPossible();
    }

    public void finishChicagoCalledRound(boolean success, Player player) {
        scoreManager.handleChicagoResult(success, player);
        playAgainIfPossible();
    }

    private void playAgainIfPossible() {
        List<Player> winners = getWinners();
        if (winners.size() > 0) {
            hasWinners = true;
            stopGame();
            return;
        }
        newDealer();
        newRound();
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
            if (player.getName().equals(name)) return true;
        }
        return false;
    }

    public void setInitialPlayer(Player player) {
        players.add(player);
        setHost(player);
    }

    public List<Player> getWinners() {
        List<Player> winners = new ArrayList<>();
        for (Player player : players) {
            if (player.hasWon()) {
                player.setWinner(true);
                winners.add(player);
            }
        }
        return winners;
    }
}
