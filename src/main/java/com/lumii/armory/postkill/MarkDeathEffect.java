package com.lumii.armory.postkill;

import com.lumii.armory.util.time.TickSchedulerClient;
import com.lumii.armory.vfx.mark.shockwave.ShockwavePost;
import com.lumii.armory.vfx.mark.shockwave.ShockwavePostHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import team.lodestar.lodestone.handlers.ScreenshakeHandler;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry;
import team.lodestar.lodestone.systems.easing.Easing;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.screenshake.ScreenshakeInstance;

import java.awt.*;
import java.util.Random;

public class MarkDeathEffect {
    private static final Random RANDOM = new Random();

    public static void effectClient(Vec3d pos) {
        ScreenshakeInstance instance = new ScreenshakeInstance((int) (20 * 3f)).setIntensity(0.75f).setEasing(Easing.QUAD_IN_OUT);
        ScreenshakeHandler.addScreenshake(instance);
        shockwaveFx(pos);
        particleFx(pos);
    }

    private static void shockwaveFx(Vec3d pos) {
        var instance = new ShockwavePost(pos, 1.5f);
        instance = ShockwavePostHandler.INSTANCE.addFxInstance(instance);
        final var finalInstance = instance;
        TickSchedulerClient.schedule((int) (5.5*20), () -> {
            if (finalInstance != null) finalInstance.remove();
        });
    }

    private static void particleFx(Vec3d pos) {
        int amount = 4;
        WorldParticleBuilder builder = WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE)
                .setFullBrightLighting()
                .setFrictionStrength(0)
                .setNoClip(true)
                .setColorData(ColorParticleData.create(new Color(139, 121, 79, 255), new Color(142, 136, 115, 255)).build())
                .setFrictionStrength(0)
                .setScaleData(GenericParticleData.create(3.5f).build())
                .setLifetime(7 + RANDOM.nextInt(0, 7));
        TickSchedulerClient.scheduleRepeating(2*20, j -> {
            if (j % 2 == 0) {
                for (int i = 0; i < amount; i++) {
                    builder.spawn(MinecraftClient.getInstance().world, pos.x + RANDOM.nextDouble(-4, 4), pos.y + RANDOM.nextDouble(-1, 3), pos.z + RANDOM.nextDouble(-4, 4));
                }
            }
        });
    }
}
