package com.lumii.armory.util.scissor;

import com.lumii.armory.registry.ArmoryPackets;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.CopyOnWriteArrayList;

public class ChainedEntitiesManager {
    private static final CopyOnWriteArrayList<ActiveChain> CHAINS = new CopyOnWriteArrayList<>();

    public static int addChain(ActiveChain chain) {
        CHAINS.add(chain);
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(chain.e1().getUuid());
        buf.writeUuid(chain.e2().getUuid());
        buf.writeFloat(chain.length());
        buf.writeFloat(chain.duration());
        buf.writeUuid(chain.id());
        buf.writeBoolean(false);
        for (PlayerEntity p : chain.e1().getWorld().getPlayers()) {
            ServerPlayNetworking.send((ServerPlayerEntity) p, ArmoryPackets.SCISSOR_CHAIN_STATUS_ID, buf);
        }
        return CHAINS.size();
    }

    public static void removeChain(int index) {
        CHAINS.remove(index);
        ActiveChain chain = CHAINS.get(index);
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(chain.e1().getUuid());
        buf.writeUuid(chain.e2().getUuid());
        buf.writeFloat(chain.length());
        buf.writeFloat(chain.duration());
        buf.writeUuid(chain.id());
        buf.writeBoolean(true);
        for (PlayerEntity p : chain.e1().getWorld().getPlayers()) {
            ServerPlayNetworking.send((ServerPlayerEntity) p, ArmoryPackets.SCISSOR_CHAIN_STATUS_ID, buf);
        }
    }

    public static void removeChain(ActiveChain chain) {
        CHAINS.remove(chain);
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(chain.e1().getUuid());
        buf.writeUuid(chain.e2().getUuid());
        buf.writeFloat(chain.length());
        buf.writeFloat(chain.duration());
        buf.writeUuid(chain.id());
        buf.writeBoolean(true);
        for (PlayerEntity p : chain.e1().getWorld().getPlayers()) {
            ServerPlayNetworking.send((ServerPlayerEntity) p, ArmoryPackets.SCISSOR_CHAIN_STATUS_ID, buf);
        }
    }

    public static ActiveChain isInChain(Entity e) {
        for (ActiveChain chain : CHAINS) {
            if (chain.e1().equals(e) || chain.e2().equals(e)) {
                return chain;
            }
        }
        return null;
    }

    public static CopyOnWriteArrayList<ActiveChain> getChains() {
        return CHAINS;
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            CHAINS.removeIf(chain -> chain.e1() == null || chain.e2() == null || !chain.e1().isAlive() || !chain.e2().isAlive());
            for (ActiveChain chain : CHAINS) {
                Entity e1 = chain.e1();
                Entity e2 = chain.e2();

                double distance = e1.getPos().distanceTo(e2.getPos());
                if (distance > chain.length()) {
                    Vec3d direction = e1.getPos().subtract(e2.getPos()).normalize();
                    double overflow = distance - chain.length();

                    e2.addVelocity(direction.multiply(overflow * 0.5));
                    e2.velocityModified = true;
                }
            }
        });
    }
}