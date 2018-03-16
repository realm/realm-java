/*
 * Copyright 2016 Realm Inc.
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

import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.UiThreadTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;

import io.realm.entities.AllJavaTypes;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class OrderedRealmCollectionIteratorTests extends CollectionTests {

    private static final int TEST_SIZE = 10;

    @Rule
    public final UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private final CollectionClass collectionClass;
    private Realm realm;
    private OrderedRealmCollection<AllJavaTypes> collection;

    @Parameterized.Parameters(name = "{0}")
    public static List<CollectionClass> data() {
        return Arrays.asList(CollectionClass.values());
    }

    public OrderedRealmCollectionIteratorTests(CollectionClass collectionType) {
        this.collectionClass = collectionType;
    }

    @Before
    public void setup() {
        realm = Realm.getInstance(configFactory.createConfiguration());
        collection = createCollection(realm, collectionClass, TEST_SIZE);
    }

    @After
    public void tearDown() {
        realm.close();
    }

    private OrderedRealmCollection<AllJavaTypes> createCollection(Realm realm, CollectionClass collectionClass, int sampleSize) {
        OrderedRealmCollection<AllJavaTypes> orderedCollection;

        switch (collectionClass) {
            case REALMRESULTS_SNAPSHOT_LIST_BASE:
            case MANAGED_REALMLIST:
                boolean isEmpty = (sampleSize == 0);
                int newSampleSize = (isEmpty) ? 2 : sampleSize;
                populateRealm(realm, newSampleSize);
                orderedCollection = realm.where(AllJavaTypes.class)
                        .equalTo(AllJavaTypes.FIELD_LONG, isEmpty ? 1 : 0)
                        .findFirst()
                        .getFieldList();
                break;

            case UNMANAGED_REALMLIST:
                populateRealm(realm, sampleSize);
                RealmResults<AllJavaTypes> objects = realm.where(AllJavaTypes.class)
                        .sort(AllJavaTypes.FIELD_LONG, Sort.ASCENDING)
                        .findAll();
                RealmList<AllJavaTypes> inMemoryList = new RealmList<AllJavaTypes>();
                inMemoryList.addAll(objects);
                return inMemoryList;

            case REALMRESULTS_SNAPSHOT_RESULTS_BASE:
            case REALMRESULTS:
                populateRealm(realm, sampleSize);
                orderedCollection = realm.where(AllJavaTypes.class)
                        .sort(AllJavaTypes.FIELD_LONG, Sort.ASCENDING)
                        .findAll();
                break;

            default:
                throw new AssertionError("Unsupported class: " + collectionClass);
        }
        if (isSnapshot(collectionClass)) {
            orderedCollection = orderedCollection.createSnapshot();
        }
        return orderedCollection;
    }

    private void createNewObject() {
        Number currentMax = realm.where(AllJavaTypes.class).max(AllJavaTypes.FIELD_ID);
        long nextId = 0;
        if (currentMax != null) {
            nextId = currentMax.longValue() + 1;
        }

        realm.beginTransaction();
        realm.createObject(AllJavaTypes.class, nextId);
        realm.commitTransaction();
    }

    /**
     * Helper method for checking if the unit test isn't supported for the current collectionClass.
     *
     * @param unsupportedTypes list of unsupported test types
     * @return {@code true} if the unit test should be aborted, {@code false} if it should continue.
     */
    private boolean skipTest(CollectionClass... unsupportedTypes) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < unsupportedTypes.length; i++) {
            if (unsupportedTypes[i].equals(collectionClass)) {
                return true;
            }
        }
        return false;
    }

    private void assertResultsOrSnapshot() {
        if (collectionClass != CollectionClass.REALMRESULTS && !isSnapshot(collectionClass))  {
            fail("Collection class " + collectionClass + "is not results or snapshot.");
        }
    }

    private void assertRealmList() {
        if (collectionClass != CollectionClass.UNMANAGED_REALMLIST &&
                collectionClass != CollectionClass.MANAGED_REALMLIST)  {
            fail("Collection class " + collectionClass + "is not RealmList.");
        }
    }

    @Test
    public void iterator() {
        Iterator<AllJavaTypes> it = collection.iterator();
        int i = 0;
        while (it.hasNext()) {
            AllJavaTypes item = it.next();
            assertEquals("Failed at index: " + i, i, item.getFieldLong());
            i++;
        }
        assertEquals(TEST_SIZE, collection.size());
        assertEquals(TEST_SIZE, i);
    }

    @Test
    public void iterator_empty() {
        collection = createCollection(realm, collectionClass, 0);
        Iterator<AllJavaTypes> it = collection.iterator();
        assertFalse(it.hasNext());
        assertEquals(0, collection.size());
    }

    @Test
    public void iterator_oneElement() {
        collection = createCollection(realm, collectionClass, 1);
        Iterator<AllJavaTypes> it = collection.iterator();
        //noinspection WhileLoopReplaceableByForEach
        int i = 0;
        while (it.hasNext()) {
            AllJavaTypes item = it.next();
            assertEquals(0, item.getFieldLong());
            i++;
        }
        assertEquals(1, collection.size());
        assertEquals(1, i);
    }

    @Test
    public void iterator_unrelatedTransactionBeforeNextItem() {
        Iterator<AllJavaTypes> it = collection.iterator();
        int i = 0;
        while (it.hasNext()) {
            AllJavaTypes item = it.next();
            assertEquals("Failed at index: " + i, i, item.getFieldLong());
            i++;

            // Committing unrelated transactions while iterating should not effect the current iterator.
            createNewObject();
        }
    }

    @Test
    public void iterator_closedRealm_methodsThrows() {
        if (skipTest(CollectionClass.UNMANAGED_REALMLIST)) {
            return;
        }

        Iterator<AllJavaTypes> it = collection.iterator();
        realm.close();
        try {
            it.hasNext();
            fail();
        } catch (IllegalStateException ignored) {
        }

        try {
            it.next();
            fail();
        } catch (IllegalStateException ignored) {
        }

        try {
            it.remove();
            fail();
        } catch (IllegalStateException e) {
            assertEquals(CollectionClass.MANAGED_REALMLIST, collectionClass);
        } catch (UnsupportedOperationException e) {
            assertResultsOrSnapshot();
        }
    }

    @Test
    public void iterator_remove_beforeNext() {
        Iterator<AllJavaTypes> it = collection.iterator();
        realm.beginTransaction();

        try {
            it.remove();
            fail();
        } catch (UnsupportedOperationException e) {
            assertResultsOrSnapshot();
        } catch (IllegalStateException ignored) {
            assertRealmList();
        }
    }

    @Test
    public void iterator_remove() {
        Iterator<AllJavaTypes> it = collection.iterator();
        AllJavaTypes obj = it.next();
        assertEquals("test data 0", obj.getFieldString());
        realm.beginTransaction();

        try {
            it.remove();
        } catch (UnsupportedOperationException e) {
            // RealmResults doesn't support remove.
            assertResultsOrSnapshot();
            return;
        }

        // Unmanaged objects are always invalid, but cannot be GC'ed while we have a reference.
        // managed objects should not be deleted (= invalid).
        assertNotEquals(CollectionClass.REALMRESULTS, collectionClass);
        assertTrue(obj.isValid());
        assertEquals("test data 1", collection.iterator().next().getFieldString());
        assertEquals(TEST_SIZE - 1, collection.size());
    }

    @Test
    public void iterator_deleteManagedObjectIndirectly() {
        realm.beginTransaction();
        Iterator<AllJavaTypes> it = collection.iterator();
        it.next(); // First item is a cyclic reference to the entire graph, avoid deleting that.
        it.next().deleteFromRealm();
        realm.commitTransaction();

        switch (collectionClass) {
            // Snapshot
            case REALMRESULTS_SNAPSHOT_RESULTS_BASE:
            case REALMRESULTS_SNAPSHOT_LIST_BASE:
                assertFalse(collection.get(1).isValid());
                break;
            // Managed RealmLists are directly associated with their table. Thus any indirect deletion will
            // also remove it from the LinkView.
            case MANAGED_REALMLIST:
            case REALMRESULTS:
                assertEquals(TEST_SIZE - 1, collection.size());
                break;

            // Unmanaged collections are not affected by changes to Realm.
            case UNMANAGED_REALMLIST:
                assertEquals(TEST_SIZE, collection.size());
                break;

            default:
                fail();
        }
    }

    @Test
    public void iterator_removeCalledTwice() {
        if (skipTest(CollectionClass.REALMRESULTS, CollectionClass.REALMRESULTS_SNAPSHOT_LIST_BASE,
                CollectionClass.REALMRESULTS_SNAPSHOT_RESULTS_BASE)) {
            return; // remove() not supported by RealmResults.
        }

        Iterator<AllJavaTypes> it = collection.iterator();
        it.next();
        realm.beginTransaction();
        it.remove();

        thrown.expect(IllegalStateException.class);
        it.remove();
    }

    @Test
    public void listIterator_empty() {
        collection = createCollection(realm, collectionClass, 0);
        ListIterator<AllJavaTypes> it = collection.listIterator();

        assertFalse(it.hasPrevious());
        assertFalse(it.hasNext());
        assertEquals(0, it.nextIndex());
        assertEquals(-1, it.previousIndex());

        try {
            it.next();
            fail();
        } catch (NoSuchElementException ignored) {
        }

        try {
            it.previous();
            fail();
        } catch (NoSuchElementException ignored) {
        }
    }

    @Test
    public void listIterator_oneElement() {
        collection = createCollection(realm, collectionClass, 1);
        ListIterator<AllJavaTypes> it = collection.listIterator();

        // Tests beginning of the list.
        assertFalse(it.hasPrevious());
        assertTrue(it.hasNext());
        assertEquals(-1, it.previousIndex());
        assertEquals(0, it.nextIndex());

        // Tests end of the list.
        AllJavaTypes firstObject = it.next();
        assertEquals(0, firstObject.getFieldLong());
        assertTrue(it.hasPrevious());
        assertFalse(it.hasNext());
        assertEquals(0, it.previousIndex());
        assertEquals(1, it.nextIndex());
    }

    @Test
    public void listIterator_manyElements() {
        ListIterator<AllJavaTypes> it = collection.listIterator();

        // Tests beginning of the list.
        assertFalse(it.hasPrevious());
        assertTrue(it.hasNext());
        assertEquals(-1, it.previousIndex());
        assertEquals(0, it.nextIndex());

        // Tests 1st element in the list.
        AllJavaTypes firstObject = it.next();
        assertEquals(0, firstObject.getFieldLong());
        assertTrue(it.hasPrevious());
        assertEquals(0, it.previousIndex());

        // Moves to second last element.
        for (int i = 1; i < TEST_SIZE - 1; i++) {
            it.next();
        }
        assertTrue(it.hasPrevious());
        assertTrue(it.hasNext());
        assertEquals(TEST_SIZE - 1, it.nextIndex());

        // Tests end of the list.
        AllJavaTypes lastObject = it.next();
        assertEquals(TEST_SIZE - 1, lastObject.getFieldLong());
        assertTrue(it.hasPrevious());
        assertFalse(it.hasNext());
        assertEquals(TEST_SIZE, it.nextIndex());
    }

    @Test
    public void listIterator_defaultStartIndex() {
        ListIterator<AllJavaTypes> it1 = collection.listIterator(0);
        ListIterator<AllJavaTypes> it2 = collection.listIterator();

        assertEquals(it1.previousIndex(), it2.previousIndex());
        assertEquals(it1.nextIndex(), it2.nextIndex());
    }

    @Test
    public void listIterator_startIndex() {
        int i = TEST_SIZE / 2;
        ListIterator<AllJavaTypes> it = collection.listIterator(i);

        assertTrue(it.hasPrevious());
        assertTrue(it.hasNext());
        assertEquals(i - 1, it.previousIndex());
        assertEquals(i, it.nextIndex());
        AllJavaTypes nextObject = it.next();
        assertEquals(i, nextObject.getFieldLong());
    }

    @Test
    public void listIterator_remove_beforeNext() {
        Iterator<AllJavaTypes> it = collection.listIterator();
        realm.beginTransaction();

        try {
            it.remove();
            fail();
        } catch (IllegalStateException e) {
            assertRealmList();
        } catch (UnsupportedOperationException e) {
            assertResultsOrSnapshot();
        }
    }

    @Test
    public void listIterator_remove_calledTwice() {
        Iterator<AllJavaTypes> it = collection.listIterator();
        it.next();
        realm.beginTransaction();

        switch (collectionClass) {
            case MANAGED_REALMLIST:
            case UNMANAGED_REALMLIST:
                it.remove();
                thrown.expect(IllegalStateException.class);
                it.remove();
                break;
            case REALMRESULTS_SNAPSHOT_LIST_BASE:
            case REALMRESULTS_SNAPSHOT_RESULTS_BASE:
            case REALMRESULTS:
                try {
                    it.remove(); // Method not supported.
                    fail();
                } catch (UnsupportedOperationException ignored) {
                }
                break;
            default:
                fail("Unknown collection class: " + collectionClass);
        }
    }

    @Test
    public void listIterator_transactionBeforeNextItem() {
        Iterator<AllJavaTypes> it = collection.listIterator();
        int i = 0;
        while (it.hasNext()) {
            AllJavaTypes item = it.next();
            assertEquals("Failed at index: " + i, i, item.getFieldLong());
            i++;

            // Committing transactions while iterating should not effect the current iterator if on a looper thread.
            createNewObject();
        }
    }

    @Test
    public void listIterator_closedRealm_methods() {
        if (skipTest(CollectionClass.UNMANAGED_REALMLIST)) {
            return;
        }

        int location = TEST_SIZE / 2;
        ListIterator<AllJavaTypes> it = collection.listIterator(location);
        realm.close();

        try {
            assertEquals(location - 1, it.previousIndex());
        } catch (IllegalStateException e) {
            fail();
        }

        try {
            assertEquals(location, it.nextIndex());
        } catch (IllegalStateException e) {
            fail();
        }

        try {
            assertTrue(it.hasNext());
        } catch (IllegalStateException ignored) {
        }

        try {
            it.next();
            fail();
        } catch (IllegalStateException ignored) {
        }

        try {
            it.previous();
            fail();
        } catch (IllegalStateException ignored) {
        }

        try {
            it.remove();
            fail();
        } catch (IllegalStateException e) {
            assertRealmList();
        } catch (UnsupportedOperationException ignored) {
            assertResultsOrSnapshot();
        }
    }

    @Test
    public void listIterator_deleteManagedObjectIndirectly() {
        if (skipTest(CollectionClass.REALMRESULTS_SNAPSHOT_LIST_BASE,
                CollectionClass.REALMRESULTS_SNAPSHOT_RESULTS_BASE)) {
            return;
        }

        realm.beginTransaction();
        ListIterator<AllJavaTypes> it = collection.listIterator();
        it.next();
        it.next().deleteFromRealm();
        realm.commitTransaction();

        switch (collectionClass) {
            case MANAGED_REALMLIST:
            case REALMRESULTS:
                assertEquals(TEST_SIZE - 1, collection.size());
                break;
            case UNMANAGED_REALMLIST:
                assertEquals(TEST_SIZE, collection.size());
                break;
            default:
                fail();
                return;
        }
        it.previous();
        AllJavaTypes types = it.next(); // Iterator can still access the deleted object

        //noinspection SimplifiableConditionalExpression
        assertTrue(collectionClass == CollectionClass.MANAGED_REALMLIST ? types.isValid() : !types.isValid());
    }

    @Test
    public void listIterator_remove_realmList_doesNotDeleteObject() {
        if (skipTest(CollectionClass.REALMRESULTS, CollectionClass.REALMRESULTS_SNAPSHOT_LIST_BASE,
                CollectionClass.REALMRESULTS_SNAPSHOT_RESULTS_BASE)) {
            return;
        }
        ListIterator<AllJavaTypes> it = collection.listIterator();
        AllJavaTypes obj = it.next();
        assertEquals("test data 0", obj.getFieldString());
        realm.beginTransaction();
        it.remove();
        assertTrue(obj.isValid());
    }

    @Test
    public void listIterator_remove_nonRealmList_throwUnsupported() {
        if (skipTest(CollectionClass.MANAGED_REALMLIST, CollectionClass.UNMANAGED_REALMLIST)) {
           return;
        }
        ListIterator<AllJavaTypes> it = collection.listIterator();
        AllJavaTypes obj = it.next();
        assertEquals("test data 0", obj.getFieldString());
        realm.beginTransaction();
        thrown.expect(UnsupportedOperationException.class);
        it.remove();
    }

    @Test
    public void listIterator_set() {
        if (skipTest(CollectionClass.REALMRESULTS, CollectionClass.REALMRESULTS_SNAPSHOT_RESULTS_BASE,
                CollectionClass.REALMRESULTS_SNAPSHOT_LIST_BASE)) {
            return;
        }

        realm.beginTransaction();
        ListIterator<AllJavaTypes> it = collection.listIterator();

        // Calling set() before next() should throw.
        try {
            it.set(new AllJavaTypes());
            fail();
        } catch (IllegalStateException ignored) {
        }

        AllJavaTypes obj = it.next();
        assertEquals(0, obj.getFieldLong());
        it.set(new AllJavaTypes(42));

        it.next();
        it.previous(); // A big ListIterator WTF!, but it is by design.
        obj = it.previous();
        assertEquals(42, obj.getFieldLong());
    }

    @Test
    public void listIterator_add() {
        if (skipTest(CollectionClass.REALMRESULTS, CollectionClass.REALMRESULTS_SNAPSHOT_RESULTS_BASE,
                CollectionClass.REALMRESULTS_SNAPSHOT_LIST_BASE)) {
            return;
        }

        realm.beginTransaction();
        ListIterator<AllJavaTypes> it = collection.listIterator();

        // The element is inserted immediately before the element that would be returned by next(), if any, and after
        // the element that would be returned by previous(), if any. (If the list contains no elements, the new element
        // becomes the sole element on the list.)
        it.add(new AllJavaTypes(4242));
        AllJavaTypes obj = collection.first();
        assertNotNull(obj);
        assertEquals(4242, obj.getFieldLong());

        obj = it.next();
        assertEquals(0, obj.getFieldLong());
        it.add(new AllJavaTypes(42));
        obj = it.previous();
        assertEquals(42, obj.getFieldLong());
    }

    @Test
    public void listIterator_unsupportedMethods() {
        ListIterator<AllJavaTypes> it = collection.listIterator();
        try {
            it.remove();
            fail();
        } catch (UnsupportedOperationException e) {
            assertResultsOrSnapshot();
        } catch (IllegalStateException e) { // since next() was never called.
            assertRealmList();
        }

        try {
            it.add(null);
            if (collectionClass != CollectionClass.UNMANAGED_REALMLIST) {
                fail();
            }
        } catch (UnsupportedOperationException e) {
            assertResultsOrSnapshot();
        } catch (IllegalArgumentException e) {
            if (collectionClass == CollectionClass.UNMANAGED_REALMLIST) {
                fail();
            }
            assertRealmList();
        }

        try {
            it.set(new AllJavaTypes());
            fail();
        } catch (UnsupportedOperationException e) {
            assertResultsOrSnapshot();
        } catch (IllegalStateException e) { // since the collection is empty
            assertRealmList();
        }
    }

    @Test
    public void iterator_outsideChangeToSizeThrowsConcurrentModification() {
        if (skipTest(CollectionClass.REALMRESULTS, CollectionClass.REALMRESULTS_SNAPSHOT_RESULTS_BASE,
                CollectionClass.REALMRESULTS_SNAPSHOT_LIST_BASE)) {
            return;
        }

        // Tests all standard collection methods.
        for (CollectionMethod method : CollectionMethod.values()) {
            collection = createCollection(realm, collectionClass, TEST_SIZE);
            realm.beginTransaction();
            Iterator<AllJavaTypes> it = collection.iterator();
            switch (method) {
                case ADD_OBJECT: collection.add(new AllJavaTypes(TEST_SIZE)); break;
                case ADD_ALL_OBJECTS: collection.addAll(Collections.singletonList(new AllJavaTypes(TEST_SIZE))); break;
                case CLEAR: collection.clear(); break;
                case REMOVE_OBJECT: collection.remove(collection.get(0)); break;
                case REMOVE_ALL: collection.removeAll(Collections.singletonList(collection.get(0))); break;
                case RETAIN_ALL: collection.retainAll(Collections.singletonList(collection.get(0))); break;

                // Does not impact size, so does not trigger ConcurrentModificationException.
                case CONTAINS:
                case CONTAINS_ALL:
                case EQUALS:
                case HASHCODE:
                case IS_EMPTY:
                case ITERATOR:
                case SIZE:
                case TO_ARRAY:
                case TO_ARRAY_INPUT:
                    realm.cancelTransaction();
                    continue;
                default:
                    fail("Unknown method: " + method);
            }
            checkIteratorThrowsConcurrentModification(realm, method.toString(), it);
        }

        for (ListMethod method : ListMethod.values()) {
            collection = createCollection(realm, collectionClass, TEST_SIZE);
            realm.beginTransaction();
            Iterator<AllJavaTypes> it = collection.iterator();
            switch (method) {
                case ADD_INDEX: collection.add(0, new AllJavaTypes(TEST_SIZE)); break;
                case ADD_ALL_INDEX: collection.addAll(0, Collections.singleton(new AllJavaTypes(TEST_SIZE))); break;
                case REMOVE_INDEX: collection.remove(0); break;

                // Does not impact size, so does not trigger ConcurrentModificationException.
                case FIRST:
                case LAST:
                case GET_INDEX:
                case INDEX_OF:
                case LAST_INDEX_OF:
                case LIST_ITERATOR:
                case LIST_ITERATOR_INDEX:
                case SET:
                case SUBLIST:
                    realm.cancelTransaction();
                    continue;

                default:
                    fail("Unknown method: " + method);
            }
            checkIteratorThrowsConcurrentModification(realm, method.toString(), it);
        }
    }

    @Test
    public void iterator_outsideChangeToSizeThrowsConcurrentModification_managedCollection() {
        if (skipTest(CollectionClass.REALMRESULTS, CollectionClass.UNMANAGED_REALMLIST,
                CollectionClass.REALMRESULTS_SNAPSHOT_LIST_BASE, CollectionClass.REALMRESULTS_SNAPSHOT_RESULTS_BASE)) {
            return;
        }

        // Tests all RealmCollection methods.
        for (RealmCollectionMethod method : RealmCollectionMethod.values()) {
            collection = createCollection(realm, collectionClass, TEST_SIZE);
            realm.beginTransaction();
            collection.remove(0); // Removes object creating circular dependency which will crash deleteAll.
            Iterator<AllJavaTypes> it = collection.iterator();
            switch (method) {
                case DELETE_ALL_FROM_REALM:
                    collection.deleteAllFromRealm(); break;

                // Does not impact size, so does not trigger ConcurrentModificationException.
                case WHERE:
                case MIN:
                case MAX:
                case SUM:
                case AVERAGE:
                case MIN_DATE:
                case MAX_DATE:
                case IS_VALID:
                case IS_MANAGED:
                    realm.cancelTransaction();
                    continue;
                default:
                    fail("Unknown method: " + method);
            }
            checkIteratorThrowsConcurrentModification(realm, method.toString(), it);
        }

        // Tests all OrderedRealmCollection methods.
        for (OrderedRealmCollectionMethod method : OrderedRealmCollectionMethod.values()) {
            collection = createCollection(realm, collectionClass, TEST_SIZE);
            realm.beginTransaction();
            Iterator<AllJavaTypes> it = collection.iterator();
            switch (method) {
                case DELETE_INDEX: collection.deleteFromRealm(0); break;
                case DELETE_FIRST: collection.deleteFirstFromRealm(); break;
                case DELETE_LAST: collection.deleteLastFromRealm(); break;

                // Does not impact size, so does not trigger ConcurrentModificationException.
                case SORT:
                case SORT_FIELD:
                case SORT_2FIELDS:
                case SORT_MULTI:
                case CREATE_SNAPSHOT:
                    realm.cancelTransaction();
                    continue;
                default:
                    fail("Unknown method: " + method);

            }
            checkIteratorThrowsConcurrentModification(realm, method.toString(), it);
        }
    }


    private void checkIteratorThrowsConcurrentModification(Realm realm, String method, Iterator<AllJavaTypes> it) {
        try {
            it.next();
            fail("Method should have thrown: " + method);
        } catch (ConcurrentModificationException ignored) {
        } catch (Exception e) {
            throw new RuntimeException("Method failed: " + method, e);
        } finally {
            realm.cancelTransaction();
        }
    }

    // Accessing RealmResults iterator after receving remote change notification will throw.
    // But it is valid operation for snapshot.
    @Test
    public void iterator_realmResultsThrowConcurrentModification_snapshotJustWorks() {
        if (skipTest(CollectionClass.MANAGED_REALMLIST, CollectionClass.UNMANAGED_REALMLIST)) {
            return;
        }

        // Verifies that ConcurrentModification is correctly detected on non-looper threads.
        Iterator<AllJavaTypes> it = collection.iterator();
        final CountDownLatch bgDone = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Realm bgRealm = Realm.getInstance(realm.getConfiguration());
                bgRealm.beginTransaction();
                bgRealm.createObject(AllJavaTypes.class, TEST_SIZE);
                bgRealm.commitTransaction();
                bgRealm.close();
                bgDone.countDown();
            }
        }).start();
        TestHelper.awaitOrFail(bgDone);
        realm.waitForChange();
        try {
            it.next();
            assertEquals(TEST_SIZE, collection.size());
        } catch (ConcurrentModificationException ignored) {
            assertEquals(collectionClass, CollectionClass.REALMRESULTS);
        }
    }

    @Test
    public void useCase_simpleIterator_modifyQueryResult_innerTransaction() {
        if (skipTest(CollectionClass.MANAGED_REALMLIST, CollectionClass.UNMANAGED_REALMLIST,
                CollectionClass.REALMRESULTS)) {
            return;
        }

        assertEquals(TEST_SIZE, collection.size());
        for (int i = 0; i < collection.size(); i++) {
            realm.beginTransaction();
            AllJavaTypes obj = collection.get(i);
            obj.setFieldLong(obj.getFieldLong() + TEST_SIZE);
            realm.commitTransaction();
        }

        // Verifies that all elements were modified.
        assertEquals(0, realm.where(AllJavaTypes.class).lessThan(AllJavaTypes.FIELD_LONG, TEST_SIZE).count());
    }

    @Test
    public void useCase_simpleIterator_modifyQueryResult_outerTransaction() {
        if (skipTest(CollectionClass.MANAGED_REALMLIST, CollectionClass.UNMANAGED_REALMLIST,
                CollectionClass.REALMRESULTS)) {
            return;
        }

        assertEquals(TEST_SIZE, collection.size());
        realm.beginTransaction();
        for (int i = 0; i < collection.size(); i++) {
            AllJavaTypes obj = collection.get(i);
            obj.setFieldLong(obj.getFieldLong() + TEST_SIZE);
        }
        realm.commitTransaction();

        // Verifies that all elements were modified.
        assertEquals(0, realm.where(AllJavaTypes.class).lessThan(AllJavaTypes.FIELD_LONG, TEST_SIZE).count());
    }

    @Test
    public void useCase_forEachIterator_modifyQueryResult_innerTransaction() {
        if (skipTest(CollectionClass.MANAGED_REALMLIST, CollectionClass.UNMANAGED_REALMLIST)) {
            return;
        }

        assertEquals(TEST_SIZE, collection.size());
        for (AllJavaTypes obj : collection) {
            realm.beginTransaction();
            obj.setFieldLong(obj.getFieldLong() + TEST_SIZE);
            realm.commitTransaction();
        }

        // Verifies that all elements were modified.
        assertEquals(0, realm.where(AllJavaTypes.class).lessThan(AllJavaTypes.FIELD_LONG, TEST_SIZE).count());
    }

    @Test
    public void useCase_forEachIterator_modifyQueryResult_outerTransaction() {
        if (skipTest(CollectionClass.MANAGED_REALMLIST, CollectionClass.UNMANAGED_REALMLIST)) {
            return;
        }

        assertEquals(TEST_SIZE, collection.size());
        realm.beginTransaction();
        for (AllJavaTypes obj : collection) {
            obj.setFieldLong(obj.getFieldLong() + TEST_SIZE);
        }
        realm.commitTransaction();

        // Verifies that all elements were modified.
        assertEquals(0, realm.where(AllJavaTypes.class).lessThan(AllJavaTypes.FIELD_LONG, TEST_SIZE).count());
    }

    @Test
    @UiThreadTest
    public void useCase_simpleIterator_modifyQueryResult_innerTransaction_looperThread() {
        if (skipTest(CollectionClass.MANAGED_REALMLIST, CollectionClass.UNMANAGED_REALMLIST,
                CollectionClass.REALMRESULTS)) {
            return;
        }

        assertEquals(TEST_SIZE, collection.size());
        for (int i = 0; i < collection.size(); i++) {
            realm.beginTransaction();
            AllJavaTypes obj = collection.get(i);
            obj.setFieldLong(obj.getFieldLong() + TEST_SIZE);
            realm.commitTransaction();
        }

        // Verifies that all elements were modified.
        assertEquals(0, realm.where(AllJavaTypes.class).lessThan(AllJavaTypes.FIELD_LONG, TEST_SIZE).count());
    }

    @Test
    @UiThreadTest
    public void useCase_simpleIterator_modifyQueryResult_outerTransaction_looperThread() {
        if (skipTest(CollectionClass.MANAGED_REALMLIST, CollectionClass.UNMANAGED_REALMLIST,
                CollectionClass.REALMRESULTS)) {
            return;
        }

        assertEquals(TEST_SIZE, collection.size());
        realm.beginTransaction();
        for (int i = 0; i < collection.size(); i++) {
            AllJavaTypes obj = collection.get(i);
            obj.setFieldLong(obj.getFieldLong() + TEST_SIZE);
        }
        realm.commitTransaction();

        // Verifies that all elements were modified.
        assertEquals(0, realm.where(AllJavaTypes.class).lessThan(AllJavaTypes.FIELD_LONG, TEST_SIZE).count());
    }

    @Test
    @UiThreadTest
    public void useCase_forEachIterator_modifyQueryResult_innerTransaction_looperThread() {
        if (skipTest(CollectionClass.MANAGED_REALMLIST, CollectionClass.UNMANAGED_REALMLIST)) {
            return;
        }

        assertEquals(TEST_SIZE, collection.size());
        for (AllJavaTypes obj : collection) {
            realm.beginTransaction();
            obj.setFieldLong(obj.getFieldLong() + TEST_SIZE);
            realm.commitTransaction();
        }

        // Verifies that all elements were modified.
        assertEquals(0, realm.where(AllJavaTypes.class).lessThan(AllJavaTypes.FIELD_LONG, TEST_SIZE).count());
    }

    @Test
    @UiThreadTest
    public void useCase_forEachIterator_modifyQueryResult_outerTransaction_looperThread() {
        if (skipTest(CollectionClass.MANAGED_REALMLIST, CollectionClass.UNMANAGED_REALMLIST)) {
            return;
        }

        assertEquals(TEST_SIZE, collection.size());
        realm.beginTransaction();
        for (AllJavaTypes obj : collection) {
            obj.setFieldLong(obj.getFieldLong() + TEST_SIZE);
        }
        realm.commitTransaction();

        // Verifies that all elements were modified.
        assertEquals(0, realm.where(AllJavaTypes.class).lessThan(AllJavaTypes.FIELD_LONG, TEST_SIZE).count());
    }
}
