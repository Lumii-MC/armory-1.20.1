package com.lumii.armory.util.time;

public class TimeUtils {
    public TimeUtils() {
    }

    public static int seconds(int ticks) {
        return ticks * 20;
    }

    public static int minutes(int ticks) {
        return ticks * 1200;
    }
}
