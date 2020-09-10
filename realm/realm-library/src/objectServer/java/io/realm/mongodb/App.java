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
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.realm.BuildConfig;
import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.annotations.Beta;
import io.realm.internal.KeepMember;
import io.realm.internal.Util;
import io.realm.internal.async.RealmThreadPoolExecutor;
import io.realm.internal.mongodb.Request;
import io.realm.internal.objectstore.OsApp;
import io.realm.internal.objectstore.OsJavaNetworkTransport;
import io.realm.internal.objectstore.OsSyncUser;
import io.realm.log.RealmLog;
import io.realm.mongodb.auth.EmailPasswordAuth;
import io.realm.mongodb.functions.Functions;
import io.realm.mongodb.sync.Sync;

/**
 * An <i>App</i> is the main client-side entry point for interacting with a <i>MongoDB Realm App</i>.
 * <p>
 * The <i>App</i> can be used to:
 * <ul>
 *   <li>Register uses and perform various user-related operations through authentication providers
 *   ({@link io.realm.mongodb.auth.ApiKeyAuth}, {@link EmailPasswordAuthImpl})</li>
 *   <li>Synchronize data between the local device and a remote Realm App with Synchronized Realms</li>
 *   <li>Invoke Realm App functions with {@link Functions}</li>
 *   <li>Access remote data from MongoDB databases with a {@link io.realm.mongodb.mongo.MongoClient}</li>
 * </ul>
 * <p>
 * To create an app that is linked with a remote <i>Realm App</i> initialize Realm and configure the
 * <i>App</i> as shown below:
 * <p>
 * <pre>
 *    class MyApplication extends Application {
 *
 *         App APP;
 *
 *         \@Override
 *         public void onCreate() {
 *             super.onCreate();
 *
 *             Realm.init(this);
 *
 *             AppConfiguration appConfiguration = new AppConfiguration.Builder(BuildConfig.MONGODB_REALM_APP_ID)
 *                     .appName(BuildConfig.VERSION_NAME)
 *                     .appVersion(Integer.toString(BuildConfig.VERSION_CODE))
 *                     .build();
 *
 *             APP = new App(appConfiguration);
 *         }
 *
 *     }
 * </pre>
 * <p>
 * After configuring the <i>App</i> you can start managing users, configure Synchronized Realms,
 * call remote Realm Functions and access remote data through Mongo Collections. The examples below
 * show the synchronized APIs which cannot be used from the main thread. For the equivalent
 * asynchronous counterparts. The example project in please see
 * https://github.com/realm/realm-java/tree/v10/examples/mongoDbRealmExample.
 * <p>
 * To register a new user and/or login with an existing user do as shown below:
 * <pre>
 *     // Register new user
 *     User user = APP.getEmailPasswordAuth().registerUser(username, password);
 *
 *     // Login with existing user
 *     APP.login(Credentials.emailPassword(username, password))
 * </pre>
 * <p>
 * With an authorized user you can synchronize data between the local device and the remote Realm
 * App by opening a Realm with a {@link io.realm.mongodb.sync.SyncConfiguration} as indicated below:
 * <pre>
 *     SyncConfiguration syncConfiguration = new SyncConfiguration.Builder(user, "&lt;partition value&gt;")
 *              .build();
 *
 *     Realm instance = Realm.getInstance(syncConfiguration);
 *     SyncSession session = APP.getSync().getSession(syncConfiguration);
 *
 *     instance.executeTransaction(realm -&gt; {
 *         realm.insert(...);
 *     });
 *     session.uploadAllLocalChanges();
 *     instance.close();
 * </pre>
 * <p>
 * You can call remove Realm functions as shown below:
 * <pre>
 *     Functions functions = user.getFunctions();
 *     Integer sum = functions.callFunction("sum", Arrays.asList(1, 2, 3, 4), Integer.class);
 * </pre>
 * <p>
 * And access collections from the remote Realm App as shown here:
 * <pre>
 *     MongoClient client = user.getMongoClient(SERVICE_NAME)
 *     MongoDatabase database = client.getDatabase(DATABASE_NAME)
 *     MongoCollection&lt;DocumentT&gt; collection = database.getCollection(COLLECTION_NAME);
 *     Long count = collection.count().get()
 * </pre>
 * <p>
 *
 * @see AppConfiguration.Builder
 * @see EmailPasswordAuth
 * @see io.realm.mongodb.sync.SyncConfiguration
 * @see User#getFunctions()
 * @see User#getMongoClient(String)
 */
@Beta
public class App {

    @KeepMember
    final OsApp osApp;

    static final class SyncImpl extends Sync {
        protected SyncImpl(App app) {
            super(app, app.osApp.getNativePtr());
        }
    }

    /**
     * Thread pool used when doing network requests against MongoDB Realm.
     * <p>
     * This pool is only exposed for testing purposes and replacing it while the queue is not
     * empty will result in undefined behaviour.
     */
    @SuppressFBWarnings("MS_SHOULD_BE_FINAL")
    public static ThreadPoolExecutor NETWORK_POOL_EXECUTOR = RealmThreadPoolExecutor.newDefaultExecutor();

    private final AppConfiguration config;
    final Sync syncManager;
    private final EmailPasswordAuth emailAuthProvider = new EmailPasswordAuthImpl(this);
    private CopyOnWriteArrayList<AuthenticationListener> authListeners = new CopyOnWriteArrayList<>();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public App(String appId) {
        this(new AppConfiguration.Builder(appId).build());
    }

