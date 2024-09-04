package com.jsorrell.databrokers.trades.stack;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.jsorrell.databrokers.trades.TradeContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.Util;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.Nullable;

public class SellStack {
    private final HolderSet<Item> items;
    private final List<LootItemFunction> functions;
    private final List<ItemPredicate> resultPredicates;
    private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;

    public static final Codec<SellStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    RegistryCodecs.homogeneousList(Registries.ITEM)
                            .fieldOf("items")
                            .forGetter(s -> s.items),
                    LootItemFunctions.ROOT_CODEC
                            .listOf()
                            .optionalFieldOf("functions", List.of())
                            .forGetter(s -> s.functions),
                    ItemPredicate.CODEC
                            .listOf()
                            .optionalFieldOf("result_predicates", List.of())
                            .forGetter(s -> s.resultPredicates))
            .apply(instance, SellStack::new));

    protected SellStack(HolderSet<Item> items, List<LootItemFunction> functions, List<ItemPredicate> resultPredicates) {
        this.items = items;
        this.functions = functions;
        this.resultPredicates = resultPredicates;
        compositeFunction = LootItemFunctions.compose(functions);
    }

    public void validate(ValidationContext validationContext) {
        for (int i = 0; i < functions.size(); i++) {
            functions.get(i).validate(validationContext.forChild(".functions[" + i + "]"));
        }
    }

    @Nullable
    public ItemStack getItemStack(TradeContext context) {
        Holder<Item> item = items.getRandomElement(context.random()).orElse(null);
        if (item == null) {
            return null;
        }

        ItemStack stack = new ItemStack(item);
        stack = compositeFunction.apply(stack, context.lootContext());
        if (Util.allOf(resultPredicates).test(stack)) {
            return stack;
        }
        return null;
    }

    public static Builder sellItems(ItemLike item, ItemLike... items) {
        return new Builder(item, items);
    }

    public static Builder sellItems(int count, ItemLike item, ItemLike... items) {
        if (count == 1) {
            return sellItems(item, items);
        }
        return new Builder(item, items)
                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(count))
                        .build());
    }

    public static Builder emeralds(int count) {
        return sellItems(count, Items.EMERALD);
    }

    public static Builder sellTag(TagKey<Item> itemTag) {
        return new Builder(itemTag);
    }

    public static Builder builder(HolderSet<Item> holderSet) {
        return new Builder(holderSet);
    }

    public static Builder fromItemStackNoComponents(ItemStack stack) {
        return sellItems(stack.getCount(), stack.getItem());
    }

    public static class Builder {
        private final HolderSet<Item> items;
        private final ImmutableList.Builder<LootItemFunction> functions = ImmutableList.builder();
        private final ImmutableList.Builder<ItemPredicate> resultPredicates = ImmutableList.builder();

        public Builder(ItemLike item, ItemLike... items) {
            this.items = HolderSet.direct(Lists.asList(item, items).stream()
                    .map(ItemLike::asItem)
                    .map(BuiltInRegistries.ITEM::wrapAsHolder)
                    .toList());
        }

        public Builder(TagKey<Item> itemTag) {
            items = BuiltInRegistries.ITEM.getTag(itemTag).orElseThrow();
        }

        public Builder(HolderSet<Item> holderSet) {
            items = holderSet;
        }

        public Builder apply(LootItemFunction.Builder functionBuilder) {
            return apply(functionBuilder.build());
        }

        public Builder apply(LootItemFunction function) {
            functions.add(function);
            return this;
        }

        public Builder validate(ItemPredicate.Builder resultPredicate) {
            return validate(resultPredicate.build());
        }

        public Builder validate(ItemPredicate resultPredicate) {
            resultPredicates.add(resultPredicate);
            return this;
        }

        public SellStack build() {
            return new SellStack(items, functions.build(), resultPredicates.build());
        }
    }
}
