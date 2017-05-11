/*
 * Copyright 2015 Realm Inc.
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
 * This abstract class represents a native object from core.
 * It specifies the operations common to all such objects.
 * All Java classes wrapping a core class should implement NativeObject.
 */
public interface NativeObject {
    long NULLPTR = 0L;

    /**
     * Gets the pointer of a native object.
     *
     * @return the native pointer.
     */
    long getNativePtr();

    /**
     * Gets the function pointer which points to the function to free the native object.
     * The function should be defined like: {@code typedef void (*FinalizeFunc)(jlong ptr)}.
     *
     * @return the function pointer for freeing the native resource.
     */
    long getNativeFinalizerPtr();
}
