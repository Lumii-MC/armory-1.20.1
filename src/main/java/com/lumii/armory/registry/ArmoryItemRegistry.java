package com.lumii.armory.registry;

import com.lumii.armory.item.*;
import com.lumii.armory.item.discontinuity.DiscontinuityItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ArmoryItemRegistry {
    public static final Item DEATH_MARK = Registry.register(Registries.ITEM, new Identifier(
            "armory", "death_mark"), new DeathMarkItem(new Item.Settings().maxCount(1))
    );

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

    public static final Item GUILLOTINE_PUB = Registry.register(Registries.ITEM, new Identifier(
            "armory", "pub_guillotine"), new PublicGuillotineItem(ToolMaterials.NETHERITE, new Item.Settings())
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
    public static final Item SCISSOR_BLADE = Registry.register(Registries.ITEM, new Identifier(
            "armory", "scissorblade"), new ScissorBladeItem(new Item.Settings())
    );
    public static final Item WORLD_REMOTE = Registry.register(Registries.ITEM, new Identifier(
            "armory", "world_remote"), new WorldRemoteItem(new Item.Settings().maxCount(1))
    );
    public static final Item DISCONTINUITY = Registry.register(Registries.ITEM, new Identifier(
            "armory", "discontinuity"), new DiscontinuityItem(new Item.Settings())
    );

    public static void init(){}

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
                        entries.add(GUILLOTINE_PUB);
                        entries.add(SCISSOR_BLADE);
                        entries.add(DISCONTINUITY);
                        entries.add(DIVINITY_DISSONANCE);
                        entries.add(HANDBELL);
                        entries.add(DEATH_MARK);
                        entries.add(WORLD_REMOTE);
                        entries.add(GUILLOTINE_SHARD);
                    })
                    .build()
    );
}
