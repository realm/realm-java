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

package io.realm.internal;

import java.util.LinkedList;

/**
 * This context allows to bypass the GC and manually release its native resources.
 *
 * It is used on object instances which lifecycle is bound to the duration of a core's callback.
 *
 * It is used to track and release instances which lifecycle is bound to a certain scope, for
 * example on Realm instances created by Core on the scope of a callback.
 */
class ManualReleaseNativeContext extends NativeContext {
    private final LinkedList<NativeObject> references = new LinkedList<>();

    @Override
    public void addReference(NativeObject referent) {
        references.add(referent);
    }

    public void release() {
        for (NativeObject object : references) {
            NativeObjectReference.nativeCleanUp(object.getNativeFinalizerPtr(), object.getNativePtr());
        }
    }
}
