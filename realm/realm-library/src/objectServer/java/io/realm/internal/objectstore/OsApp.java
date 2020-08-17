package io.realm.internal.objectstore;

import io.realm.internal.NativeObject;

public class OsApp implements NativeObject {
    private static final long nativeFinalizerPtr = nativeGetFinalizerMethodPtr();

    private final long nativePtr;

    public OsApp(long nativePtr) {
        this.nativePtr = nativePtr;
    }

    @Override
    public long getNativePtr() {
        return this.nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
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

    private static native long nativeGetFinalizerMethodPtr();

    private static native OsJavaNetworkTransport.Request nativeMakeStreamingRequest(long nativeAppPtr, long nativeUserPtr, String functionName, String bsonArgs, String serviceName);
}
