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

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.entities.AnnotationIndexTypes;
import io.realm.entities.AnnotationNameConventions;
import io.realm.entities.AnnotationTypes;
import io.realm.entities.PrimaryKeyAsLong;
import io.realm.entities.PrimaryKeyAsString;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;
import io.realm.internal.Table;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmAnnotationTests {
    private Realm realm;

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    @Before
    public void setUp() {
        RealmConfiguration realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);
        realm.beginTransaction();
        AnnotationTypes object = realm.createObject(AnnotationTypes.class, 0);
        object.setNotIndexString("String 1");
        object.setIndexString("String 2");
        object.setIgnoreString("String 3");
        realm.commitTransaction();
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    @Test
    public void ignore() {
        Table table = realm.getTable(AnnotationTypes.class);
        assertEquals(-1, table.getColumnIndex("ignoreString"));
    }

    // Test if "index" annotation works with supported types
    @Test
    public void index() {
        Table table = realm.getTable(AnnotationIndexTypes.class);

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
    @Test
    public void primaryKey_migration_long() {
        realm.beginTransaction();
        for (int i = 1; i <= 2; i++) {
            PrimaryKeyAsString obj = realm.createObject(PrimaryKeyAsString.class, "String" + i);
            obj.setId(i);
        }

        Table table = realm.getTable(PrimaryKeyAsString.class);
        table.setPrimaryKey("id");
        assertEquals(1, table.getPrimaryKey());
        realm.cancelTransaction();
    }

    // Test migrating primary key from string to long with existing data
    @Test
    public void primaryKey_migration_longDuplicateValues() {
        realm.beginTransaction();
        for (int i = 1; i <= 2; i++) {
            PrimaryKeyAsString obj = realm.createObject(PrimaryKeyAsString.class, "String" + i);
            obj.setId(1); // Create duplicate values
        }

        Table table = realm.getTable(PrimaryKeyAsString.class);
        try {
            table.setPrimaryKey("id");
            fail("It should not be possible to set a primary key column which already contains duplicate values.");
        } catch (IllegalArgumentException ignored) {
            assertEquals(0, table.getPrimaryKey());
        } finally {
            realm.cancelTransaction();
        }
    }

    // Test migrating primary key from long to str with existing data
    @Test
    public void primaryKey_migration_string() {
        realm.beginTransaction();
        for (int i = 1; i <= 2; i++) {
            PrimaryKeyAsLong obj = realm.createObject(PrimaryKeyAsLong.class, i);
            obj.setName("String" + i);
        }

        Table table = realm.getTable(PrimaryKeyAsLong.class);
        table.setPrimaryKey("name");
        assertEquals(1, table.getPrimaryKey());
        realm.cancelTransaction();
    }

    // Test migrating primary key from long to str with existing data
    @Test
    public void primaryKey_migration_stringDuplicateValues() {
        realm.beginTransaction();
        for (int i = 1; i <= 2; i++) {
            PrimaryKeyAsLong obj = realm.createObject(PrimaryKeyAsLong.class, i);
            obj.setName("String"); // Create duplicate values
        }

        Table table = realm.getTable(PrimaryKeyAsLong.class);
        try {
            table.setPrimaryKey("name");
            fail("It should not be possible to set a primary key column which already contains duplicate values.");
        } catch (IllegalArgumentException ignored) {
            assertEquals(0, table.getPrimaryKey());
        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void primaryKey_checkPrimaryKeyOnCreate() {
        realm.beginTransaction();
        try {
            realm.createObject(AnnotationTypes.class, 0);
            fail("Two empty objects cannot be created on the same table if a primary key is defined");
        } catch (RealmPrimaryKeyConstraintException ignored) {
        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void primaryKey_errorOnInsertingSameObject() {
        try {
            realm.beginTransaction();
            realm.createObject(AnnotationTypes.class, 1);
            realm.createObject(AnnotationTypes.class, 1);
            fail("Inserting two objects with same primary key should fail");
        } catch (RealmPrimaryKeyConstraintException ignored) {
        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void primaryKey_isIndexed() {
        Table table = realm.getTable(PrimaryKeyAsString.class);
        assertTrue(table.hasPrimaryKey());
        assertTrue(table.hasSearchIndex(table.getColumnIndex("name")));

        table = realm.getTable(PrimaryKeyAsLong.class);
        assertTrue(table.hasPrimaryKey());
        assertTrue(table.hasSearchIndex(table.getColumnIndex("id")));
    }

    // Annotation processor honors common naming conventions
    // We check if setters and getters are generated and working
    @Test
    public void namingConvention() {
        realm.beginTransaction();
        realm.delete(AnnotationNameConventions.class);
        AnnotationNameConventions anc1 = realm.createObject(AnnotationNameConventions.class);
        anc1.setHasObject(true);
        anc1.setId_object(1);
        anc1.setmObject(2);
        anc1.setObject_id(3);
        anc1.setObject(true);
        realm.commitTransaction();

        AnnotationNameConventions anc2 = realm.where(AnnotationNameConventions.class).findFirst();
        assertTrue(anc2.isHasObject());
        assertEquals(1, anc2.getId_object());
        assertEquals(2, anc2.getmObject());
        assertEquals(3, anc2.getObject_id());
        assertTrue(anc2.isObject());
        realm.close();
    }
}
