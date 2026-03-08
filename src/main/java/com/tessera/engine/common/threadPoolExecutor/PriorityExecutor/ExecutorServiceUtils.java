package com.tessera.engine.common.threadPoolExecutor.PriorityExecutor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class ExecutorServiceUtils {
    public static void cancelAllTasks(ThreadPoolExecutor executor) {
        for (Runnable scheduledTask : executor.getQueue()) {
            // Cast to access the Future method cancel
            Future<?> future = (Future<?>) scheduledTask;
            // Cancel scheduled but not started tasks
            future.cancel(false);

            //Wait for the task to finish
            try {
                future.get();
            } catch (java.util.concurrent.CancellationException | InterruptedException ex) {
                //Concurrent cancellation exceptions are normal, so they are ignored
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
