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
public class OsResultsTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    private final long[] oneNullTable = new long[] {NativeObject.NULLPTR};

    private OsSharedRealm sharedRealm;
    private Table table;

    @Before
    public void setUp() {
        sharedRealm = getSharedRealm();
        populateData(sharedRealm);
    }

    @After
    public void tearDown() {
        sharedRealm.close();
    }

    private OsSharedRealm getSharedRealm() {
        RealmConfiguration config = configFactory.createConfiguration();
        return getSharedRealm(config);
    }

    private OsSharedRealm getSharedRealmForLooper() {
        RealmConfiguration config = looperThread.createConfiguration();
        return getSharedRealm(config);
    }

    private OsSharedRealm getSharedRealm(RealmConfiguration config) {
        OsRealmConfig.Builder configBuilder = new OsRealmConfig.Builder(config)
                .autoUpdateNotification(true);
        OsSharedRealm sharedRealm = OsSharedRealm.getInstance(configBuilder);
        sharedRealm.beginTransaction();
        OsObjectStore.setSchemaVersion(sharedRealm, OsObjectStore.SCHEMA_NOT_VERSIONED);
        sharedRealm.commitTransaction();
        return sharedRealm;
    }

    private Table getTable(OsSharedRealm sharedRealm) {
        return sharedRealm.getTable(Table.getTableNameForClass("test_table"));
    }

    private void populateData(OsSharedRealm sharedRealm) {
        sharedRealm.beginTransaction();
        table = sharedRealm.createTable(Table.getTableNameForClass("test_table"));
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
        sharedRealm.commitTransaction();
    }

    private void addRowAsync(final OsSharedRealm sharedRealm) {
        final CountDownLatch latch = new CountDownLatch(1);
        final RealmConfiguration configuration = sharedRealm.getConfiguration();
        new Thread(new Runnable() {
            @Override
            public void run() {
                OsSharedRealm sharedRealm = getSharedRealm(configuration);
                addRow(sharedRealm);
                sharedRealm.close();
                latch.countDown();
            }
        }).start();
        TestHelper.awaitOrFail(latch);
    }

    private void addRow(OsSharedRealm sharedRealm) {
        sharedRealm.beginTransaction();
        Table table = getTable(sharedRealm);
        OsObject.createRow(table);
        sharedRealm.commitTransaction();
    }

    @Test
    public void constructor_withDistinct() {
        SortDescriptor distinctDescriptor = SortDescriptor.getInstanceForDistinct(null, table, "firstName");
        OsResults osResults = OsResults.createFromQuery(sharedRealm, table.where(), null, distinctDescriptor);

        assertEquals(3, osResults.size());
        assertEquals("John", osResults.getUncheckedRow(0).getString(0));
        assertEquals("Erik", osResults.getUncheckedRow(1).getString(0));
        assertEquals("Henry", osResults.getUncheckedRow(2).getString(0));
    }


    @Test(expected = UnsupportedOperationException.class)
    public void constructor_queryIsValidated() {
        // OsResults's constructor should call TableQuery.validateQuery()
        OsResults.createFromQuery(sharedRealm, table.where().or());
    }

    @Test
    public void constructor_queryOnDeletedTable() {
        TableQuery query = table.where();
        sharedRealm.beginTransaction();
        assertTrue(OsObjectStore.deleteTableForObject(sharedRealm, table.getClassName()));
        sharedRealm.commitTransaction();
        // Query should be checked before creating OS Results.
        thrown.expect(IllegalStateException.class);
        OsResults.createFromQuery(sharedRealm, query);
    }

    @Test
    public void size() {
        OsResults osResults = OsResults.createFromQuery(sharedRealm, table.where());
        assertEquals(4, osResults.size());
    }

    @Test
    public void where() {
        OsResults osResults = OsResults.createFromQuery(sharedRealm, table.where());
        OsResults osResults2 = OsResults.createFromQuery(sharedRealm, osResults.where().equalTo(new long[] {0}, oneNullTable, "John"));
        OsResults osResults3 = OsResults.createFromQuery(sharedRealm, osResults2.where().equalTo(new long[] {1}, oneNullTable, "Anderson"));

        // A new native Results should be created.
        assertTrue(osResults.getNativePtr() != osResults2.getNativePtr());
        assertTrue(osResults2.getNativePtr() != osResults3.getNativePtr());

        assertEquals(4, osResults.size());
        assertEquals(2, osResults2.size());
        assertEquals(1, osResults3.size());
    }

    @Test
    public void sort() {
        OsResults osResults = OsResults.createFromQuery(sharedRealm, table.where().greaterThan(new long[] {2}, oneNullTable, 1));
        SortDescriptor sortDescriptor = SortDescriptor.getTestInstance(table, new long[] {2});

        OsResults osResults2 = osResults.sort(sortDescriptor);

        // A new native Results should be created.
        assertTrue(osResults.getNativePtr() != osResults2.getNativePtr());
        assertEquals(2, osResults.size());
        assertEquals(2, osResults2.size());

        assertEquals(3, osResults2.getUncheckedRow(0).getLong(2));
        assertEquals(4, osResults2.getUncheckedRow(1).getLong(2));
    }

    @Test
    public void clear() {
        assertEquals(4, table.size());
        OsResults osResults = OsResults.createFromQuery(sharedRealm, table.where());
        sharedRealm.beginTransaction();
        osResults.clear();
        sharedRealm.commitTransaction();
        assertEquals(0, table.size());
    }

    @Test
    public void contains() {
        OsResults osResults = OsResults.createFromQuery(sharedRealm, table.where());
        UncheckedRow row = table.getUncheckedRow(0);
        assertTrue(osResults.contains(row));
    }

    @Test
    public void indexOf() {
        SortDescriptor sortDescriptor = SortDescriptor.getTestInstance(table, new long[] {2});

        OsResults osResults = OsResults.createFromQuery(sharedRealm, table.where(), sortDescriptor, null);
        UncheckedRow row = table.getUncheckedRow(0);
        assertEquals(3, osResults.indexOf(row));
    }

    @Test
    public void distinct() {
        OsResults osResults = OsResults.createFromQuery(sharedRealm, table.where().lessThan(new long[] {2}, oneNullTable, 4));

        SortDescriptor distinctDescriptor = SortDescriptor.getTestInstance(table, new long[] {2});
        OsResults osResults2 = osResults.distinct(distinctDescriptor);

        // A new native Results should be created.
        assertTrue(osResults.getNativePtr() != osResults2.getNativePtr());
        assertEquals(3, osResults.size());
        assertEquals(2, osResults2.size());

        assertEquals(3, osResults2.getUncheckedRow(0).getLong(2));
        assertEquals(1, osResults2.getUncheckedRow(1).getLong(2));
    }

    // 1. Create a results and add listener.
    // 2. Query results should be returned in the next loop.
    @Test
    @RunTestInLooperThread
    public void addListener_shouldBeCalledToReturnTheQueryResults() {
        final OsSharedRealm sharedRealm = getSharedRealmForLooper();
        populateData(sharedRealm);
        Table table = getTable(sharedRealm);

        final OsResults osResults = OsResults.createFromQuery(sharedRealm, table.where());
        looperThread.keepStrongReference(osResults);
        osResults.addListener(osResults, new RealmChangeListener<OsResults>() {
            @Override
            public void onChange(OsResults osResults1) {
                assertEquals(osResults, osResults1);
                assertEquals(4, osResults1.size());
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
        final OsSharedRealm sharedRealm = getSharedRealm();
        Table table = getTable(sharedRealm);

        final OsResults osResults = OsResults.createFromQuery(sharedRealm, table.where());
        osResults.addListener(osResults, new RealmChangeListener<OsResults>() {
            @Override
            public void onChange(OsResults osResults1) {
                assertEquals(osResults, osResults1);
                assertEquals(4, osResults1.size());
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
        final OsResults osResults = OsResults.createFromQuery(sharedRealm, table.where());
        assertEquals(4, osResults.size()); // See `populateData()`
        osResults.addListener(osResults, new RealmChangeListener<OsResults>() {
            @Override
            public void onChange(OsResults element) {
                if (latch.getCount() == 2) {
                    // triggered by beginTransaction
                    assertEquals(4, osResults.size());
                } else if (latch.getCount() == 1) {
                    // triggered by refresh
                    assertEquals(5, osResults.size());
                } else {
                    fail();
                }
                latch.countDown();
            }
        });
        sharedRealm.beginTransaction();
        OsObject.createRow(table);
        sharedRealm.commitTransaction();
        sharedRealm.refresh();
        TestHelper.awaitOrFail(latch);
    }

    // Local commit will trigger the listener first when beginTransaction gets called then again when call refresh.
    @Test
    public void addListener_triggeredByRefresh() {
        final CountDownLatch latch = new CountDownLatch(1);
        OsResults osResults = OsResults.createFromQuery(sharedRealm, table.where());
        osResults.size();
        osResults.addListener(osResults, new RealmChangeListener<OsResults>() {
            @Override
            public void onChange(OsResults element) {
                assertEquals(1, latch.getCount());
                latch.countDown();
            }
        });

        addRowAsync(sharedRealm);

        sharedRealm.waitForChange();
        sharedRealm.refresh();
        TestHelper.awaitOrFail(latch);
    }

    @Test
    @RunTestInLooperThread
    public void addListener_queryNotReturned() {
        final OsSharedRealm sharedRealm = getSharedRealmForLooper();
        populateData(sharedRealm);
        Table table = getTable(sharedRealm);

        final OsResults osResults = OsResults.createFromQuery(sharedRealm, table.where());
        looperThread.keepStrongReference(osResults);
        osResults.addListener(osResults, new RealmChangeListener<OsResults>() {
            @Override
            public void onChange(OsResults osResults1) {
                assertEquals(osResults, osResults1);
                assertEquals(5, osResults1.size());
                sharedRealm.close();
                looperThread.testComplete();
            }
        });

        addRowAsync(sharedRealm);
    }

    @Test
    @RunTestInLooperThread
    public void addListener_queryReturned() {
        final OsSharedRealm sharedRealm = getSharedRealmForLooper();
        populateData(sharedRealm);
        Table table = getTable(sharedRealm);

        final OsResults osResults = OsResults.createFromQuery(sharedRealm, table.where());
        looperThread.keepStrongReference(osResults);
        assertEquals(4, osResults.size()); // Trigger the query to run.
        osResults.addListener(osResults, new RealmChangeListener<OsResults>() {
            @Override
            public void onChange(OsResults osResults1) {
                assertEquals(osResults, osResults1);
                assertEquals(5, osResults1.size());
                sharedRealm.close();
                looperThread.testComplete();
            }
        });

        addRowAsync(sharedRealm);
    }

    // Local commit will trigger the listener first when beginTransaction gets called then again when transaction
    // committed.
    @Test
    @RunTestInLooperThread
    public void addListener_triggeredByLocalCommit() {
        final OsSharedRealm sharedRealm = getSharedRealmForLooper();
        populateData(sharedRealm);
        Table table = getTable(sharedRealm);
        final AtomicInteger listenerCounter = new AtomicInteger(0);

        final OsResults osResults = OsResults.createFromQuery(sharedRealm, table.where());
        looperThread.keepStrongReference(osResults);
        osResults.addListener(osResults, new RealmChangeListener<OsResults>() {
            @Override
            public void onChange(OsResults osResults1) {
                switch (listenerCounter.getAndIncrement()) {
                    case 0:
                        assertEquals(4, osResults1.size());
                        break;
                    case 1:
                        assertEquals(5, osResults1.size());
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

    private static class TestIterator extends OsResults.Iterator<Integer> {
        TestIterator(OsResults osResults) {
            super(osResults);
        }

        @Override
        protected Integer convertRowToObject(UncheckedRow row) {
            return null;
        }

        boolean isDetached(OsSharedRealm sharedRealm) {
            for (WeakReference<OsResults.Iterator> iteratorRef : sharedRealm.iterators) {
                OsResults.Iterator iterator = iteratorRef.get();
                if (iterator == this) {
                    return false;
                }
            }
            return true;
        }
    }

    @Test
    public void collectionIterator_detach_byBeginTransaction() {
        final OsResults osResults = OsResults.createFromQuery(sharedRealm, table.where());
        TestIterator iterator = new TestIterator(osResults);
        assertFalse(iterator.isDetached(sharedRealm));
        sharedRealm.beginTransaction();
        assertTrue(iterator.isDetached(sharedRealm));
        sharedRealm.commitTransaction();
        assertTrue(iterator.isDetached(sharedRealm));
    }

    @Test
    public void collectionIterator_detach_createdInTransaction() {
        sharedRealm.beginTransaction();
        final OsResults osResults = OsResults.createFromQuery(sharedRealm, table.where());
        TestIterator iterator = new TestIterator(osResults);
        assertTrue(iterator.isDetached(sharedRealm));
    }

    @Test
    public void collectionIterator_invalid_nonLooperThread_byRefresh() {
        final OsResults osResults = OsResults.createFromQuery(sharedRealm, table.where());
        TestIterator iterator = new TestIterator(osResults);
        assertFalse(iterator.isDetached(sharedRealm));
        sharedRealm.refresh();
        thrown.expect(ConcurrentModificationException.class);
        iterator.checkValid();
    }

    @Test
    @RunTestInLooperThread
    public void collectionIterator_invalid_looperThread_byRemoteTransaction() {
        final OsSharedRealm sharedRealm = getSharedRealmForLooper();
        populateData(sharedRealm);
        Table table = getTable(sharedRealm);
        final OsResults osResults = OsResults.createFromQuery(sharedRealm, table.where());
        final TestIterator iterator = new TestIterator(osResults);
        looperThread.keepStrongReference(osResults);
        assertFalse(iterator.isDetached(sharedRealm));
        osResults.addListener(osResults, new RealmChangeListener<OsResults>() {
            @Override
            public void onChange(OsResults element) {
                try {
                    iterator.checkValid();
                    fail();
                } catch (ConcurrentModificationException ignored) {
                }
                sharedRealm.close();
                looperThread.testComplete();
            }
        });

        addRowAsync(sharedRealm);
    }

    @Test
    public void collectionIterator_newInstance_throwsWhenSharedRealmIsClosed() {
        final OsResults osResults = OsResults.createFromQuery(sharedRealm, table.where());
        sharedRealm.close();
        thrown.expect(IllegalStateException.class);
        new TestIterator(osResults);
    }

    @Test
    public void getMode() {
        OsResults osResults = OsResults.createFromQuery(sharedRealm, table.where());
        assertTrue(OsResults.Mode.QUERY == osResults.getMode());
        osResults.firstUncheckedRow(); // Run the query
        assertTrue(OsResults.Mode.TABLEVIEW == osResults.getMode());
    }

    @Test
    public void createSnapshot() {
        OsResults osResults = OsResults.createFromQuery(sharedRealm, table.where());
        OsResults snapshot = osResults.createSnapshot();
        assertTrue(OsResults.Mode.TABLEVIEW == snapshot.getMode());
        thrown.expect(IllegalStateException.class);
        snapshot.addListener(snapshot, new RealmChangeListener<OsResults>() {
            @Override
            public void onChange(OsResults element) {
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void load() {
        final OsSharedRealm sharedRealm = getSharedRealmForLooper();
        looperThread.closeAfterTest(sharedRealm);
        populateData(sharedRealm);
        final OsResults osResults = OsResults.createFromQuery(sharedRealm, table.where());
        osResults.addListener(osResults, new RealmChangeListener<OsResults>() {
            @Override
            public void onChange(OsResults element) {
                assertTrue(osResults.isLoaded());
                looperThread.testComplete();
            }
        });
        assertFalse(osResults.isLoaded());
        osResults.load();
    }
}
