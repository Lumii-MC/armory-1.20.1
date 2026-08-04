package com.lumii.armory;

import com.lumii.armory.cca.ModComponents;
import com.lumii.armory.registry.ArmoryPackets;
import com.lumii.armory.util.ChainClientTracker;
import com.lumii.armory.util.time.TickSchedulerClient;
import com.lumii.armory.util.visual.CubeRenderer;
import com.lumii.armory.util.visual.QuadRenderer;
import com.lumii.armory.vfx.beam.BeamPost;
import com.lumii.armory.vfx.distort.DistortionPost;
import com.lumii.armory.vfx.beam.BeamPostHandler;
import com.lumii.armory.vfx.mark.MarkedPost;
import com.lumii.armory.vfx.mark.shockwave.ShockwavePost;
import com.lumii.armory.vfx.mark.shockwave.ShockwavePostHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import team.lodestar.lodestone.systems.postprocess.PostProcessHandler;

public class ArmoryClient implements ClientModInitializer {
    public void onInitializeClient() {
        ArmoryPackets.initClient();
        TickSchedulerClient.init();
        QuadRenderer.init();
        CubeRenderer.init();

        // Single-instance post
        PostProcessHandler.addInstance(DistortionPost.INSTANCE);
        PostProcessHandler.addInstance(MarkedPost.INSTANCE);

        DistortionPost.INSTANCE.setActive(false);
        MarkedPost.INSTANCE.setActive(false);

        // Instanced post
        PostProcessHandler.addInstance(ShockwavePostHandler.INSTANCE);
        PostProcessHandler.addInstance(BeamPostHandler.INSTANCE);

        // dev test commands
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
                dispatcher.register(ClientCommandManager.literal("devAddBeam")
                        .executes(context -> {
                            var instance = new BeamPost(MinecraftClient.getInstance().player.getEyePos().add(MinecraftClient.getInstance().player.getRotationVecClient().multiply(7)));
                            instance = BeamPostHandler.INSTANCE.addFxInstance(instance);
                            BeamPost finalInstance = instance;
                            TickSchedulerClient.schedule((int) (5.5*20), () -> {
                                if (finalInstance != null) finalInstance.remove();
                            });
                            return 1;
                        })
                );
            });
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
                dispatcher.register(ClientCommandManager.literal("devAddShockwave")
                        .executes(context -> {
                            var instance = new ShockwavePost(MinecraftClient.getInstance().player.getEyePos().add(MinecraftClient.getInstance().player.getRotationVecClient().multiply(7)), 1);
                            instance = ShockwavePostHandler.INSTANCE.addFxInstance(instance);
                            ShockwavePost finalInstance = instance;
                            TickSchedulerClient.schedule((int) (3.5*20), () -> {
                                if (finalInstance != null) finalInstance.remove();
                            });
                            return 1;
                        })
                );
            });
        }

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
