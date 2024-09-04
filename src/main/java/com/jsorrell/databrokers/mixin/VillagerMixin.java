package com.jsorrell.databrokers.mixin;

import com.jsorrell.databrokers.registries.DataBrokersRegistries;
import com.jsorrell.databrokers.trades.BuiltInTradeLineups;
import com.jsorrell.databrokers.trades.TradeContext;
import com.jsorrell.databrokers.trades.TradeLineup;
import com.jsorrell.databrokers.util.VillagerTierHolder;
import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager {
    @Shadow
    public abstract VillagerData getVillagerData();

    public VillagerMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void updateTrades() {
        VillagerTierHolder<ResourceKey<TradeLineup>> professionKeys =
                BuiltInTradeLineups.villagerLineups(getVillagerData().getProfession());
        if (professionKeys == null) return;
        Optional<ResourceKey<TradeLineup>> lineupKey =
                professionKeys.getTier(getVillagerData().getLevel());
        if (lineupKey.isEmpty()) {
            return;
        }

        TradeLineup tradeLineup = level().getServer()
                .reloadableRegistries()
                .get()
                .lookup(DataBrokersRegistries.TRADE_LINEUP)
                .flatMap(registry -> registry.get(lineupKey.get()))
                .map(net.minecraft.core.Holder::value)
                .orElse(TradeLineup.EMPTY);
        tradeLineup.generateOffers(getOffers()::add, new TradeContext(this, random));
    }
}
