package com.jsorrell.databrokers.data;

import com.jsorrell.databrokers.DataBrokers;
import com.jsorrell.databrokers.lootfunctions.DyeRandomlyFunction;
import com.jsorrell.databrokers.lootfunctions.EnchantWithProviderFunction;
import com.jsorrell.databrokers.lootfunctions.SetRandomPotionFunction;
import com.jsorrell.databrokers.mixin.VillagerTradesMixin;
import com.jsorrell.databrokers.trades.BuiltInTradeLineups;
import com.jsorrell.databrokers.trades.TradeLineup;
import com.jsorrell.databrokers.trades.TradePool;
import com.jsorrell.databrokers.trades.listings.AlternativesListing;
import com.jsorrell.databrokers.trades.listings.EmptyListing;
import com.jsorrell.databrokers.trades.listings.Listing;
import com.jsorrell.databrokers.trades.listings.SimpleListing;
import com.jsorrell.databrokers.trades.offermodifiers.VanillaEnchantBookModifier;
import com.jsorrell.databrokers.trades.offermodifiers.VanillaEnchantItemModifier;
import com.jsorrell.databrokers.trades.stack.BuyStack;
import com.jsorrell.databrokers.trades.stack.SellStack;
import com.jsorrell.databrokers.util.VillagerTierHolder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntitySubPredicates;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import net.minecraft.world.level.storage.loot.functions.SetNameFunction;
import net.minecraft.world.level.storage.loot.functions.SetStewEffectFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.apache.commons.lang3.tuple.Pair;

public class VanillaTradesConverter {
    static void consumeVillagerTrades(
            BiConsumer<ResourceKey<TradeLineup>, TradeLineup.Builder> output,
            Map<VillagerProfession, Int2ObjectMap<VillagerTrades.ItemListing[]>> vanillaVillagerTrades,
            HolderLookup.Provider registryProvider) {
        vanillaVillagerTrades.forEach((profession, vanillaTrades) -> {
            VillagerTierHolder<ResourceKey<TradeLineup>> keys = BuiltInTradeLineups.villagerLineups(profession);
            if (keys == null) {
                DataBrokers.LOGGER.warn("Missing TradeLineup key for profession: {}", profession);
                return;
            }
            for (int i = 1; i <= VillagerData.MAX_VILLAGER_LEVEL; i++) {
                VillagerTrades.ItemListing[] tierTrades = vanillaTrades.get(i);
                Optional<ResourceKey<TradeLineup>> key = keys.getTier(i);
                if (key.isEmpty()) {
                    DataBrokers.LOGGER.warn("Missing TradeLineup key for tier {} of profession {}", i, profession);
                    continue;
                }
                output.accept(key.get(), VanillaTradesConverter.convertVillagerTradeTier(tierTrades, registryProvider));
            }
        });
    }

    public static VillagerTierHolder.Builder<TradeLineup> convertVillagerTrades(
            Int2ObjectMap<VillagerTrades.ItemListing[]> trades, HolderLookup.Provider registryProvider) {
        VillagerTierHolder.Builder<TradeLineup> builder = VillagerTierHolder.builder();
        trades.int2ObjectEntrySet()
                .forEach(e -> builder.withTier(
                        e.getIntKey(),
                        convertVillagerTradeTier(e.getValue(), registryProvider).build()));
        return builder;
    }

    public static TradeLineup.Builder convertVillagerTradeTier(
            VillagerTrades.ItemListing[] trades, HolderLookup.Provider registryProvider) {
        return TradeLineup.villager(convertItemListings(trades, registryProvider));
    }

    public static TradeLineup.Builder convertWanderingTraderTrades(
            Int2ObjectMap<VillagerTrades.ItemListing[]> trades, HolderLookup.Provider registryProvider) {
        return TradeLineup.wanderingTrader(
                convertItemListings(trades.get(1), registryProvider),
                convertItemListings(trades.get(2), registryProvider));
    }

    public static TradeLineup.Builder convertExperimentalWanderingTraderTrades(
            List<Pair<VillagerTrades.ItemListing[], Integer>> trades, HolderLookup.Provider registryProvider) {
        TradeLineup.Builder builder = TradeLineup.lineup();
        trades.forEach(p -> builder.withPool(
                convertItemListings(p.getLeft(), registryProvider).setRolls(ConstantValue.exactly(p.getRight()))));
        return builder;
    }

    public static TradePool.Builder convertItemListings(
            VillagerTrades.ItemListing[] listings, HolderLookup.Provider registryProvider) {
        TradePool.Builder builder = TradePool.tradePool();
        Arrays.stream(listings).forEach(l -> builder.add(convertItemListing(l, registryProvider)));
        return builder;
    }

