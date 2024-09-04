package com.jsorrell.databrokers.mixin;

import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

public class VillagerTradesMixin {
    @Mixin(VillagerTrades.DyedArmorForEmeralds.class)
    public interface DyedArmorForEmeraldsAccessor {
        @Accessor
        Item getItem();

        @Accessor
        int getValue();

        @Accessor
        int getMaxUses();

        @Accessor
        int getVillagerXp();
    }

    @Mixin(VillagerTrades.EmeraldForItems.class)
    public interface EmeraldForItemsAccessor {
        @Accessor("itemStack")
        ItemCost getCost();

        @Accessor
        int getEmeraldAmount();

        @Accessor
        int getMaxUses();

        @Accessor
        int getVillagerXp();

        @Accessor
        float getPriceMultiplier();
    }

    @Mixin(VillagerTrades.EmeraldsForVillagerTypeItem.class)
    public interface EmeraldsForVillagerTypeItemAccessor {
        @Accessor
        Map<VillagerType, Item> getTrades();

        @Accessor
        int getCost();

        @Accessor
        int getMaxUses();

        @Accessor
        int getVillagerXp();
    }

    @Mixin(VillagerTrades.EnchantBookForEmeralds.class)
    public interface EnchantBookForEmeraldsAccessor {
        @Accessor
        int getVillagerXp();

        @Accessor
        TagKey<Enchantment> getTradeableEnchantments();

        @Accessor
        int getMinLevel();

        @Accessor
        int getMaxLevel();
    }

    @Mixin(VillagerTrades.EnchantedItemForEmeralds.class)
    public interface EnchantedItemForEmeraldsAccessor {
        @Accessor
        ItemStack getItemStack();

        @Accessor
        int getBaseEmeraldCost();

        @Accessor
        int getMaxUses();

        @Accessor
        int getVillagerXp();

        @Accessor
        float getPriceMultiplier();
    }

    @Mixin(VillagerTrades.ItemsAndEmeraldsToItems.class)
    public interface ItemsAndEmeraldsToItemsAccessor {
        @Accessor
        ItemCost getFromItem();

        @Accessor
        int getEmeraldCost();

        @Accessor
        ItemStack getToItem();

        @Accessor
        int getMaxUses();

        @Accessor
        int getVillagerXp();

        @Accessor
        float getPriceMultiplier();

        @Accessor
        Optional<ResourceKey<EnchantmentProvider>> getEnchantmentProvider();
    }

    @Mixin(VillagerTrades.ItemsForEmeralds.class)
    public interface ItemsForEmeraldsAccessor {
        @Accessor
        ItemStack getItemStack();

        @Accessor
        int getEmeraldCost();

        @Accessor
        int getMaxUses();

        @Accessor
        int getVillagerXp();

        @Accessor
        float getPriceMultiplier();

        @Accessor
        Optional<ResourceKey<EnchantmentProvider>> getEnchantmentProvider();
    }

    @Mixin(VillagerTrades.SuspiciousStewForEmerald.class)
    public interface SuspiciousStewForEmeraldAccessor {
        @Accessor
        SuspiciousStewEffects getEffects();

        @Accessor
        int getXp();

        @Accessor
        float getPriceMultiplier();
    }

    @Mixin(VillagerTrades.TippedArrowForItemsAndEmeralds.class)
    public interface TippedArrowForItemsAndEmeraldsAccessor {
        @Accessor
        ItemStack getToItem();

        @Accessor
        int getToCount();

        @Accessor
        int getEmeraldCost();

        @Accessor
        int getMaxUses();

        @Accessor
        int getVillagerXp();

        @Accessor
        Item getFromItem();

        @Accessor
        int getFromCount();

        @Accessor
        float getPriceMultiplier();
    }

    @Mixin(VillagerTrades.TreasureMapForEmeralds.class)
    public interface TreasureMapForEmeraldsAccessor {
        @Accessor
        int getEmeraldCost();

        @Accessor
        TagKey<Structure> getDestination();

        @Accessor
        String getDisplayName();

        @Accessor
        Holder<MapDecorationType> getDestinationType();

        @Accessor
        int getMaxUses();

        @Accessor
        int getVillagerXp();
    }
}
