package com.jsorrell.databrokers.trades.listings;

import com.jsorrell.databrokers.registries.DataBrokersBuiltInRegistries;
import com.jsorrell.databrokers.util.DataBrokersResourceLocation;
import net.minecraft.core.Registry;

public abstract class Listings {
    public static final Listing.Type<EmptyListing> EMPTY = register("empty", new Listing.Type<>(EmptyListing.CODEC));
    public static final Listing.Type<SimpleListing> SIMPLE =
            register("simple", new Listing.Type<>(SimpleListing.CODEC));
    public static final Listing.Type<AlternativesListing> ALTERNATIVES =
            register("alternatives", new Listing.Type<>(AlternativesListing.CODEC));

    private static <T extends Listing> Listing.Type<T> register(String name, Listing.Type<T> type) {
        Registry.register(DataBrokersBuiltInRegistries.LISTING_TYPE, DataBrokersResourceLocation.fromPath(name), type);
        return type;
    }
}
