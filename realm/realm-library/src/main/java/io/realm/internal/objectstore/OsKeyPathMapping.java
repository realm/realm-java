/*
 * Copyright 2021 Realm Inc.
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

import io.realm.RealmSchema;
import io.realm.exceptions.RealmError;
import io.realm.internal.NativeContext;
import io.realm.internal.NativeObject;

/**
 * Wrapper for ObjectStore's KeyPathMapping class.
 * <p>
 * The primary use case for this  class is to make sure that we release native memory for any old
 * mappings when the schema is updated.
 * <p>
 * It will use the already constructed ObjectStore schema to create the mappings required by
 * the Query Parser.
 *
 * @see RealmSchema#refresh()
 */
public class OsKeyPathMapping implements NativeObject {

    private static final long nativeFinalizerPtr = nativeGetFinalizerMethodPtr();
    public long mappingPointer = -1;

    public OsKeyPathMapping(long sharedRealmNativePointer) {
        mappingPointer = nativeCreateMapping(sharedRealmNativePointer);
        NativeContext.dummyContext.addReference(this);
    }

    @Override
    public long getNativePtr() {
        return mappingPointer;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    private static native long nativeGetFinalizerMethodPtr();
    private static native long nativeCreateMapping(long sharedRealmNativePointer);
}
