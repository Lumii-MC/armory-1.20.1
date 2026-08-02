package com.lumii.armory.vfx.mark;

import com.lumii.armory.Armory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import team.lodestar.lodestone.systems.postprocess.PostProcessor;

// To turn it on and off use INSTANCE.setActive(bool);
public class MarkedPost extends PostProcessor {
    public static final MarkedPost INSTANCE = new MarkedPost();

    @Override
    public Identifier getPostChainLocation() {
        return Armory.id("marked");
    }

    @Override
    public void beforeProcess(MatrixStack viewModelStack) {

    }

    @Override
    public void afterProcess() {

    }
}