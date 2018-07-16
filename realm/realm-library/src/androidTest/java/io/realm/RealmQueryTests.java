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

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.AllTypes;
import io.realm.entities.AnnotationIndexTypes;
import io.realm.entities.Cat;
import io.realm.entities.CatOwner;
import io.realm.entities.Dog;
import io.realm.entities.IndexedFields;
import io.realm.entities.NoPrimaryKeyNullTypes;
import io.realm.entities.NonLatinFieldNames;
import io.realm.entities.NullTypes;
import io.realm.entities.Owner;
import io.realm.entities.PrimaryKeyAsBoxedByte;
import io.realm.entities.PrimaryKeyAsBoxedInteger;
import io.realm.entities.PrimaryKeyAsBoxedLong;
import io.realm.entities.PrimaryKeyAsBoxedShort;
import io.realm.entities.PrimaryKeyAsString;
import io.realm.entities.StringOnly;
import io.realm.rule.RunTestInLooperThread;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmQueryTests extends QueryTests {

    private void populateTestRealm(Realm testRealm, int dataSize) {
        testRealm.beginTransaction();
        testRealm.deleteAll();
        for (int i = 0; i < dataSize; ++i) {
            AllTypes allTypes = testRealm.createObject(AllTypes.class);
            allTypes.setColumnBoolean((i % 3) == 0);
            allTypes.setColumnBinary(new byte[]{1, 2, 3});
            allTypes.setColumnDate(new Date(DECADE_MILLIS * (i - (dataSize / 2))));
            allTypes.setColumnDouble(Math.PI);
            allTypes.setColumnFloat(1.2345f + i);
            allTypes.setColumnString("test data " + i);
            allTypes.setColumnLong(i);
            NonLatinFieldNames nonLatinFieldNames = testRealm.createObject(NonLatinFieldNames.class);
            nonLatinFieldNames.set델타(i);
            nonLatinFieldNames.setΔέλτα(i);
            nonLatinFieldNames.set베타(1.2345f + i);
            nonLatinFieldNames.setΒήτα(1.2345f + i);

            Dog dog = testRealm.createObject(Dog.class);
            dog.setAge(i);
            dog.setName("test data " + i);
            allTypes.setColumnRealmObject(dog);
        }
        testRealm.commitTransaction();
    }

    private void populateTestRealm() {
        populateTestRealm(realm, TEST_DATA_SIZE);
    }

    private void populateNoPrimaryKeyNullTypesRows(Realm testRealm, int dataSize) {
        testRealm.beginTransaction();
        testRealm.deleteAll();
        for (int i = 0; i < dataSize; ++i) {
            NoPrimaryKeyNullTypes noPrimaryKeyNullTypes = testRealm.createObject(NoPrimaryKeyNullTypes.class);
            noPrimaryKeyNullTypes.setFieldStringNull((i % 3) == 0 ? null : "test data " + i);
            noPrimaryKeyNullTypes.setFieldStringNotNull("test data " + i);
            noPrimaryKeyNullTypes.setFieldBooleanNull((i % 3) == 0 ? null : (i % 3) == 1);
            noPrimaryKeyNullTypes.setFieldBooleanNotNull((i % 3) == 0);
            noPrimaryKeyNullTypes.setFieldByteNull((i % 3) == 0 ? null : (byte) i);
            noPrimaryKeyNullTypes.setFieldByteNotNull((byte) i);
            noPrimaryKeyNullTypes.setFieldShortNull((i % 3) == 0 ? null : (short) i);
            noPrimaryKeyNullTypes.setFieldShortNotNull((short) i);
            noPrimaryKeyNullTypes.setFieldIntegerNull((i % 3) == 0 ? null : i);
            noPrimaryKeyNullTypes.setFieldIntegerNotNull(i);
            noPrimaryKeyNullTypes.setFieldLongNull((i % 3) == 0 ? null : (long) i);
            noPrimaryKeyNullTypes.setFieldLongNotNull((long) i);
            noPrimaryKeyNullTypes.setFieldFloatNull((i % 3) == 0 ? null : 1.2345f + i);
            noPrimaryKeyNullTypes.setFieldFloatNotNull(1.2345f + i);
            noPrimaryKeyNullTypes.setFieldDoubleNull((i % 3) == 0 ? null : Math.PI + i);
            noPrimaryKeyNullTypes.setFieldDoubleNotNull(Math.PI + i);
            noPrimaryKeyNullTypes.setFieldDateNull((i % 3) == 0 ? null : new Date(DECADE_MILLIS * (i - (dataSize / 2))));
            noPrimaryKeyNullTypes.setFieldDateNotNull(new Date(DECADE_MILLIS * (i - (dataSize / 2))));
        }
        testRealm.commitTransaction();
    }

    private void populateNoPrimaryKeyNullTypesRows() {
        populateNoPrimaryKeyNullTypesRows(realm, TEST_NO_PRIMARY_KEY_NULL_TYPES_SIZE);
    }

    private enum ThreadConfinedMethods {
        EQUAL_TO_STRING,
        EQUAL_TO_STRING_WITH_CASE,
        EQUAL_TO_BYTE,
        EQUAL_TO_BYTE_ARRAY,
        EQUAL_TO_SHORT,
        EQUAL_TO_INTEGER,
        EQUAL_TO_LONG,
        EQUAL_TO_DOUBLE,
        EQUAL_TO_FLOAT,
        EQUAL_TO_BOOLEAN,
        EQUAL_TO_DATE,

        IN_STRING,
        IN_STRING_WITH_CASE,
        IN_BYTE,
        IN_SHORT,
        IN_INTEGER,
        IN_LONG,
        IN_DOUBLE,
        IN_FLOAT,
        IN_BOOLEAN,
        IN_DATE,

        NOT_EQUAL_TO_STRING,
        NOT_EQUAL_TO_STRING_WITH_CASE,
        NOT_EQUAL_TO_BYTE,
        NOT_EQUAL_TO_BYTE_ARRAY,
        NOT_EQUAL_TO_SHORT,
        NOT_EQUAL_TO_INTEGER,
        NOT_EQUAL_TO_LONG,
        NOT_EQUAL_TO_DOUBLE,
        NOT_EQUAL_TO_FLOAT,
        NOT_EQUAL_TO_BOOLEAN,
        NOT_EQUAL_TO_DATE,

        GREATER_THAN_INTEGER,
        GREATER_THAN_LONG,
        GREATER_THAN_DOUBLE,
        GREATER_THAN_FLOAT,
        GREATER_THAN_DATE,

        GREATER_THAN_OR_EQUAL_TO_INTEGER,
        GREATER_THAN_OR_EQUAL_TO_LONG,
        GREATER_THAN_OR_EQUAL_TO_DOUBLE,
        GREATER_THAN_OR_EQUAL_TO_FLOAT,
        GREATER_THAN_OR_EQUAL_TO_DATE,

        LESS_THAN_INTEGER,
        LESS_THAN_LONG,
        LESS_THAN_DOUBLE,
        LESS_THAN_FLOAT,
        LESS_THAN_DATE,

        LESS_THAN_OR_EQUAL_TO_INTEGER,
        LESS_THAN_OR_EQUAL_TO_LONG,
        LESS_THAN_OR_EQUAL_TO_DOUBLE,
        LESS_THAN_OR_EQUAL_TO_FLOAT,
        LESS_THAN_OR_EQUAL_TO_DATE,

        BETWEEN_INTEGER,
        BETWEEN_LONG,
        BETWEEN_DOUBLE,
        BETWEEN_FLOAT,
        BETWEEN_DATE,

        CONTAINS_STRING,
        CONTAINS_STRING_WITH_CASE,

        BEGINS_WITH_STRING,
        BEGINS_WITH_STRING_WITH_CASE,

        ENDS_WITH_STRING,
        ENDS_WITH_STRING_WITH_CASE,

        LIKE_STRING,
        LIKE_STRING_WITH_CASE,

        BEGIN_GROUP,
        END_GROUP,
        OR,
        AND,
        NOT,
        IS_NULL,
        IS_NOT_NULL,
        IS_EMPTY,
        IS_NOT_EMPTY,

        IS_VALID,
        DISTINCT,
        DISTINCT_BY_MULTIPLE_FIELDS,

        SUM,
        AVERAGE,
        MIN,
        MINIMUM_DATE,
        MAX,
        MAXIMUM_DATE,
        COUNT,

        FIND_ALL,
        FIND_ALL_ASYNC,
        SORT,
        SORT_WITH_ORDER,
        SORT_WITH_MANY_ORDERS,

        FIND_FIRST,
        FIND_FIRST_ASYNC,
    }

    private static void callThreadConfinedMethod(RealmQuery<?> query, ThreadConfinedMethods method) {
        switch (method) {
            case EQUAL_TO_STRING: query.equalTo(           AllJavaTypes.FIELD_STRING,  "dummy value"); break;
            case EQUAL_TO_STRING_WITH_CASE: query.equalTo( AllJavaTypes.FIELD_STRING,  "dummy value", Case.INSENSITIVE); break;
            case EQUAL_TO_BYTE: query.equalTo(             AllJavaTypes.FIELD_BYTE,    (byte) 1); break;
            case EQUAL_TO_BYTE_ARRAY: query.equalTo(       AllJavaTypes.FIELD_BINARY,  new byte[] {0, 1, 2}); break;
            case EQUAL_TO_SHORT: query.equalTo(            AllJavaTypes.FIELD_SHORT,   (short) 1); break;
            case EQUAL_TO_INTEGER: query.equalTo(          AllJavaTypes.FIELD_INT,     1); break;
            case EQUAL_TO_LONG: query.equalTo(             AllJavaTypes.FIELD_LONG,    1L); break;
            case EQUAL_TO_DOUBLE: query.equalTo(           AllJavaTypes.FIELD_DOUBLE,  1D); break;
            case EQUAL_TO_FLOAT: query.equalTo(            AllJavaTypes.FIELD_FLOAT,   1F); break;
            case EQUAL_TO_BOOLEAN: query.equalTo(          AllJavaTypes.FIELD_BOOLEAN, true); break;
            case EQUAL_TO_DATE: query.equalTo(             AllJavaTypes.FIELD_DATE,    new Date(0L)); break;

            case IN_STRING: query.in(           AllJavaTypes.FIELD_STRING,  new String[] {"dummy value1", "dummy value2"}); break;
            case IN_STRING_WITH_CASE: query.in( AllJavaTypes.FIELD_STRING,  new String[] {"dummy value1", "dummy value2"}, Case.INSENSITIVE); break;
            case IN_BYTE: query.in(             AllJavaTypes.FIELD_BYTE,    new Byte[] {1, 2, 3}); break;
            case IN_SHORT: query.in(            AllJavaTypes.FIELD_SHORT,   new Short[] {1, 2, 3}); break;
            case IN_INTEGER: query.in(          AllJavaTypes.FIELD_INT,     new Integer[] {1, 2, 3}); break;
            case IN_LONG: query.in(             AllJavaTypes.FIELD_LONG,    new Long[] {1L, 2L, 3L}); break;
            case IN_DOUBLE: query.in(           AllJavaTypes.FIELD_DOUBLE,  new Double[] {1D, 2D, 3D}); break;
            case IN_FLOAT: query.in(            AllJavaTypes.FIELD_FLOAT,   new Float[] {1F, 2F, 3F}); break;
            case IN_BOOLEAN: query.in(          AllJavaTypes.FIELD_BOOLEAN, new Boolean[] {true, false}); break;
            case IN_DATE: query.in(             AllJavaTypes.FIELD_DATE,    new Date[] {new Date(0L)}); break;

            case NOT_EQUAL_TO_STRING: query.notEqualTo(           AllJavaTypes.FIELD_STRING,  "dummy value"); break;
            case NOT_EQUAL_TO_STRING_WITH_CASE: query.notEqualTo( AllJavaTypes.FIELD_STRING,  "dummy value", Case.INSENSITIVE); break;
            case NOT_EQUAL_TO_BYTE: query.notEqualTo(             AllJavaTypes.FIELD_BYTE,    (byte) 1); break;
            case NOT_EQUAL_TO_BYTE_ARRAY: query.notEqualTo(       AllJavaTypes.FIELD_BINARY,  new byte[] {1,2,3}); break;
            case NOT_EQUAL_TO_SHORT: query.notEqualTo(            AllJavaTypes.FIELD_SHORT,   (short) 1); break;
            case NOT_EQUAL_TO_INTEGER: query.notEqualTo(          AllJavaTypes.FIELD_INT,     1); break;
            case NOT_EQUAL_TO_LONG: query.notEqualTo(             AllJavaTypes.FIELD_LONG,    1L); break;
            case NOT_EQUAL_TO_DOUBLE: query.notEqualTo(           AllJavaTypes.FIELD_DOUBLE,  1D); break;
            case NOT_EQUAL_TO_FLOAT: query.notEqualTo(            AllJavaTypes.FIELD_FLOAT,   1F); break;
            case NOT_EQUAL_TO_BOOLEAN: query.notEqualTo(          AllJavaTypes.FIELD_BOOLEAN, true); break;
            case NOT_EQUAL_TO_DATE: query.notEqualTo(             AllJavaTypes.FIELD_DATE,    new Date(0L)); break;

            case GREATER_THAN_INTEGER: query.greaterThan( AllJavaTypes.FIELD_INT,    1); break;
            case GREATER_THAN_LONG: query.greaterThan(    AllJavaTypes.FIELD_LONG,   1L); break;
            case GREATER_THAN_DOUBLE: query.greaterThan(  AllJavaTypes.FIELD_DOUBLE, 1D); break;
            case GREATER_THAN_FLOAT: query.greaterThan(   AllJavaTypes.FIELD_FLOAT,  1F); break;
            case GREATER_THAN_DATE: query.greaterThan(    AllJavaTypes.FIELD_DATE,   new Date(0L)); break;

            case GREATER_THAN_OR_EQUAL_TO_INTEGER: query.greaterThanOrEqualTo( AllJavaTypes.FIELD_INT,    1); break;
            case GREATER_THAN_OR_EQUAL_TO_LONG: query.greaterThanOrEqualTo(    AllJavaTypes.FIELD_LONG,   1L); break;
            case GREATER_THAN_OR_EQUAL_TO_DOUBLE: query.greaterThanOrEqualTo(  AllJavaTypes.FIELD_DOUBLE, 1D); break;
            case GREATER_THAN_OR_EQUAL_TO_FLOAT: query.greaterThanOrEqualTo(   AllJavaTypes.FIELD_FLOAT,  1F); break;
            case GREATER_THAN_OR_EQUAL_TO_DATE: query.greaterThanOrEqualTo(    AllJavaTypes.FIELD_DATE,   new Date(0L)); break;

            case LESS_THAN_INTEGER: query.lessThan( AllJavaTypes.FIELD_INT,    1); break;
            case LESS_THAN_LONG: query.lessThan(    AllJavaTypes.FIELD_LONG,   1L); break;
            case LESS_THAN_DOUBLE: query.lessThan(  AllJavaTypes.FIELD_DOUBLE, 1D); break;
            case LESS_THAN_FLOAT: query.lessThan(   AllJavaTypes.FIELD_FLOAT,  1F); break;
            case LESS_THAN_DATE: query.lessThan(    AllJavaTypes.FIELD_DATE,   new Date(0L)); break;

            case LESS_THAN_OR_EQUAL_TO_INTEGER: query.lessThanOrEqualTo( AllJavaTypes.FIELD_INT,    1); break;
            case LESS_THAN_OR_EQUAL_TO_LONG: query.lessThanOrEqualTo(    AllJavaTypes.FIELD_LONG,   1L); break;
            case LESS_THAN_OR_EQUAL_TO_DOUBLE: query.lessThanOrEqualTo(  AllJavaTypes.FIELD_DOUBLE, 1D); break;
            case LESS_THAN_OR_EQUAL_TO_FLOAT: query.lessThanOrEqualTo(   AllJavaTypes.FIELD_FLOAT,  1F); break;
            case LESS_THAN_OR_EQUAL_TO_DATE: query.lessThanOrEqualTo(    AllJavaTypes.FIELD_DATE,   new Date(0L)); break;

            case BETWEEN_INTEGER: query.between( AllJavaTypes.FIELD_INT,    1, 100); break;
            case BETWEEN_LONG: query.between(    AllJavaTypes.FIELD_LONG,   1L, 100L); break;
            case BETWEEN_DOUBLE: query.between(  AllJavaTypes.FIELD_DOUBLE, 1D, 100D); break;
            case BETWEEN_FLOAT: query.between(   AllJavaTypes.FIELD_FLOAT,  1F, 100F); break;
            case BETWEEN_DATE: query.between(    AllJavaTypes.FIELD_DATE,   new Date(0L), new Date(10000L)); break;

            case CONTAINS_STRING: query.contains(           AllJavaTypes.FIELD_STRING, "dummy value"); break;
            case CONTAINS_STRING_WITH_CASE: query.contains( AllJavaTypes.FIELD_STRING, "dummy value", Case.INSENSITIVE); break;

            case BEGINS_WITH_STRING: query.beginsWith(           AllJavaTypes.FIELD_STRING, "dummy value"); break;
            case BEGINS_WITH_STRING_WITH_CASE: query.beginsWith( AllJavaTypes.FIELD_STRING, "dummy value", Case.INSENSITIVE); break;

            case ENDS_WITH_STRING: query.endsWith(           AllJavaTypes.FIELD_STRING, "dummy value"); break;
            case ENDS_WITH_STRING_WITH_CASE: query.endsWith( AllJavaTypes.FIELD_STRING, "dummy value", Case.INSENSITIVE); break;

            case LIKE_STRING: query.like(           AllJavaTypes.FIELD_STRING, "dummy value"); break;
            case LIKE_STRING_WITH_CASE: query.like( AllJavaTypes.FIELD_STRING, "dummy value", Case.INSENSITIVE); break;

            case BEGIN_GROUP: query.beginGroup(); break;
            case END_GROUP: query.endGroup(); break;
            case OR: query.or(); break;
            case AND: query.and(); break;
            case NOT: query.not(); break;
            case IS_NULL: query.isNull(          AllJavaTypes.FIELD_DATE); break;
            case IS_NOT_NULL: query.isNotNull(   AllJavaTypes.FIELD_DATE); break;
            case IS_EMPTY: query.isEmpty(        AllJavaTypes.FIELD_STRING); break;
            case IS_NOT_EMPTY: query.isNotEmpty( AllJavaTypes.FIELD_STRING); break;

            case IS_VALID: query.isValid(); break;
            case DISTINCT: query.distinct(                    AllJavaTypes.FIELD_STRING); break;
            case DISTINCT_BY_MULTIPLE_FIELDS: query.distinct( AllJavaTypes.FIELD_STRING, AllJavaTypes.FIELD_ID); break;

            case SUM: query.sum(                  AllJavaTypes.FIELD_INT); break;
            case AVERAGE: query.average(          AllJavaTypes.FIELD_INT); break;
            case MIN: query.min(                  AllJavaTypes.FIELD_INT); break;
            case MINIMUM_DATE: query.minimumDate( AllJavaTypes.FIELD_INT); break;
            case MAX: query.max(                  AllJavaTypes.FIELD_INT); break;
            case MAXIMUM_DATE: query.maximumDate( AllJavaTypes.FIELD_INT); break;
            case COUNT: query.count(); break;

            case FIND_ALL: query.findAll(); break;
            case FIND_ALL_ASYNC: query.findAllAsync(); break;
            case SORT: query.sort(AllJavaTypes.FIELD_STRING); break;
            case SORT_WITH_ORDER: query.sort(AllJavaTypes.FIELD_STRING, Sort.ASCENDING); break;
            case SORT_WITH_MANY_ORDERS: query.sort(new String[] {AllJavaTypes.FIELD_STRING, AllJavaTypes.FIELD_ID}, new Sort[] {Sort.DESCENDING, Sort.DESCENDING}); break;
            case FIND_FIRST: query.findFirst(); break;
            case FIND_FIRST_ASYNC: query.findFirstAsync(); break;

            default:
                throw new AssertionError("missing case for " + method);
        }
    }

    @Test
    public void callThreadConfinedMethodsFromWrongThread() throws Throwable {
        final RealmQuery<AllJavaTypes> query = realm.where(AllJavaTypes.class);

        final CountDownLatch testFinished = new CountDownLatch(1);

        final String expectedMessage;
        //noinspection TryWithIdenticalCatches
        try {
            final Field expectedMessageField = BaseRealm.class.getDeclaredField("INCORRECT_THREAD_MESSAGE");
            expectedMessageField.setAccessible(true);
            expectedMessage = (String) expectedMessageField.get(null);
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }

        final Thread thread = new Thread("callThreadConfinedMethodsFromWrongThread") {
            @Override
            public void run() {
                try {
                    for (ThreadConfinedMethods method : ThreadConfinedMethods.values()) {
                        try {
                            callThreadConfinedMethod(query, method);
                            fail("IllegalStateException must be thrown.");
                        } catch (IllegalStateException e) {
                            assertEquals(expectedMessage, e.getMessage());
                        }
                    }
                } finally {
                    testFinished.countDown();
                }
            }
        };
        thread.start();

        TestHelper.awaitOrFail(testFinished);
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
                .greaterThan(AllTypes.FIELD_FLOAT, 10.2345f).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 10, resultList.size());

        resultList = realm.where(AllTypes.class).beginsWith(AllTypes.FIELD_STRING, "test data 1")
                .greaterThan(AllTypes.FIELD_FLOAT, 150.2345f).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 150, resultList.size());

        RealmQuery<AllTypes> query = realm.where(AllTypes.class).greaterThan(AllTypes.FIELD_FLOAT, 11.2345f);
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
                .greaterThanOrEqualTo(AllTypes.FIELD_FLOAT, 10.2345f).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 9, resultList.size());

        resultList = realm.where(AllTypes.class).beginsWith(AllTypes.FIELD_STRING, "test data 1")
                .greaterThanOrEqualTo(AllTypes.FIELD_FLOAT, 50.2345f).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 100, resultList.size());

        RealmQuery<AllTypes> query = realm.where(AllTypes.class)
                .greaterThanOrEqualTo(AllTypes.FIELD_FLOAT, 11.2345f);
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

        RealmQuery<AllTypes> query = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_FLOAT, 31.2345f);
        RealmResults<AllTypes> resultList = query.or().between(AllTypes.FIELD_LONG, 1, 20).findAll();
        assertEquals(21, resultList.size());

        resultList = query.or().equalTo(AllTypes.FIELD_STRING, "test data 15").findAll();
        assertEquals(21, resultList.size());

        resultList = query.or().equalTo(AllTypes.FIELD_STRING, "test data 117").findAll();
        assertEquals(22, resultList.size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void or_missingFilters() {
        realm.where(AllTypes.class).or().findAll();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void or_missingFilterBefore() {
        realm.where(AllTypes.class).or().equalTo(AllTypes.FIELD_FLOAT, 31.2345f).findAll();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void or_missingFilterAfter() {
        realm.where(AllTypes.class).or().equalTo(AllTypes.FIELD_FLOAT, 31.2345f).findAll();
    }

    @Test
    public void not() {
        populateTestRealm(); // create TEST_DATA_SIZE objects

        // Only one object with value 5 -> TEST_DATA_SIZE-1 object with value "not 5".
        RealmResults<AllTypes> list1 = realm.where(AllTypes.class).not().equalTo(AllTypes.FIELD_LONG, 5).findAll();
        assertEquals(TEST_DATA_SIZE - 1, list1.size());

        // not().greater() and lessThenOrEqual() must be the same.
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

        RealmQuery<AllTypes> query = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_FLOAT, 31.2345f);
        RealmResults<AllTypes> resultList = query.between(AllTypes.FIELD_LONG, 1, 10).findAll();
        assertEquals(0, resultList.size());

        query = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_FLOAT, 81.2345f);
        resultList = query.between(AllTypes.FIELD_LONG, 1, 100).findAll();
        assertEquals(1, resultList.size());
    }
    
    @Test
    public void and_explicit() {
        populateTestRealm(realm, 200);

        RealmQuery<AllTypes> query = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_FLOAT, 31.2345f);
        RealmResults<AllTypes> resultList = query.and().between(AllTypes.FIELD_LONG, 1, 10).findAll();
        assertEquals(0, resultList.size());

        query = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_FLOAT, 81.2345f);
        resultList = query.and().between(AllTypes.FIELD_LONG, 1, 100).findAll();
        assertEquals(1, resultList.size());
    }

    @Test
    public void lessThan() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(realm, TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).
                lessThan(AllTypes.FIELD_FLOAT, 31.2345f).findAll();
        assertEquals(30, resultList.size());
        RealmQuery<AllTypes> query = realm.where(AllTypes.class).lessThan(AllTypes.FIELD_FLOAT, 31.2345f);
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
                .lessThanOrEqualTo(AllTypes.FIELD_FLOAT, 31.2345f).findAll();
        assertEquals(31, resultList.size());
        resultList = realm.where(AllTypes.class).lessThanOrEqualTo(AllTypes.FIELD_FLOAT, 31.2345f)
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
                .equalTo(AllTypes.FIELD_FLOAT, 31.2345f).findAll();
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

    private void doTestForInString(String targetField) {
        populateNoPrimaryKeyNullTypesRows();
        RealmResults<NoPrimaryKeyNullTypes> resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new String[]{"test data 14"}).findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new String[]{"test data 14", "test data 118", "test data 31", "test data 199"}).findAll();
        assertEquals(4, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new String[]{"TEST data 14", "test data 118", "test data 31", "test DATA 199"}, Case.INSENSITIVE).findAll();
        assertEquals(4, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).not().in(targetField, new String[]{"TEST data 14", "test data 118", "test data 31", "test DATA 199"}, Case.INSENSITIVE).findAll();
        assertEquals(196, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).not().in(targetField, new String[]{"TEST data 14", "test data 118", "test data 31", "test DATA 199"}, Case.INSENSITIVE).findAll();
        assertEquals(196, resultList.size());

        // Empty input always produces zero results
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, (String[]) null).findAll();
        assertTrue(resultList.isEmpty());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new String[]{}).findAll();
        assertTrue(resultList.isEmpty());
    }

    private void doTestForInBoolean(String targetField, int expected1, int expected2, int expected3, int expected4) {
        populateNoPrimaryKeyNullTypesRows();
        RealmResults<NoPrimaryKeyNullTypes> resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Boolean[]{false}).findAll();
        assertEquals(expected1, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Boolean[]{true}).findAll();
        assertEquals(expected2, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Boolean[]{true, false}).findAll();
        assertEquals(expected3, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).not().in(targetField, new Boolean[]{true, false}).findAll();
        assertEquals(expected4, resultList.size());

        // Empty input always produces zero results
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, (Boolean[]) null).findAll();
        assertTrue(resultList.isEmpty());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Boolean[]{}).findAll();
        assertTrue(resultList.isEmpty());
    }

    private void doTestForInDate(String targetField) {
        populateNoPrimaryKeyNullTypesRows();
        RealmResults<NoPrimaryKeyNullTypes> resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Date[]{new Date(DECADE_MILLIS * -80)}).findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Date[]{new Date(0)}).findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Date[]{new Date(DECADE_MILLIS * -80), new Date(0)}).findAll();
        assertEquals(2, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).not().in(targetField, new Date[]{new Date(DECADE_MILLIS * -80), new Date(0)}).findAll();
        assertEquals(198, resultList.size());

        // Empty input always produces zero results
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, (Date[]) null).findAll();
        assertTrue(resultList.isEmpty());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Date[]{}).findAll();
        assertTrue(resultList.isEmpty());
    }

    private void doTestForInDouble(String targetField) {
        populateNoPrimaryKeyNullTypesRows();
        RealmResults<NoPrimaryKeyNullTypes> resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Double[]{Math.PI + 1}).findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Double[]{Math.PI + 2}).findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Double[]{Math.PI + 1, Math.PI + 2}).findAll();
        assertEquals(2, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).not().in(targetField, new Double[]{Math.PI + 1, Math.PI + 2}).findAll();
        assertEquals(198, resultList.size());

        // Empty input always produces zero results
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, (Double[]) null).findAll();
        assertTrue(resultList.isEmpty());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Double[]{}).findAll();
        assertTrue(resultList.isEmpty());
    }

    private void doTestForInFloat(String targetField) {
        populateNoPrimaryKeyNullTypesRows();
        RealmResults<NoPrimaryKeyNullTypes> resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Float[]{1.2345f + 1}).findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Float[]{1.2345f + 2}).findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Float[]{1.2345f + 1, 1.2345f + 2}).findAll();
        assertEquals(2, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).not().in(targetField, new Float[]{1.2345f + 1, 1.2345f + 2}).findAll();
        assertEquals(198, resultList.size());

        // Empty input always produces zero results
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, (Float[]) null).findAll();
        assertTrue(resultList.isEmpty());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Float[]{}).findAll();
        assertTrue(resultList.isEmpty());
    }

    private void doTestForInByte(String targetField) {
        populateNoPrimaryKeyNullTypesRows();
        RealmResults<NoPrimaryKeyNullTypes> resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Byte[]{11}).findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Byte[]{13}).findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Byte[]{11, 13, 16, 98}).findAll();
        assertEquals(4, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).not().in(targetField, new Byte[]{11, 13, 16, 98}).findAll();
        assertEquals(196, resultList.size());

        // Empty input always produces zero results
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, (Byte[]) null).findAll();
        assertTrue(resultList.isEmpty());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Byte[]{}).findAll();
        assertTrue(resultList.isEmpty());
    }

    private void doTestForInShort(String targetField) {
        populateNoPrimaryKeyNullTypesRows();
        RealmResults<NoPrimaryKeyNullTypes> resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Short[]{11}).findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Short[]{4}).findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Short[]{2, 4, 5, 8}).findAll();
        assertEquals(4, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).not().in(targetField, new Short[]{2, 4, 5, 8}).findAll();
        assertEquals(196, resultList.size());

        // Empty input always produces zero results
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, (Float[]) null).findAll();
        assertTrue(resultList.isEmpty());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Float[]{}).findAll();
        assertTrue(resultList.isEmpty());
    }

    private void doTestForInInteger(String targetField) {
        populateNoPrimaryKeyNullTypesRows();
        RealmResults<NoPrimaryKeyNullTypes> resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Integer[]{11}).findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Integer[]{1}).findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Integer[]{1, 2, 4, 5}).findAll();
        assertEquals(4, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).not().in(targetField, new Integer[]{1, 2, 4, 5}).findAll();
        assertEquals(196, resultList.size());

        // Empty input always produces zero results
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, (Integer[]) null).findAll();
        assertTrue(resultList.isEmpty());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Integer[]{}).findAll();
        assertTrue(resultList.isEmpty());
    }

    private void doTestForInLong(String targetField) {
        populateNoPrimaryKeyNullTypesRows();
        RealmResults<NoPrimaryKeyNullTypes> resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Long[]{11l}).findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Long[]{13l}).findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Long[]{13l, 14l, 16l, 98l}).findAll();
        assertEquals(4, resultList.size());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).not().in(targetField, new Long[]{13l, 14l, 16l, 98l}).findAll();
        assertEquals(196, resultList.size());

        // Empty input always produces zero results
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, (Long[]) null).findAll();
        assertTrue(resultList.isEmpty());
        resultList = realm.where(NoPrimaryKeyNullTypes.class).in(targetField, new Long[]{}).findAll();
        assertTrue(resultList.isEmpty());
    }

    @Test
    public void in_stringNotNull() {
        doTestForInString(NoPrimaryKeyNullTypes.FIELD_STRING_NOT_NULL);
        try {
            realm.where(NoPrimaryKeyNullTypes.class).not().in(NoPrimaryKeyNullTypes.FIELD_STRING_NOT_NULL, new String[]{"TEST data 14", "test data 118", null, "test DATA 199"}, Case.INSENSITIVE).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void in_stringNull() {
        doTestForInString(NoPrimaryKeyNullTypes.FIELD_STRING_NULL);
        RealmResults<NoPrimaryKeyNullTypes> resultList = realm.where(NoPrimaryKeyNullTypes.class).not().in(NoPrimaryKeyNullTypes.FIELD_STRING_NULL, new String[]{"TEST data 14", "test data 118", null, "test DATA 199"}, Case.INSENSITIVE).findAll();
        assertEquals(130, resultList.size());
    }

    @Test
    public void in_booleanNotNull() {
        doTestForInBoolean(NoPrimaryKeyNullTypes.FIELD_BOOLEAN_NOT_NULL, 133, 67, 200, 0);
        try {
            realm.where(NoPrimaryKeyNullTypes.class).not().in(NoPrimaryKeyNullTypes.FIELD_BOOLEAN_NOT_NULL, new Boolean[]{true, null, false}).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void in_booleanNull() {
        doTestForInBoolean(NoPrimaryKeyNullTypes.FIELD_BOOLEAN_NULL, 66, 67, 133, 67);
        RealmResults<NoPrimaryKeyNullTypes> resultList = realm.where(NoPrimaryKeyNullTypes.class).not().in(NoPrimaryKeyNullTypes.FIELD_BOOLEAN_NULL, new Boolean[]{true, null, false}).findAll();
        assertEquals(0, resultList.size());
    }

    @Test
    public void in_dateNotNull() {
        doTestForInDate(NoPrimaryKeyNullTypes.FIELD_DATE_NOT_NULL);
        try {
            realm.where(NoPrimaryKeyNullTypes.class).not().in(NoPrimaryKeyNullTypes.FIELD_DATE_NOT_NULL, new Date[]{new Date(DECADE_MILLIS * -80), null, new Date(0)}).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void in_dateNull() {
        doTestForInDate(NoPrimaryKeyNullTypes.FIELD_DATE_NULL);
        RealmResults<NoPrimaryKeyNullTypes> resultList = realm.where(NoPrimaryKeyNullTypes.class).not().in(NoPrimaryKeyNullTypes.FIELD_DATE_NULL, new Date[]{new Date(DECADE_MILLIS * -80), null, new Date(0)}).findAll();
        assertEquals(131, resultList.size());
    }

    @Test
    public void in_doubleNotNull() {
        doTestForInDouble(NoPrimaryKeyNullTypes.FIELD_DOUBLE_NOT_NULL);
        try {
            realm.where(NoPrimaryKeyNullTypes.class).not().in(NoPrimaryKeyNullTypes.FIELD_DOUBLE_NOT_NULL, new Double[]{Math.PI + 1, null, Math.PI + 2}).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void in_doubleNull() {
        doTestForInDouble(NoPrimaryKeyNullTypes.FIELD_DOUBLE_NULL);
        RealmResults<NoPrimaryKeyNullTypes> resultList = realm.where(NoPrimaryKeyNullTypes.class).not().in(NoPrimaryKeyNullTypes.FIELD_DOUBLE_NULL, new Double[]{Math.PI + 1, null, Math.PI + 2}).findAll();
        assertEquals(131, resultList.size());
    }

    @Test
    public void in_floatNotNull() {
        doTestForInFloat(NoPrimaryKeyNullTypes.FIELD_FLOAT_NOT_NULL);
        try {
            realm.where(NoPrimaryKeyNullTypes.class).not().in(NoPrimaryKeyNullTypes.FIELD_FLOAT_NOT_NULL, new Float[]{1.2345f + 1, null, 1.2345f + 2}).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void in_floatNull() {
        doTestForInFloat(NoPrimaryKeyNullTypes.FIELD_FLOAT_NULL);
        RealmResults<NoPrimaryKeyNullTypes> resultList = realm.where(NoPrimaryKeyNullTypes.class).not().in(NoPrimaryKeyNullTypes.FIELD_FLOAT_NULL, new Float[]{1.2345f + 1, null, 1.2345f + 2}).findAll();
        assertEquals(131, resultList.size());
    }

    @Test
    public void in_byteNotNull() {
        doTestForInByte(NoPrimaryKeyNullTypes.FIELD_BYTE_NOT_NULL);
        try {
            realm.where(NoPrimaryKeyNullTypes.class).not().in(NoPrimaryKeyNullTypes.FIELD_BYTE_NOT_NULL, new Byte[]{11, null, 13, 99}).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void in_byteNull() {
        doTestForInByte(NoPrimaryKeyNullTypes.FIELD_BYTE_NULL);
        RealmResults resultList = realm.where(NoPrimaryKeyNullTypes.class).not().in(NoPrimaryKeyNullTypes.FIELD_BYTE_NULL, new Byte[]{11, null, 13, 99}).findAll();
        assertEquals(131, resultList.size());
    }

    @Test
    public void in_shortNotNull() {
        doTestForInShort(NoPrimaryKeyNullTypes.FIELD_SHORT_NOT_NULL);
        try {
            realm.where(NoPrimaryKeyNullTypes.class).not().in(NoPrimaryKeyNullTypes.FIELD_SHORT_NOT_NULL, new Short[]{2, null, 5, 8}).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void in_shortNull() {
        doTestForInShort(NoPrimaryKeyNullTypes.FIELD_SHORT_NULL);
        RealmResults<NoPrimaryKeyNullTypes> resultList = realm.where(NoPrimaryKeyNullTypes.class).not().in(NoPrimaryKeyNullTypes.FIELD_SHORT_NULL, new Short[]{2, null, 5, 8}).findAll();
        assertEquals(130, resultList.size());
    }

    @Test
    public void in_integerNotNull() {
        doTestForInInteger(NoPrimaryKeyNullTypes.FIELD_INTEGER_NOT_NULL);
        try {
            realm.where(NoPrimaryKeyNullTypes.class).not().in(NoPrimaryKeyNullTypes.FIELD_INTEGER_NOT_NULL, new Integer[]{1, null, 4, 5}).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void in_integerNull() {
        doTestForInInteger(NoPrimaryKeyNullTypes.FIELD_INTEGER_NULL);
        RealmResults<NoPrimaryKeyNullTypes> resultList = realm.where(NoPrimaryKeyNullTypes.class).not().in(NoPrimaryKeyNullTypes.FIELD_INTEGER_NULL, new Integer[]{1, null, 4, 5}).findAll();
        assertEquals(130, resultList.size());
    }

    @Test
    public void in_longNotNull() {
        doTestForInLong(NoPrimaryKeyNullTypes.FIELD_LONG_NOT_NULL);
        try {
            realm.where(NoPrimaryKeyNullTypes.class).not().in(NoPrimaryKeyNullTypes.FIELD_LONG_NOT_NULL, new Long[]{13l, null, 16l, 98l}).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void in_longNull() {
        doTestForInLong(NoPrimaryKeyNullTypes.FIELD_LONG_NULL);
        RealmResults<NoPrimaryKeyNullTypes> resultList = realm.where(NoPrimaryKeyNullTypes.class).not().in(NoPrimaryKeyNullTypes.FIELD_LONG_NULL, new Long[]{13l, null, 16l, 98l}).findAll();
        assertEquals(130, resultList.size());
    }

    @Test
    public void notEqualTo() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(realm, TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList = realm.where(AllTypes.class)
                .notEqualTo(AllTypes.FIELD_LONG, 31).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 1, resultList.size());

        resultList = realm.where(AllTypes.class).notEqualTo(AllTypes.FIELD_FLOAT, 11.2345f)
                .equalTo(AllTypes.FIELD_LONG, 10).findAll();
        assertEquals(0, resultList.size());

        resultList = realm.where(AllTypes.class).notEqualTo(AllTypes.FIELD_FLOAT, 11.2345f)
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
    public void like_caseSensitive() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(realm, TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).like("columnString", "*DaTa*").findAll();
        assertEquals(0, resultList.size());

        resultList = realm.where(AllTypes.class).like("columnString", "*DaTa*", Case.INSENSITIVE).findAll();
        assertEquals(TEST_OBJECTS_COUNT, resultList.size());

        resultList = realm.where(AllTypes.class).like("columnString", "*DaTa 2?").findAll();
        assertEquals(0, resultList.size());

        resultList = realm.where(AllTypes.class).like("columnString", "*DaTa 2?", Case.INSENSITIVE).findAll();
        assertEquals(10, resultList.size());

        resultList = realm.where(AllTypes.class).like("columnString", "TEST*0").findAll();
        assertEquals(0, resultList.size());

        resultList = realm.where(AllTypes.class).like("columnString", "TEST*0", Case.INSENSITIVE).findAll();
        assertEquals(20, resultList.size());
    }

    @Test
    public void like_caseSensitiveWithNonLatinCharacters() {
        populateTestRealm();

        String flagEmoji = new StringBuilder().append(Character.toChars(0x1F1E9)).toString();
        String emojis = "ABC" + flagEmoji + "DEF";

        realm.beginTransaction();
        realm.delete(AllTypes.class);
        AllTypes at1 = realm.createObject(AllTypes.class);
        at1.setColumnString("Αλφα");
        AllTypes at2 = realm.createObject(AllTypes.class);
        at2.setColumnString("βήτα");
        AllTypes at3 = realm.createObject(AllTypes.class);
        at3.setColumnString("δέλτα");
        AllTypes at4 = realm.createObject(AllTypes.class);
        at4.setColumnString(emojis);
        realm.commitTransaction();

        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).like("columnString", "*Α*").findAll();
        assertEquals(1, resultList.size());

        resultList = realm.where(AllTypes.class).like("columnString", "*λ*").findAll();
        assertEquals(2, resultList.size());

        resultList = realm.where(AllTypes.class).like("columnString", "*Δ*").findAll();
        assertEquals(0, resultList.size());

        resultList = realm.where(AllTypes.class).like("columnString", "*Α*", Case.INSENSITIVE).findAll();
        // without ASCII-only limitation A matches α
        // assertEquals(3, resultList.size());
        assertEquals(1, resultList.size());

        resultList = realm.where(AllTypes.class).like("columnString", "*λ*", Case.INSENSITIVE).findAll();
        assertEquals(2, resultList.size());

        resultList = realm.where(AllTypes.class).like("columnString", "*Δ*", Case.INSENSITIVE).findAll();
        // without ASCII-only limitation Δ matches δ
        // assertEquals(1, resultList.size());
        assertEquals(0, resultList.size());

        resultList = realm.where(AllTypes.class).like("columnString", "?λ*").findAll();
        assertEquals(1, resultList.size());

        resultList = realm.where(AllTypes.class).like("columnString", "??λ*").findAll();
        assertEquals(1, resultList.size());

        resultList = realm.where(AllTypes.class).like("columnString", "?λ*").findAll();
        assertEquals(1, resultList.size());

        resultList = realm.where(AllTypes.class).like("columnString", "??λ*").findAll();
        assertEquals(1, resultList.size());

        resultList = realm.where(AllTypes.class).like("columnString", "ABC?DEF*").findAll();
        assertEquals(1, resultList.size());

        resultList = realm.where(AllTypes.class).like("columnString", "*" + flagEmoji + "*").findAll();
        assertEquals(1, resultList.size());
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
                .sort("name", Sort.ASCENDING)
                .findAll();
        Dog dog = dogs.where().equalTo("weight", 1d).findFirst();
        assertEquals(dog1, dog);
    }

    @Test
    public void sort_multiFailures() {
        // Zero fields specified.
        try {
            realm.where(AllTypes.class).sort(new String[]{}, new Sort[]{}).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // Number of fields and sorting orders don't match.
        try {
            realm.where(AllTypes.class)
                    .sort(new String[]{AllTypes.FIELD_STRING},new Sort[]{Sort.ASCENDING, Sort.ASCENDING})
                    .findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // Null is not allowed.
        try {
            realm.where(AllTypes.class)
                    .sort((String[]) null, null)
                    .findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            realm.where(AllTypes.class)
                    .sort(new String[]{AllTypes.FIELD_STRING}, null)
                    .findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // Non-existing field name.
        try {
            realm.where(AllTypes.class)
                    .sort(new String[]{AllTypes.FIELD_STRING, "do-not-exist"}, new Sort[]{Sort.ASCENDING, Sort.ASCENDING})
                    .findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // Defining sort multiple times
        try {
            realm.where(AllTypes.class)
                    .sort(AllTypes.FIELD_STRING)
                    .sort(AllTypes.FIELD_STRING);
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
    public void sort_singleField() {
        realm.beginTransaction();
        for (int i = 0; i < TEST_DATA_SIZE; i++) {
            AllTypes allTypes = realm.createObject(AllTypes.class);
            allTypes.setColumnLong(i);
        }
        realm.commitTransaction();

        RealmResults<AllTypes> sortedList = realm.where(AllTypes.class)
                .sort(new String[]{AllTypes.FIELD_LONG}, new Sort[]{Sort.DESCENDING})
                .findAll();
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
    public void findFirst_withSorting() {
        realm.beginTransaction();
        realm.insert(new Dog("Milo"));
        realm.insert(new Dog("Fido"));
        realm.insert(new Dog("Bella"));
        realm.commitTransaction();

        Dog dog = realm.where(Dog.class).sort("name").findFirst();
        assertEquals("Bella", dog.getName());
    }

    @Test
    public void findFirst_withSortedConstrictingView() {
        realm.beginTransaction();
        realm.insert(new Dog("Milo"));
        realm.insert(new Dog("Fido"));
        realm.insert(new Dog("Bella"));
        realm.commitTransaction();

        RealmResults<Dog> dogs = realm.where(Dog.class)
                .in("name", new String[] { "Fido", "Bella" })
                .sort("name", Sort.ASCENDING)
                .findAll();
        Dog dog = dogs.where().findFirst();
        assertEquals("Bella", dog.getName());
    }

    @Test
    public void findFirst_subQuery_withSorting() {
        realm.beginTransaction();
        realm.insert(new Dog("Milo"));
        realm.insert(new Dog("Fido"));
        realm.insert(new Dog("Bella"));
        realm.commitTransaction();

        RealmResults<Dog> dogs = realm.where(Dog.class).in("name", new String[] { "Fido", "Bella" }).findAll();
        Dog dog = dogs.where().sort("name", Sort.ASCENDING).findFirst();
        assertEquals("Bella", dog.getName());
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

        RealmResults<StringOnly> stringOnlies2 = realm.where(StringOnly.class).findAll();
        stringOnlies2 = stringOnlies2.sort("chars");
        for (int i = 0; i < stringOnlies2.size(); i++) {
            assertEquals(sorted[i], stringOnlies2.get(i).getChars());
        }
    }

    // Quering a non-nullable field with null is an error.
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
        // 2 Bytes
        try {
            realm.where(NullTypes.class).equalTo(NullTypes.FIELD_BYTES_NOT_NULL, (byte[]) null).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
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

    // Querying a non-nullable field with null is an error.
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

    // Queries nullable PrimaryKey.
    @Test
    public void equalTo_nullPrimaryKeys() {
        final long SECONDARY_FIELD_NUMBER = 49992417L;
        final String SECONDARY_FIELD_STRING = "Realm is a mobile database hundreds of millions of people rely on.";
        // Fills up a Realm with one user PrimaryKey value and 9 numeric values, starting from -5.
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
        // Fills up a realm with one user PrimaryKey value and 9 numeric values, starting from -5.
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
        // Fills up a realm with one user PrimaryKey value and one numeric values, starting from -1.
        TestHelper.populateTestRealmWithStringPrimaryKey(realm,  (String) null,  SECONDARY_FIELD_NUMBER, 2, -1);
        TestHelper.populateTestRealmWithBytePrimaryKey(realm,    (Byte) null,    SECONDARY_FIELD_STRING, 2, -1);
        TestHelper.populateTestRealmWithShortPrimaryKey(realm,   (Short) null,   SECONDARY_FIELD_STRING, 2, -1);
        TestHelper.populateTestRealmWithIntegerPrimaryKey(realm, (Integer) null, SECONDARY_FIELD_STRING, 2, -1);
        TestHelper.populateTestRealmWithLongPrimaryKey(realm,    (Long) null,    SECONDARY_FIELD_STRING, 2, -1);

        // String
        assertEquals(SECONDARY_FIELD_NUMBER, realm.where(PrimaryKeyAsString.class).notEqualTo(PrimaryKeyAsString.FIELD_PRIMARY_KEY,             "-1").findAll().first().getId());
        // Boxed Byte
        assertEquals(SECONDARY_FIELD_STRING, realm.where(PrimaryKeyAsBoxedByte.class).notEqualTo(PrimaryKeyAsBoxedByte.FIELD_PRIMARY_KEY,       Byte.valueOf((byte) -1)).findAll().first().getName());
        // Boxed Short
        assertEquals(SECONDARY_FIELD_STRING, realm.where(PrimaryKeyAsBoxedShort.class).notEqualTo(PrimaryKeyAsBoxedShort.FIELD_PRIMARY_KEY,     Short.valueOf((short) -1)).findAll().first().getName());
        // Boxed Integer
        assertEquals(SECONDARY_FIELD_STRING, realm.where(PrimaryKeyAsBoxedInteger.class).notEqualTo(PrimaryKeyAsBoxedInteger.FIELD_PRIMARY_KEY, Integer.valueOf(-1)).findAll().first().getName());
        // Boxed Long
        assertEquals(SECONDARY_FIELD_STRING, realm.where(PrimaryKeyAsBoxedLong.class).notEqualTo(PrimaryKeyAsBoxedLong.FIELD_PRIMARY_KEY,       Long.valueOf((long) -1)).findAll().first().getName());
    }

    @Test
    public void beginWith_nullStringPrimaryKey() {
        final long SECONDARY_FIELD_NUMBER = 49992417L;
        TestHelper.populateTestRealmWithStringPrimaryKey(realm, (String) null, SECONDARY_FIELD_NUMBER, 10, -5);

        assertEquals(SECONDARY_FIELD_NUMBER, realm.where(PrimaryKeyAsString.class).beginsWith(PrimaryKeyAsString.FIELD_PRIMARY_KEY, (String) null).findAll().first().getId());
    }

    @Test
    public void contains_nullStringPrimaryKey() {
        final long SECONDARY_FIELD_NUMBER = 49992417L;
        TestHelper.populateTestRealmWithStringPrimaryKey(realm, (String) null, SECONDARY_FIELD_NUMBER, 10, -5);

        assertEquals(SECONDARY_FIELD_NUMBER, realm.where(PrimaryKeyAsString.class).contains(PrimaryKeyAsString.FIELD_PRIMARY_KEY, (String) null).findAll().first().getId());
    }

    @Test
    public void endsWith_nullStringPrimaryKey() {
        final long SECONDARY_FIELD_NUMBER = 49992417L;
        TestHelper.populateTestRealmWithStringPrimaryKey(realm, (String) null, SECONDARY_FIELD_NUMBER, 10, -5);

        assertEquals(SECONDARY_FIELD_NUMBER, realm.where(PrimaryKeyAsString.class).endsWith(PrimaryKeyAsString.FIELD_PRIMARY_KEY, (String) null).findAll().first().getId());
    }

    @Test
    public void like_nullStringPrimaryKey() {
        final long SECONDARY_FIELD_NUMBER = 49992417L;
        TestHelper.populateTestRealmWithStringPrimaryKey(realm, (String) null, SECONDARY_FIELD_NUMBER, 10, -5);

        assertEquals(
                SECONDARY_FIELD_NUMBER,
                realm.where(PrimaryKeyAsString.class).like(PrimaryKeyAsString.FIELD_PRIMARY_KEY, (String) null)
                        .findAll().first().getId());
    }

    @Test
    public void between_nullPrimaryKeysIsNotZero() {
        // Fills up a realm with one user PrimaryKey value and 9 numeric values, starting from -5.
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
        // Fills up a realm with one user PrimaryKey value and 9 numeric values, starting from -5.
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
        // Fills up a realm with one user PrimaryKey value and 9 numeric values, starting from -5.
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
        // Fills up a realm with one user PrimaryKey value and 9 numeric values, starting from -5.
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
        // Fills up a realm with one user PrimaryKey value and 9 numeric values, starting from -5.
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

    // Queries nullable fields with equalTo null.
    @Test
    public void equalTo_nullableFields() {
        TestHelper.populateTestRealmForNullTests(realm);

        // 1 String
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_STRING_NULL, "Horse").count());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_STRING_NULL, (String) null).count());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_STRING_NULL, "Fish").count());
        assertEquals(0, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_STRING_NULL, "Goat").count());
        // 2 Bytes
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_BYTES_NULL, new byte[] {0}).count());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_BYTES_NULL, (byte[]) null).count());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_BYTES_NULL, new byte[] {1, 2}).count());
        assertEquals(0, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_BYTES_NULL, new byte[] {1, 2, 3}).count());
        // 3 Boolean
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_BOOLEAN_NULL, true).count());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_BOOLEAN_NULL, (Boolean) null).count());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_BOOLEAN_NULL, false).count());
        // 4 Byte
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_BYTE_NULL, 1).count());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_BYTE_NULL, (byte) 1).count());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_BYTE_NULL, (Byte) null).count());
        assertEquals(0, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_BYTE_NULL, (byte) 42).count());
        // 5 Short for other long based columns, only test null
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_SHORT_NULL, 1).count());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_SHORT_NULL, (short) 1).count());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_SHORT_NULL, (Short) null).count());
        assertEquals(0, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_SHORT_NULL, (short) 42).count());
        // 6 Integer
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_INTEGER_NULL, 1).count());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_INTEGER_NULL, (Integer) null).count());
        assertEquals(0, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_INTEGER_NULL, 42).count());
        // 7 Long
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_LONG_NULL, 1).count());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_LONG_NULL, (long) 1).count());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_LONG_NULL, (Long) null).count());
        assertEquals(0, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_LONG_NULL, (long) 42).count());
        // 8 Float
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_FLOAT_NULL, 1F).count());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_FLOAT_NULL, (Float) null).count());
        assertEquals(0, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_FLOAT_NULL, 42F).count());
        // 9 Double
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_DOUBLE_NULL, 1D).count());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_DOUBLE_NULL, (Double) null).count());
        assertEquals(0, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_DOUBLE_NULL, 42D).count());
        // 10 Date
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_DATE_NULL, new Date(0)).count());
        assertEquals(1, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_DATE_NULL, (Date) null).count());
        assertEquals(0, realm.where(NullTypes.class).equalTo(NullTypes.FIELD_DATE_NULL, new Date(424242)).count());
        // 11 Object skipped, doesn't support equalTo query
    }

    // Queries nullable field for null.
    @Test
    public void isNull_nullableFields() {
        TestHelper.populateTestRealmForNullTests(realm);

        // 1 String
        assertEquals(1, realm.where(NullTypes.class).isNull(NullTypes.FIELD_STRING_NULL).count());
        // 2 Bytes
        assertEquals(1, realm.where(NullTypes.class).isNull(NullTypes.FIELD_BYTES_NULL).count());
        // 3 Boolean
        assertEquals(1, realm.where(NullTypes.class).isNull(NullTypes.FIELD_BOOLEAN_NULL).count());
        // 4 Byte
        assertEquals(1, realm.where(NullTypes.class).isNull(NullTypes.FIELD_BYTE_NULL).count());
        // 5 Short
        assertEquals(1, realm.where(NullTypes.class).isNull(NullTypes.FIELD_SHORT_NULL).count());
        // 6 Integer
        assertEquals(1, realm.where(NullTypes.class).isNull(NullTypes.FIELD_INTEGER_NULL).count());
        // 7 Long
        assertEquals(1, realm.where(NullTypes.class).isNull(NullTypes.FIELD_LONG_NULL).count());
        // 8 Float
        assertEquals(1, realm.where(NullTypes.class).isNull(NullTypes.FIELD_FLOAT_NULL).count());
        // 9 Double
        assertEquals(1, realm.where(NullTypes.class).isNull(NullTypes.FIELD_DOUBLE_NULL).count());
        // 10 Date
        assertEquals(1, realm.where(NullTypes.class).isNull(NullTypes.FIELD_DATE_NULL).count());
        // 11 Object
        assertEquals(1, realm.where(NullTypes.class).isNull(NullTypes.FIELD_OBJECT_NULL).count());
    }

    // Queries nullable field for not null.
    @Test
    public void notEqualTo_nullableFields() {
        TestHelper.populateTestRealmForNullTests(realm);
        // 1 String
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_STRING_NULL, "Horse").count());
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_STRING_NULL, (String) null).count());
        // 2 Bytes
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_BYTES_NULL, new byte[] {1, 2}).count());
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_BYTES_NULL, (byte[]) null).count());
        // 3 Boolean
        assertEquals(1, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_BOOLEAN_NULL, false).count());
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_BOOLEAN_NULL, (Boolean) null).count());
        // 4 Byte
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_BYTE_NULL, (byte) 1).count());
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_BYTE_NULL, (Byte) null).count());
        // 5 Short
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_SHORT_NULL, (short) 1).count());
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_SHORT_NULL, (Byte) null).count());
        // 6 Integer
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_INTEGER_NULL, 1).count());
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_INTEGER_NULL, (Integer) null).count());
        // 7 Long
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_LONG_NULL, 1).count());
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_LONG_NULL, (Integer) null).count());
        // 8 Float
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_FLOAT_NULL, 1F).count());
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_FLOAT_NULL, (Float) null).count());
        // 9 Double
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_DOUBLE_NULL, 1D).count());
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_DOUBLE_NULL, (Double) null).count());
        // 10 Date
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_DATE_NULL, new Date(0)).count());
        assertEquals(2, realm.where(NullTypes.class).notEqualTo(NullTypes.FIELD_DATE_NULL, (Date) null).count());
        // 11 Object skipped, doesn't support notEqualTo query
    }

    // Queries nullable field for not null.
    @Test
    public void isNotNull_nullableFields() {
        TestHelper.populateTestRealmForNullTests(realm);

        // 1 String
        assertEquals(2, realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_STRING_NULL).count());
        // 2 Bytes
        assertEquals(2, realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_BYTES_NULL).count());
        // 3 Boolean
        assertEquals(2, realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_BOOLEAN_NULL).count());
        // 4 Byte
        assertEquals(2, realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_BYTE_NULL).count());
        // 5 Short
        assertEquals(2, realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_SHORT_NULL).count());
        // 6 Integer
        assertEquals(2, realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_INTEGER_NULL).count());
        // 7 Long
        assertEquals(2, realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_LONG_NULL).count());
        // 8 Float
        assertEquals(2, realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_FLOAT_NULL).count());
        // 9 Double
        assertEquals(2, realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_DOUBLE_NULL).count());
        // 10 Date
        assertEquals(2, realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_DATE_NULL).count());
        // 11 Object
        assertEquals(2, realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_OBJECT_NULL).count());
    }

    // Queries nullable field with beginsWith - all strings begin with null.
    @Test
    public void beginWith_nullForNullableStrings() {
        TestHelper.populateTestRealmForNullTests(realm);
        assertEquals("Fish", realm.where(NullTypes.class).beginsWith(NullTypes.FIELD_STRING_NULL,
                (String) null).findFirst().getFieldStringNotNull());
    }

    // Queries nullable field with contains - all strings contain null.
    @Test
    public void contains_nullForNullableStrings() {
        TestHelper.populateTestRealmForNullTests(realm);
        assertEquals("Fish", realm.where(NullTypes.class).contains(NullTypes.FIELD_STRING_NULL,
                (String) null).findFirst().getFieldStringNotNull());
    }

    // Queries nullable field with endsWith - all strings end with null.
    @Test
    public void endsWith_nullForNullableStrings() {
        TestHelper.populateTestRealmForNullTests(realm);
        assertEquals("Fish", realm.where(NullTypes.class).endsWith(NullTypes.FIELD_STRING_NULL,
                (String) null).findFirst().getFieldStringNotNull());
    }

    // Queries nullable field with like - nulls do not match either '?' or '*'.
    @Test
    public void like_nullForNullableStrings() {
        TestHelper.populateTestRealmForNullTests(realm);
        RealmResults<NullTypes> resultList = realm.where(NullTypes.class).like(NullTypes.FIELD_STRING_NULL, "*")
                .findAll();
        assertEquals(2, resultList.size());

        resultList = realm.where(NullTypes.class).like(NullTypes.FIELD_STRING_NULL, "?").findAll();
        assertEquals(0, resultList.size());
    }

    // Queries with between and table has null values in row.
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

    // Queries with greaterThan and table has null values in row.
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

    // Queries with greaterThanOrEqualTo and table has null values in row.
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

    // Queries with lessThan and table has null values in row.
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

    // Queries with lessThanOrEqualTo and table has null values in row.
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
        // Uses 30 here can ensure GC happen. (Tested with 4.3 1G Ram and 5.0 3G Ram)
        final int count = 30;
        RealmResults<CatOwner> results = realm.where(CatOwner.class).findAll();

        for (int i = 1; i <= count; i++) {
            @SuppressWarnings({"unused"})
            byte garbage[] = TestHelper.allocGarbage(0);
            results = results.where().findAll();
            System.gc(); // If a native resource has a reference count = 0, doing GC here might lead to a crash.
        }
    }

    private static byte[][] binaries = {{1, 2, 3}, {1, 2}, {1, 2, 3}, {2, 3}, {2}, {4, 5, 6}};

    private void createBinaryOnlyDataSet() {
        realm.beginTransaction();
        for (int i = 0; i < binaries.length; i++) {
            AllJavaTypes binaryOnly = new AllJavaTypes((long) i);
            binaryOnly.setFieldBinary(binaries[i]);
            realm.copyToRealm(binaryOnly);
        }
        realm.commitTransaction();
    }

    @Test
    public void equalTo_binary() {
        createBinaryOnlyDataSet();

        RealmResults<AllJavaTypes> resultList;
        resultList = realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_BINARY, binaries[0]).findAll();
        assertEquals(2, resultList.size());
        resultList = realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_BINARY, binaries[1]).findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_BINARY, new byte[] {1}).findAll();
        assertEquals(0, resultList.size());
    }

    @Test
    public void equalTo_binary_multiFailures() {
        createBinaryOnlyDataSet();

        // Non-binary field.
        try {
            RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class)
                    .equalTo(AllJavaTypes.FIELD_INT, binaries[0]).findAll();
            fail("Should throw exception.");
        } catch (IllegalArgumentException ignored) {
        }

        // Non-existent field.
        try {
            RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class)
                    .equalTo("NotAField", binaries[0]).findAll();
            fail("Should throw exception.");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void notEqualTo_binary() {
        createBinaryOnlyDataSet();

        RealmResults<AllJavaTypes> resultList;
        resultList = realm.where(AllJavaTypes.class).notEqualTo(AllJavaTypes.FIELD_BINARY, binaries[0]).findAll();
        assertEquals(4, resultList.size());
        resultList = realm.where(AllJavaTypes.class).notEqualTo(AllJavaTypes.FIELD_BINARY, binaries[1]).findAll();
        assertEquals(5, resultList.size());
        resultList = realm.where(AllJavaTypes.class).notEqualTo(AllJavaTypes.FIELD_BINARY, new byte[] {1}).findAll();
        assertEquals(6, resultList.size());
    }

    @Test
    public void notEqualTo_binary_multiFailures() {
        createBinaryOnlyDataSet();

        // Non-binary field.
        try {
            RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class)
                    .notEqualTo(AllJavaTypes.FIELD_INT, binaries[0]).findAll();
            fail("Should throw exception.");
        } catch (IllegalArgumentException ignored) {
        }

        // Non-existent field.
        try {
            RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class)
                    .notEqualTo("NotAField", binaries[0]).findAll();
            fail("Should throw exception.");
        } catch (IllegalArgumentException ignored) {
        }
    }

    // Tests min on empty columns.
    @Test
    public void min_emptyColumns() {
        RealmQuery<NullTypes> query = realm.where(NullTypes.class);
        assertNull(query.min(NullTypes.FIELD_INTEGER_NOT_NULL));
        assertNull(query.min(NullTypes.FIELD_FLOAT_NOT_NULL));
        assertNull(query.min(NullTypes.FIELD_DOUBLE_NOT_NULL));
        assertNull(query.minimumDate(NullTypes.FIELD_DATE_NOT_NULL));
    }

    // Tests min on columns with all null rows.
    @Test
    public void min_allNullColumns() {
        TestHelper.populateAllNullRowsForNumericTesting(realm);

        RealmQuery<NullTypes> query = realm.where(NullTypes.class);
        assertNull(query.min(NullTypes.FIELD_INTEGER_NULL));
        assertNull(query.min(NullTypes.FIELD_FLOAT_NULL));
        assertNull(query.min(NullTypes.FIELD_DOUBLE_NULL));
        assertNull(query.minimumDate(NullTypes.FIELD_DATE_NULL));
    }

    // Tests min on columns with all non-null rows.
    @Test
    public void min_allNonNullRows() {
        TestHelper.populateAllNonNullRowsForNumericTesting(realm);
        RealmQuery<NullTypes> query = realm.where(NullTypes.class);

        assertEquals(-1, query.min(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(-2f, query.min(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(-3d, query.min(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
        assertEquals(-2000, query.minimumDate(NullTypes.FIELD_DATE_NULL).getTime());
    }

    // Tests min on columns with partial null rows.
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

    // Tests max on columns with all null rows.
    @Test
    public void max_allNullColumns() {
        TestHelper.populateAllNullRowsForNumericTesting(realm);

        RealmQuery<NullTypes> query = realm.where(NullTypes.class);
        assertNull(query.max(NullTypes.FIELD_INTEGER_NULL));
        assertNull(query.max(NullTypes.FIELD_FLOAT_NULL));
        assertNull(query.max(NullTypes.FIELD_DOUBLE_NULL));
        assertNull(query.maximumDate(NullTypes.FIELD_DATE_NULL));
    }

    // Tests max on columns with all non-null rows.
    @Test
    public void max_allNonNullRows() {
        TestHelper.populateAllNonNullRowsForNumericTesting(realm);
        RealmQuery<NullTypes> query = realm.where(NullTypes.class);

        assertEquals(4, query.max(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(5f, query.max(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(6d, query.max(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
        assertEquals(12345, query.maximumDate(NullTypes.FIELD_DATE_NULL).getTime());
    }

    // Tests max on columns with partial null rows.
    @Test
    public void max_partialNullRows() {
        TestHelper.populatePartialNullRowsForNumericTesting(realm);
        RealmQuery<NullTypes> query = realm.where(NullTypes.class);

        assertEquals(4, query.max(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(5f, query.max(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(6d, query.max(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
        assertEquals(12345, query.maximumDate(NullTypes.FIELD_DATE_NULL).getTime());
    }

    // Tests average on empty columns.
    @Test
    public void average_emptyColumns() {
        RealmQuery<NullTypes> query = realm.where(NullTypes.class);
        assertEquals(0d, query.average(NullTypes.FIELD_INTEGER_NULL), 0d);
        assertEquals(0d, query.average(NullTypes.FIELD_FLOAT_NULL), 0d);
        assertEquals(0d, query.average(NullTypes.FIELD_DOUBLE_NULL), 0d);
    }

    // Tests average on columns with all null rows.
    @Test
    public void average_allNullColumns() {
        TestHelper.populateAllNullRowsForNumericTesting(realm);

        RealmQuery<NullTypes> query = realm.where(NullTypes.class);
        assertEquals(0d, query.average(NullTypes.FIELD_INTEGER_NULL), 0d);
        assertEquals(0d, query.average(NullTypes.FIELD_FLOAT_NULL), 0d);
        assertEquals(0d, query.average(NullTypes.FIELD_DOUBLE_NULL), 0d);
    }

    // Tests average on columns with all non-null rows.
    @Test
    public void average_allNonNullRows() {
        TestHelper.populateAllNonNullRowsForNumericTesting(realm);
        RealmQuery<NullTypes> query = realm.where(NullTypes.class);

        assertEquals(2.0, query.average(NullTypes.FIELD_INTEGER_NULL), 0d);
        assertEquals(7.0 / 3, query.average(NullTypes.FIELD_FLOAT_NULL), 0.001d);
        assertEquals(8.0 / 3, query.average(NullTypes.FIELD_DOUBLE_NULL), 0.001d);
    }

    // Tests average on columns with partial null rows.
    @Test
    public void average_partialNullRows() {
        TestHelper.populatePartialNullRowsForNumericTesting(realm);
        RealmQuery<NullTypes> query = realm.where(NullTypes.class);

        assertEquals(3.5, query.average(NullTypes.FIELD_INTEGER_NULL), 0d);
        assertEquals(4.5, query.average(NullTypes.FIELD_FLOAT_NULL), 0d);
        assertEquals(5.5, query.average(NullTypes.FIELD_DOUBLE_NULL), 0d);
    }

    // Tests sum on empty columns.
    @Test
    public void sum_emptyColumns() {
        RealmQuery<NullTypes> query = realm.where(NullTypes.class);
        assertEquals(0, query.sum(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(0f, query.sum(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(0d, query.sum(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
    }

    // Tests sum on columns with all null rows.
    @Test
    public void sum_allNullColumns() {
        TestHelper.populateAllNullRowsForNumericTesting(realm);

        RealmQuery<NullTypes> query = realm.where(NullTypes.class);
        assertEquals(0, query.sum(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(0f, query.sum(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(0d, query.sum(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
    }

    // Tests sum on columns with all non-null rows.
    @Test
    public void sum_allNonNullRows() {
        TestHelper.populateAllNonNullRowsForNumericTesting(realm);
        RealmQuery<NullTypes> query = realm.where(NullTypes.class);

        assertEquals(6, query.sum(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(7f, query.sum(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(8d, query.sum(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
    }

    // Tests sum on columns with partial null rows.
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

    // Verify that count correctly when using distinct.
    // See https://github.com/realm/realm-java/issues/5958
    @Test
    public void distinctCount() {
        realm.executeTransaction(r -> {
            for (int i = 0; i < 5; i++) {
                AllTypes obj = new AllTypes();
                obj.setColumnString("Foo");
                realm.copyToRealm(obj);
            }
        });
        assertEquals(1, realm.where(AllTypes.class).distinct(AllTypes.FIELD_STRING).count());
    }

    // Tests isNull on link's nullable field.
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

    // Tests isNull on link's not-nullable field. Should throw.
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

    // Tests isNotNull on link's nullable field.
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

    // Tests isNotNull on link's not-nullable field. Should throw.
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

    // Calling isNull on fields with the RealmList type will trigger an exception.
    @Test
    public void isNull_listFieldThrows() {
        try {
            realm.where(Owner.class).isNull("dogs");
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("Illegal Argument: RealmList(dogs) is not nullable.", expected.getMessage());
        }

        try {
            realm.where(Cat.class).isNull("owner.dogs");
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("Illegal Argument: RealmList(dogs) is not nullable.", expected.getMessage());
        }
    }

    // Calling isNotNull on fields with the RealmList type will trigger an exception.
    @Test
    public void isNotNull_listFieldThrows() {
        try {
            realm.where(Owner.class).isNotNull("dogs");
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("Illegal Argument: RealmList(dogs) is not nullable.", expected.getMessage());
        }

        try {
            realm.where(Cat.class).isNotNull("owner.dogs");
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("Illegal Argument: RealmList(dogs) is not nullable.", expected.getMessage());
        }
    }

    @Test
    public void isValid_tableQuery() {
        final RealmQuery<AllTypes> query = realm.where(AllTypes.class);

        assertTrue(query.isValid());
        populateTestRealm(realm, 1);
        // Still valid if result changed.
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
        // Still valid if table view changed.
        assertTrue(query.isValid());

        realm.close();
        assertFalse(query.isValid());
    }

    // Test for https://github.com/realm/realm-java/issues/1905
    @Test
    public void resultOfTableViewQuery() {
        populateTestRealm();

        final RealmResults<AllTypes> results = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_LONG, 3L).findAll();
        assertEquals(1, results.size());
        assertEquals("test data 3", results.first().getColumnString());

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

        // Still valid if base view changed.
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

        // Invalid if parent has been removed.
        assertFalse(query.isValid());
    }

    @Test
    public void isEmpty() {
        createIsEmptyDataSet(realm);
        for (RealmFieldType type : SUPPORTED_IS_EMPTY_TYPES) {
            switch (type) {
                case STRING:
                    assertEquals(2, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_STRING).count());
                    break;
                case BINARY:
                    assertEquals(2, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_BINARY).count());
                    break;
                case LIST:
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LIST).count());
                    break;
                case LINKING_OBJECTS:
                    // Row 2 does not have a backlink
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LO_OBJECT).count());
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LO_LIST).count());
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
                    // Row 0: Backlink list to row 1, list to row 0; included
                    // Row 1: Backlink list to row 2, list to row 1; included
                    // Row 2: No backlink list; not included
                    assertEquals(2, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_LIST).count());
                    break;
                case LINKING_OBJECTS:
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_LO_LIST).count());

                    // Row 0: Link to row 0, backlink to row 0; not included
                    // Row 1: Link to row 1m backlink to row 1; not included
                    // Row 2: Empty link; included
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_LO_OBJECT).count());
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

    @Test
    public void isEmpty_acrossLink_wrongTypeThrows() {
        for (RealmFieldType type : RealmFieldType.values()) {
            if (SUPPORTED_IS_EMPTY_TYPES.contains(type)) {
                continue;
            }

            RealmQuery<Owner> query = realm.where(Owner.class);
            try {
                query.isEmpty(Owner.FIELD_CAT + "." + Cat.FIELD_AGE);
                fail();
            } catch (IllegalArgumentException expected) {
                assertEquals(String.format(Locale.US,
                        "Invalid query: field '%s' in class '%s' is of invalid type '%s'.",
                        Cat.FIELD_AGE, Cat.CLASS_NAME, RealmFieldType.INTEGER.name()),
                        expected.getMessage());
            }
        }
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
                case LINKING_OBJECTS:
                    assertEquals(2, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_LO_OBJECT).count());
                    assertEquals(1, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_LO_LIST).count());
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
                case LINKING_OBJECTS:
                    assertEquals(1, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_LO_LIST).count());
                    assertEquals(2, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_LO_OBJECT).count());
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

    // Tests that deep queries work on a lot of data.
    @Test
    public void deepLinkListQuery() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                // Crashes with i == 1000, 500, 100, 89, 85, 84.
                // Doesn't crash for i == 10, 50, 75, 82, 83.
                for (int i = 0; i < 84; i++) {
                    AllJavaTypes obj = realm.createObject(AllJavaTypes.class, i + 1);
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
    public void sort_onSubObjectField() {
        populateTestRealm(realm, TEST_DATA_SIZE);
        RealmResults<AllTypes> results = realm.where(AllTypes.class)
                .sort(AllTypes.FIELD_REALMOBJECT + "." + Dog.FIELD_AGE)
                .findAll();
        assertEquals(0, results.get(0).getColumnRealmObject().getAge());
        assertEquals(TEST_DATA_SIZE - 1, results.get(TEST_DATA_SIZE - 1).getColumnRealmObject().getAge());
    }

    @Test
    @RunTestInLooperThread
    public void sort_async_onSubObjectField() {
        Realm realm = looperThread.getRealm();
        populateTestRealm(realm, TEST_DATA_SIZE);
        RealmResults<AllTypes> results = realm.where(AllTypes.class)
                .sort(AllTypes.FIELD_REALMOBJECT + "." + Dog.FIELD_AGE)
                .findAllAsync();
        looperThread.keepStrongReference(results);
        results.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> results) {
                assertEquals(0, results.get(0).getColumnRealmObject().getAge());
                assertEquals(TEST_DATA_SIZE - 1, results.get(TEST_DATA_SIZE - 1).getColumnRealmObject().getAge());
                looperThread.testComplete();
            }
        });
    }

    @Test
    public void findAll_indexedCaseInsensitiveFields() {
        // Catches https://github.com/realm/realm-java/issues/4788
        realm.beginTransaction();
        realm.createObject(IndexedFields.class).indexedString = "ROVER";
        realm.createObject(IndexedFields.class).indexedString = "Rover";
        realm.commitTransaction();

        RealmResults<IndexedFields> results = realm.where(IndexedFields.class)
                .equalTo(IndexedFields.FIELD_INDEXED_STRING, "rover", Case.INSENSITIVE)
                .findAll();
        assertEquals(2, results.size());
    }

    @Test
    public void sort_listOnSubObjectField() {
        String[] fieldNames = new String[2];
        fieldNames[0] = AllTypes.FIELD_REALMOBJECT + "." + Dog.FIELD_AGE;
        fieldNames[1] = AllTypes.FIELD_REALMOBJECT + "." + Dog.FIELD_AGE;

        Sort[] sorts = new Sort[2];
        sorts[0] = Sort.ASCENDING;
        sorts[1] = Sort.ASCENDING;

        populateTestRealm(realm, TEST_DATA_SIZE);
        RealmResults<AllTypes> results = realm.where(AllTypes.class)
                .sort(fieldNames, sorts)
                .findAll();
        assertEquals(0, results.get(0).getColumnRealmObject().getAge());
        assertEquals(TEST_DATA_SIZE - 1, results.get(TEST_DATA_SIZE - 1).getColumnRealmObject().getAge());
    }

    // RealmQuery.distinct(): requires indexing, and type = boolean, integer, date, string.
    private void populateForDistinct(Realm realm, long numberOfBlocks, long numberOfObjects, boolean withNull) {
        realm.beginTransaction();
        for (int i = 0; i < numberOfObjects * numberOfBlocks; i++) {
            for (int j = 0; j < numberOfBlocks; j++) {
                AnnotationIndexTypes obj = realm.createObject(AnnotationIndexTypes.class);
                obj.setIndexBoolean(j % 2 == 0);
                obj.setIndexLong(j);
                obj.setIndexDate(withNull ? null : new Date(1000L * j));
                obj.setIndexString(withNull ? null : "Test " + j);
                obj.setNotIndexBoolean(j % 2 == 0);
                obj.setNotIndexLong(j);
                obj.setNotIndexDate(withNull ? null : new Date(1000L * j));
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
        final long numberOfBlocks = 3;
        final long numberOfObjects = 3; // Must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        RealmResults<AnnotationIndexTypes> distinctBool = realm.where(AnnotationIndexTypes.class).distinct(AnnotationIndexTypes.FIELD_INDEX_BOOL).findAll();
        assertEquals(2, distinctBool.size());
        for (String field : new String[]{AnnotationIndexTypes.FIELD_INDEX_LONG, AnnotationIndexTypes.FIELD_INDEX_DATE, AnnotationIndexTypes.FIELD_INDEX_STRING}) {
            RealmResults<AnnotationIndexTypes> distinct = realm.where(AnnotationIndexTypes.class).distinct(field).findAll();
            assertEquals(field, numberOfBlocks, distinct.size());
        }
    }

    @Test
    public void distinct_withNullValues() {
        final long numberOfBlocks = 3;
        final long numberOfObjects = 3;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        for (String field : new String[]{AnnotationIndexTypes.FIELD_INDEX_DATE, AnnotationIndexTypes.FIELD_INDEX_STRING}) {
            RealmResults<AnnotationIndexTypes> distinct = realm.where(AnnotationIndexTypes.class).distinct(field).findAll();
            assertEquals(field, 1, distinct.size());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void distinct_failIfAppliedMultipleTimes() {
        realm.where(AnnotationIndexTypes.class)
                .distinct(AnnotationIndexTypes.FIELD_INDEX_DATE)
                .distinct(AnnotationIndexTypes.FIELD_INDEX_DATE);
    }

    @Test
    public void distinct_notIndexedFields() {
        final long numberOfBlocks = 3;
        final long numberOfObjects = 3;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        RealmResults<AnnotationIndexTypes> distinctBool = realm.where(AnnotationIndexTypes.class)
                .distinct(AnnotationIndexTypes.FIELD_NOT_INDEX_BOOL)
                .findAll();
        assertEquals(2, distinctBool.size());
        for (String field : new String[]{AnnotationIndexTypes.FIELD_NOT_INDEX_LONG,
                AnnotationIndexTypes.FIELD_NOT_INDEX_DATE, AnnotationIndexTypes.FIELD_NOT_INDEX_STRING}) {
            RealmResults<AnnotationIndexTypes> distinct = realm.where(AnnotationIndexTypes.class).distinct(field).findAll();
            assertEquals(field, numberOfBlocks, distinct.size());
        }
    }

    @Test
    public void distinct_doesNotExist() {
        final long numberOfBlocks = 3;
        final long numberOfObjects = 3; // Must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        try {
            realm.where(AnnotationIndexTypes.class).distinct("doesNotExist").findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinct_invalidTypes() {
        populateTestRealm();

        for (String field : new String[]{AllTypes.FIELD_REALMOBJECT, AllTypes.FIELD_REALMLIST, AllTypes.FIELD_DOUBLE, AllTypes.FIELD_FLOAT}) {
            try {
                realm.where(AllTypes.class).distinct(field).findAll();
                fail(field);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void distinct_indexedLinkedFields() {
        final long numberOfBlocks = 3;
        final long numberOfObjects = 3;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        for (String field : AnnotationIndexTypes.INDEX_FIELDS) {
            try {
                realm.where(AnnotationIndexTypes.class)
                        .distinct(AnnotationIndexTypes.FIELD_OBJECT + "." + field)
                        .findAll();
                fail("Unsupported Index" + field + " linked field");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void distinct_notIndexedLinkedFields() {
        final long numberOfBlocks = 3;
        final long numberOfObjects = 3;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        for (String field : AnnotationIndexTypes.NOT_INDEX_FIELDS) {
            try {
                realm.where(AnnotationIndexTypes.class)
                        .distinct(AnnotationIndexTypes.FIELD_OBJECT + "." + field)
                        .findAll();
                fail("Unsupported notIndex" + field + " linked field");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void distinct_invalidTypesLinkedFields() {
        populateForDistinctInvalidTypesLinked(realm);

        try {
            realm.where(AllJavaTypes.class)
                    .distinct(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_BINARY)
                    .findAll();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    @RunTestInLooperThread
    public void distinct_async() throws Throwable {
        final AtomicInteger changeListenerCalled = new AtomicInteger(4);
        final Realm realm = looperThread.getRealm();
        final long numberOfBlocks = 3;
        final long numberOfObjects = 3; // Must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        final RealmResults<AnnotationIndexTypes> distinctBool = realm.where(AnnotationIndexTypes.class).distinct(AnnotationIndexTypes.FIELD_INDEX_BOOL).findAllAsync();
        final RealmResults<AnnotationIndexTypes> distinctLong = realm.where(AnnotationIndexTypes.class).distinct(AnnotationIndexTypes.FIELD_INDEX_LONG).findAllAsync();
        final RealmResults<AnnotationIndexTypes> distinctDate = realm.where(AnnotationIndexTypes.class).distinct(AnnotationIndexTypes.FIELD_INDEX_DATE).findAllAsync();
        final RealmResults<AnnotationIndexTypes> distinctString = realm.where(AnnotationIndexTypes.class).distinct(AnnotationIndexTypes.FIELD_INDEX_STRING).findAllAsync();

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
    public void distinct_async_withNullValues() throws Throwable {
        final AtomicInteger changeListenerCalled = new AtomicInteger(2);
        final Realm realm = looperThread.getRealm();
        final long numberOfBlocks = 3;
        final long numberOfObjects = 3; // must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        final RealmResults<AnnotationIndexTypes> distinctDate = realm.where(AnnotationIndexTypes.class)
                .distinct(AnnotationIndexTypes.FIELD_INDEX_DATE)
                .findAllAsync();
        final RealmResults<AnnotationIndexTypes> distinctString = realm.where(AnnotationIndexTypes.class)
                .distinct(AnnotationIndexTypes.FIELD_INDEX_STRING)
                .findAllAsync();

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
                assertEquals(1, distinctDate.size());
                endTest.run();
            }
        });

        distinctString.addChangeListener(new RealmChangeListener<RealmResults<AnnotationIndexTypes>>() {
            @Override
            public void onChange(RealmResults<AnnotationIndexTypes> object) {
                assertEquals(1, distinctString.size());
                endTest.run();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void distinct_async_doesNotExist() {
        final long numberOfBlocks = 3;
        final long numberOfObjects = 3;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        try {
            realm.where(AnnotationIndexTypes.class).distinct("doesNotExist").findAllAsync();
        } catch (IllegalArgumentException ignored) {
        }
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void distinct_async_invalidTypes() {
        populateTestRealm(realm, TEST_DATA_SIZE);

        for (String field : new String[]{AllTypes.FIELD_REALMOBJECT, AllTypes.FIELD_REALMLIST, AllTypes.FIELD_DOUBLE, AllTypes.FIELD_FLOAT}) {
            try {
                realm.where(AllTypes.class).distinct(field).findAllAsync();
            } catch (IllegalArgumentException ignored) {
            }
        }
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void distinct_async_indexedLinkedFields() {
        final long numberOfBlocks = 3;
        final long numberOfObjects = 3;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        for (String field : AnnotationIndexTypes.INDEX_FIELDS) {
            try {
                realm.where(AnnotationIndexTypes.class).distinct(AnnotationIndexTypes.FIELD_OBJECT + "." + field).findAllAsync();
                fail("Unsupported " + field + " linked field");
            } catch (IllegalArgumentException ignored) {
            }
        }
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void distinct_async_notIndexedLinkedFields() {
        populateForDistinctInvalidTypesLinked(realm);

        try {
            realm.where(AllJavaTypes.class).distinct(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_BINARY).findAllAsync();
        } catch (IllegalArgumentException ignored) {
        }
        looperThread.testComplete();
    }

    @Test
    public void distinctMultiArgs() {
        final long numberOfBlocks = 3;
        final long numberOfObjects = 3; // Must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        RealmQuery<AnnotationIndexTypes> query = realm.where(AnnotationIndexTypes.class);
        RealmResults<AnnotationIndexTypes> distinctMulti = query.distinct(AnnotationIndexTypes.FIELD_INDEX_BOOL, AnnotationIndexTypes.INDEX_FIELDS).findAll();
        assertEquals(numberOfBlocks, distinctMulti.size());
    }

    @Test
    public void distinctMultiArgs_switchedFieldsOrder() {
        final long numberOfBlocks = 3;
        TestHelper.populateForDistinctFieldsOrder(realm, numberOfBlocks);

        // Regardless of the block size defined above, the output size is expected to be the same, 4 in this case, due to receiving unique combinations of tuples.
        RealmResults<AnnotationIndexTypes> distinctStringLong = realm.where(AnnotationIndexTypes.class).distinct(AnnotationIndexTypes.FIELD_INDEX_STRING, AnnotationIndexTypes.FIELD_INDEX_LONG).findAll();
        RealmResults<AnnotationIndexTypes> distinctLongString = realm.where(AnnotationIndexTypes.class).distinct(AnnotationIndexTypes.FIELD_INDEX_LONG, AnnotationIndexTypes.FIELD_INDEX_STRING).findAll();
        assertEquals(4, distinctStringLong.size());
        assertEquals(4, distinctLongString.size());
        assertEquals(distinctStringLong.size(), distinctLongString.size());
    }

    @Test
    public void distinctMultiArgs_emptyField() {
        final long numberOfBlocks = 3;
        final long numberOfObjects = 3;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        RealmQuery<AnnotationIndexTypes> query = realm.where(AnnotationIndexTypes.class);
        // An empty string field in the middle.
        try {
            query.distinct(AnnotationIndexTypes.FIELD_INDEX_BOOL, "", AnnotationIndexTypes.FIELD_INDEX_INT).findAll();
        } catch (IllegalArgumentException ignored) {
        }
        // An empty string field at the end.
        try {
            query.distinct(AnnotationIndexTypes.FIELD_INDEX_BOOL, AnnotationIndexTypes.FIELD_INDEX_INT, "").findAll();
        } catch (IllegalArgumentException ignored) {
        }
        // A null string field in the middle.
        try {
            query.distinct(AnnotationIndexTypes.FIELD_INDEX_BOOL, (String) null, AnnotationIndexTypes.FIELD_INDEX_INT).findAll();
        } catch (IllegalArgumentException ignored) {
        }
        // A null string field at the end.
        try {
            query.distinct(AnnotationIndexTypes.FIELD_INDEX_BOOL, AnnotationIndexTypes.FIELD_INDEX_INT, (String) null).findAll();
        } catch (IllegalArgumentException ignored) {
        }
        // (String) Null makes varargs a null array.
        try {
            query.distinct(AnnotationIndexTypes.FIELD_INDEX_BOOL, (String) null).findAll();
        } catch (IllegalArgumentException ignored) {
        }
        // Two (String) null for first and varargs fields.
        try {
            query.distinct((String) null, (String) null).findAll();
        } catch (IllegalArgumentException ignored) {
        }
        // "" & (String) null combination.
        try {
            query.distinct("", (String) null).findAll();
        } catch (IllegalArgumentException ignored) {
        }
        // "" & (String) null combination.
        try {
            query.distinct((String) null, "").findAll();
        } catch (IllegalArgumentException ignored) {
        }
        // Two empty fields tests.
        try {
            query.distinct("", "").findAll();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctMultiArgs_withNullValues() {
        final long numberOfBlocks = 3;
        final long numberOfObjects = 3;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        RealmQuery<AnnotationIndexTypes> query = realm.where(AnnotationIndexTypes.class);
        RealmResults<AnnotationIndexTypes> distinctMulti = query.distinct(AnnotationIndexTypes.FIELD_INDEX_DATE, AnnotationIndexTypes.FIELD_INDEX_STRING).findAll();
        assertEquals(1, distinctMulti.size());
    }

    @Test
    public void distinctMultiArgs_notIndexedFields() {
        final long numberOfBlocks = 3;
        final long numberOfObjects = 3;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        RealmQuery<AnnotationIndexTypes> query = realm.where(AnnotationIndexTypes.class);
        try {
            query.distinct(AnnotationIndexTypes.FIELD_NOT_INDEX_STRING, AnnotationIndexTypes.NOT_INDEX_FIELDS).findAll();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctMultiArgs_doesNotExistField() {
        final long numberOfBlocks = 3;
        final long numberOfObjects = 3;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        RealmQuery<AnnotationIndexTypes> query = realm.where(AnnotationIndexTypes.class);
        try {
            query.distinct(AnnotationIndexTypes.FIELD_INDEX_INT, AnnotationIndexTypes.NONEXISTANT_MIX_FIELDS).findAll();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctMultiArgs_invalidTypesFields() {
        populateTestRealm();

        RealmQuery<AllTypes> query = realm.where(AllTypes.class);
        try {
            query.distinct(AllTypes.FIELD_REALMOBJECT, AllTypes.INVALID_TYPES_FIELDS_FOR_DISTINCT).findAll();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctMultiArgs_indexedLinkedFields() {
        final long numberOfBlocks = 3;
        final long numberOfObjects = 3;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        RealmQuery<AnnotationIndexTypes> query = realm.where(AnnotationIndexTypes.class);
        try {
            query.distinct(AnnotationIndexTypes.INDEX_LINKED_FIELD_STRING, AnnotationIndexTypes.INDEX_LINKED_FIELDS).findAll();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctMultiArgs_notIndexedLinkedFields() {
        final long numberOfBlocks = 3;
        final long numberOfObjects = 3;
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        RealmQuery<AnnotationIndexTypes> query = realm.where(AnnotationIndexTypes.class);
        try {
            query.distinct(AnnotationIndexTypes.NOT_INDEX_LINKED_FILED_STRING, AnnotationIndexTypes.NOT_INDEX_LINKED_FIELDS).findAll();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void distinctMultiArgs_invalidTypesLinkedFields() {
        populateForDistinctInvalidTypesLinked(realm);

        RealmQuery<AllJavaTypes> query = realm.where(AllJavaTypes.class);
        try {
            query.distinct(AllJavaTypes.INVALID_LINKED_BINARY_FIELD_FOR_DISTINCT, AllJavaTypes.INVALID_LINKED_TYPES_FIELDS_FOR_DISTINCT).findAll();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void beginGroup_missingEndGroup() {
        realm.where(AllTypes.class).beginGroup().findAll();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void endGroup_missingBeginGroup() {
        realm.where(AllTypes.class).endGroup().findAll();
    }

    @Test
    public void alwaysTrue() {
        populateTestRealm();
        assertEquals(TEST_DATA_SIZE, realm.where(AllTypes.class).alwaysTrue().findAll().size());
    }

    @Test
    public  void alwaysTrue_inverted() {
        populateTestRealm();
        assertEquals(0, realm.where(AllTypes.class).not().alwaysTrue().findAll().size());
    }

    @Test
    public void alwaysFalse() {
        populateTestRealm();
        assertEquals(0, realm.where(AllTypes.class).alwaysFalse().findAll().size());
    }

    @Test
    public void alwaysFalse_inverted() {
        populateTestRealm();
        assertEquals(TEST_DATA_SIZE, realm.where(AllTypes.class).not().alwaysFalse().findAll().size());
    }

    @Test
    public void getRealm() {
        assertTrue(realm == realm.where(AllTypes.class).getRealm());
    }

    @Test
    public void getRealm_throwsIfDynamicRealm() {
        DynamicRealm dRealm = DynamicRealm.getInstance(realm.getConfiguration());
        try {
            dRealm.where(AllTypes.CLASS_NAME).getRealm();
            fail();
        } catch (IllegalStateException ignore) {
        } finally {
            dRealm.close();
        }
    }

    @Test
    public void getRealm_throwsIfRealmClosed() {
        RealmQuery<AllTypes> query = realm.where(AllTypes.class);
        realm.close();
        try {
            query.getRealm();
            fail();
        } catch (IllegalStateException ignore) {
        }
    }
}
