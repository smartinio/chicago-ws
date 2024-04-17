package io.smartin.id1212.controller;

import io.smartin.id1212.exceptions.game.*;
import io.smartin.id1212.exceptions.key.AlreadyStartedException;
import io.smartin.id1212.exceptions.key.UnknownInvitationKeyException;
import io.smartin.id1212.exceptions.FatalException;
import io.smartin.id1212.exceptions.GameException;
import io.smartin.id1212.exceptions.KeyException;
import io.smartin.id1212.exceptions.NicknameException;
import io.smartin.id1212.model.store.GamesRepository;
import io.smartin.id1212.model.components.Player;
import io.smartin.id1212.net.communication.SessionHandler;
import io.smartin.id1212.net.dto.Action;
import io.smartin.id1212.net.dto.Message;
import io.smartin.id1212.net.dto.Message.MessageType;
import io.smartin.id1212.net.services.Converter;

import java.util.List;

import static io.smartin.id1212.config.Strings.*;
import static io.smartin.id1212.net.dto.Action.ActionType.JOIN_GAME;
import static io.smartin.id1212.net.dto.Action.ActionType.NEW_GAME;
import static io.smartin.id1212.net.dto.Action.ActionType.RECONNECT;
import static io.smartin.id1212.net.dto.Action.ActionType.CHECK_GAME;
import static io.smartin.id1212.net.dto.Action.ActionType.LEAVE_GAME;
import static io.smartin.id1212.net.dto.Action.ActionType.PING;

public class PlayerController {
    private Player player;

    public PlayerController(String sessionId) {
        player = new Player(sessionId);
    }

    public void handleAction(Action action, String sessionId)
            throws GameException, NicknameException, KeyException, FatalException {
        validateAuthority(action);
        System.out.println(action + " -> handling for player: " + player.getId() + ". session: " + sessionId);
        switch (action.type()) {
            case NEW_GAME -> createNewGame(action);
            case JOIN_GAME -> joinGame(action);
            case START_GAME -> startGame();
            case DEAL_CARDS -> dealCards();
            case THROW -> handleThrowAction(action);
            case THROW_ONE_OPEN -> handleThrowOneOpen(action);
            case RESPOND_ONE_OPEN -> handleRespondOneOpen(action);
            case RESPOND_RESET_OTHERS_SCORE -> handleRespondResetOtherScore(action);
            case CHICAGO -> handleChicagoAction(action);
            case MOVE -> handleMoveAction(action);
            case RESTART_GAME -> restartGame();
            case SEND_CHAT_MESSAGE -> sendChatMessage(action);
            case RECONNECT -> rejoin(action, sessionId);
            case KICK_PLAYER -> kickPlayer(action);
            case LEAVE_GAME -> leaveGame(action, sessionId);
            case CHECK_GAME -> checkGame(action, sessionId);
            case PING -> {}
            default -> throw new UnknownActionException(UNKNOWN_ACTION);
        }
    }

    private void checkGame(Action action, String sessionId) {
        try {
            var request = Converter.toRejoinRequest(action.value());
            var key = request.key();
            var oldPlayerId = request.playerId();
            var game = GamesRepository.getInstance().findGame(key);
            var exists = game != null && game.hasPlayerWithId(oldPlayerId);
            var message = new Message(MessageType.CURRENTLY_IN_GAME, Converter.toJson(exists));
            SessionHandler.getInstance().sendMsgToSessionId(sessionId, message);
        } catch (Throwable e) {
            var message = new Message(MessageType.CURRENTLY_IN_GAME, Converter.toJson(false));
            SessionHandler.getInstance().sendMsgToSessionId(sessionId, message);
        }
    }

    private void handleRespondOneOpen(Action action) throws GameException {
        var accepted = Converter.toBoolean(action.value());
        player.respondToOneOpen(accepted);
    }

    private void handleRespondResetOtherScore(Action action) throws GameException {
        var accepted = Converter.toBoolean(action.value());
        player.respondToResetOtherScore(accepted);
    }

    private void handleThrowOneOpen(Action action) throws GameException {
        var cardToThrow = Converter.toCard(action.value());
        player.throwOneOpen(cardToThrow);
    }

