package com.lumii.armory.item;

import com.lumii.armory.registry.ArmoryItemRegistry;
import com.lumii.armory.registry.ArmorySoundsRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.carpentry.citrine.api.item.OnCritItem;
import org.carpentry.citrine.api.item.VaryingModelItem;
import org.carpentry.citrine.api.util.model.ModelPresets;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GuillotineItem extends SwordItem implements OnCritItem, VaryingModelItem {

    public GuillotineItem(ToolMaterial toolMaterial, Settings settings) {
        super(toolMaterial, 5, -2.7f, settings);
    }
    public static Identifier GUI = new Identifier("armory", "guillotine_gui");
    public static Identifier MAIN = new Identifier("armory", "guillotine_skin");

    private boolean isHooking;

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.getHealth() < attacker.getMaxHealth()){
            attacker.setHealth(attacker.getHealth() + 4);
        }
        return super.postHit(stack, target, attacker);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user.isSneaking() && !isHooking){
            isHooking = true;
            user.sendMessage(Text.literal("Mode: HOOK").formatted(Formatting.DARK_RED), true);
        }
        if (!user.isSneaking() && isHooking){
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
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.armory.guillotine1").formatted(Formatting.DARK_RED));
        if (!isHooking){
            tooltip.add(Text.translatable("tooltip.armory.guillotine2").formatted(Formatting.GOLD));
        }
        if (isHooking){
            tooltip.add(Text.translatable("tooltip.armory.guillotine3").formatted(Formatting.GOLD));
        }
    }

    public Text getName(ItemStack stack){
        return super.getName(stack).copy().styled(style -> style.withColor(Formatting.GOLD));
    }

    @Override
    public void onCrit(ItemStack itemStack, LivingEntity attacker, LivingEntity target) {
        if (!isHooking){
            target.getWorld().playSound(null, target.getBlockPos(),
                    ArmorySoundsRegistry.SLASH,
                    SoundCategory.NEUTRAL, 1, 1);
            Vec3d toAttacker = attacker.getPos().subtract(target.getPos()).normalize();
            double strength = 1.1;

            target.setVelocity(
                    toAttacker.x * strength,
                    toAttacker.y * strength,
                    toAttacker.z * strength
            );
            target.velocityModified = true;
        }
        if (isHooking){
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