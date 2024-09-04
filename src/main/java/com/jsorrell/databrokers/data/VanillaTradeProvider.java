package com.jsorrell.databrokers.data;

import com.jsorrell.databrokers.trades.BuiltInTradeLineups;
import com.jsorrell.databrokers.trades.TradeLineup;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.VillagerTrades;

public class VanillaTradeProvider extends TradeProvider {
    public VanillaTradeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, BuiltInTradeLineups.all(), registries);
    }

    @Override
    protected void generateTrades(
            HolderLookup.Provider registries, BiConsumer<ResourceKey<TradeLineup>, TradeLineup.Builder> output) {
        VanillaTradesConverter.consumeVillagerTrades(output, VillagerTrades.TRADES, registries);
        output.accept(
                BuiltInTradeLineups.WANDERING_TRADER,
                VanillaTradesConverter.convertWanderingTraderTrades(
                        VillagerTrades.WANDERING_TRADER_TRADES, registries));
    }
}
