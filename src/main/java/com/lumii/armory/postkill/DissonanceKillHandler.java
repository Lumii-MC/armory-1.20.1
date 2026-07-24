package com.lumii.armory.postkill;

import com.lumii.armory.Armory;
import com.lumii.armory.item.DivinityDissonanceItem;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public class DissonanceKillHandler {
    public static void init() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((server, entity, killed) -> {
            if (entity instanceof LivingEntity attacker) {
                ItemStack offHand = attacker.getOffHandStack();
                if (offHand.getItem() instanceof DivinityDissonanceItem) {
                    offHand.decrement(1);
                    attacker.getWorld().playSound(null, attacker.getBlockPos(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 0.8f, 1f);
                    // lumii come on add the permakill
                    Armory.LOGGER.info("Killed " + killed.getEntityName() + " by " + attacker.getEntityName());
                }
            }
        });
    }
}
