package io.realm;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.internal.SharedGroup;

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

    private final SharedGroup sharedGroup;
    private final Callback callback;
    private static final ThreadFactory threadFactory = new ThreadFactory() {
        AtomicInteger counter = new AtomicInteger(0);
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "RealmChangeDaemon_" + counter.getAndIncrement());
            return thread;
        }
    };
    private static ExecutorService executorService = Executors.newCachedThreadPool(threadFactory);

    /**
     * Creates and starts a new RealmChangeDaemon with given initial parameters.
     *
     * @param configuration the {@link RealmConfiguration} refers to the Realm which will be watched by this daemon.
     * @param onChangedCallback the callback will be ran when the changes happen to the given Realm.
     */
    public RealmChangeDaemon(final RealmConfiguration configuration, Callback onChangedCallback) {
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
            }
        };

        executorService.execute(runnable);
    }

    /**
     * Stops the daemon thread.
     */
    public void stop() {
        sharedGroup.setWaitForChangeEnabled(false);
    }
}
