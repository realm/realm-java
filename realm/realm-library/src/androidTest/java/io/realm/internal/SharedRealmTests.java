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
    private OsSharedRealm osSharedRealm;

    @Before
    public void setUp() {
        config = configFactory.createConfiguration();
        osSharedRealm = OsSharedRealm.getInstance(config);
    }

    @After
    public void tearDown() {
        if (osSharedRealm != null) {
            osSharedRealm.close();
        }
    }

    @Test
    public void getVersionID() {
        OsSharedRealm.VersionID versionID1 = osSharedRealm.getVersionID();
        osSharedRealm.beginTransaction();
        osSharedRealm.commitTransaction();
        OsSharedRealm.VersionID versionID2 = osSharedRealm.getVersionID();
        assertFalse(versionID1.equals(versionID2));
    }

    @Test
    public void hasTable() {
        assertFalse(osSharedRealm.hasTable("MyTable"));
        osSharedRealm.beginTransaction();
        osSharedRealm.createTable("MyTable");
        osSharedRealm.commitTransaction();
        assertTrue(osSharedRealm.hasTable("MyTable"));
    }

    @Test
    public void getTable() {
        assertFalse(osSharedRealm.hasTable("MyTable"));
        osSharedRealm.beginTransaction();
        osSharedRealm.createTable("MyTable");
        osSharedRealm.commitTransaction();
        assertTrue(osSharedRealm.hasTable("MyTable"));

        // Table is existing, no need transaction to create it
        assertTrue(osSharedRealm.getTable("MyTable").isValid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getTable_throwsIfTableNotExist() {
        osSharedRealm.getTable("NON_EXISTING");
    }

    @Test
    public void isInTransaction() {
        assertFalse(osSharedRealm.isInTransaction());
        osSharedRealm.beginTransaction();
        assertTrue(osSharedRealm.isInTransaction());
        osSharedRealm.cancelTransaction();
        assertFalse(osSharedRealm.isInTransaction());
    }

    @Test
    public void isInTransaction_returnFalseWhenRealmClosed() {
        osSharedRealm.close();
        assertFalse(osSharedRealm.isInTransaction());
        osSharedRealm = null;
    }

    @Test
    public void removeTable() {
        osSharedRealm.beginTransaction();
        osSharedRealm.createTable("TableToRemove");
        assertTrue(osSharedRealm.hasTable("TableToRemove"));
        osSharedRealm.removeTable("TableToRemove");
        assertFalse(osSharedRealm.hasTable("TableToRemove"));
        osSharedRealm.commitTransaction();
    }

    @Test
    public void removeTable_notInTransactionThrows() {
        osSharedRealm.beginTransaction();
        osSharedRealm.createTable("TableToRemove");
        osSharedRealm.commitTransaction();
        thrown.expect(IllegalStateException.class);
        osSharedRealm.removeTable("TableToRemove");
    }

    @Test
    public void removeTable_tableNotExist() {
        osSharedRealm.beginTransaction();
        assertFalse(osSharedRealm.hasTable("TableToRemove"));
        thrown.expect(RealmError.class);
        osSharedRealm.removeTable("TableToRemove");
        osSharedRealm.cancelTransaction();
    }

    @Test
    public void renameTable() {
        osSharedRealm.beginTransaction();
        osSharedRealm.createTable("OldTable");
        assertTrue(osSharedRealm.hasTable("OldTable"));
        osSharedRealm.renameTable("OldTable", "NewTable");
        assertFalse(osSharedRealm.hasTable("OldTable"));
        assertTrue(osSharedRealm.hasTable("NewTable"));
        osSharedRealm.commitTransaction();
    }

    @Test
    public void renameTable_notInTransactionThrows() {
        osSharedRealm.beginTransaction();
        osSharedRealm.createTable("OldTable");
        osSharedRealm.commitTransaction();
        thrown.expect(IllegalStateException.class);
        osSharedRealm.renameTable("OldTable", "NewTable");
    }

    @Test
    public void renameTable_tableNotExist() {
        osSharedRealm.beginTransaction();
        assertFalse(osSharedRealm.hasTable("TableToRemove"));
        thrown.expect(RealmError.class);
        osSharedRealm.renameTable("TableToRemove", "newName");
        osSharedRealm.cancelTransaction();
    }

    @Test
    public void beginTransaction_SchemaVersionListener() {
        final AtomicBoolean listenerCalled = new AtomicBoolean(false);
        final AtomicLong schemaVersionFromListener = new AtomicLong(-1L);

        osSharedRealm.close();
        osSharedRealm = OsSharedRealm.getInstance(config, new OsSharedRealm.SchemaVersionListener() {
            @Override
            public void onSchemaVersionChanged(long currentVersion) {
                listenerCalled.set(true);
                schemaVersionFromListener.set(currentVersion);
            }
        }, true);

        final long before = osSharedRealm.getSchemaVersion();

        osSharedRealm.beginTransaction();
        try {
            // Listener is not called if there was no schema change.
            assertFalse(listenerCalled.get());

            // Changes the schema version.
            osSharedRealm.setSchemaVersion(before + 1);
        } finally {
            osSharedRealm.commitTransaction();
        }

        // Listener is not yet called.
        assertFalse(listenerCalled.get());

        osSharedRealm.beginTransaction();
        try {
            assertTrue(listenerCalled.get());
            assertEquals(before + 1, schemaVersionFromListener.get());
        } finally {
            osSharedRealm.cancelTransaction();
        }
    }

    @Test
    public void refresh_SchemaVersionListener() {
        final AtomicBoolean listenerCalled = new AtomicBoolean(false);
        final AtomicLong schemaVersionFromListener = new AtomicLong(-1L);

        osSharedRealm.close();
        osSharedRealm = OsSharedRealm.getInstance(config, new OsSharedRealm.SchemaVersionListener() {
            @Override
            public void onSchemaVersionChanged(long currentVersion) {
                listenerCalled.set(true);
                schemaVersionFromListener.set(currentVersion);
            }
        }, true);

        final long before = osSharedRealm.getSchemaVersion();

        osSharedRealm.refresh();
        // Listener is not called if there was no schema change.
        assertFalse(listenerCalled.get());

        osSharedRealm.beginTransaction();
        try {
            // Changes the schema version.
            osSharedRealm.setSchemaVersion(before + 1);
        } finally {
            osSharedRealm.commitTransaction();
        }

        // Listener is not yet called.
        assertFalse(listenerCalled.get());

        osSharedRealm.refresh();
        assertTrue(listenerCalled.get());
        assertEquals(before + 1, schemaVersionFromListener.get());
    }

    @Test
    public void isClosed() {
        osSharedRealm.close();
        assertTrue(osSharedRealm.isClosed());
        osSharedRealm = null;
    }

    @Test
    public void close_twice() {
        osSharedRealm.close();
        osSharedRealm.close();
        assertTrue(osSharedRealm.isClosed());
        osSharedRealm = null;
    }
}
