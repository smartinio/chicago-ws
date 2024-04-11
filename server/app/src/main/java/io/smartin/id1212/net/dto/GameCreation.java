package io.smartin.id1212.net.dto;

import com.google.gson.annotations.Expose;

import io.smartin.id1212.net.services.Converter;

public class GameCreation {
    public enum OneOpenMode {
        ALL,
        FINAL,
    }

    public static class GameRules {
        @Expose
        public boolean chicagoBestHand;
        @Expose
        public boolean chicagoBefore15;
        @Expose
        public int numTrades;
        @Expose
        public int tradeBanScore;
        @Expose
        public OneOpenMode oneOpen;
        @Expose
        public int roundWinScore;
        @Expose
        public int winWithTwoScore;
    }

    @Expose
    private String nickname;
    @Expose
    private GameRules rules;

    public GameCreation() {
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public GameRules getRules() {
        return rules;
    }

    public void setRules(GameRules rules) {
        this.rules = rules;
    }

    @Override
    public String toString() {
        return Converter.toJson(this);
    }
}
