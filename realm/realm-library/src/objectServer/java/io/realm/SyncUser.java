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

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Nullable;

import io.realm.internal.RealmNotifier;
import io.realm.internal.Util;
import io.realm.internal.android.AndroidCapabilities;
import io.realm.internal.android.AndroidRealmNotifier;
import io.realm.internal.async.RealmAsyncTaskImpl;
import io.realm.internal.network.AuthenticateResponse;
import io.realm.internal.network.AuthenticationServer;
import io.realm.internal.network.ChangePasswordResponse;
import io.realm.internal.network.ExponentialBackoffTask;
import io.realm.internal.network.LogoutResponse;
import io.realm.internal.network.LookupUserIdResponse;
import io.realm.internal.objectserver.Token;
import io.realm.internal.permissions.ManagementModule;
import io.realm.internal.permissions.PermissionModule;
import io.realm.log.RealmLog;

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
    private final String identity;
    private Token refreshToken;
    private final URL authenticationUrl;
    // maps all RealmConfiguration and accessToken, using this SyncUser.
    private final Map<SyncConfiguration, Token> realms = new HashMap<SyncConfiguration, Token>();

    private static class ManagementConfig {
        private SyncConfiguration managementRealmConfig;

        synchronized SyncConfiguration initAndGetManagementRealmConfig(final SyncUser user) {
            if (managementRealmConfig == null) {
                managementRealmConfig = new SyncConfiguration.Builder(
                        user, getManagementRealmUrl(user.getAuthenticationUrl()))
                        .errorHandler(new SyncSession.ErrorHandler() {
                            @Override
                            public void onError(SyncSession session, ObjectServerError error) {
                                if (error.getErrorCode() == ErrorCode.CLIENT_RESET) {
                                    RealmLog.error("Client Reset required for user's management Realm: " + user.toString());
                                } else {
                                    RealmLog.error(String.format(Locale.US,
                                            "Unexpected error with %s's management Realm: %s",
                                            user.getIdentity(),
                                            error.toString()));
                                }
                            }
                        })
                        .modules(new ManagementModule())
                        .build();
            }

            return managementRealmConfig;
        }
    }

    private final ManagementConfig managementConfig = new ManagementConfig();

    SyncUser(Token refreshToken, URL authenticationUrl) {
        this.identity = refreshToken.identity();
        this.authenticationUrl = authenticationUrl;
        this.refreshToken = refreshToken;
    }

    /**
     * Returns the current user that is logged in and still valid.
     * A user is invalidated when he/she logs out or the user's access token expires.
     *
     * @return current {@link SyncUser} that has logged in and is still valid. {@code null} if no user is logged in or the user has
     * expired.
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
        Map<String, SyncUser> map = new HashMap<>();
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
     * @return the user object.
     * @throws IllegalArgumentException if the JSON couldn't be converted to a valid {@link SyncUser} object.
     */
    public static SyncUser fromJson(String user) {
        try {
            JSONObject obj = new JSONObject(user);
            URL authUrl = new URL(obj.getString("authUrl"));
            Token userToken = Token.from(obj.getJSONObject("userToken"));//TODO rename to refresh_token
            return new SyncUser(userToken, authUrl);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Could not parse user json: " + user, e);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL in JSON not valid: " + user, e);
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
                SyncUser user = new SyncUser(result.getRefreshToken(), authUrl);
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
     * as this this method is called on.
     * @return representation of the async task that can be used to cancel it if needed.
     * @throws IllegalArgumentException if not on a Looper thread.
     */
    public static RealmAsyncTask loginAsync(final SyncCredentials credentials, final String authenticationUrl, final Callback callback) {
        checkLooperThread("Asynchronous login is only possible from looper threads.");
        return new Request(SyncManager.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public SyncUser run() throws ObjectServerError {
                return login(credentials, authenticationUrl);
            }
        }.start();
    }

    /**
     * Log a user out, destroying their server state, unregistering them from the SDK, and removing
     * any synced Realms associated with them, from on-disk storage on next app launch (or directly
     * if all instances are closed).
     * If the user is already logged out or in an error state, this method does nothing.
     *
     * This method should be called whenever the application is committed to not using a user again
     * unless they are recreated. Failing to call this method may result in unused files and metadata
     * needlessly taking up space.
     *
     * Once the Object Server has confirmed the logout any registered {@link AuthenticationListener}
     * will be notified and user credentials will be deleted from this device.
     */
//    /* FIXME: Add this back to the javadoc when enable SyncConfiguration.Builder#deleteRealmOnLogout()
//     <p>
//     Any Realms owned by the user will be deleted, when the application restart.
//     */
    // this is a fire and forget, end user should not worry about the state of the async query
    @SuppressWarnings("FutureReturnValueIgnored")
    public void logout() {
        // Acquire lock to prevent users creating new instances
        synchronized (Realm.class) {
            if (!SyncManager.getUserStore().isActive(identity, authenticationUrl.toString())) {
                return; // Already logged out status
            }

            // Mark the user as logged out in the ObjectStore
            SyncManager.getUserStore().remove(identity, authenticationUrl.toString());

            // invalidate all pending refresh_token queries
            for (SyncConfiguration syncConfiguration : realms.keySet()) {
                SyncSession session = SyncManager.getSession(syncConfiguration);
                if (session != null) {
                    session.clearScheduledAccessTokenRefresh();
                }
            }

            // Remove all local tokens, preventing further connections.
            // don't remove identity as this SyncUser might be re-activated and we need
            // to avoid throwing a mismatch SyncConfiguration in RealmCache if we have
            // the similar SyncConfiguration using the same identity, but with different (new)
            // refresh-token.
            realms.clear();

            // Finally revoke server token. The local user is logged out in any case.
            final AuthenticationServer server = SyncManager.getAuthServer();
            // don't reference directly the refreshToken inside the revoke request
            // as it may revoke the newly acquired and refresh_token
            final Token refreshTokenToBeRevoked = refreshToken;

            ThreadPoolExecutor networkPoolExecutor = SyncManager.NETWORK_POOL_EXECUTOR;
            networkPoolExecutor.submit(new ExponentialBackoffTask<LogoutResponse>() {

                @Override
                protected LogoutResponse execute() {
                    return server.logout(refreshTokenToBeRevoked, getAuthenticationUrl());
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
     * Changes this user's password. This is done synchronously and involves the network, so calling this method on the
     * Android UI thread will always crash.
     * <p>
     * <b>WARNING:</b> Changing a user's password through an authentication server that doesn't use HTTPS is a major
     * security flaw, and should only be done while testing.
     *
     * @param newPassword the user's new password.
     * @throws ObjectServerError if the password could not be changed.
     */
    public void changePassword(final String newPassword) throws ObjectServerError {
        //noinspection ConstantConditions
        if (newPassword == null) {
            throw new IllegalArgumentException("Not-null 'newPassword' required.");
        }
        AuthenticationServer authServer = SyncManager.getAuthServer();
        ChangePasswordResponse response = authServer.changePassword(refreshToken, newPassword, getAuthenticationUrl());
        if (!response.isValid()) {
            throw response.getError();
        }
    }

    /**
     * Changes another user's password. This is done synchronously and involves the network, so calling this method on the
     * Android UI thread will always crash.
     * <p>
     * This user needs admin privilege in order to change someone else's password.
     * <p>
     * <b>WARNING:</b> Changing a user's password through an authentication server that doesn't use HTTPS is a major
     * security flaw, and should only be done while testing.
     *
     * @param userId identity ({@link #getIdentity()}) of the user we want to change the password for.
     * @param newPassword the user's new password.
     * @throws ObjectServerError if the password could not be changed.
     */
    public void changePassword(final String userId, final String newPassword) throws ObjectServerError {
        //noinspection ConstantConditions
        if (newPassword == null) {
            throw new IllegalArgumentException("Not-null 'newPassword' required.");
        }

        if (Util.isEmptyString(userId)) {
            throw new IllegalArgumentException("None empty 'userId' required.");
        }

        if (userId.equals(getIdentity())) { // user want's to change his/her own password
            changePassword(newPassword);

        } else {
            if (!isAdmin()) {
                throw new IllegalStateException("User need to be admin in order to change another user's password.");
            }

            AuthenticationServer authServer = SyncManager.getAuthServer();
            ChangePasswordResponse response = authServer.changePassword(refreshToken, userId, newPassword, getAuthenticationUrl());
            if (!response.isValid()) {
                throw response.getError();
            }
        }
    }

    /**
     * Changes this user's password asynchronously.
     * <p>
     * <b>WARNING:</b> Changing a users password using an authentication server that doesn't use HTTPS is a major
     * security flaw, and should only be done while testing.
     *
     * @param newPassword the user's new password.
     * @param callback callback when login has completed or failed. The callback will always happen on the same thread
     * as this method is called on.
     * @return representation of the async task that can be used to cancel it if needed.
     * @throws IllegalArgumentException if not on a Looper thread.
     */
    public RealmAsyncTask changePasswordAsync(final String newPassword, final Callback callback) {
        checkLooperThread("Asynchronous changing password is only possible from looper threads.");
        //noinspection ConstantConditions
        if (callback == null) {
            throw new IllegalArgumentException("Non-null 'callback' required.");
        }
        return new Request(SyncManager.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public SyncUser run() {
                changePassword(newPassword);
                return SyncUser.this;
            }
        }.start();
    }

    /**
     * Changes another user's password asynchronously.
     * <p>
     * This user needs admin privilege in order to change someone else's password.
     *
     * <b>WARNING:</b> Changing a users password using an authentication server that doesn't use HTTPS is a major
     * security flaw, and should only be done while testing.
     *
     * @param userId identity ({@link #getIdentity()}) of the user we want to change the password for.
     * @param newPassword the user's new password.
     * @param callback callback when login has completed or failed. The callback will always happen on the same thread
     * as this method is called on.
     * @return representation of the async task that can be used to cancel it if needed.
     * @throws IllegalArgumentException if not on a Looper thread.
     */
    public RealmAsyncTask changePasswordAsync(final String userId, final String newPassword, final Callback callback) {
        checkLooperThread("Asynchronous changing password is only possible from looper threads.");
        //noinspection ConstantConditions
        if (callback == null) {
            throw new IllegalArgumentException("Non-null 'callback' required.");
        }

        return new Request(SyncManager.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public SyncUser run() {
                changePassword(userId, newPassword);
                return SyncUser.this;
            }
        }.start();
    }

    /**
     * Helper method for Admin users in order to lookup a {@code SyncUser} using the identity provider and the used username.
     *
     * @param provider identity providers {@link io.realm.SyncCredentials.IdentityProvider} used when the account was created.
     * @param providerId username or email used to create the account for the first time,
     *                   what is needed will depend on what type of {@link SyncCredentials} was used.
     *
     * @return {@code SyncUser} associated with the given identity provider and providerId, or {@code null} in case
     * of an {@code invalid} provider or {@code providerId}.
     * @throws ObjectServerError in case of an error.
     * @deprecated as of release 3.6.0, replaced by {@link #retrieveInfoForUser(String, String)}}
     */
    @Deprecated
    public SyncUser retrieveUser(final String provider, final String providerId) throws ObjectServerError {
        if (Util.isEmptyString(provider)) {
            throw new IllegalArgumentException("Not-null 'provider' required.");
        }

        if (Util.isEmptyString(providerId)) {
            throw new IllegalArgumentException("None empty 'providerId' required.");
        }

        if (!isAdmin()) {
            throw new IllegalArgumentException("SyncUser needs to be admin in order to lookup other users ID.");
        }

        AuthenticationServer authServer = SyncManager.getAuthServer();
        LookupUserIdResponse response = authServer.retrieveUser(refreshToken, provider, providerId, getAuthenticationUrl());
        if (!response.isValid()) {
            // the endpoint returns a 404 if it can't honor the query, either because
            // - provider is not valid
            // - provider_id is not valid
            // - token used is not an admin one
            // in this case we should return null instead of throwing
            if (response.getError().getErrorCode() == ErrorCode.NOT_FOUND) {
                return null;
            } else {
                throw response.getError();
            }
        } else {
            SyncUser syncUser = SyncManager.getUserStore().get(response.getUserId(), getAuthenticationUrl().toString());
            if (syncUser != null) {
                return syncUser;
            } else {
                // build a SynUser without a token
                Token refreshToken = new Token(null, response.getUserId(), null, 0, null, response.isAdmin());
                return new SyncUser(refreshToken, getAuthenticationUrl());
            }
        }
    }

    /**
     * Asynchronously lookup a {@code SyncUser} using the identity provider and the used username.
     * This is for Admin users only.
     *
     * @param provider identity providers {@link io.realm.SyncCredentials.IdentityProvider} used when the account was created.
     * @param providerId  username or email used to create the account for the first time,
     *                    what is needed will depend on what type of {@link SyncCredentials} was used.
     * @param callback callback when the lookup has completed or failed. The callback will always happen on the same thread
     * as this method is called on.
     * @return representation of the async task that can be used to cancel it if needed.
     * @deprecated as of release 3.6.0, replaced by {@link #retrieveInfoForUserAsync(String, String, RequestCallback)}}
     */
    @Deprecated
    public RealmAsyncTask retrieveUserAsync(final String provider, final String providerId, final Callback callback) {
        checkLooperThread("Asynchronously retrieving user id is only possible from looper threads.");
        //noinspection ConstantConditions
        if (callback == null) {
            throw new IllegalArgumentException("Non-null 'callback' required.");
        }

        return new Request(SyncManager.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public SyncUser run() {
                return retrieveUser(provider, providerId);
            }
        }.start();
    }

    /**
     * Given a Realm Object Server authentication provider and a provider identifier for a user (for example, a username), look up and return user information for that user.
     *
     * @param providerUserIdentity The username or identity of the user as issued by the authentication provider.
     *                             In most cases this is different from the Realm Object Server-issued identity.
     * @param provider The authentication provider {@link io.realm.SyncCredentials.IdentityProvider} that manages the user whose information is desired.
     *
     * @return {@code SyncUser} associated with the given identity provider and providerId, or {@code null} in case
     * of an {@code invalid} provider or {@code providerId}.
     * @throws ObjectServerError in case of an error.
     */
    public SyncUserInfo retrieveInfoForUser(final String providerUserIdentity, final String provider) throws ObjectServerError {
        if (Util.isEmptyString(providerUserIdentity)) {
            throw new IllegalArgumentException("'providerUserIdentity' cannot be empty.");
        }

        if (Util.isEmptyString(provider)) {
            throw new IllegalArgumentException("'provider' cannot be empty.");
        }

        if (!isAdmin()) {
            throw new IllegalArgumentException("SyncUser needs to be admin in order to lookup other users ID.");
        }

        AuthenticationServer authServer = SyncManager.getAuthServer();
        LookupUserIdResponse response = authServer.retrieveUser(refreshToken, provider, providerUserIdentity, getAuthenticationUrl());
        if (!response.isValid()) {
            // the endpoint returns a 404 if it can't honor the query, either because
            // - provider is not valid
            // - provider_id is not valid
            // - token used is not an admin one
            // in this case we should return null instead of throwing
            if (response.getError().getErrorCode() == ErrorCode.NOT_FOUND) {
                return null;
            } else {
                throw response.getError();
            }
        } else {
            return SyncUserInfo.fromLookupUserIdResponse(response);
        }
    }

    /**
     * Given a Realm Object Server authentication provider and a provider identifier for a user (for example, a username), asynchronously look up and return user information for that user.
     *
     * @param providerUserIdentity The username or identity of the user as issued by the authentication provider.
     *                             In most cases this is different from the Realm Object Server-issued identity.
     * @param provider The authentication provider {@link io.realm.SyncCredentials.IdentityProvider} that manages the user whose information is desired.
     *
     * @return {@code SyncUser} associated with the given identity provider and providerId, or {@code null} in case
     * of an {@code invalid} provider or {@code providerId}.
     * @param callback callback when the lookup has completed or failed. The callback will always happen on the same thread
     * as this method is called on.
     * @return representation of the async task that can be used to cancel it if needed.
     */
    public RealmAsyncTask retrieveInfoForUserAsync(final String providerUserIdentity, final String provider, final RequestCallback<SyncUserInfo> callback) {
        checkLooperThread("Asynchronously retrieving user is only possible from looper threads.");
        //noinspection ConstantConditions
        if (callback == null) {
            throw new IllegalArgumentException("Non-null 'callback' required.");
        }

        return new Request<SyncUserInfo>(SyncManager.NETWORK_POOL_EXECUTOR, callback) {
            // TODO remove this override on next major release when we remove the deprecated Callback
            @Override
            public SyncUser run() {return null;}

            @Override
            public SyncUserInfo execute() throws ObjectServerError {
                return retrieveInfoForUser(providerUserIdentity, provider);
            }
        }.start();
    }

    private static void checkLooperThread(String errorMessage) {
        AndroidCapabilities capabilities = new AndroidCapabilities();
        capabilities.checkCanDeliverNotification(errorMessage);
    }

    /**
     * Returns a JSON token representing this user.
     * <p>
     * Possession of this JSON token can potentially grant access to data stored on the Realm Object Server, so it
     * should be treated as sensitive data.
     *
     * @return JSON string representing this user. It can be converted back into a real user object using
     * {@link #fromJson(String)}.
     * @see #fromJson(String)
     */
    public String toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("authUrl", authenticationUrl);
            obj.put("userToken", refreshToken.toJson());
            return obj.toString();
        } catch (JSONException e) {
            throw new RuntimeException("Could not convert SyncUser to JSON", e);
        }
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
        return refreshToken != null && refreshToken.expiresMs() > System.currentTimeMillis() && SyncManager.getUserStore().isActive(identity, authenticationUrl.toString());
    }

    /**
     * Returns {@code true} if this user is an administrator on the Realm Object Server, {@code false} otherwise.
     * <p>
     * Administrators can access all Realms on the server as well as change the permissions of the Realms.
     *
     * @return {@code true} if the user is an administrator on the Realm Object Server, {@code false} otherwise.
     */
    public boolean isAdmin() {
        return refreshToken.isAdmin();
    }

    /**
     * Returns the identity of this user on the Realm Object Server. The identity is a guaranteed to be unique
     * among all users on the Realm Object Server.
     *
     * @return identity of the user on the Realm Object Server. If the user has logged out or the login has expired
     * {@code null} is returned.
     */
    public String getIdentity() {
        return identity;
    }

    /**
     * Returns this user's access token. This is the users credential for accessing the Realm Object Server and should
     * be treated as sensitive data.
     *
     * @return the user's access token. If this user has logged out or the login has expired {@code null} is returned.
     */
    public Token getAccessToken() {
        return refreshToken;
    }

    void setRefreshToken(Token refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * Returns an instance of the Management Realm owned by the user.
     * <p>
     * This Realm can be used to control access and permissions for Realms owned by the user. This includes
     * giving other users access to Realms.
     *
     * @see <a href="https://realm.io/docs/realm-object-server/#permissions">How to control permissions</a>
     * @deprecated use {@link #getPermissionManager()} instead.
     */
    @Deprecated
    public Realm getManagementRealm() {
        return Realm.getInstance(managementConfig.initAndGetManagementRealmConfig(this));
    }

    /**
     * Returns all the valid sessions belonging to the user.
     *
     * @return the all valid sessions belong to the user.
     */
    public List<SyncSession> allSessions() {
        return SyncManager.getAllSessions(this);
    }

    /**
     * Checks if the user has access to the given Realm. Being authenticated means that the
     * user is known by the Realm Object Server and have been granted access to the given Realm.
     *
     * Authenticating will happen automatically as part of opening a Realm.
     */
    boolean isRealmAuthenticated(SyncConfiguration configuration) {
        Token token = realms.get(configuration);
        return token != null && token.expiresMs() > System.currentTimeMillis();
    }

    Token getAccessToken(SyncConfiguration configuration) {
        return realms.get(configuration);
    }

    void addRealm(SyncConfiguration syncConfiguration, Token accessToken) {
        realms.put(syncConfiguration, accessToken);
    }
    /**
     * Returns the {@link URL} where this user was authenticated.
     *
     * @return {@link URL} where the user was authenticated.
     */
    public URL getAuthenticationUrl() {
        return authenticationUrl;
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

    /**
     * Returns an instance of the {@link PermissionManager} for this user that makes it possible to see, modify and create
     * permissions related to this users Realms.
     * <p>
     * Every instance returned by this method must be closed by calling {@link PermissionManager#close()} when it
     * no longer is needed.
     * <p>
     * The {@link PermissionManager} can only be opened from the main tread, calling this method from any other thread
     * will throw an {@link IllegalStateException}.
     *
     * @throws IllegalStateException if this method is not called from the UI thread.
     * @return an instance of the PermissionManager.
     */
    public PermissionManager getPermissionManager() {
        if (!new AndroidCapabilities().isMainThread()) {
            throw new IllegalStateException("The PermissionManager can only be opened from the main thread.");
        }
        return PermissionManager.getInstance(this);
    }

    // what defines a user is it's identity(Token) and authURL (as required by the constructor)
    //
    // not the list of Realms it's managing, furthermore, trying to include the `realms` in the `hashCode` will
    // end in a StackOverFlow, since we need to calculate the `hashCode` of the SyncConfiguration which itself
    // contains a reference to the SyncUser.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SyncUser syncUser = (SyncUser) o;

        if (!identity.equals(syncUser.identity)) return false;
        return authenticationUrl.toExternalForm().equals(syncUser.authenticationUrl.toExternalForm());
    }

    @Override
    public int hashCode() {
        int result = identity.hashCode();
        result = 31 * result + authenticationUrl.toExternalForm().hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        sb.append("UserId: ").append(identity);
        sb.append(", AuthUrl: ").append(getAuthenticationUrl());
        sb.append("}");
        return sb.toString();
    }

    // Class wrapping requests made against the auth server. Is also responsible for calling with success/error on the
    // correct thread.
    private static abstract class Request<T> {

        @Nullable
        private final Callback callback;
        @Nullable
        private final RequestCallback<T> genericCallback;
        private final RealmNotifier handler;
        private final ThreadPoolExecutor networkPoolExecutor;

        Request(ThreadPoolExecutor networkPoolExecutor, @Nullable Callback callback) {
            this.callback = callback;
            this.genericCallback = null;
            this.handler = new AndroidRealmNotifier(null, new AndroidCapabilities());
            this.networkPoolExecutor = networkPoolExecutor;
        }

        Request(ThreadPoolExecutor networkPoolExecutor, @Nullable RequestCallback<T> callback) {
            this.callback = null;
            this.genericCallback = callback;
            this.handler = new AndroidRealmNotifier(null, new AndroidCapabilities());
            this.networkPoolExecutor = networkPoolExecutor;
        }

        // Implements the request. Return the current sync user if the request succeeded. Otherwise throw an error.
        public abstract SyncUser run() throws ObjectServerError;
        //TODO next major release, remove run, rename execute to run and make it abstract
        public T execute() throws ObjectServerError {return null;}

        // Start the request
        public RealmAsyncTask start() {
            Future<?> authenticateRequest = networkPoolExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        // co-exist the old and new callback
                        if (genericCallback != null) {
                            postSuccess(Request.this.execute());
                        } else {
                            postSuccess(Request.this.run());
                        }

                    } catch (ObjectServerError e) {
                        postError(e);
                    } catch (Throwable e) {
                        postError(new ObjectServerError(ErrorCode.UNKNOWN, "Unexpected error", e));
                    }
                }
            });
            return new RealmAsyncTaskImpl(authenticateRequest, networkPoolExecutor);
        }

        private void postError(final ObjectServerError error) {
            boolean errorHandled = false;
            if (callback != null) {
                Runnable action = new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(error);
                    }
                };
                errorHandled = handler.post(action);
            }

            if (!errorHandled) {
                RealmLog.error(error, "An error was thrown, but could not be handled.");
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

        private void postSuccess(final T result) {
            if (genericCallback != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        genericCallback.onSuccess(result);
                    }
                });
            }
        }
    }

    // TODO remove and replace uses by RequestCallback on next major release
    public interface Callback {
        /**
         * @deprecated as per 3.6.0 release, replaced by {@link RequestCallback#onSuccess(Object)}
         */
        @Deprecated
        void onSuccess(SyncUser user);

        /**
         * @deprecated as per 3.6.0 release, replaced by {@link RequestCallback#onError(ObjectServerError)}
         */
        @Deprecated
        void onError(ObjectServerError error);
    }

    public interface RequestCallback<T> {
        void onSuccess(T result);

        void onError(ObjectServerError error);
    }
}
