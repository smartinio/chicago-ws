package io.smartin.id1212.config;

import java.util.HashMap;
import java.util.Map;

import io.smartin.id1212.model.components.Hand.HandType;

public class Rules {
    public static final int    MAX_GAME_IDLE_TIME_SECONDS = 1800; // 30 min
    public static final int    MAX_TRADES_PER_ROUND = 2;
    public static final int    ROUND_WIN_SCORE = 2;
    public static final int    WIN_WITH_TWO_SCORE = 5;
    public static final int    TRADE_BAN_SCORE = 45;
    public static final int    MAX_CARDS_PER_PLAYER = 5;
    public static final int    POINTS_FOR_WON_CHICAGO = 15;
    public static final int    MAX_GAME_SCORE = 52;
    public static final boolean CHICAGO_MUST_HAVE_BEST_HAND = false;
    public static final Map<HandType, Integer> HAND_SCORES = new HashMap<HandType, Integer>() {{
        put(HandType.NOTHING, 0);
        put(HandType.PAIR, 1);
        put(HandType.TWO_PAIR, 2);
        put(HandType.THREE_OF_A_KIND, 3);
        put(HandType.STRAIGHT, 4);
        put(HandType.FLUSH, 5);
        put(HandType.FULL_HOUSE, 6);
        put(HandType.FOUR_OF_A_KIND, 8);
        put(HandType.STRAIGHT_FLUSH, 11);
        put(HandType.ROYAL_STRAIGHT_FLUSH, 52);
    }};
}
