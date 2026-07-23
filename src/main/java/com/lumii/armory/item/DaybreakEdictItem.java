package com.lumii.armory.item;

import com.lumii.armory.Armory;
import com.lumii.armory.packets.ParticleSpawnPacket;
import com.lumii.armory.registry.ArmoryDamageRegistry;
import com.lumii.armory.registry.ArmoryPackets;
import com.lumii.armory.registry.ArmorySoundsRegistry;
import com.lumii.armory.util.ChainEntityUtils;
import com.lumii.armory.util.time.TickSchedulerServer;
import com.lumii.armory.util.visual.QuadRenderer;
import io.netty.buffer.Unpooled;
import net.chemthunder.lux.api.LuxFlashRenderer;
import net.chemthunder.reflect.api.ReflectPlugin;
import net.chemthunder.reflect.api.interfaces.SimpleModelItem;
import net.chemthunder.reflect.api.presets.ReflectModelPresets;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import silly.chemthunder.ozone.api.thingies.CustomBipedEntityModelPoseItem;
import team.lodestar.lodestone.handlers.ScreenshakeHandler;
import team.lodestar.lodestone.network.screenshake.ScreenshakePacket;
import team.lodestar.lodestone.systems.screenshake.ScreenshakeInstance;

import java.awt.*;

public class DaybreakEdictItem extends SwordItem implements SimpleModelItem, CustomBipedEntityModelPoseItem {
    public DaybreakEdictItem(ToolMaterial toolMaterial, Settings settings) {
        super(toolMaterial, 7, -3f, settings);
    }

    @Override
    public ReflectPlugin getPlugin() {
        return Armory.BASE;
    }

    @Override
    public String primaryModel() {
        return "daybreak_edict";
    }

    @Override
    public String secondaryModel() {
        return "daybreak_edict_gui";
    }

    @Override
    public ReflectModelPresets getPreset(ItemStack itemStack) {
        return ReflectModelPresets.Handheld;
    }

    public Text getName(ItemStack stack){
        return super.getName(stack).copy().styled(style -> style.withColor(Formatting.GOLD));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        target.playSound(ArmorySoundsRegistry.SLASH, 1, 1);

        if (!ChainEntityUtils.isChained(target)
                && target.getHealth() <= 4.0F) {

            target.setHealth(2.0F);
            ChainEntityUtils.setChained(target, true);
            return true;
        }

        if (ChainEntityUtils.isChained(target)) {
            target.damage(ArmoryDamageRegistry.daybreak(target), Float.MAX_VALUE);
            if (!target.getWorld().isClient() && target != null) {
                Vec3d targetPos = target.getPos().add(0, 1.6, 0);
                Color startColor = new Color(0x967A49);
                Color endColor = new Color(0x090700);
                ParticleSpawnPacket packet = new ParticleSpawnPacket(targetPos.addRandom(Random.create(), 1), startColor.getRGB(), endColor.getRGB());
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                packet.toBytes(buf);

                ServerPlayerEntity srPlayer = (ServerPlayerEntity) attacker;
                if (srPlayer != null) {
                    int delayTicks = 20;
                    for (ServerPlayerEntity player : srPlayer.getServerWorld().getPlayers()) {
                        QuadRenderer.scheduleCommon(player.getServerWorld(), targetPos.subtract(0, 1.499, 0), 1, 1,
                                new Vec3d(90, 0, 0), 4 ,Armory.id("textures/vfx/grd.png"), delayTicks, false, 1,
                                false, 1, 1,
                                1, QuadRenderer.SpinAxis.Z, 5f);
                    }
                    TickSchedulerServer.schedule(delayTicks, () -> {
                        // I wish i didn't look at that thing
                        for (int i = 0; i < 15; i++) {
                            ServerPlayNetworking.send(srPlayer, ArmoryPackets.PARTICLE_SPAWN_ID, buf);
                        }
                        QuadRenderer.scheduleCommon(srPlayer.getServerWorld(),
                                targetPos,
                                1, 1,
                                new Vec3d(0, 0, 0), 4,
                                Armory.id("textures/vfx/circ.png"),
                                20 * 10,
                                true, 1,
                                true, 1, 450,
                                0.75f);

                        QuadRenderer.scheduleCommon(srPlayer.getServerWorld(),
                                targetPos,
                                1, 1,
                                new Vec3d(0, 90, 0), 4,
                                Armory.id("textures/vfx/circ.png"),
                                20 * 10,
                                true, 1,
                                true, 1, 450,
                                0.75f);

                        for (ServerPlayerEntity player : srPlayer.getServerWorld().getPlayers()) {
                            LuxFlashRenderer.sendFlash(player, new Color(255, 219, 136, 62).getRGB());
                            ServerPlayNetworking.send(srPlayer, ArmoryPackets.SHAKE_ID, PacketByteBufs.empty());
                        }

                        ChainEntityUtils.setChained(target, false);
                    });
                }
            }
        }

        return super.postHit(stack, target, attacker);
    }

    @Override
    public BipedEntityModel.ArmPose getArmPose(ItemStack itemStack, PlayerEntity playerEntity) {
        return BipedEntityModel.ArmPose.CROSSBOW_CHARGE;
    }
}
