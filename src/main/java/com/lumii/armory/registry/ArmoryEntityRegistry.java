package com.lumii.armory.registry;

import com.lumii.armory.Armory;
import com.lumii.armory.entity.FuseEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ArmoryEntityRegistry {
    public static final EntityType<FuseEntity> FUSE = Registry.register(Registries.ENTITY_TYPE, Armory.id("fuse"), FabricEntityTypeBuilder.create(SpawnGroup.MISC, FuseEntity::new).dimensions(EntityDimensions.fixed(0.75F, 0.75F)).trackRangeBlocks(60).trackedUpdateRate(10).build());
    public static void init(){}
}