    /**
     * Constructor for creating an <i>App</i> according to the given <i>AppConfiguration</i>.
     *
     * @param config The configuration to use for this <i>App</i> instance.
     * @see AppConfiguration.Builder
     */
    public App(AppConfiguration config) {
        this.config = config;
        this.syncManager = new SyncImpl(this);
        this.osApp = init(config);
    }

    private OsApp init(AppConfiguration config) {
        String userAgentBindingInfo = getBindingInfo();
        String appDefinedUserAgent = getAppInfo(config);
        String syncDir = getSyncBaseDirectory();

        return new OsApp(config, userAgentBindingInfo, appDefinedUserAgent, syncDir);
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
     * <p>
     * A user is invalidated when he/she logs out or the user's refresh token expires or is revoked.
     * <p>
     * If two or more users are logged in, it is the last valid user that is returned by this method.
     *
     * @return current {@link User} that has logged in and is still valid. {@code null} if no
     * user is logged in or the user has expired.
     */
    @Nullable
    public User currentUser() {
        OsSyncUser osSyncUser = osApp.currentUser();
        return (osSyncUser != null) ? new User(osSyncUser, this) : null;
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
        OsSyncUser[] allUsers = osApp.allUsers();

        HashMap<String, User> users = new HashMap<>(allUsers.length);
        for (int i = 0; i < allUsers.length; i++) {
            User user = new User(allUsers[i], this);
            users.put(user.getId(), user);
        }
        return users;
    }

    /**
     * Switch current user.
     * <p>
     * The current user is the user returned by {@link #currentUser()}.
     *
     * @param user the new current user.
     * @throws IllegalArgumentException if the user is is not {@link User.State#LOGGED_IN}.
     */
    public User switchUser(User user) {
        Util.checkNull(user, "user");
        osApp.switchUser(user.osUser);

        return user;
    }

    /**
     * Removes a users credentials from this device. If the user was currently logged in, they
     * will be logged out as part of the process. This is only a local change and does not
     * affect the user state on the server.
     *
     * @param user to remove
     * @return user that was removed.
     * @throws AppException if called from the UI thread or if the user was logged in, but
     *                      could not be logged out.
     */
    public User removeUser(User user) throws AppException {
        return user.remove();
    }

    /**
     * Removes a user's credentials from this device. If the user was currently logged in, they
     * will be logged out as part of the process. This is only a local change and does not
     * affect the user state on the server.
     *
     * @param user to remove
     * @param callback callback when removing the user has completed or failed. The callback will always
     *                 happen on the same thread as this method is called on.
     * @throws IllegalStateException if called from a non-looper thread.
     */
    RealmAsyncTask removeAsync(User user, App.Callback<User> callback) {
        return user.removeAsync(callback);
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
     * @throws AppException if the user could not be logged in.
     */
    public User login(Credentials credentials) throws AppException {
        Util.checkNull(credentials, "credentials");

        OsSyncUser osSyncUser = osApp.login(credentials.osCredentials);
        User user = new User(osSyncUser, this);

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
     * @param callback    callback when logging in has completed or failed. The callback will always
     *                    happen on the same thread as this method is called on.
     * @throws IllegalStateException if not called on a looper thread.
     */
    public RealmAsyncTask loginAsync(Credentials credentials, Callback<User> callback) {
        Util.checkLooperThread("Asynchronous log in is only possible from looper threads.");
        return new Request<User>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public User run() throws AppException {
                return login(credentials);
            }
        }.start();
    }

    /**
     * Returns a wrapper for interacting with functionality related to users either being created or
     * logged in using the {@link Credentials.Provider#EMAIL_PASSWORD} identity provider.
     *
     * @return wrapper for interacting with the {@link Credentials.Provider#EMAIL_PASSWORD} identity provider.
     */
    public EmailPasswordAuth getEmailPassword() {
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
     * Returns the <i>Sync</i> instance managing the ongoing <i>Realm Sync</i> sessions
     * synchronizing data between the local and the remote <i>Realm App</i> associated with this app.
     *
     * @return the <i>Sync</i> instance associated with this <i>App</i>.
     */
    public Sync getSync() {
        return syncManager;
    }

    /**
     * Returns a <i>Functions</i> manager for invoking the Realm App's Realm Functions.
     * <p>
     * This will use the app's default codec registry to encode and decode arguments and results.
     *
     * @see Functions
     * @see AppConfiguration#getDefaultCodecRegistry()
     */
    public Functions getFunctions(User user) {
        return new FunctionsImpl(user);
    }

    /**
     * Returns a <i>Functions</i> manager for invoking the Realm App's Realm Functions with a custom
     * codec registry for encoding and decoding arguments and results.
     *
     * @see Functions
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
     * <p>
     * Swap the currently configured network transport with the provided one.
     * This should only be done if no network requests are currently running.
     */
    protected void setNetworkTransport(OsJavaNetworkTransport transport) {
        osApp.setNetworkTransport(transport);
    }

    /**
     * Result class representing the result of an async request from this app towards MongoDB Realm.
     *
     * @param <T> Type returned if the request was a success.
     * @see Callback
     */
    public static class Result<T> {
        private T result;
        private AppException error;

        private Result(@Nullable T result, @Nullable AppException exception) {
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
        public static <T> Result<T> withError(AppException exception) {
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
         * @throws AppException provided error in case the request failed.
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
         * @return the {@link AppException} in case of a failed request.
         */
        public AppException getError() {
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
}
