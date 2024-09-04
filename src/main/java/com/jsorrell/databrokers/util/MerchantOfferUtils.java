package com.jsorrell.databrokers.util;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

public abstract class MerchantOfferUtils {
    public static class MutableMerchantOffer {
        public ItemCost itemCostA;
        public Optional<ItemCost> itemCostB;
        public ItemStack result;
        public int uses;
        public int maxUses;
        public boolean rewardExp;
        public int specialPriceDiff;
        public int demand;
        public float priceMultiplier;
        public int xp;

        public MutableMerchantOffer(MerchantOffer offer) {
            itemCostA = offer.getItemCostA();
            itemCostB = offer.getItemCostB();
            result = offer.getResult();
            uses = offer.getUses();
            maxUses = offer.getMaxUses();
            rewardExp = offer.shouldRewardExp();
            specialPriceDiff = offer.getSpecialPriceDiff();
            demand = offer.getDemand();
            priceMultiplier = offer.getPriceMultiplier();
            xp = offer.getXp();
        }

        public MerchantOffer offer() {
            return new MerchantOffer(
                    itemCostA,
                    itemCostB,
                    result,
                    uses,
                    maxUses,
                    rewardExp,
                    specialPriceDiff,
                    demand,
                    priceMultiplier,
                    xp);
        }
    }

    public static MerchantOfferEditor editor() {
        return new MerchantOfferEditor();
    }

    public static class MerchantOfferEditor implements UnaryOperator<MerchantOffer> {
        private final ArrayList<Consumer<MutableMerchantOffer>> edits = new ArrayList<>();

        public MerchantOfferEditor replaceItemCost(UnaryOperator<ItemCost> operator) {
            edits.add(offer -> offer.itemCostA = operator.apply(offer.itemCostA));
            return this;
        }

        public MerchantOfferEditor replaceItemCostB(UnaryOperator<Optional<ItemCost>> operator) {
            edits.add(offer -> offer.itemCostB = operator.apply(offer.itemCostB));
            return this;
        }

        public MerchantOfferEditor replaceItemCostCount(UnaryOperator<Integer> operator) {
            edits.add(offer -> offer.itemCostA = new ItemCost(
                    offer.itemCostA.item(), operator.apply(offer.itemCostA.count()), offer.itemCostA.components()));
            return this;
        }

        public MerchantOfferEditor replaceResult(UnaryOperator<ItemStack> operator) {
            edits.add(offer -> offer.result = operator.apply(offer.result));
            return this;
        }

        @Override
        public MerchantOffer apply(MerchantOffer merchantOffer) {
            MutableMerchantOffer mutableOffer = new MutableMerchantOffer(merchantOffer);
            for (Consumer<MutableMerchantOffer> edit : edits) {
                edit.accept(mutableOffer);
            }
            return mutableOffer.offer();
        }
    }
}
