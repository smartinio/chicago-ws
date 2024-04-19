package io.smartin.id1212.net.communication;

import io.smartin.id1212.net.services.Converter;
import io.smartin.id1212.model.components.ChicagoGame;
import io.smartin.id1212.model.components.Player;
import io.smartin.id1212.net.dto.Message;

import javax.websocket.Session;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.smartin.id1212.net.dto.Message.MessageType.*;

public class SessionHandler {
    private final Map<String, Session> sessions = new HashMap<>();
    private final static SessionHandler ourInstance = new SessionHandler();

    public static SessionHandler getInstance() {
        return ourInstance;
    }

    private SessionHandler() {
    }

    private static void log(String message) {
        System.out.println(message);
    }

    public void broadcastSnapshots(ChicagoGame game) {
        if (game == null) {
            log("game is null, skipping snapshot");
            return;
        }

        List<Player> players = game.getPlayers();

        for (Player player : players) {
            var msg = new Message(SNAPSHOT, Converter.toJson(game.snapshot(player)));
            var sessionId = player.getSessionId();
            var session = sessions.get(sessionId);
            log("Attempting message to player ID: " + sessionId);
            log("Exists in sessions: " + sessions.containsKey(sessionId));
            sendMsg(session, msg);
        }
    }

    void register(Session session) {
        sessions.put(session.getId(), session);
        log(sessions.size() + " clients connected");
    }

    void unregister(Session session) {
        kill(session.getId());
    }

    public void kill(String sessionId) {
        sessions.remove(sessionId);
        log(sessions.size() + " clients connected");
    }

    public void sendMsgToSessionId(String sessionId, Message message) {
        Session session = sessions.get(sessionId);
        if (session != null) {
            sendMsg(session, message);
        }
    }

    synchronized static void sendMsg(Session session, Message msg) {
        if (session == null || !session.isOpen()) {
            log("Cannot send message to closed session");
            return;
        }

        try {
            session.getBasicRemote().sendText(Converter.toJson(msg));
        } catch (IOException e) {
            log("Failed to send text with basic remote, falling back to async:");
            log("->" + e.getMessage());
            session.getAsyncRemote().sendText(Converter.toJson(msg));
        }
    }
}