    public static Listing.Builder<?> convertItemListing(
            VillagerTrades.ItemListing listing, HolderLookup.Provider registryProvider) {
        /* Emerald -> Dyed Armor */
        if (listing instanceof VillagerTrades.DyedArmorForEmeralds) {
            VillagerTradesMixin.DyedArmorForEmeraldsAccessor aListing =
                    ((VillagerTradesMixin.DyedArmorForEmeraldsAccessor) listing);

            return SimpleListing.builder(
                            BuyStack.emeralds(aListing.getValue()),
                            SellStack.sellItems(aListing.getItem())
                                    .apply(DyeRandomlyFunction.vanillaDyedArmorWeights()))
                    .uses(aListing.getMaxUses())
                    .xp(aListing.getVillagerXp())
                    .highMultiplier();
        }

        /* Item -> Emerald */
        if (listing instanceof VillagerTrades.EmeraldForItems) {
            VillagerTradesMixin.EmeraldForItemsAccessor aListing =
                    ((VillagerTradesMixin.EmeraldForItemsAccessor) listing);

            return SimpleListing.builder(
                            BuyStack.fromItemCostNoComponents(aListing.getCost()),
                            SellStack.emeralds(aListing.getEmeraldAmount()))
                    .uses(aListing.getMaxUses())
                    .xp(aListing.getVillagerXp())
                    .multiplier(aListing.getPriceMultiplier());
        }

        /* Villager Type Item -> Emerald */
        if (listing instanceof VillagerTrades.EmeraldsForVillagerTypeItem) {
            VillagerTradesMixin.EmeraldsForVillagerTypeItemAccessor aListing =
                    ((VillagerTradesMixin.EmeraldsForVillagerTypeItemAccessor) listing);

            return AlternativesListing.alternatives(aListing.getTrades().entrySet(), e -> SimpleListing.builder(
                            BuyStack.buyItems(aListing.getCost(), e.getValue()), SellStack.emeralds(1))
                    .when(LootItemEntityPropertyCondition.hasProperties(
                            LootContext.EntityTarget.THIS,
                            EntityPredicate.Builder.entity()
                                    .subPredicate(EntitySubPredicates.VILLAGER.createPredicate(e.getKey()))))
                    .uses(aListing.getMaxUses())
                    .xp(aListing.getVillagerXp()));
        }

        /* Emerald + Book -> Enchanted Book */
        if (listing instanceof VillagerTrades.EnchantBookForEmeralds) {
            VillagerTradesMixin.EnchantBookForEmeraldsAccessor aListing =
                    ((VillagerTradesMixin.EnchantBookForEmeraldsAccessor) listing);
            return SimpleListing.builder(
                            BuyStack.emeralds(1),
                            BuyStack.buyItems(Items.BOOK),
                            SellStack.sellItems(Items.ENCHANTED_BOOK))
                    .apply(new VanillaEnchantBookModifier(
                            registryProvider
                                    .lookupOrThrow(Registries.ENCHANTMENT)
                                    .get(aListing.getTradeableEnchantments())
                                    .orElseThrow(),
                            aListing.getMinLevel(),
                            aListing.getMaxLevel()))
                    .uses(12)
                    .xp(aListing.getVillagerXp())
                    .highMultiplier();
        }

        /* Emerald -> Enchanted Item */
        if (listing instanceof VillagerTrades.EnchantedItemForEmeralds) {
            VillagerTradesMixin.EnchantedItemForEmeraldsAccessor aListing =
                    ((VillagerTradesMixin.EnchantedItemForEmeraldsAccessor) listing);

            return SimpleListing.builder(
                            BuyStack.emeralds(aListing.getBaseEmeraldCost()),
                            SellStack.fromItemStackNoComponents(aListing.getItemStack()))
                    .apply(new VanillaEnchantItemModifier())
                    .uses(aListing.getMaxUses())
                    .xp(aListing.getVillagerXp())
                    .multiplier(aListing.getPriceMultiplier());
        }

        /* Emerald + Item -> Item */
        if (listing instanceof VillagerTrades.ItemsAndEmeraldsToItems) {
            VillagerTradesMixin.ItemsAndEmeraldsToItemsAccessor aListing =
                    ((VillagerTradesMixin.ItemsAndEmeraldsToItemsAccessor) listing);

            SellStack.Builder sellBuilder = SellStack.fromItemStackNoComponents(aListing.getToItem());
            if (aListing.getEnchantmentProvider().isPresent()) {
                EnchantmentProvider provider = registryProvider
                        .lookupOrThrow(Registries.ENCHANTMENT_PROVIDER)
                        .getOrThrow(aListing.getEnchantmentProvider().get())
                        .value();
                sellBuilder.apply(EnchantWithProviderFunction.enchantWithProvider(provider));
            }

            return SimpleListing.builder(
                            BuyStack.emeralds(aListing.getEmeraldCost()),
                            BuyStack.fromItemCostNoComponents(aListing.getFromItem()),
                            sellBuilder)
                    .uses(aListing.getMaxUses())
                    .xp(aListing.getVillagerXp())
                    .multiplier(aListing.getPriceMultiplier());
        }

        /* Emerald -> Item */
        if (listing instanceof VillagerTrades.ItemsForEmeralds) {
            VillagerTradesMixin.ItemsForEmeraldsAccessor aListing =
                    ((VillagerTradesMixin.ItemsForEmeraldsAccessor) listing);

            SellStack.Builder sellBuilder = SellStack.fromItemStackNoComponents(aListing.getItemStack());
            if (aListing.getEnchantmentProvider().isPresent()) {
                EnchantmentProvider provider = registryProvider
                        .lookupOrThrow(Registries.ENCHANTMENT_PROVIDER)
                        .getOrThrow(aListing.getEnchantmentProvider().get())
                        .value();
                sellBuilder.apply(EnchantWithProviderFunction.enchantWithProvider(provider));
            }

            return SimpleListing.builder(BuyStack.emeralds(aListing.getEmeraldCost()), sellBuilder)
                    .uses(aListing.getMaxUses())
                    .xp(aListing.getVillagerXp())
                    .multiplier(aListing.getPriceMultiplier());
        }

        /* Emerald -> Suspicious Stew */
        if (listing instanceof VillagerTrades.SuspiciousStewForEmerald) {
            VillagerTradesMixin.SuspiciousStewForEmeraldAccessor aListing =
                    ((VillagerTradesMixin.SuspiciousStewForEmeraldAccessor) listing);

            SellStack.Builder sellsBuilder = SellStack.sellItems(Items.SUSPICIOUS_STEW);
            if (!aListing.getEffects().effects().isEmpty()) {
                SetStewEffectFunction.Builder stewEffectBuilder = SetStewEffectFunction.stewEffect();
                for (SuspiciousStewEffects.Entry effect : aListing.getEffects().effects()) {
                    double durationInSeconds = ((double) effect.duration()) / 20.0;
                    stewEffectBuilder.withEffect(effect.effect(), ConstantValue.exactly((float) durationInSeconds));
                }
                sellsBuilder.apply(stewEffectBuilder);
            }

            return SimpleListing.builder(BuyStack.emeralds(1), sellsBuilder)
                    .uses(12)
                    .xp(aListing.getXp())
                    .multiplier(aListing.getPriceMultiplier());
        }

        /* Emerald -> Tipped Arrow */
        if (listing instanceof VillagerTrades.TippedArrowForItemsAndEmeralds) {
            VillagerTradesMixin.TippedArrowForItemsAndEmeraldsAccessor aListing =
                    ((VillagerTradesMixin.TippedArrowForItemsAndEmeraldsAccessor) listing);

            return SimpleListing.builder(
                            BuyStack.emeralds(aListing.getEmeraldCost()),
                            BuyStack.buyItems(aListing.getFromCount(), aListing.getFromItem()),
                            SellStack.sellItems(
                                            aListing.getToCount(),
                                            aListing.getToItem().getItem())
                                    .apply(SetRandomPotionFunction.randomBrewablePotion()))
                    .uses(aListing.getMaxUses())
                    .xp(aListing.getVillagerXp())
                    .multiplier(aListing.getPriceMultiplier());
        }

        /* Emerald + Compass -> Treasure Map */
        if (listing instanceof VillagerTrades.TreasureMapForEmeralds) {
            VillagerTradesMixin.TreasureMapForEmeraldsAccessor aListing =
                    ((VillagerTradesMixin.TreasureMapForEmeraldsAccessor) listing);

            return SimpleListing.builder(
                            BuyStack.emeralds(aListing.getEmeraldCost()),
                            BuyStack.buyItems(Items.COMPASS),
                            SellStack.sellItems(Items.MAP)
                                    .apply(ExplorationMapFunction.makeExplorationMap()
                                            .setDestination(aListing.getDestination())
                                            .setMapDecoration(aListing.getDestinationType())
                                            .setSearchRadius(100))
                                    .apply(SetNameFunction.setName(
                                            Component.translatable(aListing.getDisplayName()),
                                            SetNameFunction.Target.ITEM_NAME))
                                    .validate(ItemPredicate.Builder.item().of(Items.FILLED_MAP)))
                    .uses(aListing.getMaxUses())
                    .xp(aListing.getVillagerXp())
                    .highMultiplier();
        }

        if (listing instanceof VillagerTrades.TypeSpecificTrade typeSpecificTrade) {
            return AlternativesListing.alternatives(
                    typeSpecificTrade.trades().entrySet(),
                    e -> convertItemListing(e.getValue(), registryProvider).whenVillagerType(e.getKey()));
        }

        if (listing instanceof VillagerTrades.FailureItemListing) {
            return EmptyListing.empty();
        }

        throw new AssertionError("Unknown vanilla listing: " + listing);
    }
}
