/*
 * Copyright 2015 Realm Inc.
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

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.entities.AllTypes;
import io.realm.internal.TableView;
import io.realm.rule.TestRealmConfigurationFactory;

@RunWith(AndroidJUnit4.class)
public class SortTest extends AndroidTestCase {
    private Realm realm;

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private Context context;
    private RealmConfiguration realmConfig;

    private final static String FIELD_STRING = "columnString";
    private final static String FIELD_LONG = "columnLong";

    private final static String[] ORDER_STRING_INT = {FIELD_STRING, FIELD_LONG};
    private final static String[] ORDER_INT_STRING = {FIELD_LONG, FIELD_STRING};

    private final static Sort[] ORDER_ASC_ASC = {Sort.ASCENDING, Sort.ASCENDING};
    private final static Sort[] ORDER_ASC_DES = {Sort.ASCENDING, Sort.DESCENDING};

    @Before
    public void setUp() {
        // Creates a Realm with the following objects:
        // 0: (5, "Adam")
        // 1: (4, "Brian")
        // 2: (4, "Adam")
        // 3: (5, "Adam")
        // Injecting the Instrumentation instance is required
        // for your test to run with AndroidJUnitRunner.
        context = InstrumentationRegistry.getInstrumentation().getContext();
        realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);

        realm.beginTransaction();
        realm.delete(AllTypes.class);
        AllTypes object1 = realm.createObject(AllTypes.class);
        object1.setColumnLong(5);
        object1.setColumnString("Adam");

        AllTypes object2 = realm.createObject(AllTypes.class);
        object2.setColumnLong(4);
        object2.setColumnString("Brian");

        AllTypes object3 = realm.createObject(AllTypes.class);
        object3.setColumnLong(4);
        object3.setColumnString("Adam");

        AllTypes object4 = realm.createObject(AllTypes.class);
        object4.setColumnLong(5);
        object4.setColumnString("Adam");
        realm.commitTransaction();
    }

    @After
    public void tearDown() throws Exception {
        realm.close();
    }

    @Test
    public void sortMultiFailures() {
        RealmResults<AllTypes> allTypes = realm.allObjects(AllTypes.class);

        // zero fields specified
        try {
            allTypes.sort(new String[]{}, new Sort[]{});
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // number of fields and sorting orders don't match
        try {
            allTypes.sort(new String[]{FIELD_STRING}, ORDER_ASC_ASC);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // null is not allowed
        try {
            allTypes.sort(null, (Sort[])null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            allTypes.sort(new String[]{FIELD_STRING}, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // non-existing field name
        try {
            allTypes.sort(new String[]{FIELD_STRING, "dont-exist"}, ORDER_ASC_ASC);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void checkSortTwoFieldsStringAscendingIntAscending(RealmResults<AllTypes> results) {
        // Sorted String (ascending), Long (ascending).
        // Expected output:
        // (4, "Adam"), row index = 2
        // (5, "Adam"), row index = 0 - stable sort!
        // (5, "Adam"), row index = 3
        // (4, "Brian"), row index = 1
        assertEquals(4, results.size());

        assertEquals("Adam", results.get(0).getColumnString());
        assertEquals(4, results.get(0).getColumnLong());
        assertEquals(2, ((TableView)results.getTable()).getSourceRowIndex(0));

        assertEquals("Adam", results.get(1).getColumnString());
        assertEquals(5, results.get(1).getColumnLong());
        assertEquals(0, ((TableView)results.getTable()).getSourceRowIndex(1));

        assertEquals("Adam", results.get(2).getColumnString());
        assertEquals(5, results.get(2).getColumnLong());
        assertEquals(3, ((TableView)results.getTable()).getSourceRowIndex(2));

        assertEquals("Brian", results.get(3).getColumnString());
        assertEquals(4, results.get(3).getColumnLong());
        assertEquals(1, ((TableView)results.getTable()).getSourceRowIndex(3));
    }

    private void checkSortTwoFieldsIntString(RealmResults<AllTypes> results) {
        // Sorted Long (ascending), String (descending)
        // Expected output:
        // (4, "Adam"), row index = 2
        // (4, "Brian"), row index = 1
        // (5, "Adam"), row index = 0 - stable sort!
        // (5, "Adam"), row index = 3
        assertEquals(4, results.size());

        assertEquals("Adam", results.get(0).getColumnString());
        assertEquals(4, results.get(0).getColumnLong());
        assertEquals(2, ((TableView)results.getTable()).getSourceRowIndex(0));

        assertEquals("Brian", results.get(1).getColumnString());
        assertEquals(4, results.get(1).getColumnLong());
        assertEquals(1, ((TableView)results.getTable()).getSourceRowIndex(1));

        assertEquals("Adam", results.get(2).getColumnString());
        assertEquals(5, results.get(2).getColumnLong());
        assertEquals(0, ((TableView)results.getTable()).getSourceRowIndex(2));

        assertEquals("Adam", results.get(3).getColumnString());
        assertEquals(5, results.get(3).getColumnLong());
        assertEquals(3, ((TableView)results.getTable()).getSourceRowIndex(3));
    }

    private void checkSortTwoFieldsIntAscendingStringDescending(RealmResults<AllTypes> results) {
        // Sorted Long (ascending), String (descending)
        // Expected output:
        // (4, "Brian"), row index = 1
        // (4, "Adam"), row index = 2
        // (5, "Adam"), row index = 0 - stable sort!
        // (5, "Adam"), row index = 3
        assertEquals(4, results.size());

        assertEquals("Brian", results.get(0).getColumnString());
        assertEquals(4, results.get(0).getColumnLong());
        assertEquals(1, ((TableView)results.getTable()).getSourceRowIndex(0));

        assertEquals("Adam", results.get(1).getColumnString());
        assertEquals(4, results.get(1).getColumnLong());
        assertEquals(2, ((TableView)results.getTable()).getSourceRowIndex(1));

        assertEquals("Adam", results.get(2).getColumnString());
        assertEquals(5, results.get(2).getColumnLong());
        assertEquals(0, ((TableView)results.getTable()).getSourceRowIndex(2));

        assertEquals("Adam", results.get(3).getColumnString());
        assertEquals(5, results.get(3).getColumnLong());
        assertEquals(3, ((TableView)results.getTable()).getSourceRowIndex(3));
    }

    private void checkSortTwoFieldsStringAscendingIntDescending(RealmResults<AllTypes> results) {
        // Sorted String (ascending), Long (descending)
        // Expected output:
        // (5, "Adam"), row index = 0 - stable sort!
        // (5, "Adam"), row index = 3
        // (4, "Adam"), row index = 2
        // (5, "Brian"), row index = 1
        assertEquals(4, results.size());

        assertEquals("Adam", results.get(0).getColumnString());
        assertEquals(5, results.get(0).getColumnLong());
        assertEquals(0, ((TableView)results.getTable()).getSourceRowIndex(0));

        assertEquals("Adam", results.get(1).getColumnString());
        assertEquals(5, results.get(1).getColumnLong());
        assertEquals(3, ((TableView)results.getTable()).getSourceRowIndex(1));

        assertEquals("Adam", results.get(2).getColumnString());
        assertEquals(4, results.get(2).getColumnLong());
        assertEquals(2, ((TableView)results.getTable()).getSourceRowIndex(2));

        assertEquals("Brian", results.get(3).getColumnString());
        assertEquals(4, results.get(3).getColumnLong());
        assertEquals(1, ((TableView)results.getTable()).getSourceRowIndex(3));
    }

    @Test
    public void sortRealmResultsTwoFields() {
        RealmResults<AllTypes> results1 = realm.where(AllTypes.class).findAll().sort(ORDER_STRING_INT, ORDER_ASC_ASC);
        checkSortTwoFieldsStringAscendingIntAscending(results1);

        RealmResults<AllTypes> results2 = realm.where(AllTypes.class).findAll().sort(ORDER_INT_STRING, ORDER_ASC_ASC);
        checkSortTwoFieldsIntString(results2);

        RealmResults<AllTypes> results3 = realm.where(AllTypes.class).findAll().sort(ORDER_STRING_INT, ORDER_ASC_DES);
        checkSortTwoFieldsStringAscendingIntDescending(results3);

        RealmResults<AllTypes> results4 = realm.where(AllTypes.class).findAll().sort(ORDER_INT_STRING, ORDER_ASC_DES);
        checkSortTwoFieldsIntAscendingStringDescending(results4);
   }

    @Test
    public void realmQuerySortTwoFields() {
        RealmResults<AllTypes> results1 = realm.where(AllTypes.class)
                .findAll().sort(ORDER_STRING_INT, ORDER_ASC_ASC);
        checkSortTwoFieldsStringAscendingIntAscending(results1);

        RealmResults<AllTypes> results2 = realm.where(AllTypes.class)
                .findAll().sort(ORDER_INT_STRING, ORDER_ASC_ASC);
        checkSortTwoFieldsIntString(results2);

        RealmResults<AllTypes> results3 = realm.where(AllTypes.class)
                .findAll().sort(ORDER_STRING_INT, ORDER_ASC_DES);
        checkSortTwoFieldsStringAscendingIntDescending(results3);

        RealmResults<AllTypes> results4 = realm.where(AllTypes.class)
                .findAll().sort(ORDER_INT_STRING, ORDER_ASC_DES);
        checkSortTwoFieldsIntAscendingStringDescending(results4);
    }

    @Test
    public void realmSortTwoFields() {
        RealmResults<AllTypes> results1 = realm.where(AllTypes.class).findAll().
                sort(ORDER_STRING_INT, ORDER_ASC_ASC);
        checkSortTwoFieldsStringAscendingIntAscending(results1);

        RealmResults<AllTypes> results2 = realm.where(AllTypes.class).findAll().
                sort(ORDER_INT_STRING, ORDER_ASC_ASC);
        checkSortTwoFieldsIntString(results2);

        RealmResults<AllTypes> results3 = realm.where(AllTypes.class).findAll().
                sort(ORDER_STRING_INT, ORDER_ASC_DES);
        checkSortTwoFieldsStringAscendingIntDescending(results3);

        RealmResults<AllTypes> results4 = realm.where(AllTypes.class).findAll().
                sort(ORDER_INT_STRING, ORDER_ASC_DES);
        checkSortTwoFieldsIntAscendingStringDescending(results4);
    }

    @Test
    public void realmSortMultiFailures() {
        RealmResults<AllTypes> allTypes = realm.where(AllTypes.class).findAll();

        // zero fields specified
        try {
            realm.where(AllTypes.class).findAll().sort(new String[]{}, new Sort[]{});
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // number of fields and sorting orders don't match
        try {
            realm.where(AllTypes.class).findAll().
                    sort(new String[]{FIELD_STRING}, ORDER_ASC_ASC);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // null is not allowed
        try {
            realm.where(AllTypes.class).findAll().sort(null, (Sort[])null);
            fail();
        } catch (IllegalArgumentException ignored) {}
        try {
            realm.where(AllTypes.class).findAll().sort(new String[]{FIELD_STRING}, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // non-existing field name
        try {
            realm.where(AllTypes.class).findAll().
                    sort(new String[]{FIELD_STRING, "dont-exist"}, ORDER_ASC_ASC);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }
}
