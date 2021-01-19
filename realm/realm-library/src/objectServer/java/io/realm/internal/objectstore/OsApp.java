package io.realm.internal.objectstore;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import io.realm.internal.KeepMember;
import io.realm.internal.NativeObject;
import io.realm.internal.jni.OsJNIResultCallback;
import io.realm.internal.network.OkHttpNetworkTransport;
import io.realm.internal.network.ResultHandler;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.AppException;

public class OsApp implements NativeObject {
    private static final long nativeFinalizerPtr = nativeGetFinalizerMethodPtr();

    private OsJavaNetworkTransport networkTransport;
    private final long nativePtr;

    @Override
    public long getNativePtr() {
        return this.nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    public OsApp(AppConfiguration config, String userAgentBindingInfo, String appDefinedUserAgent, String syncDir) {
        synchronized (OsApp.class) { // We need to synchronize access as OS caches the App instance
            nativePtr = nativeCreate(
                    config.getAppId(),
                    config.getBaseUrl().toString(),
                    config.getAppName(),
                    config.getAppVersion(),
                    config.getRequestTimeoutMs(),
                    config.getEncryptionKey(),
                    syncDir,
                    userAgentBindingInfo,
                    appDefinedUserAgent,
                    "android",
                    android.os.Build.VERSION.RELEASE,
                    io.realm.BuildConfig.VERSION_NAME);
        }

        this.networkTransport = new OkHttpNetworkTransport(config.getHttpLogObfuscator());
        networkTransport.setAuthorizationHeaderName(config.getAuthorizationHeaderName());
        for (Map.Entry<String, String> entry : config.getCustomRequestHeaders().entrySet()) {
            networkTransport.addCustomRequestHeader(entry.getKey(), entry.getValue());
        }
    }

    public void setNetworkTransport(OsJavaNetworkTransport transport) {
        networkTransport = transport;
    }

    /**
     * Creates a request for a streaming function
     *
     * @param user         that requests the execution
     * @param functionName name of the function
     * @param arguments    function arguments encoded as a {@link String}
     * @param serviceName  service that will handle the function
     * @return {@link io.realm.internal.objectstore.OsJavaNetworkTransport.Request}
     */
    public OsJavaNetworkTransport.Request makeStreamingRequest(OsSyncUser user,
                                                               String functionName,
                                                               String arguments,
                                                               String serviceName) {
        return nativeMakeStreamingRequest(nativePtr, user.getNativePtr(), functionName, arguments, serviceName);
    }

    public OsSyncUser currentUser() {
        Long userPtr = nativeCurrentUser(nativePtr);
        return (userPtr != null) ? new OsSyncUser(userPtr) : null;
    }

    public OsSyncUser[] allUsers() {
        long[] nativeUsers = nativeGetAllUsers(nativePtr);
        OsSyncUser[] osSyncUsers = new OsSyncUser[nativeUsers.length];

        for (int i = 0; i < nativeUsers.length; i++) {
            osSyncUsers[i] = new OsSyncUser(nativeUsers[i]);
        }
        return osSyncUsers;
    }

    public void switchUser(OsSyncUser osUser) {
        nativeSwitchUser(nativePtr, osUser.getNativePtr());
    }

    public OsSyncUser login(OsAppCredentials credentials) {
        AtomicReference<OsSyncUser> success = new AtomicReference<>(null);
        AtomicReference<AppException> error = new AtomicReference<>(null);

        nativeLogin(nativePtr, credentials.getNativePtr(), new OsJNIResultCallback<OsSyncUser>(success, error) {
            @Override
            protected OsSyncUser mapSuccess(Object result) {
                Long nativePtr = (Long) result;
                return new OsSyncUser(nativePtr);
            }
        });

        return ResultHandler.handleResult(success, error);
    }

    // Called from JNI
    @KeepMember
    public OsJavaNetworkTransport getNetworkTransport() {
        return networkTransport;
    }

    private native long nativeCreate(String appId,
                                     String baseUrl,
                                     String appName,
                                     String appVersion,
                                     long requestTimeoutMs,
                                     byte[] encryptionKey,
                                     String syncDirPath,
                                     String bindingUserInfo,
                                     String appUserInfo,
                                     String platform,
                                     String platformVersion,
                                     String sdkVersion);

    private static native void nativeLogin(long nativeAppPtr, long nativeCredentialsPtr, OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);

    @Nullable
    private static native Long nativeCurrentUser(long nativePtr);

    private static native long[] nativeGetAllUsers(long nativePtr);

    private static native void nativeSwitchUser(long nativeAppPtr, long nativeUserPtr);

    private static native long nativeGetFinalizerMethodPtr();

    private static native OsJavaNetworkTransport.Request nativeMakeStreamingRequest(long nativeAppPtr, long nativeUserPtr, String functionName, String bsonArgs, String serviceName);
}
