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

import android.test.AndroidTestCase;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.realm.entities.AllTypes;
import io.realm.entities.CyclicType;
import io.realm.entities.Dog;
import io.realm.entities.NullTypes;
import io.realm.entities.Thread;
import io.realm.internal.Row;

import static io.realm.internal.test.ExtraTests.assertArrayEquals;

public class RealmObjectTest extends AndroidTestCase {

    private Realm testRealm;
    private RealmConfiguration realmConfig;

    private static final int TEST_SIZE = 5;
    private static final boolean REMOVE_FIRST = true;
    private static final boolean REMOVE_LAST = false;

    @Override
    protected void setUp() throws Exception {
        realmConfig = new RealmConfiguration.Builder(getContext()).build();
        Realm.deleteRealm(realmConfig);
        testRealm = Realm.getInstance(realmConfig);
    }

    @Override
    protected void tearDown() throws Exception {
        testRealm.close();
    }

    // Row realmGetRow()
    public void testRealmGetRowReturnsValidRow() {

        testRealm.beginTransaction();
        RealmObject realmObject = testRealm.createObject(AllTypes.class);

        Row row = realmObject.row;

        testRealm.commitTransaction();
        assertNotNull("RealmObject.realmGetRow returns zero ", row);
        assertEquals(9, row.getColumnCount());
    }

    public void testStringEncoding() {
        String[] strings = {"ABCD", "ÆØÅ", "Ö∫Ë", "ΠΑΟΚ", "Здравей"};

        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);

        for (String str : strings) {
            AllTypes obj1 = testRealm.createObject(AllTypes.class);
            obj1.setColumnString(str);
        }
        testRealm.commitTransaction();

