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
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.RealmConfiguration;
import io.realm.SyncTestUtils;
import io.realm.log.LogLevel;
import io.realm.log.RealmLog;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

// Tests for OsObjectStore
@RunWith(AndroidJUnit4.class)
public class OsObjectStoreTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();


    @Before
    public void setUp() throws IOException {
        SyncTestUtils.prepareEnvironmentForTest();
        RealmLog.setLevel(LogLevel.ERROR);
    }

    @After
    public void tearDown() {
        SyncTestUtils.restoreEnvironmentAfterTest();
    }

    @Test
    public void callWithLock() {
        RealmConfiguration config = configFactory.createConfiguration();

        // Return false if there are opened OsSharedRealm instance
        OsSharedRealm sharedRealm = OsSharedRealm.getInstance(config);
        assertFalse(OsObjectStore.callWithLock(config, new Runnable() {
            @Override
            public void run() {
                fail();
            }
        }));
        sharedRealm.close();

        final AtomicBoolean callbackCalled = new AtomicBoolean(false);
        assertTrue(OsObjectStore.callWithLock(config, new Runnable() {
            @Override
            public void run() {
                callbackCalled.set(true);
            }
        }));
        assertTrue(callbackCalled.get());
    }

    // Test if a java exception can be thrown from the callback.
    @Test
    public void callWithLock_throwInCallback() {
        RealmConfiguration config = configFactory.createConfiguration();
        final RuntimeException exception = new RuntimeException();

        try {
            OsObjectStore.callWithLock(config, new Runnable() {
                @Override
                public void run() {
                    throw exception;
                }
            });
            fail();
        } catch (RuntimeException e) {
            assertEquals(exception, e);
        }

        // The lock should be released after exception thrown
        final AtomicBoolean callbackCalled = new AtomicBoolean(false);
        assertTrue(OsObjectStore.callWithLock(config, new Runnable() {
            @Override
            public void run() {
                callbackCalled.set(true);
            }
        }));
    }
}
