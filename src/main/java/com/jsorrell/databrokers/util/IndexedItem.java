package com.jsorrell.databrokers.util;

import com.google.common.collect.ImmutableList;
import java.util.List;

public record IndexedItem<T>(int index, T item) {
    public static <T> IndexedItem<T> of(int index, T item) {
        return new IndexedItem<>(index, item);
    }

    public static <T> List<IndexedItem<T>> indexList(List<T> list) {
        ImmutableList.Builder<IndexedItem<T>> builder = ImmutableList.builderWithExpectedSize(list.size());
        for (int i = 0; i < list.size(); i++) {
            builder.add(IndexedItem.of(i, list.get(i)));
        }
        return builder.build();
    }
}
