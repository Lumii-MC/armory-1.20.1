package com.lumii.armory.item;

import com.lumii.armory.util.scissor.ActiveChain;
import com.lumii.armory.util.scissor.ChainedEntitiesManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import java.util.UUID;

public class ScissorBladeItem extends SwordItem {
    public ScissorBladeItem(Settings settings) {
        super(ToolMaterials.IRON, 5, -2.4f, settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (user.isSneaking()) if (!user.getWorld().isClient) ChainedEntitiesManager.addChain(new ActiveChain(entity, user, 5, 100, UUID.randomUUID()));
        if (!user.isSneaking()) if (!user.getWorld().isClient) ChainedEntitiesManager.addChain(new ActiveChain(user, entity, 5, 100, UUID.randomUUID()));
        return super.useOnEntity(stack, user, entity, hand);
    }
}
