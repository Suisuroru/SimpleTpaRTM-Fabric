package fun.bm.simpletpartm.managers;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportScheduler {
    private static int lastUsedId = 0;
    private static final Object lock = new Object();
    public static Map<Integer, ScheduleTask> tasks = new ConcurrentHashMap<>();

    public static void tick() {
        long time = System.currentTimeMillis();
        synchronized (lock) {
            for (ScheduleTask task : tasks.values()) {
                if (task.getStartTime() + task.getDelay() <= time) {
                    task.getTask().run();
                    tasks.remove(task.getTaskId());
                } else {
                    if (task.getLastRunPeriod() + task.getPeriod() <= time) {
                        if (task.getPeriodicTask() != null) {
                            task.getPeriodicTask().run();
                            task.setLastRunPeriod(time);
                        }
                    }
                }
            }
        }
    }

    public static int schedule(Runnable task, @Nullable Runnable periodicTask, long period, long delay, long startTime) {
        ScheduleTask scheduleTask = new ScheduleTask(task, periodicTask, period, delay, startTime);
        tasks.put(scheduleTask.getTaskId(), scheduleTask);
        return scheduleTask.getTaskId();
    }

    private static int generateNewId() {
        int newId = lastUsedId;
        synchronized (lock) {
            Set<Integer> usedIds = tasks.keySet();
            for (; ; ) {
                newId++;
                if (!usedIds.contains(newId)) {
                    lastUsedId = newId;
                    return newId;
                }
            }
        }
    }

    public static void cancel(Integer taskId) {
        synchronized (lock) {
            tasks.remove(taskId);
        }
    }

    public static class ScheduleTask {
        private final Runnable task;
        private final Runnable periodicTask;
        private final long period;
        private long lastRunPeriod;
        private final long delay;
        private final long startTime;
        private final int taskId;

        public ScheduleTask(Runnable task, @Nullable Runnable periodicTask, long period, long delay, long startTime) {
            this.task = task;
            this.periodicTask = periodicTask;
            this.period = period;
            this.delay = delay;
            this.taskId = generateNewId();
            this.startTime = startTime;
            this.lastRunPeriod = startTime;
        }

        public int getTaskId() {
            return taskId;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getDelay() {
            return delay;
        }

        public long getPeriod() {
            return period;
        }

        public Runnable getPeriodicTask() {
            return periodicTask;
        }

        public Runnable getTask() {
            return task;
        }

        public long getLastRunPeriod() {
            return lastRunPeriod;
        }

        public void setLastRunPeriod(long lastRunPeriod) {
            this.lastRunPeriod = lastRunPeriod;
        }
    }
}
