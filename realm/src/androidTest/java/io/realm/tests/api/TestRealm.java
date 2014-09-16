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

package io.realm.tests.api;

import android.test.AndroidTestCase;

import java.io.IOException;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.internal.SharedGroup;
import io.realm.internal.Table;
import io.realm.tests.api.entities.AllColumns;

public class TestRealm extends AndroidTestCase {

    // Test setup methods:
    private void setupSharedGroup() {
        Realm.setDefaultDurability(SharedGroup.Durability.FULL);
    }

    private Realm getTestRealm() {
        setupSharedGroup();
        Realm testRealm = null;
        try {
            testRealm = new Realm(getContext().getFilesDir());
        } catch (IOException ex)
        {
            fail("Unexpected exception while initializing test case: "+ex.getMessage());
        }
        return testRealm;
    }

    private <E extends RealmObject> E getTestObject(Realm realm, Class<E> clazz) {
        setupSharedGroup();
        E result = realm.create(clazz);
        return result;
    }

    private void buildAllColumnsTestData(Realm realm)
    {
        realm.clear();
        realm.beginWrite();

        AllColumns allColumns = getTestObject(realm, AllColumns.class);
        allColumns.setColumnBoolean(true);
        allColumns.setColumnBinary(new byte[]{1,2,3});
        allColumns.setColumnDate(new Date());
        allColumns.setColumnDouble(3.1415);
        allColumns.setColumnFloat(1.234567f);
        allColumns.setColumnString("test data");
        allColumns.setColumnLong(45);

        allColumns = getTestObject(realm, AllColumns.class);
        allColumns.setColumnBoolean(false);
        allColumns.setColumnBinary(new byte[]{4,5,6});
        allColumns.setColumnDate(new Date());
        allColumns.setColumnDouble(9999.99);
        allColumns.setColumnFloat(0.1f);
        allColumns.setColumnString("more data");
        allColumns.setColumnLong(46);

        realm.commit();
    }

    //Test Realm.java API

    // setDefaultDurability(SharedGroup.Durability durability)
    public void testShouldSetDurabilityFull() {
        Realm.setDefaultDurability(SharedGroup.Durability.FULL);
    }

    public void testShouldSetDurabilityFullByName() {
        Realm.setDefaultDurability(SharedGroup.Durability.valueOf("FULL"));
    }

    public void testShouldSetDurabilityWithInvalidNameFail() {
        try {
            Realm.setDefaultDurability(SharedGroup.Durability.valueOf("INVALID"));
            fail("Expected IllegalArgumentException when providing illegal Durability value");
        } catch (IllegalArgumentException ioe)
        {
        }
    }

    public void testShouldSetDurabilityMemOnly() {
        Realm.setDefaultDurability(SharedGroup.Durability.MEM_ONLY);
    }

    // Realm Constructors
    public void testShouldCreateRealm() {
        setupSharedGroup();

        try {
            Realm realm = new Realm(getContext().getFilesDir());
        } catch (Exception ex) {
            fail("Unexpected Exception "+ex);
        }
    }

    public void testShouldFailCreateRealmWithNullDir() {
        setupSharedGroup();

        try {
            Realm realm = new Realm(null);
            fail("Expected IOException");
        } catch (IOException ioe) {
        //} catch (NoClassDefFoundError ncdf) {
        } catch (Exception ex) {
            fail("Unexpected exception: "+ex);
        }
    }

    public void testShouldFailWithNullFileName() {
        setupSharedGroup();

        try {
            Realm realm = new Realm(getContext().getFilesDir(), null);
            fail("Expected IOException");
        } catch (IOException ioe) {
        //} catch (NullPointerException npe) {
        } catch (Exception ex) {
            fail("Unexpected exception: "+ex.toString());
        }
    }

    // Table creation and manipulation

    public void testShouldGetTable() {
        Realm testRealm = getTestRealm();

        Table table = testRealm.getTable(AllColumns.class);
        assertNotNull("getTable is returning a null Table object", table);
    }

    public void testShouldContainTable() {
        Realm testRealm = getTestRealm();
        testRealm.getTable(AllColumns.class);
        boolean testIfContained = testRealm.contains(AllColumns.class);
        assertTrue("contains returns false for newly created table", testIfContained);
    }

    public void testShouldCreateObject() {
        Realm testRealm = getTestRealm();

        RealmObject allColumns = testRealm.create(AllColumns.class);
        assertTrue("Realm.create is returning wrong object type", allColumns instanceof AllColumns);
    }

    public void testShouldRemoveRow() {
        Realm realm = getTestRealm();
        buildAllColumnsTestData(realm);

        realm.remove(AllColumns.class,0);
    }

    public void testShouldGetObject() {
        Realm realm = getTestRealm();
        buildAllColumnsTestData(realm);

        RealmObject allColumns = realm.get(AllColumns.class,0);
        assertTrue("Realm.get is returning wrong object type", allColumns instanceof AllColumns);
    }

    //<E extends RealmObject> RealmQuery<E> where(Class<E> clazz)
    public void testShouldReturnResultSet() {
    }


    public void testShouldReturnTableOrViewList() {
        Realm realm = getTestRealm();
        buildAllColumnsTestData(realm);


        realm.allObjects(AllColumns.class);
    }

    //void ensureRealmAtVersion(int version, RealmMigration migration)
    public void testShouldVerifyVersion() {
    }


    // Notifications

    //void addChangeListener(RealmChangeListener listener)

    //void removeChangeListener(RealmChangeListener listener)

    //void removeAllChangeListeners()

    //boolean hasChanged()

    // Transactions

    //void refresh()


    //void beginWrite()

    //void commit()

    //void clear(Class<?> classSpec)

    //void clear()

    //int getVersion()

    //void setVersion(int version)

}