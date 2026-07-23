package com.lumii.armory.util.visual;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Licensed MIT, free to use with attribution to the author (Homak)
 *
 * <p>This class provides a renderer for 2D quads in the world.</p>
 *
 * <p><b>Usage:</b> Call {@link #scheduleClient} to queue a quad for rendering.</p>
 *
 * <ul>
 *   <li><b>pos:</b> Position of the quad in world space (Vec3d)</li>
 *   <li><b>texture:</b> The texture of the quad, in the format of "namespace", "path/to/texture.png"</li>
 * </ul>
 *
 * @author Homak
 */
public final class QuadRenderer {
    private static final List<Quad> queuedQuads = new ArrayList<>();
    private static final List<QuadFrameTimed> queuedFrameQuads = new ArrayList<>();
    private static final List<Quad> quadsToRemove = new ArrayList<>();
    private static final List<QuadFrameTimed> frameQuadsToRemove = new ArrayList<>();
    private static int tickCounter = 0;
    private static float partialTicks = 0f;
    private static int frameCounter = 0;

    private static final Identifier QUAD_PACKET_ID = new Identifier("tritium", "quad_render");
    private static final Identifier QUAD_PACKET_SPIN_ID = new Identifier("tritium", "quad_render_spin");

    public static void scheduleCommon(ServerWorld world, Vec3d pos, float width, float height,
                                      Vec3d rotation, float scale, Identifier texture,
                                      int duration, boolean fade, int fadeStart,
                                      boolean scaleUp, int scaleStart, float scaleFactor,
                                      float alpha) {

        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);

        buf.writeFloat(width);
        buf.writeFloat(height);

        buf.writeDouble(rotation.x);
        buf.writeDouble(rotation.y);
        buf.writeDouble(rotation.z);

        buf.writeFloat(scale);
        buf.writeString(texture.toString());

        buf.writeInt(duration);
        buf.writeBoolean(fade);
        buf.writeInt(fadeStart);

        buf.writeBoolean(scaleUp);
        buf.writeInt(scaleStart);
        buf.writeFloat(scaleFactor);

        buf.writeFloat(alpha);

        for (ServerPlayerEntity player : world.getPlayers()) {
            ServerPlayNetworking.send(player, QUAD_PACKET_ID, buf);
        }
    }

    public static void scheduleCommon(ServerWorld world, Vec3d pos, float width, float height,
                                      Vec3d rotation, float scale, Identifier texture,
                                      int duration, boolean fade, int fadeStart,
                                      boolean scaleUp, int scaleStart, float scaleFactor,
                                      float alpha, SpinAxis rotationAxis, float rotMult) {

        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);

        buf.writeFloat(width);
        buf.writeFloat(height);

        buf.writeDouble(rotation.x);
        buf.writeDouble(rotation.y);
        buf.writeDouble(rotation.z);

        buf.writeFloat(scale);
        buf.writeString(texture.toString());

        buf.writeInt(duration);
        buf.writeBoolean(fade);
        buf.writeInt(fadeStart);

        buf.writeBoolean(scaleUp);
        buf.writeInt(scaleStart);
        buf.writeFloat(scaleFactor);

        buf.writeFloat(alpha);

        buf.writeInt(rotationAxis.ordinal());
        buf.writeFloat(rotMult);

        for (ServerPlayerEntity player : world.getPlayers()) {
            ServerPlayNetworking.send(player, QUAD_PACKET_SPIN_ID, buf);
        }
    }

    public static void scheduleClient(Vec3d pos, float width, float height,
                                      Vec3d rotation, float scale, Identifier texture,
                                      int duration, boolean fade, int fadeStart,
                                      boolean scaleUp, int scaleStart, float scaleFactor,
                                      float alpha) {
        scheduleClient(pos, width, height, rotation, scale, texture, duration, fade, fadeStart,
                scaleUp, scaleStart, scaleFactor, alpha, SpinAxis.Y, 0f);
    }

    public static void scheduleClient(Vec3d pos, float width, float height,
                                      Vec3d rotation, float scale, Identifier texture,
                                      int duration, boolean fade, int fadeStart,
                                      boolean scaleUp, int scaleStart, float scaleFactor,
                                      float alpha, SpinAxis spinAxis, float rotMult) {
        if (duration <= 0) return;
        synchronized (queuedQuads) {
            queuedQuads.add(new Quad(pos, width, height, rotation, scale, texture, duration,
                    fade, fadeStart, scaleUp, scaleStart, scaleFactor, alpha, spinAxis, rotMult));
        }
    }

    public static void scheduleClient240Hz(Vec3d pos, float width, float height,
                                           Vec3d rotation, float scale, Identifier texture,
                                           int duration, boolean fade, int fadeStart,
                                           boolean scaleUp, int scaleStart, float scaleFactor,
                                           float baseAlpha) {
        int currentFPS = MinecraftClient.getInstance().getCurrentFps();

        if (duration == 1) {
            int durationA = 1;
            int fadeStartA = fadeStart;
            int scaleStartA = scaleStart;
            scheduleClientFrames(pos, width, height,
                    rotation, scale, texture,
                    durationA, fade, fadeStartA,
                    scaleUp, scaleStartA, scaleFactor,
                    baseAlpha);
        } else {
            int durationA = Math.max(1, Math.round(duration * (currentFPS / 240f)));
            int fadeStartA = Math.round(fadeStart * (currentFPS / 240f));
            int scaleStartA = Math.round(scaleStart * (currentFPS / 240f));
            scheduleClientFrames(pos, width, height,
                    rotation, scale, texture,
                    durationA, fade, fadeStartA,
                    scaleUp, scaleStartA, scaleFactor,
                    baseAlpha);
        }
    }

    public static void scheduleClientFrames(Vec3d pos, float width, float height,
                                            Vec3d rotation, float scale, Identifier texture,
                                            int durationFrames, boolean fade, int fadeStartFrame,
                                            boolean scaleUp, int scaleStartFrame, float scaleFactor,
                                            float baseAlpha) {
        if (durationFrames <= 0) return;
        synchronized (queuedFrameQuads) {
            queuedFrameQuads.add(new QuadFrameTimed(
                    pos, width, height, rotation, scale, texture, durationFrames,
                    fade, fadeStartFrame, scaleUp, scaleStartFrame, scaleFactor, baseAlpha,
                    frameCounter
            ));
        }
    }

    private static void clientTick() {
        synchronized (queuedQuads) {
            if (!queuedQuads.isEmpty()) {
                tickCounter++;

                quadsToRemove.clear();
                for (Quad quad : queuedQuads) {
                    if (!MinecraftClient.getInstance().isPaused() || !MinecraftClient.getInstance().isInSingleplayer()) {
                        quad.prevDuration = quad.duration;
                        quad.duration--;

                        if (quad.duration <= 0) {
                            quadsToRemove.add(quad);
                        }
                    }
                }
                queuedQuads.removeAll(quadsToRemove);
            }
        }
        synchronized (queuedFrameQuads) {
            if (!queuedFrameQuads.isEmpty()) {
                frameCounter++;

                frameQuadsToRemove.clear();
                for (QuadFrameTimed quad : queuedFrameQuads) {
                    int framesLived = frameCounter - quad.startFrame;
                    if (framesLived >= quad.durationFrames) {
                        frameQuadsToRemove.add(quad);
                    }
                }
                queuedFrameQuads.removeAll(frameQuadsToRemove);
            } else {
                frameCounter = 0;
            }
        }
    }

    public static void init() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            clientTick();
        });

        ClientPlayNetworking.registerGlobalReceiver(QUAD_PACKET_ID,
                (client, handler, buf, responseSender) -> {

                    double x = buf.readDouble();
                    double y = buf.readDouble();
                    double z = buf.readDouble();
                    Vec3d pos = new Vec3d(x, y, z);

                    float width = buf.readFloat();
                    float height = buf.readFloat();

                    double rotX = buf.readDouble();
                    double rotY = buf.readDouble();
                    double rotZ = buf.readDouble();
                    Vec3d rotation = new Vec3d(rotX, rotY, rotZ);

                    float scale = buf.readFloat();
                    String textureStr = buf.readString();
                    Identifier texture = new Identifier(textureStr);

                    int duration = buf.readInt();
                    boolean fade = buf.readBoolean();
                    int fadeStart = buf.readInt();

                    boolean scaleUp = buf.readBoolean();
                    int scaleStart = buf.readInt();
                    float scaleFactor = buf.readFloat();

                    float alpha = buf.readFloat();

                    client.execute(() -> {
                        scheduleClient(pos, width, height, rotation, scale, texture,
                                duration, fade, fadeStart, scaleUp, scaleStart, scaleFactor, alpha);
                    });
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(QUAD_PACKET_SPIN_ID,
                (client, handler, buf, responseSender) -> {

                    double x = buf.readDouble();
                    double y = buf.readDouble();
                    double z = buf.readDouble();
                    Vec3d pos = new Vec3d(x, y, z);

                    float width = buf.readFloat();
                    float height = buf.readFloat();

                    double rotX = buf.readDouble();
                    double rotY = buf.readDouble();
                    double rotZ = buf.readDouble();
                    Vec3d rotation = new Vec3d(rotX, rotY, rotZ);

                    float scale = buf.readFloat();
                    String textureStr = buf.readString();
                    Identifier texture = new Identifier(textureStr);

                    int duration = buf.readInt();
                    boolean fade = buf.readBoolean();
                    int fadeStart = buf.readInt();

                    boolean scaleUp = buf.readBoolean();
                    int scaleStart = buf.readInt();
                    float scaleFactor = buf.readFloat();

                    float alpha = buf.readFloat();

                    int axisNum = buf.readInt();

                    SpinAxis axis = SpinAxis.values()[axisNum];

                    float mult = buf.readFloat();

                    client.execute(() -> {
                        scheduleClient(pos, width, height, rotation, scale, texture,
                                duration, fade, fadeStart, scaleUp, scaleStart, scaleFactor, alpha, axis, mult);
                    });
                }
        );
    }

    static void renderQuad(MatrixStack matrices, Vec3d camPos, Quad q, float partialTicks, boolean isTickBased) {
        float interpolatedDuration = q.prevDuration + (q.duration - q.prevDuration) * partialTicks;
        float interpolatedTicksLived = q.maxDuration - interpolatedDuration;

        float alpha = q.baseAlpha;
        if (q.fade) {
            if (interpolatedTicksLived >= q.fadeStart) {
                int fadeTicks = q.maxDuration - q.fadeStart;
                float remainingDuration = Math.max(0, interpolatedDuration - q.fadeStart);
                alpha = q.baseAlpha * (remainingDuration / fadeTicks);
                if (alpha < 0f) alpha = 0f;
            }
        }

        float scale = q.scale;
        if (q.scaleUp) {
            if (interpolatedTicksLived >= q.scaleStart) {
                float t = (interpolatedTicksLived - q.scaleStart) / (q.maxDuration - q.scaleStart);
                if (t > 1f) t = 1f;
                scale = q.scale * (1f + (q.scaleFactor - 1f) * t * t);
            }
        }

        RenderSystem.setShaderTexture(0, q.texture);
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

        matrices.push();
        matrices.translate(q.position.x - camPos.x, q.position.y - camPos.y, q.position.z - camPos.z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) q.rotation.y));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) q.rotation.x));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) q.rotation.z));

        if (q.rotMult != 0f) {
            float spinDegrees = q.rotMult * interpolatedTicksLived;
            switch (q.spinAxis) {
                case X -> matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(spinDegrees));
                case Y -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spinDegrees));
                case Z -> matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(spinDegrees));
            }
        }

        matrices.scale(scale, scale, scale);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float hw = q.width  / 2f;
        float hh = q.height / 2f;
        int r = 255, g = 255, b = 255;
        int a = (int) (alpha * 255);

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        buffer.vertex(matrix, -hw, -hh, 0).texture(0f, 1f).color(r, g, b, a).next();
        buffer.vertex(matrix,  hw, -hh, 0).texture(1f, 1f).color(r, g, b, a).next();
        buffer.vertex(matrix,  hw,  hh, 0).texture(1f, 0f).color(r, g, b, a).next();
        buffer.vertex(matrix, -hw,  hh, 0).texture(0f, 0f).color(r, g, b, a).next();

        Tessellator.getInstance().draw();
        matrices.pop();
    }

    static void renderQuadFrameTimed(MatrixStack matrices, Vec3d camPos, QuadFrameTimed q) {
        int framesLived = frameCounter - q.startFrame;
        float interpolatedFramesLived = framesLived + partialTicks;

        float progress = interpolatedFramesLived / q.durationFrames;
        if (progress > 1f) progress = 1f;

        float alpha = q.baseAlpha;
        if (q.fade) {
            float fadeStartProgress = (float) q.fadeStartFrame / q.durationFrames;
            if (progress >= fadeStartProgress) {
                float fadeRange = 1f - fadeStartProgress;
                float fadeProgress = (progress - fadeStartProgress) / fadeRange;
                alpha = q.baseAlpha * (1f - fadeProgress);
                if (alpha < 0f) alpha = 0f;
            }
        }

        float scale = q.scale;
        if (q.scaleUp) {
            float scaleStartProgress = (float) q.scaleStartFrame / q.durationFrames;
            if (progress >= scaleStartProgress) {
                float scaleRange = 1f - scaleStartProgress;
                float scaleProgress = (progress - scaleStartProgress) / scaleRange;
                scale = q.scale * (1f + (q.scaleFactor - 1f) * scaleProgress * scaleProgress);
            }
        }

        RenderSystem.setShaderTexture(0, q.texture);
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

        matrices.push();
        matrices.translate(q.position.x - camPos.x, q.position.y - camPos.y, q.position.z - camPos.z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) q.rotation.y));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) q.rotation.x));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) q.rotation.z));

        matrices.scale(scale, scale, scale);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float hw = q.width  / 2f;
        float hh = q.height / 2f;
        int r = 255, g = 255, b = 255;
        int a = (int) (alpha * 255);

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        buffer.vertex(matrix, -hw, -hh, 0).texture(0f, 1f).color(r, g, b, a).next();
        buffer.vertex(matrix,  hw, -hh, 0).texture(1f, 1f).color(r, g, b, a).next();
        buffer.vertex(matrix,  hw,  hh, 0).texture(1f, 0f).color(r, g, b, a).next();
        buffer.vertex(matrix, -hw,  hh, 0).texture(0f, 0f).color(r, g, b, a).next();

        Tessellator.getInstance().draw();
        matrices.pop();
    }

    public enum SpinAxis { X, Y, Z }

    private static class Quad {
        Vec3d position;
        float width, height;
        Vec3d rotation;
        float scale;
        Identifier texture;
        int duration, maxDuration;
        int prevDuration;
        boolean fade;
        int fadeStart;
        boolean scaleUp;
        int scaleStart;
        float scaleFactor;
        float baseAlpha;
        SpinAxis spinAxis;
        float rotMult;

        Quad(Vec3d pos, float w, float h, Vec3d rot, float s, Identifier tex,
             int duration, boolean fade, int fadeStart,
             boolean scaleUp, int scaleStart, float scaleFactor, float baseAlpha,
             SpinAxis spinAxis, float rotMult) {
            this.position = pos;
            this.width = w;
            this.height = h;
            this.rotation = rot;
            this.scale = s;
            this.texture = tex;
            this.duration = duration;
            this.prevDuration = duration;
            this.maxDuration = duration;
            this.fade = fade;
            this.fadeStart = fadeStart;
            this.scaleUp = scaleUp;
            this.scaleStart = scaleStart;
            this.scaleFactor = scaleFactor;
            this.baseAlpha = baseAlpha;
            this.spinAxis = spinAxis;
            this.rotMult = rotMult;
        }
    }

    private static class QuadFrameTimed {
        Vec3d position;
        float width, height;
        Vec3d rotation;
        float scale;
        Identifier texture;
        int durationFrames;
        int startFrame;
        boolean fade;
        int fadeStartFrame;
        boolean scaleUp;
        int scaleStartFrame;
        float scaleFactor;
        float baseAlpha;

        QuadFrameTimed(Vec3d pos, float w, float h, Vec3d rot, float s, Identifier tex,
                       int durationFrames, boolean fade, int fadeStartFrame,
                       boolean scaleUp, int scaleStartFrame, float scaleFactor, float baseAlpha,
                       int startFrame) {
            this.position = pos;
            this.width = w;
            this.height = h;
            this.rotation = rot;
            this.scale = s;
            this.texture = tex;
            this.durationFrames = durationFrames;
            this.startFrame = startFrame;
            this.fade = fade;
            this.fadeStartFrame = fadeStartFrame;
            this.scaleUp = scaleUp;
            this.scaleStartFrame = scaleStartFrame;
            this.scaleFactor = scaleFactor;
            this.baseAlpha = baseAlpha;
        }
    }

    public static void clearAllQuads() {
        synchronized (queuedQuads) {
            queuedQuads.clear();
        }
        synchronized (queuedFrameQuads) {
            queuedFrameQuads.clear();
        }
    }

    public static void clearFrameQuads() {
        synchronized (queuedFrameQuads) {
            queuedFrameQuads.clear();
        }
    }

    public static void renderImmediate(MatrixStack matrices, Vec3d camPos,
                                       Vec3d pos, float width, float height,
                                       Vec3d rotation, float scale, Identifier texture, float alpha) {
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, texture);

        int a = (int) (alpha * 255);
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

        matrices.push();
        matrices.translate(pos.x - camPos.x, pos.y - camPos.y, pos.z - camPos.z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) rotation.y));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) rotation.x));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) rotation.z));
        matrices.scale(scale, scale, scale);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float hw = width  / 2f;
        float hh = height / 2f;

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(matrix, -hw, -hh, 0).texture(0f, 1f).color(255, 255, 255, a).next();
        buffer.vertex(matrix,  hw, -hh, 0).texture(1f, 1f).color(255, 255, 255, a).next();
        buffer.vertex(matrix,  hw,  hh, 0).texture(1f, 0f).color(255, 255, 255, a).next();
        buffer.vertex(matrix, -hw,  hh, 0).texture(0f, 0f).color(255, 255, 255, a).next();
        Tessellator.getInstance().draw();

        matrices.pop();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static class RenderableQuad extends CubeRenderer.RenderableObject {
        private final Quad quad;
        private final QuadFrameTimed quadFrameTimed;
        private final boolean isFrameTimed;
        private final double distanceSq;
        private final float partialTicks;

        public RenderableQuad(Quad quad, Vec3d camPos, float partialTicks) {
            super(quad, quad.position.squaredDistanceTo(camPos), partialTicks, false, false);
            this.quad = quad;
            this.quadFrameTimed = null;
            this.isFrameTimed = false;
            this.distanceSq = quad.position.squaredDistanceTo(camPos);
            this.partialTicks = partialTicks;
        }

        public RenderableQuad(QuadFrameTimed quad, Vec3d camPos) {
            super(quad, quad.position.squaredDistanceTo(camPos), 0, false, true);
            this.quad = null;
            this.quadFrameTimed = quad;
            this.isFrameTimed = true;
            this.distanceSq = quad.position.squaredDistanceTo(camPos);
            this.partialTicks = 0;
        }

        @Override
        public void render(MatrixStack matrices, Vec3d camPos) {
            if (isFrameTimed) {
                renderQuadFrameTimed(matrices, camPos, quadFrameTimed);
            } else {
                renderQuad(matrices, camPos, quad, partialTicks, true);
            }
        }

        public double getDistanceSq()  { return distanceSq; }
        public float  getPartialTicks() { return partialTicks; }
        public boolean isFrameTimed()  { return isFrameTimed; }
    }

    public static List<RenderableQuad> getRenderableQuads(Vec3d camPos, float partialTicks) {
        List<RenderableQuad> result = new ArrayList<>();

        synchronized (queuedQuads) {
            for (Quad quad : queuedQuads) {
                result.add(new RenderableQuad(quad, camPos, partialTicks));
            }
        }

        synchronized (queuedFrameQuads) {
            for (QuadFrameTimed quad : queuedFrameQuads) {
                result.add(new RenderableQuad(quad, camPos));
            }
        }

        return result;
    }
}