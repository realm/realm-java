package io.realm.objectserver;

import android.os.Handler;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.realm.BaseRealm;
import io.realm.BuildConfig;
import io.realm.internal.Keep;
import io.realm.internal.RealmCore;
import io.realm.internal.log.RealmLog;
import io.realm.internal.objectserver.network.AuthenticationServer;
import io.realm.internal.objectserver.network.OkHttpAuthentificationServer;
import io.realm.objectserver.session.Session;

@Keep
public final class SyncManager {

    public static final String APP_ID = BuildConfig.APPLICATION_ID;
    // Thread pool used when doing network requests against the Realm Authentication Server.
    // FIXME Set proper parameters
    public static ThreadPoolExecutor NETWORK_POOL_EXECUTOR = new ThreadPoolExecutor(
            10, 10, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100));

    private static final ErrorHandler CLIENT_NO_OP_ERROR_HANDLER = new SyncManager.ErrorHandler() {
        @Override
        public void onError(ErrorCode errorCodeCode, String errorMessage) {
            switch (errorCodeCode.getCategory()) {
                case FATAL:
                    RealmLog.e(errorCodeCode.toString() + ":" + errorMessage);
                    break;
                case RECOVERABLE:
                    RealmLog.i(errorCodeCode.toString() + ":" + errorMessage);
                    break;
                case INFO:
                    RealmLog.d(errorCodeCode.toString() + ":" + errorMessage);
                    break;
            }
        }
    };
    private static final Session.ErrorHandler SESSION_NO_OP_ERROR_HANDLER = new Session.ErrorHandler() {
        @Override
        public void onError(Session session, ErrorCode errorCodeCode, String errorMessage) {
            String errorMsg = String.format("Session Error[%s]: %s : %s",
                    session.getConfiguration().getServerUrl(),
                    errorCodeCode.toString(),
                    errorMessage);
            switch (errorCodeCode.getCategory()) {
                case FATAL:
                    RealmLog.e(errorMsg);
                    break;
                case RECOVERABLE:
                    RealmLog.i(errorMsg);
                    break;
                case INFO:
                    RealmLog.d(errorMsg);
                    break;
            }
        }
    };

    // The Sync Client is lightweight, but consider creating/removing it when there is no sessions.
    // Right now it just lives and dies together with the process.
    private static long nativeSyncClientPointer;
    private static volatile AuthenticationServer authServer = new OkHttpAuthentificationServer();
    private static volatile SyncManager.ErrorHandler globalErrorHandler = CLIENT_NO_OP_ERROR_HANDLER;
    static volatile Session.ErrorHandler defaultSessionErrorHandler = SESSION_NO_OP_ERROR_HANDLER;

    // Map of between a local Realm path and any associated sessionInfo
    private static ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<String, Session>();

    static {
        RealmCore.loadLibrary();
        nativeSyncClientPointer = nativeCreateSyncClient();
    }

    /**
     * Sets the global error handler used by underlying network client. All connection errors will be reported here,
     * while all session related errors will be posted to the sessions error handler
     */
    public static void setGlobalErrorHandler(SyncManager.ErrorHandler errorHandler) {
        if (errorHandler == null) {
            globalErrorHandler = CLIENT_NO_OP_ERROR_HANDLER;
        } else {
            globalErrorHandler = errorHandler;
        }
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

    //    //
//    // OLD IMPLEMENTATION
//    //
//    public synchronized static long getSession(final String userToken, final String path, final String serverUrl) {
//        if (syncClientPointer == 0) {
//            // client event loop is not created for this token
//            // we createFrom 1 client per credentials token
//            syncClientPointer = nativeCreateSyncClient();
//        }
//
//        // check if the session is not already available for the provided RealmConfiguration
//        Long syncSessionPointer = SYNC_SESSIONS.get(path);
//        if (syncSessionPointer == null) {
//            syncSessionPointer = nativeCreateSession(syncClientPointer, path);
//        }
//
//        SYNC_SESSIONS.put(path, syncSessionPointer);
//        return syncSessionPointer;
//    }
//
//
    // Called from native code whenever a commit from sync is detected
    // TODO Remove once the Object Store is introduced.
    public static void notifyHandlers(String path) {

        for (Map.Entry<Handler, String> handlerIntegerEntry : BaseRealm.handlers.entrySet()) {
            Handler handler = handlerIntegerEntry.getKey();
            String realmPath = handlerIntegerEntry.getValue();

            // For all other threads, use the Handler
            // Note there is a race condition with handler.hasMessages() and handler.sendEmptyMessage()
            // as the target thread consumes messages at the same time. In this case it is not a problem as worst
            // case we end up with two REALM_CHANGED messages in the queue.
            if (
                    realmPath.equals(path)                           // It's the right realm
                            && !handler.hasMessages(14930352)    // HandlerController.REALM_CHANGED The right message
                            && handler.getLooper().getThread().isAlive()                // HandlerController.REALM_CHANGED The receiving thread is alive
                            && !handler.sendEmptyMessage(14930352)) {
                RealmLog.w("Cannot update Looper threads when the Looper has quit. Use realm.setAutoRefresh(false) " +
                        "to prevent this.");
            }
        }
    }

    // This is called for SyncManager.cpp from the worker thread the Sync Client is running on
    private static void notifyErrorHandler(int errorCode, String errorMessage) {
        ErrorCode error = ErrorCode.fromInt(errorCode);
        globalErrorHandler.onError(error, errorMessage);
        // FIXME Still need to test this. After that we can remove this
        throw new RuntimeException("BOOM FROM JNI:" + error + errorMessage);
    }

    private static native long nativeCreateSyncClient();

    /**
     * Interface used by Object Server Network Client to report back errors.
     *
     * @see SyncManager#setGlobalErrorHandler(ErrorHandler)
     */
    public interface ErrorHandler {
        /**
         * Callback for errors on the Realm Object Server Network Client.
         * Only errors with an ID between 100-199 will be reported here.
         *
         * @param errorCodeCode type of error.
         * @param errorMessage additional error message.
         */
        void onError(ErrorCode errorCodeCode, String errorMessage);
    }
}
