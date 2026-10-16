package com.lumii.armory.vfx.beam;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import team.lodestar.lodestone.systems.postprocess.DynamicShaderFxInstance;

import java.awt.*;
import java.util.function.BiConsumer;

public class BeamPost extends DynamicShaderFxInstance {
    private final Vec3d position;
    public final float spawnTime;
    public final int color;

    public BeamPost(Vec3d pos, int color) {
        this.position = pos;
        this.spawnTime = getCurrentWorldTime();
        this.color = color;
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
        Color col = new Color(this.color);
        float colorR = col.getRed();
        float colorG = col.getGreen();
        float colorB = col.getBlue();
        writer.accept(4, colorR / 255);
        writer.accept(5, colorG / 255);
        writer.accept(6, colorB / 255);
    }
}
