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

import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.realm.entities.AllTypes;
import io.realm.exceptions.RealmException;

public class RealmResultsIteratorTests extends AndroidTestCase {

    protected final static int TEST_DATA_SIZE = 10;
    protected Realm testRealm;

    @Override
    protected void setUp() throws InterruptedException {
        RealmConfiguration realmConfig = TestHelper.createConfiguration(getContext());
        Realm.deleteRealm(realmConfig);
        testRealm = Realm.getInstance(realmConfig);
        testRealm.beginTransaction();
        for (int i = 0; i < TEST_DATA_SIZE; ++i) {
            AllTypes allTypes = testRealm.createObject(AllTypes.class);
            allTypes.setColumnBoolean((i % 2) == 0);
            allTypes.setColumnBinary(new byte[]{1, 2, 3});
            allTypes.setColumnDate(new Date((long) i));
            allTypes.setColumnDouble(3.1415 + i);
            allTypes.setColumnFloat(1.234567f + i);
            allTypes.setColumnString("test data " + i);
            allTypes.setColumnLong(i);
        }
        testRealm.commitTransaction();
    }

    @Override
    protected void tearDown() throws Exception {
        testRealm.close();
    }

    public void testListIteratorAtBeginning() {
        ListIterator<AllTypes> it = testRealm.allObjects(AllTypes.class).listIterator();
        assertFalse(it.hasPrevious());
        assertEquals(-1, it.previousIndex());
        assertTrue(it.hasNext());
        assertEquals(0, it.nextIndex());
    }

    public void testListIteratorAtEnd() {
        ListIterator<AllTypes> it = testRealm.allObjects(AllTypes.class).listIterator(TEST_DATA_SIZE);
        assertTrue(it.hasPrevious());
        assertEquals(TEST_DATA_SIZE - 1, it.previousIndex());
        assertFalse(it.hasNext());
        assertEquals(TEST_DATA_SIZE, it.nextIndex());
    }
    // TODO: Should we reenable this test?
    public void DISABLEDtestListIteratorRemove() {
        RealmResults<AllTypes> result = testRealm.allObjects(AllTypes.class);

        ListIterator<AllTypes> it = result.listIterator();
        while (it.hasNext()) {
            testRealm.beginTransaction();
            it.next();
            it.remove();
            assertFalse(it.hasPrevious());
            testRealm.commitTransaction();
        }

        assertEquals(0, testRealm.allObjects(AllTypes.class).size());
    }
    // TODO: Should we reenable this test?
    public void DISABLEDtestListIteratorFailOnDeleteBeforeNext() {
        RealmResults<AllTypes> result = testRealm.allObjects(AllTypes.class);

        ListIterator<AllTypes> it = result.listIterator();
        testRealm.beginTransaction();
        try {
            it.remove();
        } catch (IllegalStateException ignored) {
            return;
        } finally {
            testRealm.cancelTransaction();
        }

        fail("You must call next() before calling remove() pr. the JavaDoc");
    }
    // TODO: Should we reenable this test?
    public void DISABLEDtestListIteratorFailOnDoubleRemove() {
        RealmResults<AllTypes> result = testRealm.allObjects(AllTypes.class);

        ListIterator<AllTypes> it = result.listIterator();
        testRealm.beginTransaction();
        it.next();
        it.remove();

        try {
            it.remove();
        } catch (IllegalStateException ignored) {
            return;
        } finally {
            testRealm.cancelTransaction();
        }

        fail("You can only make one call to remove() after calling next() pr. the JavaDoc");
    }

    private enum ListIteratorMethods { ADD, SET; }
    public void testListIteratorUnsupportedMethods() {
        for (ListIteratorMethods method : ListIteratorMethods.values()) {
            ListIterator<AllTypes> it = testRealm.allObjects(AllTypes.class).listIterator();
            try {
                switch (method) {
                    case ADD: it.add(new AllTypes()); break;
                    case SET: it.set(new AllTypes()); break;
                }

                fail(method + " should not be supported");

            } catch(RealmException ignored) {
            }
        }
    }

    public void testRemovingObjectsInsideLoop() {
        RealmResults<AllTypes> result = testRealm.allObjects(AllTypes.class);

        testRealm.beginTransaction();
        try {
            for (AllTypes obj : result) {
                obj.removeFromRealm();
            }
        } catch (ConcurrentModificationException ignored) {
            return;
        } finally {
            testRealm.cancelTransaction();
        }

        fail("Modifying Realm while iterating is not allowed");
    }

    // TODO: Should we reenable this test?
    // Query iterator should still be valid if we modify Realm after query but before iterator is
    // fetched.
    public void DISABLEDtestIteratorValidAfterAutoUpdate() {
        RealmResults<AllTypes> result = testRealm.allObjects(AllTypes.class);

        testRealm.beginTransaction();
        result.removeLast();
        testRealm.commitTransaction();

        // For..each uses RealmResults.iterator() in the byte code.
        long realmSum = 0;
        for (AllTypes obj : result) {
            realmSum += obj.getColumnLong();
        }

        assertEquals(sum(0, TEST_DATA_SIZE - 2), realmSum);
    }


