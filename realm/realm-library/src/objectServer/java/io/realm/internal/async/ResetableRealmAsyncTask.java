package io.realm.internal.async;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import io.realm.internal.network.ExponentialBackoffTask;

/**
 * Wrapper class for tasks that can internally retry operations until they succeed.
 *
 * Using this class will automatically add the task to the ThreadPoolExecutor.
 */
public class ResetableRealmAsyncTask extends RealmAsyncTaskImpl {

    private final ExponentialBackoffTask<?> task;

    public ResetableRealmAsyncTask(ExponentialBackoffTask<?> pendingTask, ThreadPoolExecutor service) {
        super(service.submit(pendingTask) , service);
        this.task = pendingTask;
    }

    /**
     * If this task is currently waiting to retry. Calling this method will reset it and make it
     * retry again immediately.
     */
    public void resetTask() {
        task.resetDelay();
    }
}
