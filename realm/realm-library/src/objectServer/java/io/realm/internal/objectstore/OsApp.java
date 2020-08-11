package io.realm.internal.objectstore;

import org.bson.BsonArray;

import io.realm.internal.NativeObject;
import io.realm.internal.jni.JniBsonProtocol;
import io.realm.mongodb.AppConfiguration;

public class OsApp implements NativeObject {
    private static final long nativeFinalizerPtr = nativeGetFinalizerMethodPtr();

    private final long nativePtr;
    private final AppConfiguration config;

    public OsApp(long nativePtr, AppConfiguration config) {
        this.nativePtr = nativePtr;
        this.config = config;
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
     * @param bsonArgs     function arguments as a {@link BsonArray}
     * @param serviceName  service that will handle the function
     * @return {@link io.realm.internal.objectstore.OsJavaNetworkTransport.Request}
     */
    public OsJavaNetworkTransport.Request makeStreamingRequest(OsSyncUser user,
                                                               String functionName,
                                                               BsonArray bsonArgs,
                                                               String serviceName) {
        final String encodedArguments = JniBsonProtocol.encode(bsonArgs, config.getDefaultCodecRegistry());
        return nativeMakeStreamingRequest(nativePtr, user.getNativePtr(), functionName, encodedArguments, serviceName);
    }

    private static native long nativeGetFinalizerMethodPtr();

    private static native OsJavaNetworkTransport.Request nativeMakeStreamingRequest(long nativeAppPtr, long nativeUserPtr, String functionName, String bsonArgs, String serviceName);
}
