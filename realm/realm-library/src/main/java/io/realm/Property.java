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

public class Property {
    public static boolean PRIMARY_KEY = true;
    public static boolean REQUIRED    = true;
    public static boolean INDEXED     = true;

    private long nativePtr;

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

    public boolean isIndexable() {
        return nativeIsIndexable(nativePtr);
    }

    public boolean requiresIndex() {
        return nativeRequiresIndex(nativePtr);
    }

    public boolean isNullable() {
        return nativeIsNullable(nativePtr);
    }

    public void setName(String name) {
        nativeSetName(nativePtr, name);
    }

    public String getName() {
        return nativeGetName(nativePtr);
    }

    public boolean isPrimaryKey() {
        return nativeIsPrimaryKey(nativePtr);
    }

    public void close() {
        nativeClose(nativePtr);
    }

    private static native long nativeCreateProperty(String name, int type, boolean isPrimary, boolean isIndexed, boolean isNullable);
    private static native long nativeCreateProperty(String name, int type, String linkedToName);
    private static native boolean nativeIsIndexable(long nativePtr);
    private static native boolean nativeRequiresIndex(long nativePtr);
    private static native boolean nativeIsNullable(long nativePtr);
    private static native String nativeGetName(long nativePtr);
    private static native void nativeSetName(long nativePtr, String name);
    private static native boolean nativeIsPrimaryKey(long nativePtr);
    private static native void nativeClose(long nativePtr);
}
