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

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.realm.internal.RealmNotifier;
import io.realm.internal.android.AndroidCapabilities;
import io.realm.internal.android.AndroidRealmNotifier;
import io.realm.internal.async.RealmAsyncTaskImpl;
import io.realm.internal.network.OkHttpNetworkTransport;
import io.realm.internal.objectstore.OsJavaNetworkTransport;
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

    // Default session error handler handler that just output errors to LogCat
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
     * Thread pool used when doing network requests against the MongoDB Realm Server.
     * <p>
     * This pool is only exposed for testing purposes and replacing it while the queue is not
     * empty will result in undefined behaviour.
     */
    @SuppressFBWarnings("MS_SHOULD_BE_FINAL")
    public static ThreadPoolExecutor NETWORK_POOL_EXECUTOR = new ThreadPoolExecutor(
            10, 10, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(100));

    private OsJavaNetworkTransport networkTransport;
    private final long nativePtr;
    private CopyOnWriteArrayList<AuthenticationListener> authListeners = new CopyOnWriteArrayList<AuthenticationListener>();

    public RealmApp(String appId) {
        this(new RealmAppConfiguration.Builder(appId).build());
    }

    /**
     * FIXME
     * @param config
     */
    public RealmApp(RealmAppConfiguration config) {
        networkTransport = new OkHttpNetworkTransport();
        nativePtr = nativeCreate(
                config.getAppId(),
                config.getBaseUrl(),
                config.getAppName(),
                config.getAppVersion(),
                config.getRequestTimeoutMs());
    }

    /**
     * Returns currently logged in user
     * @return
     */
    public static SyncUser currentUser() {
        return null;
    }

    /**
     * Returns all currently logged in users
     * @return
     */
    public static Map<String, RealmUser> allUsers() {
        return null;
    }

    /**
     * TODO: Manually set the user returned by {@link #currentUser()}
     *
     * @param user
     */
    public static void setCurrentUser(SyncUser user) {
        // FIXME
    }

    /**
     * FIXME
     *
     * @param credentials
     * @return
     * @throws ObjectServerError
     */
    public RealmUser login(RealmCredentials credentials) throws ObjectServerError {
        long nativeUserPtr = nativeLogin(nativePtr, credentials.osCredentials.getNativePtr());
        return new RealmUser(nativeUserPtr);

    }

    /**
     * FIXME
     * @param credentials
     * @param callback
     * @return
     */
    public RealmAsyncTask loginAsync(RealmCredentials credentials, Callback<RealmUser> callback) {
        checkLooperThread("Asynchronous login is only possible from looper threads.");
        return new Request<RealmUser>(SyncManager.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public RealmUser run() throws ObjectServerError {
                return login(credentials);
            }
        }.start();
    }

    public static void logout(RealmUser user) {

    }
    public RealmAsyncTask logoutAsync(RealmUser user, Callback<Void> callback) {
        return null;
    }

    public RealmUser registerWithEmail(String email, String password) {
        return null;
    }
    public RealmAsyncTask registerWithEmailAsync(String email, String password, Callback<RealmUser> callback) {
        return null;
    }
    public RealmUser confirmUser(String token, String tokenId) {
        return null;
    }
    public RealmAsyncTask confirmUserAsync(String token, String tokenId, Callback<Void> callback) {
        return null;
    }
    public void resendConfirmationEmail(String email) {
    }
    public RealmAsyncTask resendConfirmationEmailAsync(String email, Callback<Void> callback) {
        return null;
    }
    public RealmUser resetPassword(String token, String tokenId, String password) {
        return null;
    }
    public RealmAsyncTask resetPasswordAsync(String token, String tokenId, String password, Callback<Void> callback) {
        return null;
    }
    public RealmUser sendResetPasswordEmail(String email) {
        return null;
    }
    public RealmAsyncTask sendResetPasswordEmailAsync(String email, Callback<Void> callback) {
        return null;
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
        return null;
    }

    public RealmFCMPushNotifications getFSMPushNotifications() {
        return null;
    }

    public RealmMongoDBService getMongoDBService() {
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

    private static void checkLooperThread(String errorMessage) {
        AndroidCapabilities capabilities = new AndroidCapabilities();
        capabilities.checkCanDeliverNotification(errorMessage);
    }

    // Class wrapping requests made against the auth server. Is also responsible for calling with success/error on the
    // correct thread.
    private static abstract class Request<T> {
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
                        postSuccess(RealmApp.Request.this.run());
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

    /**
     * Callback for async methods available to the {@link RealmApp}.
     *
     * @param <T> Type returned if the request was a success.
     */
    public interface Callback<T> {
        /**
         * The request was a success.
         * @param t The object representing the successful request. See each method for details.
         */
        void onSuccess(T t);

        /**
         * The request failed for some reason, either because there was a network error or the Realm
         * Object Server returned an error.
         *
         * @param error the error that was detected.
         */
        void onError(ObjectServerError error);
    }

    private static native long nativeCreate(String appId, String baseUrl, String appName, String appVersion, long requestTimeoutMs);
    private static native long nativeLogin(long nativeAppPtr, long nativeCredentialsPtr) throws ObjectServerError;
}
