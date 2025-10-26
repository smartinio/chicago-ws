package io.smartin.id1212.net.communication;

import com.google.gson.JsonSyntaxException;

import io.smartin.id1212.exceptions.FatalException;
import io.smartin.id1212.exceptions.GameException;
import io.smartin.id1212.exceptions.KeyException;
import io.smartin.id1212.exceptions.NicknameException;
import io.smartin.id1212.model.components.ChicagoGame;
import io.smartin.id1212.net.dto.Action;
import io.smartin.id1212.net.dto.Message;
import io.smartin.id1212.controller.PlayerController;
import io.smartin.id1212.net.services.Converter;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.smartin.id1212.net.dto.Message.MessageType.*;


@ServerEndpoint("/game")
public class GameEndpoint {
    private PlayerController playerController;
    private final SessionHandler sessionHandler = SessionHandler.getInstance();
    private Logger logger;

    @OnOpen
    public void onOpen(Session session) {
        logger = LogManager.getLogger("sessionId:" + session.getId());
        logger.info("onOpen. creating playerController");
        playerController = new PlayerController(session.getId());
        // Disable the container-level idle and async send timeouts so Tomcat keeps
        // long-lived matches connected.
        session.setMaxIdleTimeout(0);
        session.getAsyncRemote().setSendTimeout(0);
        sessionHandler.register(session);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        logger.info("Closing (code {}) because: {}", reason.getCloseCode(), reason.getReasonPhrase());
        logger.info("onClose. marking as disconnected and unregistering session. should fail broadcast to 1 player. on session {}", session.getId());
        ChicagoGame game = playerController.getPlayer().getGame();
        playerController.setConnected(false);
        sessionHandler.unregister(session);
        sessionHandler.broadcastSnapshots(game);
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        System.out.println("!!!!!!! onError called: ");
        System.out.println(thr.getMessage());
        thr.printStackTrace();
    }

    private boolean requiresSnapshot(Action action) {
        return switch (action.type()) {
            case PING, CHECK_GAME -> false;
            default -> true;
        };
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        try {
            playerController.updateSessionId(session.getId());
            playerController.markAsConnected();
            Action action = Converter.toAction(message);

            logger.info("Message received on session {}", session.getId());
            playerController.handleAction(action, session.getId());

            if (requiresSnapshot(action)) {
                ChicagoGame game = playerController.getPlayer().getGame();
                sessionHandler.broadcastSnapshots(game);
            }
        } catch (JsonSyntaxException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            SessionHandler.sendMsg(session, new Message(JSON_ERROR, e.getMessage()));
        } catch (GameException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            SessionHandler.sendMsg(session, new Message(GAME_ERROR, e.getMessage()));
        } catch (NicknameException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            SessionHandler.sendMsg(session, new Message(NICKNAME_ERROR, e.getMessage()));
        } catch (KeyException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            SessionHandler.sendMsg(session, new Message(KEY_ERROR, e.getMessage()));
        } catch (FatalException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            SessionHandler.sendMsg(session, new Message(FATAL_ERROR, e.getMessage()));
        } catch (Exception e) {
            System.out.println("Unexpected error:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
