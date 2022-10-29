package io.smartin.id1212.net.dto;

import com.google.gson.annotations.Expose;

public class LeaveRequest {
    @Expose
    private String playerId;
    @Expose
    private String key;

    public LeaveRequest() {
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