    public void testIteratorStandardBehavior() {
        RealmResults<AllTypes> result = testRealm.allObjects(AllTypes.class);

        // For..each uses RealmResults.iterator() in the byte code.
        long realmSum = 0;
        for (AllTypes obj : result) {
            realmSum += obj.getColumnLong();
        }

        assertEquals(sum(0, TEST_DATA_SIZE - 1), realmSum);
    }
    // TODO: Should we reenable this test?
    public void DISABLEDtestIteratorRemove() {
        RealmResults<AllTypes> result = testRealm.allObjects(AllTypes.class);

        Iterator<AllTypes> it = result.iterator();
        while (it.hasNext()) {
            testRealm.beginTransaction();
            it.next();
            it.remove();
            testRealm.commitTransaction();
        }

        assertEquals(0, testRealm.allObjects(AllTypes.class).size());
    }

    // TODO: Should we reenable this test?
    public void DISABLEDtestIteratorFailOnDeleteBeforeNext() {
        RealmResults<AllTypes> result = testRealm.allObjects(AllTypes.class);

        Iterator<AllTypes> it = result.iterator();
        testRealm.beginTransaction();
        try {
            it.remove();
        } catch (IllegalStateException ignored) {
            return;
        } finally {
            testRealm.cancelTransaction();
        }

        fail("You must call next() before calling remove() pr. the JavaDoc");
    }
    // TODO: Should we reenable this test?
    public void DISABLEDtestIteratorFailOnDoubleRemove() {
        RealmResults<AllTypes> result = testRealm.allObjects(AllTypes.class);

        Iterator<AllTypes> it = result.iterator();
        testRealm.beginTransaction();
        it.next();
        it.remove();

        try {
            it.remove();

        } catch (IllegalStateException ignored) {
            return;
        } finally {
            testRealm.cancelTransaction();
        }

        fail("You can only make one call to remove() after calling next() pr. the JavaDoc");
    }


    // TODO: Should we reenable this test?
    // Using size() as heuristic for concurrent modifications is dangerous as we might skip
    // elements.
    // TODO Possible bug: Why does this interfere with reference counting check. They are separate Realm files.
    // TODO Possible bug: Why is realm.refresh() needed?
    public void DISABLEDtestRemovingObjectsFromOtherThreadWhileIterating() throws InterruptedException, ExecutionException {

        // Prefill
        final RealmConfiguration realmConfig = TestHelper.createConfiguration(getContext(), "test");
        Realm realm = Realm.getInstance(realmConfig);
        realm.beginTransaction();
        realm.clear(AllTypes.class);
        AllTypes o1 = realm.createObject(AllTypes.class);
        o1.setColumnLong(1);
        AllTypes o2 = realm.createObject(AllTypes.class);
        o2.setColumnLong(2);
        realm.commitTransaction();

        // Iterate past 1st item
        RealmResults<AllTypes> result = realm.allObjects(AllTypes.class);
        Iterator<AllTypes> it = result.iterator();
        it.next();

        // Delete first item and insert new. Meaning Item 2 gets Item 1s place, and is skipped
        // in iterator when calling next().
        Callable<Boolean> backgroundWorker = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Realm backgroundRealm = Realm.getInstance(realmConfig);
                backgroundRealm.beginTransaction();
                RealmResults<AllTypes> backgroundResult = backgroundRealm.allObjects(AllTypes.class);
                if (backgroundResult.size() != 2) {
                    backgroundRealm.close();
                    return false;
                }
                backgroundResult.sort("columnLong", RealmResults.SORT_ORDER_ASCENDING);
                backgroundResult.remove(0);
                AllTypes o3 = backgroundRealm.createObject(AllTypes.class);
                o3.setColumnLong(3);
                backgroundRealm.commitTransaction();
                int size = backgroundResult.size();
                backgroundRealm.close();
                if (size != 2) {
                    return false;
                } else {
                    return true;
                }
            }
        };

        // Wait for background thread to finish
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<Boolean> backgroundResult = executorService.submit(backgroundWorker);
        assertTrue(backgroundResult.get());
        realm.refresh(); // This shouldn't be needed, but is currently.

        // Next item would now be o3.
        try {
            AllTypes o3 = it.next();
            assertEquals(3, o3.getColumnLong());
            fail("Failed to detect the list was modified, but retained it's size while iterating");
        } catch (ConcurrentModificationException ignored) {
            return;
        } finally {
            realm.close();
        }
    }

    private long sum(int start, int end) {
        long sum = 0;
        for (int i = start; i <= end; i++) {
            sum += i;
        }
        return sum;
    }
}
