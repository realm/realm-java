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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InterruptedIOException;
import java.net.URI;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import io.realm.internal.Keep;
import io.realm.internal.SyncObjectServerFacade;
import io.realm.internal.Util;
import io.realm.internal.android.AndroidCapabilities;
import io.realm.internal.async.RealmAsyncTaskImpl;
import io.realm.internal.network.AuthenticateResponse;
import io.realm.internal.network.AuthenticationServer;
import io.realm.internal.network.ExponentialBackoffTask;
import io.realm.internal.network.NetworkStateReceiver;
import io.realm.internal.objectserver.Token;
import io.realm.internal.util.Pair;
import io.realm.log.RealmLog;

/**
 * A session controls how data is synchronized between a single Realm on the device and the server
 * Realm on the Realm Object Server.
 * <p>
 * A Session is created by opening a Realm instance using a {@link SyncConfiguration}. Once a session has been created,
 * it will continue to exist until the app is closed or all threads using this {@link SyncConfiguration} closes their respective {@link Realm}s.
 * <p>
 * A session is controlled by Realm, but can provide additional information in case of errors.
 * These errors are passed along in the {@link SyncSession.ErrorHandler}.
 * <p>
 * When creating a session, Realm will establish a connection to the server. This connection is
 * controlled by Realm and might be shared between multiple sessions. It is possible to get insight
 * into the connection using {@link #addConnectionChangeListener(ConnectionListener)} and {@link #isConnected()}.
 * <p>
 * The session itself has a different lifecycle than the underlying connection. The state of the session
 * can be found using {@link #getState()}.
 * <p>
 * The {@link SyncSession} object is thread safe.
 */
@Keep
public class SyncSession {
    private final static ScheduledThreadPoolExecutor REFRESH_TOKENS_EXECUTOR = new ScheduledThreadPoolExecutor(1);
    private final static long REFRESH_MARGIN_DELAY = TimeUnit.SECONDS.toMillis(10);
    private final static int DIRECTION_DOWNLOAD = 1;
    private final static int DIRECTION_UPLOAD = 2;

    private final SyncConfiguration configuration;
    private final ErrorHandler errorHandler;
    private RealmAsyncTask networkRequest;
    private RealmAsyncTask refreshTokenTask;
    private RealmAsyncTask refreshTokenNetworkRequest;
    private AtomicBoolean onGoingAccessTokenQuery = new AtomicBoolean(false);
    private volatile boolean isClosed = false;
    private final AtomicReference<WaitForSessionWrapper> waitingForServerChanges = new AtomicReference<>(null);

    // Keeps track of how many times `uploadAllLocalChanges()` or `downloadAllServerChanges()` have
    // been called. This is needed so we can correctly ignore canceled requests.
    private final AtomicInteger waitCounter = new AtomicInteger(0);
    private final Object waitForChangesMutex = new Object();

    // We need JavaId -> Listener so C++ can trigger callbacks without keeping a reference to the
    // jobject, which would require a similar map on the C++ side.
    // We need Listener -> Token map in order to remove the progress listener in C++ from Java.
    private final Map<Long, Pair<ProgressListener, Progress>> listenerIdToProgressListenerMap = new HashMap<>();
    private final Map<ProgressListener, Long> progressListenerToOsTokenMap = new IdentityHashMap<>();
    // Counter used to assign all ProgressListeners on this session with a unique id.
    // ListenerId is created by Java to enable C++ to reference the java listener without holding
    // a reference to the actual object.
    // ListenerToken is the same concept, but created by OS and represents the listener.
    // We can unfortunately not just use the ListenerToken, since we need it to be available before
    // we register the listener.
    private final AtomicLong progressListenerId = new AtomicLong(-1);

    // represent different states as defined in SyncSession::PublicState 'sync_session.hpp'
    private static final byte STATE_VALUE_WAITING_FOR_ACCESS_TOKEN = 0;
    private static final byte STATE_VALUE_ACTIVE = 1;
    private static final byte STATE_VALUE_DYING = 2;
    private static final byte STATE_VALUE_INACTIVE = 3;
    private static final byte STATE_VALUE_ERROR = 4;

    // List of Java connection change listeners
    private final CopyOnWriteArrayList<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<>();

    // Reference to the token representing the native listener for connection changes
    // Only one native listener is used for all Java listeners
    private long nativeConnectionListenerToken;

