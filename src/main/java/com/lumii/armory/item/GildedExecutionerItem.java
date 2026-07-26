package com.lumii.armory.item;

import com.lumii.armory.Armory;
import com.lumii.armory.packets.DissonanceEffectPacket;
import com.lumii.armory.packets.ParticleSpawnPacket;
import com.lumii.armory.registry.ArmoryDamageRegistry;
import com.lumii.armory.registry.ArmoryPackets;
import com.lumii.armory.registry.ArmorySoundsRegistry;
import com.lumii.armory.util.ChainEntityUtils;
import com.lumii.armory.util.time.TickSchedulerServer;
import com.lumii.armory.util.time.TimeUtils;
import com.lumii.armory.util.visual.QuadRenderer;
import com.lumii.armory.vfx.DivinityDissonanceHandler;
import io.netty.buffer.Unpooled;
import net.chemthunder.lux.api.LuxFlashRenderer;
import net.chemthunder.lux.impl.util.Easing;
import net.chemthunder.reflect.api.ReflectPlugin;
import net.chemthunder.reflect.api.interfaces.SimpleModelItem;
import net.chemthunder.reflect.api.presets.ReflectModelPresets;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import silly.chemthunder.ozone.api.thingies.CustomBipedEntityModelPoseItem;

import java.awt.*;
import java.util.List;

public class GildedExecutionerItem extends SwordItem implements SimpleModelItem, CustomBipedEntityModelPoseItem {
    public GildedExecutionerItem(ToolMaterial toolMaterial, Settings settings) {
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
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {;
        target.damage(ArmoryDamageRegistry.daybreak(target), 2);

        target.getWorld().playSound(
                null,
                target.getBlockPos(),
                ArmorySoundsRegistry.SLASH,
                SoundCategory.NEUTRAL,
                1,
                1
        );

        if (!ChainEntityUtils.isChained(target)  && target.getHealth() <= 4.0F) {

            target.setHealth(2.0F);
            ChainEntityUtils.setChained(target, true);
            if (!target.getWorld().isClient() && target != null) {
                Vec3d targetPos = target.getPos().add(0, 1.6, 0);
                Color startColor = new Color(0x967A49);
                Color endColor = new Color(0x090700);
                ParticleSpawnPacket packet = new ParticleSpawnPacket(targetPos.addRandom(Random.create(), 1), startColor.getRGB(), endColor.getRGB());
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                packet.toBytes(buf);

                ServerPlayerEntity srPlayer = (ServerPlayerEntity) attacker;
                if (srPlayer != null) {
                    int delayTicks = TimeUtils.seconds(2);
                    for (ServerPlayerEntity player : srPlayer.getServerWorld().getPlayers()) {
                        QuadRenderer.scheduleCommon(player.getServerWorld(), targetPos.subtract(0, 1.59, 0), 1F, 1F,
                                new Vec3d(90, 0, 0), 2 ,Armory.id("textures/vfx/execution_ring.png"), delayTicks, false, 1,
                                true, 1, 10f,
                                1, QuadRenderer.SpinAxis.Z, 5f);
                    }
                    TickSchedulerServer.schedule((int) delayTicks, () -> {
                        // I wish i didn't look at that thing
                        // LMFAO
                        for (int i = 0; i < 25; i++) {
                            ServerPlayNetworking.send(srPlayer, ArmoryPackets.PARTICLE_SPAWN_ID, buf);
                        }
                        QuadRenderer.scheduleCommon(srPlayer.getServerWorld(),
                                targetPos,
                                1, 1,
                                new Vec3d(0, 0, 0), 10,
                                Armory.id("textures/vfx/shockwave.png"),
                                TimeUtils.seconds(3),
                                true, 1,
                                true, 0, 3000,
                                0.75f);

                        QuadRenderer.scheduleCommon(srPlayer.getServerWorld(),
                                targetPos,
                                1, 1,
                                new Vec3d(0, 90, 0), 10,
                                Armory.id("textures/vfx/shockwave.png"),
                                TimeUtils.seconds(3),
                                true, 1,
                                true, 0, 3000,
                                0.75f);

                        QuadRenderer.scheduleCommon(srPlayer.getServerWorld(),
                                targetPos,
                                1, 1,
                                new Vec3d(90, 0, 0), 10,
                                Armory.id("textures/vfx/shockwave.png"),
                                TimeUtils.seconds(3),
                                true, 1,
                                true, 0, 3000,
                                0.75f);

                        for (ServerPlayerEntity player : srPlayer.getServerWorld().getPlayers()) {
                            LuxFlashRenderer.sendFlash(player, new Color(243, 207, 117, 255).getRGB());
                            ServerPlayNetworking.send(srPlayer, ArmoryPackets.SHAKE_ID, PacketByteBufs.empty());
                        }


                        ChainEntityUtils.setChained(target, false);
                        target.damage(ArmoryDamageRegistry.daybreak(target), Float.MAX_VALUE);

                        target.getWorld().playSound(null,
                                target.getBlockPos(),
                                ArmorySoundsRegistry.EXECUTION,
                                SoundCategory.MASTER,
                                2,
                                1);
                        }
                    );
                }
                if (ChainEntityUtils.isChained(target) && target.isDead()){
                    ChainEntityUtils.setChained(target, false);
                }
                ItemStack offhand = attacker.getOffHandStack();
                if (offhand.getItem() instanceof DivinityDissonanceItem){
                    for (ServerPlayerEntity player : srPlayer.getServerWorld().getPlayers()) {
                        LuxFlashRenderer.sendFlash(player, 0xffffff, Easing.linear,80);
                        ServerPlayNetworking.send(srPlayer, ArmoryPackets.SHAKE_ID, PacketByteBufs.empty());
                    }
                    target.getWorld().playSound(
                            null,
                            target.getBlockPos(),
                            ArmorySoundsRegistry.FUCKYOUREARS,
                            SoundCategory.MASTER,
                            50,
                            1
                    );
                    DivinityDissonanceHandler.addEffect(target);
                }
            }
        }
        return super.postHit(stack, target, attacker);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.armory.executioner").formatted(Formatting.DARK_GRAY));
    }

    @Override
    public BipedEntityModel.ArmPose getArmPose(ItemStack itemStack, PlayerEntity playerEntity) {
        return BipedEntityModel.ArmPose.CROSSBOW_CHARGE;
    }
}
