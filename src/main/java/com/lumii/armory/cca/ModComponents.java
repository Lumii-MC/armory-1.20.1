package com.lumii.armory.cca;

import com.lumii.armory.Armory;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import net.minecraft.entity.player.PlayerEntity;

public class ModComponents implements EntityComponentInitializer {
    public static final ComponentKey<BooleanComponent> MARKED = ComponentRegistry.getOrCreate(Armory.id("marked"), BooleanComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(PlayerEntity.class, MARKED, it -> new MarkedBooleanComponent());
    }

    public static void init() {}
}
