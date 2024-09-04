package com.jsorrell.databrokers.lootfunctions;

import com.jsorrell.databrokers.util.DataBrokersResourceLocation;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public class DataBrokersLootItemFunctions {
    public static final LootItemFunctionType<EnchantWithProviderFunction> ENCHANT_WITH_PROVIDER =
            register("enchant_with_provider", EnchantWithProviderFunction.CODEC);
    public static final LootItemFunctionType<DyeRandomlyFunction> DYE_RANDOMLY =
            register("dye_randomly", DyeRandomlyFunction.CODEC);
    public static final LootItemFunctionType<SetRandomPotionFunction> SET_RANDOM_POTION =
            register("set_random_potion", SetRandomPotionFunction.CODEC);

    private static <T extends LootItemFunction> LootItemFunctionType<T> register(String name, MapCodec<T> codec) {
        return Registry.register(
                BuiltInRegistries.LOOT_FUNCTION_TYPE,
                DataBrokersResourceLocation.fromPath(name),
                new LootItemFunctionType<>(codec));
    }

    public static void registerLootFunctions() {}
}
