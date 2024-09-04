package com.jsorrell.databrokers.trades.listings;

import com.google.common.collect.ImmutableList;
import com.jsorrell.databrokers.trades.TradeContext;
import com.jsorrell.databrokers.trades.offermodifiers.OfferModifier;
import java.util.Collection;
import java.util.List;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GroupListing extends Listing {
    private final List<Listing> children;

    public GroupListing(List<Listing> children, List<OfferModifier> modifiers, List<LootItemCondition> conditions) {
        super(
                modifiers,
                conditions,
                children.stream().mapToInt(Listing::getWeight).sum());
        this.children = children;
    }

    public Collection<Listing> expand() {
        ImmutableList.Builder<Listing> builder = ImmutableList.builder();
        for (Listing child : children) {
            builder.add(new ExpandedListing(modifiers, conditions, child));
        }
        return builder.build();
    }

    @Override
    @Deprecated
    public @Nullable MerchantOffer makeOffer(TradeContext context) {
        throw new UnsupportedOperationException("group listing must be expanded");
    }

    @Override
    public void validate(ValidationContext validator) {
        super.validate(validator);
        for (int i = 0; i < children.size(); i++) {
            conditions.get(i).validate(validator.forChild(".child[" + i + "]"));
        }
    }

    @Override
    public @NotNull Type<?> type() {
        return null;
    }

    private static class ExpandedListing extends Listing {
        private final Listing listing;

        public ExpandedListing(List<OfferModifier> modifiers, List<LootItemCondition> conditions, Listing listing) {
            super(modifiers, conditions, listing.getWeight());
            this.listing = listing;
        }

        @Override
        public @Nullable MerchantOffer makeOffer(TradeContext context) {
            return listing.makeOffer(context);
        }

        @Override
        @Deprecated
        public @NotNull Type<?> type() {
            throw new UnsupportedOperationException("should not be registered");
        }
    }
}
