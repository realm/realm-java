package io.realm.internal.network;

import android.os.Looper;
import android.os.NetworkOnMainThreadException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.internal.ErrorCategory;
import io.realm.internal.Keep;
import io.realm.internal.objectstore.OsJavaNetworkTransport;
import io.realm.log.RealmLog;
import io.realm.mongodb.AppException;
import io.realm.mongodb.ErrorCode;

/**
 * Class wrapping the back-and-forth required by the Network Transport layer in ObjectStore.
 *
 * This class wraps the request itself as well as handles the completion block triggered by
 * ObjectStore.
 *
 * That API exposed by this class is synchronous, but the callbacks from ObjectStore will happen
 * on a thread determined by the {@link OsJavaNetworkTransport} implementation. In release builds,
 * this is {@link OkHttpNetworkTransport} which run on a special Looper thread.
 */
@Keep
public abstract class NetworkRequest<T> extends OsJavaNetworkTransport.NetworkTransportJNIResultCallback {

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<T> success = new AtomicReference<>(null);
    AtomicReference<AppException> error = new AtomicReference<>(null);

    /**
     * In the case of a successful request. Map the return value back to the type
     * expected by top-level consumers.
     */
    protected abstract T mapSuccess(Object result);

    /**
     * Responsible for doing the network request by calling the relevant ObjectStore method.
     */
    protected abstract void execute(NetworkRequest<T> callback);

    /**
     * Request was successful. Will be called by the ObjectStore completion handler.
     */
    @SuppressWarnings("unused") // Called by JNI
    @Override
    public void onSuccess(Object result) {
        T mappedResult = mapSuccess(result);
        success.set(mappedResult);
        latch.countDown();
    }

    /**
     * Request failed. Will be called by the ObjectStore completion handler.
     */
    @SuppressWarnings("unused")  // Called by JNI
    @Override
    public void onError(byte nativeErrorCategory, int nativeErrorCode, String errorMessage) {
        ErrorCode code = ErrorCode.fromNativeError(ErrorCategory.toCategory(nativeErrorCategory), nativeErrorCode);
        if (code == ErrorCode.UNKNOWN) {
            // In case of UNKNOWN errors parse as much error information on as possible.
            String detailedErrorMessage = String.format("{%s::%s} %s", nativeErrorCategory, nativeErrorCode, errorMessage);
            error.set(new AppException(code, detailedErrorMessage));
        } else {
            error.set(new AppException(code, errorMessage));
        }
        latch.countDown();
    }

    /**
     * Run the network request and wait for the result.
     * If the request was a success, the result is returned. 
     * 
     * If not, an error occurred and it will be thrown as an AppException.
     */
    public T resultOrThrow() {

        // Avoid changing behavior. Old implementation would throw an exception if
        // this method was called from the UI thread
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new AppException(ErrorCode.NETWORK_UNKNOWN, new NetworkOnMainThreadException());
        }

        execute(this);
        try {
            // Wait indefinitely. Timeouts should be handled by the Network layer, otherwise
            // it can be interrupted manually by calling `RealmAsyncTask.cancel()`
            latch.await();
        } catch (InterruptedException e) {
            error.set(new AppException(ErrorCode.NETWORK_INTERRUPTED, "Network request interrupted."));
        }

        // Result of request should be available. Throw if an error happened, otherwise return
        // the result.
        if (error.get() != null) {
            throw error.get();
        } else {
            return success.get();
        }
    }
}
