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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.realm.internal.Keep;
import io.realm.internal.RealmNotifier;
import io.realm.internal.Util;
import io.realm.internal.android.AndroidCapabilities;
import io.realm.internal.android.AndroidRealmNotifier;
import io.realm.internal.async.RealmAsyncTaskImpl;
import io.realm.internal.async.RealmThreadPoolExecutor;
import io.realm.internal.network.OkHttpNetworkTransport;
import io.realm.internal.objectstore.OsJavaNetworkTransport;
import io.realm.internal.objectstore.OsSyncUser;
import io.realm.log.RealmLog;
import io.realm.mongodb.RealmMongoDBService;

/**
 * FIXME
 */
public class RealmApp {

    // Implementation notes:
    // The public API's currently only allow for one RealmApp, however this is a restriction
    // we might want to lift in the future. So any implementation details so ideally be made
    // with that in mind, i.e. keep static state to minimum.

    // Default session error handler that just output errors to LogCat
    private static final SyncSession.ErrorHandler SESSION_NO_OP_ERROR_HANDLER = new SyncSession.ErrorHandler() {
        @Override
        public void onError(SyncSession session, ObjectServerError error) {
            if (error.getErrorCode() == ErrorCode.CLIENT_RESET) {
                RealmLog.error("Client Reset required for: " + session.getConfiguration().getServerUrl());
                return;
            }

            String errorMsg = String.format(Locale.US, "Session Error[%s]: %s",
                    session.getConfiguration().getServerUrl(),
                    error.toString());
            switch (error.getErrorCode().getCategory()) {
                case FATAL:
                    RealmLog.error(errorMsg);
                    break;
                case RECOVERABLE:
                    RealmLog.info(errorMsg);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported error category: " + error.getErrorCode().getCategory());
            }
        }
    };

    /**
     * Thread pool used when doing network requests against MongoDB Realm.
     * <p>
     * This pool is only exposed for testing purposes and replacing it while the queue is not
     * empty will result in undefined behaviour.
     */
    @SuppressFBWarnings("MS_SHOULD_BE_FINAL")
    public static ThreadPoolExecutor NETWORK_POOL_EXECUTOR = RealmThreadPoolExecutor.newDefaultExecutor();

    private final RealmAppConfiguration config;
    private OsJavaNetworkTransport networkTransport;
    final long nativePtr;
    private final EmailPasswordAuthProvider emailAuthProvider = new EmailPasswordAuthProvider(this);
    private ApiKeyAuthProvider apiKeyAuthProvider = null;
    private CopyOnWriteArrayList<AuthenticationListener> authListeners = new CopyOnWriteArrayList<>();

    public RealmApp(String appId) {
        this(new RealmAppConfiguration.Builder(appId).build());
    }

    /**
     * FIXME
     * @param config
     */
    public RealmApp(RealmAppConfiguration config) {
        this.config = config;
        this.networkTransport = new OkHttpNetworkTransport();
        this.nativePtr = nativeCreate(
                config.getAppId(),
                config.getBaseUrl(),
                config.getAppName(),
                config.getAppVersion(),
                config.getRequestTimeoutMs());
    }

    /**
     * Returns the current user that is logged in and still valid.
     * A user is invalidated when he/she logs out or the user's refresh token expires or is revoked.
     * <p>
     * If two or more users are logged in, it is the last valid user that is returned by this method.
     *
     * @return current {@link RealmUser} that has logged in and is still valid. {@code null} if no
     * user is logged in or the user has expired.
     */
    @Nullable
    public RealmUser currentUser() {
        Long userPtr = nativeCurrentUser(nativePtr);
        return (userPtr != null) ? new RealmUser(userPtr, this) : null;
    }

    /**
     * Returns all known users that are either {@link RealmUser.State#LOGGED_IN} or
     * {@link RealmUser.State#LOGGED_OUT}.
     * <p>
     * Only users that at some point logged into this device will be returned.
     *
     * @return a map of user identifiers and users known locally.
     */
    public Map<String, RealmUser> allUsers() {
        long[] nativeUsers = nativeGetAllUsers(nativePtr);
        HashMap<String, RealmUser> users = new HashMap<>(nativeUsers.length);
        for (int i = 0; i < nativeUsers.length; i++) {
            RealmUser user = new RealmUser(nativeUsers[i], this);
            users.put(user.getId(), user);
        }
        return users;
    }

