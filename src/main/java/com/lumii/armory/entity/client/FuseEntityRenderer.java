package com.lumii.armory.entity.client;

import com.lumii.armory.Armory;
import com.lumii.armory.entity.FuseEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class FuseEntityRenderer extends EntityRenderer<FuseEntity> {
    private static final Identifier TEXTURE = Armory.id("textures/entity/fuse.png");
    private final FuseEntityModel model;

    public FuseEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.model = new FuseEntityModel(ctx.getPart(FuseEntityModel.MODEL_LAYER));
        this.shadowRadius = 0.5F;
    }

    @Override
    public Identifier getTexture(FuseEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(FuseEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        float rotationTime = (entity.age + tickDelta) * 10f;
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationTime));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotationTime * 0.5f));

        matrices.translate(0, 0, 0);
        matrices.scale(-1, -1, 1);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.model.getLayer(this.getTexture(entity)));
        this.model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}