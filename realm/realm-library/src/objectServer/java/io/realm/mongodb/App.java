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

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import org.bson.codecs.configuration.CodecRegistry;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.realm.BuildConfig;
import io.realm.Realm;
import io.realm.mongodb.auth.EmailPasswordAuth;
import io.realm.RealmAsyncTask;
import io.realm.mongodb.sync.Sync;
import io.realm.internal.KeepMember;
import io.realm.internal.RealmNotifier;
import io.realm.internal.network.ResultHandler;
import io.realm.internal.Util;
import io.realm.internal.android.AndroidCapabilities;
import io.realm.internal.android.AndroidRealmNotifier;
import io.realm.internal.async.RealmAsyncTaskImpl;
import io.realm.internal.async.RealmThreadPoolExecutor;
import io.realm.internal.jni.OsJNIResultCallback;
import io.realm.internal.network.OkHttpNetworkTransport;
import io.realm.internal.objectstore.OsJavaNetworkTransport;
import io.realm.log.RealmLog;
import io.realm.mongodb.functions.Functions;

/**
 * FIXME
 */
public class App {

    static final class SyncImpl extends Sync {
        protected SyncImpl(App app) {
            super(app);
        }
    }

    // Implementation notes:
    // The public API's currently only allow for one App, however this is a restriction
    // we might want to lift in the future. So any implementation details so ideally be made
    // with that in mind, i.e. keep static state to minimum.

    // Currently we only allow one instance of App (due to restrictions in ObjectStore that
    // only allows one underlying SyncClient).
    // FIXME: Lift this restriction so it is possible to create multiple app instances.
    public volatile static boolean CREATED = false;

    /**
     * Thread pool used when doing network requests against MongoDB Realm.
     * <p>
     * This pool is only exposed for testing purposes and replacing it while the queue is not
     * empty will result in undefined behaviour.
     */
    @SuppressFBWarnings("MS_SHOULD_BE_FINAL")
    public static ThreadPoolExecutor NETWORK_POOL_EXECUTOR = RealmThreadPoolExecutor.newDefaultExecutor();

    private final AppConfiguration config;
    // FIXME Review public exposure - Test only can be hidden by extension functions
    public OsJavaNetworkTransport networkTransport;
    final Sync syncManager;
    // FIXME Review public exposure - Can be removed when MongoClient is abstracted and implementation is side-by-side with App
    public final long nativePtr; //FIXME Find a way to make this package protected
    private final EmailPasswordAuth emailAuthProvider = new EmailPasswordAuthImpl(this);
    private CopyOnWriteArrayList<AuthenticationListener> authListeners = new CopyOnWriteArrayList<>();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public App(String appId) {
        this(new AppConfiguration.Builder(appId).build());
    }

    /**
     * FIXME
     * @param config
     */
    public App(AppConfiguration config) {
        this.config = config;
        this.networkTransport = new OkHttpNetworkTransport();
        networkTransport.setAuthorizationHeaderName(config.getAuthorizationHeaderName());
        for (Map.Entry<String, String> entry : config.getCustomRequestHeaders().entrySet()) {
            networkTransport.addCustomRequestHeader(entry.getKey(), entry.getValue());
        }
        this.syncManager = new SyncImpl(this);
        this.nativePtr = init(config);

        // FIXME: Right now we only support one App. This class will throw a
        // exception if you try to create it twice. This is a really hacky way to do this
        // Figure out a better API that is always forward compatible
        synchronized (Sync.class) {
            if (CREATED) {
                throw new IllegalStateException("Only one App is currently supported. " +
                        "This restriction will be lifted soon. Instead, store the App" +
                        "instance in a shared global variable.");
            }
            CREATED = true;
        }
    }

    private long init(AppConfiguration config) {
        String userAgentBindingInfo = getBindingInfo();
        String appDefinedUserAgent = getAppInfo(config);
        String syncDir = getSyncBaseDirectory();
        return nativeCreate(
                config.getAppId(),
                config.getBaseUrl().toString(),
                config.getAppName(),
                config.getAppVersion(),
                config.getRequestTimeoutMs(),
                syncDir,
                userAgentBindingInfo,
                appDefinedUserAgent,
                "android",
                android.os.Build.VERSION.RELEASE,
                io.realm.BuildConfig.VERSION_NAME);
    }

