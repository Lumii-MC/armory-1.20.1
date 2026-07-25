package com.lumii.armory.postkill;

import com.lumii.armory.Armory;
import com.lumii.armory.item.DivinityDissonanceItem;
import com.lumii.armory.item.DivinityLauncherItem;
import com.lumii.armory.registry.ArmoryDamageRegistry;
import com.lumii.armory.registry.ArmoryPackets;
import com.lumii.armory.registry.ArmorySoundsRegistry;
import com.lumii.armory.util.ChainEntityUtils;
import com.lumii.armory.util.time.TickSchedulerServer;
import com.lumii.armory.util.time.TimeUtils;
import com.lumii.armory.vfx.DivinityDissonanceHandler;
import net.chemthunder.lux.api.LuxFlashRenderer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class DissonanceKillHandler {
    public static void init() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, attacker, victim) -> {

            if (attacker instanceof LivingEntity player) {
                ItemStack offHand = player.getOffHandStack();
                if (offHand.getItem() instanceof DivinityDissonanceItem) {
                    if (attacker instanceof PlayerEntity entity) {
                        if (!entity.isCreative()) {
                            offHand.decrement(1);
                            attacker.getWorld().playSound(null, attacker.getBlockPos(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 0.8f, 1f);
                        }
                        if  (victim.getHealth() <=2){
                            victim.setHealth(2);
                            ChainEntityUtils.setChained(victim, true);
                            TickSchedulerServer.schedule(TimeUtils.seconds(2)+1/2, () -> {
                                ChainEntityUtils.setChained(victim, false);
                                victim.damage(ArmoryDamageRegistry.beam(victim), Float.MAX_VALUE);
                                victim.getServer().sendMessage(Text.literal(victim.getEntityName() + "got fucking chartered"));
                                for (ServerPlayerEntity srplayer : player.getServer().getOverworld().getPlayers()) {
                                    LuxFlashRenderer.sendFlash(srplayer, new Color(243, 207, 117, 255).getRGB());
                                    ServerPlayNetworking.send(srplayer, ArmoryPackets.SHAKE_ID, PacketByteBufs.empty());
                                }
                            });
                        }
                    }
                    DivinityDissonanceHandler.addEffect(victim);
                    world.playSound(
                            null,
                            victim.getBlockPos(),
                            ArmorySoundsRegistry.BIGFUCKINGBEAM,
                            SoundCategory.MASTER,
                            2,
                            1
                    );
                    Armory.LOGGER.info("victim {} by {}", victim.getEntityName(), attacker.getEntityName());
                    // TODO: Lumii add the kill logic please
                    // oh fuck you
                }
            }

            if (attacker instanceof LivingEntity player) {
                ItemStack offHand = player.getOffHandStack();
                if (offHand.getItem() instanceof DivinityLauncherItem) {
                    if (attacker instanceof PlayerEntity entity) {
                        if (!entity.isCreative()) {
                            offHand.decrement(1);
                            attacker.getWorld().playSound(null, attacker.getBlockPos(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 0.8f, 1f);
                        }
                        if  (victim.getHealth() <=2){
                            victim.setHealth(2);
                            ChainEntityUtils.setChained(victim, true);
                            TickSchedulerServer.schedule(TimeUtils.seconds(2)+1/2, () -> {
                                victim.addVelocity(new Vec3d(600, 600, 600));
                                victim.velocityModified = true;
                                ChainEntityUtils.setChained(victim, false);
                                victim.getServer().sendMessage(Text.literal(victim.getEntityName() + "got fucking chartered"));
                                for (ServerPlayerEntity srplayer : player.getServer().getOverworld().getPlayers()) {
                                    LuxFlashRenderer.sendFlash(srplayer, new Color(243, 207, 117, 255).getRGB());
                                    ServerPlayNetworking.send(srplayer, ArmoryPackets.SHAKE_ID, PacketByteBufs.empty());
                                }
                            });
                        }
                    }
                    DivinityDissonanceHandler.addEffect(victim);
                    world.playSound(
                            null,
                            victim.getBlockPos(),
                            ArmorySoundsRegistry.BIGFUCKINGBEAM,
                            SoundCategory.MASTER,
                            2,
                            1
                    );
                    Armory.LOGGER.info("victim {} by {}", victim.getEntityName(), attacker.getEntityName());
                }
            }
        });
    }
}
