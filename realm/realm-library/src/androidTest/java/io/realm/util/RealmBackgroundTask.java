package io.realm.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.util.ExceptionHolder;

/**
 * Utility class for running a task on a background thread.
 *
 * This class ensures that the background Realm is automatically closed no matter the outcome of
 * the test.
 *
 * Failures can be asserted using {@code task.checkFailure()}
 */
public class RealmBackgroundTask  {

    private final RealmConfiguration configuration;
    private final ExceptionHolder exceptionHolder = new ExceptionHolder();

    public RealmBackgroundTask(RealmConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Runs the task on a background thread. Use `checkFailure` afterwards to check for errors.
     */
    public void run(final Task task) {
        final CountDownLatch jobDone = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(configuration);
                try {
                    task.run(realm);
                } catch (Throwable throwable) {
                    exceptionHolder.setException(throwable);
                } finally {
                    realm.close();
                    jobDone.countDown();
                }
            }
        }).start();

        try {
            if (!jobDone.await(10, TimeUnit.SECONDS)) {
                exceptionHolder.setError("Job timed out!");
            }
        } catch (InterruptedException e) {
            exceptionHolder.setException(e);
        }
    }

    /**
     * Calling this will {@code fail()} the unit test if an exception or failure happend on the background thread.
     */
    public void checkFailure() {
        exceptionHolder.checkFailure();
    }

    public interface Task {
        void run(Realm realm);
    }
}
