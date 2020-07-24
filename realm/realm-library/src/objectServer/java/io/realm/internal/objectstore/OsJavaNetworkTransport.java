/*
 * Copyright 2020 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.realm.internal.objectstore;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.realm.internal.Keep;
import io.realm.mongodb.AppConfiguration;

/**
 * Java implementation of the transport layer exposed by ObjectStore when communicating with
 * the MongoDB Realm Server.
 */
@Keep
public abstract class OsJavaNetworkTransport {

    // Custom error codes. These must not match any HTTP response error codes
    public static final int ERROR_IO = 1000;
    public static final int ERROR_INTERRUPTED = 1001;
    public static final int ERROR_UNKNOWN = 1002;

    // Header configuration
    private String authorizationHeaderName;
    private Map<String, String> customHeaders = new HashMap<>();

    /**
     * This method is being called from JNI in order to execute the network transport itself.
     * All logic around retry and parsing of results should be done by ObjectStore.
     *
     * Warning: This method is not allowed to throw. Any exception should be wrapped in a {@link Response}
     * and be returned as a result.
     *
     * @param method Which kind of HTTP method in lowercase.
     * @param url Url to connect to.
     * @param timeoutMs How long does the request has to complete?
     * @param headers Which headers to send?
     * @param body Which body to include?
     * @return Result of the request. All exceptions should also be wrapped in this.
     */
    protected abstract Response sendRequest(String method, String url, long timeoutMs, Map<String, String> headers, String body);

    public abstract Response sendStreamingRequest(Request request) throws IOException;

    public void setAuthorizationHeaderName(String headerName) {
        authorizationHeaderName = headerName;
    }

    public void addCustomRequestHeader(String headerName, String headerValue) {
        customHeaders.put(headerName, headerValue);
    }

    public String getAuthorizationHeaderName() {
        return authorizationHeaderName;
    }

    public Map<String, String> getCustomRequestHeaders() {
        return customHeaders;
    }

    /**
     * Reset all configured headers to their default.
     * Used for testing.
     */
    public void resetHeaders() {
        authorizationHeaderName = AppConfiguration.DEFAULT_AUTHORIZATION_HEADER_NAME;
        customHeaders.clear();
    }

    public static abstract class Response implements Closeable {
        private final int httpResponseCode;
        private final int customResponseCode;
        private final Map<String, String> headers;
        private final String body;

        protected Response(int httpResponseCode, int customResponseCode, Map<String, String> headers, String body) {
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

        public String readBodyLine() throws IOException {
            return null;
        }

        public boolean isOpen() {
            return false;
        }


        @Override
        public String toString() {
            return "Response{" +
                    "httpResponseCode=" + httpResponseCode +
                    ", customResponseCode=" + customResponseCode +
                    ", headers=" + headers +
                    ", body='" + body + '\'' +
                    '}';
        }
    }

    public static class Request {
        private String method;
        private String url;
        private Map<String, String> headers;
        private String body;

        public Request(String method, String url, Map<String, String> headers, String body) {
            this.method = method;
            this.url = url;
            this.headers = headers;
            this.body = body;
        }

        public String getMethod() {
            return method;
        }

        public String getUrl() {
            return url;
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
    // Abstract because these methods needs to be called from JNI and we cannot look up interface methods.
    @Keep
    public static abstract class NetworkTransportJNIResultCallback {
        public void onSuccess(Object result) {}
        public void onError(String nativeErrorCategory, int nativeErrorCode, String errorMessage) {}
    }
}
