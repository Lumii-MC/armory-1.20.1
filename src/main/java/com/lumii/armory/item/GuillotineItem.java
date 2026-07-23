package com.lumii.armory.item;

import com.lumii.armory.Armory;
import com.lumii.armory.registry.ArmoryItemRegistry;
import com.lumii.armory.registry.ArmorySoundsRegistry;
import net.chemthunder.reflect.api.ReflectPlugin;
import net.chemthunder.reflect.api.interfaces.SimpleModelItem;
import net.chemthunder.reflect.api.presets.ReflectModelPresets;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import silly.chemthunder.ozone.api.thingies.CustomCritEffectItem;

import java.util.List;

public class GuillotineItem extends SwordItem implements CustomCritEffectItem, SimpleModelItem {

    public GuillotineItem(ToolMaterial toolMaterial, Settings settings) {
        super(toolMaterial, 5, -2.7f, settings);
    }

    private boolean isHooking;

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.getHealth() < attacker.getMaxHealth()){
            attacker.setHealth(attacker.getHealth() + 4);
        }
        return super.postHit(stack, target, attacker);
    }

    @Override
    public void critEffect(ItemStack itemStack, World world, LivingEntity attacker, LivingEntity target) {
        if (isHooking == false){
            target.playSound(ArmorySoundsRegistry.SLASH, 1, 1);
            Vec3d toAttacker = attacker.getPos().subtract(target.getPos()).normalize();
            double strength = 1.1;

            target.setVelocity(
                    toAttacker.x * strength,
                    toAttacker.y * strength,
                    toAttacker.z * strength
            );
            target.velocityModified = true;
        }
        if (isHooking == true){
            target.playSound(ArmorySoundsRegistry.SLASH, 2, 1);
            Vec3d toTarget = target.getPos().subtract(attacker.getPos()).normalize();
            double strength = 1.1;

            attacker.setVelocity(
                    toTarget.x * strength,
                    toTarget.y * strength,
                    toTarget.z * strength
            );
            attacker.velocityModified = true;
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user.isSneaking() && isHooking == false){
            isHooking = true;
            user.sendMessage(Text.literal("Mode: HOOK").formatted(Formatting.DARK_RED), true);
        }
        if (!user.isSneaking() && isHooking == true){
            isHooking = false;
            user.sendMessage(Text.literal("Mode: REAP").formatted(Formatting.GOLD), true);
        }
        return super.use(world, user, hand);
    }

    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockState state = context.getWorld().getBlockState(context.getBlockPos());
        PlayerEntity user = context.getPlayer();
        if (user != null && user.isSneaking() && state.isOf(Blocks.SMITHING_TABLE)) {
            ItemStack stack = user.getMainHandStack();
            if (stack.isOf(ArmoryItemRegistry.GUILLOTINE)) {
                stack.decrement(1);
                user.giveItemStack(ArmoryItemRegistry.GUILLOTINE_BREAKABLE.getDefaultStack());
                user.playSound(SoundEvents.BLOCK_SMITHING_TABLE_USE, 0.8F, 1.0F);
            }
            return ActionResult.SUCCESS;
        }
        return super.useOnBlock(context);
    }

    @Override
    public ReflectPlugin getPlugin() {
        return Armory.BASE;
    }

    @Override
    public String primaryModel() {
        return "guillotine";
    }

    @Override
    public String secondaryModel() {
        return "guillotine_gui";
    }

    @Override
    public ReflectModelPresets getPreset(ItemStack itemStack) {
        return ReflectModelPresets.Handheld;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.armory.guillotine1").formatted(Formatting.DARK_RED));
        if (isHooking == false){
            tooltip.add(Text.translatable("tooltip.armory.guillotine2").formatted(Formatting.GOLD));
        }
        if (isHooking){
            tooltip.add(Text.translatable("tooltip.armory.guillotine3").formatted(Formatting.GOLD));
        }
    }

    public Text getName(ItemStack stack){
        return super.getName(stack).copy().styled(style -> style.withColor(Formatting.GOLD));
    }
}