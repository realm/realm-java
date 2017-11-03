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

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmError;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class OsSharedRealmTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private RealmConfiguration config;
    private OsSharedRealm sharedRealm;

    @Before
    public void setUp() {
        config = configFactory.createConfiguration();
        sharedRealm = OsSharedRealm.getInstance(config);
    }

    @After
    public void tearDown() {
        if (sharedRealm != null) {
            sharedRealm.close();
        }
    }

    @Test
    public void getVersionID() {
        OsSharedRealm.VersionID versionID1 = sharedRealm.getVersionID();
        sharedRealm.beginTransaction();
        sharedRealm.commitTransaction();
        OsSharedRealm.VersionID versionID2 = sharedRealm.getVersionID();
        assertFalse(versionID1.equals(versionID2));
    }

    @Test
    public void hasTable() {
        assertFalse(sharedRealm.hasTable("MyTable"));
        sharedRealm.beginTransaction();
        sharedRealm.createTable("MyTable");
        sharedRealm.commitTransaction();
        assertTrue(sharedRealm.hasTable("MyTable"));
    }

    @Test
    public void getTable() {
        assertFalse(sharedRealm.hasTable("MyTable"));
        sharedRealm.beginTransaction();
        sharedRealm.createTable("MyTable");
        sharedRealm.commitTransaction();
        assertTrue(sharedRealm.hasTable("MyTable"));

        // Table is existing, no need transaction to create it
        assertTrue(sharedRealm.getTable("MyTable").isValid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getTable_throwsIfTableNotExist() {
        sharedRealm.getTable("NON_EXISTING");
    }

    @Test
    public void isInTransaction() {
        assertFalse(sharedRealm.isInTransaction());
        sharedRealm.beginTransaction();
        assertTrue(sharedRealm.isInTransaction());
        sharedRealm.cancelTransaction();
        assertFalse(sharedRealm.isInTransaction());
    }

    @Test
    public void isInTransaction_returnFalseWhenRealmClosed() {
        sharedRealm.close();
        assertFalse(sharedRealm.isInTransaction());
        sharedRealm = null;
    }

    @Test
    public void renameTable() {
        sharedRealm.beginTransaction();
        sharedRealm.createTable("OldTable");
        assertTrue(sharedRealm.hasTable("OldTable"));
        sharedRealm.renameTable("OldTable", "NewTable");
        assertFalse(sharedRealm.hasTable("OldTable"));
        assertTrue(sharedRealm.hasTable("NewTable"));
        sharedRealm.commitTransaction();
    }

    @Test
    public void renameTable_notInTransactionThrows() {
        sharedRealm.beginTransaction();
        sharedRealm.createTable("OldTable");
        sharedRealm.commitTransaction();
        thrown.expect(IllegalStateException.class);
        sharedRealm.renameTable("OldTable", "NewTable");
    }

    @Test
    public void renameTable_tableNotExist() {
        sharedRealm.beginTransaction();
        assertFalse(sharedRealm.hasTable("TableToRemove"));
        thrown.expect(RealmError.class);
        sharedRealm.renameTable("TableToRemove", "newName");
        sharedRealm.cancelTransaction();
    }


    private void changeSchemaByAnotherRealm() {
        OsSharedRealm sharedRealm = OsSharedRealm.getInstance(config);
        sharedRealm.beginTransaction();
        sharedRealm.createTable("NewTable");
        sharedRealm.commitTransaction();
        sharedRealm.close();
    }

    @Test
    public void registerSchemaChangedCallback_beginTransaction() {
        final AtomicBoolean listenerCalled = new AtomicBoolean(false);

        assertFalse(sharedRealm.hasTable("NewTable"));

        sharedRealm.registerSchemaChangedCallback(new OsSharedRealm.SchemaChangedCallback() {
            @Override
            public void onSchemaChanged() {
                assertTrue(sharedRealm.hasTable("NewTable"));
                listenerCalled.set(true);
            }
        });
        changeSchemaByAnotherRealm();
        sharedRealm.beginTransaction();
        assertTrue(listenerCalled.get());
    }

    @Test
    public void registerSchemaChangedCallback_refresh() {
        final AtomicBoolean listenerCalled = new AtomicBoolean(false);

        assertFalse(sharedRealm.hasTable("NewTable"));

        sharedRealm.registerSchemaChangedCallback(new OsSharedRealm.SchemaChangedCallback() {
            @Override
            public void onSchemaChanged() {
                assertTrue(sharedRealm.hasTable("NewTable"));
                listenerCalled.set(true);
            }
        });
        changeSchemaByAnotherRealm();
        sharedRealm.refresh();
        assertTrue(listenerCalled.get());
    }

    @Test
    public void isClosed() {
        sharedRealm.close();
        assertTrue(sharedRealm.isClosed());
        sharedRealm = null;
    }

    @Test
    public void close_twice() {
        sharedRealm.close();
        sharedRealm.close();
        assertTrue(sharedRealm.isClosed());
        sharedRealm = null;
    }
}
