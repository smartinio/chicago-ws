package io.smartin.id1212.net.communication;

import com.google.gson.JsonSyntaxException;

import io.smartin.id1212.exceptions.FatalException;
import io.smartin.id1212.exceptions.GameException;
import io.smartin.id1212.exceptions.KeyException;
import io.smartin.id1212.exceptions.NicknameException;
import io.smartin.id1212.model.components.ChicagoGame;
import io.smartin.id1212.net.dto.Action;
import io.smartin.id1212.net.dto.Message;
import io.smartin.id1212.net.dto.Action.ActionType;
import io.smartin.id1212.controller.PlayerController;
import io.smartin.id1212.net.services.Converter;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

import static io.smartin.id1212.net.dto.Message.MessageType.*;

@ServerEndpoint("/game")
public class GameEndpoint {
    private PlayerController playerController;
    private final SessionHandler sessionHandler = SessionHandler.getInstance();

    @OnOpen
    public void onOpen(Session session) {
        playerController = new PlayerController(session.getId());
        sessionHandler.register(session);
    }

    @OnClose
    public void onClose(Session session) {
        ChicagoGame game = playerController.getPlayer().getGame();
        playerController.setConnected(false);
        sessionHandler.unregister(session);
        sessionHandler.broadcastSnapshots(game);
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        System.out.println("!!!!!!! onError called: ");
        System.out.println(thr);
        System.out.println(thr.getMessage());
        thr.printStackTrace();
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        try {
            Action action = Converter.toAction(message);

            if (action.getType() == ActionType.PING) {
                return;
            }

            playerController.handleAction(action, session.getId());
            ChicagoGame game = playerController.getPlayer().getGame();
            sessionHandler.broadcastSnapshots(game);
        } catch (JsonSyntaxException e) {
            SessionHandler.sendMsg(session, new Message(JSON_ERROR, e.getMessage()));
        } catch (GameException e) {
            SessionHandler.sendMsg(session, new Message(GAME_ERROR, e.getMessage()));
        } catch (NicknameException e) {
            SessionHandler.sendMsg(session, new Message(NICKNAME_ERROR, e.getMessage()));
        } catch (KeyException e) {
            SessionHandler.sendMsg(session, new Message(KEY_ERROR, e.getMessage()));
        } catch (FatalException e) {
            SessionHandler.sendMsg(session, new Message(FATAL_ERROR, e.getMessage()));
        } catch (Exception e) {
            System.out.println("Unexpected error:");
            System.out.println(e);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
