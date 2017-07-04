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

package io.realm.internal;

/**
 * Java wrapper for the Object Store Schema object.
 * <p>
 * When it is created from java binding, it is used for initializing/validating the schemas through Object Store. It
 * won't contain the column indices information.
 * <p>
 * When this is get from the Object Store {@code SharedRealm} instance, this represents the real schema of the Realm
 * file. It will contain all the schema information as well as the information about the column indices.
 */
public class OsSchemaInfo implements NativeObject {
    private long nativePtr;
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    /**
     * Construct a {@code OsSchemaInfo} object from a given {@code OsObjectSchemaInfo} list.
     *
     * @param objectSchemaInfoList all the object schemas should be contained in this {@code OsObjectSchemaInfo}.
     */
    public OsSchemaInfo(java.util.Collection<OsObjectSchemaInfo> objectSchemaInfoList) {
        long[] schemaNativePointers = new long[objectSchemaInfoList.size()];
        int i = 0;
        for (OsObjectSchemaInfo info : objectSchemaInfoList) {
            schemaNativePointers[i] = info.getNativePtr();
            i++;
        }
        this.nativePtr = nativeCreateFromList(schemaNativePointers);
        NativeContext.dummyContext.addReference(this);
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    private static native long nativeCreateFromList(long[] objectSchemaPtrs);

    private static native long nativeGetFinalizerPtr();
}
