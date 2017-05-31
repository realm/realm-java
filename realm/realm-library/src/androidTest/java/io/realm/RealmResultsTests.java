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

import android.support.test.annotation.UiThreadTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.AllTypes;
import io.realm.entities.AnnotationIndexTypes;
import io.realm.entities.DefaultValueOfField;
import io.realm.entities.Dog;
import io.realm.entities.NonLatinFieldNames;
import io.realm.entities.Owner;
import io.realm.entities.RandomPrimaryKey;
import io.realm.entities.StringOnly;
import io.realm.internal.Collection;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmResultsTests extends CollectionTests {

    private final static int TEST_DATA_SIZE = 2516;
    private final static long YEAR_MILLIS = TimeUnit.DAYS.toMillis(365);
    private final static long DECADE_MILLIS = 10 * TimeUnit.DAYS.toMillis(365);

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    private Realm realm;
    private RealmResults<AllTypes> collection;

    @Before
    public void setUp() {
        RealmConfiguration realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);
        populateTestRealm();
        collection = realm.where(AllTypes.class).findAllSorted(AllTypes.FIELD_LONG, Sort.ASCENDING);
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    private void populateTestRealm() {
        populateTestRealm(TEST_DATA_SIZE);
    }

    @Test
    public void findFirst() {
        AllTypes result = realm.where(AllTypes.class).findFirst();
        assertEquals(0, result.getColumnLong());
        assertEquals("test data 0", result.getColumnString());

        AllTypes none = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "smurf").findFirst();
        assertNull(none);
    }

    @Test
    public void size_returns_Integer_MAX_VALUE_for_huge_results() {
        final Collection collection = Mockito.mock(Collection.class);
        final RealmResults<AllTypes> targetResult = TestHelper.newRealmResults(realm, collection, AllTypes.class);

        Mockito.when(collection.isLoaded()).thenReturn(true);
        Mockito.when(collection.size()).thenReturn(((long) Integer.MAX_VALUE) - 1);
        assertEquals(Integer.MAX_VALUE - 1, targetResult.size());
        Mockito.when(collection.size()).thenReturn(((long) Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, targetResult.size());
        Mockito.when(collection.size()).thenReturn(((long) Integer.MAX_VALUE) + 1);
        assertEquals(Integer.MAX_VALUE, targetResult.size());
    }

    @Test
    public void subList() {
        RealmResults<AllTypes> list = realm.where(AllTypes.class).findAll();
        list.sort("columnLong");
        List<AllTypes> sublist = list.subList(Math.max(list.size() - 20, 0), list.size());
        assertEquals(TEST_DATA_SIZE - 1, sublist.get(sublist.size() - 1).getColumnLong());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void unsupportedMethods() {
        for (CollectionMutatorMethod method : CollectionMutatorMethod.values()) {
            try {
                switch (method) {
                    case ADD_OBJECT: collection.add(new AllTypes()); break;
                    case ADD_ALL_OBJECTS: collection.addAll(Collections.singletonList(new AllTypes())); break;
                    case CLEAR: collection.clear(); break;
                    case REMOVE_OBJECT: collection.remove(new AllTypes()); break;
                    case REMOVE_ALL: collection.removeAll(Collections.singletonList(new AllTypes())); break;
                    case RETAIN_ALL: collection.retainAll(Collections.singletonList(new AllTypes())); break;

                    // Supported methods.
                    case DELETE_ALL:
                        continue;
                }
                fail("Unknown method or failed to throw:" + method);
            } catch (UnsupportedOperationException ignored) {
            }
        }

        for (OrderedCollectionMutatorMethod method : OrderedCollectionMutatorMethod.values()) {
            try {
                switch (method) {
                    case ADD_INDEX: collection.add(0, new AllTypes()); break;
                    case ADD_ALL_INDEX: collection.addAll(0, Collections.singletonList(new AllTypes())); break;
                    case SET: collection.set(0, new AllTypes()); break;
                    case REMOVE_INDEX: collection.remove(0); break;

                    // Supported methods.
                    case DELETE_INDEX:
                    case DELETE_FIRST:
                    case DELETE_LAST:
                        continue;
                }
                fail("Unknown method or failed to throw:" + method);
            } catch (UnsupportedOperationException ignored) {
            }
        }
    }

    // Triggers an ARM bug.
    @Test
    public void verifyArmComparisons() {
        realm.beginTransaction();
        realm.delete(AllTypes.class);
        long id = -1;
        for (int i = 0; i < 10; i++) {
            AllTypes allTypes = realm.createObject(AllTypes.class);
            allTypes.setColumnLong(id--);
        }
        realm.commitTransaction();

        assertEquals(10, realm.where(AllTypes.class).between(AllTypes.FIELD_LONG, -10, -1).findAll().size());
        assertEquals(10, realm.where(AllTypes.class).greaterThan(AllTypes.FIELD_LONG, -11).findAll().size());
        assertEquals(10, realm.where(AllTypes.class).greaterThanOrEqualTo(AllTypes.FIELD_LONG, -10).findAll().size());
        assertEquals(10, realm.where(AllTypes.class).lessThan(AllTypes.FIELD_LONG, 128).findAll().size());
        assertEquals(10, realm.where(AllTypes.class).lessThan(AllTypes.FIELD_LONG, 127).findAll().size());
        assertEquals(10, realm.where(AllTypes.class).lessThanOrEqualTo(AllTypes.FIELD_LONG, -1).findAll().size());
        assertEquals(10, realm.where(AllTypes.class).lessThan(AllTypes.FIELD_LONG, 0).findAll().size());
    }

    // RealmResults.distinct(): requires indexing, and type = boolean, integer, date, string.
    private void populateForDistinct(Realm realm, long numberOfBlocks, long numberOfObjects, boolean withNull) {
        realm.beginTransaction();
        for (int i = 0; i < numberOfObjects * numberOfBlocks; i++) {
            for (int j = 0; j < numberOfBlocks; j++) {
                AnnotationIndexTypes obj = realm.createObject(AnnotationIndexTypes.class);
                obj.setIndexBoolean(j % 2 == 0);
                obj.setIndexLong(j);
                obj.setIndexDate(withNull ? null : new Date(1000 * (long) j));
                obj.setIndexString(withNull ? null : "Test " + j);
                obj.setNotIndexBoolean(j % 2 == 0);
                obj.setNotIndexLong(j);
                obj.setNotIndexDate(withNull ? null : new Date(1000 * (long) j));
                obj.setNotIndexString(withNull ? null : "Test " + j);
            }
        }
        realm.commitTransaction();
    }

    private void populateForDistinctInvalidTypesLinked(Realm realm) {
        realm.beginTransaction();
        AllJavaTypes notEmpty = new AllJavaTypes();
        notEmpty.setFieldBinary(new byte[]{1, 2, 3});
        notEmpty.setFieldObject(notEmpty);
        notEmpty.setFieldList(new RealmList<AllJavaTypes>(notEmpty));
        realm.copyToRealm(notEmpty);
        realm.commitTransaction();
    }

    @Test
    public void distinct() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // Must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        RealmResults<AnnotationIndexTypes> distinctBool = realm.where(AnnotationIndexTypes.class).findAll().distinct(AnnotationIndexTypes.FIELD_INDEX_BOOL);
        assertEquals(2, distinctBool.size());
        for (String field : new String[]{AnnotationIndexTypes.FIELD_INDEX_LONG, AnnotationIndexTypes.FIELD_INDEX_DATE, AnnotationIndexTypes.FIELD_INDEX_STRING}) {
            RealmResults<AnnotationIndexTypes> distinct = realm.where(AnnotationIndexTypes.class).findAll().distinct(field);
            assertEquals(field, numberOfBlocks, distinct.size());
        }
    }

    @Test
    @SuppressWarnings("ReferenceEquality")
    public void distinct_restrictedByPreviousDistinct() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        // All objects
        RealmResults<AnnotationIndexTypes> allResults = realm.where(AnnotationIndexTypes.class).findAll();
        assertEquals("All Objects Count", numberOfBlocks * numberOfBlocks * numberOfObjects, allResults.size());
        // Distinctive dates
        RealmResults<AnnotationIndexTypes> distinctDates = allResults.distinct(AnnotationIndexTypes.FIELD_INDEX_DATE);
        assertEquals("Distinctive Dates", numberOfBlocks, distinctDates.size());
        // Distinctive Booleans
        RealmResults<AnnotationIndexTypes> distinctBooleans = distinctDates.distinct(AnnotationIndexTypes.FIELD_INDEX_BOOL);
        assertEquals("Distinctive Booleans", 2, distinctBooleans.size());
        // distinct results are not the same object
        assertTrue(allResults != distinctDates);
        assertTrue(allResults != distinctBooleans);
    }

    @Test
    public void distinct_withNullValues() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // Must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        for (String field : new String[]{AnnotationIndexTypes.FIELD_INDEX_DATE, AnnotationIndexTypes.FIELD_INDEX_STRING}) {
            RealmResults<AnnotationIndexTypes> distinct = realm.where(AnnotationIndexTypes.class).findAll().distinct(field);
            assertEquals(field, 1, distinct.size());
        }
    }

    @Test
    public void distinct_notIndexedFields() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // Must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        RealmResults<AnnotationIndexTypes> distinctBool = realm.where(AnnotationIndexTypes.class)
                .findAll().distinct(AnnotationIndexTypes.FIELD_NOT_INDEX_BOOL);
        assertEquals(2, distinctBool.size());
        for (String field : new String[]{AnnotationIndexTypes.FIELD_NOT_INDEX_LONG,
                AnnotationIndexTypes.FIELD_NOT_INDEX_DATE, AnnotationIndexTypes.FIELD_NOT_INDEX_STRING}) {
            RealmResults<AnnotationIndexTypes> distinct = realm.where(AnnotationIndexTypes.class).findAll()
                    .distinct(field);
            assertEquals(field, numberOfBlocks, distinct.size());
        }
    }

    @Test
    public void distinct_noneExistingField() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // Must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        try {
            realm.where(AnnotationIndexTypes.class).findAll().distinct("doesNotExist");
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinct_invalidTypes() {
        populateTestRealm();

        for (String field : new String[]{AllTypes.FIELD_REALMOBJECT, AllTypes.FIELD_REALMLIST, AllTypes.FIELD_DOUBLE, AllTypes.FIELD_FLOAT}) {
            try {
                realm.where(AllTypes.class).findAll().distinct(field);
                fail(field);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void distinct_indexedLinkedFields() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // Must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        for (String field : AnnotationIndexTypes.INDEX_FIELDS) {
            try {
                realm.where(AnnotationIndexTypes.class).findAll().distinct(AnnotationIndexTypes.FIELD_OBJECT + "." + field);
                fail("Unsupported Index" + field + " linked field");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void distinct_notIndexedLinkedFields() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // Must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        for (String field : AnnotationIndexTypes.NOT_INDEX_FIELDS) {
            try {
                realm.where(AnnotationIndexTypes.class).findAll().distinct(AnnotationIndexTypes.FIELD_OBJECT + "." + field);
                fail("Unsupported notIndex" + field + " linked field");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void distinct_invalidTypesLinkedFields() {
        populateForDistinctInvalidTypesLinked(realm);

        try {
            realm.where(AllJavaTypes.class).findAll().distinct(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_BINARY);
            fail("Unsupported columnBinary linked field");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    @RunTestInLooperThread
    public void changeListener_syncIfNeeded_updatedFromOtherThread() {
        final Realm realm = looperThread.getRealm();
        populateTestRealm(realm, 10);

        final RealmResults<AllTypes> results = realm.where(AllTypes.class).lessThan(AllTypes.FIELD_LONG, 10).findAll();
        assertEquals(10, results.size());

        // 1. Deletes first object from another thread.
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(AllTypes.class).equalTo(AllTypes.FIELD_LONG, 0).findFirst().deleteFromRealm();
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                // 2. RealmResults are refreshed before onSuccess is called.
                assertEquals(9, results.size());
                realm.close();
                looperThread.testComplete();
            }
        });
    }

    // distinctAsync
    private void populateTestRealm(int objects) {
        realm.beginTransaction();
        realm.deleteAll();
        for (int i = 0; i < objects; ++i) {
            AllTypes allTypes = realm.createObject(AllTypes.class);
            allTypes.setColumnBoolean((i % 2) == 0);
            allTypes.setColumnBinary(new byte[]{1, 2, 3});
            allTypes.setColumnDate(new Date(YEAR_MILLIS * (i - objects / 2)));
            allTypes.setColumnDouble(Math.PI + i);
            allTypes.setColumnFloat(1.234567f + i);
            allTypes.setColumnString("test data " + i);
            allTypes.setColumnLong(i);
            Dog d = realm.createObject(Dog.class);
            d.setName("Foo " + i);
            allTypes.setColumnRealmObject(d);
            allTypes.getColumnRealmList().add(d);
            NonLatinFieldNames nonLatinFieldNames = realm.createObject(NonLatinFieldNames.class);
            nonLatinFieldNames.set델타(i);
            nonLatinFieldNames.setΔέλτα(i);
        }
        realm.commitTransaction();
    }


    private void populateTestRealm(Realm testRealm, int objects) {
        testRealm.beginTransaction();
        testRealm.deleteAll();
        for (int i = 0; i < objects; i++) {
            AllTypes allTypes = testRealm.createObject(AllTypes.class);
            allTypes.setColumnBoolean((i % 3) == 0);
            allTypes.setColumnBinary(new byte[]{1, 2, 3});
            allTypes.setColumnDate(new Date(DECADE_MILLIS * (i - (objects / 2))));
            allTypes.setColumnDouble(Math.PI);
            allTypes.setColumnFloat(1.234567f + i);
            allTypes.setColumnString("test data " + i);
            allTypes.setColumnLong(i);
            NonLatinFieldNames nonLatinFieldNames = testRealm.createObject(NonLatinFieldNames.class);
            nonLatinFieldNames.set델타(i);
            nonLatinFieldNames.setΔέλτα(i);
            nonLatinFieldNames.set베타(1.234567f + i);
            nonLatinFieldNames.setΒήτα(1.234567f + i);
        }
        testRealm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void distinctAsync() throws Throwable {
        final AtomicInteger changeListenerCalled = new AtomicInteger(4);
        final Realm realm = looperThread.getRealm();
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // Must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        final RealmResults<AnnotationIndexTypes> distinctBool = realm.where(AnnotationIndexTypes.class).findAll().distinctAsync(AnnotationIndexTypes.FIELD_INDEX_BOOL);
        final RealmResults<AnnotationIndexTypes> distinctLong = realm.where(AnnotationIndexTypes.class).findAll().distinctAsync(AnnotationIndexTypes.FIELD_INDEX_LONG);
        final RealmResults<AnnotationIndexTypes> distinctDate = realm.where(AnnotationIndexTypes.class).findAll().distinctAsync(AnnotationIndexTypes.FIELD_INDEX_DATE);
        final RealmResults<AnnotationIndexTypes> distinctString = realm.where(AnnotationIndexTypes.class).findAll().distinctAsync(AnnotationIndexTypes.FIELD_INDEX_STRING);

        assertFalse(distinctBool.isLoaded());
        assertTrue(distinctBool.isValid());
        assertTrue(distinctBool.isEmpty());

        assertFalse(distinctLong.isLoaded());
        assertTrue(distinctLong.isValid());
        assertTrue(distinctLong.isEmpty());

        assertFalse(distinctDate.isLoaded());
        assertTrue(distinctDate.isValid());
        assertTrue(distinctDate.isEmpty());

        assertFalse(distinctString.isLoaded());
        assertTrue(distinctString.isValid());
        assertTrue(distinctString.isEmpty());

        final Runnable endTest = new Runnable() {
            @Override
            public void run() {
                if (changeListenerCalled.decrementAndGet() == 0) {
                    looperThread.testComplete();
                }
            }
        };

        looperThread.keepStrongReference(distinctBool);
        looperThread.keepStrongReference(distinctLong);
        looperThread.keepStrongReference(distinctDate);
        looperThread.keepStrongReference(distinctString);
        distinctBool.addChangeListener(new RealmChangeListener<RealmResults<AnnotationIndexTypes>>() {
            @Override
            public void onChange(RealmResults<AnnotationIndexTypes> object) {
                assertEquals(2, distinctBool.size());
                endTest.run();
            }
        });

        distinctLong.addChangeListener(new RealmChangeListener<RealmResults<AnnotationIndexTypes>>() {
            @Override
            public void onChange(RealmResults<AnnotationIndexTypes> object) {
                assertEquals(numberOfBlocks, distinctLong.size());
                endTest.run();
            }
        });

        distinctDate.addChangeListener(new RealmChangeListener<RealmResults<AnnotationIndexTypes>>() {
            @Override
            public void onChange(RealmResults<AnnotationIndexTypes> object) {
                assertEquals(numberOfBlocks, distinctDate.size());
                endTest.run();
            }
        });

        distinctString.addChangeListener(new RealmChangeListener<RealmResults<AnnotationIndexTypes>>() {
            @Override
            public void onChange(RealmResults<AnnotationIndexTypes> object) {
                assertEquals(numberOfBlocks, distinctString.size());
                endTest.run();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void distinctAsync_withNullValues() throws Throwable {
        final AtomicInteger changeListenerCalled = new AtomicInteger(2);
        final Realm realm = looperThread.getRealm();
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // Must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        final RealmResults<AnnotationIndexTypes> distinctDate = realm.where(AnnotationIndexTypes.class).findAll().distinctAsync(AnnotationIndexTypes.FIELD_INDEX_DATE);
        final RealmResults<AnnotationIndexTypes> distinctString = realm.where(AnnotationIndexTypes.class).findAll().distinctAsync(AnnotationIndexTypes.FIELD_INDEX_STRING);

        assertFalse(distinctDate.isLoaded());
        assertTrue(distinctDate.isValid());
        assertTrue(distinctDate.isEmpty());

        assertFalse(distinctString.isLoaded());
        assertTrue(distinctString.isValid());
        assertTrue(distinctString.isEmpty());

        final Runnable endTest = new Runnable() {
            @Override
            public void run() {
                if (changeListenerCalled.decrementAndGet() == 0) {
                    looperThread.testComplete();
                }
            }
        };

        looperThread.keepStrongReference(distinctDate);
        looperThread.keepStrongReference(distinctString);
        distinctDate.addChangeListener(new RealmChangeListener<RealmResults<AnnotationIndexTypes>>() {
            @Override
            public void onChange(RealmResults<AnnotationIndexTypes> object) {
                assertEquals("distinctDate", 1, distinctDate.size());
                endTest.run();
            }
        });

        distinctString.addChangeListener(new RealmChangeListener<RealmResults<AnnotationIndexTypes>>() {
            @Override
            public void onChange(RealmResults<AnnotationIndexTypes> object) {
                assertEquals("distinctString", 1, distinctString.size());
                endTest.run();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void distinctAsync_notIndexedFields() {
        final AtomicInteger changeListenerCalled = new AtomicInteger(4);
        Realm realm = looperThread.getRealm();
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        final RealmResults<AnnotationIndexTypes> distinctBool = realm.where(AnnotationIndexTypes.class).findAll()
                .distinctAsync(AnnotationIndexTypes.FIELD_INDEX_BOOL);
        final RealmResults<AnnotationIndexTypes> distinctLong = realm.where(AnnotationIndexTypes.class).findAll()
                .distinctAsync(AnnotationIndexTypes.FIELD_NOT_INDEX_LONG);
        final RealmResults<AnnotationIndexTypes> distinctDate = realm.where(AnnotationIndexTypes.class).findAll()
                .distinctAsync(AnnotationIndexTypes.FIELD_NOT_INDEX_DATE);
        final RealmResults<AnnotationIndexTypes> distinctString = realm.where(AnnotationIndexTypes.class).findAll()
                .distinctAsync(AnnotationIndexTypes.FIELD_NOT_INDEX_STRING);

        assertFalse(distinctBool.isLoaded());
        assertTrue(distinctBool.isValid());
        assertTrue(distinctBool.isEmpty());

        assertFalse(distinctLong.isLoaded());
        assertTrue(distinctLong.isValid());
        assertTrue(distinctLong.isEmpty());

        assertFalse(distinctDate.isLoaded());
        assertTrue(distinctDate.isValid());
        assertTrue(distinctDate.isEmpty());

        assertFalse(distinctString.isLoaded());
        assertTrue(distinctString.isValid());
        assertTrue(distinctString.isEmpty());

        final Runnable endTest = new Runnable() {
            @Override
            public void run() {
                if (changeListenerCalled.decrementAndGet() == 0) {
                    looperThread.testComplete();
                }
            }
        };

        looperThread.keepStrongReference(distinctBool);
        looperThread.keepStrongReference(distinctLong);
        looperThread.keepStrongReference(distinctDate);
        looperThread.keepStrongReference(distinctString);
        distinctBool.addChangeListener(new RealmChangeListener<RealmResults<AnnotationIndexTypes>>() {
            @Override
            public void onChange(RealmResults<AnnotationIndexTypes> object) {
                assertEquals(2, distinctBool.size());
                endTest.run();
            }
        });

        distinctLong.addChangeListener(new RealmChangeListener<RealmResults<AnnotationIndexTypes>>() {
            @Override
            public void onChange(RealmResults<AnnotationIndexTypes> object) {
                assertEquals(numberOfBlocks, distinctLong.size());
                endTest.run();
            }
        });

        distinctDate.addChangeListener(new RealmChangeListener<RealmResults<AnnotationIndexTypes>>() {
            @Override
            public void onChange(RealmResults<AnnotationIndexTypes> object) {
                assertEquals(numberOfBlocks, distinctDate.size());
                endTest.run();
            }
        });

        distinctString.addChangeListener(new RealmChangeListener<RealmResults<AnnotationIndexTypes>>() {
            @Override
            public void onChange(RealmResults<AnnotationIndexTypes> object) {
                assertEquals(numberOfBlocks, distinctString.size());
                endTest.run();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void distinctAsync_doesNotExist() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        try {
            realm.where(AnnotationIndexTypes.class).findAll().distinctAsync("doesNotExist");
        } catch (IllegalArgumentException ignored) {
        }
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void distinctAsync_invalidTypes() {
        populateTestRealm(realm, TEST_DATA_SIZE);

        for (String field : new String[]{AllTypes.FIELD_REALMOBJECT, AllTypes.FIELD_REALMLIST, AllTypes.FIELD_DOUBLE, AllTypes.FIELD_FLOAT}) {
            try {
                realm.where(AllTypes.class).findAll().distinctAsync(field);
            } catch (IllegalArgumentException ignored) {
            }
        }
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void distinctAsync_indexedLinkedFields() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        for (String field : AnnotationIndexTypes.INDEX_FIELDS) {
            try {
                realm.where(AnnotationIndexTypes.class).findAll().distinctAsync(AnnotationIndexTypes.FIELD_OBJECT + "." + field);
                fail("Unsupported " + field + " linked field");
            } catch (IllegalArgumentException ignored) {
            }
        }
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void distinctAsync_notIndexedLinkedFields() {
        populateForDistinctInvalidTypesLinked(realm);

        try {
            realm.where(AllJavaTypes.class).findAll().distinctAsync(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_BINARY);
        } catch (IllegalArgumentException ignored) {
        }
        looperThread.testComplete();
    }

    @Test
    public void distinctMultiArgs() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // Must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        RealmResults<AnnotationIndexTypes> results = realm.where(AnnotationIndexTypes.class).findAll();
        RealmResults<AnnotationIndexTypes> distinctMulti = results.distinct(AnnotationIndexTypes.FIELD_INDEX_BOOL, AnnotationIndexTypes.INDEX_FIELDS);
        assertEquals(numberOfBlocks, distinctMulti.size());
    }

    @Test
    public void distinctMultiArgs_switchedFieldsOrder() {
        final long numberOfBlocks = 25;
        TestHelper.populateForDistinctFieldsOrder(realm, numberOfBlocks);

        // Regardless of the block size defined above, the output size is expected to be the same, 4 in this case, due to receiving unique combinations of tuples.
        RealmResults<AnnotationIndexTypes> results = realm.where(AnnotationIndexTypes.class).findAll();
        RealmResults<AnnotationIndexTypes> distinctStringLong = results.distinct(AnnotationIndexTypes.FIELD_INDEX_STRING, AnnotationIndexTypes.FIELD_INDEX_LONG);
        RealmResults<AnnotationIndexTypes> distinctLongString = results.distinct(AnnotationIndexTypes.FIELD_INDEX_LONG, AnnotationIndexTypes.FIELD_INDEX_STRING);
        assertEquals(4, distinctStringLong.size());
        assertEquals(4, distinctLongString.size());
        assertEquals(distinctStringLong.size(), distinctLongString.size());
    }

    @Test
    public void distinctMultiArgs_emptyField() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        RealmResults<AnnotationIndexTypes> results = realm.where(AnnotationIndexTypes.class).findAll();
        // An empty string field in the middle.
        try {
            results.distinct(AnnotationIndexTypes.FIELD_INDEX_BOOL, "", AnnotationIndexTypes.FIELD_INDEX_INT);
        } catch (IllegalArgumentException ignored) {
        }
        // An empty string field at the end.
        try {
            results.distinct(AnnotationIndexTypes.FIELD_INDEX_BOOL, AnnotationIndexTypes.FIELD_INDEX_INT, "");
        } catch (IllegalArgumentException ignored) {
        }
        // A null string field in the middle.
        try {
            results.distinct(AnnotationIndexTypes.FIELD_INDEX_BOOL, null, AnnotationIndexTypes.FIELD_INDEX_INT);
        } catch (IllegalArgumentException ignored) {
        }
        // A null string field at the end.
        try {
            results.distinct(AnnotationIndexTypes.FIELD_INDEX_BOOL, AnnotationIndexTypes.FIELD_INDEX_INT, null);
        } catch (IllegalArgumentException ignored) {
        }
        // (String) Null makes varargs a null array.
        try {
            results.distinct(AnnotationIndexTypes.FIELD_INDEX_BOOL, (String)null);
        } catch (IllegalArgumentException ignored) {
        }
        // Two (String) null for first and varargs fields.
        try {
            results.distinct(null, (String) null);
        } catch (IllegalArgumentException ignored) {
        }
        // "" & (String)null combination.
        try {
            results.distinct("", (String) null);
        } catch (IllegalArgumentException ignored) {
        }
        // "" & (String)null combination.
        try {
            results.distinct(null, "");
        } catch (IllegalArgumentException ignored) {
        }
        // Two empty fields tests.
        try {
            results.distinct("", "");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctMultiArgs_withNullValues() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        RealmResults<AnnotationIndexTypes> results = realm.where(AnnotationIndexTypes.class).findAll();
        RealmResults<AnnotationIndexTypes> distinctMulti = results.distinct(AnnotationIndexTypes.FIELD_INDEX_DATE, AnnotationIndexTypes.FIELD_INDEX_STRING);
        assertEquals(1, distinctMulti.size());
    }

    @Test
    public void distinctMultiArgs_notIndexedFields() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        RealmResults<AnnotationIndexTypes> results = realm.where(AnnotationIndexTypes.class).findAll();
        try {
            results.distinct(AnnotationIndexTypes.FIELD_NOT_INDEX_STRING, AnnotationIndexTypes.NOT_INDEX_FIELDS);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctMultiArgs_doesNotExistField() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        RealmResults<AnnotationIndexTypes> results = realm.where(AnnotationIndexTypes.class).findAll();
        try {
            results.distinct(AnnotationIndexTypes.FIELD_INDEX_INT, AnnotationIndexTypes.NONEXISTANT_MIX_FIELDS);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctMultiArgs_invalidTypesFields() {
        populateTestRealm();

        RealmResults<AnnotationIndexTypes> results = realm.where(AnnotationIndexTypes.class).findAll();
        try {
            results.distinct(AllTypes.FIELD_REALMOBJECT, AllTypes.INVALID_TYPES_FIELDS_FOR_DISTINCT);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctMultiArgs_indexedLinkedFields() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        RealmResults<AnnotationIndexTypes> results = realm.where(AnnotationIndexTypes.class).findAll();
        try {
            results.distinct(AnnotationIndexTypes.INDEX_LINKED_FIELD_STRING, AnnotationIndexTypes.INDEX_LINKED_FIELDS);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctMultiArgs_notIndexedLinkedFields() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        RealmResults<AnnotationIndexTypes> results = realm.where(AnnotationIndexTypes.class).findAll();
        try {
            results.distinct(AnnotationIndexTypes.NOT_INDEX_LINKED_FILED_STRING, AnnotationIndexTypes.NOT_INDEX_LINKED_FIELDS);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctMultiArgs_invalidTypesLinkedFields() {
        populateForDistinctInvalidTypesLinked(realm);

        RealmResults<AnnotationIndexTypes> results = realm.where(AnnotationIndexTypes.class).findAll();
        try {
            results.distinct(AllJavaTypes.INVALID_LINKED_BINARY_FIELD_FOR_DISTINCT, AllJavaTypes.INVALID_LINKED_TYPES_FIELDS_FOR_DISTINCT);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private RealmResults<Dog> populateRealmResultsOnLinkView(Realm realm) {
        realm.beginTransaction();
        Owner owner = realm.createObject(Owner.class);
        for (int i = 0; i < 10; i++) {
            Dog dog = new Dog();
            dog.setName("name_" + i);
            dog.setOwner(owner);
            dog.setAge(i);
            dog.setBirthday(new Date(i));
            owner.getDogs().add(dog);
        }
        realm.commitTransaction();


        return owner.getDogs().where().lessThan(Dog.FIELD_AGE, 5).findAll();
    }

    // If a RealmResults is built on a link view, when the link view is deleted on the same thread, within the same
    // event loop, the RealmResults stays without changes since it is detached until the next event loop. In the next
    // event loop, the results will be empty because of the parent link view is deleted.
    // 1. Create results from link view.
    // 2. Delete the parent link view by a local transaction.
    // 3. Within the same event loop, the results stays the same.
    // 4. The results change listener called, the results becomes empty.
    @Test
    @RunTestInLooperThread
    public void accessors_resultsBuiltOnDeletedLinkView_deletionAsALocalCommit() {
        Realm realm = looperThread.getRealm();
        // Step 1
        RealmResults<Dog> dogs = populateRealmResultsOnLinkView(realm);
        looperThread.keepStrongReference(dogs);
        dogs.addChangeListener(new RealmChangeListener<RealmResults<Dog>>() {
            @Override
            public void onChange(RealmResults<Dog> dogs) {
                // Step 4.
                // The results is still valid, but empty.
                assertEquals(true, dogs.isValid());
                assertEquals(true, dogs.isEmpty());
                assertEquals(0, dogs.size());
                try {
                    dogs.first();
                    fail();
                } catch (IndexOutOfBoundsException ignored) {
                }

                assertEquals(0, dogs.sum(Dog.FIELD_AGE).intValue());
                assertEquals(0f, dogs.sum(Dog.FIELD_HEIGHT).floatValue(), 0f);
                assertEquals(0d, dogs.sum(Dog.FIELD_WEIGHT).doubleValue(), 0d);
                assertEquals(0d, dogs.average(Dog.FIELD_AGE), 0d);
                assertEquals(0d, dogs.average(Dog.FIELD_HEIGHT), 0d);
                assertEquals(0d, dogs.average(Dog.FIELD_WEIGHT), 0d);
                assertEquals(null, dogs.min(Dog.FIELD_AGE));
                assertEquals(null, dogs.max(Dog.FIELD_AGE));
                assertEquals(null, dogs.minDate(Dog.FIELD_BIRTHDAY));
                assertEquals(null, dogs.maxDate(Dog.FIELD_BIRTHDAY));

                assertEquals(0, dogs.where().findAll().size());

                looperThread.testComplete();
            }
        });

        // Step 2
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Owner.class).findAll().deleteAllFromRealm();
            }
        });

        // Step 3
        assertEquals(true, dogs.isValid());
        assertEquals(0, dogs.size());
        // The link view has been deleted.
        assertEquals(0, dogs.where().findAll().size());
    }

    // If a RealmResults is built on a link view, when the link view is deleted on a remote thread, within the same
    // event loop, the RealmResults stays without changes since the Realm version doesn't change. In the next
    // event loop, the results will be empty because of the parent link view is deleted.
    // 1. Create results from link view.
    // 2. Delete the parent link view by a remote transaction.
    // 3. Within the same event loop, the results stays the same.
    // 4. The results change listener called, the results becomes empty.
    @Test
    @RunTestInLooperThread
    public void accessors_resultsBuiltOnDeletedLinkView_deletionAsARemoteCommit() {
        // Step 1
        Realm realm = looperThread.getRealm();
        RealmResults<Dog> dogs = populateRealmResultsOnLinkView(realm);
        looperThread.keepStrongReference(dogs);
        dogs.addChangeListener(new RealmChangeListener<RealmResults<Dog>>() {
            @Override
            public void onChange(RealmResults<Dog> dogs) {
                // Step 4
                // The results is still valid, but empty.
                assertEquals(true, dogs.isValid());
                assertEquals(true, dogs.isEmpty());
                assertEquals(0, dogs.size());
                try {
                    dogs.first();
                    fail();
                } catch (IndexOutOfBoundsException ignored) {
                }

                assertEquals(0, dogs.sum(Dog.FIELD_AGE).intValue());
                assertEquals(0f, dogs.sum(Dog.FIELD_HEIGHT).floatValue(), 0f);
                assertEquals(0d, dogs.sum(Dog.FIELD_WEIGHT).doubleValue(), 0d);
                assertEquals(0d, dogs.average(Dog.FIELD_AGE), 0d);
                assertEquals(0d, dogs.average(Dog.FIELD_HEIGHT), 0d);
                assertEquals(0d, dogs.average(Dog.FIELD_WEIGHT), 0d);
                assertEquals(null, dogs.min(Dog.FIELD_AGE));
                assertEquals(null, dogs.max(Dog.FIELD_AGE));
                assertEquals(null, dogs.minDate(Dog.FIELD_BIRTHDAY));
                assertEquals(null, dogs.maxDate(Dog.FIELD_BIRTHDAY));

                assertEquals(0, dogs.where().findAll().size());

                looperThread.testComplete();
            }
        });


        // Step 2
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Owner.class).findAll().deleteAllFromRealm();
            }
        });

        // Step 3
        assertEquals(true, dogs.isValid());
        assertEquals(5, dogs.size());
        // The link view still exists
        assertEquals(5, dogs.where().findAll().size());
    }

    @Test
    @RunTestInLooperThread
    public void addChangeListener() {
        Realm realm = looperThread.getRealm();
        RealmResults<AllTypes> collection = realm.where(AllTypes.class).findAll();

        looperThread.keepStrongReference(collection);
        collection.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                looperThread.testComplete();
            }
        });

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void addChangeListener_twice() {
        final AtomicInteger listenersTriggered = new AtomicInteger(0);
        final Realm realm = looperThread.getRealm();
        RealmResults<AllTypes> collection = realm.where(AllTypes.class).findAll();

        RealmChangeListener<RealmResults<AllTypes>> listener = new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                listenersTriggered.incrementAndGet();
            }
        };

        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                listenersTriggered.incrementAndGet();
                looperThread.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (listenersTriggered.get() == 1) {
                            looperThread.testComplete();
                        } else {
                            fail("Only global listener should be triggered");
                        }
                    }
                });
            }
        });

        // Adding it twice will be ignored, so removing it will not cause the listener to be triggered.
        looperThread.keepStrongReference(collection);
        collection.addChangeListener(listener);
        collection.addChangeListener(listener);
        collection.removeChangeListener(listener);

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();
    }

    @Test
    @UiThreadTest
    public void addChangeListener_null() {
        try {
            collection.addChangeListener((RealmChangeListener<RealmResults<AllTypes>>) null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    @RunTestInLooperThread
    public void removeChangeListener() {
        final AtomicInteger listenersTriggered = new AtomicInteger(0);
        final Realm realm = looperThread.getRealm();
        RealmResults<AllTypes> collection = realm.where(AllTypes.class).findAll();

        RealmChangeListener<RealmResults<AllTypes>> listener = new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                listenersTriggered.incrementAndGet();
            }
        };

        looperThread.keepStrongReference(collection);
        collection.addChangeListener(listener);
        collection.removeChangeListener(listener);

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        // The above commit should have put a REALM_CHANGED event on the Looper queue before this runnable.
        looperThread.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (listenersTriggered.get() == 0) {
                    looperThread.testComplete();
                } else {
                    fail("Listener wasn't removed");
                }
            }
        });
    }

    @Test
    @UiThreadTest
    public void removeChangeListener_null() {
        try {
            collection.removeChangeListener((RealmChangeListener) null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    @RunTestInLooperThread
    public void removeAllChangeListeners() {
        final AtomicInteger listenersTriggered = new AtomicInteger(0);
        final Realm realm = looperThread.getRealm();
        RealmResults<AllTypes> collection = realm.where(AllTypes.class).findAll();

        RealmChangeListener<RealmResults<AllTypes>> listenerA = new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                listenersTriggered.incrementAndGet();
            }
        };
        RealmChangeListener<RealmResults<AllTypes>> listenerB = new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                listenersTriggered.incrementAndGet();
            }
        };

        looperThread.keepStrongReference(collection);
        collection.addChangeListener(listenerA);
        collection.addChangeListener(listenerB);
        collection.removeAllChangeListeners();

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        // The above commit should have put a REALM_CHANGED event on the Looper queue before this runnable.
        looperThread.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (listenersTriggered.get() == 0) {
                    looperThread.testComplete();
                } else {
                    fail("Listeners wasn't removed");
                }
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void removeAllChangeListeners_thenAdd() {
        final Realm realm = looperThread.getRealm();
        RealmResults<AllTypes> collection = realm.where(AllTypes.class).findAll();

        collection.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> element) {
                fail();
            }
        });
        collection.removeAllChangeListeners();

        collection.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> results) {
                assertEquals(1, results.size());
                looperThread.testComplete();
            }
        });

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();
    }

    @Test
    public void deleteAndDeleteAll() {
        realm.beginTransaction();
        for (int i = 0; i < 10; i++) {
            StringOnly stringOnly = realm.createObject(StringOnly.class);
            stringOnly.setChars("String " + i);
        }
        realm.commitTransaction();

        RealmResults<StringOnly> stringOnlies = realm.where(StringOnly.class).findAll();

        realm.beginTransaction();
        // Removes one object.
        stringOnlies.get(0).deleteFromRealm();
        realm.commitTransaction();

        realm.beginTransaction();
        // Removes the rest.
        stringOnlies.deleteAllFromRealm();
        realm.commitTransaction();

        assertEquals(0, realm.where(StringOnly.class).findAll().size());
    }

    @Test
    public void syncQuery_defaultValuesAreIgnored() {
        final String fieldIgnoredValue = DefaultValueOfField.FIELD_IGNORED_DEFAULT_VALUE + ".modified";
        final String fieldStringValue = DefaultValueOfField.FIELD_STRING_DEFAULT_VALUE + ".modified";
        final String fieldRandomStringValue = "non-random";
        final short fieldShortValue = (short) (DefaultValueOfField.FIELD_SHORT_DEFAULT_VALUE + 1);
        final int fieldIntValue = DefaultValueOfField.FIELD_INT_DEFAULT_VALUE + 1;
        final long fieldLongPrimaryKeyValue = DefaultValueOfField.FIELD_LONG_PRIMARY_KEY_DEFAULT_VALUE + 1;
        final long fieldLongValue = DefaultValueOfField.FIELD_LONG_DEFAULT_VALUE + 1;
        final byte fieldByteValue = (byte) (DefaultValueOfField.FIELD_BYTE_DEFAULT_VALUE + 1);
        final float fieldFloatValue = DefaultValueOfField.FIELD_FLOAT_DEFAULT_VALUE + 1;
        final double fieldDoubleValue = DefaultValueOfField.FIELD_DOUBLE_DEFAULT_VALUE + 1;
        final boolean fieldBooleanValue = !DefaultValueOfField.FIELD_BOOLEAN_DEFAULT_VALUE;
        final Date fieldDateValue = new Date(DefaultValueOfField.FIELD_DATE_DEFAULT_VALUE.getTime() + 1);
        final byte[] fieldBinaryValue = {(byte) (DefaultValueOfField.FIELD_BINARY_DEFAULT_VALUE[0] - 1)};
        final int fieldObjectIntValue = RandomPrimaryKey.FIELD_INT_DEFAULT_VALUE + 1;
        final int fieldListIntValue = RandomPrimaryKey.FIELD_INT_DEFAULT_VALUE + 2;

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                final DefaultValueOfField obj = new DefaultValueOfField();
                obj.setFieldIgnored(fieldIgnoredValue);
                obj.setFieldString(fieldStringValue);
                obj.setFieldRandomString(fieldRandomStringValue);
                obj.setFieldShort(fieldShortValue);
                obj.setFieldInt(fieldIntValue);
                obj.setFieldLongPrimaryKey(fieldLongPrimaryKeyValue);
                obj.setFieldLong(fieldLongValue);
                obj.setFieldByte(fieldByteValue);
                obj.setFieldFloat(fieldFloatValue);
                obj.setFieldDouble(fieldDoubleValue);
                obj.setFieldBoolean(fieldBooleanValue);
                obj.setFieldDate(fieldDateValue);
                obj.setFieldBinary(fieldBinaryValue);

                final RandomPrimaryKey fieldObjectValue = new RandomPrimaryKey();
                fieldObjectValue.setFieldInt(fieldObjectIntValue);
                obj.setFieldObject(fieldObjectValue);

                final RealmList<RandomPrimaryKey> list = new RealmList<RandomPrimaryKey>();
                final RandomPrimaryKey listItem = new RandomPrimaryKey();
                listItem.setFieldInt(fieldListIntValue);
                list.add(listItem);
                obj.setFieldList(list);

                realm.copyToRealm(obj);
            }
        });

        final RealmResults<DefaultValueOfField> result = realm.where(DefaultValueOfField.class)
                .equalTo(DefaultValueOfField.FIELD_LONG_PRIMARY_KEY,
                        fieldLongPrimaryKeyValue).findAll();

        final DefaultValueOfField obj = result.first();

        assertEquals(DefaultValueOfField.FIELD_IGNORED_DEFAULT_VALUE/*not fieldIgnoredValue*/,
                obj.getFieldIgnored());
        assertEquals(fieldStringValue, obj.getFieldString());
        assertEquals(fieldRandomStringValue, obj.getFieldRandomString());
        assertEquals(fieldShortValue, obj.getFieldShort());
        assertEquals(fieldIntValue, obj.getFieldInt());
        assertEquals(fieldLongPrimaryKeyValue, obj.getFieldLongPrimaryKey());
        assertEquals(fieldLongValue, obj.getFieldLong());
        assertEquals(fieldByteValue, obj.getFieldByte());
        assertEquals(fieldFloatValue, obj.getFieldFloat(), 0f);
        assertEquals(fieldDoubleValue, obj.getFieldDouble(), 0d);
        assertEquals(fieldBooleanValue, obj.isFieldBoolean());
        assertEquals(fieldDateValue, obj.getFieldDate());
        assertTrue(Arrays.equals(fieldBinaryValue, obj.getFieldBinary()));
        assertEquals(fieldObjectIntValue, obj.getFieldObject().getFieldInt());
        assertEquals(1, obj.getFieldList().size());
        assertEquals(fieldListIntValue, obj.getFieldList().first().getFieldInt());
    }
}
