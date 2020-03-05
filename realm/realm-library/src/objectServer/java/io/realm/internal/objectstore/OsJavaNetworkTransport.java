package io.realm.internal.objectstore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Nullable;

import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.RealmApp;
import io.realm.RealmAsyncTask;
import io.realm.internal.Keep;
import io.realm.internal.KeepMember;
import io.realm.internal.NativeObject;
import io.realm.internal.RealmNotifier;
import io.realm.internal.android.AndroidCapabilities;
import io.realm.internal.android.AndroidRealmNotifier;
import io.realm.internal.async.RealmAsyncTaskImpl;
import io.realm.log.RealmLog;

/**
 * Java implementation of the transport layer exposed by ObjectStore when communicating with
 * the MongoDB Realm Server.
 */
@Keep
public abstract class OsJavaNetworkTransport {

    // Custom error codes. These must not match any HTTP response error codes
    public static int ERROR_IO = 1000;
    public static int ERROR_INTERRUPTED = 1001;
    public static int ERROR_UNKNOWN = 1002;

    /**
     * This method is being called from JNI in order to execute the network transport itself.
     * All logic around retry and parsing of results should be done by ObjectStore.
     *
     * Warning: This method is not allowed to throw. Any exception should be wrapped in a {@link Response}
     * and be returned as a result.
     *
     * @param method
     * @param url
     * @param timeoutMs
     * @param headers
     * @param body
     * @return
     */
    protected abstract Response sendRequest(String method, String url, int timeoutMs, Map<String, String> headers, String body);

//    /**
//     * This method is called from JNI when {@code realm::app::NetworkTransport} wants to execute a network
//     * request.
//     *
//     * WARNING: All threading is the responsibility of the SDK. This should therefor always be called on a
//     * looper thread and the request itself should be moved to a worker thread.
//     *
//     * This method is not allowed to throw and doing so will result in undefined behavior. All errors
//     * should be reported using {@link nativeNotifyError}
//     *
//     * @param method
//     * @param url
//     * @param timeoutMs
//     * @param headers
//     * @param body
//     */
//    @SuppressWarnings("unused")
//    public void executeRequest(String method, String url, int timeoutMs, Map<String, String> headers, String body) {
//        checkLooperThread("Starting async network requests can only be done from a Looper thread.");
//        return new Request<Void>(RealmApp.NETWORK_POOL_EXECUTOR, callback) {
//            @Override
//            public RealmUser run() throws ObjectServerError {
//                sendRequest(method, url, timeoutMs, headers, body);
//
//
//                return login(credentials);
//            }
//        }.start();
//    }


    private static void checkLooperThread(String errorMessage) {
        AndroidCapabilities capabilities = new AndroidCapabilities();
        capabilities.checkCanDeliverNotification(errorMessage);
    }

    public static class Response {

        private final int code;
        private final Map<String, String> headers;
        private final String body;

        public static Response unknownError(String stacktrace) {
            return new Response(ERROR_UNKNOWN, new HashMap<>(), stacktrace);
        }

        public static Response ioError(String stackTrace) {
            return new Response(ERROR_IO, new HashMap<>(), stackTrace);
        }

        public static Response interruptedError(String stackTrace) {
            return new Response(ERROR_INTERRUPTED, new HashMap<>(), stackTrace);
        }

        public static Response httpResponse(int statusCode, Map<String, String> responseHeaders, String body) {
            return new Response(statusCode, responseHeaders, body);
        }

        private Response(int code, Map<String, String> headers, String body) {
            this.code = code;
            this.headers = headers;
            this.body = body;
        }

        public int getCode() {
            return code;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public String getBody() {
            return body;
        }
    }

    /**
     * Callback used when sending back results from network requests performed by ObjectStores
     * {@code realm::app::NetworkTransport}.
     *
     * The callback will happen on the thread running the network request, not the intended receiver thread.
     */
    @Keep
    public interface NetworkTransportJNIResultCallback {
        void onSuccess(Object result);
        void onError(String nativeErrorCategory, int nativeErrorCode, String errorMessage);
    }
}
