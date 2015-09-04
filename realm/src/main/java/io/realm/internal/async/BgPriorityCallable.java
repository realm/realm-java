package io.realm.internal.async;

import java.util.concurrent.Callable;

/**
 * Created by Nabil on 04/09/15.
 */
public class BgPriorityCallable<T> implements Callable<T> {
    private final Callable<T> callable;

    BgPriorityCallable(Callable<T> callable) {
        this.callable = callable;
    }

    @Override
    public T call() throws Exception {
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        return callable.call();
    }
}