    /**
     * Switch current user. The current user is the user returned by {@link #currentUser()}.
     *
     * @param user the new current user.
     * @throws IllegalArgumentException if the user is is not {@link RealmUser.State#LOGGED_IN}.
     */
    public RealmUser switchUser(RealmUser user) {
        Util.checkNull(user, "user");
        nativeSwitchUser(nativePtr, user.osUser.getNativePtr());
        return user;
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
     * app.linkUser(RealmCredentials.emailPassword("email", "password"));
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
    public RealmUser linkUser(RealmCredentials credentials) {
        Util.checkNull(credentials, "credentials");
        final RealmUser user = currentUser();
        if (user == null) {
            throw new IllegalStateException("No user is logged in");
        }
        AtomicReference<RealmUser> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        nativeLinkUser(nativePtr, user.osUser.getNativePtr(), credentials.osCredentials.getNativePtr(), new OsJNIResultCallback<RealmUser>(success, error) {
            @Override
            protected RealmUser mapSuccess(Object result) {
                user.osUser = new OsSyncUser((long) result); // OS returns the updated user as a new one.
                return user;
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
     * app.linkUser(RealmCredentials.emailPassword("email", "password"));
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
    public RealmAsyncTask linkUserAsync(RealmCredentials credentials, Callback<RealmUser> callback) {
        Util.checkLooperThread("Asynchronous linking identities is only possible from looper threads.");
        return new Request<RealmUser>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public RealmUser run() throws ObjectServerError {
                return linkUser(credentials);
            }
        }.start();
    }

    /**
     * Removes a users credentials from this device. If the user was currently logged in, they
     * will be logged out as part of the process. This is only a local change and does not
     * affect the user state on the server.
     *
     * @param user user to remove.
     * @return user that was removed.
     * @throws ObjectServerError if called from the UI thread or if the user was logged in, but
     * could not be logged out.
     */
    public RealmUser removeUser(RealmUser user) throws ObjectServerError {
        Util.checkNull(user, "user");
        AtomicReference<RealmUser> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        nativeRemoveUser(nativePtr, user.osUser.getNativePtr(), new OsJNIResultCallback<RealmUser>(success, error) {
            @Override
            protected RealmUser mapSuccess(Object result) {
                return user;
            }
        });
        return handleResult(success, error);
    }

    /**
     * Removes a users credentials from this device. If the user was currently logged in, they
     * will be logged out as part of the process. This is only a local change and does not
     * affect the user state on the server.
     *
     * @param user user to remove.
     * @param callback callback when removing the user has completed or failed. The callback will always
     * happen on the same thread as this method is called on.
     * @throws IllegalStateException if called from a non-looper thread.
     */
    public RealmAsyncTask removeUserAsync(RealmUser user, Callback<RealmUser> callback) {
        Util.checkLooperThread("Asynchronous removal of users is only possible from looper threads.");
        return new Request<RealmUser>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public RealmUser run() throws ObjectServerError {
                return removeUser(user);
            }
        }.start();
    }

    /**
     * Logs in as a user with the given credentials associated with an authentication provider.
     * <p>
     * The user who logs in becomes the current user. Other RealmApp functionality acts on behalf of
     * the current user.
     * <p>
     * If there was already a current user, that user is still logged in and can be found in the
     * list returned by {@link #allUsers()}.
     * <p>
     * It is also possible to switch between which user is considered the current user by using
     * {@link #switchUser(RealmUser)}.
     *
     * @param credentials the credentials representing the type of login.
     * @return a {@link RealmUser} representing the logged in user.
     * @throws ObjectServerError if the user could not be logged in.
     */
    public RealmUser login(RealmCredentials credentials) throws ObjectServerError {
        Util.checkNull(credentials, "credentials");
        AtomicReference<RealmUser> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        nativeLogin(nativePtr, credentials.osCredentials.getNativePtr(), new OsJNIResultCallback<RealmUser>(success, error) {
            @Override
            protected RealmUser mapSuccess(Object result) {
                Long nativePtr = (Long) result;
                return new RealmUser(nativePtr, RealmApp.this);
            }
        });
        return handleResult(success, error);
    }

    /**
     * Logs in as a user with the given credentials associated with an authentication provider.
     * <p>
     * The user who logs in becomes the current user. Other RealmApp functionality acts on behalf of
     * the current user.
     * <p>
     * If there was already a current user, that user is still logged in and can be found in the
     * list returned by {@link #allUsers()}.
     * <p>
     * It is also possible to switch between which user is considered the current user by using
     * {@link #switchUser(RealmUser)}.
     *
     * @param credentials the credentials representing the type of login.
     * @param callback callback when logging in has completed or failed. The callback will always
     * happen on the same thread as this method is called on.
     * @throws IllegalStateException if not called on a looper thread.
     */
     public RealmAsyncTask loginAsync(RealmCredentials credentials, Callback<RealmUser> callback) {
        Util.checkLooperThread("Asynchronous log in is only possible from looper threads.");
        return new Request<RealmUser>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public RealmUser run() throws ObjectServerError {
                return login(credentials);
            }
        }.start();
    }

    /**
     * Log the current user out of the Realm App, destroying their server state, unregistering them from the
     * SDK, and removing any synced Realms associated with them from on-disk storage on next app
     * launch.
     * <p>
     * This method should be called whenever the application is committed to not using a user again.
     * Failing to call this method may result in unused files and metadata needlessly taking up space.
     * <p>
     * Once the Realm App has confirmed the logout any registered {@link AuthenticationListener}
     * will be notified and user credentials will be deleted from this device.
     * <p>
     * Logging out anonymous users will remove them immediately instead of marking them as
     * {@link RealmUser.State#LOGGED_OUT}. All other users will be marked as {@link RealmUser.State#LOGGED_OUT}
     * and will still be returned by {@link #allUsers()}.
     *
     * @throws IllegalStateException if no current user could be found.
     * @throws ObjectServerError if an error occurred while trying to log the user out of the Realm
     * App.
     */
     public void logOut() throws ObjectServerError {
        RealmUser user = currentUser();
        if (user == null) {
            throw new IllegalStateException("No current user was found.");
        }
        logOut(user);
     }

    /**
     * Log the current user out of the Realm App asynchronously, destroying their server state, unregistering them from the
     * SDK, and removing any synced Realms associated with them from on-disk storage on next app
     * launch.
     * <p>
     * This method should be called whenever the application is committed to not using a user again.
     * Failing to call this method may result in unused files and metadata needlessly taking up space.
     * <p>
     * Once the Realm App has confirmed the logout any registered {@link AuthenticationListener}
     * will be notified and user credentials will be deleted from this device.
     * <p>
     * Logging out anonymous users will remove them immediately instead of marking them as
     * {@link RealmUser.State#LOGGED_OUT}. All other users will be marked as {@link RealmUser.State#LOGGED_OUT}
     * and will still be returned by {@link #allUsers()}.
     *
     * @param callback callback when logging out has completed or failed. The callback will always
     * happen on the same thread as this method is called on.
     * @throws IllegalStateException if not called on a looper thread or no current user could be found.
     */
     public RealmAsyncTask logOutAsync(Callback<RealmUser> callback) {
         RealmUser user = currentUser();
         if (user == null) {
             throw new IllegalStateException("No current user was found.");
         }
         return logOutAsync(user, callback);
     }

    /**
     * Returns a wrapper for managing API keys controlled by the current user.
     *
     * @return wrapper for managing API keys controlled by the current user.
     * @throws IllegalStateException if no user is currently logged in.
     */
     public synchronized ApiKeyAuthProvider getApiKeyAuthProvider() {
         RealmUser user = currentUser();
         if (user == null) {
             throw new IllegalStateException("No user is currently logged in.");
         }
         if (apiKeyAuthProvider == null || !user.equals(apiKeyAuthProvider.getUser())) {
             apiKeyAuthProvider = new ApiKeyAuthProvider(user);
         }
         return apiKeyAuthProvider;
     }

    /**
     * Returns a wrapper for interacting with functionality related to users either being created or
     * login using the {@link RealmCredentials.IdentityProvider#EMAIL_PASSWORD} identity provider.
     *
     * @return wrapper for interacting with the {@link RealmCredentials.IdentityProvider#EMAIL_PASSWORD} identity provider.
     */
     public EmailPasswordAuthProvider getEmailPasswordAuthProvider() {
         return emailAuthProvider;
     }

    void logOut(RealmUser user) {
        Util.checkNull(user, "user");
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        nativeLogOut(nativePtr, user.osUser.getNativePtr(), new OsJNIVoidResultCallback(error));
        handleResult(null, error);
    }

    RealmAsyncTask logOutAsync(RealmUser user, Callback<RealmUser> callback) {
        Util.checkLooperThread("Asynchronous log out is only possible from looper threads.");
        return new Request<RealmUser>(SyncManager.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public RealmUser run() throws ObjectServerError {
                logOut(user);
                return user;
            }
        }.start();
    }

    public SyncSession getSyncSession(SyncConfiguration config) {
        return null;
    }

    public void refreshConnections() {

    }

    /**
     * Sets a global authentication listener that will be notified about User events like
     * login and logout.
     *
     * @param listener listener to register.
     * @throws IllegalArgumentException if {@code listener} is {@code null}.
     */
    public void addAuthenticationListener(AuthenticationListener listener) {
        //noinspection ConstantConditions
        if (listener == null) {
            throw new IllegalArgumentException("Non-null 'listener' required.");
        }
        authListeners.add(listener);
    }


    /**
     * Removes the provided global authentication listener.
     *
     * @param listener listener to remove.
     */
    public void removeAuthenticationListener(AuthenticationListener listener) {
        //noinspection ConstantConditions
        if (listener == null) {
            return;
        }
        authListeners.remove(listener);
    }

    // Services entry point
    public RealmFunctions getFunctions() {
        // FIXME
        return null;
    }

    public RealmPushNotifications getFSMPushNotifications() {
        // FIXME
        return null;

    }

    public RealmMongoDBService getMongoDBService() {
        // FIXME
        return null;
    }

    // Private API's for now.

    /**
     * Exposed for testing.
     *
     * Swap the currently configured network transport with the provided one.
     * This should only be done if no network requests are currently running.
     */
    void setNetworkTransport(OsJavaNetworkTransport transport) {
        networkTransport = transport;
    }

    OsJavaNetworkTransport getNetworkTransport() {
        return networkTransport;
    }

    // Handle returning the correct result or throw an exception. Must be separated from
    // OsJNIResultCallback due to how the Object Store callbacks work.
    static <T> T handleResult(@Nullable AtomicReference<T> success, AtomicReference<ObjectServerError> error) {
        if (success != null && success.get() == null && error.get() == null) {
            throw new IllegalStateException("Network result callback did not trigger correctly");
        }
        if (error.get() != null) {
            throw error.get();
        } else {
            if (success != null) {
                return success.get();
            } else {
                return null;
            }
        }
    }

    // Common callback for handling callbacks from the ObjectStore layer.
    // NOTE: This class is called from JNI. If renamed, adjust callbacks in RealmApp.cpp
    @Keep
    static class OsJNIVoidResultCallback extends OsJNIResultCallback {

        public OsJNIVoidResultCallback(AtomicReference error) {
            super(null, error);
        }

        @Override
        protected Void mapSuccess(Object result) {
            return null;
        }
    }

    // Common callback for handling results from the ObjectStore layer.
    // NOTE: This class is called from JNI. If renamed, adjust callbacks in RealmApp.cpp
    @Keep
    static abstract class OsJNIResultCallback<T> extends OsJavaNetworkTransport.NetworkTransportJNIResultCallback {

        private final AtomicReference<T> success;
        private final AtomicReference<ObjectServerError> error;

        public OsJNIResultCallback(@Nullable AtomicReference<T> success, AtomicReference<ObjectServerError> error) {
            this.success = success;
            this.error = error;
        }

        @Override
        public void onSuccess(Object result) {
            T mappedResult = mapSuccess(result);
            if (success != null) {
                success.set(mappedResult);
            }
        }

        // Must map the underlying success Object to the appropriate type in Java
        protected abstract T mapSuccess(Object result);

        @Override
        public void onError(String nativeErrorCategory, int nativeErrorCode, String errorMessage) {
            ErrorCode code = ErrorCode.fromNativeError(nativeErrorCategory, nativeErrorCode);
            if (code == ErrorCode.UNKNOWN) {
                // In case of UNKNOWN errors parse as much error information on as possible.
                String detailedErrorMessage = String.format("{%s::%s} %s", nativeErrorCategory, nativeErrorCode, errorMessage);
                error.set(new ObjectServerError(code, detailedErrorMessage));
            } else {
                error.set(new ObjectServerError(code, errorMessage));
            }
        }
    }

    // Class wrapping requests made against MongoDB Realm. Is also responsible for calling with success/error on the
    // correct thread.
    static abstract class Request<T> {
        @Nullable
        private final RealmApp.Callback<T> callback;
        private final RealmNotifier handler;
        private final ThreadPoolExecutor networkPoolExecutor;

        Request(ThreadPoolExecutor networkPoolExecutor, @Nullable RealmApp.Callback<T> callback) {
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
                        callback.onResult(Result.withError(error));
                    }
                };
                errorHandled = handler.post(action);
            }

            if (!errorHandled) {
                RealmLog.error(error, "An error was thrown, but could not be posted: \n" + error.toString());
            }
        }

