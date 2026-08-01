package com.lumii.armory.mixin;

import com.lumii.armory.util.ChainClientTracker;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void cancelButton(CallbackInfo ci) {
        if (ChainClientTracker.isChained()) ci.cancel();
    }

    @Inject(method = "onChar", at = @At("HEAD"), cancellable = true)
    private void cancelType(CallbackInfo ci) {
        if (ChainClientTracker.isChained()) ci.cancel();
    }
}
