package com.jsorrell.databrokers.lootfunctions;

import java.util.function.Consumer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public abstract class DataBrokersLootContextParamSets {
    public static final LootContextParamSet TRADES =
            register(builder -> builder.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY));

    private static LootContextParamSet register(Consumer<LootContextParamSet.Builder> builderConsumer) {
        LootContextParamSet.Builder builder = new LootContextParamSet.Builder();
        builderConsumer.accept(builder);
        return builder.build();
    }
}
