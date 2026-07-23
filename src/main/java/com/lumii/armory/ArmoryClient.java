package com.lumii.armory;

import com.lumii.armory.registry.ArmoryPackets;
import com.lumii.armory.util.time.TickSchedulerClient;
import net.fabricmc.api.ClientModInitializer;

public class ArmoryClient implements ClientModInitializer {
    public void onInitializeClient() {
        ArmoryPackets.initClient();
        TickSchedulerClient.init();
    }
}
