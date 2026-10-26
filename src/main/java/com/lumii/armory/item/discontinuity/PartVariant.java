package com.lumii.armory.item.discontinuity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.util.TriConsumer;

import java.awt.*;
import java.util.function.Function;

public enum PartVariant {
    HANDLE_STANDARD(Text.literal("Standard").styled(style -> style.withColor(new Color(172, 150, 219).getRGB())), PartGroup.HANDLE, 1, 0, 0,
            n -> n, n -> n, n -> n,
            (entity, entity2, stack) -> {}, false),
    HANDLE_GRIP(Text.literal("Grippy").styled(style -> style.withColor(new Color(172, 150, 219).getRGB())), PartGroup.HANDLE, 1, 0, 0,
            n -> n * 0.95f,
            n -> n + 0.2f,
            n -> n,
            (entity, entity2, stack) -> {}, false),
    HANDLE_LONG(Text.literal("Elongated").styled(style -> style.withColor(new Color(172, 150, 219).getRGB())), PartGroup.HANDLE, 1, 4, -4,
            n -> n * 1.1f,
            n -> n - 0.2f,
            n -> n + 0.3f,
            (entity, entity2, stack) -> {}, false),
    HANDLE_VAMPIRE(Text.literal("Vampiric").styled(style -> style.withColor(new Color(255, 41, 66).getRGB())), PartGroup.HANDLE, 1, 0, 0,
            n -> n * 1.1f,
            n -> n - 0.2f,
            n -> n + 0.3f,
            (entity, entity2, stack) -> {
            if (entity.fallDistance > 0 && entity instanceof PlayerEntity p && p.getAttackCooldownProgress(0.0f) >= 1f) {
                entity.setHealth(entity.getHealth() + 2);
                p.getHungerManager().setFoodLevel(p.getHungerManager().getFoodLevel() + 2);
            }
            }, false),
    HANDLE_BERSERK(Text.literal("Berserker").styled(style -> style.withColor(new Color(255, 41, 119).getRGB())), PartGroup.HANDLE, 1, 0, 0,
            n -> n * 1.1f,
            n -> n - 0.2f,
            n -> n + 0.3f,
            (entity, entity2, stack) -> {
                if (entity instanceof PlayerEntity p) entity2.damage(entity.getDamageSources().playerAttack(p), entity.getMaxHealth() - entity.getHealth() / 2);
            }, false),

    MID_BLADE(Text.literal("Blade").styled(style -> style.withColor(new Color(172, 150, 219).getRGB())), PartGroup.MIDDLE, 2, 0, 0,
            n -> n, n -> n, n -> n,
            (entity, entity2, stack) -> {}, false),
    MID_HOOK(Text.literal("Hook").styled(style -> style.withColor(new Color(172, 150, 219).getRGB())), PartGroup.MIDDLE, 2, 0, 0,
            n -> n * 0.95f,
            n -> n,
            n -> n + 0.1f,
            (entity, entity2, stack) -> {
                if (!entity.getWorld().isClient) {
                    Vec3d dir = entity.getPos().subtract(entity2.getPos()).normalize();
                    entity2.addVelocity(dir.x * 0.25, 0, dir.z * 0.25);
                    entity2.velocityModified = true;
                }
            }, false),
    MID_AXE(Text.literal("Axe").styled(style -> style.withColor(new Color(172, 150, 219).getRGB())), PartGroup.MIDDLE, 2, 3, -1,
            n -> n * 1.15f,
            n -> n - 0.3f,
            n -> n + 0.15f,
            (entity, entity2, stack) -> {}, false),

