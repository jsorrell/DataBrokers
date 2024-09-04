package com.jsorrell.databrokers.lootfunctions;

import com.jsorrell.databrokers.util.IndexedItem;
import com.jsorrell.databrokers.util.WeightedPicker;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

public class DyeRandomlyFunction extends LootItemConditionalFunction {
    private final List<Integer> weights;
    private final WeightedPicker<IndexedItem<Integer>, Integer, Void> numDyesPicker;
    public static final List<Integer> ONE_DYE = List.of(1);
    public static final List<Integer> TWO_DYES = List.of(0, 1);
    public static final List<Integer> THREE_DYES = List.of(0, 0, 1);
    // In vanilla trades, 1 dyes is applied, then separate 30% and 20% chances of other dyes being added. This means:
    // 100% chance of at least 1 | 56% chance of exactly 1
    // 44% chance of at least 2 | 38% chance of exactly 2
    // 6% chance of 3
    public static final List<Integer> VANILLA_DYED_ARMOR_WEIGHTS = List.of(56, 38, 6);

    public static final MapCodec<DyeRandomlyFunction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> commonFields(instance)
                    .and(Codec.INT
                            .listOf(1, Integer.MAX_VALUE)
                            .optionalFieldOf("weights", ONE_DYE)
                            .validate(w -> validateDyeWeights(w)
                                    ? DataResult.success(w)
                                    : DataResult.error(() -> "Negative dye weights not allowed"))
                            .forGetter(f -> f.weights))
                    .apply(instance, DyeRandomlyFunction::new));

    private DyeRandomlyFunction(List<LootItemCondition> predicates, List<Integer> weights) {
        super(predicates);
        if (!validateDyeWeights(weights)) {
            throw new IllegalArgumentException("Invalid dye weights");
        }
        this.weights = weights;
        numDyesPicker = WeightedPicker.indexPicker(weights);
    }

    private static boolean validateDyeWeights(List<Integer> weights) {
        return !weights.isEmpty() && weights.stream().noneMatch(weight -> weight < 0);
    }

    @Override
    public @NotNull LootItemFunctionType<? extends LootItemConditionalFunction> getType() {
        return DataBrokersLootItemFunctions.DYE_RANDOMLY;
    }

    private static DyeItem getRandomDye(RandomSource random) {
        return DyeItem.byColor(DyeColor.byId(random.nextInt(16)));
    }

    @Override
    protected @NotNull ItemStack run(ItemStack stack, LootContext context) {
        Supplier<List<DyeItem>> dyeSupplier = () -> {
            Integer numDyesIdx = numDyesPicker.doSinglePick(context.getRandom(), null);
            assert numDyesIdx != null;
            int numDyes = numDyesIdx + 1;
            List<DyeItem> dyes = new ArrayList<>(numDyes);
            IntStream.range(0, numDyes).forEach(i -> dyes.add(getRandomDye(context.getRandom())));
            return dyes;
        };
        return stack.is(ItemTags.DYEABLE) ? DyedItemColor.applyDyes(stack, dyeSupplier.get()) : stack;
    }

    public static Builder singleDye() {
        return new Builder(ONE_DYE);
    }

    public static Builder vanillaDyedArmorWeights() {
        return new Builder(VANILLA_DYED_ARMOR_WEIGHTS);
    }

    public static Builder withWeights(List<Integer> weights) {
        return new Builder(weights);
    }

    public static class Builder extends LootItemConditionalFunction.Builder<Builder> {
        private final List<Integer> weights;

        public Builder(List<Integer> weights) {
            if (!validateDyeWeights(weights)) {
                throw new IllegalArgumentException("Invalid dye weights");
            }
            this.weights = weights;
        }

        @Override
        protected @NotNull Builder getThis() {
            return this;
        }

        @Override
        public @NotNull LootItemFunction build() {
            return new DyeRandomlyFunction(getConditions(), weights);
        }
    }
}
