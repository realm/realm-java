/*
 * Copyright 2022 Realm Inc.
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
package io.realm.internal.objectstore;

import java.util.Date;

import io.realm.internal.NativeObject;
import io.realm.internal.annotations.ObjectServer;
import io.realm.mongodb.sync.Subscription;

// TODO Adding @ObjectServer here seems to break the Realm Build Transformer. Investigate why.
//@ObjectServer
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
}
