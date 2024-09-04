package com.jsorrell.databrokers.trades;

import com.google.common.collect.ImmutableSet;
import com.jsorrell.databrokers.lootfunctions.DataBrokersLootContextParamSets;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

public class TradeContext {
    private final Entity trader;
    private final RandomSource random;
    private final LootContext lootContext;

    public TradeContext(Entity trader, RandomSource random) {
        this.trader = trader;
        this.random = random;
        ServerLevel level = (ServerLevel) trader.level();
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, trader)
                .withParameter(LootContextParams.ORIGIN, trader.position())
                .create(DataBrokersLootContextParamSets.TRADES);
        lootContext = new LootContext.Builder(params).create(Optional.empty());
    }

    public Entity trader() {
        return trader;
    }

    public RandomSource random() {
        return random;
    }

    @Nullable
    public VillagerType villagerType() {
        if (trader instanceof VillagerDataHolder villager) {
            return villager.getVariant();
        }
        return null;
    }

    public ServerLevel level() {
        return (ServerLevel) trader.level();
    }

    public LootContext lootContext() {
        return lootContext;
    }

    public static Set<LootContextParam<?>> suppliedContextParams() {
        return ImmutableSet.of(LootContextParams.ORIGIN, LootContextParams.THIS_ENTITY);
    }

    public static <T extends LootContextUser> DataResult<T> validateLootContext(T lootContextUser) {
        return suppliedContextParams().containsAll(lootContextUser.getReferencedContextParams())
                ? DataResult.success(lootContextUser)
                : DataResult.error(() -> "Not enough loot context for trade");
    }
}
