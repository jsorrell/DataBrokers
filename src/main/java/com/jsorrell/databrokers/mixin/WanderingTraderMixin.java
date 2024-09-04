package com.jsorrell.databrokers.mixin;

import com.jsorrell.databrokers.registries.DataBrokersRegistries;
import com.jsorrell.databrokers.trades.BuiltInTradeLineups;
import com.jsorrell.databrokers.trades.TradeContext;
import com.jsorrell.databrokers.trades.TradeLineup;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(net.minecraft.world.entity.npc.WanderingTrader.class)
public abstract class WanderingTraderMixin extends AbstractVillager {
    public WanderingTraderMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void updateTrades() {
        TradeLineup tradeLineup = level().getServer()
                .reloadableRegistries()
                .get()
                .lookup(DataBrokersRegistries.TRADE_LINEUP)
                .flatMap(registry -> registry.get(BuiltInTradeLineups.WANDERING_TRADER))
                .map(Holder::value)
                .orElse(TradeLineup.EMPTY);
        tradeLineup.generateOffers(getOffers()::add, new TradeContext(this, random));
    }
}
