package com.lumii.armory.registry;

import com.lumii.armory.packets.ParticleSpawnPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class ArmoryPackets {
    public static final Identifier PARTICLE_SPAWN_ID = new Identifier("armory", "particle_spawn");

    public static void initServer(){
        ServerPlayNetworking.registerGlobalReceiver(PARTICLE_SPAWN_ID, ((server,
                                                                         player,
                                                                         handler,
                                                                         buf, sender) -> {
            ParticleSpawnPacket packet = new ParticleSpawnPacket(buf);
                    server.execute(() -> {

                    });
        }));
    }

    public static void initClient(){
        ClientPlayNetworking.registerGlobalReceiver(PARTICLE_SPAWN_ID, (client, handler, buf, sender) ->{
            ParticleSpawnPacket packet = new ParticleSpawnPacket(buf);
            packet.handle(client);
        } );
    }
}
