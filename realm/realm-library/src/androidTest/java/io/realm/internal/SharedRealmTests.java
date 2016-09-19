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
import java.util.concurrent.atomic.AtomicLong;

import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmError;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SharedRealmTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private RealmConfiguration config;
    private SharedRealm sharedRealm;

    @Before
    public void setUp() {
        config = configFactory.createConfiguration();
        sharedRealm = SharedRealm.getInstance(config);
    }

    @After
    public void tearDown() {
        sharedRealm.close();
    }

    @Test
    public void getVersionID() {
        SharedRealm.VersionID versionID1 = sharedRealm.getVersionID();
        sharedRealm.beginTransaction();
        sharedRealm.commitTransaction();
        SharedRealm.VersionID versionID2 = sharedRealm.getVersionID();
        assertFalse(versionID1.equals(versionID2));
    }

    @Test
    public void hasTable() {
        assertFalse(sharedRealm.hasTable("MyTable"));
        sharedRealm.beginTransaction();
        sharedRealm.getTable("MyTable");
        sharedRealm.commitTransaction();
        assertTrue(sharedRealm.hasTable("MyTable"));
    }

    @Test(expected = IllegalStateException.class)
    public void getTable_createNotInTransactionThrows() {
        sharedRealm.getTable("NON-EXISTING");
    }

    @Test
    public void getTable() {
        assertFalse(sharedRealm.hasTable("MyTable"));
        sharedRealm.beginTransaction();
        sharedRealm.getTable("MyTable");
        sharedRealm.commitTransaction();
        assertTrue(sharedRealm.hasTable("MyTable"));

        // Table is existing, no need transaction to create it
        sharedRealm.getTable("MyTable");
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
    public void removeTable() {
        sharedRealm.beginTransaction();
        sharedRealm.getTable("TableToRemove");
        assertTrue(sharedRealm.hasTable("TableToRemove"));
        sharedRealm.removeTable("TableToRemove");
        assertFalse(sharedRealm.hasTable("TableToRemove"));
        sharedRealm.commitTransaction();
    }

    @Test
    public void removeTable_notInTransactionThrows() {
        sharedRealm.beginTransaction();
        sharedRealm.getTable("TableToRemove");
        sharedRealm.commitTransaction();
        thrown.expect(IllegalStateException.class);
        sharedRealm.removeTable("TableToRemove");
    }

    @Test
    public void removeTable_tableNotExist() {
        sharedRealm.beginTransaction();
        assertFalse(sharedRealm.hasTable("TableToRemove"));
        thrown.expect(RealmError.class);
        sharedRealm.removeTable("TableToRemove");
        sharedRealm.cancelTransaction();
    }

    @Test
    public void renameTable() {
        sharedRealm.beginTransaction();
        sharedRealm.getTable("OldTable");
        assertTrue(sharedRealm.hasTable("OldTable"));
        sharedRealm.renameTable("OldTable", "NewTable");
        assertFalse(sharedRealm.hasTable("OldTable"));
        assertTrue(sharedRealm.hasTable("NewTable"));
        sharedRealm.commitTransaction();
    }

    @Test
    public void renameTable_notInTransactionThrows() {
        sharedRealm.beginTransaction();
        sharedRealm.getTable("OldTable");
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

    @Test
    public void beginTransaction_SchemaVersionListener() {
        final AtomicBoolean listenerCalled = new AtomicBoolean(false);
        final AtomicLong schemaVersionFromListener = new AtomicLong(-1L);

        sharedRealm.close();
        sharedRealm = SharedRealm.getInstance(config, null, new SharedRealm.SchemaVersionListener() {
            @Override
            public void onSchemaVersionChanged(long currentVersion) {
                listenerCalled.set(true);
                schemaVersionFromListener.set(currentVersion);
            }
        });

        final long before = sharedRealm.getSchemaVersion();

        sharedRealm.beginTransaction();
        try {
            // listener is not called if there was no schema change
            assertFalse(listenerCalled.get());

            // change the schema version
            sharedRealm.setSchemaVersion(before + 1);
        } finally {
            sharedRealm.commitTransaction();
        }

        // listener is not yet called
        assertFalse(listenerCalled.get());

        sharedRealm.beginTransaction();
        try {
            assertTrue(listenerCalled.get());
            assertEquals(before + 1, schemaVersionFromListener.get());
        } finally {
            sharedRealm.cancelTransaction();
        }
    }

    @Test
    public void refresh_SchemaVersionListener() {
        final AtomicBoolean listenerCalled = new AtomicBoolean(false);
        final AtomicLong schemaVersionFromListener = new AtomicLong(-1L);

        sharedRealm.close();
        sharedRealm = SharedRealm.getInstance(config, null, new SharedRealm.SchemaVersionListener() {
            @Override
            public void onSchemaVersionChanged(long currentVersion) {
                listenerCalled.set(true);
                schemaVersionFromListener.set(currentVersion);
            }
        });

        final long before = sharedRealm.getSchemaVersion();

        sharedRealm.refresh();
        // listener is not called if there was no schema change
        assertFalse(listenerCalled.get());

        sharedRealm.beginTransaction();
        try {
            // change the schema version
            sharedRealm.setSchemaVersion(before + 1);
        } finally {
            sharedRealm.commitTransaction();
        }

        // listener is not yet called
        assertFalse(listenerCalled.get());

        sharedRealm.refresh();
        assertTrue(listenerCalled.get());
        assertEquals(before + 1, schemaVersionFromListener.get());
    }
}
