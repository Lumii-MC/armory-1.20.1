package com.lumii.armory.mixin;

import com.lumii.armory.item.discontinuity.VariantData;
import com.lumii.armory.item.discontinuity.tooltip.DiscontinuityTooltipComponent;
import com.lumii.armory.item.discontinuity.tooltip.VariantTooltipData;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TooltipComponent.class)
public interface TooltipComponentMixin {
    @Inject(method = "of(Lnet/minecraft/client/item/TooltipData;)Lnet/minecraft/client/gui/tooltip/TooltipComponent;", at = @At("HEAD"), cancellable = true)
    private static void inject(TooltipData data, CallbackInfoReturnable<TooltipComponent> cir) {
        if (data instanceof VariantTooltipData dat) {
            cir.setReturnValue(new DiscontinuityTooltipComponent(dat.stack()));
        }
    }
}
