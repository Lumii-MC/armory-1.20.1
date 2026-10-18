package com.lumii.armory.item;

import com.lumii.armory.registry.ArmoryEnchantRegistry;
import com.lumii.armory.util.scissor.ActiveChain;
import com.lumii.armory.util.scissor.ChainedEntitiesManager;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.carpentry.citrine.api.item.OnCritItem;
import org.carpentry.citrine.api.item.VaryingModelItem;
import org.carpentry.citrine.api.util.TimeUtils;
import org.carpentry.citrine.api.util.model.ModelPresets;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class ScissorBladeItem extends SwordItem implements VaryingModelItem, OnCritItem {
    public ScissorBladeItem(Settings settings) {
        super(ToolMaterials.IRON, 5, -2.6f, settings);
    }

    private Identifier GUI = new Identifier("armory", "scissorblade_gui");

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (user.isSneaking()) if (!user.getWorld().isClient) ChainedEntitiesManager.addChain(new ActiveChain(entity, user, 10, TimeUtils.seconds(5), UUID.randomUUID()));
        if (!user.isSneaking()) if (!user.getWorld().isClient) ChainedEntitiesManager.addChain(new ActiveChain(user, entity, 10, TimeUtils.seconds(5), UUID.randomUUID()));
        return super.useOnEntity(stack, user, entity, hand);
    }

    @Override
    public List<Identifier> getModels() {
        return List.of(
                GUI
        );
    }

    @Override
    public @Nullable Identifier getModel(ItemStack stack, @Nullable LivingEntity holder, ModelTransformationMode mode) {
        return ModelPresets.handheld(mode, GUI);
    }

    @Override
    public void onCrit(ItemStack itemStack, LivingEntity attacker, LivingEntity target) {
        if (hasEnchant(itemStack, ArmoryEnchantRegistry.HOOK)) {
            Vec3d toAttacker = attacker.getPos().subtract(target.getPos()).normalize();
            double strength = -1.1;

            attacker.setVelocity(
                    toAttacker.x * strength,
                    toAttacker.y * strength,
                    toAttacker.z * strength
            );
            attacker.velocityModified = true;
        }
    }
    private boolean hasEnchant(ItemStack stack, Enchantment ench) {
        return EnchantmentHelper.getLevel(ench, stack) > 0;
    }
}
