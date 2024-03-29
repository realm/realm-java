package io.realm.internal.network;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.realm.internal.async.RealmThreadPoolExecutor;
import io.realm.internal.objectstore.OsJavaNetworkTransport;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.AppException;
import io.realm.mongodb.ErrorCode;
import io.realm.mongodb.log.obfuscator.HttpLogObfuscator;
import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSource;

public class OkHttpNetworkTransport extends OsJavaNetworkTransport {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private volatile OkHttpClient client = null;
    private volatile OkHttpClient streamClient = null;

    @Nullable
    private final HttpLogObfuscator httpLogObfuscator;
    // Cannot use App.NETWORK_POOL_EXECUTOR as they end up blocking each other.
    private final ThreadPoolExecutor threadPool = RealmThreadPoolExecutor.newDefaultExecutor();

    public OkHttpNetworkTransport(@Nullable HttpLogObfuscator httpLogObfuscator) {
        this.httpLogObfuscator = httpLogObfuscator;
    }

    private okhttp3.Request createRequest(String method, String url, Map<String, String> headers, String body){
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder().url(url);

        // Ensure that we have correct custom headers until OS handles it.

        // 1. First of all add all custom headers
        for (Map.Entry<String, String> entry : getCustomRequestHeaders().entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        // 2. Then replace default authorization header with custom one if present
        String authorizationHeaderValue = headers.get(AppConfiguration.DEFAULT_AUTHORIZATION_HEADER_NAME);
        String authorizationHeaderName = getAuthorizationHeaderName();
        if (authorizationHeaderValue != null && !AppConfiguration.DEFAULT_AUTHORIZATION_HEADER_NAME.equals(authorizationHeaderName)) {
            headers.remove(AppConfiguration.DEFAULT_AUTHORIZATION_HEADER_NAME);
            headers.put(authorizationHeaderName, authorizationHeaderValue);
        }

        // 3. Finally add all headers defined by Object Store
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        
        switch (method) {
            case "get":
                builder.get();
                break;
            case "delete":
                builder.delete(RequestBody.create(JSON, body));
                break;
            case "patch":
                builder.patch(RequestBody.create(JSON, body));
                break;
            case "post":
                builder.post(RequestBody.create(JSON, body));
                break;
            case "put":
                builder.put(RequestBody.create(JSON, body));
                break;
            default:
                throw new IllegalArgumentException("Unknown method type: " + method);
        }

        return builder.build();
    }


    @SuppressFBWarnings("REC_CATCH_EXCEPTION")
    @Override
    public void sendRequestAsync(String method,
                            String url,
                            long timeoutMs,
                            Map<String, String> headers,
                            String body,
                            long completionBlockPtr) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    OsJavaNetworkTransport.Response response = OkHttpNetworkTransport.this.executeRequest(method, url, timeoutMs, headers, body);
                    OkHttpNetworkTransport.this.handleResponse(response, completionBlockPtr);
                } catch (Error e) {
                    OkHttpNetworkTransport.this.handleResponse(Response.unknownError(e.toString()), completionBlockPtr);
                }
            }
        });
    }

    @Override
    public OsJavaNetworkTransport.Response executeRequest(String method, String url, long timeoutMs, Map<String, String> headers, String body) {
        try {
            OkHttpClient client = getClient(timeoutMs);

            okhttp3.Response response = null;
            try {
                okhttp3.Request request = createRequest(method, url, headers, body);

                Call call = client.newCall(request);
                response = call.execute();
                ResponseBody responseBody = response.body();
                String result = "";
                if (responseBody != null) {
                    result = responseBody.string();
                }
                return Response.httpResponse(response.code(), parseHeaders(response.headers()), result);
            } catch (IOException ex) {
                return Response.ioError(ex.toString());
            } catch (Exception ex) {
                return Response.unknownError(ex.toString());
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        } catch (Exception e) {
            return Response.unknownError(e.toString());
        }
    }

    @Override
    public OsJavaNetworkTransport.Response sendStreamingRequest(Request request) throws IOException, AppException {
        OkHttpClient client = getStreamClient();

        okhttp3.Request okRequest = createRequest(request.getMethod(), request.getUrl(), request.getHeaders(), request.getBody());

        Call call = client.newCall(okRequest);
        okhttp3.Response response = call.execute();

        if ((response.code() >= 300) || ((response.code() < 200) && (response.code() != 0))) {
            throw new AppException(ErrorCode.fromNativeError(ErrorCode.Type.HTTP, response.code()), response.message());
        }

        return Response.httpResponse(response.code(), parseHeaders(response.headers()), response.body().source());
    }

    @Override
    public void reset() {
        try {
            threadPool.shutdownNow();
            threadPool.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Threadpool did not terminate in time", e);
        }
        super.reset();
    }

    // Lazily creates the client if not already created
    // TODO: timeOuts are not expected to change between requests. So for now just use the timeout first send.
    private synchronized OkHttpClient getClient(long timeoutMs) {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    // TODO There doesn't appear to set a timeout for the entire http request,
                    //  so just reuse it across all 3 phases. In the worst case that means the
                    //  the timeout is 3x the defined timeout which is acceptable.
                    .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                    .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                    .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                    .followRedirects(true)
                    .addInterceptor(new LoggingInterceptor(httpLogObfuscator))
                    // using custom Connection Pool to evict idle connection after 5 seconds rather than 5 minutes (which is the default)
                    // keeping idle connection on the pool will prevent the ROS to be stopped, since the HttpUtils#stopSyncServer query
                    // will not return before the tests timeout (ex 10 seconds for AuthTests)
                    .connectionPool(new ConnectionPool(5, 5, TimeUnit.SECONDS))
                    .build();
        }

        return client;
    }

    private synchronized OkHttpClient getStreamClient() {
        if (streamClient == null) {
            streamClient = new OkHttpClient.Builder()
                    .readTimeout(0, TimeUnit.MILLISECONDS)
                    .followRedirects(true)
                    .addInterceptor(new LoggingInterceptor(httpLogObfuscator))
                    .build();
        }

        return streamClient;
    }

    // Parse Headers output from OKHttp to the format expected by ObjectStore
    private Map<String, String> parseHeaders(Headers headers) {
        HashMap<String, String> osHeaders = new HashMap<>(headers.size() / 2);
        for (String key : headers.names()) {
            osHeaders.put(key, headers.get(key));
        }
        return osHeaders;
    }

    public static class Response extends OsJavaNetworkTransport.Response {
        private BufferedSource bufferedSource;
        private volatile boolean closed;

        public static OsJavaNetworkTransport.Response unknownError(String stacktrace) {
            return new Response(0, ERROR_UNKNOWN, new HashMap<>(), stacktrace);
        }

        public static OsJavaNetworkTransport.Response ioError(String stackTrace) {
            return new Response(0, ERROR_IO, new HashMap<>(), stackTrace);
        }

        public static OsJavaNetworkTransport.Response interruptedError(String stackTrace) {
            return new Response(0, ERROR_INTERRUPTED, new HashMap<>(), stackTrace);
        }

        public static OsJavaNetworkTransport.Response httpResponse(int statusCode, Map<String, String> responseHeaders, String body) {
            return new Response(statusCode, 0, responseHeaders, body);
        }

        private Response(int httpResponseCode, int customResponseCode, Map<String, String> headers, String body) {
            super(httpResponseCode, customResponseCode, headers, body);
        }

        private Response(int httpResponseCode, Map<String, String> headers, BufferedSource bufferedSource) {
            super(httpResponseCode, 0, headers, "");

            this.bufferedSource = bufferedSource;
        }

        public static OsJavaNetworkTransport.Response httpResponse(int httpResponseCode, Map<String, String> headers, BufferedSource originalResponse) {
            return new Response(httpResponseCode, headers, originalResponse);
        }

        @Override
        public String readBodyLine() throws IOException {
            if (!closed){
                return bufferedSource.readUtf8LineStrict();
            } else{
                bufferedSource.close();
                throw new IOException("Stream closed");
            }
        }

        /**
         * Closes the current stream.
         *
         * Note: we use a close flag because okio buffers are not thread safe:
         * @see <a href="http://google.com">https://github.com/square/okio/issues/163#issuecomment-127052956</a>
         */
        @Override
        public void close() {
            closed = true;
        }

        @Override
        public boolean isOpen() {
            return !closed && bufferedSource.isOpen();
        }
    }

}

