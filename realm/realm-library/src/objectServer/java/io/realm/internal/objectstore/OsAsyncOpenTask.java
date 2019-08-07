package io.realm.internal.objectstore;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.internal.Keep;
import io.realm.internal.KeepMember;
import io.realm.internal.OsRealmConfig;

/**
 * Wrapper for the ASyncOpenTask in ObjectStore, which also support timeouts.
 *
 * This ObjectStore class controls its own lifecycle, i.e. discards itself once complete, so the
 * Java object does not need to implement {@link io.realm.internal.NativeObject}.
 */
@KeepMember
public class OsAsyncOpenTask {

    private final OsRealmConfig config;
    private long nativePtr;
    private final CountDownLatch taskComplete = new CountDownLatch(1);
    private final AtomicReference<String> error = new AtomicReference<>(null);

    public OsAsyncOpenTask(OsRealmConfig config) {
        this.config = config;
    }

    public void start(long timeOut, TimeUnit unit) throws InterruptedException {
        this.nativePtr = start(config.getNativePtr());

        try {
            taskComplete.await(timeOut, unit);
        } catch (InterruptedException e) {
            cancel(nativePtr);
            throw e;
        }

        String errorMessage = error.get();
        if (errorMessage != null) {
            throw new ObjectServerError(ErrorCode.UNKNOWN, errorMessage);
        }
    }

    /**
     * Called from JNI when the underlying async task has successfully downloaded the Realm.
     */
    @KeepMember
    @SuppressWarnings("unused")
    private void notifyRealmReady() {
        error.set(null);
        taskComplete.countDown();
    }

    /**
     * Called from JNI when the underlying async task encounters an error.
     */
    @KeepMember
    @SuppressWarnings("unused")
    private void notifyError(String errorMessage) {
        error.set(errorMessage);
        taskComplete.countDown();
    }

    private native long start(long configPtr);
    private native void cancel(long nativePtr);
}
