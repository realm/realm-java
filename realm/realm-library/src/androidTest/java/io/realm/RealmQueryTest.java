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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.AllTypes;
import io.realm.entities.Cat;
import io.realm.entities.CatOwner;
import io.realm.entities.Dog;
import io.realm.entities.NonLatinFieldNames;
import io.realm.entities.NullTypes;
import io.realm.entities.Owner;
import io.realm.entities.StringOnly;
import io.realm.exceptions.RealmError;

public class RealmQueryTest extends AndroidTestCase {

    protected final static int TEST_DATA_SIZE = 10;

    protected Realm testRealm;

    private final static String FIELD_STRING = "columnString";
    private final static String FIELD_LONG = "columnLong";
    private final static String FIELD_FLOAT = "columnFloat";
    private final static String FIELD_DATE = "columnDate";
    private final static String FIELD_LONG_KOREAN_CHAR = "델타";
    private final static String FIELD_LONG_GREEK_CHAR = "Δέλτα";
    private final static String FIELD_FLOAT_KOREAN_CHAR = "베타";
    private final static String FIELD_FLOAT_GREEK_CHAR = "βήτα";

    private final static long DECADE_MILLIS = 10 * TimeUnit.DAYS.toMillis(365);

    @Override
    protected void setUp() throws Exception {
        RealmConfiguration realmConfig = TestHelper.createConfiguration(getContext());
        Realm.deleteRealm(realmConfig);
        testRealm = Realm.getInstance(realmConfig);
    }

    @Override
    protected void tearDown() throws Exception {
        if (testRealm != null)
            testRealm.close();
    }

    private void populateTestRealm(int objects) {
        testRealm.beginTransaction();
        testRealm.allObjects(AllTypes.class).clear();
        testRealm.allObjects(NonLatinFieldNames.class).clear();
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
        populateTestRealm(TEST_DATA_SIZE);
    }

