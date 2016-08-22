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

package io.realm.objectserver.session;

import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.realm.RealmAsyncTask;
import io.realm.internal.Util;
import io.realm.internal.log.RealmLog;
import io.realm.internal.objectserver.Error;
import io.realm.internal.objectserver.Token;
import io.realm.internal.objectserver.network.AuthenticateResponse;
import io.realm.internal.objectserver.network.AuthentificationServer;
import io.realm.internal.objectserver.network.NetworkStateReceiver;
import io.realm.objectserver.SyncConfiguration;
import io.realm.objectserver.SyncManager;
import io.realm.objectserver.User;
import io.realm.objectserver.Credentials;
import io.realm.objectserver.syncpolicy.SyncPolicy;

/**
 * This class controls the connection to a Realm Object Server for one Realm. If
 * {@link SyncConfiguration#isAutoConnectEnabled()} returns {@code true}, the session will automatically be
 * managed when opening and closing Realm instances.
 *
 * In particular the Realm will begin syncing depending on the given {@link SyncPolicy} . When a Realm is closed
 * the session will close immediately if there is no local or remote changes that needs to by synced, otherwise it will
 * try to sync all those changes before closing the session.
 *
 *
 * The SessionInfo lifecycle consists of the following steps:
 *
 * 1. CREATE:
 * Creates the SessionInfo object using the defined configuration and credentials. No connection have yet been
 * made.
 *
 * 2. BIND:
 * Bind the locale Realm to the remote Realm and start synchronizing. This might trigger a AUTHENTICATE step if
 * the credentials have not yet been authenticated.
 *
 * 3. UNBIND:
 * Stop synchronizing local and remote data. It is possible to BIND again. This will not clear any current Access Token.

 * 4. CLOSE:
 * Unbind the Realm and clear any credentials and access tokens. Once a session has been closed it can no longer
 * be re-opened or reused.
 *
 * a. AUTHENTICATE
 * Authenticates the given credentials. This will produce an Access Token that determine access and permissions.
 * In order to successfully BIND a Realm, a valid Access Token must be present
 *
 *
 * This object is thread safe.
 *
 */
// TODO Rename to Session / ObjectServerSession instead? This is turning into a mutable object instead
// of a value type which the name suggests.

public final class Session {

    private final HashMap<SessionState, FsmState> FSM = new HashMap<SessionState, FsmState>();

    // Variables used by the FSM
    final SyncConfiguration configuration;
    final long nativeSyncClientPointer;
    final AuthentificationServer authServer;
    public long nativeSessionPointer;
    final User user;
    RealmAsyncTask networkRequest;
    NetworkStateReceiver.ConnectionListener networkListener;

    // Keeping track of currrent FSM state
    SessionState currentStateDescription;
    FsmState currentState;

    /**
     * Creates a new Object Server Session
     */
    public Session(SyncConfiguration objectServerConfiguration, long nativeSyncClientPointer, AuthentificationServer authServer) {
        this.configuration = objectServerConfiguration;
        this.user = configuration.getUser();
        this.nativeSyncClientPointer = nativeSyncClientPointer;
        this.authServer = authServer;
        setupStateMachine();
    }

    private void setupStateMachine() {
        FSM.put(SessionState.INITIAL, new InitialState());
        FSM.put(SessionState.STARTED, new StartedState());
        FSM.put(SessionState.UNBOUND, new UnboundState());
        FSM.put(SessionState.BINDING_REALM, new BindingRealmState());
        FSM.put(SessionState.AUTHENTICATING, new AuthenticatingState());
        FSM.put(SessionState.BOUND, new BoundState());
        FSM.put(SessionState.STOPPED, new StoppedState());
        RealmLog.d("Session started: " + configuration.getServerUrl());
        currentState = FSM.get(SessionState.INITIAL);
        currentState.entry(this);
    }

    // Goto the next state. The FsmState classes are responsible for calling this method as a reaction to a FsmAction
    // being called or an internal action triggering a state transition.
    void nextState(SessionState nextStateDescription) {
        FsmState nextState = FSM.get(nextStateDescription);
        if (nextState == null) {
            throw new IllegalStateException("No state was configured to handle: " + nextStateDescription);
        }
        RealmLog.d(String.format("Session[%s]: %s -> %s", configuration.getServerUrl(), currentStateDescription, nextStateDescription));
        currentStateDescription = nextStateDescription;
        currentState = nextState;
        nextState.entry(this);
    }

