package io.smartin.id1212.net.dto;

import com.google.gson.annotations.Expose;

public record Action(@Expose ActionType type, @Expose String value) {
    public enum ActionType {
        NEW_GAME,
        JOIN_GAME,
        MOVE,
        CHICAGO,
        THROW,
        THROW_ONE_OPEN,
        RESPOND_ONE_OPEN,
        START_GAME,
        LEAVE_GAME,
        RESTART_GAME,
        PING,
        DEAL_CARDS,
        SEND_CHAT_MESSAGE,
        KICK_PLAYER,
        RECONNECT,
        CHECK_GAME,
        RESPOND_RESET_OTHERS_SCORE,
    }
}
