package com.lumii.armory.util.scissor;

import com.lumii.armory.Armory;
import com.lumii.armory.util.visual.QuadRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.CopyOnWriteArrayList;

@Environment(EnvType.CLIENT)
public class ClientChainRenderer {
    private static final CopyOnWriteArrayList<ActiveChain> CHAINS = new CopyOnWriteArrayList<>();

    public static CopyOnWriteArrayList<ActiveChain> getChains() {
        return CHAINS;
    }

    public static int addChain(ActiveChain chain) {
        CHAINS.add(chain);
        return CHAINS.size();
    }

    public static void removeChain(int index) {
        CHAINS.remove(index);
    }

    public static void removeChain(ActiveChain chain) {
        CHAINS.remove(chain);
    }

    public static ActiveChain isInChain(Entity e) {
        for (ActiveChain chain : CHAINS) {
            if (chain.e1().equals(e) || chain.e2().equals(e)) {
                return chain;
            }
        }
        return null;
    }

    public static void init() {
        WorldRenderEvents.LAST.register(context -> {
            float tickDelta = context.tickDelta();
            for (ActiveChain c : CHAINS) {
                Entity e1 = c.e1();
                Entity e2 = c.e2();
                if (e1 == null || e2 == null || !e1.isAlive() || !e2.isAlive()) continue;

                Vec3d pos1 = e1.getLerpedPos(tickDelta).add(0, e1.getHeight() / 2.0, 0);
                Vec3d pos2 = e2.getLerpedPos(tickDelta).add(0, e2.getHeight() / 2.0, 0);

                Vec3d delta = pos2.subtract(pos1);
                float distance = (float) delta.length();
                if (distance < 0.0001f) continue;
                Vec3d dir = delta.multiply(1.0 / distance);
                Vec3d midPoint = pos1.add(pos2).multiply(0.5);

                double horiz = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
                float yaw = (float) Math.toDegrees(Math.atan2(dir.x, dir.z));
                float pitch = (float) Math.toDegrees(Math.atan2(horiz, dir.y));

                QuadRenderer.renderImmediate(context.matrixStack(), context.camera().getPos(), midPoint, 1.0f, distance, new Vec3d(pitch, yaw, 0), 1, Armory.id("textures/vfx/chain.png"), 1, true);
            }
        });
    }
}