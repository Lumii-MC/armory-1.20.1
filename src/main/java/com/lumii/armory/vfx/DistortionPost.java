package com.lumii.armory.vfx;

import com.lumii.armory.Armory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import team.lodestar.lodestone.systems.postprocess.PostProcessor;

// To turn it on and off use INSTANCE.setActive(bool);
public class DistortionPost extends PostProcessor {
    public static final DistortionPost INSTANCE = new DistortionPost();

    @Override
    public Identifier getPostChainLocation() {
        return Armory.id("distortion");
    }

    @Override
    public void beforeProcess(MatrixStack viewModelStack) {

    }

    @Override
    public void afterProcess() {

    }
}
