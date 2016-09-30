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

package io.realm;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import io.realm.annotations.Beta;
import io.realm.internal.Util;
import io.realm.internal.async.RealmAsyncTaskImpl;
import io.realm.internal.network.AuthenticateResponse;
import io.realm.internal.network.AuthenticationServer;
import io.realm.internal.network.ExponentialBackoffTask;
import io.realm.internal.network.LogoutResponse;
import io.realm.internal.objectserver.SyncUser;
import io.realm.internal.objectserver.Token;
import io.realm.log.RealmLog;

/**
 * @Beta
 * This class represents a user on the Realm Object Server. The credentials are provided by various 3rd party
 * providers (Facebook, Google, etc.).
 * <p>
 * A user can log in to the Realm Object Server, and if access is granted, it is possible to synchronize the local
 * and the remote Realm. Moreover, synchronization is halted when the user is logged out.
 * <p>
 * It is possible to persist a user. By retrieving a user, there is no need to log in to the 3rd party provider again.
 * Persisting a user between sessions, the user's credentials are stored locally on the device, and should be treated
 * as sensitive data.
 */
@Beta
public class User {

    private final SyncUser syncUser;

    private User(SyncUser user) {
        this.syncUser = user;
    }

    /**
     * Returns the last user that has logged in and who is still valid.
     * A user is invalidated when he/she logs out or the user's access token expire.
     *
     * @return last {@link User} that has logged in and who is still valid. {@code null} if no current user or user has
     *         been invalidated.
     */
    public static User currentUser() {
        User user = SyncManager.getUserStore().get(UserStore.CURRENT_USER_KEY);
        if (user != null && user.isValid()) {
            return user;
        }
        return null;
    }

