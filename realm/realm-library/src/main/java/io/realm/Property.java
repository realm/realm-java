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

package io.realm;


/**
 * Class for handling properties/fields.
 */

class Property {
    public static final boolean PRIMARY_KEY = true;
    public static final boolean REQUIRED    = true;
    public static final boolean INDEXED     = true;

    private final long nativePtr;

    public Property(String name, RealmFieldType type, boolean isPrimary, boolean isIndexed, boolean isRequired) {
        this.nativePtr = nativeCreateProperty(name, type.getNativeValue(), isPrimary, isIndexed, !isRequired);
    }

    public Property(String name, RealmFieldType type, RealmObjectSchema linkedTo) {
        String linkedToName = linkedTo.getClassName();
        this.nativePtr = nativeCreateProperty(name, type.getNativeValue(), linkedToName);
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
        }
    }

    private static native long nativeCreateProperty(String name, int type, boolean isPrimary, boolean isIndexed, boolean isNullable);
    private static native long nativeCreateProperty(String name, int type, String linkedToName);
    private static native void nativeClose(long nativePtr);
}
