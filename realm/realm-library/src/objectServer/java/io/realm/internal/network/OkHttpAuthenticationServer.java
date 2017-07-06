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
    private static final String ACTION_LOGOUT = "revoke"; // Auth end point for logging out users
    private static final String ACTION_CHANGE_PASSWORD = "password"; // Auth end point for changing passwords
    private static final String ACTION_LOOKUP_USER_ID = "api/providers"; // Auth end point for looking up user id

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
            return logout(buildActionUrl(authenticationUrl, ACTION_LOGOUT), requestBody);
        } catch (Exception e) {
            return LogoutResponse.from(e);
        }
    }

    @Override
    public ChangePasswordResponse changePassword(Token userToken, String newPassword, URL authenticationUrl) {
        try {
            String requestBody = ChangePasswordRequest.create(userToken, newPassword).toJson();
            return changePassword(buildActionUrl(authenticationUrl, ACTION_CHANGE_PASSWORD), requestBody);
        } catch (Exception e) {
            return ChangePasswordResponse.from(e);
        }
    }

    @Override
    public ChangePasswordResponse changePassword(Token adminToken, String userId, String newPassword, URL authenticationUrl) {
        try {
            String requestBody = ChangePasswordRequest.create(adminToken, userId, newPassword).toJson();
            return changePassword(buildActionUrl(authenticationUrl, ACTION_CHANGE_PASSWORD), requestBody);
        } catch (Exception e) {
            return ChangePasswordResponse.from(e);
        }
    }

    @Override
    public LookupUserIdResponse retrieveUser(Token adminToken, String provider, String providerId, URL authenticationUrl) {
        try {
            return lookupUserId(buildLookupUserIdUrl(authenticationUrl, ACTION_LOOKUP_USER_ID, provider, providerId), adminToken.value());
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

    private static URL buildLookupUserIdUrl(URL authenticationUrl, String action, String provider, String providerId) {
        String authURL = authenticationUrl.toExternalForm();
        // we need the base URL without the '/auth' part
        String baseUrlString = authURL.substring(0, authURL.indexOf(authenticationUrl.getPath()));
        try {
            String separator = baseUrlString.endsWith("/") ? "" : "/";
            return new URL(baseUrlString + separator + action + "/" + provider + "/accounts/" + providerId);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private AuthenticateResponse authenticate(URL authenticationUrl, String requestBody) throws Exception {
        RealmLog.debug("Network request (authenticate): " + authenticationUrl);
        Request request = newAuthRequest(authenticationUrl).post(RequestBody.create(JSON, requestBody)).build();
        Call call = client.newCall(request);
        Response response = call.execute();
        return AuthenticateResponse.from(response);
    }

    private LogoutResponse logout(URL logoutUrl, String requestBody) throws Exception {
        RealmLog.debug("Network request (logout): " + logoutUrl);
        Request request = newAuthRequest(logoutUrl).post(RequestBody.create(JSON, requestBody)).build();
        Call call = client.newCall(request);
        Response response = call.execute();
        return LogoutResponse.from(response);
    }

    private ChangePasswordResponse changePassword(URL changePasswordUrl, String requestBody) throws Exception {
        RealmLog.debug("Network request (changePassword): " + changePasswordUrl);
        Request request = newAuthRequest(changePasswordUrl).put(RequestBody.create(JSON, requestBody)).build();
        Call call = client.newCall(request);
        Response response = call.execute();
        return ChangePasswordResponse.from(response);
    }

    private LookupUserIdResponse lookupUserId(URL lookupUserIdUrl, String token) throws Exception {
        RealmLog.debug("Network request (lookupUserId): " + lookupUserIdUrl);
        Request request = newAuthRequest(lookupUserIdUrl).get().header("Authorization", token).build();
        Call call = client.newCall(request);
        Response response = call.execute();
        return LookupUserIdResponse.from(response);
    }

    private Request.Builder newAuthRequest(URL url) {
        return new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json");
    }

}
