package com.lumii.armory.mixin;

import com.lumii.armory.util.ChainClientTracker;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "tickNewAi", at = @At("HEAD"))
    private void disableMovement(CallbackInfo ci) {
        if (ChainClientTracker.isChained()) {
            ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

            // Why did i do this i hate myself because of this code now

            player.sidewaysSpeed = 0.0F;
            player.forwardSpeed = 0.0F;
            player.input.jumping = false;
            player.input.sneaking = false;
            player.input.movementForward = 0.0F;
            player.input.movementSideways = 0.0F;
            player.input.pressingLeft = false;
            player.input.pressingRight = false;
            player.input.pressingForward = false;
            player.input.pressingBack = false;
        }
    }
}