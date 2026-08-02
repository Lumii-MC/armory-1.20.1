package com.lumii.armory.util;

import com.lumii.armory.vfx.distort.DistortionPost;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ChainClientTracker {
    private static boolean chained;

    public static boolean isChained() {
        return chained;
    }

    public static void setChained(boolean isChained) {
        chained = isChained;
        DistortionPost.INSTANCE.setActive(isChained);
    }
}
