package io.smartin.id1212.model.managers;

import org.apache.logging.log4j.LogManager;

import io.smartin.id1212.config.Rules;
import io.smartin.id1212.exceptions.game.HandsAreEqualException;
import io.smartin.id1212.model.components.*;
import io.smartin.id1212.model.components.Hand.HandType;

import java.util.*;
import java.util.stream.Collectors;

public class ScoreManager {
    private final List<Player> players;

    public ScoreManager(List<Player> players) {
        this.players = players;
    }

    public List<BestHandResult> previewBestHandResults() {
        var candidates = getFinalCandidates();
        var winners = getWinners(candidates);

        return winners.stream().map(player -> {
            var winnerHandType = player.getHand().getPokerHand().getType();
            var points = Rules.HAND_SCORES.get(winnerHandType);

            return new BestHandResult(
                player,
                points,
                winnerHandType == HandType.FOUR_OF_A_KIND
            );
        }).collect(Collectors.toList());
    }

    public record BestHandResult(Player player, int points, boolean isFourOfAKindPending) { }

    public void resetOthersScore(Player actor) {
        for (var player : players)
            if (!player.equals(actor))
                player.resetScore();
    }

    public List<BestHandResult> givePointsForBestHand() {
        var finalCandidates = getFinalCandidates();

        if (finalCandidates.isEmpty()) {
            return new ArrayList<>();
        }

        return finalizeWinners(finalCandidates);
    }

    private List<Player> getFinalCandidates() {
        var countMap = new HashMap<Hand.HandType,List<Player>>();
        Hand.HandType bestType = null;

        for (Player player : players) {
            var playerLogger = LogManager.getLogger(player.getName());
            var hand = player.getHand();
            var currentPokerHand = hand.getPokerHand();
            var type = currentPokerHand.getType();

            playerLogger.info("hand: {}", type);

            playerLogger.info("remaining cards:");
            hand.getCards().forEach(playerLogger::info);

            playerLogger.info("played cards:");
            hand.getPlayed().forEach(playerLogger::info);

            if (!countMap.containsKey(type)) countMap.put(type, new ArrayList<>());
            countMap.get(type).add(player);
        }
        for (var entry : countMap.entrySet()) {
            if (bestType == null || entry.getKey().ordinal() > bestType.ordinal()) {
                bestType = entry.getKey();
            }
        }
        if (bestType != null && bestType.equals(Hand.HandType.NOTHING))
            return new ArrayList<>();

        return countMap.get(bestType);
    }

    private List<BestHandResult> finalizeWinners(List<Player> finalCandidates) {
        var winners = getWinners(finalCandidates);

        List<BestHandResult> results = new ArrayList<>();

        for (Player winner : winners) {
            var winnerHandType = winner.getHand().getPokerHand().getType();
            var points = Rules.HAND_SCORES.get(winnerHandType);
            winner.addPoints(points);
            System.out.println("Added " + points + " points to player " + winner.getName());
            results.add(new BestHandResult(winner, points, false));
        }

        return results;
    }

    private List<Player> getWinners(List<Player> finalCandidates) {
        List<Player> winners = new ArrayList<>();

        for (var candidate : finalCandidates) {
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
