package com.lumii.armory.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public class FuseEntity extends Entity {
    private static final TrackedData<Integer> FUSE = DataTracker.registerData(FuseEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private int maxFuseTicks = 100;

    public FuseEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(FUSE, maxFuseTicks);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getWorld().isClient()) {
            int currentFuse = this.dataTracker.get(FUSE);
            if (currentFuse <= 0) {
                this.explode();
            } else {
                this.dataTracker.set(FUSE, currentFuse - 1);
            }
        }
    }

    private void explode() {
        if (!this.getWorld().isClient()) {
            this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(), 3.0F, World.ExplosionSourceType.TNT);
            this.discard();
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putShort("Fuse", (short) this.dataTracker.get(FUSE).intValue());
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("Fuse")) {
            this.dataTracker.set(FUSE, (int) nbt.getShort("Fuse"));
        }
    }
}
