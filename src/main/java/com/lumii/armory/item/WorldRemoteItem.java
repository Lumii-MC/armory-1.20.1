package com.lumii.armory.item;

import com.lumii.armory.Armory;
import com.lumii.armory.registry.ArmoryPackets;
import com.lumii.armory.registry.ArmorySoundsRegistry;
import com.lumii.armory.util.time.TickSchedulerServer;
import com.lumii.armory.util.visual.QuadRenderer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.carpentry.citrine.api.client.flash.FlashEasingType;
import org.carpentry.citrine.api.util.TimeUtils;

import java.awt.*;
import java.util.UUID;

public class WorldRemoteItem extends Item {
    public WorldRemoteItem(Settings settings) {
        super(settings);
    }
    private static final UUID AZMODIAN_UUID = UUID.fromString("5622cd73-ce65-4977-9fab-8d7e3e89d98a");

    public Text getName(ItemStack stack){
        return super.getName(stack).copy().styled(
                style -> style.withColor(new Color(0xc93426).getRGB()));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();

        if (!world.isClient && player instanceof ServerPlayerEntity srPlayer) {
            ServerWorld serverWorld = srPlayer.getServerWorld();
            int delayTicks = TimeUtils.seconds(2);
            Vec3d quadPos = pos.toCenterPos().add(0, 0.51, 0);
            world.playSound(
                    null,
                    pos,
                    ArmorySoundsRegistry.WORLD_REMOTE,
                    SoundCategory.NEUTRAL,
                    1,
                    1
            );

            for (ServerPlayerEntity sPlayer : serverWorld.getEntitiesByClass(ServerPlayerEntity.class, srPlayer.getBoundingBox().expand(120), ServerPlayerEntity::isAlive)) {
                QuadRenderer.scheduleCommon(sPlayer.getServerWorld(), quadPos, 1F, 1F,
                        new Vec3d(90, 0, 0), 3, Armory.id("textures/vfx/remote_target.png"), 50, false, 1,
                        true, 1, 30f,
                        1, QuadRenderer.SpinAxis.Z, 8f);
            }

            TickSchedulerServer.schedule(delayTicks, () -> {
                PacketByteBuf buf1 = PacketByteBufs.create();
                buf1.writeFloat(TimeUtils.seconds(3.5f));
                buf1.writeInt(new Color(0xc93426).getRGB());
                buf1.writeInt(FlashEasingType.LINEAR.ordinal());
                for (ServerPlayerEntity sPlayer : serverWorld.getPlayers()) {
                    ServerPlayNetworking.send(sPlayer, ArmoryPackets.FLASH_ID, buf1);
                }

                serverWorld.playSound(null, pos, ArmorySoundsRegistry.REMOTE,
                        SoundCategory.PLAYERS, 1, 1);

                ServerPlayerEntity target = serverWorld.getServer().getPlayerManager().getPlayer(AZMODIAN_UUID);
                if (target != null) {
                    target.teleport(serverWorld,
                            pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                            target.getYaw(), target.getPitch());
                }
            });

            TickSchedulerServer.schedule(delayTicks / 2, () -> {
                PacketByteBuf buf2 = PacketByteBufs.create();
                buf2.writeVector3f(quadPos.toVector3f());
                buf2.writeFloat(TimeUtils.seconds(3.5f));
                buf2.writeInt(new Color(0xc93426).getRGB());
                for (ServerPlayerEntity sPlayer : serverWorld.getPlayers()) {
                    ServerPlayNetworking.send(sPlayer, ArmoryPackets.COLORED_BEAM_ID, buf2);
                }
            });
        }
        return super.useOnBlock(context);
    }
}
