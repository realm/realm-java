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
    protected abstract Response sendRequest(String method, String url, long timeoutMs, Map<String, String> headers, String body);

    public static class Response {
        private final int httpResponseCode;
        private final int customResponseCode;
        private final Map<String, String> headers;
        private final String body;

        public static Response unknownError(String stacktrace) {
            return new Response(0, ERROR_UNKNOWN, new HashMap<>(), stacktrace);
        }

        public static Response ioError(String stackTrace) {
            return new Response(0, ERROR_IO, new HashMap<>(), stackTrace);
        }

        public static Response interruptedError(String stackTrace) {
            return new Response(0, ERROR_INTERRUPTED, new HashMap<>(), stackTrace);
        }

        public static Response httpResponse(int statusCode, Map<String, String> responseHeaders, String body) {
            return new Response(statusCode, 0, responseHeaders, body);
        }

        private Response(int httpResponseCode, int customResponseCode, Map<String, String> headers, String body) {
            this.httpResponseCode = httpResponseCode;
            this.customResponseCode = customResponseCode;
            this.headers = headers;
            this.body = body;
        }

        public int getHttpResponseCode() {
            return httpResponseCode;
        }

        public int getCustomResponseCode() {
            return customResponseCode;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        // Returns the HTTP headers in a JNI friendly way where it is being serialized to a
        // String array consisting of pairs of { key , value } pairs.
        public String[] getJNIFriendlyHeaders() {
            String[] jniHeaders = new String[headers.size() * 2];
            int i = 0;
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                jniHeaders[i] = entry.getKey();
                jniHeaders[i + 1] = entry.getValue();
                i = i + 2;
            }
            return jniHeaders;
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
    // Abstract because these methods needs to be called from JNI and we cannot look up interface methods.
    @Keep
    public static abstract class NetworkTransportJNIResultCallback {
        public void onSuccess(Object result) {}
        public void onError(String nativeErrorCategory, int nativeErrorCode, String errorMessage) {}
    }
}
