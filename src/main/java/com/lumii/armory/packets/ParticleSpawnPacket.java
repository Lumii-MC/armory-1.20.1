package com.lumii.armory.packets;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;

public class ParticleSpawnPacket {
    public final Vec3d pos;
    public final int startColor;
    public final int endColor;

    public ParticleSpawnPacket(Vec3d pos, int startColor, int endColor){
        this.pos = pos;
        this.startColor = startColor;
        this.endColor = endColor;
    }

    public ParticleSpawnPacket(PacketByteBuf buf){
        this.pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.startColor = buf.readInt();
        this.endColor = buf.readInt();
    }

    public void toBytes(PacketByteBuf buf){
        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);
        buf.writeInt(startColor);
        buf.writeInt(endColor);
    }
}