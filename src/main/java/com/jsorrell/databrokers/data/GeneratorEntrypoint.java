package com.jsorrell.databrokers.data;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class GeneratorEntrypoint implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(VanillaTradeProvider::new);

        FabricDataGenerator.Pack tradeRebalancePack =
                fabricDataGenerator.createBuiltinResourcePack(ResourceLocation.withDefaultNamespace("trade_rebalance"));
        tradeRebalancePack.addProvider(TradeRebalanceTradeProvider::new);
        tradeRebalancePack.addProvider((FabricDataGenerator.Pack.Factory<PackMetadataGenerator>)
                packOutput -> PackMetadataGenerator.forFeaturePack(
                        packOutput, Component.translatable("dataPack.databrokers.trade_rebalance.description")));
    }
}
