package com.lumii.armory.item;

import com.lumii.armory.Armory;
import com.lumii.armory.registry.ArmorySoundsRegistry;
import com.lumii.armory.util.time.TickSchedulerServer;
import com.lumii.armory.util.visual.QuadRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.carpentry.citrine.api.client.flash.FlashEasingType;
import org.carpentry.citrine.api.client.flash.ScreenFlashManager;
import org.carpentry.citrine.api.util.TimeUtils;

import java.awt.*;
import java.util.UUID;

public class WorldRemoteItem extends Item {
    public WorldRemoteItem(Settings settings) {
        super(settings);
    }
    private static final UUID AZMODIAN_UUID = UUID.fromString("5622cd73-ce65-4977-9fab-8d7e3e89d98a");

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();

        if (!world.isClient && player instanceof ServerPlayerEntity srPlayer) {
            ServerWorld serverWorld = srPlayer.getServerWorld();
            int delayTicks = TimeUtils.seconds(2);
            Vec3d quadPos = pos.toCenterPos().add(0, 0.51, 0);

            for (ServerPlayerEntity sPlayer : serverWorld.getEntitiesByClass(ServerPlayerEntity.class, srPlayer.getBoundingBox().expand(120), ServerPlayerEntity::isAlive)) {
                QuadRenderer.scheduleCommon(sPlayer.getServerWorld(), quadPos, 1F, 1F,
                        new Vec3d(90, 0, 0), 1, Armory.id("textures/vfx/remote_target.png"), delayTicks, false, 1,
                        true, 1, 20f,
                        1, QuadRenderer.SpinAxis.Z, 5f);
            }

            TickSchedulerServer.schedule(delayTicks, () -> {
                for (ServerPlayerEntity sPlayer : serverWorld.getPlayers()) {
                    ScreenFlashManager.addFlash(TimeUtils.seconds(3.5f), new Color(0xc93426), FlashEasingType.LINEAR);
                }

                serverWorld.playSound(null, pos, ArmorySoundsRegistry.EXECUTION,
                        SoundCategory.PLAYERS, 1, 1);

                ServerPlayerEntity target = serverWorld.getServer().getPlayerManager().getPlayer(AZMODIAN_UUID);
                if (target != null) {
                    target.teleport(serverWorld,
                            pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                            target.getYaw(), target.getPitch());
                }
            });
        }
        return super.useOnBlock(context);
    }
}
