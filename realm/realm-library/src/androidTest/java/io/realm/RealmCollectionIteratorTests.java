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

import android.test.suitebuilder.annotation.SmallTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.ListIterator;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.NonLatinFieldNames;
import io.realm.exceptions.RealmException;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

// TODO Refactor this when we have a shared collection interface between RealmList/RealmResults
@RunWith(Parameterized.class)
public class RealmCollectionIteratorTests {

    private static final int TEST_SIZE = 10;

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    private final Class<? extends AbstractList> listType;

    private Realm realm;
    private AbstractList<AllJavaTypes> results;

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Class<? extends AbstractList>> data() {
        return Arrays.asList(RealmResults.class, RealmList.class);
    }

    public RealmCollectionIteratorTests(Class<? extends AbstractList> listType) {
        this.listType = listType;
    }

    @Before
    public void setup() {
        RealmConfiguration configuration = configFactory.createConfiguration();
        Realm.deleteRealm(configuration);
        realm = Realm.getInstance(configuration);

        populateRealm(realm, TEST_SIZE);

        if (listType == RealmResults.class) {
            results = realm.allObjectsSorted(AllJavaTypes.class, AllJavaTypes.FIELD_LONG, Sort.ASCENDING);
        } else {
            results = realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_LONG, 0).findFirst().getFieldList();
        }
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    private void populateRealm(Realm realm, int objects) {
        realm.beginTransaction();
        realm.allObjects(AllJavaTypes.class).clear();
        realm.allObjects(NonLatinFieldNames.class).clear();
        for (int i = 0; i < objects; i++) {
            AllJavaTypes AllJavaTypes = realm.createObject(AllJavaTypes.class, i);
            AllJavaTypes.setFieldBoolean(((i % 3) == 0));
            AllJavaTypes.setFieldBinary(new byte[]{1, 2, 3});
            AllJavaTypes.setFieldDate(new Date());
            AllJavaTypes.setFieldDouble(3.1415);
            AllJavaTypes.setFieldFloat(1.234567f + i);
            AllJavaTypes.setFieldString("test data " + i);
        }

        // Add all items to the RealmList for the first object
        AllJavaTypes firstObj = realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_LONG, 0).findFirst();
        firstObj.getFieldList().addAll(realm.allObjects(AllJavaTypes.class));
        realm.commitTransaction();
    }

    private void createNewObject() {
        realm.beginTransaction();
        realm.createObject(AllJavaTypes.class, realm.where(AllJavaTypes.class).max(AllJavaTypes.FIELD_LONG).longValue() + 1);
        realm.commitTransaction();
    }

    @Test
    public void iterator() {
        Iterator<AllJavaTypes> it = results.iterator();
        int i = 0;
        while (it.hasNext()) {
            AllJavaTypes item = it.next();
            assertEquals("Failed at index: " + i, i, item.getFieldLong());
            i++;
        }
    }

    @Test
    public void iterator_remove_beforeNext() {
        Iterator<AllJavaTypes> it = results.iterator();
        realm.beginTransaction();

        thrown.expect(IllegalStateException.class);
        it.remove();
    }

    @Test
    public void iterator_remove_deletesObject() {
        Iterator<AllJavaTypes> it = results.iterator();
        AllJavaTypes obj = it.next();
        assertEquals(0, obj.getFieldLong());
        realm.beginTransaction();
        it.remove();
        assertFalse(obj.isValid());
        assertEquals(1, results.get(0).getFieldLong());
    }

    @Test
    public void iterator_remove_calledTwice() {
        Iterator<AllJavaTypes> it = results.iterator();
        it.next();
        realm.beginTransaction();
        it.remove();

        thrown.expect(IllegalStateException.class);
        it.remove();
    }

    @Test
    public void iterator_transactionBeforeNextItem() {
        Iterator<AllJavaTypes> it = results.iterator();
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
    public void iterator_refreshWhileIterating() {
        Iterator<AllJavaTypes> it = results.iterator();
        it.next();

        createNewObject();
        realm.refresh(); // This will trigger rerunning all queries

        thrown.expect(ConcurrentModificationException.class);
        it.next();
    }

    @Test
    public void iterator_removedObjectsStillAccessible() {
        realm.beginTransaction();
        results.get(0).removeFromRealm();
        realm.commitTransaction();

        assertEquals(TEST_SIZE, results.size()); // Size is same even if object is deleted
        Iterator<AllJavaTypes> it = results.iterator();
        AllJavaTypes obj = it.next(); // Iterator can still access the deleted object
        assertFalse(obj.isValid());
    }

    public void iterator_refreshClearsRemovedObjects() {
        realm.beginTransaction();
        if (results instanceof RealmList) {
            ((RealmList) results).where().equalTo(AllJavaTypes.FIELD_LONG, 0).findFirst().removeFromRealm();
        } else if (results instanceof RealmResults) {
            ((RealmResults) results).where().equalTo(AllJavaTypes.FIELD_LONG, 0).findFirst().removeFromRealm();
        }
        realm.commitTransaction();

        // TODO How does refresh work with async queries?
        realm.refresh(); // Refresh forces a refresh of all RealmResults

        assertEquals(TEST_SIZE - 1, results.size()); // Size is same even if object is deleted
        Iterator<AllJavaTypes> it = results.iterator();
        AllJavaTypes types = it.next(); // Iterator can no longer access the deleted object

        assertTrue(types.isValid());
        assertEquals(1, types.getFieldLong());
    }

    @Test
    public void iterator_closedRealm_methodsThrows() {
        Iterator<AllJavaTypes> it = results.iterator();
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

    @Test
    public void iterator_forEach() {
        int i = 0;
        for (AllJavaTypes item : results) {
            assertEquals("Failed at index: " + i, i, item.getFieldLong());
            i++;
        }
    }

    @Test
    public void simple_iterator() {
        for (int i = 0; i < results.size(); i++) {
            assertEquals("Failed at index: " + i, i, results.get(i).getFieldLong());
        }
    }

    public void simple_iterator_transactionBeforeNextItem() {
        for (int i = 0; i < results.size(); i++) {
            // Committing transactions while iterating should not effect the current iterator.
            realm.beginTransaction();
            realm.createObject(AllJavaTypes.class).setFieldLong(i);
            realm.commitTransaction();

            assertEquals("Failed at index: " + i, i, results.get(i).getFieldLong());
        }
    }

    @Test
    public void listIterator() {
        ListIterator<AllJavaTypes> it = results.listIterator();

        // Test beginning of the list
        assertFalse(it.hasPrevious());
        assertTrue(it.hasNext());
        assertEquals(0, it.nextIndex());
        AllJavaTypes firstObject = it.next();
        assertEquals(0, firstObject.getFieldLong());
        assertFalse(it.hasPrevious());

        // Move to second last element
        for (int i = 1; i < TEST_SIZE - 1; i++) {
            it.next();

        }

        // Test end of the list
        assertTrue(it.hasPrevious());
        assertTrue(it.hasNext());
        assertEquals(TEST_SIZE - 1, it.nextIndex());
        AllJavaTypes lastObject = it.next();
        assertEquals(TEST_SIZE - 1, lastObject.getFieldLong());
        assertFalse(it.hasNext());
        assertEquals(TEST_SIZE, it.nextIndex());
    }

    @Test
    public void listIterator_defaultStartIndex() {
        ListIterator<AllJavaTypes> it1 = results.listIterator(0);
        ListIterator<AllJavaTypes> it2 = results.listIterator();

        assertEquals(it1.previousIndex(), it2.previousIndex());
        assertEquals(it1.nextIndex(), it2.nextIndex());
    }

    @Test
    public void listIterator_startIndex() {
        int i = TEST_SIZE/2;
        ListIterator<AllJavaTypes> it = results.listIterator(i);

        assertTrue(it.hasPrevious());
        assertTrue(it.hasNext());
        assertEquals(i - 1, it.previousIndex());
        assertEquals(i, it.nextIndex());
        AllJavaTypes nextObject = it.next();
        assertEquals(i, nextObject.getFieldLong());
    }

    @Test
    public void listIterator_closedRealm_methods() {
        int location = TEST_SIZE / 2;
        ListIterator<AllJavaTypes> it = results.listIterator(location);
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
        }
    }

    @Test(expected = RealmException.class)
    public void listIterator_set_thows() {
        results.listIterator().set(null);
    }

    @Test(expected = RealmException.class)
    public void listIterator_add_thows() {
        results.listIterator().set(null);
    }

    @Test
    public void listIterator_remove_beforeNext() {
        Iterator<AllJavaTypes> it = results.listIterator();
        realm.beginTransaction();

        thrown.expect(IllegalStateException.class);
        it.remove();
    }

    @Test
    public void listIterator_remove_deletesObject() {
        Iterator<AllJavaTypes> it = results.listIterator();
        AllJavaTypes obj = it.next();
        assertEquals(0, obj.getFieldLong());
        realm.beginTransaction();
        it.remove();
        assertFalse(obj.isValid());
    }

    @Test
    public void listIterator_remove_calledTwice() {
        Iterator<AllJavaTypes> it = results.listIterator();
        it.next();
        realm.beginTransaction();
        it.remove();

        thrown.expect(IllegalStateException.class);
        it.remove();
    }

    @Test
    public void listIterator_transactionBeforeNextItem() {
        Iterator<AllJavaTypes> it = results.listIterator();
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
    public void listIterator_refreshWhileIterating() {
        Iterator<AllJavaTypes> it = results.listIterator();
        it.next();

        createNewObject();
        realm.refresh(); // This will trigger rerunning all queries

        thrown.expect(ConcurrentModificationException.class);
        it.next();
    }

    @Test
    public void listIterator_removedObjectsStillAccessible() {
        realm.beginTransaction();
        if (results instanceof RealmList) {
            ((RealmList) results).where().equalTo(AllJavaTypes.FIELD_LONG, 0).findFirst().removeFromRealm();
        } else if (results instanceof RealmResults) {
            ((RealmResults) results).where().equalTo(AllJavaTypes.FIELD_LONG, 0).findFirst().removeFromRealm();
        }
        realm.commitTransaction();

        assertEquals(TEST_SIZE, results.size()); // Size is same even if object is deleted
        Iterator<AllJavaTypes> it = results.listIterator();
        AllJavaTypes types = it.next(); // Iterator can still access the deleted object

        assertFalse(types.isValid());
    }

    public void listIterator_refreshClearsRemovedObjects() {
        realm.beginTransaction();
        if (results instanceof RealmList) {
            ((RealmList) results).where().equalTo(AllJavaTypes.FIELD_LONG, 0).findFirst().removeFromRealm();
        } else if (results instanceof RealmResults) {
            ((RealmResults) results).where().equalTo(AllJavaTypes.FIELD_LONG, 0).findFirst().removeFromRealm();
        }
        realm.commitTransaction();

        // TODO How does refresh work with async queries?
        realm.refresh(); // Refresh forces a refresh of all RealmResults

        assertEquals(TEST_SIZE - 1, results.size()); // Size is same even if object is deleted
        Iterator<AllJavaTypes> it = results.listIterator();
        AllJavaTypes types = it.next(); // Iterator can no longer access the deleted object

        assertTrue(types.isValid());
        assertEquals(1, types.getFieldLong());
    }
}
