package io.realm.internal.objectstore;

import java.util.Date;

import io.realm.internal.NativeObject;
import io.realm.mongodb.sync.Subscription;

public class OsSubscription implements NativeObject, Subscription {

    private static final long nativeFinalizerPtr = nativeGetFinalizerMethodPtr();
    private final long nativePtr;

    public OsSubscription(long nativePtr) {
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

    @Override
    public Date getCreatedAt() {
        return new Date(nativeCreatedAt(nativePtr));
    }

    @Override
    public Date getUpdatedAt() {
        return new Date(nativeUpdatedAt(nativePtr));
    }

    @Override
    public String getName() {
        return nativeName(nativePtr);
    }

    @Override
    public String getObjectType() {
        return nativeObjectClassName(nativePtr);
    }

    @Override
    public String getQuery() {
        return nativeQueryString(nativePtr);
    }

    private static native long nativeGetFinalizerMethodPtr();
    private static native String nativeName(long nativePtr);
    private static native String nativeObjectClassName(long nativePtr);
    private static native String nativeQueryString(long nativePtr);
    private static native long nativeCreatedAt(long nativePtr);
    private static native long nativeUpdatedAt(long nativePtr);

//    // Returns the unique ID for this subscription.
//    ObjectId id() const;
//
//    // Returns the timestamp of when this subscription was originally created.
//    Timestamp created_at() const;
//
//    // Returns the timestamp of the last time this subscription was updated by calling update_query.
//    Timestamp updated_at() const;
}
