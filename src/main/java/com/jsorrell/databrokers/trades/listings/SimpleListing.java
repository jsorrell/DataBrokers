package com.jsorrell.databrokers.trades.listings;

import com.jsorrell.databrokers.trades.TradeContext;
import com.jsorrell.databrokers.trades.offermodifiers.*;
import com.jsorrell.databrokers.trades.stack.BuyStack;
import com.jsorrell.databrokers.trades.stack.SellStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleListing extends Listing {
    public static final int DEFAULT_USES = 12;
    public static final int DEFAULT_XP = 1;
    public static final float DEFAULT_MULTIPLIER = LOW_MULTIPLIER;

    public static final MapCodec<SimpleListing> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    BuyStack.CODEC.fieldOf("buy").forGetter(l -> l.buyStack),
                    BuyStack.CODEC.optionalFieldOf("buyB").forGetter(l -> Optional.ofNullable(l.buyStack2)),
                    SellStack.CODEC.fieldOf("sell").forGetter(l -> l.sellStack),
                    Codec.INT.optionalFieldOf("maxUses", DEFAULT_USES).forGetter(l -> l.maxUses),
                    Codec.INT.optionalFieldOf("xp", DEFAULT_XP).forGetter(l -> l.villagerXp),
                    Codec.FLOAT
                            .optionalFieldOf("priceMultiplier", DEFAULT_MULTIPLIER)
                            .forGetter(l -> l.priceMultiplier),
                    Codec.BOOL.lenientOptionalFieldOf("rewardExp", true).forGetter(l -> l.rewardExp),
                    commonFields(instance).t1(),
                    commonFields(instance).t2(),
                    commonFields(instance).t3())
            .apply(
                    instance,
                    (a, b, c, d, e, f, g, h, i, j) -> new SimpleListing(a, b.orElse(null), c, d, e, f, g, h, i, j)));

    protected final BuyStack buyStack;
    protected final @Nullable BuyStack buyStack2;
    protected final SellStack sellStack;

    protected final int maxUses;
    protected final int villagerXp;
    protected final float priceMultiplier;
    protected final boolean rewardExp;

    public SimpleListing(
            BuyStack buyStack,
            @Nullable BuyStack buyStack2,
            SellStack sellStack,
            int maxUses,
            int villagerXp,
            float priceMultiplier,
            boolean rewardExp,
            List<OfferModifier> modifiers,
            List<LootItemCondition> conditions,
            int weight) {
        super(modifiers, conditions, weight);
        this.buyStack = buyStack;
        this.buyStack2 = buyStack2;
        this.sellStack = sellStack;
        this.maxUses = maxUses;
        this.villagerXp = villagerXp;
        this.priceMultiplier = priceMultiplier;
        this.rewardExp = rewardExp;
    }

    public void validate(ValidationContext validator) {
        super.validate(validator);
        buyStack.validate(validator);
        if (buyStack2 != null) {
            buyStack2.validate(validator);
        }
        sellStack.validate(validator);
    }

    @Override
    public @NotNull Type<?> type() {
        return Listings.SIMPLE;
    }

    @Override
    public MerchantOffer makeOffer(TradeContext context) {
        ItemCost cost = buyStack.getItemCost(context);
        if (cost == null) {
            return null;
        }

        ItemStack result = sellStack.getItemStack(context);
        if (result == null) {
            return null;
        }

        Optional<ItemCost> costBOpt = Optional.empty();
        if (buyStack2 != null) {
            ItemCost cost2 = buyStack2.getItemCost(context);
            if (cost2 == null) {
                return null;
            }
            costBOpt = Optional.of(cost2);
        }

        return new MerchantOffer(cost, costBOpt, result, 0, maxUses, rewardExp, 0, 0, priceMultiplier, villagerXp);
    }

    public static Builder builder(BuyStack.Builder buys, SellStack.Builder sells) {
        return builder(buys, null, sells);
    }

    public static Builder builder(BuyStack.Builder buys, @Nullable BuyStack.Builder buys2, SellStack.Builder sells) {
        return new Builder(buys.build(), buys2 == null ? null : buys2.build(), sells.build());
    }

    public static class Builder extends Listing.Builder<Builder> {
        private final BuyStack buys;
        private final @Nullable BuyStack buys2;
        private final SellStack sells;
        private int uses = DEFAULT_USES;
        private int xp = DEFAULT_XP;
        private float multiplier = DEFAULT_MULTIPLIER;
        private boolean rewardXp = true;

        public Builder(BuyStack buys, @Nullable BuyStack buys2, SellStack sells) {
            this.buys = buys;
            this.buys2 = buys2;
            this.sells = sells;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        public Builder uses(int uses) {
            this.uses = uses;
            return this;
        }

        public Builder xp(int xp) {
            this.xp = xp;
            return this;
        }

        public Builder highMultiplier() {
            multiplier = HIGH_MULTIPLIER;
            return this;
        }

        public Builder multiplier(float multiplier) {
            this.multiplier = multiplier;
            return this;
        }

        public Builder disableRewardXp() {
            rewardXp = false;
            return this;
        }

        @Override
        public Listing build() {
            return new SimpleListing(
                    buys, buys2, sells, uses, xp, multiplier, rewardXp, getModifiers(), getConditions(), getWeight());
        }
    }
}
