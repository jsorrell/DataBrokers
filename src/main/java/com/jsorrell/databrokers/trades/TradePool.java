package com.jsorrell.databrokers.trades;

import com.google.common.collect.ImmutableList;
import com.jsorrell.databrokers.trades.listings.Listing;
import com.jsorrell.databrokers.util.WeightedPicker;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import org.jetbrains.annotations.NotNull;

// Generates [rolls] listings
public class TradePool implements MerchantOfferProvider {
    protected final NumberProvider rolls;
    protected final List<Listing> entries;
    protected final List<LootItemCondition> conditions;
    private final Predicate<LootContext> compositeCondition;
    private final WeightedPicker<Listing, MerchantOffer, TradeContext> picker;

    public static final Codec<TradePool> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Listing.CODEC.listOf().fieldOf("entries").forGetter(p -> p.entries),
                    LootItemCondition.DIRECT_CODEC
                            .listOf()
                            .optionalFieldOf("conditions", List.of())
                            .forGetter(p -> p.conditions),
                    NumberProviders.CODEC.fieldOf("rolls").forGetter(p -> p.rolls))
            .apply(instance, TradePool::new));

    TradePool(List<Listing> entries, List<LootItemCondition> conditions, NumberProvider rolls) {
        this.rolls = rolls;
        this.entries = entries;
        this.conditions = conditions;
        this.compositeCondition = Util.allOf(conditions);
        this.picker = new WeightedPicker<>(entries, Listing::getWeight, Listing::getOffer);
    }

    public void validate(ValidationContext validator) {
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).validate(validator.forChild(".entries[" + i + "]"));
        }
        for (int i = 0; i < conditions.size(); i++) {
            conditions.get(i).validate(validator.forChild(".conditions[" + i + "]"));
        }
        rolls.validate(validator.forChild(".rolls"));
    }

    @Override
    public void generateOffers(Consumer<MerchantOffer> output, TradeContext context) {
        if (!compositeCondition.test(context.lootContext())) {
            return;
        }
        picker.doPick(rolls.getInt(context.lootContext()), context.random(), context)
                .forEach(output);

        //        WeightedPicker.pick(
        //                        entries,
        //                        rolls.getInt(context.lootContext()),
        //                        Listing::getWeight,
        //                        l -> l.getOffer(context),
        //                        Objects::nonNull,
        //                        context.random())
        //                .forEach(output);
    }

    public static Builder tradePool() {
        return new Builder();
    }

    public static class Builder implements ConditionUserBuilder<Builder> {
        private final ImmutableList.Builder<Listing> entries = ImmutableList.builder();
        private final ImmutableList.Builder<LootItemCondition> conditions = ImmutableList.builder();
        private NumberProvider rolls = ConstantValue.exactly(1);

        public Builder setRolls(NumberProvider rolls) {
            this.rolls = rolls;
            return this;
        }

        public Builder add(Listing.Builder<?> listingBuilder) {
            entries.add(listingBuilder.build());
            return this;
        }

        @Override
        public @NotNull Builder when(LootItemCondition.Builder conditionBuilder) {
            conditions.add(conditionBuilder.build());
            return this;
        }

        @Override
        public @NotNull Builder unwrap() {
            return this;
        }

        public TradePool build() {
            return new TradePool(entries.build(), conditions.build(), rolls);
        }
    }
}
