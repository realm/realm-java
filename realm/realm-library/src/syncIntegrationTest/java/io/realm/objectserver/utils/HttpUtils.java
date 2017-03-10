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

import java.io.IOException;

import io.realm.log.RealmLog;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Start and Stop the node server responsible of creating a
 * temp directory & start a sync server on it for each unit test.
 */
public class HttpUtils {
    private final static OkHttpClient client = new OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .build();

    // adb reverse tcp:8888 tcp:8888
    // will forward this query to the host, running the integration test server on 8888
    private final static String START_SERVER = "http://127.0.0.1:8888/start";
    private final static String STOP_SERVER = "http://127.0.0.1:8888/stop";

    public static void startSyncServer() throws Exception {
        Request request = new Request.Builder()
                .url(START_SERVER)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        Headers responseHeaders = response.headers();
        for (int i = 0; i < responseHeaders.size(); i++) {
            RealmLog.debug(responseHeaders.name(i) + ": " + responseHeaders.value(i));
        }

        RealmLog.debug(response.body().string());

        // FIXME: Server ready checking should be done in the control server side!
        if (!waitAuthServerReady()) {
            stopSyncServer();
            throw new RuntimeException("Auth server cannot be started.");
        }
    }

    // Checking the server
    private static boolean waitAuthServerReady() throws InterruptedException {
        int retryTimes = 20;

        // Dummy invalid request, which will trigger a 400 (BAD REQUEST), but indicate the auth
        // server is responsive
        Request request = new Request.Builder()
                .url(Constants.AUTH_SERVER_URL)
                .build();

        while (retryTimes != 0) {
            Response response = null;
            try {
                response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    return true;
                }
                RealmLog.error("Error response from auth server: %s", response.toString());
            } catch (IOException e) {
                // TODO As long as the auth server hasn't started yet, OKHttp cannot parse the response
                // correctly. At this point it is unknown weather is a bug in OKHttp or an
                // unknown host is reported. This can cause a lot of "false" errors in the log.
                RealmLog.error(e);
                Thread.sleep(500);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
            retryTimes--;
        }

        return false;
    }

    public static void stopSyncServer() throws Exception {
        Request request = new Request.Builder()
                .url(STOP_SERVER)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        Headers responseHeaders = response.headers();
        for (int i = 0; i < responseHeaders.size(); i++) {
            RealmLog.debug(responseHeaders.name(i) + ": " + responseHeaders.value(i));
        }

        RealmLog.debug(response.body().string());
    }
}
