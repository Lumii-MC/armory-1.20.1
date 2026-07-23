package com.lumii.armory.registry;

import com.lumii.armory.Armory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public interface ArmoryDamageRegistry {

    RegistryKey<DamageType> DAYBREAK = of("daybreak");

    static DamageSource daybreak(LivingEntity entity) {
        return entity.getDamageSources().create(DAYBREAK); }

    private static RegistryKey<DamageType> of(String name) {
        return RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Armory.id(name));
    }
}
