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

import java.io.ByteArrayInputStream;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.realm.internal.Keep;
import io.realm.internal.Util;
import io.realm.internal.network.AuthenticationServer;
import io.realm.internal.network.NetworkStateReceiver;
import io.realm.internal.network.OkHttpAuthenticationServer;
import io.realm.log.RealmLog;
import okhttp3.internal.tls.OkHostnameVerifier;

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

        /**
         * Set this to true to init a SyncManager with a directory named by the process ID. This is useful for
         * integration tests which are emulating multiple sync client by using multiple processes.
         */
        public static boolean separatedDirForSyncManager = false;
    }

    /**
     * APP ID sent to the Realm Object Server. Is automatically initialized to the package name for the app.
     */
    public static String APP_ID = null;

    /**
     * Thread pool used when doing network requests against the Realm Object Server.
     * <p>
     * This pool is only exposed for testing purposes and replacing it while the queue is not
     * empty will result in undefined behaviour.
     */
    @SuppressFBWarnings("MS_SHOULD_BE_FINAL")
    public static ThreadPoolExecutor NETWORK_POOL_EXECUTOR = new ThreadPoolExecutor(
            10, 10, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(100));

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
    // keeps track of SyncSession, using 'realm_path'. Java interface with the ObjectStore using the 'realm_path'
    private static Map<String, SyncSession> sessions = new ConcurrentHashMap<>();
    private static CopyOnWriteArrayList<AuthenticationListener> authListeners = new CopyOnWriteArrayList<AuthenticationListener>();

    // The Sync Client is lightweight, but consider creating/removing it when there is no sessions.
    // Right now it just lives and dies together with the process.
    private static volatile AuthenticationServer authServer = new OkHttpAuthenticationServer();
    private static volatile UserStore userStore;

    // Header configuration
    private static String globalAuthorizationHeaderName = "Authorization"; // authorization header name if no host-defined header is available
    private static Map<String, String> hostRestrictedAuthorizationHeaderName = new HashMap<>(); // authorization header name for the given host
    private static Map<String, String> globalCustomHeaders = new HashMap<>();
    private static Map<String, Map<String, String>> hostRestrictedCustomHeaders = new HashMap<>();

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
     * If no Userstore is specified {@link SyncUser#current()} will always return {@code null}.
     *
     * @param userStore {@link UserStore} to use.
     * @throws IllegalArgumentException if {@code userStore} is {@code null}.
     */
    public static void setUserStore(UserStore userStore) {
        //noinspection ConstantConditions
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
    public static void removeAuthenticationListener(AuthenticationListener listener) {
        //noinspection ConstantConditions
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
    public static void setDefaultSessionErrorHandler(@Nullable SyncSession.ErrorHandler errorHandler) {
        if (errorHandler == null) {
            defaultSessionErrorHandler = SESSION_NO_OP_ERROR_HANDLER;
        } else {
            defaultSessionErrorHandler = errorHandler;
        }
    }

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
    public static synchronized SyncSession getSession(SyncConfiguration syncConfiguration) throws IllegalStateException {
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
    public static synchronized SyncSession getOrCreateSession(SyncConfiguration syncConfiguration, @Nullable URI resolvedRealmURL) {
        // This will not create a new native (Object Store) session, this will only associate a Realm's path
        // with a SyncSession. Object Store's SyncManager is responsible of the life cycle (including creation)
        // of the native session, the provided Java wrap, helps interact with the native session, when reporting error
        // or requesting an access_token for example.

        //noinspection ConstantConditions
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
            if (resolvedRealmURL != null) {
                session.setResolvedRealmURI(resolvedRealmURL);
                // Currently when the user login, the Object Store will try to revive it's inactive sessions
                // (stored previously after a logout). this will cause the OS to call bindSession to obtain an
                // access token, however since the Realm might not be open yet, the wrapObjectStoreSessionIfRequired
                // will not be invoked to wrap the OS store session with the Java session, the Sync client to not resume
                // syncing.
                session.getAccessToken(authServer, "");
            }
        }

        return session;
    }

    /**
     * Sets the name of the HTTP header used to send authorization data in when making requests to
     * all Realm Object Servers used by the app. These servers must have been configured to expect a
     * custom authorization header.
     * <p>
     * The default authorization header is named "Authorization".
     *
     * @param headerName name of the header.
     * @throws IllegalArgumentException if a null or empty header is provided.
     * @see <a href="https://docs.realm.io/platform/guides/learn-realm-sync-and-integrate-with-a-proxy#adding-a-custom-proxy">Adding a custom proxy</a>
     */
    public static synchronized void setAuthorizationHeaderName(String headerName) {
        checkNotEmpty(headerName, "headerName");
        authServer.setAuthorizationHeaderName(headerName, null);
        globalAuthorizationHeaderName = headerName;
    }

    /**
     * Sets the name of the HTTP header used to send authorization data in when making requests to
     * the Realm Object Server running on the defined {@code host}. This server must have been
     * configured to expect a custom authorization header.
     * <p>
     * The default authorization header is named "Authorization".
     *
     * @param headerName name of the header.
     * @param host if this is provided, the authorization header name will only be used on this particular host.
     *             Example of valid values: "localhost", "127.0.0.1" and "myinstance.us1.cloud.realm.io".
     * @throws IllegalArgumentException if a {@code null} or empty header and/or host is provided.
     * @see <a href="https://docs.realm.io/platform/guides/learn-realm-sync-and-integrate-with-a-proxy#adding-a-custom-proxy">Adding a custom proxy</a>
     */

    public static synchronized void setAuthorizationHeaderName(String headerName, String host) {
        checkNotEmpty(headerName, "headerName");
        checkNotEmpty(host, "host");
        host = host.toLowerCase(Locale.US);
        authServer.setAuthorizationHeaderName(headerName, host);
        hostRestrictedAuthorizationHeaderName.put(host, headerName);
    }

    /**
     * Adds an extra HTTP header to append to every request to a Realm Object Server.
     *
     * @param headerName the name of the header.
     * @param headerValue the value of header.
     * @throws IllegalArgumentException if a non-empty {@code headerName} is provided or a null {@code headerValue}.
     */
    public static synchronized void addCustomRequestHeader(String headerName, String headerValue) {
        checkNotEmpty(headerName, "headerName");
        checkNotNull(headerValue, "headerValue");
        authServer.addHeader(headerName, headerValue, null);
        globalCustomHeaders.put(headerName, headerValue);
    }

    /**
     * Adds an extra HTTP header to append to every request to a Realm Object Server.
     *
     * @param headerName the name of the header.
     * @param headerValue the value of header.
     * @param host if this is provided, the this header will only be used on this particular host.
     *             Example of valid values: "localhost", "127.0.0.1" and "myinstance.us1.cloud.realm.io".
     * @throws IllegalArgumentException If an non-empty {@code headerName}, {@code headerValue} or {@code host} is provided.
     */
    public static synchronized void addCustomRequestHeader(String headerName, String headerValue, String host) {
        checkNotEmpty(headerName, "headerName");
        checkNotNull(headerValue, "headerValue");
        checkNotEmpty(host, "host");

        // Headers
        host = host.toLowerCase(Locale.US);
        authServer.addHeader(headerName, headerValue, host);
        Map<String, String> headers = hostRestrictedCustomHeaders.get(host);
        if (headers == null) {
            headers = new LinkedHashMap<>();
            hostRestrictedCustomHeaders.put(host, headers);
        }
        headers.put(headerName, headerValue);
    }

    /**
     * Adds extra HTTP headers to append to every request to a Realm Object Server.
     *
     * @param headers map of (headerName, headerValue) pairs.
     * @throws IllegalArgumentException If any of the headers provided are illegal.
     */
    public static synchronized void addCustomRequestHeaders(@Nullable Map<String, String> headers) {
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                addCustomRequestHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Adds extra HTTP headers to append to every request to a Realm Object Server.
     *
     * @param headers map of (headerName, headerValue) pairs.
     * @param host if this is provided, the this header will only be used on this particular host.
     *             Example of valid values: "localhost", "127.0.0.1" and "myinstance.us1.cloud.realm.io".
     * @throws IllegalArgumentException If any of the headers provided are illegal.
     */
    public static synchronized void addCustomRequestHeaders(@Nullable Map<String, String> headers, String host) {
        if (Util.isEmptyString(host)) {
            throw new IllegalArgumentException("Non-empty 'host' required");
        }
        host = host.toLowerCase(Locale.US);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                addCustomRequestHeader(entry.getKey(), entry.getValue(), host);
            }
        }
    }

    /**
     * Returns the authentication header name used for the http request to the given url.
     *
     * @param objectServerUrl Url to get header for.
     * @return the authorization header name used by http requests to this url.
     */
    public static synchronized String getAuthorizationHeaderName(URI objectServerUrl) {
        String host = objectServerUrl.getHost().toLowerCase(Locale.US);
        String hostRestrictedHeader = hostRestrictedAuthorizationHeaderName.get(host);
        return (hostRestrictedHeader != null) ? hostRestrictedHeader : globalAuthorizationHeaderName;
    }

    /**
     * Returns all the custom headers added to requests to the given url.
     *
     * @return all defined custom headers used when making http requests to the given url.
     * f
     */
    public static synchronized Map<String, String> getCustomRequestHeaders(URI serverSyncUrl) {
        Map<String, String> headers = new LinkedHashMap<>(globalCustomHeaders);
        String host = serverSyncUrl.getHost().toLowerCase(Locale.US);
        Map<String, String> hostHeaders = hostRestrictedCustomHeaders.get(host);
        if (hostHeaders != null) {
            for (Map.Entry<String, String> entry : hostHeaders.entrySet()) {
                headers.put(entry.getKey(), entry.getValue());
            }
        }
        return headers;
    }

    /**
     * Remove the wrapped Java session.
     * @param syncConfiguration configuration object for the synchronized Realm.
     */
    @SuppressWarnings("unused")
    private static synchronized void removeSession(SyncConfiguration syncConfiguration) {
        //noinspection ConstantConditions
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

    /**
     * Retruns the all valid sessions belonging to the user.
     *
     * @param syncUser the user to use.
     * @return the all valid sessions belonging to the user.
     */
    static List<SyncSession> getAllSessions(SyncUser syncUser) {
        //noinspection ConstantConditions
        if (syncUser == null) {
            throw new IllegalArgumentException("A non-empty 'syncUser' is required.");
        }
        ArrayList<SyncSession> allSessions = new ArrayList<SyncSession>();
        for (SyncSession syncSession : sessions.values()) {
            if (syncSession.getState() != SyncSession.State.ERROR && syncSession.getUser().equals(syncUser)) {
                allSessions.add(syncSession);
            }
        }
        return allSessions;
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
    public static UserStore getUserStore() {
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
    private static synchronized void notifyErrorHandler(int errorCode, String errorMessage, @Nullable String path) {
        if (Util.isEmptyString(path)) {
            // notify all sessions
            for (SyncSession syncSession : sessions.values()) {
                    try {
                        syncSession.notifySessionError(errorCode, errorMessage);
                    } catch (Exception exception) {
                        RealmLog.error(exception);
                    }
                }
        } else {
            SyncSession syncSession = sessions.get(path);
            if (syncSession != null) {
                try {
                    syncSession.notifySessionError(errorCode, errorMessage);
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
     * Called from native code. This method is not allowed to throw as it would be swallowed
     * by the native Sync Client thread. Instead log all exceptions to logcat.
     */
    @SuppressWarnings("unused")
    private static synchronized void notifyConnectionListeners(String localRealmPath, long oldState, long newState) {
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
    private synchronized static String bindSessionWithConfig(String sessionPath, String refreshToken) {
        final SyncSession syncSession = sessions.get(sessionPath);
        if (syncSession == null) {
            RealmLog.error("Matching Java SyncSession could not be found for: " + sessionPath);
        } else {
            try {
                return syncSession.getAccessToken(authServer, refreshToken);
            } catch (Exception exception) {
                RealmLog.error(exception);
            }
        }
        return null;
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

    private static void checkNotEmpty(String headerName, String varName) {
        if (Util.isEmptyString(headerName)) {
            throw new IllegalArgumentException("Non-empty '" + varName +"' required.");
        }
    }

    private static void checkNotNull(@Nullable String val, String varName) {
        if (val == null) {
            throw new IllegalArgumentException("Non-null'" + varName +"' required.");
        }
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
        hostRestrictedAuthorizationHeaderName.clear();
        globalAuthorizationHeaderName = "Authorization";
        hostRestrictedCustomHeaders.clear();
        globalCustomHeaders.clear();
        authServer.clearCustomHeaderSettings();
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
