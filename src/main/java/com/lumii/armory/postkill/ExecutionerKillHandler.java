package com.lumii.armory.postkill;

import com.lumii.armory.Armory;
import com.lumii.armory.item.DivinityDissonanceItem;
import com.lumii.armory.item.GildedExecutionerItem;
import com.lumii.armory.packets.ParticleSpawnPacket;
import com.lumii.armory.registry.ArmoryDamageRegistry;
import com.lumii.armory.registry.ArmoryPackets;
import com.lumii.armory.registry.ArmorySoundsRegistry;
import com.lumii.armory.util.ChainEntityUtils;
import com.lumii.armory.util.time.TickSchedulerServer;
import com.lumii.armory.util.visual.QuadRenderer;
import com.lumii.armory.vfx.DivinityDissonanceHandler;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.carpentry.citrine.api.client.flash.FlashEasingType;
import org.carpentry.citrine.api.client.flash.ScreenFlashManager;
import org.carpentry.citrine.api.util.TimeUtils;

import java.awt.Color;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutionerKillHandler {
    private static final Color FLASH_COL = new Color(243, 207, 117, 255);
    private static final Color PARTICLE_START = new Color(0x967A49);
    private static final Color PARTICLE_END = new Color(0x090700);
    private static final int RANGE = 120;


    private static final Set<UUID> FINISHING = ConcurrentHashMap.newKeySet();

    public static void init() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((target, source, amount) -> {
            if (target.getWorld().isClient()) return true;
            if (FINISHING.contains(target.getUuid())) return true;
            if (ChainEntityUtils.isChained(target)) return true;

            if (!(source.getAttacker() instanceof ServerPlayerEntity attacker)) return true;
            if (!(attacker.getMainHandStack().getItem() instanceof GildedExecutionerItem)) return true;

            startExecution(attacker, target);
            return false;
        });
    }

    private static void startExecution(ServerPlayerEntity attacker, LivingEntity target) {
        ServerWorld world = (ServerWorld) target.getWorld();

        target.setHealth(2.0F);
        ChainEntityUtils.setChained(target, true);

        Vec3d targetPos = target.getPos().add(0, 1.6, 0);
        int delayTicks = TimeUtils.seconds(2);


        for (ServerPlayerEntity player : nearbyPlayers(world, target)) {
            QuadRenderer.scheduleCommon(world, targetPos.subtract(0, 1.59, 0), 1F, 1F,
                    new Vec3d(90, 0, 0), 2, Armory.id("textures/vfx/execution_ring.png"), delayTicks, false, 1,
                    true, 1, 10f,
                    1, QuadRenderer.SpinAxis.Z, 5f);
        }


        if (attacker.getOffHandStack().getItem() instanceof DivinityDissonanceItem) {
            for (ServerPlayerEntity player : world.getPlayers()) {
                ScreenFlashManager.addFlash(40, Color.WHITE, FlashEasingType.LINEAR);
                ServerPlayNetworking.send(player, ArmoryPackets.SHAKE_ID, PacketByteBufs.empty());
            }
            world.playSound(null, target.getBlockPos(),
                    ArmorySoundsRegistry.FUCKYOUREARS, SoundCategory.MASTER, 50, 1);
            DivinityDissonanceHandler.addEffect(target);
        }

        TickSchedulerServer.schedule(delayTicks, () -> finishExecution(world, target, targetPos));
    }

    private static void finishExecution(ServerWorld world, LivingEntity target, Vec3d targetPos) {
        if (target.isDead() || target.isRemoved()) {
            ChainEntityUtils.setChained(target, false);
            return;
        }

        List<ServerPlayerEntity> nearby = nearbyPlayers(world, target);


        for (ServerPlayerEntity player : nearby) {
            for (int i = 0; i < 25; i++) {
                ParticleSpawnPacket packet = new ParticleSpawnPacket(
                        targetPos.addRandom(Random.create(), 1), PARTICLE_START.getRGB(), PARTICLE_END.getRGB());
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                packet.toBytes(buf);
                ServerPlayNetworking.send(player, ArmoryPackets.PARTICLE_SPAWN_ID, buf);
            }
        }

        Vec3d[] rotations = { new Vec3d(0, 0, 0), new Vec3d(0, 90, 0), new Vec3d(90, 0, 0) };
        for (Vec3d rot : rotations) {
            QuadRenderer.scheduleCommon(world, targetPos,
                    1, 1, rot, 10,
                    Armory.id("textures/vfx/shockwave.png"),
                    TimeUtils.seconds(3),
                    true, 1,
                    true, 0, 3000,
                    0.75f);
        }


        for (ServerPlayerEntity player : world.getPlayers()) {
            ScreenFlashManager.addFlash(40, FLASH_COL, FlashEasingType.LINEAR);
            ServerPlayNetworking.send(player, ArmoryPackets.SHAKE_ID, PacketByteBufs.empty());
        }


        ChainEntityUtils.setChained(target, false);
        FINISHING.add(target.getUuid());
        try {
            target.damage(ArmoryDamageRegistry.daybreak(target), Float.MAX_VALUE);
        } finally {
            FINISHING.remove(target.getUuid());
        }

        world.playSound(null, target.getBlockPos(),
                ArmorySoundsRegistry.EXECUTION, SoundCategory.MASTER, 2, 1);
    }

    private static List<ServerPlayerEntity> nearbyPlayers(ServerWorld world, LivingEntity around) {
        return world.getEntitiesByClass(ServerPlayerEntity.class,
                around.getBoundingBox().expand(RANGE), ServerPlayerEntity::isAlive);
    }
}
