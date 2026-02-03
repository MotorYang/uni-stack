package com.github.motoryang.common.utils;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Exponential backoff execution utility
 */
public class BackoffUtils {

    private BackoffUtils() {
    }

    /**
     * A Runnable interface that supports throwing exceptions
     */
    @FunctionalInterface
    public interface RunnableTask {
        void run() throws Exception;
    }

    /**
     * Execute a void task with exponential backoff
     *
     * @param task         task to execute
     * @param maxRetries   maximum number of retries
     * @param initialDelay initial delay between retries
     * @param multiplier   delay multiplier
     */
    public static void execute(RunnableTask task, int maxRetries, Duration initialDelay, double multiplier) {
        execute(() -> {
            task.run();
            return null;
        }, maxRetries, initialDelay, multiplier);
    }

    /**
     * Execute a task with return value using exponential backoff
     *
     * @param task         task to execute
     * @param maxRetries   maximum number of retries
     * @param initialDelay initial delay between retries
     * @param multiplier   delay multiplier
     * @param <T>          return type
     * @return task result
     */
    public static <T> T execute(Callable<T> task, int maxRetries, Duration initialDelay, double multiplier) {
        int attempt = 0;
        long currentDelayMillis = initialDelay.toMillis();
        Exception lastException = null;

        while (attempt < maxRetries) {
            try {
                return task.call();
            } catch (Exception e) {
                lastException = e;
                attempt++;
                if (attempt >= maxRetries) {
                    break;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(currentDelayMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry thread interrupted", ie);
                }
                currentDelayMillis = (long) (currentDelayMillis * multiplier);
            }
        }

        if (lastException == null) {
            throw new RuntimeException("Retry logic error: task not executed or exception not caught");
        }
        throw new RuntimeException("Retry exhausted, last exception: " + lastException.getMessage(), lastException);
    }
}
