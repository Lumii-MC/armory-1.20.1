package com.lumii.armory.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class DivinityLauncherItem extends Item {
    public DivinityLauncherItem(Settings settings) {
        super(settings);
    }

    public Text getName(ItemStack stack){
        return super.getName(stack).copy().styled(style -> style.withColor(0xfff3ae));
    }
}