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

import android.test.AndroidTestCase;

import io.realm.entities.AllTypes;
import io.realm.internal.TableView;

public class SortTest extends AndroidTestCase {
    private Realm testRealm = null;

    private final static String FIELD_STRING = "columnString";
    private final static String FIELD_LONG = "columnLong";

    @Override
    public void setUp() {
        Realm.deleteRealmFile(getContext());
        testRealm = Realm.getInstance(getContext());

        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        AllTypes object1 = testRealm.createObject(AllTypes.class);
        object1.setColumnLong(5);
        object1.setColumnString("Adam");

        AllTypes object2 = testRealm.createObject(AllTypes.class);
        object2.setColumnLong(4);
        object2.setColumnString("Brian");

        AllTypes object3 = testRealm.createObject(AllTypes.class);
        object3.setColumnLong(4);
        object3.setColumnString("Adam");

        AllTypes object4 = testRealm.createObject(AllTypes.class);
        object4.setColumnLong(5);
        object4.setColumnString("Adam");
        testRealm.commitTransaction();
    }

    @Override
    public void tearDown() throws Exception {
        testRealm.close();
    }

    public void testSortMultiFailures() {
        RealmResults<AllTypes> allTypes = testRealm.allObjects(AllTypes.class);

        // zero fields specified
        try {
            allTypes.sort(new String[]{}, new boolean[]{});
            fail();
        } catch (IllegalArgumentException ignored) {}

        // number of fields and sorting orders don't match
        try {
            allTypes.sort(new String[]{FIELD_STRING}, new boolean[]{RealmResults.SORT_ORDER_ASCENDING, RealmResults.SORT_ORDER_ASCENDING});
            fail();
        } catch (IllegalArgumentException ignored) {}

        // null is not allowed
        try {
            allTypes.sort(null, null);
            fail();
        } catch (IllegalArgumentException ignored) {}
        try {
            allTypes.sort(new String[]{FIELD_STRING}, null);
            fail();
        } catch (IllegalArgumentException ignored) {}

        // non-existing field name
        try {
            allTypes.sort(new String[]{FIELD_STRING, "dont-exist"}, new boolean[]{RealmResults.SORT_ORDER_ASCENDING, RealmResults.SORT_ORDER_ASCENDING});
            fail();
        } catch (IllegalArgumentException ignored) {}
    }

    private void checkSortTwoFieldsStringInt(RealmResults<AllTypes> results) {
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

        assertEquals("Adam", results.get(2).getColumnString());
        assertEquals(5, results.get(2).getColumnLong());
        assertEquals(3, ((TableView)results.getTable()).getSourceRowIndex(3));
    }

    public void testSortRealmResultsTwoFields() {
        RealmResults<AllTypes> results1 = testRealm.allObjects(AllTypes.class);
        results1.sort(new String[]{FIELD_STRING, FIELD_LONG}, new boolean[] {RealmResults.SORT_ORDER_ASCENDING, RealmResults.SORT_ORDER_ASCENDING});
        checkSortTwoFieldsStringInt(results1);

        RealmResults<AllTypes> results2 = testRealm.allObjects(AllTypes.class);
        results2.sort(new String[]{FIELD_LONG, FIELD_STRING}, new boolean[]{RealmResults.SORT_ORDER_ASCENDING, RealmResults.SORT_ORDER_ASCENDING});
        checkSortTwoFieldsIntString(results2);
   }


    public void testRealmQuerySortTwoFields() {
        RealmResults<AllTypes> results1 = testRealm.where(AllTypes.class)
                .findAllSorted(new String[]{FIELD_STRING, FIELD_LONG},
                        new boolean[] {RealmResults.SORT_ORDER_ASCENDING, RealmResults.SORT_ORDER_ASCENDING});
        checkSortTwoFieldsStringInt(results1);

        RealmResults<AllTypes> results2 = testRealm.where(AllTypes.class)
                .findAllSorted(new String[]{FIELD_LONG, FIELD_STRING},
                        new boolean[]{RealmResults.SORT_ORDER_ASCENDING, RealmResults.SORT_ORDER_ASCENDING});
        checkSortTwoFieldsIntString(results2);
    }

    public void testRealmSortTwoFields() {
        RealmResults<AllTypes> results1 = testRealm.allObjectsSorted(AllTypes.class,
                new String[]{FIELD_STRING, FIELD_LONG},
                new boolean[] {RealmResults.SORT_ORDER_ASCENDING, RealmResults.SORT_ORDER_ASCENDING});
        checkSortTwoFieldsStringInt(results1);

        RealmResults<AllTypes> results2 = testRealm.allObjectsSorted(AllTypes.class,
                new String[]{FIELD_LONG, FIELD_STRING},
                new boolean[]{RealmResults.SORT_ORDER_ASCENDING, RealmResults.SORT_ORDER_ASCENDING});
        checkSortTwoFieldsIntString(results2);
    }

    public void testRealmSortMultiFailures() {
        RealmResults<AllTypes> allTypes = testRealm.allObjects(AllTypes.class);

        // zero fields specified
        try {
            testRealm.allObjectsSorted(AllTypes.class, new String[]{}, new boolean[]{});
            fail();
        } catch (IllegalArgumentException ignored) {}

        // number of fields and sorting orders don't match
        try {
            testRealm.allObjectsSorted(AllTypes.class,
                    new String[]{FIELD_STRING},
                    new boolean[]{RealmResults.SORT_ORDER_ASCENDING, RealmResults.SORT_ORDER_ASCENDING});
            fail();
        } catch (IllegalArgumentException ignored) {}

        // null is not allowed
        try {
            testRealm.allObjectsSorted(AllTypes.class, null, null);
            fail();
        } catch (IllegalArgumentException ignored) {}
        try {
            testRealm.allObjectsSorted(AllTypes.class, new String[]{FIELD_STRING}, null);
            fail();
        } catch (IllegalArgumentException ignored) {}

        // non-existing field name
        try {
            testRealm.allObjectsSorted(AllTypes.class,
                    new String[]{FIELD_STRING, "dont-exist"},
                    new boolean[]{RealmResults.SORT_ORDER_ASCENDING, RealmResults.SORT_ORDER_ASCENDING});
            fail();
        } catch (IllegalArgumentException ignored) {}
    }
}
