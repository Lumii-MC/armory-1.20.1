package com.lumii.armory.item.discontinuity;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import com.lumii.armory.registry.ArmorySoundsRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.UUID;

public class DiscontinuityItem extends SwordItem {
    private static final UUID REACH_MODIFIER = UUID.randomUUID();

    private static final VariantData DEFAULT = new VariantData(PartVariant.HANDLE_STANDARD, PartVariant.MID_BLADE, PartVariant.END_POINTY);

    public DiscontinuityItem(Settings settings) {
        super(ToolMaterials.IRON, 6, -2.75f, settings);
    }

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        PartVariant mid = getVariantData(stack).mid();
        if (mid.equals(PartVariant.MID_AXE) && state.isIn(BlockTags.AXE_MINEABLE)) {
            return 12.0f;
        }
        if (mid.equals(PartVariant.MID_HOOK) && state.isIn(BlockTags.HOE_MINEABLE)) {
            return 12.0f;
        }
        return super.getMiningSpeedMultiplier(stack, state);
    }

    @Override
    public boolean isSuitableFor(BlockState state) {
        return state.isIn(BlockTags.AXE_MINEABLE) || state.isIn(BlockTags.HOE_MINEABLE) || super.isSuitableFor(state);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClient() && state.getHardness(world, pos) != 0.0f) {
            PartVariant mid = getVariantData(stack).mid();
            if ((mid.equals(PartVariant.MID_AXE) && state.isIn(BlockTags.AXE_MINEABLE)) ||
                    (mid.equals(PartVariant.MID_HOOK) && state.isIn(BlockTags.HOE_MINEABLE))) {
                stack.damage(1, miner, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
                return true;
            }
        }
        return super.postMine(stack, world, state, pos, miner);
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        PartVariant mid = getVariantData(miner.getMainHandStack()).mid();
        if (state.isIn(BlockTags.AXE_MINEABLE) && mid.equals(PartVariant.MID_AXE)) return true;
        if (state.isIn(BlockTags.HOE_MINEABLE) && mid.equals(PartVariant.MID_HOOK)) return true;
        return super.canMine(state, world, pos, miner);
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        VariantData data = getVariantData(stack);
        float dmg = getAttackDamage();
        float speed = -2.75f;
        float reach = 0;
        dmg = data.handle().getDmgFunc().apply(dmg);
        speed = data.handle().getSpeedFunc().apply(speed);
        reach = data.handle().getReachFunc().apply(reach);
        dmg = data.mid().getDmgFunc().apply(dmg);
        speed = data.mid().getSpeedFunc().apply(speed);
        reach = data.mid().getReachFunc().apply(reach);
        dmg = data.end().getDmgFunc().apply(dmg);
        speed = data.end().getSpeedFunc().apply(speed);
        reach = data.end().getReachFunc().apply(reach);
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", dmg, EntityAttributeModifier.Operation.ADDITION));
        builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Weapon modifier", speed, EntityAttributeModifier.Operation.ADDITION));
        builder.put(ReachEntityAttributes.ATTACK_RANGE, new EntityAttributeModifier(REACH_MODIFIER, "Weapon modifier", reach, EntityAttributeModifier.Operation.ADDITION));
        builder.put(ReachEntityAttributes.REACH, new EntityAttributeModifier(REACH_MODIFIER, "Weapon modifier", reach * 1.25, EntityAttributeModifier.Operation.ADDITION));
        if (slot.equals(EquipmentSlot.MAINHAND)) return builder.build();
        return super.getAttributeModifiers(stack, slot);
    }

    public static PartGroup getSelectedGroup(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return PartGroup.HANDLE;
        PartGroup[] groups = PartGroup.values();
        return groups[MathHelper.clamp(nbt.getInt("selected"), 0, groups.length - 1)];
    }

    public static void cycleSelectedGroup(ItemStack stack) {
        stack.getOrCreateNbt().putInt("selected", getSelectedGroup(stack).next().ordinal());
    }

    public static void cycleSelectedVariant(ItemStack stack) {
        VariantData data = getVariantData(stack);
        PartGroup group = getSelectedGroup(stack);
        setVariantData(stack.getOrCreateNbt(), data.with(group, data.get(group).next()));
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType != ClickType.RIGHT || !otherStack.isEmpty() || !slot.canTakePartial(player)) return false;
        cycleSelectedVariant(stack);
        player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), ArmorySoundsRegistry.PART_SWAP, SoundCategory.PLAYERS, 0.5f, 1.0f);
        return true;
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        VariantData data = getVariantData(stack);
        data.handle().getOnHit().accept(attacker, target, stack);
        data.mid().getOnHit().accept(attacker, target, stack);
        data.end().getOnHit().accept(attacker, target, stack);
        return super.postHit(stack, target, attacker);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        VariantData data = getVariantData(stack);
        PartGroup selected = getSelectedGroup(stack);
        for (PartGroup group : PartGroup.values()) {
            Text name = data.get(group).getName();
            tooltip.add(group == selected ? name.copy().styled(s -> s.withUnderline(true)) : name);
        }
        super.appendTooltip(stack, world, tooltip, context);
    }

    public static void setVariantData(NbtCompound nbt, VariantData data) {
        nbt.putString("handle", data.handle().getId());
        nbt.putString("middle", data.mid().getId());
        nbt.putString("end", data.end().getId());
    }

    public static VariantData getVariantData(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return DEFAULT;
        return new VariantData(PartVariant.byId(nbt.getString("handle"), PartVariant.HANDLE_STANDARD), PartVariant.byId(nbt.getString("middle"), PartVariant.MID_BLADE), PartVariant.byId(nbt.getString("end"), PartVariant.END_POINTY));
    }

    @Override
    public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
    }

    @Override
    public Text getName(ItemStack stack) {
        return super.getName(stack).copy().styled(style -> style.withColor(new Color(117, 116, 211).getRGB()));
    }
}