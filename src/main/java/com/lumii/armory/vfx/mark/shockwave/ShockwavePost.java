package com.lumii.armory.vfx.mark.shockwave;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import team.lodestar.lodestone.systems.postprocess.DynamicShaderFxInstance;

import java.util.function.BiConsumer;

public class ShockwavePost extends DynamicShaderFxInstance {
    public Vec3d center;
    public float scale;
    public final float spawnTime;

    public ShockwavePost(Vec3d center, float scale) {
        this.center = center;
        this.scale = scale;
        this.spawnTime = getCurrentWorldTime();
    }

    private static float getCurrentWorldTime() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return 0f;
        return (client.world.getTime() + client.getTickDelta()) / 20.0f;
    }

    @Override
    public void writeDataToBuffer(BiConsumer<Integer, Float> writer) {
        writer.accept(0, (float) center.x);
        writer.accept(1, (float) center.y);
        writer.accept(2, (float) center.z);
        writer.accept(3, scale);
        writer.accept(4, getCurrentWorldTime() - spawnTime);
    }
}