        private void postSuccess(final T result) {
            if (callback != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult((result == null) ? Result.success() : Result.withResult(result));
                    }
                });
            }
        }
    }

    /**
     * Result class representing the result of an async request from this app towards MongoDB Realm.
     *
     * @param <T> Type returned if the request was a success.
     * @see Callback
     */
    public static class Result<T> {
        private T result;
        private ObjectServerError error;

        private Result(@Nullable T result, @Nullable ObjectServerError exception) {
            this.result = result;
            this.error = exception;
        }

        /**
         * Creates a successful request result with no return value.
         */
        public static <T> Result<T> success() {
            return new Result(null, null);
        }

        /**
         * Creates a successful request result with a return value.
         *
         * @param result the result value.
         */
        public static <T> Result<T> withResult(T result) {
            return new Result<>(result, null);
        }

        /**
         * Creates a failed request result. The request failed for some reason, either because there
         * was a network error or the Realm Object Server returned an error.
         *
         * @param exception error that occurred.
         */
        public static <T> Result<T> withError(ObjectServerError exception) {
            return new Result<>(null, exception);
        }

        /**
         * Returns whether or not request was successful
         *
         * @return {@code true} if the request was a success, {@code false} if not.
         */
        public boolean isSuccess() {
            return error == null;
        }

        /**
         * Returns the response in case the request was a success.
         *
         * @return the response value in case of a successful request.
         */
        public T get() {
            return result;
        }

        /**
         * Returns the response if the request was a success. If it failed, the default value is
         * returned instead.
         *
         * @return the response value in case of a successful request. If the request failed, the
         * default value is returned instead.
         */
        public T getOrDefault(T defaultValue) {
            return isSuccess() ? result : defaultValue;
        }

        /**
         * If the request was successful the response is returned, otherwise the provided error
         * is thrown.
         *
         * @return the response object in case the request was a success.
         * @throws ObjectServer provided error in case the request failed.
         */
        public T getOrThrow() {
            if (isSuccess()) {
                return result;
            } else {
                throw error;
            }
        }

        /**
         * Returns the error in case of a failed request.
         *
         * @return the {@link ObjectServerError} in case of a failed request.
         */
        public ObjectServerError getError() {
            return error;
        }
    }

    /**
     * Callback for async methods available to the {@link RealmApp}.
     *
     * @param <T> Type returned if the request was a success.
     */
    public interface Callback<T> {
        /**
         * Returns the result of the request when available.
         *
         * @param result the request response.
         */
        void onResult(Result<T> result);
    }

    private native long nativeCreate(String appId, String baseUrl, String appName, String appVersion, long requestTimeoutMs);
    private static native void nativeLogin(long nativeAppPtr, long nativeCredentialsPtr, OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    @Nullable
    private static native Long nativeCurrentUser(long nativePtr);
    private static native long[] nativeGetAllUsers(long nativePtr);
    private static native void nativeLogOut(long appNativePtr, long userNativePtr, OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeSwitchUser(long nativeAppPtr, long nativeUserPtr);
    private static native void nativeRemoveUser(long nativeAppPtr, long nativeUserPtr, OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeLinkUser(long nativeAppPtr, long nativeUserPtr, long nativeCredentialsPtr, OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
}
