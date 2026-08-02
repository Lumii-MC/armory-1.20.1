package com.lumii.armory.postkill;

import com.lumii.armory.Armory;
import com.lumii.armory.cca.ModComponents;
import com.lumii.armory.item.DeathMarkItem;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

public class MarkKillHandler {
    public static void init() {
        ServerLivingEntityEvents.ALLOW_DEATH.register(((entity, damageSource, damageAmount) -> {
            Entity attacker = damageSource.getAttacker();
            if (attacker instanceof LivingEntity living) {
                if (living.getOffHandStack().getItem() instanceof DeathMarkItem) {
                    if (entity instanceof PlayerEntity player) {
                        var component = ModComponents.MARKED.get(player);
                        if (!component.getValue()) {
                            component.setValue(true);
                            ModComponents.MARKED.sync(player);
                        }
                        entity.setHealth(4);
                    }
                    else return true;
                    return false;
                }
                return true;
            }
            if (entity instanceof PlayerEntity player) {
                var component = ModComponents.MARKED.get(player);
                if (component.getValue()) {
                    Armory.LOGGER.info("{} Got chartered by a mark!", player.getName());
                    component.setValue(false);
                    ModComponents.MARKED.sync(player);
                    player.setHealth(player.getMaxHealth());
                    ((ServerPlayerEntity) player).changeGameMode(GameMode.SPECTATOR);
                    return false;
                }
            }
            return true;
        }));
    }
}
