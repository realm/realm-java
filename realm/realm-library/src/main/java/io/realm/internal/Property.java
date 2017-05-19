/*
 * Copyright 2016 Realm Inc.
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


import io.realm.RealmFieldType;


/**
 * Class for handling properties/fields.
 */

public class Property {
    public static final boolean PRIMARY_KEY = true;
    public static final boolean REQUIRED = true;
    public static final boolean INDEXED = true;

    private long nativePtr;

    Property(String name, RealmFieldType type, boolean isPrimary, boolean isIndexed, boolean isRequired) {
        this.nativePtr = nativeCreateProperty(name, type.getNativeValue(), isPrimary, isIndexed, !isRequired);
    }

    Property(String name, RealmFieldType type, String linkedClassName) {
        this.nativePtr = nativeCreateProperty(name, type.getNativeValue(), linkedClassName);
    }

    protected Property(long nativePtr) {
        this.nativePtr = nativePtr;
    }

    protected long getNativePtr() {
        return nativePtr;
    }

    public void close() {
        if (nativePtr != 0) {
            nativeClose(nativePtr);
            nativePtr = 0L;
        }
    }

    private static native long nativeCreateProperty(String name, int type, boolean isPrimary, boolean isIndexed, boolean isNullable);

    private static native long nativeCreateProperty(String name, int type, String linkedToName);

    private static native void nativeClose(long nativePtr);
}
