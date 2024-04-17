package io.smartin.id1212.model.components;

import com.google.gson.annotations.Expose;

public class ResetOthersScore {
  @Expose
  private int points;

  @Expose
  private boolean isPending = false;

  @Expose
  private Player player;

  public boolean isPendingFor(Player player) {
    return isPending && this.player.equals(player);
  }

  public void start(Player player, int points) {
    this.player = player;
    this.isPending = true;
    this.points = points;
  }

  public void stop() {
    this.player = null;
    this.isPending = false;
    this.points = 0;
  }

  public boolean isPending() {
    return isPending;
  }
}
