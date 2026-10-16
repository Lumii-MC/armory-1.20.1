package com.lumii.armory.postkill;

import com.lumii.armory.Armory;
import com.lumii.armory.item.DivinityDissonanceItem;
import com.lumii.armory.registry.ArmoryDamageRegistry;
import com.lumii.armory.registry.ArmoryPackets;
import com.lumii.armory.registry.ArmorySoundsRegistry;
import com.lumii.armory.util.ChainEntityUtils;
import com.lumii.armory.util.time.TickSchedulerServer;
import com.lumii.armory.vfx.DivinityDissonanceHandler;
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
import org.carpentry.citrine.api.client.flash.FlashEasingType;
import org.carpentry.citrine.api.client.flash.ScreenFlashManager;
import org.carpentry.citrine.api.util.TimeUtils;

import java.awt.*;

public class DissonanceKillHandler {
    public static Color FLASH_COL = new Color(243, 207, 117, 255);
    public static void init() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((living, source, delta) -> {
            Entity attacker = source.getAttacker();
            if (attacker instanceof LivingEntity player) {
                ItemStack offHand = player.getOffHandStack();
                if (offHand.getItem() instanceof DivinityDissonanceItem) {
                    if (attacker instanceof PlayerEntity entity) {
                        if (!entity.isCreative()) {
                            offHand.decrement(1);
                            attacker.getWorld().playSound(null, attacker.getBlockPos(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 0.8f, 1f);
                        }
                        if  (living.getHealth() <=2){
                            living.setHealth(2);
                            ChainEntityUtils.setChained(living, true);
                            TickSchedulerServer.schedule(TimeUtils.seconds(2), () -> {
                                ChainEntityUtils.setChained(living, false);
                                living.damage(ArmoryDamageRegistry.beam(living), Float.MAX_VALUE);
                                for (ServerPlayerEntity srplayer : player.getWorld().getEntitiesByClass(ServerPlayerEntity.class, living.getBoundingBox().expand(120), ServerPlayerEntity::isAlive)) {
                                    ScreenFlashManager.addFlash(org.carpentry.citrine.api.util.TimeUtils.seconds(3f), FLASH_COL, FlashEasingType.LINEAR);
                                    ServerPlayNetworking.send(srplayer, ArmoryPackets.BEAM_SHAKE_ID, PacketByteBufs.empty());
                                }
                                living.getServer().sendMessage(Text.literal(living.getEntityName() + " got fucking chartered"));
                            });
                            DivinityDissonanceHandler.addEffect(living);
                            living.getWorld().playSound(
                                    null,
                                    living.getBlockPos(),
                                    ArmorySoundsRegistry.BIGFUCKINGBEAM,
                                    SoundCategory.MASTER,
                                    2,
                                    1);
                            return false;
                        }
                    }
                    DivinityDissonanceHandler.addEffect(living);
                    living.getWorld().playSound(
                            null,
                            living.getBlockPos(),
                            ArmorySoundsRegistry.BIGFUCKINGBEAM,
                            SoundCategory.MASTER,
                            2,
                            1
                    );
                    Armory.LOGGER.info("victim {} by {}", living.getEntityName(), attacker.getEntityName());
                }
            }
            return true;
        });
    }
}
