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

import android.content.Context;
import android.os.Build;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.realm.internal.Keep;
import io.realm.internal.OsRealmConfig;
import io.realm.internal.Util;
import io.realm.internal.network.NetworkStateReceiver;
import io.realm.log.RealmLog;
import okhttp3.internal.tls.OkHostnameVerifier;

/**
 * Class wrapping Sync responsibilities for a {@link io.realm.RealmApp}.
 *
 * FIXME: Better description that makes sense for end users.
 */
@Keep
@SuppressFBWarnings("MS_CANNOT_BE_FINAL")
public class RealmSync {

    private final RealmApp app;
    // keeps track of SyncSession, using 'realm_path'. Java interface with the ObjectStore using the 'realm_path'
    private Map<String, SyncSession> sessions = new ConcurrentHashMap<>();

    RealmSync(RealmApp app) {
        this.app = app;
    }

    /**
     * Debugging related options.
     */
    @SuppressFBWarnings("MS_SHOULD_BE_FINAL")
    public static class Debug {
        /**
         * Set this to true to bypass checking if the device is offline before making HTTP requests.
         */
        public static boolean skipOnlineChecking = false;

        /**
         * Set this to true to init a SyncManager with a directory named by the process ID. This is useful for
         * integration tests which are emulating multiple sync client by using multiple processes.
         */
        public static boolean separatedDirForSyncManager = false;
    }

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

    /**
     * Gets a cached {@link SyncSession} for the given {@link SyncConfiguration} or throw if no one exists yet.
     *
     * A session should exist after you open a Realm with a {@link SyncConfiguration}.
     *
     * @param syncConfiguration configuration object for the synchronized Realm.
     * @return the {@link SyncSession} for the specified Realm.
     * @throws IllegalArgumentException if syncConfiguration is {@code null}.
     * @throws IllegalStateException if the session could not be found using the provided {@code SyncConfiguration}.
     */
    public synchronized SyncSession getSession(SyncConfiguration syncConfiguration) throws IllegalStateException {
        //noinspection ConstantConditions
        if (syncConfiguration == null) {
            throw new IllegalArgumentException("A non-empty 'syncConfiguration' is required.");
        }

        SyncSession session = sessions.get(syncConfiguration.getPath());
        if (session == null) {
            throw new IllegalStateException("No SyncSession found using the path : " + syncConfiguration.getPath()
                    + "\nplease ensure to call this method after you've open the Realm");
        }

        return session;
    }

    /**
     * Gets any cached {@link SyncSession} for the given {@link SyncConfiguration} or create a new one if
     * no one exists.
     *
     * Note: This is mainly for internal usage, consider using {@link #getSession(SyncConfiguration)} instead.
     *
     * @param syncConfiguration configuration object for the synchronized Realm.
     * @return the {@link SyncSession} for the specified Realm.
     * @throws IllegalArgumentException if syncConfiguration is {@code null}.
     */
    public synchronized SyncSession getOrCreateSession(SyncConfiguration syncConfiguration) {
        // This will not create a new native (Object Store) session, this will only associate a Realm's path
        // with a SyncSession. Object Store's SyncManager is responsible of the life cycle (including creation)
        // of the native session. The provided Java wrap, helps interact with the native session, when reporting error
        // or requesting an access_token for example.

        //noinspection ConstantConditions
        if (syncConfiguration == null) {
            throw new IllegalArgumentException("A non-empty 'syncConfiguration' is required.");
        }

        SyncSession session = sessions.get(syncConfiguration.getPath());
        if (session == null) {
            RealmLog.debug("Creating session for: %s", syncConfiguration.getPath());
            session = new SyncSession(syncConfiguration);
            sessions.put(syncConfiguration.getPath(), session);
            if (sessions.size() == 1) {
                RealmLog.debug("First session created. Adding network listener.");
                NetworkStateReceiver.addListener(networkListener);
            }
            // The underlying session will be created as part of opening the Realm, but this approach
            // does not work when using `Realm.getInstanceAsync()` in combination with AsyncOpen.
            //
            // So instead we manually create the underlying native session.
            OsRealmConfig config = new OsRealmConfig.Builder(syncConfiguration).build();
            nativeCreateSession(config.getNativePtr());
        }

        return session;
    }

