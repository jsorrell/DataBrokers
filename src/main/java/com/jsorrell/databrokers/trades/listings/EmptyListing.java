package com.jsorrell.databrokers.trades.listings;

import com.jsorrell.databrokers.trades.TradeContext;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EmptyListing extends Listing {
    public static final MapCodec<EmptyListing> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    commonFields(instance).t2(), commonFields(instance).t3())
            .apply(instance, EmptyListing::new));

    public EmptyListing(List<LootItemCondition> conditions, int weight) {
        super(List.of(), conditions, weight);
    }

    @Override
    public @Nullable MerchantOffer makeOffer(TradeContext context) {
        return null;
    }

    @Override
    public @NotNull Type<?> type() {
        return Listings.EMPTY;
    }

    public static <T> Builder empty() {
        return new Builder();
    }

    public static class Builder extends Listing.Builder<Builder> {
        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public Listing build() {
            return new EmptyListing(getConditions(), getWeight());
        }
    }
}
