package io.smartin.id1212.net.dto;

import com.google.gson.annotations.Expose;

public class Message {
    @Expose
    private MessageType type;
    @Expose
    private String body;

    public Message(MessageType type, String body) {
        this.type = type;
        this.body = body;
    }

    public Message () {

    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public enum MessageType {
        JSON_ERROR,
        NICKNAME_ERROR,
        KEY_ERROR,
        GAME_ERROR,
        GAME_WINNER,
        SNAPSHOT,
        KEEP_ALIVE,
    }
}
