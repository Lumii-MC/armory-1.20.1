package com.lumii.armory.vfx.beam;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import team.lodestar.lodestone.systems.postprocess.DynamicShaderFxInstance;

import java.util.function.BiConsumer;

public class BeamPost extends DynamicShaderFxInstance {
    private final Vec3d position;
    public final float spawnTime;

    public BeamPost(Vec3d pos) {
        this.position = pos;
        this.spawnTime = getCurrentWorldTime();
    }

    private static float getCurrentWorldTime() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return 0f;
        return (client.world.getTime() + client.getTickDelta()) / 20.0f;
    }

    @Override
    public void writeDataToBuffer(BiConsumer<Integer, Float> writer) {
        writer.accept(0, (float) position.x);
        writer.accept(1, (float) position.y);
        writer.accept(2, (float) position.z);
        writer.accept(3, getCurrentWorldTime() - spawnTime);
    }
}
