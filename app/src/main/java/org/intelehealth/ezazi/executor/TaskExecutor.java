package org.intelehealth.ezazi.executor;

import android.util.Log;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Vaghela Mithun R. on 18-06-2023 - 11:53.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
//https://howtodoinjava.com/java/multi-threading/java-callable-future-example/
//    https://stackoverflow.com/questions/34793424/using-future-with-executorservice
//    https://stackoverflow.com/questions/2104676/java-executor-best-practices-for-tasks-that-should-run-forever

public class TaskExecutor<T> {
    private static final String TAG = "TaskExecutor";

    private static final int CORE_POOL_SIZE = 1;
    private static final int MAXIMUM_POOL_SIZE = 20;
    private static final int KEEP_ALIVE_SECONDS = 3;

    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
            new SynchronousQueue<>(), sThreadFactory);

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
        }
    };

    public void executeTask(TaskCompleteListener<T> callable) {
        threadPoolExecutor.execute(() -> {
            try {
                T result = callable.call();
                callable.onComplete(result);
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
//                throw new RuntimeException(e);
            }
        });
    }

    public void executeAll(List<TaskCompleteListener<T>> tasks) {
        try {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            List<Future<T>> futures = executorService.invokeAll(tasks);
            for (int i = 0; i < tasks.size(); i++) {
                if (futures.get(i).isDone()) {
                    tasks.get(i).onComplete(futures.get(i).get());
                }
            }
            executorService.shutdown();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, e.getLocalizedMessage());
//            throw new RuntimeException(e);
        }
    }
}
