package com.jsorrell.databrokers.lootfunctions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

public class EnchantWithProviderFunction extends LootItemConditionalFunction {
    private final EnchantmentProvider provider;
    public static final MapCodec<EnchantWithProviderFunction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> commonFields(instance)
                    .and(EnchantmentProvider.DIRECT_CODEC.fieldOf("provider").forGetter(m -> m.provider))
                    .apply(instance, EnchantWithProviderFunction::new));

    protected EnchantWithProviderFunction(List<LootItemCondition> predicates, EnchantmentProvider provider) {
        super(predicates);
        this.provider = provider;
    }

    @Override
    public @NotNull LootItemFunctionType<? extends LootItemConditionalFunction> getType() {
        return DataBrokersLootItemFunctions.ENCHANT_WITH_PROVIDER;
    }

    @Override
    protected @NotNull ItemStack run(ItemStack stack, LootContext context) {
        DifficultyInstance difficulty = context.getLevel()
                .getCurrentDifficultyAt(BlockPos.containing(context.getParam(LootContextParams.ORIGIN)));
        EnchantmentHelper.updateEnchantments(
                stack, enchantments -> provider.enchant(stack, enchantments, context.getRandom(), difficulty));
        return stack;
    }

    @Override
    public @NotNull Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.ORIGIN);
    }

    public static EnchantWithProviderFunction.Builder enchantWithProvider(EnchantmentProvider provider) {
        return new EnchantWithProviderFunction.Builder(provider);
    }

    public static class Builder extends LootItemConditionalFunction.Builder<Builder> {
        private final EnchantmentProvider provider;

        public Builder(EnchantmentProvider provider) {
            this.provider = provider;
        }

        @Override
        protected @NotNull Builder getThis() {
            return this;
        }

        @Override
        public @NotNull LootItemFunction build() {
            return new EnchantWithProviderFunction(getConditions(), provider);
        }
    }
}
