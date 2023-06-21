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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.realm.annotations.Beta;
import io.realm.internal.ErrorCategory;
import io.realm.internal.jni.JniBsonProtocol;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.ErrorCode;
import io.realm.internal.Keep;
import io.realm.internal.network.NetworkStateReceiver;
import io.realm.log.RealmLog;
import io.realm.mongodb.App;
import okhttp3.internal.tls.OkHostnameVerifier;

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
        }

        return session;
    }

    /**
     * Returns the absolute path for the location of the Realm file on disk.
     * Partition-based sync files will construct their name from the partition value while Flexible
     * Sync Realms will use "default.realm" unless overriden.
     */
    String getAbsolutePathForRealm(String userId, @Nullable BsonValue partitionValue, @Nullable String overrideFileName) {
        String encodedPartitionValue;
        if (partitionValue != null) {
            switch (partitionValue.getBsonType()) {
                case STRING:
                case OBJECT_ID:
                case INT32:
                case INT64:
                    // Only way to here is through Realm API's which only allow UUID's. So we can safely
                    // just convert it to a Bson binary which will give it its correct UUID subtype.
                case BINARY:
                case NULL:
                    encodedPartitionValue = JniBsonProtocol.encode(partitionValue, AppConfiguration.DEFAULT_BSON_CODEC_REGISTRY);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported type: " + partitionValue);
            }
        } else {
            // Encode a dummy value for Flexible Sync Realms, it will be ignored by the native codepaths
            // anyway, but is currently required due to how the native code is wired together when
            // creating the synced Realm paths.
            encodedPartitionValue = JniBsonProtocol.encode("", AppConfiguration.DEFAULT_BSON_CODEC_REGISTRY);
            if (overrideFileName == null) {
                overrideFileName = "default";
            }
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
    private void notifyErrorHandler(byte nativeErrorCategory, int nativeErrorCode, String errorMessage, String clientResetPathInfo, String path) {
        ErrorCode errCode = ErrorCode.fromNativeError(ErrorCategory.toCategory(nativeErrorCategory), nativeErrorCode);

        if (errCode == ErrorCode.CLIENT_RESET) {
            // Avoid deadlock while trying to close realm instances during a client reset
            // Not acquiring the lock here would allow acquiring it later on when we try to close the
            // session in `Sync.removeSession()`.
            doNotifyError(nativeErrorCategory, nativeErrorCode, errorMessage, clientResetPathInfo, path);
        } else {
            // Keep the logic for the rest of the sync errors
            synchronized (this) {
                doNotifyError(nativeErrorCategory, nativeErrorCode, errorMessage, clientResetPathInfo, path);
            }
        }
    }

    private void doNotifyError(byte nativeErrorCategory, int nativeErrorCode, String errorMessage, String clientResetPathInfo, String path) {
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
     * WARNING: This method will not prepare the metadata for an actual Client Reset, thus trying
     * to call `ClientResetRequiredError.executeClientReset()` will not actually work, since the
     * underlying metadata has not been correctly modified.
     *
     * @param session Session to trigger Client Reset for.
     */
    void simulateClientReset(SyncSession session) {
        // Client reset events are thrown from the sync client thread. We need to invoke the `simulateClientReset`
        // on a different thread to fully simulate the scenario.
        new Thread(() -> simulateClientReset(session, ErrorCode.DIVERGING_HISTORIES), "Simulated sync thread").start();
    }

    /**
     * Simulate a Client Reset by triggering the Object Store error handler
     *
     * Only call this method when testing.
     *
     * @param session Session to trigger Client Reset for.
     * @param errorCode error code to simulate.
     */
    void simulateClientReset(SyncSession session, ErrorCode errorCode) {
        nativeSimulateSyncError(appNativePointer,
                session.getConfiguration().getPath(),
                errorCode.intValue(),
                errorCode.getType(),
                "Simulate Client Reset",
                true);
    }

    // Holds the certificate chain (per hostname). We need to keep the order of each certificate
    // according to it's depth in the chain. The depth of the last
    // certificate is 0. The depth of the first certificate is chain
    // length - 1.
    private static HashMap<String, List<String>> ATLAS_CERTIFICATES_CHAIN;

    // The default Android Trust Manager which uses the default KeyStore to
    // validate the certificate chain.
    private static X509TrustManager TRUST_MANAGER;

    // Help transform a String PEM representation of the certificate, into
    // X509Certificate format.
    private static CertificateFactory CERTIFICATE_FACTORY;

    // From Sync implementation:
    //  A recommended way of using the callback function is to return true
    //  if preverify_ok = 1 and depth > 0,
    //  always check the host name if depth = 0,
    //  and use an independent verification step if preverify_ok = 0.
    //
    //  Another possible way of using the callback is to collect all the
    //  ROS_CERTIFICATES_CHAIN until depth = 0, and present the entire chain for
    //  independent verification.
    //
    // In this implementation we use the second method, since it's more suitable for
    // the underlying Java API we need to call to validate the certificate chain.
    @SuppressWarnings("unused")
    static synchronized boolean sslVerifyCallback(String serverAddress, String pemData, int depth) {
        try {
            if (ATLAS_CERTIFICATES_CHAIN == null) {
                ATLAS_CERTIFICATES_CHAIN = new HashMap<>();
                TRUST_MANAGER = systemDefaultTrustManager();
                CERTIFICATE_FACTORY = CertificateFactory.getInstance("X.509");
            }

            if (!ATLAS_CERTIFICATES_CHAIN.containsKey(serverAddress)) {
                ATLAS_CERTIFICATES_CHAIN.put(serverAddress, new ArrayList<String>());
            }

            ATLAS_CERTIFICATES_CHAIN.get(serverAddress).add(pemData);

            if (depth == 0) {
                // transform all PEM ROS_CERTIFICATES_CHAIN into Java X509
                // with respecting the order/depth provided from Sync.
                List<String> pemChain = ATLAS_CERTIFICATES_CHAIN.get(serverAddress);
                int n = pemChain.size();
                X509Certificate[] chain = new X509Certificate[n];
                for (String pem : pemChain) {
                    // The depth of the last certificate is 0.
                    // The depth of the first certificate is chain length - 1.
                    chain[--n] = buildCertificateFromPEM(pem);
                }

                // verify the entire chain
                try {
                    TRUST_MANAGER.checkClientTrusted(chain, "RSA");
                    // verify the hostname
                    boolean isValid = OkHostnameVerifier.INSTANCE.verify(serverAddress, chain[0]);
                    if (isValid) {
                        return true;
                    } else {
                        RealmLog.error("Can not verify the hostname for the host: " + serverAddress);
                        return false;
                    }
                } catch (CertificateException e) {
                    RealmLog.error(e, "Can not validate SSL chain certificate for the host: " + serverAddress);
                    return false;
                } finally {
                    // don't keep the certificate chain in memory
                    ATLAS_CERTIFICATES_CHAIN.remove(serverAddress);
                }
            } else {
                // return true, since the verification will happen for the entire chain
                // when receiving the depth == 0 (host certificate)
                return true;
            }
        } catch (Exception e) {
            RealmLog.error(e, "Error during certificate validation for host: " + serverAddress);
            return false;
        }
    }

    // Credit OkHttp https://github.com/square/okhttp/blob/e5c84e1aef9572adb493197c1b6c4e882aca085b/okhttp/src/main/java/okhttp3/OkHttpClient.java#L270
    private static X509TrustManager systemDefaultTrustManager() {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }
            return (X509TrustManager) trustManagers[0];
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No System TLS", e); // The system has no TLS. Just give up.
        }
    }

    private static X509Certificate buildCertificateFromPEM(String pem) throws IOException, CertificateException {
        InputStream stream = null;
        try {
            stream = new ByteArrayInputStream(pem.getBytes("UTF-8"));
            return (X509Certificate) CERTIFICATE_FACTORY.generateCertificate(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private static native void nativeReset(long appNativePointer);
    private static native void nativeSimulateSyncError(long appNativePointer, String realmPath, int errorCode, String type, String errorMessage, boolean isFatal);
    private static native void nativeReconnect(long appNativePointer);
    private static native String nativeGetPathForRealm(long appNativePointer, String userId, String partitionValue, @Nullable String overrideFileName);
}
