package com.lumii.armory.enchant.publicguillotine;

import com.lumii.armory.registry.ArmoryItemRegistry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class NukeEnchant extends Enchantment {
    public NukeEnchant(Rarity weight, EnchantmentTarget target, EquipmentSlot[] slotTypes) {
        super(weight, target, slotTypes);
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.isOf(ArmoryItemRegistry.PYROTECHNIC);
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
