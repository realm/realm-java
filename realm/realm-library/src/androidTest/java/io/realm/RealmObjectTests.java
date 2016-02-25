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

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.concurrent.atomic.AtomicReference;

import io.realm.entities.AllTypes;
import io.realm.entities.CyclicType;
import io.realm.entities.Dog;
import io.realm.entities.NullTypes;
import io.realm.entities.StringAndInt;
import io.realm.entities.Thread;
import io.realm.internal.Row;
import io.realm.internal.Table;
import io.realm.rule.TestRealmConfigurationFactory;

import static io.realm.internal.test.ExtraTests.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmObjectTests {

    private static final int TEST_SIZE = 5;
    private static final boolean REMOVE_FIRST = true;
    private static final boolean REMOVE_LAST = false;

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Realm realm;
    private RealmConfiguration realmConfig;

    @Before
    public void setUp() {
        realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    // FIXME remove?
    @Test
    public void row_isValid() {
        realm.beginTransaction();
        RealmObject realmObject = realm.createObject(AllTypes.class);
        Row row = realmObject.row;
        realm.commitTransaction();

        assertNotNull("RealmObject.realmGetRow returns zero ", row);
        assertEquals(9, row.getColumnCount());
    }

    @Test
    public void stringEncoding() {
        String[] strings = {"ABCD", "ÆØÅ", "Ö∫Ë", "ΠΑΟΚ", "Здравей"};

        realm.beginTransaction();
        realm.clear(AllTypes.class);

        for (String str : strings) {
            AllTypes obj1 = realm.createObject(AllTypes.class);
            obj1.setColumnString(str);
        }
        realm.commitTransaction();

        RealmResults<AllTypes> objects = realm.allObjects(AllTypes.class);
        assertEquals(strings.length, objects.size());
        int i = 0;
        for (AllTypes obj : objects) {
            String s = obj.getColumnString();
            assertEquals(strings[i], s);
            i++;
        }
    }

    // invalid surrogate pairs:
    // both high and low should lead to an IllegalArgumentException
    @Test
    public void invalidSurrogates() {
        String high = "Invalid high surrogate \uD83C\uD83C\uDF51";
        String low  = "Invalid low surrogate \uD83C\uDF51\uDF51";

        realm.beginTransaction();
        realm.clear(AllTypes.class);
        realm.commitTransaction();

        realm.beginTransaction();
        try {
            AllTypes highSurrogate = realm.createObject(AllTypes.class);
            highSurrogate.setColumnString(high);
            fail();
        } catch (IllegalArgumentException ignored) {}
        realm.cancelTransaction();

        realm.beginTransaction();
        try {
            AllTypes lowSurrogate = realm.createObject(AllTypes.class);
            lowSurrogate.setColumnString(low);
            fail();
        } catch (IllegalArgumentException ignored) {}
        realm.cancelTransaction();
    }

    // removing original object and see if has been removed
    @Test
    public void removeFromRealm() {
        realm.beginTransaction();
        Dog rex = realm.createObject(Dog.class);
        rex.setName("Rex");
        Dog fido = realm.createObject(Dog.class);
        fido.setName("Fido");
        realm.commitTransaction();

        RealmResults<Dog> allDogsBefore = realm.where(Dog.class).equalTo("name", "Rex").findAll();
        assertEquals(1, allDogsBefore.size());

        realm.beginTransaction();
        rex.removeFromRealm();
        realm.commitTransaction();

        RealmResults<Dog> allDogsAfter = realm.where(Dog.class).equalTo("name", "Rex").findAll();
        assertEquals(0, allDogsAfter.size());

        fido.getName();
        try {
            rex.getName();
            realm.close();
            fail();
        } catch (IllegalStateException ignored) {}

        // deleting rex twice should fail
        realm.beginTransaction();
        try {
            rex.removeFromRealm();
            realm.close();
            fail();
        } catch (IllegalStateException ignored) {}
        realm.commitTransaction();
        realm.close();
    }

    @Test
    public void removeFromRealm_twiceThrows() {
        realm.beginTransaction();
        Dog dog = realm.createObject(Dog.class);
        dog.setAge(42);
        realm.commitTransaction();

        realm.beginTransaction();
        assertTrue(dog.isValid());
        dog.removeFromRealm();
        assertFalse(dog.isValid());

        try {
            dog.removeFromRealm();
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    // query for an object, remove it and see it has been removed from realm
    @Test
    public void removeFromRealm_removedFromResults() {
        realm.beginTransaction();
        realm.clear(Dog.class);
        Dog dogToAdd = realm.createObject(Dog.class);
        dogToAdd.setName("Rex");
        realm.commitTransaction();

        assertEquals(1, realm.allObjects(Dog.class).size());

        Dog dogToRemove = realm.where(Dog.class).findFirst();
        assertNotNull(dogToRemove);
        realm.beginTransaction();
        dogToRemove.removeFromRealm();
        realm.commitTransaction();

        assertEquals(0, realm.allObjects(Dog.class).size());
        try {
            dogToAdd.getName();
            realm.close();
            fail();
        } catch (IllegalStateException ignored) {
        }
        try {
            dogToRemove.getName();
            realm.close();
            fail();
        } catch (IllegalStateException ignored) {
        }
        realm.close();
    }

    private void removeOneByOne(boolean atFirst) {
        Set<Long> ages = new HashSet<Long>();
        realm.beginTransaction();
        realm.clear(Dog.class);
        for (int i = 0; i < TEST_SIZE; i++) {
            Dog dog = realm.createObject(Dog.class);
            dog.setAge(i);
            ages.add((long) i);
        }
        realm.commitTransaction();

        assertEquals(TEST_SIZE, realm.allObjects(Dog.class).size());

        RealmResults<Dog> dogs = realm.allObjects(Dog.class);
        for (int i = 0; i < TEST_SIZE; i++) {
            realm.beginTransaction();
            Dog dogToRemove;
            if (atFirst) {
                dogToRemove = dogs.first();
            } else {
                dogToRemove = dogs.last();
            }
            ages.remove(dogToRemove.getAge());
            dogToRemove.removeFromRealm();

            // object is no longer valid
            try {
                dogToRemove.getAge();
                fail();
            }
            catch (IllegalStateException ignored) {}

            realm.commitTransaction();

            // and removed from realm and remaining objects are place correctly
            RealmResults<Dog> remainingDogs = realm.allObjects(Dog.class);
            assertEquals(TEST_SIZE - i - 1, remainingDogs.size());
            for (Dog dog : remainingDogs) {
                assertTrue(ages.contains(dog.getAge()));
            }
        }
    }

    @Test
    public void removeFromRealm_atPosition() {
        removeOneByOne(REMOVE_FIRST);
        removeOneByOne(REMOVE_LAST);
    }

    private enum Method {
        METHOD_GETTER,
        METHOD_SETTER,
        METHOD_REMOVE_FROM_REALM
    }

    private boolean runMethodOnWrongThread(final Method method) throws ExecutionException, InterruptedException {
        final AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    switch (method) {
                        case METHOD_GETTER:
                            allTypes.getColumnFloat();
                           break;
                        case METHOD_SETTER:
                            allTypes.setColumnFloat(1.0f);
                            break;
                        case METHOD_REMOVE_FROM_REALM:
                            allTypes.removeFromRealm();
                            break;
                    }
                    return false;
                } catch (IllegalStateException ignored) {
                    return true;
                }
            }
        });

        Boolean result = future.get();
        return result;
    }

    @Test
    public void methodsThrowOnWrongThread() throws ExecutionException, InterruptedException {
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        for (Method method : Method.values()) {
            assertTrue(runMethodOnWrongThread(method));
        }
    }

    @Test
    public void equals_sameObjectDifferentInstance() {
        realm.beginTransaction();
        CyclicType ct = realm.createObject(CyclicType.class);
        ct.setName("Foo");
        realm.commitTransaction();

        CyclicType ct1 = realm.where(CyclicType.class).findFirst();
        CyclicType ct2 = realm.where(CyclicType.class).findFirst();

        assertTrue(ct1.equals(ct2));
        assertTrue(ct2.equals(ct1));
    }

    @Test
    public void equals_differentObjects() {
        realm.beginTransaction();
        CyclicType objA = realm.createObject(CyclicType.class);
        objA.setName("Foo");
        CyclicType objB = realm.createObject(CyclicType.class);
        objB.setName("Bar");
        realm.commitTransaction();

        assertFalse(objA.equals(objB));
        assertFalse(objB.equals(objA));
    }

    @Test
    public void equals_afterModification() {
        realm.beginTransaction();
        CyclicType ct = realm.createObject(CyclicType.class);
        ct.setName("Foo");
        realm.commitTransaction();

        CyclicType ct1 = realm.where(CyclicType.class).findFirst();
        CyclicType ct2 = realm.where(CyclicType.class).findFirst();

        realm.beginTransaction();
        ct1.setName("Baz");
        realm.commitTransaction();

        assertTrue(ct1.equals(ct2));
        assertTrue(ct2.equals(ct1));
    }

    @Test
    public void equals_standAloneObject() {
        realm.beginTransaction();
        CyclicType ct1 = realm.createObject(CyclicType.class);
        ct1.setName("Foo");
        realm.commitTransaction();

        CyclicType ct2 = new CyclicType();
        ct2.setName("Bar");

        assertFalse(ct1.equals(ct2));
        assertFalse(ct2.equals(ct1));
    }

    @Test
    public void equals_cyclicObject() {
        realm.beginTransaction();
        CyclicType foo = createCyclicData();
        realm.commitTransaction();

        assertEquals(foo, realm.where(CyclicType.class).equalTo("name", "Foo").findFirst());
    }

    @Test
    public void toString_cyclicObject() {
        realm.beginTransaction();
        CyclicType foo = createCyclicData();
        realm.commitTransaction();
        String expected = "CyclicType = [{name:Foo},{object:CyclicType},{otherObject:null},{objects:RealmList<CyclicType>[0]}]";
        assertEquals(expected, foo.toString());
    }

    @Test
    public void hashCode_cyclicObject() {
        realm.beginTransaction();
        final CyclicType foo = createCyclicData();
        realm.commitTransaction();

        // Check that the hash code is always the same between multiple calls.
        assertEquals(foo.hashCode(), foo.hashCode());
        // Check that the hash code is the same among same object
        assertEquals(foo.hashCode(), realm.where(CyclicType.class).equalTo("name", foo.getName()).findFirst().hashCode());
        // hash code is different from other objects.
        assertNotEquals(foo.getObject().hashCode(), foo.hashCode());

        final int originalHashCode = foo.hashCode();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                foo.setName(foo.getName() + "1234");
            }
        });
        // Check that Updating the value of its field does not affect the hash code.
        assertEquals(originalHashCode, foo.hashCode());

        // Check the hash code of the object from a Realm in different file name.
        RealmConfiguration realmConfig_differentName = configFactory.createConfiguration(
                "another_" + realmConfig.getRealmFileName());
        Realm realm_differentName = Realm.getInstance(realmConfig_differentName);
        //noinspection TryFinallyCanBeTryWithResources
        try {
            realm_differentName.beginTransaction();
            CyclicType fooFromDifferentName = createCyclicData(realm_differentName);
            realm_differentName.commitTransaction();

            assertNotEquals(fooFromDifferentName.hashCode(), foo.hashCode());
        } finally {
            realm_differentName.close();
        }

        // Check the hash code of the object from a Realm in different folder.
        RealmConfiguration realmConfig_differentPath = configFactory.createConfiguration(
                "anotherDir", realmConfig.getRealmFileName());
        Realm realm_differentPath = Realm.getInstance(realmConfig_differentPath);
        //noinspection TryFinallyCanBeTryWithResources
        try {
            realm_differentPath.beginTransaction();
            CyclicType fooFromDifferentPath = createCyclicData(realm_differentPath);
            realm_differentPath.commitTransaction();

            assertNotEquals(fooFromDifferentPath.hashCode(), foo.hashCode());
        } finally {
            realm_differentPath.close();
        }
    }

    private CyclicType createCyclicData(Realm realm) {
        CyclicType foo = realm.createObject(CyclicType.class);
        foo.setName("Foo");
        CyclicType bar = realm.createObject(CyclicType.class);
        bar.setName("Bar");

        // Setup cycle on normal object references
        foo.setObject(bar);
        bar.setObject(foo);
        return foo;
    }

    private CyclicType createCyclicData() {
        return createCyclicData(realm);
    }

    @Test
    public void dateType() {
        long testDatesValid[] = {-1000, 0, 1000};
        long testDatesLoosePrecision[] = {Long.MIN_VALUE, 1, 1001, Long.MAX_VALUE};

        // test valid dates
        realm.beginTransaction();
        for (long value : testDatesValid) {
            AllTypes allTypes = realm.createObject(AllTypes.class);
            allTypes.setColumnDate(new Date(value));
        }
        realm.commitTransaction();

        int i = 0;
        for (AllTypes allTypes : realm.allObjects(AllTypes.class)) {
            assertEquals("Item " + i, new Date(testDatesValid[i]), allTypes.getColumnDate());
            i++;
        }

        // test valid dates but with precision lost
        realm.beginTransaction();
        realm.clear(AllTypes.class);
        for (long value : testDatesLoosePrecision) {
            AllTypes allTypes = realm.createObject(AllTypes.class);
            allTypes.setColumnDate(new Date(value));
        }
        realm.commitTransaction();

        i = 0;
        for (AllTypes allTypes : realm.allObjects(AllTypes.class)) {
            assertFalse("Item " + i, new Date(testDatesLoosePrecision[i]) == allTypes.getColumnDate());
            assertEquals("Item " + i, new Date(1000*(testDatesLoosePrecision[i]/1000)), allTypes.getColumnDate());
            i++;
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

    @Test
    public void setter_outsideTransactionThrows() {
        realm.beginTransaction();
        Dog dog = realm.createObject(Dog.class);
        realm.commitTransaction();

        thrown.expect(IllegalStateException.class);
        dog.setName("Rex");
    }

    @Test
    public void setter_link_null() {
        realm.beginTransaction();
        CyclicType objA = realm.createObject(CyclicType.class);
        objA.setName("Foo");
        CyclicType objB = realm.createObject(CyclicType.class);
        objB.setName("Bar");

        objA.setObject(objB);

        assertNotNull(objA.getObject());

        try {
            objA.setObject(null);
        } catch (NullPointerException nullPointer) {
            fail();
        }
        realm.commitTransaction();
        assertNull(objA.getObject());
    }

    @Test
    public void setter_link_standaloneObject() {
        CyclicType standalone = new CyclicType();

        realm.beginTransaction();
        try {
            CyclicType target = realm.createObject(CyclicType.class);

            try {
                target.setObject(standalone);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void setter_link_deletedObject() {
        realm.beginTransaction();
        try {
            CyclicType target = realm.createObject(CyclicType.class);

            CyclicType removed = realm.createObject(CyclicType.class);
            removed.removeFromRealm();

            try {
                target.setObject(removed);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void setter_link_closedObject() {
        realm.beginTransaction();
        CyclicType closed = realm.createObject(CyclicType.class);
        realm.commitTransaction();
        realm.close();
        assertTrue(realm.isClosed());

        realm = Realm.getInstance(realmConfig);
        realm.beginTransaction();
        try {
            CyclicType target = realm.createObject(CyclicType.class);

            try {
                target.setObject(closed);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void setter_link_objectFromOtherRealm() {
        RealmConfiguration config = configFactory.createConfiguration("another.realm");
        Realm anotherRealm = Realm.getInstance(config);
        //noinspection TryFinallyCanBeTryWithResources
        try {
            anotherRealm.beginTransaction();
            CyclicType objFromAnotherRealm = anotherRealm.createObject(CyclicType.class);
            anotherRealm.commitTransaction();

            realm.beginTransaction();
            try {
                CyclicType target = realm.createObject(CyclicType.class);

                try {
                    target.setObject(objFromAnotherRealm);
                    fail();
                } catch (IllegalArgumentException ignored) {
                }
            } finally {
                realm.cancelTransaction();
            }
        } finally {
            anotherRealm.close();
        }
    }

    @Test
    public void setter_link_objectFromAnotherThread() throws InterruptedException {
        final CountDownLatch createLatch = new CountDownLatch(1);
        final CountDownLatch testEndLatch = new CountDownLatch(1);

        final AtomicReference<CyclicType> objFromAnotherThread = new AtomicReference<>();

        java.lang.Thread thread = new java.lang.Thread() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(realmConfig);

                // 1. create an object
                realm.beginTransaction();
                objFromAnotherThread.set(realm.createObject(CyclicType.class));
                realm.commitTransaction();

                createLatch.countDown();
                try {
                    testEndLatch.await();
                } catch (InterruptedException ignored) {
                }

                // 3. close Realm in this thread and finish.
                realm.close();
            }
        };
        thread.start();

        createLatch.await();
        // 2. set created object to target
        realm.beginTransaction();
        try {
            CyclicType target = realm.createObject(CyclicType.class);
            try {
                target.setObject(objFromAnotherThread.get());
                fail();
            } catch (IllegalArgumentException ignored) {
            }
        } finally {
            testEndLatch.countDown();
            realm.cancelTransaction();
        }

        // wait for finishing the thread
        thread.join();
    }

    @Test
    public void setter_list_withStandaloneObject() {
        CyclicType standalone = new CyclicType();

        realm.beginTransaction();
        try {
            CyclicType target = realm.createObject(CyclicType.class);

            RealmList<CyclicType> list = new RealmList<>();
            list.add(realm.createObject(CyclicType.class));
            list.add(standalone); // List contains a standalone object
            list.add(realm.createObject(CyclicType.class));

            try {
                target.setObjects(list);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void setter_list_withDeletedObject() {
        realm.beginTransaction();
        try {
            CyclicType target = realm.createObject(CyclicType.class);

            CyclicType removed = realm.createObject(CyclicType.class);
            removed.removeFromRealm();

            RealmList<CyclicType> list = new RealmList<>();
            list.add(realm.createObject(CyclicType.class));
            list.add(removed); // List contains a deleted object
            list.add(realm.createObject(CyclicType.class));

            try {
                target.setObjects(list);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void setter_list_withClosedObject() {
        realm.beginTransaction();
        CyclicType closed = realm.createObject(CyclicType.class);
        realm.commitTransaction();
        realm.close();
        assertTrue(realm.isClosed());

        realm = Realm.getInstance(realmConfig);
        realm.beginTransaction();
        try {
            CyclicType target = realm.createObject(CyclicType.class);

            RealmList<CyclicType> list = new RealmList<>();
            list.add(realm.createObject(CyclicType.class));
            list.add(closed); // List contains a closed object
            list.add(realm.createObject(CyclicType.class));

            try {
                target.setObjects(list);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void setter_list_withObjectFromAnotherRealm() {
        RealmConfiguration config = configFactory.createConfiguration("another.realm");
        Realm anotherRealm = Realm.getInstance(config);
        //noinspection TryFinallyCanBeTryWithResources
        try {
            anotherRealm.beginTransaction();
            CyclicType objFromAnotherRealm = anotherRealm.createObject(CyclicType.class);
            anotherRealm.commitTransaction();

            realm.beginTransaction();
            try {
                CyclicType target = realm.createObject(CyclicType.class);

                RealmList<CyclicType> list = new RealmList<>();
                list.add(realm.createObject(CyclicType.class));
                list.add(objFromAnotherRealm); // List contains an object from another Realm
                list.add(realm.createObject(CyclicType.class));

                try {
                    target.setObjects(list);
                    fail();
                } catch (IllegalArgumentException ignored) {
                }
            } finally {
                realm.cancelTransaction();
            }
        } finally {
            anotherRealm.close();
        }
    }

    @Test
    public void setter_list_withObjectFromAnotherThread() throws InterruptedException {
        final CountDownLatch createLatch = new CountDownLatch(1);
        final CountDownLatch testEndLatch = new CountDownLatch(1);

        final AtomicReference<CyclicType> objFromAnotherThread = new AtomicReference<>();

        java.lang.Thread thread = new java.lang.Thread() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(realmConfig);

                // 1. create an object
                realm.beginTransaction();
                objFromAnotherThread.set(realm.createObject(CyclicType.class));
                realm.commitTransaction();

                createLatch.countDown();
                try {
                    testEndLatch.await();
                } catch (InterruptedException ignored) {
                }

                // 3. close Realm in this thread and finish.
                realm.close();
            }
        };
        thread.start();

        createLatch.await();
        // 2. set created object to target
        realm.beginTransaction();
        try {
            CyclicType target = realm.createObject(CyclicType.class);

            RealmList<CyclicType> list = new RealmList<>();
            list.add(realm.createObject(CyclicType.class));
            list.add(objFromAnotherThread.get()); // List contains an object from another thread.
            list.add(realm.createObject(CyclicType.class));

            try {
                target.setObjects(list);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
        } finally {
            testEndLatch.countDown();
            realm.cancelTransaction();
        }

        // wait for finishing the thread
        thread.join();
    }

    @Test
    public void classNameConflictsWithFrameworkClass() {
        // The model class' name (Thread) clashed with a common Java class.
        // The annotation process must be able to handle that.
        realm.beginTransaction();
        @SuppressWarnings("unused")
        Thread thread = realm.createObject(Thread.class);
        realm.commitTransaction();
    }

    @Test
    public void isValid_standaloneObject() {
        AllTypes allTypes = new AllTypes();
        assertFalse(allTypes.isValid());
    }

    @Test
    public void isValid_closedRealm() {
        RealmConfiguration otherConfig = configFactory.createConfiguration("other-realm");
        Realm testRealm = Realm.getInstance(otherConfig);
        testRealm.beginTransaction();
        AllTypes allTypes = testRealm.createObject(AllTypes.class);
        assertTrue(allTypes.isValid());
        testRealm.commitTransaction();
        testRealm.close();
        assertFalse(allTypes.isValid());
    }

    @Test
    public void IsValid_deletedObject() {
        realm.beginTransaction();
        AllTypes allTypes = realm.createObject(AllTypes.class);
        assertTrue(allTypes.isValid());
        realm.clear(AllTypes.class);
        realm.commitTransaction();
        assertFalse(allTypes.isValid());
    }

    @Test
    public void isValid_managedObject() {
        realm.beginTransaction();
        AllTypes allTypes = realm.createObject(AllTypes.class);
        assertTrue(allTypes.isValid());
        realm.commitTransaction();
        assertTrue(allTypes.isValid());
    }

    // store and retrieve null values for nullable fields
    @Test
    public void set_get_nullOnNullableFields() {
        realm.beginTransaction();
        NullTypes nullTypes = realm.createObject(NullTypes.class);
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
        realm.commitTransaction();

        nullTypes = realm.where(NullTypes.class).findFirst();
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
    @Test
    public void get_set_nonNullValueOnNullableFields() {
        final String testString = "FooBar";
        final byte[] testBytes = new byte[] {42};
        final Date testDate = newDate(2000, 1, 1);
        realm.beginTransaction();
        NullTypes nullTypes = realm.createObject(NullTypes.class);
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
        realm.commitTransaction();

        nullTypes = realm.where(NullTypes.class).findFirst();
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
        assertEquals(42.42F, nullTypes.getFieldFloatNull(), 0.0F);
        // 9 Double
        assertEquals(42.42D, nullTypes.getFieldDoubleNull(), 0.0D);
        // 10 Date
        assertEquals(testDate.getTime(), nullTypes.getFieldDateNull().getTime());
    }

    // try to store null values in non-nullable fields
    @Test
    public void set_nullValuesToNonNullableFields() {
        try {
            realm.beginTransaction();
            NullTypes nullTypes = realm.createObject(NullTypes.class);
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
            realm.cancelTransaction();
        }
    }

    @Test
    public void defaultValuesForNewObject() {
        realm.beginTransaction();
        NullTypes nullTypes = realm.createObject(NullTypes.class);
        realm.commitTransaction();

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
        assertEquals(0F, nullTypes.getFieldFloatNotNull(), 0.0F);
        assertNull(nullTypes.getFieldFloatNull());
        // 9 Double
        assertEquals(0D, nullTypes.getFieldDoubleNotNull(), 0.0D);
        assertNull(nullTypes.getFieldDoubleNull());
        // 10 Date
        assertEquals(new Date(0), nullTypes.getFieldDateNotNull());
        assertNull(nullTypes.getFieldDateNull());
    }

    @Test
    public void getter_afterDeleteFromOtherThreadThrows() {
        realm.beginTransaction();
        AllTypes obj = realm.createObject(AllTypes.class);
        realm.commitTransaction();

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
        TestHelper.awaitOrFail(objectDeletedInBackground);
        realm.refresh(); // Move to version where underlying object is deleted.

        // Object should no longer be available
        assertFalse(obj.isValid());
        thrown.expect(IllegalStateException.class);
        obj.getColumnLong();
    }

    @Test
    public void isValid() {
        realm.beginTransaction();
        Dog dog = realm.createObject(Dog.class);
        dog.setName("Fido");
        realm.commitTransaction();

        assertTrue(dog.isValid());

        realm.beginTransaction();
        dog.removeFromRealm();
        realm.commitTransaction();

        assertFalse(dog.isValid());
    }

    // Test NaN value on float and double columns
    @Test
    public void float_double_NaN() {
        realm.beginTransaction();
        AllTypes allTypes = realm.createObject(AllTypes.class);
        allTypes.setColumnFloat(Float.NaN);
        allTypes.setColumnDouble(Double.NaN);
        realm.commitTransaction();
        assertEquals(Float.NaN, realm.where(AllTypes.class).findFirst().getColumnFloat(), 0.0F);
        assertEquals(Double.NaN, realm.where(AllTypes.class).findFirst().getColumnDouble(), 0.0D);
        // NaN != NaN !!!
        assertEquals(0, realm.where(AllTypes.class).equalTo("columnFloat", Float.NaN).count());
        assertEquals(0, realm.where(AllTypes.class).equalTo("columnDouble", Double.NaN).count());
    }

    // Test max value on float and double columns
    @Test
    public void float_double_maxValue() {
        realm.beginTransaction();
        AllTypes allTypes = realm.createObject(AllTypes.class);
        allTypes.setColumnFloat(Float.MAX_VALUE);
        allTypes.setColumnDouble(Double.MAX_VALUE);
        realm.commitTransaction();
        assertEquals(Float.MAX_VALUE, realm.where(AllTypes.class).findFirst().getColumnFloat(), 0.0F);
        assertEquals(Double.MAX_VALUE, realm.where(AllTypes.class).findFirst().getColumnDouble(), 0.0D);
        assertEquals(1, realm.where(AllTypes.class).equalTo("columnFloat", Float.MAX_VALUE).count());
        assertEquals(1, realm.where(AllTypes.class).equalTo("columnDouble", Double.MAX_VALUE).count());
    }

    // Test min normal value on float and double columns
    @Test
    public void float_double_minNormal() {
        realm.beginTransaction();
        AllTypes allTypes = realm.createObject(AllTypes.class);
        allTypes.setColumnFloat(Float.MIN_NORMAL);
        allTypes.setColumnDouble(Double.MIN_NORMAL);
        realm.commitTransaction();
        assertEquals(Float.MIN_NORMAL, realm.where(AllTypes.class).findFirst().getColumnFloat(), 0.0F);
        assertEquals(Double.MIN_NORMAL, realm.where(AllTypes.class).findFirst().getColumnDouble(), 0.0D);
        assertEquals(1, realm.where(AllTypes.class).equalTo("columnFloat", Float.MIN_NORMAL).count());
        assertEquals(1, realm.where(AllTypes.class).equalTo("columnDouble", Double.MIN_NORMAL).count());
    }

    // Test min value on float and double columns
    @Test
    public void float_double_minValue() {
        realm.beginTransaction();
        AllTypes allTypes = realm.createObject(AllTypes.class);
        allTypes.setColumnFloat(Float.MIN_VALUE);
        allTypes.setColumnDouble(Double.MIN_VALUE);
        realm.commitTransaction();
        assertEquals(Float.MIN_VALUE, realm.where(AllTypes.class).findFirst().getColumnFloat(), 0.0F);
        assertEquals(Double.MIN_VALUE, realm.where(AllTypes.class).findFirst().getColumnDouble(), 0.0D);
        assertEquals(1, realm.where(AllTypes.class).equalTo("columnFloat", Float.MIN_VALUE).count());
        assertEquals(1, realm.where(AllTypes.class).equalTo("columnDouble", Double.MIN_VALUE).count());
    }

    // Test negative infinity value on float and double columns
    @Test
    public void float_double_negativeInfinity() {
        realm.beginTransaction();
        AllTypes allTypes = realm.createObject(AllTypes.class);
        allTypes.setColumnFloat(Float.NEGATIVE_INFINITY);
        allTypes.setColumnDouble(Double.NEGATIVE_INFINITY);
        realm.commitTransaction();
        assertEquals(Float.NEGATIVE_INFINITY, realm.where(AllTypes.class).findFirst().getColumnFloat(), 0.0F);
        assertEquals(Double.NEGATIVE_INFINITY, realm.where(AllTypes.class).findFirst().getColumnDouble(), 0.0D);
        assertEquals(1, realm.where(AllTypes.class).equalTo("columnFloat", Float.NEGATIVE_INFINITY).count());
        assertEquals(1, realm.where(AllTypes.class).equalTo("columnDouble", Double.NEGATIVE_INFINITY).count());
    }

    // Test positive infinity value on float and double columns
    @Test
    public void float_double_positiveInfinity() {
        realm.beginTransaction();
        AllTypes allTypes = realm.createObject(AllTypes.class);
        allTypes.setColumnFloat(Float.POSITIVE_INFINITY);
        allTypes.setColumnDouble(Double.POSITIVE_INFINITY);
        realm.commitTransaction();
        assertEquals(Float.POSITIVE_INFINITY, realm.where(AllTypes.class).findFirst().getColumnFloat(), 0.0F);
        assertEquals(Double.POSITIVE_INFINITY, realm.where(AllTypes.class).findFirst().getColumnDouble(), 0.0D);
        assertEquals(1, realm.where(AllTypes.class).equalTo("columnFloat", Float.POSITIVE_INFINITY).count());
        assertEquals(1, realm.where(AllTypes.class).equalTo("columnDouble", Double.POSITIVE_INFINITY).count());
    }

    private RealmConfiguration prepareColumnSwappedRealm() throws FileNotFoundException {

        final RealmConfiguration columnSwappedRealmConfigForV0 = configFactory.createConfigurationBuilder()
                .name("columnSwapped.realm")
                .migration(new RealmMigration() {
                    @Override
                    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                        final Table table = realm.schema.getTable(StringAndInt.class);
                        final long strIndex = table.getColumnIndex("str");
                        final long numberIndex = table.getColumnIndex("number");

                        while (0 < table.getColumnCount()) {
                            table.removeColumn(0);
                        }

                        final long newStrIndex;
                        // swap column indices
                        if (strIndex < numberIndex) {
                            table.addColumn(RealmFieldType.INTEGER, "number");
                            newStrIndex = table.addColumn(RealmFieldType.STRING, "str");
                        } else {
                            newStrIndex = table.addColumn(RealmFieldType.STRING, "str");
                            table.addColumn(RealmFieldType.INTEGER, "number");
                        }
                        table.convertColumnToNullable(newStrIndex);
                    }
                })
                .build();

        final RealmConfiguration columnSwappedRealmConfigForV1 = configFactory.createConfigurationBuilder()
                .name("columnSwapped.realm")
                .migration(new RealmMigration() {
                    @Override
                    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                        // Do nothing
                    }
                })
                .schemaVersion(1L)
                .build();

        Realm.deleteRealm(columnSwappedRealmConfigForV0);
        Realm.getInstance(columnSwappedRealmConfigForV0).close();
        Realm.migrateRealm(columnSwappedRealmConfigForV0);
        return columnSwappedRealmConfigForV1;
    }

    @Test
    public void realmProxy_columnIndex() throws FileNotFoundException {
        final RealmConfiguration configForSwapped = prepareColumnSwappedRealm();

        // open swapped Realm in order to load column index
        Realm.getInstance(configForSwapped).close();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                final StringAndInt obj = RealmObjectTests.this.realm.createObject(StringAndInt.class);
                /*
                 * If https://github.com/realm/realm-java/issues/1611 issue exists,
                 * setter/getter of RealmObjectProxy uses last loaded column index for every Realm.
                 */
                obj.setStr("foo");
                obj.getStr();
            }
        });
    }
}
