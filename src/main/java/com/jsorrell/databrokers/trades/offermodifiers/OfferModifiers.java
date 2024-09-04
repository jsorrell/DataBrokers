package com.jsorrell.databrokers.trades.offermodifiers;

import com.jsorrell.databrokers.registries.DataBrokersBuiltInRegistries;
import com.jsorrell.databrokers.util.DataBrokersResourceLocation;
import net.minecraft.core.Registry;

public abstract class OfferModifiers {
    public static final OfferModifier.Type<VanillaEnchantBookModifier> VANILLA_ENCHANT_BOOK =
            register("vanilla_enchant_book", new OfferModifier.Type<>(VanillaEnchantBookModifier.CODEC));
    public static final OfferModifier.Type<VanillaEnchantItemModifier> VANILLA_ENCHANT_ITEM =
            register("vanilla_enchant_item", new OfferModifier.Type<>(VanillaEnchantItemModifier.CODEC));

    private static <T extends OfferModifier> OfferModifier.Type<T> register(String name, OfferModifier.Type<T> type) {
        Registry.register(
                DataBrokersBuiltInRegistries.OFFER_MODIFIER_TYPE, DataBrokersResourceLocation.fromPath(name), type);
        return type;
    }
}
