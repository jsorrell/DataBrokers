package com.jsorrell.databrokers.trades.offermodifiers;

import com.jsorrell.databrokers.registries.DataBrokersBuiltInRegistries;
import com.jsorrell.databrokers.trades.TradeContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.ValidationContext;
import org.jetbrains.annotations.Nullable;

public interface OfferModifier {
    Codec<OfferModifier> CODEC =
            DataBrokersBuiltInRegistries.OFFER_MODIFIER_TYPE.byNameCodec().dispatch(OfferModifier::type, Type::codec);

    @Nullable
    MerchantOffer apply(MerchantOffer offer, TradeContext context);

    record Type<T extends OfferModifier>(MapCodec<T> codec) {
        public static final Registry<Type<?>> REGISTRY = DataBrokersBuiltInRegistries.OFFER_MODIFIER_TYPE;
    }

    default void validate(ValidationContext validationContext) {}

    Type<?> type();
}
