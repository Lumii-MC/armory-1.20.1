package com.lumii.armory.item;

import com.lumii.armory.registry.ArmoryEnchantRegistry;
import com.lumii.armory.registry.ArmorySoundsRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.carpentry.citrine.api.item.ItemWithSkins;
import org.carpentry.citrine.api.item.OnCritItem;
import org.carpentry.citrine.api.item.VaryingModelItem;
import org.carpentry.citrine.api.util.model.ModelPresets;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PublicGuillotineItem extends SwordItem implements OnCritItem, VaryingModelItem, ItemWithSkins {
    public PublicGuillotineItem(ToolMaterial toolMaterial, Settings settings) {
        super(toolMaterial, 3, -2.7f, settings);
    }

    public static Identifier GUI = new Identifier("armory", "pub_guillotine_gui");
    public static Identifier STRAIGHT = new Identifier("armory", "pub_guillotine_straight");
    public static Identifier FLESH = new Identifier("armory", "pub_guillotine_flesh");
    public static Identifier HONEY = new Identifier("armory", "pub_guillotine_honey");
    public static Identifier BEE = new Identifier("armory", "pub_guillotine_bee");
    public static Identifier FOLLY = new Identifier("armory", "pub_guillotine_folly");
    public static Identifier JEVIL = new Identifier("armory", "pub_guillotine_jevil");

    private static Identifier suffix(Identifier id, String s) {
        return new Identifier(id.getNamespace(), id.getPath() + s);
    }

    @Override
    public List<Identifier> getSkins() {
        return List.of(
                STRAIGHT,
                FLESH,
                HONEY,
                BEE,
                JEVIL,
                FOLLY);
    }

    @Override
    public int getCurrentSkin(NbtCompound nbt) {
        return ItemWithSkins.super.getCurrentSkin(nbt);
    }

    @Override
    public void setSkin(NbtCompound nbt, int skin) {
        ItemWithSkins.super.setSkin(nbt, skin);
    }

    @Override
    public void onCrit(ItemStack itemStack, LivingEntity attacker, LivingEntity target) {
            target.getWorld().playSound(null, target.getBlockPos(),
                    ArmorySoundsRegistry.SLASH,
                    SoundCategory.PLAYERS, 0.75f, 1);

            if (hasEnchant(itemStack, ArmoryEnchantRegistry.REAP)) {
                Vec3d toAttacker = attacker.getPos().subtract(target.getPos()).normalize();
                double strength = 1.1;

                target.setVelocity(
                        toAttacker.x * strength,
                        toAttacker.y * strength,
                        toAttacker.z * strength
                );
                target.velocityModified = true;
            }
            else if (hasEnchant(itemStack, ArmoryEnchantRegistry.HOOK)) {
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

    @Override
    public List<Identifier> getModels() {
        List<Identifier> all = new ArrayList<>(List.of(GUI));
        for (Identifier skin : getSkins()) {
            all.add(skin);
            all.add(PublicGuillotineItem.suffix(skin, "_gui"));
        }
        return all;
    }

    @Override
    public @Nullable Identifier getModel(ItemStack stack, @Nullable LivingEntity holder, ModelTransformationMode mode) {
        NbtCompound nbt = stack.getNbt();
        int skin = nbt == null ? -1 : getCurrentSkin(nbt);
        List<Identifier> skins = getSkins();
        boolean skinned = skin >= 0 && skin < skins.size();

        Identifier base = skinned ? skins.get(skin) : null;
        Identifier gui = skinned ? suffix(base, "_gui") : GUI;

        Identifier id = ModelPresets.handheld(mode, gui);
        if (id != null) return id;

        return base;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ItemStack stack = context.getStack();
        BlockState state = context.getWorld().getBlockState(context.getBlockPos());
        World world = context.getWorld();
        NbtCompound nbt = stack.getOrCreateNbt();
        PlayerEntity user = context.getPlayer();
        int next = getCurrentSkin(stack.getOrCreateNbt()) +1;

        if (state.isOf(Blocks.SMITHING_TABLE) && !world.isClient && user != null &&user.isSneaking()){
            if (next >=getSkins().size()){
                nbt.remove("skin");
                if (nbt.isEmpty()) stack.setNbt(null);
            } else {
                setSkin(nbt, next);
            }
            return ActionResult.SUCCESS;
        }
        return super.useOnBlock(context);
    }

    private boolean hasEnchant(ItemStack stack, Enchantment ench) {
        return EnchantmentHelper.getLevel(ench, stack) > 0;
    }
}
