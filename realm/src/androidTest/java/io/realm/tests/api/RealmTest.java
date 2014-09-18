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

import android.content.Context;

import java.io.File;
import java.io.IOException;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.ResultList;
import io.realm.internal.SharedGroup;
import io.realm.internal.Table;
import io.realm.tests.api.entities.AllTypes;
import io.realm.tests.api.entities.AllTypesRealmProxy;
import io.realm.tests.api.entities.Dog;
import io.realm.tests.api.entities.NoAnnotationObject;


public class RealmTest extends RealmSetupTests {
    final static int TEST_DATA_SIZE = 2;

    //Test Realm API

    // Realm Constructors
    public void testShouldCreateRealm() {
        setupSharedGroup();

        try {
            Realm realm = new Realm(getContext().getFilesDir());
        } catch (Exception ex) {
            fail("Unexpected Exception: "+ex.getMessage());
        }
    }

    public void testShouldNotFailCreateRealmWithNullDir() {
        setupSharedGroup();

        File f = null;
        Realm realm = new Realm(f);
    }

    public void testShouldNotFailCreateRealmWithNullContext() {
        setupSharedGroup();
        Context c = null;
        Realm realm = new Realm(c);
    }

    public void testShouldNotFailWithNullFileName() {
        setupSharedGroup();
        Realm realm = new Realm(getContext().getFilesDir(), null);
    }


    // setDefaultDurability(SharedGroup.Durability durability)
    public void testShouldSetDurabilityFull() {
        Realm.setDefaultDurability(SharedGroup.Durability.FULL);
        //TODO Add code that checks that the DefaultDurability has been set
    }

    // setDefaultDurability(SharedGroup.Durability durability)
    public void testShouldSetDurabilityFullByName() {
        Realm.setDefaultDurability(SharedGroup.Durability.valueOf("FULL"));
        //TODO Add code that checks that the DefaultDurability has been set
    }

    // setDefaultDurability(SharedGroup.Durability durability)
    public void testShouldSetDurabilityWithInvalidNameFail() {
        try {
            Realm.setDefaultDurability(SharedGroup.Durability.valueOf("INVALID"));
            fail("Expected IllegalArgumentException when providing illegal Durability value");
        } catch (IllegalArgumentException ioe) {
        }
    }

    // setDefaultDurability(SharedGroup.Durability durability)
    public void testShouldSetDurabilityMemOnly() {
        Realm.setDefaultDurability(SharedGroup.Durability.MEM_ONLY);
        //TODO Add code that checks that the DefaultDurability has been set
    }

    //Table getTable(Class<?> clazz)
    public void testShouldGetTable() throws IOException {
        Realm testRealm = getTestRealm();
        testRealm.beginWrite();

        Table table = testRealm.getTable(AllTypes.class);
        testRealm.commit();
        assertNotNull("getTable is returning a null Table object", table);
    }

    //<E extends RealmObject> E create(Class<E> clazz)
    public void testShouldCreateObject() throws IOException {
        Realm testRealm = getTestRealm();

        testRealm.beginWrite();
        RealmObject allTypes = testRealm.create(AllTypes.class);
        testRealm.commit();
        assertTrue("Realm.create is returning wrong object type", allTypes instanceof AllTypesRealmProxy);
    }
    //<E extends RealmObject> E create(Class<E> clazz)
    public void testShouldNotCreateObject() throws IOException {
        Realm testRealm = getTestRealm();

        RealmObject noAnnotationObject = testRealm.create(NoAnnotationObject.class);
        assertNull("Realm create expected to fail", noAnnotationObject);
    }

    // <E> void remove(Class<E> clazz, long objectIndex)
    public void testShouldRemoveRow() throws IOException {
        Realm realm = getTestRealm();

        buildAllTypesTestData(realm, TEST_DATA_SIZE);
        realm.beginWrite();

        realm.remove(AllTypes.class, 0);

        realm.commit();
        ResultList<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        assertEquals("Realm.delete has not deleted record correctly", TEST_DATA_SIZE - 1, resultList.size());
    }

    // <E extends RealmObject> E get(Class<E> clazz, long rowIndex)
    public void testShouldGetObject() throws IOException {
        Realm realm = getTestRealm();
        buildAllTypesTestData(realm, TEST_DATA_SIZE);

        RealmObject allTypes = realm.get(AllTypes.class,0);
        assertNotNull("get has returned null object", allTypes);
        assertTrue("Realm.get is returning wrong object type", allTypes instanceof AllTypes);
    }

    //boolean contains(Class<?> clazz)
    public void testShouldContainTable() throws IOException {
        Realm testRealm = getTestRealm();

        testRealm.beginWrite();
        AllTypes allTypes = testRealm.create(AllTypes.class);
        assertTrue("contains returns false for newly created table", testRealm.contains(AllTypes.class));
        testRealm.commit();
    }

    //boolean contains(Class<?> clazz)
    public void testShouldNotContainTable() throws IOException {
        Realm testRealm = getTestRealm();

        assertFalse("contains returns true for non-existing table", testRealm.contains(AllTypes.class));
    }

