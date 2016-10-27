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

package io.realm.internal.objectserver;

import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.Future;

import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.RealmAsyncTask;
import io.realm.SyncSession;
import io.realm.SessionState;
import io.realm.SyncConfiguration;
import io.realm.SyncManager;
import io.realm.SyncUser;
import io.realm.internal.KeepMember;
import io.realm.internal.async.RealmAsyncTaskImpl;
import io.realm.internal.network.AuthenticateResponse;
import io.realm.internal.network.AuthenticationServer;
import io.realm.internal.network.ExponentialBackoffTask;
import io.realm.internal.network.NetworkStateReceiver;
import io.realm.internal.syncpolicy.SyncPolicy;
import io.realm.log.RealmLog;

/**
 * Internal class describing a Realm Object Server Session.
 * There is currently a split between the public {@link SyncSession} and this class.
 * This class is intended as a wrapper for Object Store's Sync Session, but it is not that yet.
 * <p>
 * A Session is created by either calling {@link SyncManager#getSession(SyncConfiguration)} or by opening
 * a Realm instance. Once a session has been created, it will continue to exist until explicitly closed or the
 * underlying Realm file is deleted.
 * <p>
 * It is typically not necessary to interact directly with a session. The interaction should be done by the {@code SyncPolicy}
 * defined using {@code io.realm.SyncConfiguration.Builder#syncPolicy(SyncPolicy)}.
 * <p>
 * A session has a lifecycle consisting of the following states:
 * <p>
 * <dl>
 * <li>
 *     <b>INITIAL</b> Initial state when creating the Session object. No connections to the object server have been
 *     created yet. At this point it is possible to register any relevant error and event listeners. Calling
 *     {@link #start()} will cause the session to become <b>UNBOUND</b> and notify the {@code SyncPolicy} that the
 *     session is ready by calling {@code SyncPolicy#onSessionCreated(Session)}.
 * </li>
 * <li>
 *     <b>UNBOUND</b> When a session is unbound, no synchronization between the local and remote Realm is taking place.
 *     Call {@link #bind()} to start synchronizing changes.
 * </li>
 * <li>
 *     <b>BINDING</b> A session is in the process of binding a local Realm to a remote one. Calling {@link #unbind()}
 *     at this stage, will cancel the process. If binding fails, the session will revert to being INBOUND and an error
 *     will be reported to the error handler.
 * </li>
 * <li>
 *     <b>AUTHENTICATING</b> During binding, if a users access has expired, the session will be <b>AUTHENTICATING</b>.
 *     During this state, Realm will automatically try to acquire new valid credentials. If it succeed <b>BINDING</b>
 *     will automatically be resumed, if not, the session will become <b>UNBOUND</b> or <b>STOPPED</b> and an
 *     appropriate error reported.
 * </li>
 * <li>
 *     <b>BOUND</b> A bound session has an active connection to the remote Realm and will synchronize any changes
 *     immediately.
 * </li>
 * <li>
 *     <b>STOPPED</b> The session are in an unrecoverable state. Check the error log for additional information, but
 *     the type of errors is usually wrong credentials for the Realm being accessed or a mismatching Object Server.
 *     Most problems can be solved by creating a new {@link SyncConfiguration} with a new {@code serverUrl} and
 *     {@code user}.
 * </li>
 * </dl>
 *
 * This object is thread safe.
 */
@KeepMember
public final class ObjectServerSession {

    private final HashMap<SessionState, FsmState> FSM = new HashMap<SessionState, FsmState>();

    // Variables used by the FSM
    final SyncConfiguration configuration;
    private final AuthenticationServer authServer;
    private final SyncSession.ErrorHandler errorHandler;
    private long nativeSessionPointer;
    private final ObjectServerUser user;
    RealmAsyncTask networkRequest;
    NetworkStateReceiver.ConnectionListener networkListener;
    private SyncPolicy syncPolicy;

    // Keeping track of current FSM state
    private SessionState currentStateDescription;
    private FsmState currentState;
    private SyncSession userSession;
    private SyncSession publicSession;

