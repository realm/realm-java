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

// class to wrap C++ vectors
public class CppVector {
    private long nativePtr;
    private class clz;

    public CppVector(class clz) {
        this.nativePtr = nativeCreateVector();
        this.clz = clz;
    }

    public void pushBack(NativeObject obj) {
        nativePushBack(nativePtr, obj.nativePointer);
    }

    public long size() {
        return nativeSize(nativePtr);
    }

    public NativeObject at(int index) {
        long nativeObjectPtr = nativeAt(nativePtr, index);
        NativeObject obj = new NativeObject(nativeObjectPtr);
        return obj;
    }

    private native long nativeCreateVector();
    private native void nativePushBack(long nativePtr, long nativeObjectPtr);
    private native long nativeSize(long nativePtr);
    private native long nativeAt(long nativePtr, int i);
}
