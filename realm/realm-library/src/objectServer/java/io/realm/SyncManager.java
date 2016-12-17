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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.realm.annotations.Beta;
import io.realm.internal.Keep;
import io.realm.internal.network.AuthenticationServer;
import io.realm.internal.network.OkHttpAuthenticationServer;
import io.realm.internal.objectserver.SessionStore;
import io.realm.internal.objectserver.ObjectServerSession;
import io.realm.log.RealmLog;

/**
 * @Beta
 * The SyncManager is the central controller for interacting with the Realm Object Server.
 * It handles the creation of {@link SyncSession}s and it is possible to configure session defaults and the underlying
 * network client using this class.
 * <p>
 * Through the SyncManager, it is possible to add authentication listeners. An authentication listener will
 * response to events like user logging in or out.
 * <p>
 * Default error handling for any {@link SyncConfiguration} can be added using the SyncManager.
 *
 */
@Keep
@Beta
public class SyncManager {

    /**
     * APP ID sent to the Realm Object Server. Is automatically initialized to the package name for the app.
     */
    public static String APP_ID = null;

    // Thread pool used when doing network requests against the Realm Authentication Server.
    // FIXME Set proper parameters
    public static final ThreadPoolExecutor NETWORK_POOL_EXECUTOR = new ThreadPoolExecutor(
            10, 10, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100));

    private static final SyncSession.ErrorHandler SESSION_NO_OP_ERROR_HANDLER = new SyncSession.ErrorHandler() {
        @Override
        public void onError(SyncSession session, ObjectServerError error) {
            String errorMsg = String.format("Session Error[%s]: %s",
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

    private static CopyOnWriteArrayList<AuthenticationListener> authListeners = new CopyOnWriteArrayList<AuthenticationListener>();

    // The Sync Client is lightweight, but consider creating/removing it when there is no sessions.
    // Right now it just lives and dies together with the process.
    private static volatile AuthenticationServer authServer = new OkHttpAuthenticationServer();
    private static volatile UserStore userStore;

    static volatile SyncSession.ErrorHandler defaultSessionErrorHandler = SESSION_NO_OP_ERROR_HANDLER;
    @SuppressWarnings("FieldCanBeLocal")
    private static Thread clientThread;

    // Initialize the SyncManager
    static void init(String appId, UserStore userStore) {

        SyncManager.APP_ID = appId;
        SyncManager.userStore = userStore;

        // Initialize underlying Sync Network Client
        nativeInitializeSyncClient();

        // Create the client thread in java to avoid problems when exceptions are being thrown. We need to attach
        // any thread to the JVM anyway in order to send back log events.
        SyncManager.clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                nativeRunClient();
            }
        }, "RealmSyncClient");
        SyncManager.clientThread.start();
    }

    /**
     * Set the {@link UserStore} used by the Realm Object Server to save user information.
     * If no Userstore is specified {@link SyncUser#currentUser()} will always return {@code null}.
     *
     * @param userStore {@link UserStore} to use.
     * @throws IllegalArgumentException if {@code userStore} is {@code null}.
     */
    public static void setUserStore(UserStore userStore) {
        if (userStore == null) {
            throw new IllegalArgumentException("Non-null 'userStore' required.");
        }
        SyncManager.userStore = userStore;
    }

    /**
     * Sets a global authentication listener that will be notified about User events like
     * login and logout.
     *
     * @param listener listener to register.
     * @throws IllegalArgumentException if {@code listener} is {@code null}.
     */
    public static void addAuthenticationListener(AuthenticationListener listener) {
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
    public static void removeAuthenticationListener(AuthenticationListener listener) {
        if (listener == null) {
            return;
        }
        authListeners.remove(listener);
    }

    /**
     * Sets the default error handler used by all {@link SyncConfiguration} objects when they are created.
     *
     * @param errorHandler the default error handler used when interacting with a Realm managed by a Realm Object Server.
     */
    public static void setDefaultSessionErrorHandler(SyncSession.ErrorHandler errorHandler) {
        if (errorHandler == null) {
            defaultSessionErrorHandler = SESSION_NO_OP_ERROR_HANDLER;
        } else {
            defaultSessionErrorHandler = errorHandler;
        }
    }

    /**
     * Gets any cached {@link SyncSession} for the given {@link SyncConfiguration} or create a new one if
     * no one exists.
     *
     * @param syncConfiguration configuration object for the synchronized Realm.
     * @return the {@link SyncSession} for the specified Realm.
     * @throws IllegalArgumentException if syncConfiguration is {@code null}.
     */
    public static synchronized SyncSession getSession(SyncConfiguration syncConfiguration) {
        if (syncConfiguration == null) {
            throw new IllegalArgumentException("A non-empty 'syncConfiguration' is required.");
        }

        if (SessionStore.hasSession(syncConfiguration)) {
            return SessionStore.getPublicSession(syncConfiguration);
        } else {
            ObjectServerSession internalSession = new ObjectServerSession(
                    syncConfiguration,
                    authServer,
                    syncConfiguration.getUser().getSyncUser(),
                    syncConfiguration.getSyncPolicy(),
                    syncConfiguration.getErrorHandler()
            );
            SyncSession publicSession = new SyncSession(internalSession);
            SessionStore.addSession(publicSession, internalSession);
            syncConfiguration.getUser().getSyncUser().addSession(publicSession);
            syncConfiguration.getSyncPolicy().onSessionCreated(internalSession);
            return publicSession;
        }
    }

    public static AuthenticationServer getAuthServer() {
        return authServer;
    }

    /**
     * Sets the auth server implementation used when validating credentials.
     */
    static void setAuthServerImpl(AuthenticationServer authServerImpl) {
        authServer = authServerImpl;
    }

    // Return the currently configured User store.
    static UserStore getUserStore() {
        return userStore;
    }

    // This is called from SyncManager.cpp from the worker thread the Sync Client is running on
    // Right now Core doesn't send these errors to the proper session, so instead we need to notify all sessions
    // from here. This can be removed once better error propagation is implemented in Sync Core.
    @SuppressWarnings("unused")
    private static void notifyErrorHandler(int errorCode, String errorMessage) {
        ObjectServerError error = new ObjectServerError(ErrorCode.fromInt(errorCode), errorMessage);
        for (ObjectServerSession session : SessionStore.getAllSessions()) {
            session.onError(error);
        }
    }

    // Notify listeners that a user logged in
    static void notifyUserLoggedIn(SyncUser user) {
        for (AuthenticationListener authListener : authListeners) {
            authListener.loggedIn(user);
        }
    }

    // Notify listeners that a user logged out successfully
    static void notifyUserLoggedOut(SyncUser user) {
        for (AuthenticationListener authListener : authListeners) {
            authListener.loggedOut(user);
        }
    }

    private static native void nativeInitializeSyncClient();
    private static native void nativeRunClient();

}
