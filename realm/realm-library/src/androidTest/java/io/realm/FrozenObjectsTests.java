/*
 * Copyright 2019 Realm Inc.
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

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Class testing various aspects of the frozen objects features
 */
@RunWith(AndroidJUnit4.class)
public class FrozenObjectsTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    private RealmConfiguration realmConfig;
    private Realm realm;
    private Realm frozenRealm;

    @Before
    public void setUp() {
        realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);
        frozenRealm = realm.freeze();
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
        if (frozenRealm != null) {
            frozenRealm.close();
        }
    }

    @Test
    public void freezeRealm() {
        Realm frozenRealm = realm.freeze();
        assertEquals(realm.getPath(), frozenRealm.getPath());
        assertTrue(frozenRealm.isFrozen());
        frozenRealm.close();

        DynamicRealm dynamicRealm = DynamicRealm.getInstance(realmConfig);
        DynamicRealm frozenDynamicRealm = dynamicRealm.freeze();
        assertEquals(dynamicRealm.getPath(), frozenDynamicRealm.getPath());
        assertTrue(frozenRealm.isFrozen());
        dynamicRealm.close();
        assertFalse(frozenDynamicRealm.isClosed());
        frozenDynamicRealm.close();
    }

    @Test
    public void freezeRealmInsideWriteTransactionsThrows() {
        try {
            frozenRealm.beginTransaction();
            fail();
        } catch (IllegalStateException ignore) {
        }
    }

    @Test
    @RunTestInLooperThread
    public void addingChangeListenerThrows() {
        try {
            frozenRealm.addChangeListener(new RealmChangeListener<Realm>() {
                @Override
                public void onChange(Realm realm) {
                }
            });
            fail();
        } catch (IllegalStateException ignore) {
            looperThread.testComplete();
        }
    }

    @Test
    public void removingChangeListeners() {
        frozenRealm.removeAllChangeListeners();
        frozenRealm.removeChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm realm) {
            }
        });
    }

    @Test
    public void refreshThrows() {
        try {
            frozenRealm.refresh();
            fail();
        } catch (IllegalStateException ignore) {
        }
    }

    @Test
    public void writeToFrozenObjectThrows() {

    }

    @Test
    public void canReadFrozenRealmAcrossThreads() {

    }

    @Test
    public void frozenObjectsReturnsFrozenRealms() {

    }

}