    /**
     * Creates a new Object Server Session.
     *
     * @param syncConfiguration Sync configuration defining this session
     * @param authServer Authentication server used to refresh credentials if needed
     * @param policy Sync Policy to use by this Session.
     */
    public ObjectServerSession(SyncConfiguration syncConfiguration,
                               AuthenticationServer authServer,
                               ObjectServerUser user,
                               SyncPolicy policy,
                               SyncSession.ErrorHandler errorHandler) {
        this.configuration = syncConfiguration;
        this.user = user;
        this.authServer = authServer;
        this.errorHandler = errorHandler;
        this.syncPolicy = policy;
        setupStateMachine();
    }

    private void setupStateMachine() {
        FSM.put(SessionState.INITIAL, new InitialState());
        FSM.put(SessionState.UNBOUND, new UnboundState());
        FSM.put(SessionState.BINDING, new BindingState());
        FSM.put(SessionState.AUTHENTICATING, new AuthenticatingState());
        FSM.put(SessionState.BOUND, new BoundState());
        FSM.put(SessionState.STOPPED, new StoppedState());
        RealmLog.debug("Session started: " + configuration.getServerUrl());
        currentState = FSM.get(SessionState.INITIAL);
        currentState.entry(this);
    }

    // Goto the next state. The FsmState classes are responsible for calling this method as a reaction to a FsmAction
    // being called or an internal action triggering a state transition.
    void nextState(SessionState nextStateDescription) {
        currentState.exit();
        FsmState nextState = FSM.get(nextStateDescription);
        if (nextState == null) {
            throw new IllegalStateException("No state was configured to handle: " + nextStateDescription);
        }
        RealmLog.debug("Session[%s]: %s -> %s", configuration.getServerUrl(), currentStateDescription, nextStateDescription);
        currentStateDescription = nextStateDescription;
        currentState = nextState;
        nextState.entry(this);
    }

    /**
     * Starts the session. This will cause the session to come <b>UNBOUND</b>. {@link #bind()} must be called to
     * actually start synchronizing data.
     */
    public synchronized void start() {
        currentState.onStart();
    }

    /**
     * Stops the session. The session can no longer be used.
     */
    public synchronized void stop() {
        currentState.onStop();
    }

    /**
     * Binds the local Realm to the remote Realm. Once bound, changes to either the local or Remote Realm will be
     * synchronized immediately.
     * <p>
     * While this method will return immediately, binding a Realm is not guaranteed to succeed. Possible reasons for
     * failure could be if the device is offline or credentials have expired. Binding is an asynchronous
     * operation and all errors will be sent first to {@code SyncPolicy#onError(Session, ObjectServerError)} and if the
     * SyncPolicy doesn't handle it, to the {@link SyncSession.ErrorHandler} defined by
     * {@link SyncConfiguration.Builder#errorHandler(SyncSession.ErrorHandler)}.
     */
    public synchronized void bind() {
        currentState.onBind();
    }

    /**
     * Stops a local Realm from synchronizing changes with the remote Realm.
     * <p>
     * It is possible to call {@link #bind()} again after a Realm has been unbound.
     */
    public synchronized void unbind() {
        currentState.onUnbind();
    }

    /**
     * Notify the session that an error has occurred.
     *
     * @param error the kind of err
     */
    public synchronized void onError(ObjectServerError error) {
        currentState.onError(error); // FSM needs to respond to the error first, before notifying the User
        if (errorHandler != null) {
            errorHandler.onError(getUserSession(), error);
        }
    }

    // Called from JniSession in native code.
    // This callback will happen on the thread running the Sync Client.
    @SuppressWarnings("unused")
    @KeepMember
    private void notifySessionError(int errorCode, String errorMessage) {
        ObjectServerError error = new ObjectServerError(ErrorCode.fromInt(errorCode), errorMessage);
        onError(error);
    }

    /**
     * Checks if the local Realm is bound to the remote Realm and can synchronize any changes happening on either
     * sides.
     *
     * @return {@code true} if the local Realm is bound to the remote Realm, {@code false} otherwise.
     */
    boolean isBound() {
        return currentStateDescription == SessionState.BOUND;
    }

