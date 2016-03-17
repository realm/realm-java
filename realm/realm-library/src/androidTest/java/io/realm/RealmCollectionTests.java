package io.realm;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.NullTypes;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for all methods part of the the {@link RealmCollection} interface.
 * This class only tests methods that have the same behavior no matter if the collection is managed or not.
 *
 * Methods tested in this class:
 *
 * # RealmCollection
 *
 * - RealmQuery<E> where();
 * - Number min(String fieldName);
 * - Number max(String fieldName);
 * - Number sum(String fieldName);
 * - double average(String fieldName);
 * - Date maxDate(String fieldName);
 * - Date minDate(String fieldName);
 * - void deleteAllFromRealm();
 * - boolean isLoaded();
 * - boolean load();
 * - boolean isValid();
 * - BaseRealm getRealm();
 *
 * # Collection
 *
 * - public boolean add(E object);
 * - public boolean addAll(Collection<? extends E> collection);
 * - public void deleteAll();
 * + public boolean contains(Object object);
 * + public boolean containsAll(Collection<?> collection);
 * + public boolean equals(Object object);
 * + public int hashCode();
 * + public boolean isEmpty();
 * + public Iterator<E> iterator();
 * + public boolean remove(Object object);
 * + public boolean removeAll(Collection<?> collection);
 * + public boolean retainAll(Collection<?> collection);
 * + public int size();
 * + public Object[] toArray();
 * + public <T> T[] toArray(T[] array);
 **
 * @see ManagedRealmCollectionTests
 * @see UnManagedRealmCollectionTests
 */
@RunWith(Parameterized.class)
public class RealmCollectionTests extends CollectionTests {

    private static final int TEST_SIZE = 10;

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private final CollectionClass collectionClass;
    private Realm realm;
    private RealmCollection<AllJavaTypes> collection;

    @Parameterized.Parameters(name = "{0}")
    public static List<CollectionClass> data() {
        return Arrays.asList(CollectionClass.values());
    }

    public RealmCollectionTests(CollectionClass collectionType) {
        this.collectionClass = collectionType;
    }

    @Before
    public void setup() {
        realm = Realm.getInstance(configFactory.createConfiguration());
        collection = createCollection(collectionClass);
    }

    @After
    public void tearDown() {
        realm.close();
    }

    private RealmCollection<AllJavaTypes> createCollection(CollectionClass collectionClass) {
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

    private OrderedRealmCollection<NullTypes> createEmptyCollection(Realm realm, CollectionClass collectionClass) {
        switch (collectionClass) {
            case MANAGED_REALMLIST:
                realm.beginTransaction();
                NullTypes obj = realm.createObject(NullTypes.class);
                realm.commitTransaction();
                return obj.getFieldListNull();

            case UNMANAGED_REALMLIST:
                return new RealmList<NullTypes>();

            case REALMRESULTS:
                return realm.where(NullTypes.class).findAll();
        }

        throw new AssertionError("Unknown collection: " + collectionClass);
    }

    private void createNewObject() {
        Number max = realm.where(AllJavaTypes.class).max(AllJavaTypes.FIELD_LONG);
        realm.beginTransaction();
        realm.createObject(AllJavaTypes.class, (max != null) ? max.longValue() + 1 : 0);
        realm.commitTransaction();
    }

    @Test
    public void contains() {
        AllJavaTypes obj = collection.iterator().next();
        assertTrue(collection.contains(obj));
    }

    @Test
    public void contains_realmObjectFromOtherRealm() {
        Realm realm2 = Realm.getInstance(configFactory.createConfiguration("other_realm.realm"));
        populateRealm(realm2, TEST_SIZE);
        AllJavaTypes otherRealmObj = realm2.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_LONG, 0).findFirst();

        try {
            assertFalse(collection.contains(otherRealmObj));
        } finally {
            realm2.close();
        }
    }

    @Test
    public void contains_null() {
        assertFalse(collection.contains(null));
    }

    @Test
    public void containsAll() {
        Iterator<AllJavaTypes> it = collection.iterator();
        List<AllJavaTypes> list = Arrays.asList(it.next(), it.next());
        assertTrue(collection.containsAll(list));
    }

    @Test
    public void containsAll_emptyInput() {
        assertTrue(collection.containsAll(Collections.emptyList()));
    }

    @Test(expected = NullPointerException.class)
    public void containsAll_nullInput() {
        collection.containsAll(null);
    }

    @Test
    public void equals() {
        ArrayList<AllJavaTypes> newList = new ArrayList<AllJavaTypes>();
        newList.addAll(collection);

        assertTrue(collection.equals(collection));
        assertTrue(collection.equals(newList));
        assertFalse(collection.equals(Collections.emptyList()));
        assertFalse(collection.equals(null));
    }

    @Test
    public void hashCode_allObjects() {
        ArrayList<AllJavaTypes> newList = new ArrayList<AllJavaTypes>();
        newList.addAll(collection);

        assertTrue(collection.hashCode() == newList.hashCode());
        assertFalse(collection.hashCode() == Collections.emptyList().hashCode());
    }

    @Test
    public void isEmpty() {
        assertFalse(collection.isEmpty());
        RealmCollection<NullTypes> collection = createEmptyCollection(realm, collectionClass);
        assertTrue(collection.isEmpty());
    }

    @Test
    public void size() {
        assertEquals(TEST_SIZE, collection.size());
    }

    @Test
    public void toArray() {
        Object[] array = collection.toArray();
        assertEquals(TEST_SIZE, array.length);
        assertEquals(collection.iterator().next(), array[0]);
    }

    @Test
    public void toArray_inputArray() {
        AllJavaTypes[] array = new AllJavaTypes[collection.size()];
        collection.toArray(array);
        assertEquals(TEST_SIZE, array.length);
        assertEquals(collection.iterator().next(), array[0]);
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
}