    // <E extends RealmObject> RealmQuery<E> where(Class<E> clazz)
    public void testShouldReturnResultSet() throws IOException {
        Realm realm = getTestRealm();
        buildAllTypesTestData(realm, TEST_DATA_SIZE);

        ResultList<AllTypes> resultList = realm.where(AllTypes.class).findAll();

        assertEquals("Realm.get is returning wrong number of objects", TEST_DATA_SIZE, resultList.size());
    }

    // <E extends RealmObject> RealmTableOrViewList<E> allObjects(Class<E> clazz)
    public void testShouldReturnTableOrViewList() throws IOException {
        Realm realm = getTestRealm();
        buildAllTypesTestData(realm, TEST_DATA_SIZE);

        realm.beginWrite();
        ResultList<AllTypes> resultList = realm.allObjects(AllTypes.class);
        assertEquals("Realm.get is returning wrong result set", TEST_DATA_SIZE, resultList.size());
        realm.commit();
    }

    //addChangeListener(RealmChangeListener listener)
    int testCount = 0;
    public void testChangeNotify() throws IOException {
        Realm realm = getTestRealm();

        realm.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                testCount++;
            }
        });

        realm.beginWrite();
        for (int i = 0; i < 5; i++) {

            Dog dog = realm.create(Dog.class);
            dog.setName("King "+Integer.toString(testCount) );
        }

        realm.commit();
        assertEquals("Have not received the expected number of events in ChangeListener", 1, testCount);
    }


    //void removeChangeListener(RealmChangeListener listener)
    public void testChangeNotifyRemove() throws IOException {
        Realm realm = getTestRealm();
        RealmChangeListener realmChangeListener = new RealmChangeListener() {
            @Override
            public void onChange() {
            }
        };
        realm.addChangeListener(realmChangeListener);

        realm.removeChangeListener(realmChangeListener);

        //TODO Check that realmChangeListener has been removed

    }

    //void removeChangeListener(RealmChangeListener listener)
    public void testFailChangeNotifyRemove() throws IOException {
        Realm realm = getTestRealm();
        RealmChangeListener realmChangeListener = new RealmChangeListener() {
            @Override
            public void onChange() {
            }
        };

        realm.removeChangeListener(realmChangeListener);

        //TODO Check that realmChangeListener has been removed

    }

    //void removeAllChangeListeners()
    public void testRemoveAllChangeListeners() throws IOException {
        Realm realm = getTestRealm();

        //This verifies that removeAllChangeListeners does not fail when no ChangeListeners are installed
        realm.removeAllChangeListeners();
    }

    //void removeAllChangeListeners()
    public void testFailRemoveAllChangeListeners() throws IOException {
        Realm realm = getTestRealm();

        realm.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                testCount++;
            }
        });

        realm.removeAllChangeListeners();

        //TODO Check that realmChangeListener has been removed

    }

    //void refresh()
    public void testRefresh() throws IOException {
        Realm realm = getTestRealm();

        realm.refresh();

        //TODO add code that tests the result of refresh
    }

    //void beginWrite()
    public void testBeginWrite() throws IOException {
        Realm realm = getTestRealm();

        realm.beginWrite();

        AllTypes allTypes = getTestObject(realm, AllTypes.class);
        allTypes.setColumnString("Test data");
        realm.commit();

        ResultList<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        assertEquals("Change has not been committed", 1, resultList.size());
    }

    //void commit()
    public void testCommit() throws IOException {
        Realm realm = getTestRealm();
        realm.beginWrite();
        AllTypes allTypes = getTestObject(realm, AllTypes.class);
        allTypes.setColumnBoolean(true);

        ResultList<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        assertEquals("Change has not been committed", 1, resultList.size());

        realm.commit();

    }

    //void clear(Class<?> classSpec)
    public void testClassClear() throws IOException {
        Realm realm = getTestRealm();
        buildAllTypesTestData(realm, TEST_DATA_SIZE);

        realm.beginWrite();
        realm.clear(AllTypes.class);
        realm.commit();

        ResultList<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        assertEquals("Realm.clear does not remove rows", 0, resultList.size());
    }

    //void clear()
    public void testClassClearAll() throws IOException {
        Realm realm = getTestRealm();
        buildAllTypesTestData(realm, TEST_DATA_SIZE);

        realm.beginWrite();

        Dog dog = getTestObject(realm, Dog.class);
        dog.setName("Castro");
        realm.commit();

        realm.clear();

        boolean allNotGone = realm.contains(AllTypes.class) || realm.contains(Dog.class);
        assertFalse("Realm.clear does not remove table", allNotGone);
    }



    //int getVersion()
    public void testGetVersion() throws IOException {
        Realm realm = getTestRealm();
        int version = -1;
        version = realm.getVersion();

        assertTrue("Realm.version returns invalid version number " + Integer.toString(version), version > 0);
    }

    //void setVersion(int version)setVersion(int version)
    public void testSetVersion() throws IOException {
        Realm realm = getTestRealm();
        int version = 42;
        realm.setVersion(version);

        assertEquals("Realm.version has not been set by setVersion", version, realm.getVersion());
    }


}
