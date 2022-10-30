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
import static io.smartin.id1212.net.dto.Action.ActionType.CHECK_GAME;
import static io.smartin.id1212.net.dto.Action.ActionType.LEAVE_GAME;
import static io.smartin.id1212.net.dto.Action.ActionType.PING;

public class PlayerController {
    private Player player;

    public PlayerController(String id) {
        player = new Player(id);
    }

    public void handleAction(Action action, String sessionId) throws GameException, NicknameException, KeyException, FatalException {
        validateAuthority(action);
        System.out.println(action + " -> handling for player: " + player.getId() + ". session: " + sessionId);
        switch (action.getType()) {
            case NEW_GAME:          createNewGame(action);          break;
            case JOIN_GAME:         joinGame(action);               break;
            case START_GAME:        startGame();                    break;
            case DEAL_CARDS:        dealCards();                    break;
            case THROW:             handleThrowAction(action);      break;
            case THROW_ONE_OPEN:    handleThrowOneOpen(action);     break;
            case RESPOND_ONE_OPEN:  handleRespondOneOpen(action);   break;
            case CHICAGO:           handleChicagoAction(action);    break;
            case MOVE:              handleMoveAction(action);       break;
            case RESTART_GAME:      restartGame();                  break;
            case SEND_CHAT_MESSAGE: sendChatMessage(action);        break;
            case RECONNECT:         rejoin(action, sessionId);      break;
            case KICK_PLAYER:       kickPlayer(action);             break;
            case LEAVE_GAME:        leaveGame(action, sessionId);   break;
            case CHECK_GAME:        checkGame(action, sessionId);   break;
            case PING:                                              return;
            default:                throw new UnknownActionException(UNKNOWN_ACTION);
        }
    }

    private void checkGame(Action action, String sessionId) {
        try {
            var request = Converter.toRejoinRequest(action.getValue());
            var key = request.getKey();
            var oldPlayerId = request.getPlayerId();
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
        boolean accepted = Converter.toBoolean(action.getValue());
        player.respondToOneOpen(accepted);
    }

    private void handleThrowOneOpen(Action action) throws GameException {
        PlayingCard cardToThrow = Converter.toCard(action.getValue());
        player.throwOneOpen(cardToThrow);
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

            if (oldPlayerId != sessionId) {
                System.out.println("killing old (" + oldPlayerId + ") " + " because its not same as new: " + sessionId);
                SessionHandler.getInstance().kill(oldPlayerId);

                List<Player> players = game.getPlayers();
                for (Player p : players) {
                    System.out.println(p.getId() + " -> remains as player");
                }
            }
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
        if (action.getType().equals(CHECK_GAME)) return;
        if (action.getType().equals(JOIN_GAME)) return;
        if (action.getType().equals(NEW_GAME)) return;
        if (action.getType().equals(LEAVE_GAME)) return;
        if (action.getType().equals(RECONNECT)) return;
        if (action.getType().equals(PING)) return;
        if (player == null) throw new NotInGameException(INVALID_PLAYER);
        if (player.getGame() == null) throw new NotInGameException(INVALID_PLAYER);
    }

    public Player getPlayer() {
        return player;
    }

    public void setConnected(boolean connected) {
        player.setConnected(connected);
    }

    public void updateId(String id) {
        player.setId(id);
    }

    public void markAsConnected() {
        System.out.println("Marking player " + player.getId() + " aka " + player.getName() + " as connected");
        if (player.getGame() != null) {
            System.out.println("-- The are in game " + player.getGame().getInvitationKey());
        }

        player.setConnected(true);
    }
}
