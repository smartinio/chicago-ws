package io.smartin.id1212.net.communication;

import io.smartin.id1212.net.services.Converter;
import io.smartin.id1212.model.components.ChicagoGame;
import io.smartin.id1212.net.dto.Message;

import javax.websocket.Session;

import java.util.HashMap;
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

    void broadcastSnapshots(ChicagoGame game) {
        game.getPlayers().forEach(player -> {
            Message msg = new Message(SNAPSHOT, Converter.toJson(game.snapshot(player)));
            Session session = sessions.get(player.getId());
            sendMsg(session, msg);
        });
    }

    void register(Session session) {
        sessions.put(session.getId(), session);
        System.out.println(sessions.size() + " clients connected");
    }

    void unregister(Session session) {
        sessions.remove(session.getId());
        System.out.println(sessions.size() + " clients connected");
    }

    static void sendMsg(Session session, Message msg) {
        session.getAsyncRemote().sendText(Converter.toJson(msg));
    }
}
