package com.lumii.armory.registry;

import com.lumii.armory.item.*;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
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
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(ArmoryItemRegistry::modifyCombatTab);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(ArmoryItemRegistry::modifyIngredientsTab);
    }

    private static void modifyCombatTab(FabricItemGroupEntries entries){
        entries.add(GUILLOTINE);
        entries.add(DIVINITY_DISSONANCE);
        entries.add(DAYBREAK_EDICT);
        entries.add(WRAITH);
        entries.add(HANDBELL);
    }

    private static void modifyIngredientsTab(FabricItemGroupEntries entries){
        entries.add(GUILLOTINE_SHARD);
    }
}
