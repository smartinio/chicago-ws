package io.smartin.id1212.model.components;

import com.google.gson.annotations.Expose;

public class OneOpen {
  @Expose
  private PlayingCard card;

  @Expose
  private boolean isOpen = false;

  @Expose
  private Player player;

  public boolean isOpenFor(Player player) {
    return isOpen && this.player == player;
  }

  public PlayingCard getCard() {
    return card;
  }

  public void start(Player player, PlayingCard card) {
    this.player = player;
    this.isOpen = true;
    this.card = card;
  }

  public void stop() {
    this.player = null;
    this.isOpen = false;
    this.card = null;
  }
}