    // represent different states as defined in SyncSession::PublicConnectionState 'sync_session.hpp'
    // saved here instead of as constants in ConnectionState.java to enable static checking by JNI
    static final byte CONNECTION_VALUE_DISCONNECTED = 0;
    static final byte CONNECTION_VALUE_CONNECTING = 1;
    static final byte CONNECTION_VALUE_CONNECTED = 2;

    private URI resolvedRealmURI;

    /**
     * Enum describing the states a SyncSession can be in. The initial state is
     * {@link State#INACTIVE}.
     * <p>
     * A Realm will automatically synchronize data with the server if the session is either {@link State#ACTIVE}
     * or {@link State#DYING} and {@link #isConnected()} returns {@code true}.
     */
    public enum State {

        /**
         * This is the initial state. The session is closed. No data is being synchronized. The session
         * will automatically transition to {@link #WAITING_FOR_ACCESS_TOKEN} when a Realm is opened.
         */
        INACTIVE(STATE_VALUE_INACTIVE),

        /**
         * The user is attempting to synchronize data but needs a valid access token to do so. Realm
         * will either use a cached token or automatically try to acquire one based on the current
         * users login. This requires a network connection.
         * <p>
         * Data cannot be synchronized in this state.
         * <p>
         * Once a valid token is acquired, the session will transition to {@link #ACTIVE}.
         */
        WAITING_FOR_ACCESS_TOKEN(STATE_VALUE_WAITING_FOR_ACCESS_TOKEN),

        /**
         * The Realm is open and data will be synchronized between the device and the server
         * if the underlying connection is {@link ConnectionState#CONNECTED}.
         * <p>
         * The session will remain in this state until either the current login expires or the Realm
         * is closed. In the first case, the session will transition to {@link #WAITING_FOR_ACCESS_TOKEN},
         * in the second case, it will become {@link #DYING}.
         */
        ACTIVE(STATE_VALUE_ACTIVE),

        /**
         * The Realm was closed, but still contains data that needs to be synchronized to the server.
         * The session will attempt to upload all local data before going {@link #INACTIVE}.
         */
        DYING(STATE_VALUE_DYING),

        /**
         * DEPRECATED: This is never used. Errors are reported to {@link ErrorHandler} instead.
         */
        @Deprecated
        ERROR(STATE_VALUE_ERROR);

        final byte value;

        State(byte value) {
            this.value = value;
        }

        static State fromNativeValue(long value) {
            State[] stateCodes = values();
            for (State state : stateCodes) {
                if (state.value == value) {
                    return state;
                }
            }

            throw new IllegalArgumentException("Unknown session state code: " + value);
        }
    }

    SyncSession(SyncConfiguration configuration) {
        this.configuration = configuration;
        this.errorHandler = configuration.getErrorHandler();
    }

    /**
     * Returns the {@link SyncConfiguration} that is responsible for controlling the session.
     *
     * @return SyncConfiguration that defines and controls this session.
     */
    public SyncConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Returns the {@link SyncUser} defined by the {@link SyncConfiguration} that is used to connect to the
     * Realm Object Server.
     *
     * @return {@link SyncUser} used to authenticate the session on the Realm Object Server.
     */
    public SyncUser getUser() {
        return configuration.getUser();
    }

    /**
     * Returns the {@link URI} describing the remote Realm which this session connects to and synchronizes changes with.
     *
     * @return {@link URI} describing the remote Realm.
     */
    public URI getServerUrl() {
        return configuration.getServerUrl();
    }

    // This callback will happen on the thread running the Sync Client.
    void notifySessionError(int errorCode, String errorMessage) {
        if (errorHandler == null) {
            return;
        }
        ErrorCode errCode = ErrorCode.fromInt(errorCode);
        if (errCode == ErrorCode.CLIENT_RESET) {
            // errorMessage contains the path to the backed up file
            RealmConfiguration backupRealmConfiguration = SyncConfiguration.forRecovery(errorMessage, configuration.getEncryptionKey(), configuration.getSchemaMediator());
            errorHandler.onError(this, new ClientResetRequiredError(errCode, "A Client Reset is required. " +
                    "Read more here: https://realm.io/docs/realm-object-server/#client-recovery-from-a-backup.",
                    configuration, backupRealmConfiguration));
        } else {
            errorHandler.onError(this, new ObjectServerError(errCode, errorMessage));
        }
    }

