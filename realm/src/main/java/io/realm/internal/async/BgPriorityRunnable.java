package io.realm.internal.async;

/**
 * Created by Nabil on 04/09/15.
 */
public class BgPriorityRunnable implements Runnable {
    private final Runnable runnable;

    BgPriorityRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void run() {
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        runnable.run();
    }

}
