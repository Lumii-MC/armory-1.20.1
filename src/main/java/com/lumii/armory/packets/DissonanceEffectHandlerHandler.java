package com.lumii.armory.packets;

import com.lumii.armory.vfx.DivinityDissonanceHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

import java.awt.*;

@Environment(EnvType.CLIENT)
public class DissonanceEffectHandlerHandler {
    public static void handle(MinecraftClient client, DissonanceEffectPacket packet){
        client.execute(() -> {
            World world = client.world;
            LivingEntity attacked = packet.attacked;
            if (world != null) {
                DivinityDissonanceHandler.effectClient(attacked);
            }
        });
    }
}
