package com.lumii.armory.registry;

import com.lumii.armory.item.*;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ArmoryItemRegistry {
    public static final Item HANDBELL = Registry.register(Registries.ITEM, new Identifier(
            "armory", "handbell"), new HandbellItem(new Item.Settings().maxCount(1))
    );

    public static final Item DIVINITY_DISSONANCE = Registry.register(Registries.ITEM, new Identifier(
            "armory", "divinity_dissonance"), new DivinityDissonanceItem(new Item.Settings().maxCount(1))
    );

    public static final Item DIVINITY_LAUNCHER = Registry.register(Registries.ITEM, new Identifier(
            "armory", "divinity_launcher"), new DivinityLauncherItem(new Item.Settings().maxCount(1))
    );
    public static final Item GUILLOTINE = Registry.register(Registries.ITEM, new Identifier(
            "armory", "guillotine"), new GuillotineItem(ToolMaterials.NETHERITE, new Item.Settings())
    );

    public static final Item GUILLOTINE_BREAKABLE = Registry.register(Registries.ITEM, new Identifier(
            "armory", "guillotine_breakable"), new BreakableGuillotineItem(ToolMaterials.NETHERITE, new Item.Settings())
    );

    public static final Item GUILLOTINE_SHARD = Registry.register(Registries.ITEM, new Identifier(
            "armory", "guillotine_shard"), new Item(new Item.Settings().maxCount(16))
    );

    public static final Item WRAITH = Registry.register(Registries.ITEM, new Identifier(
            "armory", "wraith"), new WraithItem(ToolMaterials.NETHERITE, new Item.Settings())
    );
    public static final Item DAYBREAK_EDICT = Registry.register(Registries.ITEM, new Identifier(
            "armory", "daybreak_edict"), new GildedExecutionerItem(ToolMaterials.NETHERITE, new Item.Settings())
    );

    public static void init(){
    }

    public static final ItemGroup GROUP = Registry.register(
            Registries.ITEM_GROUP,
            new Identifier("armory", "group"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemGroup.armory.group"))
                    .icon(() -> new ItemStack(DAYBREAK_EDICT))
                    .entries((displayContext, entries) -> {
                        entries.add(GUILLOTINE);
                        entries.add(DAYBREAK_EDICT);
                        entries.add(WRAITH);
                        entries.add(DIVINITY_DISSONANCE);
                        entries.add(HANDBELL);
                        entries.add(GUILLOTINE_SHARD);
                    })
                    .build()
    );
}
