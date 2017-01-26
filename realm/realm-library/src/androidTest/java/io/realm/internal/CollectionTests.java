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

package io.realm.internal;


import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmFieldType;
import io.realm.TestHelper;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;


@RunWith(AndroidJUnit4.class)
public class CollectionTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    private RealmConfiguration config;
    private SharedRealm sharedRealm;
    private Table table;

    @Before
    public void setUp() {
        config = configFactory.createConfiguration();
        sharedRealm = getSharedRealm();
        populateData();
    }

    @After
    public void tearDown() {
        sharedRealm.close();
    }

    private SharedRealm getSharedRealm() {
        return SharedRealm.getInstance(config, null, true);
    }

    private void populateData() {
        sharedRealm.beginTransaction();
        table = sharedRealm.getTable("test_table");
        // Specify the column types and names
        long columnIdx = table.addColumn(RealmFieldType.STRING, "firstName");
        table.addSearchIndex(columnIdx);
        table.addColumn(RealmFieldType.STRING, "lastName");
        table.addColumn(RealmFieldType.INTEGER, "age");

        // Add data to the table
        long row = table.addEmptyRow();
        table.setString(0, row, "John", false);
        table.setString(1, row, "Lee", false);
        table.setLong(2, row, 4, false);

        row = table.addEmptyRow();
        table.setString(0, row, "John", false);
        table.setString(1, row, "Anderson", false);
        table.setLong(2, row, 3, false);

        row = table.addEmptyRow();
        table.setString(0, row, "Erik", false);
        table.setString(1, row, "Lee", false);
        table.setLong(2, row, 1, false);

        row = table.addEmptyRow();
        table.setString(0, row, "Henry", false);
        table.setString(1, row, "Anderson", false);
        table.setLong(2, row, 1, false);
        sharedRealm.commitTransaction();
    }

    private void addRowAsync() {
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedRealm sharedRealm = getSharedRealm();
                addRow(sharedRealm);
                sharedRealm.close();
                latch.countDown();
            }
        }).start();
        TestHelper.awaitOrFail(latch);
    }

    private void addRow(SharedRealm sharedRealm) {
        sharedRealm.beginTransaction();
        table = sharedRealm.getTable("test_table");
        table.addEmptyRow();
        sharedRealm.commitTransaction();
    }

    @Test
    public void constructor_withDistinct() {
        SortDescriptor distinctDescriptor = SortDescriptor.getInstanceForDistinct(table, "firstName");
        Collection collection = new Collection(sharedRealm, table.where(), null, distinctDescriptor);

        assertEquals(collection.size(), 3);
        assertEquals(collection.getUncheckedRow(0).getString(0), "John");
        assertEquals(collection.getUncheckedRow(1).getString(0), "Erik");
        assertEquals(collection.getUncheckedRow(2).getString(0), "Henry");
    }


    @Test(expected = UnsupportedOperationException.class)
    public void constructor_queryIsValidated() {
        // Collection's constructor should call TableQuery.validateQuery()
        new Collection(sharedRealm, table.where().or());
    }

    @Test
    public void constructor_queryOnDeletedTable() {
        TableQuery query = table.where();
        sharedRealm.beginTransaction();
        sharedRealm.removeTable(table.getName());
        sharedRealm.commitTransaction();
        // Query should be checked before creating OS Results.
        thrown.expect(IllegalStateException.class);
        new Collection(sharedRealm, query);
    }

    @Test
    public void size() {
        Collection collection = new Collection(sharedRealm, table.where());
        assertEquals(4, collection.size());
    }

    @Test
    public void where() {
        Collection collection = new Collection(sharedRealm, table.where());
        Collection collection2 =new Collection(sharedRealm, collection.where().equalTo(new long[]{0}, "John"));
        Collection collection3 =new Collection(sharedRealm, collection2.where().equalTo(new long[]{1}, "Anderson"));

        // A new native Results should be created.
        assertTrue(collection.getNativePtr() != collection2.getNativePtr());
        assertTrue(collection2.getNativePtr() != collection3.getNativePtr());

        assertEquals(4, collection.size());
        assertEquals(2, collection2.size());
        assertEquals(1, collection3.size());
    }

    @Test
    public void sort() {
        Collection collection = new Collection(sharedRealm, table.where().greaterThan(new long[]{2}, 1));
        SortDescriptor sortDescriptor = new SortDescriptor(table, new long[] {2});

        Collection collection2 =collection.sort(sortDescriptor);

        // A new native Results should be created.
        assertTrue(collection.getNativePtr() != collection2.getNativePtr());
        assertEquals(2, collection.size());
        assertEquals(2, collection2.size());

        assertEquals(collection2.getUncheckedRow(0).getLong(2), 3);
        assertEquals(collection2.getUncheckedRow(1).getLong(2), 4);
    }

    @Test
    public void clear() {
        assertEquals(table.size(), 4);
        Collection collection = new Collection(sharedRealm, table.where());
        sharedRealm.beginTransaction();
        collection.clear();
        sharedRealm.commitTransaction();
        assertEquals(table.size(), 0);
    }

    @Test
    public void contains() {
        Collection collection = new Collection(sharedRealm, table.where());
        UncheckedRow row = table.getUncheckedRow(0);
        assertTrue(collection.contains(row));
    }

    @Test
    public void indexOf() {
        SortDescriptor sortDescriptor = new SortDescriptor(table, new long[] {2});

        Collection collection = new Collection(sharedRealm, table.where(), sortDescriptor);
        UncheckedRow row = table.getUncheckedRow(0);
        assertEquals(collection.indexOf(row), 3);
    }

    @Test
    public void indexOf_long() {
        SortDescriptor sortDescriptor = new SortDescriptor(table, new long[] {2});

        Collection collection = new Collection(sharedRealm, table.where(), sortDescriptor);
        assertEquals(collection.indexOf(0), 3);
    }

    @Test
    public void distinct() {
        Collection collection = new Collection(sharedRealm, table.where().lessThan(new long[]{2}, 4));

        SortDescriptor distinctDescriptor = new SortDescriptor(table, new long[] {2});
        Collection collection2 =collection.distinct(distinctDescriptor);

        // A new native Results should be created.
        assertTrue(collection.getNativePtr() != collection2.getNativePtr());
        assertEquals(3, collection.size());
        assertEquals(2, collection2.size());

        assertEquals(collection2.getUncheckedRow(0).getLong(2), 3);
        assertEquals(collection2.getUncheckedRow(1).getLong(2), 1);
    }

    // 1. Create a results and add listener.
    // 2. Query results should be returned in the next loop.
    @Test
    @RunTestInLooperThread
    public void addListener_shouldBeCalledToReturnTheQueryResults() {
        final SharedRealm sharedRealm = getSharedRealm();
        Table table = sharedRealm.getTable("test_table");

        final Collection collection = new Collection(sharedRealm, table.where());
        looperThread.keepStrongReference.add(collection);
        collection.addListener(collection, new RealmChangeListener<Collection>() {
            @Override
            public void onChange(Collection collection1) {
                assertEquals(collection1, collection);
                assertEquals(collection1.size(), 4);
                sharedRealm.close();
                looperThread.testComplete();
            }
        });
    }

    // 1. Create a results and add listener on a non-looper thread.
    // 2. Query results should be returned when refresh() called.
    @Test
    public void addListener_shouldBeCalledWhenRefreshToReturnTheQueryResults() {
        final AtomicBoolean onChangeCalled = new AtomicBoolean(false);
        final SharedRealm sharedRealm = getSharedRealm();
        Table table = sharedRealm.getTable("test_table");

        final Collection collection = new Collection(sharedRealm, table.where());
        collection.addListener(collection, new RealmChangeListener<Collection>() {
            @Override
            public void onChange(Collection collection1) {
                assertEquals(collection1, collection);
                assertEquals(collection1.size(), 4);
                sharedRealm.close();
                onChangeCalled.set(true);
            }
        });
        sharedRealm.refresh();
        assertTrue(onChangeCalled.get());
    }

    @Test
    public void addListener_shouldBeCalledWhenRefreshAfterLocalCommit() {
        final CountDownLatch latch = new CountDownLatch(1);
        Collection collection = new Collection(sharedRealm, table.where());
        collection.size();
        collection.addListener(collection, new RealmChangeListener<Collection>() {
            @Override
            public void onChange(Collection element) {
                assertEquals(latch.getCount(), 1);
                latch.countDown();
            }
        });
        sharedRealm.beginTransaction();
        table.addEmptyRow();
        sharedRealm.commitTransaction();
        sharedRealm.refresh();
        TestHelper.awaitOrFail(latch);
    }

    @Test
    public void addListener_shouldBeCalledByWaitForChangeThenRefresh() {
        final CountDownLatch latch = new CountDownLatch(1);
        Collection collection = new Collection(sharedRealm, table.where());
        collection.size();
        collection.addListener(collection, new RealmChangeListener<Collection>() {
            @Override
            public void onChange(Collection element) {
                assertEquals(latch.getCount(), 1);
                latch.countDown();
            }
        });

        addRowAsync();

        sharedRealm.waitForChange();
        sharedRealm.refresh();
        TestHelper.awaitOrFail(latch);
    }

    @Test
    @RunTestInLooperThread
    public void addListener_queryNotReturned() {
        final SharedRealm sharedRealm = getSharedRealm();
        Table table = sharedRealm.getTable("test_table");

        final Collection collection = new Collection(sharedRealm, table.where());
        looperThread.keepStrongReference.add(collection);
        collection.addListener(collection, new RealmChangeListener<Collection>() {
            @Override
            public void onChange(Collection collection1) {
                assertEquals(collection1, collection);
                assertEquals(collection1.size(), 5);
                sharedRealm.close();
                looperThread.testComplete();
            }
        });

        addRowAsync();
    }

    @Test
    @RunTestInLooperThread
    public void addListener_queryReturned() {
        final SharedRealm sharedRealm = getSharedRealm();
        Table table = sharedRealm.getTable("test_table");

        final Collection collection = new Collection(sharedRealm, table.where());
        looperThread.keepStrongReference.add(collection);
        assertEquals(collection.size(), 4); // Trigger the query to run.
        collection.addListener(collection, new RealmChangeListener<Collection>() {
            @Override
            public void onChange(Collection collection1) {
                assertEquals(collection1, collection);
                assertEquals(collection1.size(), 5);
                sharedRealm.close();
                looperThread.testComplete();
            }
        });

        addRowAsync();
    }

    // The query has not been executed.
    // Local commit won't trigger the listener immediately. Instead, the notification comes after the background commit.
    @Test
    @RunTestInLooperThread
    public void addListener_queryNotReturnedLocalAndRemoteCommit() {
        final SharedRealm sharedRealm = getSharedRealm();
        Table table = sharedRealm.getTable("test_table");

        final Collection collection = new Collection(sharedRealm, table.where());
        looperThread.keepStrongReference.add(collection);
        collection.addListener(collection, new RealmChangeListener<Collection>() {
            @Override
            public void onChange(Collection collection1) {
                assertEquals(collection1, collection);
                assertEquals(collection1.size(), 6);
                sharedRealm.close();
                looperThread.testComplete();
            }
        });
        addRow(sharedRealm);
        addRowAsync();
    }

    // The query has not been executed.
    // Local commit will trigger the listener in following event loops.
    @Test
    @RunTestInLooperThread
    public void addListener_queryNotReturnedLocalCommitOnly() {
        final SharedRealm sharedRealm = getSharedRealm();
        Table table = sharedRealm.getTable("test_table");

        final Collection collection = new Collection(sharedRealm, table.where());
        looperThread.keepStrongReference.add(collection);
        collection.addListener(collection, new RealmChangeListener<Collection>() {
            @Override
            public void onChange(Collection collection1) {
                assertEquals(collection1, collection);
                assertEquals(collection1.size(), 5);
                sharedRealm.close();
                looperThread.testComplete();
            }
        });
        addRow(sharedRealm);
    }

    // The query has been executed.
    // Local commit will trigger the listener in following event loops.
    @Test
    @RunTestInLooperThread
    public void addListener_queryReturnedLocalCommitOnly() {
        final SharedRealm sharedRealm = getSharedRealm();
        Table table = sharedRealm.getTable("test_table");

        final Collection collection = new Collection(sharedRealm, table.where());
        assertEquals(collection.size(), 4); // Trigger the query to run.
        looperThread.keepStrongReference.add(collection);
        collection.addListener(collection, new RealmChangeListener<Collection>() {
            @Override
            public void onChange(Collection collection1) {
                assertEquals(collection1, collection);
                assertEquals(collection1.size(), 5);
                sharedRealm.close();
                looperThread.testComplete();
            }
        });
        addRow(sharedRealm);
        assertEquals(collection.size(), 4);
    }

    @Test
    public void detach_byBeginTransaction() {
        final Collection collection = new Collection(sharedRealm, table.where());
        assertFalse(collection.isDetached());
        assertEquals(collection.size(), 4);
        addRowAsync();
        // beginTransaction will do advance read, but the table view should stay without changes.
        sharedRealm.beginTransaction();
        assertTrue(collection.isDetached());
        assertEquals(collection.size(), 4);
    }

    @Test
    public void detach_newCollectionCreatedInTransaction() {
        sharedRealm.beginTransaction();
        final Collection collection = new Collection(sharedRealm, table.where());
        assertTrue(collection.isDetached());
    }

    @Test
    public void detach_commitTransactionWontReattach() {
        final Collection collection = new Collection(sharedRealm, table.where());
        sharedRealm.beginTransaction();
        sharedRealm.commitTransaction();
        assertTrue(collection.isDetached());
        assertEquals(collection.size(), 4);
    }

    @Test
    public void detach_cancelTransactionWontReattach() {
        final Collection collection = new Collection(sharedRealm, table.where());
        sharedRealm.beginTransaction();
        sharedRealm.cancelTransaction();
        assertTrue(collection.isDetached());
        assertEquals(collection.size(), 4);
    }

    @Test
    public void reattach_nonLooperThread_byRefresh() {
        final Collection collection = new Collection(sharedRealm, table.where());
        assertEquals(collection.size(), 4);
        addRow(sharedRealm);
        // The results is backed by snapshot now.
        assertTrue(collection.isDetached());
        assertEquals(collection.size(), 4);
        sharedRealm.refresh();
        // The results is switched back to the original Results.
        assertFalse(collection.isDetached());
        assertEquals(collection.size(), 5);
    }

    @Test
    @RunTestInLooperThread
    public void reattach_looperThread_byLocalTransaction() {
        final SharedRealm sharedRealm = getSharedRealm();
        Table table = sharedRealm.getTable("test_table");
        final Collection collection = new Collection(sharedRealm, table.where());
        looperThread.keepStrongReference.add(collection);
        assertFalse(collection.isDetached());
        assertEquals(collection.size(), 4);
        collection.addListener(collection, new RealmChangeListener<Collection>() {
            @Override
            public void onChange(Collection element) {
                assertFalse(collection.isDetached());
                assertEquals(collection.size(), 5);
                sharedRealm.close();
                looperThread.testComplete();
            }
        });
        addRow(sharedRealm);
        // The results is backed by snapshot now.
        assertTrue(collection.isDetached());
        assertEquals(collection.size(), 4);
    }

    @Test
    @RunTestInLooperThread
    public void reattach_looperThread_byRemoteTransaction() {
        final SharedRealm sharedRealm = getSharedRealm();
        Table table = sharedRealm.getTable("test_table");
        final Collection collection = new Collection(sharedRealm, table.where());
        looperThread.keepStrongReference.add(collection);
        assertFalse(collection.isDetached());
        assertEquals(collection.size(), 4);
        collection.addListener(collection, new RealmChangeListener<Collection>() {
            @Override
            public void onChange(Collection element) {
                assertFalse(collection.isDetached());
                assertEquals(collection.size(), 5);
                sharedRealm.close();
                looperThread.testComplete();
            }
        });
        sharedRealm.beginTransaction();
        sharedRealm.commitTransaction();
        // The results is backed by snapshot now.
        assertTrue(collection.isDetached());
        assertEquals(collection.size(), 4);

        addRowAsync();
    }

    @Test
    @RunTestInLooperThread
    public void reattach_looperThread_shouldHappenBeforeAnyOtherLoopEventWithEmptyLocalTransaction() {
        final SharedRealm sharedRealm = getSharedRealm();
        Table table = sharedRealm.getTable("test_table");
        final Collection collection = new Collection(sharedRealm, table.where());
        looperThread.keepStrongReference.add(collection);
        assertEquals(collection.size(), 4);
        looperThread.postRunnable(new Runnable() {
            @Override
            public void run() {
                // The results is switched back to the original Results.
                assertFalse(collection.isDetached());
                assertEquals(collection.size(), 4);
                sharedRealm.close();
                looperThread.testComplete();
            }
        });
        sharedRealm.beginTransaction();
        sharedRealm.commitTransaction();
        // The results is backed by snapshot now.
        assertTrue(collection.isDetached());
        assertEquals(collection.size(), 4);
    }

    @Test
    @RunTestInLooperThread
    public void reattach_looperThread_shouldHappenBeforeAnyOtherLoopEventWithLocalTransactionCanceled() {
        final SharedRealm sharedRealm = getSharedRealm();
        Table table = sharedRealm.getTable("test_table");
        final Collection[] collections = new Collection[2];
        collections[0] = new Collection(sharedRealm, table.where());
        looperThread.keepStrongReference.add(collections[0]);
        assertEquals(collections[0].size(), 4);
        looperThread.postRunnable(new Runnable() {
            @Override
            public void run() {
                // The results is switched back to the original Results.
                assertFalse(collections[0].isDetached());
                assertEquals(collections[0].size(), 4);
                assertFalse(collections[1].isDetached());
                assertEquals(collections[1].size(), 4);

                sharedRealm.close();
                looperThread.testComplete();
            }
        });
        sharedRealm.beginTransaction();
        // The results is backed by snapshot now.
        assertTrue(collections[0].isDetached());
        assertEquals(collections[0].size(), 4);

        table.addEmptyRow();
        collections[1] = new Collection(sharedRealm, table.where());
        UncheckedRow row = collections[1].getUncheckedRow(4);
        assertTrue(row.isAttached());
        assertEquals(collections[1].size(), 5);
        sharedRealm.cancelTransaction();

        // The results is still backed by snapshot.
        assertTrue(collections[0].isDetached());
        assertEquals(collections[0].size(), 4);
        assertEquals(collections[1].size(), 5);
        row = collections[1].getUncheckedRow(4);
        assertFalse(row.isAttached());
    }

    @Test
    public void getMode() {
        Collection collection = new Collection(sharedRealm, table.where());
        assertTrue(Collection.Mode.QUERY == collection.getMode());
        collection.firstUncheckedRow(); // Run the query
        assertTrue(Collection.Mode.TABLEVIEW == collection.getMode());
    }
}
