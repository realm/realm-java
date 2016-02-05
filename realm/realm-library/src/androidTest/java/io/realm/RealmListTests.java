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
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.realm.entities.AllTypes;
import io.realm.entities.CyclicType;
import io.realm.entities.CyclicTypePrimaryKey;
import io.realm.entities.Dog;
import io.realm.entities.Owner;
import io.realm.exceptions.RealmException;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmListTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final int TEST_OBJECTS = 10;
    private Realm testRealm;

    @Before
    public void setUp() throws Exception {
        RealmConfiguration realmConfig = configFactory.createConfiguration();
        testRealm = Realm.getInstance(realmConfig);

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

    @After
    public void tearDown() throws Exception {
        if (testRealm != null) {
            testRealm.close();
        }
    }

    private RealmList<Dog> createNonManagedDogList() {
        RealmList<Dog> list = new RealmList<Dog>();
        for (int i = 0; i < TEST_OBJECTS; i++) {
            list.add(new Dog("Dog " + i));
        }
        return list;
    }

    private RealmList<Dog> createDeletedRealmList() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();

        testRealm.beginTransaction();
        owner.removeFromRealm();
        testRealm.commitTransaction();
        return dogs;
    }

    // Check that all methods work correctly on a empty RealmList
    private void checkMethodsOnEmptyList(Realm realm, RealmList<Dog> list) {
        realm.beginTransaction();
        for (int i = 0; i < 4; i++) {
            try {
                switch (i) {
                    case 0: list.get(0); break;
                    case 1: list.remove(0); break;
                    case 2: list.set(0, new Dog()); break;
                    case 3: list.move(0, 0); break;
                    default: break;
                }
                fail();
            } catch (IndexOutOfBoundsException ignored) {
            } catch (RealmException ignored) {
            }
        }
        realm.cancelTransaction();

        assertEquals(0, list.size());
        assertNull(list.first());
        assertNull(list.last());
    }

    /*********************************************************
     * Non-Managed mode tests                                *
     *********************************************************/

    @Test
    public void isValid_nonManagedMode() {
        //noinspection MismatchedQueryAndUpdateOfCollection
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        assertFalse(list.isValid());
    }

    @Test (expected = RealmException.class)
    public void where_nonManagedMode() {
        new RealmList<AllTypes>().where();
    }

    @Test
    public void add_nonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        AllTypes object = new AllTypes();
        object.setColumnString("String");
        list.add(object);
        assertEquals(1, list.size());
        assertEquals(object, list.get(0));
    }

    @Test (expected = IllegalArgumentException.class)
    public void add_nullInNonManagedMode() {
        new RealmList<AllTypes>().add(null);
    }

    @Test
    public void add_managedObjectInNonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        testRealm.beginTransaction();
        AllTypes managedAllTypes = testRealm.createObject(AllTypes.class);
        testRealm.commitTransaction();
        list.add(managedAllTypes);

        assertEquals(managedAllTypes, list.get(0));
    }

    @Test
    public void add_standaloneObjectAtIndexInNonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        AllTypes object = new AllTypes();
        object.setColumnString("String");
        list.add(0, object);
        assertEquals(1, list.size());
        assertEquals(object, list.get(0));
    }

    @Test
    public void add_ManagedObjectAtIndexInNonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        list.add(new AllTypes());
        testRealm.beginTransaction();
        AllTypes managedAllTypes = testRealm.createObject(AllTypes.class);
        testRealm.commitTransaction();
        list.add(0, managedAllTypes);

        assertEquals(managedAllTypes, list.get(0));
    }

    @Test (expected = IllegalArgumentException.class)
    public void add_nullAtIndexInNonManagedMode() {
        new RealmList<AllTypes>().add(0, null);
    }

    @Test
    public void set_nonManagedMode() {
        RealmList<Dog> list = new RealmList<Dog>();
        Dog dog1 = new Dog("dog1");
        Dog dog2 = new Dog("dog2");
        list.add(dog1);
        assertEquals(dog1, list.set(0, dog2));
        assertEquals(1, list.size());
    }

    @Test
    public void set_managedMode() {
        testRealm.beginTransaction();
        try {
            RealmList<Dog> list = testRealm.createObject(Owner.class).getDogs();
            Dog dog1 = testRealm.createObject(Dog.class);
            dog1.setName("dog1");
            Dog dog2 = testRealm.createObject(Dog.class);
            dog2.setName("dog2");
            list.add(dog1);
            assertEquals(dog1, list.set(0, dog2));
            assertEquals(1, list.size());
        } finally {
            testRealm.cancelTransaction();
        }
    }

    @Test
    public void set_nullInNonManagedMode() {
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        list.add(new AllTypes());
        thrown.expect(IllegalArgumentException.class);
        list.set(0, null);
    }

    @Test
    public void set_managedObjectInNonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        list.add(new AllTypes());
        testRealm.beginTransaction();
        AllTypes managedAllTypes = testRealm.createObject(AllTypes.class);
        testRealm.commitTransaction();
        list.set(0, managedAllTypes);

        assertEquals(managedAllTypes, list.get(0));
    }

    @Test
    public void clear_nonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        list.add(new AllTypes());
        assertEquals(1, list.size());
        list.clear();
        assertTrue(list.isEmpty());
    }

    @Test
    public void remove_nonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        AllTypes object1 = new AllTypes();
        list.add(object1);
        AllTypes object2 = list.remove(0);
        assertEquals(object1, object2);
    }

    @Test
    public void get_nonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        AllTypes object1 = new AllTypes();
        list.add(object1);
        AllTypes object2 = list.get(0);
        assertEquals(object1, object2);
    }

    @Test
    public void size_nonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        list.add(new AllTypes());
        assertEquals(1, list.size());
    }

    // Test move where oldPosition > newPosition
    @Test
    public void move_down() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        Dog dog1 = owner.getDogs().get(1);
        testRealm.beginTransaction();
        owner.getDogs().move(1, 0);
        testRealm.commitTransaction();

        assertEquals(0, owner.getDogs().indexOf(dog1));
    }

    // Test move where oldPosition < newPosition
    @Test
    public void move_up() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        int oldIndex = TEST_OBJECTS / 2;
        int newIndex = oldIndex + 1;
        Dog dog = owner.getDogs().get(oldIndex);
        testRealm.beginTransaction();
        owner.getDogs().move(oldIndex, newIndex); // This doesn't do anything as oldIndex is now empty so the index's above gets shifted to the left.
        testRealm.commitTransaction();

        assertEquals(TEST_OBJECTS, owner.getDogs().size());
        assertEquals(newIndex, owner.getDogs().indexOf(dog));
    }

    @Test
    public void first_nonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        AllTypes object1 = new AllTypes();
        AllTypes object2 = new AllTypes();
        list.add(object1);
        list.add(object2);

        assertEquals(object1, list.first());
    }

    @Test
    public void last_nonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        AllTypes object1 = new AllTypes();
        AllTypes object2 = new AllTypes();
        list.add(object1);
        list.add(object2);

        assertEquals(object2, list.last());
    }

    @Test
    public void allMethods_emptyListInNonManagedMode() {
        RealmList<Dog> list = new RealmList<Dog>();
        checkMethodsOnEmptyList(testRealm, list);
    }

    // Test move where oldPosition > newPosition
    @Test
    public void move_downInNonManagedMode() {
        RealmList<Dog> dogs = createNonManagedDogList();
        Dog dog1 = dogs.get(1);
        dogs.move(1, 0);

        assertEquals(0, dogs.indexOf(dog1));
    }

    // Test move where oldPosition < newPosition
    @Test
    public void move_upInNonManagedMode() {
        RealmList<Dog> dogs = createNonManagedDogList();
        int oldIndex = TEST_OBJECTS / 2;
        int newIndex = oldIndex + 1;
        Dog dog = dogs.get(oldIndex);
        dogs.move(oldIndex, newIndex); // This doesn't do anything as oldIndex is now empty so the index's above gets shifted to the left.

        assertEquals(TEST_OBJECTS, dogs.size());
        assertEquals(oldIndex, dogs.indexOf(dog));
    }

    /*********************************************************
     * Managed mode tests                                    *
     *********************************************************/

    @Test
    public void isValid() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();

        assertTrue(dogs.isValid());

        testRealm.close();
        assertFalse(dogs.isValid());
    }

    @Test
    public void isValid_whenParentRemoved() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();

        testRealm.beginTransaction();
        owner.removeFromRealm();
        testRealm.commitTransaction();

        // RealmList contained in removed object is invalid.
        assertFalse(dogs.isValid());
    }

    @Test
    public void move_outOfBoundsLowerThrows() {
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

    @Test
    public void move_outOfBoundsHigherThrows() {
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

    @Test
    public void add_managedObjectToManagedList() {
        testRealm.beginTransaction();
        testRealm.clear(Owner.class);
        Owner owner = testRealm.createObject(Owner.class);
        Dog dog = testRealm.createObject(Dog.class);
        owner.getDogs().add(dog);
        testRealm.commitTransaction();

        assertEquals(1, testRealm.where(Owner.class).findFirst().getDogs().size());
    }

    // Test that add correctly uses Realm.copyToRealm() on standalone objects.
    @Test
    public void add_nonManagedObjectToManagedList() {
        testRealm.beginTransaction();
        CyclicType parent = testRealm.createObject(CyclicType.class);
        RealmList<CyclicType> children = parent.getObjects();
        children.add(new CyclicType());
        testRealm.commitTransaction();
        assertEquals(1, testRealm.where(CyclicType.class).findFirst().getObjects().size());
    }

    // Make sure that standalone objects with a primary key are added using copyToRealmOrUpdate
    @Test
    public void add_nonManagedPrimaryKeyObjectToManagedList() {
        testRealm.beginTransaction();
        testRealm.copyToRealm(new CyclicTypePrimaryKey(2, "original"));
        RealmList<CyclicTypePrimaryKey> children = testRealm.copyToRealm(new CyclicTypePrimaryKey(1)).getObjects();
        children.add(new CyclicTypePrimaryKey(2, "new"));
        testRealm.commitTransaction();

        assertEquals(1, testRealm.where(CyclicTypePrimaryKey.class).equalTo("id", 1).findFirst().getObjects().size());
        assertEquals("new", testRealm.where(CyclicTypePrimaryKey.class).equalTo("id", 2).findFirst().getName());
    }

    // Test that set correctly uses Realm.copyToRealm() on standalone objects.
    @Test
    public void set_nonManagedObjectToManagedList() {
        testRealm.beginTransaction();
        CyclicType parent = testRealm.copyToRealm(new CyclicType("Parent"));
        RealmList<CyclicType> children = parent.getObjects();
        children.add(new CyclicType());
        children.add(new CyclicType("original"));
        children.add(new CyclicType());
        children.set(1, new CyclicType("updated"));
        testRealm.commitTransaction();

        RealmList<CyclicType> list = testRealm.where(CyclicType.class).findFirst().getObjects();
        assertEquals(3, list.size());
        assertEquals("updated", list.get(1).getName());
        assertEquals(5, testRealm.where(CyclicType.class).count());
    }

    // Test that set correctly uses Realm.copyToRealmOrUpdate() on standalone objects with a primary key.
    @Test
    public void  set_nonManagedPrimaryKeyObjectToManagedList() {
        testRealm.beginTransaction();
        CyclicTypePrimaryKey parent = testRealm.copyToRealm(new CyclicTypePrimaryKey(1, "Parent"));
        RealmList<CyclicTypePrimaryKey> children = parent.getObjects();
        children.add(new CyclicTypePrimaryKey(2));
        children.add(new CyclicTypePrimaryKey(3, "original"));
        children.add(new CyclicTypePrimaryKey(4));
        children.set(1, new CyclicTypePrimaryKey(3, "updated"));
        testRealm.commitTransaction();

        RealmList<CyclicTypePrimaryKey> list = testRealm.where(CyclicTypePrimaryKey.class).findFirst().getObjects();
        assertEquals(3, list.size());
        assertEquals("updated", list.get(1).getName());
    }

    @Test
    public void add_nullToManagedListThrows() {
        testRealm.beginTransaction();
        Owner owner = testRealm.createObject(Owner.class);
        thrown.expect(IllegalArgumentException.class);
        owner.getDogs().add(null);
    }

    @Test
    public void size() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        assertEquals(TEST_OBJECTS, owner.getDogs().size());
    }

    @Test
    public void getObjects() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();

        assertNotNull(dogs);
        assertEquals("Dog 1", dogs.get(1).getName());
    }

    @Test
    public void first() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();

        assertEquals("Dog 0", dogs.first().getName());
    }

    @Test
    public void last() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();

        assertEquals("Dog " + (TEST_OBJECTS - 1), dogs.last().getName());
    }

    @Test
    public void remove_byIndex() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();
        Dog dog5 = dogs.get(5);

        testRealm.beginTransaction();
        Dog removedDog = dogs.remove(5);
        testRealm.commitTransaction();

        assertEquals(dog5, removedDog);
        assertEquals(TEST_OBJECTS - 1, dogs.size());
    }

    @Test
    public void remove_last() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();

        testRealm.beginTransaction();
        dogs.remove(TEST_OBJECTS - 1);
        testRealm.commitTransaction();

        assertEquals(TEST_OBJECTS - 1, dogs.size());
    }

    @Test
    public void remove_fromEmptyListThrows() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();

        testRealm.beginTransaction();
        dogs.clear();
        thrown.expect(IndexOutOfBoundsException.class);
        dogs.remove(0);
    }

    @Test
    public void remove_byObject() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();
        Dog dog = dogs.get(0);

        testRealm.beginTransaction();
        boolean result = dogs.remove(dog);
        testRealm.commitTransaction();

        assertTrue(result);
        assertEquals(TEST_OBJECTS - 1, dogs.size());
    }

    @Test
    public void add_atAfterContainerObjectRemoved() {
        RealmList<Dog> dogs = createDeletedRealmList();

        testRealm.beginTransaction();
        Dog dog = testRealm.createObject(Dog.class);
        dog.setName("Dog");
        thrown.expect(IllegalStateException.class);
        dogs.add(0, dog);
    }

    @Test
    public void add_afterContainerObjectRemoved() {
        RealmList<Dog> dogs = createDeletedRealmList();

        testRealm.beginTransaction();
        Dog dog = testRealm.createObject(Dog.class);
        dog.setName("Dog");
        thrown.expect(IllegalStateException.class);
        dogs.add(dog);
    }

    @Test
    public void set_afterContainerObjectRemoved() {
        RealmList<Dog> dogs = createDeletedRealmList();

        testRealm.beginTransaction();
        Dog dog = testRealm.createObject(Dog.class);
        dog.setName("Dog");
        thrown.expect(IllegalStateException.class);
        dogs.set(0, dog);
    }

    @Test
    public void move_afterContainerObjectRemoved() {
        RealmList<Dog> dogs = createDeletedRealmList();

        testRealm.beginTransaction();
        thrown.expect(IllegalStateException.class);
        dogs.move(0, 1);
    }

    @Test
    public void clear_afterContainerObjectRemoved() {
        RealmList<Dog> dogs = createDeletedRealmList();

        testRealm.beginTransaction();
        thrown.expect(IllegalStateException.class);
        dogs.clear();
    }

    @Test
    public void remove_atAfterContainerObjectRemoved() {
        RealmList<Dog> dogs = createDeletedRealmList();

        testRealm.beginTransaction();
        Dog dog = testRealm.createObject(Dog.class);
        dog.setName("Dog");
        thrown.expect(IllegalStateException.class);
        dogs.remove(0);
    }

    @Test
    public void remove_bbjectAfterContainerObjectRemoved() {
        RealmList<Dog> dogs = createDeletedRealmList();

        testRealm.beginTransaction();
        Dog dog = testRealm.createObject(Dog.class);
        dog.setName("Dog");
        thrown.expect(IllegalStateException.class);
        dogs.remove(dog);
    }

    @Test
    public void remove_allAfterContainerObjectRemoved() {
        RealmList<Dog> dogs = createDeletedRealmList();

        testRealm.beginTransaction();
        thrown.expect(IllegalStateException.class);
        dogs.removeAll(Collections.<Dog>emptyList());
    }

    @Test
    public void get_afterContainerObjectRemoved() {
        RealmList<Dog> dogs = createDeletedRealmList();
        thrown.expect(IllegalStateException.class);
        dogs.get(0);
    }

    @Test
    public void first_afterContainerObjectRemoved() {
        RealmList<Dog> dogs = createDeletedRealmList();

        thrown.expect(IllegalStateException.class);
        dogs.first();
    }

    @Test
    public void last_afterContainerObjectRemoved() {
        RealmList<Dog> dogs = createDeletedRealmList();

        thrown.expect(IllegalStateException.class);
        dogs.last();
    }

    @Test
    public void size_afterContainerObjectRemoved() {
        RealmList<Dog> dogs = createDeletedRealmList();

        thrown.expect(IllegalStateException.class);
        dogs.size();
    }

    @Test
    public void where_afterContainerObjectRemoved() {
        RealmList<Dog> dogs = createDeletedRealmList();

        thrown.expect(IllegalStateException.class);
        dogs.where();
    }

    @Test
    public void toString_AfterContainerObjectRemoved() {
        RealmList<Dog> dogs = createDeletedRealmList();

        assertEquals("Dog@[invalid]", dogs.toString());
    }

    @Test
    public void query() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();
        Dog firstDog = dogs.where().equalTo("name", "Dog 0").findFirst();

        assertNotNull(firstDog);
    }

    @Test
    public void allMethods_emptyListInManagedMode() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        testRealm.beginTransaction();
        owner.getDogs().clear();
        testRealm.commitTransaction();

        checkMethodsOnEmptyList(testRealm, owner.getDogs());
    }

    @Test
    public void clear() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        testRealm.beginTransaction();
        assertEquals(TEST_OBJECTS, owner.getDogs().size());
        owner.getDogs().clear();
        assertEquals(0, owner.getDogs().size());
        testRealm.commitTransaction();
    }

    @Test
    public void clear_NotDeleting() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        testRealm.beginTransaction();
        assertEquals(TEST_OBJECTS, testRealm.allObjects(Dog.class).size());
        owner.getDogs().clear();
        assertEquals(TEST_OBJECTS, testRealm.allObjects(Dog.class).size());
        testRealm.commitTransaction();
    }

    @Test
    public void contains() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        Dog dog = owner.getDogs().get(0);
        assertTrue("Should contain a particular dog.", owner.getDogs().contains(dog));
    }

    /**
     * Test to see if a particular item that does exist in the same Realm does not
     * exist in a query that excludes said item.
     */
    @Test
    public void contains_sameRealmNotContained() {
        RealmResults<Dog> dogs = testRealm.where(Dog.class)
                .equalTo("name", "Dog 1").or().equalTo("name", "Dog 2").findAll();
        Dog thirdDog = testRealm.where(Dog.class)
                .equalTo("name", "Dog 3").findFirst();
        assertFalse("Should not contain a particular dog.", dogs.contains(thirdDog));
    }

    @Test
    public void contains_inNonManagedList() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> managedDogs = owner.getDogs();
        // Create a non-managed RealmList
        RealmList<Dog> nonManagedDogs
                = new RealmList<Dog>(managedDogs.toArray(new Dog[managedDogs.size()]));
        Dog dog = managedDogs.get(0);
        assertTrue("Should contain a particular dog", nonManagedDogs.contains(dog));
    }

    @Test
    public void contains_null() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        assertFalse("Should not contain a null item.", owner.getDogs().contains(null));
    }

    /**
     * Test that the {@link Realm#contains(Class)} method of one Realm will not contain a
     * {@link RealmObject} from another Realm.
     */
    @Test
    public void contains_doesNotContainAnItem() {
        RealmConfiguration realmConfig = configFactory.createConfiguration("contains_test.realm");
        Realm testRealmTwo = Realm.getInstance(realmConfig);
        try {
            // Set up the test realm
            testRealmTwo.beginTransaction();
            Owner owner2 = testRealmTwo.createObject(Owner.class);
            owner2.setName("Owner");
            for (int i = 0; i < TEST_OBJECTS; i++) {
                Dog dog = testRealmTwo.createObject(Dog.class);
                dog.setName("Dog " + i);
                owner2.getDogs().add(dog);
            }
            testRealmTwo.commitTransaction();

            // Get a dog from the test realm.
            Dog dog2 = testRealmTwo.where(Owner.class).findFirst().getDogs().get(0);

            // Access the original Realm. Then see if the above dog object is contained. (It shouldn't).
            Owner owner1 = testRealm.where(Owner.class).findFirst();

            assertFalse("Should not be able to find one object in another Realm via contains",
                    owner1.getDogs().contains(dog2));
        } finally {
            if (testRealmTwo != null && !testRealmTwo.isClosed()) {
                testRealmTwo.close();
            }
        }
    }

    @Test
    public void contains_shouldNotContainDeletedRealmObject() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();
        Dog dog1 = dogs.get(0);
        testRealm.beginTransaction();
        dog1.removeFromRealm();
        testRealm.commitTransaction();
        assertFalse("Should not contain a deleted RealmObject", dogs.contains(dog1));
    }

    // Test that all methods that require a transaction (ie. any function that mutates Realm data)
    @Test
    public void mutableMethods_OutsideTransactions() {
        testRealm.beginTransaction();
        RealmList<Dog> list = testRealm.createObject(AllTypes.class).getColumnRealmList();
        Dog dog = testRealm.createObject(Dog.class);
        list.add(dog);
        testRealm.commitTransaction();

        try { list.add(dog);    fail(); } catch (IllegalStateException ignored) {}
        try { list.add(0, dog); fail(); } catch (IllegalStateException ignored) {}
        try { list.clear();     fail(); } catch (IllegalStateException ignored) {}
        try { list.move(0, 1);  fail(); } catch (IllegalStateException ignored) {}
        try { list.remove(0);   fail(); } catch (IllegalStateException ignored) {}
        try { list.set(0, dog); fail(); } catch (IllegalStateException ignored) {}
    }

    private enum Method {
        METHOD_ADD,
        METHOD_ADD_AT,
        METHOD_CLEAR,
        METHOD_MOVE,
        METHOD_REMOVE,
        METHOD_SET
    }

    // Calling methods from the wrong thread should fail
    private boolean methodWrongThread(final Method method) throws InterruptedException, ExecutionException {
        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        testRealm.clear(Dog.class);
        final RealmList<Dog> list = testRealm.createObject(AllTypes.class).getColumnRealmList();
        Dog dog = testRealm.createObject(Dog.class);
        list.add(dog);
        testRealm.commitTransaction();

        testRealm.beginTransaction(); // Make sure that a valid transaction has begun on the correct thread
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    switch (method) {
                        case METHOD_ADD:
                            list.add(new Dog());
                            break;
                        case METHOD_ADD_AT:
                            list.add(1, new Dog());
                            break;
                        case METHOD_CLEAR:
                            list.clear();
                            break;
                        case METHOD_MOVE:
                            list.add(new Dog());
                            list.move(0, 1);
                            break;
                        case METHOD_REMOVE:
                            list.remove(0);
                            break;
                        case METHOD_SET:
                            list.set(0, new Dog());
                            break;
                    }
                    return false;
                } catch (IllegalStateException ignored) {
                    return true;
                }
            }
        });

        boolean result = future.get();
        testRealm.cancelTransaction();
        return result;
    }

    @Test
    public void methods_ThrowOnWrongThread() throws ExecutionException, InterruptedException {
        for (Method method : Method.values()) {
            assertTrue(method.toString(), methodWrongThread(method));
        }
    }

    @Test
    public void setList_ClearsOldItems() {
        testRealm.beginTransaction();
        CyclicType one = testRealm.copyToRealm(new CyclicType());
        CyclicType two = testRealm.copyToRealm(new CyclicType());
        two.setObjects(new RealmList<CyclicType>(one));
        two.setObjects(new RealmList<CyclicType>(one));
        testRealm.commitTransaction();

        assertEquals(1, two.getObjects().size());
    }

    @Test
    public void testRemoveAllFromRealm() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();
        assertEquals(TEST_OBJECTS, dogs.size());

        testRealm.beginTransaction();
        dogs.removeAllFromRealm();
        testRealm.commitTransaction();
        assertEquals(0, dogs.size());
        assertEquals(0, testRealm.where(Dog.class).count());
    }

    @Test
    public void testRealmRemoveAllNotManagedList() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();
        assertEquals(TEST_OBJECTS, dogs.size());

        RealmList<Dog> notManagedDogs = new RealmList<Dog>();
        for (Dog dog : dogs) {
            notManagedDogs.add(dog);
        }

        testRealm.beginTransaction();
        notManagedDogs.removeAllFromRealm();
        testRealm.commitTransaction();
        assertEquals(0, dogs.size());
        assertEquals(0, notManagedDogs.size());
        assertEquals(0, testRealm.where(Dog.class).count());
    }

    @Test
    public void testRealmRemoveAllOutsideTransaction() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();
        try {
            dogs.removeAllFromRealm();
            fail("removeAllFromRealm should be called in a transaction.");
        } catch (IllegalStateException e) {
            assertEquals("Changing Realm data can only be done from inside a transaction.", e.getMessage());
        }
    }

    @Test
    public void testRemoveAllFromListStandaloneObjectShouldThrow() {
        final RealmList<Dog> list = new RealmList<Dog>();

        testRealm.beginTransaction();
        Dog dog1 = testRealm.where(Dog.class).findFirst();
        testRealm.commitTransaction();
        Dog dog2 = new Dog();

        list.add(dog1);
        list.add(dog2);

        testRealm.beginTransaction();
        try {
            list.removeAllFromRealm();
            fail("Cannot remove a list with a standalone object in it!");
        } catch (IllegalStateException e) {
            assertEquals("Object malformed: missing object in Realm. Make sure to instantiate RealmObjects with" +
                    " Realm.createObject()", e.getMessage());
        } finally {
            testRealm.cancelTransaction();
        }

        assertEquals(TEST_OBJECTS, testRealm.where(Dog.class).count());
        assertEquals(2, list.size());
    }

    @Test
    public void testRemoveAllFromRealmEmptyList() {
        RealmList<Dog> dogs = testRealm.where(Owner.class).findFirst().getDogs();
        assertEquals(TEST_OBJECTS, dogs.size());

        testRealm.beginTransaction();
        dogs.removeAllFromRealm();
        testRealm.commitTransaction();
        assertEquals(0, dogs.size());
        assertEquals(0, testRealm.where(Dog.class).count());

        // The dogs is empty now.
        testRealm.beginTransaction();
        dogs.removeAllFromRealm();
        testRealm.commitTransaction();
        assertEquals(0, dogs.size());
        assertEquals(0, testRealm.where(Dog.class).count());

    }

    @Test
    public void testRemoveAllFromRealmInvalidListShouldThrow() {
        RealmList<Dog> dogs = testRealm.where(Owner.class).findFirst().getDogs();
        assertEquals(TEST_OBJECTS, dogs.size());
        testRealm.close();
        testRealm = null;

        try {
            dogs.removeAllFromRealm();
            fail("dogs is invalid and it should throw an exception");
        } catch (IllegalStateException e) {
            assertEquals("This Realm instance has already been closed, making it unusable.", e.getMessage());
        }
    }
}
