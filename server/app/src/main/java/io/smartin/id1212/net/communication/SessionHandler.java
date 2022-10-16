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

import static io.smartin.id1212.net.dto.Message.MessageType.SNAPSHOT;

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
        List<Player> players = game.getPlayers();
        long logId = Math.round(Math.random() * 1000);

        log(logId, "REQUESTED synchronized");
        synchronized (players) {
            log(logId, "BEGAN synchronized");

            if (players.size() > 0) {
                log(logId, "Broadcasting to:");
            } else {
                log(logId, "No players to broadcast to");
            }

            for (Player player : players) {
                System.out.print(player.getName() + ". ");
                Message msg = new Message(SNAPSHOT, Converter.toJson(game.snapshot(player)));
                Session session = sessions.get(player.getId());
                sendMsg(session, msg, logId);
            }

            System.out.println("");

            log(logId, "ENDED synchronized");
        }
        log(logId, "EXITED synchronized");
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