    /**
     * Loads a user that has previously been serialized using {@link #toJson()}.
     *
     * @param user JSON string representing the user.
     *
     * @return the user object.
     * @throws IllegalArgumentException if the JSON couldn't be converted to a valid {@link User} object.
     */
    public static User fromJson(String user) {
        try {
            JSONObject obj = new JSONObject(user);
            URL authUrl = new URL(obj.getString("authUrl"));
            Token userToken = Token.from(obj.getJSONObject("userToken"));
            SyncUser syncUser = new SyncUser(userToken, authUrl);
            JSONArray realmTokens = obj.getJSONArray("realms");
            for (int i = 0; i < realmTokens.length(); i++) {
                JSONObject token = realmTokens.getJSONObject(i);
                URI uri = new URI(token.getString("uri"));
                SyncUser.AccessDescription realmDesc = SyncUser.AccessDescription.fromJson(token.getJSONObject("description"));
                syncUser.addRealm(uri, realmDesc);
            }
            return new User(syncUser);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Could not parse user json: " + user, e);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL in JSON not valid: " + user, e);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URI is not valid: " + user, e);
        }
    }

    /**
     * Logs in the user to the Realm Object Server. This is done synchronously, so calling this method on the Android
     * UI thread will always crash. A logged in user is required to be able to create a {@link SyncConfiguration}.
     *
     * @param credentials credentials to use.
     * @param authenticationUrl server that can authenticate against.
     * @throws ObjectServerError if the login failed.
     * @throws IllegalArgumentException if the URL is malformed.
     */
    public static User login(final Credentials credentials, final String authenticationUrl) throws ObjectServerError {
        final URL authUrl;
        try {
            authUrl = new URL(authenticationUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL " + authenticationUrl + ".", e);
        }

        final AuthenticationServer server = SyncManager.getAuthServer();
        ObjectServerError error;
        try {
            AuthenticateResponse result = server.loginUser(credentials, authUrl);
            if (result.isValid()) {
                SyncUser syncUser = new SyncUser(result.getRefreshToken(), authUrl);
                User user = new User(syncUser);
                RealmLog.info("Succeeded authenticating user.\n%s", user);
                SyncManager.getUserStore().put(UserStore.CURRENT_USER_KEY, user);
                SyncManager.notifyUserLoggedIn(user);
                return user;
            } else {
                RealmLog.info("Failed authenticating user.\n%s", result.getError());
                error = result.getError();
            }
        } catch (Throwable e) {
            throw new ObjectServerError(ErrorCode.UNKNOWN, e);
        }
        throw error;
    }

    /**
     * Logs in the user to the Realm Object Server. A logged in user is required to be able to create a
     * {@link SyncConfiguration}.
     *
     * @param credentials credentials to use.
     * @param authenticationUrl server that the user is authenticated against.
     * @param callback callback when login has completed or failed. The callback will always happen on the same thread
     *                 as this this method is called on.
     * @throws IllegalArgumentException if not on a Looper thread.
     */
    public static RealmAsyncTask loginAsync(final Credentials credentials, final String authenticationUrl, final Callback callback) {
        if (Looper.myLooper() == null) {
            throw new IllegalStateException("Asynchronous login is only possible from looper threads.");
        }
        final Handler handler = new Handler(Looper.myLooper());
        ThreadPoolExecutor networkPoolExecutor = SyncManager.NETWORK_POOL_EXECUTOR;
        Future<?> authenticateRequest = networkPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    User user = login(credentials, authenticationUrl);
                    postSuccess(user);
                } catch (ObjectServerError e) {
                    postError(e);
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

        return new RealmAsyncTaskImpl(authenticateRequest, networkPoolExecutor);
    }

    /**
     * Logs out the user from the Realm Object Server. Once the Object Server has confirmed the logout any registered
     * {@link AuthenticationListener} will be notified and user credentials will be deleted from this device.
     *
     * @throws IllegalStateException if any Realms owned by this user is still open. They should be closed before
     *         logging out.
     */
    /* FIXME: Add this back to the javadoc when enable SyncConfiguration.Builder#deleteRealmOnLogout()
     <p>
     Any Realms owned by the user will be deleted if {@link SyncConfiguration.Builder#deleteRealmOnLogout()} is
     also set.
     */
    public void logout() {
        // Acquire lock to prevent users creating new instances
        synchronized (Realm.class) {
            if (!syncUser.isLoggedIn()) {
                return; // Already logged out
            }

            // Ensure that we can log out. If any Realm file is still open we should abort before doing anything
            // else.
            Collection<Session> sessions = syncUser.getSessions();
            for (Session session : sessions) {
                SyncConfiguration config = session.getConfiguration();
                if (Realm.getGlobalInstanceCount(config) > 0) {
                    throw new IllegalStateException("A Realm controlled by this user is still open. Close all Realms " +
                            "before logging out: " + config.getPath());
                }
            }

            // Stop all active sessions immediately. If we waited until after talking to the server
            // there is a high chance errors would be reported from the Sync Client first which would
            // be confusing.
            for (Session session : sessions) {
                session.getSyncSession().stop();
            }

            final AuthenticationServer server = SyncManager.getAuthServer();
            ThreadPoolExecutor networkPoolExecutor = SyncManager.NETWORK_POOL_EXECUTOR;
            networkPoolExecutor.submit(new ExponentialBackoffTask<LogoutResponse>() {

                @Override
                protected LogoutResponse execute() {
                    return server.logout(User.this, syncUser.getAuthenticationUrl());
                }

                @Override
                protected void onSuccess(LogoutResponse response) {
                    // Remove all local tokens, preventing further connections.
                    syncUser.clearTokens();

                    if (User.this.equals(User.currentUser())) {
                        SyncManager.getUserStore().remove(UserStore.CURRENT_USER_KEY);
                    }

                    // Delete all Realms if needed.
                    for (SyncUser.AccessDescription desc : syncUser.getRealms()) {
                        // FIXME: This will always be false since SyncConfiguration.Builder.deleteRealmOnLogout() is
                        // disabled. Make sure this works for Realm opened in the client thread/other processes.
                        if (desc.deleteOnLogout) {
                            File realmFile = new File(desc.localPath);
                            if (realmFile.exists() && !Util.deleteRealm(desc.localPath, realmFile.getParentFile(), realmFile.getName())) {
                                RealmLog.error("Could not delete Realm when user logged out: " + desc.localPath);
                            }
                        }
                    }

                    SyncManager.notifyUserLoggedOut(User.this);
                }

                @Override
                protected void onError(LogoutResponse response) {
                    RealmLog.error("Failed to log user out.\n" + response.getError().toString());
                }
            });
        }
    }

    /**
     * Returns a JSON token representing this user.
     * <p>
     * Possession of this JSON token can potentially grant access to data stored on the Realm Object Server, so it
     * should be treated as sensitive data.
     *
     * @return JSON string representing this user. It can be converted back into a real user object using
     *         {@link #fromJson(String)}.
     *
     * @see #fromJson(String)
     */
    public String toJson() {
        return syncUser.toJson();
    }

    /**
     * Returns {@code true} if the user is logged into the Realm Object Server. If this method returns {@code true} it
     * implies that the user has valid credentials that have not expired.
     * <p>
     * The user might still be have been logged out by the Realm Object Server which will not be detected before the
     * user tries to actively synchronize a Realm. If a logged out user tries to synchronize a Realm, an error will be
     * reported to the {@link Session.ErrorHandler} defined by
     * {@link SyncConfiguration.Builder#errorHandler(Session.ErrorHandler)}.
     *
     * @return {@code true} if the User is logged into the Realm Object Server, {@code false} otherwise.
     */
    public boolean isValid() {
        Token userToken = getSyncUser().getUserToken();
        return syncUser.isLoggedIn() && userToken != null && userToken.expiresMs() > System.currentTimeMillis();
    }

    /**
     * Returns the identity of this user on the Realm Object Server. The identity is a guaranteed to be unique
     * among all users on the Realm Object Server.
     *
     * @return identity of the user on the Realm Object Server. If the user has logged out or the login has expired
     *         {@code null} is returned.
     */
    public String getIdentity() {
        return syncUser.getIdentity();
    }

    /**
     * Returns this user's access token. This is the users credential for accessing the Realm Object Server and should
     * be treated as sensitive data.
     *
     * @return the user's access token. If this user has logged out or the login has expired {@code null} is returned.
     */
    public String getAccessToken() {
        Token userToken = syncUser.getUserToken();
        return (userToken != null) ? userToken.value() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return syncUser.equals(user.syncUser);

    }

    @Override
    public int hashCode() {
        return syncUser.hashCode();
    }

    // Expose internal representation for other package protected classes
    SyncUser getSyncUser() {
        return syncUser;
    }

    public interface Callback {
        void onSuccess(User user);
        void onError(ObjectServerError error);
    }
}
