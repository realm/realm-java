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

import android.os.SystemClock;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
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
    // FIXME re-adjust timeout after https://github.com/realm/realm-object-server-private/issues/697 is fixed
    private final static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(40, TimeUnit.SECONDS)
            .readTimeout(40, TimeUnit.SECONDS)// since ROS startup timeout is 30s
            .build();

    // adb reverse tcp:8888 tcp:8888
    // will forward this query to the host, running the integration test server on 8888
    private static final String START_SERVER = "http://127.0.0.1:8888/start";
    private static final String STOP_SERVER = "http://127.0.0.1:8888/stop";
    public static final String TAG = "IntegrationTestServer";

    /**
     * Start the sync server. If the server has been started before, stop it first.
     */
    public static void startSyncServer() throws Exception {
        Request request = new Request.Builder()
                .url(START_SERVER)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
    }

    /**
     * Stop the sync server if it is alive. {@link #startSyncServer()} will implicitly stop the server if needed, so
     * normally there is no need to call this.
     */
    public static void stopSyncServer() throws Exception {
        Request request = new Request.Builder()
                .url(STOP_SERVER)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
    }
}