        RealmResults<AllTypes> objects = testRealm.allObjects(AllTypes.class);
        assertEquals(strings.length, objects.size());
        int i = 0;
        for (AllTypes obj : objects) {
            String s = obj.getColumnString();
            assertEquals(strings[i], s);
            i++;
        }
    }

    // removing original object and see if has been removed
    public void testRemoveFromRealm() {
        testRealm = Realm.getInstance(getContext());
        testRealm.beginTransaction();
        Dog rex = testRealm.createObject(Dog.class);
        rex.setName("Rex");
        Dog fido = testRealm.createObject(Dog.class);
        fido.setName("Fido");
        testRealm.commitTransaction();

        RealmResults<Dog> allDogsBefore = testRealm.where(Dog.class).equalTo("name", "Rex").findAll();
        assertEquals(1, allDogsBefore.size());

        testRealm.beginTransaction();
        rex.removeFromRealm();
        testRealm.commitTransaction();

        RealmResults<Dog> allDogsAfter = testRealm.where(Dog.class).equalTo("name", "Rex").findAll();
        assertEquals(0, allDogsAfter.size());

        fido.getName();
        try {
            rex.getName();
            testRealm.close();
            fail();
        } catch (IllegalStateException ignored) {}

        // deleting rex twice should fail
        testRealm.beginTransaction();
        try {
            rex.removeFromRealm();
            testRealm.close();
            fail();
        } catch (IllegalStateException ignored) {}
        testRealm.commitTransaction();
        testRealm.close();
    }

    // query for an object, remove it and see it has been removed from realm
    public void testRemoveResultFromRealm() {
        testRealm = Realm.getInstance(getContext());
        testRealm.beginTransaction();
        testRealm.clear(Dog.class);
        Dog dogToAdd = testRealm.createObject(Dog.class);
        dogToAdd.setName("Rex");
        testRealm.commitTransaction();

        assertEquals(1, testRealm.allObjects(Dog.class).size());

        Dog dogToRemove = testRealm.where(Dog.class).findFirst();
        assertNotNull(dogToRemove);
        testRealm.beginTransaction();
        dogToRemove.removeFromRealm();
        testRealm.commitTransaction();

        assertEquals(0, testRealm.allObjects(Dog.class).size());
        try {
            dogToAdd.getName();
            testRealm.close();
            fail();
        }
        catch (IllegalStateException ignored) {}
        try {
            dogToRemove.getName();
            testRealm.close();
            fail();
        }
        catch (IllegalStateException ignored) {}
        testRealm.close();
    }

    public void removeOneByOne(boolean atFirst) {
        Set<Long> ages = new HashSet<Long>();
        testRealm.beginTransaction();
        testRealm.clear(Dog.class);
        for (int i = 0; i < TEST_SIZE; i++) {
            Dog dog = testRealm.createObject(Dog.class);
            dog.setAge(i);
            ages.add((long) i);
        }
        testRealm.commitTransaction();

        assertEquals(TEST_SIZE, testRealm.allObjects(Dog.class).size());

        RealmResults<Dog> dogs = testRealm.allObjects(Dog.class);
        for (int i = 0; i < TEST_SIZE; i++) {
            testRealm.beginTransaction();
            Dog dogToRemove;
            if (atFirst) {
                dogToRemove = dogs.first();
            } else {
                dogToRemove = dogs.last();
            }
            ages.remove(Long.valueOf(dogToRemove.getAge()));
            dogToRemove.removeFromRealm();

            // object is no longer valid
            try {
                dogToRemove.getAge();
                fail();
            }
            catch (IllegalStateException ignored) {}

            testRealm.commitTransaction();

            // and removed from realm and remaining objects are place correctly
            RealmResults<Dog> remainingDogs = testRealm.allObjects(Dog.class);
            assertEquals(TEST_SIZE - i - 1, remainingDogs.size());
            for (Dog dog : remainingDogs) {
                assertTrue(ages.contains(Long.valueOf(dog.getAge())));
            }
        }
    }

    public void testRemoveFromRealmAtPosition() {
        removeOneByOne(REMOVE_FIRST);
        removeOneByOne(REMOVE_LAST);
    }

    public boolean methodWrongThread(final boolean callGetter) throws ExecutionException, InterruptedException {
        testRealm = Realm.getInstance(getContext());
        testRealm.beginTransaction();
        testRealm.createObject(AllTypes.class);
        testRealm.commitTransaction();
        final AllTypes allTypes = testRealm.where(AllTypes.class).findFirst();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    if (callGetter) {
                        allTypes.getColumnFloat();
                    } else {
                        allTypes.setColumnFloat(1.0f);
                    }
                    return false;
                } catch (IllegalStateException ignored) {
                    return true;
                }
            }
        });

        Boolean result = future.get();
        testRealm.close();
        return result;
    }

    public void testGetSetWrongThread() throws ExecutionException, InterruptedException {
        assertTrue(methodWrongThread(true));
        assertTrue(methodWrongThread(false));
    }

    public void testEqualsSameRealmObject() {
        testRealm.beginTransaction();
        CyclicType ct = testRealm.createObject(CyclicType.class);
        ct.setName("Foo");
        testRealm.commitTransaction();

        CyclicType ct1 = testRealm.where(CyclicType.class).findFirst();
        CyclicType ct2 = testRealm.where(CyclicType.class).findFirst();

        assertTrue(ct1.equals(ct2));
        assertTrue(ct2.equals(ct1));
    }

    public void testEqualsDifferentRealmObjects() {
        testRealm.beginTransaction();
        CyclicType objA = testRealm.createObject(CyclicType.class);
        objA.setName("Foo");
        CyclicType objB = testRealm.createObject(CyclicType.class);
        objB.setName("Bar");
        testRealm.commitTransaction();

        assertFalse(objA.equals(objB));
        assertFalse(objB.equals(objA));
    }

    public void testEqualsAfterModification() {
        testRealm.beginTransaction();
        CyclicType ct = testRealm.createObject(CyclicType.class);
        ct.setName("Foo");
        testRealm.commitTransaction();

        CyclicType ct1 = testRealm.where(CyclicType.class).findFirst();
        CyclicType ct2 = testRealm.where(CyclicType.class).findFirst();

        testRealm.beginTransaction();
        ct1.setName("Baz");
        testRealm.commitTransaction();

        assertTrue(ct1.equals(ct2));
        assertTrue(ct2.equals(ct1));
    }

    public void testEqualsStandAlone() {
        testRealm.beginTransaction();
        CyclicType ct1 = testRealm.createObject(CyclicType.class);
        ct1.setName("Foo");
        testRealm.commitTransaction();

        CyclicType ct2 = new CyclicType();
        ct2.setName("Bar");

        assertFalse(ct1.equals(ct2));
        assertFalse(ct2.equals(ct1));
    }

    public void testCyclicEquals() {
        testRealm.beginTransaction();
        CyclicType foo = createCyclicData();
        testRealm.commitTransaction();

        assertEquals(foo, testRealm.where(CyclicType.class).equalTo("name", "Foo").findFirst());
    }

    public void testCyclicToString() {
        testRealm.beginTransaction();
        CyclicType foo = createCyclicData();
        testRealm.commitTransaction();

        String expected = "CyclicType = [{name:Foo},{object:CyclicType},{objects:RealmList<CyclicType>[0]}]";
        assertEquals(expected, foo.toString());
    }

    public void testCyclicHashCode() {
        testRealm.beginTransaction();
        CyclicType foo = createCyclicData();
        testRealm.commitTransaction();

        assertEquals(1344723738, foo.hashCode());
    }

    private CyclicType createCyclicData() {
        CyclicType foo = testRealm.createObject(CyclicType.class);
        foo.setName("Foo");
        CyclicType bar = testRealm.createObject(CyclicType.class);
        bar.setName("Bar");

        // Setup cycle on normal object references
        foo.setObject(bar);
        bar.setObject(foo);
        return foo;
    }

    public void testDateType() {
        long testDatesNotValid[] = {Long.MIN_VALUE, Long.MAX_VALUE};
        long testDatesValid[] = {-1000, 0, 1000};
        long testDatesLoosePrecision[] = {1, 1001};

        // test valid dates
        testRealm.beginTransaction();
        for (long value : testDatesValid) {
            AllTypes allTypes = testRealm.createObject(AllTypes.class);
            allTypes.setColumnDate(new Date(value));
        }
        testRealm.commitTransaction();

        int i = 0;
        for (AllTypes allTypes : testRealm.allObjects(AllTypes.class)) {
            assertEquals("Item " + i, new Date(testDatesValid[i]), allTypes.getColumnDate());
            i++;
        }

        // test valid dates but with precision lost
        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        for (long value : testDatesLoosePrecision) {
            AllTypes allTypes = testRealm.createObject(AllTypes.class);
            allTypes.setColumnDate(new Date(value));
        }
        testRealm.commitTransaction();

        i = 0;
        for (AllTypes allTypes : testRealm.allObjects(AllTypes.class)) {
            assertFalse("Item " + i, new Date(testDatesLoosePrecision[i]) == allTypes.getColumnDate());
            assertEquals("Item " + i, new Date(1000*(testDatesLoosePrecision[i]/1000)), allTypes.getColumnDate());
            i++;
        }

        // test invalid dates
        for (long value : testDatesNotValid) {
            try {
                testRealm.beginTransaction();
                testRealm.clear(AllTypes.class);
                AllTypes allTypes = testRealm.createObject(AllTypes.class);
                allTypes.setColumnDate(new Date(value));
                testRealm.commitTransaction();
                fail();
            } catch (IllegalArgumentException ignored) { testRealm.cancelTransaction(); }
        }
    }

    private Date newDate(int year, int month, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private void addDate(int year, int month, int dayOfMonth) {
        Date date = newDate(year, month, dayOfMonth);

        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        AllTypes allTypes = testRealm.createObject(AllTypes.class);
        allTypes.setColumnDate(date);
        testRealm.commitTransaction();

        AllTypes object = testRealm.allObjects(AllTypes.class).first();

        // Realm does not support millisec precision
        assertEquals(1000 * (date.getTime() / 1000), 1000 * (object.getColumnDate().getTime() / 1000));
    }

    public void testDateTypeOutOfRange() {
        // ** Must throw if date is too old
        for (int i = 0; i < 2; i++) {
            try {
                addDate(1900 + i, 1, 1);
                fail();
            } catch (IllegalArgumentException ignored) {
                testRealm.cancelTransaction();
            }
        }

        // ** Supported dates works
        for (int i = 2; i < 10; i++) {
            addDate(1900 + i, 1, 1);
        }

        // ** Must throw if date is too new
        for (int i = 0; i < 2; i++) {
            try {
                addDate(2038 + i, 1, 20);
                fail();
            } catch (IllegalArgumentException ignored) {
                testRealm.cancelTransaction();
            }
        }
    }

    public void testWriteMustThrowOutOfTransaction() {
        testRealm.beginTransaction();
        Dog dog = testRealm.createObject(Dog.class);
        testRealm.commitTransaction();

        try {
            dog.setName("Rex");
            fail();
        } catch (IllegalStateException ignored) {
            // Don't fail
        } catch (Exception ignored) {
            fail();
        }

    }

    public void testSetNullLink() {
        testRealm.beginTransaction();
        CyclicType objA = testRealm.createObject(CyclicType.class);
        objA.setName("Foo");
        CyclicType objB = testRealm.createObject(CyclicType.class);
        objB.setName("Bar");

        objA.setObject(objB);

        assertNotNull(objA.getObject());

        try {
            objA.setObject(null);
        } catch (NullPointerException nullPointer) {
            fail();
        }
        testRealm.commitTransaction();
        assertNull(objA.getObject());
    }

    public void testThreadModelClass() {
        // The model class' name (Thread) clashed with a common Java class.
        // The annotation process must be able to handle that.
        testRealm.beginTransaction();
        @SuppressWarnings("unused")
        Thread thread = testRealm.createObject(Thread.class);
        testRealm.commitTransaction();
    }

    public void testIsValidUnManagedObject() {
        AllTypes allTypes = new AllTypes();
        assertFalse(allTypes.isValid());
    }

    public void testIsValidClosedRealm() {
        RealmConfiguration otherConfig = new RealmConfiguration.Builder(getContext()).name("other-realm").build();
        Realm.deleteRealm(otherConfig);
        Realm testRealm = Realm.getInstance(otherConfig);
        testRealm.beginTransaction();
        AllTypes allTypes = testRealm.createObject(AllTypes.class);
        assertTrue(allTypes.isValid());
        testRealm.commitTransaction();
        testRealm.close();
        assertFalse(allTypes.isValid());
    }

    public void testIsValidDeletedObject() {
        testRealm.beginTransaction();
        AllTypes allTypes = testRealm.createObject(AllTypes.class);
        assertTrue(allTypes.isValid());
        testRealm.clear(AllTypes.class);
        testRealm.commitTransaction();
        assertFalse(allTypes.isValid());
    }

    public void testIsValidManagedObject() {
        testRealm.beginTransaction();
        AllTypes allTypes = testRealm.createObject(AllTypes.class);
        assertTrue(allTypes.isValid());
        testRealm.commitTransaction();
        assertTrue(allTypes.isValid());
    }

    // store and retrieve null values for nullable fields
    public void testStoreRetrieveNullOnNullableFields() {
        testRealm.beginTransaction();
        NullTypes nullTypes = testRealm.createObject(NullTypes.class);
        // 1 String
        nullTypes.setFieldStringNull(null);
        // 2 Bytes
        nullTypes.setFieldBytesNull(null);
        // 3 Boolean
        nullTypes.setFieldBooleanNull(null);
        // 4 Byte
        nullTypes.setFieldByteNull(null);
        // 5 Short
        nullTypes.setFieldShortNull(null);
        // 6 Integer
        nullTypes.setFieldIntegerNull(null);
        // 7 Long
        nullTypes.setFieldLongNull(null);
        // 8 Float
        nullTypes.setFieldFloatNull(null);
        // 9 Double
        nullTypes.setFieldDoubleNull(null);
        // 10 Date
        nullTypes.setFieldDateNull(null);
        testRealm.commitTransaction();

        nullTypes = testRealm.where(NullTypes.class).findFirst();
        // 1 String
        assertNull(nullTypes.getFieldStringNull());
        // 2 Bytes
        assertNull(nullTypes.getFieldBytesNull());
        // 3 Boolean
        assertNull(nullTypes.getFieldBooleanNull());
        // 4 Byte
        assertNull(nullTypes.getFieldByteNull());
        // 5 Short
        assertNull(nullTypes.getFieldShortNull());
        // 6 Integer
        assertNull(nullTypes.getFieldIntegerNull());
        // 7 Long
        assertNull(nullTypes.getFieldLongNull());
        // 8 Float
        assertNull(nullTypes.getFieldFloatNull());
        // 9 Double
        assertNull(nullTypes.getFieldDoubleNull());
        // 10 Date
        assertNull(nullTypes.getFieldDateNull());
    }

    // store and retrieve non-null values when field can contain null strings
    public void testStoreRetrieveNonNullValueOnNullableFields() {
        final String testString = "FooBar";
        final byte[] testBytes = new byte[] {42};
        final Date testDate = newDate(2000, 1, 1);
        testRealm.beginTransaction();
        NullTypes nullTypes = testRealm.createObject(NullTypes.class);
        // 1 String
        nullTypes.setFieldStringNull(testString);
        // 2 Bytes
        nullTypes.setFieldBytesNull(testBytes);
        // 3 Boolean
        nullTypes.setFieldBooleanNull(true);
        // 4 Byte
        nullTypes.setFieldByteNull((byte)42);
        // 5 Short
        nullTypes.setFieldShortNull((short)42);
        // 6 Integer
        nullTypes.setFieldIntegerNull(42);
        // 7 Long
        nullTypes.setFieldLongNull(42L);
        // 8 Float
        nullTypes.setFieldFloatNull(42.42F);
        // 9 Double
        nullTypes.setFieldDoubleNull(42.42D);
        // 10 Date
        nullTypes.setFieldDateNull(testDate);
        testRealm.commitTransaction();

        nullTypes = testRealm.where(NullTypes.class).findFirst();
        // 1 String
        assertEquals(testString, nullTypes.getFieldStringNull());
        // 2 Bytes
        assertArrayEquals(testBytes, nullTypes.getFieldBytesNull());
        // 3 Boolean
        assertTrue(nullTypes.getFieldBooleanNull());
        // 4 Byte
        assertEquals((byte)42, (byte)nullTypes.getFieldByteNull().intValue());
        // 5 Short
        assertEquals((short)42, (short)nullTypes.getFieldShortNull().intValue());
        // 6 Integer
        assertEquals(42, nullTypes.getFieldIntegerNull().intValue());
        // 7 Long
        assertEquals(42L, nullTypes.getFieldLongNull().longValue());
        // 8 Float
        assertEquals(42.42F, nullTypes.getFieldFloatNull());
        // 9 Double
        assertEquals(42.42D, nullTypes.getFieldDoubleNull());
        // 10 Date
        assertEquals(testDate.getTime(), nullTypes.getFieldDateNull().getTime());
    }

    // try to store null values in non-nullable fields
    public void testStoreNullValuesToNonNullableFields() {
        try {
            testRealm.beginTransaction();
            NullTypes nullTypes = testRealm.createObject(NullTypes.class);
            // 1 String
            try {
                nullTypes.setFieldStringNotNull(null);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
            // 2 Bytes
            try {
                nullTypes.setFieldBytesNotNull(null);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
            // 3 Boolean
            try {
                nullTypes.setFieldBooleanNotNull(null);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
            // 4 Byte
            try {
                nullTypes.setFieldByteNotNull(null);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
            // 5 Short
            try {
                nullTypes.setFieldShortNotNull(null);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
            // 6 Integer
            try {
                nullTypes.setFieldIntegerNotNull(null);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
            // 7 Long
            try {
                nullTypes.setFieldLongNotNull(null);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
            // 8 Float
            try {
                nullTypes.setFieldFloatNotNull(null);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
            // 9 Double
            try {
                nullTypes.setFieldDoubleNotNull(null);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
            // 10 Date
            try {
                nullTypes.setFieldDateNotNull(null);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
        }
        finally {
            testRealm.cancelTransaction();
        }
    }

    public void testDefaultValuesForNewlyCreatedObject() {
        testRealm.beginTransaction();
        testRealm.createObject(NullTypes.class);
        testRealm.commitTransaction();

        NullTypes nullTypes = testRealm.where(NullTypes.class).findFirst();
        assertNotNull(nullTypes);

        assertEquals(0, nullTypes.getId());
        // 1 String
        assertEquals("", nullTypes.getFieldStringNotNull());
        assertNull(nullTypes.getFieldStringNull());
        // 2 Bytes
        assertArrayEquals(new byte[0], nullTypes.getFieldBytesNotNull());
        assertNull(nullTypes.getFieldByteNull());
        // 3 Boolean
        assertFalse(nullTypes.getFieldBooleanNotNull());
        assertNull(nullTypes.getFieldBooleanNull());
        // 4 Byte
        assertEquals(0, nullTypes.getFieldByteNotNull().byteValue());
        assertNull(nullTypes.getFieldByteNull());
        // 5 Short
        assertEquals(0, nullTypes.getFieldShortNotNull().shortValue());
        assertNull(nullTypes.getFieldShortNull());
        // 6 Integer
        assertEquals(0, nullTypes.getFieldIntegerNotNull().intValue());
        assertNull(nullTypes.getFieldIntegerNull());
        // 7 Long
        assertEquals(0, nullTypes.getFieldLongNotNull().longValue());
        assertNull(nullTypes.getFieldLongNull());
        // 8 Float
        assertEquals(0F, nullTypes.getFieldFloatNotNull());
        assertNull(nullTypes.getFieldFloatNull());
        // 9 Double
        assertEquals(0D, nullTypes.getFieldDoubleNotNull());
        assertNull(nullTypes.getFieldDoubleNull());
        // 10 Date
        assertEquals(new Date(0), nullTypes.getFieldDateNotNull());
        assertNull(nullTypes.getFieldDateNull());
    }

    public void testAccessObjectRemovalThrows() throws InterruptedException {

        testRealm.beginTransaction();
        AllTypes obj = testRealm.createObject(AllTypes.class);
        testRealm.commitTransaction();

        final CountDownLatch objectDeletedInBackground = new CountDownLatch(1);
        new java.lang.Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(realmConfig);
                realm.beginTransaction();
                realm.clear(AllTypes.class);
                realm.commitTransaction();
                realm.close();
                objectDeletedInBackground.countDown();
            }
        }).start();
        objectDeletedInBackground.await(2, TimeUnit.SECONDS);
        testRealm.refresh(); // Move to version where underlying object is deleted.

        try {
            obj.getColumnLong();
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    public void testIsValid() {
        testRealm.beginTransaction();
        Dog dog = testRealm.createObject(Dog.class);
        dog.setName("Fido");
        testRealm.commitTransaction();

        assertTrue(dog.isValid());

        testRealm.beginTransaction();
        dog.removeFromRealm();
        testRealm.commitTransaction();

        assertFalse(dog.isValid());
    }

    // Test NaN value on float and double columns
    public void testFloatDoubleNaN() {
        testRealm.beginTransaction();
        AllTypes allTypes = testRealm.createObject(AllTypes.class);
        allTypes.setColumnFloat(Float.NaN);
        allTypes.setColumnDouble(Double.NaN);
        testRealm.commitTransaction();
        assertEquals(Float.NaN, testRealm.where(AllTypes.class).findFirst().getColumnFloat());
        assertEquals(Double.NaN, testRealm.where(AllTypes.class).findFirst().getColumnDouble());
        // NaN != NaN !!!
        assertEquals(0, testRealm.where(AllTypes.class).equalTo("columnFloat", Float.NaN).count());
        assertEquals(0, testRealm.where(AllTypes.class).equalTo("columnDouble", Double.NaN).count());
    }

    // Test max value on float and double columns
    public void testFloatDoubleMaxValue() {
        testRealm.beginTransaction();
        AllTypes allTypes = testRealm.createObject(AllTypes.class);
        allTypes.setColumnFloat(Float.MAX_VALUE);
        allTypes.setColumnDouble(Double.MAX_VALUE);
        testRealm.commitTransaction();
        assertEquals(Float.MAX_VALUE, testRealm.where(AllTypes.class).findFirst().getColumnFloat());
        assertEquals(Double.MAX_VALUE, testRealm.where(AllTypes.class).findFirst().getColumnDouble());
        assertEquals(1, testRealm.where(AllTypes.class).equalTo("columnFloat", Float.MAX_VALUE).count());
        assertEquals(1, testRealm.where(AllTypes.class).equalTo("columnDouble", Double.MAX_VALUE).count());
    }

    // Test min normal value on float and double columns
    public void testFloatDoubleMinNormal() {
        testRealm.beginTransaction();
        AllTypes allTypes = testRealm.createObject(AllTypes.class);
        allTypes.setColumnFloat(Float.MIN_NORMAL);
        allTypes.setColumnDouble(Double.MIN_NORMAL);
        testRealm.commitTransaction();
        assertEquals(Float.MIN_NORMAL, testRealm.where(AllTypes.class).findFirst().getColumnFloat());
        assertEquals(Double.MIN_NORMAL, testRealm.where(AllTypes.class).findFirst().getColumnDouble());
        assertEquals(1, testRealm.where(AllTypes.class).equalTo("columnFloat", Float.MIN_NORMAL).count());
        assertEquals(1, testRealm.where(AllTypes.class).equalTo("columnDouble", Double.MIN_NORMAL).count());
    }

    // Test min value on float and double columns
    public void testFloatDoubleMinValue() {
        testRealm.beginTransaction();
        AllTypes allTypes = testRealm.createObject(AllTypes.class);
        allTypes.setColumnFloat(Float.MIN_VALUE);
        allTypes.setColumnDouble(Double.MIN_VALUE);
        testRealm.commitTransaction();
        assertEquals(Float.MIN_VALUE, testRealm.where(AllTypes.class).findFirst().getColumnFloat());
        assertEquals(Double.MIN_VALUE, testRealm.where(AllTypes.class).findFirst().getColumnDouble());
        assertEquals(1, testRealm.where(AllTypes.class).equalTo("columnFloat", Float.MIN_VALUE).count());
        assertEquals(1, testRealm.where(AllTypes.class).equalTo("columnDouble", Double.MIN_VALUE).count());
    }

    // Test negative infinity value on float and double columns
    public void testFloatDoubleNegativeInfinity() {
        testRealm.beginTransaction();
        AllTypes allTypes = testRealm.createObject(AllTypes.class);
        allTypes.setColumnFloat(Float.NEGATIVE_INFINITY);
        allTypes.setColumnDouble(Double.NEGATIVE_INFINITY);
        testRealm.commitTransaction();
        assertEquals(Float.NEGATIVE_INFINITY, testRealm.where(AllTypes.class).findFirst().getColumnFloat());
        assertEquals(Double.NEGATIVE_INFINITY, testRealm.where(AllTypes.class).findFirst().getColumnDouble());
        assertEquals(1, testRealm.where(AllTypes.class).equalTo("columnFloat", Float.NEGATIVE_INFINITY).count());
        assertEquals(1, testRealm.where(AllTypes.class).equalTo("columnDouble", Double.NEGATIVE_INFINITY).count());
    }

    // Test positive infinity value on float and double columns
    public void testFloatPositiveInfinity() {
        testRealm.beginTransaction();
        AllTypes allTypes = testRealm.createObject(AllTypes.class);
        allTypes.setColumnFloat(Float.POSITIVE_INFINITY);
        allTypes.setColumnDouble(Double.POSITIVE_INFINITY);
        testRealm.commitTransaction();
        assertEquals(Float.POSITIVE_INFINITY, testRealm.where(AllTypes.class).findFirst().getColumnFloat());
        assertEquals(Double.POSITIVE_INFINITY, testRealm.where(AllTypes.class).findFirst().getColumnDouble());
        assertEquals(1, testRealm.where(AllTypes.class).equalTo("columnFloat", Float.POSITIVE_INFINITY).count());
        assertEquals(1, testRealm.where(AllTypes.class).equalTo("columnDouble", Double.POSITIVE_INFINITY).count());
    }
}
