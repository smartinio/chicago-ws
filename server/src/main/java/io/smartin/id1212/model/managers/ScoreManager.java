package io.smartin.id1212.model.managers;

import com.sun.media.jfxmedia.logging.Logger;
import io.smartin.id1212.exceptions.game.HandsAreEqualException;
import io.smartin.id1212.model.components.*;
import io.smartin.id1212.model.components.pokerhands.abstracts.PokerHand;

import java.util.*;

public class ScoreManager {
    private List<Player> players;

    public ScoreManager(List<Player> players) {
        this.players = players;
    }

    public void givePointsForBestHand() {
        Map<Hand.HandType,List<Player>> countMap = new HashMap<>();
        Hand.HandType bestType = null;
        for (Player player : players) {
            PokerHand currentPokerHand = player.getHand().getPokerHand();
            Hand.HandType type = currentPokerHand.getType();
            if (!countMap.containsKey(type)) countMap.put(type, new ArrayList<>());
            countMap.get(type).add(player);
        }
        for (Map.Entry<Hand.HandType, List<Player>> entry : countMap.entrySet()) {
            if (bestType == null || entry.getKey().ordinal() > bestType.ordinal()) {
                bestType = entry.getKey();
            }
        }
        if (bestType != null && bestType.equals(Hand.HandType.NOTHING))
            return;
        List<Player> finalCandidates = countMap.get(bestType);
        finalizeWinners(finalCandidates);
    }

    private void finalizeWinners(List<Player> finalCandidates) {
        List<Player> winners = new ArrayList<>();
        for (Player candidate : finalCandidates) {
            try {
                if (winners.size() == 0) {
                    winners.add(candidate);
                } else if (candidate.getHand().getPokerHand().beats(winners.get(0).getHand().getPokerHand())) {
                    winners = new ArrayList<>();
                    winners.add(candidate);
                }
            } catch (HandsAreEqualException e) {
                winners.add(candidate);
            }
        }
        for (Player winner : winners) {
            int points = winner.getHand().getPokerHand().getType().ordinal();
            winner.addPoints(points);
            Logger.logMsg(1, "Added " + points + " points to player " + winner.getName());
        }
    }

    public void handleChicagoResult(boolean success, Player player) {
        if (success) {
            player.addPoints(15);
            player.setHasTakenChicago(true);
        }
        else {
            player.removePoints(15);
        }
    }
}
