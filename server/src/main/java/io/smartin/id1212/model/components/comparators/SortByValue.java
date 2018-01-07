package io.smartin.id1212.model.components.comparators;

import io.smartin.id1212.model.components.PlayingCard;

import java.util.Comparator;

public class SortByValue implements Comparator<PlayingCard> {
    @Override
    public int compare(PlayingCard o1, PlayingCard o2) {
        return o1.getValue().ordinal() - o2.getValue().ordinal();
    }
}
