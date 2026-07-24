package com.lumii.armory.mixin;

import com.lumii.armory.util.ChainEntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "getVelocity", at = @At("HEAD"), cancellable = true)
    public void getVelocity(CallbackInfoReturnable<Vec3d> cir) {
        if ((Entity)(Object)this instanceof LivingEntity livingEntity) {
            if (ChainEntityUtils.isChained(livingEntity)) {
                cir.setReturnValue(new Vec3d(0, 0, 0));
            }
        }
    }

}
