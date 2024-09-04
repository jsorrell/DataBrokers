package com.jsorrell.databrokers.data;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.jsorrell.databrokers.DataBrokers;
import com.jsorrell.databrokers.lootfunctions.DataBrokersLootContextParamSets;
import com.jsorrell.databrokers.trades.TradeLineup;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.*;
import net.minecraft.data.CachedOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.ValidationContext;
import org.jetbrains.annotations.NotNull;

public abstract class TradeProvider extends FabricDynamicRegistryProvider {
    private final boolean strictValidation;
    private final Set<ResourceKey<TradeLineup>> requiredLineups;

    public TradeProvider(
            FabricDataOutput output,
            Set<ResourceKey<TradeLineup>> requiredLineups,
            CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
        strictValidation = output.isStrictValidationEnabled();
        this.requiredLineups = requiredLineups;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput writer) {
        return super.run(writer);
    }

    @Override
    protected final void configure(HolderLookup.Provider registries, Entries entries) {
        ProblemReporter.Collector collector = new ProblemReporter.Collector();
        ValidationContext validationContext =
                new ValidationContext(collector, DataBrokersLootContextParamSets.TRADES, registries.asGetterLookup());

        HashSet<ResourceKey<TradeLineup>> trades = new HashSet<>();
        generateTrades(registries, (key, builder) -> {
            TradeLineup lineup = builder.build();
            lineup.validate(validationContext.enterElement("{" + key.location() + "}", key));
            trades.add(key);
            entries.add(key, lineup);
        });

        if (strictValidation) {
            for (ResourceKey<TradeLineup> missingKey : Sets.difference(requiredLineups, trades)) {
                collector.report("Missing built-in lineup: " + missingKey.location());
            }
        }

        Multimap<String, String> validationErrors = collector.get();
        if (!validationErrors.isEmpty()) {
            validationErrors.forEach(
                    (name, message) -> DataBrokers.LOGGER.warn("Found validation problem in {}: {}", name, message));
            throw new IllegalStateException("Failed to validate trade lineups");
        }
    }

    protected abstract void generateTrades(
            HolderLookup.Provider registries, BiConsumer<ResourceKey<TradeLineup>, TradeLineup.Builder> output);

    @Override
    public @NotNull String getName() {
        return "Trade Lineups";
    }
}