    private void rejoin(Action action, String sessionId) throws FatalException {
        try {
            var request = Converter.toRejoinRequest(action.value());
            var key = request.key();
            var playerId = request.playerId();
            var game = GamesRepository.getInstance().findGameForPlayer(key, playerId);
            player = game.getPlayer(playerId);
            player.setSessionId(sessionId);
            player.setConnected(true);
        } catch (UnknownInvitationKeyException | NotInGameException e) {
            throw new FatalException(e.getMessage());
        }
    }

    private void leaveGame(Action action, String sessionId) throws FatalException {
        try {
            var request = Converter.toLeaveRequest(action.value());
            var key = request.key();
            var oldPlayerId = request.playerId();
            var game = GamesRepository.getInstance().findGameForPlayer(key, oldPlayerId);
            player = game.getPlayer(oldPlayerId);
            player.leaveGame();

            if (!player.getSessionId().equals(sessionId)) {
                System.out.println("killing old session (" + player.getSessionId() + ") "
                        + " because its not same as new: " + sessionId);
                SessionHandler.getInstance().kill(player.getSessionId());
                player.setSessionId(sessionId);

                List<Player> players = game.getPlayers();
                for (Player p : players) {
                    System.out.println(p.getId() + " -> remains as player");
                }
            }
        } catch (UnknownInvitationKeyException | NotInGameException e) {
            throw new FatalException(e.getMessage());
        }
    }

    private void kickPlayer(Action action) throws NotInGameException, UnauthorizedKickException {
        var playerIdToKick = action.value();
        var kickedPlayer = player.kickPlayer(playerIdToKick);
        var message = new Message(MessageType.KICKED, "");
        SessionHandler.getInstance().sendMsgToSessionId(kickedPlayer.getSessionId(), message);
    }

    private void sendChatMessage(Action action) throws NotInGameException {
        String message = action.value();
        player.sendChatMessage(message);
    }

    private void restartGame() throws TooFewPlayersException, UnauthorizedStartException {
        player.restartGame();
    }

    private void dealCards() throws GameOverException, RoundNotFinishedException, UnauthorizedDealerException {
        player.dealCards();
    }

    private void createNewGame(Action action) {
        var creation = Converter.toGameCreation(action.value());
        var nickname = creation.nickname();
        var rules = creation.rules();

        player.setName(nickname);
        GamesRepository.getInstance().createGame(player, rules);
    }

    private void joinGame(Action action)
            throws UnknownInvitationKeyException, NicknameException, AlreadyStartedException {
        var request = Converter.toJoinRequest(action.value());
        player.reset();
        player.setName(request.nickname());
        GamesRepository.getInstance().joinGame(player, request.key());
    }

    private void startGame() throws TooFewPlayersException, UnauthorizedStartException {
        player.startGame();
    }

    private void handleThrowAction(Action action) throws TradeBannedException, OutOfCardsException,
            WaitYourTurnException, InappropriateActionException, TooManyCardsException, UnauthorizedTradeException {
        var cards = Converter.toCards(action.value());
        player.throwCards(cards);
    }

    private void handleChicagoAction(Action action)
            throws ChicagoAlreadyCalledException, WaitYourTurnException, InappropriateActionException {
        player.respondToChicago(Boolean.parseBoolean(action.value()));
    }

    private void handleMoveAction(Action action)
            throws IllegalCardException, WaitYourTurnException, InappropriateActionException, IllegalMoveException {
        var card = Converter.toCard(action.value());
        player.playCard(card);
    }

    private void validateAuthority(Action action) throws NotInGameException {
        switch (action.type()) {
            case CHECK_GAME, JOIN_GAME, NEW_GAME, LEAVE_GAME, RECONNECT, PING -> {}
            default -> {
                if (player == null)
                    throw new NotInGameException(INVALID_PLAYER);
                if (player.getGame() == null)
                    throw new NotInGameException(INVALID_PLAYER);
            }
        }
    }

    public Player getPlayer() {
        return player;
    }

    public void setConnected(boolean connected) {
        player.setConnected(connected);
    }

    public void updateSessionId(String sessionId) {
        player.setSessionId(sessionId);
    }

    public void markAsConnected() {
        System.out.println("Marking player " + player.getId() + " aka " + player.getName() + " as connected");
        if (player.getGame() != null) {
            System.out.println("-- They are in game " + player.getGame().getInvitationKey());
        }

        player.setConnected(true);
    }
}
