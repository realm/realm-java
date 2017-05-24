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

package io.realm.instrumentation;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import io.realm.RealmConfiguration;

public class MockActivityManager {
    private Lifecycle instance;
    private final RealmConfiguration realmConfiguration;
    private final ReferenceQueue<Lifecycle> queue;
    private static final Set<WeakReference<Lifecycle>> references = new CopyOnWriteArraySet<WeakReference<Lifecycle>>();

    private MockActivityManager(RealmConfiguration realmConfiguration) {
        this.realmConfiguration = realmConfiguration;

        instance = LifecycleComponentFactory.newInstance(realmConfiguration);

        this.queue = new ReferenceQueue<Lifecycle>();
        references.add(new WeakReference<Lifecycle>(instance, queue));

        instance.onStart();
    }

    public static MockActivityManager newInstance (RealmConfiguration realmConfiguration) {
        return new MockActivityManager(realmConfiguration);
    }

    // simulates a configuration change, that should trigger
    // to recreate the Lifecycle component
    public void sendConfigurationChange () {
        instance.onStop();
        // creates a new instance
        instance = LifecycleComponentFactory.newInstance(realmConfiguration);
        references.add(new WeakReference<Lifecycle>(instance, queue));

        instance.onStart();
    }

    public int numberOfInstances () {
        triggerGC();
        // WeakReferences are enqueued as soon as the object to which they point to becomes
        // weakly reachable.
        deleteWeaklyReachableReferences();
        return references.size();
    }

    // call onStop on the Activity, this help closing any open open realm
    public void onStop() {
        instance.onStop();
    }

    private void triggerGC () {
        // From the AOSP FinalizationTest:
        // https://android.googlesource.com/platform/libcore/+/master/support/src/test/java/libcore/
        // java/lang/ref/FinalizationTester.java
        // System.gc() does not garbage collect every time. Runtime.gc() is
        // more likely to perform a gc.
        Runtime.getRuntime().gc();
        enqueueReferences();
        System.runFinalization();
    }

    private void enqueueReferences() {
        // Hack. We don't have a programmatic way to wait for the reference queue daemon to move
        // references to the appropriate queues.
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new AssertionError();
        }
    }

    private void deleteWeaklyReachableReferences() {
        Reference<? extends Lifecycle> weakReference;
        while ((weakReference = queue.poll()) != null ) { // Does not wait for a reference to become available.
            references.remove(weakReference);
        }
    }
}
