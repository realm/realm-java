package io.realm;

import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.UiThreadTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.Dog;
import io.realm.exceptions.RealmException;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
        collection = createCollection(realm, collectionClass);
    }

    private OrderedRealmCollection<AllJavaTypes> createCollection(Realm realm, CollectionClass collectionClass) {
        switch (collectionClass) {
            case MANAGED_REALMLIST:
                populateRealm(realm, TEST_SIZE);
                return realm.where(AllJavaTypes.class)
                        .equalTo(AllJavaTypes.FIELD_LONG, 0)
                        .findFirst()
                        .getFieldList();

            case UNMANAGED_REALMLIST:
                return populateInMemoryList(TEST_SIZE);

            case REALMRESULTS:
                populateRealm(realm, TEST_SIZE);
                return realm.allObjects(AllJavaTypes.class);

            default:
                throw new AssertionError("Unsupported class: " + collectionClass);
        }
    }

    private OrderedRealmCollection<AllJavaTypes> createEmptyCollection(Realm realm, CollectionClass collectionClass) {
        switch (collectionClass) {
            case MANAGED_REALMLIST:
                return realm.where(AllJavaTypes.class)
                        .equalTo(AllJavaTypes.FIELD_LONG, 1)
                        .findFirst()
                        .getFieldList();

            case UNMANAGED_REALMLIST:
                return new RealmList<AllJavaTypes>();

            case REALMRESULTS:
                return realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_LONG, -1).findAll();

            default:
                throw new AssertionError("Unsupported class: " + collectionClass);
        }
    }

    private void createNewObject() {
        Number currentMax = realm.where(AllJavaTypes.class).max(AllJavaTypes.FIELD_LONG);
        long nextId = 0;
        if (currentMax != null) {
            nextId = currentMax.longValue() + 1;
        }

        realm.beginTransaction();
        realm.createObject(AllJavaTypes.class, nextId);
        realm.commitTransaction();
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
    }

    @Test
    public void iterator_transactionBeforeNextItem() {
        Iterator<AllJavaTypes> it = collection.iterator();
        int i = 0;
        while (it.hasNext()) {
            AllJavaTypes item = it.next();
            assertEquals("Failed at index: " + i, i, item.getFieldLong());
            i++;

            // Committing transactions while iterating should not effect the current iterator.
            createNewObject();
        }
    }

    @Test
    public void iterator_forEach() {
        int i = 0;
        for (AllJavaTypes item : collection) {
            assertEquals("Failed at index: " + i, i, item.getFieldLong());
            i++;
        }
    }




    @Test
    public void iterator_closedRealm_methodsThrows() {
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
        } catch (IllegalStateException ignored) {
        }
    }

    // TODO Remove once waitForChange is introduced
    @Test
    public void iterator_refreshClearsRemovedObjects() {
        assertEquals(0, collection.iterator().next().getFieldLong());
        realm.setAutoRefresh(false);
        realm.beginTransaction();
        Iterator<AllJavaTypes> it = collection.iterator();
        it.next(); // First item is a cyclic reference, avoid deleting that
        AllJavaTypes obj = it.next();
        assertEquals(1, obj.getFieldLong());
        obj.deleteFromRealm();
        realm.commitTransaction();
        realm.refresh(); // Refresh forces a refresh of all RealmResults

        assertEquals(TEST_SIZE - 1, collection.size());

        it = collection.iterator();
        it.next();
        obj = it.next(); // Iterator can no longer access the deleted object
        assertTrue(obj.isValid());
        assertEquals(2, obj.getFieldLong());
    }

    @Test
    public void iterator_remove_beforeNext() {
        Iterator<AllJavaTypes> it = collection.iterator();
        realm.beginTransaction();

        //noinspection TryWithIdenticalCatches
        try {
            it.remove();
            fail();
        } catch (UnsupportedOperationException ignored) {
            // Thrown by implementations not supporting `remove`
        } catch (IllegalStateException ignored) {
            // Thrown by implementations supporting `remove`
        }
    }

    @Test
    public void simple_iterator() {
        for (int i = 0; i < collection.size(); i++) {
            assertEquals("Failed at index: " + i, i, collection.get(i).getFieldLong());
        }
    }

    public void simple_iterator_transactionBeforeNextItem() {
        for (int i = 0; i < collection.size(); i++) {
            // Committing transactions while iterating should not effect the current iterator.
            realm.beginTransaction();
            realm.createObject(AllJavaTypes.class).setFieldLong(i);
            realm.commitTransaction();

            assertEquals("Failed at index: " + i, i, collection.get(i).getFieldLong());
        }
    }

    @Test
    public void listIterator_empty() {
        collection = createEmptyCollection(realm, collectionClass);
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
        ListIterator<AllJavaTypes> it = collection.subList(0, 1).listIterator();

        // Test beginning of the list
        assertFalse(it.hasPrevious());
        assertTrue(it.hasNext());
        assertEquals(-1, it.previousIndex());
        assertEquals(0, it.nextIndex());

        // Test end of the list
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

        // Test beginning of the list
        assertFalse(it.hasPrevious());
        assertTrue(it.hasNext());
        assertEquals(-1, it.previousIndex());
        assertEquals(0, it.nextIndex());

        // Test 1st element in the list
        AllJavaTypes firstObject = it.next();
        assertEquals(0, firstObject.getFieldLong());
        assertTrue(it.hasPrevious());
        assertEquals(0, it.previousIndex());

        // Move to second last element
        for (int i = 1; i < TEST_SIZE - 1; i++) {
            it.next();

        }
        assertTrue(it.hasPrevious());
        assertTrue(it.hasNext());
        assertEquals(TEST_SIZE - 1, it.nextIndex());

        // Test end of the list
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
        int i = TEST_SIZE/2;
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

        thrown.expect(IllegalStateException.class);
        it.remove();
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
            case REALMRESULTS:
                try {
                    it.remove(); // Method not supported
                    fail();
                } catch (UnsupportedOperationException ignored) {
                }
                break;
            default:
                fail("Unknown collection class: " + collectionClass);
        }
    }

    @Test
    @UiThreadTest
    public void listIterator_transactionBeforeNextItem() {
        Iterator<AllJavaTypes> it = collection.listIterator();
        int i = 0;
        while (it.hasNext()) {
            AllJavaTypes item = it.next();
            assertEquals("Failed at index: " + i, i, item.getFieldLong());
            i++;

            // Committing transactions while iterating should not effect the current iterator if on a looper thread
            createNewObject();
        }
    }

    public void listIterator_refreshClearsRemovedObjects() {
        realm.beginTransaction();
        collection.iterator().next().deleteFromRealm();
        realm.commitTransaction();

        realm.refresh(); // Refresh forces a refresh of all RealmResults

        assertEquals(TEST_SIZE - 1, collection.size()); // Size is same even if object is deleted
        Iterator<AllJavaTypes> it = collection.listIterator();
        AllJavaTypes types = it.next(); // Iterator can no longer access the deleted object

        assertTrue(types.isValid());
        assertEquals(1, types.getFieldLong());
    }

    @Test
    public void listIterator_closedRealm_methods() {
        int location = TEST_SIZE / 2;
        ListIterator<AllJavaTypes> it = collection.listIterator(location);
        realm.close();

        try {
            it.previousIndex();
            fail();
        } catch (IllegalStateException ignored) {
        }

        try {
            it.nextIndex();
            fail();
        } catch (IllegalStateException ignored) {
        }

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
            it.previous();
            fail();
        } catch (IllegalStateException ignored) {
        }

        try {
            it.remove();
            fail();
        } catch (IllegalStateException ignored) {
        } catch (UnsupportedOperationException ignored) {
        }
    }

    // FIXME: Unfortunately this corner case is un-fixable for non-looper threads until we can remove `realm.refresh()`
    // by introducing waitForChange instead.
    @Test
    public void listIterator_refreshWhileIterating() {
        Iterator<AllJavaTypes> it = collection.listIterator();
        it.next();

        createNewObject();
        realm.refresh(); // This will trigger rerunning all queries
        thrown.expect(ConcurrentModificationException.class);
        it.next(); // This can return anything :(
    }

    // Check that the iterator `remove` only removes the object from the collection
    @Test
    public void iterator_managed_remove() {
        Iterator<AllJavaTypes> it = collection.iterator();
        AllJavaTypes obj = it.next();
        assertEquals("Dog 0", obj.getFieldString());
        realm.beginTransaction();
        it.remove();
        assertTrue(obj.isValid());
        assertEquals("Dog 1", collection.iterator().next().getFieldString());
        assertEquals(TEST_SIZE - 1, collection.size());
    }

    @Test
    public void iterator_managed_deletedObjectNotAccessible() {
        realm.beginTransaction();
        Iterator<AllJavaTypes> it = collection.iterator();
        it.next(); // First item is a cyclic reference, avoid deleting that.
        it.next().deleteFromRealm();
        realm.commitTransaction();

        // RealmLists are automatically updated.
        assertEquals(TEST_SIZE - 1, collection.size());
        it = collection.iterator();
        it.next();
        AllJavaTypes obj = it.next();
        assertEquals("Dog 2", obj.getFieldString());
    }



    @Test
    public void iterator_unManaged_remove() {
        Iterator<AllJavaTypes> it = collection.iterator();
        AllJavaTypes obj = it.next();
        assertEquals("Dog 0", obj.getFieldString());
        it.remove();
        assertTrue(obj.isValid());
        assertEquals("Dog 1", collection.iterator().next().getFieldString());
        assertEquals(TEST_SIZE - 1, collection.size());
    }

    @Test
    public void iterator_managed_remove_calledTwice() {
        Iterator<AllJavaTypes> it = collection.iterator();
        it.next();
        realm.beginTransaction();
        it.remove();

        thrown.expect(IllegalStateException.class);
        it.remove();
    }

    // TODO Remove once waitForChange is introduced
    public void iterator_managed_refreshWhileIterating() {
        Iterator<AllJavaTypes> it = collection.iterator();
        it.next();

        realm.beginTransaction();
        realm.createObject(Dog.class).setName("Dog " + TEST_SIZE);
        realm.commitTransaction();
        realm.refresh(); // This will trigger rerunning all queries, but shouldn't effect RealmLists

        assertEquals("Dog 1", it.next().getFieldString());
    }

    @Test
    public void listIterator_managed_emovedObjectsStillAccessible() {
        realm.beginTransaction();
        collection.iterator().next().deleteFromRealm();
        realm.commitTransaction();

        assertEquals(TEST_SIZE, collection.size()); // Size is same even if object is deleted
        ListIterator<AllJavaTypes> it = collection.listIterator();
        AllJavaTypes types = it.next(); // Iterator can still access the deleted object

        assertFalse(types.isValid());
    }

    @Test
    public void listIterator_remove_doesNotDeleteObject() {
        ListIterator<AllJavaTypes> it = collection.listIterator();
        AllJavaTypes obj = it.next();
        assertEquals("Dog 0", obj.getFieldString());
        realm.beginTransaction();
        it.remove();
        assertTrue(obj.isValid());
    }

    @Test
    public void listIterator_managed_set() {
//        collection.listIterator().set(null);
        fail();
    }

    @Test
    public void listIterator_unManaged_set() {
//        collection.listIterator().set(null);
        fail();
    }

    @Test(expected = RealmException.class)
    public void listIterator_managed_add() {
//        collection.listIterator().add(null);
        fail();
    }

    @Test(expected = RealmException.class)
    public void listIterator_unManaged_add() {
//        collection.listIterator().add(null);
        fail();
    }

}
