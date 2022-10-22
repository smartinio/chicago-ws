package io.smartin.id1212.controller;

import io.smartin.id1212.exceptions.game.*;
import io.smartin.id1212.exceptions.key.AlreadyStartedException;
import io.smartin.id1212.exceptions.key.UnknownInvitationKeyException;
import io.smartin.id1212.exceptions.NicknameException;
import io.smartin.id1212.model.store.GamesRepository;
import io.smartin.id1212.model.components.Player;
import io.smartin.id1212.model.components.PlayingCard;
import io.smartin.id1212.net.dto.Action;
import io.smartin.id1212.net.dto.JoinRequest;
import io.smartin.id1212.net.services.Converter;

import java.util.List;

import static io.smartin.id1212.config.Strings.*;
import static io.smartin.id1212.net.dto.Action.ActionType.JOIN_GAME;
import static io.smartin.id1212.net.dto.Action.ActionType.NEW_GAME;

public class PlayerController {
    private Player player;

    public PlayerController(String id) {
        player = new Player(id);
    }

    public void leaveGame() {
        if (player.isInGame()) {
            player.leaveGame();
        }
    }

    public void handleAction(Action action) throws UnknownActionException, NotInGameException, UnknownInvitationKeyException, ChicagoAlreadyCalledException, IllegalCardException, TooFewPlayersException, UnauthorizedStartException, TradeBannedException, OutOfCardsException, WaitYourTurnException, InappropriateActionException, IllegalMoveException, NicknameException, AlreadyStartedException {
        validateAuthority(action);
        switch (action.getType()) {
            case NEW_GAME:      createNewGame(action);          break;
            case JOIN_GAME:     joinGame(action);               break;
            case START_GAME:    startGame();                    break;
            case THROW:         handleThrowAction(action);      break;
            case CHICAGO:       handleChicagoAction(action);    break;
            case MOVE:          handleMoveAction(action);       break;
            case RESTART_GAME:  restartGame();                  break;
            default: throw new UnknownActionException(UNKNOWN_ACTION);
        }
    }

    private void restartGame() throws TooFewPlayersException, UnauthorizedStartException {
        player.restartGame();
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

    private void handleThrowAction(Action action) throws TradeBannedException, OutOfCardsException, WaitYourTurnException, InappropriateActionException {
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
        if (player == null) throw new NotInGameException(INVALID_PLAYER);
        if (player.getGame() == null) throw new NotInGameException(INVALID_PLAYER);
    }

    public Player getPlayer() {
        return player;
    }
}
