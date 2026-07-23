package com.lumii.armory.packets;

import com.lumii.armory.util.TimeUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
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

public class ParticleSpawnPacket {
    private final Vec3d pos;
    private int startColor;
    private int endColor;

    public ParticleSpawnPacket(Vec3d pos, int startColor, int endColor){
        this.pos = pos;
        this.startColor = startColor;
        this.endColor = endColor;
    }

    public ParticleSpawnPacket(PacketByteBuf buf ){
        this.pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.startColor = buf.readInt();
        this.endColor = buf.readInt();
    }

    public void toBytes(PacketByteBuf buf){
        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);
        buf.writeInt(startColor);
        buf.writeInt(endColor);
    }

    public void handle(MinecraftClient client){
        client.execute(() -> {
            World world = client.world;
            if (world != null){
                Color startColor = new Color(this.startColor);
                Color endColor = new Color(this.endColor);
                spawnParticles(world, pos, startColor, endColor);
            }
        });
    }

    public static void spawnParticles(World world, Vec3d pos, Color startColor, Color endColor){
        WorldParticleBuilder.create(LodestoneParticleRegistry.SMOKE_PARTICLE)
                .setScaleData(GenericParticleData.create(2f).setCoefficient(1f).build())
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
                .setSpinData(SpinParticleData.createRandomDirection(Random.create(), 0.1f).build())
                .addMotion(new Vec3d(0, 0.04, 0).addRandom(Random.create(), 1))
                .spawn(world, pos.x, pos.y, pos.z);
    }
}
