package com.jsorrell.databrokers.trades;

import com.jsorrell.databrokers.registries.DataBrokersRegistries;
import com.jsorrell.databrokers.util.DataBrokersResourceLocation;
import com.jsorrell.databrokers.util.VillagerTierHolder;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.jetbrains.annotations.Nullable;

public abstract class BuiltInTradeLineups {
    private static final Map<VillagerProfession, VillagerTierHolder<ResourceKey<TradeLineup>>> VILLAGER_TRADES =
            new HashMap<>();

    private static final Set<ResourceKey<TradeLineup>> TRADE_LINEUPS = new HashSet<>();
    private static final Set<ResourceKey<TradeLineup>> IMMUTABLE_TRADE_LINEUPS =
            Collections.unmodifiableSet(TRADE_LINEUPS);

    public static final VillagerTierHolder<ResourceKey<TradeLineup>> ARMORER =
            registerVillager(VillagerProfession.ARMORER);
    public static final VillagerTierHolder<ResourceKey<TradeLineup>> BUTCHER =
            registerVillager(VillagerProfession.BUTCHER);
    public static final VillagerTierHolder<ResourceKey<TradeLineup>> CARTOGRAPHER =
            registerVillager(VillagerProfession.CARTOGRAPHER);
    public static final VillagerTierHolder<ResourceKey<TradeLineup>> CLERIC =
            registerVillager(VillagerProfession.CLERIC);
    public static final VillagerTierHolder<ResourceKey<TradeLineup>> FARMER =
            registerVillager(VillagerProfession.FARMER);
    public static final VillagerTierHolder<ResourceKey<TradeLineup>> FISHERMAN =
            registerVillager(VillagerProfession.FISHERMAN);
    public static final VillagerTierHolder<ResourceKey<TradeLineup>> FLETCHER =
            registerVillager(VillagerProfession.FLETCHER);
    public static final VillagerTierHolder<ResourceKey<TradeLineup>> LEATHERWORKER =
            registerVillager(VillagerProfession.LEATHERWORKER);
    public static final VillagerTierHolder<ResourceKey<TradeLineup>> LIBRARIES =
            registerVillager(VillagerProfession.LIBRARIAN);
    public static final VillagerTierHolder<ResourceKey<TradeLineup>> MASON = registerVillager(VillagerProfession.MASON);
    public static final VillagerTierHolder<ResourceKey<TradeLineup>> SHEPHERD =
            registerVillager(VillagerProfession.SHEPHERD);
    public static final VillagerTierHolder<ResourceKey<TradeLineup>> TOOLSMITH =
            registerVillager(VillagerProfession.TOOLSMITH);
    public static final VillagerTierHolder<ResourceKey<TradeLineup>> WEAPONSMITH =
            registerVillager(VillagerProfession.WEAPONSMITH);

    public static final ResourceKey<TradeLineup> WANDERING_TRADER = register("wandering_trader");

    private static VillagerTierHolder<ResourceKey<TradeLineup>> registerVillager(VillagerProfession profession) {
        VillagerTierHolder.Builder<ResourceKey<TradeLineup>> builder = VillagerTierHolder.builder();
        for (int tier = 1; tier <= VillagerData.MAX_VILLAGER_LEVEL; tier++) {
            builder.withTier(
                    tier,
                    register(BuiltInRegistries.VILLAGER_PROFESSION
                                    .getKey(profession)
                                    .getPath() + "_" + tier));
        }
        VillagerTierHolder<ResourceKey<TradeLineup>> trades = builder.build();
        VILLAGER_TRADES.put(profession, trades);
        return trades;
    }

    private static ResourceKey<TradeLineup> register(String name) {
        return register(
                ResourceKey.create(DataBrokersRegistries.TRADE_LINEUP, DataBrokersResourceLocation.fromPath(name)));
    }

    private static ResourceKey<TradeLineup> register(ResourceKey<TradeLineup> name) {
        if (TRADE_LINEUPS.add(name)) {
            return name;
        } else {
            throw new IllegalArgumentException(name.location() + " is already a registered trade lineup table");
        }
    }

    public static @Nullable VillagerTierHolder<ResourceKey<TradeLineup>> villagerLineups(
            VillagerProfession profession) {
        return VILLAGER_TRADES.get(profession);
    }

    public static Set<ResourceKey<TradeLineup>> all() {
        return IMMUTABLE_TRADE_LINEUPS;
    }
}
