package com.lumii.armory.mixin;

import com.lumii.armory.util.ChainEntityUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void chainEntity(Vec3d movementInput, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;

        if (ChainEntityUtils.isChained(self)) {
            ci.cancel();
        }
    }
}
