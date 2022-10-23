package io.smartin.id1212.net.dto;

import com.google.gson.annotations.Expose;

import javax.websocket.Session;

public class Action {
    @Expose
    private ActionType type;
    @Expose
    private String value;
    private transient Session session;

    public Action(ActionType type, String value) {
        this.type = type;
        this.value = value;
    }

    public Action(ActionType type) {
        this.type = type;
    }

    public Action() {
    }

    public ActionType getType() {
        return type;
    }

    public void setType(String type) {
        this.type = ActionType.valueOf(type);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public enum ActionType {
        NEW_GAME,
        JOIN_GAME,
        MOVE,
        CHICAGO,
        THROW,
        START_GAME,
        LEAVE_GAME,
        RESTART_GAME,
        PING,
        DEAL_CARDS,
        SEND_CHAT_MESSAGE,
    }
}
