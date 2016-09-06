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
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
 * This class represents a user on the Realm Object Server.
 *
 *
 * TODO Rewrite this section
 */
public class User {

    // Time left on current refresh token, when we want to begin refreshing it.
    // Failing to refresh it before it expires, will result in the user getting logged out.
    private static RealmAsyncTask authenticateTask;
    private RealmAsyncTask refreshTask;

    private final String identifier;
    private Token refreshToken;
    private URL authentificationUrl;
    private Map<URI, Token> accessTokens = new HashMap<URI, Token>();

    /**
     * Creates a User only known to this device.
     * @return
     */
    public static User createLocal() {
        Token token = new Token(UUID.randomUUID().toString(), Long.MAX_VALUE, Token.Permission.values());
        return new User(UUID.randomUUID().toString(), token, null);
    }

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

    /**
     * Creates a user from an existing token. This user is automatically consider validated by the device, but the
     * Realm Object Server might determine that the token has expired or no longer is valid.
     *
     * This should only be used when debugging or testing. In most other cases the user object obtained from a
     * {@link #login(Credentials, String, Callback)} should be saved and reused. This can e.g. be done using a
     * {@link UserStore}.
     *
     * @param token token to represent user.
     */
    public static User fromToken(String token) {
        // Define a user with unlimited access. Object Server will reject any invalid access anyway.
        return new User(null, new Token(token, Long.MAX_VALUE, Token.Permission.values()), null);
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

    void setRefreshToken(final Token refreshToken) {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
        this.refreshToken = refreshToken;

        if (authentificationUrl == null) {
            return;
        }
        // Schedule a refresh. This method cannot fail, but will continue retrying until either the app is killed
        // or the attempt was successful.
        // TODO Consider combining refresh across all users?
        final long expire = refreshToken.expiresMs();
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
     * Returns true if the User is authenticated by the Realm Object Server. Being authenticated means that the
     * user is know by the Realm Object Server, but nothing about which Realms that user might have access to and with
     * what kind of permissions.
     */
    public boolean isAuthenticated() {
        return refreshToken != null && refreshToken.expiresMs() > System.currentTimeMillis();
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
            obj.put("authUrl", authentificationUrl);
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
     * Return the access token for the given Realm or {@code null} if no token exists.
     */
    Token getAccessToken(URI serverUrl) {
        return accessTokens.get(serverUrl);
    }

    void addAccessToken(URI uri, Token accessToken) {
        accessTokens.put(uri, accessToken);
    }

    /**
     * Adds an access token to this user.
     * <p>
     * An access token is a token granting access to one remote Realm. They are normally fetched transparently when
     * opening a Realm, but using this method it is possible to add tokens upfront if they have been fetched or
     * created manually.
     *
     * @param uri {@link java.net.URI} pointing to a remote Realm.
     * @param accessToken
     */
    void addAccessToken(URI uri, String accessToken) {
        // TODO Currently package protected as we will be unifying the tokens shortly, so each user only has one
        // access token that can be used everywhere. Permissions/access are then fully handled by the Object Server.
        if (uri == null || accessToken == null) {
            throw new IllegalArgumentException("Non-null 'uri' and 'accessToken' required.");
        }
        uri = SyncConfiguration.getFullServerUrl(uri, identifier);

        // Optimistically create a long-lived token with all permissions. If this is incorrect the Object Server
        // will reject it anyway. If tokens are added manually it is up to the user to ensure they are also used
        // correctly.
        addAccessToken(uri, new Token(accessToken, Long.MAX_VALUE, Token.Permission.values()));
    }


    URL getAuthenticationUrl() {
        return authentificationUrl;
    }

    // TODO Figure out how to make this non-public
    public Token getRefreshToken() {
        return refreshToken;
    }

    public interface Callback {
        void onSuccess(User user);
        void onError(ObjectServerError error);
    }
}
