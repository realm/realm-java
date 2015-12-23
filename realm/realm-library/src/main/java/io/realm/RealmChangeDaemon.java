package io.realm;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.internal.SharedGroup;
import io.realm.internal.log.RealmLog;

/**
 * Daemon thread sleeps until one ore more commits have been made to the corresponding Realm. When change happens, the
 * callback will be run in the daemon thread and then the thread go back to sleep again.
 * There is no guarantee that every single change will trigger the callback once, but it is guaranteed that the latest
 * change will trigger the callback.
 */
class RealmChangeDaemon {

    public interface Callback {
        void onChanged(RealmConfiguration configuration);
    }

    private final static int STOP_TIMEOUT_IN_SECONDS = 2;

    private final SharedGroup sharedGroup;
    private final Callback callback;
    private static final ThreadFactory threadFactory = new ThreadFactory() {
        AtomicInteger counter = new AtomicInteger(0);
        @Override
        public Thread newThread(@SuppressWarnings("NullableProblems") Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("RealmChangeDaemon_" + counter.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    };
    private static ExecutorService executorService = Executors.newCachedThreadPool(threadFactory);
    private CountDownLatch stoppedLatch = new CountDownLatch(1);
    private final RealmConfiguration configuration;

    /**
     * Creates and starts a new RealmChangeDaemon with given initial parameters.
     *
     * @param configuration the {@link RealmConfiguration} refers to the Realm which will be watched by this daemon.
     * @param onChangedCallback the callback will be ran when the changes happen to the given Realm.
     */
    public RealmChangeDaemon(final RealmConfiguration configuration, Callback onChangedCallback) {
        this.configuration = configuration;
        this.sharedGroup = new SharedGroup(
                configuration.getPath(),
                SharedGroup.IMPLICIT_TRANSACTION,
                configuration.getDurability(),
                configuration.getEncryptionKey());
        this.callback = onChangedCallback;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (sharedGroup.waitForChange()) {
                    callback.onChanged(configuration);
                    sharedGroup.beginRead();
                    sharedGroup.endRead();
                }
                sharedGroup.close();
                stoppedLatch.countDown();
            }
        };

        executorService.execute(runnable);
    }

    /**
     * Stops the daemon thread.
     */
    public void stop() {
        sharedGroup.setWaitForChangeEnabled(false);
        try {
            // Two seconds should be more than enough to stop daemon. And timeout should never happen!
            if(!stoppedLatch.await(STOP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)) {
                RealmLog.e("Timeout happened when stop RealmChangeDaemon for RealmConfiguration:\n"
                        + configuration.toString());
            }
        } catch (InterruptedException e) {
            RealmLog.e("An exception was thrown when stop RealmChangeDaemon for RealmConfiguration:\n"
                    + configuration.toString() + "\n The exception is: " + e.getMessage());
        }
    }
}
