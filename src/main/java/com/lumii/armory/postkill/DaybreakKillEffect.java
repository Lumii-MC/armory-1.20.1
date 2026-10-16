package com.lumii.armory.postkill;

import com.lumii.armory.item.DivinityDissonanceItem;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class DaybreakKillEffect {
    public static void init() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((server, entity, killed) -> {
            if (entity instanceof LivingEntity attacker) {
                ItemStack stack = attacker.getOffHandStack();
                if (stack.getItem() instanceof DivinityDissonanceItem) {
                    // TODO: Lumii add the fucking permakill please
                    // ykw no, figure it out yourself!!!!!! /silly :3
                    // bitch fym no its your mod 😭
                }
            }
        });
    }
}
