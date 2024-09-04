package com.jsorrell.databrokers.util;

import com.jsorrell.databrokers.DataBrokers;
import net.minecraft.resources.ResourceLocation;

public abstract class DataBrokersResourceLocation {
    public static final String NAMESPACE = DataBrokers.MOD_ID;

    public static ResourceLocation fromPath(String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
    }
}
