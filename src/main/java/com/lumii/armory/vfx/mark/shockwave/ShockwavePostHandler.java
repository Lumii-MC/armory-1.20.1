package com.lumii.armory.vfx.mark.shockwave;

import com.lumii.armory.Armory;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.JsonEffectShaderProgram;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import team.lodestar.lodestone.systems.postprocess.MultiInstancePostProcessor;

public class ShockwavePostHandler extends MultiInstancePostProcessor<ShockwavePost> {
    public static final ShockwavePostHandler INSTANCE = new ShockwavePostHandler();
    private JsonEffectShaderProgram shockwaveFx;

    @Override
    protected int getMaxInstances() {
        return 16;
    }

    @Override
    protected int getDataSizePerInstance() {
        return 5;
    }

    @Override
    public Identifier getPostChainLocation() {
        return Armory.id("shock");
    }

    @Override
    public void init() {
        super.init();
        if (postChain != null) {
            shockwaveFx = effects[0];
        }
    }

    @Override
    public void afterProcess() {

    }

    @Override
    public void beforeProcess(MatrixStack viewModelStack) {
        Matrix4f viewMatrix = new Matrix4f(viewModelStack.peek().getPositionMatrix());
        Matrix4f projMatrix = new Matrix4f(RenderSystem.getProjectionMatrix());

        if (this.effects != null) {
            for (JsonEffectShaderProgram effect : this.effects) {
                GlUniform viewUniform = effect.getUniformByName("ViewMat");
                if (viewUniform != null) {
                    viewUniform.set(viewMatrix);
                }

                GlUniform projUniform = effect.getUniformByName("ProjMat");
                if (projUniform != null) {
                    projUniform.set(projMatrix);
                }
            }
        }

        super.beforeProcess(viewModelStack);
        setDataBufferUniform(shockwaveFx, "DataBuffer", "InstanceCount");
    }
}
