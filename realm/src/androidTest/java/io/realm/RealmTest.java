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

import android.content.Context;

import java.io.IOException;

import io.realm.entities.AllTypes;
import io.realm.entities.Dog;
import io.realm.internal.SharedGroup;
import io.realm.internal.Table;


public class RealmTest extends RealmSetupTests {

    //Test io.realm.Realm API

    // Realm Constructors
    public void testShouldCreateRealm() {

        try {
            Realm realm = new Realm(getContext());
        } catch (Exception ex) {
            fail("Unexpected Exception: "+ex.getMessage());
        }
    }

    public void testShouldNotFailCreateRealmWithNullContext() {
        Context c = null;

        Realm realm = new Realm(c);
        fail("Realm has been created with null Context");
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
    public void testShouldNotFailSettingInvalidDurability() {
        Realm.setDefaultDurability(SharedGroup.Durability.valueOf("INVALID"));
        //TODO Add code that checks that the DefaultDurability has NOT been set
    }

    // setDefaultDurability(SharedGroup.Durability durability)
    public void testShouldSetDurabilityMemOnly() {
        Realm.setDefaultDurability(SharedGroup.Durability.MEM_ONLY);
        //TODO Add code that checks that the DefaultDurability has been set
    }

    //Table getTable(Class<?> clazz)
    public void testShouldGetTable() {
        testRealm.beginWrite();

        Table table = testRealm.getTable(AllTypes.class);
        testRealm.commit();
        assertNotNull("getTable is returning a null Table object", table);
    }

    // <E> void remove(Class<E> clazz, long objectIndex)
    public void testShouldRemoveRow() {
        testRealm.beginWrite();

        testRealm.remove(AllTypes.class, 0);

        testRealm.commit();
        ResultList<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        assertEquals("Realm.delete has not deleted record correctly", TEST_DATA_SIZE - 1, resultList.size());

    }

    // <E extends RealmObject> E get(Class<E> clazz, long rowIndex)
    public void testShouldGetObject() {

        RealmObject allTypes = testRealm.get(AllTypes.class,0);
        assertNotNull("get has returned null object", allTypes);
    }

    //boolean contains(Class<?> clazz)
    public void testShouldContainTable() {

        testRealm.beginWrite();
        AllTypes allTypes = testRealm.create(AllTypes.class);
        testRealm.commit();
        assertTrue("contains returns false for newly created table", testRealm.contains(AllTypes.class));
    }

    //boolean contains(Class<?> clazz)
    public void testShouldNotContainTable() {

        assertFalse("contains returns true for non-existing table", testRealm.contains(Dog.class));
    }

    // <E extends RealmObject> RealmQuery<E> where(Class<E> clazz)
    public void testShouldReturnResultSet()  {

        ResultList<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        assertEquals("Realm.get is returning wrong number of objects", TEST_DATA_SIZE, resultList.size());
    }

    // <E extends RealmObject> RealmTableOrViewList<E> allObjects(Class<E> clazz)
    public void testShouldReturnTableOrViewList() {
        testRealm.beginWrite();
        ResultList<AllTypes> resultList = testRealm.allObjects(AllTypes.class);
        assertEquals("Realm.get is returning wrong result set", TEST_DATA_SIZE, resultList.size());
        testRealm.commit();
    }

    //addChangeListener(RealmChangeListener listener)
    int testCount = 0;
    public void testChangeNotify() {

        testRealm.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                testCount++;
            }
        });

        testRealm.beginWrite();
        for (int i = 0; i < 5; i++) {

            Dog dog = testRealm.create(Dog.class);
            dog.setName("King "+Integer.toString(testCount) );
        }

        testRealm.commit();
        assertEquals("Have not received the expected number of events in ChangeListener", 1, testCount);
    }


    //void removeChangeListener(RealmChangeListener listener)
    public void testChangeNotifyRemove() {

        RealmChangeListener realmChangeListener = new RealmChangeListener() {
            @Override
            public void onChange() {
            }
        };
        testRealm.addChangeListener(realmChangeListener);

        testRealm.removeChangeListener(realmChangeListener);

        //TODO Check that realmChangeListener has been removed

    }

    //void removeChangeListener(RealmChangeListener listener)
    public void testFailChangeNotifyRemove() {

        RealmChangeListener realmChangeListener = new RealmChangeListener() {
            @Override
            public void onChange() {
            }
        };

        testRealm.removeChangeListener(realmChangeListener);

        //TODO Check that realmChangeListener has been removed

    }

    //void removeAllChangeListeners()
    public void testRemoveAllChangeListeners() {

        //This verifies that removeAllChangeListeners does not fail when no ChangeListeners are installed
        testRealm.removeAllChangeListeners();
    }

    //void removeAllChangeListeners()
    public void testFailRemoveAllChangeListeners() {

        testRealm.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                testCount++;
            }
        });

        testRealm.removeAllChangeListeners();

        //TODO Check that realmChangeListener has been removed

    }

    //void refresh()
    public void testRefresh() {
        testRealm.refresh();

        //TODO add code that tests the result of refresh
    }

    //void beginWrite()
    public void testBeginWrite() throws IOException {

        testRealm.beginWrite();

        AllTypes allTypes = testRealm.create(AllTypes.class);
        allTypes.setColumnString("Test data");
        testRealm.commit();

        ResultList<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        assertEquals("Change has not been committed", TEST_DATA_SIZE + 1, resultList.size());
        //tableInit();

    }

    //void commit()
    public void testCommit() {
        testRealm.beginWrite();
        AllTypes allTypes = testRealm.create(AllTypes.class);
        allTypes.setColumnBoolean(true);
        testRealm.commit();

        testRealm.beginWrite();
        ResultList<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        assertEquals("Change has not been committed", TEST_DATA_SIZE + 1, resultList.size());
        testRealm.commit();
    }

    //void clear(Class<?> classSpec)
    public void testClassClear() {

        testRealm.clear(AllTypes.class);

        ResultList<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        assertEquals("Realm.clear does not empty table", 0, resultList.size());
    }

    //void clear()
    public void testClassClearAllWithTwoTables() {
        testRealm.beginWrite();

        Dog dog = testRealm.create(Dog.class);
        dog.setName("Castro");
        testRealm.commit();

        testRealm.clear();

        boolean allNotGone = testRealm.contains(AllTypes.class) || testRealm.contains(Dog.class);
        assertFalse("Realm.clear does not remove table", allNotGone);
    }



    //int getVersion()
    public void testGetVersion() throws IOException {

        int version = testRealm.getVersion();

        assertTrue("Realm.version returns invalid version number " + Integer.toString(version), version > 0);
    }

    //void setVersion(int version)setVersion(int version)
    public void testSetVersion() {
        int version = 42;

        testRealm.setVersion(version);

        assertEquals("Realm.version has not been set by setVersion", version, testRealm.getVersion());
    }


}
