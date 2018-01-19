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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.AllTypes;
import io.realm.entities.AnnotationIndexTypes;
import io.realm.entities.Dog;
import io.realm.entities.StringOnly;
import io.realm.internal.Table;
import io.realm.internal.UncheckedRow;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class SortTest {
    private Realm realm;

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    private Context context;
    private RealmConfiguration realmConfig;

    private final static String FIELD_STRING = "columnString";
    private final static String FIELD_LONG = "columnLong";

    private final static String[] ORDER_STRING_INT = {FIELD_STRING, FIELD_LONG};
    private final static String[] ORDER_INT_STRING = {FIELD_LONG, FIELD_STRING};

    private final static Sort[] ORDER_ASC_ASC = {Sort.ASCENDING, Sort.ASCENDING};
    private final static Sort[] ORDER_ASC_DES = {Sort.ASCENDING, Sort.DESCENDING};

    private static String chars;
    private int numberOfPermutations;

    private void populateRealm(Realm realm) {
        realm.beginTransaction();

        realm.delete(AllTypes.class);
        AllTypes object1 = realm.createObject(AllTypes.class);
        object1.setColumnLong(5);
        object1.setColumnString("Adam");
        object1.setColumnRealmObject(realm.copyToRealm(new Dog("D")));

        AllTypes object2 = realm.createObject(AllTypes.class);
        object2.setColumnLong(4);
        object2.setColumnString("Brian");
        object2.setColumnRealmObject(realm.copyToRealm(new Dog("C")));

        AllTypes object3 = realm.createObject(AllTypes.class);
        object3.setColumnLong(4);
        object3.setColumnString("Adam");
        object3.setColumnRealmObject(realm.copyToRealm(new Dog("B")));

        AllTypes object4 = realm.createObject(AllTypes.class);
        object4.setColumnLong(5);
        object4.setColumnString("Adam");
        object4.setColumnRealmObject(realm.copyToRealm(new Dog("A")));

        realm.delete(AnnotationIndexTypes.class);
        AnnotationIndexTypes obj1 = realm.createObject(AnnotationIndexTypes.class);
        obj1.setIndexLong(1);
        obj1.setIndexInt(1);
        obj1.setIndexString("A");

        AnnotationIndexTypes obj2 = realm.createObject(AnnotationIndexTypes.class);
        obj2.setIndexLong(2);
        obj2.setIndexInt(1);
        obj2.setIndexString("B");

        AnnotationIndexTypes obj3 = realm.createObject(AnnotationIndexTypes.class);
        obj3.setIndexLong(3);
        obj3.setIndexInt(1);
        obj3.setIndexString("C");

        realm.commitTransaction();
    }

    private UncheckedRow getRowBySourceIndexFromAllTypesTable(long sourceRowIndex) {
        Table table = realm.getTable(AllTypes.class);
        return table.getUncheckedRow(sourceRowIndex);
    }

    @Before
    public void setUp() {
        // Creates a Realm with the following objects:
        // 0: (5, "Adam", Dog("D"))
        // 1: (4, "Brian", Dog("C"))
        // 2: (4, "Adam", Dog("B"))
        // 3: (5, "Adam", Dog("A"))

        // Injecting the Instrumentation instance is required
        // for your test to run with AndroidJUnitRunner.
        context = InstrumentationRegistry.getInstrumentation().getContext();
        realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);

        populateRealm(realm);
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    @Test
    public void sortMultiFailures() {
        RealmResults<AllTypes> allTypes = realm.where(AllTypes.class).findAll();

        // Zero fields specified.
        try {
            allTypes.sort(new String[]{}, new Sort[]{});
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // Number of fields and sorting orders don't match.
        try {
            allTypes.sort(new String[]{FIELD_STRING}, ORDER_ASC_ASC);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // Null is not allowed.
        try {
            allTypes.sort(null, (Sort[]) null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            allTypes.sort(new String[]{FIELD_STRING}, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // Non-existing field name.
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
        assertEquals(0, results.getOsResults().indexOf(getRowBySourceIndexFromAllTypesTable(2)));

        assertEquals("Adam", results.get(1).getColumnString());
        assertEquals(5, results.get(1).getColumnLong());
        assertEquals(1, results.getOsResults().indexOf(getRowBySourceIndexFromAllTypesTable(0)));

        assertEquals("Adam", results.get(2).getColumnString());
        assertEquals(5, results.get(2).getColumnLong());
        assertEquals(2, results.getOsResults().indexOf(getRowBySourceIndexFromAllTypesTable(3)));

        assertEquals("Brian", results.get(3).getColumnString());
        assertEquals(4, results.get(3).getColumnLong());
        assertEquals(3, results.getOsResults().indexOf(getRowBySourceIndexFromAllTypesTable(1)));
    }

    private void checkSortTwoFieldsIntString(RealmResults<AllTypes> results) {
        // Sorted Long (ascending), String (descending).
        // Expected output:
        // (4, "Adam"), row index = 2
        // (4, "Brian"), row index = 1
        // (5, "Adam"), row index = 0 - stable sort!
        // (5, "Adam"), row index = 3
        assertEquals(4, results.size());

        assertEquals("Adam", results.get(0).getColumnString());
        assertEquals(4, results.get(0).getColumnLong());
        assertEquals(0, results.getOsResults().indexOf(getRowBySourceIndexFromAllTypesTable(2)));

        assertEquals("Brian", results.get(1).getColumnString());
        assertEquals(4, results.get(1).getColumnLong());
        assertEquals(1, results.getOsResults().indexOf(getRowBySourceIndexFromAllTypesTable(1)));

        assertEquals("Adam", results.get(2).getColumnString());
        assertEquals(5, results.get(2).getColumnLong());
        assertEquals(2, results.getOsResults().indexOf(getRowBySourceIndexFromAllTypesTable(0)));

        assertEquals("Adam", results.get(3).getColumnString());
        assertEquals(5, results.get(3).getColumnLong());
        assertEquals(3, results.getOsResults().indexOf(getRowBySourceIndexFromAllTypesTable(3)));
    }

    private void checkSortTwoFieldsIntAscendingStringDescending(RealmResults<AllTypes> results) {
        // Sorted Long (ascending), String (descending).
        // Expected output:
        // (4, "Brian"), row index = 1
        // (4, "Adam"), row index = 2
        // (5, "Adam"), row index = 0 - stable sort!
        // (5, "Adam"), row index = 3
        assertEquals(4, results.size());

        assertEquals("Brian", results.get(0).getColumnString());
        assertEquals(4, results.get(0).getColumnLong());
        assertEquals(0, results.getOsResults().indexOf(getRowBySourceIndexFromAllTypesTable(1)));

        assertEquals("Adam", results.get(1).getColumnString());
        assertEquals(4, results.get(1).getColumnLong());
        assertEquals(1, results.getOsResults().indexOf(getRowBySourceIndexFromAllTypesTable(2)));

        assertEquals("Adam", results.get(2).getColumnString());
        assertEquals(5, results.get(2).getColumnLong());
        assertEquals(2, results.getOsResults().indexOf(getRowBySourceIndexFromAllTypesTable(0)));

        assertEquals("Adam", results.get(3).getColumnString());
        assertEquals(5, results.get(3).getColumnLong());
        assertEquals(3, results.getOsResults().indexOf(getRowBySourceIndexFromAllTypesTable(3)));
    }

    private void checkSortTwoFieldsStringAscendingIntDescending(RealmResults<AllTypes> results) {
        // Sorted String (ascending), Long (descending).
        // Expected output:
        // (5, "Adam"), row index = 0 - stable sort!
        // (5, "Adam"), row index = 3
        // (4, "Adam"), row index = 2
        // (5, "Brian"), row index = 1
        assertEquals(4, results.size());

        assertEquals("Adam", results.get(0).getColumnString());
        assertEquals(5, results.get(0).getColumnLong());
        assertEquals(0, results.getOsResults().indexOf(getRowBySourceIndexFromAllTypesTable(0)));

        assertEquals("Adam", results.get(1).getColumnString());
        assertEquals(5, results.get(1).getColumnLong());
        assertEquals(1, results.getOsResults().indexOf(getRowBySourceIndexFromAllTypesTable(3)));

        assertEquals("Adam", results.get(2).getColumnString());
        assertEquals(4, results.get(2).getColumnLong());
        assertEquals(2, results.getOsResults().indexOf(getRowBySourceIndexFromAllTypesTable(2)));

        assertEquals("Brian", results.get(3).getColumnString());
        assertEquals(4, results.get(3).getColumnLong());
        assertEquals(3, results.getOsResults().indexOf(getRowBySourceIndexFromAllTypesTable(1)));
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

        // Zero fields specified.
        try {
            realm.where(AllTypes.class).findAll().sort(new String[]{}, new Sort[]{});
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // Number of fields and sorting orders don't match.
        try {
            realm.where(AllTypes.class).findAll().
                    sort(new String[]{FIELD_STRING}, ORDER_ASC_ASC);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // Null is not allowed.
        try {
            realm.where(AllTypes.class).findAll().sort(null, (Sort[]) null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            realm.where(AllTypes.class).findAll().sort(new String[]{FIELD_STRING}, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // Non-existing field name.
        try {
            realm.where(AllTypes.class).findAll().
                    sort(new String[]{FIELD_STRING, "dont-exist"}, ORDER_ASC_ASC);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    @RunTestInLooperThread
    public void resorting() throws InterruptedException {
        final AtomicInteger changeListenerCalled = new AtomicInteger(4);

        final Realm realm = looperThread.getRealm();
        realm.setAutoRefresh(true);

        final Runnable endTest = new Runnable() {
            @Override
            public void run() {
                if (changeListenerCalled.decrementAndGet() == 0) {
                    realm.close();
                    looperThread.testComplete();
                }
            }
        };

        // 0: (5, "Adam")
        // 1: (4, "Brian")
        // 2: (4, "Adam")
        // 3: (5, "Adam")
        populateRealm(realm);

        // rr0: [0, 1, 2, 3]
        final RealmResults<AllTypes> rr0 = realm.where(AllTypes.class).findAll();
        looperThread.keepStrongReference(rr0);
        rr0.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> element) {
                // After commit: [0, 1, 2, 3, 4] - most likely as order isn't guaranteed.
                assertEquals(5, element.size());
                endTest.run();
            }
        });

        // rr1: [1, 2, 0, 3]
        final RealmResults<AllTypes> rr1 = realm.where(AllTypes.class).findAll().sort(FIELD_LONG, Sort.ASCENDING);
        looperThread.keepStrongReference(rr1);
        rr1.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> element) {
                // After commit: [1, 2, 0, 3, 4]
                assertEquals(4, element.first().getColumnLong());
                assertEquals(6, element.last().getColumnLong());
                assertEquals(5, element.size());
                endTest.run();
            }
        });
        assertEquals(4, rr1.first().getColumnLong());
        assertEquals(5, rr1.last().getColumnLong());

        // rr2: [0, 3, 1, 2]
        final RealmResults<AllTypes> rr2 = realm.where(AllTypes.class).findAll().sort(FIELD_LONG, Sort.DESCENDING);
        looperThread.keepStrongReference(rr2);
        rr2.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> element) {
                // After commit: [4, 0, 3, 1, 2]
                assertEquals(6, element.first().getColumnLong());
                assertEquals(4, element.last().getColumnLong());
                assertEquals(5, element.size());
                endTest.run();
            }
        });
        assertEquals(5, rr2.first().getColumnLong());
        assertEquals(4, rr2.last().getColumnLong());

        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm element) {
                assertEquals(5, element.where(AllTypes.class).findAll().size());
                endTest.run();
            }
        });

        // After commit:
        // 0: (5, "Adam")
        // 1: (4, "Brian")
        // 2: (4, "Adam")
        // 3: (5, "Adam")
        // 4: (6, "")
        realm.beginTransaction();
        AllTypes allTypes = realm.createObject(AllTypes.class);
        allTypes.setColumnLong(6);
        realm.commitTransaction();
    }

    private void populateDates(Realm realm, int n) {
        realm.beginTransaction();
        realm.delete(AllTypes.class);
        for (int i = 0; i < n; i++) {
            AllTypes allTypes = realm.createObject(AllTypes.class);
            allTypes.setColumnDate(new Date(i));
        }
        realm.commitTransaction();
    }

    @Test
    public void sortingDates() {
        final int TEST_SIZE = 10;

        populateDates(realm, TEST_SIZE);

        RealmResults<AllTypes> objectsAscending = realm.where(AllTypes.class).sort(AllTypes.FIELD_DATE, Sort.ASCENDING).findAll();
        assertEquals(TEST_SIZE, objectsAscending.size());
        int i = 0;
        for (AllTypes allTypes : objectsAscending) {
            assertEquals(new Date(i), allTypes.getColumnDate());
            i++;
        }

        RealmResults<AllTypes> objectsDescending = realm.where(AllTypes.class).sort(AllTypes.FIELD_DATE, Sort.DESCENDING).findAll();
        assertEquals(TEST_SIZE, objectsDescending.size());
        i = TEST_SIZE - 1;
        for (AllTypes allTypes : objectsDescending) {
            assertEquals(new Date(i), allTypes.getColumnDate());
            i--;
        }
    }

    @Test
    @RunTestInLooperThread
    public void resortingDates() {
        final int TEST_SIZE = 10;
        final AtomicInteger changeListenerCalled = new AtomicInteger(2);

        final Realm realm = Realm.getInstance(looperThread.createConfiguration());
        realm.setAutoRefresh(true);
        populateDates(realm, TEST_SIZE);

        final Runnable endTest = new Runnable() {
            @Override
            public void run() {
                if (changeListenerCalled.decrementAndGet() == 0) {
                    realm.close();
                    looperThread.testComplete();
                }
            }
        };

        RealmResults<AllTypes> objectsAscending = realm.where(AllTypes.class).sort(AllTypes.FIELD_DATE, Sort.ASCENDING).findAll();
        assertEquals(TEST_SIZE, objectsAscending.size());
        looperThread.keepStrongReference(objectsAscending);
        objectsAscending.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> element) {
                assertEquals(TEST_SIZE + 1, element.size());
                int i = 0;
                for (AllTypes allTypes : element) {
                    assertEquals(new Date(i), allTypes.getColumnDate());
                    i++;
                }
                endTest.run();
            }
        });

        RealmResults<AllTypes> objectsDescending = realm.where(AllTypes.class).sort(AllTypes.FIELD_DATE, Sort.DESCENDING).findAll();
        assertEquals(TEST_SIZE, objectsDescending.size());
        looperThread.keepStrongReference(objectsDescending);
        objectsDescending.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> element) {
                assertEquals(TEST_SIZE + 1, element.size());
                int i = element.size() - 1;
                for (AllTypes allTypes : element) {
                    assertEquals(new Date(i), allTypes.getColumnDate());
                    i--;
                }
                endTest.run();
            }
        });

        realm.beginTransaction();
        AllTypes allTypes = realm.createObject(AllTypes.class);
        allTypes.setColumnDate(new Date(TEST_SIZE));
        realm.commitTransaction();
    }

    @Test
    public void sortByLongDistinctByInt() {
        // Before sorting:
        // (FIELD_INDEX_LONG, FIELD_INDEX_INT, FIELD_INDEX_STRING)
        // (1, 1, "A")
        // (2, 1, "B")
        // (3, 1, "C")
        // After sorting
        // (3, 1, "C")
        // (2, 1, "B")
        // (1, 1, "A)
        RealmResults<AnnotationIndexTypes> results1 = realm.where(AnnotationIndexTypes.class)
                .sort(AnnotationIndexTypes.FIELD_INDEX_LONG, Sort.DESCENDING)
                .findAll();
        assertEquals(3, results1.size());
        assertEquals(3, results1.get(0).getIndexLong());

        // After distinct:
        // (3, 1, "C")
        RealmResults<AnnotationIndexTypes> results2 =  results1.where().distinct(AnnotationIndexTypes.FIELD_INDEX_INT).findAll();
        assertEquals(1, results2.size());
        assertEquals("C", results2.get(0).getIndexString());
        assertEquals(3, results2.get(0).getIndexLong());
    }

    @Test
    public void sortAndDistinctMixed() {
        // Dataset:
        // (FIELD_INDEX_LONG, FIELD_INDEX_INT, FIELD_INDEX_STRING)
        // (1, 1, "A")
        // (2, 1, "B")
        // (3, 1, "C")
        // Depending on the sorting, distinct should pick the first element encountered.
        // The order of sort/distinct in the query should not matter

        // Case 1: Selecting highest numbers
        RealmResults<AnnotationIndexTypes> results1a = realm.where(AnnotationIndexTypes.class)
                .sort(AnnotationIndexTypes.FIELD_INDEX_LONG, Sort.DESCENDING)
                .distinct(AnnotationIndexTypes.FIELD_INDEX_INT)
                .findAll();
        assertEquals(1, results1a.size());
        assertEquals(3, results1a.get(0).getIndexLong());

        RealmResults<AnnotationIndexTypes> results1b = realm.where(AnnotationIndexTypes.class)
                .distinct(AnnotationIndexTypes.FIELD_INDEX_INT)
                .sort(AnnotationIndexTypes.FIELD_INDEX_LONG, Sort.DESCENDING)
                .findAll();
        assertEquals(1, results1b.size());
        assertEquals(3, results1b.get(0).getIndexLong());

        // Case 1: Selecting lowest number numbers
        RealmResults<AnnotationIndexTypes> results2a = realm.where(AnnotationIndexTypes.class)
                .sort(AnnotationIndexTypes.FIELD_INDEX_LONG, Sort.ASCENDING)
                .distinct(AnnotationIndexTypes.FIELD_INDEX_INT)
                .findAll();
        assertEquals(1, results2a.size());
        assertEquals(1, results2a.get(0).getIndexLong());

        RealmResults<AnnotationIndexTypes> results2b = realm.where(AnnotationIndexTypes.class)
                .distinct(AnnotationIndexTypes.FIELD_INDEX_INT)
                .sort(AnnotationIndexTypes.FIELD_INDEX_LONG, Sort.ASCENDING)
                .findAll();
        assertEquals(1, results2b.size());
        assertEquals(1, results2b.get(0).getIndexLong());
    }

    @Test
    public void sortByChildValue() {
        RealmResults<AllTypes> result = realm.where(AllTypes.class)
                .sort(AllTypes.FIELD_REALMOBJECT + "." + Dog.FIELD_NAME, Sort.ASCENDING)
                .findAll();

        assertEquals("A", result.first().getColumnRealmObject().getName());
        assertEquals("D", result.last().getColumnRealmObject().getName());
    }

    private void createAndTest(String str) {
        realm.beginTransaction();
        realm.delete(StringOnly.class);
        for (int i = 0; i < str.length(); i++) {
            StringOnly stringOnly = realm.createObject(StringOnly.class);
            stringOnly.setChars(str.substring(i, i + 1));
        }
        realm.commitTransaction();
        RealmResults<StringOnly> stringOnlies = realm.where(StringOnly.class).sort("chars").findAll();
        for (int i = 0; i < chars.length(); i++) {
            assertEquals(chars.substring(i, i + 1), stringOnlies.get(i).getChars());
        }
    }

    // permute and swap: http://www.geeksforgeeks.org/write-a-c-program-to-print-all-permutations-of-a-given-string/
    private void permute(String str, int l, int r) {
        if (l == r) {
            numberOfPermutations++;
            createAndTest(str);
        } else {
            for (int i = l; i <= r; i++) {
                str = swap(str,l,i);
                permute(str, l+1, r);
                str = swap(str,l,i);
            }
        }
    }

    private String swap(String a, int i, int j) {
        char temp;
        char[] charArray = a.toCharArray();
        temp = charArray[i] ;
        charArray[i] = charArray[j];
        charArray[j] = temp;
        return String.valueOf(charArray);
    }

    private int factorial(int n) {
        int fac = 1;
        for(int i = 1; i <= n; i++) {
            fac *= i;
        }
        return fac;
    }

    @Test
    public void sortCaseSensitive() {
        chars = "'- !\"#$%&()*,./:;?_+<=>123aAbBcCxXyYzZ";
        createAndTest(new StringBuilder(chars).reverse().toString());

        // try all permutations - keep the list short
        chars = "12aAbB";
        numberOfPermutations = 0;
        permute(chars, 0, chars.length()-1);
        assertEquals(numberOfPermutations, factorial(chars.length()));
    }
}
