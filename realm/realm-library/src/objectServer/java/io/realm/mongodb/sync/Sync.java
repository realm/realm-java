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

package io.realm.mongodb.sync;

import org.bson.BsonValue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.realm.annotations.Beta;
import io.realm.internal.jni.JniBsonProtocol;
import io.realm.internal.objectstore.OsSyncUser;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.ErrorCode;
import io.realm.internal.Keep;
import io.realm.internal.OsRealmConfig;
import io.realm.internal.Util;
import io.realm.internal.network.NetworkStateReceiver;
import io.realm.log.RealmLog;
import io.realm.mongodb.App;
import io.realm.mongodb.User;

/**
 * A <i>sync</i> manager handling synchronization of local Realms with remote Realm Apps.
 * <p>
 * The primary role of this is to access the {@link SyncSession} for a synchronized Realm. After
 * opening the synchronized Realm you can access the {@link SyncSession} and perform synchronization
 * related operations as shown below:
 * <pre>
 *     App app = new App("app-id");
 *     User user = app.login(Credentials.anonymous());
 *     SyncConfiguration syncConfiguration = new SyncConfiguration.Builder(user, "&lt;partition value&gt;")
 *              .build();
 *     Realm instance = Realm.getInstance(syncConfiguration);
 *     SyncSession session = app.getSync().getSession(syncConfiguration);
 *
 *     instance.executeTransaction(realm -&gt; {
 *         realm.insert(...);
 *     });
 *     session.uploadAllLocalChanges();
 *     instance.close();
 * </pre>
 *
 * @see App#getSync()
 * @see Sync#getSession(SyncConfiguration)
 */
@Keep
@SuppressFBWarnings("MS_CANNOT_BE_FINAL")
@Beta
public abstract class Sync {

    private final App app;
    private final long appNativePointer;
    // keeps track of SyncSession, using 'realm_path'. Java interface with the ObjectStore using the 'realm_path'
    private Map<String, SyncSession> sessions = new ConcurrentHashMap<>();

    protected Sync(App app, long appNativePointer) {
        this.app = app;
        this.appNativePointer = appNativePointer;
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

    private NetworkStateReceiver.ConnectionListener networkListener = new NetworkStateReceiver.ConnectionListener() {
        @Override
        public void onChange(boolean connectionAvailable) {
            if (connectionAvailable) {
                RealmLog.debug("[App(%s)] NetworkListener: Connection available", app.getConfiguration().getAppId());
                // notify all sessions
                notifyNetworkIsBack();
            } else {
                RealmLog.debug("[App(%s)] NetworkListener: Connection lost", app.getConfiguration().getAppId());
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
     * Gets a collection of all the cached {@link SyncSession}.
     *
     * @return a collection of {@link SyncSession}.
     */
    public synchronized Collection<SyncSession> getAllSessions(){
        return this.sessions.values();
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
            session = new SyncSession(syncConfiguration, appNativePointer);
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

    /**
     * Returns the absolute path for the location of the Realm file on disk
     */
    String getAbsolutePathForRealm(String userId, BsonValue partitionValue, @Nullable String overrideFileName) {
        String encodedPartitionValue;
        switch (partitionValue.getBsonType()) {
            case STRING:
            case OBJECT_ID:
            case INT32:
            case INT64:
            case NULL:
                encodedPartitionValue = JniBsonProtocol.encode(partitionValue, AppConfiguration.DEFAULT_BSON_CODEC_REGISTRY);
                break;
            default:
                throw new IllegalArgumentException("Unsupported type: " + partitionValue);
        }
        return nativeGetPathForRealm(appNativePointer, userId, encodedPartitionValue, overrideFileName);
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
     * session to contact.
     */
    @SuppressWarnings("unused")
    private synchronized void notifyErrorHandler(String nativeErrorCategory, int nativeErrorCode, String errorMessage, String clientResetPathInfo, String path) {
        SyncSession syncSession = sessions.get(path);
        if (syncSession != null) {
            try {
                syncSession.notifySessionError(nativeErrorCategory, nativeErrorCode, errorMessage, clientResetPathInfo);
            } catch (Exception exception) {
                RealmLog.error(exception);
            }
        } else {
            RealmLog.warn("Cannot find the SyncSession corresponding to the path: " + path);
        }
    }

    private synchronized void notifyNetworkIsBack() {
        try {
            nativeReconnect(appNativePointer);
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
    public void reconnect() {
        notifyNetworkIsBack();
    }

    /**
     * Resets the SyncManger and clear all existing users.
     * This will also terminate all sessions.
     *
     * Only call this method when testing.
     */
    synchronized void reset() {
        nativeReset(appNativePointer);
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
    void simulateClientReset(SyncSession session) {
        nativeSimulateSyncError(appNativePointer, session.getConfiguration().getPath(),
                ErrorCode.DIVERGING_HISTORIES.intValue(),
                "Simulate Client Reset",
                true);
    }

    private static native void nativeReset(long appNativePointer);
    private static native void nativeSimulateSyncError(long appNativePointer, String realmPath, int errorCode, String errorMessage, boolean isFatal);
    private static native void nativeReconnect(long appNativePointer);
    private static native void nativeCreateSession(long nativeConfigPtr);
    private static native String nativeGetPathForRealm(long appNativePointer, String userId, String partitionValue, @Nullable String overrideFileName);
}
