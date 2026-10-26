package com.lumii.armory.mixin;

import com.lumii.armory.item.discontinuity.DiscontinuityItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {
    @Shadow @Final public DefaultedList<Slot> slots;

    @Inject(method = "internalOnSlotClick", at = @At("HEAD"), cancellable = true)
    private void armory$cycleSelectedPart(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (actionType != SlotActionType.QUICK_MOVE || button != 1) return;
        if (slotIndex < 0 || slotIndex >= slots.size()) return;

        ItemStack stack = slots.get(slotIndex).getStack();
        if (!(stack.getItem() instanceof DiscontinuityItem)) return;

        DiscontinuityItem.cycleSelectedGroup(stack);
        ci.cancel();
    }
}