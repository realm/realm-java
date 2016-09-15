package io.realm.objectserver.internal;/*
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

import android.os.SystemClock;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.realm.RealmAsyncTask;
import io.realm.internal.IOException;
import io.realm.internal.Util;
import io.realm.log.RealmLog;
import io.realm.objectserver.ObjectServerError;
import io.realm.objectserver.SyncConfiguration;
import io.realm.objectserver.SyncManager;
import io.realm.objectserver.User;
import io.realm.objectserver.internal.network.AuthenticationServer;
import io.realm.objectserver.internal.network.RefreshResponse;

/**
 * Internal representation of a user on the Realm Object Server.
 * The public API is defined by {@link User}.
 */
public class SyncUser {

    // Time left on current refresh token, when we want to begin refreshing it.
    // Failing to refresh it before it expires, will result in the user getting logged out.
    private RealmAsyncTask refreshTask;

    private final String identifier;
    private Token refreshToken;
    private URL authentificationUrl;
    private Map<URI, Token> accessTokens = new HashMap<URI, Token>();

    /**
     * Create a new Realm Object Server User
     */
    public SyncUser(String identifier, Token refreshToken, URL authenticationUrl) {
        this.identifier = identifier;
        this.authentificationUrl = authenticationUrl;
        setRefreshToken(refreshToken);
    }

    public void setRefreshToken(final Token refreshToken) {
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
     * Returns {@code true} if the user is logged into the Realm Object Server. If this method returns {@code true it
     * means that the user has valid credentials that have not expired.
     * <p>
     * The user might still be logged out by the Realm Object Server which will not be detected before the user
     * tries to actively synchronize a Realm. If a logged out user tries to synchronize a Realm, errors will be reported
     * to the {@link io.realm.objectserver.Session.ErrorHandler} defined by
     * {@link io.realm.objectserver.SyncConfiguration.Builder#errorHandler}.
     *
     * @return {@code true} if the User is considered logged into the Realm Object Server, {@code false} otherwise.
     */
    public boolean isAuthenticated() {
        return refreshToken != null && refreshToken.expiresMs() > System.currentTimeMillis();
    }

    /**
     * Checks if the user has access to the given Realm. Being authenticated means that the
     * user is know by the Realm Object Server and have been granted access to the given Realm.
     *
     * Authenticating will happen automatically as part of opening a Realm.
     */
    public boolean isAuthenticated(SyncConfiguration configuration) {
        Token token = getAccessToken(configuration.getServerUrl());
        return token != null && token.expiresMs() > System.currentTimeMillis();
    }

    public void logout() {
        // TODO Stop any session
        // TODO Clear all tokens
    }

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

    public String getIdentity() {
        return identifier;
    }

    public Token getAccessToken(URI serverUrl) {
        return accessTokens.get(serverUrl);
    }

    public void addAccessToken(URI uri, Token accessToken) {
        accessTokens.put(uri, accessToken);
    }

    /**
     * Adds an access token to this user.
     * <p>
     * An access token is a token granting access to one remote Realm. Access Tokens are normally fetched transparently
     * when opening a Realm, but using this method it is possible to add tokens upfront if they have been fetched or
     * created manually.
     *
     * @param uri {@link java.net.URI} pointing to a remote Realm.
     * @param accessToken
     */
    public void addAccessToken(URI uri, String accessToken) {
        // TODO Currently package protected as we will be unifying the tokens shortly, so each user only has one
        // access token that can be used everywhere. Permissions/access are then fully handled by the Object Server.
        if (uri == null || accessToken == null) {
            throw new IllegalArgumentException("Non-null 'uri' and 'accessToken' required.");
        }
        uri = SyncUtil.getFullServerUrl(uri, identifier);

        // Optimistically create a long-lived token with all permissions. If this is incorrect the Object Server
        // will reject it anyway. If tokens are added manually it is up to the user to ensure they are also used
        // correctly.
        addAccessToken(uri, new Token(accessToken, null, uri.toString(), Long.MAX_VALUE, Token.Permission.values()));
    }

    URL getAuthenticationUrl() {
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
