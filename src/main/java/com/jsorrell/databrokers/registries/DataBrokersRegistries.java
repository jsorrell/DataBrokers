package com.jsorrell.databrokers.registries;

import com.jsorrell.databrokers.trades.TradeLineup;
import com.jsorrell.databrokers.trades.listings.Listing;
import com.jsorrell.databrokers.trades.offermodifiers.OfferModifier;
import com.jsorrell.databrokers.util.DataBrokersResourceLocation;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public abstract class DataBrokersRegistries {
    public static final ResourceKey<Registry<TradeLineup>> TRADE_LINEUP =
            dynamicRegistry("trade_lineup", TradeLineup.CODEC);
    public static final ResourceKey<Registry<Listing.Type<?>>> LISTING_TYPE = registry("listing_type");
    public static final ResourceKey<Registry<OfferModifier.Type<?>>> OFFER_MODIFIER_TYPE =
            registry("offer_modifier_type");

    private static <T> ResourceKey<Registry<T>> registry(String key) {
        return ResourceKey.createRegistryKey(DataBrokersResourceLocation.fromPath(key));
    }

    private static <T> ResourceKey<Registry<T>> dynamicRegistry(String key, Codec<T> codec) {
        ResourceKey<Registry<T>> registryKey = registry(key);
        DynamicRegistries.register(registryKey, codec);
        return registryKey;
    }
}
