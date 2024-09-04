package com.jsorrell.databrokers.trades;

import com.google.common.collect.ImmutableList;
import com.jsorrell.databrokers.lootfunctions.DataBrokersLootContextParamSets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

// Generates listings for every pool
public class TradeLineup implements MerchantOfferProvider {
    public static final TradeLineup EMPTY = new TradeLineup(List.of());
    public static final LootContextParamSet PARAM_SET = DataBrokersLootContextParamSets.TRADES;

    public final List<TradePool> pools;

    public static final Codec<TradeLineup> CODEC =
            Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(TradePool.CODEC
                            .listOf()
                            .optionalFieldOf("pools", List.of())
                            .forGetter(l -> l.pools))
                    .apply(instance, TradeLineup::new)));

    protected TradeLineup(List<TradePool> pools) {
        this.pools = pools;
    }

    public void validate(ValidationContext validator) {
        for (int i = 0; i < pools.size(); i++) {
            pools.get(i).validate(validator.forChild(".pools[" + i + "]"));
        }
    }

    @Override
    public void generateOffers(Consumer<MerchantOffer> output, TradeContext context) {
        pools.forEach(pool -> pool.generateOffers(output, context));
    }

    public static Builder lineup() {
        return new Builder();
    }

    public static Builder villager(TradePool.Builder pool) {
        return lineup().withPool(pool.setRolls(ConstantValue.exactly(2)));
    }

    public static Builder wanderingTrader(TradePool.Builder tier1Pool, TradePool.Builder tier2Pool) {
        return lineup().withPool(tier1Pool.setRolls(ConstantValue.exactly(5)))
                .withPool(tier2Pool.setRolls(ConstantValue.exactly(1)));
    }

    public static class Builder {
        private final ImmutableList.Builder<TradePool> pools = ImmutableList.builder();

        public Builder withPool(TradePool.Builder poolBuilder) {
            pools.add(poolBuilder.build());
            return this;
        }

        public TradeLineup build() {
            return new TradeLineup(pools.build());
        }
    }
}