    END_POINTY(Text.literal("Pointy").styled(style -> style.withColor(new Color(172, 150, 219).getRGB())), PartGroup.END, 3, 0, 0,
            n -> n, n -> n, n -> n,
            (entity, entity2, stack) -> {}, false),
    END_HARPOON(Text.literal("Harpoon").styled(style -> style.withColor(new Color(172, 150, 219).getRGB())), PartGroup.END, 3, 0, 0,
            n -> n,
            n -> n - 0.1f,
            n -> n + 0.15f,
            (entity, entity2, stack) -> {
                if (!entity.getWorld().isClient) {
                    Vec3d dir = entity.getPos().subtract(entity2.getPos()).normalize();
                    entity2.addVelocity(dir.x * 0.35, 0, dir.z * 0.35);
                    entity2.velocityModified = true;
                }
            }, false),
    END_SPEARHEAD(Text.literal("Spearhead").styled(style -> style.withColor(new Color(172, 150, 219).getRGB())), PartGroup.END, 3, 0, 0,
            n -> n * 1.05f,
            n -> n - 0.1f,
            n -> n + 0.2f,
            (entity, entity2, stack) -> {
                if (!entity.getWorld().isClient) {
                    Vec3d dir = entity2.getPos().subtract(entity.getPos()).normalize();
                    entity2.addVelocity(dir.x * 0.25, 0, dir.z * 0.25);
                    entity2.velocityModified = true;
                }
            }, false),
    END_BLAZING(Text.literal("Blazing").styled(style -> style.withColor(new Color(255, 127, 41).getRGB())), PartGroup.END, 3, 0, 0,
                  n -> n * 1.15f,
    n -> n - 0.1f,
    n -> n + 0.2f,
            (entity, entity2, stack) -> {
        if (!entity.getWorld().isClient) {
            entity2.setFireTicks(4 * 20);
        }
    }, true),
    END_FROSTY(Text.literal("Frosty").styled(style -> style.withColor(new Color(41, 162, 255).getRGB())), PartGroup.END, 3, 0, 0,
                n -> n * 1.1f,
    n -> n - 0.1f,
    n -> n + 0.2f,
            (entity, entity2, stack) -> {
                if (!entity.getWorld().isClient) {
                    entity2.setFrozenTicks(20 * 20);
                }
            }, false);

    private final Text name;
    private final PartGroup group;
    private final int scaleFactor;
    private final int offsetNext;
    private final int fullOffset;
    private final boolean doIgnoreLight;

    private final Function<Float, Float> dmgFunc;
    private final Function<Float, Float> speedFunc;
    private final Function<Float, Float> reachFunc;

    private final TriConsumer<LivingEntity, LivingEntity, ItemStack> onHit;
    PartVariant(Text name, PartGroup group, int scaleFactor, int offsetNext, int fullOffset, Function<Float, Float> dmgFunc, Function<Float, Float> speedFunc, Function<Float, Float> reachFunc, TriConsumer<LivingEntity, LivingEntity, ItemStack> onHit, boolean doIgnoreLight) {
        this.name = name;
        this.group = group;
        this.scaleFactor = scaleFactor;
        this.offsetNext = offsetNext;
        this.fullOffset = fullOffset;
        this.doIgnoreLight = doIgnoreLight;

        this.dmgFunc = dmgFunc;
        this.speedFunc = speedFunc;
        this.reachFunc = reachFunc;

        this.onHit = onHit;
    }

    public static PartVariant byName(String name) {
        for (PartVariant variant : PartVariant.values()) {
            if (variant.getName().getString().equalsIgnoreCase(name)) return variant;
        }
        return null;
    }

    public String getId() { return name().toLowerCase(); }
    public Identifier getModelId() {return new Identifier("armory", "item/disc/" + getId());}
    public static PartVariant byId(String id, PartVariant fallback) {
        for (PartVariant v : values()) {
            if (v.getId().equals(id)) return v;
        }
        return fallback;
    }
    public Text getName() {return name.copy().append(" ").append(group.getName());}
    public String getTextureName() {return name.getString().toLowerCase() + "_" + group.getName().toLowerCase();}
    public int getScaleFactor() {return scaleFactor;}
    public PartGroup getGroup() {return group;}
    public int getOffsetNext() {return offsetNext;}
    public boolean doIgnoreLight() {return doIgnoreLight;}
    public int getFullOffset() {return fullOffset;}

    public Function<Float, Float> getDmgFunc() {return dmgFunc;}
    public Function<Float, Float> getReachFunc() {return reachFunc;}
    public Function<Float, Float> getSpeedFunc() {return speedFunc;}

    public TriConsumer<LivingEntity, LivingEntity, ItemStack> getOnHit() {return onHit;}
}