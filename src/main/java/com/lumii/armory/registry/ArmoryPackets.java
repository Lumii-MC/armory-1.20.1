package com.lumii.armory.registry;

import com.lumii.armory.Armory;
import com.lumii.armory.packets.ParticleSpawnPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import team.lodestar.lodestone.handlers.ScreenshakeHandler;
import team.lodestar.lodestone.systems.easing.Easing;
import team.lodestar.lodestone.systems.screenshake.ScreenshakeInstance;

public class ArmoryPackets {
    public static final Identifier PARTICLE_SPAWN_ID = new Identifier("armory", "particle_spawn");
    public static final Identifier SHAKE_ID = new Identifier("armory", "screenshake");

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

        ClientPlayNetworking.registerGlobalReceiver(SHAKE_ID, (client, handler, buf, sender) -> {
            client.execute(() -> {
                ScreenshakeInstance instance = new ScreenshakeInstance((int) (20 * 2f)).setIntensity(0.75f).setEasing(Easing.QUAD_IN_OUT);
                ScreenshakeHandler.addScreenshake(instance);
            });
        });
    }
}
