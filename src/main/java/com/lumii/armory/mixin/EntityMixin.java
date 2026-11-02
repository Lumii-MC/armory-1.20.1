package com.lumii.armory.mixin;

import com.lumii.armory.util.ChainEntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public boolean velocityDirty;

    @Shadow
    public boolean velocityModified;

    @Shadow
    private Vec3d velocity;

    @Shadow
    public float fallDistance;

    @Inject(method = "getVelocity", at = @At("HEAD"), cancellable = true)
    public void getVelocity(CallbackInfoReturnable<Vec3d> cir) {
        if ((Entity)(Object)this instanceof LivingEntity livingEntity) {
            if (ChainEntityUtils.isChained(livingEntity)) {
                cir.setReturnValue(new Vec3d(0, 0, 0));
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void cancelDirt(CallbackInfo ci) {
        if ((Entity)(Object)this instanceof LivingEntity livingEntity) {
            if (ChainEntityUtils.isChained(livingEntity)) {
                this.velocityDirty = false;
                this.velocityModified = false;
                this.velocity = Vec3d.ZERO;
                this.fallDistance = 0;
            }
        }
    }

    @Inject(method = "addVelocity(DDD)V", at = @At("HEAD"), cancellable = true)
    private void cancelVel(double deltaX, double deltaY, double deltaZ, CallbackInfo ci) {
        if ((Entity)(Object)this instanceof LivingEntity livingEntity) {
            if (ChainEntityUtils.isChained(livingEntity)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "addVelocity(Lnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"), cancellable = true)
    private void cancelVel(Vec3d vec, CallbackInfo ci) {
        if ((Entity)(Object)this instanceof LivingEntity livingEntity) {
            if (ChainEntityUtils.isChained(livingEntity)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "updatePosition", at = @At("HEAD"), cancellable = true)
    private void cancel(double x, double y, double z, CallbackInfo ci) {
        if ((Entity)(Object)this instanceof LivingEntity livingEntity) {
            if (ChainEntityUtils.isChained(livingEntity)) ci.cancel();
        }
    }


    @Inject(method = "requestTeleport", at = @At("HEAD"), cancellable = true)
    private void cancelTp(double destX, double destY, double destZ, CallbackInfo ci) {
        if ((Entity)(Object)this instanceof LivingEntity livingEntity) {
            if (ChainEntityUtils.isChained(livingEntity)) ci.cancel();
        }
    }

    @Inject(method = "requestTeleportAndDismount", at = @At("HEAD"), cancellable = true)
    private void cancelTpDismount(double destX, double destY, double destZ, CallbackInfo ci) {
        if ((Entity)(Object)this instanceof LivingEntity livingEntity) {
            if (ChainEntityUtils.isChained(livingEntity)) ci.cancel();
        }
    }
}
