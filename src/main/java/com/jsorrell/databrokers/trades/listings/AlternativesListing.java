package com.jsorrell.databrokers.trades.listings;

import com.google.common.collect.ImmutableList;
import com.jsorrell.databrokers.trades.TradeContext;
import com.jsorrell.databrokers.trades.offermodifiers.OfferModifier;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

public class AlternativesListing extends Listing {
    public static final MapCodec<AlternativesListing> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Listing.CODEC.listOf().fieldOf("children").forGetter(g -> g.subListings))
            .and(commonFields(instance))
            .apply(instance, AlternativesListing::new));

    private final List<Listing> subListings;

    private AlternativesListing(
            List<Listing> subListings, List<OfferModifier> modifiers, List<LootItemCondition> conditions, int weight) {
        super(modifiers, conditions, weight);
        this.subListings = subListings;
    }

    @Override
    public Listing.@NotNull Type<?> type() {
        return Listings.ALTERNATIVES;
    }

    @Override
    public MerchantOffer makeOffer(TradeContext context) {
        return subListings.stream()
                .map(l -> l.getOffer(context))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public static Builder alternatives(Listing.Builder<?>... children) {
        return new Builder(children);
    }

    public static <T> Builder alternatives(
            Collection<T> childrenSources, Function<T, Listing.Builder<?>> toChildrenFunction) {
        return new Builder(
                childrenSources.stream().map(toChildrenFunction::apply).toArray(Listing.Builder[]::new));
    }

    public static class Builder extends Listing.Builder<Builder> {
        private final ImmutableList.Builder<Listing> children = ImmutableList.builder();

        public Builder(Listing.Builder<?>... listings) {
            for (Listing.Builder<?> builder : listings) {
                children.add(builder.build());
            }
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public Builder otherwise(Listing.Builder<?> childBuilder) {
            children.add(childBuilder.build());
            return this;
        }

        @Override
        public Listing build() {
            return new AlternativesListing(children.build(), getModifiers(), getConditions(), getWeight());
        }
    }
}
