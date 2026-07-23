package com.lumii.armory.item;

import net.chemthunder.reflect.api.ReflectPlugin;
import net.chemthunder.reflect.api.interfaces.SimpleModelItem;
import net.chemthunder.reflect.api.presets.ReflectModelPresets;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import silly.chemthunder.ozone.api.thingies.CustomBipedEntityModelPoseItem;

import static com.lumii.armory.Armory.BASE;

public class WraithItem extends SwordItem implements SimpleModelItem{
    public WraithItem(ToolMaterial toolMaterial, Settings settings) {
        super(toolMaterial, 4, -2.5f, settings);
    }

    @Override
    public ReflectPlugin getPlugin() {
        return BASE;
    }

    @Override
    public String primaryModel() {
        return "wraith";
    }

    @Override
    public String secondaryModel() {
        return "wraith_gui";
    }

    @Override
    public ReflectModelPresets getPreset(ItemStack itemStack) {
        return ReflectModelPresets.Handheld;
    }
}
