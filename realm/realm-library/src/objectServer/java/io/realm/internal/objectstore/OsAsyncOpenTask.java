package io.realm.internal.objectstore;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.internal.ErrorCategory;
import io.realm.mongodb.ErrorCode;
import io.realm.mongodb.AppException;
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
    private final AtomicReference<ErrorCode> errorCode = new AtomicReference<>(null);
    private final AtomicReference<String> errorMessage = new AtomicReference<>(null);

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

        ErrorCode errorCode = this.errorCode.get();
        String errorMessage = this.errorMessage.get();
        if (errorCode != null && errorMessage != null) {
            throw new AppException(errorCode, errorMessage);
        }
    }

    /**
     * Called from JNI when the underlying async task has successfully downloaded the Realm.
     */
    @KeepMember
    @SuppressWarnings("unused")
    private void notifyRealmReady() {
        errorCode.set(null);
        errorMessage.set(null);
        taskComplete.countDown();
    }

    /**
     * Called from JNI when the underlying async task encounters an error.
     */
    @KeepMember
    @SuppressWarnings("unused")
    private void notifyError(byte nativeErrorCategory, int nativeErrorCode, String errorMessage) {
        ErrorCode errorCode = ErrorCode.fromNativeError(ErrorCategory.toCategory(nativeErrorCategory), nativeErrorCode);
        this.errorCode.set(errorCode);
        this.errorMessage.set(errorMessage);
        taskComplete.countDown();
    }

    private native long start(long configPtr);
    private native void cancel(long nativePtr);
}
