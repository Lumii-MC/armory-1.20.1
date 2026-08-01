package com.lumii.armory.mixin;

import com.lumii.armory.util.ChainClientTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.GameOptions;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow @Nullable public ClientPlayerEntity player;
    @Shadow @Nullable public ClientPlayerInteractionManager interactionManager;
    @Shadow @Final public GameOptions options;

    @Inject(method = "handleInputEvents", at = @At("HEAD"), cancellable = true)
    private void cancelInputEvents(CallbackInfo ci) {
        if (ChainClientTracker.isChained()) {
            if (player != null && player.isUsingItem()) {
                if (interactionManager != null) {
                    interactionManager.stopUsingItem(player);
                } else {
                    player.clearActiveItem();
                }
            }

            options.useKey.setPressed(false);
            options.attackKey.setPressed(false);

            ci.cancel();
        }
    }

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void cancelAttack(CallbackInfoReturnable<Boolean> cir) {
        if (ChainClientTracker.isChained()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void cancelItemUse(CallbackInfo ci) {
        if (ChainClientTracker.isChained()) {
            ci.cancel();
        }
    }
}