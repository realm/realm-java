/*
 * Copyright 2016 Realm Inc.
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

package io.realm.objectserver.utils;

import android.util.Log;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import io.realm.log.RealmLog;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Start and Stop the node server responsible of creating a
 * temp directory & start a sync server on it for each unit test.
 *
 * WARNING: This class is called before Realm is initialized, so RealmLog cannot be used.
 */
public class HttpUtils {
    // TODO If the timeouts are longer than the test timeout you risk getting
    // "Realm could not be deleted errors".
    private final static OkHttpClient client = new OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    // adb reverse tcp:8888 tcp:8888
    // will forward this query to the host, running the integration test server on 8888
    private static final String START_SERVER = "http://127.0.0.1:8888/start";
    private static final String STOP_SERVER = "http://127.0.0.1:8888/stop";
    public static final String TAG = "IntegrationTestServer";

    public static void startSyncServer() throws Exception {
        Request request = new Request.Builder()
                .url(START_SERVER)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        Headers responseHeaders = response.headers();
        for (int i = 0; i < responseHeaders.size(); i++) {
            Log.d(TAG, responseHeaders.name(i) + ": " + responseHeaders.value(i));
        }
        Log.d(TAG, response.body().string());
    }

    public static void stopSyncServer() throws Exception {
        Request request = new Request.Builder()
                .url(STOP_SERVER)
                .build();

        Response response;
        int attempts = 3;
        while (attempts > 0) {
            attempts--;
            try {
                response = client.newCall(request).execute();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                return;
            } catch (SocketTimeoutException e) {
                RealmLog.error("HTTP test server did not respond to /stop in time. Retrying.");
            }
        }

        throw new IllegalStateException("Failed to stop ROS.");
    }
}
