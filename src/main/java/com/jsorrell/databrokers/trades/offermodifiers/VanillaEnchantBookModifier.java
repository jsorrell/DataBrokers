package com.jsorrell.databrokers.trades.offermodifiers;

import com.jsorrell.databrokers.DataBrokers;
import com.jsorrell.databrokers.registries.DataBrokersBuiltInRegistries;
import com.jsorrell.databrokers.trades.TradeContext;
import com.jsorrell.databrokers.util.MerchantOfferUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

public class VanillaEnchantBookModifier implements OfferModifier {
    public static final MapCodec<VanillaEnchantBookModifier> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                            RegistryCodecs.homogeneousList(Registries.ENCHANTMENT)
                                    .optionalFieldOf("enchantments")
                                    .forGetter(s -> Optional.ofNullable(s.availableEnchantments)),
                            Codec.INT.optionalFieldOf("min_level", 0).forGetter(l -> l.minLevel),
                            Codec.INT
                                    .optionalFieldOf("max_level", Integer.MAX_VALUE)
                                    .forGetter(l -> l.maxLevel))
                    .apply(instance, (a, b, c) -> new VanillaEnchantBookModifier(a.orElse(null), b, c)));

    @Nullable
    private final HolderSet<Enchantment> availableEnchantments;

    private final int minLevel;
    private final int maxLevel;

    public VanillaEnchantBookModifier(
            @Nullable HolderSet<Enchantment> availableEnchantments, int minLevel, int maxLevel) {
        this.availableEnchantments = availableEnchantments;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
    }

    @Override
    public @Nullable MerchantOffer apply(MerchantOffer offer, TradeContext context) {
        if (!(offer.getResult().is(Items.ENCHANTED_BOOK) || offer.getResult().is(Items.BOOK))) {
            // skip if result isn't book
            DataBrokers.LOGGER.warn(
                    "{} modifier applied to non-book. Skipping.",
                    DataBrokersBuiltInRegistries.OFFER_MODIFIER_TYPE.getKey(type()));
            return offer;
        }

        Supplier<HolderSet.Named<Enchantment>> defaultEnchSupplier = () -> context.level()
                .registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getTag(EnchantmentTags.TRADEABLE)
                .orElseThrow();
        Optional<Holder<Enchantment>> enchOpt = Objects.requireNonNullElseGet(
                        availableEnchantments, defaultEnchSupplier)
                .getRandomElement(context.random());

        if (enchOpt.isEmpty()) {
            return MerchantOfferUtils.editor()
                    .replaceResult(_s -> new ItemStack(Items.BOOK))
                    .replaceItemCostCount(_c -> 1)
                    .apply(offer);
        }

        Holder<Enchantment> enchHolder = enchOpt.get();
        Enchantment ench = enchHolder.value();
        int min = Math.max(ench.getMinLevel(), minLevel);
        int max = Math.min(ench.getMaxLevel(), maxLevel);
        int level = Mth.nextInt(context.random(), min, max);
        int multiplier = enchHolder.is(EnchantmentTags.DOUBLE_TRADE_PRICE) ? 2 : 1;
        return MerchantOfferUtils.editor()
                .replaceResult(_s -> EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchHolder, level)))
                .replaceItemCostCount(c -> 2 + context.random().nextInt(5 + level * 10) + 3 * level)
                .replaceItemCostCount(c -> c * multiplier)
                .replaceItemCostCount(c -> Math.min(c, 64))
                .apply(offer);
    }

    @Override
    public OfferModifier.Type<?> type() {
        return OfferModifiers.VANILLA_ENCHANT_BOOK;
    }
}
