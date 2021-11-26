/*
 * Copyright 2014 Realm Inc.
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

import java.lang.ref.ReferenceQueue;
import java.util.LinkedList;


// Currently we free native objects in two threads, the SharedGroup is freed in the caller thread, others are freed in
// RealmFinalizingDaemon thread. And the destruction in both threads are locked by the corresponding context.
// The purpose of locking on NativeContext is:
// Destruction of SharedGroup (and hence Group and Table) is currently not thread-safe with respect to destruction of
// other accessors, you have to ensure mutual exclusion. This is also illustrated by the use of locks in the test
// test_destructor_thread_safety.cpp. Explicit call of SharedGroup::close() or Table::detach() is also not thread-safe
// with respect to destruction of other accessors.
public class NativeContext {
    private static final ReferenceQueue<NativeObject> referenceQueue = new ReferenceQueue<NativeObject>();
    private static final Thread finalizingThread = new Thread(new FinalizerRunnable(referenceQueue));
    // Dummy context which will be used by native objects which's destructors are always thread safe.
    public static final NativeContext dummyContext = new NativeContext();

    static {
        finalizingThread.setName("RealmFinalizingDaemon");
        finalizingThread.start();
    }

    public void addReference(NativeObject referent) {
        new NativeObjectReference(this, referent, referenceQueue);
    }

    /**
     * Executes a given NativeContextRunnable with a NativeContext which lifecycle is bound
     * to the runnable scope.
     *
     * @param runnable function to execute.
     */
    static void execute(ManualReleaseNativeContext.NativeContextRunnable runnable) {
        ManualReleaseNativeContext nativeContext = new ManualReleaseNativeContext();
        runnable.run(nativeContext);
        nativeContext.release();
    }

    public interface NativeContextRunnable {
        void run(NativeContext nativeContext);
    }

    private static class ManualReleaseNativeContext extends NativeContext {
        private final LinkedList<NativeObject> references = new LinkedList<>();

        ManualReleaseNativeContext(){ }

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
}
