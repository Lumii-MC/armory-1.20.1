package com.lumii.armory.util;

import com.lumii.armory.registry.ArmoryPackets;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

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
        if (entity instanceof PlayerEntity) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(chained);
            ServerPlayNetworking.send((ServerPlayerEntity) entity, ArmoryPackets.CHAIN_STATUS_ID, buf);
        }
    }

    public static boolean isChained(LivingEntity entity) {
        return CHAINED.contains(entity.getUuid());
    }
}
