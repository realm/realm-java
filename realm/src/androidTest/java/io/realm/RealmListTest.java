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
import io.realm.entities.CyclicType;
import io.realm.entities.Dog;
import io.realm.entities.Owner;
import io.realm.exceptions.RealmException;

public class RealmListTest extends AndroidTestCase {

    public static final int TEST_OBJECTS = 10;
    private Realm testRealm;

    @Override
    protected void setUp() throws Exception {
        Realm.deleteRealmFile(getContext());
        testRealm = Realm.getInstance(getContext());

        testRealm.beginTransaction();
        Owner owner = testRealm.createObject(Owner.class);
        owner.setName("Owner");
        for (int i = 0; i < TEST_OBJECTS; i++) {
            Dog dog = testRealm.createObject(Dog.class);
            dog.setName("Dog " + i);
            owner.getDogs().add(dog);
        }
        testRealm.commitTransaction();
    }

    private RealmList<Dog> createNonManagedDogList() {
        RealmList<Dog> list = new RealmList<Dog>();
        for (int i = 0; i < TEST_OBJECTS; i++) {
            list.add(new Dog("Dog " + i));
        }
        return list;
    }

    // Check that all methods work correctly on a empty RealmList
    private void checkMethodsOnEmptyList(Realm realm, RealmList<Dog> list) {
        realm.beginTransaction();
        for (int i = 0; i < 4; i++) {
            try {
                switch(i) {
                    case 0: list.get(0); break;
                    case 1: list.remove(0); break;
                    case 2: list.set(0, new Dog()); break;
                    case 3: list.move(0,0); break;
                }
                fail();
            } catch (IndexOutOfBoundsException expected) {
            } catch (RealmException expected) {
            }
        }
        realm.cancelTransaction();

        assertEquals(0, list.size());
        assertNull(list.first());
        assertNull(list.last());
    }

    @Override
    protected void tearDown() throws Exception {
        testRealm.close();
    }

    /*********************************************************
     * Non-Managed mode tests                                *
     *********************************************************/