    private String getSyncBaseDirectory() {
        Context context = Realm.getApplicationContext();
        if (context == null) {
            throw new IllegalStateException("Call Realm.init() first.");
        }
        String syncDir;
        if (Sync.Debug.separatedDirForSyncManager) {
            try {
                // Files.createTempDirectory is not available on JDK 6.
                File dir = File.createTempFile("remote_sync_", "_" + android.os.Process.myPid(), context.getFilesDir());
                if (!dir.delete()) {
                    throw new IllegalStateException(String.format(Locale.US,
                            "Temp file '%s' cannot be deleted.", dir.getPath()));
                }
                if (!dir.mkdir()) {
                    throw new IllegalStateException(String.format(Locale.US,
                            "Directory '%s' for SyncManager cannot be created. ",
                            dir.getPath()));
                }
                syncDir = dir.getPath();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        } else {
            syncDir = context.getFilesDir().getPath();
        }
        return syncDir;
    }

    private String getAppInfo(AppConfiguration config) {
        // Create app UserAgent string
        String appDefinedUserAgent = "Unknown";
        try {
            String appName = config.getAppName();
            String appVersion = config.getAppVersion();
            if (!Util.isEmptyString(appName) || !Util.isEmptyString(appVersion)) {
                StringBuilder sb = new StringBuilder();
                sb.append(Util.isEmptyString(appName) ? "Undefined" : appName);
                sb.append('/');
                sb.append(Util.isEmptyString(appName) ? "Undefined" : appVersion);
                appDefinedUserAgent = sb.toString();
            }
        } catch (Exception e) {
            // Failures to construct the user agent should never cause the system itself to crash.
            RealmLog.warn("Constructing Binding User-Agent description failed.", e);
        }
        return appDefinedUserAgent;
    }

    private String getBindingInfo() {
        // Setup Realm part of User-Agent string
        String userAgentBindingInfo = "Unknown"; // Fallback in case of anything going wrong
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("RealmJava/");
            sb.append(BuildConfig.VERSION_NAME);
            sb.append(" (");
            sb.append(Util.isEmptyString(Build.DEVICE) ? "unknown-device" : Build.DEVICE);
            sb.append(", ");
            sb.append(Util.isEmptyString(Build.MODEL) ? "unknown-model" : Build.MODEL);
            sb.append(", v");
            sb.append(Build.VERSION.SDK_INT);
            sb.append(")");
            userAgentBindingInfo = sb.toString();
        } catch (Exception e) {
            // Failures to construct the user agent should never cause the system itself to crash.
            RealmLog.warn("Constructing User-Agent description failed.", e);
        }
        return userAgentBindingInfo;
    }

    /**
     * Returns the current user that is logged in and still valid.
     * A user is invalidated when he/she logs out or the user's refresh token expires or is revoked.
     * <p>
     * If two or more users are logged in, it is the last valid user that is returned by this method.
     *
     * @return current {@link User} that has logged in and is still valid. {@code null} if no
     * user is logged in or the user has expired.
     */
    @Nullable
    public User currentUser() {
        Long userPtr = nativeCurrentUser(nativePtr);
        return (userPtr != null) ? new User(userPtr, this) : null;
    }

    /**
     * Returns all known users that are either {@link User.State#LOGGED_IN} or
     * {@link User.State#LOGGED_OUT}.
     * <p>
     * Only users that at some point logged into this device will be returned.
     *
     * @return a map of user identifiers and users known locally.
     */
    public Map<String, User> allUsers() {
        long[] nativeUsers = nativeGetAllUsers(nativePtr);
        HashMap<String, User> users = new HashMap<>(nativeUsers.length);
        for (int i = 0; i < nativeUsers.length; i++) {
            User user = new User(nativeUsers[i], this);
            users.put(user.getId(), user);
        }
        return users;
    }

    /**
     * Switch current user. The current user is the user returned by {@link #currentUser()}.
     *
     * @param user the new current user.
     * @throws IllegalArgumentException if the user is is not {@link User.State#LOGGED_IN}.
     */
    public User switchUser(User user) {
        Util.checkNull(user, "user");
        nativeSwitchUser(nativePtr, user.osUser.getNativePtr());
        return user;
    }

    /**
     * Logs in as a user with the given credentials associated with an authentication provider.
     * <p>
     * The user who logs in becomes the current user. Other App functionality acts on behalf of
     * the current user.
     * <p>
     * If there was already a current user, that user is still logged in and can be found in the
     * list returned by {@link #allUsers()}.
     * <p>
     * It is also possible to switch between which user is considered the current user by using
     * {@link #switchUser(User)}.
     *
     * @param credentials the credentials representing the type of login.
     * @return a {@link User} representing the logged in user.
     * @throws ObjectServerError if the user could not be logged in.
     */
    public User login(Credentials credentials) throws ObjectServerError {
        Util.checkNull(credentials, "credentials");
        AtomicReference<User> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        nativeLogin(nativePtr, credentials.osCredentials.getNativePtr(), new OsJNIResultCallback<User>(success, error) {
            @Override
            protected User mapSuccess(Object result) {
                Long nativePtr = (Long) result;
                return new User(nativePtr, App.this);
            }
        });
        User user = ResultHandler.handleResult(success, error);
        notifyUserLoggedIn(user);
        return user;
    }

