package com.lumii.armory.util.visual;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
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
 * <p>This class provides a renderer for 3D cubes in the world.</p>
 *
 * <p><b>Usage:</b> Call {@link #scheduleClient} to queue a cube for rendering.</p>
 *
 * <ul>
 *   <li><b>pos:</b> Position of the cube in world space (Vec3d)</li>
 *   <li><b>texture:</b> The texture of the cube, in the format of "namespace", "path/to/texture.png"</li>
 * </ul>
 *
 * @author Homak
 */
public final class CubeRenderer {

    private static final List<Cube> queuedCubes = new ArrayList<>();
    private static final List<CubeFrameTimed> queuedFrameCubes = new ArrayList<>();
    private static final List<Cube> cubesToRemove = new ArrayList<>();
    private static final List<CubeFrameTimed> frameCubesToRemove = new ArrayList<>();
    private static int tickCounter = 0;
    private static float partialTicks = 0f;
    private static int frameCounter = 0;

    private static final Identifier CUBE_PACKET_ID = new Identifier("tritium", "cube_render");

    public static class TextureFaceData {
        private final Identifier[] textures = new Identifier[6];

        public static final int FRONT = 0;
        public static final int BACK = 1;
        public static final int TOP = 2;
        public static final int BOTTOM = 3;
        public static final int RIGHT = 4;
        public static final int LEFT = 5;

        public TextureFaceData(Identifier front, Identifier back, Identifier top,
                               Identifier bottom, Identifier right, Identifier left) {
            textures[FRONT] = front;
            textures[BACK] = back;
            textures[TOP] = top;
            textures[BOTTOM] = bottom;
            textures[RIGHT] = right;
            textures[LEFT] = left;
        }

        public static TextureFaceData fromSingleTexture(Identifier texture) {
            return new TextureFaceData(texture, texture, texture, texture, texture, texture);
        }

        public Identifier getFront()  { return textures[FRONT]; }
        public Identifier getBack()   { return textures[BACK]; }
        public Identifier getTop()    { return textures[TOP]; }
        public Identifier getBottom() { return textures[BOTTOM]; }
        public Identifier getRight()  { return textures[RIGHT]; }
        public Identifier getLeft()   { return textures[LEFT]; }
    }

    /**
     * Schedule a cube to be rendered on the server and sent to all clients.
     * Call this from server-side code.
     */
    public static void scheduleCommon(ServerWorld world, Vec3d pos, float width, float height, float depth,
                                      Vec3d rotation, float scale, TextureFaceData textures,
                                      int duration, boolean fade, int fadeStart,
                                      boolean scaleUp, int scaleStart, float scaleFactor,
                                      float alpha) {

        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);

        buf.writeFloat(width);
        buf.writeFloat(height);
        buf.writeFloat(depth);

        buf.writeDouble(rotation.x);
        buf.writeDouble(rotation.y);
        buf.writeDouble(rotation.z);

        buf.writeFloat(scale);

        buf.writeString(textures.getFront().toString());
        buf.writeString(textures.getBack().toString());
        buf.writeString(textures.getTop().toString());
        buf.writeString(textures.getBottom().toString());
        buf.writeString(textures.getRight().toString());
        buf.writeString(textures.getLeft().toString());

        buf.writeInt(duration);
        buf.writeBoolean(fade);
        buf.writeInt(fadeStart);

        buf.writeBoolean(scaleUp);
        buf.writeInt(scaleStart);
        buf.writeFloat(scaleFactor);

        buf.writeFloat(alpha);

        for (ServerPlayerEntity player : world.getPlayers()) {
            ServerPlayNetworking.send(player, CUBE_PACKET_ID, buf);
        }
    }

    /**
     * Schedule a cube to be rendered only on the client with tick-based timing.
     * Call this from client-side code.
     */
    public static void scheduleClient(Vec3d pos, float width, float height, float depth,
                                      Vec3d rotation, float scale, TextureFaceData textures,
                                      int duration, boolean fade, int fadeStart,
                                      boolean scaleUp, int scaleStart, float scaleFactor,
                                      float alpha) {
        if (duration <= 0) return;
        synchronized (queuedCubes) {
            queuedCubes.add(new Cube(pos, width, height, depth, rotation, scale, textures, duration,
                    fade, fadeStart, scaleUp, scaleStart, scaleFactor, alpha));
        }
    }

    /**
     * Schedule a cube to be rendered only on the client with precision timing.
     * This is useful for complex math-driven animations that need frame-accurate timing.
     * 240 Of the timing unit here equals 1 second.
     * Automatically translates the given timing variables to the player's current fps.
     * This means that the cube always runs in 240 fps.
     */
    public static void scheduleClient240Hz(Vec3d pos, float width, float height, float depth,
                                           Vec3d rotation, float scale, TextureFaceData textures,
                                           int duration, boolean fade, int fadeStart,
                                           boolean scaleUp, int scaleStart, float scaleFactor,
                                           float baseAlpha) {

        int currentFPS = MinecraftClient.getInstance().getCurrentFps();
        if (duration == 1) {
            int durationA = 1;
            int fadeStartA = Math.round(fadeStart * (currentFPS / 240f));
            int scaleStartA = Math.round(scaleStart * (currentFPS / 240f));

            scheduleClientFrames(pos, width, height, depth,
                    rotation, scale, textures,
                    durationA, fade, fadeStartA,
                    scaleUp, scaleStartA, scaleFactor,
                    baseAlpha);
        } else {
            int durationA = Math.max(1, Math.round(duration * (currentFPS / 240f)));
            int fadeStartA = Math.round(fadeStart * (currentFPS / 240f));
            int scaleStartA = Math.round(scaleStart * (currentFPS / 240f));

            scheduleClientFrames(pos, width, height, depth,
                    rotation, scale, textures,
                    durationA, fade, fadeStartA,
                    scaleUp, scaleStartA, scaleFactor,
                    baseAlpha);
        }
    }

    private static void scheduleClientFrames(Vec3d pos, float width, float height, float depth,
                                             Vec3d rotation, float scale, TextureFaceData textures,
                                             int durationFrames, boolean fade, int fadeStartFrame,
                                             boolean scaleUp, int scaleStartFrame, float scaleFactor,
                                             float baseAlpha) {
        if (durationFrames <= 0) return;
        synchronized (queuedFrameCubes) {
            queuedFrameCubes.add(new CubeFrameTimed(
                    pos, width, height, depth, rotation, scale, textures, durationFrames,
                    fade, fadeStartFrame, scaleUp, scaleStartFrame, scaleFactor, baseAlpha,
                    frameCounter
            ));
        }
    }

    private static void clientTick() {
        synchronized (queuedCubes) {
            if (!queuedCubes.isEmpty()) {
                tickCounter++;
                cubesToRemove.clear();
                for (Cube cube : queuedCubes) {
                    if (!MinecraftClient.getInstance().isPaused() || !MinecraftClient.getInstance().isInSingleplayer()) {
                        cube.prevDuration = cube.duration;
                        cube.duration--;
                    }
                    if (cube.duration <= 0) {
                        cubesToRemove.add(cube);
                    }
                }
                queuedCubes.removeAll(cubesToRemove);
            }
        }

        synchronized (queuedFrameCubes) {
            if (!queuedFrameCubes.isEmpty()) {
                frameCounter++;

                frameCubesToRemove.clear();
                for (CubeFrameTimed cube : queuedFrameCubes) {
                    int framesLived = frameCounter - cube.startFrame;
                    if (framesLived >= cube.durationFrames) {
                        frameCubesToRemove.add(cube);
                    }
                }
                queuedFrameCubes.removeAll(frameCubesToRemove);
            } else {
                frameCounter = 0;
            }
        }
    }

    public static void init() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            clientTick();
        });

        ClientPlayNetworking.registerGlobalReceiver(CUBE_PACKET_ID,
                (client, handler, buf, responseSender) -> {

                    double x = buf.readDouble();
                    double y = buf.readDouble();
                    double z = buf.readDouble();
                    Vec3d pos = new Vec3d(x, y, z);

                    float width = buf.readFloat();
                    float height = buf.readFloat();
                    float depth = buf.readFloat();

                    double rotX = buf.readDouble();
                    double rotY = buf.readDouble();
                    double rotZ = buf.readDouble();
                    Vec3d rotation = new Vec3d(rotX, rotY, rotZ);

                    float scale = buf.readFloat();

                    Identifier front  = new Identifier(buf.readString());
                    Identifier back   = new Identifier(buf.readString());
                    Identifier top    = new Identifier(buf.readString());
                    Identifier bottom = new Identifier(buf.readString());
                    Identifier right  = new Identifier(buf.readString());
                    Identifier left   = new Identifier(buf.readString());
                    TextureFaceData textures = new TextureFaceData(front, back, top, bottom, right, left);

                    int duration = buf.readInt();
                    boolean fade = buf.readBoolean();
                    int fadeStart = buf.readInt();

                    boolean scaleUp = buf.readBoolean();
                    int scaleStart = buf.readInt();
                    float scaleFactor = buf.readFloat();

                    float alpha = buf.readFloat();

                    client.execute(() -> {
                        scheduleClient(pos, width, height, depth, rotation, scale, textures,
                                duration, fade, fadeStart, scaleUp, scaleStart, scaleFactor, alpha);
                    });
                }
        );

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            MatrixStack matrices = context.matrixStack();
            Vec3d camPos = context.camera().getPos();
            partialTicks = context.tickDelta();

            List<RenderableCubeFace> cubeFaces = new ArrayList<>();
            List<QuadRenderer.RenderableQuad> quads = QuadRenderer.getRenderableQuads(camPos, partialTicks);

            synchronized (queuedCubes) {
                for (Cube cube : queuedCubes) {
                    cubeFaces.addAll(RenderableCubeFace.fromCube(cube, camPos, partialTicks));
                }
            }
            synchronized (queuedFrameCubes) {
                for (CubeFrameTimed cube : queuedFrameCubes) {
                    cubeFaces.addAll(RenderableCubeFace.fromCubeFrameTimed(cube, camPos));
                }
            }

            cubeFaces.sort((a, b) -> Double.compare(b.distanceSq, a.distanceSq));
            quads.sort((a, b) -> Double.compare(b.getDistanceSq(), a.getDistanceSq()));

            RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            if (!cubeFaces.isEmpty()) {
                RenderSystem.enableCull();
                RenderSystem.depthMask(true);
                for (RenderableCubeFace face : cubeFaces) {
                    face.render(matrices, camPos);
                }
            }

            if (!quads.isEmpty()) {
                RenderSystem.disableCull();
                RenderSystem.depthMask(false);
                for (QuadRenderer.RenderableQuad quad : quads) {
                    quad.render(matrices, camPos);
                }
            }

            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        });
    }

    private static void renderFaceGeometry(BufferBuilder buffer, Tessellator tessellator,
                                           Matrix4f matrix, TextureFaceData textures,
                                           int faceIndex, float hw, float hh, float hd,
                                           int r, int g, int b, int a) {
        switch (faceIndex) {
            case TextureFaceData.FRONT -> {
                RenderSystem.setShaderTexture(0, textures.getFront());
                buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                buffer.vertex(matrix, -hw, -hh, -hd).texture(0f, 1f).color(r, g, b, a).next();
                buffer.vertex(matrix, -hw,  hh, -hd).texture(0f, 0f).color(r, g, b, a).next();
                buffer.vertex(matrix,  hw,  hh, -hd).texture(1f, 0f).color(r, g, b, a).next();
                buffer.vertex(matrix,  hw, -hh, -hd).texture(1f, 1f).color(r, g, b, a).next();
                tessellator.draw();
            }
            case TextureFaceData.BACK -> {
                RenderSystem.setShaderTexture(0, textures.getBack());
                buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                buffer.vertex(matrix, -hw, -hh,  hd).texture(1f, 1f).color(r, g, b, a).next();
                buffer.vertex(matrix,  hw, -hh,  hd).texture(0f, 1f).color(r, g, b, a).next();
                buffer.vertex(matrix,  hw,  hh,  hd).texture(0f, 0f).color(r, g, b, a).next();
                buffer.vertex(matrix, -hw,  hh,  hd).texture(1f, 0f).color(r, g, b, a).next();
                tessellator.draw();
            }
            case TextureFaceData.TOP -> {
                RenderSystem.setShaderTexture(0, textures.getTop());
                buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                buffer.vertex(matrix, -hw,  hh, -hd).texture(0f, 1f).color(r, g, b, a).next();
                buffer.vertex(matrix, -hw,  hh,  hd).texture(0f, 0f).color(r, g, b, a).next();
                buffer.vertex(matrix,  hw,  hh,  hd).texture(1f, 0f).color(r, g, b, a).next();
                buffer.vertex(matrix,  hw,  hh, -hd).texture(1f, 1f).color(r, g, b, a).next();
                tessellator.draw();
            }
            case TextureFaceData.BOTTOM -> {
                RenderSystem.setShaderTexture(0, textures.getBottom());
                buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                buffer.vertex(matrix, -hw, -hh, -hd).texture(0f, 0f).color(r, g, b, a).next();
                buffer.vertex(matrix,  hw, -hh, -hd).texture(1f, 0f).color(r, g, b, a).next();
                buffer.vertex(matrix,  hw, -hh,  hd).texture(1f, 1f).color(r, g, b, a).next();
                buffer.vertex(matrix, -hw, -hh,  hd).texture(0f, 1f).color(r, g, b, a).next();
                tessellator.draw();
            }
            case TextureFaceData.RIGHT -> {
                RenderSystem.setShaderTexture(0, textures.getRight());
                buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                buffer.vertex(matrix,  hw, -hh, -hd).texture(0f, 1f).color(r, g, b, a).next();
                buffer.vertex(matrix,  hw,  hh, -hd).texture(0f, 0f).color(r, g, b, a).next();
                buffer.vertex(matrix,  hw,  hh,  hd).texture(1f, 0f).color(r, g, b, a).next();
                buffer.vertex(matrix,  hw, -hh,  hd).texture(1f, 1f).color(r, g, b, a).next();
                tessellator.draw();
            }
            case TextureFaceData.LEFT -> {
                RenderSystem.setShaderTexture(0, textures.getLeft());
                buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                buffer.vertex(matrix, -hw, -hh, -hd).texture(1f, 1f).color(r, g, b, a).next();
                buffer.vertex(matrix, -hw, -hh,  hd).texture(0f, 1f).color(r, g, b, a).next();
                buffer.vertex(matrix, -hw,  hh,  hd).texture(0f, 0f).color(r, g, b, a).next();
                buffer.vertex(matrix, -hw,  hh, -hd).texture(1f, 0f).color(r, g, b, a).next();
                tessellator.draw();
            }
        }
    }

    private static void renderCubeFace(MatrixStack matrices, Vec3d camPos,
                                       Cube cube, int faceIndex, float alpha, float scale) {
        matrices.push();
        matrices.translate(cube.position.x - camPos.x, cube.position.y - camPos.y, cube.position.z - camPos.z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) cube.rotation.y));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) cube.rotation.x));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) cube.rotation.z));
        matrices.scale(scale, scale, scale);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float hw = cube.width  / 2f;
        float hh = cube.height / 2f;
        float hd = cube.depth  / 2f;
        int a = (int) (alpha * 255);

        renderFaceGeometry(Tessellator.getInstance().getBuffer(), Tessellator.getInstance(),
                matrix, cube.textures, faceIndex, hw, hh, hd, 255, 255, 255, a);
        matrices.pop();
    }

    private static void renderCubeFrameTimedFace(MatrixStack matrices, Vec3d camPos,
                                                 CubeFrameTimed cube, int faceIndex, float alpha, float scale) {
        matrices.push();
        matrices.translate(cube.position.x - camPos.x, cube.position.y - camPos.y, cube.position.z - camPos.z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) cube.rotation.y));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) cube.rotation.x));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) cube.rotation.z));
        matrices.scale(scale, scale, scale);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float hw = cube.width  / 2f;
        float hh = cube.height / 2f;
        float hd = cube.depth  / 2f;
        int a = (int) (alpha * 255);

        renderFaceGeometry(Tessellator.getInstance().getBuffer(), Tessellator.getInstance(),
                matrix, cube.textures, faceIndex, hw, hh, hd, 255, 255, 255, a);
        matrices.pop();
    }

    private static class Cube {
        Vec3d position;
        float width, height, depth;
        Vec3d rotation;
        float scale;
        TextureFaceData textures;
        int duration, maxDuration;
        int prevDuration;
        boolean fade;
        int fadeStart;
        boolean scaleUp;
        int scaleStart;
        float scaleFactor;
        float baseAlpha;

        Cube(Vec3d pos, float w, float h, float d, Vec3d rot, float s, TextureFaceData tex,
             int duration, boolean fade, int fadeStart,
             boolean scaleUp, int scaleStart, float scaleFactor, float baseAlpha) {
            this.position = pos;
            this.width = w;
            this.height = h;
            this.depth = d;
            this.rotation = rot;
            this.scale = s;
            this.textures = tex;
            this.duration = duration;
            this.prevDuration = duration;
            this.maxDuration = duration;
            this.fade = fade;
            this.fadeStart = fadeStart;
            this.scaleUp = scaleUp;
            this.scaleStart = scaleStart;
            this.scaleFactor = scaleFactor;
            this.baseAlpha = baseAlpha;
        }
    }

    private static class CubeFrameTimed {
        Vec3d position;
        float width, height, depth;
        Vec3d rotation;
        float scale;
        TextureFaceData textures;
        int durationFrames;
        int startFrame;
        boolean fade;
        int fadeStartFrame;
        boolean scaleUp;
        int scaleStartFrame;
        float scaleFactor;
        float baseAlpha;

        CubeFrameTimed(Vec3d pos, float w, float h, float d, Vec3d rot, float s, TextureFaceData tex,
                       int durationFrames, boolean fade, int fadeStartFrame,
                       boolean scaleUp, int scaleStartFrame, float scaleFactor, float baseAlpha,
                       int startFrame) {
            this.position = pos;
            this.width = w;
            this.height = h;
            this.depth = d;
            this.rotation = rot;
            this.scale = s;
            this.textures = tex;
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

    public static void clearAllCubes() {
        synchronized (queuedCubes) {
            queuedCubes.clear();
        }
        synchronized (queuedFrameCubes) {
            queuedFrameCubes.clear();
        }
    }

    public static void clearFrameQuads() {
        synchronized (queuedCubes) {
            queuedCubes.clear();
        }
    }

    public static class RenderableObject {
        protected final Object object;
        protected final double distanceSq;
        protected static float partialTicks;
        protected final boolean isCube;
        protected final boolean isFrameTimed;

        public RenderableObject(Object object, double distanceSq, float partialTicks,
                                boolean isCube, boolean isFrameTimed) {
            this.object = object;
            this.distanceSq = distanceSq;
            this.partialTicks = partialTicks;
            this.isCube = isCube;
            this.isFrameTimed = isFrameTimed;
        }

        RenderableObject(QuadRenderer.RenderableQuad quad) {
            this(quad, quad.getDistanceSq(), quad.getPartialTicks(), false, quad.isFrameTimed());
        }

        public void render(MatrixStack matrices, Vec3d camPos) {
            if (!isCube) {
                ((QuadRenderer.RenderableQuad) object).render(matrices, camPos);
            }
        }
    }

    public static class RenderableCubeFace extends RenderableObject {

        private final int faceIndex;
        private final float resolvedAlpha;
        private final float resolvedScale;
        private final Object cubeObj;
        private final boolean isFrameTimedCube;

        private RenderableCubeFace(Object cubeObj, boolean isFrameTimedCube,
                                   int faceIndex, double distanceSq,
                                   float partialTicks, float resolvedAlpha, float resolvedScale) {
            super(cubeObj, distanceSq, partialTicks, true, isFrameTimedCube);
            this.cubeObj = cubeObj;
            this.faceIndex = faceIndex;
            this.resolvedAlpha = resolvedAlpha;
            this.resolvedScale = resolvedScale;
            this.isFrameTimedCube = isFrameTimedCube;
        }

        private static Vec3d faceCenterWorld(Vec3d cubePos, float hw, float hh, float hd,
                                             Vec3d rotation, int faceIndex) {
            float lx, ly, lz;
            switch (faceIndex) {
                case TextureFaceData.FRONT  -> { lx =  0;  ly =  0;  lz = -hd; }
                case TextureFaceData.BACK   -> { lx =  0;  ly =  0;  lz =  hd; }
                case TextureFaceData.TOP    -> { lx =  0;  ly =  hh; lz =  0;  }
                case TextureFaceData.BOTTOM -> { lx =  0;  ly = -hh; lz =  0;  }
                case TextureFaceData.RIGHT  -> { lx =  hw; ly =  0;  lz =  0;  }
                case TextureFaceData.LEFT   -> { lx = -hw; ly =  0;  lz =  0;  }
                default                     -> { lx =  0;  ly =  0;  lz =  0;  }
            }

            double ry = Math.toRadians(rotation.y);
            double rx = Math.toRadians(rotation.x);
            double rz = Math.toRadians(rotation.z);

            double x1 =  lx * Math.cos(ry) + lz * Math.sin(ry);
            double y1 =  ly;
            double z1 = -lx * Math.sin(ry) + lz * Math.cos(ry);

            double x2 =  x1;
            double y2 =  y1 * Math.cos(rx) - z1 * Math.sin(rx);
            double z2 =  y1 * Math.sin(rx) + z1 * Math.cos(rx);

            double x3 =  x2 * Math.cos(rz) - y2 * Math.sin(rz);
            double y3 =  x2 * Math.sin(rz) + y2 * Math.cos(rz);
            double z3 =  z2;

            return cubePos.add(x3, y3, z3);
        }

        static List<RenderableCubeFace> fromCube(Cube cube, Vec3d camPos, float partialTicks) {
            float interpolatedDuration = cube.prevDuration + (cube.duration - cube.prevDuration) * partialTicks;
            float interpolatedTicksLived = cube.maxDuration - interpolatedDuration;

            float alpha = cube.baseAlpha;
            if (cube.fade && interpolatedTicksLived >= cube.fadeStart) {
                int fadeTicks = cube.maxDuration - cube.fadeStart;
                float remaining = Math.max(0, interpolatedDuration - cube.fadeStart);
                alpha = cube.baseAlpha * (remaining / fadeTicks);
                if (alpha < 0f) alpha = 0f;
            }

            float scale = cube.scale;
            if (cube.scaleUp && interpolatedTicksLived >= cube.scaleStart) {
                float t = (interpolatedTicksLived - cube.scaleStart) / (cube.maxDuration - cube.scaleStart);
                if (t > 1f) t = 1f;
                scale = cube.scale * (1f + (cube.scaleFactor - 1f) * t * t);
            }

            float hw = (cube.width  / 2f) * scale;
            float hh = (cube.height / 2f) * scale;
            float hd = (cube.depth  / 2f) * scale;

            List<RenderableCubeFace> faces = new ArrayList<>(6);
            for (int i = 0; i < 6; i++) {
                Vec3d faceCenter = faceCenterWorld(cube.position, hw, hh, hd, cube.rotation, i);
                double distSq = faceCenter.squaredDistanceTo(camPos);
                faces.add(new RenderableCubeFace(cube, false, i, distSq, partialTicks, alpha, scale));
            }
            return faces;
        }

        static List<RenderableCubeFace> fromCubeFrameTimed(CubeFrameTimed cube, Vec3d camPos) {
            int framesLived = frameCounter - cube.startFrame;
            float interpolatedFramesLived = framesLived + partialTicks;
            float progress = Math.min(1f, interpolatedFramesLived / cube.durationFrames);

            float alpha = cube.baseAlpha;
            if (cube.fade) {
                float fadeStartProgress = (float) cube.fadeStartFrame / cube.durationFrames;
                if (progress >= fadeStartProgress) {
                    float fadeProgress = (progress - fadeStartProgress) / (1f - fadeStartProgress);
                    alpha = cube.baseAlpha * (1f - fadeProgress);
                    if (alpha < 0f) alpha = 0f;
                }
            }

            float scale = cube.scale;
            if (cube.scaleUp) {
                float scaleStartProgress = (float) cube.scaleStartFrame / cube.durationFrames;
                if (progress >= scaleStartProgress) {
                    float scaleProgress = (progress - scaleStartProgress) / (1f - scaleStartProgress);
                    scale = cube.scale * (1f + (cube.scaleFactor - 1f) * scaleProgress * scaleProgress);
                }
            }

            float hw = (cube.width  / 2f) * scale;
            float hh = (cube.height / 2f) * scale;
            float hd = (cube.depth  / 2f) * scale;

            List<RenderableCubeFace> faces = new ArrayList<>(6);
            for (int i = 0; i < 6; i++) {
                Vec3d faceCenter = faceCenterWorld(cube.position, hw, hh, hd, cube.rotation, i);
                double distSq = faceCenter.squaredDistanceTo(camPos);
                faces.add(new RenderableCubeFace(cube, true, i, distSq, 0, alpha, scale));
            }
            return faces;
        }

        @Override
        public void render(MatrixStack matrices, Vec3d camPos) {
            if (isFrameTimedCube) {
                renderCubeFrameTimedFace(matrices, camPos, (CubeFrameTimed) cubeObj,
                        faceIndex, resolvedAlpha, resolvedScale);
            } else {
                renderCubeFace(matrices, camPos, (Cube) cubeObj,
                        faceIndex, resolvedAlpha, resolvedScale);
            }
        }
    }
}