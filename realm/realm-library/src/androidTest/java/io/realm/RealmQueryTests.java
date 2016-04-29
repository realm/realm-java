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

import android.os.Looper;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.AllTypes;
import io.realm.entities.AnnotationIndexTypes;
import io.realm.entities.Cat;
import io.realm.entities.CatOwner;
import io.realm.entities.Dog;
import io.realm.entities.NonLatinFieldNames;
import io.realm.entities.NullTypes;
import io.realm.entities.Owner;
import io.realm.entities.PrimaryKeyAsBoxedByte;
import io.realm.entities.PrimaryKeyAsBoxedInteger;
import io.realm.entities.PrimaryKeyAsBoxedLong;
import io.realm.entities.PrimaryKeyAsBoxedShort;
import io.realm.entities.PrimaryKeyAsString;
import io.realm.entities.StringOnly;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmQueryTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    protected final static int TEST_DATA_SIZE = 10;

    private final static long DECADE_MILLIS = 10 * TimeUnit.DAYS.toMillis(365);

    protected Realm realm;

    @Before
    public void setUp() throws Exception {
        RealmConfiguration realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);
    }

    @After
    public void tearDown() throws Exception {
        if (realm != null) {
            realm.close();
        }
    }

    private void populateTestRealm(Realm testRealm, int objects) {
        testRealm.beginTransaction();
        testRealm.deleteAll();
        for (int i = 0; i < objects; ++i) {
            AllTypes allTypes = testRealm.createObject(AllTypes.class);
            allTypes.setColumnBoolean((i % 3) == 0);
            allTypes.setColumnBinary(new byte[]{1, 2, 3});
            allTypes.setColumnDate(new Date(DECADE_MILLIS * (i - (objects / 2))));
            allTypes.setColumnDouble(3.1415);
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

    private void populateTestRealm() {
        populateTestRealm(realm, TEST_DATA_SIZE);
    }

    @Test
    public void between() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(realm, TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList = realm.where(AllTypes.class)
                .between(AllTypes.FIELD_LONG, 0, 9).findAll();
        assertEquals(10, resultList.size());

        resultList = realm.where(AllTypes.class).beginsWith(AllTypes.FIELD_STRING, "test data ").findAll();
        assertEquals(TEST_OBJECTS_COUNT, resultList.size());

        resultList = realm.where(AllTypes.class).beginsWith(AllTypes.FIELD_STRING, "test data 1")
                .between(AllTypes.FIELD_LONG, 2, 20).findAll();
        assertEquals(10, resultList.size());

        resultList = realm.where(AllTypes.class).between(AllTypes.FIELD_LONG, 2, 20)
                .beginsWith(AllTypes.FIELD_STRING, "test data 1").findAll();
        assertEquals(10, resultList.size());

        assertEquals(51, realm.where(AllTypes.class).between(AllTypes.FIELD_DATE,
                new Date(0),
                new Date(DECADE_MILLIS * 50)).count());
    }

    @Test
    public void greaterThan() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(realm, TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList = realm.where(AllTypes.class)
                .greaterThan(AllTypes.FIELD_FLOAT, 10.234567f).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 10, resultList.size());

        resultList = realm.where(AllTypes.class).beginsWith(AllTypes.FIELD_STRING, "test data 1")
                .greaterThan(AllTypes.FIELD_FLOAT, 50.234567f).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 100, resultList.size());

        RealmQuery<AllTypes> query = realm.where(AllTypes.class).greaterThan(AllTypes.FIELD_FLOAT, 11.234567f);
        resultList = query.between(AllTypes.FIELD_LONG, 1, 20).findAll();
        assertEquals(10, resultList.size());
    }

    @Test
    public void greaterThan_date() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(realm, TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList;
        resultList = realm.where(AllTypes.class).greaterThan(AllTypes.FIELD_DATE, new Date(Long.MIN_VALUE)).findAll();
        assertEquals(TEST_OBJECTS_COUNT, resultList.size());
        resultList = realm.where(AllTypes.class).greaterThan(AllTypes.FIELD_DATE, new Date(DECADE_MILLIS * -80)).findAll();
        assertEquals(179, resultList.size());
        resultList = realm.where(AllTypes.class).greaterThan(AllTypes.FIELD_DATE, new Date(0)).findAll();
        assertEquals(TEST_OBJECTS_COUNT / 2 - 1, resultList.size());
        resultList = realm.where(AllTypes.class).greaterThan(AllTypes.FIELD_DATE, new Date(DECADE_MILLIS * 80)).findAll();
        assertEquals(19, resultList.size());
        resultList = realm.where(AllTypes.class).greaterThan(AllTypes.FIELD_DATE, new Date(Long.MAX_VALUE)).findAll();
        assertEquals(0, resultList.size());
    }


    @Test
    public void greaterThanOrEqualTo() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(realm, TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList = realm.where(AllTypes.class)
                .greaterThanOrEqualTo(AllTypes.FIELD_FLOAT, 10.234567f).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 9, resultList.size());

        resultList = realm.where(AllTypes.class).beginsWith(AllTypes.FIELD_STRING, "test data 1")
                .greaterThanOrEqualTo(AllTypes.FIELD_FLOAT, 50.234567f).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 100, resultList.size());

        RealmQuery<AllTypes> query = realm.where(AllTypes.class)
                .greaterThanOrEqualTo(AllTypes.FIELD_FLOAT, 11.234567f);
        query = query.between(AllTypes.FIELD_LONG, 1, 20);

        resultList = query.beginsWith(AllTypes.FIELD_STRING, "test data 15").findAll();
        assertEquals(1, resultList.size());
    }

    @Test
    public void greaterThanOrEqualTo_date() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(realm, TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList;
        resultList = realm.where(AllTypes.class).greaterThanOrEqualTo(AllTypes.FIELD_DATE, new Date(Long.MIN_VALUE)).findAll();
        assertEquals(TEST_OBJECTS_COUNT, resultList.size());
        resultList = realm.where(AllTypes.class).greaterThanOrEqualTo(AllTypes.FIELD_DATE, new Date(DECADE_MILLIS * -80)).findAll();
        assertEquals(180, resultList.size());
        resultList = realm.where(AllTypes.class).greaterThanOrEqualTo(AllTypes.FIELD_DATE, new Date(0)).findAll();
        assertEquals(TEST_OBJECTS_COUNT / 2, resultList.size());
        resultList = realm.where(AllTypes.class).greaterThanOrEqualTo(AllTypes.FIELD_DATE, new Date(DECADE_MILLIS * 80)).findAll();
        assertEquals(20, resultList.size());
        resultList = realm.where(AllTypes.class).greaterThanOrEqualTo(AllTypes.FIELD_DATE, new Date(Long.MAX_VALUE)).findAll();
        assertEquals(0, resultList.size());
    }

    @Test
    public void or() {
        populateTestRealm(realm, 200);

        RealmQuery<AllTypes> query = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_FLOAT, 31.234567f);
        RealmResults<AllTypes> resultList = query.or().between(AllTypes.FIELD_LONG, 1, 20).findAll();
        assertEquals(21, resultList.size());

        resultList = query.or().equalTo(AllTypes.FIELD_STRING, "test data 15").findAll();
        assertEquals(21, resultList.size());

        resultList = query.or().equalTo(AllTypes.FIELD_STRING, "test data 117").findAll();
        assertEquals(22, resultList.size());
    }

    @Test
    public void not() {
        populateTestRealm(); // create TEST_DATA_SIZE objects

        // only one object with value 5 -> TEST_DATA_SIZE-1 object with value "not 5"
        RealmResults<AllTypes> list1 = realm.where(AllTypes.class).not().equalTo(AllTypes.FIELD_LONG, 5).findAll();
        assertEquals(TEST_DATA_SIZE - 1, list1.size());

        // not().greater() and lessThenOrEqual() must be the same
        RealmResults<AllTypes> list2 = realm.where(AllTypes.class).not().greaterThan(AllTypes.FIELD_LONG, 5).findAll();
        RealmResults<AllTypes> list3 = realm.where(AllTypes.class).lessThanOrEqualTo(AllTypes.FIELD_LONG, 5).findAll();
        assertEquals(list2.size(), list3.size());
        for (int i = 0; i < list2.size(); i++) {
            assertEquals(list2.get(i).getColumnLong(), list3.get(i).getColumnLong());
        }

        // excepted result: 0, 1, 2, 5
        long expected[] = {0, 1, 2, 5};
        RealmResults<AllTypes> list4 = realm.where(AllTypes.class)
                .equalTo(AllTypes.FIELD_LONG, 5)
                .or()
                .not().beginGroup()
                .greaterThan(AllTypes.FIELD_LONG, 2)
                .endGroup()
                .findAll();
        assertEquals(4, list4.size());
        for (int i = 0; i < list4.size(); i++) {
            assertEquals(expected[i], list4.get(i).getColumnLong());
        }
    }

    @Test (expected = UnsupportedOperationException.class)
    public void not_aloneThrows() {
        // a not() alone must fail
        realm.where(AllTypes.class).not().findAll();
    }

    @Test
    public void and_implicit() {
        populateTestRealm(realm, 200);

        RealmQuery<AllTypes> query = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_FLOAT, 31.234567f);
        RealmResults<AllTypes> resultList = query.between(AllTypes.FIELD_LONG, 1, 10).findAll();
        assertEquals(0, resultList.size());

        query = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_FLOAT, 81.234567f);
        resultList = query.between(AllTypes.FIELD_LONG, 1, 100).findAll();
        assertEquals(1, resultList.size());
    }

    @Test
    public void lessThan() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(realm, TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).
                lessThan(AllTypes.FIELD_FLOAT, 31.234567f).findAll();
        assertEquals(30, resultList.size());
        RealmQuery<AllTypes> query = realm.where(AllTypes.class).lessThan(AllTypes.FIELD_FLOAT, 31.234567f);
        resultList = query.between(AllTypes.FIELD_LONG, 1, 10).findAll();
        assertEquals(10, resultList.size());
    }

    @Test
    public void lessThan_Date() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(realm, TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList;
        resultList = realm.where(AllTypes.class).lessThan(AllTypes.FIELD_DATE, new Date(Long.MIN_VALUE)).findAll();
        assertEquals(0, resultList.size());
        resultList = realm.where(AllTypes.class).lessThan(AllTypes.FIELD_DATE, new Date(DECADE_MILLIS * -80)).findAll();
        assertEquals(20, resultList.size());
        resultList = realm.where(AllTypes.class).lessThan(AllTypes.FIELD_DATE, new Date(0)).findAll();
        assertEquals(TEST_OBJECTS_COUNT / 2, resultList.size());
        resultList = realm.where(AllTypes.class).lessThan(AllTypes.FIELD_DATE, new Date(DECADE_MILLIS * 80)).findAll();
        assertEquals(180, resultList.size());
        resultList = realm.where(AllTypes.class).lessThan(AllTypes.FIELD_DATE, new Date(Long.MAX_VALUE)).findAll();
        assertEquals(TEST_OBJECTS_COUNT, resultList.size());
    }

    @Test
    public void lessThanOrEqualTo() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(realm, TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList = realm.where(AllTypes.class)
                .lessThanOrEqualTo(AllTypes.FIELD_FLOAT, 31.234567f).findAll();
        assertEquals(31, resultList.size());
        resultList = realm.where(AllTypes.class).lessThanOrEqualTo(AllTypes.FIELD_FLOAT, 31.234567f)
                .between(AllTypes.FIELD_LONG, 11, 20).findAll();
        assertEquals(10, resultList.size());
    }

    @Test
    public void lessThanOrEqualTo_date() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(realm, TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList;
        resultList = realm.where(AllTypes.class).lessThanOrEqualTo(AllTypes.FIELD_DATE, new Date(Long.MIN_VALUE)).findAll();
        assertEquals(0, resultList.size());
        resultList = realm.where(AllTypes.class).lessThanOrEqualTo(AllTypes.FIELD_DATE, new Date(DECADE_MILLIS * -80)).findAll();
        assertEquals(21, resultList.size());
        resultList = realm.where(AllTypes.class).lessThanOrEqualTo(AllTypes.FIELD_DATE, new Date(0)).findAll();
        assertEquals(TEST_OBJECTS_COUNT / 2 + 1, resultList.size());
        resultList = realm.where(AllTypes.class).lessThanOrEqualTo(AllTypes.FIELD_DATE, new Date(DECADE_MILLIS * 80)).findAll();
        assertEquals(181, resultList.size());
        resultList = realm.where(AllTypes.class).lessThanOrEqualTo(AllTypes.FIELD_DATE, new Date(Long.MAX_VALUE)).findAll();
        assertEquals(TEST_OBJECTS_COUNT, resultList.size());
    }

    @Test
    public void equalTo() {
        populateTestRealm(realm, 200);

        RealmResults<AllTypes> resultList = realm.where(AllTypes.class)
                .equalTo(AllTypes.FIELD_FLOAT, 31.234567f).findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(AllTypes.class).greaterThan(AllTypes.FIELD_FLOAT, 11.0f)
                .equalTo(AllTypes.FIELD_LONG, 10).findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(AllTypes.class).greaterThan(AllTypes.FIELD_FLOAT, 11.0f)
                .equalTo(AllTypes.FIELD_LONG, 1).findAll();
        assertEquals(0, resultList.size());
    }

    @Test
    public void equalTo_date() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(realm, TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList;
        resultList = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_DATE, new Date(Long.MIN_VALUE)).findAll();
        assertEquals(0, resultList.size());
        resultList = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_DATE, new Date(DECADE_MILLIS * -80)).findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_DATE, new Date(0)).findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_DATE, new Date(DECADE_MILLIS * 80)).findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_DATE, new Date(Long.MAX_VALUE)).findAll();
        assertEquals(0, resultList.size());
    }

    @Test
    public void equalTo_nonLatinCharacters() {
        populateTestRealm(realm, 200);

        RealmResults<NonLatinFieldNames> resultList = realm.where(NonLatinFieldNames.class)
                .equalTo(NonLatinFieldNames.FIELD_LONG_KOREAN_CHAR, 13).findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(NonLatinFieldNames.class)
                .greaterThan(NonLatinFieldNames.FIELD_FLOAT_KOREAN_CHAR, 11.0f)
                .equalTo(NonLatinFieldNames.FIELD_LONG_KOREAN_CHAR, 10).findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(NonLatinFieldNames.class)
                .greaterThan(NonLatinFieldNames.FIELD_FLOAT_KOREAN_CHAR, 11.0f)
                .equalTo(NonLatinFieldNames.FIELD_LONG_KOREAN_CHAR, 1).findAll();
        assertEquals(0, resultList.size());

        resultList = realm.where(NonLatinFieldNames.class)
                .equalTo(NonLatinFieldNames.FIELD_LONG_GREEK_CHAR, 13).findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(NonLatinFieldNames.class)
                .greaterThan(NonLatinFieldNames.FIELD_FLOAT_GREEK_CHAR, 11.0f)
                .equalTo(NonLatinFieldNames.FIELD_LONG_GREEK_CHAR, 10).findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(NonLatinFieldNames.class)
                .greaterThan(NonLatinFieldNames.FIELD_FLOAT_GREEK_CHAR, 11.0f)
                .equalTo(NonLatinFieldNames.FIELD_LONG_GREEK_CHAR, 1).findAll();
        assertEquals(0, resultList.size());
    }

    @Test
    public void notEqualTo() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(realm, TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList = realm.where(AllTypes.class)
                .notEqualTo(AllTypes.FIELD_LONG, 31).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 1, resultList.size());

        resultList = realm.where(AllTypes.class).notEqualTo(AllTypes.FIELD_FLOAT, 11.234567f)
                .equalTo(AllTypes.FIELD_LONG, 10).findAll();
        assertEquals(0, resultList.size());

        resultList = realm.where(AllTypes.class).notEqualTo(AllTypes.FIELD_FLOAT, 11.234567f)
                .equalTo(AllTypes.FIELD_LONG, 1).findAll();
        assertEquals(1, resultList.size());
    }

    @Test
    public void notEqualTo_date() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(realm, TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList;
        resultList = realm.where(AllTypes.class).notEqualTo(AllTypes.FIELD_DATE, new Date(Long.MIN_VALUE)).findAll();
        assertEquals(TEST_OBJECTS_COUNT, resultList.size());
        resultList = realm.where(AllTypes.class).notEqualTo(AllTypes.FIELD_DATE, new Date(DECADE_MILLIS * -80)).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 1, resultList.size());
        resultList = realm.where(AllTypes.class).notEqualTo(AllTypes.FIELD_DATE, new Date(0)).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 1, resultList.size());
        resultList = realm.where(AllTypes.class).notEqualTo(AllTypes.FIELD_DATE, new Date(DECADE_MILLIS * 80)).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 1, resultList.size());
        resultList = realm.where(AllTypes.class).notEqualTo(AllTypes.FIELD_DATE, new Date(Long.MAX_VALUE)).findAll();
        assertEquals(TEST_OBJECTS_COUNT, resultList.size());
    }

    @Test
    public void contains_caseSensitive() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(realm, TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList = realm.where(AllTypes.class)
                .contains("columnString", "DaTa 0", Case.INSENSITIVE)
                .or().contains("columnString", "20")
                .findAll();
        assertEquals(3, resultList.size());

        resultList = realm.where(AllTypes.class).contains("columnString", "DATA").findAll();
        assertEquals(0, resultList.size());

        resultList = realm.where(AllTypes.class)
                .contains("columnString", "TEST", Case.INSENSITIVE).findAll();
        assertEquals(TEST_OBJECTS_COUNT, resultList.size());
    }

    @Test
    public void contains_caseSensitiveWithNonLatinCharacters() {
        populateTestRealm();

        realm.beginTransaction();
        realm.delete(AllTypes.class);
        AllTypes at1 = realm.createObject(AllTypes.class);
        at1.setColumnString("Αλφα");
        AllTypes at2 = realm.createObject(AllTypes.class);
        at2.setColumnString("βήτα");
        AllTypes at3 = realm.createObject(AllTypes.class);
        at3.setColumnString("δέλτα");
        realm.commitTransaction();

        RealmResults<AllTypes> resultList = realm.where(AllTypes.class)
                .contains("columnString", "Α", Case.INSENSITIVE)
                .or().contains("columnString", "δ")
                .findAll();
        // Without case sensitive there is 3, Α = α
        // assertEquals(3,resultList.size());
        assertEquals(2, resultList.size());

        resultList = realm.where(AllTypes.class).contains("columnString", "α").findAll();
        assertEquals(3, resultList.size());

        resultList = realm.where(AllTypes.class).contains("columnString", "Δ").findAll();
        assertEquals(0, resultList.size());

        resultList = realm.where(AllTypes.class).contains("columnString", "Δ",
                Case.INSENSITIVE).findAll();
        // Without case sensitive there is 1, Δ = δ
        // assertEquals(1,resultList.size());
        assertEquals(0, resultList.size());
    }

    @Test
    public void equalTo_withNonExistingField() {
        try {
            realm.where(AllTypes.class).equalTo("NotAField", 13).findAll();
            fail("Should throw exception");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void queryLink() {
        realm.beginTransaction();
        Owner owner = realm.createObject(Owner.class);
        Dog dog1 = realm.createObject(Dog.class);
        dog1.setName("Dog 1");
        dog1.setWeight(1);
        Dog dog2 = realm.createObject(Dog.class);
        dog2.setName("Dog 2");
        dog2.setWeight(2);
        owner.getDogs().add(dog1);
        owner.getDogs().add(dog2);
        realm.commitTransaction();

        // Dog.weight has index 4 which is more than the total number of columns in Owner
        // This tests exposes a subtle error where the Owner table spec is used instead of Dog table spec.
        RealmResults<Dog> dogs = realm.where(Owner.class).findFirst().getDogs().where()
                .findAllSorted("name", Sort.ASCENDING);
        Dog dog = dogs.where().equalTo("weight", 1d).findFirst();
        assertEquals(dog1, dog);
    }


    @Test
    public void findAllSorted_multiFailures() {
        // zero fields specified
        try {
            realm.where(AllTypes.class).findAllSorted(new String[]{}, new Sort[]{});
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // number of fields and sorting orders don't match
        try {
            realm.where(AllTypes.class).findAllSorted(new String[]{AllTypes.FIELD_STRING},
                    new Sort[]{Sort.ASCENDING, Sort.ASCENDING});
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // null is not allowed
        try {
            realm.where(AllTypes.class).findAllSorted((String[]) null, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            realm.where(AllTypes.class).findAllSorted(new String[]{AllTypes.FIELD_STRING}, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // non-existing field name
        try {
            realm.where(AllTypes.class)
                    .findAllSorted(new String[]{AllTypes.FIELD_STRING, "do-not-exist"},
                            new Sort[]{Sort.ASCENDING, Sort.ASCENDING});
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void findAllSorted_singleField() {
        realm.beginTransaction();
        for (int i = 0; i < TEST_DATA_SIZE; i++) {
            AllTypes allTypes = realm.createObject(AllTypes.class);
            allTypes.setColumnLong(i);
        }
        realm.commitTransaction();

        RealmResults<AllTypes> sortedList = realm.where(AllTypes.class)
                .findAllSorted(new String[]{AllTypes.FIELD_LONG}, new Sort[]{Sort.DESCENDING});
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals(TEST_DATA_SIZE - 1, sortedList.first().getColumnLong());
        assertEquals(0, sortedList.last().getColumnLong());
    }

    @Test
    public void subQueryScope() {
        populateTestRealm();
        RealmResults<AllTypes> result = realm.where(AllTypes.class).lessThan("columnLong", 5).findAll();
        RealmResults<AllTypes> subQueryResult = result.where().greaterThan("columnLong", 3).findAll();
        assertEquals(1, subQueryResult.size());
    }

    @Test
    public void findFirst() {
        realm.beginTransaction();
        Owner owner1 = realm.createObject(Owner.class);
        owner1.setName("Owner 1");
        Dog dog1 = realm.createObject(Dog.class);
        dog1.setName("Dog 1");
        dog1.setWeight(1);
        Dog dog2 = realm.createObject(Dog.class);
        dog2.setName("Dog 2");
        dog2.setWeight(2);
        owner1.getDogs().add(dog1);
        owner1.getDogs().add(dog2);

        Owner owner2 = realm.createObject(Owner.class);
        owner2.setName("Owner 2");
        Dog dog3 = realm.createObject(Dog.class);
        dog3.setName("Dog 3");
        dog3.setWeight(1);
        Dog dog4 = realm.createObject(Dog.class);
        dog4.setName("Dog 4");
        dog4.setWeight(2);
        owner2.getDogs().add(dog3);
        owner2.getDogs().add(dog4);
        realm.commitTransaction();

        RealmList<Dog> dogs = realm.where(Owner.class).equalTo("name", "Owner 2").findFirst().getDogs();
        Dog dog = dogs.where().equalTo("name", "Dog 4").findFirst();
        assertEquals(dog4, dog);
    }

    @Test
    public void georgian() {
        String words[] = {"მონაცემთა ბაზა", "მიწისქვეშა გადასასვლელი", "რუსთაველის გამზირი",
                "მთავარი ქუჩა", "სადგურის მოედანი", "ველოცირაპტორების ჯოგი"};

        String sorted[] = {"ველოცირაპტორების ჯოგი", "მთავარი ქუჩა", "მიწისქვეშა გადასასვლელი",
                "მონაცემთა ბაზა", "რუსთაველის გამზირი", "სადგურის მოედანი"};

        realm.beginTransaction();
        realm.delete(StringOnly.class);
        for (String word : words) {
            StringOnly stringOnly = realm.createObject(StringOnly.class);
            stringOnly.setChars(word);
        }
        realm.commitTransaction();

        RealmResults<StringOnly> stringOnlies1 = realm.where(StringOnly.class).contains("chars", "მთავარი").findAll();
        assertEquals(1, stringOnlies1.size());

        RealmResults<StringOnly> stringOnlies2 = realm.allObjects(StringOnly.class);
        stringOnlies2 = stringOnlies2.sort("chars");
        for (int i = 0; i < stringOnlies2.size(); i++) {
            assertEquals(sorted[i], stringOnlies2.get(i).getChars());
        }
    }

    // Querying a non-nullable field with null is an error
    @Test
    public void equalTo_notNullableFields() {
        TestHelper.populateTestRealmForNullTests(realm);

        // 1 String
        try {
            realm.where(NullTypes.class).equalTo(NullTypes.FIELD_STRING_NOT_NULL,
                    (String) null).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 2 Bytes skipped, doesn't support equalTo query
        // 3 Boolean
        try {
            realm.where(NullTypes.class).equalTo(NullTypes.FIELD_BOOLEAN_NOT_NULL, (Boolean) null).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 4 Byte
        try {
            realm.where(NullTypes.class).equalTo(NullTypes.FIELD_BYTE_NOT_NULL, (Byte) null).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 5 Short
        try {
            realm.where(NullTypes.class).equalTo(NullTypes.FIELD_SHORT_NOT_NULL, (Short) null).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 6 Integer
        try {
            realm.where(NullTypes.class).equalTo(NullTypes.FIELD_INTEGER_NOT_NULL, (Integer) null).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 7 Long
        try {
            realm.where(NullTypes.class).equalTo(NullTypes.FIELD_LONG_NOT_NULL, (Long) null).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 8 Float
        try {
            realm.where(NullTypes.class).equalTo(NullTypes.FIELD_FLOAT_NOT_NULL, (Float) null).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 9 Double
        try {
            realm.where(NullTypes.class).equalTo(NullTypes.FIELD_DOUBLE_NOT_NULL, (Double) null).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 10 Date
        try {
            realm.where(NullTypes.class).equalTo(NullTypes.FIELD_DATE_NOT_NULL, (Date) null).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    // Querying a non-nullable field with null is an error
    @Test
    public void isNull_notNullableFields() {
        // 1 String
        try {
            realm.where(NullTypes.class).isNull(NullTypes.FIELD_STRING_NOT_NULL).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 2 Bytes
        try {
            realm.where(NullTypes.class).isNull(NullTypes.FIELD_BYTES_NOT_NULL).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 3 Boolean
        try {
            realm.where(NullTypes.class).isNull(NullTypes.FIELD_BOOLEAN_NOT_NULL).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 4 Byte
        try {
            realm.where(NullTypes.class).isNull(NullTypes.FIELD_BYTE_NOT_NULL).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 5 Short
        try {
            realm.where(NullTypes.class).isNull(NullTypes.FIELD_SHORT_NOT_NULL).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 6 Integer
        try {
            realm.where(NullTypes.class).isNull(NullTypes.FIELD_INTEGER_NOT_NULL).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 7 Long
        try {
            realm.where(NullTypes.class).isNull(NullTypes.FIELD_LONG_NOT_NULL).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 8 Float
        try {
            realm.where(NullTypes.class).isNull(NullTypes.FIELD_FLOAT_NOT_NULL).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 9 Double
        try {
            realm.where(NullTypes.class).isNull(NullTypes.FIELD_DOUBLE_NOT_NULL).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 10 Date
        try {
            realm.where(NullTypes.class).isNull(NullTypes.FIELD_DATE_NOT_NULL).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    // Querying nullable PrimaryKey
    @Test
    public void equalTo_nullPrimaryKeys() {
        final long SECONDARY_FIELD_NUMBER = 49992417L;
        final String SECONDARY_FIELD_STRING = "Realm is a mobile database hundreds of millions of people rely on.";
        // fill up a Realm with one user PrimaryKey value and 9 numeric values, starting from -5
        TestHelper.populateTestRealmWithStringPrimaryKey(realm,  (String) null,  SECONDARY_FIELD_NUMBER, 10, -5);
        TestHelper.populateTestRealmWithBytePrimaryKey(realm,    (Byte) null,    SECONDARY_FIELD_STRING, 10, -5);
        TestHelper.populateTestRealmWithShortPrimaryKey(realm,   (Short) null,   SECONDARY_FIELD_STRING, 10, -5);
        TestHelper.populateTestRealmWithIntegerPrimaryKey(realm, (Integer) null, SECONDARY_FIELD_STRING, 10, -5);
        TestHelper.populateTestRealmWithLongPrimaryKey(realm,    (Long) null,    SECONDARY_FIELD_STRING, 10, -5);

        // String
        assertEquals(SECONDARY_FIELD_NUMBER, realm.where(PrimaryKeyAsString.class).equalTo(PrimaryKeyAsString.FIELD_PRIMARY_KEY,             (String) null).findAll().first().getId());
        // Boxed Byte
        assertEquals(SECONDARY_FIELD_STRING, realm.where(PrimaryKeyAsBoxedByte.class).equalTo(PrimaryKeyAsBoxedByte.FIELD_PRIMARY_KEY,       (Byte) null).findAll().first().getName());
        // Boxed Short
        assertEquals(SECONDARY_FIELD_STRING, realm.where(PrimaryKeyAsBoxedShort.class).equalTo(PrimaryKeyAsBoxedShort.FIELD_PRIMARY_KEY,     (Short) null).findAll().first().getName());
        // Boxed Integer
        assertEquals(SECONDARY_FIELD_STRING, realm.where(PrimaryKeyAsBoxedInteger.class).equalTo(PrimaryKeyAsBoxedInteger.FIELD_PRIMARY_KEY, (Integer) null).findAll().first().getName());
        // Boxed Long
        assertEquals(SECONDARY_FIELD_STRING, realm.where(PrimaryKeyAsBoxedLong.class).equalTo(PrimaryKeyAsBoxedLong.FIELD_PRIMARY_KEY,       (Long) null).findAll().first().getName());
    }

    @Test
    public void isNull_nullPrimaryKeys() {
        final long SECONDARY_FIELD_NUMBER = 49992417L;
        final String SECONDARY_FIELD_STRING = "Realm is a mobile database hundreds of millions of people rely on.";
        // fill up a realm with one user PrimaryKey value and 9 numeric values, starting from -5
        TestHelper.populateTestRealmWithStringPrimaryKey(realm,  (String) null,  SECONDARY_FIELD_NUMBER, 10, -5);
        TestHelper.populateTestRealmWithBytePrimaryKey(realm,    (Byte) null,    SECONDARY_FIELD_STRING, 10, -5);
        TestHelper.populateTestRealmWithShortPrimaryKey(realm,   (Short) null,   SECONDARY_FIELD_STRING, 10, -5);
        TestHelper.populateTestRealmWithIntegerPrimaryKey(realm, (Integer) null, SECONDARY_FIELD_STRING, 10, -5);
        TestHelper.populateTestRealmWithLongPrimaryKey(realm,    (Long) null,    SECONDARY_FIELD_STRING, 10, -5);

        // String
        assertEquals(SECONDARY_FIELD_NUMBER, realm.where(PrimaryKeyAsString.class).isNull(PrimaryKeyAsString.FIELD_PRIMARY_KEY).findAll().first().getId());
        // Boxed Byte
        assertEquals(SECONDARY_FIELD_STRING, realm.where(PrimaryKeyAsBoxedByte.class).isNull(PrimaryKeyAsBoxedByte.FIELD_PRIMARY_KEY).findAll().first().getName());
        // Boxed Short
        assertEquals(SECONDARY_FIELD_STRING, realm.where(PrimaryKeyAsBoxedShort.class).isNull(PrimaryKeyAsBoxedShort.FIELD_PRIMARY_KEY).findAll().first().getName());
        // Boxed Integer
        assertEquals(SECONDARY_FIELD_STRING, realm.where(PrimaryKeyAsBoxedInteger.class).isNull(PrimaryKeyAsBoxedInteger.FIELD_PRIMARY_KEY).findAll().first().getName());
        // Boxed Long
        assertEquals(SECONDARY_FIELD_STRING, realm.where(PrimaryKeyAsBoxedLong.class).isNull(PrimaryKeyAsBoxedLong.FIELD_PRIMARY_KEY).findAll().first().getName());
    }

    @Test
    public void notEqualTo_nullPrimaryKeys() {
        final long SECONDARY_FIELD_NUMBER = 49992417L;
        final String SECONDARY_FIELD_STRING = "Realm is a mobile database hundreds of millions of people rely on.";
        // fill up a realm with one user PrimaryKey value and one numeric values, starting from -1
        TestHelper.populateTestRealmWithStringPrimaryKey(realm,  (String) null,  SECONDARY_FIELD_NUMBER, 2, -1);
        TestHelper.populateTestRealmWithBytePrimaryKey(realm,    (Byte) null,    SECONDARY_FIELD_STRING, 2, -1);
        TestHelper.populateTestRealmWithShortPrimaryKey(realm,   (Short) null,   SECONDARY_FIELD_STRING, 2, -1);
        TestHelper.populateTestRealmWithIntegerPrimaryKey(realm, (Integer) null, SECONDARY_FIELD_STRING, 2, -1);
        TestHelper.populateTestRealmWithLongPrimaryKey(realm,    (Long) null,    SECONDARY_FIELD_STRING, 2, -1);

        // String
        assertEquals(SECONDARY_FIELD_NUMBER, realm.where(PrimaryKeyAsString.class).notEqualTo(PrimaryKeyAsString.FIELD_PRIMARY_KEY,             "-1").findAll().first().getId());
        // Boxed Byte
        assertEquals(SECONDARY_FIELD_STRING, realm.where(PrimaryKeyAsBoxedByte.class).notEqualTo(PrimaryKeyAsBoxedByte.FIELD_PRIMARY_KEY,       Byte.valueOf((byte)-1)).findAll().first().getName());
        // Boxed Short
        assertEquals(SECONDARY_FIELD_STRING, realm.where(PrimaryKeyAsBoxedShort.class).notEqualTo(PrimaryKeyAsBoxedShort.FIELD_PRIMARY_KEY,     Short.valueOf((short)-1)).findAll().first().getName());
        // Boxed Integer
        assertEquals(SECONDARY_FIELD_STRING, realm.where(PrimaryKeyAsBoxedInteger.class).notEqualTo(PrimaryKeyAsBoxedInteger.FIELD_PRIMARY_KEY, Integer.valueOf(-1)).findAll().first().getName());
        // Boxed Long
        assertEquals(SECONDARY_FIELD_STRING, realm.where(PrimaryKeyAsBoxedLong.class).notEqualTo(PrimaryKeyAsBoxedLong.FIELD_PRIMARY_KEY,       Long.valueOf((long)-1)).findAll().first().getName());
    }

    @Test
    public void beginWith_nullStringPrimaryKey() {
        final long SECONDARY_FIELD_NUMBER = 49992417L;
        TestHelper.populateTestRealmWithStringPrimaryKey(realm,  (String) null,  SECONDARY_FIELD_NUMBER, 10, -5);

        assertEquals(SECONDARY_FIELD_NUMBER, realm.where(PrimaryKeyAsString.class).beginsWith(PrimaryKeyAsString.FIELD_PRIMARY_KEY, null).findAll().first().getId());
    }

    @Test
    public void contains_nullStringPrimaryKey() {
        final long SECONDARY_FIELD_NUMBER = 49992417L;
        TestHelper.populateTestRealmWithStringPrimaryKey(realm,  (String) null,  SECONDARY_FIELD_NUMBER, 10, -5);

        assertEquals(SECONDARY_FIELD_NUMBER, realm.where(PrimaryKeyAsString.class).contains(PrimaryKeyAsString.FIELD_PRIMARY_KEY, null).findAll().first().getId());
    }

    @Test
    public void endsWith_nullStringPrimaryKey() {
        final long SECONDARY_FIELD_NUMBER = 49992417L;
        TestHelper.populateTestRealmWithStringPrimaryKey(realm,  (String) null,  SECONDARY_FIELD_NUMBER, 10, -5);

        assertEquals(SECONDARY_FIELD_NUMBER, realm.where(PrimaryKeyAsString.class).endsWith(PrimaryKeyAsString.FIELD_PRIMARY_KEY, null).findAll().first().getId());
    }

    @Test
    public void between_nullPrimaryKeysIsNotZero() {
        // fill up a realm with one user PrimaryKey value and 9 numeric values, starting from -5
        TestHelper.populateTestRealmWithBytePrimaryKey(realm,    (Byte) null,    (String) null, 10, -5);
        TestHelper.populateTestRealmWithShortPrimaryKey(realm,   (Short) null,   (String) null, 10, -5);
        TestHelper.populateTestRealmWithIntegerPrimaryKey(realm, (Integer) null, (String) null, 10, -5);
        TestHelper.populateTestRealmWithLongPrimaryKey(realm,    (Long) null,    (String) null, 10, -5);

        // Boxed Byte
        assertEquals(3, realm.where(PrimaryKeyAsBoxedByte.class).between(PrimaryKeyAsBoxedByte.FIELD_PRIMARY_KEY,       -1, 1).count());
        // Boxed Short
        assertEquals(3, realm.where(PrimaryKeyAsBoxedShort.class).between(PrimaryKeyAsBoxedShort.FIELD_PRIMARY_KEY,     -1, 1).count());
        // Boxed Integer
        assertEquals(3, realm.where(PrimaryKeyAsBoxedInteger.class).between(PrimaryKeyAsBoxedInteger.FIELD_PRIMARY_KEY, -1, 1).count());
        // Boxed Long
        assertEquals(3, realm.where(PrimaryKeyAsBoxedLong.class).between(PrimaryKeyAsBoxedLong.FIELD_PRIMARY_KEY,       -1, 1).count());
    }

    @Test
    public void greaterThan_nullPrimaryKeysIsNotZero() {
        // fill up a realm with one user PrimaryKey value and 9 numeric values, starting from -5
        TestHelper.populateTestRealmWithBytePrimaryKey(realm,    (Byte) null,    (String) null, 10, -5);
        TestHelper.populateTestRealmWithShortPrimaryKey(realm,   (Short) null,   (String) null, 10, -5);
        TestHelper.populateTestRealmWithIntegerPrimaryKey(realm, (Integer) null, (String) null, 10, -5);
        TestHelper.populateTestRealmWithLongPrimaryKey(realm,    (Long) null,    (String) null, 10, -5);

        // Boxed Byte
        assertEquals(4, realm.where(PrimaryKeyAsBoxedByte.class).greaterThan(PrimaryKeyAsBoxedByte.FIELD_PRIMARY_KEY,       -1).count());
        // Boxed Short
        assertEquals(4, realm.where(PrimaryKeyAsBoxedShort.class).greaterThan(PrimaryKeyAsBoxedShort.FIELD_PRIMARY_KEY,     -1).count());
        // Boxed Integer
        assertEquals(4, realm.where(PrimaryKeyAsBoxedInteger.class).greaterThan(PrimaryKeyAsBoxedInteger.FIELD_PRIMARY_KEY, -1).count());
        // Boxed Long
        assertEquals(4, realm.where(PrimaryKeyAsBoxedLong.class).greaterThan(PrimaryKeyAsBoxedLong.FIELD_PRIMARY_KEY,       -1).count());
    }

    @Test
    public void greaterThanOrEqualTo_nullPrimaryKeysIsNotZero() {
        // fill up a realm with one user PrimaryKey value and 9 numeric values, starting from -5
        TestHelper.populateTestRealmWithBytePrimaryKey(realm,    (Byte) null,    (String) null, 10, -5);
        TestHelper.populateTestRealmWithShortPrimaryKey(realm,   (Short) null,   (String) null, 10, -5);
        TestHelper.populateTestRealmWithIntegerPrimaryKey(realm, (Integer) null, (String) null, 10, -5);
        TestHelper.populateTestRealmWithLongPrimaryKey(realm,    (Long) null,    (String) null, 10, -5);

        // Boxed Byte
        assertEquals(5, realm.where(PrimaryKeyAsBoxedByte.class).greaterThanOrEqualTo(PrimaryKeyAsBoxedByte.FIELD_PRIMARY_KEY,       -1).count());
        // Boxed Short
        assertEquals(5, realm.where(PrimaryKeyAsBoxedShort.class).greaterThanOrEqualTo(PrimaryKeyAsBoxedShort.FIELD_PRIMARY_KEY,     -1).count());
        // Boxed Integer
        assertEquals(5, realm.where(PrimaryKeyAsBoxedInteger.class).greaterThanOrEqualTo(PrimaryKeyAsBoxedInteger.FIELD_PRIMARY_KEY, -1).count());
        // Boxed Long
        assertEquals(5, realm.where(PrimaryKeyAsBoxedLong.class).greaterThanOrEqualTo(PrimaryKeyAsBoxedLong.FIELD_PRIMARY_KEY,       -1).count());
    }

    @Test
    public void lessThan_nullPrimaryKeysIsNotZero() {
        // fill up a realm with one user PrimaryKey value and 9 numeric values, starting from -5
        TestHelper.populateTestRealmWithBytePrimaryKey(realm,    (Byte) null,    (String) null, 10, -5);
        TestHelper.populateTestRealmWithShortPrimaryKey(realm,   (Short) null,   (String) null, 10, -5);
        TestHelper.populateTestRealmWithIntegerPrimaryKey(realm, (Integer) null, (String) null, 10, -5);
        TestHelper.populateTestRealmWithLongPrimaryKey(realm,    (Long) null,    (String) null, 10, -5);

        // Boxed Byte
        assertEquals(6, realm.where(PrimaryKeyAsBoxedByte.class).lessThan(PrimaryKeyAsBoxedByte.FIELD_PRIMARY_KEY,       1).count());
        // Boxed Short
        assertEquals(6, realm.where(PrimaryKeyAsBoxedShort.class).lessThan(PrimaryKeyAsBoxedShort.FIELD_PRIMARY_KEY,     1).count());
        // Boxed Integer
        assertEquals(6, realm.where(PrimaryKeyAsBoxedInteger.class).lessThan(PrimaryKeyAsBoxedInteger.FIELD_PRIMARY_KEY, 1).count());
        // Boxed Long
        assertEquals(6, realm.where(PrimaryKeyAsBoxedLong.class).lessThan(PrimaryKeyAsBoxedLong.FIELD_PRIMARY_KEY,       1).count());
    }

    @Test
    public void lessThanOrEqualTo_nullPrimaryKeysIsNotZero() {
        // fill up a realm with one user PrimaryKey value and 9 numeric values, starting from -5
        TestHelper.populateTestRealmWithBytePrimaryKey(realm,    (Byte) null,    (String) null, 10, -5);
        TestHelper.populateTestRealmWithShortPrimaryKey(realm,   (Short) null,   (String) null, 10, -5);
        TestHelper.populateTestRealmWithIntegerPrimaryKey(realm, (Integer) null, (String) null, 10, -5);
        TestHelper.populateTestRealmWithLongPrimaryKey(realm,    (Long) null,    (String) null, 10, -5);

        // Boxed Byte
        assertEquals(7, realm.where(PrimaryKeyAsBoxedByte.class).lessThanOrEqualTo(PrimaryKeyAsBoxedByte.FIELD_PRIMARY_KEY,       1).count());
        // Boxed Short
        assertEquals(7, realm.where(PrimaryKeyAsBoxedShort.class).lessThanOrEqualTo(PrimaryKeyAsBoxedShort.FIELD_PRIMARY_KEY,     1).count());
        // Boxed Integer
        assertEquals(7, realm.where(PrimaryKeyAsBoxedInteger.class).lessThanOrEqualTo(PrimaryKeyAsBoxedInteger.FIELD_PRIMARY_KEY, 1).count());
        // Boxed Long
        assertEquals(7, realm.where(PrimaryKeyAsBoxedLong.class).lessThanOrEqualTo(PrimaryKeyAsBoxedLong.FIELD_PRIMARY_KEY,       1).count());
    }

    // Querying nullable fields, querying with equalTo null
    @Test
    public void equalTo_nullableFields() {
        TestHelper.populateTestRealmForNullTests(realm);

        // 1 String
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_STRING_NULL, "Horse").findAll().size());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_STRING_NULL, (String) null).findAll().size());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_STRING_NULL, "Fish").findAll().size());
        assertEquals(0, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_STRING_NULL, "Goat").findAll().size());
        // 2 Bytes skipped, doesn't support equalTo query
        // 3 Boolean
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_BOOLEAN_NULL, true).findAll().size());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_BOOLEAN_NULL, (Boolean) null).findAll().size());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_BOOLEAN_NULL, false).findAll().size());
        // 4 Byte
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_BYTE_NULL, 1).findAll().size());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_BYTE_NULL, (byte) 1).findAll().size());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_BYTE_NULL, (Byte) null).findAll().size());
        assertEquals(0, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_BYTE_NULL, (byte) 42).findAll().size());
        // 5 Short for other long based columns, only test null
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_SHORT_NULL, 1).findAll().size());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_SHORT_NULL, (short) 1).findAll().size());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_SHORT_NULL, (Short) null).findAll().size());
        assertEquals(0, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_SHORT_NULL, (short) 42).findAll().size());
        // 6 Integer
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_INTEGER_NULL, 1).findAll().size());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_INTEGER_NULL, (Integer) null).findAll().size());
        assertEquals(0, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_INTEGER_NULL, 42).findAll().size());
        // 7 Long
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_LONG_NULL, 1).findAll().size());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_LONG_NULL, (long) 1).findAll().size());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_LONG_NULL, (Long) null).findAll().size());
        assertEquals(0, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_LONG_NULL, (long) 42).findAll().size());
        // 8 Float
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_FLOAT_NULL, 1F).findAll().size());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_FLOAT_NULL, (Float) null).findAll().size());
        assertEquals(0, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_FLOAT_NULL, 42F).findAll().size());
        // 9 Double
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_DOUBLE_NULL, 1D).findAll().size());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_DOUBLE_NULL, (Double) null).findAll().size());
        assertEquals(0, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_DOUBLE_NULL, 42D).findAll().size());
        // 10 Date
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_DATE_NULL, new Date(0)).findAll().size());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_DATE_NULL, (Date) null).findAll().size());
        assertEquals(0, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_DATE_NULL, new Date(424242)).findAll().size());
        // 11 Object skipped, doesn't support equalTo query
    }

    // Querying nullable field for null
    @Test
    public void isNull_nullableFields() {
        TestHelper.populateTestRealmForNullTests(realm);

        // 1 String
        assertEquals(1, realm.where(NullTypes.class).isNull(NullTypes.FIELD_STRING_NULL).findAll().size());
        // 2 Bytes
        assertEquals(1, realm.where(NullTypes.class).isNull(NullTypes.FIELD_BYTES_NULL).findAll().size());
        // 3 Boolean
        assertEquals(1, realm.where(NullTypes.class).isNull(NullTypes.FIELD_BOOLEAN_NULL).findAll().size());
        // 4 Byte
        assertEquals(1, realm.where(NullTypes.class).isNull(NullTypes.FIELD_BYTE_NULL).findAll().size());
        // 5 Short
        assertEquals(1, realm.where(NullTypes.class).isNull(NullTypes.FIELD_SHORT_NULL).findAll().size());
        // 6 Integer
        assertEquals(1, realm.where(NullTypes.class).isNull(NullTypes.FIELD_INTEGER_NULL).findAll().size());
        // 7 Long
        assertEquals(1, realm.where(NullTypes.class).isNull(NullTypes.FIELD_LONG_NULL).findAll().size());
        // 8 Float
        assertEquals(1, realm.where(NullTypes.class).isNull(NullTypes.FIELD_FLOAT_NULL).findAll().size());
        // 9 Double
        assertEquals(1, realm.where(NullTypes.class).isNull(NullTypes.FIELD_DOUBLE_NULL).findAll().size());
        // 10 Date
        assertEquals(1, realm.where(NullTypes.class).isNull(NullTypes.FIELD_DATE_NULL).findAll().size());
        // 11 Object
        assertEquals(1, realm.where(NullTypes.class).isNull(NullTypes.FIELD_OBJECT_NULL).findAll().size());
    }

    // Querying nullable field for not null
    @Test
    public void notEqualTo_nullableFields() {
        TestHelper.populateTestRealmForNullTests(realm);
        // 1 String
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_STRING_NULL, "Horse").findAll().size());
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_STRING_NULL, (String) null).findAll().size());
        // 2 Bytes skipped, doesn't support notEqualTo query
        // 3 Boolean
        assertEquals(1, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_BOOLEAN_NULL, false).findAll().size());
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_BOOLEAN_NULL, (Boolean) null).findAll().size());
        // 4 Byte
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_BYTE_NULL, (byte) 1).findAll().size());
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_BYTE_NULL, (Byte) null).findAll().size());
        // 5 Short
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_SHORT_NULL, (short) 1).findAll().size());
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_SHORT_NULL, (Byte) null).findAll().size());
        // 6 Integer
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_INTEGER_NULL, 1).findAll().size());
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_INTEGER_NULL, (Integer) null).findAll().size());
        // 7 Long
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_LONG_NULL, 1).findAll().size());
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_LONG_NULL, (Integer) null).findAll().size());
        // 8 Float
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_FLOAT_NULL, 1F).findAll().size());
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_FLOAT_NULL, (Float) null).findAll().size());
        // 9 Double
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_DOUBLE_NULL, 1D).findAll().size());
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_DOUBLE_NULL, (Double) null).findAll().size());
        // 10 Date
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_DATE_NULL, new Date(0)).findAll().size());
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_DATE_NULL, (Date) null).findAll().size());
        // 11 Object skipped, doesn't support notEqualTo query
    }

    // Querying nullable field for not null
    @Test
    public void isNotNull_nullableFields() {
        TestHelper.populateTestRealmForNullTests(realm);

        // 1 String
        assertEquals(2, realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_STRING_NULL).findAll().size());
        // 2 Bytes
        assertEquals(2, realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_BYTES_NULL).findAll().size());
        // 3 Boolean
        assertEquals(2, realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_BOOLEAN_NULL).findAll().size());
        // 4 Byte
        assertEquals(2, realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_BYTE_NULL).findAll().size());
        // 5 Short
        assertEquals(2, realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_SHORT_NULL).findAll().size());
        // 6 Integer
        assertEquals(2, realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_INTEGER_NULL).findAll().size());
        // 7 Long
        assertEquals(2, realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_LONG_NULL).findAll().size());
        // 8 Float
        assertEquals(2, realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_FLOAT_NULL).findAll().size());
        // 9 Double
        assertEquals(2, realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_DOUBLE_NULL).findAll().size());
        // 10 Date
        assertEquals(2, realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_DATE_NULL).findAll().size());
        // 11 Object
        assertEquals(2, realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_OBJECT_NULL).findAll().size());
    }

    // Querying nullable field with beginsWith - all strings begin with null
    @Test
    public void beginWith_nullForNullableStrings() {
        TestHelper.populateTestRealmForNullTests(realm);
        assertEquals("Fish", realm.where(NullTypes.class).beginsWith(NullTypes.FIELD_STRING_NULL,
                null).findFirst().getFieldStringNotNull());
    }

    // Querying nullable field with endsWith - all strings contain with null
    @Test
    public void contains_nullForNullableStrings() {
        TestHelper.populateTestRealmForNullTests(realm);
        assertEquals("Fish", realm.where(NullTypes.class).contains(NullTypes.FIELD_STRING_NULL,
                null).findFirst().getFieldStringNotNull());
    }

    // Querying nullable field with endsWith - all strings end with null
    @Test
    public void endsWith_nullForNullableStrings() {
        TestHelper.populateTestRealmForNullTests(realm);
        assertEquals("Fish", realm.where(NullTypes.class).endsWith(NullTypes.FIELD_STRING_NULL,
                null).findFirst().getFieldStringNotNull());
    }

    // Querying with between and table has null values in row.
    @Test
    public void between_nullValuesInRow() {
        TestHelper.populateTestRealmForNullTests(realm);

        // 6 Integer
        assertEquals(1, realm.where(NullTypes.class).between(NullTypes.FIELD_INTEGER_NULL, 2, 4).count());
        // 7 Long
        assertEquals(1, realm.where(NullTypes.class).between(NullTypes.FIELD_LONG_NULL, 2L, 4L).count());
        // 8 Float
        assertEquals(1, realm.where(NullTypes.class).between(NullTypes.FIELD_FLOAT_NULL, 2F, 4F).count());
        // 9 Double
        assertEquals(1, realm.where(NullTypes.class).between(NullTypes.FIELD_DOUBLE_NULL, 2D, 4D).count());
        // 10 Date
        assertEquals(1, realm.where(NullTypes.class).between(NullTypes.FIELD_DATE_NULL, new Date(10000),
                new Date(20000)).count());
    }

    // Querying with greaterThan and table has null values in row.
    @Test
    public void greaterThan_nullValuesInRow() {
        TestHelper.populateTestRealmForNullTests(realm);

        // 6 Integer
        assertEquals(1, realm.where(NullTypes.class).greaterThan(NullTypes.FIELD_INTEGER_NULL, 2).count());
        // 7 Long
        assertEquals(1, realm.where(NullTypes.class).greaterThan(NullTypes.FIELD_LONG_NULL, 2L).count());
        // 8 Float
        assertEquals(1, realm.where(NullTypes.class).greaterThan(NullTypes.FIELD_FLOAT_NULL, 2F).count());
        // 9 Double
        assertEquals(1, realm.where(NullTypes.class).greaterThan(NullTypes.FIELD_DOUBLE_NULL, 2D).count());
        // 10 Date
        assertEquals(1, realm.where(NullTypes.class).greaterThan(NullTypes.FIELD_DATE_NULL,
                new Date(5000)).count());
    }

    // Querying with greaterThanOrEqualTo and table has null values in row.
    @Test
    public void greaterThanOrEqualTo_nullValuesInRow() {
        TestHelper.populateTestRealmForNullTests(realm);

        // 6 Integer
        assertEquals(1, realm.where(NullTypes.class).greaterThanOrEqualTo(NullTypes.FIELD_INTEGER_NULL, 3).count());
        // 7 Long
        assertEquals(1, realm.where(NullTypes.class).greaterThanOrEqualTo(NullTypes.FIELD_LONG_NULL, 3L).count());
        // 8 Float
        assertEquals(1, realm.where(NullTypes.class).greaterThanOrEqualTo(NullTypes.FIELD_FLOAT_NULL, 3F).count());
        // 9 Double
        assertEquals(1, realm.where(NullTypes.class).greaterThanOrEqualTo(NullTypes.FIELD_DOUBLE_NULL, 3D).count());
        // 10 Date
        assertEquals(1, realm.where(NullTypes.class).greaterThanOrEqualTo(NullTypes.FIELD_DATE_NULL,
                new Date(10000)).count());
    }

    // Querying with lessThan and table has null values in row.
    @Test
    public void lessThan_nullValuesInRow() {
        TestHelper.populateTestRealmForNullTests(realm);

        // 6 Integer
        assertEquals(1, realm.where(NullTypes.class).lessThan(NullTypes.FIELD_INTEGER_NULL, 2).count());
        // 7 Long
        assertEquals(1, realm.where(NullTypes.class).lessThan(NullTypes.FIELD_LONG_NULL, 2L).count());
        // 8 Float
        assertEquals(1, realm.where(NullTypes.class).lessThan(NullTypes.FIELD_FLOAT_NULL, 2F).count());
        // 9 Double
        assertEquals(1, realm.where(NullTypes.class).lessThan(NullTypes.FIELD_DOUBLE_NULL, 2D).count());
        // 10 Date
        assertEquals(1, realm.where(NullTypes.class).lessThan(NullTypes.FIELD_DATE_NULL,
                new Date(5000)).count());

    }

    // Querying with lessThanOrEqualTo and table has null values in row.
    @Test
    public void lessThanOrEqual_nullValuesInRow() {
        TestHelper.populateTestRealmForNullTests(realm);

        // 6 Integer
        assertEquals(1, realm.where(NullTypes.class).lessThanOrEqualTo(NullTypes.FIELD_INTEGER_NULL, 1).count());
        // 7 Long
        assertEquals(1, realm.where(NullTypes.class).lessThanOrEqualTo(NullTypes.FIELD_LONG_NULL, 1L).count());
        // 8 Float
        assertEquals(1, realm.where(NullTypes.class).lessThanOrEqualTo(NullTypes.FIELD_FLOAT_NULL, 1F).count());
        // 9 Double
        assertEquals(1, realm.where(NullTypes.class).lessThanOrEqualTo(NullTypes.FIELD_DOUBLE_NULL, 1D).count());
        // 10 Date
        assertEquals(1, realm.where(NullTypes.class).lessThanOrEqualTo(NullTypes.FIELD_DATE_NULL,
                new Date(9999)).count());
    }

    // If the RealmQuery is built on a TableView, it should not crash when used after GC.
    // See issue #1161 for more details.
    @Test
    public void buildQueryFromResultsGC() {
        // According to the testing, setting this to 10 can almost certainly trigger the GC.
        // Use 30 here can ensure GC happen. (Tested with 4.3 1G Ram and 5.0 3G Ram)
        final int count = 30;
        RealmResults<CatOwner> results = realm.where(CatOwner.class).findAll();

        for (int i = 1; i <= count; i++) {
            @SuppressWarnings({"unused"})
            byte garbage[] = TestHelper.allocGarbage(0);
            results = results.where().findAll();
            System.gc(); // if a native resource has a reference count = 0, doing GC here might lead to a crash
        }
    }

    // Test min on empty columns
    @Test
    public void min_emptyColumns() {
        RealmQuery<NullTypes> query = realm.where(NullTypes.class);
        assertNull(query.min(NullTypes.FIELD_INTEGER_NOT_NULL));
        assertNull(query.min(NullTypes.FIELD_FLOAT_NOT_NULL));
        assertNull(query.min(NullTypes.FIELD_DOUBLE_NOT_NULL));
        assertNull(query.minimumDate(NullTypes.FIELD_DATE_NOT_NULL));
    }

    // Test min on columns with all null rows
    @Test
    public void min_allNullColumns() {
        TestHelper.populateAllNullRowsForNumericTesting(realm);

        RealmQuery<NullTypes> query = realm.where(NullTypes.class);
        assertNull(query.min(NullTypes.FIELD_INTEGER_NULL));
        assertNull(query.min(NullTypes.FIELD_FLOAT_NULL));
        assertNull(query.min(NullTypes.FIELD_DOUBLE_NULL));
        assertNull(query.minimumDate(NullTypes.FIELD_DATE_NULL));
    }

    // Test min on columns with all non-null rows
    @Test
    public void min_allNonNullRows() {
        TestHelper.populateAllNonNullRowsForNumericTesting(realm);
        RealmQuery<NullTypes> query = realm.where(NullTypes.class);

        assertEquals(-1, query.min(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(-2f, query.min(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(-3d, query.min(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
        assertEquals(-2000, query.minimumDate(NullTypes.FIELD_DATE_NULL).getTime());
    }

    // Test min on columns with partial null rows
    @Test
    public void min_partialNullRows() {
        TestHelper.populatePartialNullRowsForNumericTesting(realm);
        RealmQuery<NullTypes> query = realm.where(NullTypes.class);

        assertEquals(3, query.min(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(4f, query.min(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(5d, query.min(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
    }

    // Test max on empty columns
    @Test
    public void max_emptyColumns() {
        RealmQuery<NullTypes> query = realm.where(NullTypes.class);
        assertNull(query.max(NullTypes.FIELD_INTEGER_NOT_NULL));
        assertNull(query.max(NullTypes.FIELD_FLOAT_NOT_NULL));
        assertNull(query.max(NullTypes.FIELD_DOUBLE_NOT_NULL));
        assertNull(query.maximumDate(NullTypes.FIELD_DATE_NOT_NULL));
    }

    // Test max on columns with all null rows
    @Test
    public void max_allNullColumns() {
        TestHelper.populateAllNullRowsForNumericTesting(realm);

        RealmQuery<NullTypes> query = realm.where(NullTypes.class);
        assertNull(query.max(NullTypes.FIELD_INTEGER_NULL));
        assertNull(query.max(NullTypes.FIELD_FLOAT_NULL));
        assertNull(query.max(NullTypes.FIELD_DOUBLE_NULL));
        assertNull(query.maximumDate(NullTypes.FIELD_DATE_NULL));
    }

    // Test max on columns with all non-null rows
    @Test
    public void max_allNonNullRows() {
        TestHelper.populateAllNonNullRowsForNumericTesting(realm);
        RealmQuery<NullTypes> query = realm.where(NullTypes.class);

        assertEquals(4, query.max(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(5f, query.max(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(6d, query.max(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
        assertEquals(12000, query.maximumDate(NullTypes.FIELD_DATE_NULL).getTime());
    }

    // Test max on columns with partial null rows
    @Test
    public void max_partialNullRows() {
        TestHelper.populatePartialNullRowsForNumericTesting(realm);
        RealmQuery<NullTypes> query = realm.where(NullTypes.class);

        assertEquals(4, query.max(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(5f, query.max(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(6d, query.max(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
        assertEquals(12000, query.maximumDate(NullTypes.FIELD_DATE_NULL).getTime());
    }

    // Test average on empty columns
    @Test
    public void average_emptyColumns() {
        RealmQuery<NullTypes> query = realm.where(NullTypes.class);
        assertEquals(0d, query.average(NullTypes.FIELD_INTEGER_NULL), 0d);
        assertEquals(0d, query.average(NullTypes.FIELD_FLOAT_NULL), 0d);
        assertEquals(0d, query.average(NullTypes.FIELD_DOUBLE_NULL), 0d);
    }

    // Test average on columns with all null rows
    @Test
    public void average_allNullColumns() {
        TestHelper.populateAllNullRowsForNumericTesting(realm);

        RealmQuery<NullTypes> query = realm.where(NullTypes.class);
        assertEquals(0d, query.average(NullTypes.FIELD_INTEGER_NULL), 0d);
        assertEquals(0d, query.average(NullTypes.FIELD_FLOAT_NULL), 0d);
        assertEquals(0d, query.average(NullTypes.FIELD_DOUBLE_NULL), 0d);
    }

    // Test average on columns with all non-null rows
    @Test
    public void average_allNonNullRows() {
        TestHelper.populateAllNonNullRowsForNumericTesting(realm);
        RealmQuery<NullTypes> query = realm.where(NullTypes.class);

        assertEquals(2.0, query.average(NullTypes.FIELD_INTEGER_NULL), 0d);
        assertEquals(7.0 / 3, query.average(NullTypes.FIELD_FLOAT_NULL), 0.001d);
        assertEquals(8.0 / 3, query.average(NullTypes.FIELD_DOUBLE_NULL), 0.001d);
    }

    // Test average on columns with partial null rows
    @Test
    public void average_partialNullRows() {
        TestHelper.populatePartialNullRowsForNumericTesting(realm);
        RealmQuery<NullTypes> query = realm.where(NullTypes.class);

        assertEquals(3.5, query.average(NullTypes.FIELD_INTEGER_NULL), 0d);
        assertEquals(4.5, query.average(NullTypes.FIELD_FLOAT_NULL), 0d);
        assertEquals(5.5, query.average(NullTypes.FIELD_DOUBLE_NULL), 0d);
    }

    // Test sum on empty columns
    @Test
    public void sum_emptyColumns() {
        RealmQuery<NullTypes> query = realm.where(NullTypes.class);
        assertEquals(0, query.sum(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(0f, query.sum(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(0d, query.sum(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
    }

    // Test sum on columns with all null rows
    @Test
    public void sum_allNullColumns() {
        TestHelper.populateAllNullRowsForNumericTesting(realm);

        RealmQuery<NullTypes> query = realm.where(NullTypes.class);
        assertEquals(0, query.sum(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(0f, query.sum(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(0d, query.sum(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
    }

    // Test sum on columns with all non-null rows
    @Test
    public void sum_allNonNullRows() {
        TestHelper.populateAllNonNullRowsForNumericTesting(realm);
        RealmQuery<NullTypes> query = realm.where(NullTypes.class);

        assertEquals(6, query.sum(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(7f, query.sum(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(8d, query.sum(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
    }

    // Test sum on columns with partial null rows
    @Test
    public void sum_partialNullRows() {
        TestHelper.populatePartialNullRowsForNumericTesting(realm);
        RealmQuery<NullTypes> query = realm.where(NullTypes.class);

        assertEquals(7, query.sum(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(9f, query.sum(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(11d, query.sum(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
    }

    @Test
    public void count() {
        populateTestRealm(realm, TEST_DATA_SIZE);
        assertEquals(TEST_DATA_SIZE, realm.where(AllTypes.class).count());
    }

    // Test isNull on link's nullable field.
    @Test
    public void isNull_linkField() {
        TestHelper.populateTestRealmForNullTests(realm);

        // For the link with null value, query isNull on its fields should return true.
        // 1 String
        assertEquals(2, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_STRING_NULL).count());
        // 2 Bytes
        assertEquals(2, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_BYTES_NULL).count());
        // 3 Boolean
        assertEquals(2, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_BOOLEAN_NULL).count());
        // 4 Byte
        assertEquals(2, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_BYTE_NULL).count());
        // 5 Short
        assertEquals(2, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_SHORT_NULL).count());
        // 6 Integer
        assertEquals(2, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_INTEGER_NULL).count());
        // 7 Long
        assertEquals(2, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_LONG_NULL).count());
        // 8 Float
        assertEquals(2, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_FLOAT_NULL).count());
        // 9 Double
        assertEquals(2, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_DOUBLE_NULL).count());
        // 10 Date
        assertEquals(2, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_DATE_NULL).count());
        // 11 Object
        // FIXME: Currently, Realm Core does not support isNull() query for nested link field.
        //assertEquals(1, realm.where(NullTypes.class).isNull(
        //        NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_OBJECT_NULL).count());
        try {
            realm.where(NullTypes.class).isNull(
                    NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_OBJECT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    // Test isNull on link's not-nullable field. should throw
    @Test
    public void isNull_linkFieldNotNullable() {
        TestHelper.populateTestRealmForNullTests(realm);

        // 1 String
        try {
            realm.where(NullTypes.class)
                    .isNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_STRING_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 2 Bytes
        try {
            realm.where(NullTypes.class)
                    .isNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_BYTES_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 3 Boolean
        try {
            realm.where(NullTypes.class)
                    .isNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_BOOLEAN_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 4 Byte
        try {
            realm.where(NullTypes.class)
                    .isNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_BYTE_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 5 Short
        try {
            realm.where(NullTypes.class)
                    .isNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_SHORT_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 6 Integer
        try {
            realm.where(NullTypes.class)
                    .isNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_INTEGER_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 7 Long
        try {
            realm.where(NullTypes.class)
                    .isNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_LONG_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 8 Float
        try {
            realm.where(NullTypes.class)
                    .isNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_FLOAT_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 9 Double
        try {
            realm.where(NullTypes.class)
                    .isNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_DOUBLE_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 10 Date
        try {
            realm.where(NullTypes.class)
                    .isNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_DATE_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 11 Object skipped, doesn't support equalTo query
    }

    // Test isNotNull on link's nullable field.
    @Test
    public void isNotNull_linkField() {
        TestHelper.populateTestRealmForNullTests(realm);

        // 1 String
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_STRING_NULL).count());
        // 2 Bytes
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_BYTES_NULL).count());
        // 3 Boolean
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_BOOLEAN_NULL).count());
        // 4 Byte
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_BYTE_NULL).count());
        // 5 Short
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_SHORT_NULL).count());
        // 6 Integer
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_INTEGER_NULL).count());
        // 7 Long
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_LONG_NULL).count());
        // 8 Float
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_FLOAT_NULL).count());
        // 9 Double
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_DOUBLE_NULL).count());
        // 10 Date
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_DATE_NULL).count());
        // 11 Object
        //assertEquals(1, realm.where(NullTypes.class).isNotNull(
        //        NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_OBJECT_NULL).count());
        // FIXME: Currently, Realm Core does not support isNotNull() query for nested link field.
        try {
            realm.where(NullTypes.class).isNotNull(
                    NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_OBJECT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    // Test isNotNull on link's not-nullable field. should throw
    @Test
    public void isNotNull_linkFieldNotNullable() {
        TestHelper.populateTestRealmForNullTests(realm);

        // 1 String
        try {
            realm.where(NullTypes.class)
                    .isNotNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_STRING_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 2 Bytes
        try {
            realm.where(NullTypes.class)
                    .isNotNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_BYTES_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 3 Boolean
        try {
            realm.where(NullTypes.class)
                    .isNotNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_BOOLEAN_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 4 Byte
        try {
            realm.where(NullTypes.class)
                    .isNotNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_BYTE_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 5 Short
        try {
            realm.where(NullTypes.class)
                    .isNotNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_SHORT_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 6 Integer
        try {
            realm.where(NullTypes.class)
                    .isNotNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_INTEGER_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 7 Long
        try {
            realm.where(NullTypes.class)
                    .isNotNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_LONG_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 8 Float
        try {
            realm.where(NullTypes.class)
                    .isNotNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_FLOAT_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 9 Double
        try {
            realm.where(NullTypes.class)
                    .isNotNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_DOUBLE_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 10 Date
        try {
            realm.where(NullTypes.class)
                    .isNotNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_DATE_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 11 Object skipped, RealmObject is always nullable.
    }

    // Calling isNull on fields with the RealmList type will trigger an exception
    @Test
    public void isNull_listFieldThrows() {
        try {
            realm.where(Owner.class).isNull("dogs");
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("Illegal Argument: RealmList is not nullable.", expected.getMessage());
        }

        try {
            realm.where(Cat.class).isNull("owner.dogs");
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("Illegal Argument: RealmList is not nullable.", expected.getMessage());
        }
    }

    // Calling isNotNull on fields with the RealmList type will trigger an exception
    @Test
    public void isNotNull_listFieldThrows() {
        try {
            realm.where(Owner.class).isNotNull("dogs");
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("Illegal Argument: RealmList is not nullable.", expected.getMessage());
        }

        try {
            realm.where(Cat.class).isNotNull("owner.dogs");
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("Illegal Argument: RealmList is not nullable.", expected.getMessage());
        }
    }

    // @Test Disabled because of time consuming.
    public void largeRealmMultipleThreads() throws InterruptedException {
        final int nObjects = 500000;
        final int nThreads = 3;
        final CountDownLatch latch = new CountDownLatch(nThreads);

        realm.beginTransaction();
        realm.delete(StringOnly.class);
        for (int i = 0; i < nObjects; i++) {
            StringOnly stringOnly = realm.createObject(StringOnly.class);
            stringOnly.setChars(String.format("string %d", i));
        }
        realm.commitTransaction();


        for (int i = 0; i < nThreads; i++) {
            Thread thread = new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            RealmConfiguration realmConfig = configFactory.createConfiguration();
                            Realm realm = Realm.getInstance(realmConfig);
                            RealmResults<StringOnly> realmResults = realm.allObjects(StringOnly.class);
                            int n = 0;
                            for (StringOnly ignored : realmResults) {
                                n = n + 1;
                            }
                            assertEquals(nObjects, n);
                            realm.close();
                            latch.countDown();
                        }
                    }
            );
            thread.start();
        }

        latch.await();
    }

    @Test
    public void isValid_tableQuery() {
        final RealmQuery<AllTypes> query = realm.where(AllTypes.class);

        assertTrue(query.isValid());
        populateTestRealm(realm, 1);
        // still valid if result changed
        assertTrue(query.isValid());

        realm.close();
        assertFalse(query.isValid());
    }

    @Test
    public void isValid_tableViewQuery() {
        populateTestRealm();
        final RealmQuery<AllTypes> query = realm.where(AllTypes.class).greaterThan(AllTypes.FIELD_FLOAT, 5f)
                .findAll().where();
        assertTrue(query.isValid());

        populateTestRealm(realm, 1);
        // still valid if table view changed
        assertTrue(query.isValid());

        realm.close();
        assertFalse(query.isValid());
    }

    // test for https://github.com/realm/realm-java/issues/1905
    @Test
    public void resultOfTableViewQuery() {
        populateTestRealm();

        final RealmResults<AllTypes> results = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_LONG, 3L).findAll();
        final RealmQuery<AllTypes> tableViewQuery = results.where();
        assertEquals("test data 3", tableViewQuery.findAll().first().getColumnString());
        assertEquals("test data 3", tableViewQuery.findFirst().getColumnString());
    }

    @Test
    public void isValid_linkViewQuery() {
        populateTestRealm(realm, 1);
        final RealmList<Dog> list = realm.where(AllTypes.class).findFirst().getColumnRealmList();
        final RealmQuery<Dog> query = list.where();
        final long listLength = query.count();
        assertTrue(query.isValid());

        realm.beginTransaction();
        final Dog dog = realm.createObject(Dog.class);
        dog.setName("Dog");
        list.add(dog);
        realm.commitTransaction();

        // still valid if base view changed
        assertEquals(listLength + 1, query.count());
        assertTrue(query.isValid());

        realm.close();
        assertFalse(query.isValid());
    }

    @Test
    public void isValid_removedParent() {
        populateTestRealm(realm, 1);
        final AllTypes obj = realm.where(AllTypes.class).findFirst();
        final RealmQuery<Dog> query = obj.getColumnRealmList().where();
        assertTrue(query.isValid());

        realm.beginTransaction();
        obj.deleteFromRealm();
        realm.commitTransaction();

        // invalid if parent has been removed
        assertFalse(query.isValid());
    }


    private static final List<RealmFieldType> SUPPORTED_IS_EMPTY_TYPES = Arrays.asList(
            RealmFieldType.STRING,
            RealmFieldType.BINARY,
            RealmFieldType.LIST);

    private static final List<RealmFieldType> NOT_SUPPORTED_IS_EMPTY_TYPES;
    static {
        final ArrayList<RealmFieldType> list = new ArrayList<RealmFieldType>(Arrays.asList(RealmFieldType.values()));
        list.removeAll(SUPPORTED_IS_EMPTY_TYPES);
        list.remove(RealmFieldType.UNSUPPORTED_MIXED);
        list.remove(RealmFieldType.UNSUPPORTED_TABLE);
        NOT_SUPPORTED_IS_EMPTY_TYPES = list;
    }

    private void createIsEmptyDataSet(Realm realm) {
        realm.beginTransaction();

        AllJavaTypes emptyValues = new AllJavaTypes();
        emptyValues.setFieldLong(1);
        emptyValues.setFieldString("");
        emptyValues.setFieldBinary(new byte[0]);
        emptyValues.setFieldObject(emptyValues);
        emptyValues.setFieldList(new RealmList<AllJavaTypes>());
        realm.copyToRealm(emptyValues);

        AllJavaTypes nonEmpty = new AllJavaTypes();
        nonEmpty.setFieldLong(2);
        nonEmpty.setFieldString("Foo");
        nonEmpty.setFieldBinary(new byte[]{1, 2, 3});
        nonEmpty.setFieldObject(nonEmpty);
        nonEmpty.setFieldList(new RealmList<AllJavaTypes>(emptyValues));
        realm.copyToRealmOrUpdate(nonEmpty);

        realm.commitTransaction();
    }

    @Test
    public void isEmpty() {
        createIsEmptyDataSet(realm);
        for (RealmFieldType type : SUPPORTED_IS_EMPTY_TYPES) {
            switch (type) {
                case STRING:
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_STRING).count());
                    break;
                case BINARY:
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_BINARY).count());
                    break;
                case LIST:
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LIST).count());
                    break;
                default:
                    fail("Unknown type: " + type);
            }
        }
    }

    @Test
    public void isEmpty_acrossLink() {
        createIsEmptyDataSet(realm);
        for (RealmFieldType type : SUPPORTED_IS_EMPTY_TYPES) {
            switch (type) {
                case STRING:
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_STRING).count());
                    break;
                case BINARY:
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_BINARY).count());
                    break;
                case LIST:
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_LIST).count());
                    break;
                default:
                    fail("Unknown type: " + type);
            }
        }
    }

    @Test
    public void isEmpty_illegalFieldTypeThrows() {
        for (RealmFieldType type : NOT_SUPPORTED_IS_EMPTY_TYPES) {
            try {
                switch (type) {
                    case INTEGER:
                        realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LONG).findAll();
                        break;
                    case FLOAT:
                        realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_FLOAT).findAll();
                        break;
                    case DOUBLE:
                        realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_DOUBLE).findAll();
                        break;
                    case BOOLEAN:
                        realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_BOOLEAN).findAll();
                        break;
                    case OBJECT:
                        realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_OBJECT).findAll();
                        break;
                    case DATE:
                        realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_DATE).findAll();
                        break;
                    default:
                        fail("Unknown type: " + type);
                }
                fail(type + " should throw an exception");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void isEmpty_invalidFieldNameThrows() {
        String[] fieldNames = new String[] {null, "", "foo", AllJavaTypes.FIELD_OBJECT + ".foo"};

        for (String fieldName : fieldNames) {
            try {
                realm.where(AllJavaTypes.class).isEmpty(fieldName).findAll();
                fail();
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    // not-empty test harnesses
    private static final List<RealmFieldType> SUPPORTED_IS_NOT_EMPTY_TYPES = Arrays.asList(
            RealmFieldType.STRING,
            RealmFieldType.BINARY,
            RealmFieldType.LIST);

    private static final List<RealmFieldType> NOT_SUPPORTED_IS_NOT_EMPTY_TYPES;
    static {
        final ArrayList<RealmFieldType> list = new ArrayList<RealmFieldType>(Arrays.asList(RealmFieldType.values()));
        list.removeAll(SUPPORTED_IS_NOT_EMPTY_TYPES);
        list.remove(RealmFieldType.UNSUPPORTED_MIXED);
        list.remove(RealmFieldType.UNSUPPORTED_TABLE);
        NOT_SUPPORTED_IS_NOT_EMPTY_TYPES = list;
    }

    private void createIsNotEmptyDataSet(Realm realm) {
        realm.beginTransaction();

        AllJavaTypes emptyValues = new AllJavaTypes();
        emptyValues.setFieldLong(1);
        emptyValues.setFieldString("");
        emptyValues.setFieldBinary(new byte[0]);
        emptyValues.setFieldObject(emptyValues);
        emptyValues.setFieldList(new RealmList<AllJavaTypes>());
        realm.copyToRealm(emptyValues);

        AllJavaTypes notEmpty = new AllJavaTypes();
        notEmpty.setFieldLong(2);
        notEmpty.setFieldString("Foo");
        notEmpty.setFieldBinary(new byte[]{1, 2, 3});
        notEmpty.setFieldObject(notEmpty);
        notEmpty.setFieldList(new RealmList<AllJavaTypes>(emptyValues));
        realm.copyToRealmOrUpdate(notEmpty);

        realm.commitTransaction();
    }

    @Test
    public void isNotEmpty() {
        createIsNotEmptyDataSet(realm);
        for (RealmFieldType type : SUPPORTED_IS_NOT_EMPTY_TYPES) {
            switch (type) {
                case STRING:
                    assertEquals(1, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_STRING).count());
                    break;
                case BINARY:
                    assertEquals(1, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_BINARY).count());
                    break;
                case LIST:
                    assertEquals(1, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_LIST).count());
                    break;
                default:
                    fail("Unknown type: " + type);
            }
        }
    }

    @Test
    public void isNotEmpty_acrossLink() {
        createIsNotEmptyDataSet(realm);
        for (RealmFieldType type : SUPPORTED_IS_NOT_EMPTY_TYPES) {
            switch (type) {
                case STRING:
                    assertEquals(1, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_STRING).count());
                    break;
                case BINARY:
                    assertEquals(1, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_BINARY).count());
                    break;
                case LIST:
                    assertEquals(1, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_LIST).count());
                    break;
                default:
                    fail("Unknown type: " + type);
            }
        }
    }

    @Test
    public void isNotEmpty_illegalFieldTypeThrows() {
        for (RealmFieldType type : NOT_SUPPORTED_IS_NOT_EMPTY_TYPES) {
            try {
                switch (type) {
                    case INTEGER:
                        realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_LONG).findAll();
                        break;
                    case FLOAT:
                        realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_FLOAT).findAll();
                        break;
                    case DOUBLE:
                        realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_DOUBLE).findAll();
                        break;
                    case BOOLEAN:
                        realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_BOOLEAN).findAll();
                        break;
                    case OBJECT:
                        realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_OBJECT).findAll();
                        break;
                    case DATE:
                        realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_DATE).findAll();
                        break;
                    default:
                        fail("Unknown type: " + type);
                }
                fail(type + " should throw an exception");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void isNotEmpty_invalidFieldNameThrows() {
        String[] fieldNames = new String[] {null, "", "foo", AllJavaTypes.FIELD_OBJECT + ".foo"};

        for (String fieldName : fieldNames) {
            try {
                realm.where(AllJavaTypes.class).isNotEmpty(fieldName).findAll();
                fail();
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    // Test that deep queries work on a lot of data
    @Test
    public void deepLinkListQuery() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                // Crash with i == 1000, 500, 100, 89, 85, 84
                // Doesn't crash for i == 10, 50, 75, 82, 83
                for (int i = 0; i < 84; i++) {
                    AllJavaTypes obj = realm.createObject(AllJavaTypes.class);
                    obj.setFieldLong(i + 1);
                    obj.setFieldBoolean(i % 2 == 0);
                    obj.setFieldObject(obj);

                    RealmResults<AllJavaTypes> items = realm.where(AllJavaTypes.class).findAll();
                    RealmList<AllJavaTypes> fieldList = obj.getFieldList();
                    for (int j = 0; j < items.size(); j++) {
                        fieldList.add(items.get(j));
                    }
                }
            }
        });

        for (int i = 0; i < 4; i++) {
            realm.where(AllJavaTypes.class).equalTo(
                    AllJavaTypes.FIELD_LIST + "." + AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_BOOLEAN, true)
                    .findAll();
        }
    }

    @Test
    public void findAllSorted_onSubObjectFieldThrows() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Sorting using child object fields is not supported: ");
        realm.where(AllTypes.class).findAllSorted(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_BOOLEAN);
    }

    @Test
    public void findAllSortedAsync_onSubObjectFieldThrows() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Sorting using child object fields is not supported: ");
        realm.where(AllTypes.class).findAllSortedAsync(
                AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_BOOLEAN);
    }

    @Test
    public void findAllSorted_listOnSubObjectFieldThrows() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Sorting using child object fields is not supported: ");
        String[] fieldNames = new String[1];
        fieldNames[0] = AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_BOOLEAN;
        Sort[] sorts = new Sort[1];
        sorts[0] = Sort.ASCENDING;
        realm.where(AllTypes.class).findAllSorted(fieldNames, sorts);
    }

    @Test
    public void findAllSortedAsync_listOnSubObjectFieldThrows() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Sorting using child object fields is not supported: ");
        String[] fieldNames = new String[1];
        fieldNames[0] = AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_BOOLEAN;
        Sort[] sorts = new Sort[1];
        sorts[0] = Sort.ASCENDING;
        realm.where(AllTypes.class).findAllSortedAsync(fieldNames, sorts);
    }

    // RealmQuery.distinct(): requires indexing, and type = boolean, integer, date, string
    private void populateForDistinct(Realm realm, long numberOfBlocks, long numberOfObjects, boolean withNull) {
        realm.beginTransaction();
        for (int i = 0; i < numberOfObjects * numberOfBlocks; i++) {
            for (int j = 0; j < numberOfBlocks; j++) {
                AnnotationIndexTypes obj = realm.createObject(AnnotationIndexTypes.class);
                obj.setIndexBoolean(j % 2 == 0);
                obj.setIndexLong(j);
                obj.setIndexDate(withNull ? null : new Date(1000 * j));
                obj.setIndexString(withNull ? null : "Test " + j);
                obj.setNotIndexBoolean(j % 2 == 0);
                obj.setNotIndexLong(j);
                obj.setNotIndexDate(withNull ? null : new Date(1000 * j));
                obj.setNotIndexString(withNull ? null : "Test " + j);
                obj.setFieldObject(obj);
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
        final long numberOfObjects = 10; // must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        RealmResults<AnnotationIndexTypes> distinctBool = realm.where(AnnotationIndexTypes.class).distinct(AnnotationIndexTypes.FIELD_INDEX_BOOL);
        assertEquals(2, distinctBool.size());
        for (String field : new String[]{AnnotationIndexTypes.FIELD_INDEX_LONG, AnnotationIndexTypes.FIELD_INDEX_DATE, AnnotationIndexTypes.FIELD_INDEX_STRING}) {
            RealmResults<AnnotationIndexTypes> distinct = realm.where(AnnotationIndexTypes.class).distinct(field);
            assertEquals(field, numberOfBlocks, distinct.size());
        }
    }

    @Test
    public void distinct_withNullValues() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        for (String field : new String[]{AnnotationIndexTypes.FIELD_INDEX_DATE, AnnotationIndexTypes.FIELD_INDEX_STRING}) {
            RealmResults<AnnotationIndexTypes> distinct = realm.where(AnnotationIndexTypes.class).distinct(field);
            assertEquals(field, 1, distinct.size());
        }
    }

    @Test
    public void distinct_notIndexedFields() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        for (String field : AnnotationIndexTypes.NOT_INDEX_FIELDS) {
            try {
                realm.where(AnnotationIndexTypes.class).distinct(field);
                fail(field);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void distinct_doesNotExist() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        try {
            realm.where(AnnotationIndexTypes.class).distinct("doesNotExist");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinct_invalidTypes() {
        populateTestRealm();

        for (String field : new String[]{AllTypes.FIELD_REALMOBJECT, AllTypes.FIELD_REALMLIST, AllTypes.FIELD_DOUBLE, AllTypes.FIELD_FLOAT}) {
            try {
                realm.where(AllTypes.class).distinct(field);
                fail(field);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void distinct_indexedLinkedFields() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        for (String field : AnnotationIndexTypes.INDEX_FIELDS) {
            try {
                realm.where(AnnotationIndexTypes.class).distinct(AnnotationIndexTypes.FIELD_OBJECT + "." + field);
                fail("Unsupported Index" + field + " linked field");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void distinct_notIndexedLinkedFields() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        for (String field : AnnotationIndexTypes.NOT_INDEX_FIELDS) {
            try {
                realm.where(AnnotationIndexTypes.class).distinct(AnnotationIndexTypes.FIELD_OBJECT + "." + field);
                fail("Unsupported notIndex" + field + " linked field");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void distinct_invalidTypesLinkedFields() {
        populateForDistinctInvalidTypesLinked(realm);

        try {
            realm.where(AllJavaTypes.class).distinct(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_BINARY);
        } catch (IllegalArgumentException ignored) {
        }
    }

    // distinctAsync
    private Realm openRealmInstance(String name) {
        RealmConfiguration config = configFactory.createConfiguration(name);
        Realm.deleteRealm(config);
        return Realm.getInstance(config);
    }

    @Test
    @RunTestInLooperThread
    public void distinctAsync() throws Throwable {
        final AtomicInteger changeListenerCalled = new AtomicInteger(4);
        final Realm realm = looperThread.realm;
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        final RealmResults<AnnotationIndexTypes> distinctBool = realm.where(AnnotationIndexTypes.class).distinctAsync(AnnotationIndexTypes.FIELD_INDEX_BOOL);
        final RealmResults<AnnotationIndexTypes> distinctLong = realm.where(AnnotationIndexTypes.class).distinctAsync(AnnotationIndexTypes.FIELD_INDEX_LONG);
        final RealmResults<AnnotationIndexTypes> distinctDate = realm.where(AnnotationIndexTypes.class).distinctAsync(AnnotationIndexTypes.FIELD_INDEX_DATE);
        final RealmResults<AnnotationIndexTypes> distinctString = realm.where(AnnotationIndexTypes.class).distinctAsync(AnnotationIndexTypes.FIELD_INDEX_STRING);

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
    public void distinctAsync_withNullValues () throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(2);
        final CountDownLatch signalClosedRealm = new CountDownLatch(1);
        final Throwable[] threadAssertionError = new Throwable[1];
        final Looper[] backgroundLooper = new Looper[1];
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                backgroundLooper[0] = Looper.myLooper();

                Realm asyncRealm = null;
                try {
                    Realm.asyncQueryExecutor.pause();
                    asyncRealm = openRealmInstance("testDistinctAsyncQueryWithNull");
                    final long numberOfBlocks = 25;
                    final long numberOfObjects = 10; // must be greater than 1
                    populateForDistinct(asyncRealm, numberOfBlocks, numberOfObjects, true);

                    final RealmResults<AnnotationIndexTypes> distinctDate = asyncRealm.where(AnnotationIndexTypes.class).distinctAsync(AnnotationIndexTypes.FIELD_INDEX_DATE);
                    final RealmResults<AnnotationIndexTypes> distinctString = asyncRealm.where(AnnotationIndexTypes.class).distinctAsync(AnnotationIndexTypes.FIELD_INDEX_STRING);

                    assertFalse(distinctDate.isLoaded());
                    assertTrue(distinctDate.isValid());
                    assertTrue(distinctDate.isEmpty());

                    assertFalse(distinctString.isLoaded());
                    assertTrue(distinctString.isValid());
                    assertTrue(distinctString.isEmpty());

                    Realm.asyncQueryExecutor.resume();

                    distinctDate.addChangeListener(new RealmChangeListener<RealmResults<AnnotationIndexTypes>>() {
                        @Override
                        public void onChange(RealmResults<AnnotationIndexTypes> object) {
                            assertEquals(1, distinctDate.size());
                            signalCallbackFinished.countDown();
                        }
                    });

                    distinctString.addChangeListener(new RealmChangeListener<RealmResults<AnnotationIndexTypes>>() {
                        @Override
                        public void onChange(RealmResults<AnnotationIndexTypes> object) {
                            assertEquals(1, distinctString.size());
                            signalCallbackFinished.countDown();
                        }
                    });

                    Looper.loop();
                } catch (Throwable e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;

                } finally {
                    if (signalCallbackFinished.getCount() > 0) {
                        signalCallbackFinished.countDown();
                    }
                    if (asyncRealm != null) {
                        asyncRealm.close();
                    }
                    signalClosedRealm.countDown();
                }
            }
        });

        TestHelper.exitOrThrow(executorService, signalCallbackFinished, signalClosedRealm, backgroundLooper, threadAssertionError);
    }

    @Test
    public void distinctAsync_notIndexedFields() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        for (String field : AnnotationIndexTypes.NOT_INDEX_FIELDS) {
            try {
                realm.where(AnnotationIndexTypes.class).distinctAsync(field);
                fail(field);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void distinctAsync_doesNotExist() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        try {
            realm.where(AnnotationIndexTypes.class).distinctAsync("doesNotExist");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctAsync_invalidTypes() {
        populateTestRealm(realm, TEST_DATA_SIZE);

        for (String field : new String[]{AllTypes.FIELD_REALMOBJECT, AllTypes.FIELD_REALMLIST, AllTypes.FIELD_DOUBLE, AllTypes.FIELD_FLOAT}) {
            try {
                realm.where(AllTypes.class).distinctAsync(field);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void distinctAsync_indexedLinkedFields() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        for (String field : AnnotationIndexTypes.INDEX_FIELDS) {
            try {
                realm.where(AnnotationIndexTypes.class).distinctAsync(AnnotationIndexTypes.FIELD_OBJECT + "." + field);
                fail("Unsupported " + field + " linked field");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void distinctAsync_notIndexedLinkedFields() {
        populateForDistinctInvalidTypesLinked(realm);

        try {
            realm.where(AllJavaTypes.class).distinctAsync(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_BINARY);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctMultiArgs() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        RealmQuery<AnnotationIndexTypes> query = realm.where(AnnotationIndexTypes.class);
        RealmResults<AnnotationIndexTypes> distinctMulti = query.distinct(AnnotationIndexTypes.FIELD_INDEX_BOOL, AnnotationIndexTypes.INDEX_FIELDS);
        assertEquals(numberOfBlocks, distinctMulti.size());
    }

    @Test
    public void distinctMultiArgs_switchedFieldsOrder() {
        final long numberOfBlocks = 25;
        TestHelper.populateForDistinctFieldsOrder(realm, numberOfBlocks);

        // Regardless of the block size defined above, the output size is expected to be the same, 4 in this case, due to receiving unique combinations of tuples
        RealmQuery<AnnotationIndexTypes> query = realm.where(AnnotationIndexTypes.class);
        RealmResults<AnnotationIndexTypes> distinctStringLong = query.distinct(AnnotationIndexTypes.FIELD_INDEX_STRING, AnnotationIndexTypes.FIELD_INDEX_LONG);
        RealmResults<AnnotationIndexTypes> distinctLongString = query.distinct(AnnotationIndexTypes.FIELD_INDEX_LONG, AnnotationIndexTypes.FIELD_INDEX_STRING);
        assertEquals(4, distinctStringLong.size());
        assertEquals(4, distinctLongString.size());
        assertEquals(distinctStringLong.size(), distinctLongString.size());
    }

    @Test
    public void distinctMultiArgs_emptyField() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        RealmQuery<AnnotationIndexTypes> query = realm.where(AnnotationIndexTypes.class);
        // an empty string field in the middle
        try {
            query.distinct(AnnotationIndexTypes.FIELD_INDEX_BOOL, "", AnnotationIndexTypes.FIELD_INDEX_INT);
        } catch (IllegalArgumentException ignored) {
        }
        // an empty string field at the end
        try {
            query.distinct(AnnotationIndexTypes.FIELD_INDEX_BOOL, AnnotationIndexTypes.FIELD_INDEX_INT, "");
        } catch (IllegalArgumentException ignored) {
        }
        // a null string field in the middle
        try {
            query.distinct(AnnotationIndexTypes.FIELD_INDEX_BOOL, (String)null, AnnotationIndexTypes.FIELD_INDEX_INT);
        } catch (IllegalArgumentException ignored) {
        }
        // a null string field at the end
        try {
            query.distinct(AnnotationIndexTypes.FIELD_INDEX_BOOL, AnnotationIndexTypes.FIELD_INDEX_INT, (String)null);
        } catch (IllegalArgumentException ignored) {
        }
        // (String)null makes varargs a null array.
        try {
            query.distinct(AnnotationIndexTypes.FIELD_INDEX_BOOL, (String)null);
        } catch (IllegalArgumentException ignored) {
        }
        // Two (String)null for first and varargs fields
        try {
            query.distinct((String)null, (String)null);
        } catch (IllegalArgumentException ignored) {
        }
        // "" & (String)null combination
        try {
            query.distinct("", (String)null);
        } catch (IllegalArgumentException ignored) {
        }
        // "" & (String)null combination
        try {
            query.distinct((String)null, "");
        } catch (IllegalArgumentException ignored) {
        }
        // Two empty fields tests
        try {
            query.distinct("", "");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctMultiArgs_withNullValues() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        RealmQuery<AnnotationIndexTypes> query = realm.where(AnnotationIndexTypes.class);
        RealmResults<AnnotationIndexTypes> distinctMulti = query.distinct(AnnotationIndexTypes.FIELD_INDEX_DATE, AnnotationIndexTypes.FIELD_INDEX_STRING);
        assertEquals(1, distinctMulti.size());
    }

    @Test
    public void distinctMultiArgs_notIndexedFields() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        RealmQuery<AnnotationIndexTypes> query = realm.where(AnnotationIndexTypes.class);
        try {
            query.distinct(AnnotationIndexTypes.FIELD_NOT_INDEX_STRING, AnnotationIndexTypes.NOT_INDEX_FIELDS);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctMultiArgs_doesNotExistField() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        RealmQuery<AnnotationIndexTypes> query = realm.where(AnnotationIndexTypes.class);
        try {
            query.distinct(AnnotationIndexTypes.FIELD_INDEX_INT, AnnotationIndexTypes.NONEXISTANT_MIX_FIELDS);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctMultiArgs_invalidTypesFields() {
        populateTestRealm();

        RealmQuery<AllTypes> query = realm.where(AllTypes.class);
        try {
            query.distinct(AllTypes.FIELD_REALMOBJECT, AllTypes.INVALID_TYPES_FIELDS_FOR_DISTINCT);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctMultiArgs_indexedLinkedFields() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        RealmQuery<AnnotationIndexTypes> query = realm.where(AnnotationIndexTypes.class);
        try {
            query.distinct(AnnotationIndexTypes.INDEX_LINKED_FIELD_STRING, AnnotationIndexTypes.INDEX_LINKED_FIELDS);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctMultiArgs_notIndexedLinkedFields() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        RealmQuery<AnnotationIndexTypes> query = realm.where(AnnotationIndexTypes.class);
        try {
            query.distinct(AnnotationIndexTypes.NOT_INDEX_LINKED_FILED_STRING, AnnotationIndexTypes.NOT_INDEX_LINKED_FIELDS);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctMultiArgs_invalidTypesLinkedFields() {
        populateForDistinctInvalidTypesLinked(realm);

        RealmQuery<AllJavaTypes> query = realm.where(AllJavaTypes.class);
        try {
            query.distinct(AllJavaTypes.INVALID_LINKED_BINARY_FIELD_FOR_DISTINCT, AllJavaTypes.INVALID_LINKED_TYPES_FIELDS_FOR_DISTINCT);
        } catch (IllegalArgumentException ignored) {
        }
    }
}