    /**
     * Get the current session's state, as defined in {@link SyncSession.State}.
     *
     * Note that the state may change after this method returns, example: the authentication
     * token will expire, causing the session to move to {@link State#WAITING_FOR_ACCESS_TOKEN}
     * after it was in {@link State#ACTIVE}.
     *
     * @return the state of the session.
     * @see SyncSession.State
     */
    public State getState() {
        byte state = nativeGetState(configuration.getPath());
        if (state == -1) {
            // session was not found, probably the Realm was closed
            throw new IllegalStateException("Could not find session, Realm was probably closed");
        }
        return State.fromNativeValue(state);
    }

    /**
     * Get the current state of the connection used by the session as defined in {@link ConnectionState}.
     *
     * @return the state of connection used by the session.
     * @see ConnectionState
     */
    public ConnectionState getConnectionState() {
        byte state = nativeGetConnectionState(configuration.getPath());
        if (state == -1) {
            // session was not found, probably the Realm was closed
            throw new IllegalStateException("Could not find session, Realm was probably closed");
        }
        return ConnectionState.fromNativeValue(state);
    }

    /**
     * Checks if the session is connected to the server and can synchronize data.
     *
     * This is a best guess effort. To conserve battery the underlying implementation uses heartbeats
     * to  detect if the connection is still available. So if no data is actively being synced
     * and some time has elapsed since the last heartbeat, the connection could have been dropped but
     * this method will still return {@code true}.
     *
     * @return {@code true} if the session is connected and ready to synchronize data, {@code false}
     * if not or if it is in the process of connecting.
     */
    public boolean isConnected() {
        ConnectionState connectionState = ConnectionState.fromNativeValue(nativeGetConnectionState(configuration.getPath()));
        State sessionState = getState();
        return (sessionState == State.ACTIVE || sessionState == State.DYING) && connectionState == ConnectionState.CONNECTED;
    }

    synchronized void notifyProgressListener(long listenerId, long transferredBytes, long transferableBytes) {
        Pair<ProgressListener, Progress> listener = listenerIdToProgressListenerMap.get(listenerId);
        if (listener != null) {
            Progress newProgressNotification = new Progress(transferredBytes, transferableBytes);
            if (!newProgressNotification.equals(listener.second)) {
                listener.second = newProgressNotification;
                listener.first.onChange(newProgressNotification);
            }
        } else {
            RealmLog.debug("Trying unknown listener failed: " + listenerId);
        }
    }

    void notifyConnectionListeners(ConnectionState oldState, ConnectionState newState) {
        for (ConnectionListener listener : connectionListeners) {
            listener.onChange(oldState, newState);
        }
    }

    /**
     * Adds a progress listener tracking changes that need to be downloaded from the Realm Object
     * Server.
     * <p>
     * The {@link ProgressListener} will be triggered immediately when registered, and periodically
     * afterwards.
     *
     * @param mode type of mode used. See {@link ProgressMode} for more information.
     * @param listener the listener to register.
     */
    public synchronized void addDownloadProgressListener(ProgressMode mode, ProgressListener listener) {
        addProgressListener(mode, DIRECTION_DOWNLOAD, listener);
    }

    /**
     * Adds a progress listener tracking changes that need to be uploaded from the device to the
     * Realm Object Server.
     * <p>
     * The {@link ProgressListener} will be triggered immediately when registered, and periodically
     * afterwards.
     *
     * @param mode type of mode used. See {@link ProgressMode} for more information.
     * @param listener the listener to register.
     */
    public synchronized void addUploadProgressListener(ProgressMode mode, ProgressListener listener) {
        addProgressListener(mode, DIRECTION_UPLOAD, listener);
    }