    public synchronized void start() {
        currentState.onStart();
    }

    public synchronized void stop() {
        currentState.onStop();
    }

    /**
     * Binds a local Realm to a remote one by using the credentials provided by the
     * {@link SyncConfiguration}. Once bound, changes on either the local or Remote Realm will be synchronized
     * immediately.
     *
     * Note that binding a Realm is not guaranteed to succeed. If a device is offline or credentials are no longer valid,
     *
     */
    public synchronized void bind() {
        currentState.onBind();
    }

    /**
     * Stops a local Realm from synchronizing changes with the remote Realm.
     * It is possible to call {@link #bind()} again after a Realm has been unbound.
     */
    public synchronized void unbind() {
        currentState.onUnbind();
    }

    /**
     * Refreshes the access token. Each access token has a predetermined lifetime after which it will no longer work.
     *
     * In order to provide a smoother sync experience for end users, it is recommended to refresh the access token
     * before it expires. Otherwise there is a chance that the access token must be refreshed as part of trying
     * to synchronize other changes which will introduce a small latency.
     *
     * Refreshing is handled automatically by this class, but this method will trigger it manually as well.
     */
    public synchronized void refresh() {
        currentState.onRefresh();
//        if (!isAuthenticated()) {
//            throw new IllegalStateException("Can only refresh already validated credentials. " +
//                    "Check with isAuthenticated() first.");
//        }
//
    }

    public void setUser(User user) {
        // This will probably replace `setCredentials()`
//        setCredentials(user.getCredentials());
    }



    /**
     * Set the credentials used to bind this Realm to the Realm Object Server. Credentials control access and
     * permissions for that Realm.
     *
     * If a Realm is already connected using older credentials, the connection wil be closed an a new connection
     * will be made using the new credentials.
     **
     * @param credentials credentials to use when authenticating access to the remote Realm.
     */
    @Deprecated
    public synchronized void setCredentials(Credentials credentials) {
        currentState.onSetCredentials(credentials);
//        if (credentials == null) {
//            throw new IllegalArgumentException("non-empty 'credentials' must be provided");
//        }
//
//        boolean wasBound = false;
//        if (isBound()) {
//            wasBound = true;
//            unbind();
//        }
//
//        resetCredentials();
//        this.credentials = credentials;
//
//        // Rebind if the connection was bound before replacing the credentials.
//        if (wasBound) {
//            bind();
//        }
//
//        // TODO This method will replace any credentials provided in the configuration? Is this acceptable and should
//        // it be documented better somehow?
    }

    private void abortBind() {
        // TODO Abort any bind currently in progress
        notifyBindAborded();
    }

    // IDEAS

    /**
     * Returns {@code true} if this session has been authenticated and the Access Token hasn't expired yet.
     *
     * WARNING: The Realm Object Server might invalidate an Access Token at any time, so even if this method returns
     * {@code true}, it does not mean it is possible t
     *
     *
     * @return
     */
    public synchronized boolean isAuthenticated() {

        return false;
    }

    /**
     * Checks if the local Realm is bound to to the remote Realm and can synchronize any changes happening on either
     * side.
     *
     * @return {@code true} if the local Realm is bound to the remote Realm, {@code false} otherwise.
     */
    public boolean isBound() {
        return currentStateDescription == SessionState.BOUND;
    }

    //
    // Package protected methods used by the FSM states to manipulate session variables. These methods should
    //

    // Initialize the Session object
    void initialize() {
        nativeSessionPointer = nativeCreateSession(nativeSyncClientPointer, configuration.getPath());
    }

    // Apply any sync policy. It is assumed that any previous running policy have been stopped
    void applySyncPolicy() {
        SyncPolicy syncPolicy = configuration.getSyncPolicy();
        syncPolicy.apply(this);
    }

