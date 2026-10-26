package com.lumii.armory.item.discontinuity;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

import java.util.List;

public class DiscontinuityRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    private final Random random = Random.create();

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        VariantData data = DiscontinuityItem.getVariantData(stack);
        BakedModelManager models = MinecraftClient.getInstance().getBakedModelManager();

        VertexConsumer consumer = ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, TexturedRenderLayers.getEntityCutout(), true, stack.hasGlint());

        boolean isGui = (mode == ModelTransformationMode.GUI || mode == ModelTransformationMode.GROUND);

        if (isGui) {
            PartVariant[] parts = new PartVariant[] { data.handle(), data.mid(), data.end() };
            for (PartVariant part : parts) {
                if (part.doIgnoreLight()) {
                    renderModel(models.getModel(part.getGuiModelId()), matrices, consumer, LightmapTextureManager.MAX_LIGHT_COORDINATE, overlay);
                }
                else renderModel(models.getModel(part.getGuiModelId()), matrices, consumer, light, overlay);
            }
        }
        else {
            boolean isFirstPerson = (mode == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND || mode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND);
            if (isFirstPerson) {
                matrices.push();
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(10.0f));
            }

            float accumulatedOffsetX = 0f;
            float accumulatedOffsetY = 0f;

            float fullOffset = 0;

            PartVariant[] parts = new PartVariant[] { data.handle(), data.mid(), data.end() };

            for (PartVariant part : parts) {
                fullOffset += part.getFullOffset();
                matrices.push();
                matrices.scale((float) part.getScaleFactor() * 1.1f + part.getScaleFactor() / 10e3f, (float) part.getScaleFactor() * 1.1f + part.getScaleFactor() / 10e3f, 1.25f + part.getScaleFactor() / 10e3f);
                matrices.translate(accumulatedOffsetX / part.getScaleFactor() / 16.0f + part.getScaleFactor() / 10e3f / 2 + fullOffset / part.getScaleFactor() / 16 - 1f / 16 / part.getScaleFactor(), accumulatedOffsetY / part.getScaleFactor() / 16.0f + part.getScaleFactor() / 10e3f / 2 + fullOffset / part.getScaleFactor() / 16 - 1f / 16 / part.getScaleFactor(), -2/16f - part.getScaleFactor() / 10e3f / 2);

                if (part.doIgnoreLight()) {
                    renderModel(models.getModel(part.getModelId()), matrices, consumer, LightmapTextureManager.MAX_LIGHT_COORDINATE, overlay);
                }
                else renderModel(models.getModel(part.getModelId()), matrices, consumer, light, overlay);
                matrices.pop();

                accumulatedOffsetX += part.getOffsetNext();
                accumulatedOffsetY += part.getOffsetNext();
            }

            if (isFirstPerson) {
                matrices.pop();
            }
        }
    }

    private void renderModel(BakedModel model, MatrixStack matrices, VertexConsumer consumer, int light, int overlay) {
        for (Direction dir : Direction.values()) {
            random.setSeed(42);
            renderQuads(matrices, consumer, model.getQuads(null, dir, random), light, overlay);
        }
        random.setSeed(42);
        renderQuads(matrices, consumer, model.getQuads(null, null, random), light, overlay);
    }

    private void renderQuads(MatrixStack matrices, VertexConsumer consumer, List<BakedQuad> quads, int light, int overlay) {
        MatrixStack.Entry entry = matrices.peek();
        for (BakedQuad quad : quads) {
            consumer.quad(entry, quad, 1f, 1f, 1f, light, overlay);
        }
    }
}