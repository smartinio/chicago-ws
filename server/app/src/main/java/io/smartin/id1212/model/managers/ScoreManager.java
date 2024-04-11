package io.smartin.id1212.model.managers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.smartin.id1212.config.Rules;
import io.smartin.id1212.exceptions.game.HandsAreEqualException;
import io.smartin.id1212.model.components.*;
import io.smartin.id1212.model.components.Hand.HandType;
import io.smartin.id1212.model.components.pokerhands.abstracts.PokerHand;

import java.util.*;

public class ScoreManager {
    private final List<Player> players;

    public ScoreManager(List<Player> players) {
        this.players = players;
    }

    public record BestHandResult(Player player, int points) { }

    public List<BestHandResult> givePointsForBestHand() {
        var finalCandidates = getFinalCandidates();

        if (finalCandidates.isEmpty()) {
            return new ArrayList<>();
        }

        return finalizeWinners(finalCandidates);
    }

    private List<Player> getFinalCandidates() {
        Map<Hand.HandType,List<Player>> countMap = new HashMap<>();
        Hand.HandType bestType = null;

        for (Player player : players) {
            Logger playerLogger = LogManager.getLogger(player.getName());
            Hand hand = player.getHand();
            PokerHand currentPokerHand = hand.getPokerHand();
            Hand.HandType type = currentPokerHand.getType();

            playerLogger.info("hand: {}", type);

            playerLogger.info("remaining cards:");
            hand.getCards().forEach(playerLogger::info);

            playerLogger.info("played cards:");
            hand.getPlayed().forEach(playerLogger::info);

            if (!countMap.containsKey(type)) countMap.put(type, new ArrayList<>());
            countMap.get(type).add(player);
        }
        for (Map.Entry<Hand.HandType, List<Player>> entry : countMap.entrySet()) {
            if (bestType == null || entry.getKey().ordinal() > bestType.ordinal()) {
                bestType = entry.getKey();
            }
        }
        if (bestType != null && bestType.equals(Hand.HandType.NOTHING))
            return new ArrayList<>();

        return countMap.get(bestType);
    }

    private List<BestHandResult> finalizeWinners(List<Player> finalCandidates) {
        List<Player> winners = getWinners(finalCandidates);

        List<BestHandResult> results = new ArrayList<>();

        for (Player winner : winners) {
            HandType winnerHandType = winner.getHand().getPokerHand().getType();
            int points = Rules.HAND_SCORES.get(winnerHandType);
            winner.addPoints(points);
            System.out.println("Added " + points + " points to player " + winner.getName());
            results.add(new BestHandResult(winner, points));
        }

        return results;
    }

    private List<Player> getWinners(List<Player> finalCandidates) {
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
        return winners;
    }

    public void handleChicagoResult(boolean success, Player player) {
        if (success) {
            player.addPoints(Rules.CHICAGO_POINTS);
            player.setHasTakenChicago(true);
        }
        else {
            player.removePoints(Rules.CHICAGO_POINTS);
        }
    }

    public Player getBestHandWinner() {
        var finalCandidates = getFinalCandidates();
        var winners = getWinners(finalCandidates);
        return winners.iterator().next();
    }

    public boolean hasBestHand(Player chicagoTaker) {
        var finalCandidates = getFinalCandidates();
        var winners = getWinners(finalCandidates);
        return winners.contains(chicagoTaker);
    }
}
