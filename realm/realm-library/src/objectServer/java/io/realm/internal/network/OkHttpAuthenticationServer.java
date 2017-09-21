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

import io.realm.SyncCredentials;
import io.realm.internal.Util;
import io.realm.internal.objectserver.Token;
import io.realm.log.RealmLog;
import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpAuthenticationServer implements AuthenticationServer {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String ACTION_LOGOUT = "revoke"; // Auth end point for logging out users
    private static final String ACTION_CHANGE_PASSWORD = "password"; // Auth end point for changing passwords
    private static final String ACTION_LOOKUP_USER_ID = "/users/:provider:/:providerId:"; // Auth end point for looking up user id

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            // using custom Connection Pool to evict idle connection after 5 seconds rather than 5 minutes (which is the default)
            // keeping idle connection on the pool will prevent the ROS to be stopped, since the HttpUtils#stopSyncServer query
            // will not return before the tests timeout (ex 10 seconds for AuthTests)
            .connectionPool(new ConnectionPool(5, 5, TimeUnit.SECONDS))
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
            return AuthenticateResponse.from(e);
        }
    }

    @Override
    public AuthenticateResponse loginToRealm(Token refreshToken, URI serverUrl, URL authenticationUrl) {
        try {
            String requestBody = AuthenticateRequest.realmLogin(refreshToken, serverUrl).toJson();
            return authenticate(authenticationUrl, requestBody);
        } catch (Exception e) {
            return AuthenticateResponse.from(e);
        }
    }

    @Override
    public AuthenticateResponse refreshUser(Token userToken, URI serverUrl, URL authenticationUrl) {
        try {
            String requestBody = AuthenticateRequest.userRefresh(userToken, serverUrl).toJson();
            return authenticate(authenticationUrl, requestBody);
        } catch (Exception e) {
            return AuthenticateResponse.from(e);
        }
    }

    @Override
    public LogoutResponse logout(Token userToken, URL authenticationUrl) {
        try {
            String requestBody = LogoutRequest.create(userToken).toJson();
            return logout(buildActionUrl(authenticationUrl, ACTION_LOGOUT), userToken.value(), requestBody);
        } catch (Exception e) {
            return LogoutResponse.from(e);
        }
    }

    @Override
    public ChangePasswordResponse changePassword(Token userToken, String newPassword, URL authenticationUrl) {
        try {
            String requestBody = ChangePasswordRequest.create(userToken, newPassword).toJson();
            return changePassword(buildActionUrl(authenticationUrl, ACTION_CHANGE_PASSWORD), userToken.value(), requestBody);
        } catch (Exception e) {
            return ChangePasswordResponse.from(e);
        }
    }

    @Override
    public ChangePasswordResponse changePassword(Token adminToken, String userId, String newPassword, URL authenticationUrl) {
        try {
            String requestBody = ChangePasswordRequest.create(adminToken, userId, newPassword).toJson();
            return changePassword(buildActionUrl(authenticationUrl, ACTION_CHANGE_PASSWORD), adminToken.value(), requestBody);
        } catch (Exception e) {
            return ChangePasswordResponse.from(e);
        }
    }

    @Override
    public LookupUserIdResponse retrieveUser(Token adminToken, String provider, String providerId, URL authenticationUrl) {
        try {
            String action = ACTION_LOOKUP_USER_ID
                .replace(":provider:", provider)
                .replace(":providerId:", providerId);
            return lookupUserId(buildActionUrl(authenticationUrl, action), adminToken.value());
        } catch (Exception e) {
            return LookupUserIdResponse.from(e);
        }
    }

    // Builds the URL for a specific auth endpoint
    private static URL buildActionUrl(URL authenticationUrl, String action) {
        final String baseUrlString = authenticationUrl.toExternalForm();
        try {
            String separator = baseUrlString.endsWith("/") ? "" : "/";
            return new URL(baseUrlString + separator + action);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private AuthenticateResponse authenticate(URL authenticationUrl, String requestBody) throws Exception {
        RealmLog.debug("Network request (authenticate): " + authenticationUrl);
        Request request = newAuthRequest(authenticationUrl)
                .post(RequestBody.create(JSON, requestBody))
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        return AuthenticateResponse.from(response);
    }

    private LogoutResponse logout(URL logoutUrl, String authToken, String requestBody) throws Exception {
        RealmLog.debug("Network request (logout): " + logoutUrl);
        Request request = newAuthRequest(logoutUrl, authToken)
                .post(RequestBody.create(JSON, requestBody))
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        return LogoutResponse.from(response);
    }

    private ChangePasswordResponse changePassword(URL changePasswordUrl, String authToken, String requestBody) throws Exception {
        RealmLog.debug("Network request (changePassword): " + changePasswordUrl);
        Request request = newAuthRequest(changePasswordUrl, authToken)
                .put(RequestBody.create(JSON, requestBody))
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        return ChangePasswordResponse.from(response);
    }

    private LookupUserIdResponse lookupUserId(URL lookupUserIdUrl, String authToken) throws Exception {
        RealmLog.debug("Network request (lookupUserId): " + lookupUserIdUrl);
        Request request = newAuthRequest(lookupUserIdUrl, authToken)
                .get()
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        return LookupUserIdResponse.from(response);
    }

    private Request.Builder newAuthRequest(URL url) {
        return newAuthRequest(url, null);
    }

    private Request.Builder newAuthRequest(URL url, String authToken) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json");

        // Only add Authorization header for those API's that require it.
        if (!Util.isEmptyString(authToken)) {
            builder.addHeader("Authorization", authToken);
        }

        return builder;
    }

}
