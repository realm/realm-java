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

import java.lang.ref.WeakReference;
import java.util.ConcurrentModificationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
import static junit.framework.Assert.fail;


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
        Collection collection2 = new Collection(sharedRealm, collection.where().equalTo(new long[]{0}, "John"));
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

        Collection collection2 = collection.sort(sortDescriptor);

        // A new native Results should be created.
        assertTrue(collection.getNativePtr() != collection2.getNativePtr());
        assertEquals(2, collection.size());
        assertEquals(2, collection2.size());

        assertEquals(collection2.getUncheckedRow(0).getLong(2), 3);
        assertEquals(collection2.getUncheckedRow(1).getLong(2), 4);
    }

    @Test
    public void clear() {
        assertEquals(4, table.size());
        Collection collection = new Collection(sharedRealm, table.where());
        sharedRealm.beginTransaction();
        collection.clear();
        sharedRealm.commitTransaction();
        assertEquals(0, table.size());
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
        assertEquals(3, collection.indexOf(row));
    }

    @Test
    public void indexOf_long() {
        SortDescriptor sortDescriptor = new SortDescriptor(table, new long[] {2});

        Collection collection = new Collection(sharedRealm, table.where(), sortDescriptor);
        assertEquals(3, collection.indexOf(0));
    }

    @Test
    public void distinct() {
        Collection collection = new Collection(sharedRealm, table.where().lessThan(new long[]{2}, 4));

        SortDescriptor distinctDescriptor = new SortDescriptor(table, new long[] {2});
        Collection collection2 = collection.distinct(distinctDescriptor);

        // A new native Results should be created.
        assertTrue(collection.getNativePtr() != collection2.getNativePtr());
        assertEquals(3, collection.size());
        assertEquals(2, collection2.size());

        assertEquals(3, collection2.getUncheckedRow(0).getLong(2));
        assertEquals(1, collection2.getUncheckedRow(1).getLong(2));
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
                assertEquals(collection, collection1);
                assertEquals(4, collection1.size());
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
                assertEquals(collection, collection1);
                assertEquals(4, collection1.size());
                sharedRealm.close();
                onChangeCalled.set(true);
            }
        });
        sharedRealm.refresh();
        assertTrue(onChangeCalled.get());
    }

    @Test
    public void addListener_shouldBeCalledWhenRefreshAfterLocalCommit() {
        final CountDownLatch latch = new CountDownLatch(2);
        final Collection collection = new Collection(sharedRealm, table.where());
        assertEquals(4, collection.size()); // See `populateData()`
        collection.addListener(collection, new RealmChangeListener<Collection>() {
            @Override
            public void onChange(Collection element) {
                if (latch.getCount() == 2) {
                    // triggered by beginTransaction
                    assertEquals(4, collection.size());
                } else if (latch.getCount() == 1) {
                    // triggered by refresh
                    assertEquals(5, collection.size());
                } else {
                    fail();
                }
                latch.countDown();
            }
        });
        sharedRealm.beginTransaction();
        table.addEmptyRow();
        sharedRealm.commitTransaction();
        sharedRealm.refresh();
        TestHelper.awaitOrFail(latch);
    }

    // Local commit will trigger the listener first when beginTransaction gets called then again when call refresh.
    @Test
    public void addListener_triggeredByRefresh() {
        final CountDownLatch latch = new CountDownLatch(1);
        Collection collection = new Collection(sharedRealm, table.where());
        collection.size();
        collection.addListener(collection, new RealmChangeListener<Collection>() {
            @Override
            public void onChange(Collection element) {
                assertEquals(1, latch.getCount());
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
                assertEquals(collection, collection1);
                assertEquals(5, collection1.size());
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
                assertEquals(collection, collection1);
                assertEquals(5, collection1.size());
                sharedRealm.close();
                looperThread.testComplete();
            }
        });

        addRowAsync();
    }

    // Local commit will trigger the listener first when beginTransaction gets called then again when transaction
    // committed.
    @Test
    @RunTestInLooperThread
    public void addListener_triggeredByLocalCommit() {
        final SharedRealm sharedRealm = getSharedRealm();
        Table table = sharedRealm.getTable("test_table");
        final AtomicInteger listenerCounter = new AtomicInteger(0);

        final Collection collection = new Collection(sharedRealm, table.where());
        looperThread.keepStrongReference.add(collection);
        collection.addListener(collection, new RealmChangeListener<Collection>() {
            @Override
            public void onChange(Collection collection1) {
                switch (listenerCounter.getAndIncrement()) {
                    case 0:
                        assertEquals(4, collection1.size());
                        break;
                    case 1:
                        assertEquals(5, collection1.size());
                        sharedRealm.close();
                        break;
                    default:
                        fail();
                        break;
                }
            }
        });
        addRow(sharedRealm);
        assertEquals(2, listenerCounter.get());
        looperThread.testComplete();
    }

    private static class TestIterator extends Collection.Iterator<Integer> {
        TestIterator(Collection collection) {
            super(collection);
        }

        @Override
        protected Integer convertRowToObject(UncheckedRow row) {
            return null;
        }

        boolean isDetached(SharedRealm sharedRealm) {
            for (WeakReference<Collection.Iterator> iteratorRef : sharedRealm.iterators) {
                Collection.Iterator iterator = iteratorRef.get();
                if (iterator == this) {
                    return false;
                }
            }
            return true;
        }
    }

    @Test
    public void collectionIterator_detach_byBeginTransaction() {
        final Collection collection = new Collection(sharedRealm, table.where());
        TestIterator iterator = new TestIterator(collection);
        assertFalse(iterator.isDetached(sharedRealm));
        sharedRealm.beginTransaction();
        assertTrue(iterator.isDetached(sharedRealm));
        sharedRealm.commitTransaction();
        assertTrue(iterator.isDetached(sharedRealm));
    }

    @Test
    public void collectionIterator_detach_createdInTransaction() {
        sharedRealm.beginTransaction();
        final Collection collection = new Collection(sharedRealm, table.where());
        TestIterator iterator = new TestIterator(collection);
        assertTrue(iterator.isDetached(sharedRealm));
    }

    @Test
    public void collectionIterator_invalid_nonLooperThread_byRefresh() {
        final Collection collection = new Collection(sharedRealm, table.where());
        TestIterator iterator = new TestIterator(collection);
        assertFalse(iterator.isDetached(sharedRealm));
        sharedRealm.refresh();
        thrown.expect(ConcurrentModificationException.class);
        iterator.checkValid();
    }

    @Test
    @RunTestInLooperThread
    public void collectionIterator_invalid_looperThread_byRemoteTransaction() {
        final SharedRealm sharedRealm = getSharedRealm();
        Table table = sharedRealm.getTable("test_table");
        final Collection collection = new Collection(sharedRealm, table.where());
        final TestIterator iterator = new TestIterator(collection);
        looperThread.keepStrongReference.add(collection);
        assertFalse(iterator.isDetached(sharedRealm));
        collection.addListener(collection, new RealmChangeListener<Collection>() {
            @Override
            public void onChange(Collection element) {
                try {
                    iterator.checkValid();
                    fail();
                } catch (ConcurrentModificationException ignored) {
                }
                sharedRealm.close();
                looperThread.testComplete();
            }
        });

        addRowAsync();
    }

    @Test
    public void collectionIterator_newInstance_throwsWhenSharedRealmIsClosed() {
        final Collection collection = new Collection(sharedRealm, table.where());
        sharedRealm.close();
        thrown.expect(IllegalStateException.class);
        new TestIterator(collection);
    }

    @Test
    public void getMode() {
        Collection collection = new Collection(sharedRealm, table.where());
        assertTrue(Collection.Mode.QUERY == collection.getMode());
        collection.firstUncheckedRow(); // Run the query
        assertTrue(Collection.Mode.TABLEVIEW == collection.getMode());
    }

    @Test
    public void createSnapshot() {
        Collection collection = new Collection(sharedRealm, table.where());
        Collection snapshot = collection.createSnapshot();
        assertTrue(Collection.Mode.TABLEVIEW == snapshot.getMode());
        thrown.expect(IllegalStateException.class);
        snapshot.addListener(snapshot, new RealmChangeListener<Collection>() {
            @Override
            public void onChange(Collection element) {
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void load() {
        final Collection collection = new Collection(sharedRealm, table.where());
        collection.addListener(collection, new RealmChangeListener<Collection>() {
            @Override
            public void onChange(Collection element) {
                assertTrue(collection.isLoaded());
                looperThread.testComplete();
            }
        });
        assertFalse(collection.isLoaded());
        collection.load();
    }
}
