package com.lumii.armory.postkill;

import com.lumii.armory.Armory;
import com.lumii.armory.cca.ModComponents;
import com.lumii.armory.item.DeathMarkItem;
import com.lumii.armory.registry.ArmoryDamageRegistry;
import com.lumii.armory.registry.ArmoryItemRegistry;
import com.lumii.armory.registry.ArmoryPackets;
import com.lumii.armory.registry.ArmorySoundsRegistry;
import com.lumii.armory.util.ChainEntityUtils;
import com.lumii.armory.util.time.TickSchedulerServer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;

public class MarkKillHandler {

    public static void init() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {

            if (!(entity instanceof ServerPlayerEntity player))
                return true;

            if (source.isOf(ArmoryDamageRegistry.MARKED))
                return true;

            var marked = ModComponents.MARKED.get(player);

            if (marked.getValue()) {
                player.setHealth(2);
                ChainEntityUtils.setChained(player, true);
                TickSchedulerServer.schedule(60, () -> {
                    player.getWorld().playSound(
                            null,
                            player.getBlockPos(),
                            ArmorySoundsRegistry.BIGFUCKINGBEAM,
                            SoundCategory.PLAYERS,
                            1,
                            1
                    );
                });
                TickSchedulerServer.schedule(100, () -> {
                    ChainEntityUtils.setChained(player, false);
                    player.damage(ArmoryDamageRegistry.marked(player), Float.MAX_VALUE);
                    var buf = PacketByteBufs.create();
                    buf.writeBoolean(false);
                    ServerPlayNetworking.send(player, ArmoryPackets.MARKED_SHADER_STATUS_ID, buf);
                    Box box = player.getBoundingBox().expand(120);
                    var buf1 = PacketByteBufs.create();
                    buf1.writeVector3f(player.getPos().toVector3f());
                    for (PlayerEntity sPlayer : player.getWorld().getEntitiesByType(EntityType.PLAYER, box, PlayerEntity::isPlayer)) {
                        ServerPlayNetworking.send((ServerPlayerEntity) sPlayer, ArmoryPackets.MARK_VFX_ID, buf1);
                    }
                    marked.setValue(false);


                });
                ModComponents.MARKED.sync(player);
                var buf = PacketByteBufs.create();
                buf.writeBoolean(false);
                ServerPlayNetworking.send(player, ArmoryPackets.MARKED_SHADER_STATUS_ID, buf);

                return false;
            }

            boolean shouldMark = false;

            Entity attacker = source.getAttacker();

            if (attacker instanceof LivingEntity living &&
                    living.getOffHandStack().getItem() instanceof DeathMarkItem) {

                shouldMark = true;

                if (living instanceof PlayerEntity p && !p.isCreative()) {
                    living.getOffHandStack().decrement(1);
                    living.getWorld().playSound(
                            null,
                            living.getBlockPos(),
                            SoundEvents.ENTITY_ITEM_BREAK,
                            SoundCategory.PLAYERS,
                            0.8f,
                            1f
                    );
                }
            }

            if (!shouldMark &&
                    player.getInventory().contains(new ItemStack(ArmoryItemRegistry.DEATH_MARK))) {

                shouldMark = true;
            }

            if (!shouldMark)
                return true;

            marked.setValue(true);
            ModComponents.MARKED.sync(player);

            player.setHealth(2.0F);

            var buf = PacketByteBufs.create();
            buf.writeBoolean(true);
            ServerPlayNetworking.send(player, ArmoryPackets.MARKED_SHADER_STATUS_ID, buf);

            Armory.LOGGER.info("{} has been marked.", player.getName().getString());

            return false;
        });
    }
}
