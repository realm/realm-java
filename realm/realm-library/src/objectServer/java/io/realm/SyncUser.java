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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import io.realm.internal.Util;
import io.realm.internal.async.RealmAsyncTaskImpl;
import io.realm.internal.network.AuthenticateResponse;
import io.realm.internal.network.AuthenticationServer;
import io.realm.internal.network.ExponentialBackoffTask;
import io.realm.internal.network.LogoutResponse;
import io.realm.internal.objectserver.ObjectServerUser;
import io.realm.internal.objectserver.Token;
import io.realm.log.RealmLog;
import io.realm.permissions.PermissionModule;

/**
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
public class SyncUser {

    private static class ManagementConfig {
        private SyncConfiguration managementRealmConfig;

        synchronized SyncConfiguration initAndGetManagementRealmConfig(
                ObjectServerUser syncUser, final SyncUser user) {
            if (managementRealmConfig == null) {
                managementRealmConfig = new SyncConfiguration.Builder(
                        user, getManagementRealmUrl(syncUser.getAuthenticationUrl()))
                        .errorHandler(new SyncSession.ErrorHandler() {
                            @Override
                            public void onError(SyncSession session, ObjectServerError error) {
                                if (error.getErrorCode() == ErrorCode.CLIENT_RESET) {
                                    RealmLog.error("Client Reset required for user's management Realm: " + user.toString());
                                } else {
                                    RealmLog.error(String.format("Unexpected error with %s's management Realm: %s",
                                            user.getIdentity(),
                                            error.toString()));
                                }
                            }
                        })
                        .modules(new PermissionModule())
                        .build();
            }

            return managementRealmConfig;
        }
    }


    private final ManagementConfig managementConfig = new ManagementConfig();

    private final ObjectServerUser syncUser;

    private SyncUser(ObjectServerUser user) {
        this.syncUser = user;
    }

    /**
     * Returns the current user that is logged in and still valid.
     * A user is invalidated when he/she logs out or the user's access token expires.
     *
     * @return current {@link SyncUser} that has logged in and is still valid. {@code null} if no user is logged in or the user has
     *         expired.
     * @throws IllegalStateException if multiple users are logged in.
     */
    public static SyncUser currentUser() {
        SyncUser user = SyncManager.getUserStore().getCurrent();
        if (user != null && user.isValid()) {
            return user;
        }
        return null;
    }

    /**
     * Returns all valid users known by this device.
     * A user is invalidated when he/she logs out or the user's access token expires.
     *
     * @return a map from user identifier to user. It includes all known valid users.
     */
    public static Map<String, SyncUser> all() {
        UserStore userStore = SyncManager.getUserStore();
        Collection<SyncUser> storedUsers = userStore.allUsers();
        Map<String, SyncUser> map = new HashMap<String, SyncUser>();
        for (SyncUser user : storedUsers) {
            if (user.isValid()) {
                map.put(user.getIdentity(), user);
            }
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Loads a user that has previously been serialized using {@link #toJson()}.
     *
     * @param user JSON string representing the user.
     *
     * @return the user object.
     * @throws IllegalArgumentException if the JSON couldn't be converted to a valid {@link SyncUser} object.
     */
    public static SyncUser fromJson(String user) {
        try {
            JSONObject obj = new JSONObject(user);
            URL authUrl = new URL(obj.getString("authUrl"));
            Token userToken = Token.from(obj.getJSONObject("userToken"));//TODO rename to refresh_token
            ObjectServerUser syncUser = new ObjectServerUser(userToken, authUrl);
            JSONArray realmTokens = obj.getJSONArray("realms");
            for (int i = 0; i < realmTokens.length(); i++) {
                JSONObject token = realmTokens.getJSONObject(i);
                URI uri = new URI(token.getString("uri"));
                ObjectServerUser.AccessDescription realmDesc = ObjectServerUser.AccessDescription.fromJson(token.getJSONObject("description"));
                syncUser.addRealm(uri, realmDesc);
            }
            return new SyncUser(syncUser);
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
    public static SyncUser login(final SyncCredentials credentials, final String authenticationUrl) throws ObjectServerError {
        URL authUrl;
        try {
            authUrl = new URL(authenticationUrl);
            // If no path segment is provided append `/auth` which is the standard location.
            if (authUrl.getPath().equals("")) {
                authUrl = new URL(authUrl.toString() + "/auth");
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL " + authenticationUrl + ".", e);
        }

        ObjectServerError error;
        try {
            AuthenticateResponse result;
            if (credentials.getIdentityProvider().equals(SyncCredentials.IdentityProvider.ACCESS_TOKEN)) {
                // Credentials using ACCESS_TOKEN as IdentityProvider are optimistically assumed to be valid already.
                // So log them in directly without contacting the authentication server. This is done by mirroring
                // the JSON response expected from the server.
                String userIdentifier = credentials.getUserIdentifier();
                String token = (String) credentials.getUserInfo().get("_token");
                boolean isAdmin = (Boolean) credentials.getUserInfo().get("_isAdmin");
                result = AuthenticateResponse.createValidResponseWithUser(userIdentifier, token, isAdmin);
            } else {
                final AuthenticationServer server = SyncManager.getAuthServer();
                result = server.loginUser(credentials, authUrl);
            }
            if (result.isValid()) {
                ObjectServerUser syncUser = new ObjectServerUser(result.getRefreshToken(), authUrl);
                SyncUser user = new SyncUser(syncUser);
                RealmLog.info("Succeeded authenticating user.\n%s", user);
                SyncManager.getUserStore().put(user);
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
    public static RealmAsyncTask loginAsync(final SyncCredentials credentials, final String authenticationUrl, final Callback callback) {
        if (Looper.myLooper() == null) {
            throw new IllegalStateException("Asynchronous login is only possible from looper threads.");
        }
        final Handler handler = new Handler(Looper.myLooper());
        ThreadPoolExecutor networkPoolExecutor = SyncManager.NETWORK_POOL_EXECUTOR;
        Future<?> authenticateRequest = networkPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    SyncUser user = login(credentials, authenticationUrl);
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
                            try {
                                callback.onError(error);
                            } catch (Exception e) {
                                RealmLog.info("onError has thrown an exception but is ignoring it: %s",
                                        Util.getStackTrace(e));
                            }
                        }
                    });
                }
            }

            private void postSuccess(final SyncUser user) {
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
                return; // Already local/global logout status
            }

            // Ensure that we can log out. If any Realm file is still open we should abort before doing anything
            // else.
            Collection<SyncSession> sessions = syncUser.getSessions();
            for (SyncSession session : sessions) {
                SyncConfiguration config = session.getConfiguration();
                if (Realm.getGlobalInstanceCount(config) > 0) {
                    throw new IllegalStateException("A Realm controlled by this user is still open. Close all Realms " +
                            "before logging out: " + config.getPath());
                }
            }

            SyncManager.getUserStore().remove(syncUser.getIdentity());

            // Delete all Realms if needed.
            for (ObjectServerUser.AccessDescription desc : syncUser.getRealms()) {
                // FIXME: This will always be false since SyncConfiguration.Builder.deleteRealmOnLogout() is
                // disabled. Make sure this works for Realm opened in the client thread/other processes.
                if (desc.deleteOnLogout) {
                    File realmFile = new File(desc.localPath);
                    if (realmFile.exists() && !Util.deleteRealm(desc.localPath, realmFile.getParentFile(), realmFile.getName())) {
                        RealmLog.error("Could not delete Realm when user logged out: " + desc.localPath);
                    }
                }
            }

            // Remove all local tokens, preventing further connections.
            final Token userToken = syncUser.getUserToken();
            syncUser.clearTokens();
            syncUser.localLogout();

            // Finally revoke server token. The local user is logged out in any case.
            final AuthenticationServer server = SyncManager.getAuthServer();
            ThreadPoolExecutor networkPoolExecutor = SyncManager.NETWORK_POOL_EXECUTOR;
            //noinspection unused
            final Future<?> future = networkPoolExecutor.submit(new ExponentialBackoffTask<LogoutResponse>() {

                @Override
                protected LogoutResponse execute() {
                    return server.logout(userToken, syncUser.getAuthenticationUrl());
                }

                @Override
                protected void onSuccess(LogoutResponse response) {
                    SyncManager.notifyUserLoggedOut(SyncUser.this);
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
     * The user might still have been logged out by the Realm Object Server which will not be detected before the
     * user tries to actively synchronize a Realm. If a logged out user tries to synchronize a Realm, an error will be
     * reported to the {@link SyncSession.ErrorHandler} defined by
     * {@link SyncConfiguration.Builder#errorHandler(SyncSession.ErrorHandler)}.
     *
     * @return {@code true} if the User is logged into the Realm Object Server, {@code false} otherwise.
     */
    public boolean isValid() {
        Token userToken = getSyncUser().getUserToken();
        return syncUser.isLoggedIn() && userToken != null && userToken.expiresMs() > System.currentTimeMillis();
    }

    /**
     * Returns {@code true} if this user is an administrator on the Realm Object Server, {@code false} otherwise.
     * <p>
     * Administrators can access all Realms on the server as well as change the permissions of the Realms.
     *
     * @return {@code true} if the user is an administrator on the Realm Object Server, {@code false} otherwise.
     */
    public boolean isAdmin() {
        return syncUser.isAdmin();
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
    public Token getAccessToken() {
        Token userToken = syncUser.getUserToken();
        return (userToken != null) ? userToken : null;
    }

    /**
     * Returns an instance of the Management Realm owned by the user.
     * <p>
     * This Realm can be used to control access and permissions for Realms owned by the user. This includes
     * giving other users access to Realms.
     *
     * @see <a href="https://realm.io/docs/realm-object-server/#permissions">How to control permissions</a>
     */
    public Realm getManagementRealm() {
        return Realm.getInstance(managementConfig.initAndGetManagementRealmConfig(syncUser, this));
    }

    /**
     * Returns the {@link URL} where this user was authenticated.
     *
     * @return {@link URL} where the user was authenticated.
     */
    public URL getAuthenticationUrl() {
        return syncUser.getAuthenticationUrl();
    }

    // Creates the URL to the permission Realm based on the authentication URL.
    private static String getManagementRealmUrl(URL authUrl) {
        String scheme = "realm";
        if (authUrl.getProtocol().equalsIgnoreCase("https")) {
            scheme = "realms";
        }
        try {
            return new URI(scheme, authUrl.getUserInfo(), authUrl.getHost(), authUrl.getPort(),
                    "/~/__management", null, null).toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Could not create URL to the management Realm", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SyncUser user = (SyncUser) o;

        return syncUser.equals(user.syncUser);

    }

    @Override
    public int hashCode() {
        return syncUser.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        sb.append("UserId: ").append(syncUser.getIdentity());
        sb.append(", AuthUrl: ").append(syncUser.getAuthenticationUrl());
        sb.append(", IsValid: ").append(isValid());
        sb.append(", Sessions: ").append(syncUser.getSessions().size());
        sb.append("}");
        return sb.toString();
    }

    // Expose internal representation for other package protected classes
    ObjectServerUser getSyncUser() {
        return syncUser;
    }

    public interface Callback {
        void onSuccess(SyncUser user);
        void onError(ObjectServerError error);
    }
}
