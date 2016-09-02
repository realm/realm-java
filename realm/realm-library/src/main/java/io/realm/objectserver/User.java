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

package io.realm.objectserver;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.realm.RealmAsyncTask;
import io.realm.internal.IOException;
import io.realm.internal.Util;
import io.realm.internal.objectserver.Token;
import io.realm.internal.objectserver.network.AuthenticateResponse;
import io.realm.internal.objectserver.network.AuthenticationServer;
import io.realm.internal.objectserver.network.RefreshResponse;
import io.realm.log.RealmLog;

/**
 * The credentials object describes a credentials on the Realm Object Server.
 *
 * It is a helper object that can hold multiple credentials at the same time and execute actions on those.
 *
 * This is e.g. useful if multiple Realms conceptually belong to the same "credentials". Then a
 *
 */
public class User {

    // Time left on current refresh token, when we want to begin refreshing it.
    // Failing to refresh it before it expires, will result in the user getting logged out.
    private static RealmAsyncTask authenticateTask;
    private static RealmAsyncTask refreshTask;

    private final String identifier;
    private Token refreshToken;
    private URL authentificationUrl;
    private Map<SyncConfiguration, Token> accessTokens = new HashMap<SyncConfiguration, Token>();

    /**
     * Load a user that has previously been saved using {@link #toJson()}.
     *
     * @param user Json string representing the user.
     *
     * @return the user object.
     * @throws IllegalArgumentException if the JSON could be be converted to a valid {@link User} object.
     */
    public static User fromJson(String user) {
        try {
            JSONObject obj = new JSONObject(user);
            String id = obj.getString("identifier");
            Token refreshToken = Token.from(obj.getJSONObject("refreshToken"));
            URL authUrl = new URL(obj.getString("authUrl"));
            // FIXME: Add support for  storing access tokens as well
            return new User(id, refreshToken, authUrl);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Could not parse user json: " + user, e);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL in JSON not valid: " + user, e);
        }
    }

    public static User login(final Credentials credentials, final URL authentificationUrl) throws ObjectServerError {
        return null; // TODO
    }

    /**
     * Login the user on the Realm Object Server
     *
     * @param credentials Credentials to use
     * @param authenticationUrl URL to authenticateUser against
     * @param callback Callback when login has completed or failed. This callback will always happen on the UI thread.
     * @throws IllegalArgumentException
     */
    // FIXME Return task that can be canceled
    public static RealmAsyncTask login(final Credentials credentials, final String authenticationUrl, final Callback callback) {
        final URL authUrl;
        try {
            authUrl = new URL(authenticationUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL", e);
        }

        // TODO Where should the callback happen? Only allow callbacks on Handler threads? Then we need a variant
        // that blocks on a background thread.
        final Handler handler = new Handler(Looper.getMainLooper());

        final AuthenticationServer server = SyncManager.getAuthServer();
        Future<?> authenticateRequest = SyncManager.NETWORK_POOL_EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                // Don't retry authenticateUser requests. The app might want to respond to errors.
                try {
                    AuthenticateResponse result = server.authenticateUser(credentials, authUrl, credentials.shouldCreateUser());
                    if (result.isValid()) {
                        User user = new User(result.getIdentifier(), result.getRefreshToken(), authUrl);
                        postSuccess(user);
                    } else {
                        postError(result.getError());
                    }
                } catch (IOException e) {
                    postError(new ObjectServerError(ErrorCode.IO_EXCEPTION, e));
                }
            }

            private void postError(final ObjectServerError error) {
                if (callback != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(error);
                        }
                    });
                }
            }

            private void postSuccess(final User user) {
                if (callback != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(user);
                        }
                    });
                }
            }
        });
        authenticateTask = new RealmAsyncTask(authenticateRequest, SyncManager.NETWORK_POOL_EXECUTOR);
        return authenticateTask;
    }

    private User(String identifier, Token refreshToken, URL authentificationUrl) {
        this.identifier = identifier;
        this.authentificationUrl = authentificationUrl;
        setRefreshToken(refreshToken);
    }

    public void setRefreshToken(final Token refreshToken) {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
        this.refreshToken = refreshToken;

        // Schedule a refresh. This method cannot fail, but will continue retrying until either the app is killed
        // or the attempt was successful.
        // TODO Consider combining refresh across all users?
        final long expire = refreshToken.expires();
        final AuthenticationServer server = SyncManager.getAuthServer();
        Future<?> task = SyncManager.NETWORK_POOL_EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                long timeToExpiration = System.currentTimeMillis() - expire;
                if (timeToExpiration > 0) {
                    SystemClock.sleep(timeToExpiration);
                }

                int attempt = 0;
                while (!Thread.interrupted()) {
                    attempt++;
                    long sleep = Util.calculateExponentialDelay(attempt - 1, TimeUnit.MINUTES.toMillis(5));
                    if (sleep > 0) {
                        try {
                            Thread.sleep(sleep);
                        } catch (InterruptedException e) {
                            return; // Abort authentication if interrupted.
                        }
                    }
                    try {
                        RefreshResponse result = server.refresh(refreshToken.value(), authentificationUrl);
                        if (result.isValid()) {
                            setRefreshToken(result.getRefreshToken());
                            break;
                        } else {
                            // FIXME: Log to session events instead
                            RealmLog.warn("Refreshing login failed: " + result.getErrorCode() + " : " + result.getErrorMessage());
                        }
                    } catch (IOException e) {
                        // FIXME: Log to session events instead.
                        RealmLog.info("Refreshing login failed: " + e.toString());
                    }
                }
            }
        });
        refreshTask = new RealmAsyncTask(task, SyncManager.NETWORK_POOL_EXECUTOR);
    }

    /**
     * Returns true if the User is authenticated by the Realm Authentication Server. Being authenticated means that the
     * user is know by the Realm Object Server, but nothing about which Realms that user might have access to and with
     * which permissions.
     */
    public boolean isAuthenticated() {
        return refreshToken != null && refreshToken.expires() < System.currentTimeMillis();
    }

    public void logout() {
        // TODO Stop any session
        // TODO Clear all tokens
    }

    /**
     * Returns a JSON token representing this user.
     *
     * Possession of this JSON token can potentially grant access to data stored on the Realm Object Server, so it
     * should be treated as sensitive data.
     *
     * @return JSON string representing this user. It can be converted back into a real user object using
     *         {@link #fromJson(String)}.
     *
     * @see #fromJson(String)
     */
    public String toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("identifier", identifier);
            obj.put("refreshToken", refreshToken.toJson());
            obj.put("authenticationUrl", authentificationUrl);
            // FIXME: Add support for  storing access tokens as well
           return obj.toString();
        } catch (JSONException e) {
            throw new RuntimeException("Could not convert User to JSON", e);
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    /**
     * Refresh the users login.
     */
    void refresh() {
        // TODO Where should the callback happen? Only allow callbacks on Handler threads? Then we need a variant
        // that blocks on a background thread.

        final AuthenticationServer server = SyncManager.getAuthServer();
    }

    /**
     * Return the access token for the given Realm or {@code null} if no token exists.
     */
    public Token getAccessToken(SyncConfiguration configuration) {
        return accessTokens.get(configuration);
    }

    public void setAccesToken(SyncConfiguration configuration, Token accessToken) {
        accessTokens.put(configuration, accessToken);
    }

    public URL getAuthentificationUrl() {
        return authentificationUrl;
    }

    public Token getRefreshToken() {
        return refreshToken;
    }

    public interface Callback {
        void onSuccess(User user);
        void onError(ObjectServerError error);
    }
}
