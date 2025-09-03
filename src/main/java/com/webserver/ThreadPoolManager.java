package com.webserver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {
    private final ExecutorService threadPool;
    private final int threadCount;

    public ThreadPoolManager(int threadCount) {
        this.threadCount = threadCount;
        this.threadPool = Executors.newFixedThreadPool(threadCount);
        System.out.println("🧵 Thread pool created with " + threadCount + " threads");
    }

    public void execute(Runnable task) {
        threadPool.execute(task);
    }

    public void shutdown() {
        System.out.println("🛑 Shutting down thread pool...");
        threadPool.shutdown();

        try {
            if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println("⚠️  Force shutting down thread pool");
                threadPool.shutdownNow();
            } else {
                System.out.println("✅ Thread pool shut down gracefully");
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public ThreadPoolInfo getInfo() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) threadPool;
        return new ThreadPoolInfo(
                executor.getCorePoolSize(),
                executor.getActiveCount(),
                executor.getCompletedTaskCount()
        );
    }

    public static class ThreadPoolInfo {
        public final int totalThreads;
        public final int activeThreads;
        public final long completedTasks;

        public ThreadPoolInfo(int totalThreads, int activeThreads, long completedTasks) {
            this.totalThreads = totalThreads;
            this.activeThreads = activeThreads;
            this.completedTasks = completedTasks;
        }

        @Override
        public String toString() {
            return String.format("Threads: %d/%d active, Completed: %d",
                    activeThreads, totalThreads, completedTasks);
        }
    }
}
