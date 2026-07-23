package com.lumii.armory.item;

import com.lumii.armory.Armory;
import com.lumii.armory.registry.ArmoryItemRegistry;
import net.chemthunder.reflect.api.ReflectPlugin;
import net.chemthunder.reflect.api.presets.ReflectModelPresets;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class BreakableGuillotineItem extends GuillotineItem{
    public BreakableGuillotineItem(ToolMaterial toolMaterial, Settings settings) {
        super(toolMaterial, settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient){
            user.dropItem(ArmoryItemRegistry.GUILLOTINE_SHARD);
            user.dropItem(ArmoryItemRegistry.GUILLOTINE_SHARD);
            user.dropItem(ArmoryItemRegistry.GUILLOTINE_SHARD);
            user.dropItem(ArmoryItemRegistry.GUILLOTINE_SHARD);
            user.dropItem(ArmoryItemRegistry.GUILLOTINE_SHARD);

            world.playSound(
                    null,
                    user.getBlockPos(),
                    SoundEvents.ENTITY_ITEM_BREAK,
                    SoundCategory.PLAYERS,
                    2,
                    1);
        }
        stack.decrement(1);
        return super.use(world, user, hand);
    }

    @Override
    public ReflectPlugin getPlugin() {
        return Armory.BASE;
    }

    @Override
    public String primaryModel() {
        return "guillotine_breakable";
    }

    @Override
    public String secondaryModel() {
        return "guillotine_breakable_gui";
    }

    @Override
    public ReflectModelPresets getPreset(ItemStack itemStack) {
        return ReflectModelPresets.Handheld;
    }
}
