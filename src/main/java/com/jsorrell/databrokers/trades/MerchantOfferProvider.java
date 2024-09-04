package com.jsorrell.databrokers.trades;

import java.util.function.Consumer;
import net.minecraft.world.item.trading.MerchantOffer;

@FunctionalInterface
public interface MerchantOfferProvider {
    void generateOffers(Consumer<MerchantOffer> output, TradeContext context);
}
