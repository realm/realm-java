/*
 * Copyright 2017 Realm Inc.
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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import io.realm.entities.Dog;
import io.realm.entities.Owner;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;


// Tests for the ordered collection fine grained notifications for both RealmResults and RealmList.
@SuppressWarnings("ConstantConditions") // Suppress the null return value warnings for RealmList.get()
@RunWith(Parameterized.class)
public class OrderedCollectionChangeSetTests {

    private enum ObservablesType {
        REALM_RESULTS, REALM_LIST
    }

    private interface ChangesCheck {
        void check(OrderedCollectionChangeSet changeSet);
    }

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    private final ObservablesType type;

    @Parameterized.Parameters(name = "{0}")
    public static List<ObservablesType> data() {
        return Arrays.asList(ObservablesType.values());
    }

    public OrderedCollectionChangeSetTests(ObservablesType type) {
        this.type = type;
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    private void populateData(Realm realm, int testSize) {
        Owner owner = null;
        realm.beginTransaction();
        if (type == ObservablesType.REALM_LIST) {
            owner = realm.createObject(Owner.class);
        }
        for (int i = 0; i < testSize; i++) {
            Dog dog = realm.createObject(Dog.class);
            dog.setAge(i);
            if (type == ObservablesType.REALM_LIST) {
                owner.getDogs().add(dog);
            }
        }
        realm.commitTransaction();
    }

    // The args should be [startIndex1, length1, startIndex2, length2, ...]
    private void checkRanges(OrderedCollectionChangeSet.Range[] ranges, int... indexAndLen) {
        if ((indexAndLen.length % 2 != 0)) {
            fail("The 'indexAndLen' array length is not an even number.");
        }
        if (ranges.length != indexAndLen.length / 2) {
            fail("The lengths of 'ranges' and 'indexAndLen' don't match.");
        }
        for (int i = 0; i < ranges.length; i++) {
            OrderedCollectionChangeSet.Range range = ranges[i];
            int startIndex = indexAndLen[i * 2];
            int length = indexAndLen[i * 2 + 1];
            if (range.startIndex != startIndex || range.length != length) {
                fail("Range at index " + i + " doesn't match start index " + startIndex + " length " + length + ".");
            }
        }
    }

    // Deletes Dogs objects which's columnLong is in the indices array.
    private void deleteObjects(Realm realm, int... indices) {
        for (int index : indices) {
            realm.where(Dog.class).equalTo(Dog.FIELD_AGE, index).findFirst().deleteFromRealm();
        }
    }

    // Creates Dogs objects with columnLong set to the value elements in indices array.
    private void createObjects(Realm realm, int... indices) {
        for (int index : indices) {
            Dog dog = realm.createObject(Dog.class);
            dog.setAge(index);
            if (type == ObservablesType.REALM_LIST) {
                Owner owner = realm.where(Owner.class).findFirst();
                assertNotNull(owner);
                RealmList<Dog> dogs = owner.getDogs();
                boolean added = false;
                // Insert the newly created dog to the RealmList by the order of age.
                for (int i = 0; i < dogs.size(); i++) {
                    if (dog.getAge() <= dogs.get(i).getAge()) {
                        dogs.add(i, dog);
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    dogs.add(dog);
                }
            }
        }
    }

    // Modifies Dogs objects which's columnLong is in the indices array.
    private void modifyObjects(Realm realm, int... indices) {
        for (int index : indices) {
            Dog obj = realm.where(Dog.class).equalTo(Dog.FIELD_AGE, index).findFirst();
            assertNotNull(obj);
            obj.setName("modified");
        }
    }

    private void moveObjects(Realm realm, int originAge, int newAge) {
        if (type == ObservablesType.REALM_LIST) {
            // For RealmList we need to:
            // 1. Find the object by the original age and move it to the new place where it should be with the new age
            //    set -- the RealmList is sorted by age.
            // 2. Set the object's age with new value.
            RealmList<Dog> dogs = realm.where(Owner.class).findFirst().getDogs();
            int originIdx = -1;
            int newIdx = -1;
            for (int i = 0; i < dogs.size(); i++) {
                Dog dog = dogs.get(i);
                assertNotNull(dog);
                if (dog.getAge() == originAge) {
                    originIdx = i;
                    break;
                }
            }
            assertNotEquals(-1, originIdx);
            for (int i = 0; i < dogs.size(); i++) {
                if (i == originIdx) {
                    // not precise code, but good enough for testing.
                    continue;
                }
                if (newAge <= dogs.get(i).getAge()) {
                    newIdx = i;
                    break;
                }
            }
            if (newIdx == -1) {
                newIdx = dogs.size() - 1;
            }
            dogs.get(originIdx).setAge(newAge);
            dogs.move(originIdx, newIdx);
        } else {
            // Since the RealmResults is sorted by age, just simply set the object's age with new value.
            realm.where(Dog.class).equalTo(Dog.FIELD_AGE, originAge).findFirst().setAge(newAge);
        }
    }

    private OrderedRealmCollection<Dog> getTestingCollection(Realm realm) {
        switch (type) {
            case REALM_RESULTS:
                RealmResults<Dog> results = realm.where(Dog.class).sort(Dog.FIELD_AGE).findAll();
                looperThread.keepStrongReference(results);
                return results;
            case REALM_LIST:
                RealmList<Dog> list = realm.where(Owner.class).findFirst().getDogs();
                looperThread.keepStrongReference(list);
                return list;
        }
        fail();
        return null;
    }

    private void registerCheckListener(Realm realm, final ChangesCheck changesCheck) {
        switch (type) {
            case REALM_RESULTS:
                RealmResults<Dog> results = (RealmResults<Dog>) getTestingCollection(realm);
                results.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<Dog>>() {
                    @Override
                    public void onChange(RealmResults<Dog> collection, @Nullable OrderedCollectionChangeSet changeSet) {
                        changesCheck.check(changeSet);
                    }
                });
                break;
            case REALM_LIST:
                RealmList<Dog> list = (RealmList<Dog>) getTestingCollection(realm);
                looperThread.keepStrongReference(list);
                list.addChangeListener(new OrderedRealmCollectionChangeListener<RealmList<Dog>>() {
                    @Override
                    public void onChange(RealmList<Dog> collection, @Nullable OrderedCollectionChangeSet changeSet) {
                        changesCheck.check(changeSet);
                    }
                });
                break;
        }
    }

    @Test
    @RunTestInLooperThread
    public void deletion() {
        Realm realm = looperThread.getRealm();
        populateData(realm, 10);

        final ChangesCheck changesCheck = new ChangesCheck() {
            @Override
            public void check(OrderedCollectionChangeSet changeSet) {
                checkRanges(changeSet.getDeletionRanges(),
                        0, 1,
                        2, 3,
                        8, 2);
                assertArrayEquals(changeSet.getDeletions(), new int[] {0, 2, 3, 4, 8, 9});
                assertEquals(0, changeSet.getChangeRanges().length);
                assertEquals(0, changeSet.getInsertionRanges().length);
                assertEquals(0, changeSet.getChanges().length);
                assertEquals(0, changeSet.getInsertions().length);
                looperThread.testComplete();
            }
        };

        registerCheckListener(realm, changesCheck);

        realm.beginTransaction();
        deleteObjects(realm,
                0,
                2, 3, 4,
                8, 9);
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void insertion() {
        Realm realm = looperThread.getRealm();
        populateData(realm, 0); // We need to create the owner.
        realm.beginTransaction();
        createObjects(realm, 0, 2, 5, 6, 7, 9);
        realm.commitTransaction();

        ChangesCheck changesCheck = new ChangesCheck() {
            @Override
            public void check(OrderedCollectionChangeSet changeSet) {
                checkRanges(changeSet.getInsertionRanges(),
                        1, 1,
                        3, 2,
                        8, 1);
                assertArrayEquals(changeSet.getInsertions(), new int[] {1, 3, 4, 8});
                assertEquals(0, changeSet.getChangeRanges().length);
                assertEquals(0, changeSet.getDeletionRanges().length);
                assertEquals(0, changeSet.getChanges().length);
                assertEquals(0, changeSet.getDeletions().length);
                looperThread.testComplete();
            }
        };
        registerCheckListener(realm, changesCheck);

        realm.beginTransaction();
        createObjects(realm,
                1,
                3, 4,
                8);
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void changes() {
        Realm realm = looperThread.getRealm();
        populateData(realm, 10);
        ChangesCheck changesCheck = new ChangesCheck() {
            @Override
            public void check(OrderedCollectionChangeSet changeSet) {
                checkRanges(changeSet.getChangeRanges(),
                        0, 1,
                        2, 3,
                        8, 2);
                assertArrayEquals(changeSet.getChanges(), new int[] {0, 2, 3, 4, 8, 9});
                assertEquals(0, changeSet.getInsertionRanges().length);
                assertEquals(0, changeSet.getDeletionRanges().length);
                assertEquals(0, changeSet.getInsertions().length);
                assertEquals(0, changeSet.getDeletions().length);
                looperThread.testComplete();
            }
        };

        registerCheckListener(realm, changesCheck);

        realm.beginTransaction();
        modifyObjects(realm,
                0,
                2, 3, 4,
                8, 9);
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void moves() {
        Realm realm = looperThread.getRealm();
        populateData(realm, 10);
        ChangesCheck changesCheck = new ChangesCheck() {
            @Override
            public void check(OrderedCollectionChangeSet changeSet) {
                checkRanges(changeSet.getDeletionRanges(),
                        0, 1,
                        9, 1);
                assertArrayEquals(changeSet.getDeletions(), new int[] {0, 9});
                checkRanges(changeSet.getInsertionRanges(),
                        0, 1,
                        9, 1);
                assertArrayEquals(changeSet.getInsertions(), new int[] {0, 9});
                assertEquals(0, changeSet.getChangeRanges().length);
                assertEquals(0, changeSet.getChanges().length);
                looperThread.testComplete();
            }
        };
        registerCheckListener(realm, changesCheck);

        realm.beginTransaction();
        moveObjects(realm, 0, 10);
        moveObjects(realm, 9, 0);
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void mixed_changes() {
        Realm realm = looperThread.getRealm();
        populateData(realm, 10);
        ChangesCheck changesCheck = new ChangesCheck() {
            @Override
            public void check(OrderedCollectionChangeSet changeSet) {
                checkRanges(changeSet.getDeletionRanges(),
                        0, 2,
                        5, 1);
                assertArrayEquals(changeSet.getDeletions(), new int[] {0, 1, 5});

                checkRanges(changeSet.getInsertionRanges(),
                        0, 2,
                        9, 2);
                assertArrayEquals(changeSet.getInsertions(), new int[] {0, 1, 9, 10});

                checkRanges(changeSet.getChangeRanges(),
                        3, 2,
                        8, 1);
                assertArrayEquals(changeSet.getChanges(), new int[] {3, 4, 8});

                looperThread.testComplete();
            }
        };

        registerCheckListener(realm, changesCheck);

        realm.beginTransaction();
        createObjects(realm, 11, 12, -1, -2);
        deleteObjects(realm, 0, 1, 5);
        modifyObjects(realm, 12, 3, 4, 9);
        realm.commitTransaction();
        // After transaction, '*' means the object has been modified. 12 has been modified as well, but it is created
        // and modified in the same transaction, should not be counted in the changes range.
        // [-1, -2, 2, *3, *4, 6, 7, 8, *9, 11, 12]
    }

    // Change some objects then delete them. Only deletion changes should be sent.
    @Test
    @RunTestInLooperThread
    public void changes_then_delete() {
        Realm realm = looperThread.getRealm();
        populateData(realm, 10);
        ChangesCheck changesCheck = new ChangesCheck() {
            @Override
            public void check(OrderedCollectionChangeSet changeSet) {
                checkRanges(changeSet.getDeletionRanges(),
                        0, 2,
                        5, 1);
                assertArrayEquals(changeSet.getDeletions(), new int[] {0, 1, 5});

                assertEquals(0, changeSet.getInsertionRanges().length);
                assertEquals(0, changeSet.getInsertions().length);
                assertEquals(0, changeSet.getChangeRanges().length);
                assertEquals(0, changeSet.getChanges().length);

                looperThread.testComplete();
            }
        };
        registerCheckListener(realm, changesCheck);

        realm.beginTransaction();
        modifyObjects(realm, 0, 1, 5);
        deleteObjects(realm, 0, 1, 5);
        realm.commitTransaction();
    }

    // Insert some objects then delete them in the same transaction, the listener should not be triggered.
    @Test
    @RunTestInLooperThread
    public void insert_then_delete() {
        Realm realm = looperThread.getRealm();
        populateData(realm, 10);
        ChangesCheck changesCheck = new ChangesCheck() {
            @Override
            public void check(OrderedCollectionChangeSet changeSet) {
                fail("The listener should not be triggered since the collection has no changes compared with before.");
            }
        };

        registerCheckListener(realm, changesCheck);

        looperThread.postRunnableDelayed(new Runnable() {
            @Override
            public void run() {
                looperThread.testComplete();
            }
        }, 1000);

        realm.beginTransaction();
        createObjects(realm, 10, 11);
        deleteObjects(realm, 10, 11);
        realm.commitTransaction();
    }

    // The change set should be empty when the async query returns at the first time.
    @Test
    @RunTestInLooperThread
    public void initialChangeSet_findAllAsync() {
        if (type == ObservablesType.REALM_LIST) {
            looperThread.testComplete();
            return;
        }

        Realm realm = looperThread.getRealm();
        populateData(realm, 10);
        final RealmResults<Dog> results = realm.where(Dog.class).sort(Dog.FIELD_AGE).findAllAsync();
        looperThread.keepStrongReference(results);
        results.addChangeListener((collection, changeSet) -> {
            assertSame(collection, results);
            assertEquals(10, collection.size());
            assertTrue(changeSet.isCompleteResult());
            assertEquals(OrderedCollectionChangeSet.State.INITIAL, changeSet.getState());
            assertEquals(0, changeSet.getInsertions().length);
            assertEquals(0, changeSet.getChanges().length);
            assertEquals(0, changeSet.getDeletions().length);
            looperThread.testComplete();
        });
    }

    // The change set should be empty when the async query returns at the first time.
    @Test
    @RunTestInLooperThread
    public void initialChangeSet_findAll() {
        if (type == ObservablesType.REALM_LIST) {
            looperThread.testComplete();
            return;
        }

        Realm realm = looperThread.getRealm();
        populateData(realm, 10);
        final RealmResults<Dog> results = realm.where(Dog.class).sort(Dog.FIELD_AGE).findAll();
        looperThread.keepStrongReference(results);
        results.addChangeListener((collection, changeSet) -> {
            assertSame(collection, results);
            assertEquals(11, collection.size());
            assertTrue(changeSet.isCompleteResult());
            assertEquals(OrderedCollectionChangeSet.State.UPDATE, changeSet.getState());
            assertEquals(1, changeSet.getInsertions().length);
            looperThread.testComplete();
        });

        realm.executeTransaction(r -> {
            r.createObject(Dog.class);
        });
    }

    // To reproduce https://github.com/realm/realm-java/issues/5507
    // 1. Add listener to a collection
    // A. change the collection in a background thread
    // 2. Remove the listener
    // 3. Add another listener
    // 4. the listener added in step 3 should be triggered with change set in step A
    @Test
    @RunTestInLooperThread
    public void addChangeListener_bug5507() throws InterruptedException {
        // FIXME: See https://github.com/realm/realm-object-store/issues/605
        if (type == ObservablesType.REALM_RESULTS) {
            looperThread.testComplete();
            return;
        }

        Realm realm = looperThread.getRealm();
        populateData(realm, 1);

        OrderedRealmCollectionChangeListener<OrderedRealmCollection<Dog>> listener1 =
                new OrderedRealmCollectionChangeListener<OrderedRealmCollection<Dog>>() {
            @Override
            public void onChange(OrderedRealmCollection<Dog> dogs, @Nullable OrderedCollectionChangeSet changeSet) {
                fail();
            }
        };

        OrderedRealmCollection<Dog> dogs = getTestingCollection(realm);
        assertEquals(1, dogs.size());

        if (type == ObservablesType.REALM_LIST) {
            //noinspection unchecked
            ((RealmList) dogs).addChangeListener(listener1);
        } else {
            //noinspection unchecked
            ((RealmResults) dogs).addChangeListener(listener1);
        }

        Thread bgThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(looperThread.getConfiguration());
                realm.beginTransaction();
                createObjects(realm, 2);
                realm.commitTransaction();
                realm.close();
            }
        });
        bgThread.start();
        bgThread.join();

        if (type == ObservablesType.REALM_LIST) {
            //noinspection unchecked
            ((RealmList) dogs).removeChangeListener(listener1);
        } else {
            //noinspection unchecked
            ((RealmResults) dogs).removeChangeListener(listener1);
        }

        OrderedRealmCollectionChangeListener<OrderedRealmCollection<Dog>> listener2 =
                new OrderedRealmCollectionChangeListener<OrderedRealmCollection<Dog>>() {
                    @Override
                    public void onChange(OrderedRealmCollection<Dog> dogs, @Nullable OrderedCollectionChangeSet changeSet) {
                        assertEquals(2, dogs.size());
                        looperThread.testComplete();
                    }
                };

        if (type == ObservablesType.REALM_LIST) {
            //noinspection unchecked
            ((RealmList) dogs).addChangeListener(listener2);
        } else {
            //noinspection unchecked
            ((RealmResults) dogs).addChangeListener(listener2);
        }
    }
}
