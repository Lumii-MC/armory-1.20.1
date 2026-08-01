package com.lumii.armory.vfx.beam;

import com.lumii.armory.Armory;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.JsonEffectShaderProgram;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import team.lodestar.lodestone.systems.postprocess.MultiInstancePostProcessor;
import team.lodestar.lodestone.systems.postprocess.PostProcessHandler;

public class BeamPostHandler extends MultiInstancePostProcessor<BeamPost> {
    public static final BeamPostHandler INSTANCE = new BeamPostHandler();
    private JsonEffectShaderProgram beam;
    @Override
    protected int getMaxInstances() {
        return 16;
    }

    @Override
    protected int getDataSizePerInstance() {
        return 4;
    }

    @Override
    public Identifier getPostChainLocation() {
        return Armory.id("beam");
    }

    @Override
    public void afterProcess() {

    }

    @Override
    public void init() {
        super.init();
        if (postChain != null) {
            beam = effects[0];
        }
    }

    @Override
    public void beforeProcess(MatrixStack viewModelStack) {
        super.beforeProcess(viewModelStack);

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

        setDataBufferUniform(beam, "DataBuffer", "InstanceCount");
    }
}
