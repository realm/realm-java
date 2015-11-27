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

import java.util.Date;

import io.realm.entities.AllTypes;
import io.realm.entities.AnnotationIndexTypes;
import io.realm.entities.DogPrimaryKey;
import io.realm.exceptions.RealmException;

public class DynamicRealmTest extends AndroidTestCase {

    private final static int TEST_DATA_SIZE = 10;
    private static final String CLASS_ALL_TYPES = "AllTypes";
    private static final String CLASS_OWNER = "Owner";

    private RealmConfiguration defaultConfig;
    private DynamicRealm realm;

    @Override
    public void setUp() {
        defaultConfig = TestHelper.createConfiguration(getContext());
        Realm.deleteRealm(defaultConfig);

        // Initialize schema. DynamicRealm will not do that, so let a normal Realm create the file first.
        Realm.getInstance(defaultConfig).close();
        realm = DynamicRealm.getInstance(defaultConfig);
    }

    @Override
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    private void populateTestRealm(DynamicRealm realm, int objects) {
        realm.beginTransaction();
        realm.allObjects(CLASS_ALL_TYPES).clear();
        for (int i = 0; i < objects; ++i) {
            DynamicRealmObject allTypes = realm.createObject(CLASS_ALL_TYPES);
            allTypes.setBoolean(AllTypes.FIELD_BOOLEAN, (i % 3) == 0);
            allTypes.setBlob(AllTypes.FIELD_BINARY, new byte[]{1, 2, 3});
            allTypes.setDate(AllTypes.FIELD_DATE, new Date());
            allTypes.setDouble(AllTypes.FIELD_DOUBLE, 3.1415D + i);
            allTypes.setFloat(AllTypes.FIELD_FLOAT, 1.234567F + i);
            allTypes.setString(AllTypes.FIELD_STRING, "test data " + i);
            allTypes.setLong(AllTypes.FIELD_LONG, i);
        }
        realm.commitTransaction();
    }

    private void populateTestRealm() {
        populateTestRealm(realm, TEST_DATA_SIZE);
    }


    // Test that the SharedGroupManager is not reused across Realm/DynamicRealm on the same thread.
    // This is done by starting a write transaction in one Realm and verifying that none of the data
    // written (but not committed) is available in the other Realm.
    public void testSeparateSharedGroups() {
        Realm typedRealm = Realm.getInstance(defaultConfig);
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(defaultConfig);

        assertEquals(0, typedRealm.where(AllTypes.class).count());
        assertEquals(0, dynamicRealm.where(CLASS_ALL_TYPES).count());

        typedRealm.beginTransaction();
        try {
            typedRealm.createObject(AllTypes.class);
            assertEquals(1, typedRealm.where(AllTypes.class).count());
            assertEquals(0, dynamicRealm.where(CLASS_ALL_TYPES).count());
            typedRealm.cancelTransaction();
        } finally {
            typedRealm.close();
            dynamicRealm.close();
        }
    }

    // Test that Realms can only be deleted after all Typed and Dynamic instances are closed
    public void testDeleteThrowsIfDynamicRealmIsOpen() {
        realm.close(); // Close Realm opened in setUp();
        Realm typedRealm = Realm.getInstance(defaultConfig);
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(defaultConfig);

        typedRealm.close();
        try {
            Realm.deleteRealm(defaultConfig);
            fail();
        } catch (IllegalStateException ignored) {
        }

        dynamicRealm.close();
        assertTrue(Realm.deleteRealm(defaultConfig));
    }

    // Test that Realms can only be deleted after all Typed and Dynamic instances are closed.
    public void testDeleteThrowsIfTypedRealmIsOpen() {
        realm.close(); // Close Realm opened in setUp();
        Realm typedRealm = Realm.getInstance(defaultConfig);
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(defaultConfig);

        dynamicRealm.close();
        try {
            Realm.deleteRealm(defaultConfig);
            fail();
        } catch (IllegalStateException ignored) {
        }

        typedRealm.close();
        assertTrue(Realm.deleteRealm(defaultConfig));
    }

    public void testCreateObject() {
        realm.beginTransaction();
        DynamicRealmObject obj = realm.createObject(CLASS_ALL_TYPES);
        realm.commitTransaction();
        assertTrue(obj.isValid());
    }

    public void testCreateObjectWithPrimaryKey() {
        realm.beginTransaction();
        DynamicRealmObject dog = realm.createObject(DogPrimaryKey.CLASS_NAME, 42);
        assertEquals(42, dog.getLong("id"));
        realm.cancelTransaction();
    }

    public void testCreateObjectWithIllegalPrimaryKeyValueThrows() {
        realm.beginTransaction();
        try {
            realm.createObject(DogPrimaryKey.CLASS_NAME, "bar");
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }
    }