    //
    // Package protected methods used by the FSM states to manipulate session variables.
    //

    // Create a native session. The session abstraction in Realm Core doesn't support multiple calls to bind()/unbind()
    // yet, so the Java SyncSession must manually create/and close the native sessions as needed.
    void createNativeSession() {
        nativeSessionPointer = nativeCreateSession(configuration.getPath());
    }

    void stopNativeSession() {
        if (nativeSessionPointer != 0) {
            nativeUnbind(nativeSessionPointer);
            nativeSessionPointer = 0;
        }
    }

    // Bind with proper access tokens
    // Access tokens are presumed to be present and valid at this point
    void bindWithTokens() {
        Token accessToken = user.getAccessToken(configuration.getServerUrl());
        if (accessToken == null) {
            throw new IllegalStateException("User '" + user.toString() + "' does not have an access token for "
                    + configuration.getServerUrl());
        }
        nativeBind(nativeSessionPointer, configuration.getServerUrl().toString(), accessToken.value());
    }

    // Authenticate by getting access tokens for the specific Realm
    void authenticateRealm(final Runnable onSuccess, final SyncSession.ErrorHandler errorHandler) {
        if (networkRequest != null) {
            networkRequest.cancel();
        }
        // Authenticate in a background thread. This allows incremental backoff and retries in a safe manner.
        Future<?> task = SyncManager.NETWORK_POOL_EXECUTOR.submit(new ExponentialBackoffTask<AuthenticateResponse>() {
            @Override
            protected AuthenticateResponse execute() {
                return authServer.loginToRealm(
                        user.getUserToken(),
                        configuration.getServerUrl(),
                        user.getAuthenticationUrl()
                );
            }

            @Override
            protected void onSuccess(AuthenticateResponse response) {
                ObjectServerUser.AccessDescription desc = new ObjectServerUser.AccessDescription(
                        response.getAccessToken(),
                        configuration.getPath(),
                        configuration.shouldDeleteRealmOnLogout()
                );
                user.addRealm(configuration.getServerUrl(), desc);
                onSuccess.run();
            }

            @Override
            protected void onError(AuthenticateResponse response) {
                errorHandler.onError(getUserSession(), response.getError());
            }
        });
        networkRequest = new RealmAsyncTaskImpl(task, SyncManager.NETWORK_POOL_EXECUTOR);
    }

    /**
     * Checks if a user has valid credentials for accessing this Realm.
     *
     * @param configuration the configuration.
     * @return {@code true} if credentials are valid, {@code false} otherwise.
     */
    boolean isAuthenticated(SyncConfiguration configuration) {
        return user.isAuthenticated(configuration);
    }

    /**
     * Returns the {@link SyncConfiguration} that is responsible for controlling this session.
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
     * Returns the {@link URI} describing the remote Realm this session connects to and synchronizes changes with.
     *
     * @return {@link URI} describing the remote Realm.
     */
    public URI getServerUrl() {
        return configuration.getServerUrl();
    }

    /**
     * Returns the state of this session.
     *
     * @return The current {@link SessionState} for this session.
     */
    public SessionState getState() {
        return currentStateDescription;
    }

    /**
     * Notify session that a commit on the device has happened.
     *
     * @param version the commit number/version.
     */
    public void notifyCommit(long version) {
        if (isBound()) {
            nativeNotifyCommitHappened(nativeSessionPointer, version);
        }
    }

    public SyncPolicy getSyncPolicy() {
        return syncPolicy;
    }

    public SyncSession getUserSession() {
        return userSession;
    }

    public void setUserSession(SyncSession userSession) {
        this.userSession = userSession;
    }

    private native long nativeCreateSession(String localRealmPath);
    private native void nativeBind(long nativeSessionPointer, String remoteRealmUrl, String userToken);
    private native void nativeUnbind(long nativeSessionPointer);
    private native void nativeRefresh(long nativeSessionPointer, String userToken);
    private native void nativeNotifyCommitHappened(long sessionPointer, long version);
}

