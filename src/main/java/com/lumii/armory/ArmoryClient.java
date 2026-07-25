package com.lumii.armory;

import com.lumii.armory.registry.ArmoryPackets;
import com.lumii.armory.util.ChainClientTracker;
import com.lumii.armory.util.time.TickSchedulerClient;
import com.lumii.armory.util.visual.CubeRenderer;
import com.lumii.armory.util.visual.QuadRenderer;
import com.lumii.armory.vfx.DistortionPost;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import team.lodestar.lodestone.systems.postprocess.PostProcessHandler;

public class ArmoryClient implements ClientModInitializer {
    public void onInitializeClient() {
        ArmoryPackets.initClient();
        TickSchedulerClient.init();
        QuadRenderer.init();
        CubeRenderer.init();

        PostProcessHandler.addInstance(DistortionPost.INSTANCE);
        DistortionPost.INSTANCE.setActive(false);
    }
}
