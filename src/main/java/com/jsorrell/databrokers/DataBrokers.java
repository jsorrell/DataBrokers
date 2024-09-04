package com.jsorrell.databrokers;

import com.jsorrell.databrokers.lootfunctions.DataBrokersLootItemFunctions;
import com.jsorrell.databrokers.registries.DataBrokersBuiltInRegistries;
import com.jsorrell.databrokers.util.DataBrokersResourceLocation;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataBrokers implements ModInitializer {
    public static final String MOD_ID = "databrokers";
    public static final String MOD_NAME = "DataBrokers";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        DataBrokersLootItemFunctions.registerLootFunctions();
        DataBrokersBuiltInRegistries.bootstrap();
        // There doesn't seem to be a good way to override the trade rebalance feature pack,
        // so we add a resource pack that must be enabled along with the feature toggle for rebalanced trades.
        // Maybe revisit in the future.
        if (!ResourceManagerHelper.registerBuiltinResourcePack(
                DataBrokersResourceLocation.fromPath("trade_rebalance"),
                FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(),
                Component.translatable("dataPack.databrokers.trade_rebalance.name"),
                ResourcePackActivationType.NORMAL)) {
            LOGGER.warn("Could not register trade_rebalance datapack");
        }
    }
}
