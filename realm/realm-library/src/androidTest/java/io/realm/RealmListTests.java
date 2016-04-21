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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import io.realm.entities.AllTypes;
import io.realm.entities.Cat;
import io.realm.entities.CyclicType;
import io.realm.entities.CyclicTypePrimaryKey;
import io.realm.entities.Dog;
import io.realm.entities.Owner;
import io.realm.internal.RealmObjectProxy;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests specific for RealmList that cannot be covered by {@link OrderedRealmCollectionTests},
 * {@link ManagedRealmCollectionTests}, {@link UnManagedRealmCollectionTests} or {@link RealmCollectionTests}.
 */
@RunWith(AndroidJUnit4.class)
public class RealmListTests extends CollectionTests {

    private static final int TEST_SIZE = 10;

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Realm realm;
    private RealmList<Dog> collection;

    @Before
    public void setUp() throws Exception {
        RealmConfiguration realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);

        realm.beginTransaction();
        Owner owner = realm.createObject(Owner.class);
        owner.setName("Owner");
        for (int i = 0; i < TEST_SIZE; i++) {
            Dog dog = realm.createObject(Dog.class);
            dog.setName("Dog " + i);
            owner.getDogs().add(dog);
        }
        realm.commitTransaction();
        collection = owner.getDogs();
    }

    @After
    public void tearDown() throws Exception {
        if (realm != null) {
            realm.close();
        }
    }

    private RealmList<Dog> createNonManagedDogList() {
        RealmList<Dog> list = new RealmList<Dog>();
        for (int i = 0; i < TEST_SIZE; i++) {
            list.add(new Dog("Dog " + i));
        }
        return list;
    }

    private RealmList<Dog> createDeletedRealmList() {
        Owner owner = realm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();

        realm.beginTransaction();
        owner.deleteFromRealm();
        realm.commitTransaction();
        return dogs;
    }

            //noinspection TryWithIdenticalCatches
    /*********************************************************
     * Un-managed mode tests                                *
     *********************************************************/

    @Test(expected = IllegalArgumentException.class)
    public void constructor_nonManaged_null() {
        AllTypes[] args = null;
        //noinspection ConstantConditions
        new RealmList<AllTypes>(args);
    }

    @Test
    public void isValid_nonManagedMode() {
        //noinspection MismatchedQueryAndUpdateOfCollection
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        assertFalse(list.isValid());
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
        realm.beginTransaction();
        AllTypes managedAllTypes = realm.createObject(AllTypes.class);
        realm.commitTransaction();
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
    public void add_managedObjectAtIndexInNonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        list.add(new AllTypes());
        realm.beginTransaction();
        AllTypes managedAllTypes = realm.createObject(AllTypes.class);
        realm.commitTransaction();
        list.add(0, managedAllTypes);

        assertEquals(managedAllTypes, list.get(0));
    }

    @Test
    public void add_objectAtIndexInManagedMode() {
        realm.beginTransaction();
        Dog obj = collection.get(0);
        collection.add(0, new Dog("Dog 42"));
        realm.commitTransaction();
        assertEquals(obj.getName(), collection.get(1).getName());
        assertEquals("Dog 42", collection.get(0).getName());
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
        realm.beginTransaction();
        try {
            RealmList<Dog> list = realm.createObject(Owner.class).getDogs();
            Dog dog1 = realm.createObject(Dog.class);
            dog1.setName("dog1");
            Dog dog2 = realm.createObject(Dog.class);
            dog2.setName("dog2");
            list.add(dog1);
            assertEquals(dog1, list.set(0, dog2));
            assertEquals(1, list.size());
        } finally {
            realm.cancelTransaction();
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
        realm.beginTransaction();
        AllTypes managedAllTypes = realm.createObject(AllTypes.class);
        realm.commitTransaction();
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

    // Test move where oldPosition > newPosition
    @Test
    public void move_down() {
        Owner owner = realm.where(Owner.class).findFirst();
        Dog dog1 = owner.getDogs().get(1);
        realm.beginTransaction();
        owner.getDogs().move(1, 0);
        realm.commitTransaction();

        assertEquals(0, owner.getDogs().indexOf(dog1));
    }

    // Test move where oldPosition < newPosition
    @Test
    public void move_up() {
        Owner owner = realm.where(Owner.class).findFirst();
        int oldIndex = TEST_SIZE / 2;
        int newIndex = oldIndex + 1;
        Dog dog = owner.getDogs().get(oldIndex);
        realm.beginTransaction();
        owner.getDogs().move(oldIndex, newIndex); // This doesn't do anything as oldIndex is now empty so the index's above gets shifted to the left.
        realm.commitTransaction();

        assertEquals(TEST_SIZE, owner.getDogs().size());
        assertEquals(newIndex, owner.getDogs().indexOf(dog));
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
        int oldIndex = TEST_SIZE / 2;
        int newIndex = oldIndex + 1;
        Dog dog = dogs.get(oldIndex);
        dogs.move(oldIndex, newIndex); // This doesn't do anything as oldIndex is now empty so the index's above gets shifted to the left.

        assertEquals(TEST_SIZE, dogs.size());
        assertEquals(oldIndex, dogs.indexOf(dog));
    }

    /*********************************************************
     * Managed mode tests                                    *
     *********************************************************/

    @Test
    public void isValid() {
        Owner owner = realm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();

        assertTrue(dogs.isValid());

        realm.close();
        assertFalse(dogs.isValid());
    }

    @Test
    public void isValid_whenParentRemoved() {
        Owner owner = realm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();

        realm.beginTransaction();
        owner.deleteFromRealm();
        realm.commitTransaction();

        // RealmList contained in removed object is invalid.
        assertFalse(dogs.isValid());
    }

    @Test
    public void move_outOfBoundsLowerThrows() {
        Owner owner = realm.where(Owner.class).findFirst();
        realm.beginTransaction();
        try {
            owner.getDogs().move(0, -1);
            fail("Indexes < 0 should throw an exception");
        } catch (IndexOutOfBoundsException ignored) {
        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void move_outOfBoundsHigherThrows() {
        Owner owner = realm.where(Owner.class).findFirst();
        realm.beginTransaction();
        try {
            int lastIndex = TEST_SIZE - 1;
            int outOfBoundsIndex = TEST_SIZE;
            owner.getDogs().move(lastIndex, outOfBoundsIndex);
            fail("Indexes >= size() should throw an exception");
        } catch (IndexOutOfBoundsException ignored) {
            ignored.printStackTrace();
        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void add_managedObjectToManagedList() {
        realm.beginTransaction();
        realm.delete(Owner.class);
        Owner owner = realm.createObject(Owner.class);
        Dog dog = realm.createObject(Dog.class);
        owner.getDogs().add(dog);
        realm.commitTransaction();

        assertEquals(1, realm.where(Owner.class).findFirst().getDogs().size());
    }

    // Test that add correctly uses Realm.copyToRealm() on standalone objects.
    @Test
    public void add_nonManagedObjectToManagedList() {
        realm.beginTransaction();
        CyclicType parent = realm.createObject(CyclicType.class);
        RealmList<CyclicType> children = parent.getObjects();
        children.add(new CyclicType());
        realm.commitTransaction();
        assertEquals(1, realm.where(CyclicType.class).findFirst().getObjects().size());
    }

    // Make sure that standalone objects with a primary key are added using copyToRealmOrUpdate
    @Test
    public void add_nonManagedPrimaryKeyObjectToManagedList() {
        realm.beginTransaction();
        realm.copyToRealm(new CyclicTypePrimaryKey(2, "original"));
        RealmList<CyclicTypePrimaryKey> children = realm.copyToRealm(new CyclicTypePrimaryKey(1)).getObjects();
        children.add(new CyclicTypePrimaryKey(2, "new"));
        realm.commitTransaction();

        assertEquals(1, realm.where(CyclicTypePrimaryKey.class).equalTo("id", 1).findFirst().getObjects().size());
        assertEquals("new", realm.where(CyclicTypePrimaryKey.class).equalTo("id", 2).findFirst().getName());
    }

    // Test that set correctly uses Realm.copyToRealm() on standalone objects.
    @Test
    public void set_nonManagedObjectToManagedList() {
        realm.beginTransaction();
        CyclicType parent = realm.copyToRealm(new CyclicType("Parent"));
        RealmList<CyclicType> children = parent.getObjects();
        children.add(new CyclicType());
        children.add(new CyclicType("original"));
        children.add(new CyclicType());
        children.set(1, new CyclicType("updated"));
        realm.commitTransaction();

        RealmList<CyclicType> list = realm.where(CyclicType.class).findFirst().getObjects();
        assertEquals(3, list.size());
        assertEquals("updated", list.get(1).getName());
        assertEquals(5, realm.where(CyclicType.class).count());
    }

    // Test that set correctly uses Realm.copyToRealmOrUpdate() on standalone objects with a primary key.
    @Test
    public void  set_nonManagedPrimaryKeyObjectToManagedList() {
        realm.beginTransaction();
        CyclicTypePrimaryKey parent = realm.copyToRealm(new CyclicTypePrimaryKey(1, "Parent"));
        RealmList<CyclicTypePrimaryKey> children = parent.getObjects();
        children.add(new CyclicTypePrimaryKey(2));
        children.add(new CyclicTypePrimaryKey(3, "original"));
        children.add(new CyclicTypePrimaryKey(4));
        children.set(1, new CyclicTypePrimaryKey(3, "updated"));
        realm.commitTransaction();

        RealmList<CyclicTypePrimaryKey> list = realm.where(CyclicTypePrimaryKey.class).findFirst().getObjects();
        assertEquals(3, list.size());
        assertEquals("updated", list.get(1).getName());
    }

    @Test
    public void add_nullToManagedListThrows() {
        realm.beginTransaction();
        Owner owner = realm.createObject(Owner.class);
        thrown.expect(IllegalArgumentException.class);
        owner.getDogs().add(null);
    }

    @Test
    public void size() {
        Owner owner = realm.where(Owner.class).findFirst();
        assertEquals(TEST_SIZE, owner.getDogs().size());
    }

    @Test
    public void getObjects() {
        Owner owner = realm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();

        assertNotNull(dogs);
        assertEquals("Dog 1", dogs.get(1).getName());
    }

    @Test
    public void remove_byIndex() {
        Owner owner = realm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();
        Dog dog5 = dogs.get(5);

        realm.beginTransaction();
        Dog removedDog = dogs.remove(5);
        realm.commitTransaction();

        assertEquals(dog5, removedDog);
        assertEquals(TEST_SIZE - 1, dogs.size());
    }

    @Test
    public void remove_last() {
        Owner owner = realm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();

        realm.beginTransaction();
        dogs.remove(TEST_SIZE - 1);
        realm.commitTransaction();

        assertEquals(TEST_SIZE - 1, dogs.size());
    }

    @Test
    public void remove_fromEmptyListThrows() {
        Owner owner = realm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();

        realm.beginTransaction();
        dogs.clear();
        thrown.expect(IndexOutOfBoundsException.class);
        dogs.remove(0);
    }

    @Test
    public void remove_byObject() {
        Owner owner = realm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();
        Dog dog = dogs.get(0);

        realm.beginTransaction();
        boolean result = dogs.remove(dog);
        realm.commitTransaction();

        assertTrue(result);
        assertEquals(TEST_SIZE - 1, dogs.size());
    }

    @Test
    public void add_atAfterContainerObjectRemoved() {
        RealmList<Dog> dogs = createDeletedRealmList();

        realm.beginTransaction();
        Dog dog = realm.createObject(Dog.class);
        dog.setName("Dog");
        thrown.expect(IllegalStateException.class);
        dogs.add(0, dog);
    }

    @Test
    public void add_afterContainerObjectRemoved() {
        RealmList<Dog> dogs = createDeletedRealmList();

        realm.beginTransaction();
        Dog dog = realm.createObject(Dog.class);
        dog.setName("Dog");
        thrown.expect(IllegalStateException.class);
        dogs.add(dog);
    }

    @Test
    public void set_afterContainerObjectRemoved() {
        RealmList<Dog> dogs = createDeletedRealmList();

        realm.beginTransaction();
        Dog dog = realm.createObject(Dog.class);
        dog.setName("Dog");
        thrown.expect(IllegalStateException.class);
        dogs.set(0, dog);
    }

    @Test
    public void move_afterContainerObjectRemoved() {
        RealmList<Dog> dogs = createDeletedRealmList();

        realm.beginTransaction();
        thrown.expect(IllegalStateException.class);
        dogs.move(0, 1);
    }

    @Test
    public void clear_afterContainerObjectRemoved() {
        RealmList<Dog> dogs = createDeletedRealmList();

        realm.beginTransaction();
        thrown.expect(IllegalStateException.class);
        dogs.clear();
    }

    @Test
    public void remove_atAfterContainerObjectRemoved() {
        RealmList<Dog> dogs = createDeletedRealmList();

        realm.beginTransaction();
        Dog dog = realm.createObject(Dog.class);
        dog.setName("Dog");
        thrown.expect(IllegalStateException.class);
        dogs.remove(0);
    }

    @Test
    public void remove_objectAfterContainerObjectRemoved() {
        RealmList<Dog> dogs = createDeletedRealmList();

        realm.beginTransaction();
        Dog dog = realm.createObject(Dog.class);
        dog.setName("Dog");
        thrown.expect(IllegalStateException.class);
        dogs.remove(dog);
    }

    @Test
    public void removeAll_managedMode() {
        realm.beginTransaction();
        List<Dog> objectsToRemove = Arrays.asList(collection.get(0));
        assertTrue(collection.removeAll(objectsToRemove));
        assertFalse(collection.contains(objectsToRemove.get(0)));
    }

    @Test
    public void removeAll_managedMode_wrongClass() {
        realm.beginTransaction();
        //noinspection SuspiciousMethodCalls
        assertFalse(collection.removeAll(Collections.singletonList(new Cat())));
    }

    @Test
    public void removeAll_unmanaged_wrongClass() {
        RealmList<Dog> list = createNonManagedDogList();
        //noinspection SuspiciousMethodCalls
        assertFalse(list.removeAll(Collections.singletonList(new Cat())));
    }

    @Test
    public void remove_allAfterContainerObjectRemoved() {
        RealmList<Dog> dogs = createDeletedRealmList();

        realm.beginTransaction();
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
    public void toString_managedMode() {
        StringBuilder sb = new StringBuilder("Dog@[");
        for (int i = 0; i < collection.size() - 1; i++) {
            sb.append(((RealmObjectProxy) (collection.get(i))).realmGet$proxyState().getRow$realm().getIndex());
            sb.append(",");
        }
        sb.append(((RealmObjectProxy)collection.get(TEST_SIZE - 1)).realmGet$proxyState().getRow$realm().getIndex());
        sb.append("]");

        assertEquals(sb.toString(), collection.toString());
    }

    @Test
    public void query() {
        Owner owner = realm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();
        Dog firstDog = dogs.where().equalTo("name", "Dog 0").findFirst();

        assertNotNull(firstDog);
    }

    @Test
    public void clear() {
        Owner owner = realm.where(Owner.class).findFirst();
        realm.beginTransaction();
        assertEquals(TEST_SIZE, owner.getDogs().size());
        owner.getDogs().clear();
        assertEquals(0, owner.getDogs().size());
        realm.commitTransaction();
    }

    @Test
    public void clear_notDeleting() {
        Owner owner = realm.where(Owner.class).findFirst();
        realm.beginTransaction();
        assertEquals(TEST_SIZE, realm.allObjects(Dog.class).size());
        owner.getDogs().clear();
        assertEquals(TEST_SIZE, realm.allObjects(Dog.class).size());
        realm.commitTransaction();
    }

    @Test
    public void setList_clearsOldItems() {
        realm.beginTransaction();
        CyclicType one = realm.copyToRealm(new CyclicType());
        CyclicType two = realm.copyToRealm(new CyclicType());

        assertEquals(0, two.getObjects().size());
        two.setObjects(new RealmList<CyclicType>(one));
        assertEquals(1, two.getObjects().size());
        two.setObjects(new RealmList<CyclicType>(one, two));
        assertEquals(2, two.getObjects().size());
    }

    @Test
    public void realmMethods_onDeletedLinkView() {
        OrderedRealmCollection<CyclicType> results = populateCollectionOnDeletedLinkView(realm, ManagedCollection.MANAGED_REALMLIST);

        for (RealmCollectionMethod method : RealmCollectionMethod.values()) {
            try {
                switch (method) {
                    case WHERE: results.where(); break;
                    case MIN: results.min(CyclicType.FIELD_ID); break;
                    case MAX: results.max(CyclicType.FIELD_ID); break;
                    case SUM: results.sum(CyclicType.FIELD_ID); break;
                    case AVERAGE: results.average(CyclicType.FIELD_ID); break;
                    case MIN_DATE: results.minDate(CyclicType.FIELD_DATE); break;
                    case MAX_DATE: results.maxDate(CyclicType.FIELD_DATE); break;
                    case DELETE_ALL_FROM_REALM: results.deleteAllFromRealm(); break;
                    case IS_VALID: continue; // Does not throw
                }
                fail(method + " should have thrown an Exception.");
            } catch (IllegalStateException ignored) {
            }
        }

        for (OrderedRealmCollectionMethod method : OrderedRealmCollectionMethod.values()) {
            realm.beginTransaction();
            try {
                switch (method) {
                    case DELETE_INDEX: results.deleteFromRealm(0); break;
                    case DELETE_FIRST: results.deleteFirstFromRealm(); break;
                    case DELETE_LAST: results.deleteLastFromRealm(); break;
                    case SORT: results.sort(CyclicType.FIELD_NAME); break;
                    case SORT_FIELD: results.sort(CyclicType.FIELD_NAME, Sort.ASCENDING); break;
                    case SORT_2FIELDS: results.sort(CyclicType.FIELD_NAME, Sort.ASCENDING, CyclicType.FIELD_DATE, Sort.DESCENDING); break;
                    case SORT_MULTI: results.sort(new String[] { CyclicType.FIELD_NAME, CyclicType.FIELD_DATE }, new Sort[] { Sort.ASCENDING, Sort.DESCENDING});
                }
                fail(method + " should have thrown an Exception");
            } catch (IllegalStateException ignored) {
            } finally {
                realm.cancelTransaction();
            }
        }
    }

    @Test
    public void removeAllFromRealm() {
        Owner owner = realm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();
        assertEquals(TEST_SIZE, dogs.size());

        realm.beginTransaction();
        dogs.deleteAllFromRealm();
        realm.commitTransaction();
        assertEquals(0, dogs.size());
        assertEquals(0, realm.where(Dog.class).count());
    }

    @Test
    public void removeAllFromRealm_outsideTransaction() {
        Owner owner = realm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();
        try {
            dogs.deleteAllFromRealm();
            fail("removeAllFromRealm should be called in a transaction.");
        } catch (IllegalStateException e) {
            assertEquals("Changing Realm data can only be done from inside a write transaction.", e.getMessage());
        }
    }

    @Test
    public void removeAllFromRealm_emptyList() {
        RealmList<Dog> dogs = realm.where(Owner.class).findFirst().getDogs();
        assertEquals(TEST_SIZE, dogs.size());

        realm.beginTransaction();
        dogs.deleteAllFromRealm();
        realm.commitTransaction();
        assertEquals(0, dogs.size());
        assertEquals(0, realm.where(Dog.class).count());

        // The dogs is empty now.
        realm.beginTransaction();
        dogs.deleteAllFromRealm();
        realm.commitTransaction();
        assertEquals(0, dogs.size());
        assertEquals(0, realm.where(Dog.class).count());

    }

    @Test
    public void removeAllFromRealm_invalidListShouldThrow() {
        RealmList<Dog> dogs = realm.where(Owner.class).findFirst().getDogs();
        assertEquals(TEST_SIZE, dogs.size());
        realm.close();
        realm = null;

        try {
            dogs.deleteAllFromRealm();
            fail("dogs is invalid and it should throw an exception");
        } catch (IllegalStateException e) {
            assertEquals("This Realm instance has already been closed, making it unusable.", e.getMessage());
        }
    }

    @Test
    public void add_set_objectFromOtherThread() {
        final CountDownLatch finishedLatch = new CountDownLatch(1);
        final Dog dog = realm.where(Dog.class).findFirst();
        final String expectedMsg = "Cannot copy an object from another Realm instance.";

        new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(RealmListTests.this.realm.getConfiguration());
                realm.beginTransaction();
                RealmList<Dog> list = realm.createObject(Owner.class).getDogs();
                list.add(realm.createObject(Dog.class));
                try {
                    list.add(dog);
                    fail();
                } catch (IllegalArgumentException expected) {
                    assertEquals(expectedMsg, expected.getMessage());
                }

                try {
                    list.add(0, dog);
                    fail();
                } catch (IllegalArgumentException expected) {
                    assertEquals(expectedMsg, expected.getMessage());
                }

                try {
                    list.set(0, dog);
                    fail();
                } catch (IllegalArgumentException expected) {
                    assertEquals(expectedMsg, expected.getMessage());
                }

                realm.cancelTransaction();
                realm.close();
                finishedLatch.countDown();
            }
        }).start();
        TestHelper.awaitOrFail(finishedLatch);
    }

    @Test
    public void add_set_dynamicObjectFromOtherThread() {
        final CountDownLatch finishedLatch = new CountDownLatch(1);
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());
        final DynamicRealmObject dynDog = dynamicRealm.where(Dog.CLASS_NAME).findFirst();
        final String expectedMsg = "Cannot copy an object to a Realm instance created in another thread.";

        new Thread(new Runnable() {
            @Override
            public void run() {
                DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());
                dynamicRealm.beginTransaction();
                RealmList<DynamicRealmObject> list = dynamicRealm.createObject(Owner.CLASS_NAME)
                        .getList(Owner.FIELD_DOGS);
                list.add(dynamicRealm.createObject(Dog.CLASS_NAME));

                try {
                    list.add(dynDog);
                    fail();
                } catch (IllegalStateException expected) {
                    assertEquals(expectedMsg, expected.getMessage());
                }

                try {
                    list.add(0,dynDog);
                    fail();
                } catch (IllegalStateException expected) {
                    assertEquals(expectedMsg, expected.getMessage());
                }

                try {
                    list.set(0,dynDog);
                    fail();
                } catch (IllegalStateException expected) {
                    assertEquals(expectedMsg, expected.getMessage());
                }

                dynamicRealm.cancelTransaction();
                dynamicRealm.close();
                finishedLatch.countDown();
            }
        }).start();
        TestHelper.awaitOrFail(finishedLatch);
        dynamicRealm.close();
    }

    @Test
    public void add_set_withWrongDynamicObjectType() {
        final String expectedMsg = "The object has a different type from list's. Type of the list is 'Dog'," +
                        " type of object is 'Cat'.";
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());

        dynamicRealm.beginTransaction();
        RealmList<DynamicRealmObject> list = dynamicRealm.createObject(Owner.CLASS_NAME)
                .getList(Owner.FIELD_DOGS);
        DynamicRealmObject dynCat = dynamicRealm.createObject(Cat.CLASS_NAME);

        try {
            list.add(dynCat);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals(expectedMsg, expected.getMessage());

        }

        try {
            list.add(0, dynCat);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals(expectedMsg, expected.getMessage());

        }

        try {
            list.set(0, dynCat);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals(expectedMsg, expected.getMessage());

        }

        dynamicRealm.cancelTransaction();
        dynamicRealm.close();
    }

    @Test
    public void add_set_dynamicObjectCreatedFromTypedRealm() {
        final String expectedMsg = "Cannot copy DynamicRealmObject between Realm instances.";
        DynamicRealmObject dynDog = new DynamicRealmObject(realm.where(Dog.class).findFirst());
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());

        dynamicRealm.beginTransaction();
        RealmList<DynamicRealmObject> list = dynamicRealm.createObject(Owner.CLASS_NAME)
                .getList(Owner.FIELD_DOGS);

        try {
            list.add(dynDog);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals(expectedMsg, expected.getMessage());
        }

        try {
            list.add(0, dynDog);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals(expectedMsg, expected.getMessage());
        }

        try {
            list.set(0, dynDog);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals(expectedMsg, expected.getMessage());
        }

        dynamicRealm.cancelTransaction();
        dynamicRealm.close();
    }

}
