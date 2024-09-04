package com.jsorrell.databrokers.trades.offermodifiers;

import com.jsorrell.databrokers.trades.TradeContext;
import com.jsorrell.databrokers.util.MerchantOfferUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.Nullable;

public record VanillaEnchantItemModifier(NumberProvider levels, @Nullable HolderSet<Enchantment> availableEnchantments)
        implements OfferModifier {
    private static final NumberProvider VANILLA_LEVELS = UniformGenerator.between(5, 19);
    public static final MapCodec<VanillaEnchantItemModifier> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                            NumberProviders.CODEC
                                    .optionalFieldOf("levels", VANILLA_LEVELS)
                                    .forGetter(vanillaEnchantItemModifier -> vanillaEnchantItemModifier.levels),
                            RegistryCodecs.homogeneousList(Registries.ENCHANTMENT)
                                    .optionalFieldOf("enchantments")
                                    .forGetter(s -> Optional.ofNullable(s.availableEnchantments)))
                    .apply(instance, (a, b) -> new VanillaEnchantItemModifier(a, b.orElse(null))));

    public VanillaEnchantItemModifier() {
        this(VANILLA_LEVELS, null);
    }

    @Override
    public MerchantOffer apply(MerchantOffer offer, TradeContext context) {
        int level = levels.getInt(context.lootContext());
        Supplier<HolderSet.Named<Enchantment>> defaultEnchSupplier = () -> context.level()
                .registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getTag(EnchantmentTags.ON_TRADED_EQUIPMENT)
                .orElseThrow();
        HolderSet<Enchantment> enchs = Objects.requireNonNullElseGet(availableEnchantments, defaultEnchSupplier);

        return MerchantOfferUtils.editor()
                .replaceResult(r -> EnchantmentHelper.enchantItem(
                        context.random(),
                        offer.getResult(),
                        level,
                        context.level().registryAccess(),
                        Optional.of(enchs)))
                .replaceItemCostCount(c -> c + level)
                .replaceItemCostCount(c -> Math.min(c, 64))
                .apply(offer);
    }

    @Override
    public Type<?> type() {
        return OfferModifiers.VANILLA_ENCHANT_ITEM;
    }
}
