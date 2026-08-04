package com.lumii.armory.postkill;

import com.lumii.armory.Armory;
import com.lumii.armory.cca.ModComponents;
import com.lumii.armory.item.DeathMarkItem;
import com.lumii.armory.registry.ArmoryDamageRegistry;
import com.lumii.armory.registry.ArmoryItemRegistry;
import com.lumii.armory.registry.ArmoryPackets;
import com.lumii.armory.util.ChainEntityUtils;
import com.lumii.armory.util.time.TickSchedulerServer;
import com.lumii.armory.util.time.TimeUtils;
import com.lumii.armory.vfx.mark.MarkedPost;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.GameMode;

public class MarkKillHandler {

    public static void init() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (!(entity instanceof ServerPlayerEntity player)) {
                return true;
            }
            var component = ModComponents.MARKED.get(player);
            // if already marked, remove
            if (component.getValue()) {
                removeMark(player);
                return false;
            }
            // if carrying the mark, apply it
            if (player.getInventory().contains(new ItemStack(ArmoryItemRegistry.DEATH_MARK))) {
                applyMark(player);
                return false;
            }
            // checks if killed by someone who has it
            Entity attacker = damageSource.getAttacker();
            if (attacker instanceof LivingEntity living &&
                    living.getOffHandStack().getItem() instanceof DeathMarkItem) {
                applyMark(player);
                return false;
            }
            return true;
        });
    }

    private static void applyMark(ServerPlayerEntity player) {
        var component = ModComponents.MARKED.get(player);
        component.setValue(true);
        ModComponents.MARKED.sync(player);
        player.setHealth(1);
        var buf = PacketByteBufs.create();
        buf.writeBoolean(true);
        ServerPlayNetworking.send(player, ArmoryPackets.MARKED_SHADER_STATUS_ID, buf);
        Armory.LOGGER.info("{} was marked.", player.getName().getString());
    }

    private static void removeMark(ServerPlayerEntity player) {
        var component = ModComponents.MARKED.get(player);

        component.setValue(false);
        ModComponents.MARKED.sync(player);
        if (!ChainEntityUtils.isChained(player)){
            ChainEntityUtils.setChained(player, true);
            TickSchedulerServer.schedule(TimeUtils.seconds(5), () -> {
                ChainEntityUtils.setChained(player, false);
                player.damage(ArmoryDamageRegistry.marked(player), Float.MAX_VALUE);
                TickSchedulerServer.schedule(1, () -> {
                    player.changeGameMode(GameMode.SPECTATOR);
                    var buf = PacketByteBufs.create();
                    buf.writeBoolean(false);
                    ServerPlayNetworking.send(player, ArmoryPackets.MARKED_SHADER_STATUS_ID, buf);
                    Box box = player.getBoundingBox().expand(120);
                    var buf1 = PacketByteBufs.create();
                    buf1.writeVector3f(player.getPos().toVector3f());
                    for (PlayerEntity sPlayer : player.getWorld().getEntitiesByType(EntityType.PLAYER, box, PlayerEntity::isPlayer)) {
                        ServerPlayNetworking.send((ServerPlayerEntity) sPlayer, ArmoryPackets.MARK_VFX_ID, buf1);
                    }
                });
            });
        }
        Armory.LOGGER.info("{} got chartered by a mark!", player.getName().getString());
    }
}
