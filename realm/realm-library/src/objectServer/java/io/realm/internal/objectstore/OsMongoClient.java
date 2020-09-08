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

import org.bson.codecs.configuration.CodecRegistry;

import java.util.concurrent.ThreadPoolExecutor;

import io.realm.internal.NativeObject;
import io.realm.internal.network.StreamNetworkTransport;

public class OsMongoClient implements NativeObject {

    private static final long nativeFinalizerPtr = nativeGetFinalizerMethodPtr();

    private final long nativePtr;
    private final String serviceName;
    private final StreamNetworkTransport streamNetworkTransport;

    public OsMongoClient(final OsApp osApp,
                         final String serviceName,
                         final StreamNetworkTransport streamNetworkTransport) {
        this.nativePtr = nativeCreate(osApp.getNativePtr(), serviceName);
        this.serviceName = serviceName;
        this.streamNetworkTransport = streamNetworkTransport;
    }

    public OsMongoDatabase getDatabase(final String databaseName,
                                       final CodecRegistry codecRegistry) {
        long nativeDatabasePtr = nativeCreateDatabase(nativePtr, databaseName);
        return new OsMongoDatabase(nativeDatabasePtr, serviceName, codecRegistry, streamNetworkTransport);
    }

    public String getServiceName() {
        return serviceName;
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
