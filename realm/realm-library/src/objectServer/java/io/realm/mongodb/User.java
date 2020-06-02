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
package io.realm.mongodb;

import org.bson.codecs.configuration.CodecRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.realm.internal.mongodb.Request;
import io.realm.mongodb.auth.ApiKeyAuth;
import io.realm.RealmAsyncTask;
import io.realm.internal.network.ResultHandler;
import io.realm.internal.Util;
import io.realm.internal.jni.OsJNIResultCallback;
import io.realm.internal.jni.OsJNIVoidResultCallback;
import io.realm.internal.objectstore.OsJavaNetworkTransport;
import io.realm.internal.objectstore.OsSyncUser;
import io.realm.internal.util.Pair;
import io.realm.mongodb.functions.Functions;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.push.Push;

/**
 * FIXME
 */
public class User {

    OsSyncUser osUser;
    private final App app;
    private ApiKeyAuth apiKeyAuthProvider = null;
    private MongoClient mongoClient = null;
    private Functions functions = null;

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


    User(long nativePtr, App app) {
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
    public List<UserIdentity> getIdentities() {
        Pair<String, String>[] osIdentities = osUser.getIdentities();
        List<UserIdentity> identities = new ArrayList<>(osIdentities.length);
        for (int i = 0; i < osIdentities.length; i++) {
            Pair<String, String> data = osIdentities[i];
            identities.add(new UserIdentity(data.first, data.second));
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
     * Returns a unique identifier for the device the user logged in to.
     *
     * @return a unique device identifier for the user.
     */
    public String getDeviceId() {
        return osUser.getDeviceId();
    }

    /**
     * Returns the {@link App} this user is associated with.
     *
     * @return the {@link App} this user is associated with.
     */
    public App getApp() {
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
     * App app = new App("app-id")
     * User user = app.login(Credentials.anonymous());
     * user.linkCredentials(Credentials.emailPassword("email", "password"));
     * }
     * </pre>
     * <p>
     * Note: It is not possible to link two existing users of MongoDB Realm. The provided credentials
     * must not have been used by another user.
     *
     * @param credentials the credentials to link with the current user.
     * @throws IllegalStateException if no user is currently logged in.
     * @return the {@link User} the credentials were linked to.
     */
    public User linkCredentials(Credentials credentials) {
        Util.checkNull(credentials, "credentials");
        checkLoggedIn();
        AtomicReference<User> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        nativeLinkUser(app.nativePtr, osUser.getNativePtr(), credentials.osCredentials.getNativePtr(), new OsJNIResultCallback<User>(success, error) {
            @Override
            protected User mapSuccess(Object result) {
                osUser = new OsSyncUser((long) result); // OS returns the updated user as a new one.
                return User.this;
            }
        });
        return ResultHandler.handleResult(success, error);
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
     * App app = new App("app-id")
     * User user = app.login(Credentials.anonymous());
     * user.linkCredentials(Credentials.emailPassword("email", "password"));
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
    public RealmAsyncTask linkCredentialsAsync(Credentials credentials, App.Callback<User> callback) {
        Util.checkLooperThread("Asynchronous linking identities is only possible from looper threads.");
        return new Request<User>(App.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public User run() throws ObjectServerError {
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
    public User remove() throws ObjectServerError {
        boolean loggedIn = isLoggedIn();
        AtomicReference<User> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        nativeRemoveUser(app.nativePtr, osUser.getNativePtr(), new OsJNIResultCallback<User>(success, error) {
            @Override
            protected User mapSuccess(Object result) {
                return User.this;
            }
        });
        ResultHandler.handleResult(success, error);
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
    public RealmAsyncTask removeAsync(App.Callback<User> callback) {
        Util.checkLooperThread("Asynchronous removal of users is only possible from looper threads.");
        return new Request<User>(App.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public User run() throws ObjectServerError {
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
     * {@link User.State#LOGGED_OUT}. All other users will be marked as {@link User.State#LOGGED_OUT}
     * and will still be returned by {@link App#allUsers()}. They can be removed completely by calling
     * {@link #remove()}.
     *
     * @throws ObjectServerError if an error occurred while trying to log the user out of the Realm
     * App.
     */
    public void logOut() throws ObjectServerError {
        boolean loggedIn = isLoggedIn();
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        nativeLogOut(app.nativePtr, osUser.getNativePtr(), new OsJNIVoidResultCallback(error));
        ResultHandler.handleResult(null, error);
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
     * {@link User.State#LOGGED_OUT}. All other users will be marked as {@link User.State#LOGGED_OUT}
     * and will still be returned by {@link App#allUsers()}. They can be removed completely by calling
     * {@link #remove()}.
     *
     * @param callback callback when logging out has completed or failed. The callback will always
     * happen on the same thread as this method is called on.
     * @throws IllegalStateException if called from a non-looper thread.
     */
    public RealmAsyncTask logOutAsync(App.Callback<User> callback) {
        Util.checkLooperThread("Asynchronous log out is only possible from looper threads.");
        return new Request<User>(App.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public User run() throws ObjectServerError {
                logOut();
                return User.this;
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
            apiKeyAuthProvider = new ApiKeyAuthImpl(this);
        }
        return apiKeyAuthProvider;
    }

    /**
     * Returns a <i>Realm Functions</i> manager for invoking MongoDB Realm Functions.
     * <p>
     * This will use the associated app's default codec registry to encode and decode arguments and
     * results.
     */
    public synchronized Functions getFunctions() {
        checkLoggedIn();
        if (functions == null) {
            functions = new FunctionsImpl(this);
        }
        return functions;
    }

    /**
     * Returns a <i>Realm Functions</i> manager for invoking MongoDB Realm Functions with custom
     * codec registry for encoding and decoding arguments and results.
     */
    public Functions getFunctions(CodecRegistry codecRegistry) {
        return new FunctionsImpl(this, codecRegistry);
    }

    /**
     * FIXME Add support for push notifications. Name of Class and method still TBD.
     */
    public Push getPushNotifications() {
        return null;
    }

    /**
     * FIXME Add support for the MongoDB wrapper. Name of Class and method still TBD.
     */
    public MongoClient getMongoClient(String serviceName) {
        if (mongoClient == null) {
            mongoClient = new MongoClient(this, serviceName, app.getConfiguration().getDefaultCodecRegistry());
        }
        return mongoClient;
    }

    @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!osUser.equals(user.osUser)) return false;
        return app.equals(user.app);
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
