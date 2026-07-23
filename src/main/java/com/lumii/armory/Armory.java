package com.lumii.armory;

import com.lumii.armory.registry.ArmoryDamageRegistry;
import com.lumii.armory.registry.ArmoryItemRegistry;
import com.lumii.armory.registry.ArmoryPackets;
import com.lumii.armory.registry.ArmorySoundsRegistry;
import com.lumii.armory.util.time.TickSchedulerServer;
import net.chemthunder.reflect.api.ReflectPlugin;
import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Armory implements ModInitializer {
	public static final String MOD_ID = "armory";
	public static ReflectPlugin BASE = ReflectPlugin.register(MOD_ID);


	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ArmoryItemRegistry.init();
		ArmorySoundsRegistry.index();
		ArmoryPackets.initServer();
		TickSchedulerServer.init();

		LOGGER.info("Armory Initializing!");
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}
