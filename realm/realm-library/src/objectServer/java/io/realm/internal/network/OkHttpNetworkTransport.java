package io.realm.internal.network;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.realm.log.LogLevel;
import io.realm.log.RealmLog;
import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.Buffer;

public class OkHttpNetworkTransport extends JvmNetworkTransport {


    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();
                    if (RealmLog.getLevel() <= LogLevel.TRACE) {
                        StringBuilder sb = new StringBuilder(request.method());
                        sb.append(' ');
                        sb.append(request.url());
                        sb.append('\n');
                        sb.append(request.headers());
                        if (request.body() != null) {
                            // Stripped down version of https://github.com/square/okhttp/blob/master/okhttp-logging-interceptor/src/main/java/okhttp3/logging/HttpLoggingInterceptor.java
                            // We only expect request context to be JSON.
                            Buffer buffer = new Buffer();
                            request.body().writeTo(buffer);
                            sb.append(buffer.readString(UTF8));
                        }
                        RealmLog.trace("HTTP Request = \n%s", sb);
                    }
                    return chain.proceed(request);
                }
            })
            // using custom Connection Pool to evict idle connection after 5 seconds rather than 5 minutes (which is the default)
            // keeping idle connection on the pool will prevent the ROS to be stopped, since the HttpUtils#stopSyncServer query
            // will not return before the tests timeout (ex 10 seconds for AuthTests)
            .connectionPool(new ConnectionPool(5, 5, TimeUnit.SECONDS))
            .build();

    private final long nativePointer;

    public OkHttpNetworkTransport() {
        nativePointer = nativeCreate();
        initHeaders();
    }

    @Override
    public void setAuthorizationHeaderName(String headerName, @Nullable String host) {
        // TODO Send to JNI?
    }

    @Override
    public void addHeader(String headerName, String headerValue, @Nullable String host) {
        // TODO Send to JNI?
    }

    @Override
    public void clearCustomHeaderSettings() {
        // TODO Send to JNI?
    }

    private void initHeaders() {
        // TODO Send to JNI?
    }

    @Override
    public Response sendRequest(String method, String url, int timeoutMs, Map<String, String> headers, String body) {
        okhttp3.Response response = null;
        try {
            Request.Builder builder = new Request.Builder().url(url);

            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }

            Call call = client.newCall(builder.build());
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
    }

    // Parse Headers outputtet from OKHttp to the format expected by ObjectStore
    private Map<String, String> parseHeaders(Headers headers) {
        // FIXME: Parse headers
        return new HashMap<>(0);
    }

    @Override
    public long getNativePtr() {
        return 0;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return 0;
    }

    private native static long nativeCreate();
}
