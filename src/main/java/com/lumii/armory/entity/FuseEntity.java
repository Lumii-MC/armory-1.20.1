package com.lumii.armory.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class FuseEntity extends Entity {
    private static final TrackedData<Integer> FUSE = DataTracker.registerData(FuseEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final int DEFAULT_FUSE_TICKS = Integer.MAX_VALUE;

    @Nullable
    private UUID ownerUuid;
    private boolean destroyTerrain = true;
    private boolean nuke = false;

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

    public void setNuke(boolean nuke) {
        this.nuke = nuke;
    }

    public boolean shouldNuke() {
        return this.nuke;
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

        for (int i = 0; i < 3 + (int) this.speed * 0.5f; i++) {
            this.getWorld().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), -this.getVelocity().x / 3 + (Math.random() - 0.5) / (4 + this.speed * 0.25f), -this.getVelocity().y / 3 + (Math.random() - 0.5) / (4 + this.speed * 0.25f), -this.getVelocity().z / 3 + (Math.random() - 0.5) / (4 + this.speed * 0.25f));
        }

        if (!this.hasNoGravity()) {
            Vec3d velocity = this.getVelocity();
            this.setVelocity(velocity.x, velocity.y - 0.04, velocity.z);
        }

        if (!this.getWorld().isClient()) {
            List<Entity> collidingEntities = this.getWorld().getOtherEntities(this, this.getBoundingBox().expand(0.1), entity -> {
                if (entity.isSpectator()) return false;
                if (entity instanceof FuseEntity && !this.nuke) return false;
                return !entity.getUuid().equals(this.ownerUuid);
            });

            if (!collidingEntities.isEmpty()) {
                AtomicBoolean doNuke = new AtomicBoolean(false);
                collidingEntities.forEach(entity -> {
                    if (entity instanceof FuseEntity) {
                        doNuke.set(true);
                    }
                });

                Vec3d ownVel = this.getVelocity();
                boolean fromBehind = false;
                boolean leading = false;
                boolean unrelated = false;
                if (doNuke.get() && this.nuke) {
                    for (Entity entity : collidingEntities) {
                        if (entity instanceof FuseEntity fuse && fuse.shouldNuke()) {
                            Vec3d otherVelocity = fuse.getVelocity();
                            if (ownVel.normalize().dotProduct(otherVelocity.normalize()) > 0.35) {
                                Vec3d travelDir = ownVel.add(otherVelocity).normalize();
                                if (this.getPos().subtract(fuse.getPos()).dotProduct(travelDir) <= 0) {
                                    fromBehind = true;
                                } else {
                                    leading = true;
                                }
                            } else {
                                unrelated = true;
                            }
                        } else {
                            unrelated = true;
                        }
                    }
                }

                if (!(leading && !fromBehind && !unrelated)) {
                    if (doNuke.get() && this.nuke) {
                        double fullSpeed = ownVel.length();
                        float power = fromBehind ? (float) fullSpeed * 2 + 2 : 6f;
                        World.ExplosionSourceType sourceType = this.destroyTerrain && !fromBehind ? World.ExplosionSourceType.TNT : World.ExplosionSourceType.NONE;
                        this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(), power, sourceType);
                        double range = fromBehind ? 45 : 30;
                        Box box = this.getBoundingBox().expand(range);
                        for (Entity e : this.getWorld().getOtherEntities(this, box, entity -> true)) {
                            if (fromBehind && e instanceof FuseEntity && collidingEntities.contains(e)) continue;
                            Vec3d vec = e.getBoundingBox().getCenter().subtract(this.getPos());
                            double dist = vec.length();
                            if (dist > range) continue;
                            double falloff = 1 - dist / range;
                            double strength = falloff * falloff;
                            if (fromBehind) strength *= (1 + fullSpeed) * 1.5;
                            e.addVelocity(vec.normalize().multiply(strength));
                            e.velocityModified = true;
                        }
                        if (!fromBehind) {
                            for (Entity entity : collidingEntities) {
                                if (entity instanceof FuseEntity other) {
                                    other.explode();
                                }
                            }
                        }
                    }
                    if (fromBehind) {
                        this.discard();
                    } else {
                        this.explode();
                    }
                    return;
                }
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
            if (!shouldNuke()) return;
            double range = 8;
            Box box = this.getBoundingBox().expand(range);
            for (Entity e : this.getWorld().getOtherEntities(this, box, entity -> true)) {
                Vec3d vec = e.getBoundingBox().getCenter().subtract(this.getPos());
                double dist = vec.length();
                if (dist > range) continue;
                double falloff = 1 - dist / range;
                double strength = falloff * falloff * falloff * 1.5;
                e.addVelocity(vec.normalize().multiply(strength));
                e.velocityModified = true;
            }
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putShort("Fuse", (short) this.dataTracker.get(FUSE).intValue());
        nbt.putBoolean("DestroyTerrain", this.destroyTerrain);
        nbt.putBoolean("Nuke", this.nuke);
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
        this.nuke = !nbt.contains("Nuke") || nbt.getBoolean("Nuke");
        if (nbt.containsUuid("Owner")) {
            this.ownerUuid = nbt.getUuid("Owner");
        }
    }

    @Override
    protected void onBlockCollision(BlockState state) {
        super.onBlockCollision(state);
    }
}