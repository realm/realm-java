package io.realm.objectserver;

import android.os.Handler;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.realm.BaseRealm;
import io.realm.internal.log.RealmLog;
import io.realm.internal.objectserver.network.AuthentificationServer;
import io.realm.internal.objectserver.network.OkHttpAuthentificationServer;
import io.realm.objectserver.session.Session;

public final class SyncManager {

    private static volatile long syncClientPointer = 0;
    private final static Map<String, Long> SYNC_SESSIONS = new HashMap<String, Long>();

    // The Sync Client is lightweight, but consider creating/removing it when there is no sessions.
    // Right now it just lives and dies together with the process.
    private static volatile long nativeSyncClientPointer = nativeCreateSyncClient();
    private static volatile AuthentificationServer authServer = new OkHttpAuthentificationServer();
    private static volatile SyncErrorHandler globalErrorHandler;
    private static volatile SyncEventHandler globalEventHandler;

    private static URL globalAuthentificationServer;

    // Map of between a local Realm path and any associated sessionInfo
    private static ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<String, Session>();

    /**
     * Sets the default global error handler used by all {@link SyncConfiguration} objects when they are created.
     * * @param errorHandler the default error handler used when communicating with a Realm Object Server.
     */
    public static void setGlobalErrorHandler(SyncErrorHandler errorHandler) {
        globalErrorHandler = errorHandler;
    }

    /**
     * Sets the global default Realm Mobile Platform authentification server. Setting this parameter means this server
     * will be used as the default server for any {@link Credentials} created.
     *
     * @param authServer the default authentification server.
     */
    public static void setGlobalAuthentificationServer(URL authServer) {

    }

    public static void setGlobalSessionErrorHandler(Session.ErrorHandler handler) {

    }

    public static void setGlobalSessionEventHandler(Session.EventHandler handler) {

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
     * @param objectServerConfiguration configuration object for the Realm that s
     * @return the {@link Session} for the specified Realm.
     */
    public static Session getSession(SyncConfiguration objectServerConfiguration) {
        if (objectServerConfiguration == null) {
            throw new IllegalArgumentException("A non-empty 'objectServerConfiguration' is required.");
        }

        String localPath = objectServerConfiguration.getPath();
        Session session = sessions.get(localPath);
        if (session == null) {
            session = new Session(objectServerConfiguration, nativeSyncClientPointer, authServer);
            sessions.put(localPath, session);
        }

        return session;
    }

    /**
     * Remove a session once it has been closed
     * @param info
     */
    static void removeSession(Session info) {
        if (info == null) {
            return;
        }

        Iterator<Map.Entry<String, Session>> it = sessions.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, Session> entry = it.next();
            if (entry.getValue().equals(info)) {
                it.remove();
                break;
            }
        }
    }

    public static URL getGlobalAuthentificationServer() {
        return globalAuthentificationServer;
    }

    /**
     * TODO Internal only? Developers can also use this to inject stubs.
     * TODO Find a better method name.
     *
     * Sets the auth server implementation used when validating credentials.
     */
    static void setAuthServerImpl(AuthentificationServer authServerImpl) {
        authServer = authServerImpl;
    }

    //
    // OLD IMPLEMENTATION
    //
    public synchronized static long getSession(final String userToken, final String path, final String serverUrl) {
        if (syncClientPointer == 0) {
            // client event loop is not created for this token
            // we create 1 client per credentials token
            syncClientPointer = nativeCreateSyncClient();
        }

        // check if the session is not already available for the provided RealmConfiguration
        Long syncSessionPointer = SYNC_SESSIONS.get(path);
        if (syncSessionPointer == null) {
            syncSessionPointer = nativeCreateSession(syncClientPointer, path);
        }

        SYNC_SESSIONS.put(path, syncSessionPointer);
        return syncSessionPointer;
    }


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

    private static native long nativeCreateSyncClient();
    private static native long nativeCreateSession(long clientPointer, String path);

    public static void downloadRealm(SyncConfiguration syncConfig, ResultCallback resultCallback) {

    }

    public interface ErrorHandler {
        void onError(int error, String errorMessage);
        void onFatalError(Exception e);
    }

    public interface EventHandler {
        void userAccepted();
        void sessionStarted();
        void realmBound();
        void realmUnbound();
        void sessionStopped();
    }

    public interface UserHandler {

    }

    public interface ResultCallback {
        void onSuccess(SyncConfiguration config);

        void onError(SyncConfiguration config, Exception e);
    }
}
