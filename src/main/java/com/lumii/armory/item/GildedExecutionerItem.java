package com.lumii.armory.item;

import com.lumii.armory.Armory;
import com.lumii.armory.packets.ParticleSpawnPacket;
import com.lumii.armory.registry.ArmoryDamageRegistry;
import com.lumii.armory.registry.ArmoryPackets;
import com.lumii.armory.registry.ArmorySoundsRegistry;
import com.lumii.armory.util.ChainEntityUtils;
import com.lumii.armory.util.time.TickSchedulerServer;
import com.lumii.armory.util.visual.QuadRenderer;
import com.lumii.armory.vfx.DivinityDissonanceHandler;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import org.carpentry.citrine.api.client.flash.FlashEasingType;
import org.carpentry.citrine.api.client.flash.ScreenFlashManager;
import org.carpentry.citrine.api.item.CustomHeldPoseItem;
import org.carpentry.citrine.api.item.VaryingModelItem;
import org.carpentry.citrine.api.util.TimeUtils;

import org.carpentry.citrine.api.util.model.ModelPresets;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class GildedExecutionerItem extends SwordItem implements VaryingModelItem, CustomHeldPoseItem {
    public GildedExecutionerItem(ToolMaterial toolMaterial, Settings settings) {
        super(toolMaterial, 7, -3f, settings);
    }

    public Color FLASH_COL = new Color(243, 207, 117, 255);
    public Identifier GUI = new Identifier("armory", "daybreak_edict_gui");
    public Identifier MAIN = new Identifier("armory", "daybreak_edict");

    public Text getName(ItemStack stack){
        return super.getName(stack).copy().styled(style -> style.withColor(Formatting.GOLD));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {;

        target.getWorld().playSound(
                null,
                target.getBlockPos(),
                ArmorySoundsRegistry.SLASH,
                SoundCategory.NEUTRAL,
                1,
                1
        );

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

    @Override
    public List<Identifier> getModels() {
        return List.of(
                GUI
        );
    }

    @Override
    public @Nullable Identifier getModel(ItemStack itemStack, @Nullable LivingEntity livingEntity, ModelTransformationMode mode) {
        return ModelPresets.handheld(mode, GUI);
    }
}
