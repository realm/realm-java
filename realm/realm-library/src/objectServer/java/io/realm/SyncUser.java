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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import io.realm.internal.network.UpdateAccountResponse;
import io.realm.internal.objectserver.Token;
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
    private SyncConfiguration defaultConfiguration;

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
    public static SyncUser current() {
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
    public static SyncUser logIn(final SyncCredentials credentials, final String authenticationUrl) throws ObjectServerError {
        URL authUrl = getUrl(authenticationUrl);

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
     * Converts the input URL to a Realm Authentication URL
     *
     * @param authenticationUrl user provided url string.
     *
     * @return normalized authentication url.
     * @throws IllegalArgumentException if something was wrong with the URL.
     */
    private static URL getUrl(String authenticationUrl) {
        try {
            URL authUrl = new URL(authenticationUrl);
            // If no path segment is provided append `/auth` which is the standard location.
            if (authUrl.getPath().equals("")) {
                authUrl = new URL(authUrl.toString() + "/auth");
            }
            return authUrl;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL " + authenticationUrl + ".", e);
        }
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
    public static RealmAsyncTask logInAsync(final SyncCredentials credentials, final String authenticationUrl, final Callback<SyncUser> callback) {
        checkLooperThread("Asynchronous login is only possible from looper threads.");
        return new Request<SyncUser>(SyncManager.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public SyncUser run() throws ObjectServerError {
                return logIn(credentials, authenticationUrl);
            }
        }.start();
    }

    /**
     * Opening a synchronized Realm requires a {@link SyncConfiguration}. This method creates a
     * {@link SyncConfiguration.Builder} that can be used to create it by calling {@link SyncConfiguration.Builder#build()}.
     * <p>
     * The default synchronization mode for this Realm is <a href="https://docs.realm.io/platform/using-synced-realms/syncing-data">query-based synchronizaton</a>,
     * but see the {@link SyncConfiguration.Builder} class for more details on how to configure a Realm.
     * <p>
     * A synchronized Realm is identified by an unique URI. In the URI, {@code /~/} can be used as a placeholder for
     * a user ID in case the Realm should only be available to one user e.g., {@code "realm://objectserver.realm.io/~/default"}.
     * <p>
     * The URL cannot end with {@code .realm}, {@code .realm.lock} or {@code .realm.management}.
     * <p>
     * The {@code /~/} will automatically be replaced with the user ID when creating the {@link SyncConfiguration}.
     * <p>
     * Moreover, the URI defines the local location on disk. The location of a synchronized Realm file is
     * {@code /data/data/<packageName>/files/realm-object-server/<user-id>/<last-path-segment>}, but this behavior
     * can be overwritten using {@link SyncConfiguration.Builder#name(String)} and {@link SyncConfiguration.Builder#directory(File)}.
     * <p>
     * Many Android devices are using FAT32 file systems. FAT32 file systems have a limitation that
     * file names cannot be longer than 255 characters. Moreover, the entire URI should not exceed 256 characters.
     * If the file name and underlying path are too long to handle for FAT32, a shorter unique name will be generated.
     * See also @{link https://msdn.microsoft.com/en-us/library/aa365247(VS.85).aspx}.
     *
     * @param uri URI identifying the Realm. If only a path like {@code /~/default} is given, the configuration will
     *            assume the file is located on the same server returned by {@link #getAuthenticationUrl()}.
     *
     * @throws IllegalStateException if the user isn't valid. See {@link #isValid()}.
     */
    public SyncConfiguration.Builder createConfiguration(String uri) {
        if (!isValid()) {
            throw new IllegalStateException("Configurations can only be created from valid users");
        }
        return new SyncConfiguration.Builder(this, uri).partialRealm();
    }

    /**
     * Returns the default configuration for this user. The default configuration points to the
     * default query-based Realm on the server the user authenticated against.
     *
     * @return the default configuration for this user.
     * @throws IllegalStateException if the user isn't valid. See {@link #isValid()}.
     */
    public SyncConfiguration getDefaultConfiguration() {
        if (!isValid()) {
            throw new IllegalStateException("The default configuration can only be created for users that are logged in.");
        }
        if (defaultConfiguration == null) {
            defaultConfiguration = new SyncConfiguration.Builder(this, createUrl(this))
                    .partialRealm()
                    .build();
        }
        return defaultConfiguration;
    }

    // Infer the URL to the default Realm based on the server used to login the user
    private static String createUrl(SyncUser user) {
        URL url = user.getAuthenticationUrl();
        String protocol = url.getProtocol();
        String host = url.getHost();
        int port = url.getPort();
        if (port != -1) { // port set
            host += ":" + port;
        }

        if (protocol.equalsIgnoreCase("https")) {
            protocol = "realms";
        } else {
            protocol = "realm";
        }

        return protocol + "://" + host + "/default";
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
    public void logOut() {
        // Acquire lock to prevent users creating new instances
        synchronized (Realm.class) {
            if (!SyncManager.getUserStore().isActive(identity, authenticationUrl.toString())) {
                return; // Already logged out status
            }

            // Mark the user as logged out in the ObjectStore
            SyncManager.getUserStore().remove(identity, authenticationUrl.toString());

            // invalidate all pending refresh_token queries
            for (SyncConfiguration syncConfiguration : realms.keySet()) {
                try {
                    SyncSession session = SyncManager.getSession(syncConfiguration);
                    session.clearScheduledAccessTokenRefresh();
                } catch (IllegalStateException e) {
                    if (!e.getMessage().contains("No SyncSession found")) {
                        throw e;
                    }// else no session, either the Realm was not opened or session was removed.
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
            // as it may revoke the newly acquired refresh_token
            final Token refreshTokenToBeRevoked = refreshToken;

            ThreadPoolExecutor networkPoolExecutor = SyncManager.NETWORK_POOL_EXECUTOR;
            networkPoolExecutor.submit(new ExponentialBackoffTask<LogoutResponse>(3) {

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
    public RealmAsyncTask changePasswordAsync(final String newPassword, final Callback<SyncUser> callback) {
        checkLooperThread("Asynchronous changing password is only possible from looper threads.");
        //noinspection ConstantConditions
        if (callback == null) {
            throw new IllegalArgumentException("Non-null 'callback' required.");
        }
        return new Request<SyncUser>(SyncManager.NETWORK_POOL_EXECUTOR, callback) {
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
    public RealmAsyncTask changePasswordAsync(final String userId, final String newPassword, final Callback<SyncUser> callback) {
        checkLooperThread("Asynchronous changing password is only possible from looper threads.");
        //noinspection ConstantConditions
        if (callback == null) {
            throw new IllegalArgumentException("Non-null 'callback' required.");
        }

        return new Request<SyncUser>(SyncManager.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public SyncUser run() {
                changePassword(userId, newPassword);
                return SyncUser.this;
            }
        }.start();
    }


    /**
     * Request a password reset email to be sent to a user's email.
     * This will not fail, even if the email doesn't belong to a Realm Object Server user.
     * <p>
     * This can only be used for users who authenticated with the {@link SyncCredentials.IdentityProvider#USERNAME_PASSWORD}
     * provider, and passed a valid email address as a username.
     *
     * @param email email that corresponds to the user's username.
     * @param authenticationUrl the url used to authenticate the user.
     * @throws IllegalStateException if this method is called on the UI thread.
     * @throws IllegalArgumentException if no email or authenticationUrl was provided.
     * @throws ObjectServerError if an error happened on the server.
     */
    public static void requestPasswordReset(String email, String authenticationUrl) throws ObjectServerError {
        if (Util.isEmptyString(email)) {
            throw new IllegalArgumentException("Not-null 'email' required.");
        }
        URL authUrl = getUrl(authenticationUrl);
        AuthenticationServer authServer = SyncManager.getAuthServer();
        UpdateAccountResponse response = authServer.requestPasswordReset(email, authUrl);
        if (!response.isValid()) {
            throw response.getError();
        }
    }

    /**
     * Request a password reset email to be sent to a user's email.
     * This will not fail, even if the email doesn't belong to a Realm Object Server user.
     * <p>
     * This can only be used for users who authenticated with the {@link SyncCredentials.IdentityProvider#USERNAME_PASSWORD}
     * provider, and passed a valid email address as a username.
     *
     * @param email email that corresponds to the user's username.
     * @param authenticationUrl the url used to authenticate the user.
     * @param callback callback when the request has completed or failed. The callback will always happen on the same thread
     * as this method is called on.
     * @return representation of the async task that can be used to cancel it if needed.
     * @throws IllegalStateException if this method is called on a non-looper thread.
     * @throws IllegalArgumentException if no email or authenticationUrl was provided.
     */
    public static RealmAsyncTask requestPasswordResetAsync(final String email, final String authenticationUrl, final Callback<Void> callback) {
        checkLooperThread("Asynchronous requesting a password reset is only possible from looper threads.");
        //noinspection ConstantConditions
        if (callback == null) {
            throw new IllegalArgumentException("Non-null 'callback' required.");
        }

        return new Request<Void>(SyncManager.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public Void run() {
                requestPasswordReset(email, authenticationUrl);
                return null;
            }
        }.start();
    }

    /**
     * Complete the password reset flow by using the reset token sent to the user's email as a one-time authorization
     * token to change the password.
     * <p>
     * This can only be used for users who authenticated with the {@link SyncCredentials.IdentityProvider#USERNAME_PASSWORD}
     * provider, and passed a valid email address as a username.
     * <p>
     * By default, Realm Object Server will send a link to the user's email that will redirect to a webpage where
     * they can enter their new password. If you wish to provide a native UX, you may wish to modify the password
     * authentication provider to use a custom URL with deep linking, so you can open the app, extract the token, and
     * navigate to a view that allows to change the password within the app.
     *
     * @param resetToken the token that was sent to the user's email address.
     * @param newPassword the user's new password.
     * @param authenticationUrl the url used to authenticate the user.
     * @throws IllegalStateException if this method is called on the UI thread.
     * @throws IllegalArgumentException if no {@code token} or {@code newPassword} was provided.
     * @throws ObjectServerError if an error happened on the server.
     */
    public static void completePasswordReset(String resetToken, String newPassword, String authenticationUrl) {
        if (Util.isEmptyString(resetToken)) {
            throw new IllegalArgumentException("Not-null 'token' required.");
        }
        if (Util.isEmptyString(newPassword)) {
            throw new IllegalArgumentException("Not-null 'newPassword' required.");
        }
        URL authUrl = getUrl(authenticationUrl);
        AuthenticationServer authServer = SyncManager.getAuthServer();
        UpdateAccountResponse response = authServer.completePasswordReset(resetToken, newPassword, authUrl);
        if (!response.isValid()) {
            throw response.getError();
        }
    }

    /**
     * Complete the password reset flow by using the reset token sent to the user's email as a one-time authorization
     * token to change the password.
     * <p>
     * This can only be used for users who authenticated with the {@link SyncCredentials.IdentityProvider#USERNAME_PASSWORD}
     * provider, and passed a valid email address as a username.
     * <p>
     * By default, Realm Object Server will send a link to the user's email that will redirect to a webpage where
     * they can enter their new password. If you wish to provide a native UX, you may wish to modify the password
     * authentication provider to use a custom URL with deep linking, so you can open the app, extract the token, and
     * navigate to a view that allows to change the password within the app.
     *
     * @param resetToken the token that was sent to the user's email address.
     * @param newPassword the user's new password.
     * @param authenticationUrl the url used to authenticate the user.
     * @param callback callback when the server has accepted the new password or failed. The callback will always happen on the same thread
     * as this method is called on.
     * @return representation of the async task that can be used to cancel it if needed.
     * @throws IllegalStateException if this method is called on a non-looper thread.
     * @throws IllegalArgumentException if no {@code token} or {@code newPassword} was provided.
     */
    public static RealmAsyncTask completePasswordResetAsync(final String resetToken,
                                           final String newPassword,
                                           final String authenticationUrl,
                                           final Callback<Void> callback) throws ObjectServerError {
        checkLooperThread("Asynchronously completing a password reset is only possible from looper threads.");
        //noinspection ConstantConditions
        if (callback == null) {
            throw new IllegalArgumentException("Non-null 'callback' required.");
        }

        return new Request<Void>(SyncManager.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public Void run() {
                completePasswordReset(resetToken, newPassword, authenticationUrl);
                return null;
            }
        }.start();
    }

    /**
     * Request an email confirmation email to be sent to a user's email.
     * This will not fail, even if the email doesn't belong to a Realm Object Server user.
     * <p>
     * This can only be used for users who authenticated with the {@link SyncCredentials.IdentityProvider#USERNAME_PASSWORD}
     * provider, and passed a valid email address as a username.
     *
     * @param email the email that corresponds to the user's username.
     * @param authenticationUrl the url used to authenticate the user.
     * @throws IllegalStateException if this method is called on the UI thread.
     * @throws IllegalArgumentException if no {@code email} was provided.
     * @throws ObjectServerError if an error happened on the server.
     */
    public static void requestEmailConfirmation(String email, String authenticationUrl) throws ObjectServerError {
        if (Util.isEmptyString(email)) {
            throw new IllegalArgumentException("Not-null 'email' required.");
        }
        URL authUrl = getUrl(authenticationUrl);
        AuthenticationServer authServer = SyncManager.getAuthServer();
        UpdateAccountResponse response = authServer.requestEmailConfirmation(email, authUrl);
        if (!response.isValid()) {
            throw response.getError();
        }
    }

    /**
     * Request an email confirmation email to be sent to a user's email.
     * This will not fail, even if the email doesn't belong to a Realm Object Server user.
     * <p>
     * This can only be used for users who authenticated with the {@link SyncCredentials.IdentityProvider#USERNAME_PASSWORD}
     * provider, and passed a valid email address as a username.
     *
     * @param email the email that corresponds to the user's username.
     * @param authenticationUrl the url used to authenticate the user.
     * @param callback callback when the request has completed or failed. The callback will always happen on the same thread
     * as this method is called on.
     * @return representation of the async task that can be used to cancel it if needed.
     * @throws IllegalStateException if this method is called on a non-looper thread.
     * @throws IllegalArgumentException if no {@code email} was provided.
     */
    public static RealmAsyncTask requestEmailConfirmationAsync(final String email, final String authenticationUrl, final Callback<Void> callback) {
        checkLooperThread("Asynchronously requesting an email confirmation is only possible from looper threads.");
        //noinspection ConstantConditions
        if (callback == null) {
            throw new IllegalArgumentException("Non-null 'callback' required.");
        }

        return new Request<Void>(SyncManager.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public Void run() {
                requestEmailConfirmation(email, authenticationUrl);
                return null;
            }
        }.start();
    }

    /**
     * Complete the email confirmation flow by using the confirmation token sent to the user's email as a one-time
     * authorization token to confirm their email.
     * <p>
     * This can only be used for users who authenticated with the {@link SyncCredentials.IdentityProvider#USERNAME_PASSWORD}
     * provider, and passed a valid email address as a username.
     * <p>
     * By default, Realm Object Server will send a link to the user's email that will redirect to a webpage where
     * they can enter their new password. If you wish to provide a native UX, you may wish to modify the password
     * authentication provider to use a custom URL with deep linking, so you can open the app, extract the token,
     * and navigate to a view that allows to confirm the email within the app.
     *
     * @param confirmationToken the token that was sent to the user's email address.
     * @param authenticationUrl the url used to authenticate the user.
     * @throws IllegalStateException if this method is called on the UI thread.
     * @throws IllegalArgumentException if no {@code confirmationToken} was provided.
     * @throws ObjectServerError if an error happened on the server.
     */
    public static void confirmEmail(String confirmationToken, String authenticationUrl) throws ObjectServerError {
        if (Util.isEmptyString(confirmationToken)) {
            throw new IllegalArgumentException("Not-null 'confirmationToken' required.");
        }
        URL authUrl = getUrl(authenticationUrl);
        AuthenticationServer authServer = SyncManager.getAuthServer();
        UpdateAccountResponse response = authServer.confirmEmail(confirmationToken, authUrl);
        if (!response.isValid()) {
            throw response.getError();
        }
    }

    /**
     * Complete the email confirmation flow by using the confirmation token sent to the user's email as a one-time
     * authorization token to confirm their email. This functionalit
     * <p>
     * This can only be used for users who authenticated with the {@link SyncCredentials.IdentityProvider#USERNAME_PASSWORD}
     * provider, and passed a valid email address as a username.
     * <p>
     * By default, Realm Object Server will send a link to the user's email that will redirect to a webpage where
     * they can enter their new password. If you wish to provide a native UX, you may wish to modify the password
     * authentication provider to use a custom URL with deep linking, so you can open the app, extract the token,
     * and navigate to a view that allows to confirm the email within the app.
     *
     * @param confirmationToken the token that was sent to the user's email address.
     * @param authenticationUrl the url used to authenticate the user.
     * @param callback callback when the server has confirmed the email or failed. The callback will always happen on the same thread
     * as this method is called on.
     * @return representation of the async task that can be used to cancel it if needed.
     * @throws IllegalStateException if this method is called on a non-looper thread.
     * @throws IllegalArgumentException if no {@code confirmationToken} was provided.
     */
    public static RealmAsyncTask confirmEmailAsync(final String confirmationToken,
                                            final String authenticationUrl,
                                            final Callback<Void> callback) {
        checkLooperThread("Asynchronously confirming an email is only possible from looper threads.");
        //noinspection ConstantConditions
        if (callback == null) {
            throw new IllegalArgumentException("Non-null 'callback' required.");
        }

        return new Request<Void>(SyncManager.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public Void run() {
                confirmEmail(confirmationToken, authenticationUrl);
                return null;
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
     * @throws IllegalStateException if this method is called on the UI thread.
     * @throws IllegalArgumentException if no {@code providerUserIdentity} or {@code provider} string was provided.
     * @throws ObjectServerError if an error happened on the server.
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
            if (response.getError().getErrorCode() == ErrorCode.UNKNOWN_ACCOUNT) {
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
     * @return representation of the async task that can be used to cancel it if needed.
     * @param callback callback when the lookup has completed or failed. The callback will always happen on the same thread
     * as this method is called on.
     * @return representation of the async task that can be used to cancel it if needed.
     */
    public RealmAsyncTask retrieveInfoForUserAsync(final String providerUserIdentity, final String provider, final Callback<SyncUserInfo> callback) {
        checkLooperThread("Asynchronously retrieving user is only possible from looper threads.");
        //noinspection ConstantConditions
        if (callback == null) {
            throw new IllegalArgumentException("Non-null 'callback' required.");
        }

        return new Request<SyncUserInfo>(SyncManager.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public SyncUserInfo run() throws ObjectServerError {
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
     * Returns this user's refresh token. This is the users credential for accessing the Realm Object Server and should
     * be treated as sensitive data.
     *
     * @return the user's refresh token. If this user has logged out or the login has expired {@code null} is returned.
     */
    Token getRefreshToken() {
        return refreshToken;
    }

    void setRefreshToken(Token refreshToken) {
        this.refreshToken = refreshToken;
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
        private final Callback<T> callback;
        private final RealmNotifier handler;
        private final ThreadPoolExecutor networkPoolExecutor;

        Request(ThreadPoolExecutor networkPoolExecutor, @Nullable Callback<T> callback) {
            this.callback = callback;
            this.handler = new AndroidRealmNotifier(null, new AndroidCapabilities());
            this.networkPoolExecutor = networkPoolExecutor;
        }

        // Implements the request. Return the current sync user if the request succeeded. Otherwise throw an error.
        public abstract T run() throws ObjectServerError;

        // Start the request
        public RealmAsyncTask start() {
            Future<?> authenticateRequest = networkPoolExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        postSuccess(Request.this.run());
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

        private void postSuccess(final T result) {
            if (callback != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(result);
                    }
                });
            }
        }
    }

    public interface Callback<T> {
        void onSuccess(T result);

        void onError(ObjectServerError error);
    }
}
