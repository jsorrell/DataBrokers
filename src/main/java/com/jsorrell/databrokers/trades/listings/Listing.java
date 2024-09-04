package com.jsorrell.databrokers.trades.listings;

import com.google.common.collect.ImmutableList;
import com.jsorrell.databrokers.registries.DataBrokersBuiltInRegistries;
import com.jsorrell.databrokers.trades.TradeContext;
import com.jsorrell.databrokers.trades.offermodifiers.*;
import com.mojang.datafixers.Products;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntitySubPredicates;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Listing implements VillagerTrades.ItemListing {

    public static final Codec<Listing> TYPED_CODEC = DataBrokersBuiltInRegistries.LISTING_TYPE
            .byNameCodec()
            .dispatch("type", Listing::type, Listing.Type::codec);
    public static final Codec<Listing> CODEC = Codec.lazyInitialized(
                    () -> Codec.either(SimpleListing.CODEC.codec(), TYPED_CODEC))
            .xmap(
                    Either::unwrap,
                    listing -> listing instanceof SimpleListing simpleListing
                            ? Either.left(simpleListing)
                            : Either.right(listing));

    public static final float LOW_MULTIPLIER = 0.05f;
    public static final float HIGH_MULTIPLIER = 0.2f;

    protected final int weight;
    protected final List<OfferModifier> modifiers;
    protected final List<LootItemCondition> conditions;

    public Listing(List<OfferModifier> modifiers, List<LootItemCondition> conditions, int weight) {
        this.modifiers = modifiers;
        this.conditions = conditions;
        if (weight < 1) {
            throw new AssertionError("weight must be positive");
        }
        this.weight = weight;
    }

    protected static <T extends Listing>
            Products.P3<RecordCodecBuilder.Mu<T>, List<OfferModifier>, List<LootItemCondition>, Integer> commonFields(
                    RecordCodecBuilder.Instance<T> instance) {
        return instance.group(
                OfferModifier.CODEC
                        .listOf()
                        .optionalFieldOf("modifiers", List.of())
                        .forGetter(l -> l.modifiers),
                LootItemCondition.DIRECT_CODEC
                        .listOf()
                        .optionalFieldOf("conditions", List.of())
                        .forGetter(l -> l.conditions),
                Codec.INT.optionalFieldOf("weight", 1).forGetter(l -> l.weight));
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public @Nullable final MerchantOffer getOffer(Entity trader, RandomSource random) {
        return getOffer(new TradeContext(trader, random));
    }

    public @Nullable final MerchantOffer getOffer(TradeContext context) {
        for (LootItemCondition condition : conditions) {
            if (!condition.test(context.lootContext())) {
                return null;
            }
        }
        MerchantOffer offer = makeOffer(context);
        for (OfferModifier modifier : modifiers) {
            if (offer == null) {
                return null;
            }
            offer = modifier.apply(offer, context);
        }
        return offer;
    }

    public void validate(ValidationContext validator) {
        for (int i = 0; i < modifiers.size(); i++) {
            modifiers.get(i).validate(validator.forChild(".modifiers[" + i + "]"));
        }
        for (int i = 0; i < conditions.size(); i++) {
            conditions.get(i).validate(validator.forChild(".conditions[" + i + "]"));
        }
    }

    public abstract @Nullable MerchantOffer makeOffer(TradeContext context);

    public record Type<T extends Listing>(MapCodec<T> codec) {
        public static final Registry<Type<?>> REGISTRY = DataBrokersBuiltInRegistries.LISTING_TYPE;
    }

    public abstract @NotNull Type<?> type();

    public abstract static class Builder<T extends Builder<T>> implements ConditionUserBuilder<T> {
        private final ImmutableList.Builder<OfferModifier> modifiers = ImmutableList.builder();
        private final ImmutableList.Builder<LootItemCondition> conditions = ImmutableList.builder();
        private int weight = 1;

        public @NotNull T when(LootItemCondition.Builder builder) {
            conditions.add(builder.build());
            return getThis();
        }

        public @NotNull T whenVillagerType(VillagerType type) {
            return when(LootItemEntityPropertyCondition.hasProperties(
                    LootContext.EntityTarget.THIS,
                    EntityPredicate.Builder.entity().subPredicate(EntitySubPredicates.VILLAGER.createPredicate(type))));
        }

        public AlternativesListing.Builder otherwise(Listing.Builder<?> childBuilder) {
            return new AlternativesListing.Builder(this, childBuilder);
        }

        public final @NotNull T unwrap() {
            return getThis();
        }

        protected abstract T getThis();

        public T apply(OfferModifier modifier) {
            modifiers.add(modifier);
            return getThis();
        }

        protected List<OfferModifier> getModifiers() {
            return modifiers.build();
        }

        public T weight(int weight) {
            this.weight = weight;
            return getThis();
        }

        protected int getWeight() {
            return weight;
        }

        protected List<LootItemCondition> getConditions() {
            return conditions.build();
        }

        public abstract Listing build();
    }
}
