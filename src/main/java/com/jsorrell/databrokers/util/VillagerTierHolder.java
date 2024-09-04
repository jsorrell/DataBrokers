package com.jsorrell.databrokers.util;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.entity.npc.VillagerData;

public class VillagerTierHolder<T> {
    protected final List<T> tiers;
    public static final int MAX_TIER_NUM = VillagerData.MAX_VILLAGER_LEVEL;

    public static <U> Codec<VillagerTierHolder<U>> codec(Codec<U> tierCodec) {
        return tierCodec.listOf(0, MAX_TIER_NUM).xmap(VillagerTierHolder::new, h -> h.tiers);
    }

    protected VillagerTierHolder(List<T> tiers) {
        this.tiers = tiers;
    }

    public Optional<T> getTier(int tierNum) {
        if (tierNum < 0) {
            throw new IllegalArgumentException("negative tierNum");
        }

        if (MAX_TIER_NUM < tierNum) {
            throw new IllegalArgumentException("tierNum too high. Max tierNum is " + MAX_TIER_NUM);
        }

        return Optional.ofNullable(tiers.get(tierNum - 1));
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        @SuppressWarnings("unchecked")
        private final List<T> tiers = Arrays.asList((T[]) new Object[MAX_TIER_NUM]);

        public Builder<T> withTier(int tierNum, T lineupBuilder) {
            if (MAX_TIER_NUM < tierNum) {
                throw new IllegalArgumentException("tierNum too high. Max tierNum is " + MAX_TIER_NUM);
            }
            tiers.set(tierNum - 1, lineupBuilder);
            return this;
        }

        public VillagerTierHolder<T> build() {
            return new VillagerTierHolder<>(tiers);
        }
    }
}
