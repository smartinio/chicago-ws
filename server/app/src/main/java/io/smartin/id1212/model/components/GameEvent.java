package io.smartin.id1212.model.components;

import java.util.List;

import com.google.gson.annotations.Expose;


public class GameEvent {
  private static final Player server = new Player("server");

  private GameEvent() {
    this.timestamp = System.currentTimeMillis();
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

  public static GameEvent lostChicago(Player loser, Player opponent, String reason) {
    GameEvent event = new GameEvent();

    event.actor = loser;
    event.action = EventAction.LOST_CHICAGO;
    event.opponentName = opponent.getName();
    event.reason = reason;

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

  public static GameEvent wonRoundGuaranteed(Player winner, List<PlayingCard> finalCards) {
    GameEvent event = new GameEvent();

    event.actor = winner;
    event.action = EventAction.WON_ROUND_GUARANTEED;
    event.cards = finalCards;

    return event;
  }

  public static GameEvent createdGame(Player creator) {
    GameEvent event = new GameEvent();

    event.actor = creator;
    event.action = EventAction.CREATED_GAME;

    return event;
  }

  public static GameEvent joinedGame(Player joiner) {
    GameEvent event = new GameEvent();

    event.actor = joiner;
    event.action = EventAction.JOINED_GAME;

    return event;
  }

  public static GameEvent leftGame(Player leaver, boolean kicked) {
    GameEvent event = new GameEvent();

    event.actor = leaver;
    event.action = EventAction.LEFT_GAME;
    event.kicked = kicked;

    return event;
  }

  public static GameEvent becameHost(Player host) {
    GameEvent event = new GameEvent();

    event.actor = host;
    event.action = EventAction.BECAME_HOST;

    return event;
  }

  public static GameEvent requestedOneOpen(Player requester, PlayingCard card) {
    GameEvent event = new GameEvent();

    event.actor = requester;
    event.action = EventAction.REQUESTED_ONE_OPEN;
    event.card = card;

    return event;
  }

  public static GameEvent respondedToOneOpen(Player host, PlayingCard card, boolean accepted) {
    GameEvent event = new GameEvent();

    event.actor = host;
    event.action = EventAction.RESPONDED_TO_ONE_OPEN;
    event.card = card;
    event.accepted = accepted;

    return event;
  }

  public static GameEvent serverNewRound() {
    GameEvent event = new GameEvent();

    event.actor = server;
    event.action = EventAction.NEW_ROUND;

    return event;
  }

  public static GameEvent serverTrickDone() {
    GameEvent event = new GameEvent();

    event.actor = server;
    event.action = EventAction.TRICK_DONE;

    return event;
  }

  public static GameEvent chatMessage(Player player, String message) {
    GameEvent event = new GameEvent();

    event.actor = player;
    event.action = EventAction.CHAT_MESSAGE;
    event.message = message;

    return event;
  }

  @Expose
  public final long timestamp;

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
  public List<PlayingCard> cards;

  @Expose
  public int numCards;

  @Expose
  public int points;

  @Expose
  public String message;

  @Expose
  public boolean kicked;

  @Expose
  public boolean accepted;

  @Expose
  public String reason;

  @Expose
  public String opponentName;

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
    WON_ROUND_GUARANTEED,
    CREATED_GAME,
    JOINED_GAME,
    LEFT_GAME,
    BECAME_HOST,
    REQUESTED_ONE_OPEN,
    RESPONDED_TO_ONE_OPEN,
    // server events
    NEW_ROUND,
    TRICK_DONE,
    // chat
    CHAT_MESSAGE,
  }
}
