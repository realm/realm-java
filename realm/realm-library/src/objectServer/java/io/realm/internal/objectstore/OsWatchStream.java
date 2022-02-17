package io.realm.internal.objectstore;

import org.bson.BsonDocument;
import org.bson.codecs.configuration.CodecRegistry;

import io.realm.internal.NativeObject;
import io.realm.internal.jni.JniBsonProtocol;
import io.realm.mongodb.AppException;

public class OsWatchStream implements NativeObject {
    public static final String NEED_DATA = "NEED_DATA";
    public static final String HAVE_EVENT = "HAVE_EVENT";
    public static final String HAVE_ERROR = "HAVE_ERROR";

    private final long nativePtr;
    private final CodecRegistry codecRegistry;

    public OsWatchStream(CodecRegistry codecRegistry) {
        this.codecRegistry = codecRegistry;
        this.nativePtr = nativeCreateWatchStream();
    }

    @Override
    public long getNativePtr() {
        return this.nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeGetFinalizerMethodPtr();
    }

    public BsonDocument getNextEvent() {
        String bsonEvent = nativeGetNextEvent(nativePtr);
        return JniBsonProtocol.decode(bsonEvent, codecRegistry.get(BsonDocument.class));
    }

    public String getState() {
        return nativeGetState(nativePtr);
    }

    public AppException getError() {
        return nativeGetError(nativePtr);
    }

    public void feedLine(String line) {
        nativeFeedLine(nativePtr, line);
    }

    private static native long nativeGetFinalizerMethodPtr();
    private static native long nativeCreateWatchStream();
    private static native void nativeFeedLine(long nativePtr, String line);
    private static native String nativeGetState(long nativePtr);
    private static native String nativeGetNextEvent(long nativePtr);
    private static native AppException nativeGetError(long nativePtr);
}
