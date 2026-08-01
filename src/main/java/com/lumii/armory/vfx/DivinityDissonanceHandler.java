package com.lumii.armory.vfx;

import com.lumii.armory.Armory;
import com.lumii.armory.packets.DissonanceEffectPacket;
import com.lumii.armory.registry.ArmoryPackets;
import com.lumii.armory.util.time.TickSchedulerClient;
import com.lumii.armory.util.time.TimeUtils;
import com.lumii.armory.util.visual.QuadRenderer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;

import java.awt.*;

public class DivinityDissonanceHandler {

    public static void addEffect(LivingEntity entity) {
        for (PlayerEntity player : entity.getWorld().getPlayers()) {
            ServerPlayNetworking.send((ServerPlayerEntity) player, ArmoryPackets.DISSONANCE_VFX_ID, DissonanceEffectPacket.toBytes(entity));
        }
    }

    public static void effectClient(LivingEntity entity) {
            Vec3d pos = entity.getPos();
            World world = entity.getEntityWorld();
            Random random = entity.getRandom();
            WorldParticleBuilder builder = WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE)
                    .setFullBrightLighting()
                    .setFrictionStrength(0)
                    .setNoClip(true)
                    .setColorData(ColorParticleData.create(new Color(139, 121, 79, 255), new Color(142, 136, 115, 255)).build())
                    .setFrictionStrength(0)
                    .setLifetime(7 + random.nextBetween(0, 7));

            int delaySecs = 2;
            QuadRenderer.scheduleClient(pos.add(0, 0.0001, 0),
                    1, 1,
                    new Vec3d(90, 0, 0), 4,
                    Armory.id("textures/vfx/execution_ring.png"),
                    TimeUtils.seconds(delaySecs),
                    false, 1,
                    true, 1, 10,
                    1, QuadRenderer.SpinAxis.Z, 5f);
        TickSchedulerClient.schedule(TimeUtils.seconds(delaySecs) - 15, () -> {
            TickSchedulerClient.scheduleRepeating(TimeUtils.seconds(delaySecs)*2, a -> {
                if (a % 2 == 0) {
                    int length = 1000;

                    for (int i = 0; i < length; i++) {
                        double currentSpread = (double) (length - i) / 1000;

                        for (int j = 0; j < (length - i) / 500 ; j++) {
                            double offsetX = (random.nextDouble() * 2.0 - 1.0) * currentSpread;
                            double offsetZ = (random.nextDouble() * 2.0 - 1.0) * currentSpread;

                            builder.setScaleData(GenericParticleData.create(1.25f).build())
                                    .spawn(world, pos.x + offsetX, pos.y + i * 1.5 + random.nextDouble(), pos.z + offsetZ);
                        }
                    }
                }
            });
        });
            TickSchedulerClient.schedule(TimeUtils.seconds(delaySecs), () -> {
                int amount = 2;
                TickSchedulerClient.scheduleRepeating(TimeUtils.seconds(delaySecs*2), i -> {
                    if (i % 2 == 0) {
                        for (int j = 0; j < amount; j++) {
                            builder.setScaleData(GenericParticleData.create(2.5f + (random.nextBetween(10, 50) / 10f)).build()).spawn(world, pos.x + random.nextBetween(-3, 3), pos.y + random.nextBetween(-1, 2), pos.z + random.nextBetween(-3, 3));
                        }
                    }
                });
            });
    }
}
