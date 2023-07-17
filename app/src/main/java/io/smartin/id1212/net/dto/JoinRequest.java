package io.smartin.id1212.net.dto;

import com.google.gson.annotations.Expose;

public class JoinRequest {
    @Expose
    private String nickname;
    @Expose
    private String key;

    public JoinRequest() {
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
