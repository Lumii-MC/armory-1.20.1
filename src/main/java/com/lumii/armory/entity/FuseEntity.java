package com.lumii.armory.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class FuseEntity extends Entity {
    private static final TrackedData<Integer> FUSE = DataTracker.registerData(FuseEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final int DEFAULT_FUSE_TICKS = Integer.MAX_VALUE;

    @Nullable
    private UUID ownerUuid;
    private boolean destroyTerrain = true;

    public FuseEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    public void setOwner(@Nullable Entity owner) {
        if (owner != null) {
            this.ownerUuid = owner.getUuid();
        }
    }

    @Nullable
    public Entity getOwner() {
        if (this.ownerUuid != null && this.getWorld() instanceof ServerWorld serverWorld) {
            return serverWorld.getEntity(this.ownerUuid);
        }
        return null;
    }

    public void setDestroyTerrain(boolean destroyTerrain) {
        this.destroyTerrain = destroyTerrain;
    }

    public boolean shouldDestroyTerrain() {
        return this.destroyTerrain;
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(FUSE, DEFAULT_FUSE_TICKS);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.hasNoGravity()) {
            Vec3d velocity = this.getVelocity();
            this.setVelocity(velocity.x, velocity.y - 0.04D, velocity.z);
        }

        if (!this.getWorld().isClient()) {
            List<Entity> collidingEntities = this.getWorld().getOtherEntities(this, this.getBoundingBox().expand(0.1D), entity -> {
                if (entity.isSpectator()) return false;
                return !entity.getUuid().equals(this.ownerUuid);
            });

            if (!collidingEntities.isEmpty()) {
                this.explode();
                return;
            }
        }

        Vec3d velocityBeforeMove = this.getVelocity();
        this.move(MovementType.SELF, velocityBeforeMove);

        if (!this.getWorld().isClient()) {
            if (this.horizontalCollision || this.verticalCollision || this.velocityModified) {
                this.explode();
                return;
            }
        }

        Vec3d velocity = this.getVelocity();
        double drag = this.isOnGround() ? 0.6D : 0.98D;
        this.setVelocity(velocity.x * drag, velocity.y * 0.98D, velocity.z * drag);

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
        if (!this.getWorld().isClient() && !this.isRemoved()) {
            World.ExplosionSourceType sourceType = this.destroyTerrain ? World.ExplosionSourceType.TNT : World.ExplosionSourceType.NONE;
            this.getWorld().createExplosion(getOwner(), null, null, this.getX(), this.getY(), this.getZ(), 3.0F, false, sourceType);
            this.discard();
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putShort("Fuse", (short) this.dataTracker.get(FUSE).intValue());
        nbt.putBoolean("DestroyTerrain", this.destroyTerrain);
        if (this.ownerUuid != null) {
            nbt.putUuid("Owner", this.ownerUuid);
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("Fuse")) {
            this.dataTracker.set(FUSE, (int) nbt.getShort("Fuse"));
        } else {
            this.dataTracker.set(FUSE, DEFAULT_FUSE_TICKS);
        }
        this.destroyTerrain = !nbt.contains("DestroyTerrain") || nbt.getBoolean("DestroyTerrain");
        if (nbt.containsUuid("Owner")) {
            this.ownerUuid = nbt.getUuid("Owner");
        }
    }

    @Override
    protected void onBlockCollision(BlockState state) {
        super.onBlockCollision(state);
    }
}