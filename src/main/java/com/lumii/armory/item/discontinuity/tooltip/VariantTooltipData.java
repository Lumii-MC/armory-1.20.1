package com.lumii.armory.item.discontinuity.tooltip;

import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;

public record VariantTooltipData(ItemStack stack) implements TooltipData {
}
