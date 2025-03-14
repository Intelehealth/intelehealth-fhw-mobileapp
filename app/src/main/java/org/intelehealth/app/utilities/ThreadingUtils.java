package org.intelehealth.app.utilities;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by - Prajwal W. on 14/03/25.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/
public class ThreadingUtils {

    public static void executeInBackground(Runnable task) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(task);
        System.out.println("ThreadingUtils.execute");
    }
}
