package com.jsorrell.databrokers.data;

import com.jsorrell.databrokers.trades.BuiltInTradeLineups;
import com.jsorrell.databrokers.trades.TradeLineup;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.registries.TradeRebalanceRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.VillagerTrades;
import org.jetbrains.annotations.NotNull;

public class TradeRebalanceTradeProvider extends TradeProvider {
    public TradeRebalanceTradeProvider(
            FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(
                output,
                Set.of(),
                TradeRebalanceRegistries.createLookup(registriesFuture)
                        .thenApply(RegistrySetBuilder.PatchedRegistries::patches));
    }

    @Override
    protected void generateTrades(
            HolderLookup.Provider registries, BiConsumer<ResourceKey<TradeLineup>, TradeLineup.Builder> output) {
        VanillaTradesConverter.consumeVillagerTrades(output, VillagerTrades.EXPERIMENTAL_TRADES, registries);
        output.accept(
                BuiltInTradeLineups.WANDERING_TRADER,
                VanillaTradesConverter.convertExperimentalWanderingTraderTrades(
                        VillagerTrades.EXPERIMENTAL_WANDERING_TRADER_TRADES, registries));
    }

    @Override
    public @NotNull String getName() {
        return "Trade Rebalance Trade Lineups";
    }
}
