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

import io.realm.entities.AllTypes;
import io.realm.exceptions.RealmException;

public class RealmListTest extends AndroidTestCase{

    protected Realm testRealm;

    protected void setUp() {
        Realm.deleteRealmFile(getContext());
        testRealm = Realm.getInstance(getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        testRealm.close();
    }

    public void testPublicNoArgConstructor() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        assertNotNull(list);
    }

    public void testUnavailableMethodsInNonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        try {
            list.move(0, 1);
            fail("move() should fail in non-managed mode.");
        } catch (RealmException ignore) {
        }
        try {
            list.where();
            fail("where() should fail in non-managed mode.");
        } catch (RealmException ignore) {
        }
    }

    public void testAddNonManagedMode() {
        RealmList list = new RealmList();
        AllTypes object = new AllTypes();
        object.setColumnString("String");
        list.add(object);
        assertEquals(1, list.size());
        assertEquals(object, list.get(0));
    }

    public void testAddNullNonManagedMode() {
        RealmList list = new RealmList();
        try {
            list.add(null);
            fail("Adding null should not be be allowed");
        } catch (NullPointerException ignore) {
        }
    }

    public void testAddManagedObject_nonManagedMode() {
        RealmList list = new RealmList();
        testRealm.beginTransaction();
        AllTypes managedAllTypes =  testRealm.createObject(AllTypes.class);
        testRealm.commitTransaction();
        try {
            list.add(managedAllTypes);
            fail("Adding managed objects to non-managed lists should fail");
        } catch (IllegalStateException ignore) {
        }
    }

    public void testAddAtIndex_nonManagedMode() {
        RealmList list = new RealmList();
        AllTypes object = new AllTypes();
        object.setColumnString("String");
        list.add(0, object);
        assertEquals(1, list.size());
        assertEquals(object, list.get(0));
    }

    public void testAddManagedObjectAtIndex_nonManagedMode() {
        RealmList list = new RealmList();
        testRealm.beginTransaction();
        AllTypes managedAllTypes = testRealm.createObject(AllTypes.class);
        testRealm.commitTransaction();
        try {
            list.add(5, managedAllTypes);
            fail("Adding managed objects to non-managed lists should fail");
        } catch (IllegalStateException ignore) {
        }
    }

    public void testAddNullAtIndex_nonManagedMode() {
        RealmList list = new RealmList();
        try {
            list.add(null);
            fail("Adding null should not be be allowed");
        } catch (NullPointerException ignore) {
        }
    }

    public void testSet_nonManagedMode() {
        RealmList list = new RealmList();
        list.add(new AllTypes());
        list.set(0, new AllTypes());
        assertEquals(1, list.size());
    }

    public void testSetNull_nonManagedMode() {
        RealmList list = new RealmList();
        try {
            list.set(5, null);
            fail("Setting a null value should result in a exception");
        } catch (NullPointerException ignore) {
        }
    }

    public void testSetManagedObject_nonManagedMode() {
        RealmList list = new RealmList();
        testRealm.beginTransaction();
        AllTypes managedAllTypes = testRealm.createObject(AllTypes.class);
        testRealm.commitTransaction();
        try {
            list.set(5, managedAllTypes);
            fail("Setting managed objects to non-managed lists should fail");
        } catch (IllegalStateException ignore) {
        }
    }

    public void testClear_nonManagedMode() {
        RealmList list = new RealmList();
        list.add(new AllTypes());
        assertEquals(1, list.size());
        list.clear();
        assertTrue(list.isEmpty());
    }

    public void testRemove_nonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        AllTypes object1 = new AllTypes();
        list.add(object1);
        AllTypes object2 = list.remove(0);
        assertEquals(object1, object2);
    }

    public void testGet_nonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        AllTypes object1 = new AllTypes();
        list.add(object1);
        AllTypes object2 = list.get(0);
        assertEquals(object1, object2);
    }

    public void testSize_nonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        list.add(new AllTypes());
        assertEquals(1, list.size());
    }
}