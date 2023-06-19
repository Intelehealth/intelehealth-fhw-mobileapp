package org.intelehealth.ezazi.executor;

import android.util.Log;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void executeTask(TaskCompleteListener<T> callable) {
        Future<T> future = executor.submit(callable);
//        Future<?> future = executor.submit(() -> {
//            try {
//                T result = callable.call();
//                callable.onComplete(result);
//            } catch (Exception e) {
//                Log.e(TAG, e.getLocalizedMessage());
//                throw new RuntimeException(e);
//            }
//        });
        executor.shutdown();
        try {
            if (future.isDone()) {
                callable.onComplete(future.get());
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void executeAll(List<TaskCompleteListener<T>> tasks) {
        try {
            List<Future<T>> futures = executor.invokeAll(tasks);
            for (int i = 0; i < tasks.size(); i++) {
                if (futures.get(i).isDone()) {
                    tasks.get(i).onComplete(futures.get(i).get());
                }
            }
            executor.shutdown();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
