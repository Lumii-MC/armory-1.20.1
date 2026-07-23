package com.lumii.armory.util.time;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.IntConsumer;

public class TickSchedulerServer {
    private static final List<ScheduledTask> tasks = new CopyOnWriteArrayList<>();
    private static final List<ScheduledTask> tasksToAdd = new ArrayList<>();
    private static final List<RepeatingTask> repeatingTasks = new CopyOnWriteArrayList<>();
    private static final List<RepeatingTask> repeatingTasksToAdd = new ArrayList<>();

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(TickSchedulerServer::onTick);
    }

    public static void schedule(int delayTicks, Runnable action) {
        tasksToAdd.add(new ScheduledTask(delayTicks, action));
    }

    public static void scheduleRepeating(int times, IntConsumer action) {
        repeatingTasksToAdd.add(new RepeatingTask(times, action));
    }

    private static void onTick(MinecraftServer server) {
        if (!tasksToAdd.isEmpty()) {
            tasks.addAll(tasksToAdd);
            tasksToAdd.clear();
        }

        if (!repeatingTasksToAdd.isEmpty()) {
            repeatingTasks.addAll(repeatingTasksToAdd);
            repeatingTasksToAdd.clear();
        }

        for (ScheduledTask task : tasks) {
            task.ticksLeft--;
            if (task.ticksLeft <= 0) {
                try {
                    task.action.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                tasks.remove(task);
            }
        }

        for (RepeatingTask task : repeatingTasks) {
            try {
                int currentIteration = task.totalExecutions - task.executionsLeft;
                task.action.accept(currentIteration);
            } catch (Exception e) {
                e.printStackTrace();
            }

            task.executionsLeft--;
            if (task.executionsLeft <= 0) {
                repeatingTasks.remove(task);
            }
        }
    }

    private static class ScheduledTask {
        int ticksLeft;
        Runnable action;

        ScheduledTask(int ticks, Runnable action) {
            this.ticksLeft = ticks;
            this.action = action;
        }
    }

    private static class RepeatingTask {
        int executionsLeft;
        int totalExecutions;
        IntConsumer action;

        RepeatingTask(int times, IntConsumer action) {
            this.executionsLeft = times;
            this.totalExecutions = times;
            this.action = action;
        }
    }
}