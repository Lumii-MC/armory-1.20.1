package com.lumii.armory.item;

import com.lumii.armory.entity.FuseEntity;
import com.lumii.armory.registry.ArmoryEnchantRegistry;
import com.lumii.armory.registry.ArmoryEntityRegistry;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.carpentry.citrine.api.item.CustomHeldPoseItem;
import org.carpentry.citrine.api.item.VaryingModelItem;
import org.carpentry.citrine.api.util.model.ModelPresets;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class PyrotechnicItem extends SwordItem implements VaryingModelItem, CustomHeldPoseItem {
    private final Identifier GUI = new Identifier("armory", "pyrotechnic_gui");
    private final String CHARGES_KEY = "charges";
    private final String COOLDOWN_KEY = "cooldown";
    private final String DESTROY_KEY = "destroy";
    private static final int RECHARGE_TICKS = 100;

    public PyrotechnicItem(ToolMaterial toolMaterial, Settings settings) {
        super(toolMaterial, 5, -2.75f, settings);
    }

    private static boolean hasEnchant(ItemStack stack, Enchantment ench) {
        return EnchantmentHelper.getLevel(ench, stack) > 0;
    }

    @Override
    public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        int charges = getCharges(stack);
        return Math.round(13.0F * (float) charges / 3);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return new Color(137, 118, 153).getRGB();
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (otherStack.isEmpty()) {
            if (clickType == ClickType.RIGHT) {
                setDestroying(stack, !getDestroying(stack));
                return true;
            }
        }
        return super.onClicked(stack, otherStack, slot, clickType, player, cursorStackReference);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("Destroying blocks: " + getDestroying(stack)).formatted(Formatting.GRAY));
        tooltip.add(Text.empty());
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        int charges = getCharges(stack);
        if (charges > 0) {
            spawn(user, world, getDestroying(stack));
            if (!user.isCreative()) setCharges(stack, charges - 1);
            setCooldown(stack, RECHARGE_TICKS);
            user.getItemCooldownManager().set(this, 3);
            return TypedActionResult.success(stack);
        } else {
            return TypedActionResult.fail(stack);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient) {
            int charges = getCharges(stack);
            if (charges < 3) {
                int cooldown = getCooldown(stack);
                if (cooldown <= 0) {
                    setCharges(stack, charges + 1);
                    setCooldown(stack, RECHARGE_TICKS);
                } else {
                    setCooldown(stack, cooldown - 1);
                }
            } else {
                setCooldown(stack, RECHARGE_TICKS);
            }
        }
        super.inventoryTick(stack, world, entity, slot, selected);
    }

    @Override
    public BipedEntityModel.ArmPose getArmPose(ItemStack itemStack, PlayerEntity playerEntity) {
        return BipedEntityModel.ArmPose.CROSSBOW_CHARGE;
    }

    @Override
    public List<Identifier> getModels() {
        return List.of(GUI);
    }

    @Override
    public @Nullable Identifier getModel(ItemStack itemStack, @Nullable LivingEntity livingEntity, ModelTransformationMode modelTransformationMode) {
        return ModelPresets.handheld(modelTransformationMode, GUI);
    }

    @Override
    public Text getName(ItemStack stack) {
        return super.getName(stack).copy().styled(s -> s.withColor(new Color(137, 118, 153).getRGB()));
    }

    private static void spawn(PlayerEntity user, World world, boolean destroy) {
        if (!world.isClient) {
            Vec3d eyePos = user.getEyePos();
            Vec3d lookVec = user.getRotationVec(1.0f);

            FuseEntity fuse = new FuseEntity(ArmoryEntityRegistry.FUSE, world);
            fuse.setPosition(eyePos.x + lookVec.x * 0.5D, eyePos.y + lookVec.y * 0.5, eyePos.z + lookVec.z * 0.5);
            fuse.setOwner(user);
            fuse.setNuke(hasEnchant(user.getStackInHand(user.getActiveHand()), ArmoryEnchantRegistry.NUKE));
            Vec3d vel = user.getVelocity();
            fuse.setVelocity(lookVec.multiply((vel.x + vel.y + vel.z) / 3 * 3 + 1));
            fuse.setDestroyTerrain(destroy);

            world.spawnEntity(fuse);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.PLAYERS, 1, 1);
        }
    }

    private void setCharges(ItemStack stack, int charges) {
        stack.getOrCreateNbt().putInt(CHARGES_KEY, charges);
    }

    private int getCharges(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains(CHARGES_KEY)) {
            return nbt.getInt(CHARGES_KEY);
        }
        setCharges(stack, 3);
        return 3;
    }

    private void setCooldown(ItemStack stack, int ticks) {
        stack.getOrCreateNbt().putInt(COOLDOWN_KEY, ticks);
    }

    private int getCooldown(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains(COOLDOWN_KEY)) {
            return nbt.getInt(COOLDOWN_KEY);
        }
        return RECHARGE_TICKS;
    }

    private void setDestroying(ItemStack stack, boolean destroy) {
        stack.getOrCreateNbt().putBoolean(DESTROY_KEY, destroy);
    }

    private boolean getDestroying(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains(DESTROY_KEY)) {
            return nbt.getBoolean(DESTROY_KEY);
        }
        return false;
    }
}