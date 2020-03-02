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

import android.content.Context;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

    private static RealmAppConfiguration defaultConfiguration;
    private static volatile RealmApp defaultApp;
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


    private CopyOnWriteArrayList<AuthenticationListener> authListeners = new CopyOnWriteArrayList<AuthenticationListener>();

    /**
     * Initializes the default RealmApp
     * @param context
     * @param appId
     */
    public static void init(Context context, String appId) {
        init(context, new RealmAppConfiguration.Builder(context, appId).build());
    }

    public static void init(Context context, RealmAppConfiguration config) {
        Realm.init(context);
        defaultConfiguration = config;
        defaultApp = new RealmApp(defaultConfiguration);
    }

    private RealmApp(RealmAppConfiguration config) {
        long nativePtr = nativeCreate(config.getAppId(),
                config.getBaseUrl(),
                config.getAppName(),
                config.getAppVersion(),
                config.getRequestTimeoutMs());
    }

//    struct Config {
//        std::string app_id;
//        GenericNetworkTransport::NetworkTransportFactory transport_generator;
//        realm::util::Optional<std::string> base_url;
//        realm::util::Optional<std::string> local_app_name;
//        realm::util::Optional<std::string> local_app_version;
//        realm::util::Optional<uint64_t> default_request_timeout_ms;
//    };

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
    public static Map<String, RealmAppUser> allUsers() {
        return null;
    }

    /**
     * TODO: Manually set the user returned by {@link #currentUser()}
     *
     * @param user
     */
    public static void setCurrentUser(SyncUser user) {

    }

    public static RealmAppUser login(RealmAppCredentials credentials) {
        return null;
    }
    public static RealmAsyncTask loginAsync(RealmAppCredentials credentials, Callback<RealmAppUser> callback) {
        return null;
    }

    public static void logout(RealmAppUser user) {

    }
    public static RealmAsyncTask logoutAsync(RealmAppUser user, Callback<Void> callback) {
        return null;
    }

    public static RealmAppUser registerWithEmail(String email, String password) {
        return null;
    }
    public static RealmAsyncTask registerWithEmailAsync(String email, String password, Callback<RealmAppUser> callback) {
        return null;
    }
    public static RealmAppUser confirmUser(String token, String tokenId) {
        return null;
    }
    public static RealmAsyncTask confirmUserAsync(String token, String tokenId, Callback<Void> callback) {
        return null;
    }
    public static void resendConfirmationEmail(String email) {
    }
    public static RealmAsyncTask resendConfirmationEmailAsync(String email, Callback<Void> callback) {
        return null;
    }
    public static RealmAppUser resetPassword(String token, String tokenId, String password) {
        return null;
    }
    public static RealmAsyncTask resetPasswordAsync(String token, String tokenId, String password, Callback<Void> callback) {
        return null;
    }
    public static RealmAppUser sendResetPasswordEmail(String email) {
        return null;
    }
    public static RealmAsyncTask sendResetPasswordEmailAsync(String email, Callback<Void> callback) {
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
    public static void addAuthenticationListener(AuthenticationListener listener) {
        //noinspection ConstantConditions
        if (listener == null) {
            throw new IllegalArgumentException("Non-null 'listener' required.");
        }
        defaultApp.authListeners.add(listener);
    }


    /**
     * Removes the provided global authentication listener.
     *
     * @param listener listener to remove.
     */
    public static void removeAuthenticationListener(AuthenticationListener listener) {
        //noinspection ConstantConditions
        if (listener == null) {
            return;
        }
        defaultApp.authListeners.remove(listener);
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

    private static native long nativeCreate(String appId, String baseUrl, String appName, String appVersion, long requestTimeoutMs);


    // Private API's for now.


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
}
