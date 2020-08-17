package io.realm.internal.network;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.realm.mongodb.log.obfuscator.HttpLogObfuscator;
import io.realm.internal.objectstore.OsJavaNetworkTransport;
import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class OkHttpNetworkTransport extends OsJavaNetworkTransport {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private volatile OkHttpClient client = null;

    @Nullable
    private final HttpLogObfuscator httpLogObfuscator;

    public OkHttpNetworkTransport(@Nullable HttpLogObfuscator httpLogObfuscator) {
        this.httpLogObfuscator = httpLogObfuscator;
    }

    @SuppressFBWarnings("REC_CATCH_EXCEPTION")
    @Override
    public Response sendRequest(String method, String url, long timeoutMs, Map<String, String> headers, String body) {
        try {
            OkHttpClient client = getClient(timeoutMs);
            okhttp3.Response response = null;
            try {
                Request.Builder builder = new Request.Builder().url(url);
                switch(method) {
                    case "get": builder.get(); break;
                    case "delete": builder.delete(RequestBody.create(JSON, body)); break;
                    case "patch": builder.patch(RequestBody.create(JSON, body)); break;
                    case "post": builder.post(RequestBody.create(JSON, body)); break;
                    case "put": builder.put(RequestBody.create(JSON, body)); break;
                    default: throw new IllegalArgumentException("Unknown method type: "+ method);
                }

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
        } catch (Exception e) {
            return Response.unknownError(e.toString());
        }
    }

    // Lazily creates the client if not already created
    // TODO: timeOuts are not expected to change between requests. So for now just use the timeout first send.
    private synchronized OkHttpClient getClient(long timeoutMs) {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .callTimeout(timeoutMs, TimeUnit.MILLISECONDS)
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

    // Parse Headers output from OKHttp to the format expected by ObjectStore
    private Map<String, String> parseHeaders(Headers headers) {
        HashMap<String, String> osHeaders = new HashMap<>(headers.size()/2);
        for (String key : headers.names()) {
            osHeaders.put(key, headers.get(key));
        }
        return osHeaders;
    }

}

