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

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.entities.AllTypes;
import io.realm.entities.Cat;
import io.realm.entities.CyclicType;
import io.realm.entities.CyclicTypePrimaryKey;
import io.realm.entities.Dog;
import io.realm.entities.Owner;
import io.realm.internal.RealmObjectProxy;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
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
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

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

    private RealmList<Dog> createUnmanagedDogList() {
        RealmList<Dog> list = new RealmList<Dog>();
        for (int i = 0; i < TEST_SIZE; i++) {
            list.add(new Dog("Dog " + i));
        }
        return list;
    }

    private RealmList<Dog> createDeletedRealmList() {
        Owner owner = realm.where(Owner.class).findFirst();
        //noinspection ConstantConditions
        RealmList<Dog> dogs = owner.getDogs();

        realm.beginTransaction();
        owner.deleteFromRealm();
        realm.commitTransaction();
        return dogs;
    }

    //noinspection TryWithIdenticalCatches
    /*********************************************************
     * Unmanaged mode tests                                  *
     *********************************************************/

    @Test(expected = IllegalArgumentException.class)
    public void constructor_unmanaged_null() {
        AllTypes[] args = null;
        //noinspection ConstantConditions
        new RealmList<AllTypes>(args);
    }

    @Test
    public void isValid_unmanagedMode() {
        //noinspection MismatchedQueryAndUpdateOfCollection
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        assertTrue(list.isValid());
    }

    @Test
    public void add_unmanagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        AllTypes object = new AllTypes();
        object.setColumnString("String");
        list.add(object);
        assertEquals(1, list.size());
        assertEquals(object, list.get(0));
    }

    @Test
    public void add_nullInUnmanagedMode() {
        final RealmList<AllTypes> list = new RealmList<>();
        assertTrue(list.add(null));
        assertEquals(1, list.size());
    }

    @Test
    public void add_managedObjectInUnmanagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        realm.beginTransaction();
        AllTypes managedAllTypes = realm.createObject(AllTypes.class);
        realm.commitTransaction();
        list.add(managedAllTypes);

        assertEquals(managedAllTypes, list.get(0));
    }

    @Test
    public void add_unmanagedObjectAtIndexInUnmanagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        AllTypes object = new AllTypes();
        object.setColumnString("String");
        list.add(0, object);
        assertEquals(1, list.size());
        assertEquals(object, list.get(0));
    }

    @Test
    public void add_managedObjectAtIndexInUnmanagedMode() {
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

    @Test
    public void add_objectAtInvalidIndexInManagedModeThrows() {
        final int initialDogCount = realm.where(Dog.class).findAll().size();

        realm.beginTransaction();
        try {
            final int invalidIndex = collection.size() + 1;
            collection.add(invalidIndex, new Dog("Dog 42"));
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertEquals(initialDogCount, realm.where(Dog.class).findAll().size());
        }
    }

    @Test
    public void add_nullAtIndexInUnmanagedMode() {
        final RealmList<AllTypes> list = new RealmList<>();
        list.add(0, null);
        assertEquals(1, list.size());
    }

    @Test
    public void set_unmanagedMode() {
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
    public void set_nullInUnmanagedMode() {
        RealmList<AllTypes> list = new RealmList<>();
        list.add(new AllTypes());
        assertNotNull(list.set(0, null));
    }

    @Test
    public void set_managedObjectInUnmanagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        list.add(new AllTypes());
        realm.beginTransaction();
        AllTypes managedAllTypes = realm.createObject(AllTypes.class);
        realm.commitTransaction();
        list.set(0, managedAllTypes);

        assertEquals(managedAllTypes, list.get(0));
    }

    @Test
    public void clear_unmanagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        list.add(new AllTypes());
        assertEquals(1, list.size());
        list.clear();
        assertTrue(list.isEmpty());
    }

    @Test
    public void remove_unmanagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        AllTypes object1 = new AllTypes();
        list.add(object1);
        AllTypes object2 = list.remove(0);
        assertEquals(object1, object2);
    }

    // Tests move where oldPosition > newPosition.
    @Test
    public void move_down() {
        Owner owner = realm.where(Owner.class).findFirst();
        Dog dog1 = owner.getDogs().get(1);
        Dog dog2 = owner.getDogs().get(0);
        realm.beginTransaction();
        owner.getDogs().move(1, 0);
        realm.commitTransaction();

        assertEquals(TEST_SIZE, owner.getDogs().size());
        assertEquals(0, owner.getDogs().indexOf(dog1));
        assertEquals(1, owner.getDogs().indexOf(dog2));
    }

    // Tests move where oldPosition < newPosition.
    @Test
    public void move_up() {
        Owner owner = realm.where(Owner.class).findFirst();
        Dog dog1 = owner.getDogs().get(0);
        Dog dog2 = owner.getDogs().get(1);
        realm.beginTransaction();
        owner.getDogs().move(0, 1);
        realm.commitTransaction();

        assertEquals(TEST_SIZE, owner.getDogs().size());
        assertEquals(1, owner.getDogs().indexOf(dog1));
        assertEquals(0, owner.getDogs().indexOf(dog2));
    }

    // Tests move where oldPosition > newPosition.
    @Test
    public void move_downInUnmanagedMode() {
        RealmList<Dog> dogs = createUnmanagedDogList();
        Dog dog1 = dogs.get(1);
        Dog dog2 = dogs.get(0);

        dogs.move(1, 0);

        assertEquals(TEST_SIZE, dogs.size());
        assertEquals(0, dogs.indexOf(dog1));
        assertEquals(1, dogs.indexOf(dog2));
    }

    // Tests move where oldPosition < newPosition.
    @Test
    public void move_upInUnmanagedMode() {
        RealmList<Dog> dogs = createUnmanagedDogList();
        Dog dog1 = dogs.get(0);
        Dog dog2 = dogs.get(1);

        dogs.move(0, 1);

        assertEquals(TEST_SIZE, dogs.size());
        assertEquals(1, dogs.indexOf(dog1));
        assertEquals(0, dogs.indexOf(dog2));
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

    // Tests that add correctly uses Realm.copyToRealm() on unmanaged objects.
    @Test
    public void add_unmanagedObjectToManagedList() {
        realm.beginTransaction();
        CyclicType parent = realm.createObject(CyclicType.class);
        RealmList<CyclicType> children = parent.getObjects();
        children.add(new CyclicType());
        realm.commitTransaction();
        assertEquals(1, realm.where(CyclicType.class).findFirst().getObjects().size());
    }

    // Makes sure that unmanaged objects with a primary key are added using copyToRealmOrUpdate.
    @Test
    public void add_unmanagedPrimaryKeyObjectToManagedList() {
        realm.beginTransaction();
        realm.copyToRealm(new CyclicTypePrimaryKey(2, "original"));
        RealmList<CyclicTypePrimaryKey> children = realm.copyToRealm(new CyclicTypePrimaryKey(1)).getObjects();
        children.add(new CyclicTypePrimaryKey(2, "new"));
        realm.commitTransaction();

        assertEquals(1, realm.where(CyclicTypePrimaryKey.class).equalTo("id", 1).findFirst().getObjects().size());
        assertEquals("new", realm.where(CyclicTypePrimaryKey.class).equalTo("id", 2).findFirst().getName());
    }

    // Tests that set correctly uses Realm.copyToRealm() on unmanaged objects.
    @Test
    public void set_unmanagedObjectToManagedList() {
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

    // Tests that set correctly uses Realm.copyToRealmOrUpdate() on unmanaged objects with a primary key.
    @Test
    public void set_unmanagedPrimaryKeyObjectToManagedList() {
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
        assertEquals(TEST_SIZE, realm.where(Dog.class).count());
    }

    @Test
    public void remove_first() {
        Owner owner = realm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();

        realm.beginTransaction();
        dogs.remove(0);
        realm.commitTransaction();

        assertEquals(TEST_SIZE - 1, dogs.size());
        assertEquals(TEST_SIZE, realm.where(Dog.class).count());
    }

    @Test
    public void remove_last() {
        Owner owner = realm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();

        realm.beginTransaction();
        dogs.remove(TEST_SIZE - 1);
        realm.commitTransaction();

        assertEquals(TEST_SIZE - 1, dogs.size());
        assertEquals(TEST_SIZE, realm.where(Dog.class).count());
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
        assertEquals(TEST_SIZE, realm.where(Dog.class).count());
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
        List<Dog> objectsToRemove = Collections.singletonList(collection.get(0));
        assertTrue(collection.removeAll(objectsToRemove));
        assertFalse(collection.contains(objectsToRemove.get(0)));
    }

    @Test
    @SuppressWarnings("CollectionIncompatibleType")
    public void removeAll_managedMode_wrongClass() {
        realm.beginTransaction();
        //noinspection SuspiciousMethodCalls
        assertFalse(collection.removeAll(Collections.singletonList(new Cat())));
    }

    @Test
    @SuppressWarnings("CollectionIncompatibleType")
    public void removeAll_unmanaged_wrongClass() {
        RealmList<Dog> list = createUnmanagedDogList();
        //noinspection SuspiciousMethodCalls
        assertFalse(list.removeAll(Collections.singletonList(new Cat())));
    }

    @Test
    public void removeAll_afterContainerObjectRemoved() {
        RealmList<Dog> dogs = createDeletedRealmList();

        realm.beginTransaction();
        thrown.expect(IllegalStateException.class);
        dogs.removeAll(Collections.<Dog>emptyList());
    }

    @Test
    public void removeAll_outsideTransaction() {
        List<Dog> objectsToRemove = Collections.singletonList(collection.get(0));
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(CoreMatchers.containsString("Objects can only be removed from inside a write transaction"));
        collection.removeAll(objectsToRemove);
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
        assertEquals("RealmList<Dog>@[invalid]", dogs.toString());
    }

    @Test
    public void toString_managedMode() {
        StringBuilder sb = new StringBuilder("RealmList<Dog>@[");
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
        assertEquals(TEST_SIZE, realm.where(Dog.class).count());
        owner.getDogs().clear();
        assertEquals(TEST_SIZE, realm.where(Dog.class).count());
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
                    case IS_VALID: continue; // Does not throw.
                    case IS_MANAGED: continue; // Does not throw.
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
                    case SORT_MULTI: results.sort(new String[] { CyclicType.FIELD_NAME, CyclicType.FIELD_DATE }, new Sort[] { Sort.ASCENDING, Sort.DESCENDING}); break;
                    case CREATE_SNAPSHOT: results.createSnapshot(); break;
                }
                fail(method + " should have thrown an Exception");
            } catch (IllegalStateException ignored) {
            } finally {
                realm.cancelTransaction();
            }
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
    public void add_set_dynamicObjectFromOtherThread() throws Throwable {
        final CountDownLatch finishedLatch = new CountDownLatch(1);
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());
        final DynamicRealmObject dynDog = dynamicRealm.where(Dog.CLASS_NAME).findFirst();
        final String expectedMsg = "Cannot copy an object to a Realm instance created in another thread.";

        final AtomicReference<Throwable> thrownErrorRef = new AtomicReference<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());
                dynamicRealm.beginTransaction();
                try {
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
                        list.add(0, dynDog);
                        fail();
                    } catch (IllegalStateException expected) {
                        assertEquals(expectedMsg, expected.getMessage());
                    }

                    try {
                        list.set(0, dynDog);
                        fail();
                    } catch (IllegalStateException expected) {
                        assertEquals(expectedMsg, expected.getMessage());
                    }
                } catch (Throwable throwable) {
                    thrownErrorRef.set(throwable);
                } finally {
                    dynamicRealm.cancelTransaction();
                    dynamicRealm.close();
                    finishedLatch.countDown();
                }
            }
        }).start();
        TestHelper.awaitOrFail(finishedLatch);
        dynamicRealm.close();

        final Throwable thrown = thrownErrorRef.get();
        if (thrown != null) {
            throw thrown;
        }
    }

    @Test
    public void add_set_withWrongDynamicObjectType() {
        final String expectedMsg = "The object has a different type from list's. Type of the list is 'Dog'," +
                        " type of object is 'Cat'.";
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());

        dynamicRealm.beginTransaction();
        RealmList<DynamicRealmObject> list = dynamicRealm.createObject(Owner.CLASS_NAME)
                .getList(Owner.FIELD_DOGS);
        list.add(dynamicRealm.createObject(Dog.CLASS_NAME));
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
        //noinspection ConstantConditions
        DynamicRealmObject dynDog = new DynamicRealmObject(realm.where(Dog.class).findFirst());
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());

        dynamicRealm.beginTransaction();
        RealmList<DynamicRealmObject> list = dynamicRealm.createObject(Owner.CLASS_NAME)
                .getList(Owner.FIELD_DOGS);
        list.add(dynamicRealm.createObject(Dog.CLASS_NAME));

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

    private RealmList<Dog> prepareRealmListInLooperThread() {
        Realm realm = looperThread.getRealm();
        realm.beginTransaction();
        Owner owner = realm.createObject(Owner.class);
        owner.setName("Owner");
        for (int i = 0; i < TEST_SIZE; i++) {
            Dog dog = realm.createObject(Dog.class);
            dog.setName("Dog " + i);
            owner.getDogs().add(dog);
        }
        realm.commitTransaction();
        return owner.getDogs();
    }

    @Test
    @RunTestInLooperThread
    public void addChangeListener() {
        collection = prepareRealmListInLooperThread();
        Realm realm = looperThread.getRealm();
        final AtomicInteger listenerCalledCount = new AtomicInteger(0);
        collection.addChangeListener(new RealmChangeListener<RealmList<Dog>>() {
            @Override
            public void onChange(RealmList<Dog> element) {
                assertEquals(0, listenerCalledCount.getAndIncrement());
            }
        });
        collection.addChangeListener(new OrderedRealmCollectionChangeListener<RealmList<Dog>>() {
            @Override
            public void onChange(RealmList<Dog> collection, OrderedCollectionChangeSet changes) {
                assertEquals(1, listenerCalledCount.getAndIncrement());
            }
        });
        realm.beginTransaction();
        collection.get(0).setAge(42);
        realm.commitTransaction();

        // This should trigger the listener.
        realm.beginTransaction();
        realm.cancelTransaction();
        assertEquals(2, listenerCalledCount.get());
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void removeAllChangeListeners() {
        collection = prepareRealmListInLooperThread();
        Realm realm = looperThread.getRealm();
        final AtomicInteger listenerCalledCount = new AtomicInteger(0);
        collection.addChangeListener(new RealmChangeListener<RealmList<Dog>>() {
            @Override
            public void onChange(RealmList<Dog> element) {
                fail();
            }
        });
        collection.addChangeListener(new OrderedRealmCollectionChangeListener<RealmList<Dog>>() {
            @Override
            public void onChange(RealmList<Dog> collection, OrderedCollectionChangeSet changes) {
                fail();
            }
        });

        collection.removeAllChangeListeners();

        // This one is added after removal, so it should be triggered.
        collection.addChangeListener(new RealmChangeListener<RealmList<Dog>>() {
            @Override
            public void onChange(RealmList<Dog> element) {
                listenerCalledCount.incrementAndGet();
                looperThread.testComplete();
            }
        });

        // This should trigger the listener if there is any.
        realm.beginTransaction();
        collection.get(0).setAge(42);
        realm.commitTransaction();

        assertEquals(1, listenerCalledCount.get());
    }

    @Test
    @RunTestInLooperThread
    public void removeChangeListener() {
        collection = prepareRealmListInLooperThread();
        Realm realm = looperThread.getRealm();
        final AtomicInteger listenerCalledCount = new AtomicInteger(0);
        RealmChangeListener<RealmList<Dog>> listener1 = new RealmChangeListener<RealmList<Dog>>() {
            @Override
            public void onChange(RealmList<Dog> element) {
                fail();
            }
        };
        OrderedRealmCollectionChangeListener<RealmList<Dog>> listener2 =
                new OrderedRealmCollectionChangeListener<RealmList<Dog>>() {
                    @Override
                    public void onChange(RealmList<Dog> collection, OrderedCollectionChangeSet changes) {
                        assertEquals(0, listenerCalledCount.getAndIncrement());
                        looperThread.testComplete();
                    }
                };

        collection.addChangeListener(listener1);
        collection.addChangeListener(listener2);

        collection.removeChangeListener(listener1);

        // This should trigger the listener if there is any.
        realm.beginTransaction();
        collection.get(0).setAge(42);
        realm.commitTransaction();
        assertEquals(1, listenerCalledCount.get());
    }

    // https://github.com/realm/realm-java/issues/4554
    @Test
    public void createSnapshot_shouldUseTargetTable() {
        int sizeBefore = collection.size();
        OrderedRealmCollectionSnapshot<Dog> snapshot = collection.createSnapshot();
        realm.beginTransaction();
        snapshot.get(0).deleteFromRealm();
        realm.commitTransaction();
        assertEquals(sizeBefore - 1, collection.size());

        assertNotNull(collection.getOsList());
        assertEquals(collection.getOsList().getTargetTable().getName(), snapshot.getTable().getName());
    }

    @Test
    public void getRealm() {
        assertTrue(realm == collection.getRealm());
    }

    @Test
    public void getRealm_throwsIfDynamicRealm() {
        DynamicRealm dRealm = DynamicRealm.getInstance(realm.getConfiguration());
        DynamicRealmObject obj = dRealm.where(Owner.CLASS_NAME).findFirst();
        RealmList<DynamicRealmObject> list = obj.getList("dogs");
        try {
            list.getRealm();
            fail();
        } catch (IllegalStateException ignore) {
        } finally {
            dRealm.close();
        }
    }

    @Test
    public void getRealm_throwsIfRealmClosed() {
        realm.close();
        try {
            collection.getRealm();
            fail();
        } catch (IllegalStateException ignore) {
        }
    }

    @Test
    public void getRealm_returnsNullForUnmanagedList() {
        assertNull(new RealmList().getRealm());
    }
}