    private void notifyUserLoggedIn(User user) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (AuthenticationListener listener : authListeners) {
                    listener.loggedIn(user);
                }
            }
        });
    }

    void notifyUserLoggedOut(User user) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (AuthenticationListener listener : authListeners) {
                    listener.loggedOut(user);
                }
            }
        });
    }

    /**
     * Logs in as a user with the given credentials associated with an authentication provider.
     * <p>
     * The user who logs in becomes the current user. Other App functionality acts on behalf of
     * the current user.
     * <p>
     * If there was already a current user, that user is still logged in and can be found in the
     * list returned by {@link #allUsers()}.
     * <p>
     * It is also possible to switch between which user is considered the current user by using
     * {@link #switchUser(User)}.
     *
     * @param credentials the credentials representing the type of login.
     * @param callback callback when logging in has completed or failed. The callback will always
     * happen on the same thread as this method is called on.
     * @throws IllegalStateException if not called on a looper thread.
     */
     public RealmAsyncTask loginAsync(Credentials credentials, Callback<User> callback) {
        Util.checkLooperThread("Asynchronous log in is only possible from looper threads.");
        return new Request<User>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public User run() throws ObjectServerError {
                return login(credentials);
            }
        }.start();
    }

    /**
     * Returns a wrapper for interacting with functionality related to users either being created or
     * logged in using the {@link Credentials.IdentityProvider#EMAIL_PASSWORD} identity provider.
     *
     * @return wrapper for interacting with the {@link Credentials.IdentityProvider#EMAIL_PASSWORD} identity provider.
     */
    public EmailPasswordAuth getEmailPasswordAuth() {
         return emailAuthProvider;
     }

    /**
     * Sets a global authentication listener that will be notified about User events like
     * login and logout.
     * <p>
     * Callbacks to authentication listeners will happen on the UI thread.
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

    /**
     * FIXME: Figure out naming of this method and class.
     * @return
     */
    public Sync getSync() {
        return syncManager;
    }

    /**
     * Returns a <i>Functions</i> manager for invoking MongoDB Realm Functions.
     * <p>
     * This will use the associated app's default codec registry to encode and decode arguments and
     * results.
     */
    public Functions getFunctions(User user) {
        return new FunctionsImpl(user);
    }

    /**
     * Returns a <i>Functions</i> manager for invoking MongoDB Realm Functions with custom
     * codec registry for encoding and decoding arguments and results.
     */
    public Functions getFunctions(User user, CodecRegistry codecRegistry) {
        return new FunctionsImpl(user, codecRegistry);
    }


    /**
     * Returns the configuration object for this app.
     *
     * @return the configuration for this app.
     */
    public AppConfiguration getConfiguration() {
        return config;
    }

    /**
     * Exposed for testing.
     *
     * Swap the currently configured network transport with the provided one.
     * This should only be done if no network requests are currently running.
     */
    void setNetworkTransport(OsJavaNetworkTransport transport) {
        networkTransport = transport;
    }

    @KeepMember // Called from JNI
    OsJavaNetworkTransport getNetworkTransport() {
        return networkTransport;
    }

    // Class wrapping requests made against MongoDB Realm. Is also responsible for calling with success/error on the
    // correct thread.
    // FIXME Made public to use in Functions. Consider reworking when App, User is moved
    //  to mongodb package and async MongoDB API's are settled
    public static abstract class Request<T> {
        @Nullable
        private final App.Callback<T> callback;
        private final RealmNotifier handler;
        private final ThreadPoolExecutor networkPoolExecutor;

        // FIXME Made public to use in Functions. Consider reworking when App, User is moved
        //  to mongodb package and async MongoDB API's are settled
        public Request(ThreadPoolExecutor networkPoolExecutor, @Nullable App.Callback<T> callback) {
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
     * Callback for async methods available to the {@link App}.
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

    private native long nativeCreate(String appId,
                                     String baseUrl,
                                     String appName,
                                     String appVersion,
                                     long requestTimeoutMs,
                                     String syncDirPath,
                                     String bindingUserInfo,
                                     String appUserInfo,
                                     String platform,
                                     String platformVersion,
                                     String sdkVersion);
    private static native void nativeLogin(long nativeAppPtr, long nativeCredentialsPtr, OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    @Nullable
    private static native Long nativeCurrentUser(long nativePtr);
    private static native long[] nativeGetAllUsers(long nativePtr);
    private static native void nativeSwitchUser(long nativeAppPtr, long nativeUserPtr);
}
