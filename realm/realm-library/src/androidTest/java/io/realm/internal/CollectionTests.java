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

    private final long[] oneNullTable = new long[] {NativeObject.NULLPTR};

    private RealmConfiguration config;
    private OsSharedRealm osSharedRealm;
    private Table table;

    @Before
    public void setUp() {
        config = configFactory.createConfiguration();
        osSharedRealm = getOsSharedRealm();
        populateData();
    }

    @After
    public void tearDown() {
        osSharedRealm.close();
    }

    private OsSharedRealm getOsSharedRealm() {
        return OsSharedRealm.getInstance(config, null, true);
    }

    private void populateData() {
        osSharedRealm.beginTransaction();
        table = osSharedRealm.createTable("test_table");
        // Specify the column types and names
        long columnIdx = table.addColumn(RealmFieldType.STRING, "firstName");
        table.addSearchIndex(columnIdx);
        table.addColumn(RealmFieldType.STRING, "lastName");
        table.addColumn(RealmFieldType.INTEGER, "age");

        // Add data to the table
        long row = OsObject.createRow(table);
        table.setString(0, row, "John", false);
        table.setString(1, row, "Lee", false);
        table.setLong(2, row, 4, false);

        row = OsObject.createRow(table);
        table.setString(0, row, "John", false);
        table.setString(1, row, "Anderson", false);
        table.setLong(2, row, 3, false);

        row = OsObject.createRow(table);
        table.setString(0, row, "Erik", false);
        table.setString(1, row, "Lee", false);
        table.setLong(2, row, 1, false);

        row = OsObject.createRow(table);
        table.setString(0, row, "Henry", false);
        table.setString(1, row, "Anderson", false);
        table.setLong(2, row, 1, false);
        osSharedRealm.commitTransaction();
    }

    private void addRowAsync() {
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                OsSharedRealm osSharedRealm = getOsSharedRealm();
                addRow(osSharedRealm);
                osSharedRealm.close();
                latch.countDown();
            }
        }).start();
        TestHelper.awaitOrFail(latch);
    }

    private void addRow(OsSharedRealm osSharedRealm) {
        osSharedRealm.beginTransaction();
        table = osSharedRealm.getTable("test_table");
        OsObject.createRow(table);
        osSharedRealm.commitTransaction();
    }

    @Test
    public void constructor_withDistinct() {
        SortDescriptor distinctDescriptor = SortDescriptor.getInstanceForDistinct(null, table, "firstName");
        Collection collection = new Collection(osSharedRealm, table.where(), null, distinctDescriptor);

        assertEquals(3, collection.size());
        assertEquals("John", collection.getUncheckedRow(0).getString(0));
        assertEquals("Erik", collection.getUncheckedRow(1).getString(0));
        assertEquals("Henry", collection.getUncheckedRow(2).getString(0));
    }


    @Test(expected = UnsupportedOperationException.class)
    public void constructor_queryIsValidated() {
        // Collection's constructor should call TableQuery.validateQuery()
        new Collection(osSharedRealm, table.where().or());
    }

    @Test
    public void constructor_queryOnDeletedTable() {
        TableQuery query = table.where();
        osSharedRealm.beginTransaction();
        osSharedRealm.removeTable(table.getName());
        osSharedRealm.commitTransaction();
        // Query should be checked before creating OS Results.
        thrown.expect(IllegalStateException.class);
        new Collection(osSharedRealm, query);
    }

    @Test
    public void size() {
        Collection collection = new Collection(osSharedRealm, table.where());
        assertEquals(4, collection.size());
    }

    @Test
    public void where() {
        Collection collection = new Collection(osSharedRealm, table.where());
        Collection collection2 = new Collection(osSharedRealm, collection.where().equalTo(new long[] {0}, oneNullTable, "John"));
        Collection collection3 = new Collection(osSharedRealm, collection2.where().equalTo(new long[] {1}, oneNullTable, "Anderson"));

        // A new native Results should be created.
        assertTrue(collection.getNativePtr() != collection2.getNativePtr());
        assertTrue(collection2.getNativePtr() != collection3.getNativePtr());

        assertEquals(4, collection.size());
        assertEquals(2, collection2.size());
        assertEquals(1, collection3.size());
    }

    @Test
    public void sort() {
        Collection collection = new Collection(osSharedRealm, table.where().greaterThan(new long[] {2}, oneNullTable, 1));
        SortDescriptor sortDescriptor = SortDescriptor.getTestInstance(table, new long[] {2});

        Collection collection2 = collection.sort(sortDescriptor);

        // A new native Results should be created.
        assertTrue(collection.getNativePtr() != collection2.getNativePtr());
        assertEquals(2, collection.size());
        assertEquals(2, collection2.size());

        assertEquals(3, collection2.getUncheckedRow(0).getLong(2));
        assertEquals(4, collection2.getUncheckedRow(1).getLong(2));
    }

    @Test
    public void clear() {
        assertEquals(4, table.size());
        Collection collection = new Collection(osSharedRealm, table.where());
        osSharedRealm.beginTransaction();
        collection.clear();
        osSharedRealm.commitTransaction();
        assertEquals(0, table.size());
    }

    @Test
    public void contains() {
        Collection collection = new Collection(osSharedRealm, table.where());
        UncheckedRow row = table.getUncheckedRow(0);
        assertTrue(collection.contains(row));
    }

    @Test
    public void indexOf() {
        SortDescriptor sortDescriptor = SortDescriptor.getTestInstance(table, new long[] {2});

        Collection collection = new Collection(osSharedRealm, table.where(), sortDescriptor);
        UncheckedRow row = table.getUncheckedRow(0);
        assertEquals(3, collection.indexOf(row));
    }

    @Test
    public void indexOf_long() {
        SortDescriptor sortDescriptor = SortDescriptor.getTestInstance(table, new long[] {2});

        Collection collection = new Collection(osSharedRealm, table.where(), sortDescriptor);
        assertEquals(3, collection.indexOf(0));
    }

    @Test
    public void distinct() {
        Collection collection = new Collection(osSharedRealm, table.where().lessThan(new long[] {2}, oneNullTable, 4));

        SortDescriptor distinctDescriptor = SortDescriptor.getTestInstance(table, new long[] {2});
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
        final OsSharedRealm osSharedRealm = getOsSharedRealm();
        Table table = osSharedRealm.getTable("test_table");

        final Collection collection = new Collection(osSharedRealm, table.where());
        looperThread.keepStrongReference(collection);
        collection.addListener(collection, new RealmChangeListener<Collection>() {
            @Override
            public void onChange(Collection collection1) {
                assertEquals(collection, collection1);
                assertEquals(4, collection1.size());
                osSharedRealm.close();
                looperThread.testComplete();
            }
        });
    }

    // 1. Create a results and add listener on a non-looper thread.
    // 2. Query results should be returned when refresh() called.
    @Test
    public void addListener_shouldBeCalledWhenRefreshToReturnTheQueryResults() {
        final AtomicBoolean onChangeCalled = new AtomicBoolean(false);
        final OsSharedRealm osSharedRealm = getOsSharedRealm();
        Table table = osSharedRealm.getTable("test_table");

        final Collection collection = new Collection(osSharedRealm, table.where());
        collection.addListener(collection, new RealmChangeListener<Collection>() {
            @Override
            public void onChange(Collection collection1) {
                assertEquals(collection, collection1);
                assertEquals(4, collection1.size());
                osSharedRealm.close();
                onChangeCalled.set(true);
            }
        });
        osSharedRealm.refresh();
        assertTrue(onChangeCalled.get());
    }

    @Test
    public void addListener_shouldBeCalledWhenRefreshAfterLocalCommit() {
        final CountDownLatch latch = new CountDownLatch(2);
        final Collection collection = new Collection(osSharedRealm, table.where());
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
        osSharedRealm.beginTransaction();
        OsObject.createRow(table);
        osSharedRealm.commitTransaction();
        osSharedRealm.refresh();
        TestHelper.awaitOrFail(latch);
    }

    // Local commit will trigger the listener first when beginTransaction gets called then again when call refresh.
    @Test
    public void addListener_triggeredByRefresh() {
        final CountDownLatch latch = new CountDownLatch(1);
        Collection collection = new Collection(osSharedRealm, table.where());
        collection.size();
        collection.addListener(collection, new RealmChangeListener<Collection>() {
            @Override
            public void onChange(Collection element) {
                assertEquals(1, latch.getCount());
                latch.countDown();
            }
        });

        addRowAsync();

        osSharedRealm.waitForChange();
        osSharedRealm.refresh();
        TestHelper.awaitOrFail(latch);
    }

    @Test
    @RunTestInLooperThread
    public void addListener_queryNotReturned() {
        final OsSharedRealm osSharedRealm = getOsSharedRealm();
        Table table = osSharedRealm.getTable("test_table");

        final Collection collection = new Collection(osSharedRealm, table.where());
        looperThread.keepStrongReference(collection);
        collection.addListener(collection, new RealmChangeListener<Collection>() {
            @Override
            public void onChange(Collection collection1) {
                assertEquals(collection, collection1);
                assertEquals(5, collection1.size());
                osSharedRealm.close();
                looperThread.testComplete();
            }
        });

        addRowAsync();
    }

    @Test
    @RunTestInLooperThread
    public void addListener_queryReturned() {
        final OsSharedRealm osSharedRealm = getOsSharedRealm();
        Table table = osSharedRealm.getTable("test_table");

        final Collection collection = new Collection(osSharedRealm, table.where());
        looperThread.keepStrongReference(collection);
        assertEquals(4, collection.size()); // Trigger the query to run.
        collection.addListener(collection, new RealmChangeListener<Collection>() {
            @Override
            public void onChange(Collection collection1) {
                assertEquals(collection, collection1);
                assertEquals(5, collection1.size());
                osSharedRealm.close();
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
        final OsSharedRealm osSharedRealm = getOsSharedRealm();
        Table table = osSharedRealm.getTable("test_table");
        final AtomicInteger listenerCounter = new AtomicInteger(0);

        final Collection collection = new Collection(osSharedRealm, table.where());
        looperThread.keepStrongReference(collection);
        collection.addListener(collection, new RealmChangeListener<Collection>() {
            @Override
            public void onChange(Collection collection1) {
                switch (listenerCounter.getAndIncrement()) {
                    case 0:
                        assertEquals(4, collection1.size());
                        break;
                    case 1:
                        assertEquals(5, collection1.size());
                        osSharedRealm.close();
                        break;
                    default:
                        fail();
                        break;
                }
            }
        });
        addRow(osSharedRealm);
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

        boolean isDetached(OsSharedRealm osSharedRealm) {
            for (WeakReference<Collection.Iterator> iteratorRef : osSharedRealm.iterators) {
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
        final Collection collection = new Collection(osSharedRealm, table.where());
        TestIterator iterator = new TestIterator(collection);
        assertFalse(iterator.isDetached(osSharedRealm));
        osSharedRealm.beginTransaction();
        assertTrue(iterator.isDetached(osSharedRealm));
        osSharedRealm.commitTransaction();
        assertTrue(iterator.isDetached(osSharedRealm));
    }

    @Test
    public void collectionIterator_detach_createdInTransaction() {
        osSharedRealm.beginTransaction();
        final Collection collection = new Collection(osSharedRealm, table.where());
        TestIterator iterator = new TestIterator(collection);
        assertTrue(iterator.isDetached(osSharedRealm));
    }

    @Test
    public void collectionIterator_invalid_nonLooperThread_byRefresh() {
        final Collection collection = new Collection(osSharedRealm, table.where());
        TestIterator iterator = new TestIterator(collection);
        assertFalse(iterator.isDetached(osSharedRealm));
        osSharedRealm.refresh();
        thrown.expect(ConcurrentModificationException.class);
        iterator.checkValid();
    }

    @Test
    @RunTestInLooperThread
    public void collectionIterator_invalid_looperThread_byRemoteTransaction() {
        final OsSharedRealm osSharedRealm = getOsSharedRealm();
        Table table = osSharedRealm.getTable("test_table");
        final Collection collection = new Collection(osSharedRealm, table.where());
        final TestIterator iterator = new TestIterator(collection);
        looperThread.keepStrongReference(collection);
        assertFalse(iterator.isDetached(osSharedRealm));
        collection.addListener(collection, new RealmChangeListener<Collection>() {
            @Override
            public void onChange(Collection element) {
                try {
                    iterator.checkValid();
                    fail();
                } catch (ConcurrentModificationException ignored) {
                }
                osSharedRealm.close();
                looperThread.testComplete();
            }
        });

        addRowAsync();
    }

    @Test
    public void collectionIterator_newInstance_throwsWhenSharedRealmIsClosed() {
        final Collection collection = new Collection(osSharedRealm, table.where());
        osSharedRealm.close();
        thrown.expect(IllegalStateException.class);
        new TestIterator(collection);
    }

    @Test
    public void getMode() {
        Collection collection = new Collection(osSharedRealm, table.where());
        assertTrue(Collection.Mode.QUERY == collection.getMode());
        collection.firstUncheckedRow(); // Run the query
        assertTrue(Collection.Mode.TABLEVIEW == collection.getMode());
    }

    @Test
    public void createSnapshot() {
        Collection collection = new Collection(osSharedRealm, table.where());
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
        final Collection collection = new Collection(osSharedRealm, table.where());
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