    public void testRealmQueryBetween() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class)
                .between(FIELD_LONG, 0, 9).findAll();
        assertEquals(10, resultList.size());

        resultList = testRealm.where(AllTypes.class).beginsWith(FIELD_STRING, "test data ").findAll();
        assertEquals(TEST_OBJECTS_COUNT, resultList.size());

        resultList = testRealm.where(AllTypes.class).beginsWith(FIELD_STRING, "test data 1")
                .between(FIELD_LONG, 2, 20).findAll();
        assertEquals(10, resultList.size());

        resultList = testRealm.where(AllTypes.class).between(FIELD_LONG, 2, 20)
                .beginsWith(FIELD_STRING, "test data 1").findAll();
        assertEquals(10, resultList.size());

        assertEquals(51, testRealm.where(AllTypes.class).between(FIELD_DATE,
                new Date(0),
                new Date(DECADE_MILLIS * 50)).count());
    }

    public void testRealmQueryGreaterThan() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class)
                .greaterThan(FIELD_FLOAT, 10.234567f).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 10, resultList.size());

        resultList = testRealm.where(AllTypes.class).beginsWith(FIELD_STRING, "test data 1")
                .greaterThan(FIELD_FLOAT, 50.234567f).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 100, resultList.size());

        RealmQuery<AllTypes> query = testRealm.where(AllTypes.class).greaterThan(FIELD_FLOAT, 11.234567f);
        resultList = query.between(FIELD_LONG, 1, 20).findAll();
        assertEquals(10, resultList.size());
    }

    public void testGreaterThanDate() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList;
        resultList = testRealm.where(AllTypes.class).greaterThan(FIELD_DATE, new Date(Long.MIN_VALUE)).findAll();
        assertEquals(TEST_OBJECTS_COUNT, resultList.size());
        resultList = testRealm.where(AllTypes.class).greaterThan(FIELD_DATE, new Date(DECADE_MILLIS * -80)).findAll();
        assertEquals(179, resultList.size());
        resultList = testRealm.where(AllTypes.class).greaterThan(FIELD_DATE, new Date(0)).findAll();
        assertEquals(TEST_OBJECTS_COUNT / 2 - 1, resultList.size());
        resultList = testRealm.where(AllTypes.class).greaterThan(FIELD_DATE, new Date(DECADE_MILLIS * 80)).findAll();
        assertEquals(19, resultList.size());
        resultList = testRealm.where(AllTypes.class).greaterThan(FIELD_DATE, new Date(Long.MAX_VALUE)).findAll();
        assertEquals(0, resultList.size());
    }


    public void testRealmQueryGreaterThanOrEqualTo() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class)
                .greaterThanOrEqualTo(FIELD_FLOAT, 10.234567f).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 9, resultList.size());

        resultList = testRealm.where(AllTypes.class).beginsWith(FIELD_STRING, "test data 1")
                .greaterThanOrEqualTo(FIELD_FLOAT, 50.234567f).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 100, resultList.size());

        RealmQuery<AllTypes> query = testRealm.where(AllTypes.class)
                .greaterThanOrEqualTo(FIELD_FLOAT, 11.234567f);
        query = query.between(FIELD_LONG, 1, 20);

        resultList = query.beginsWith(FIELD_STRING, "test data 15").findAll();
        assertEquals(1, resultList.size());
    }

    public void testGreaterThanOrEqualToDate() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList;
        resultList = testRealm.where(AllTypes.class).greaterThanOrEqualTo(FIELD_DATE, new Date(Long.MIN_VALUE)).findAll();
        assertEquals(TEST_OBJECTS_COUNT, resultList.size());
        resultList = testRealm.where(AllTypes.class).greaterThanOrEqualTo(FIELD_DATE, new Date(DECADE_MILLIS * -80)).findAll();
        assertEquals(180, resultList.size());
        resultList = testRealm.where(AllTypes.class).greaterThanOrEqualTo(FIELD_DATE, new Date(0)).findAll();
        assertEquals(TEST_OBJECTS_COUNT / 2, resultList.size());
        resultList = testRealm.where(AllTypes.class).greaterThanOrEqualTo(FIELD_DATE, new Date(DECADE_MILLIS * 80)).findAll();
        assertEquals(20, resultList.size());
        resultList = testRealm.where(AllTypes.class).greaterThanOrEqualTo(FIELD_DATE, new Date(Long.MAX_VALUE)).findAll();
        assertEquals(0, resultList.size());
    }

    public void testRealmQueryOr() {
        populateTestRealm(200);

        RealmQuery<AllTypes> query = testRealm.where(AllTypes.class).equalTo(FIELD_FLOAT, 31.234567f);
        RealmResults<AllTypes> resultList = query.or().between(FIELD_LONG, 1, 20).findAll();
        assertEquals(21, resultList.size());

        resultList = query.or().equalTo(FIELD_STRING, "test data 15").findAll();
        assertEquals(21, resultList.size());

        resultList = query.or().equalTo(FIELD_STRING, "test data 117").findAll();
        assertEquals(22, resultList.size());
    }

    public void testRealmQueryNot() {
        populateTestRealm(); // create TEST_DATA_SIZE objects

        // only one object with value 5 -> TEST_DATA_SIZE-1 object with value "not 5"
        RealmResults<AllTypes> list1 = testRealm.where(AllTypes.class).not().equalTo(FIELD_LONG, 5).findAll();
        assertEquals(TEST_DATA_SIZE - 1, list1.size());

        // not().greater() and lessThenOrEqual() must be the same
        RealmResults<AllTypes> list2 = testRealm.where(AllTypes.class).not().greaterThan(FIELD_LONG, 5).findAll();
        RealmResults<AllTypes> list3 = testRealm.where(AllTypes.class).lessThanOrEqualTo(FIELD_LONG, 5).findAll();
        assertEquals(list2.size(), list3.size());
        for (int i = 0; i < list2.size(); i++) {
            assertEquals(list2.get(i).getColumnLong(), list3.get(i).getColumnLong());
        }

        // excepted result: 0, 1, 2, 5
        long expected[] = {0, 1, 2, 5};
        RealmResults<AllTypes> list4 = testRealm.where(AllTypes.class)
                .equalTo(FIELD_LONG, 5)
                .or()
                .not().beginGroup()
                .greaterThan(FIELD_LONG, 2)
                .endGroup()
                .findAll();
        assertEquals(4, list4.size());
        for (int i = 0; i < list4.size(); i++) {
            assertEquals(expected[i], list4.get(i).getColumnLong());
        }
    }

    public void testRealmQueryNotFailure() {
        // a not() alone must fail
        try {
            testRealm.where(AllTypes.class).not().findAll();
            fail();
        } catch (RuntimeException ignored) {
        }
    }

    public void testRealmQueryImplicitAnd() {
        populateTestRealm(200);

        RealmQuery<AllTypes> query = testRealm.where(AllTypes.class).equalTo(FIELD_FLOAT, 31.234567f);
        RealmResults<AllTypes> resultList = query.between(FIELD_LONG, 1, 10).findAll();
        assertEquals(0, resultList.size());

        query = testRealm.where(AllTypes.class).equalTo(FIELD_FLOAT, 81.234567f);
        resultList = query.between(FIELD_LONG, 1, 100).findAll();
        assertEquals(1, resultList.size());
    }

    public void testRealmQueryLessThan() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).
                lessThan(FIELD_FLOAT, 31.234567f).findAll();
        assertEquals(30, resultList.size());
        RealmQuery<AllTypes> query = testRealm.where(AllTypes.class).lessThan(FIELD_FLOAT, 31.234567f);
        resultList = query.between(FIELD_LONG, 1, 10).findAll();
        assertEquals(10, resultList.size());
    }

    public void testLessThanDate() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList;
        resultList = testRealm.where(AllTypes.class).lessThan(FIELD_DATE, new Date(Long.MIN_VALUE)).findAll();
        assertEquals(0, resultList.size());
        resultList = testRealm.where(AllTypes.class).lessThan(FIELD_DATE, new Date(DECADE_MILLIS * -80)).findAll();
        assertEquals(20, resultList.size());
        resultList = testRealm.where(AllTypes.class).lessThan(FIELD_DATE, new Date(0)).findAll();
        assertEquals(TEST_OBJECTS_COUNT / 2, resultList.size());
        resultList = testRealm.where(AllTypes.class).lessThan(FIELD_DATE, new Date(DECADE_MILLIS * 80)).findAll();
        assertEquals(180, resultList.size());
        resultList = testRealm.where(AllTypes.class).lessThan(FIELD_DATE, new Date(Long.MAX_VALUE)).findAll();
        assertEquals(TEST_OBJECTS_COUNT, resultList.size());
    }

    public void testRealmQueryLessThanOrEqualTo() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class)
                .lessThanOrEqualTo(FIELD_FLOAT, 31.234567f).findAll();
        assertEquals(31, resultList.size());
        resultList = testRealm.where(AllTypes.class).lessThanOrEqualTo(FIELD_FLOAT, 31.234567f)
                .between(FIELD_LONG, 11, 20).findAll();
        assertEquals(10, resultList.size());
    }

    public void testLessThanOrEqualToDate() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList;
        resultList = testRealm.where(AllTypes.class).lessThanOrEqualTo(FIELD_DATE, new Date(Long.MIN_VALUE)).findAll();
        assertEquals(0, resultList.size());
        resultList = testRealm.where(AllTypes.class).lessThanOrEqualTo(FIELD_DATE, new Date(DECADE_MILLIS * -80)).findAll();
        assertEquals(21, resultList.size());
        resultList = testRealm.where(AllTypes.class).lessThanOrEqualTo(FIELD_DATE, new Date(0)).findAll();
        assertEquals(TEST_OBJECTS_COUNT / 2 + 1, resultList.size());
        resultList = testRealm.where(AllTypes.class).lessThanOrEqualTo(FIELD_DATE, new Date(DECADE_MILLIS * 80)).findAll();
        assertEquals(181, resultList.size());
        resultList = testRealm.where(AllTypes.class).lessThanOrEqualTo(FIELD_DATE, new Date(Long.MAX_VALUE)).findAll();
        assertEquals(TEST_OBJECTS_COUNT, resultList.size());
    }

    public void testRealmQueryEqualTo() {
        populateTestRealm(200);

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class)
                .equalTo(FIELD_FLOAT, 31.234567f).findAll();
        assertEquals(1, resultList.size());
        resultList = testRealm.where(AllTypes.class).greaterThan(FIELD_FLOAT, 11.0f)
                .equalTo(FIELD_LONG, 10).findAll();
        assertEquals(1, resultList.size());
        resultList = testRealm.where(AllTypes.class).greaterThan(FIELD_FLOAT, 11.0f)
                .equalTo(FIELD_LONG, 1).findAll();
        assertEquals(0, resultList.size());
    }

    public void testEqualToDate() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList;
        resultList = testRealm.where(AllTypes.class).equalTo(FIELD_DATE, new Date(Long.MIN_VALUE)).findAll();
        assertEquals(0, resultList.size());
        resultList = testRealm.where(AllTypes.class).equalTo(FIELD_DATE, new Date(DECADE_MILLIS * -80)).findAll();
        assertEquals(1, resultList.size());
        resultList = testRealm.where(AllTypes.class).equalTo(FIELD_DATE, new Date(0)).findAll();
        assertEquals(1, resultList.size());
        resultList = testRealm.where(AllTypes.class).equalTo(FIELD_DATE, new Date(DECADE_MILLIS * 80)).findAll();
        assertEquals(1, resultList.size());
        resultList = testRealm.where(AllTypes.class).equalTo(FIELD_DATE, new Date(Long.MAX_VALUE)).findAll();
        assertEquals(0, resultList.size());
    }

    public void testRealmQueryEqualToNonLatinCharacters() {
        populateTestRealm(200);

        RealmResults<NonLatinFieldNames> resultList = testRealm.where(NonLatinFieldNames.class)
                .equalTo(FIELD_LONG_KOREAN_CHAR, 13).findAll();
        assertEquals(1, resultList.size());
        resultList = testRealm.where(NonLatinFieldNames.class)
                .greaterThan(FIELD_FLOAT_KOREAN_CHAR, 11.0f)
                .equalTo(FIELD_LONG_KOREAN_CHAR, 10).findAll();
        assertEquals(1, resultList.size());
        resultList = testRealm.where(NonLatinFieldNames.class)
                .greaterThan(FIELD_FLOAT_KOREAN_CHAR, 11.0f)
                .equalTo(FIELD_LONG_KOREAN_CHAR, 1).findAll();
        assertEquals(0, resultList.size());

        resultList = testRealm.where(NonLatinFieldNames.class)
                .equalTo(FIELD_LONG_GREEK_CHAR, 13).findAll();
        assertEquals(1, resultList.size());
        resultList = testRealm.where(NonLatinFieldNames.class)
                .greaterThan(FIELD_FLOAT_GREEK_CHAR, 11.0f)
                .equalTo(FIELD_LONG_GREEK_CHAR, 10).findAll();
        assertEquals(1, resultList.size());
        resultList = testRealm.where(NonLatinFieldNames.class)
                .greaterThan(FIELD_FLOAT_GREEK_CHAR, 11.0f)
                .equalTo(FIELD_LONG_GREEK_CHAR, 1).findAll();
        assertEquals(0, resultList.size());
    }

    public void testRealmQueryNotEqualTo() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class)
                .notEqualTo(FIELD_LONG, 31).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 1, resultList.size());

        resultList = testRealm.where(AllTypes.class).notEqualTo(FIELD_FLOAT, 11.234567f)
                .equalTo(FIELD_LONG, 10).findAll();
        assertEquals(0, resultList.size());

        resultList = testRealm.where(AllTypes.class).notEqualTo(FIELD_FLOAT, 11.234567f)
                .equalTo(FIELD_LONG, 1).findAll();
        assertEquals(1, resultList.size());
    }

    public void testNotEqualToDate() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList;
        resultList = testRealm.where(AllTypes.class).notEqualTo(FIELD_DATE, new Date(Long.MIN_VALUE)).findAll();
        assertEquals(TEST_OBJECTS_COUNT, resultList.size());
        resultList = testRealm.where(AllTypes.class).notEqualTo(FIELD_DATE, new Date(DECADE_MILLIS * -80)).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 1, resultList.size());
        resultList = testRealm.where(AllTypes.class).notEqualTo(FIELD_DATE, new Date(0)).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 1, resultList.size());
        resultList = testRealm.where(AllTypes.class).notEqualTo(FIELD_DATE, new Date(DECADE_MILLIS * 80)).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 1, resultList.size());
        resultList = testRealm.where(AllTypes.class).notEqualTo(FIELD_DATE, new Date(Long.MAX_VALUE)).findAll();
        assertEquals(TEST_OBJECTS_COUNT, resultList.size());
    }

    public void testRealmQueryContainsAndCaseSensitive() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class)
                .contains("columnString", "DaTa 0", Case.INSENSITIVE)
                .or().contains("columnString", "20")
                .findAll();
        assertEquals(3, resultList.size());

        resultList = testRealm.where(AllTypes.class).contains("columnString", "DATA").findAll();
        assertEquals(0, resultList.size());

        resultList = testRealm.where(AllTypes.class)
                .contains("columnString", "TEST", Case.INSENSITIVE).findAll();
        assertEquals(TEST_OBJECTS_COUNT, resultList.size());
    }

    public void testRealmQueryContainsAndCaseSensitiveWithNonLatinCharacters() {
        populateTestRealm();

        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        AllTypes at1 = testRealm.createObject(AllTypes.class);
        at1.setColumnString("Αλφα");
        AllTypes at2 = testRealm.createObject(AllTypes.class);
        at2.setColumnString("βήτα");
        AllTypes at3 = testRealm.createObject(AllTypes.class);
        at3.setColumnString("δέλτα");
        testRealm.commitTransaction();

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class)
                .contains("columnString", "Α", Case.INSENSITIVE)
                .or().contains("columnString", "δ")
                .findAll();
        // Without case sensitive there is 3, Α = α
        // assertEquals(3,resultList.size());
        assertEquals(2, resultList.size());

        resultList = testRealm.where(AllTypes.class).contains("columnString", "α").findAll();
        assertEquals(3, resultList.size());

        resultList = testRealm.where(AllTypes.class).contains("columnString", "Δ").findAll();
        assertEquals(0, resultList.size());

        resultList = testRealm.where(AllTypes.class).contains("columnString", "Δ",
                Case.INSENSITIVE).findAll();
        // Without case sensitive there is 1, Δ = δ
        // assertEquals(1,resultList.size());
        assertEquals(0, resultList.size());
    }

    public void testQueryWithNonExistingField() {
        try {
            testRealm.where(AllTypes.class).equalTo("NotAField", 13).findAll();
            fail("Should throw exception");
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testRealmQueryLink() {
        testRealm.beginTransaction();
        Owner owner = testRealm.createObject(Owner.class);
        Dog dog1 = testRealm.createObject(Dog.class);
        dog1.setName("Dog 1");
        dog1.setWeight(1);
        Dog dog2 = testRealm.createObject(Dog.class);
        dog2.setName("Dog 2");
        dog2.setWeight(2);
        owner.getDogs().add(dog1);
        owner.getDogs().add(dog2);
        testRealm.commitTransaction();

        // Dog.weight has index 4 which is more than the total number of columns in Owner
        // This tests exposes a subtle error where the Owner tablespec is used instead of Dog tablespec.
        RealmResults<Dog> dogs = testRealm.where(Owner.class).findFirst().getDogs().where()
                .findAllSorted("name", Sort.ASCENDING);
        Dog dog = dogs.where().equalTo("weight", 1d).findFirst();
        assertEquals(dog1, dog);
    }


    public void testSortMultiFailures() {
        // zero fields specified
        try {
            RealmResults<AllTypes> results = testRealm.where(AllTypes.class)
                    .findAllSorted(new String[]{}, new Sort[]{});
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // number of fields and sorting orders don't match
        try {
            RealmResults<AllTypes> results = testRealm.where(AllTypes.class)
                    .findAllSorted(new String[]{FIELD_STRING},
                            new Sort[]{Sort.ASCENDING, Sort.ASCENDING});
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // null is not allowed
        try {
            testRealm.where(AllTypes.class).findAllSorted((String[]) null, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            testRealm.where(AllTypes.class).findAllSorted(new String[]{FIELD_STRING}, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // non-existing field name
        try {
            testRealm.where(AllTypes.class)
                    .findAllSorted(new String[]{FIELD_STRING, "dont-exist"}, new Sort[]{Sort.ASCENDING, Sort.ASCENDING});
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testSortSingleField() {
        testRealm.beginTransaction();
        for (int i = 0; i < TEST_DATA_SIZE; i++) {
            AllTypes allTypes = testRealm.createObject(AllTypes.class);
            allTypes.setColumnLong(i);
        }
        testRealm.commitTransaction();

        RealmResults<AllTypes> sortedList = testRealm.where(AllTypes.class)
                .findAllSorted(new String[]{FIELD_LONG}, new Sort[]{Sort.DESCENDING});
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals(TEST_DATA_SIZE - 1, sortedList.first().getColumnLong());
        assertEquals(0, sortedList.last().getColumnLong());
    }

    public void testSubqueryScope() {
        populateTestRealm();
        RealmResults<AllTypes> result = testRealm.where(AllTypes.class).lessThan("columnLong", 5).findAll();
        RealmResults<AllTypes> subQueryResult = result.where().greaterThan("columnLong", 3).findAll();
        assertEquals(1, subQueryResult.size());
    }

    public void testFindFirst() {
        testRealm.beginTransaction();
        Owner owner1 = testRealm.createObject(Owner.class);
        owner1.setName("Owner 1");
        Dog dog1 = testRealm.createObject(Dog.class);
        dog1.setName("Dog 1");
        dog1.setWeight(1);
        Dog dog2 = testRealm.createObject(Dog.class);
        dog2.setName("Dog 2");
        dog2.setWeight(2);
        owner1.getDogs().add(dog1);
        owner1.getDogs().add(dog2);

        Owner owner2 = testRealm.createObject(Owner.class);
        owner2.setName("Owner 2");
        Dog dog3 = testRealm.createObject(Dog.class);
        dog3.setName("Dog 3");
        dog3.setWeight(1);
        Dog dog4 = testRealm.createObject(Dog.class);
        dog4.setName("Dog 4");
        dog4.setWeight(2);
        owner2.getDogs().add(dog3);
        owner2.getDogs().add(dog4);
        testRealm.commitTransaction();

        RealmList<Dog> dogs = testRealm.where(Owner.class).equalTo("name", "Owner 2").findFirst().getDogs();
        Dog dog = dogs.where().equalTo("name", "Dog 4").findFirst();
        assertEquals(dog4, dog);
    }

    public void testGeorgian() {
        String words[] = {"მონაცემთა ბაზა", "მიწისქვეშა გადასასვლელი", "რუსთაველის გამზირი",
                "მთავარი ქუჩა", "სადგურის მოედანი", "ველოცირაპტორების ჯოგი"};
        String sorted[] = {"ველოცირაპტორების ჯოგი", "მთავარი ქუჩა", "მიწისქვეშა გადასასვლელი",
                "მონაცემთა ბაზა", "რუსთაველის გამზირი", "სადგურის მოედანი"};

        testRealm.beginTransaction();
        testRealm.clear(StringOnly.class);
        for (String word : words) {
            StringOnly stringOnly = testRealm.createObject(StringOnly.class);
            stringOnly.setChars(word);
        }
        testRealm.commitTransaction();

        RealmResults<StringOnly> stringOnlies1 = testRealm.where(StringOnly.class).contains("chars", "მთავარი").findAll();
        assertEquals(1, stringOnlies1.size());

        RealmResults<StringOnly> stringOnlies2 = testRealm.allObjects(StringOnly.class);
        stringOnlies2.sort("chars");
        for (int i = 0; i < stringOnlies2.size(); i++) {
            assertEquals(sorted[i], stringOnlies2.get(i).getChars());
        }
    }

    // Querying a non-nullable field with null is an error
    public void testQueryNullFieldNotNullableField() {
        TestHelper.populateTestRealmForNullTests(testRealm);

        // 1 String
        try {
            testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_STRING_NOT_NULL,
                    (String) null).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 2 Bytes skipped, doesn't support equalTo query
        // 3 Boolean
        try {
            testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_BOOLEAN_NOT_NULL, (Boolean) null).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 4 Byte
        try {
            testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_BYTE_NOT_NULL, (Byte) null).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 5 Short
        try {
            testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_SHORT_NOT_NULL, (Short) null).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 6 Integer
        try {
            testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_INTEGER_NOT_NULL, (Integer) null).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 7 Long
        try {
            testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_LONG_NOT_NULL, (Long) null).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 8 Float
        try {
            testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_FLOAT_NOT_NULL, (Float) null).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 9 Double
        try {
            testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_DOUBLE_NOT_NULL, (Double) null).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 10 Date
        try {
            testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_DATE_NOT_NULL, (Date) null).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    // Querying a non-nullable field with null is an error
    public void testQueryNullFieldsNotNullableFieldIsNull() {
        // 1 String
        try {
            testRealm.where(NullTypes.class).isNull(NullTypes.FIELD_STRING_NOT_NULL).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 2 Bytes
        try {
            testRealm.where(NullTypes.class).isNull(NullTypes.FIELD_BYTES_NOT_NULL).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 3 Boolean
        try {
            testRealm.where(NullTypes.class).isNull(NullTypes.FIELD_BOOLEAN_NOT_NULL).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 4 Byte
        try {
            testRealm.where(NullTypes.class).isNull(NullTypes.FIELD_BYTE_NOT_NULL).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 5 Short
        try {
            testRealm.where(NullTypes.class).isNull(NullTypes.FIELD_SHORT_NOT_NULL).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 6 Integer
        try {
            testRealm.where(NullTypes.class).isNull(NullTypes.FIELD_INTEGER_NOT_NULL).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 7 Long
        try {
            testRealm.where(NullTypes.class).isNull(NullTypes.FIELD_LONG_NOT_NULL).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 8 Float
        try {
            testRealm.where(NullTypes.class).isNull(NullTypes.FIELD_FLOAT_NOT_NULL).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 9 Double
        try {
            testRealm.where(NullTypes.class).isNull(NullTypes.FIELD_DOUBLE_NOT_NULL).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 10 Date
        try {
            testRealm.where(NullTypes.class).isNull(NullTypes.FIELD_DATE_NOT_NULL).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    // Querying nullable fields, querying with equalTo null
    public void testQueryNullFieldsEqual() {
        TestHelper.populateTestRealmForNullTests(testRealm);

        // 1 String
        assertEquals(1, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_STRING_NULL, "Horse").findAll().size());
        assertEquals(1, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_STRING_NULL, (String) null).findAll().size());
        assertEquals(1, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_STRING_NULL, "Fish").findAll().size());
        assertEquals(0, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_STRING_NULL, "Goat").findAll().size());
        // 2 Bytes skipped, doesn't support equalTo query
        // 3 Boolean
        assertEquals(1, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_BOOLEAN_NULL, true).findAll().size());
        assertEquals(1, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_BOOLEAN_NULL, (Boolean) null).findAll().size());
        assertEquals(1, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_BOOLEAN_NULL, false).findAll().size());
        // 4 Byte
        assertEquals(1, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_BYTE_NULL, 1).findAll().size());
        assertEquals(1, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_BYTE_NULL, (byte) 1).findAll().size());
        assertEquals(1, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_BYTE_NULL, (Byte) null).findAll().size());
        assertEquals(0, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_BYTE_NULL, (byte) 42).findAll().size());
        // 5 Short for other long based columns, only test null
        assertEquals(1, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_SHORT_NULL, 1).findAll().size());
        assertEquals(1, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_SHORT_NULL, (short) 1).findAll().size());
        assertEquals(1, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_SHORT_NULL, (Short) null).findAll().size());
        assertEquals(0, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_SHORT_NULL, (short) 42).findAll().size());
        // 6 Integer
        assertEquals(1, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_INTEGER_NULL, 1).findAll().size());
        assertEquals(1, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_INTEGER_NULL, (Integer) null).findAll().size());
        assertEquals(0, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_INTEGER_NULL, 42).findAll().size());
        // 7 Long
        assertEquals(1, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_LONG_NULL, 1).findAll().size());
        assertEquals(1, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_LONG_NULL, (long) 1).findAll().size());
        assertEquals(1, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_LONG_NULL, (Long) null).findAll().size());
        assertEquals(0, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_LONG_NULL, (long) 42).findAll().size());
        // 8 Float
        assertEquals(1, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_FLOAT_NULL, 1F).findAll().size());
        assertEquals(1, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_FLOAT_NULL, (Float) null).findAll().size());
        assertEquals(0, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_FLOAT_NULL, 42F).findAll().size());
        // 9 Double
        assertEquals(1, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_DOUBLE_NULL, 1D).findAll().size());
        assertEquals(1, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_DOUBLE_NULL, (Double) null).findAll().size());
        assertEquals(0, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_DOUBLE_NULL, 42D).findAll().size());
        // 10 Date
        assertEquals(1, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_DATE_NULL, new Date(0)).findAll().size());
        assertEquals(1, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_DATE_NULL, (Date) null).findAll().size());
        assertEquals(0, testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_DATE_NULL, new Date(424242)).findAll().size());
        // 11 Object skipped, doesn't support equalTo query
    }

    // Querying nullable field for null
    public void testQueryNullFieldIsNull() {
        TestHelper.populateTestRealmForNullTests(testRealm);

        // 1 String
        assertEquals(1, testRealm.where(NullTypes.class).isNull(NullTypes.FIELD_STRING_NULL).findAll().size());
        // 2 Bytes
        assertEquals(1, testRealm.where(NullTypes.class).isNull(NullTypes.FIELD_BYTES_NULL).findAll().size());
        // 3 Boolean
        assertEquals(1, testRealm.where(NullTypes.class).isNull(NullTypes.FIELD_BOOLEAN_NULL).findAll().size());
        // 4 Byte
        assertEquals(1, testRealm.where(NullTypes.class).isNull(NullTypes.FIELD_BYTE_NULL).findAll().size());
        // 5 Short
        assertEquals(1, testRealm.where(NullTypes.class).isNull(NullTypes.FIELD_SHORT_NULL).findAll().size());
        // 6 Integer
        assertEquals(1, testRealm.where(NullTypes.class).isNull(NullTypes.FIELD_INTEGER_NULL).findAll().size());
        // 7 Long
        assertEquals(1, testRealm.where(NullTypes.class).isNull(NullTypes.FIELD_LONG_NULL).findAll().size());
        // 8 Float
        assertEquals(1, testRealm.where(NullTypes.class).isNull(NullTypes.FIELD_FLOAT_NULL).findAll().size());
        // 9 Double
        assertEquals(1, testRealm.where(NullTypes.class).isNull(NullTypes.FIELD_DOUBLE_NULL).findAll().size());
        // 10 Date
        assertEquals(1, testRealm.where(NullTypes.class).isNull(NullTypes.FIELD_DATE_NULL).findAll().size());
        // 11 Object
        assertEquals(1, testRealm.where(NullTypes.class).isNull(NullTypes.FIELD_OBJECT_NULL).findAll().size());
    }

    // Querying nullable field for not null
    public void testQueryNullFieldsNotEqual() {
        TestHelper.populateTestRealmForNullTests(testRealm);
        // 1 String
        assertEquals(2, testRealm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_STRING_NULL, "Horse").findAll().size());
        assertEquals(2, testRealm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_STRING_NULL, (String) null).findAll().size());
        // 2 Bytes skipped, doesn't support notEqualTo query
        // 3 Boolean
        assertEquals(1, testRealm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_BOOLEAN_NULL, false).findAll().size());
        assertEquals(2, testRealm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_BOOLEAN_NULL, (Boolean) null).findAll().size());
        // 4 Byte
        assertEquals(2, testRealm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_BYTE_NULL, (byte) 1).findAll().size());
        assertEquals(2, testRealm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_BYTE_NULL, (Byte) null).findAll().size());
        // 5 Short
        assertEquals(2, testRealm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_SHORT_NULL, (short) 1).findAll().size());
        assertEquals(2, testRealm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_SHORT_NULL, (Byte) null).findAll().size());
        // 6 Integer
        assertEquals(2, testRealm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_INTEGER_NULL, 1).findAll().size());
        assertEquals(2, testRealm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_INTEGER_NULL, (Integer) null).findAll().size());
        // 7 Long
        assertEquals(2, testRealm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_LONG_NULL, 1).findAll().size());
        assertEquals(2, testRealm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_LONG_NULL, (Integer) null).findAll().size());
        // 8 Float
        assertEquals(2, testRealm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_FLOAT_NULL, 1F).findAll().size());
        assertEquals(2, testRealm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_FLOAT_NULL, (Float) null).findAll().size());
        // 9 Double
        assertEquals(2, testRealm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_DOUBLE_NULL, 1D).findAll().size());
        assertEquals(2, testRealm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_DOUBLE_NULL, (Double) null).findAll().size());
        // 10 Date
        assertEquals(2, testRealm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_DATE_NULL, new Date(0)).findAll().size());
        assertEquals(2, testRealm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_DATE_NULL, (Date) null).findAll().size());
        // 11 Object skipped, doesn't support notEqualTo query
    }

    // Querying nullable field for not null
    public void testQueryNullFieldsIsNotNull() {
        TestHelper.populateTestRealmForNullTests(testRealm);

        // 1 String
        assertEquals(2, testRealm.where(NullTypes.class).isNotNull(NullTypes.FIELD_STRING_NULL).findAll().size());
        // 2 Bytes
        assertEquals(2, testRealm.where(NullTypes.class).isNotNull(NullTypes.FIELD_BYTES_NULL).findAll().size());
        // 3 Boolean
        assertEquals(2, testRealm.where(NullTypes.class).isNotNull(NullTypes.FIELD_BOOLEAN_NULL).findAll().size());
        // 4 Byte
        assertEquals(2, testRealm.where(NullTypes.class).isNotNull(NullTypes.FIELD_BYTE_NULL).findAll().size());
        // 5 Short
        assertEquals(2, testRealm.where(NullTypes.class).isNotNull(NullTypes.FIELD_SHORT_NULL).findAll().size());
        // 6 Integer
        assertEquals(2, testRealm.where(NullTypes.class).isNotNull(NullTypes.FIELD_INTEGER_NULL).findAll().size());
        // 7 Long
        assertEquals(2, testRealm.where(NullTypes.class).isNotNull(NullTypes.FIELD_LONG_NULL).findAll().size());
        // 8 Float
        assertEquals(2, testRealm.where(NullTypes.class).isNotNull(NullTypes.FIELD_FLOAT_NULL).findAll().size());
        // 9 Double
        assertEquals(2, testRealm.where(NullTypes.class).isNotNull(NullTypes.FIELD_DOUBLE_NULL).findAll().size());
        // 10 Date
        assertEquals(2, testRealm.where(NullTypes.class).isNotNull(NullTypes.FIELD_DATE_NULL).findAll().size());
        // 11 Object
        assertEquals(2, testRealm.where(NullTypes.class).isNotNull(NullTypes.FIELD_OBJECT_NULL).findAll().size());
    }

    // Querying nullable field with beginsWith - all strings begin with null
    public void testQueryNullStringsBeginsWith() {
        TestHelper.populateTestRealmForNullTests(testRealm);
        assertEquals("Fish", testRealm.where(NullTypes.class).beginsWith(NullTypes.FIELD_STRING_NULL,
                null).findFirst().getFieldStringNotNull());
    }

    // Querying nullable field with endsWith - all strings contain with null
    public void testQueryNullStringsContains() {
        TestHelper.populateTestRealmForNullTests(testRealm);
        assertEquals("Fish", testRealm.where(NullTypes.class).contains(NullTypes.FIELD_STRING_NULL,
                null).findFirst().getFieldStringNotNull());
    }

    // Querying nullable field with endsWith - all strings end with null
    public void testQueryNullStringsEndsWith() {
        TestHelper.populateTestRealmForNullTests(testRealm);
        assertEquals("Fish", testRealm.where(NullTypes.class).endsWith(NullTypes.FIELD_STRING_NULL,
                null).findFirst().getFieldStringNotNull());
    }

    // Querying with between and table has null values in row.
    public void testRealmQueryBetweenWithNullValuesInRow() {
        TestHelper.populateTestRealmForNullTests(testRealm);

        // 6 Integer
        assertEquals(1, testRealm.where(NullTypes.class).between(NullTypes.FIELD_INTEGER_NULL, 2, 4).count());
        // 7 Long
        assertEquals(1, testRealm.where(NullTypes.class).between(NullTypes.FIELD_LONG_NULL, 2L, 4L).count());
        // 8 Float
        assertEquals(1, testRealm.where(NullTypes.class).between(NullTypes.FIELD_FLOAT_NULL, 2F, 4F).count());
        // 9 Double
        assertEquals(1, testRealm.where(NullTypes.class).between(NullTypes.FIELD_DOUBLE_NULL, 2D, 4D).count());
        // 10 Date
        assertEquals(1, testRealm.where(NullTypes.class).between(NullTypes.FIELD_DATE_NULL, new Date(10000),
                new Date(20000)).count());
    }

    // Querying with greaterThan and table has null values in row.
    public void testRealmQueryGreaterThanWithNullValuesInRow() {
        TestHelper.populateTestRealmForNullTests(testRealm);

        // 6 Integer
        assertEquals(1, testRealm.where(NullTypes.class).greaterThan(NullTypes.FIELD_INTEGER_NULL, 2).count());
        // 7 Long
        assertEquals(1, testRealm.where(NullTypes.class).greaterThan(NullTypes.FIELD_LONG_NULL, 2L).count());
        // 8 Float
        assertEquals(1, testRealm.where(NullTypes.class).greaterThan(NullTypes.FIELD_FLOAT_NULL, 2F).count());
        // 9 Double
        assertEquals(1, testRealm.where(NullTypes.class).greaterThan(NullTypes.FIELD_DOUBLE_NULL, 2D).count());
        // 10 Date
        assertEquals(1, testRealm.where(NullTypes.class).greaterThan(NullTypes.FIELD_DATE_NULL,
                new Date(5000)).count());
    }

    // Querying with greaterThanOrEqualTo and table has null values in row.
    public void testRealmQueryGreaterThanOrEqualToWithNullValuesInRow() {
        TestHelper.populateTestRealmForNullTests(testRealm);

        // 6 Integer
        assertEquals(1, testRealm.where(NullTypes.class).greaterThanOrEqualTo(NullTypes.FIELD_INTEGER_NULL, 3).count());
        // 7 Long
        assertEquals(1, testRealm.where(NullTypes.class).greaterThanOrEqualTo(NullTypes.FIELD_LONG_NULL, 3L).count());
        // 8 Float
        assertEquals(1, testRealm.where(NullTypes.class).greaterThanOrEqualTo(NullTypes.FIELD_FLOAT_NULL, 3F).count());
        // 9 Double
        assertEquals(1, testRealm.where(NullTypes.class).greaterThanOrEqualTo(NullTypes.FIELD_DOUBLE_NULL, 3D).count());
        // 10 Date
        assertEquals(1, testRealm.where(NullTypes.class).greaterThanOrEqualTo(NullTypes.FIELD_DATE_NULL,
                new Date(10000)).count());
    }

    // Querying with lessThan and table has null values in row.
    public void testRealmQueryLessThanWithNullValuesInRow() {
        TestHelper.populateTestRealmForNullTests(testRealm);

        // 6 Integer
        assertEquals(1, testRealm.where(NullTypes.class).lessThan(NullTypes.FIELD_INTEGER_NULL, 2).count());
        // 7 Long
        assertEquals(1, testRealm.where(NullTypes.class).lessThan(NullTypes.FIELD_LONG_NULL, 2L).count());
        // 8 Float
        assertEquals(1, testRealm.where(NullTypes.class).lessThan(NullTypes.FIELD_FLOAT_NULL, 2F).count());
        // 9 Double
        assertEquals(1, testRealm.where(NullTypes.class).lessThan(NullTypes.FIELD_DOUBLE_NULL, 2D).count());
        // 10 Date
        assertEquals(1, testRealm.where(NullTypes.class).lessThan(NullTypes.FIELD_DATE_NULL,
                new Date(5000)).count());

    }

    // Querying with lessThanOrEqualTo and table has null values in row.
    public void testRealmQueryLessThanOrEqualWithNullValuesInRow() {
        TestHelper.populateTestRealmForNullTests(testRealm);

        // 6 Integer
        assertEquals(1, testRealm.where(NullTypes.class).lessThanOrEqualTo(NullTypes.FIELD_INTEGER_NULL, 1).count());
        // 7 Long
        assertEquals(1, testRealm.where(NullTypes.class).lessThanOrEqualTo(NullTypes.FIELD_LONG_NULL, 1L).count());
        // 8 Float
        assertEquals(1, testRealm.where(NullTypes.class).lessThanOrEqualTo(NullTypes.FIELD_FLOAT_NULL, 1F).count());
        // 9 Double
        assertEquals(1, testRealm.where(NullTypes.class).lessThanOrEqualTo(NullTypes.FIELD_DOUBLE_NULL, 1D).count());
        // 10 Date
        assertEquals(1, testRealm.where(NullTypes.class).lessThanOrEqualTo(NullTypes.FIELD_DATE_NULL,
                new Date(9999)).count());
    }

    // If the RealmQuery is built on a TableView, it should not crash when used after GC.
    // See issue #1161 for more details.
    public void testBuildQueryFromResultsGC() {
        // According to the testing, setting this to 10 can almost certainly trigger the GC.
        // Use 30 here can ensure GC happen. (Tested with 4.3 1G Ram and 5.0 3G Ram)
        final int count = 30;
        RealmResults<CatOwner> results = testRealm.where(CatOwner.class).findAll();

        for (int i = 1; i <= count; i++) {
            @SuppressWarnings({"unused"})
            byte garbage[] = TestHelper.allocGarbage(0);
            results = results.where().findAll();
            System.gc(); // if a native resource has a reference count = 0, doing GC here might lead to a crash
        }
    }

    // Test min on empty columns
    public void testMinValueForEmptyColumns() {
        RealmQuery<NullTypes> query = testRealm.where(NullTypes.class);
        assertNull(query.min(NullTypes.FIELD_INTEGER_NOT_NULL));
        assertNull(query.min(NullTypes.FIELD_FLOAT_NOT_NULL));
        assertNull(query.min(NullTypes.FIELD_DOUBLE_NOT_NULL));
        assertNull(query.minimumDate(NullTypes.FIELD_DATE_NOT_NULL));
    }

    // Test min on columns with all null rows
    public void testMinValueForAllNullColumns() {
        TestHelper.populateAllNullRowsForNumericTesting(testRealm);

        RealmQuery<NullTypes> query = testRealm.where(NullTypes.class);
        assertNull(query.min(NullTypes.FIELD_INTEGER_NULL));
        assertNull(query.min(NullTypes.FIELD_FLOAT_NULL));
        assertNull(query.min(NullTypes.FIELD_DOUBLE_NULL));
        assertNull(query.minimumDate(NullTypes.FIELD_DATE_NULL));
    }

    // Test min on columns with all non-null rows
    public void testMinForAllNonNullRows() {
        TestHelper.populateAllNonNullRowsForNumericTesting(testRealm);
        RealmQuery<NullTypes> query = testRealm.where(NullTypes.class);

        assertEquals(-1, query.min(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(-2f, query.min(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(-3d, query.min(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
        assertEquals(-2000, query.minimumDate(NullTypes.FIELD_DATE_NULL).getTime());
    }

    // Test min on columns with partial null rows
    public void testMinForPartialNullRows() {
        TestHelper.populatePartialNullRowsForNumericTesting(testRealm);
        RealmQuery<NullTypes> query = testRealm.where(NullTypes.class);

        assertEquals(3, query.min(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(4f, query.min(NullTypes.FIELD_FLOAT_NULL).floatValue());
        assertEquals(5d, query.min(NullTypes.FIELD_DOUBLE_NULL).doubleValue());
    }

    // Test max on empty columns
    public void testMaxValueForEmptyColumns() {
        RealmQuery<NullTypes> query = testRealm.where(NullTypes.class);
        assertNull(query.max(NullTypes.FIELD_INTEGER_NOT_NULL));
        assertNull(query.max(NullTypes.FIELD_FLOAT_NOT_NULL));
        assertNull(query.max(NullTypes.FIELD_DOUBLE_NOT_NULL));
        assertNull(query.maximumDate(NullTypes.FIELD_DATE_NOT_NULL));
    }

    // Test max on columns with all null rows
    public void testMaxValueForAllNullColumns() {
        TestHelper.populateAllNullRowsForNumericTesting(testRealm);

        RealmQuery<NullTypes> query = testRealm.where(NullTypes.class);
        assertNull(query.max(NullTypes.FIELD_INTEGER_NULL));
        assertNull(query.max(NullTypes.FIELD_FLOAT_NULL));
        assertNull(query.max(NullTypes.FIELD_DOUBLE_NULL));
        assertNull(query.maximumDate(NullTypes.FIELD_DATE_NULL));
    }

    // Test max on columns with all non-null rows
    public void testMaxForAllNonNullRows() {
        TestHelper.populateAllNonNullRowsForNumericTesting(testRealm);
        RealmQuery<NullTypes> query = testRealm.where(NullTypes.class);

        assertEquals(4, query.max(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(5f, query.max(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(6d, query.max(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
        assertEquals(12000, query.maximumDate(NullTypes.FIELD_DATE_NULL).getTime());
    }

    // Test max on columns with partial null rows
    public void testMaxForPartialNullRows() {
        TestHelper.populatePartialNullRowsForNumericTesting(testRealm);
        RealmQuery<NullTypes> query = testRealm.where(NullTypes.class);

        assertEquals(4, query.max(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(5f, query.max(NullTypes.FIELD_FLOAT_NULL).floatValue());
        assertEquals(6d, query.max(NullTypes.FIELD_DOUBLE_NULL).doubleValue());
        assertEquals(12000, query.maximumDate(NullTypes.FIELD_DATE_NULL).getTime());
    }

    // Test average on empty columns
    public void testAverageValueForEmptyColumns() {
        RealmQuery<NullTypes> query = testRealm.where(NullTypes.class);
        assertEquals(0d, query.average(NullTypes.FIELD_INTEGER_NULL), 0d);
        assertEquals(0d, query.average(NullTypes.FIELD_FLOAT_NULL), 0d);
        assertEquals(0d, query.average(NullTypes.FIELD_DOUBLE_NULL), 0d);
    }

    // Test average on columns with all null rows
    public void testAverageValueForAllNullColumns() {
        TestHelper.populateAllNullRowsForNumericTesting(testRealm);

        RealmQuery<NullTypes> query = testRealm.where(NullTypes.class);
        assertEquals(0d, query.average(NullTypes.FIELD_INTEGER_NULL), 0d);
        assertEquals(0d, query.average(NullTypes.FIELD_FLOAT_NULL), 0d);
        assertEquals(0d, query.average(NullTypes.FIELD_DOUBLE_NULL), 0d);
    }

    // Test average on columns with all non-null rows
    public void testAvgForAllNonNullRows() {
        TestHelper.populateAllNonNullRowsForNumericTesting(testRealm);
        RealmQuery<NullTypes> query = testRealm.where(NullTypes.class);

        assertEquals(2.0, query.average(NullTypes.FIELD_INTEGER_NULL), 0d);
        assertEquals(7.0 / 3, query.average(NullTypes.FIELD_FLOAT_NULL), 0.001d);
        assertEquals(8.0 / 3, query.average(NullTypes.FIELD_DOUBLE_NULL), 0.001d);
    }

    // Test average on columns with partial null rows
    public void testAvgForPartialNullRows() {
        TestHelper.populatePartialNullRowsForNumericTesting(testRealm);
        RealmQuery<NullTypes> query = testRealm.where(NullTypes.class);

        assertEquals(3.5, query.average(NullTypes.FIELD_INTEGER_NULL), 0d);
        assertEquals(4.5, query.average(NullTypes.FIELD_FLOAT_NULL), 0d);
        assertEquals(5.5, query.average(NullTypes.FIELD_DOUBLE_NULL), 0d);
    }

    // Test sum on empty columns
    public void testSumValueForEmptyColumns() {
        RealmQuery<NullTypes> query = testRealm.where(NullTypes.class);
        assertEquals(0, query.sum(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(0f, query.sum(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(0d, query.sum(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
    }

    // Test sum on columns with all null rows
    public void testSumValueForAllNullColumns() {
        TestHelper.populateAllNullRowsForNumericTesting(testRealm);

        RealmQuery<NullTypes> query = testRealm.where(NullTypes.class);
        assertEquals(0, query.sum(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(0f, query.sum(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(0d, query.sum(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
    }

    // Test sum on columns with all non-null rows
    public void testSumForAllNonNullRows() {
        TestHelper.populateAllNonNullRowsForNumericTesting(testRealm);
        RealmQuery<NullTypes> query = testRealm.where(NullTypes.class);

        assertEquals(6, query.sum(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(7f, query.sum(NullTypes.FIELD_FLOAT_NULL).floatValue());
        assertEquals(8d, query.sum(NullTypes.FIELD_DOUBLE_NULL).doubleValue());
    }

    // Test sum on columns with partial null rows
    public void testSumForPartialNullRows() {
        TestHelper.populatePartialNullRowsForNumericTesting(testRealm);
        RealmQuery<NullTypes> query = testRealm.where(NullTypes.class);

        assertEquals(7, query.sum(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(9f, query.sum(NullTypes.FIELD_FLOAT_NULL).floatValue());
        assertEquals(11d, query.sum(NullTypes.FIELD_DOUBLE_NULL).doubleValue());
    }

    // Test isNull on link's nullable field.
    public void testIsNullOnLinkField() {
        TestHelper.populateTestRealmForNullTests(testRealm);

        // For the link with null value, query isNull on its fields should return true.
        // 1 String
        assertEquals(2, testRealm.where(NullTypes.class).isNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_STRING_NULL).count());
        // 2 Bytes
        assertEquals(2, testRealm.where(NullTypes.class).isNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_BYTES_NULL).count());
        // 3 Boolean
        assertEquals(2, testRealm.where(NullTypes.class).isNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_BOOLEAN_NULL).count());
        // 4 Byte
        assertEquals(2, testRealm.where(NullTypes.class).isNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_BYTE_NULL).count());
        // 5 Short
        assertEquals(2, testRealm.where(NullTypes.class).isNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_SHORT_NULL).count());
        // 6 Integer
        assertEquals(2, testRealm.where(NullTypes.class).isNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_INTEGER_NULL).count());
        // 7 Long
        assertEquals(2, testRealm.where(NullTypes.class).isNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_LONG_NULL).count());
        // 8 Float
        assertEquals(2, testRealm.where(NullTypes.class).isNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_FLOAT_NULL).count());
        // 9 Double
        assertEquals(2, testRealm.where(NullTypes.class).isNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_DOUBLE_NULL).count());
        // 10 Date
        assertEquals(2, testRealm.where(NullTypes.class).isNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_DATE_NULL).count());
        // 11 Object
        // FIXME: Currently, Realm Core does not support isNull() query for nested link field.
        //assertEquals(1, testRealm.where(NullTypes.class).isNull(
        //        NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_OBJECT_NULL).count());
        try {
            testRealm.where(NullTypes.class).isNull(
                    NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_OBJECT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    // Test isNull on link's not-nullable field. should throw
    public void testIsNullOnLinkFieldNotNullable() {
        TestHelper.populateTestRealmForNullTests(testRealm);

        // 1 String
        try {
            testRealm.where(NullTypes.class)
                    .isNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_STRING_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 2 Bytes
        try {
            testRealm.where(NullTypes.class)
                    .isNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_BYTES_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 3 Boolean
        try {
            testRealm.where(NullTypes.class)
                    .isNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_BOOLEAN_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 4 Byte
        try {
            testRealm.where(NullTypes.class)
                    .isNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_BYTE_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 5 Short
        try {
            testRealm.where(NullTypes.class)
                    .isNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_SHORT_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 6 Integer
        try {
            testRealm.where(NullTypes.class)
                    .isNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_INTEGER_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 7 Long
        try {
            testRealm.where(NullTypes.class)
                    .isNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_LONG_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 8 Float
        try {
            testRealm.where(NullTypes.class)
                    .isNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_FLOAT_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 9 Double
        try {
            testRealm.where(NullTypes.class)
                    .isNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_DOUBLE_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 10 Date
        try {
            testRealm.where(NullTypes.class)
                    .isNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_DATE_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 11 Object skipped, doesn't support equalTo query
    }

    // Test isNotNull on link's nullable field.
    public void testIsNotNullOnLinkField() {
        TestHelper.populateTestRealmForNullTests(testRealm);

        // 1 String
        assertEquals(1, testRealm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_STRING_NULL).count());
        // 2 Bytes
        assertEquals(1, testRealm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_BYTES_NULL).count());
        // 3 Boolean
        assertEquals(1, testRealm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_BOOLEAN_NULL).count());
        // 4 Byte
        assertEquals(1, testRealm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_BYTE_NULL).count());
        // 5 Short
        assertEquals(1, testRealm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_SHORT_NULL).count());
        // 6 Integer
        assertEquals(1, testRealm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_INTEGER_NULL).count());
        // 7 Long
        assertEquals(1, testRealm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_LONG_NULL).count());
        // 8 Float
        assertEquals(1, testRealm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_FLOAT_NULL).count());
        // 9 Double
        assertEquals(1, testRealm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_DOUBLE_NULL).count());
        // 10 Date
        assertEquals(1, testRealm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_DATE_NULL).count());
        // 11 Object
        //assertEquals(1, testRealm.where(NullTypes.class).isNotNull(
        //        NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_OBJECT_NULL).count());
        // FIXME: Currently, Realm Core does not support isNotNull() query for nested link field.
        try {
            testRealm.where(NullTypes.class).isNotNull(
                    NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_OBJECT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    // Test isNotNull on link's not-nullable field. should throw
    public void testIsNotNullOnLinkFieldNotNullable() {
        TestHelper.populateTestRealmForNullTests(testRealm);

        // 1 String
        try {
            testRealm.where(NullTypes.class)
                    .isNotNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_STRING_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 2 Bytes
        try {
            testRealm.where(NullTypes.class)
                    .isNotNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_BYTES_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 3 Boolean
        try {
            testRealm.where(NullTypes.class)
                    .isNotNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_BOOLEAN_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 4 Byte
        try {
            testRealm.where(NullTypes.class)
                    .isNotNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_BYTE_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 5 Short
        try {
            testRealm.where(NullTypes.class)
                    .isNotNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_SHORT_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 6 Integer
        try {
            testRealm.where(NullTypes.class)
                    .isNotNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_INTEGER_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 7 Long
        try {
            testRealm.where(NullTypes.class)
                    .isNotNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_LONG_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 8 Float
        try {
            testRealm.where(NullTypes.class)
                    .isNotNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_FLOAT_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 9 Double
        try {
            testRealm.where(NullTypes.class)
                    .isNotNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_DOUBLE_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 10 Date
        try {
            testRealm.where(NullTypes.class)
                    .isNotNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_DATE_NOT_NULL);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        // 11 Object skipped, RealmObject is always nullable.
    }

    // Calling isNull on fields with the RealmList type will trigger an exception
    public void testIsNullOnRealmListFieldThrows() {
        try {
            testRealm.where(Owner.class).isNull("dogs");
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("Illegal Argument: RealmList is not nullable.", expected.getMessage());
        }

        try {
            testRealm.where(Cat.class).isNull("owner.dogs");
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("Illegal Argument: RealmList is not nullable.", expected.getMessage());
        }
    }

    // Calling isNotNull on fields with the RealmList type will trigger an exception
    public void testIsNotNullOnRealmListFieldThrows() {
        try {
            testRealm.where(Owner.class).isNotNull("dogs");
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("Illegal Argument: RealmList is not nullable.", expected.getMessage());
        }

        try {
            testRealm.where(Cat.class).isNotNull("owner.dogs");
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("Illegal Argument: RealmList is not nullable.", expected.getMessage());
        }
    }

    public void NOtestLargeRealmMultipleThreads() throws InterruptedException {
        final int nObjects = 500000;
        final int nThreads = 3;
        final CountDownLatch latch = new CountDownLatch(nThreads);

        testRealm.beginTransaction();
        testRealm.clear(StringOnly.class);
        for (int i = 0; i < nObjects; i++) {
            StringOnly stringOnly = testRealm.createObject(StringOnly.class);
            stringOnly.setChars(String.format("string %d", i));
        }
        testRealm.commitTransaction();


        for (int i = 0; i < nThreads; i++) {
            Thread thread = new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            RealmConfiguration realmConfig = TestHelper.createConfiguration(getContext());
                            Realm realm = Realm.getInstance(realmConfig);
                            RealmResults<StringOnly> realmResults = realm.allObjects(StringOnly.class);
                            int n = 0;
                            for (StringOnly stringOnly : realmResults) {
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

    public void testIsValidOfTableQuery() {
        final RealmQuery<AllTypes> query = testRealm.where(AllTypes.class);

        assertTrue(query.isValid());
        populateTestRealm(1);
        // still valid if result changed
        assertTrue(query.isValid());

        testRealm.close();
        assertFalse(query.isValid());
    }

    public void testIsValidOfTableViewQuery() {
        populateTestRealm();
        final RealmQuery<AllTypes> query = testRealm.where(AllTypes.class).greaterThan(FIELD_FLOAT, 5f)
                .findAll().where();
        assertTrue(query.isValid());

        populateTestRealm(1);
        // still valid if table view changed
        assertTrue(query.isValid());

        testRealm.close();
        assertFalse(query.isValid());
    }

    // test for https://github.com/realm/realm-java/issues/1905
    public void testResultOfTableViewQuery() {
        populateTestRealm();

        final RealmResults<AllTypes> results = testRealm.where(AllTypes.class).equalTo(AllTypes.FIELD_LONG, 3L).findAll();
        final RealmQuery<AllTypes> tableViewQuery = results.where();
        assertEquals("test data 3", tableViewQuery.findAll().first().getColumnString());
        assertEquals("test data 3", tableViewQuery.findFirst().getColumnString());
    }

    public void testIsValidOfLinkViewQuery() {
        populateTestRealm(1);
        final RealmList<Dog> list = testRealm.where(AllTypes.class).findFirst().getColumnRealmList();
        final RealmQuery<Dog> query = list.where();
        final long listLength = query.count();
        assertTrue(query.isValid());

        testRealm.beginTransaction();
        final Dog dog = testRealm.createObject(Dog.class);
        dog.setName("Dog");
        list.add(dog);
        testRealm.commitTransaction();

        // still valid if base view changed
        assertEquals(listLength + 1, query.count());
        assertTrue(query.isValid());

        testRealm.close();
        assertFalse(query.isValid());
    }

    public void testIsValidOfRemovedParent() {
        populateTestRealm(1);
        final AllTypes obj = testRealm.where(AllTypes.class).findFirst();
        final RealmQuery<Dog> query = obj.getColumnRealmList().where();
        assertTrue(query.isValid());

        testRealm.beginTransaction();
        obj.removeFromRealm();
        testRealm.commitTransaction();

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

    public void testIsEmpty() {
        createIsEmptyDataSet(testRealm);
        for (RealmFieldType type : SUPPORTED_IS_EMPTY_TYPES) {
            switch (type) {
                case STRING:
                    assertEquals(1, testRealm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_STRING).count());
                    break;
                case BINARY:
                    assertEquals(1, testRealm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_BINARY).count());
                    break;
                case LIST:
                    assertEquals(1, testRealm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LIST).count());
                    break;
                default:
                    fail("Unknown type: " + type);
            }
        }
    }

    public void testIsEmptyAcrossLink() {
        createIsEmptyDataSet(testRealm);
        for (RealmFieldType type : SUPPORTED_IS_EMPTY_TYPES) {
            switch (type) {
                case STRING:
                    assertEquals(1, testRealm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_STRING).count());
                    break;
                case BINARY:
                    assertEquals(1, testRealm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_BINARY).count());
                    break;
                case LIST:
                    assertEquals(1, testRealm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_LIST).count());
                    break;
                default:
                    fail("Unknown type: " + type);
            }
        }
    }

    public void testIsEmptyIllegalFieldTypeThrows() {
        for (RealmFieldType type : NOT_SUPPORTED_IS_EMPTY_TYPES) {
            try {
                switch (type) {
                    case INTEGER:
                        testRealm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LONG).findAll();
                        break;
                    case FLOAT:
                        testRealm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_FLOAT).findAll();
                        break;
                    case DOUBLE:
                        testRealm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_DOUBLE).findAll();
                        break;
                    case BOOLEAN:
                        testRealm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_BOOLEAN).findAll();
                        break;
                    case OBJECT:
                        testRealm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_OBJECT).findAll();
                        break;
                    case DATE:
                        testRealm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_DATE).findAll();
                        break;
                    default:
                        fail("Unknown type: " + type);
                }
                fail(type + " should throw an exception");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void testIsEmptyInvalidFieldNameThrows() {
        String[] fieldNames = new String[] {null, "", "foo", AllJavaTypes.FIELD_OBJECT + ".foo"};

        for (String fieldName : fieldNames) {
            try {
                testRealm.where(AllJavaTypes.class).isEmpty(fieldName).findAll();
                fail();
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    // Test that deep queries work on a lot of data
    public void testDeepLinkListQuery() {
        testRealm.executeTransaction(new Realm.Transaction() {
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
            RealmResults<AllJavaTypes> result = testRealm.where(AllJavaTypes.class)
                    .equalTo("fieldList.fieldObject.fieldBoolean", true)
                    .findAll();
        }
    }
}
