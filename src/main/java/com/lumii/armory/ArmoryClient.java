package com.lumii.armory;

import com.lumii.armory.registry.ArmoryPackets;
import net.fabricmc.api.ClientModInitializer;

public class ArmoryClient implements ClientModInitializer {
    public void onInitializeClient() {
        ArmoryPackets.initClient();
    }
}
