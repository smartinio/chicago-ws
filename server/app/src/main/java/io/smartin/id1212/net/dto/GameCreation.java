package io.smartin.id1212.net.dto;

import com.google.gson.annotations.Expose;

import io.smartin.id1212.net.services.Converter;

public record GameCreation(@Expose String nickname, @Expose GameRules rules) {
    public enum OneOpenMode {
        ALL,
        FINAL,
    }

    public record GameRules (
        @Expose
         boolean chicagoBestHand,
        @Expose
         boolean chicagoBefore15,
        @Expose
         int numTrades,
        @Expose
         int tradeBanScore,
        @Expose
         OneOpenMode oneOpen,
        @Expose
         int roundWinScore,
        @Expose
         int winWithTwoScore
    ) { }

    @Override
    public String toString() {
        return Converter.toJson(this);
    }
}
