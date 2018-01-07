package io.smartin.id1212.net.dto;

import com.google.gson.annotations.Expose;
import io.smartin.id1212.model.components.comparators.SortBySuitThenValue;
import io.smartin.id1212.model.components.ChicagoGame;
import io.smartin.id1212.model.components.Player;
import io.smartin.id1212.model.components.PlayingCard;

import java.util.*;

public class Snapshot {
    @Expose
    private Player me;
    @Expose
    private ChicagoGame game;
    @Expose
    private boolean myTurn = false;
    @Expose
    private List<PlayingCard> myHand = new ArrayList<>();
    @Expose
    private List<PlayingCard> myPlayed = new ArrayList<>();

    public Snapshot(Player me, ChicagoGame game) {
        this.game = game;
        this.me = me;
        if (!game.getStarted()) return;
        List<PlayingCard> sortedHand = new ArrayList<>(me.getHand().getCards());
        sortedHand.sort(new SortBySuitThenValue());
        this.myHand = sortedHand;
        this.myPlayed = me.getHand().getPlayed();
        this.myTurn = me.equals(game.getCurrentRound().getCurrentPlayer());
    }
}
