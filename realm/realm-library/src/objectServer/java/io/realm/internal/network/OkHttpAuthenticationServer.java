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

import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.realm.SyncCredentials;
import io.realm.internal.Util;
import io.realm.internal.objectserver.Token;
import io.realm.log.LogLevel;
import io.realm.log.RealmLog;
import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

public class OkHttpAuthenticationServer implements AuthenticationServer {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String ACTION_LOGOUT = "revoke"; // Auth end point for logging out users
    private static final String ACTION_CHANGE_PASSWORD = "password"; // Auth end point for changing passwords
    private static final String ACTION_LOOKUP_USER_ID = "users/:provider:/:providerId:"; // Auth end point for looking up user id
    private static final String ACTION_UPDATE_ACCOUNT = "password/updateAccount"; // Password reset and email confirmation
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
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

    private Map<String, Map<String, String>> customHeaders = new LinkedHashMap<>();
    private Map<String, String> customAuthorizationHeaders = new HashMap<>();

    public OkHttpAuthenticationServer() {
        initHeaders();
    }

    private void initHeaders() {
        customAuthorizationHeaders.put("", "Authorization"); // Default value for authorization header
        customHeaders.put("", new LinkedHashMap<>()); // Add holder for headers used across all hosts
    }

    @Override
    public void setAuthorizationHeaderName(String headerName, @Nullable String host) {
        if (Util.isEmptyString(host)) {
            customAuthorizationHeaders.put("", headerName);
        } else {
            customAuthorizationHeaders.put(host, headerName);
        }
    }

    @Override
    public void addHeader(String headerName, String headerValue, @Nullable String host) {
        if (Util.isEmptyString(host)) {
            customHeaders.get("").put(headerName, headerValue);
        } else {
            Map<String, String> headers = customHeaders.get(host);
            if (headers == null) {
                headers = new LinkedHashMap<>();
                customHeaders.put(host, headers);
            }
            headers.put(headerName, headerValue);
        }
    }

    @Override
    public void clearCustomHeaderSettings() {
        customAuthorizationHeaders.clear();
        customHeaders.clear();
        initHeaders();
    }

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
            String requestBody = AuthenticateRequest.realmLogin(refreshToken, serverUrl.getPath()).toJson();
            return authenticate(authenticationUrl, requestBody);
        } catch (Exception e) {
            return AuthenticateResponse.from(e);
        }
    }

    @Override
    public AuthenticateResponse refreshUser(Token userToken, URI serverUrl, URL authenticationUrl) {
        try {
            String requestBody = AuthenticateRequest.userRefresh(userToken, serverUrl.getPath()).toJson();
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

    @Override
    public UpdateAccountResponse requestPasswordReset(String email, URL authenticationUrl) {
        try {
            String requestBody = UpdateAccountRequest.requestPasswordReset(email).toJson();
            return updateAccount(buildActionUrl(authenticationUrl, ACTION_UPDATE_ACCOUNT), requestBody);
        } catch (Exception e) {
            return UpdateAccountResponse.from(e);
        }
    }

    @Override
    public UpdateAccountResponse completePasswordReset(String token, String newPassword, URL authenticationUrl) {
        try {
            String requestBody = UpdateAccountRequest.completePasswordReset(token, newPassword).toJson();
            return updateAccount(buildActionUrl(authenticationUrl, ACTION_UPDATE_ACCOUNT), requestBody);
        } catch (Exception e) {
            return UpdateAccountResponse.from(e);
        }
    }

    @Override
    public UpdateAccountResponse requestEmailConfirmation(String email, URL authenticationUrl) {
        try {
            String requestBody = UpdateAccountRequest.requestEmailConfirmation(email).toJson();
            return updateAccount(buildActionUrl(authenticationUrl, ACTION_UPDATE_ACCOUNT), requestBody);
        } catch (Exception e) {
            return UpdateAccountResponse.from(e);
        }
    }

    @Override
    public UpdateAccountResponse confirmEmail(String confirmationToken, URL authenticationUrl) {
        try {
            String requestBody = UpdateAccountRequest.completeEmailConfirmation(confirmationToken).toJson();
            return updateAccount(buildActionUrl(authenticationUrl, ACTION_UPDATE_ACCOUNT), requestBody);
        } catch (Exception e) {
            return UpdateAccountResponse.from(e);
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

    private UpdateAccountResponse updateAccount(URL updateAccountUrl, String requestBody) throws Exception {
        RealmLog.debug("Network request (updateAccount): " + updateAccountUrl);
        Request request = newAuthRequest(updateAccountUrl)
                .post(RequestBody.create(JSON, requestBody))
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        return UpdateAccountResponse.from(response);
    }

    private Request.Builder newAuthRequest(URL url) {
        return newAuthRequest(url, null);
    }

    private Request.Builder newAuthRequest(URL url, String authToken) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json");

        // Add custom headers used by all hosts
        for (Map.Entry<String, String> entry : customHeaders.get("").entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        // add custom headers used by specific host (may override g
        Map<String, String> customHeaders = this.customHeaders.get(url.getHost());
        if (customHeaders != null) {
            for (Map.Entry<String, String> entry : customHeaders.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        // Only add Authorization header for those API's that require it.
        // Use the defined custom authorization name if one is available for this host.
        if (!Util.isEmptyString(authToken)) {
            String headerName = customAuthorizationHeaders.get(url.getHost());
            if (headerName != null) {
                builder.addHeader(headerName, authToken);
            } else {
                builder.addHeader(customAuthorizationHeaders.get(""), authToken);
            }
        }

        return builder;
    }

}
