package com.jsorrell.databrokers.util;

import com.google.common.collect.ImmutableList;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

public class WeightedPicker<T, U, A> {
    private final LinkedList<WeightedItem<T>> items;
    private final int totalWeight;
    private final BiFunction<T, A, U> validator;

    public WeightedPicker(Collection<T> items, ToIntFunction<T> weigher, BiFunction<T, A, U> validator) {
        this.items = items.stream()
                .filter(Objects::nonNull)
                .map(WeightedItem.mapper(weigher))
                .filter(w -> 0 < w.weight)
                .collect(Collectors.toCollection(LinkedList::new));
        totalWeight = this.items.stream().mapToInt(WeightedItem::weight).sum();
        this.validator = validator;
    }

    // Throws if nothing to pick
    public @Nullable U doSinglePick(RandomSource random, A arg) {
        List<U> results = doPick(1, random, arg);
        return results.isEmpty() ? null : results.getFirst();
    }

    public List<U> doPick(int num, RandomSource random, A arg) {
        if (items.isEmpty()) {
            return List.of();
        }

        ArrayList<U> results = new ArrayList<>(num);
        @SuppressWarnings("unchecked")
        LinkedList<WeightedItem<T>> modifiableItems = (LinkedList<WeightedItem<T>>) items.clone();
        int weightRemaining = totalWeight;
        while (0 < num--) {
            WeightedItem<U> pickedItem =
                    pickTargetWeight(modifiableItems, random.nextInt(weightRemaining), validator, arg);
            weightRemaining -= pickedItem.weight;
            results.add(pickedItem.item);
            if (weightRemaining == 0) {
                break;
            }
        }
        return ImmutableList.copyOf(results);
    }

    private static <T, U, A> WeightedItem<U> pickTargetWeight(
            LinkedList<WeightedItem<T>> modifiableItems, int targetWeight, BiFunction<T, A, U> validator, A arg) {
        int runningWeight = 0;
        for (Iterator<WeightedItem<T>> it = modifiableItems.iterator(); ; ) {
            WeightedItem<T> item = it.next();
            // Item chosen
            if (targetWeight < (runningWeight += item.weight)) {
                //                totalWeight -= item.weight;
                U mappedItem = validator.apply(item.item, arg);
                if (mappedItem == null) {
                    continue;
                }
                it.remove();
                return WeightedItem.of(mappedItem, item.weight);
            }
        }
    }

    private record WeightedItem<T>(T item, int weight) {
        public static <U> WeightedItem<U> of(U item, int weight) {
            return new WeightedItem<>(item, weight);
        }

        public static <U> Function<U, WeightedItem<U>> mapper(ToIntFunction<U> toIntFunction) {
            return item -> WeightedItem.of(item, toIntFunction.applyAsInt(item));
        }
    }

    public static WeightedPicker<IndexedItem<Integer>, Integer, Void> indexPicker(List<Integer> list) {
        return new WeightedPicker<>(IndexedItem.indexList(list), IndexedItem::item, (item, _null) -> item.index());
    }
}