    public void testPublicNoArgConstructor() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        assertNotNull(list);
    }

    public void testUnavailableMethods_nonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        try {
            list.where();
            fail("where() should fail in non-managed mode.");
        } catch (RealmException ignore) {
        }
    }

    public void testAdd_nonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        AllTypes object = new AllTypes();
        object.setColumnString("String");
        list.add(object);
        assertEquals(1, list.size());
        assertEquals(object, list.get(0));
    }

    public void testAddNull_nonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        try {
            list.add(null);
            fail("Adding null should not be be allowed");
        } catch (IllegalArgumentException ignore) {
        }
    }

    public void testAddManagedObject_nonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        testRealm.beginTransaction();
        AllTypes managedAllTypes =  testRealm.createObject(AllTypes.class);
        testRealm.commitTransaction();
        list.add(managedAllTypes);

        assertEquals(managedAllTypes, list.get(0));
    }

    public void testAddAtIndex_nonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        AllTypes object = new AllTypes();
        object.setColumnString("String");
        list.add(0, object);
        assertEquals(1, list.size());
        assertEquals(object, list.get(0));
    }

    public void testAddManagedObjectAtIndex_nonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        list.add(new AllTypes());
        testRealm.beginTransaction();
        AllTypes managedAllTypes = testRealm.createObject(AllTypes.class);
        testRealm.commitTransaction();
        list.add(0, managedAllTypes);

        assertEquals(managedAllTypes, list.get(0));
    }

    public void testAddNullAtIndex_nonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        try {
            list.add(null);
            fail("Adding null should not be be allowed");
        } catch (IllegalArgumentException ignore) {
        }
    }

    public void testSet_nonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        list.add(new AllTypes());
        list.set(0, new AllTypes());
        assertEquals(1, list.size());
    }

    public void testSetNull_nonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        list.add(new AllTypes());
        try {
            list.set(0, null);
            fail("Setting a null value should result in a exception");
        } catch (IllegalArgumentException ignore) {
        }
    }

    public void testSetManagedObject_nonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        list.add(new AllTypes());
        testRealm.beginTransaction();
        AllTypes managedAllTypes = testRealm.createObject(AllTypes.class);
        testRealm.commitTransaction();
        list.set(0, managedAllTypes);

        assertEquals(managedAllTypes, list.get(0));
    }

    public void testClear_nonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
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

    // Test move where oldPosition > newPosition
    public void testMoveDown() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        Dog dog1 = owner.getDogs().get(1);
        testRealm.beginTransaction();
        owner.getDogs().move(1, 0);
        testRealm.commitTransaction();

        assertEquals(0, owner.getDogs().indexOf(dog1));
    }

    // Test move where oldPosition < newPosition
    public void testMoveUp() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        int oldIndex = TEST_OBJECTS / 2;
        int newIndex = oldIndex + 1;
        Dog dog = owner.getDogs().get(oldIndex);
        testRealm.beginTransaction();
        owner.getDogs().move(oldIndex, newIndex); // This doesn't do anything as oldIndex is now empty so the index's above gets shifted to the left.
        testRealm.commitTransaction();

        assertEquals(TEST_OBJECTS, owner.getDogs().size());
        assertEquals(oldIndex, owner.getDogs().indexOf(dog));
    }

    public void testFirstAndLast_nonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        AllTypes object1 = new AllTypes();
        AllTypes object2 = new AllTypes();
        list.add(object1);
        list.add(object2);

        assertEquals(object1, list.first());
        assertEquals(object2, list.last());
    }

    public void testEmptyList_nonManagedMode() {
        RealmList<Dog> list = new RealmList<Dog>();
        checkMethodsOnEmptyList(testRealm, list);
    }

    /*********************************************************
     * Managed mode tests                                    *
     *********************************************************/

    // Test move where oldPosition > newPosition
    public void testMoveDown_nonManagedMode() {
        RealmList<Dog> dogs = createNonManagedDogList();
        Dog dog1 = dogs.get(1);
        dogs.move(1, 0);

        assertEquals(0, dogs.indexOf(dog1));
    }

    // Test move where oldPosition < newPosition
    public void testMoveUp_nonManagedMode() {
        RealmList<Dog> dogs = createNonManagedDogList();
        int oldIndex = TEST_OBJECTS / 2;
        int newIndex = oldIndex + 1;
        Dog dog = dogs.get(oldIndex);
        dogs.move(oldIndex, newIndex); // This doesn't do anything as oldIndex is now empty so the index's above gets shifted to the left.

        assertEquals(TEST_OBJECTS, dogs.size());
        assertEquals(oldIndex, dogs.indexOf(dog));
    }

    public void testMoveOutOfBoundsLowerThrows() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        testRealm.beginTransaction();
        try {
            owner.getDogs().move(0, -1);
            fail("Indexes < 0 should throw an exception");
        } catch (IndexOutOfBoundsException ignored) {
        } finally {
            testRealm.cancelTransaction();
        }
    }

    public void testMoveOutOfBoundsHigherThrows() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        testRealm.beginTransaction();
        try {
            int lastIndex = TEST_OBJECTS - 1;
            int outOfBoundsIndex = TEST_OBJECTS;
            owner.getDogs().move(lastIndex, outOfBoundsIndex);
            fail("Indexes >= size() should throw an exception");
        } catch (IndexOutOfBoundsException ignored) {
            ignored.printStackTrace();
        } finally {
            testRealm.cancelTransaction();
        }
    }

    public void testAddObject() {
        testRealm.beginTransaction();
        testRealm.clear(Owner.class);
        Owner owner = testRealm.createObject(Owner.class);
        Dog dog = testRealm.createObject(Dog.class);
        owner.getDogs().add(dog);
        testRealm.commitTransaction();

        assertEquals(1, testRealm.where(Owner.class).findFirst().getDogs().size());
    }

    public void testAddObjectNullThrows() {
        testRealm.beginTransaction();
        Owner owner = testRealm.createObject(Owner.class);
        try {
            owner.getDogs().add(null);
            fail("Adding null values is not supported");
        } catch (IllegalArgumentException ignored) {
        } finally {
            testRealm.cancelTransaction();
        }
    }

    public void testSize() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        assertEquals(TEST_OBJECTS, owner.getDogs().size());
    }

    public void testGetObjects() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();

        assertNotNull(dogs);
        assertEquals("Dog 1", dogs.get(1).getName());
    }

    public void testFirstLast() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();

        assertEquals("Dog 0", dogs.first().getName());
        assertEquals("Dog " + (TEST_OBJECTS - 1), dogs.last().getName());
    }

    public void testRemoveByIndex() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();
        Dog dog5 = dogs.get(5);

        testRealm.beginTransaction();
        Dog removedDog = dogs.remove(5);
        testRealm.commitTransaction();

        assertEquals(dog5, removedDog);
        assertEquals(TEST_OBJECTS - 1, dogs.size());
    }

    public void testRemoveLast() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();

        testRealm.beginTransaction();
        dogs.remove(TEST_OBJECTS - 1);
        testRealm.commitTransaction();

        assertEquals(TEST_OBJECTS - 1, dogs.size());
    }

    public void testRemoveFromEmptyListThrows() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();

        testRealm.beginTransaction();
        dogs.clear();
        try {
            dogs.remove(0);
        } catch (IndexOutOfBoundsException expected) {
            return;
        } finally {
            testRealm.cancelTransaction();
        }
        fail("Calling remove() should fail on an empty list.");
    }

    public void testRemoveByObject() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();
        Dog dog = dogs.get(0);

        testRealm.beginTransaction();
        boolean result = dogs.remove(dog);
        testRealm.commitTransaction();

        assertTrue(result);
        assertEquals(TEST_OBJECTS - 1, dogs.size());
    }

    public void testQuery() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();
        Dog firstDog = dogs.where().equalTo("name", "Dog 0").findFirst();

        assertNotNull(firstDog);
    }

    public void testEmptyListMethods() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        testRealm.beginTransaction();
        owner.getDogs().clear();
        testRealm.commitTransaction();

        checkMethodsOnEmptyList(testRealm, owner.getDogs());
    }

    public void testClear() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        testRealm.beginTransaction();
        assertEquals(TEST_OBJECTS, owner.getDogs().size());
        owner.getDogs().clear();
        assertEquals(0, owner.getDogs().size());
        testRealm.commitTransaction();
    }

    public void testClearNotDeleting() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        testRealm.beginTransaction();
        assertEquals(TEST_OBJECTS, testRealm.allObjects(Dog.class).size());
        owner.getDogs().clear();
        assertEquals(TEST_OBJECTS, testRealm.allObjects(Dog.class).size());
        testRealm.commitTransaction();
    }

    // Test that all methods that require a write transaction (ie. any function that mutates Realm data)
    public void testMutableMethodsOutsideWriteTransactions() {
        testRealm.beginTransaction();
        RealmList<Dog> list = testRealm.createObject(AllTypes.class).getColumnRealmList();
        Dog dog = testRealm.createObject(Dog.class);
        list.add(dog);
        testRealm.commitTransaction();

        try { list.add(dog);    fail(); } catch (IllegalStateException expected) {}
        try { list.add(0, dog); fail(); } catch (IllegalStateException expected) {}
        try { list.clear();     fail(); } catch (IllegalStateException expected) {}
        try { list.move(0, 1);  fail(); } catch (IllegalStateException expected) {}
        try { list.remove(0);   fail(); } catch (IllegalStateException expected) {}
        try { list.set(0, dog); fail(); } catch (IllegalStateException expected) {}
    }

    public void testSettingListClearsOldItems() {
        testRealm.beginTransaction();
        CyclicType one = testRealm.copyToRealm(new CyclicType());
        CyclicType two = testRealm.copyToRealm(new CyclicType());
        two.setObjects(new RealmList<CyclicType>(one));
        two.setObjects(new RealmList<CyclicType>(one));
        testRealm.commitTransaction();

        assertEquals(1, two.getObjects().size());
    }
}
