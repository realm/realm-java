/*
 * Copyright 2020 Realm Inc.
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

import io.realm.RealmUser;
import io.realm.internal.NativeObject;

public class OsRemoteMongoClient implements NativeObject {

    private static final long nativeFinalizerPtr = nativeGetFinalizerMethodPtr();

    private final long nativePtr;

    public OsRemoteMongoClient(RealmUser realmUser, String serviceName) {
        this.nativePtr = nativeCreate(realmUser.getApp().nativePtr, serviceName);
    }

    public OsRemoteMongoDatabase getRemoteDatabase(String databaseName) {
        long nativeDatabasePtr = nativeCreateDatabase(nativePtr, databaseName);
        return new OsRemoteMongoDatabase(nativeDatabasePtr);
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    private static native long nativeCreate(long nativeAppPtr, String serviceName);
    private static native long nativeCreateDatabase(long nativeAppPtr, String databaseName);
    private static native long nativeGetFinalizerMethodPtr();
}
