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
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.entities.AllTypes;
import io.realm.entities.AllTypesPrimaryKey;
import io.realm.entities.ConflictingFieldName;
import io.realm.entities.CustomMethods;
import io.realm.entities.CyclicType;
import io.realm.entities.Dog;
import io.realm.entities.NullTypes;
import io.realm.entities.StringAndInt;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.internal.Table;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
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
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();
    @Rule
    public final UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

    private Realm realm;
    private RealmConfiguration realmConfig;

    private Dog createManagedDogObjectFromRealmInstance(Realm testRealm) {
        testRealm.beginTransaction();
        Dog dog = testRealm.createObject(Dog.class);
        testRealm.commitTransaction();
        return dog;
    }

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
        RealmObjectProxy realmObject = (RealmObjectProxy) realm.createObject(AllTypes.class);
        Row row = realmObject.realmGet$proxyState().getRow$realm();
        realm.commitTransaction();

        assertNotNull("RealmObject.realmGetRow returns zero ", row);
        assertEquals(9, row.getColumnCount());
    }

    @Test
    public void stringEncoding() {
        String[] strings = {"ABCD", "ÆØÅ", "Ö∫Ë", "ΠΑΟΚ", "Здравей"};

        realm.beginTransaction();
        realm.delete(AllTypes.class);

        for (String str : strings) {
            AllTypes obj1 = realm.createObject(AllTypes.class);
            obj1.setColumnString(str);
        }
        realm.commitTransaction();

        RealmResults<AllTypes> objects = realm.where(AllTypes.class).findAll();
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
        String low = "Invalid low surrogate \uD83C\uDF51\uDF51";

        realm.beginTransaction();
        realm.delete(AllTypes.class);
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
    public void deleteFromRealm() {
        realm.beginTransaction();
        Dog rex = realm.createObject(Dog.class);
        rex.setName("Rex");
        Dog fido = realm.createObject(Dog.class);
        fido.setName("Fido");
        realm.commitTransaction();

        RealmResults<Dog> allDogsBefore = realm.where(Dog.class).equalTo("name", "Rex").findAll();
        assertEquals(1, allDogsBefore.size());

        realm.beginTransaction();
        rex.deleteFromRealm();
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
            rex.deleteFromRealm();
            realm.close();
            fail();
        } catch (IllegalStateException ignored) {}
        realm.commitTransaction();
        realm.close();
    }

    @Test
    public void deleteFromRealm_twiceThrows() {
        realm.beginTransaction();
        Dog dog = realm.createObject(Dog.class);
        dog.setAge(42);
        realm.commitTransaction();

        realm.beginTransaction();
        assertTrue(dog.isValid());
        dog.deleteFromRealm();
        assertFalse(dog.isValid());

        try {
            dog.deleteFromRealm();
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
    public void deleteFromRealm_throwOnUnmanagedObject() {
        Dog dog = new Dog();

        try {
            dog.deleteFromRealm();
            fail("Failed on deleting a RealmObject from null Row.");
        } catch (IllegalArgumentException ignored) {
        }
    }

    // query for an object, remove it and see it has been removed from realm
    @Test
    public void deleteFromRealm_removedFromResults() {
        realm.beginTransaction();
        realm.delete(Dog.class);
        Dog dogToAdd = realm.createObject(Dog.class);
        dogToAdd.setName("Rex");
        realm.commitTransaction();

        assertEquals(1, realm.where(Dog.class).count());

        Dog dogToRemove = realm.where(Dog.class).findFirst();
        assertNotNull(dogToRemove);
        realm.beginTransaction();
        dogToRemove.deleteFromRealm();
        realm.commitTransaction();

        assertEquals(0, realm.where(Dog.class).count());
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

    private void removeOneByOne(boolean removeFromFront) {
        // Create test data
        realm.beginTransaction();
        realm.delete(Dog.class);
        for (int i = 0; i < TEST_SIZE; i++) {
            realm.createObject(Dog.class);
        }
        realm.commitTransaction();

        // Check initial size
        RealmResults<Dog> dogs = realm.where(Dog.class).findAll();
        assertEquals(TEST_SIZE, dogs.size());

        // Check that calling deleteFromRealm doesn't remove the object from the RealmResult
        realm.beginTransaction();
        for (int i = 0; i < TEST_SIZE; i++) {
            dogs.get(removeFromFront ? i : TEST_SIZE - 1 - i).deleteFromRealm();
        }
        realm.commitTransaction();

        assertEquals(TEST_SIZE, dogs.size());
        assertEquals(0, realm.where(Dog.class).count());
    }

    // Tests calling deleteFromRealm on a RealmResults instead of RealmResults.remove()
    @Test
    public void deleteFromRealm_atPosition() {
        removeOneByOne(REMOVE_FIRST);
        removeOneByOne(REMOVE_LAST);
    }

    private enum Method {
        METHOD_GETTER,
        METHOD_SETTER,
        METHOD_DELETE_FROM_REALM
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
                        case METHOD_DELETE_FROM_REALM:
                            allTypes.deleteFromRealm();
                            break;
                    }
                    return false;
                } catch (IllegalStateException ignored) {
                    return true;
                }
            }
        });

        return future.get();
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
    public void equals_unmanagedObject() {
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
    public void equals_plainCustomMethod() {
        realm.beginTransaction();
        CustomMethods cm = realm.createObject(CustomMethods.class);
        cm.setName("Foo");
        realm.commitTransaction();

        CustomMethods cm1 = realm.where(CustomMethods.class).findFirst();
        CustomMethods cm2 = realm.where(CustomMethods.class).findFirst();
        assertTrue(cm1.equals(cm2));
    }

    @Test
    public void equals_reverseCustomMethod() {
        realm.beginTransaction();
        CustomMethods cm = realm.createObject(CustomMethods.class);
        cm.setName("Foo");
        realm.commitTransaction();

        CustomMethods cm1 = realm.where(CustomMethods.class).findFirst();
        CustomMethods cm2 = realm.where(CustomMethods.class).findFirst();

        realm.beginTransaction();
        cm1.reverseEquals = true;
        realm.commitTransaction();

        assertFalse(cm1.equals(cm2));
    }

    @Test
    public void equals_unmanagedCustomMethod() {
        CustomMethods cm1 = new CustomMethods();
        cm1.setName("Bar");
        CustomMethods cm2 = new CustomMethods();
        cm2.setName("Bar");
        assertTrue(cm1.equals(cm2));
    }

    @Test
    public void equals_mixedCustomMethod() {
        CustomMethods cm1 = new CustomMethods();
        cm1.setName("Bar");
        CustomMethods cm2 = new CustomMethods();
        cm2.setName("Bar");

        realm.beginTransaction();
        realm.deleteAll();
        realm.copyToRealm(cm1);
        realm.commitTransaction();

        CustomMethods cm3 = realm.where(CustomMethods.class).findFirst();
        assertFalse(cm3.equals(cm2));
        assertTrue(cm3.getName().equals(cm2.getName()));
    }

    @Test
    public void toString_cyclicObject() {
        realm.beginTransaction();
        CyclicType foo = createCyclicData();
        realm.commitTransaction();
        String expected = "CyclicType = [{id:0},{name:Foo},{date:null},{object:CyclicType},{otherObject:null},{objects:RealmList<CyclicType>[0]}]";
        assertEquals(expected, foo.toString());
    }

    @Test
    public void toString_customMethod() {
        realm.beginTransaction();
        CustomMethods cm = realm.createObject(CustomMethods.class);
        cm.setName("Foo");
        realm.commitTransaction();
        String expected = CustomMethods.CUSTOM_TO_STRING;
        assertEquals(expected, cm.toString());
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

        // Check the hash code of the object from a Realm in different directory.
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

    @Test
    public void hashCode_customMethod() {
        realm.beginTransaction();
        CustomMethods cm = realm.createObject(CustomMethods.class);
        cm.setName("Foo");
        realm.commitTransaction();
        assertEquals(CustomMethods.HASHCODE, cm.hashCode());
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
        long testDatesValid[] = {Long.MIN_VALUE, -1001, -1000, -1, 0, 1, 1000, 1001, Long.MAX_VALUE};

        realm.beginTransaction();
        for (long value : testDatesValid) {
            AllTypes allTypes = realm.createObject(AllTypes.class);
            allTypes.setColumnDate(new Date(value));
        }
        realm.commitTransaction();

        int i = 0;
        for (AllTypes allTypes : realm.where(AllTypes.class).findAll()) {
            assertEquals("Item " + i, new Date(testDatesValid[i]), allTypes.getColumnDate());
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
    public void setter_link_unmanagedObject() {
        CyclicType unmanaged = new CyclicType();

        realm.beginTransaction();
        try {
            CyclicType target = realm.createObject(CyclicType.class);

            try {
                target.setObject(unmanaged);
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
            removed.deleteFromRealm();

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
    public void setter_list_withUnmanagedObject() {
        CyclicType unmanaged = new CyclicType();

        realm.beginTransaction();
        try {
            CyclicType target = realm.createObject(CyclicType.class);

            RealmList<CyclicType> list = new RealmList<>();
            list.add(realm.createObject(CyclicType.class));
            list.add(unmanaged); // List contains an unmanaged object
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
            removed.deleteFromRealm();

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
        io.realm.entities.Thread thread = realm.createObject(io.realm.entities.Thread.class);
        realm.commitTransaction();
    }

    @Test
    public void isValid_unmanagedObject() {
        AllTypes allTypes = new AllTypes();
        assertTrue(allTypes.isValid());
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
    public void isValid_deletedObject() {
        realm.beginTransaction();
        AllTypes allTypes = realm.createObject(AllTypes.class);
        assertTrue(allTypes.isValid());
        realm.delete(AllTypes.class);
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
        } finally {
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
        final CountDownLatch bgRealmDone = new CountDownLatch(1);
        realm.beginTransaction();
        final AllTypes obj = realm.createObject(AllTypes.class);
        realm.commitTransaction();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Realm bgRealm = Realm.getInstance(realm.getConfiguration());
                bgRealm.beginTransaction();
                bgRealm.delete(AllTypes.class);
                bgRealm.commitTransaction();
                bgRealm.close();
                bgRealmDone.countDown();
            }
        }).start();
        TestHelper.awaitOrFail(bgRealmDone);
        realm.waitForChange();

        // Object should no longer be available
        assertFalse(obj.isValid());
        try {
            obj.getColumnLong();
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
    public void isValid() {
        realm.beginTransaction();
        Dog dog = realm.createObject(Dog.class);
        dog.setName("Fido");
        realm.commitTransaction();

        assertTrue(dog.isValid());

        realm.beginTransaction();
        dog.deleteFromRealm();
        realm.commitTransaction();

        assertFalse(dog.isValid());
    }

    @Test
    public void isManaged_managedObject() {
        realm.beginTransaction();
        Dog dog = realm.createObject(Dog.class);
        realm.commitTransaction();

        assertTrue(dog.isManaged());
    }

    @Test
    public void isManaged_unmanagedObject() {
        Dog dog = new Dog();
        assertFalse(dog.isManaged());
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

    @Test
    public void conflictingFieldName_readAndUpdate() {
        final ConflictingFieldName unmanaged = new ConflictingFieldName();
        unmanaged.setRealm("realm");
        unmanaged.setRow("row");
        unmanaged.setIsCompleted("isCompleted");
        unmanaged.setListeners("listeners");
        unmanaged.setPendingQuery("pendingQuery");
        unmanaged.setCurrentTableVersion("currentTableVersion");

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(unmanaged);
            }
        });

        // tests those values are persisted
        final ConflictingFieldName managed = realm.where(ConflictingFieldName.class).findFirst();
        assertEquals("realm", managed.getRealm());
        assertEquals("row", managed.getRow());
        assertEquals("isCompleted", managed.getIsCompleted());
        assertEquals("listeners", managed.getListeners());
        assertEquals("pendingQuery", managed.getPendingQuery());
        assertEquals("currentTableVersion", managed.getCurrentTableVersion());

        // tests those values can be updated
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                managed.setRealm("realm_updated");
                managed.setRow("row_updated");
                managed.setIsCompleted("isCompleted_updated");
                managed.setListeners("listeners_updated");
                managed.setPendingQuery("pendingQuery_updated");
                managed.setCurrentTableVersion("currentTableVersion_updated");
            }
        });

        assertEquals("realm_updated", managed.getRealm());
        assertEquals("row_updated", managed.getRow());
        assertEquals("isCompleted_updated", managed.getIsCompleted());
        assertEquals("listeners_updated", managed.getListeners());
        assertEquals("pendingQuery_updated", managed.getPendingQuery());
        assertEquals("currentTableVersion_updated", managed.getCurrentTableVersion());
    }

    // Setting a not-nullable field to null is an error
    // TODO Move this to RealmObjectTests?
    @Test
    public void setter_nullValueInRequiredField() {
        TestHelper.populateTestRealmForNullTests(realm);
        RealmResults<NullTypes> list = realm.where(NullTypes.class).findAll();

        // 1 String
        try {
            realm.beginTransaction();
            list.first().setFieldStringNotNull(null);
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }

        // 2 Bytes
        try {
            realm.beginTransaction();
            list.first().setFieldBytesNotNull(null);
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }

        // 3 Boolean
        try {
            realm.beginTransaction();
            list.first().setFieldBooleanNotNull(null);
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }

        // 4 Byte
        try {
            realm.beginTransaction();
            list.first().setFieldBytesNotNull(null);
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }

        // 5 Short 6 Integer 7 Long are skipped for this case, same with Bytes

        // 8 Float
        try {
            realm.beginTransaction();
            list.first().setFieldFloatNotNull(null);
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }

        // 9 Double
        try {
            realm.beginTransaction();
            list.first().setFieldDoubleNotNull(null);
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }

        // 10 Date
        try {
            realm.beginTransaction();
            list.first().setFieldDateNotNull(null);
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }
    }

    // Setting a nullable field to null is not an error
    // TODO Move this to RealmObjectsTest?
    @Test
    public void setter_nullValueInNullableField() {
        TestHelper.populateTestRealmForNullTests(realm);
        RealmResults<NullTypes> list = realm.where(NullTypes.class).findAll();

        // 1 String
        realm.beginTransaction();
        list.first().setFieldStringNull(null);
        realm.commitTransaction();
        assertNull(realm.where(NullTypes.class).findFirst().getFieldStringNull());

        // 2 Bytes
        realm.beginTransaction();
        list.first().setFieldBytesNull(null);
        realm.commitTransaction();
        assertNull(realm.where(NullTypes.class).findFirst().getFieldBytesNull());

        // 3 Boolean
        realm.beginTransaction();
        list.first().setFieldBooleanNull(null);
        realm.commitTransaction();
        assertNull(realm.where(NullTypes.class).findFirst().getFieldBooleanNull());

        // 4 Byte
        // 5 Short 6 Integer 7 Long are skipped
        realm.beginTransaction();
        list.first().setFieldByteNull(null);
        realm.commitTransaction();
        assertNull(realm.where(NullTypes.class).findFirst().getFieldByteNull());

        // 8 Float
        realm.beginTransaction();
        list.first().setFieldFloatNull(null);
        realm.commitTransaction();
        assertNull(realm.where(NullTypes.class).findFirst().getFieldFloatNull());

        // 9 Double
        realm.beginTransaction();
        list.first().setFieldDoubleNull(null);
        realm.commitTransaction();
        assertNull(realm.where(NullTypes.class).findFirst().getFieldDoubleNull());

        // 10 Date
        realm.beginTransaction();
        list.first().setFieldDateNull(null);
        realm.commitTransaction();
        assertNull(realm.where(NullTypes.class).findFirst().getFieldDateNull());
    }

    @Test
    @RunTestInLooperThread
    public void addChangeListener_throwOnAddingNullListenerFromLooperThread() {
        final Realm realm = looperThread.realm;
        Dog dog = createManagedDogObjectFromRealmInstance(realm);

        try {
            dog.addChangeListener(null);
            fail("adding null change listener must throw an exception.");
        } catch (IllegalArgumentException ignore) {
        } finally {
            looperThread.testComplete();
        }
    }

    @Test
    public void addChangeListener_throwOnAddingNullListenerFromNonLooperThread() throws Throwable {
        TestHelper.executeOnNonLooperThread(new TestHelper.Task() {
            @Override
            public void run() throws Exception {
                final Realm realm = Realm.getInstance(realmConfig);
                final Dog dog = createManagedDogObjectFromRealmInstance(realm);

                //noinspection TryFinallyCanBeTryWithResources
                try {
                    dog.addChangeListener(null);
                    fail("adding null change listener must throw an exception.");
                } catch (IllegalArgumentException ignore) {
                } finally {
                    realm.close();
                }
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void addChangeListener_throwOnUnmanagedObject() {
        Dog dog = new Dog();

        try {
            dog.addChangeListener(new RealmChangeListener<Dog>() {
                @Override
                public void onChange(Dog object) {
                }
            });
            fail("adding change listener on unmanaged object must throw an exception.");
        } catch (IllegalArgumentException ignore) {
        } finally {
            looperThread.testComplete();
        }
    }

    @Test
    @RunTestInLooperThread
    public void removeChangeListener_throwOnRemovingNullListenerFromLooperThread() {
        final Realm realm = looperThread.realm;
        Dog dog = createManagedDogObjectFromRealmInstance(realm);

        try {
            dog.removeChangeListener(null);
            fail("removing null change listener must throw an exception.");
        } catch (IllegalArgumentException ignore) {
        } finally {
            looperThread.testComplete();
        }
    }

    @Test
    public void removeChangeListener_throwOnRemovingNullListenerFromNonLooperThread() throws Throwable {
        TestHelper.executeOnNonLooperThread(new TestHelper.Task() {
            @Override
            public void run() throws Exception {
                final Realm realm = Realm.getInstance(realmConfig);
                final Dog dog = createManagedDogObjectFromRealmInstance(realm);

                //noinspection TryFinallyCanBeTryWithResources
                try {
                    dog.removeChangeListener(null);
                    fail("removing null change listener must throw an exception.");
                } catch (IllegalArgumentException ignore) {
                } finally {
                    realm.close();
                }
            }
        });
    }

    /**
     * This test is to see if RealmObject.removeChangeListeners() works as it is intended.
     */
    @Test
    @RunTestInLooperThread
    public void removeChangeListeners() {
        final Realm realm = looperThread.realm;
        realm.beginTransaction();
        Dog dog = realm.createObject(Dog.class);
        dog.setAge(13);
        realm.commitTransaction();
        dog.addChangeListener(new RealmChangeListener<Dog>() {
            @Override
            public void onChange(Dog object) {
                assertTrue(false);
            }
        });
        dog.removeChangeListeners();

        realm.beginTransaction();
        Dog sameDog = realm.where(Dog.class).equalTo(Dog.FIELD_AGE, 13).findFirst();
        sameDog.setName("Jesper");
        realm.commitTransaction();
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void removeChangeListener_throwOnUnmanagedObject() {
        Dog dog = new Dog();
        RealmChangeListener listener = new RealmChangeListener<Dog>() {
            @Override
            public void onChange(Dog object) {
            }
        };

        try {
            dog.removeChangeListener(listener);
            fail("Failed to remove a listener from null Realm.");
        } catch (IllegalArgumentException ignore) {
            looperThread.testComplete();
        }
    }

    @Test
    @RunTestInLooperThread
    public void removeChangeListeners_throwOnUnmanagedObject() {
        Dog dog = new Dog();

        try {
            dog.removeChangeListeners();
            fail("Failed to remove null listener.");
        } catch (IllegalArgumentException ignore) {
            looperThread.testComplete();
        }
    }

    // Bug https://github.com/realm/realm-java/issues/2569
    @Test
    @RunTestInLooperThread
    public void addChangeListener_returnedObjectOfCopyToRealmOrUpdate() {
        Realm realm = looperThread.realm;
        realm.beginTransaction();
        realm.createObject(AllTypesPrimaryKey.class, 1);

        AllTypesPrimaryKey allTypesPrimaryKey = new AllTypesPrimaryKey();
        allTypesPrimaryKey.setColumnLong(1);
        allTypesPrimaryKey.setColumnFloat(42f);
        allTypesPrimaryKey = realm.copyToRealmOrUpdate(allTypesPrimaryKey);
        realm.commitTransaction();

        looperThread.keepStrongReference.add(allTypesPrimaryKey);
        allTypesPrimaryKey.addChangeListener(new RealmChangeListener<AllTypesPrimaryKey>() {
            @Override
            public void onChange(AllTypesPrimaryKey element) {
                assertEquals(42.0f, element.getColumnFloat(), 0f);
                looperThread.testComplete();
            }
        });
    }

    // The object should be added to HandlerController.realmObjects only when the first time addListener called.
    @Test
    @UiThreadTest
    public void addChangeListener_shouldAddTheObjectToHandlerRealmObjects() {
        realm.beginTransaction();
        AllTypesPrimaryKey allTypesPrimaryKey = realm.createObject(AllTypesPrimaryKey.class, 1);
        realm.commitTransaction();
        final ConcurrentHashMap<WeakReference<RealmObjectProxy>, Object> realmObjects =
                realm.handlerController.realmObjects;

        assertTrue(realmObjects.isEmpty());

        allTypesPrimaryKey.addChangeListener(new RealmChangeListener<AllTypesPrimaryKey>() {
            @Override
            public void onChange(AllTypesPrimaryKey element) {
            }
        });

        assertEquals(1, realmObjects.size());
        for (WeakReference<RealmObjectProxy> ref : realmObjects.keySet()) {
            assertTrue(ref.get() == allTypesPrimaryKey);
        }
    }

    // The object should be added to HandlerController.realmObjects only once.
    @Test
    @UiThreadTest
    public void addChangeListener_shouldNotAddDupEntriesToHandlerRealmObjects() {
        realm.beginTransaction();
        AllTypesPrimaryKey allTypesPrimaryKey = realm.createObject(AllTypesPrimaryKey.class, 1);
        realm.commitTransaction();
        final ConcurrentHashMap<WeakReference<RealmObjectProxy>, Object> realmObjects =
                realm.handlerController.realmObjects;

        for (WeakReference<RealmObjectProxy> ref : realmObjects.keySet()) {
            assertFalse(ref.get() == allTypesPrimaryKey);
        }

        // Add different listeners twice
        allTypesPrimaryKey.addChangeListener(new RealmChangeListener<AllTypesPrimaryKey>() {
            @Override
            public void onChange(AllTypesPrimaryKey element) {
            }
        });
        allTypesPrimaryKey.addChangeListener(new RealmChangeListener<AllTypesPrimaryKey>() {
            @Override
            public void onChange(AllTypesPrimaryKey element) {
            }
        });

        assertEquals(1, realmObjects.size());
        for (WeakReference<RealmObjectProxy> ref : realmObjects.keySet()) {
            assertTrue(ref.get() == allTypesPrimaryKey);
        }
    }

    // The object should not be added to HandlerController again after the async query loaded.
    @Test
    @RunTestInLooperThread
    public void addChangeListener_checkHandlerRealmObjectsWhenCallingOnAsyncObject() {
        Realm realm = looperThread.realm;
        realm.beginTransaction();
        realm.createObject(AllTypesPrimaryKey.class, 1);
        realm.commitTransaction();
        final ConcurrentHashMap<WeakReference<RealmObjectProxy>, Object> realmObjects =
                realm.handlerController.realmObjects;

        final AllTypesPrimaryKey allTypesPrimaryKey = realm.where(AllTypesPrimaryKey.class).findFirstAsync();
        looperThread.keepStrongReference.add(allTypesPrimaryKey);
        allTypesPrimaryKey.addChangeListener(new RealmChangeListener<AllTypesPrimaryKey>() {
            @Override
            public void onChange(AllTypesPrimaryKey element) {
                allTypesPrimaryKey.addChangeListener(new RealmChangeListener<AllTypesPrimaryKey>() {
                    @Override
                    public void onChange(AllTypesPrimaryKey element) {

                    }
                });
                assertEquals(1, realmObjects.size());
                looperThread.testComplete();
            }
        });
        assertEquals(1, realmObjects.size());
        for (Object query : realmObjects.values()) {
            assertNotNull(query);
        }
    }
}
