package com.lumii.armory.item.discontinuity;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class DiscontinuityItem extends SwordItem {
    private static final UUID REACH_MODIFIER = UUID.randomUUID();

    private static final VariantData DEFAULT = new VariantData(PartVariant.HANDLE_STANDARD, PartVariant.MID_BLADE, PartVariant.END_POINTY);
    public DiscontinuityItem(Settings settings) {
        super(ToolMaterials.IRON, 6, -2.75f, settings);
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
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        setVariantData(user.getStackInHand(hand).getOrCreateNbt(), VariantData.random());
        return super.use(world, user, hand);
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
        tooltip.add(data.handle().getName());
        tooltip.add(data.mid().getName());
        tooltip.add(data.end().getName());
        super.appendTooltip(stack, world, tooltip, context);
    }
}