    List<SyncSession> getAllSyncSessions(RealmUser user) {
        //noinspection ConstantConditions
        if (user == null) {
            throw new IllegalArgumentException("A non-empty 'syncUser' is required.");
        }
        ArrayList<SyncSession> allSessions = new ArrayList<SyncSession>();
        for (SyncSession syncSession : sessions.values()) {
            if (syncSession.getUser().equals(user)) {
                allSessions.add(syncSession);
            }
        }
        return allSessions;
    }



    /**
     * Remove the wrapped Java session.
     * @param syncConfiguration configuration object for the synchronized Realm.
     */
    @SuppressWarnings("unused")
    private synchronized void removeSession(SyncConfiguration syncConfiguration) {
        //noinspection ConstantConditions
        if (syncConfiguration == null) {
            throw new IllegalArgumentException("A non-empty 'syncConfiguration' is required.");
        }
        RealmLog.debug("Removing session for: %s", syncConfiguration.getPath());
        SyncSession syncSession = sessions.remove(syncConfiguration.getPath());
        if (syncSession != null) {
            syncSession.close();
        }
        if (sessions.isEmpty()) {
            RealmLog.debug("Last session dropped. Remove network listener.");
            NetworkStateReceiver.removeListener(networkListener);
        }
    }

    /**
     * All errors from native Sync is reported to this method. From the path we can determine which
     * session to contact. If {@code path == null} all sessions are effected.
     */
    @SuppressWarnings("unused")
    private synchronized void notifyErrorHandler(String nativeErrorCategory, int nativeErrorCode, String errorMessage, @Nullable String path) {
        if (Util.isEmptyString(path)) {
            // notify all sessions
            for (SyncSession syncSession : sessions.values()) {
                try {
                    syncSession.notifySessionError(nativeErrorCategory, nativeErrorCode, errorMessage);
                } catch (Exception exception) {
                    RealmLog.error(exception);
                }
            }
        } else {
            SyncSession syncSession = sessions.get(path);
            if (syncSession != null) {
                try {
                    syncSession.notifySessionError(nativeErrorCategory, nativeErrorCode, errorMessage);
                } catch (Exception exception) {
                    RealmLog.error(exception);
                }
            } else {
                RealmLog.warn("Cannot find the SyncSession corresponding to the path: " + path);
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
    private synchronized void notifyProgressListener(String localRealmPath, long listenerId, long transferedBytes, long transferableBytes) {
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
     * Called from native code. This method is not allowed to throw as it would be swallowed
     * by the native Sync Client thread. Instead log all exceptions to logcat.
     */
    @SuppressWarnings("unused")
    private synchronized void notifyConnectionListeners(String localRealmPath, long oldState, long newState) {
        SyncSession session = sessions.get(localRealmPath);
        if (session != null) {
            try {
                session.notifyConnectionListeners(ConnectionState.fromNativeValue(oldState), ConnectionState.fromNativeValue(newState));
            } catch (Exception exception) {
                RealmLog.error(exception);
            }
        }
    }

    /**
     * Realm will automatically detect when a device gets connectivity after being offline and
     * resume syncing.
     * <p>
     * However, as some of these checks are performed using incremental backoff, this will in some
     * cases not happen immediately.
     * <p>
     * In those cases it can be beneficial to call this method manually, which will force all
     * sessions to attempt to reconnect immediately and reset any timers they are using for
     * incremental backoff.
     */
    public static void refreshConnections() {
        notifyNetworkIsBack();
    }

    /**
     * Resets the SyncManger and clear all existing users.
     * This will also terminate all sessions.
     *
     * Only call this method when testing.
     */
    synchronized void reset() {
        nativeReset();
        sessions.clear();
        app.networkTransport.resetHeaders();
    }

    /**
     * Simulate a Client Reset by triggering the Object Store error handler with Sync Error Code that will be
     * converted to a Client Reset (211 - Diverging Histories).
     *
     * Only call this method when testing.
     *
     * @param session Session to trigger Client Reset for.
     */
    void simulateClientReset(SyncSession session) {
        nativeSimulateSyncError(session.getConfiguration().getPath(),
                ErrorCode.DIVERGING_HISTORIES.intValue(),
                "Simulate Client Reset",
                true);
    }

    private static native void nativeReset();
    private static native void nativeSimulateSyncError(String realmPath, int errorCode, String errorMessage, boolean isFatal);
    private static native void nativeReconnect();
    private static native void nativeCreateSession(long nativeConfigPtr);
}
