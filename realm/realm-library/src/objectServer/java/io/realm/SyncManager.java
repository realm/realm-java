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

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.realm.internal.Keep;
import io.realm.internal.KeepMember;
import io.realm.internal.network.AuthenticationServer;
import io.realm.internal.network.NetworkStateReceiver;
import io.realm.internal.network.OkHttpAuthenticationServer;
import io.realm.log.RealmLog;

/**
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
@SuppressFBWarnings("MS_CANNOT_BE_FINAL")
public class SyncManager {

    /**
     * Debugging related options.
     */
    @SuppressFBWarnings("MS_SHOULD_BE_FINAL")
    public static class Debug {
        /**
         * Set this to true to bypass checking if the device is offline before making HTTP requests.
         */
        public static boolean skipOnlineChecking = false;

    }

    /**
     * APP ID sent to the Realm Object Server. Is automatically initialized to the package name for the app.
     */
    public static String APP_ID = null;

    // Thread pool used when doing network requests against the Realm Authentication Server.
    // FIXME Set proper parameters
    static final ThreadPoolExecutor NETWORK_POOL_EXECUTOR = new ThreadPoolExecutor(
            10, 10, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100));

    private static final SyncSession.ErrorHandler SESSION_NO_OP_ERROR_HANDLER = new SyncSession.ErrorHandler() {
        @Override
        public void onError(SyncSession session, ObjectServerError error) {
            if (error.getErrorCode() == ErrorCode.CLIENT_RESET) {
                RealmLog.error("Client Reset required for: " + session.getConfiguration().getServerUrl());
                return;
            }

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
    // keeps track of SyncSession, using 'realm_path'. Java interface with the ObjectStore using the 'realm_path'
    private static Map<String, SyncSession> sessions = new ConcurrentHashMap<>();
    private static CopyOnWriteArrayList<AuthenticationListener> authListeners = new CopyOnWriteArrayList<AuthenticationListener>();

    // The Sync Client is lightweight, but consider creating/removing it when there is no sessions.
    // Right now it just lives and dies together with the process.
    private static volatile AuthenticationServer authServer = new OkHttpAuthenticationServer();
    private static volatile UserStore userStore;

    private static NetworkStateReceiver.ConnectionListener networkListener = new NetworkStateReceiver.ConnectionListener() {
        @Override
        public void onChange(boolean connectionAvailable) {
            if (connectionAvailable) {
                RealmLog.debug("NetworkListener: Connection available");
                // notify all sessions
                notifyNetworkIsBack();
            } else {
                RealmLog.debug("NetworkListener: Connection lost");
            }
        }
    };

    static volatile SyncSession.ErrorHandler defaultSessionErrorHandler = SESSION_NO_OP_ERROR_HANDLER;

    // Initialize the SyncManager
    static void init(String appId, UserStore userStore) {
        SyncManager.APP_ID = appId;
        SyncManager.userStore = userStore;
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
        // This will not create a new native (Object Store) session, this will only associate a Realm's path
        // with a SyncSession. Object Store's SyncManager is responsible of the life cycle (including creation)
        // of the native session, the provided Java wrap, helps interact with the native session, when reporting error
        // or requesting an access_token for example.

        if (syncConfiguration == null) {
            throw new IllegalArgumentException("A non-empty 'syncConfiguration' is required.");
        }

        SyncSession session = sessions.get(syncConfiguration.getPath());
        if (session == null) {
            session = new SyncSession(syncConfiguration);
            sessions.put(syncConfiguration.getPath(), session);
            if (sessions.size() == 1) {
                RealmLog.debug("first session created add network listener");
                NetworkStateReceiver.addListener(networkListener);
            }
        }

        return session;
    }

    /**
     * Remove the wrapped Java session.
     * @param syncConfiguration configuration object for the synchronized Realm.
     */
    @SuppressWarnings("unused")
    private static synchronized void removeSession(SyncConfiguration syncConfiguration) {
        if (syncConfiguration == null) {
            throw new IllegalArgumentException("A non-empty 'syncConfiguration' is required.");
        }
        SyncSession syncSession = sessions.remove(syncConfiguration.getPath());
        if (syncSession != null) {
            syncSession.close();
        }
        if (sessions.isEmpty()) {
            RealmLog.debug("last session dropped, remove network listener");
            NetworkStateReceiver.removeListener(networkListener);
        }
    }

    static AuthenticationServer getAuthServer() {
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

    /**
     * All errors from native Sync is reported to this method. From the path we can determine which
     * session to contact. If {@code path == null} all sessions are effected.
     */
    @SuppressWarnings("unused")
    private static synchronized void notifyErrorHandler(int errorCode, String errorMessage, String path) {
        for (SyncSession syncSession : sessions.values()) {
            if (path == null || path.equals(syncSession.getConfiguration().getPath())) {
                try {
                    syncSession.notifySessionError(errorCode, errorMessage);
                } catch (Exception exception) {
                    RealmLog.error(exception);
                }
            }
        }
    }

    private static synchronized void notifyNetworkIsBack() {
        try {
            nativeReconnect();
        } catch (Exception exception) {
            RealmLog.error(exception);
        }
    }

    /**
     * All progress listener events from native Sync are reported to this method.
     * It costs 2 HashMap lookups for each listener triggered (one to find the session, one to
     * find the progress listener), but it means we don't have to cache anything on the C++ side which
     * can leak since we don't have control over the session lifecycle.
     */
    @SuppressWarnings("unused")
    @KeepMember
    private static synchronized void notifyProgressListener(String localRealmPath, long listenerId, long transferedBytes, long transferableBytes) {
        SyncSession session = sessions.get(localRealmPath);
        if (session != null) {
            try {
                session.notifyProgressListener(listenerId, transferedBytes, transferableBytes);
            } catch (Exception exception) {
                RealmLog.error(exception);
            }
        }
    }

    /**
     * This is called from the Object Store (through JNI) to request an {@code access_token} for
     * the session specified by sessionPath.
     *
     * This will also schedule a timer to proactively refresh the {@code access_token} regularly, before
     * the {@code access_token} expires.
     *
     * @throws IllegalStateException if the wrapped Java session is not found.
     * @param sessionPath The path to the previously Java wraped session.
     * @return a valid cached {@code access_token} if available or null.
     */
    @SuppressWarnings("unused")
    private synchronized static String bindSessionWithConfig(String sessionPath) {
        final SyncSession syncSession = sessions.get(sessionPath);
        if (syncSession == null) {
            RealmLog.error("Matching Java SyncSession could not be found for: " + sessionPath);
        } else {
            try {
                return syncSession.getAccessToken(authServer);
            } catch (Exception exception) {
                RealmLog.error(exception);
            }
        }
        return null;
    }

    /**
     * Resets the SyncManger and clear all existing users.
     * This will also terminate all sessions.
     *
     * Only call this method when testing.
     */
    static synchronized void reset() {
        nativeReset();
        sessions.clear();
    }

    /**
     * Simulate a Client Reset by triggering the Object Store error handler with Sync Error Code that will be
     * converted to a Client Reset (211 - Diverging Histories).
     *
     * Only call this method when testing.
     *
     * @param session Session to trigger Client Reset for.
     */
    static void simulateClientReset(SyncSession session) {
        nativeSimulateSyncError(session.getConfiguration().getPath(),
                ErrorCode.DIVERGING_HISTORIES.intValue(),
                "Simulate Client Reset",
                true);
    }

    protected static native void nativeInitializeSyncManager(String syncBaseDir);
    private static native void nativeReset();
    private static native void nativeSimulateSyncError(String realmPath, int errorCode, String errorMessage, boolean isFatal);
    private static native void nativeReconnect();
}
