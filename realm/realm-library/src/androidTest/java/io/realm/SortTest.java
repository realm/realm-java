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
import io.realm.internal.TableView;
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

    private void populateRealm(Realm realm) {
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
            allTypes.sort(null, (Sort[]) null);
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
        assertEquals(2, ((TableView) results.getTableOrView()).getSourceRowIndex(0));

        assertEquals("Adam", results.get(1).getColumnString());
        assertEquals(5, results.get(1).getColumnLong());
        assertEquals(0, ((TableView) results.getTableOrView()).getSourceRowIndex(1));

        assertEquals("Adam", results.get(2).getColumnString());
        assertEquals(5, results.get(2).getColumnLong());
        assertEquals(3, ((TableView) results.getTableOrView()).getSourceRowIndex(2));

        assertEquals("Brian", results.get(3).getColumnString());
        assertEquals(4, results.get(3).getColumnLong());
        assertEquals(1, ((TableView) results.getTableOrView()).getSourceRowIndex(3));
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
        assertEquals(2, ((TableView) results.getTableOrView()).getSourceRowIndex(0));

        assertEquals("Brian", results.get(1).getColumnString());
        assertEquals(4, results.get(1).getColumnLong());
        assertEquals(1, ((TableView) results.getTableOrView()).getSourceRowIndex(1));

        assertEquals("Adam", results.get(2).getColumnString());
        assertEquals(5, results.get(2).getColumnLong());
        assertEquals(0, ((TableView) results.getTableOrView()).getSourceRowIndex(2));

        assertEquals("Adam", results.get(3).getColumnString());
        assertEquals(5, results.get(3).getColumnLong());
        assertEquals(3, ((TableView) results.getTableOrView()).getSourceRowIndex(3));
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
        assertEquals(1, ((TableView) results.getTableOrView()).getSourceRowIndex(0));

        assertEquals("Adam", results.get(1).getColumnString());
        assertEquals(4, results.get(1).getColumnLong());
        assertEquals(2, ((TableView) results.getTableOrView()).getSourceRowIndex(1));

        assertEquals("Adam", results.get(2).getColumnString());
        assertEquals(5, results.get(2).getColumnLong());
        assertEquals(0, ((TableView) results.getTableOrView()).getSourceRowIndex(2));

        assertEquals("Adam", results.get(3).getColumnString());
        assertEquals(5, results.get(3).getColumnLong());
        assertEquals(3, ((TableView) results.getTableOrView()).getSourceRowIndex(3));
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
        assertEquals(0, ((TableView) results.getTableOrView()).getSourceRowIndex(0));

        assertEquals("Adam", results.get(1).getColumnString());
        assertEquals(5, results.get(1).getColumnLong());
        assertEquals(3, ((TableView) results.getTableOrView()).getSourceRowIndex(1));

        assertEquals("Adam", results.get(2).getColumnString());
        assertEquals(4, results.get(2).getColumnLong());
        assertEquals(2, ((TableView) results.getTableOrView()).getSourceRowIndex(2));

        assertEquals("Brian", results.get(3).getColumnString());
        assertEquals(4, results.get(3).getColumnLong());
        assertEquals(1, ((TableView) results.getTableOrView()).getSourceRowIndex(3));
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
            realm.where(AllTypes.class).findAll().sort(null, (Sort[]) null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
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

    @Test
    @RunTestInLooperThread
    public void resorting() throws InterruptedException {
        final AtomicInteger changeListenerCalled = new AtomicInteger(4);

        final Realm realm = looperThread.realm;
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
        looperThread.keepStrongReference.add(rr0);
        rr0.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> element) {
                // After commit: [0, 1, 2, 3, 4] - most likely as order isn't guaranteed
                assertEquals(5, element.size());
                endTest.run();
            }
        });

        // rr1: [1, 2, 0, 3]
        final RealmResults<AllTypes> rr1 = realm.where(AllTypes.class).findAll().sort(FIELD_LONG, Sort.ASCENDING);
        looperThread.keepStrongReference.add(rr1);
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
        looperThread.keepStrongReference.add(rr2);
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

        RealmResults<AllTypes> objectsAscending = realm.where(AllTypes.class).findAllSorted(AllTypes.FIELD_DATE, Sort.ASCENDING);
        assertEquals(TEST_SIZE, objectsAscending.size());
        int i = 0;
        for (AllTypes allTypes : objectsAscending) {
            assertEquals(new Date(i), allTypes.getColumnDate());
            i++;
        }

        RealmResults<AllTypes> objectsDescending = realm.where(AllTypes.class).findAllSorted(AllTypes.FIELD_DATE, Sort.DESCENDING);
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

        RealmResults<AllTypes> objectsAscending = realm.where(AllTypes.class).findAllSorted(AllTypes.FIELD_DATE, Sort.ASCENDING);
        assertEquals(TEST_SIZE, objectsAscending.size());
        looperThread.keepStrongReference.add(objectsAscending);
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

        RealmResults<AllTypes> objectsDescending = realm.where(AllTypes.class).findAllSorted(AllTypes.FIELD_DATE, Sort.DESCENDING);
        assertEquals(TEST_SIZE, objectsDescending.size());
        looperThread.keepStrongReference.add(objectsDescending);
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
}
