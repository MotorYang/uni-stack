package com.github.motoryang.common.utils;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 指数回避执行特定方法工具类
 */
public class BackoffUtils {

    /**
     * 一个支持抛出异常的 Runnable 接口
     */
    @FunctionalInterface
    public interface  RunnableTask {
        void run() throws Exception;
    }

    /**
     * 指数回避调用无返回值的任务
     *
     * @param task          需要执行的任务 (Supplier)
     * @param maxRetries    最大重试次数
     * @param initialDelay  初始等待时间
     * @param multiplier    时间倍数
     */
    public static void execute(RunnableTask task, int maxRetries, Duration initialDelay, double multiplier) {
        execute(() -> {
            task.run();
            return null;
        }, maxRetries, initialDelay, multiplier);
    }

    /**
     * 指数回避调用带有返回值的任务
     *
     * @param task          需要执行的任务 (Supplier)
     * @param maxRetries    最大重试次数
     * @param initialDelay  初始等待时间
     * @param multiplier    时间倍数
     * @param <T>           返回类型
     * @return 任务执行结果
     */
    public static <T> T execute(Callable<T> task, int maxRetries, Duration initialDelay, double multiplier) {
        int attempt = 0;
        long currentDelayMilis = initialDelay.toMillis();
        Exception lastException = null;
        while (attempt < maxRetries) {
            try {
                return task.call();
            } catch (Exception e) {
                lastException = e;
                attempt++;
                if (attempt > maxRetries) {
                    break;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(currentDelayMilis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw  new RuntimeException("重试线程被中断", ie);
                }
                currentDelayMilis = (long) (currentDelayMilis * multiplier);
            }
        }
        if (lastException == null) {
            throw new RuntimeException("重试逻辑异常：任务未执行或未捕捉到异常");
        }
        throw new RuntimeException("重试次数耗尽，最后一次执行异常: " + lastException.getMessage(), lastException);
    }

}
