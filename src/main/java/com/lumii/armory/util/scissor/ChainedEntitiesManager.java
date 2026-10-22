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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChainedEntitiesManager {
    private static final CopyOnWriteArrayList<ActiveChain> CHAINS = new CopyOnWriteArrayList<>();
    private static final Map<UUID, Long> EXPIRATIONS = new HashMap<>();

    public static int addChain(ActiveChain chain) {
        CHAINS.add(chain);
        EXPIRATIONS.put(
                chain.id(),
                chain.e1().getWorld().getTime() + (long) chain.duration());
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
        CHAINS.remove(index);
        EXPIRATIONS.remove(chain.id());
    }

    public static void removeChain(ActiveChain chain) {
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
        CHAINS.remove(chain);
        EXPIRATIONS.remove(chain.id());

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
            for (ActiveChain chain : CHAINS) {
                Entity e1 = chain.e1();
                Entity e2 = chain.e2();

                if (e1 == null || e2 == null
                        || !e1.isAlive() || !e2.isAlive()) {
                    removeChain(chain);
                    continue;
                }
                long expiration = EXPIRATIONS.getOrDefault(
                        chain.id(), Long.MAX_VALUE
                );
                if (e1.getWorld().getTime() >= expiration) {
                    removeChain(chain);
                    continue;
                }
                double distance = e1.getPos().distanceTo(e2.getPos());
                if (distance > chain.length()) {
                    Vec3d direction = e1.getPos()
                            .subtract(e2.getPos())
                            .normalize();
                    double overflow = distance - chain.length();
                    e2.addVelocity(direction.multiply(overflow * 0.5));
                    e2.velocityModified = true;
                }
            }
        });
    }
}