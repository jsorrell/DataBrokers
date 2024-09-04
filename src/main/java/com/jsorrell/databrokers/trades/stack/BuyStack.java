package com.jsorrell.databrokers.trades.stack;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.jsorrell.databrokers.trades.TradeContext;
import com.jsorrell.databrokers.util.DataBrokersCodecs;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import net.minecraft.Util;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.Nullable;

public class BuyStack {
    private final HolderSet<Item> items;
    private final List<LootItemFunction> functions;
    private final List<ItemPredicate> resultPredicates;
    private final Set<DataComponentType<?>> forcedComponents;
    private final Set<DataComponentType<?>> ignoredComponents;
    private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;

    public static final Codec<BuyStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    RegistryCodecs.homogeneousList(Registries.ITEM)
                            .fieldOf("items")
                            .forGetter(s -> s.items),
                    DataBrokersCodecs.whitelistBlacklistPair(DataComponentType.CODEC)
                            .optionalFieldOf("checked_components", Pair.of(Set.of(), Set.of()))
                            .forGetter(s -> Pair.of(s.forcedComponents, s.ignoredComponents)),
                    LootItemFunctions.ROOT_CODEC
                            .listOf()
                            .optionalFieldOf("functions", List.of())
                            .forGetter(s -> s.functions),
                    ItemPredicate.CODEC
                            .listOf()
                            .optionalFieldOf("result_predicates", List.of())
                            .forGetter(s -> s.resultPredicates))
            .apply(instance, BuyStack::new));

    private BuyStack(
            HolderSet<Item> items,
            Pair<Set<DataComponentType<?>>, Set<DataComponentType<?>>> components,
            List<LootItemFunction> functions,
            List<ItemPredicate> resultPredicates) {
        this(items, components.getFirst(), components.getSecond(), functions, resultPredicates);
    }

    private BuyStack(
            HolderSet<Item> items,
            Set<DataComponentType<?>> forcedComponents,
            Set<DataComponentType<?>> ignoredComponents,
            List<LootItemFunction> functions,
            List<ItemPredicate> resultPredicates) {
        this.items = items;
        this.forcedComponents = forcedComponents;
        this.ignoredComponents = ignoredComponents;
        this.functions = functions;
        this.resultPredicates = resultPredicates;
        compositeFunction = LootItemFunctions.compose(functions);
    }

    public void validate(ValidationContext validationContext) {
        for (int i = 0; i < functions.size(); i++) {
            functions.get(i).validate(validationContext.forChild(".functions[" + i + "]"));
        }
    }

    private static <T> void addTdc(DataComponentPredicate.Builder builder, TypedDataComponent<T> tdc) {
        builder.expect(tdc.type(), tdc.value());
    }

    @Nullable
    public ItemCost getItemCost(TradeContext context) {
        Holder<Item> item = items.getRandomElement(context.random()).orElse(null);
        if (item == null) {
            return null;
        }

        ItemStack stack = new ItemStack(item);
        stack = compositeFunction.apply(stack, context.lootContext());
        if (!Util.allOf(resultPredicates).test(stack)) {
            return null;
        }

        DataComponentPredicate.Builder predicateBuilder = DataComponentPredicate.builder();
        stack.getComponents().stream()
                .filter(tdc -> {
                    DataComponentMap baseComponents = new ItemStack(item).getComponents();
                    return !ignoredComponents.contains(tdc.type())
                            && (forcedComponents.contains(tdc.type()) || baseComponents.get(tdc.type()) != tdc.value());
                })
                .forEach(tdc -> addTdc(predicateBuilder, tdc));
        return new ItemCost(item, stack.getCount(), predicateBuilder.build());
    }

    public static Builder buyItems(ItemLike item, ItemLike... items) {
        return new Builder(item, items);
    }

    public static Builder buyItems(int count, ItemLike item, ItemLike... items) {
        if (count == 1) {
            return buyItems(item, items);
        }
        return new Builder(item, items)
                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(count))
                        .build());
    }

    public static Builder emeralds(int count) {
        return buyItems(count, Items.EMERALD);
    }

    public static Builder buyTag(TagKey<Item> itemTag) {
        return new Builder(itemTag);
    }

    public static Builder builder(HolderSet<Item> holderSet) {
        return new Builder(holderSet);
    }

    public static Builder fromItemCostNoComponents(ItemCost cost) {
        return buyItems(cost.count(), cost.item().value());
    }

    public static class Builder {
        private final HolderSet<Item> items;
        private final ImmutableList.Builder<LootItemFunction> functions = ImmutableList.builder();
        private final ImmutableList.Builder<ItemPredicate> resultPredicates = ImmutableList.builder();
        private final ImmutableSet.Builder<DataComponentType<?>> forcedComponents = ImmutableSet.builder();
        private final ImmutableSet.Builder<DataComponentType<?>> ignoredComponents = ImmutableSet.builder();

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

        public Builder apply(LootItemFunction function) {
            functions.add(function);
            return this;
        }

        public Builder validate(ItemPredicate resultPredicate) {
            resultPredicates.add(resultPredicate);
            return this;
        }

        public Builder checkComponent(DataComponentType<?> component) {
            forcedComponents.add(component);
            return this;
        }

        public Builder ignoreComponent(DataComponentType<?> component) {
            ignoredComponents.add(component);
            return this;
        }

        public BuyStack build() {
            return new BuyStack(
                    items,
                    forcedComponents.build(),
                    ignoredComponents.build(),
                    functions.build(),
                    resultPredicates.build());
        }
    }
}
