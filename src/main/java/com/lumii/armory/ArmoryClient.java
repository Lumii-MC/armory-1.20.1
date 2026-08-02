package com.lumii.armory;

import com.lumii.armory.cca.ModComponents;
import com.lumii.armory.registry.ArmoryPackets;
import com.lumii.armory.util.ChainClientTracker;
import com.lumii.armory.util.time.TickSchedulerClient;
import com.lumii.armory.util.visual.CubeRenderer;
import com.lumii.armory.util.visual.QuadRenderer;
import com.lumii.armory.vfx.distort.DistortionPost;
import com.lumii.armory.vfx.beam.BeamPostHandler;
import com.lumii.armory.vfx.mark.MarkedPost;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import team.lodestar.lodestone.systems.postprocess.PostProcessHandler;

public class ArmoryClient implements ClientModInitializer {
    public void onInitializeClient() {
        ArmoryPackets.initClient();
        TickSchedulerClient.init();
        QuadRenderer.init();
        CubeRenderer.init();

        PostProcessHandler.addInstance(DistortionPost.INSTANCE);
        PostProcessHandler.addInstance(BeamPostHandler.INSTANCE);
        PostProcessHandler.addInstance(MarkedPost.INSTANCE);
        DistortionPost.INSTANCE.setActive(false);
        MarkedPost.INSTANCE.setActive(false);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("distortionToggle")
                    .executes(context -> {
                        if (!ChainClientTracker.isChained()) {
                            DistortionPost.INSTANCE.setActive(!DistortionPost.INSTANCE.isActive());
                            context.getSource().sendFeedback(Text.literal("Set the shader to: " + DistortionPost.INSTANCE.isActive()));
                        }
                        else {
                            context.getSource().sendFeedback(Text.literal("You cannot toggle the shader while chained."));
                            return 0;
                        }
                        return 1;
                    })
            );
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("markedToggle")
                    .executes(context -> {
                        if (!ModComponents.MARKED.get(MinecraftClient.getInstance().player).getValue()) {
                            MarkedPost.INSTANCE.setActive(!MarkedPost.INSTANCE.isActive());
                            context.getSource().sendFeedback(Text.literal("Set the shader to: " + MarkedPost.INSTANCE.isActive()));
                        }
                        else {
                            context.getSource().sendFeedback(Text.literal("You cannot toggle the shader while marked."));
                            return 0;
                        }
                        return 1;
                    })
            );
        });
    }
}
