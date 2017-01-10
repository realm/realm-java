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


import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.internal.android.AndroidRealmNotifier;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmNotifierTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    private RealmConfiguration config;
    Capabilities capabilitiesCanDeliver = new Capabilities() {
        @Override
        public boolean canDeliverNotification() {
            return true;
        }

        @Override
        public void checkCanDeliverNotification(String exceptionMessage) {
        }
    };

    @Before
    public void setUp() throws Exception {
        config = configFactory.createConfiguration();
    }

    @After
    public void tearDown() {
    }

    private SharedRealm getSharedRealm() {
        return SharedRealm.getInstance(config, null, true);
    }

    @Test
    @RunTestInLooperThread
    public void post() {
        RealmNotifier notifier = new AndroidRealmNotifier(capabilitiesCanDeliver);
        notifier.post(new Runnable() {
            @Override
            public void run() {
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void postAtFrontOfQueue() {
        final RealmNotifier notifier = new AndroidRealmNotifier(capabilitiesCanDeliver);
        notifier.post(new Runnable() {
            @Override
            public void run() {
                fail();
            }
        });
        notifier.postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                looperThread.testComplete();
                notifier.close();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void addChangeListener_byLocalChanges() {
        SharedRealm sharedRealm = getSharedRealm();
        sharedRealm.realmNotifier.addChangeListener(sharedRealm, new RealmChangeListener<SharedRealm>() {
            @Override
            public void onChange(SharedRealm sharedRealm) {
                // Transaction has been committed in core, but commitTransaction hasn't returned in java.
                // Need a flag in java.
                //assertTrue(sharedRealm.isInTransaction());
                looperThread.testComplete();
                sharedRealm.close();
            }
        });
        sharedRealm.beginTransaction();
        sharedRealm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void addChangeListener_byRemoteChanges() {
        SharedRealm sharedRealm = getSharedRealm();
        sharedRealm.realmNotifier.addChangeListener(sharedRealm, new RealmChangeListener<SharedRealm>() {
            @Override
            public void onChange(SharedRealm sharedRealm) {
                looperThread.testComplete();
                sharedRealm.close();
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedRealm sharedRealm = getSharedRealm();
                sharedRealm.beginTransaction();
                sharedRealm.commitTransaction();
                sharedRealm.close();
            }
        }).start();
    }

    @Test
    @RunTestInLooperThread
    public void removeChangeListeners() {
        SharedRealm sharedRealm = getSharedRealm();
        Integer dummyObserver = 1;
        looperThread.keepStrongReference.add(dummyObserver);
        sharedRealm.realmNotifier.addChangeListener(dummyObserver, new RealmChangeListener<Integer>() {
            @Override
            public void onChange(Integer dummy) {
                fail();
            }
        });
        sharedRealm.realmNotifier.addChangeListener(sharedRealm, new RealmChangeListener<SharedRealm>() {
            @Override
            public void onChange(SharedRealm sharedRealm) {
                sharedRealm.close();
                looperThread.testComplete();
            }
        });

        // This should only remove the listeners related with dummyObserver
        sharedRealm.realmNotifier.removeChangeListeners(dummyObserver);

        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedRealm sharedRealm = getSharedRealm();
                sharedRealm.beginTransaction();
                sharedRealm.commitTransaction();
                sharedRealm.close();
            }
        }).start();
    }
}
