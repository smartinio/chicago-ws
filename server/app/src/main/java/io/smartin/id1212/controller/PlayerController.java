package io.smartin.id1212.controller;

import io.smartin.id1212.exceptions.game.*;
import io.smartin.id1212.exceptions.key.AlreadyStartedException;
import io.smartin.id1212.exceptions.key.UnknownInvitationKeyException;
import io.smartin.id1212.exceptions.FatalException;
import io.smartin.id1212.exceptions.GameException;
import io.smartin.id1212.exceptions.KeyException;
import io.smartin.id1212.exceptions.NicknameException;
import io.smartin.id1212.model.store.GamesRepository;
import io.smartin.id1212.model.components.ChicagoGame;
import io.smartin.id1212.model.components.Player;
import io.smartin.id1212.model.components.PlayingCard;
import io.smartin.id1212.net.communication.SessionHandler;
import io.smartin.id1212.net.dto.Action;
import io.smartin.id1212.net.dto.JoinRequest;
import io.smartin.id1212.net.dto.LeaveRequest;
import io.smartin.id1212.net.dto.Message;
import io.smartin.id1212.net.dto.RejoinRequest;
import io.smartin.id1212.net.dto.Message.MessageType;
import io.smartin.id1212.net.services.Converter;

import java.util.List;

import static io.smartin.id1212.config.Strings.*;
import static io.smartin.id1212.net.dto.Action.ActionType.JOIN_GAME;
import static io.smartin.id1212.net.dto.Action.ActionType.NEW_GAME;
import static io.smartin.id1212.net.dto.Action.ActionType.RECONNECT;
import static io.smartin.id1212.net.dto.Action.ActionType.LEAVE_GAME;;

public class PlayerController {
    private Player player;

    public PlayerController(String id) {
        player = new Player(id);
    }

    public void handleAction(Action action, String sessionId) throws GameException, NicknameException, KeyException, FatalException {
        validateAuthority(action);
        switch (action.getType()) {
            case NEW_GAME:          createNewGame(action);          break;
            case JOIN_GAME:         joinGame(action);               break;
            case START_GAME:        startGame();                    break;
            case DEAL_CARDS:        dealCards();                    break;
            case THROW:             handleThrowAction(action);      break;
            case CHICAGO:           handleChicagoAction(action);    break;
            case MOVE:              handleMoveAction(action);       break;
            case RESTART_GAME:      restartGame();                  break;
            case SEND_CHAT_MESSAGE: sendChatMessage(action);        break;
            case RECONNECT:         rejoin(action, sessionId);      break;
            case KICK_PLAYER:       kickPlayer(action);             break;
            case LEAVE_GAME:        leaveGame(action, sessionId);   break;
            default:                throw new UnknownActionException(UNKNOWN_ACTION);
        }
    }

    private void rejoin(Action action, String sessionId) throws FatalException {
        try {
            RejoinRequest request = Converter.toRejoinRequest(action.getValue());
            String key = request.getKey();
            String oldPlayerId = request.getPlayerId();
            ChicagoGame game = GamesRepository.getInstance().findGameForPlayer(key, oldPlayerId);
            player = game.getPlayer(oldPlayerId);
            player.setId(sessionId);
            player.setConnected(true);
        } catch (UnknownInvitationKeyException e) {
            throw new FatalException(e.getMessage());
        } catch (NotInGameException e) {
            throw new FatalException(e.getMessage());
        }
    }

    private void leaveGame(Action action, String sessionId) throws FatalException {
        try {
            LeaveRequest request = Converter.toLeaveRequest(action.getValue());
            String key = request.getKey();
            String oldPlayerId = request.getPlayerId();
            ChicagoGame game = GamesRepository.getInstance().findGameForPlayer(key, oldPlayerId);
            player = game.getPlayer(oldPlayerId);
            player.leaveGame();
            SessionHandler.getInstance().kill(oldPlayerId);
        } catch (UnknownInvitationKeyException e) {
            throw new FatalException(e.getMessage());
        } catch (NotInGameException e) {
            throw new FatalException(e.getMessage());
        }
    }

    private void kickPlayer(Action action) throws NotInGameException, UnauthorizedKickException {
        String playerIdToKick = action.getValue();
        player.kickPlayer(playerIdToKick);
        Message message = new Message(MessageType.KICKED, "");
        SessionHandler.getInstance().sendMsgToSessionId(playerIdToKick, message);
    }

    private void sendChatMessage(Action action) throws NotInGameException {
        String message = action.getValue();
        player.sendChatMessage(message);
    }

    private void restartGame() throws TooFewPlayersException, UnauthorizedStartException {
        player.restartGame();
    }

    private void dealCards() throws GameOverException, RoundNotFinishedException, UnauthorizedDealerException {
        player.dealCards();
    }

    private void createNewGame(Action action) {
        player.setName(action.getValue());
        GamesRepository.getInstance().createGame(player);
    }

    private void joinGame(Action action) throws UnknownInvitationKeyException, NicknameException, AlreadyStartedException {
        JoinRequest request = Converter.toJoinRequest(action.getValue());
        player.setName(request.getNickname());
        GamesRepository.getInstance().joinGame(player, request.getKey());
    }

    private void startGame() throws TooFewPlayersException, UnauthorizedStartException {
        player.startGame();
    }

    private void handleThrowAction(Action action) throws TradeBannedException, OutOfCardsException, WaitYourTurnException, InappropriateActionException, TooManyCardsException, UnauthorizedTradeException {
        List<PlayingCard> cards = Converter.toCards(action.getValue());
        player.throwCards(cards);
    }

    private void handleChicagoAction(Action action) throws ChicagoAlreadyCalledException, WaitYourTurnException, InappropriateActionException {
        player.respondToChicago(Boolean.valueOf(action.getValue()));
    }

    private void handleMoveAction(Action action) throws IllegalCardException, WaitYourTurnException, InappropriateActionException, IllegalMoveException {
        PlayingCard card = Converter.toCard(action.getValue());
        player.playCard(card);
    }

    private void validateAuthority(Action action) throws NotInGameException {
        if (action.getType().equals(JOIN_GAME)) return;
        if (action.getType().equals(NEW_GAME)) return;
        if (action.getType().equals(LEAVE_GAME)) return;
        if (action.getType().equals(RECONNECT)) return;
        if (player == null) throw new NotInGameException(INVALID_PLAYER);
        if (player.getGame() == null) throw new NotInGameException(INVALID_PLAYER);
    }

    public Player getPlayer() {
        return player;
    }

    public void setConnected(boolean connected) {
        player.setConnected(connected);
    }
}
