package org.intelehealth.app.utilities;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by - Prajwal W. on 14/03/25.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/
public class ThreadingUtils {

    /*public static void executeInBackground(Runnable task) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(task);
        System.out.println("ThreadingUtils.execute");
    }*/

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void executeInBackground(Runnable task) {
        if (!executorService.isShutdown()) {
            executorService.execute(task);
            System.out.println("ThreadingUtils.execute");
        } else {
            System.err.println("Executor is already shut down. Task ignored.");
        }
    }

    /*public static void shutdownExecutor() {   // TODO : avoiding calling this since we are having multiple long running items...
        executorService.shutdown();  // Stop accepting new tasks
        try {
            if (!executorService.awaitTermination(3, TimeUnit.SECONDS)) {  // Wait for 3 sec
                executorService.shutdownNow();  // Force stop remaining tasks
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }*/
}
