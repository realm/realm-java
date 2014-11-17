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
        testRealm.clear(AnnotationTypes.class);
        AnnotationTypes object = testRealm.createObject(AnnotationTypes.class);
        object.setNotIndexString("String 1");
        object.setIndexString("String 2");
        object.setIgnoreString("String 3");
        testRealm.commitTransaction();
    }

    public void testIgnore() {
        Table table = testRealm.getTable(AnnotationTypes.class);
        assertEquals(-1, table.getColumnIndex("ignoreString"));
    }

    public void testIndex() {
        Table table = testRealm.getTable(AnnotationTypes.class);
        assertTrue(table.hasIndex(table.getColumnIndex("indexString")));
        assertFalse(table.hasIndex(table.getColumnIndex("notIndexString")));
    }

    public void testHasPrimaryKey() {
        Table table = testRealm.getTable(AnnotationTypes.class);
        assertTrue(table.hasPrimaryKey());
    }

    public void testPrimaryKey_checkPrimaryKeyOnCreate() {
        testRealm.beginTransaction();
        testRealm.clear(AnnotationTypes.class);
        testRealm.createObject(AnnotationTypes.class);
        try {
            testRealm.createObject(AnnotationTypes.class);
        } catch (RealmException e) {
            return;
        } finally {
            testRealm.cancelTransaction();
        }

        fail("Two empty objects cannot be created on the same table if a primary key is defined");
    }

    public void testPrimaryKey_defaultStringValue() {
        testRealm.beginTransaction();
        testRealm.clear(PrimaryKeyAsString.class);
        try {
            PrimaryKeyAsString str = testRealm.createObject(PrimaryKeyAsString.class);
            str.setName("");
        } catch (RealmException e) {
            assertTrue(e.getMessage().endsWith("not allowed as value in a field that is a primary key."));
            return;
        } finally {
            testRealm.cancelTransaction();
        }

        fail("It should not be allowed to set a primary key to the default value for the field type");
    }

    public void testPrimaryKey_defaultLongValue() {
        testRealm.beginTransaction();
        testRealm.clear(PrimaryKeyAsLong.class);
        try {
            PrimaryKeyAsLong str = testRealm.createObject(PrimaryKeyAsLong.class);
            str.setId(0);
        } catch (RealmException e) {
            assertTrue(e.getMessage().endsWith("not allowed as value in a field that is a primary key."));
            return;
        } finally {
            testRealm.cancelTransaction();
        }

        fail("It should not be allowed to set a primary key to the default value for the field type");
    }

    public void testPrimaryKey_errorOnInsertingSameObject() {
        try {
            testRealm.beginTransaction();
            testRealm.clear(AnnotationTypes.class);
            AnnotationTypes obj1 = testRealm.createObject(AnnotationTypes.class);
            obj1.setId(1);
            AnnotationTypes obj2 = testRealm.createObject(AnnotationTypes.class);
            obj2.setId(1);
        } catch (RealmException e) {
            return;
        } finally {
            testRealm.cancelTransaction();
        }

        fail("Inserting two objects with same primary key should fail");
    }

    public void testPrimaryKeyIsIndexed() {
        Table table = testRealm.getTable(PrimaryKeyAsString.class);
        assertTrue(table.hasPrimaryKey());
        assertTrue(table.hasIndex(0));
    }
}