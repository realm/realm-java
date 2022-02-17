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


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.TestRealmConfigurationFactory;
import io.realm.internal.android.AndroidRealmNotifier;
import io.realm.rule.BlockingLooperThread;

@RunWith(AndroidJUnit4.class)
public class RealmNotifierTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private final BlockingLooperThread looperThread = new BlockingLooperThread();

    private RealmConfiguration realmConfig;

    private Capabilities capabilitiesCanDeliver = new Capabilities() {
        @Override
        public boolean canDeliverNotification() {
            return true;
        }

        @Override
        public void checkCanDeliverNotification(@Nullable String exceptionMessage) {
        }

        @Override
        public boolean isMainThread() {
            return false;
        }
    };

    @Before
    public void setUp() throws Exception {
        realmConfig = configFactory.createConfiguration(UUID.randomUUID().toString());
    }

    @After
    public void tearDown() {
    }

    private OsSharedRealm getSharedRealm(RealmConfiguration config) {
        OsRealmConfig.Builder configBuilder = new OsRealmConfig.Builder(config)
                .autoUpdateNotification(true);
        return OsSharedRealm.getInstance(configBuilder, OsSharedRealm.VersionID.LIVE);
    }

    @Test
    public void post() {
        looperThread.runBlocking(() -> {
            RealmNotifier notifier = new AndroidRealmNotifier(null, capabilitiesCanDeliver);
            notifier.post(new Runnable() {
                @Override
                public void run() {
                    looperThread.testComplete();
                }
            });
        });
    }

    // Callback is immediately called when commitTransaction for local changes.
    @Test
    public void addChangeListener_byLocalChanges() {
        looperThread.runBlocking(() -> {
            final AtomicBoolean commitReturns = new AtomicBoolean(false);
            OsSharedRealm sharedRealm = getSharedRealm(realmConfig);
            sharedRealm.realmNotifier.addChangeListener(sharedRealm, new RealmChangeListener<OsSharedRealm>() {
                @Override
                public void onChange(OsSharedRealm sharedRealm) {
                    // Transaction has been committed in core, but commitTransaction hasn't returned in java.
                    assertFalse(commitReturns.get());
                    sharedRealm.close();
                    looperThread.testComplete();
                }
            });
            sharedRealm.beginTransaction();
            sharedRealm.commitTransaction();
            commitReturns.set(true);
        });
    }

    private void makeRemoteChanges(final RealmConfiguration config) {
        // We don't use cache from RealmCoordinator
        OsSharedRealm sharedRealm = getSharedRealm(config);
        sharedRealm.beginTransaction();
        sharedRealm.commitTransaction();
        sharedRealm.close();
    }

    @Test
    public void addChangeListener_byRemoteChanges() {
        looperThread.runBlocking(() -> {
            // To catch https://github.com/realm/realm-java/pull/4037 CI failure.
            // In this case, object store should not send more than 100 notifications.
            final int TIMES = 100;
            final AtomicInteger commitCounter = new AtomicInteger(0);
            final AtomicInteger listenerCounter = new AtomicInteger(0);

            OsSharedRealm sharedRealm = getSharedRealm(realmConfig);
            looperThread.keepStrongReference(sharedRealm);
            sharedRealm.realmNotifier.addChangeListener(sharedRealm, new RealmChangeListener<OsSharedRealm>() {
                @Override
                public void onChange(OsSharedRealm sharedRealm) {
                    int commits = commitCounter.get();
                    int listenerCount = listenerCounter.addAndGet(1);
                    assertEquals(commits, listenerCount);
                    if (commits == TIMES) {
                        sharedRealm.close();
                        looperThread.testComplete();
                    } else {
                        makeRemoteChanges(realmConfig);
                        commitCounter.getAndIncrement();
                    }
                }
            });
            makeRemoteChanges(realmConfig);
            commitCounter.getAndIncrement();
        });
    }

    // Ensure that exceptions in changelisteners do not cause native crashes, but instead
    // propagate correctly to end users
    @Test
    public void addChangeListener_exceptionsPropagateCorrectly() {
        try {
            looperThread.runBlocking(() -> {
                OsSharedRealm sharedRealm = getSharedRealm(realmConfig);
                looperThread.closeAfterTest(sharedRealm);
                sharedRealm.realmNotifier.addChangeListener(sharedRealm, new RealmChangeListener<OsSharedRealm>() {
                    @Override
                    public void onChange(OsSharedRealm sharedRealm) {
                        throw new RuntimeException("BOOM");
                    }
                });
                makeRemoteChanges(realmConfig);
            });
            fail();
        } catch (RuntimeException ex) {
            assertTrue(ex.getMessage().contains("BOOM"));
        }
    }

    @Test
    public void removeChangeListeners() {
        looperThread.runBlocking(() -> {
            OsSharedRealm sharedRealm = getSharedRealm(realmConfig);
            Integer dummyObserver = 1;
            looperThread.keepStrongReference(dummyObserver);
            looperThread.keepStrongReference(sharedRealm);
            sharedRealm.realmNotifier.addChangeListener(dummyObserver, new RealmChangeListener<Integer>() {
                @Override
                public void onChange(Integer dummy) {
                    fail();
                }
            });
            sharedRealm.realmNotifier.addChangeListener(sharedRealm, new RealmChangeListener<OsSharedRealm>() {
                @Override
                public void onChange(OsSharedRealm sharedRealm) {
                    sharedRealm.close();
                    looperThread.testComplete();
                }
            });

            // This should only remove the listeners related with dummyObserver
            sharedRealm.realmNotifier.removeChangeListeners(dummyObserver);

            makeRemoteChanges(realmConfig);
        });
    }
}
