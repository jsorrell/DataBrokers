package com.jsorrell.databrokers.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.util.ExtraCodecs;

public abstract class DataBrokersCodecs {
    public static <T> Codec<List<T>> nonEmptyInlineList(Codec<T> codec) {
        return ExtraCodecs.nonEmptyList(Codec.either(codec, codec.listOf())
                .xmap(
                        either -> either.map(List::of, Function.identity()),
                        list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list)));
    }

    // Must encode to string
    public static <T> Codec<Pair<Set<T>, Set<T>>> whitelistBlacklistPair(Codec<T> codec) {
        return Codec.STRING
                .listOf()
                .comapFlatMap(
                        components -> {
                            ImmutableSet.Builder<T> whitelistBuilder = ImmutableSet.builder();
                            ImmutableSet.Builder<T> blacklistBuilder = ImmutableSet.builder();
                            for (String component : components) {
                                boolean blacklist = false;
                                if (component.startsWith("!")) {
                                    blacklist = true;
                                    component = component.substring(1);
                                }
                                DataResult<Pair<T, JsonElement>> result =
                                        codec.decode(JsonOps.INSTANCE, new JsonPrimitive(component));
                                Optional<DataResult.Error<Pair<T, JsonElement>>> error = result.error();
                                if (error.isPresent()) {
                                    return DataResult.error(() -> error.get().message());
                                }
                                (blacklist ? blacklistBuilder : whitelistBuilder)
                                        .add(result.getOrThrow().getFirst());
                            }
                            Set<T> whitelist = whitelistBuilder.build();
                            Set<T> blacklist = blacklistBuilder.build();
                            Sets.SetView<T> intersection = Sets.intersection(whitelist, blacklist);
                            if (!intersection.isEmpty()) {
                                return DataResult.error(() -> "both included and excluded: "
                                        + intersection.stream().findAny().orElseThrow());
                            }
                            return DataResult.success(Pair.of(whitelist, blacklist));
                        },
                        pair -> {
                            ImmutableList.Builder<String> builder = ImmutableList.builder();
                            Function<T, String> encoder = t -> {
                                DataResult<JsonElement> result = codec.encodeStart(JsonOps.INSTANCE, t);
                                JsonElement element = result.getOrThrow();
                                if (element.isJsonPrimitive() || !((JsonPrimitive) element).isString()) {
                                    throw new AssertionError("must encode to string");
                                }
                                return element.getAsString();
                            };
                            pair.getFirst().stream()
                                    .map(encoder)
                                    .sorted()
                                    .toList()
                                    .forEach(builder::add);
                            pair.getSecond().stream()
                                    .map(encoder)
                                    .sorted()
                                    .map(s -> "!" + s)
                                    .sorted()
                                    .toList()
                                    .forEach(builder::add);
                            return builder.build();
                        });
    }
}
