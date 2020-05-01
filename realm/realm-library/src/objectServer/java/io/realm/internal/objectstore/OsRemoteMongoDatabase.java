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

import io.realm.internal.NativeObject;

/**
 * TODO
 */
public class OsRemoteMongoDatabase implements NativeObject {

    private static final long nativeFinalizerPtr = nativeGetFinalizerMethodPtr();

    private final long nativePtr;

    public OsRemoteMongoDatabase(long nativeDatabasePtr) {
        this.nativePtr = nativeDatabasePtr;
    }

    public OsRemoteMongoCollection getCollection(String collectionName) {
        long nativeCollectionPtr = nativeGetCollection(nativePtr, collectionName);
        return new OsRemoteMongoCollection(nativeCollectionPtr);
    }

    // FIXME: what about this one?
//    public <DocumentT> RemoteMongoCollection<DocumentT> getCollection(
//            final String collectionName,
//            final Class<DocumentT> documentClass
//    ) {
//        throw new RuntimeException("Not implemented");
//    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    private static native long nativeGetCollection(long nativeDatabasePtr, String collectionName);
    private static native long nativeGetFinalizerMethodPtr();
}
