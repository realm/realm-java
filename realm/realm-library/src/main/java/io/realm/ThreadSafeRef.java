/*
 * Copyright 2017 Realm Inc.
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

import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.internal.NativeContext;
import io.realm.internal.NativeObject;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.internal.SharedRealm;
import io.realm.internal.UncheckedRow;


public class ThreadSafeRef<T extends RealmObject> implements NativeObject {
    private static final NativeContext context = new NativeContext();

    public static <T1 extends RealmObject> ThreadSafeRef<T1> createThreadSafeRef(Realm realm, Class<T1> clazz, T1 obj) {
        ThreadSafeRef<T1> ref = new ThreadSafeRef<>(realm, clazz, obj);
        context.addReference(ref); // this cannot be in the constructor.
        return ref;
    }

    private final AtomicBoolean dead = new AtomicBoolean(false);
    private final Class<T> clazz;
    private final long nativePtr;
    private final long nativeFinalizerPtr;

    private ThreadSafeRef(Realm realm, Class<T> clazz, T obj) {
        this.clazz = clazz;

        SharedRealm sharedRealm = getSharedRealm(realm);

        if (!(obj instanceof RealmObjectProxy)) {
            throw new IllegalArgumentException("ThreadSafeRefs can be used only on live RealmModel objects");
        }
        RealmObjectProxy proxyObj = (RealmObjectProxy) obj;

        Row row = proxyObj.realmGet$proxyState().getRow$realm();
        if (!(row instanceof UncheckedRow)) {
            throw new IllegalArgumentException("??? WTF?");
        }
        UncheckedRow uncheckedRow = (UncheckedRow) row;

        // What to pass here?  A row is almost, but not quite, right.
        this.nativePtr = nativeGetThreadSafeRef(sharedRealm.getNativePtr(), uncheckedRow.getNativePtr());

        this.nativeFinalizerPtr = nativeGetFinalizerPtr();
    }

    @Override
    public long getNativeFinalizerPtr() {
        if (dead.get()) {
            throw new IllegalStateException("This ref is dead.");
        }
        return nativeFinalizerPtr;
    }

    @Override
    public long getNativePtr() {
        if (dead.get()) {
            throw new IllegalStateException("This ref is dead.");
        }
        return nativePtr;
    }

    /**
     * Get the referenced object.
     *
     * @param realm The thread-local realm
     * @return the object to which this reference refers.
     */
    public T resolve(Realm realm) {
        SharedRealm sharedRealm = realm.sharedRealm;

        long ptr = getNativePtr();

        // What should this return?  Is a native row pointer the right thing?
        long row = nativeResolveThreadSafeRef(sharedRealm.getNativePtr(), ptr);

        // it would be nice if we could remove this thing from the native pool, right now

        return realm.get(clazz, clazz.getSimpleName(), getUncheckedRow(row));
    }

    // What to do here?  Is a row pointer even what we want?
    private UncheckedRow getUncheckedRow(long rowNativePtr) {
        return null;
    }

    private SharedRealm getSharedRealm(Realm realm) {
        SharedRealm sharedRealm = realm.sharedRealm;
        if (sharedRealm == null) {
            throw new IllegalStateException("ThreadSafeRefs only work with shared realms.");
        }
        return sharedRealm;
    }

    private static native long nativeGetThreadSafeRef(long realmPtr, long rowPtr);

    private static native long nativeResolveThreadSafeRef(long realmPtr, long rowPtr);

    private static native long nativeGetFinalizerPtr();
}
