package com.lumii.armory.util;

import net.minecraft.entity.LivingEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChainEntityUtils {

    private static final Set<UUID> CHAINED = new HashSet<>();

    public static void setChained(LivingEntity entity, boolean chained) {
        if (chained) {
            CHAINED.add(entity.getUuid());
        } else {
            CHAINED.remove(entity.getUuid());
        }
    }

    public static boolean isChained(LivingEntity entity) {
        return CHAINED.contains(entity.getUuid());
    }
}
