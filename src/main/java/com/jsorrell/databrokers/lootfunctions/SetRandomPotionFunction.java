package com.jsorrell.databrokers.lootfunctions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SetRandomPotionFunction extends LootItemConditionalFunction {
    private final boolean onlyBrewable;
    private final @Nullable HolderSet<Potion> options;

    public static final MapCodec<SetRandomPotionFunction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> commonFields(instance)
                    .and(instance.group(
                            RegistryCodecs.homogeneousList(Registries.POTION)
                                    .optionalFieldOf("options")
                                    .forGetter(f -> Optional.ofNullable(f.options)),
                            Codec.BOOL.optionalFieldOf("onlyBrewable", true).forGetter(f -> f.onlyBrewable)))
                    .apply(instance, (a, b, c) -> new SetRandomPotionFunction(a, b.orElse(null), c)));

    protected SetRandomPotionFunction(
            List<LootItemCondition> predicates, @Nullable HolderSet<Potion> options, boolean onlyBrewable) {
        super(predicates);
        this.options = options;
        this.onlyBrewable = onlyBrewable;
    }

    @Override
    public @NotNull LootItemFunctionType<? extends LootItemConditionalFunction> getType() {
        return DataBrokersLootItemFunctions.SET_RANDOM_POTION;
    }

    @Override
    protected @NotNull ItemStack run(ItemStack stack, LootContext context) {
        List<Holder<Potion>> potionPossibilities = options != null
                ? options.stream().toList()
                : BuiltInRegistries.POTION
                        .holders()
                        .filter(p -> !p.value().getEffects().isEmpty())
                        .filter(p -> !onlyBrewable
                                || context.getLevel().potionBrewing().isBrewablePotion(p))
                        .map(p -> (Holder<Potion>) p)
                        .toList();
        Holder<Potion> potion = Util.getRandom(potionPossibilities, context.getRandom());
        stack.update(DataComponents.POTION_CONTENTS, PotionContents.EMPTY, potion, PotionContents::withPotion);
        return stack;
    }

    public static Builder randomPotion() {
        return new Builder().allowingNonBrewable();
    }

    public static Builder randomBrewablePotion() {
        return new Builder();
    }

    public static class Builder extends LootItemConditionalFunction.Builder<Builder> {
        private @Nullable HolderSet<Potion> options;
        private boolean onlyBrewable = true;

        @Override
        protected @NotNull Builder getThis() {
            return this;
        }

        public Builder withPotion(Holder<Potion> potion) {
            options = HolderSet.direct(potion);
            return this;
        }

        public Builder withOneOf(HolderSet<Potion> potions) {
            options = potions;
            return this;
        }

        public Builder allowingNonBrewable() {
            onlyBrewable = false;
            return this;
        }

        @Override
        public @NotNull LootItemFunction build() {
            return new SetRandomPotionFunction(getConditions(), options, onlyBrewable);
        }
    }
}
