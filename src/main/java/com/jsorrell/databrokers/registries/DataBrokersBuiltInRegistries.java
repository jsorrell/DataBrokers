package com.jsorrell.databrokers.registries;

import com.jsorrell.databrokers.DataBrokers;
import com.jsorrell.databrokers.trades.listings.Listing;
import com.jsorrell.databrokers.trades.listings.Listings;
import com.jsorrell.databrokers.trades.offermodifiers.OfferModifier;
import com.jsorrell.databrokers.trades.offermodifiers.OfferModifiers;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

public abstract class DataBrokersBuiltInRegistries {
    private static final List<Pair<ResourceLocation, Supplier<?>>> LOADERS = new LinkedList<>();

    public static final Registry<Listing.Type<?>> LISTING_TYPE =
            simpleRegistry(DataBrokersRegistries.LISTING_TYPE, registry -> Listings.EMPTY);
    public static final Registry<OfferModifier.Type<?>> OFFER_MODIFIER_TYPE =
            simpleRegistry(DataBrokersRegistries.OFFER_MODIFIER_TYPE, registry -> OfferModifiers.VANILLA_ENCHANT_BOOK);

    private static <T> Registry<T> simpleRegistry(
            ResourceKey<Registry<T>> registryKey, RegistryBootstrap<T> bootstrapper) {
        Registry<T> registry = FabricRegistryBuilder.createSimple(registryKey).buildAndRegister();
        LOADERS.add(Pair.of(registryKey.location(), () -> bootstrapper.run(registry)));
        return registry;
    }

    private static void fillRegistries() {
        LOADERS.forEach(pair -> {
            if (pair.getRight().get() == null) {
                DataBrokers.LOGGER.error("Unable to bootstrap registry '{}'", pair.getLeft());
            }
        });
    }

    public static void bootstrap() {
        fillRegistries();
    }

    @FunctionalInterface
    interface RegistryBootstrap<T> {
        Object run(Registry<T> registry);
    }
}
