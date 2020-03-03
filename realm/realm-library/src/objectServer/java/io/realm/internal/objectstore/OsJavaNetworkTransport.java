package io.realm.internal.objectstore;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import io.realm.internal.Keep;
import io.realm.internal.KeepMember;
import io.realm.internal.NativeObject;

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
    public abstract Response sendRequest(String method, String url, int timeoutMs, Map<String, String> headers, String body);

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
    }
}
