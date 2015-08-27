/*
 * Copyright 2014 Realm Inc.
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

import android.test.AndroidTestCase;

import io.realm.entities.AnnotationIndexTypes;
import io.realm.entities.AnnotationNameConventions;
import io.realm.entities.AnnotationTypes;
import io.realm.entities.PrimaryKeyAsLong;
import io.realm.entities.PrimaryKeyAsString;
import io.realm.exceptions.RealmException;
import io.realm.internal.Table;

public class RealmAnnotationTest extends AndroidTestCase {
    protected Realm testRealm;

    @Override
    protected void setUp() throws Exception {
        Realm.deleteRealmFile(getContext());
        testRealm = Realm.getInstance(getContext());
        testRealm.beginTransaction();
        AnnotationTypes object = testRealm.createObject(AnnotationTypes.class);
        object.setNotIndexString("String 1");
        object.setIndexString("String 2");
        object.setIgnoreString("String 3");
        testRealm.commitTransaction();
    }

    @Override
    protected void tearDown() throws Exception {
        testRealm.close();
    }

    public void testIgnore() {
        Table table = testRealm.getTable(AnnotationTypes.class);
        assertEquals(-1, table.getColumnIndex("ignoreString"));
    }

    // Test if "index" annotation works with supported types
    public void testIndex() {
        Table table = testRealm.getTable(AnnotationIndexTypes.class);

        assertTrue(table.hasSearchIndex(table.getColumnIndex("indexString")));
        assertFalse(table.hasSearchIndex(table.getColumnIndex("notIndexString")));

        assertTrue(table.hasSearchIndex(table.getColumnIndex("indexInt")));
        assertFalse(table.hasSearchIndex(table.getColumnIndex("notIndexInt")));

        assertTrue(table.hasSearchIndex(table.getColumnIndex("indexByte")));
        assertFalse(table.hasSearchIndex(table.getColumnIndex("notIndexByte")));

        assertTrue(table.hasSearchIndex(table.getColumnIndex("indexShort")));
        assertFalse(table.hasSearchIndex(table.getColumnIndex("notIndexShort")));

        assertTrue(table.hasSearchIndex(table.getColumnIndex("indexLong")));
        assertFalse(table.hasSearchIndex(table.getColumnIndex("notIndexLong")));

        assertTrue(table.hasSearchIndex(table.getColumnIndex("indexBoolean")));
        assertFalse(table.hasSearchIndex(table.getColumnIndex("notIndexBoolean")));

        assertTrue(table.hasSearchIndex(table.getColumnIndex("indexDate")));
        assertFalse(table.hasSearchIndex(table.getColumnIndex("notIndexDate")));
    }

    // Test migrating primary key from string to long with existing data
    public void testPrimaryKeyMigration_long() {
        testRealm.beginTransaction();
        for (int i = 1; i <= 2; i++) {
            PrimaryKeyAsString obj = testRealm.createObject(PrimaryKeyAsString.class);
            obj.setId(i);
            obj.setName("String" + i);
        }

        Table table = testRealm.getTable(PrimaryKeyAsString.class);
        table.setPrimaryKey("id");
        assertEquals(1, table.getPrimaryKey());
        testRealm.cancelTransaction();
    }

    // Test migrating primary key from string to long with existing data
    public void testPrimaryKeyMigration_longDuplicateValues() {
        testRealm.beginTransaction();
        for (int i = 1; i <= 2; i++) {
            PrimaryKeyAsString obj = testRealm.createObject(PrimaryKeyAsString.class);
            obj.setId(1); // Create duplicate values
            obj.setName("String" + i);
        }

        Table table = testRealm.getTable(PrimaryKeyAsString.class);
        try {
            table.setPrimaryKey("id");
            fail("It should not be possible to set a primary key column which already contains duplicate values.");
        } catch (IllegalArgumentException expected) {
            assertEquals(0, table.getPrimaryKey());
        } finally {
            testRealm.cancelTransaction();
        }
    }

    // Test migrating primary key from long to str with existing data
    public void testPrimaryKeyMigration_string() {
        testRealm.beginTransaction();
        for (int i = 1; i <= 2; i++) {
            PrimaryKeyAsLong obj = testRealm.createObject(PrimaryKeyAsLong.class);
            obj.setId(i);
            obj.setName("String" + i);
        }

        Table table = testRealm.getTable(PrimaryKeyAsLong.class);
        table.setPrimaryKey("name");
        assertEquals(1, table.getPrimaryKey());
        testRealm.cancelTransaction();
    }

    // Test migrating primary key from long to str with existing data
    public void testPrimaryKeyMigration_stringDuplicateValues() {
        testRealm.beginTransaction();
        for (int i = 1; i <= 2; i++) {
            PrimaryKeyAsLong obj = testRealm.createObject(PrimaryKeyAsLong.class);
            obj.setId(i);
            obj.setName("String"); // Create duplicate values
        }

        Table table = testRealm.getTable(PrimaryKeyAsLong.class);
        try {
            table.setPrimaryKey("name");
            fail("It should not be possible to set a primary key column which already contains duplicate values.");
        } catch (IllegalArgumentException expected) {
            assertEquals(0, table.getPrimaryKey());
        } finally {
            testRealm.cancelTransaction();
        }
    }

    public void testPrimaryKey_checkPrimaryKeyOnCreate() {
        testRealm.beginTransaction();
        try {
            testRealm.createObject(AnnotationTypes.class);
            fail("Two empty objects cannot be created on the same table if a primary key is defined");
        } catch (RealmException expected) {
        } finally {
            testRealm.cancelTransaction();
        }
    }

    // It should be allowed to override the primary key value with the same value
    public void testPrimaryKey_defaultStringValue() {
        testRealm.beginTransaction();
        PrimaryKeyAsString str = testRealm.createObject(PrimaryKeyAsString.class);
        str.setName("");
        testRealm.commitTransaction();
    }

    // It should be allowed to override the primary key value with the same value
    public void testPrimaryKey_defaultLongValue() {
        testRealm.beginTransaction();
        PrimaryKeyAsLong str = testRealm.createObject(PrimaryKeyAsLong.class);
        str.setId(0);
        testRealm.commitTransaction();
    }

    public void testPrimaryKey_errorOnInsertingSameObject() {
        try {
            testRealm.beginTransaction();
            AnnotationTypes obj1 = testRealm.createObject(AnnotationTypes.class);
            obj1.setId(1);
            AnnotationTypes obj2 = testRealm.createObject(AnnotationTypes.class);
            obj2.setId(1);
            fail("Inserting two objects with same primary key should fail");
        } catch (RealmException expected) {
        } finally {
            testRealm.cancelTransaction();
        }
    }

    public void testPrimaryKeyIsIndexed() {
        Table table = testRealm.getTable(PrimaryKeyAsString.class);
        assertTrue(table.hasPrimaryKey());
        assertTrue(table.hasSearchIndex(table.getColumnIndex("name")));

        table = testRealm.getTable(PrimaryKeyAsLong.class);
        assertTrue(table.hasPrimaryKey());
        assertTrue(table.hasSearchIndex(table.getColumnIndex("id")));
    }

    // Annotation processor honors common naming conventions
    // We check if setters and getters are generated and working
    public void testNamingConvention() {
        Realm realm = Realm.getInstance(getContext());
        realm.beginTransaction();
        realm.clear(AnnotationNameConventions.class);
        AnnotationNameConventions anc1 = realm.createObject(AnnotationNameConventions.class);
        anc1.setHasObject(true);
        anc1.setId_object(1);
        anc1.setmObject(2);
        anc1.setObject_id(3);
        anc1.setObject(true);
        realm.commitTransaction();

        AnnotationNameConventions anc2 = realm.allObjects(AnnotationNameConventions.class).first();
        assertTrue(anc2.isHasObject());
        assertEquals(1, anc2.getId_object());
        assertEquals(2, anc2.getmObject());
        assertEquals(3, anc2.getObject_id());
        assertTrue(anc2.isObject());
        realm.close();
    }
}
