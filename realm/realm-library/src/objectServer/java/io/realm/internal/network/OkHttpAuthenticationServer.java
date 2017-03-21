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

package io.realm.internal.network;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.SyncCredentials;
import io.realm.internal.objectserver.Token;
import io.realm.log.RealmLog;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpAuthenticationServer implements AuthenticationServer {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * Authenticate the given credentials on the specified Realm Authentication Server.
     */
    @Override
    public AuthenticateResponse loginUser(SyncCredentials credentials, URL authenticationUrl) {
        try {
            String requestBody = AuthenticateRequest.userLogin(credentials).toJson();
            return authenticate(authenticationUrl, requestBody);
        } catch (Exception e) {
            return AuthenticateResponse.from(new ObjectServerError(ErrorCode.UNKNOWN, e));
        }
    }

    @Override
    public AuthenticateResponse loginToRealm(Token refreshToken, URI serverUrl, URL authenticationUrl) {
        try {
            String requestBody = AuthenticateRequest.realmLogin(refreshToken, serverUrl).toJson();
            return authenticate(authenticationUrl, requestBody);
        } catch (Exception e) {
            return AuthenticateResponse.from(new ObjectServerError(ErrorCode.UNKNOWN, e));
        }
    }

    @Override
    public AuthenticateResponse refreshUser(Token userToken, URI serverUrl, URL authenticationUrl) {
        try {
            String requestBody = AuthenticateRequest.userRefresh(userToken, serverUrl).toJson();
            return authenticate(authenticationUrl, requestBody);
        } catch (Exception e) {
            return AuthenticateResponse.from(new ObjectServerError(ErrorCode.UNKNOWN, e));
        }
    }

    @Override
    public LogoutResponse logout(Token userToken, URL authenticationUrl) {
        try {
            String requestBody = LogoutRequest.revoke(userToken).toJson();
            return logout(buildLogoutUrl(authenticationUrl), requestBody);
        } catch (Exception e) {
            return LogoutResponse.from(new ObjectServerError(ErrorCode.UNKNOWN, e));
        }
    }

    private static URL buildLogoutUrl(URL authenticationUrl) {
        final String baseUrlString = authenticationUrl.toExternalForm();
        try {
            if (baseUrlString.endsWith("/")) {
                return new URL(baseUrlString + "revoke");
            } else {
                return new URL(baseUrlString + "/revoke");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private AuthenticateResponse authenticate(URL authenticationUrl, String requestBody) throws Exception {
        Request request = new Request.Builder()
                .url(authenticationUrl)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .post(RequestBody.create(JSON, requestBody))
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        return AuthenticateResponse.from(response);
    }

    private LogoutResponse logout(URL logoutUrl, String requestBody) throws Exception {
        Request request = new Request.Builder()
                .url(logoutUrl)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .post(RequestBody.create(JSON, requestBody))
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        return LogoutResponse.from(response);
    }
}
