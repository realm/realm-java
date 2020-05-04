/*
 * Copyright 2020 Realm Inc.
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.realm.internal.Util;
import io.realm.internal.jni.OsJNIResultCallback;
import io.realm.internal.jni.OsJNIVoidResultCallback;
import io.realm.internal.objectstore.OsJavaNetworkTransport;
import io.realm.internal.objectstore.OsSyncUser;
import io.realm.internal.util.Pair;
import io.realm.mongodb.RemoteMongoClient;

import static io.realm.RealmApp.handleResult;

/**
 * FIXME
 */
public class RealmUser {

    OsSyncUser osUser;
    private final RealmApp app;
    private ApiKeyAuth apiKeyAuthProvider = null;
    private RemoteMongoClient remoteMongoClient = null;

    /**
     * FIXME
     */
    enum UserType {
        NORMAL("normal"),
        SERVER("server"),
        UNKNOWN("unknown");

        private final String key;

        UserType(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public enum State {
        LOGGED_IN(OsSyncUser.STATE_LOGGED_IN),
        REMOVED(OsSyncUser.STATE_REMOVED),
        LOGGED_OUT(OsSyncUser.STATE_LOGGED_OUT);

        private final byte nativeValue;

        State(byte nativeValue) {
            this.nativeValue = nativeValue;
        }

        byte getKey() {
            return nativeValue;
        }
    }


    RealmUser(long nativePtr, RealmApp app) {
        this.osUser = new OsSyncUser(nativePtr);
        this.app = app;
    }

    /**
     * FIXME
     * @return
     */
    public String getId() {
        return osUser.getIdentity();
    }

    /**
     * FIXME
     * @return
     */
    public String getName() {
        return osUser.nativeGetName();
    }

    /**
     * FIXME
     * @return
     */
    @Nullable
    public String getEmail() {
        return osUser.getEmail();
    }

    /**
     * FIXME
     * @return
     */
    @Nullable
    public String getPictureUrl() {
        return osUser.getPictureUrl();
    }

    /**
     * FIXME
     * @return
     */
    @Nullable
    public String getFirstName() {
        return osUser.getFirstName();
    }

    /**
     * FIXME
     * @return
     */
    @Nullable
    public String getLastName() {
        return osUser.getLastName();
    }

    /**
     * FIXME
     * @return
     */
    @Nullable
    public String getGender() {
        return osUser.getGender();
    }

    /**
     * FIXME
     * @return
     */
    @Nullable
    public String getBirthday() {
        return osUser.getBirthday();
    }

    /**
     * FIXME
     * @return
     */
    @Nullable
    public Long getMinAge() {
        String minAge = osUser.getMinAge();
        return (minAge == null) ? null : Long.parseLong(minAge);
    }

    /**
     * FIXME
     * @return
     */
    @Nullable
    public Long getMaxAge() {
        String maxAge = osUser.getMaxAge();
        return (maxAge == null) ? null : Long.parseLong(maxAge);
    }

    /**
     * FIXME
     * @return
     */
    public List<RealmUserIdentity> getIdentities() {
        Pair<String, String>[] osIdentities = osUser.getIdentities();
        List<RealmUserIdentity> identities = new ArrayList<>(osIdentities.length);
        for (int i = 0; i < osIdentities.length; i++) {
            Pair<String, String> data = osIdentities[i];
            identities.add(new RealmUserIdentity(data.first, data.second));
        }
        return identities;
    }

    /**
     * FIXME
     * @return
     */
    public String getAccessToken() {
        return osUser.getAccessToken();
    }

    /**
     * FIXME
     * @return
     */
    public String getRefreshToken() {
        return osUser.getRefreshToken();
    }


    /**
     * Returns the {@link RealmApp} this user is associated with.
     *
     * @return the {@link RealmApp} this user is associated with.
     */
    public RealmApp getApp() {
        return app;
    }

    /**
     * Returns the {@link State} the user is in.
     *
     * @return the {@link State} of the user.
     */
    public State getState() {
        byte nativeState = osUser.getState();
        for (State state : State.values()) {
            if (state.nativeValue == nativeState) {
                return state;
            }
        }
        throw new IllegalStateException("Unknown state: " + nativeState);
    }

    /**
     * Returns true if the user is currently logged in.
     * Returns whether or not this user is still logged into the MongoDB Realm App.
     *
     * @return {@code true} if the user is logged in. {@code false} otherwise.
     * @return {@code true} if still logged in, {@code false} if not.
     */
    public boolean isLoggedIn() {
        return getState() == State.LOGGED_IN;
    }

    /**
     * Links the current user with a new user identity represented by the given credentials.
     * <p>
     * Linking a user with more credentials, mean the user can login either of these credentials.
     * It also makes it possible to "upgrade" an anonymous user by linking it with e.g.
     * Email/Password credentials.
     * <pre>
     * {@code
     * // Example
     * RealmApp app = new RealmApp("app-id")
     * RealmUser user = app.login(RealmCredentials.anonymous());
     * user.linkCredentials(RealmCredentials.emailPassword("email", "password"));
     * }
     * </pre>
     * <p>
     * Note: It is not possible to link two existing users of MongoDB Realm. The provided credentials
     * must not have been used by another user.
     *
     * @param credentials the credentials to link with the current user.
     * @throws IllegalStateException if no user is currently logged in.
     * @return the {@link io.realm.RealmUser} the credentials were linked to.
     */
    public RealmUser linkCredentials(RealmCredentials credentials) {
        Util.checkNull(credentials, "credentials");
        checkLoggedIn();
        AtomicReference<RealmUser> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        nativeLinkUser(app.nativePtr, osUser.getNativePtr(), credentials.osCredentials.getNativePtr(), new OsJNIResultCallback<RealmUser>(success, error) {
            @Override
            protected RealmUser mapSuccess(Object result) {
                osUser = new OsSyncUser((long) result); // OS returns the updated user as a new one.
                return RealmUser.this;
            }
        });
        return handleResult(success, error);
    }

    /**
     * Links the current user with a new user identity represented by the given credentials.
     * <p>
     * Linking a user with more credentials, mean the user can login either of these credentials.
     * It also makes it possible to "upgrade" an anonymous user by linking it with e.g.
     * Email/Password credentials.
     * <pre>
     * {@code
     * // Example
     * RealmApp app = new RealmApp("app-id")
     * RealmUser user = app.login(RealmCredentials.anonymous());
     * user.linkCredentials(RealmCredentials.emailPassword("email", "password"));
     * }
     * </pre>
     * <p>
     * Note: It is not possible to link two existing users of MongoDB Realm. The provided credentials
     * must not have been used by another user.
     *
     * @param credentials the credentials to link with the current user.
     * @param callback callback when user identities has been linked or it failed. The callback will
     * always happen on the same thread as this method is called on.
     * @throws IllegalStateException if called from a non-looper thread.
     */
    public RealmAsyncTask linkCredentialsAsync(RealmCredentials credentials, RealmApp.Callback<RealmUser> callback) {
        Util.checkLooperThread("Asynchronous linking identities is only possible from looper threads.");
        return new RealmApp.Request<RealmUser>(RealmApp.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public RealmUser run() throws ObjectServerError {
                return linkCredentials(credentials);
            }
        }.start();
    }

    /**
     * Removes a users credentials from this device. If the user was currently logged in, they
     * will be logged out as part of the process. This is only a local change and does not
     * affect the user state on the server.
     *
     * @return user that was removed.
     * @throws ObjectServerError if called from the UI thread or if the user was logged in, but
     * could not be logged out.
     */
    public RealmUser remove() throws ObjectServerError {
        boolean loggedIn = isLoggedIn();
        AtomicReference<RealmUser> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        nativeRemoveUser(app.nativePtr, osUser.getNativePtr(), new OsJNIResultCallback<RealmUser>(success, error) {
            @Override
            protected RealmUser mapSuccess(Object result) {
                return RealmUser.this;
            }
        });
        handleResult(success, error);
        if (loggedIn) {
            app.notifyUserLoggedOut(this);
        }
        return this;
    }

    /**
     * Removes a user's credentials from this device. If the user was currently logged in, they
     * will be logged out as part of the process. This is only a local change and does not
     * affect the user state on the server.
     *
     * @param callback callback when removing the user has completed or failed. The callback will always
     * happen on the same thread as this method is called on.
     * @throws IllegalStateException if called from a non-looper thread.
     */
    public RealmAsyncTask removeAsync(RealmApp.Callback<RealmUser> callback) {
        Util.checkLooperThread("Asynchronous removal of users is only possible from looper threads.");
        return new RealmApp.Request<RealmUser>(RealmApp.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public RealmUser run() throws ObjectServerError {
                return remove();
            }
        }.start();
    }

    /**
     * Log the user out of the Realm App. This will unregister them on the device, stop any
     * synchronization to and from the users' Realms, and those Realms will be deleted next time
     * the app restarts. Therefor logging out should not be done until all changes to Realms have
     * been uploaded to the server.
     * <p>
     * Once the Realm App has confirmed the logout any registered {@link AuthenticationListener}
     * will be notified and user credentials will be deleted from this device.
     * <p>
     * Logging out anonymous users will remove them immediately instead of marking them as
     * {@link RealmUser.State#LOGGED_OUT}. All other users will be marked as {@link RealmUser.State#LOGGED_OUT}
     * and will still be returned by {@link RealmApp#allUsers()}. They can be removed completely by calling
     * {@link #remove()}.
     *
     * @throws ObjectServerError if an error occurred while trying to log the user out of the Realm
     * App.
     */
    public void logOut() throws ObjectServerError {
        boolean loggedIn = isLoggedIn();
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        nativeLogOut(app.nativePtr, osUser.getNativePtr(), new OsJNIVoidResultCallback(error));
        handleResult(null, error);
        if (loggedIn) {
            app.notifyUserLoggedOut(this);
        }
    }

    /**
     * Log the user out of the Realm App asynchronously. This will unregister them on the device, stop any
     * synchronization to and from the users' Realms, and those Realms will be deleted next time
     * the app restarts. Therefor logging out should not be done until all changes to Realms have
     * been uploaded to the server.
     * <p>
     * Once the Realm App has confirmed the logout any registered {@link AuthenticationListener}
     * will be notified and user credentials will be deleted from this device.
     * <p>
     * Logging out anonymous users will remove them immediately instead of marking them as
     * {@link RealmUser.State#LOGGED_OUT}. All other users will be marked as {@link RealmUser.State#LOGGED_OUT}
     * and will still be returned by {@link RealmApp#allUsers()}. They can be removed completely by calling
     * {@link #remove()}.
     *
     * @param callback callback when logging out has completed or failed. The callback will always
     * happen on the same thread as this method is called on.
     * @throws IllegalStateException if called from a non-looper thread.
     */
    public RealmAsyncTask logOutAsync(RealmApp.Callback<RealmUser> callback) {
        Util.checkLooperThread("Asynchronous log out is only possible from looper threads.");
        return new RealmApp.Request<RealmUser>(RealmApp.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public RealmUser run() throws ObjectServerError {
                logOut();
                return RealmUser.this;
            }
        }.start();
    }

    /**
     * Returns a wrapper for managing API keys controlled by the current user.
     *
     * @return wrapper for managing API keys controlled by the current user.
     * @throws IllegalStateException if no user is currently logged in.
     */
    public synchronized ApiKeyAuth getApiKeyAuth() {
        checkLoggedIn();
        if (apiKeyAuthProvider == null) {
            apiKeyAuthProvider = new ApiKeyAuth(this);
        }
        return apiKeyAuthProvider;
    }

    /**
     * FIXME Add support for functions. Name of Class and method still TBD.
     */
    public RealmFunctions getFunctions() {
        return null;
    }

    /**
     * FIXME Add support for push notifications. Name of Class and method still TBD.
     */
    public RealmPushNotifications getPushNotifications() {
        return null;
    }

    /**
     * FIXME Add support for the MongoDB wrapper. Name of Class and method still TBD.
     */
    public RemoteMongoClient getRemoteMongoClient() {
        if (remoteMongoClient == null) {
            // FIXME: serviceName?
            remoteMongoClient = new RemoteMongoClient(this, "serviceName");
        }
        return remoteMongoClient;
    }

    @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RealmUser realmUser = (RealmUser) o;

        if (!osUser.equals(realmUser.osUser)) return false;
        return app.equals(realmUser.app);
    }

    @Override
    public int hashCode() {
        int result = osUser.hashCode();
        result = 31 * result + app.hashCode();
        return result;
    }

    private void checkLoggedIn() {
        if (!isLoggedIn()) {
            throw new IllegalStateException("User is not logged in.");
        }
    }

    private static native void nativeRemoveUser(long nativeAppPtr, long nativeUserPtr, OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeLinkUser(long nativeAppPtr, long nativeUserPtr, long nativeCredentialsPtr, OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeLogOut(long appNativePtr, long userNativePtr, OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
}
