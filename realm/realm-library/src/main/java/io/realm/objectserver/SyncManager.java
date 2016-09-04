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

package io.realm.objectserver;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.realm.BaseRealm;
import io.realm.internal.Keep;
import io.realm.internal.RealmCore;
import io.realm.internal.objectserver.network.AuthenticationServer;
import io.realm.internal.objectserver.network.OkHttpAuthenticationServer;
import io.realm.log.RealmLog;
import io.realm.objectserver.session.Session;

@Keep
public final class SyncManager {

    public static final String APP_ID = "foo"; // FIXME Find a way to get an application ID
    // Thread pool used when doing network requests against the Realm Authentication Server.
    // FIXME Set proper parameters
    public static ThreadPoolExecutor NETWORK_POOL_EXECUTOR = new ThreadPoolExecutor(
            10, 10, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100));

    private static final Session.ErrorHandler SESSION_NO_OP_ERROR_HANDLER = new Session.ErrorHandler() {
        @Override
        public void onError(Session session, ObjectServerError error) {
            String errorMsg = String.format("Session Error[%s]: %s",
                    session.getConfiguration().getServerUrl(),
                    error.toString());
            switch (error.errorCode().getCategory()) {
                case FATAL:
                    RealmLog.error(errorMsg);
                    break;
                case RECOVERABLE:
                    RealmLog.info(errorMsg);
                    break;
                case INFO:
                    RealmLog.debug(errorMsg);
                    break;
            }
        }
    };

    // The Sync Client is lightweight, but consider creating/removing it when there is no sessions.
    // Right now it just lives and dies together with the process.
    private static long nativeSyncClientPointer;
    private static volatile AuthenticationServer authServer = new OkHttpAuthenticationServer();
    static volatile Session.ErrorHandler defaultSessionErrorHandler = SESSION_NO_OP_ERROR_HANDLER;

    // Map of between a local Realm path and any associated sessionInfo
    private static ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<String, Session>();

    static {
        RealmCore.loadLibrary();
        nativeSyncClientPointer = nativeCreateSyncClient();
    }

    /**
     * Sets the default error handler used by all {@link SyncConfiguration} objects when they are created.
     *
     * @param errorHandler the default error handler used when interacting with a Realm managed by a Realm Object Server.
     */
    public static void setDefaultSessionErrorHandler(Session.ErrorHandler errorHandler) {
        if (errorHandler == null) {
            defaultSessionErrorHandler = SESSION_NO_OP_ERROR_HANDLER;
        } else {
            defaultSessionErrorHandler = errorHandler;
        }
    }

    /**
     * Convenience method for creating an {@link Session} using {@link #getSession(SyncConfiguration)} and
     * calling {@link Session#start()} on it.
     */
    public static Session connect(SyncConfiguration configuration) {
        // Get any cached session or create a new one if needed.
        Session session = getSession(configuration);
        session.start();
        return session;
    }

    /**
     * Gets any cached {@link Session} for the given {@link SyncConfiguration} or create a new one if
     * no one exists.
     *
     * @param syncConfiguration configuration object for the synchronized Realm.
     * @return the {@link Session} for the specified Realm.
     */
    public static Session getSession(SyncConfiguration syncConfiguration) {
        if (syncConfiguration == null) {
            throw new IllegalArgumentException("A non-empty 'syncConfiguration' is required.");
        }

        String localPath = syncConfiguration.getPath();
        Session session = sessions.get(localPath);
        if (session == null) {
            session = new Session(syncConfiguration, nativeSyncClientPointer, authServer);
            syncConfiguration.getSyncPolicy().onSessionCreated(session);
            sessions.put(localPath, session);
        }

        return session;
    }

    public static AuthenticationServer getAuthServer() {
        return authServer;
    }

    /**
     * Remove a session once it has been closed
     *
     * @param info
     */
    static void removeSession(Session info) {
        if (info == null) {
            return;
        }

        Iterator<Map.Entry<String, Session>> it = sessions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Session> entry = it.next();
            if (entry.getValue().equals(info)) {
                it.remove();
                break;
            }
        }
    }

    /**
     * TODO Internal only? Developers can also use this to inject stubs.
     * TODO Find a better method name.
     * <p>
     * Sets the auth server implementation used when validating credentials.
     */
    static void setAuthServerImpl(AuthenticationServer authServerImpl) {
        authServer = authServerImpl;
    }

    // This is called from SyncManager.cpp from the worker thread the Sync Client is running on
    // Right now Core doesn't send these errors to the proper session, so instead we need to notify all sessions
    // from here. This can be removed once better error propagation is implemented in Sync Core.
    private static void notifyErrorHandler(int errorCode, String errorMessage) {
        ObjectServerError error = new ObjectServerError(ErrorCode.fromInt(errorCode), errorMessage);
        for(Session session : sessions.values()) {
            session.onError(error);
        }
    }

    private static native long nativeCreateSyncClient();
}
