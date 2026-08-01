package com.lumii.armory.mixin;

import com.lumii.armory.util.ChainClientTracker;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Shadow
    private int activeButton;

    @Inject(method = "updateMouse", at = @At("HEAD"), cancellable = true)
    private void cancel(CallbackInfo ci) {
        if (ChainClientTracker.isChained()) {
            ci.cancel();
        }
    }

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void cancelButton(CallbackInfo ci) {
        if (ChainClientTracker.isChained()) {
            this.activeButton = -1;
            ci.cancel();
        }
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void cancelScroll(CallbackInfo ci) {
        if (ChainClientTracker.isChained()) {
            ci.cancel();
        }
    }
}