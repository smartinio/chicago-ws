package io.smartin.id1212.model.components;

import com.google.gson.annotations.Expose;
import io.smartin.id1212.exceptions.game.HandsAreEqualException;
import io.smartin.id1212.model.components.PlayingCard.Value;
import io.smartin.id1212.model.components.comparators.SortByValue;
import io.smartin.id1212.model.components.pokerhands.*;
import io.smartin.id1212.model.components.pokerhands.abstracts.PokerHand;

import java.util.*;

public class Hand {
    private Set<PlayingCard> cards = new HashSet<>();
    @Expose
    private List<PlayingCard> played = new ArrayList<>();

    public Hand(Set<PlayingCard> cards) {
        this.cards = cards;
    }

    public Set<PlayingCard> getCards() {
        return cards;
    }

    private Set<PlayingCard> getAllFive() {
        Set<PlayingCard> allFive = new HashSet<>();
        allFive.addAll(played);
        allFive.addAll(cards);
        return allFive;
    }

    public void moveToPlayed(PlayingCard card) {
        remove(card);
        played.add(card);
    }

    public boolean contains(PlayingCard card) {
        return cards.contains(card);
    }

    public void remove(PlayingCard card) {
        cards.remove(card);
    }

    public void removeAll(Set<PlayingCard> thrown) {
        cards.removeAll(thrown);
    }

    public void addAll(Set<PlayingCard> draw) {
        cards.addAll(draw);
    }

    public boolean beats(Hand otherHand) throws HandsAreEqualException {
        PokerHand thisHand = getPokerHand();
        PokerHand thatHand = otherHand.getPokerHand();
        return thisHand.beats(thatHand);
    }

    public PokerHand getPokerHand() {
        NOfAKindResult nOfAKind;

        if (isStraight() && isFlush()) {
            return new StraightFlush(highestCard().getValue());
        }
        nOfAKind = hasNOfAKind(4);
        if (nOfAKind.isTrue) {
            return new FourOfAKind(nOfAKind.of);
        }
        if (isFullHouse()) {
            return new FullHouse(highestCard().getValue());
        }
        if (isFlush()) {
            return new Flush(this);
        }
        if (isStraight()) {
            return new Straight(highestCard().getValue());
        }
        nOfAKind = hasNOfAKind(3);
        if (nOfAKind.isTrue) {
            return new ThreeOfAKind(nOfAKind.of);
        }
        if (hasTwoPair()) {
            return new TwoPair(countValues());
        }
        nOfAKind = hasNOfAKind(2);
        if (nOfAKind.isTrue) {
            return new Pair(nOfAKind.of);
        }
        return new Nothing(this);
    }

    private boolean hasTwoPair() {
        Map<PlayingCard.Value, Integer> countMap = countValues();
        int pairs = 0;
        for (Integer integer : countMap.values()) {
            if (integer == 2) pairs++;
        }
        return pairs == 2;
    }

    private boolean isFullHouse() {
        Map<PlayingCard.Value, Integer> countMap = countValues();
        return (countMap.size() == 2 && countMap.containsValue(3));
    }

    private Map<PlayingCard.Value, Integer> countValues() {
        Map<PlayingCard.Value, Integer> countMap = new HashMap<>();
        for (PlayingCard card : getAllFive()) {
            PlayingCard.Value v = card.getValue();
            if (!countMap.containsKey(v)) countMap.put(v, 0);
            countMap.put(v, countMap.get(v) + 1);
        }
        return countMap;
    }

    private PlayingCard highestCard() {
        List<PlayingCard> sortedCards = new ArrayList<>(getAllFive());
        sortedCards.sort(new SortByValue());
        return sortedCards.get(sortedCards.size() - 1);
    }

    private NOfAKindResult hasNOfAKind(int n) {
        Map<PlayingCard.Value,Integer> countMap = countValues();
        for (Map.Entry<PlayingCard.Value, Integer> v : countMap.entrySet()) {
            if (v.getValue() == n) {
                return new NOfAKindResult(true, v.getKey());
            }
        }
        return new NOfAKindResult(false, PlayingCard.Value.TWO);
    }

    private boolean isFlush() {
        PlayingCard prev = null;
        for (PlayingCard card : getAllFive()) {
            if (prev != null) {
                if (!card.getSuit().equals(prev.getSuit())) {
                    return false;
                }
            }
            prev = card;
        }
        return true;
    }

    private boolean isStraight() {
        List<PlayingCard> cardList = new ArrayList<>(getAllFive());
        cardList.sort(new SortByValue());
        PlayingCard prev = null;

        cardList.removeIf(c -> c.getValue() == Value.ACE);
        boolean hasOneAce = cardList.size() == 4;

        // Had more than one Ace, not a straight:
        if (cardList.size() < 4) {
            return false;
        }

        for (PlayingCard card : cardList) {
            if (prev != null) {
                if (card.getValue().ordinal() != (prev.getValue().ordinal() + 1)) {
                    return false;
                }
            }
            prev = card;
        }

        if (hasOneAce) {
            Value lowest = cardList.get(0).getValue();
            return lowest == Value.TWO || lowest == Value.TEN;
        }

        return true;
    }

    public List<PlayingCard> getPlayed() {
        return played;
    }

    public enum HandType {
        NOTHING,
        PAIR,
        TWO_PAIR,
        THREE_OF_A_KIND,
        STRAIGHT,
        FLUSH,
        FULL_HOUSE,
        FOUR_OF_A_KIND,
        STRAIGHT_FLUSH,
        ROYAL_STRAIGHT_FLUSH,
    }

    private class NOfAKindResult {
        boolean isTrue;
        PlayingCard.Value of;

        NOfAKindResult(boolean isTrue, PlayingCard.Value of) {
            this.isTrue = isTrue;
            this.of = of;
        }
    }

    public void moveAllToPlayed(boolean highestFirst) {
        List<PlayingCard> remaining = new ArrayList<>(cards);
        remaining.sort(new SortByValue(!highestFirst));
        played.addAll(remaining);
        cards.clear();
    }

    public PlayingCard getLastPlayedCard() {
        if (played.size() == 0) {
            return null;
        }
        return played.get(played.size() - 1);
    }
}
