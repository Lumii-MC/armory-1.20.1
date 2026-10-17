package com.lumii.armory.enchant.publicguillotine;

import com.lumii.armory.registry.ArmoryEnchantRegistry;
import com.lumii.armory.registry.ArmoryItemRegistry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class ReapEnchant extends Enchantment {
    public ReapEnchant(Rarity weight, EnchantmentTarget target, EquipmentSlot[] slotTypes) {
        super(weight, target, slotTypes);
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.isOf(ArmoryItemRegistry.GUILLOTINE_PUB);
    }

    @Override
    protected boolean canAccept(Enchantment other) {
        return super.canAccept(other) && !other.equals(ArmoryEnchantRegistry.HOOK);
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
