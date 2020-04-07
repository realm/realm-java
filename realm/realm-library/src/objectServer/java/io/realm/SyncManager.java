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
 * Class encapsulating Sync responsibilities for a {@link io.realm.RealmApp}
 *
 * Each Sync Manager is backed by an underlying SyncClient that is responsible for all communication
 * against one specific host.
 *
 *
 *
 */

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

    private volatile static String CREATED_APP_ID = null; //
    private final RealmApp app;
    private final String appId;
    // keeps track of SyncSession, using 'realm_path'. Java interface with the ObjectStore using the 'realm_path'
    private Map<String, SyncSession> sessions = new ConcurrentHashMap<>();
    // init the "sync_manager.cpp" metadata Realm, this is also needed later, when re try
    // to schedule a client reset. in realm-java#master this is already done, when initialising
    // the RealmFileUserStore (not available now on releases)
    Context context = Realm.applicationContext;

    SyncManager(RealmApp app) {
        this.app = app;
        this.appId = app.getConfiguration().getAppId();

         // TODO: Right now we only support one SyncClient or one RealmApp. This class will throw a
        // exception if you try to create it twice. Which will happen as part of setting up a
        // RealmApp.
        synchronized (SyncManager.class) {
            if (CREATED_APP_ID != null && !appId.equals(CREATED_APP_ID)) {
                throw new IllegalStateException("Only one RealmApp is currently supported. " + CREATED_APP_ID + " already exists.");
            }
            init(app.getConfiguration());
            CREATED_APP_ID = appId;
        }
    }

    /**
     * Initializes both the Java and the underlying C++ Sync components.
     */
    private void init(RealmAppConfiguration appConfig) {
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

        // Create app UserAgent string
        String appDefinedUserAgent = null;
        String appName = appConfig.getAppName();
        String appVersion = appConfig.getAppVersion();
        if (!Util.isEmptyString(appName) || !Util.isEmptyString(appVersion)) {
            StringBuilder sb = new StringBuilder();
            sb.append(Util.isEmptyString(appName) ? "Undefined" : appName);
            sb.append('/');
            sb.append(Util.isEmptyString(appName) ? "Undefined" : appVersion);
            appDefinedUserAgent = sb.toString();
        }

        if (SyncManager.Debug.separatedDirForSyncManager) {
            try {
                // Files.createTempDirectory is not available on JDK 6.
                File dir = File.createTempFile("remote_sync_", "_" + android.os.Process.myPid(),
                        context.getFilesDir());
                if (!dir.delete()) {
                    throw new IllegalStateException(String.format(Locale.US,
                            "Temp file '%s' cannot be deleted.", dir.getPath()));
                }
                if (!dir.mkdir()) {
                    throw new IllegalStateException(String.format(Locale.US,
                            "Directory '%s' for SyncManager cannot be created. ",
                            dir.getPath()));
                }
                nativeInitializeSyncManager(dir.getPath(), userAgentBindingInfo, appDefinedUserAgent);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        } else {
            SyncManager.nativeInitializeSyncManager(context.getFilesDir().getPath(), userAgentBindingInfo, appDefinedUserAgent);
        }
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
     * @param resolvedRealmURL resolved Realm URL with the user specific part if not a global Realm.
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

    // Holds the certificate chain (per hostname). We need to keep the order of each certificate
    // according to it's depth in the chain. The depth of the last
    // certificate is 0. The depth of the first certificate is chain
    // length - 1.
    private static HashMap<String, List<String>> ROS_CERTIFICATES_CHAIN;

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
    synchronized static boolean sslVerifyCallback(String serverAddress, String pemData, int depth) {
        try {
            if (ROS_CERTIFICATES_CHAIN == null) {
                ROS_CERTIFICATES_CHAIN = new HashMap<>();
                TRUST_MANAGER = systemDefaultTrustManager();
                CERTIFICATE_FACTORY = CertificateFactory.getInstance("X.509");
            }

            if (!ROS_CERTIFICATES_CHAIN.containsKey(serverAddress)) {
                ROS_CERTIFICATES_CHAIN.put(serverAddress, new ArrayList<String>());
            }

            ROS_CERTIFICATES_CHAIN.get(serverAddress).add(pemData);

            if (depth == 0) {
                // transform all PEM ROS_CERTIFICATES_CHAIN into Java X509
                // with respecting the order/depth provided from Sync.
                List<String> pemChain = ROS_CERTIFICATES_CHAIN.get(serverAddress);
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
                    ROS_CERTIFICATES_CHAIN.remove(serverAddress);
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

    private static native void nativeInitializeSyncManager(String syncBaseDir, String bindingUserAgentInfo, String appUserAgentInfo);
    private static native void nativeReset();
    private static native void nativeSimulateSyncError(String realmPath, int errorCode, String errorMessage, boolean isFatal);
    private static native void nativeReconnect();
    private static native void nativeCreateSession(long nativeConfigPtr);
}
