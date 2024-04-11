package io.smartin.id1212.model.managers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.annotations.Expose;

import io.smartin.id1212.exceptions.game.IllegalMoveException;
import io.smartin.id1212.model.components.Move;
import io.smartin.id1212.model.components.Player;
import io.smartin.id1212.model.components.PlayingCard;
import io.smartin.id1212.model.components.Round;
import io.smartin.id1212.model.components.Trick;
import io.smartin.id1212.model.components.PlayingCard.Suit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.smartin.id1212.config.Rules.MAX_CARDS_PER_PLAYER;

public class TrickingManager {
    private static final Logger logger = LogManager.getLogger(TrickingManager.class);

    public record MoveResult(Move winningMove, boolean hasMoreTricks, boolean isTrickDone) { }

    private final Round round;
    @Expose
    private final List<Trick> tricks = new ArrayList<>();
    private final Map<Suit, Set<Player>> playersWithSuit = new HashMap<>();

    public TrickingManager(Round round) {
        this.round = round;
        this.tricks.add(new Trick(round));

        // Assume everyone can follow suit until proven otherwise
        for (Suit suit : Suit.values()) {
            Set<Player> players = new HashSet<>(round.getGame().getPlayers());
            playersWithSuit.put(suit, players);
        }
    }

    public Set<Player> playersWithPotentialSuit(Suit suit) {
        return new HashSet<>(playersWithSuit.get(suit));
    }

    public MoveResult handle(Move move) throws IllegalMoveException {
        Trick trick = currentTrick();
        trick.addMove(move);

        Suit startingSuit = trick.getStartingMove().getCard().getSuit();
        Player player = move.getPlayer();
        PlayingCard playedCard = move.getCard();
        Suit playedSuit = playedCard.getSuit();

        if (playedSuit != startingSuit) {
            logger.info("Player '{}' can not follow {} (played {} instead)", player, startingSuit, playedSuit);
            playersWithSuit.get(startingSuit).remove(player);
        }

        player.getHand().moveToPlayed(playedCard);
        Move winningMove = trick.getWinningMove();
        boolean hasMoreTricks = !thisWasLastTrick();
        boolean isTrickDone = trick.isDone();
        MoveResult result = new MoveResult(winningMove, hasMoreTricks, isTrickDone);

        if (isTrickDone) {
            this.tricks.add(new Trick(round));
        }

        return result;
    }

    private boolean thisWasLastTrick() {
        return tricks.size() == MAX_CARDS_PER_PLAYER && currentTrick().isDone();
    }

    public Trick currentTrick() {
        return tricks.get(tricks.size() - 1);
    }

    public List<Trick> getTricks() {
        return tricks;
    }
}
