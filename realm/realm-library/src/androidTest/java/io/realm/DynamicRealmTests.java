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


import android.os.Handler;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.AllTypes;
import io.realm.entities.AnnotationIndexTypes;
import io.realm.entities.Cat;
import io.realm.entities.Dog;
import io.realm.entities.DogPrimaryKey;
import io.realm.entities.Owner;
import io.realm.entities.PrimaryKeyAsBoxedByte;
import io.realm.entities.PrimaryKeyAsBoxedInteger;
import io.realm.entities.PrimaryKeyAsBoxedLong;
import io.realm.entities.PrimaryKeyAsBoxedShort;
import io.realm.entities.PrimaryKeyAsString;
import io.realm.internal.log.RealmLog;
import io.realm.proxy.HandlerProxy;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class DynamicRealmTests {

    private final static int TEST_DATA_SIZE = 10;

    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private RealmConfiguration defaultConfig;
    private DynamicRealm realm;

    @Before
    public void setUp() {
        defaultConfig = configFactory.createConfiguration();

        // Initialize schema. DynamicRealm will not do that, so let a normal Realm create the file first.
        Realm.getInstance(defaultConfig).close();
        realm = DynamicRealm.getInstance(defaultConfig);
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    private void populateTestRealm(DynamicRealm realm, int objects) {
        boolean autoRefreshEnabled = realm.isAutoRefresh();
        if (autoRefreshEnabled) {
            realm.setAutoRefresh(false);
        }
        realm.beginTransaction();
        realm.allObjects(AllTypes.CLASS_NAME).clear();
        for (int i = 0; i < objects; ++i) {
            DynamicRealmObject allTypes = realm.createObject(AllTypes.CLASS_NAME);
            allTypes.setBoolean(AllTypes.FIELD_BOOLEAN, (i % 3) == 0);
            allTypes.setBlob(AllTypes.FIELD_BINARY, new byte[]{1, 2, 3});
            allTypes.setDate(AllTypes.FIELD_DATE, new Date());
            allTypes.setDouble(AllTypes.FIELD_DOUBLE, 3.1415D + i);
            allTypes.setFloat(AllTypes.FIELD_FLOAT, 1.234567F + i);
            allTypes.setString(AllTypes.FIELD_STRING, "test data " + i);
            allTypes.setLong(AllTypes.FIELD_LONG, i);
            allTypes.getList(AllTypes.FIELD_REALMLIST).add(realm.createObject(Dog.CLASS_NAME));
            allTypes.getList(AllTypes.FIELD_REALMLIST).add(realm.createObject(Dog.CLASS_NAME));
        }
        realm.commitTransaction();
        if (autoRefreshEnabled) {
            realm.setAutoRefresh(true);
        }
    }

    private void populateTestRealm() {
        populateTestRealm(realm, TEST_DATA_SIZE);
    }

    // Test that the SharedGroupManager is not reused across Realm/DynamicRealm on the same thread.
    // This is done by starting a write transaction in one Realm and verifying that none of the data
    // written (but not committed) is available in the other Realm.
    @Test
    public void separateSharedGroups() {
        Realm typedRealm = Realm.getInstance(defaultConfig);
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(defaultConfig);

        assertEquals(0, typedRealm.where(AllTypes.class).count());
        assertEquals(0, dynamicRealm.where(AllTypes.CLASS_NAME).count());

        typedRealm.beginTransaction();
        try {
            typedRealm.createObject(AllTypes.class);
            assertEquals(1, typedRealm.where(AllTypes.class).count());
            assertEquals(0, dynamicRealm.where(AllTypes.CLASS_NAME).count());
            typedRealm.cancelTransaction();
        } finally {
            typedRealm.close();
            dynamicRealm.close();
        }
    }

    // Test that Realms can only be deleted after all Typed and Dynamic instances are closed
    @Test
    public void deleteRealm_ThrowsIfDynamicRealmIsOpen() {
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
    @Test
    public void deleteRealm_throwsIfTypedRealmIsOpen() {
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

    @Test
    public void createObject() {
        realm.beginTransaction();
        DynamicRealmObject obj = realm.createObject(AllTypes.CLASS_NAME);
        realm.commitTransaction();
        assertTrue(obj.isValid());
    }

    @Test
    public void createObject_withPrimaryKey() {
        realm.beginTransaction();
        DynamicRealmObject dog = realm.createObject(DogPrimaryKey.CLASS_NAME, 42);
        assertEquals(42, dog.getLong("id"));
        realm.cancelTransaction();
    }

    @Test
    public void createObject_withNullStringPrimaryKey() {
        realm.beginTransaction();
        realm.createObject(PrimaryKeyAsString.CLASS_NAME, (String) null);
        realm.commitTransaction();

        RealmResults<DynamicRealmObject> results = realm.allObjects(PrimaryKeyAsString.CLASS_NAME);
        assertEquals(1, results.size());
        assertEquals(null, results.first().getString("name"));
    }

    @Test
    public void createObject_withNullBytePrimaryKey() {
        realm.beginTransaction();
        realm.createObject(PrimaryKeyAsBoxedByte.CLASS_NAME, (Byte) null);
        realm.commitTransaction();

        RealmResults<DynamicRealmObject> results = realm.allObjects(PrimaryKeyAsBoxedByte.CLASS_NAME);
        assertEquals(1, results.size());
        assertEquals(null, results.first().getObject("id"));
    }

    @Test
    public void createObject_withNullShortPrimaryKey() {
        realm.beginTransaction();
        realm.createObject(PrimaryKeyAsBoxedShort.CLASS_NAME, (Short) null);
        realm.commitTransaction();

        RealmResults<DynamicRealmObject> results = realm.allObjects(PrimaryKeyAsBoxedShort.CLASS_NAME);
        assertEquals(1, results.size());
        assertEquals(null, results.first().getObject("id"));
    }

    @Test
    public void createObject_withNullIntegerPrimaryKey() {
        realm.beginTransaction();
        realm.createObject(PrimaryKeyAsBoxedInteger.CLASS_NAME, (Integer) null);
        realm.commitTransaction();

        RealmResults<DynamicRealmObject> results = realm.allObjects(PrimaryKeyAsBoxedInteger.CLASS_NAME);
        assertEquals(1, results.size());
        assertEquals(null, results.first().getObject("id"));
    }

    @Test
    public void createObject_withNullLongPrimaryKey() {
        realm.beginTransaction();
        realm.createObject(PrimaryKeyAsBoxedLong.CLASS_NAME, (Long) null);
        realm.commitTransaction();

        RealmResults<DynamicRealmObject> results = realm.allObjects(PrimaryKeyAsBoxedLong.CLASS_NAME);
        assertEquals(1, results.size());
        assertEquals(null, results.first().getObject("id"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createObject_illegalPrimaryKeyValue() {
        realm.beginTransaction();
        realm.createObject(DogPrimaryKey.CLASS_NAME, "bar");
    }

    @Test
    public void where() {
        realm.beginTransaction();
        realm.createObject(AllTypes.CLASS_NAME);
        realm.commitTransaction();

        RealmResults<DynamicRealmObject> results = realm.where(AllTypes.CLASS_NAME).findAll();
        assertEquals(1, results.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void clear_invalidName() {
        realm.beginTransaction();
        realm.clear("I don't exist");
    }

    @Test(expected = IllegalStateException.class)
    public void clear_outsideTransactionClearOutsideTransactionThrows() {
        realm.clear(AllTypes.CLASS_NAME);
    }

    @Test
    public void clear() {
        realm.beginTransaction();
        realm.createObject(AllTypes.CLASS_NAME);
        realm.commitTransaction();

        assertEquals(1, realm.where(AllTypes.CLASS_NAME).count());
        realm.beginTransaction();
        realm.clear(AllTypes.CLASS_NAME);
        realm.commitTransaction();
        assertEquals(0, realm.where(AllTypes.CLASS_NAME).count());
    }

    @Test(expected = IllegalArgumentException.class)
    public void executeTransaction_null() {
        realm.executeTransaction(null);
    }

    @Test
    public void executeTransaction() {
        assertEquals(0, realm.allObjects(Owner.CLASS_NAME).size());
        realm.executeTransaction(new DynamicRealm.Transaction() {
            @Override
            public void execute(DynamicRealm realm) {
                DynamicRealmObject owner = realm.createObject(Owner.CLASS_NAME);
                owner.setString("name", "Owner");
            }
        });

        RealmResults<DynamicRealmObject> allObjects = realm.allObjects(Owner.CLASS_NAME);
        assertEquals(1, allObjects.size());
        assertEquals("Owner", allObjects.get(0).getString("name"));
    }

    @Test
    public void executeTransaction_cancelled() {
        final AtomicReference<RuntimeException> thrownException = new AtomicReference<>(null);

        assertEquals(0, realm.allObjects(Owner.CLASS_NAME).size());
        try {
            realm.executeTransaction(new DynamicRealm.Transaction() {
                @Override
                public void execute(DynamicRealm realm) {
                    DynamicRealmObject owner = realm.createObject(Owner.CLASS_NAME);
                    owner.setString("name", "Owner");
                    thrownException.set(new RuntimeException("Boom"));
                    throw thrownException.get();
                }
            });
        } catch (RuntimeException e) {
            //noinspection ThrowableResultOfMethodCallIgnored
            assertTrue(e == thrownException.get());
        }
        assertEquals(0, realm.allObjects(Owner.CLASS_NAME).size());
    }

    @Test
    public void executeTransaction_warningIfManuallyCancelled() {
        assertEquals(0, realm.allObjects("Owner").size());
        TestHelper.TestLogger testLogger = new TestHelper.TestLogger();
        try {
            RealmLog.add(testLogger);
            realm.executeTransaction(new DynamicRealm.Transaction() {
                @Override
                public void execute(DynamicRealm realm) {
                    DynamicRealmObject owner = realm.createObject("Owner");
                    owner.setString("name", "Owner");
                    realm.cancelTransaction();
                    throw new RuntimeException("Boom");
                }
            });
        } catch (RuntimeException ignored) {
            // Ensure that we pass a valuable error message to the logger for developers.
            assertEquals(testLogger.message, "Could not cancel transaction, not currently in a transaction.");
        } finally {
            RealmLog.remove(testLogger);
        }
        assertEquals(0, realm.allObjects("Owner").size());
    }

    @Test
    public void allObjectsSorted() {
        populateTestRealm();
        RealmResults<DynamicRealmObject> sortedList = realm.allObjectsSorted(AllTypes.CLASS_NAME, AllTypes.FIELD_STRING, Sort.ASCENDING);
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals("test data 0", sortedList.first().getString(AllTypes.FIELD_STRING));

        RealmResults<DynamicRealmObject> reverseList = realm.allObjectsSorted(AllTypes.CLASS_NAME, AllTypes.FIELD_STRING, Sort.DESCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals("test data 0", reverseList.last().getString(AllTypes.FIELD_STRING));
    }

    @Test(expected = IllegalArgumentException.class)
    public void allObjectsSorted_wrongFieldName() {
        realm.allObjectsSorted(AllTypes.CLASS_NAME, "invalid", Sort.ASCENDING);
    }

    @Test
    public void allObjectsSorted_sortTwoFields() {
        TestHelper.populateForMultiSort(realm);

        RealmResults<DynamicRealmObject> results1 = realm.allObjectsSorted(AllTypes.CLASS_NAME,
                new String[]{AllTypes.FIELD_STRING, AllTypes.FIELD_LONG},
                new Sort[]{Sort.ASCENDING, Sort.ASCENDING});

        assertEquals(3, results1.size());

        assertEquals("Adam", results1.get(0).getString(AllTypes.FIELD_STRING));
        assertEquals(4, results1.get(0).getLong(AllTypes.FIELD_LONG));

        assertEquals("Adam", results1.get(1).getString(AllTypes.FIELD_STRING));
        assertEquals(5, results1.get(1).getLong(AllTypes.FIELD_LONG));

        assertEquals("Brian", results1.get(2).getString(AllTypes.FIELD_STRING));
        assertEquals(4, results1.get(2).getLong(AllTypes.FIELD_LONG));

        RealmResults<DynamicRealmObject> results2 = realm.allObjectsSorted(AllTypes.CLASS_NAME,
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

    @Test
    public void allObjectsSorted_failures() {
        // zero fields specified
        try {
            realm.allObjectsSorted(AllTypes.CLASS_NAME, new String[]{}, new Sort[]{});
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // number of fields and sorting orders don't match
        try {
            realm.allObjectsSorted(AllTypes.CLASS_NAME,
                    new String[]{AllTypes.FIELD_STRING},
                    new Sort[]{Sort.ASCENDING, Sort.ASCENDING});
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // null is not allowed
        try {
            realm.allObjectsSorted(AllTypes.CLASS_NAME, null, (Sort[]) null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            realm.allObjectsSorted(AllTypes.CLASS_NAME, new String[]{AllTypes.FIELD_STRING}, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // non-existing field name
        try {
            realm.allObjectsSorted(AllTypes.CLASS_NAME,
                    new String[]{AllTypes.FIELD_STRING, "dont-exist"},
                    new Sort[]{Sort.ASCENDING, Sort.ASCENDING});
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void allObjectsSorted_singleField() {
        populateTestRealm();
        RealmResults<DynamicRealmObject> sortedList = realm.allObjectsSorted(AllTypes.CLASS_NAME,
                new String[]{AllTypes.FIELD_LONG},
                new Sort[]{Sort.DESCENDING});
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals(TEST_DATA_SIZE - 1, sortedList.first().getLong(AllTypes.FIELD_LONG));
        assertEquals(0, sortedList.last().getLong(AllTypes.FIELD_LONG));
    }

    private void populateForDistinct(DynamicRealm realm, long numberOfBlocks, long numberOfObjects, boolean withNull) {
        boolean autoRefreshEnabled = realm.isAutoRefresh();
        if (autoRefreshEnabled) {
            realm.setAutoRefresh(false);
        }
        realm.beginTransaction();
        for (int i = 0; i < numberOfObjects * numberOfBlocks; i++) {
            for (int j = 0; j < numberOfBlocks; j++) {
                DynamicRealmObject obj = realm.createObject("AnnotationIndexTypes");
                obj.setBoolean("indexBoolean", j % 2 == 0);
                obj.setLong("indexLong", j);
                obj.setDate("indexDate", withNull ? null : new Date(1000 * ((long) j)));
                obj.setString("indexString", withNull ? null : "Test " + j);
                obj.setBoolean("notIndexBoolean", j % 2 == 0);
                obj.setLong("notIndexLong", j);
                obj.setDate("notIndexDate", withNull ? null : new Date(1000 * ((long) j)));
                obj.setString("notIndexString", withNull ? null : "Test " + j);
            }
        }
        realm.commitTransaction();
        if (autoRefreshEnabled) {
            realm.setAutoRefresh(true);
        }
    }

    private void populateForDistinctInvalidTypesLinked(DynamicRealm realm) {
        realm.beginTransaction();
        DynamicRealmObject notEmpty = realm.createObject(AllJavaTypes.CLASS_NAME);
        notEmpty.setBlob(AllJavaTypes.FIELD_BINARY, new byte[]{1, 2, 3});
        notEmpty.setObject(AllJavaTypes.FIELD_OBJECT, notEmpty);
        notEmpty.setList(AllJavaTypes.FIELD_LIST, new RealmList<DynamicRealmObject>(notEmpty));
        realm.commitTransaction();
    }

    /*
     * Fields order test for Chained or Multi-Arguments Distinct()
     *
     * The idea is to interweave different values in 2's multiplier and 3's multiplier in a way that
     * the outcome is different if the order of distinct* operations alternates. More numbers of
     * fields can be constructed with the combination of multipliers in prime numbers such as 2, 3,
     * and 5.
     *
     * An example is illustrated below.
     *
     * Object      : O1| O2| O3| O4| O5| O6
     * indexString : A | A | B | B | A | A
     * indexLong   : 1 | 1 | 1 | 2 | 2 | 2
     */
    private void populateForDistinctFieldsOrder(DynamicRealm realm, long numberOfBlocks) {
        realm.beginTransaction();
        for (int i = 0; i < numberOfBlocks; i++) {
            for (int j = 0; j < 6; j++) {
                DynamicRealmObject obj = realm.createObject(AnnotationIndexTypes.CLASS_NAME);
                obj.setString("indexString", (((j / 2) % 2) == 0) ? "A" : "B");
                obj.setLong("indexLong", (j < 3) ? 1 : 2);
            }
        }
        realm.commitTransaction();
    }

    @Test
    public void distinct_invalidClassNames() {
        String[] classNames = new String[]{null, "", "foo", "foo.bar"};
        for (String className : classNames) {
            try {
                realm.distinct(className, "foo");
                fail();
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void distinct_invalidFieldNames() {
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
    @Test
    public void distinct() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        RealmResults<DynamicRealmObject> distinctBool = realm.distinct(AnnotationIndexTypes.CLASS_NAME, AnnotationIndexTypes.FIELD_INDEX_BOOL);
        assertEquals(2, distinctBool.size());
        for (String field : new String[]{AnnotationIndexTypes.FIELD_INDEX_LONG, AnnotationIndexTypes.FIELD_INDEX_DATE, AnnotationIndexTypes.FIELD_INDEX_STRING}) {
            RealmResults<DynamicRealmObject> distinct = realm.distinct(AnnotationIndexTypes.CLASS_NAME, field);
            assertEquals(field, numberOfBlocks, distinct.size());
        }
    }

    @Test
    public void distinct_notIndexedFields() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // must be greater than 1

        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        for (String field : AnnotationIndexTypes.NOT_INDEX_FIELDS) {
            try {
                realm.distinct(AnnotationIndexTypes.CLASS_NAME, field);
                fail(field);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void distinct_invalidTypes() {
        populateTestRealm();

        for (String field : new String[]{AllTypes.FIELD_REALMOBJECT, AllTypes.FIELD_REALMLIST, AllTypes.FIELD_DOUBLE, AllTypes.FIELD_FLOAT}) {
            try {
                realm.distinct(AllTypes.CLASS_NAME, field);
                fail(field);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void distinctMultiArgs() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        RealmResults<DynamicRealmObject> distinctMulti = realm.distinct(AnnotationIndexTypes.CLASS_NAME, AnnotationIndexTypes.FIELD_INDEX_BOOL, AnnotationIndexTypes.INDEX_FIELDS);
        assertEquals(numberOfBlocks, distinctMulti.size());
    }

    @Test
    public void distinctMultiArgs_switchedFieldsOrder() {
        final long numberOfBlocks = 25;
        populateForDistinctFieldsOrder(realm, numberOfBlocks);

        // Regardless of the block size defined above, the output size is expected to be the same, 4 in this case, due to receiving unique combinations of tuples
        RealmResults<DynamicRealmObject> distinctStringLong = realm.distinct(AnnotationIndexTypes.CLASS_NAME, AnnotationIndexTypes.FIELD_INDEX_STRING, AnnotationIndexTypes.FIELD_INDEX_LONG);
        RealmResults<DynamicRealmObject> distinctLongString = realm.distinct(AnnotationIndexTypes.CLASS_NAME, AnnotationIndexTypes.FIELD_INDEX_LONG, AnnotationIndexTypes.FIELD_INDEX_STRING);
        assertEquals(4, distinctStringLong.size());
        assertEquals(4, distinctLongString.size());
        assertEquals(distinctStringLong.size(), distinctLongString.size());
    }

    @Test
    public void distinctMultiArgs_emptyFields() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        // an empty string field in the middle
        try {
            realm.distinct(AnnotationIndexTypes.CLASS_NAME, AnnotationIndexTypes.FIELD_INDEX_BOOL, "", AnnotationIndexTypes.FIELD_INDEX_INT);
        } catch (IllegalArgumentException ignored) {
        }
        // an empty string field at the end
        try {
            realm.distinct(AnnotationIndexTypes.CLASS_NAME, AnnotationIndexTypes.FIELD_INDEX_BOOL, AnnotationIndexTypes.FIELD_INDEX_INT, "");
        } catch (IllegalArgumentException ignored) {
        }
        // a null string field in the middle
        try {
            realm.distinct(AnnotationIndexTypes.CLASS_NAME, AnnotationIndexTypes.FIELD_INDEX_BOOL, (String)null, AnnotationIndexTypes.FIELD_INDEX_INT);
        } catch (IllegalArgumentException ignored) {
        }
        // a null string field at the end
        try {
            realm.distinct(AnnotationIndexTypes.CLASS_NAME, AnnotationIndexTypes.FIELD_INDEX_BOOL, AnnotationIndexTypes.FIELD_INDEX_INT, (String)null);
        } catch (IllegalArgumentException ignored) {
        }
        // (String)null makes varargs a null array.
        try {
            realm.distinct(AnnotationIndexTypes.CLASS_NAME, AnnotationIndexTypes.FIELD_INDEX_BOOL, (String)null);
        } catch (IllegalArgumentException ignored) {
        }
        // Two (String)null for first and varargs fields
        try {
            realm.distinct(AnnotationIndexTypes.CLASS_NAME, (String)null, (String)null);
        } catch (IllegalArgumentException ignored) {
        }
        // "" & (String)null combination
        try {
            realm.distinct(AnnotationIndexTypes.CLASS_NAME, "", (String)null);
        } catch (IllegalArgumentException ignored) {
        }
        // "" & (String)null combination
        try {
            realm.distinct(AnnotationIndexTypes.CLASS_NAME, (String)null, "");
        } catch (IllegalArgumentException ignored) {
        }
        // Two empty fields tests
        try {
            realm.distinct(AnnotationIndexTypes.CLASS_NAME, "", "");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctMultiArgs_withNullValues() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        RealmResults<DynamicRealmObject> distinctMulti = realm.distinct(AnnotationIndexTypes.CLASS_NAME, AnnotationIndexTypes.FIELD_INDEX_DATE, AnnotationIndexTypes.FIELD_INDEX_STRING);
        assertEquals(1, distinctMulti.size());
    }

    @Test
    public void distinctMultiArgs_notIndexedFields() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        try {
            realm.distinct(AnnotationIndexTypes.CLASS_NAME, AnnotationIndexTypes.FIELD_NOT_INDEX_STRING, AnnotationIndexTypes.NOT_INDEX_FIELDS);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctMultiArgs_doesNotExistField() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        try {
            realm.distinct(AnnotationIndexTypes.CLASS_NAME, AnnotationIndexTypes.FIELD_INDEX_INT, AnnotationIndexTypes.NONEXISTANT_MIX_FIELDS);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctMultiArgs_invalidTypesFields() {
        populateTestRealm();

        try {
            realm.distinct(AllTypes.CLASS_NAME, AllTypes.FIELD_REALMOBJECT, AllTypes.INVALID_TYPES_FIELDS_FOR_DISTINCT);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctMultiArgs_indexedLinkedFields() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        try {
            realm.distinct(AnnotationIndexTypes.CLASS_NAME, AnnotationIndexTypes.INDEX_LINKED_FIELD_STRING, AnnotationIndexTypes.INDEX_LINKED_FIELDS);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctMultiArgs_notIndexedLinkedFields() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        try {
            realm.distinct(AnnotationIndexTypes.CLASS_NAME, AnnotationIndexTypes.NOT_INDEX_LINKED_FILED_STRING, AnnotationIndexTypes.NOT_INDEX_LINKED_FIELDS);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctMultiArgs_invalidTypesLinkedFields() {
        populateForDistinctInvalidTypesLinked(realm);

        try {
            realm.distinct(AllJavaTypes.CLASS_NAME, AllJavaTypes.INVALID_LINKED_BINARY_FIELD_FOR_DISTINCT, AllJavaTypes.INVALID_LINKED_TYPES_FIELDS_FOR_DISTINCT);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    @RunTestInLooperThread
    public void findFirstAsync() {
        final DynamicRealm dynamicRealm = initializeDynamicRealm();
        final DynamicRealmObject allTypes = dynamicRealm.where(AllTypes.CLASS_NAME)
                .between(AllTypes.FIELD_LONG, 4, 9)
                .findFirstAsync();
        assertFalse(allTypes.isLoaded());

        allTypes.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                assertEquals("test data 4", allTypes.getString(AllTypes.FIELD_STRING));
                dynamicRealm.close();
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void findAllAsync() {
        final DynamicRealm dynamicRealm = initializeDynamicRealm();
        final RealmResults<DynamicRealmObject> allTypes = dynamicRealm.where(AllTypes.CLASS_NAME)
                .between(AllTypes.FIELD_LONG, 4, 9)
                .findAllAsync();

        assertFalse(allTypes.isLoaded());
        assertEquals(0, allTypes.size());

        allTypes.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                assertEquals(6, allTypes.size());
                for (int i = 0; i < allTypes.size(); i++) {
                    assertEquals("test data " + (4 + i), allTypes.get(i).getString(AllTypes.FIELD_STRING));
                }
                dynamicRealm.close();
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void findAllSortedAsync() {
        final DynamicRealm dynamicRealm = initializeDynamicRealm();
        final RealmResults<DynamicRealmObject> allTypes = dynamicRealm.where(AllTypes.CLASS_NAME)
                .between(AllTypes.FIELD_LONG, 0, 4)
                .findAllSortedAsync(AllTypes.FIELD_STRING, Sort.DESCENDING);
        assertFalse(allTypes.isLoaded());
        assertEquals(0, allTypes.size());

        allTypes.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                assertEquals(5, allTypes.size());
                for (int i = 0; i < 5; i++) {
                    int iteration = (4 - i);
                    assertEquals("test data " + iteration, allTypes.get(4 - iteration).getString(AllTypes.FIELD_STRING));
                }
                dynamicRealm.close();
                looperThread.testComplete();
            }
        });
    }

    // Initialize a Dynamic Realm used by the *Async tests.
    private DynamicRealm initializeDynamicRealm() {
        RealmConfiguration defaultConfig = looperThread.realmConfiguration;
        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(defaultConfig);
        populateTestRealm(dynamicRealm, 10);
        return dynamicRealm;
    }

    @Test
    @RunTestInLooperThread
    public void findAllSortedAsync_usingMultipleFields() {
        final DynamicRealm dynamicRealm = initializeDynamicRealm();

        dynamicRealm.setAutoRefresh(false);
        dynamicRealm.beginTransaction();
        dynamicRealm.clear(AllTypes.CLASS_NAME);
        for (int i = 0; i < 5; ) {
            DynamicRealmObject allTypes = dynamicRealm.createObject(AllTypes.CLASS_NAME);
            allTypes.set(AllTypes.FIELD_LONG, i);
            allTypes.set(AllTypes.FIELD_STRING, "data " + i % 3);

            allTypes = dynamicRealm.createObject(AllTypes.CLASS_NAME);
            allTypes.set(AllTypes.FIELD_LONG, i);
            allTypes.set(AllTypes.FIELD_STRING, "data " + (++i % 3));
        }
        dynamicRealm.commitTransaction();
        dynamicRealm.setAutoRefresh(true);

        // Sort first set by using: String[ASC], Long[DESC]
        final RealmResults<DynamicRealmObject> realmResults1 = dynamicRealm.where(AllTypes.CLASS_NAME)
                .findAllSortedAsync(
                        new String[]{AllTypes.FIELD_STRING, AllTypes.FIELD_LONG},
                        new Sort[]{Sort.ASCENDING, Sort.DESCENDING}
                );

        // Sort second set by using: String[DESC], Long[ASC]
        final RealmResults<DynamicRealmObject> realmResults2 = dynamicRealm.where(AllTypes.CLASS_NAME)
                .between(AllTypes.FIELD_LONG, 0, 5)
                .findAllSortedAsync(
                        new String[]{AllTypes.FIELD_STRING, AllTypes.FIELD_LONG},
                        new Sort[]{Sort.DESCENDING, Sort.ASCENDING}
                );

        final Runnable signalCallbackDone = new Runnable() {
            final AtomicInteger callbacksDone = new AtomicInteger(2);
            @Override
            public void run() {
                if (callbacksDone.decrementAndGet() == 0) {
                    dynamicRealm.close();
                    looperThread.testComplete();
                }
            }
        };

        realmResults1.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                assertEquals("data 0", realmResults1.get(0).get(AllTypes.FIELD_STRING));
                assertEquals(3L, realmResults1.get(0).get(AllTypes.FIELD_LONG));
                assertEquals("data 0", realmResults1.get(1).get(AllTypes.FIELD_STRING));
                assertEquals(2L, realmResults1.get(1).get(AllTypes.FIELD_LONG));
                assertEquals("data 0", realmResults1.get(2).get(AllTypes.FIELD_STRING));
                assertEquals(0L, realmResults1.get(2).get(AllTypes.FIELD_LONG));

                assertEquals("data 1", realmResults1.get(3).get(AllTypes.FIELD_STRING));
                assertEquals(4L, realmResults1.get(3).get(AllTypes.FIELD_LONG));
                assertEquals("data 1", realmResults1.get(4).get(AllTypes.FIELD_STRING));
                assertEquals(3L, realmResults1.get(4).get(AllTypes.FIELD_LONG));
                assertEquals("data 1", realmResults1.get(5).get(AllTypes.FIELD_STRING));
                assertEquals(1L, realmResults1.get(5).get(AllTypes.FIELD_LONG));
                assertEquals("data 1", realmResults1.get(6).get(AllTypes.FIELD_STRING));
                assertEquals(0L, realmResults1.get(6).get(AllTypes.FIELD_LONG));

                assertEquals("data 2", realmResults1.get(7).get(AllTypes.FIELD_STRING));
                assertEquals(4L, realmResults1.get(7).get(AllTypes.FIELD_LONG));
                assertEquals("data 2", realmResults1.get(8).get(AllTypes.FIELD_STRING));
                assertEquals(2L, realmResults1.get(8).get(AllTypes.FIELD_LONG));
                assertEquals("data 2", realmResults1.get(9).get(AllTypes.FIELD_STRING));
                assertEquals(1L, realmResults1.get(9).get(AllTypes.FIELD_LONG));

                signalCallbackDone.run();
            }
        });

        realmResults2.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                assertEquals("data 2", realmResults2.get(0).get(AllTypes.FIELD_STRING));
                assertEquals(1L, realmResults2.get(0).get(AllTypes.FIELD_LONG));
                assertEquals("data 2", realmResults2.get(1).get(AllTypes.FIELD_STRING));
                assertEquals(2L, realmResults2.get(1).get(AllTypes.FIELD_LONG));
                assertEquals("data 2", realmResults2.get(2).get(AllTypes.FIELD_STRING));
                assertEquals(4L, realmResults2.get(2).get(AllTypes.FIELD_LONG));

                assertEquals("data 1", realmResults2.get(3).get(AllTypes.FIELD_STRING));
                assertEquals(0L, realmResults2.get(3).get(AllTypes.FIELD_LONG));
                assertEquals("data 1", realmResults2.get(4).get(AllTypes.FIELD_STRING));
                assertEquals(1L, realmResults2.get(4).get(AllTypes.FIELD_LONG));
                assertEquals("data 1", realmResults2.get(5).get(AllTypes.FIELD_STRING));
                assertEquals(3L, realmResults2.get(5).get(AllTypes.FIELD_LONG));
                assertEquals("data 1", realmResults2.get(6).get(AllTypes.FIELD_STRING));
                assertEquals(4L, realmResults2.get(6).get(AllTypes.FIELD_LONG));

                assertEquals("data 0", realmResults2.get(7).get(AllTypes.FIELD_STRING));
                assertEquals(0L, realmResults2.get(7).get(AllTypes.FIELD_LONG));
                assertEquals("data 0", realmResults2.get(8).get(AllTypes.FIELD_STRING));
                assertEquals(2L, realmResults2.get(8).get(AllTypes.FIELD_LONG));
                assertEquals("data 0", realmResults2.get(9).get(AllTypes.FIELD_STRING));
                assertEquals(3L, realmResults2.get(9).get(AllTypes.FIELD_LONG));

                signalCallbackDone.run();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void distinctAsync() {
        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(looperThread.realmConfiguration);
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // must be greater than 1
        populateForDistinct(dynamicRealm, numberOfBlocks, numberOfObjects, false);

        final RealmResults<DynamicRealmObject> distinctBool = dynamicRealm.distinctAsync(AnnotationIndexTypes.CLASS_NAME, AnnotationIndexTypes.FIELD_INDEX_BOOL);
        final RealmResults<DynamicRealmObject> distinctLong = dynamicRealm.distinctAsync(AnnotationIndexTypes.CLASS_NAME, AnnotationIndexTypes.FIELD_INDEX_LONG);
        final RealmResults<DynamicRealmObject> distinctDate = dynamicRealm.distinctAsync(AnnotationIndexTypes.CLASS_NAME, AnnotationIndexTypes.FIELD_INDEX_DATE);
        final RealmResults<DynamicRealmObject> distinctString = dynamicRealm.distinctAsync(AnnotationIndexTypes.CLASS_NAME, AnnotationIndexTypes.FIELD_INDEX_STRING);

        final Runnable callbackDoneTask = new Runnable() {
            final CountDownLatch signalTestFinished = new CountDownLatch(4);
            @Override
            public void run() {
                signalTestFinished.countDown();
                if (signalTestFinished.getCount() == 0) {
                    dynamicRealm.close();
                    looperThread.testComplete();
                }
            }
        };

        distinctBool.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                assertEquals(2, distinctBool.size());
                callbackDoneTask.run();
            }
        });

        distinctLong.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                assertEquals(numberOfBlocks, distinctLong.size());
                callbackDoneTask.run();
            }
        });

        distinctDate.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                assertEquals(numberOfBlocks, distinctDate.size());
                callbackDoneTask.run();
            }
        });

        distinctString.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                assertEquals(numberOfBlocks, distinctString.size());
                callbackDoneTask.run();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void accessingDynamicRealmObjectBeforeAsyncQueryCompleted() {
        final DynamicRealm dynamicRealm = initializeDynamicRealm();
        final DynamicRealmObject[] dynamicRealmObject = new DynamicRealmObject[1];

        // Intercept completion of the async DynamicRealmObject query
        Handler handler = new HandlerProxy(dynamicRealm.handlerController) {
            @Override
            public boolean onInterceptInMessage(int what) {
                switch (what) {
                    case HandlerController.COMPLETED_ASYNC_REALM_OBJECT: {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                assertFalse(dynamicRealmObject[0].isLoaded());
                                assertFalse(dynamicRealmObject[0].isValid());
                                try {
                                    dynamicRealmObject[0].getObject(AllTypes.FIELD_BINARY);
                                    fail("trying to access a DynamicRealmObject property should throw");
                                } catch (IllegalStateException ignored) {

                                } finally {
                                    dynamicRealm.close();
                                    looperThread.testComplete();
                                }
                            }
                        });
                        return true;
                    }
                }
                return false;
            }
        };

        dynamicRealm.setHandler(handler);
        dynamicRealmObject[0] = dynamicRealm.where(AllTypes.CLASS_NAME)
                .between(AllTypes.FIELD_LONG, 4, 9)
                .findFirstAsync();
    }

    @Test
    public void clear_all() {
        realm.beginTransaction();
        realm.createObject(AllTypes.CLASS_NAME);
        DynamicRealmObject cat = realm.createObject(Cat.CLASS_NAME);
        DynamicRealmObject owner =  realm.createObject(Owner.CLASS_NAME);
        owner.setObject("cat", cat);
        realm.getSchema().create("TestRemoveAll").addField("Field1", String.class);
        realm.createObject("TestRemoveAll");
        realm.commitTransaction();

        assertEquals(1, realm.where(AllTypes.CLASS_NAME).count());
        assertEquals(1, realm.where(Owner.CLASS_NAME).count());
        assertEquals(1, realm.where(Cat.CLASS_NAME).count());
        assertEquals(1, realm.where("TestRemoveAll").count());

        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();

        assertEquals(0, realm.where(AllTypes.CLASS_NAME).count());
        assertEquals(0, realm.where(Owner.CLASS_NAME).count());
        assertEquals(0, realm.where(Cat.CLASS_NAME).count());
        assertEquals(0, realm.where("TestRemoveAll").count());
        assertTrue(realm.isEmpty());
    }

    @Test
    public void realmListRemoveAllFromRealm() {
        populateTestRealm(realm, 1);
        RealmList<DynamicRealmObject> list = realm.where(AllTypes.CLASS_NAME).findFirst().getList(AllTypes.FIELD_REALMLIST);
        assertEquals(2, list.size());

        realm.beginTransaction();
        list.deleteAllFromRealm();
        realm.commitTransaction();

        assertEquals(0, list.size());
        assertEquals(0, realm.where(Dog.CLASS_NAME).count());
    }
}
