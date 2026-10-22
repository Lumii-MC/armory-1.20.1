package com.lumii.armory.item.discontinuity;

import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.nbt.NbtCompound;

public class DiscontinuityItem extends SwordItem {
    public DiscontinuityItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    public static void setVariantData(NbtCompound nbt, VariantData data) {

    }
}
