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

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import io.realm.entities.AllTypes;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertArrayEquals;

// Tests for the ordered collection fine grained notifications.
// This should be expanded to test the notifications for RealmList as well in the future.
@RunWith(AndroidJUnit4.class)
public class OrderedCollectionChangeSetTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    private void populateData(Realm realm, int testSize) {
        realm.beginTransaction();
        for (int i = 0; i < testSize; i++) {
            realm.createObject(AllTypes.class).setColumnLong(i);
        }
        realm.commitTransaction();
    }

    // The args should be [startIndex1, length1, startIndex2, length2, ...]
    private void checkRanges(OrderedCollectionChangeSet.Range[] ranges, long... indexAndLen) {
        if ((indexAndLen.length % 2 != 0))  {
            fail("The 'indexAndLen' array length is not an even number.");
        }
        if (ranges.length != indexAndLen.length / 2) {
            fail("The lengths of 'ranges' and 'indexAndLen' don't match.");
        }
        for (int i = 0; i < ranges.length; i++) {
            OrderedCollectionChangeSet.Range range = ranges[i];
            long startIndex = indexAndLen[i * 2];
            long length = indexAndLen[i * 2 + 1];
            if (range.startIndex != startIndex || range.length != length) {
                fail("Range at index " + i + " doesn't match start index " + startIndex + " length " + length + ".");
            }
        }
    }

    // Deletes AllTypes objects which's columnLong is in the indices array.
    private void deleteObjects(Realm realm, long... indices) {
        for (long index : indices) {
            realm.where(AllTypes.class).equalTo(AllTypes.FIELD_LONG, index).findFirst().deleteFromRealm();
        }
    }

    // Creates AllTypes objects with columnLong set to the value elements in indices array.
    private void createObjects(Realm realm, long... indices) {
        for (long index : indices) {
            realm.createObject(AllTypes.class).setColumnLong(index);
        }
    }

    // Modifies AllTypes objects which's columnLong is in the indices array.
    private void modifyObjects(Realm realm, long... indices) {
        for (long index : indices) {
            AllTypes obj = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_LONG, index).findFirst();
            assertNotNull(obj);
            obj.setColumnString("modified");
        }
    }

    @Test
    @RunTestInLooperThread
    public void deletion() {
        Realm realm = looperThread.realm;
        populateData(realm, 10);
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAllSorted(AllTypes.FIELD_LONG);
        results.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> collection, OrderedCollectionChangeSet changeSet) {
                checkRanges(changeSet.getDeletionRanges(),
                        0, 1,
                        2, 3,
                        8, 2);
                assertArrayEquals(changeSet.getDeletions(), new int[]{0, 2, 3, 4, 8, 9});
                assertEquals(0, changeSet.getChangeRanges().length);
                assertEquals(0, changeSet.getInsertionRanges().length);
                assertEquals(0, changeSet.getChanges().length);
                assertEquals(0, changeSet.getInsertions().length);
                looperThread.testComplete();
            }
        });

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
        Realm realm = looperThread.realm;
        realm.beginTransaction();
        createObjects(realm, 0, 2, 5, 6, 7, 9);
        realm.commitTransaction();
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAllSorted(AllTypes.FIELD_LONG);
        results.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> collection, OrderedCollectionChangeSet changeSet) {
                checkRanges(changeSet.getInsertionRanges(),
                        1, 1,
                        3, 2,
                        8, 1);
                assertArrayEquals(changeSet.getInsertions(), new int[]{1, 3, 4, 8});
                assertEquals(0, changeSet.getChangeRanges().length);
                assertEquals(0, changeSet.getDeletionRanges().length);
                assertEquals(0, changeSet.getChanges().length);
                assertEquals(0, changeSet.getDeletions().length);
                looperThread.testComplete();
            }
        });

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
        Realm realm = looperThread.realm;
        populateData(realm, 10);
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAllSorted(AllTypes.FIELD_LONG);
        results.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> collection, OrderedCollectionChangeSet changeSet) {
                checkRanges(changeSet.getChangeRanges(),
                        0, 1,
                        2, 3,
                        8, 2);
                assertArrayEquals(changeSet.getChanges(), new int[]{0, 2, 3, 4, 8, 9});
                assertEquals(0, changeSet.getInsertionRanges().length);
                assertEquals(0, changeSet.getDeletionRanges().length);
                assertEquals(0, changeSet.getInsertions().length);
                assertEquals(0, changeSet.getDeletions().length);
                looperThread.testComplete();
            }
        });

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
        Realm realm = looperThread.realm;
        populateData(realm, 10);
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAllSorted(AllTypes.FIELD_LONG);
        results.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> collection, OrderedCollectionChangeSet changeSet) {
                checkRanges(changeSet.getDeletionRanges(),
                        0, 1,
                        9, 1);
                assertArrayEquals(changeSet.getDeletions(), new int[]{0, 9});
                checkRanges(changeSet.getInsertionRanges(),
                        0, 1,
                        9, 1);
                assertArrayEquals(changeSet.getInsertions(), new int[]{0, 9});
                assertEquals(0, changeSet.getChangeRanges().length);
                assertEquals(0, changeSet.getChanges().length);
                looperThread.testComplete();
            }
        });
        realm.beginTransaction();
        realm.where(AllTypes.class).equalTo(AllTypes.FIELD_LONG, 0).findFirst().setColumnLong(10);
        realm.where(AllTypes.class).equalTo(AllTypes.FIELD_LONG, 9).findFirst().setColumnLong(0);
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void mixed_changes() {
        Realm realm = looperThread.realm;
        populateData(realm, 10);
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAllSorted(AllTypes.FIELD_LONG);
        results.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> collection, OrderedCollectionChangeSet changeSet) {
                checkRanges(changeSet.getDeletionRanges(),
                        0, 2,
                        5, 1);
                assertArrayEquals(changeSet.getDeletions(), new int[]{0, 1, 5});

                checkRanges(changeSet.getInsertionRanges(),
                        0, 2,
                        9, 2);
                assertArrayEquals(changeSet.getInsertions(), new int[]{0, 1, 9, 10});

                checkRanges(changeSet.getChangeRanges(),
                        3, 2,
                        8, 1);
                assertArrayEquals(changeSet.getChanges(), new int[]{3, 4, 8});

                looperThread.testComplete();
            }
        });

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
        Realm realm = looperThread.realm;
        populateData(realm, 10);
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAllSorted(AllTypes.FIELD_LONG);
        results.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> collection, OrderedCollectionChangeSet changeSet) {
                checkRanges(changeSet.getDeletionRanges(),
                        0, 2,
                        5, 1);
                assertArrayEquals(changeSet.getDeletions(), new int[]{0, 1, 5});

                assertEquals(0, changeSet.getInsertionRanges().length);
                assertEquals(0, changeSet.getInsertions().length);
                assertEquals(0, changeSet.getChangeRanges().length);
                assertEquals(0, changeSet.getChanges().length);

                looperThread.testComplete();
            }
        });

        realm.beginTransaction();
        modifyObjects(realm, 0, 1, 5);
        deleteObjects(realm, 0, 1, 5);
        realm.commitTransaction();
    }

    // Insert some objects then delete them in the same transaction, the listener should not be triggered.
    @Test
    @RunTestInLooperThread
    public void insert_then_delete() {
        Realm realm = looperThread.realm;
        populateData(realm, 10);
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAllSorted(AllTypes.FIELD_LONG);
        results.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> collection, OrderedCollectionChangeSet changeSet) {
                fail("The listener should not be triggered since the collection has no changes compared with before.");
            }
        });

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

    // The change set should empty when the async query returns at the first time.
    @Test
    @RunTestInLooperThread
    public void emptyChangeSet_findAllAsync(){
        Realm realm = looperThread.realm;
        populateData(realm, 10);
        final RealmResults<AllTypes> results = realm.where(AllTypes.class).findAllSortedAsync(AllTypes.FIELD_LONG);
        results.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> collection, OrderedCollectionChangeSet changeSet) {
                assertSame(collection, results);
                assertEquals(9, collection.size());
                assertNull(changeSet);
                looperThread.testComplete();
            }
        });

        final CountDownLatch bgDeletionLatch = new CountDownLatch(1);
        // beginTransaction() will make the async query return immediately. So we have to create an object in another
        // thread. Also, the latch has to be counted down after transaction committed so the async query results can
        // contain the modification in the background transaction.
        new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(looperThread.realmConfiguration)      ;
                realm.beginTransaction();
                realm.where(AllTypes.class).equalTo(AllTypes.FIELD_LONG, 0).findFirst().deleteFromRealm();
                realm.commitTransaction();
                realm.close();
                bgDeletionLatch.countDown();
            }
        }).start();
        TestHelper.awaitOrFail(bgDeletionLatch);
    }
}
