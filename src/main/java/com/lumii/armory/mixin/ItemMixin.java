package com.lumii.armory.mixin;

import com.lumii.armory.item.discontinuity.DiscontinuityItem;
import com.lumii.armory.item.discontinuity.tooltip.VariantTooltipData;
import com.lumii.armory.registry.ArmoryItemRegistry;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "getTooltipData", at = @At("HEAD"), cancellable = true)
    private void injectTootip(ItemStack stack, CallbackInfoReturnable<Optional<TooltipData>> cir) {
        if (stack.isOf(ArmoryItemRegistry.DISCONTINUITY)) {
            cir.setReturnValue(Optional.of(new VariantTooltipData(stack)));
        }
    }
}
