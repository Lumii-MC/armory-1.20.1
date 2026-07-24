package com.lumii.armory.postkill;

import com.lumii.armory.Armory;
import com.lumii.armory.item.DivinityDissonanceItem;
import com.lumii.armory.util.ChainEntityUtils;
import com.lumii.armory.util.time.TickSchedulerServer;
import com.lumii.armory.util.time.TimeUtils;
import com.lumii.armory.vfx.DivinityDissonanceHandler;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public class DissonanceKillHandler {
    public static void init() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((victim, damageSource, damageAmount) -> {
            Entity attacker = damageSource.getAttacker();

            if (attacker instanceof LivingEntity player) {
                ItemStack offHand = player.getOffHandStack();
                if (offHand.getItem() instanceof DivinityDissonanceItem) {
                    if (attacker instanceof PlayerEntity entity) {
                        if (!entity.isCreative()) {
                            offHand.decrement(1);
                            attacker.getWorld().playSound(null, attacker.getBlockPos(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 0.8f, 1f);
                        }
                    }
                    else {
                        offHand.decrement(1);
                        attacker.getWorld().playSound(null, attacker.getBlockPos(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 0.8f, 1f);
                    }
                    DivinityDissonanceHandler.addEffect(victim);
                    Armory.LOGGER.info("Killed {} by {}", victim.getEntityName(), attacker.getEntityName());
                    // TODO: Lumii add the kill logic please
                }
            }
            return false;
        });
    }
}
