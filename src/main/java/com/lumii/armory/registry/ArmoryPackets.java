package com.lumii.armory.registry;

import com.lumii.armory.packets.DissonanceEffectHandlerHandler;
import com.lumii.armory.packets.DissonanceEffectPacket;
import com.lumii.armory.packets.ParticleSpawnPacket;
import com.lumii.armory.packets.ParticleSpawnPacketHandler;
import com.lumii.armory.util.ChainClientTracker;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import team.lodestar.lodestone.handlers.ScreenshakeHandler;
import team.lodestar.lodestone.systems.easing.Easing;
import team.lodestar.lodestone.systems.screenshake.ScreenshakeInstance;

public class ArmoryPackets {
    public static final Identifier PARTICLE_SPAWN_ID = new Identifier("armory", "particle_spawn");
    public static final Identifier SHAKE_ID = new Identifier("armory", "screenshake");
    public static final Identifier BEAM_SHAKE_ID = new Identifier("armory", "screenshake_beam");
    public static final Identifier MARK_VFX_ID = new Identifier("armory", "mark_vfx");
    public static final Identifier DISSONANCE_VFX_ID = new Identifier("armory", "dis_vfx");
    public static final Identifier CHAIN_STATUS_ID = new Identifier("armory", "chain_status");

    public static void initServer(){
        ServerPlayNetworking.registerGlobalReceiver(PARTICLE_SPAWN_ID, ((server,
                                                                         player,
                                                                         handler,
                                                                         buf, sender) -> {
            ParticleSpawnPacket packet = new ParticleSpawnPacket(buf);
                    server.execute(() -> {
                        // why the fuck do you need a server init if you don't have anything here
                        // idk man i was js following the lodestone tutorial ok :sob:
                    });
        }));
    }

    public static void initClient(){
        ClientPlayNetworking.registerGlobalReceiver(PARTICLE_SPAWN_ID, (client, handler, buf, sender) -> {
            ParticleSpawnPacket packet = new ParticleSpawnPacket(buf);
            ParticleSpawnPacketHandler.handle(client, packet);
        });

        ClientPlayNetworking.registerGlobalReceiver(SHAKE_ID, (client, handler, buf, sender) -> {
            client.execute(() -> {
                ScreenshakeInstance instance = new ScreenshakeInstance((int) (20 * 3f)).setIntensity(0.75f).setEasing(Easing.QUAD_IN_OUT);
                ScreenshakeHandler.addScreenshake(instance);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(MARK_VFX_ID, (client, handler, buf, sender) -> {
            client.execute(() -> {

            });
        });

        ClientPlayNetworking.registerGlobalReceiver(BEAM_SHAKE_ID, (client, handler, buf, sender) -> {
            client.execute(() -> {
                ScreenshakeInstance instance = new ScreenshakeInstance((int) (20 * 4.5f)).setIntensity(0.75f).setEasing(Easing.QUAD_IN_OUT);
                ScreenshakeHandler.addScreenshake(instance);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(DISSONANCE_VFX_ID, (client, handler, buf, sender) ->{
            DissonanceEffectPacket packet = new DissonanceEffectPacket(buf);
            DissonanceEffectHandlerHandler.handle(client, packet);
        } );

        ClientPlayNetworking.registerGlobalReceiver(CHAIN_STATUS_ID, (client, handler, buf, sender) ->{
            boolean chained = buf.readBoolean();
            client.execute(() -> {
                ChainClientTracker.setChained(chained);
            });
        } );
    }
}
