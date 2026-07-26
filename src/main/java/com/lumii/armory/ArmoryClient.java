package com.lumii.armory;

import com.lumii.armory.registry.ArmoryPackets;
import com.lumii.armory.util.ChainClientTracker;
import com.lumii.armory.util.time.TickSchedulerClient;
import com.lumii.armory.util.visual.CubeRenderer;
import com.lumii.armory.util.visual.QuadRenderer;
import com.lumii.armory.vfx.DistortionPost;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.text.Text;
import team.lodestar.lodestone.systems.postprocess.PostProcessHandler;

public class ArmoryClient implements ClientModInitializer {
    public void onInitializeClient() {
        ArmoryPackets.initClient();
        TickSchedulerClient.init();
        QuadRenderer.init();
        CubeRenderer.init();

        PostProcessHandler.addInstance(DistortionPost.INSTANCE);
        DistortionPost.INSTANCE.setActive(false);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {

            dispatcher.register(ClientCommandManager.literal("distortionToggle")
                    .executes(context -> {
                        DistortionPost.INSTANCE.setActive(!DistortionPost.INSTANCE.isActive());
                        context.getSource().sendFeedback(Text.literal("Set the shader to: " + DistortionPost.INSTANCE.isActive()));
                        return 1;
                    })
            );

        });
    }
}
