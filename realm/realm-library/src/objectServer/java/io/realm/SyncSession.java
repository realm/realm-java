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

import java.net.URI;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.internal.Keep;
import io.realm.internal.KeepMember;
import io.realm.internal.SyncObjectServerFacade;
import io.realm.internal.async.RealmAsyncTaskImpl;
import io.realm.internal.network.AuthenticateResponse;
import io.realm.internal.network.AuthenticationServer;
import io.realm.internal.network.ExponentialBackoffTask;
import io.realm.internal.network.NetworkStateReceiver;
import io.realm.internal.objectserver.ObjectServerUser;
import io.realm.internal.objectserver.Token;
import io.realm.log.RealmLog;

/**
 * This class represents the connection to the Realm Object Server for one {@link SyncConfiguration}.
 * <p>
 * A Session is created by opening a Realm instance using that configuration. Once a session has been created,
 * it will continue to exist until the app is closed or all threads using this {@link SyncConfiguration} closes their respective {@link Realm}s.
 * <p>
 * A session is fully controlled by Realm, but can provide additional information in case of errors.
 * It is passed along in all {@link SyncSession.ErrorHandler}s.
 * <p>
 * This object is thread safe.
 */
@Keep
public class SyncSession {
    private final static ScheduledThreadPoolExecutor REFRESH_TOKENS_EXECUTOR = new ScheduledThreadPoolExecutor(1);
    private final static long REFRESH_MARGIN_DELAY = TimeUnit.SECONDS.toMillis(10);

    private final SyncConfiguration configuration;
    private final ErrorHandler errorHandler;
    private RealmAsyncTask networkRequest;
    private NetworkStateReceiver.ConnectionListener networkListener;
    private RealmAsyncTask refreshTokenTask;
    private RealmAsyncTask refreshTokenNetworkRequest;
    private AtomicBoolean onGoingAccessTokenQuery = new AtomicBoolean(false);
    private volatile boolean isClosed = false;

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
    @KeepMember
    void notifySessionError(int errorCode, String errorMessage) {
        if (errorHandler == null) {
            return;
        }
        ErrorCode errCode = ErrorCode.fromInt(errorCode);
        if (errCode == ErrorCode.CLIENT_RESET) {
            // errorMessage contains the path to the backed up file
            errorHandler.onError(this, new ClientResetRequiredError(errCode, "A Client Reset is required. " +
                    "Read more here: https://realm.io/docs/realm-object-server/#client-recovery-from-a-backup.",
                    errorMessage, getConfiguration()));
        } else {
            errorHandler.onError(this, new ObjectServerError(errCode, errorMessage));
        }
    }

    void close() {
        isClosed = true;
        if (networkRequest != null) {
            networkRequest.cancel();
        }
        clearScheduledAccessTokenRefresh();
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

    String accessToken(final AuthenticationServer authServer) {
        // check first if there's a valid access_token we can return immediately
        if (getUser().getSyncUser().isAuthenticated(configuration)) {
            Token accessToken = getUser().getSyncUser().getAccessToken(configuration.getServerUrl());
            // start refreshing this token if a refresh is not going on
            if (!onGoingAccessTokenQuery.getAndSet(true)) {
                scheduleRefreshAccessToken(authServer, accessToken.expiresMs());
            }
            return accessToken.value();

        } else {
            if (!onGoingAccessTokenQuery.getAndSet(true)) {
                if (NetworkStateReceiver.isOnline(SyncObjectServerFacade.getApplicationContext())) {
                    authenticateRealm(authServer);

                } else {
                    // Wait for connection to become available, before trying again.
                    // The Session might potentially stay in this state for the lifetime of the application.
                    // This is acceptable.
                    networkListener = new NetworkStateReceiver.ConnectionListener() {
                        @Override
                        public void onChange(boolean connectionAvailable) {
                            if (connectionAvailable) {
                                if (!onGoingAccessTokenQuery.getAndSet(true)) {
                                    authenticateRealm(authServer);
                                }
                                NetworkStateReceiver.removeListener(this);
                            }
                        }
                    };
                    NetworkStateReceiver.addListener(networkListener);
                }
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

        // Authenticate in a background thread. This allows incremental backoff and retries in a safe manner.
        Future<?> task = SyncManager.NETWORK_POOL_EXECUTOR.submit(new ExponentialBackoffTask<AuthenticateResponse>() {
            @Override
            protected AuthenticateResponse execute() {
                if (!isClosed && !Thread.currentThread().isInterrupted()) {
                    return authServer.loginToRealm(
                            getUser().getAccessToken(),//refresh token in fact
                            configuration.getServerUrl(),
                            getUser().getSyncUser().getAuthenticationUrl()
                    );
                }
                return null;
            }

            @Override
            protected void onSuccess(AuthenticateResponse response) {
                RealmLog.debug("Session[%s]: Access token acquired", configuration.getPath());
                if (!isClosed && !Thread.currentThread().isInterrupted()) {
                    ObjectServerUser.AccessDescription desc = new ObjectServerUser.AccessDescription(
                            response.getAccessToken(),
                            configuration.getPath(),
                            configuration.shouldDeleteRealmOnLogout()
                    );
                    getUser().getSyncUser().addRealm(configuration.getServerUrl(), desc);
                    // schedule a token refresh before it expires
                    if (nativeRefreshAccessToken(configuration.getPath(), getUser().getSyncUser().getAccessToken(configuration.getServerUrl()).value(), configuration.getServerUrl().toString())) {
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
                RealmLog.debug("Session[%s]: Failed to get access token (%d)", configuration.getPath(), response.getError().getErrorCode());
                if (!isClosed && !Thread.currentThread().isInterrupted()) {
                    errorHandler.onError(SyncSession.this, response.getError());
                }
            }
        });
        networkRequest = new RealmAsyncTaskImpl(task, SyncManager.NETWORK_POOL_EXECUTOR);
    }

    private void scheduleRefreshAccessToken(final AuthenticationServer authServer, long expireDateInMs) {
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
                    if (!isClosed && !Thread.currentThread().isInterrupted()) {
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
                    return authServer.refreshUser(getUser().getSyncUser().getUserToken(), configuration.getServerUrl(), getUser().getSyncUser().getAuthenticationUrl());
                }
                return null;
            }

            @Override
            protected void onSuccess(AuthenticateResponse response) {
                synchronized (SyncSession.this) {
                    if (!isClosed && !Thread.currentThread().isInterrupted()) {
                        RealmLog.debug("Access Token refreshed successfully, Sync URL: " + configuration.getServerUrl());
                        if (nativeRefreshAccessToken(configuration.getPath(), response.getAccessToken().value(), configuration.getUser().getAuthenticationUrl().toString())) {
                            // replaced the user old access_token
                            ObjectServerUser.AccessDescription desc = new ObjectServerUser.AccessDescription(
                                    response.getAccessToken(),
                                    configuration.getPath(),
                                    configuration.shouldDeleteRealmOnLogout()
                            );
                            getUser().getSyncUser().addRealm(configuration.getServerUrl(), desc);

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

    private void clearScheduledAccessTokenRefresh() {
        if (refreshTokenTask != null) {
            refreshTokenTask.cancel();
        }
        if (refreshTokenNetworkRequest != null) {
            refreshTokenNetworkRequest.cancel();
        }
    }

    private static native boolean nativeRefreshAccessToken(String path, String accessToken, String authURL);
}

