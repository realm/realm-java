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

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import io.realm.RealmConfiguration;
import io.realm.RealmFieldType;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class PrimaryKeyTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private android.content.Context context;
    private RealmConfiguration config;
    private SharedRealm sharedRealm;

    @Before
    public void setUp() throws Exception {
        config = configFactory.createConfiguration();
        context = InstrumentationRegistry.getInstrumentation().getContext();
    }

    @After
    public void tearDown() {
        if (sharedRealm != null && !sharedRealm.isClosed()) {
            sharedRealm.close();
        }
    }

    private Table getTableWithStringPrimaryKey() {
        sharedRealm = SharedRealm.getInstance(config);
        sharedRealm.beginTransaction();
        Table t = sharedRealm.createTable(Table.getTableNameForClass("TestTable"));
        long column = t.addColumn(RealmFieldType.STRING, "colName", true);
        t.addSearchIndex(column);
        t.setPrimaryKey("colName");
        return t;
    }

    private Table getTableWithIntegerPrimaryKey() {
        sharedRealm = SharedRealm.getInstance(config);
        sharedRealm.beginTransaction();
        Table t = sharedRealm.createTable(Table.getTableNameForClass("class_TestTable"));
        long column = t.addColumn(RealmFieldType.INTEGER, "colName");
        t.addSearchIndex(column);
        t.setPrimaryKey("colName");
        return t;
    }

    // Tests that primary key constraints are actually removed.
    @Test
    public void removingPrimaryKeyRemovesConstraint_typeSetters() {
        RealmConfiguration config = configFactory.createConfigurationBuilder()
                .name("removeConstraints").build();
        SharedRealm sharedRealm = SharedRealm.getInstance(config);

        sharedRealm.beginTransaction();
        Table tbl = sharedRealm.createTable(Table.getTableNameForClass("EmployeeTable"));
        tbl.addColumn(RealmFieldType.STRING, "name");
        tbl.setPrimaryKey("name");

        // Creates first entry with name "Foo".
        tbl.setString(0, OsObject.createRow(tbl), "Foo", false);

        long rowIndex = OsObject.createRow(tbl);
        try {
            tbl.setString(0, rowIndex, "Foo", false); // Tries to create 2nd entry with name Foo.
        } catch (RealmPrimaryKeyConstraintException e1) {
            tbl.setPrimaryKey(""); // Primary key check worked, now removes it and tries again.
            try {
                tbl.setString(0, rowIndex, "Foo", false);
                return;
            } catch (RealmException e2) {
                fail("Primary key not removed");
            }
        }

        fail("Primary key not enforced.");
        sharedRealm.close();
    }

    @Test
    public void addEmptyRowWithPrimaryKeyWrongTypeStringThrows() {
        Table t = getTableWithStringPrimaryKey();
        try {
            OsObject.createWithPrimaryKey(t, 42);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        sharedRealm.cancelTransaction();
    }

    @Test
    public void addEmptyRowWithPrimaryKeyNullString() {
        Table t = getTableWithStringPrimaryKey();
        OsObject.createWithPrimaryKey(t, null);
        assertEquals(1, t.size());
        sharedRealm.cancelTransaction();
    }

    @Test
    public void addEmptyRowWithPrimaryKeyWrongTypeIntegerThrows() {
        Table t = getTableWithIntegerPrimaryKey();
        try {
            OsObject.createWithPrimaryKey(t, "Foo");
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        sharedRealm.cancelTransaction();
    }

    @Test
    public void addEmptyRowWithPrimaryKeyString() {
        Table t = getTableWithStringPrimaryKey();
        UncheckedRow row = OsObject.createWithPrimaryKey(t, "Foo");
        assertEquals(1, t.size());
        assertEquals("Foo", row.getString(0));
        sharedRealm.cancelTransaction();
    }

    @Test
    public void addEmptyRowWithPrimaryKeyLong() {
        Table t = getTableWithIntegerPrimaryKey();
        UncheckedRow row = OsObject.createWithPrimaryKey(t, 42);
        assertEquals(1, t.size());
        assertEquals(42L, row.getLong(0));
        sharedRealm.cancelTransaction();
    }

    @Test
    public void migratePrimaryKeyTableIfNeeded_first() throws IOException {
        configFactory.copyRealmFromAssets(context, "080_annotationtypes.realm", "default.realm");
        sharedRealm = SharedRealm.getInstance(config);
        sharedRealm.beginTransaction();
        assertTrue(Table.migratePrimaryKeyTableIfNeeded(sharedRealm));
        sharedRealm.commitTransaction();
        Table t = sharedRealm.getTable("class_AnnotationTypes");
        assertTrue(t.hasPrimaryKey());
        assertEquals(t.getColumnIndex("id"), t.getPrimaryKey());
        assertEquals(RealmFieldType.STRING, sharedRealm.getTable("pk").getColumnType(0));
    }

    @Test
    public void migratePrimaryKeyTableIfNeeded_second() throws IOException {
        configFactory.copyRealmFromAssets(context, "0841_annotationtypes.realm", "default.realm");
        sharedRealm = SharedRealm.getInstance(config);
        sharedRealm.beginTransaction();
        assertTrue(Table.migratePrimaryKeyTableIfNeeded(sharedRealm));
        sharedRealm.commitTransaction();
        Table t = sharedRealm.getTable("class_AnnotationTypes");
        assertTrue(t.hasPrimaryKey());
        assertEquals(t.getColumnIndex("id"), t.getPrimaryKey());
        assertEquals("AnnotationTypes", sharedRealm.getTable("pk").getString(0, 0));
    }

    // See https://github.com/realm/realm-java/issues/1775
    // Before 0.84.2, pk table added prefix "class_" to every class's name.
    // After 0.84.2, the pk table should be migrated automatically to remove the "class_".
    // In 0.84.2, the class names in pk table has been renamed to some incorrect names like "Thclass", "Mclass",
    // "NClass", "Meclass" and etc..
    // The 0841_pk_migration.realm is made to produce the issue.
    @Test
    public void migratePrimaryKeyTableIfNeeded_primaryKeyTableMigratedWithRightName() throws IOException {
        List<String> tableNames = Arrays.asList(
                "ChatList", "Drafts", "Member", "Message", "Notifs", "NotifyLink", "PopularPost",
                "Post", "Tags", "Threads", "User");

        configFactory.copyRealmFromAssets(context, "0841_pk_migration.realm", "default.realm");
        sharedRealm = SharedRealm.getInstance(config);
        sharedRealm.beginTransaction();
        assertTrue(Table.migratePrimaryKeyTableIfNeeded(sharedRealm));
        sharedRealm.commitTransaction();

        Table table = sharedRealm.getTable("pk");
        for (int i = 0; i < table.size(); i++) {
            UncheckedRow row = table.getUncheckedRow(i);
            // io_realm_internal_Table_PRIMARY_KEY_CLASS_COLUMN_INDEX 0LL
            assertTrue(tableNames.contains(row.getString(0)));
        }
    }

    // PK table's column 'pk_table' needs search index in order to use set_string_unique.
    // See https://github.com/realm/realm-java/pull/3488
    @Test
    public void migratePrimaryKeyTableIfNeeded_primaryKeyTableNeedSearchIndex() {
        sharedRealm = SharedRealm.getInstance(config);
        sharedRealm.beginTransaction();
        Table table = sharedRealm.createTable(Table.getTableNameForClass("TestTable"));
        long column = table.addColumn(RealmFieldType.INTEGER, "PKColumn");
        table.addSearchIndex(column);
        table.setPrimaryKey(column);
        sharedRealm.commitTransaction();

        assertEquals(table.getPrimaryKey(), table.getColumnIndex("PKColumn"));
        // Now we have a pk table with search index.

        sharedRealm.beginTransaction();
        Table pkTable = sharedRealm.getTable("pk");
        long classColumn = pkTable.getColumnIndex("pk_table");
        pkTable.removeSearchIndex(classColumn);

        // Tries to add a pk for another table.
        Table table2 = sharedRealm.createTable(Table.getTableNameForClass("TestTable2"));
        long column2 = table2.addColumn(RealmFieldType.INTEGER, "PKColumn");
        table2.addSearchIndex(column2);
        try {
            table2.setPrimaryKey(column2);
        } catch (IllegalStateException ignored) {
            // Column has no search index.
        }

        assertFalse(pkTable.hasSearchIndex(classColumn));

        Table.migratePrimaryKeyTableIfNeeded(sharedRealm);
        assertTrue(pkTable.hasSearchIndex(classColumn));
        // Now it works.
        table2.addSearchIndex(column2);
        sharedRealm.cancelTransaction();
    }
}
