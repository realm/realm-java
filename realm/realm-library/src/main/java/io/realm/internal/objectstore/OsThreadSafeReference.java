package io.realm.internal.objectstore;

import javax.annotation.Nonnull;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.internal.NativeObject;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.SharedRealm;

/**
 * Wrapper for the Object Stores ThreadSafeReferenceClass
 */

public class OsThreadSafeReference implements NativeObject {

    private final long nativePtr;
    private final int nativeObjectType;

    public OsThreadSafeReference(long osRefPtr, int objectType) {
        this.nativePtr = osRefPtr;
        this.nativeObjectType = objectType;
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return 0;
    }
}
