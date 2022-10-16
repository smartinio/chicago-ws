package io.smartin.id1212.model.components.comparators;

import io.smartin.id1212.model.components.PlayingCard;

import java.util.Comparator;

public class SortBySuitThenValue implements Comparator<PlayingCard> {
    @Override
    public int compare(PlayingCard o1, PlayingCard o2) {
        int x1 = o1.getSuit().ordinal();
        int x2 = o2.getSuit().ordinal();
        int sComp = x1 - x2;
        if (sComp != 0) {
            return sComp;
        } else {
            int y1 = o1.getValue().ordinal();
            int y2 = o2.getValue().ordinal();
            return y1 - y2;
        }
    }
}