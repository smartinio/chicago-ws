package io.smartin.id1212.net.dto;

import com.google.gson.annotations.Expose;

public record Message(@Expose MessageType type, @Expose String body) {
    public enum MessageType {
        FATAL_ERROR,
        KICKED,
        JSON_ERROR,
        NICKNAME_ERROR,
        KEY_ERROR,
        GAME_ERROR,
        GAME_WINNER,
        SNAPSHOT,
        PONG,
        CURRENTLY_IN_GAME,
    }
}
