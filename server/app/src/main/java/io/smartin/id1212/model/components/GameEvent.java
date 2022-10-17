package io.smartin.id1212.model.components;

import com.google.gson.annotations.Expose;

import io.smartin.id1212.model.components.pokerhands.abstracts.PokerHand;

public class GameEvent {
  private GameEvent() {
    this.timestamp = System.currentTimeMillis();;
  }

  public static GameEvent bestHand(Player winner, int points) {
    GameEvent event = new GameEvent();

    event.actor = winner;
    event.action = EventAction.WON_BEST_HAND;
    event.handType = winner.getHand().getPokerHand().getType();
    event.points = points;

    return event;
  }

  public static GameEvent tradedCards(Player trader, int numCards) {
    GameEvent event = new GameEvent();

    event.actor = trader;
    event.action = EventAction.TRADED;
    event.numCards = numCards;

    return event;
  }

  public static GameEvent playedCard(Player player, PlayingCard card) {
    GameEvent event = new GameEvent();

    event.actor = player;
    event.action = EventAction.PLAYED;
    event.card = card;

    return event;
  }

  public static GameEvent calledChicago(Player caller) {
    GameEvent event = new GameEvent();

    event.actor = caller;
    event.action = EventAction.CALLED_CHICAGO;

    return event;
  }

  public static GameEvent lostChicago(Player loser) {
    GameEvent event = new GameEvent();

    event.actor = loser;
    event.action = EventAction.LOST_CHICAGO;

    return event;
  }

  public static GameEvent wonChicago(Player winner) {
    GameEvent event = new GameEvent();

    event.actor = winner;
    event.action = EventAction.WON_CHICAGO;

    return event;
  }

  public static GameEvent wonTrick(Move winningMove) {
    GameEvent event = new GameEvent();

    event.actor = winningMove.getPlayer();
    event.action = EventAction.WON_TRICK;
    event.card = winningMove.getCard();

    return event;
  }

  public static GameEvent wonRound(Move winningMove, int points) {
    GameEvent event = new GameEvent();

    event.actor = winningMove.getPlayer();
    event.action = EventAction.WON_ROUND;
    event.card = winningMove.getCard();
    event.points = points;

    return event;
  }

  public static GameEvent wonGame(Player winner) {
    GameEvent event = new GameEvent();

    event.actor = winner;
    event.action = EventAction.WON_GAME;

    return event;
  }

  @Expose
  public long timestamp;

  @Expose
  public Player actor;

  @Expose
  public EventAction action;

  @Expose
  public Player target;

  @Expose
  public Hand.HandType handType;

  @Expose
  public PlayingCard card;

  @Expose
  public int numCards;

  @Expose
  public int points;

  public enum EventAction {
    CALLED_CHICAGO,
    LOST_CHICAGO,
    PLAYED,
    TRADED,
    WON_BEST_HAND,
    WON_CHICAGO,
    WON_GAME,
    WON_ROUND,
    WON_TRICK,
  }
}
