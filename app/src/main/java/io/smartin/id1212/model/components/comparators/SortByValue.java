package io.smartin.id1212.model.components.comparators;

import io.smartin.id1212.model.components.PlayingCard;

import java.util.Comparator;

public class SortByValue implements Comparator<PlayingCard> {
    private boolean ascending;

    public SortByValue(boolean ascending) {
        this.ascending = ascending;
    }

    public SortByValue() {
        this.ascending = true;
    }

    @Override
    public int compare(PlayingCard o1, PlayingCard o2) {
        int compared = o1.getValue().ordinal() - o2.getValue().ordinal();
        return ascending ? compared : -compared;
    }
}