    public void testWhere() {
        realm.beginTransaction();
        realm.createObject(CLASS_ALL_TYPES);
        realm.commitTransaction();

        RealmResults<DynamicRealmObject> results = realm.where(CLASS_ALL_TYPES).findAll();
        assertEquals(1, results.size());
    }

    public void testClearInvalidNameThrows() {
        realm.beginTransaction();
        try {
            realm.clear("I don't exist");
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }
    }

    public void testClearOutsideTransactionThrows() {
        try {
            realm.clear(CLASS_ALL_TYPES);
            fail();
        } catch(IllegalStateException ignored) {
        }
    }

    public void testClear() {
        realm.beginTransaction();
        realm.createObject(CLASS_ALL_TYPES);
        realm.commitTransaction();

        assertEquals(1, realm.where(CLASS_ALL_TYPES).count());
        realm.beginTransaction();
        realm.clear(CLASS_ALL_TYPES);
        realm.commitTransaction();
        assertEquals(0, realm.where(CLASS_ALL_TYPES).count());
    }

    public void testExecuteTransactionNull() {
        realm.executeTransaction(null); // Nothing happens
        assertFalse(realm.hasChanged());
    }

    public void testExecuteTransactionCommit() {
        assertEquals(0, realm.allObjects(CLASS_OWNER).size());
        realm.executeTransaction(new DynamicRealm.Transaction() {
            @Override
            public void execute(DynamicRealm realm) {
                DynamicRealmObject owner = realm.createObject(CLASS_OWNER);
                owner.setString("name", "Owner");
            }
        });

        RealmResults<DynamicRealmObject> allObjects = realm.allObjects(CLASS_OWNER);
        assertEquals(1, allObjects.size());
        assertEquals("Owner", allObjects.get(0).getString("name"));
    }

    public void testExecuteTransactionCancel() {
        assertEquals(0, realm.allObjects(CLASS_OWNER).size());
        try {
            realm.executeTransaction(new DynamicRealm.Transaction() {
                @Override
                public void execute(DynamicRealm realm) {
                    DynamicRealmObject owner = realm.createObject(CLASS_OWNER);
                    owner.setString("name", "Owner");
                    throw new RuntimeException("Boom");
                }
            });
        } catch (RealmException ignore) {
        }
        assertEquals(0, realm.allObjects(CLASS_OWNER).size());
    }