    // Bind with proper access tokens
    void bindWithTokens() {
        // TODO How do we handle errors from bind?
        Token accessToken = user.getAccessToken(configuration);
        if (accessToken == null) {
            throw new IllegalStateException("User '" + user.toString() + "' does not have an access token for "
                    + configuration.getServerUrl());
        }
        nativeBind(nativeSessionPointer, configuration.getRealmFileName(), accessToken.value());
    }

    // Unbind a Realm that is currently bound
    void unbindActiveConnection() {
        nativeUnbind(nativeSessionPointer);
        nativeSessionPointer = 0;
        notifyUnbinded();
    }

    void replaceCredentials(Credentials credentials) {
        // TODO
    }

    void authenticate(final Runnable onSuccess, final Runnable onError) {
        if (networkRequest != null) {
            networkRequest.cancel();
        }
        // Authenticate in a background thread. This allows incremental backoff and retries in a safe manner.
        // TODO: This is a potentially very long-lived thread. Should we use a separate thread pool?
        Future<?> task = SyncManager.NETWORK_POOL_EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                // FIXME Align how many credentials are supported. Just assume 1 for now.
                int attempt = 0;
                boolean success = false;

                while (true) {
                    attempt++;
                    long sleep = Util.calculateExponentialDelay(attempt - 1, TimeUnit.MINUTES.toMillis(5));
                    if (sleep > 0) {
                        try {
                            Thread.sleep(sleep);
                        } catch (InterruptedException e) {
                            return; // Abort authentication if interrupted.
                        }
                    }

                    AuthenticateResponse response = authServer.authenticateRealm(
                            user.getRefreshToken(),
                            configuration.getServerUrl(),
                            user.getAuthentificationUrl()
                    );
                    if (response.isValid()) {
                        user.setAccesToken(configuration, response.getAccessToken());
                        user.setRefreshToken(response.getRefreshToken());
                        // TODO Save tokens
                        success = true;
                    } else {
                        // TODO Report bad credentials. How?
                        break;
                    }
                }

                if (success) {
                    onSuccess.run();
                } else {
                    onError.run();
                }
            }
        });
        networkRequest = new RealmAsyncTask(task, SyncManager.NETWORK_POOL_EXECUTOR);
    }



    public void refreshAccessToken(Token accessToken) {
        // TODO Refresh access token in the auth server
        user.setAccesToken(configuration, accessToken);
        nativeRefresh(nativeSessionPointer, accessToken.value());
    }

    private void notifyStarted() {

    }

    private void notifyAuthentifcationRequired() {

    }

    private void notifyUnbinded() {

    }

    private void notifyAttemptBind() {

    }

    private void notifyStopped() {

    }

    private void notifyBindAborded() {

    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (currentStateDescription != SessionState.STOPPED) {
            RealmLog.w("Session was not closed before being finalized. This is a potential resource leak.");
            stop();
        }
    }

    private native long nativeCreateSession(long nativeSyncClientPointer, String localRealmPath);
    private native void nativeBind(long nativeSessionPointer, String remoteRealmUrl, String userToken);
    private native void nativeUnbind(long nativeSessionPointer);
    private native void nativeRefresh(long nativeSessionPointer, String userToken);

    public boolean isAuthenticated(SyncConfiguration configuration) {
        Token token = user.getAccessToken(configuration);
        return token != null && token.expires() < System.currentTimeMillis();
    }

    private interface AuthentificationHandler {
        void onSuccesss(String token);
        void onError(int errrorCode, String errorMsg);
    }


    public interface EventListener {
        void started();
        void authenticatationRequired();
        void authenticationSuccess();
        void authentifcationError(int errorCode, String errorMsg);
        void attemptBind();
        void attemptBindAborted();
        void bindSuccessfull();
        void bindFailed(int errorCode, String errorMsg);
        void unbinded();
        void stopped();
    }

    public interface ErrorHandler {
        void onError(Error error, String errorMessage);
    }

    public interface EventHandler {
        void sessionCreated(Session session);
        void sessionStarted(Session session);
        void realmUnbound(Session session);
        void bindingRealm(Session session);
        void realmBound(Session session);
        void sessionStopped(Session session);
        void authenticating(Session session);
        void authorizationMissing(Session session);
        void authorizationExpired(Session session);
        void error(Error error, String errorMessage);
    }
}

