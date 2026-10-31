package com.lumii.armory.registry;

import com.lumii.armory.Armory;
import com.lumii.armory.enchant.publicguillotine.HookEnchant;
import com.lumii.armory.enchant.publicguillotine.NukeEnchant;
import com.lumii.armory.enchant.publicguillotine.ReapEnchant;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ArmoryEnchantRegistry {
    public static Enchantment registerEnch(Enchantment ench, String id) {return Registry.register(Registries.ENCHANTMENT, Armory.id(id), ench);}

    public static final Enchantment REAP = registerEnch(new ReapEnchant(Enchantment.Rarity.UNCOMMON, EnchantmentTarget.WEAPON, EquipmentSlot.values()), "reap");
    public static final Enchantment HOOK = registerEnch(new HookEnchant(Enchantment.Rarity.UNCOMMON, EnchantmentTarget.WEAPON, EquipmentSlot.values()), "hook");

    public static final Enchantment NUKE = registerEnch(new NukeEnchant(Enchantment.Rarity.UNCOMMON, EnchantmentTarget.WEAPON, EquipmentSlot.values()), "nuke");

    public static void init() {}
}
