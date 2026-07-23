package com.lumii.armory.registry;

import com.lumii.armory.Armory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public interface ArmorySoundsRegistry {
    Map<SoundEvent, Identifier> SOUNDS = new LinkedHashMap<>();


    SoundEvent SLASH = create("slash");

    private static SoundEvent create(String path) {
        SoundEvent soundEvent = SoundEvent.of(new Identifier(Armory.MOD_ID, path));
        SOUNDS.put(soundEvent, new Identifier(Armory.MOD_ID, path));
        return soundEvent;
    }

    static void index() {
        SOUNDS.keySet().forEach(soundEvent -> {
            Registry.register(Registries.SOUND_EVENT, SOUNDS.get(soundEvent), soundEvent);
        });
    }
}
