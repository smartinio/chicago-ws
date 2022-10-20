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
import java.util.Timer;
import java.util.TimerTask;

import static io.smartin.id1212.net.dto.Message.MessageType.*;

class SessionHandler {
    private final Map<String,Session> sessions = new HashMap<>();
    private static SessionHandler ourInstance = new SessionHandler();

    static SessionHandler getInstance() {
        return ourInstance;
    }

    private SessionHandler() {
    }

    private static void log(long id, String message) {
        System.out.println(id + ": " + message);
    }

    void broadcastSnapshots(ChicagoGame game) {
        if (game == null) {
            return;
        }

        long logId = Math.round(Math.random() * 1000);
        List<Player> players = game.getPlayers();

        synchronized (players) {
            for (Player player : players) {
                Message msg = new Message(SNAPSHOT, Converter.toJson(game.snapshot(player)));
                Session session = sessions.get(player.getId());
                sendMsg(session, msg, logId);
            }
        }
    }

    void register(Session session) {
        sessions.put(session.getId(), session);
        System.out.println(sessions.size() + " clients connected");
    }

    void unregister(Session session) {
        sessions.remove(session.getId());
        System.out.println(sessions.size() + " clients connected");
    }

    static void sendMsg(Session session, Message msg, long logId) {
        if (session == null || !session.isOpen()) {
            log(logId, "Cannot send message to closed session");
            return;
        }

        try {
            session.getBasicRemote().sendText(Converter.toJson(msg));
        } catch (IOException e) {
            System.out.println("Failed to send text with basic remote, falling back to async:");
            System.out.println("->" + e.getMessage());
            session.getAsyncRemote().sendText(Converter.toJson(msg));
        }
    }
}