    public void testAllObjectsSorted() {
        populateTestRealm();
        RealmResults<DynamicRealmObject> sortedList = realm.allObjectsSorted(CLASS_ALL_TYPES, AllTypes.FIELD_STRING, Sort.ASCENDING);
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals("test data 0", sortedList.first().getString(AllTypes.FIELD_STRING));

        RealmResults<DynamicRealmObject> reverseList = realm.allObjectsSorted(CLASS_ALL_TYPES, AllTypes.FIELD_STRING, Sort.DESCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals("test data 0", reverseList.last().getString(AllTypes.FIELD_STRING));
   }

    public void testAllObjectsSortedWrongFieldNameThrows() {
        try {
            realm.allObjectsSorted(CLASS_ALL_TYPES, "invalid", Sort.ASCENDING);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testSortTwoFields() {
        TestHelper.populateForMultiSort(realm);

        RealmResults<DynamicRealmObject> results1 = realm.allObjectsSorted(CLASS_ALL_TYPES,
                new String[]{AllTypes.FIELD_STRING, AllTypes.FIELD_LONG},
                new Sort[]{Sort.ASCENDING, Sort.ASCENDING});

        assertEquals(3, results1.size());

        assertEquals("Adam", results1.get(0).getString(AllTypes.FIELD_STRING));
        assertEquals(4, results1.get(0).getLong(AllTypes.FIELD_LONG));

        assertEquals("Adam", results1.get(1).getString(AllTypes.FIELD_STRING));
        assertEquals(5, results1.get(1).getLong(AllTypes.FIELD_LONG));

        assertEquals("Brian", results1.get(2).getString(AllTypes.FIELD_STRING));
        assertEquals(4, results1.get(2).getLong(AllTypes.FIELD_LONG));

        RealmResults<DynamicRealmObject> results2 = realm.allObjectsSorted(CLASS_ALL_TYPES,
                new String[]{AllTypes.FIELD_LONG, AllTypes.FIELD_STRING},
                new Sort[]{Sort.ASCENDING, Sort.ASCENDING});

        assertEquals(3, results2.size());

        assertEquals("Adam", results2.get(0).getString(AllTypes.FIELD_STRING));
        assertEquals(4, results2.get(0).getLong(AllTypes.FIELD_LONG));

        assertEquals("Brian", results2.get(1).getString(AllTypes.FIELD_STRING));
        assertEquals(4, results2.get(1).getLong(AllTypes.FIELD_LONG));

        assertEquals("Adam", results2.get(2).getString(AllTypes.FIELD_STRING));
        assertEquals(5, results2.get(2).getLong(AllTypes.FIELD_LONG));
    }

    public void testSortMultiFailures() {
        // zero fields specified
        try {
            realm.allObjectsSorted(CLASS_ALL_TYPES, new String[]{}, new Sort[]{});
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // number of fields and sorting orders don't match
        try {
            realm.allObjectsSorted(CLASS_ALL_TYPES,
                    new String[]{AllTypes.FIELD_STRING},
                    new Sort[]{Sort.ASCENDING, Sort.ASCENDING});
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // null is not allowed
        try {
            realm.allObjectsSorted(CLASS_ALL_TYPES, null, (Sort[])null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            realm.allObjectsSorted(CLASS_ALL_TYPES, new String[]{AllTypes.FIELD_STRING}, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // non-existing field name
        try {
            realm.allObjectsSorted(CLASS_ALL_TYPES,
                    new String[]{AllTypes.FIELD_STRING, "dont-exist"},
                    new Sort[]{Sort.ASCENDING, Sort.ASCENDING});
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testSortSingleField() {
        populateTestRealm();
        RealmResults<DynamicRealmObject> sortedList = realm.allObjectsSorted(CLASS_ALL_TYPES,
                new String[]{AllTypes.FIELD_LONG},
                new Sort[]{Sort.DESCENDING});
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals(TEST_DATA_SIZE - 1, sortedList.first().getLong(AllTypes.FIELD_LONG));
        assertEquals(0, sortedList.last().getLong(AllTypes.FIELD_LONG));
    }

    private void populateForDistinct(DynamicRealm realm, long numberOfBlocks, long numberOfObjects, boolean withNull) {
        realm.beginTransaction();
        for (int i = 0; i < numberOfObjects * numberOfBlocks; i++) {
            for (int j = 0; j < numberOfBlocks; j++) {
                DynamicRealmObject obj = realm.createObject("AnnotationIndexTypes");
                obj.setBoolean("indexBoolean", j % 2 == 0);
                obj.setLong("indexLong", j);
                obj.setDate("indexDate", withNull ? null : new Date(1000 * j));
                obj.setString("indexString", withNull ? null :  "Test " + j);
                obj.setBoolean("notIndexBoolean", j % 2 == 0);
                obj.setLong("notIndexLong", j);
                obj.setDate("notIndexDate", withNull ? null : new Date(1000 * j));
                obj.setString("notIndexString", withNull ? null : "Test " + j);
            }
        }
        realm.commitTransaction();
    }

    public void testDistinctInvalidClassNameThrows() {
        String[] classNames = new String[]{null, "", "foo", "foo.bar"};
        for (String className : classNames) {
            try {
                realm.distinct(className, "foo");
                fail();
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void testDistinctInvalidFieldNamesThrows() {
        String[] fieldNames = new String[]{null, "", "foo", "foo.bar"};
        for (String fieldName : fieldNames) {
            try {
                realm.distinct(AnnotationIndexTypes.CLASS_NAME, fieldName);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    // Realm.distinct(): requires indexing, and type = boolean, integer, date, string
    public void testDistinct() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // must be greater than 1

        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        RealmResults<DynamicRealmObject> distinctBool = realm.distinct(AnnotationIndexTypes.CLASS_NAME , "indexBoolean");
        assertEquals(2, distinctBool.size());

        for (String fieldName : new String[]{"Long", "Date", "String"}) {
            RealmResults<DynamicRealmObject> distinct = realm.distinct(AnnotationIndexTypes.CLASS_NAME , "index" + fieldName);
            assertEquals("index" + fieldName, numberOfBlocks, distinct.size());
        }
    }

    public void testDistinctNotIndexedFields() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // must be greater than 1

        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        for (String fieldName : new String[]{"Boolean", "Long", "Date", "String"}) {
            try {
                realm.distinct(AnnotationIndexTypes.CLASS_NAME, "notIndex" + fieldName);
                fail("notIndex" + fieldName);
            } catch (UnsupportedOperationException ignored) {
            }
        }
    }

    public void testDistinctInvalidTypes() {
        populateTestRealm();

        for (String field : new String[]{"columnRealmObject", "columnRealmList", "columnDouble", "columnFloat"}) {
            try {
                realm.distinct(AllTypes.CLASS_NAME, field);
                fail(field);
            } catch (UnsupportedOperationException ignored) {
            }
        }
    }

}
