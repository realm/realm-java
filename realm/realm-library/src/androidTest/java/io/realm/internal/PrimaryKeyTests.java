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

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.RealmConfiguration;
import io.realm.RealmFieldType;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class PrimaryKeyTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private RealmConfiguration config;
    private OsSharedRealm sharedRealm;

    @Before
    public void setUp() {
        config = configFactory.createConfiguration();
    }

    @After
    public void tearDown() {
        if (sharedRealm != null && !sharedRealm.isClosed()) {
            sharedRealm.close();
        }
    }

    private Table getTableWithStringPrimaryKey() {
        sharedRealm = OsSharedRealm.getInstance(config, OsSharedRealm.VersionID.LIVE);
        sharedRealm.beginTransaction();
        OsObjectStore.setSchemaVersion(sharedRealm,0); // Create meta table
        Table t = sharedRealm.createTable(Table.getTableNameForClass("TestTable"));
        long column = t.addColumn(RealmFieldType.STRING, "colName", true);
        t.addSearchIndex(column);
        OsObjectStore.setPrimaryKeyForObject(sharedRealm, "TestTable", "colName");
        return t;
    }

    private Table getTableWithIntegerPrimaryKey() {
        sharedRealm = OsSharedRealm.getInstance(config, OsSharedRealm.VersionID.LIVE);
        sharedRealm.beginTransaction();
        OsObjectStore.setSchemaVersion(sharedRealm,0); // Create meta table
        Table t = sharedRealm.createTable(Table.getTableNameForClass("TestTable"));
        long column = t.addColumn(RealmFieldType.INTEGER, "colName");
        t.addSearchIndex(column);
        OsObjectStore.setPrimaryKeyForObject(sharedRealm, "TestTable", "colName");
        return t;
    }

    private Table getTableWithPrimaryKey(RealmFieldType fieldType, boolean isNullable) {
        OsObjectStore.setSchemaVersion(sharedRealm,0); // Create meta table
        return sharedRealm.createTableWithPrimaryKey(Table.getTableNameForClass("TestTable"), "colName", fieldType, isNullable);
    }

    /**
     * This test surfaces a bunch of problems, most of them seem to be around caching of the schema
     * during a transaction
     *
     * 1) Removing the primary key do not invalidate the cache in RealmSchema and those cached
     *    are ImmutableRealmObjectSchema so do not change when the primary key is removed.
     *
     * 2) Addding `schema.refresh()` to RealmObjectSchema.removePrimaryKey()` causes
     *    RealmPrimaryKeyConstraintException anyway. Unclear why.
     */
    @Test
    public void removingPrimaryKeyRemovesConstraint_typeSetters() {
        RealmConfiguration config = configFactory.createConfigurationBuilder()
                .name("removeConstraints").build();

        DynamicRealm realm = DynamicRealm.getInstance(config);
        RealmSchema realmSchema = realm.getSchema();
        realm.beginTransaction();
        RealmObjectSchema tableSchema = realmSchema.create("Employee")
                .addField("name", String.class, FieldAttribute.PRIMARY_KEY);

        realm.createObject("Employee", "Foo");
        DynamicRealmObject obj = realm.createObject("Employee", "Foo2");

        try {
            // Tries to create 2nd entry with name Foo.
            obj.setString("name", "Foo");
        } catch (IllegalArgumentException e) {
            tableSchema.removePrimaryKey();
            obj.setString("name", "Foo");
        } finally {
            realm.close();
        }
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
        assertEquals("Foo", row.getString(row.getColumnKey("colName")));
        sharedRealm.cancelTransaction();
    }

    @Test
    public void addEmptyRowWithPrimaryKeyLong() {
        Table t = getTableWithIntegerPrimaryKey();
        UncheckedRow row = OsObject.createWithPrimaryKey(t, 42);
        assertEquals(1, t.size());
        assertEquals(42L, row.getLong(row.getColumnKey("colName")));
        sharedRealm.cancelTransaction();
    }

    @Test
    public void createTableWithIntegerPrimaryKeyNullable(){
        sharedRealm = OsSharedRealm.getInstance(config, OsSharedRealm.VersionID.LIVE);
        sharedRealm.beginTransaction();

        Table t = getTableWithPrimaryKey(RealmFieldType.INTEGER, true);

        UncheckedRow row = OsObject.createWithPrimaryKey(t, 42);
        assertEquals(1, t.size());
        assertEquals(42, row.getLong(row.getColumnKey("colName")));


        row = OsObject.createWithPrimaryKey(t, null);
        assertEquals(2, t.size());
        assertTrue(row.isNull(row.getColumnKey("colName")));

        sharedRealm.cancelTransaction();
    }

    @Test
    public void createTableWithStringPrimaryKeyNullable(){
        sharedRealm = OsSharedRealm.getInstance(config, OsSharedRealm.VersionID.LIVE);
        sharedRealm.beginTransaction();

        Table t = getTableWithPrimaryKey(RealmFieldType.STRING, true);

        UncheckedRow row = OsObject.createWithPrimaryKey(t, "Foo");
        assertEquals(1, t.size());
        assertEquals("Foo", row.getString(row.getColumnKey("colName")));

        row = OsObject.createWithPrimaryKey(t, null);
        assertEquals(2, t.size());
        assertTrue(row.isNull(row.getColumnKey("colName")));

        sharedRealm.cancelTransaction();
    }

    @Test
    public void createTableWithObjectIdPrimaryKeyNullable(){
        sharedRealm = OsSharedRealm.getInstance(config, OsSharedRealm.VersionID.LIVE);
        sharedRealm.beginTransaction();

        Table t = getTableWithPrimaryKey(RealmFieldType.OBJECT_ID, true);

        Date date = new Date();

        UncheckedRow row = OsObject.createWithPrimaryKey(t, new ObjectId(date, 0));
        assertEquals(1, t.size());
        assertEquals(new ObjectId(date, 0), row.getObjectId(row.getColumnKey("colName")));

        row = OsObject.createWithPrimaryKey(t, null);
        assertEquals(2, t.size());
        assertTrue(row.isNull(row.getColumnKey("colName")));

        sharedRealm.cancelTransaction();
    }

    @Test
    public void createTableWithIntegerPrimaryKey(){
        sharedRealm = OsSharedRealm.getInstance(config, OsSharedRealm.VersionID.LIVE);
        sharedRealm.beginTransaction();

        Table t = getTableWithPrimaryKey(RealmFieldType.INTEGER, false);

        UncheckedRow row = OsObject.createWithPrimaryKey(t, 42);
        assertEquals(1, t.size());
        assertEquals(42, row.getLong(row.getColumnKey("colName")));

        try {
            OsObject.createWithPrimaryKey(t, null);
            fail("Non-nullable primary key created with a null value.");
        } catch (IllegalArgumentException e){
            assertEquals("Illegal Argument: This field(colName) is not nullable.", e.getMessage());
        }

        sharedRealm.cancelTransaction();
    }

    @Test
    public void createTableWithStringPrimaryKey(){
        sharedRealm = OsSharedRealm.getInstance(config, OsSharedRealm.VersionID.LIVE);
        sharedRealm.beginTransaction();

        Table t = getTableWithPrimaryKey(RealmFieldType.STRING, false);

        UncheckedRow row = OsObject.createWithPrimaryKey(t, "Foo");
        assertEquals(1, t.size());
        assertEquals("Foo", row.getString(row.getColumnKey("colName")));

        try {
            OsObject.createWithPrimaryKey(t, null);
            fail("Non-nullable primary key created with a null value.");
        } catch (IllegalArgumentException e){
            assertEquals("Illegal Argument: This field(colName) is not nullable.", e.getMessage());
        }

        sharedRealm.cancelTransaction();
    }

    @Test
    public void createTableWithObjectIdPrimaryKey(){
        sharedRealm = OsSharedRealm.getInstance(config, OsSharedRealm.VersionID.LIVE);
        sharedRealm.beginTransaction();

        Table t = getTableWithPrimaryKey(RealmFieldType.OBJECT_ID, false);

        Date date = new Date();

        UncheckedRow row = OsObject.createWithPrimaryKey(t, new ObjectId(date, 0));
        assertEquals(1, t.size());
        assertEquals(new ObjectId(date, 0), row.getObjectId(row.getColumnKey("colName")));

        try {
            OsObject.createWithPrimaryKey(t, null);
            fail("Non-nullable primary key created with a null value.");
        } catch (IllegalArgumentException e){
            assertEquals("Illegal Argument: This field(colName) is not nullable.", e.getMessage());
        }

        sharedRealm.cancelTransaction();
    }
}
