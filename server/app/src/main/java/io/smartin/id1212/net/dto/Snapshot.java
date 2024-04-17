package io.smartin.id1212.net.dto;

import com.google.gson.annotations.Expose;
import io.smartin.id1212.model.components.comparators.SortBySuitThenValue;
import io.smartin.id1212.model.components.ChicagoGame;
import io.smartin.id1212.model.components.Player;
import io.smartin.id1212.model.components.PlayingCard;

import java.util.*;

public class Snapshot {
    @Expose
    public final Player me;
    @Expose
    public final ChicagoGame game;
    @Expose
    public boolean myTurn = false;
    @Expose
    public boolean imDealing = false;
    @Expose
    public boolean isOneOpenAvailable = false;
    @Expose
    public List<PlayingCard> myHand = new ArrayList<>();
    @Expose
    public List<PlayingCard> myPlayed = new ArrayList<>();

    public Snapshot(Player me, ChicagoGame game) {
        this.game = game;
        this.me = me;
        if (!game.getStarted()) return;
        List<PlayingCard> sortedHand = new ArrayList<>(me.getHand().getCards());
        sortedHand.sort(new SortBySuitThenValue());
        this.myHand = sortedHand;
        this.myPlayed = me.getHand().getPlayed();
        this.myTurn = me.equals(game.getCurrentRound().getCurrentPlayer());
        this.imDealing = me.equals(game.getDealer());
        this.isOneOpenAvailable = game.getCurrentRound().isOneOpenAvailable();
    }
}
