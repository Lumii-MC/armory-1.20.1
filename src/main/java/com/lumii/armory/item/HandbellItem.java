package com.lumii.armory.item;

import com.lumii.armory.registry.ArmorySoundsRegistry;
import com.lumii.armory.util.ChainEntityUtils;
import com.lumii.armory.util.time.TickSchedulerServer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.carpentry.citrine.api.cooldown.StackCooldownManager;
import org.carpentry.citrine.api.util.TimeUtils;

public class HandbellItem extends Item {
    public HandbellItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            if (!user.isCreative()){
                StackCooldownManager.setCooldown(world, stack, TimeUtils.seconds(45));
            }

            Box box = user.getBoundingBox().expand(10);

            for (Entity entity : world.getOtherEntities(user, box)) {
                if (entity instanceof LivingEntity living) {
                    ChainEntityUtils.setChained(living, true);

                    TickSchedulerServer.schedule(TimeUtils.seconds(5), () -> {
                        ChainEntityUtils.setChained(living, false);
                    });
                }
            }
            world.playSound(
                    null,
                    user.getBlockPos(),
                    ArmorySoundsRegistry.HANDBELL,
                    SoundCategory.MASTER,
                    1,
                    1
            );
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    @Override
    public Text getName(ItemStack stack){
        return super.getName(stack).copy().styled(style -> style.withColor(0xf8d16d));
    }
}