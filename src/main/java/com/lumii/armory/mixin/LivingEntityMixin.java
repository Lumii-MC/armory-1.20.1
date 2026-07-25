package com.lumii.armory.mixin;

import com.lumii.armory.item.GildedExecutionerItem;
import com.lumii.armory.registry.ArmoryDamageRegistry;
import com.lumii.armory.util.ChainEntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

//    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
//    private void chainEntity(Vec3d movementInput, CallbackInfo ci) {
//        LivingEntity self = (LivingEntity)(Object)this;
//
//        if (ChainEntityUtils.isChained(self)) {
//            ci.cancel();
//        }
//    }

    @Shadow
    private float movementSpeed;

    @Inject(method = "tickMovement", at = @At("HEAD"), cancellable = true)
    private void tickMovement(CallbackInfo ci) {
        if (ChainEntityUtils.isChained((LivingEntity)(Object)this)) {
            // the most scuffed way this could be achieved but idc
            this.movementSpeed = 0;
            ci.cancel();
        }
    }

    @Inject(method = "isDead", at = @At("HEAD"), cancellable = true)
    private void isDead(CallbackInfoReturnable<Boolean> cir) {
        if (ChainEntityUtils.isChained((LivingEntity)(Object)this)) {
            // ugly ahh code
            // fym "ugly ahh code" 😭
            cir.setReturnValue(false);
        }
    }

    @ModifyVariable(
            method = "damage",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private DamageSource replaceDamageSource(DamageSource source) {
        Entity attacker = source.getAttacker();

        if (attacker instanceof LivingEntity living
                && living.getMainHandStack().getItem() instanceof GildedExecutionerItem) {
            return ArmoryDamageRegistry.daybreak((LivingEntity)(Object)this);
        }
        return source;
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void cancelDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (ChainEntityUtils.isChained((LivingEntity)(Object)this)) {
            cir.setReturnValue(false);
        }
    }
}
