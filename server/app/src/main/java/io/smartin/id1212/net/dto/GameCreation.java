package io.smartin.id1212.net.dto;

import com.google.gson.annotations.Expose;

import io.smartin.id1212.net.services.Converter;

public record GameCreation(@Expose String nickname, @Expose GameRules rules) {
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

    @Override
    public String toString() {
        return Converter.toJson(this);
    }
}
