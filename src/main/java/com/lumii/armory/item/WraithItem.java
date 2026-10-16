package com.lumii.armory.item;

import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.util.Identifier;
import org.carpentry.citrine.api.item.VaryingModelItem;
import org.carpentry.citrine.api.util.model.ModelPresets;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WraithItem extends SwordItem implements VaryingModelItem {
    public WraithItem(ToolMaterial toolMaterial, Settings settings) {
        super(toolMaterial, 4, -2.5f, settings);
    }

    public static Identifier GUI = new Identifier("armory", "wraith_gui");

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