    /**
     * Removes a progress listener. If the listener wasn't registered, this method will do nothing.
     *
     * @param listener listener to remove.
     */
    public synchronized void removeProgressListener(ProgressListener listener) {
        //noinspection ConstantConditions
        if (listener == null) {
            return;
        }
        // If an exception is thrown somewhere in here, we will most likely leave the various
        // maps in an inconsistent manner. Not much we can do about it.
        Long token = progressListenerToOsTokenMap.remove(listener);
        if (token != null) {
            Iterator<Map.Entry<Long, Pair<ProgressListener, Progress>>> it = listenerIdToProgressListenerMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Long, Pair<ProgressListener, Progress>> entry = it.next();
                if (entry.getValue().first.equals(listener)) {
                    it.remove();
                    break;
                }
            }
            nativeRemoveProgressListener(configuration.getPath(), token);
        }
    }

    private void addProgressListener(ProgressMode mode, int direction, ProgressListener listener) {
        checkProgressListenerArguments(mode, listener);
        boolean isStreaming = (mode == ProgressMode.INDEFINITELY);
        long listenerId = progressListenerId.incrementAndGet();

        // A listener might be triggered immediately as part of `nativeAddProgressListener`, so
        // we need to make sure it can be found by SyncManager.notifyProgressListener()
        listenerIdToProgressListenerMap.put(listenerId, new Pair<ProgressListener, Progress>(listener, null));
        long listenerToken = nativeAddProgressListener(configuration.getPath(), listenerId , direction, isStreaming);
        if (listenerToken == 0) {
            // ObjectStore did not register the listener. This can happen if a
            // listener is registered with ProgressMode.CURRENT_CHANGES and no changes actually
            // exists. In that case the listener was triggered immediately and we just need
            // to clean it up, since it will never be called again.
            listenerIdToProgressListenerMap.remove(listenerId);
        } else {
            // Listener was properly registered.
            progressListenerToOsTokenMap.put(listener, listenerToken);
        }
    }

    private void checkProgressListenerArguments(ProgressMode mode, ProgressListener listener) {
        //noinspection ConstantConditions
        if (listener == null) {
            throw new IllegalArgumentException("Non-null 'listener' required.");
        }
        //noinspection ConstantConditions
        if (mode == null) {
            throw new IllegalArgumentException("Non-null 'mode' required.");
        }
    }

    /**
     * Adds a listener tracking changes to the connection backing this session. See {@link ConnectionState}
     * for further details.
     *
     * @param listener the listener to register.
     * @throws IllegalArgumentException if the listener is {@code null}.
     * @see ConnectionState
     */
    public synchronized void addConnectionChangeListener(ConnectionListener listener) {
        checkNonNullListener(listener);
        if (connectionListeners.isEmpty()) {
            nativeConnectionListenerToken = nativeAddConnectionListener(configuration.getPath());
        }
        connectionListeners.add(listener);
    }

    /**
     * Removes a previously registered {@link ConnectionListener}.
     *
     * @param listener listener to remove
     * @throws IllegalArgumentException if the listener is {@code null}.
     */
    public synchronized void removeConnectionChangeListener(ConnectionListener listener) {
        checkNonNullListener(listener);
        connectionListeners.remove(listener);
        if (connectionListeners.isEmpty()) {
            nativeRemoveConnectionListener(nativeConnectionListenerToken, configuration.getPath());
        }
    }

    void close() {
        isClosed = true;
        if (networkRequest != null) {
            networkRequest.cancel();
        }
        clearScheduledAccessTokenRefresh();
    }

    // This method will be called once all changes have been downloaded or uploaded.
    // This method might be called on another thread than the one that called `downloadAllServerChanges` or
    // `uploadAllLocalChanges()`
    //
    // Be very careful with synchronized blocks.
    // If the native listener was successfully registered, Object Store guarantees that this method will be called at
    // least once, even if the session is closed.
    @SuppressWarnings("unused")
    private void notifyAllChangesSent(int callbackId, Long errorcode, String errorMessage) {
        WaitForSessionWrapper wrapper = waitingForServerChanges.get();
        if (wrapper != null) {
            // Only react to callback if the callback is "active"
            // A callback can only become inactive if the thread was interrupted:
            // 1. Call `downloadAllServerChanges()` (callback = 1)
            // 2. Interrupt it
            // 3. Call `uploadAllLocalChanges()` ( callback = 2)
            // 4. Sync notifies session that callback:1 is done. It should be ignored.
            if (waitCounter.get() == callbackId) {
                wrapper.handleResult(errorcode, errorMessage);
            }
        }
    }

    /**
     * Calling this method will block until all known remote changes have been downloaded and applied to the Realm.
     * This will involve network access, so calling this method should only be done from a non-UI thread.
     * <p>
     * If the device is offline, this method might never return.
     * <p>
     * This method cannot be called before the session has been started.
     *
     * @throws IllegalStateException if called on the Android main thread.
     * @throws InterruptedException if the thread was interrupted while downloading was in progress.
     */
    public void downloadAllServerChanges() throws InterruptedException {
        checkIfNotOnMainThread("downloadAllServerChanges() cannot be called from the main thread.");

        // Blocking only happens at the Java layer. To prevent deadlocking the underlying SyncSession we register
        // an async listener there and let it callback to the Java Session when done. This feels icky at best, but
        // since all operations on the SyncSession operate under a shared mutex, we would prevent all other actions on the
        // session, including trying to stop it.
        // In Java we cannot lock on the Session object either since it will prevent any attempt at modifying the
        // lifecycle while it is in a waiting state. Thus we use a specialised mutex.
        synchronized (waitForChangesMutex) {
            waitForChanges(DIRECTION_DOWNLOAD);
        }
    }

    /**
     * Calling this method will block until all known local changes have been uploaded to the server.
     * This will involve network access, so calling this method should only be done from a non-UI thread.
     * <p>
     * If the device is offline, this method might never return.
     * <p>
     * This method cannot be called before the session has been started.
     *
     * @throws IllegalStateException if called on the Android main thread.
     * @throws InterruptedException if the thread was interrupted while downloading was in progress.
     */
    public void uploadAllLocalChanges() throws InterruptedException {
        checkIfNotOnMainThread("uploadAllLocalChanges() cannot be called from the main thread.");

        // Blocking only happens at the Java layer. To prevent deadlocking the underlying SyncSession we register
        // an async listener there and let it callback to the Java Session when done. This feels icky at best, but
        // since all operations on the SyncSession operate under a shared mutex, we would prevent all other actions on the
        // session, including trying to stop it.
        // In Java we cannot lock on the Session object either since it will prevent any attempt at modifying the
        // lifecycle while it is in a waiting state. Thus we use a specialised mutex.
        synchronized (waitForChangesMutex) {
            waitForChanges(DIRECTION_UPLOAD);
        }
    }

    /**
     * Attempts to start the session and enable synchronization with the Realm Object Server.
     * <p>
     * This happens automatically when opening the Realm instance, so doing it manually should only
     * be needed if the session was stopped using {@link #stop()}.
     * <p>
     * If the session was already started, calling this method will do nothing.
     * <p>
     * A session is considered started if {@link #getState()} returns either {@link State#ACTIVE} or
     * {@link State#WAITING_FOR_ACCESS_TOKEN}. If the session is {@link State#DYING}, the session
     * will be moved back to {@link State#ACTIVE}.
     *
     * @see #getState()
     * @see #stop()
     */
    public synchronized void start() {
        nativeStart(configuration.getPath());
    }

    /**
     * Stops any synchronization with the Realm Object Server until the Realm is re-opened again
     * after fully closing it.
     * <p>
     * Synchronization can be re-enabled by calling {@link #start()} again.
     * <p>
     * If the session is already stopped, calling this method will do nothing.
     */
    public synchronized void stop() {
        nativeStop(configuration.getPath());
    }

    void setResolvedRealmURI(URI resolvedRealmURI) {
        this.resolvedRealmURI = resolvedRealmURI;
    }

    /**
     * This method should only be called when guarded by the {@link #waitForChangesMutex}.
     * It will block into all changes have been either uploaded or downloaded depending on the chosen direction.
     *
     * @param direction either {@link #DIRECTION_DOWNLOAD} or {@link #DIRECTION_UPLOAD}
     */
    private void waitForChanges(int direction) throws InterruptedException {
        if (direction != DIRECTION_DOWNLOAD && direction != DIRECTION_UPLOAD) {
            throw new IllegalArgumentException("Unknown direction: " + direction);
        }
        if (!isClosed) {
            String realmPath = configuration.getPath();
            WaitForSessionWrapper wrapper = new WaitForSessionWrapper();
            waitingForServerChanges.set(wrapper);
            int callbackId = waitCounter.incrementAndGet();
            boolean listenerRegistered = (direction == DIRECTION_DOWNLOAD)
                    ? nativeWaitForDownloadCompletion(callbackId, realmPath)
                    : nativeWaitForUploadCompletion(callbackId, realmPath);
            if (!listenerRegistered) {
                waitingForServerChanges.set(null);
                String errorMsg = "";
                switch (direction) {
                    case DIRECTION_DOWNLOAD: errorMsg = "It was not possible to download all remote changes."; break;
                    case DIRECTION_UPLOAD: errorMsg = "It was not possible upload all local changes."; break;
                    default:
                        throw new IllegalArgumentException("Unknown direction: " + direction);
                }

                throw new ObjectServerError(ErrorCode.UNKNOWN, errorMsg + " Has the SyncClient been started?");
            }
            try {
                wrapper.waitForServerChanges();
            } catch(InterruptedException e) {
                waitingForServerChanges.set(null); // Ignore any results being sent if the wait was interrupted.
                throw e;
            }

            // This might return after the session was closed. In that case, just ignore any result
            try {
                if (!isClosed) {
                    if (!wrapper.isSuccess()) {
                        wrapper.throwExceptionIfNeeded();
                    }
                }
            } finally {
                waitingForServerChanges.set(null);
            }
        }
    }

    private void checkIfNotOnMainThread(String errorMessage) {
        if (new AndroidCapabilities().isMainThread()) {
            throw new IllegalStateException(errorMessage);
        }
    }

    private void checkNonNullListener(@Nullable Object listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Non-null 'listener' required.");
        }
    }

    /**
     * Interface used to report any session errors.
     *
     * @see SyncManager#setDefaultSessionErrorHandler(ErrorHandler)
     * @see SyncConfiguration.Builder#errorHandler(ErrorHandler)
     */
    public interface ErrorHandler {
        /**
         * Callback for errors on a session object. It is not allowed to throw an exception inside an error handler.
         * If the operations in an error handler can throw, it is safer to catch any exception in the error handler.
         * When an exception is thrown in the error handler, the occurrence will be logged and the exception
         * will be ignored.
         *
         * <p>
         * When the {@code error.getErrorCode()} returns {@link ErrorCode#CLIENT_RESET}, it indicates the Realm
         * needs to be reset and the {@code error} can be cast to {@link ClientResetRequiredError}.
         * <p>
         * A synced Realm may need to be reset because the Realm Object Server encountered an error and had
         * to be restored from a backup. If the backup copy of the remote Realm is of an earlier version
         * than the local copy of the Realm, the server will ask the client to reset the Realm.
         * <p>
         * The reset process is as follows: the local copy of the Realm is copied into a recovery directory
         * for safekeeping, and then deleted from the original location. The next time the Realm for that
         * URL is opened, the Realm will automatically be re-downloaded from the Realm Object Server, and
         * can be used as normal.
         * <p>
         * Data written to the Realm after the local copy of the Realm diverged from the backup remote copy
         * will be present in the local recovery copy of the Realm file. The re-downloaded Realm will
         * initially contain only the data present at the time the Realm was backed up on the server.
         * <p>
         * The client reset process can be initiated in one of two ways:
         * <ol>
         *     <li>
         *         Run {@link ClientResetRequiredError#executeClientReset()} manually. All Realm instances must be
         *         closed before this method is called.
         *     </li>
         *     <li>
         *         If Client Reset isn't executed manually, it will automatically be carried out the next time all
         *         Realm instances have been closed and re-opened. This will most likely be
         *         when the app is restarted.
         *     </li>
         * </ol>
         *
         * <b>WARNING:</b>
         * Any writes to the Realm file between this callback and Client Reset has been executed, will not be
         * synchronized to the Object Server. Those changes will only be present in the backed up file. It is therefore
         * recommended to close all open Realm instances as soon as possible.
         *
         *
         * @param session {@link SyncSession} this error happened on.
         * @param error type of error.
         */
        void onError(SyncSession session, ObjectServerError error);
    }

    // Return the access token for the Realm this Session is connected to.
    String getAccessToken(final AuthenticationServer authServer, String refreshToken) {
        // check first if there's a valid access_token we can return immediately
        if (getUser().isRealmAuthenticated(configuration)) {
            Token accessToken = getUser().getAccessToken(configuration);
            // start refreshing this token if a refresh is not going on
            if (!onGoingAccessTokenQuery.getAndSet(true)) {
                scheduleRefreshAccessToken(authServer, accessToken.expiresMs());
            }
            return accessToken.value();

        } else {
            // check and update if we received a new refresh_token
            if (!Util.isEmptyString(refreshToken)) {
                try {
                    JSONObject refreshTokenJSON = new JSONObject(refreshToken);
                    Token newRefreshToken = Token.from(refreshTokenJSON.getJSONObject("userToken"));
                    if (newRefreshToken.hashCode() != getUser().getRefreshToken().hashCode()) {
                        RealmLog.debug("Session[%s]: Access token updated", configuration.getPath());
                        getUser().setRefreshToken(newRefreshToken);
                    }
                } catch (JSONException e) {
                    RealmLog.error(e, "Session[%s]: Can not parse the refresh_token into a valid JSONObject: ", configuration.getPath());
                }
            }
            if (!onGoingAccessTokenQuery.get() && NetworkStateReceiver.isOnline(SyncObjectServerFacade.getApplicationContext())) {
                authenticateRealm(authServer);
            }
        }
        return null;
    }

    // Authenticate by getting access tokens for the specific Realm
    private void authenticateRealm(final AuthenticationServer authServer) {
        if (networkRequest != null) {
            networkRequest.cancel();
        }
        clearScheduledAccessTokenRefresh();

        onGoingAccessTokenQuery.set(true);
        // Authenticate in a background thread. This allows incremental backoff and retries in a safe manner.
        Future<?> task = SyncManager.NETWORK_POOL_EXECUTOR.submit(new ExponentialBackoffTask<AuthenticateResponse>() {
            @Override
            protected AuthenticateResponse execute() {
                if (!isClosed && !Thread.currentThread().isInterrupted()) {
                    return authServer.loginToRealm(
                            getUser().getRefreshToken(), //refresh token in fact
                            resolvedRealmURI,
                            getUser().getAuthenticationUrl()
                    );
                }
                return null;
            }

            @Override
            protected void onSuccess(AuthenticateResponse response) {
                RealmLog.debug("Session[%s]: Access token acquired", configuration.getPath());
                if (!isClosed && !Thread.currentThread().isInterrupted()) {
                    URI realmUrl = configuration.getServerUrl();
                    getUser().addRealm(configuration, response.getAccessToken());
                    if (nativeRefreshAccessToken(configuration.getPath(), response.getAccessToken().value(), realmUrl.toString())) {
                        scheduleRefreshAccessToken(authServer, response.getAccessToken().expiresMs());

                    } else {
                        // token not applied, no refresh will be scheduled
                        onGoingAccessTokenQuery.set(false);
                    }
                }
            }

            @Override
            protected void onError(AuthenticateResponse response) {
                onGoingAccessTokenQuery.set(false);
                RealmLog.debug("Session[%s]: Failed to get access token (%s)", configuration.getPath(),
                        response.getError().getErrorCode());
                if (!isClosed
                        && !Thread.currentThread().isInterrupted()
                        // We might be interrupted while negotiating an access token with the Realm Object Server
                        // This will result in a InterruptedIOException from OkHttp. We should ignore this as
                        // well.
                        && !(response.getError().getException() instanceof InterruptedIOException)) {
                    errorHandler.onError(SyncSession.this, response.getError());
                }
            }
        });
        networkRequest = new RealmAsyncTaskImpl(task, SyncManager.NETWORK_POOL_EXECUTOR);
    }

    private void scheduleRefreshAccessToken(final AuthenticationServer authServer, long expireDateInMs) {
        onGoingAccessTokenQuery.set(true);
        // calculate the delay time before which we should refresh the access_token,
        // we adjust to 10 second to proactively refresh the access_token before the session
        // hit the expire date on the token
        long refreshAfter =  expireDateInMs - System.currentTimeMillis() - REFRESH_MARGIN_DELAY;
        if (refreshAfter < 0) {
            // Token already expired
            RealmLog.debug("Expires time already reached for the access token, refresh as soon as possible");
            // we avoid refreshing directly to avoid an edge case where the client clock is ahead
            // of the server, causing all access_token received from the server to be always
            // expired, we will flood the server with refresh token requests then, so adding
            // a bit of delay is the best effort in this case.
            refreshAfter = REFRESH_MARGIN_DELAY;
        }

        RealmLog.debug("Scheduling an access_token refresh in " + (refreshAfter) + " milliseconds");

        if (refreshTokenTask != null) {
            refreshTokenTask.cancel();
        }

        ScheduledFuture<?> task = REFRESH_TOKENS_EXECUTOR.schedule(new Runnable() {
            @Override
            public void run() {
                if (!isClosed && !Thread.currentThread().isInterrupted() && !refreshTokenTask.isCancelled()) {
                    refreshAccessToken(authServer);
                }
            }
        }, refreshAfter, TimeUnit.MILLISECONDS);
        refreshTokenTask = new RealmAsyncTaskImpl(task, REFRESH_TOKENS_EXECUTOR);
    }

    // Authenticate by getting access tokens for the specific Realm
    private void refreshAccessToken(final AuthenticationServer authServer) {
        // Authenticate in a background thread. This allows incremental backoff and retries in a safe manner.
        clearScheduledAccessTokenRefresh();

        Future<?> task = SyncManager.NETWORK_POOL_EXECUTOR.submit(new ExponentialBackoffTask<AuthenticateResponse>() {
            @Override
            protected AuthenticateResponse execute() {
                if (!isClosed && !Thread.currentThread().isInterrupted()) {
                    return authServer.refreshUser(getUser().getRefreshToken(), resolvedRealmURI, getUser().getAuthenticationUrl());
                }
                return null;
            }

            @Override
            protected void onSuccess(AuthenticateResponse response) {
                synchronized (SyncSession.this) {
                    if (!isClosed && !Thread.currentThread().isInterrupted() && !refreshTokenNetworkRequest.isCancelled()) {
                        RealmLog.debug("Access Token refreshed successfully, Sync URL: " + configuration.getServerUrl());
                        URI realmUrl = configuration.getServerUrl();
                        if (nativeRefreshAccessToken(configuration.getPath(), response.getAccessToken().value(), realmUrl.toString())) {
                            // replace the user old access_token
                            getUser().addRealm(configuration, response.getAccessToken());
                            // schedule the next refresh
                            scheduleRefreshAccessToken(authServer, response.getAccessToken().expiresMs());
                        }
                    }
                }
            }

            @Override
            protected void onError(AuthenticateResponse response) {
                if (!isClosed && !Thread.currentThread().isInterrupted()) {
                    onGoingAccessTokenQuery.set(false);
                    RealmLog.error("Unrecoverable error, while refreshing the access Token (" + response.getError().toString() + ") reschedule will not happen");
                }
            }
        });
        refreshTokenNetworkRequest = new RealmAsyncTaskImpl(task, SyncManager.NETWORK_POOL_EXECUTOR);
    }

    void clearScheduledAccessTokenRefresh() {
        if (refreshTokenTask != null) {
            refreshTokenTask.cancel();
        }
        if (refreshTokenNetworkRequest != null) {
            refreshTokenNetworkRequest.cancel();
        }
        onGoingAccessTokenQuery.set(false);
    }

    // Wrapper class for handling the async operations of the underlying SyncSession calling
    // `async_wait_for_download_completion` or `async_wait_for_upload_completion`
    private static class WaitForSessionWrapper {

        private final CountDownLatch waiter = new CountDownLatch(1);
        private volatile boolean resultReceived = false;
        private Long errorCode = null;
        private String errorMessage;

        /**
         * Block until the wait either completes or is terminated for other reasons.
         */
        public void waitForServerChanges() throws InterruptedException {
            if (!resultReceived) {
                waiter.await();
            }
        }

        /**
         * Process the result of a waiting action. This will also unblock anyone who called {@link #waiter}.
         *
         * @param errorCode error code if an error occurred, {@code null} if changes were successfully downloaded.
         * @param errorMessage error message (if any).
         */
        public void handleResult(Long errorCode, String errorMessage) {
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            this.resultReceived = true;
            waiter.countDown();
        }

        public boolean isSuccess() {
            return resultReceived && errorCode == null;
        }

        /**
         * Will throw an exception if the wait was terminated with an error. If it was canceled, this method will
         * do nothing.
         */
        public void throwExceptionIfNeeded() {
            if (resultReceived && errorCode != null) {
                throw new ObjectServerError(ErrorCode.UNKNOWN,
                        String.format(Locale.US, "Internal error (%d): %s", errorCode, errorMessage));
            }
        }
    }

    private static native long nativeAddConnectionListener(String localRealmPath);
    private static native void nativeRemoveConnectionListener(long listenerId, String localRealmPath);
    private static native long nativeAddProgressListener(String localRealmPath, long listenerId, int direction, boolean isStreaming);
    private static native void nativeRemoveProgressListener(String localRealmPath, long listenerToken);
    private static native boolean nativeRefreshAccessToken(String localRealmPath, String accessToken, String realmUrl);
    private native boolean nativeWaitForDownloadCompletion(int callbackId, String localRealmPath);
    private native boolean nativeWaitForUploadCompletion(int callbackId, String localRealmPath);
    private static native byte nativeGetState(String localRealmPath);
    private static native byte nativeGetConnectionState(String localRealmPath);
    private static native void nativeStart(String localRealmPath);
    private static native void nativeStop(String localRealmPath);
}
