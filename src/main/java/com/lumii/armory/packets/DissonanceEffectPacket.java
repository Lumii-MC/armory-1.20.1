package com.lumii.armory.packets;

import com.lumii.armory.vfx.DivinityDissonanceHandler;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.UUID;

public class DissonanceEffectPacket {
    public final LivingEntity attacked;

    public DissonanceEffectPacket(LivingEntity attacked) {
        this.attacked = attacked;
    }

    public DissonanceEffectPacket(PacketByteBuf buf) {
        this.attacked = getLivingEntityByUuid(MinecraftClient.getInstance().world, buf.readUuid());
    }

    public static PacketByteBuf toBytes(LivingEntity attacked) {
        return PacketByteBufs.create().writeUuid(attacked.getUuid());
    }

    public @Nullable LivingEntity getLivingEntityByUuid(ClientWorld world, UUID uuid) {
        for (Entity entity : world.getEntities()) {
            if (entity.getUuid().equals(uuid) && entity instanceof LivingEntity living) {
                return living;
            }
        }
        return null;
    }
}
