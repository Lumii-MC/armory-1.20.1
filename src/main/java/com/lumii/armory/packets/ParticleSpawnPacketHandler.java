package com.lumii.armory.packets;

import com.lumii.armory.util.time.TimeUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry;
import team.lodestar.lodestone.systems.easing.Easing;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.particle.data.spin.SpinParticleData;

import java.awt.*;

@Environment(EnvType.CLIENT)
public class ParticleSpawnPacketHandler {
    public static void handle(MinecraftClient client, ParticleSpawnPacket packet){
        client.execute(() -> {
            World world = client.world;
            if (world != null){
                spawnParticles(world, packet.pos, new Color(packet.startColor), new Color(packet.endColor));
            }
        });
    }

    public static void spawnParticles(World world, Vec3d pos, Color startColor, Color endColor){
        WorldParticleBuilder.create(LodestoneParticleRegistry.SMOKE_PARTICLE)
                .setScaleData(GenericParticleData.create(3).setCoefficient(5f).build())
                .setTransparencyData(GenericParticleData.create(5f).build())
                .setColorData(ColorParticleData.create(startColor, endColor).setCoefficient(1.5f)
                        .setEasing(Easing.LINEAR).build())
                .setLifetime(TimeUtils.seconds(5))
                .setSpinData(SpinParticleData.createRandomDirection(Random.create(), 0.1f).build())
                .addMotion(new Vec3d(0, 0.001, 0).addRandom(Random.create(), 1))
                .spawn(world, pos.x, pos.y, pos.z);

        WorldParticleBuilder.create(LodestoneParticleRegistry.TWINKLE_PARTICLE)
                .setScaleData(GenericParticleData.create(0.2f).setCoefficient(1f).build())
                .setTransparencyData(GenericParticleData.create(5f).build())
                .setColorData(ColorParticleData.create(startColor, endColor).setCoefficient(1.5f)
                        .setEasing(Easing.LINEAR).build())
                .setLifetime(TimeUtils.seconds(5))
                .enableNoClip()
                .setSpinData(SpinParticleData.createRandomDirection(Random.create(), 0.1f).build())
                .addMotion(new Vec3d(0, 0.04, 0).addRandom(Random.create(), 1))
                .spawn(world, pos.x, pos.y, pos.z);
    }
}