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

import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.AllTypes;
import io.realm.entities.AllTypesPrimaryKey;
import io.realm.entities.ConflictingFieldName;
import io.realm.entities.CustomMethods;
import io.realm.entities.CyclicType;
import io.realm.entities.Dog;
import io.realm.entities.NonLatinFieldNames;
import io.realm.entities.NullTypes;
import io.realm.entities.StringAndInt;
import io.realm.entities.pojo.AllTypesRealmModel;
import io.realm.exceptions.RealmException;
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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
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
        assertEquals(17, row.getColumnCount());
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

    // Invalid surrogate pairs:
    // Both high and low should lead to an IllegalArgumentException.
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

    // Removes original object and sees if has been removed.
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

        // Deleting rex twice should fail.
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

    // Queries for an object, removes it and sees it has been removed from realm.
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
        // Creates test data.
        realm.beginTransaction();
        realm.delete(Dog.class);
        for (int i = 0; i < TEST_SIZE; i++) {
            realm.createObject(Dog.class);
        }
        realm.commitTransaction();

        // Checks initial size.
        RealmResults<Dog> dogs = realm.where(Dog.class).findAll();
        OrderedRealmCollectionSnapshot<Dog> snapshot = dogs.createSnapshot();
        assertEquals(TEST_SIZE, snapshot.size());

        // Checks that calling deleteFromRealm doesn't remove the object from the RealmResult.
        realm.beginTransaction();
        for (int i = 0; i < TEST_SIZE; i++) {
            snapshot.get(removeFromFront ? i : TEST_SIZE - 1 - i).deleteFromRealm();
        }
        realm.commitTransaction();

        assertEquals(TEST_SIZE, snapshot.size());
        assertEquals(0, dogs.size());
    }

    // Tests calling deleteFromRealm on a OrderedRealmCollectionSnapshot instead of RealmResults.remove().
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
        assertTrue(cm3.equals(cm2));
        assertTrue(cm2.equals(cm3));
    }

    @Test
    public void toString_cyclicObject() {
        realm.beginTransaction();
        CyclicType foo = createCyclicData();
        realm.commitTransaction();
        assertEquals(
                "CyclicType = proxy[{id:0},{name:Foo},{date:null},{object:CyclicType},{otherObject:null},{objects:RealmList<CyclicType>[0]}]",
                foo.toString());
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

        // Checks that the hash code is always the same between multiple calls.
        assertEquals(foo.hashCode(), foo.hashCode());
        // Checks that the hash code is the same among same object.
        assertEquals(foo.hashCode(), realm.where(CyclicType.class).equalTo("name", foo.getName()).findFirst().hashCode());
        // Hash code is different from other objects.
        assertNotEquals(foo.getObject().hashCode(), foo.hashCode());

        final int originalHashCode = foo.hashCode();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                foo.setName(foo.getName() + "1234");
            }
        });
        // Checks that Updating the value of its field does not affect the hash code.
        assertEquals(originalHashCode, foo.hashCode());

        // Checks the hash code of the object from a Realm in different file name.
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

        // Checks the hash code of the object from a Realm in different directory.
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

        // Setups cycle on normal object references.
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

        final AtomicReference<CyclicType> objFromAnotherThread = new AtomicReference<CyclicType>();

        java.lang.Thread thread = new java.lang.Thread() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(realmConfig);

                // 1. Creates an object.
                realm.beginTransaction();
                objFromAnotherThread.set(realm.createObject(CyclicType.class));
                realm.commitTransaction();

                createLatch.countDown();
                TestHelper.awaitOrFail(testEndLatch);

                // 3. Closes Realm in this thread and finishes.
                realm.close();
            }
        };
        thread.start();

        TestHelper.awaitOrFail(createLatch);
        // 2. Sets created object to target.
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

        // Waits for finishing the thread.
        thread.join();
    }

    @Test
    public void setter_list_withUnmanagedObject() {
        CyclicType unmanaged = new CyclicType();

        realm.beginTransaction();
        try {
            CyclicType target = realm.createObject(CyclicType.class);

            RealmList<CyclicType> list = new RealmList<CyclicType>();
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

            RealmList<CyclicType> list = new RealmList<CyclicType>();
            list.add(realm.createObject(CyclicType.class));
            list.add(removed); // List contains a deleted object.
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

            RealmList<CyclicType> list = new RealmList<CyclicType>();
            list.add(realm.createObject(CyclicType.class));
            list.add(closed); // List contains a closed object.
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

                RealmList<CyclicType> list = new RealmList<CyclicType>();
                list.add(realm.createObject(CyclicType.class));
                list.add(objFromAnotherRealm); // List contains an object from another Realm.
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

        final AtomicReference<CyclicType> objFromAnotherThread = new AtomicReference<CyclicType>();

        java.lang.Thread thread = new java.lang.Thread() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(realmConfig);

                // 1. Creates an object.
                realm.beginTransaction();
                objFromAnotherThread.set(realm.createObject(CyclicType.class));
                realm.commitTransaction();

                createLatch.countDown();
                TestHelper.awaitOrFail(testEndLatch);

                // 3. Close Realm in this thread and finishes.
                realm.close();
            }
        };
        thread.start();

        TestHelper.awaitOrFail(createLatch);
        // 2. Sets created object to target.
        realm.beginTransaction();
        try {
            CyclicType target = realm.createObject(CyclicType.class);

            RealmList<CyclicType> list = new RealmList<CyclicType>();
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

        // Waits for finishing the thread.
        thread.join();
    }

    @Test
    public void setter_list_ownList() {
        // Create initial list
        realm.beginTransaction();
        RealmList<AllJavaTypes> allTypesRealmModels = new RealmList<>();
        for (int i = 0; i < 2; i++) {
            allTypesRealmModels.add(new AllJavaTypes(i));
        }
        AllJavaTypes model = new AllJavaTypes(2);
        model.setFieldList(allTypesRealmModels);
        model = realm.copyToRealm(model);
        realm.commitTransaction();
        assertEquals(2, model.getFieldList().size());

        // Check that setting own list does not clear it by accident.
        realm.beginTransaction();
        model.setFieldList(model.getFieldList());
        realm.commitTransaction();
        assertEquals(2, model.getFieldList().size());

        // Check that a unmanaged list throws the correct exception
        realm.beginTransaction();
        RealmList<AllJavaTypes> unmanagedList = new RealmList<>();
        unmanagedList.addAll(realm.copyFromRealm(model.getFieldList()));
        try {
            model.setFieldList(unmanagedList);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
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

    @Test
    public void isValid_null() {
        //noinspection ConstantConditions
        assertFalse(RealmObject.isValid(null));
    }

    // Stores and retrieves null values for nullable fields.
    @Test
    public void set_get_nullOnNullableFields() {
        realm.beginTransaction();
        NullTypes nullTypes = realm.createObject(NullTypes.class, 0);
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

    // Stores and retrieves non-null values when field can contain null strings.
    @Test
    public void get_set_nonNullValueOnNullableFields() {
        final String testString = "FooBar";
        final byte[] testBytes = new byte[] {42};
        final Date testDate = newDate(2000, 1, 1);
        realm.beginTransaction();
        NullTypes nullTypes = realm.createObject(NullTypes.class, 0);
        // 1 String
        nullTypes.setFieldStringNull(testString);
        // 2 Bytes
        nullTypes.setFieldBytesNull(testBytes);
        // 3 Boolean
        nullTypes.setFieldBooleanNull(true);
        // 4 Byte
        nullTypes.setFieldByteNull((byte) 42);
        // 5 Short
        nullTypes.setFieldShortNull((short) 42);
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
        assertEquals((byte) 42, (byte) nullTypes.getFieldByteNull().intValue());
        // 5 Short
        assertEquals((short) 42, (short) nullTypes.getFieldShortNull().intValue());
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

    // Tries to store null values in non-nullable fields.
    @Test
    public void set_nullValuesToNonNullableFields() {
        try {
            realm.beginTransaction();
            NullTypes nullTypes = realm.createObject(NullTypes.class, 0);
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
        NullTypes nullTypes = realm.createObject(NullTypes.class, 0);
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

        // Object should no longer be available.
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

    // Tests NaN value on float and double columns.
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

    // Tests max value on float and double columns.
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

    // Tests min normal value on float and double columns.
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

    // Tests min value on float and double columns.
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

    // Tests negative infinity value on float and double columns.
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

    // Tests positive infinity value on float and double columns.
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
                        final Table table = realm.getSchema().getTable(StringAndInt.class);
                        final long strIndex = table.getColumnIndex("str");
                        final long numberIndex = table.getColumnIndex("number");

                        while (0 < table.getColumnCount()) {
                            table.removeColumn(0);
                        }

                        final long newStrIndex;
                        // Swaps column indices.
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
                        // Does nothing.
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

        // Opens swapped Realm in order to load column index.
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
        unmanaged.setRealmString("realm");
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

        // Tests those values are persisted.
        final ConflictingFieldName managed = realm.where(ConflictingFieldName.class).findFirst();
        assertEquals("realm", managed.getRealmString());
        assertEquals("row", managed.getRow());
        assertEquals("isCompleted", managed.getIsCompleted());
        assertEquals("listeners", managed.getListeners());
        assertEquals("pendingQuery", managed.getPendingQuery());
        assertEquals("currentTableVersion", managed.getCurrentTableVersion());

        // Tests those values can be updated.
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                managed.setRealmString("realm_updated");
                managed.setRow("row_updated");
                managed.setIsCompleted("isCompleted_updated");
                managed.setListeners("listeners_updated");
                managed.setPendingQuery("pendingQuery_updated");
                managed.setCurrentTableVersion("currentTableVersion_updated");
            }
        });

        assertEquals("realm_updated", managed.getRealmString());
        assertEquals("row_updated", managed.getRow());
        assertEquals("isCompleted_updated", managed.getIsCompleted());
        assertEquals("listeners_updated", managed.getListeners());
        assertEquals("pendingQuery_updated", managed.getPendingQuery());
        assertEquals("currentTableVersion_updated", managed.getCurrentTableVersion());
    }

    // Setting a not-nullable field to null is an error.
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
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_STRING_NOT_NULL));
        } finally {
            realm.cancelTransaction();
        }

        // 2 Bytes
        try {
            realm.beginTransaction();
            list.first().setFieldBytesNotNull(null);
            fail();
        } catch (IllegalArgumentException ignored) {
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_BYTES_NOT_NULL));
        } finally {
            realm.cancelTransaction();
        }

        // 3 Boolean
        try {
            realm.beginTransaction();
            list.first().setFieldBooleanNotNull(null);
            fail();
        } catch (IllegalArgumentException ignored) {
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_BOOLEAN_NOT_NULL));
        } finally {
            realm.cancelTransaction();
        }

        // 4 Byte
        try {
            realm.beginTransaction();
            list.first().setFieldByteNotNull(null);
            fail();
        } catch (IllegalArgumentException ignored) {
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_BYTE_NOT_NULL));
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
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_FLOAT_NOT_NULL));
        } finally {
            realm.cancelTransaction();
        }

        // 9 Double
        try {
            realm.beginTransaction();
            list.first().setFieldDoubleNotNull(null);
            fail();
        } catch (IllegalArgumentException ignored) {
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_DOUBLE_NOT_NULL));
        } finally {
            realm.cancelTransaction();
        }

        // 10 Date
        try {
            realm.beginTransaction();
            list.first().setFieldDateNotNull(null);
            fail();
        } catch (IllegalArgumentException ignored) {
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_DATE_NOT_NULL));
        } finally {
            realm.cancelTransaction();
        }
    }

    // Setting a nullable field to null is not an error.
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
    public void setter_changePrimaryKeyThrows() {
        realm.beginTransaction();
        AllJavaTypes allJavaTypes = realm.createObject(AllJavaTypes.class, 42);
        thrown.expect(RealmException.class);
        allJavaTypes.setFieldId(111);
        realm.cancelTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void addChangeListener_throwOnAddingNullListenerFromLooperThread() {
        final Realm realm = looperThread.getRealm();
        Dog dog = createManagedDogObjectFromRealmInstance(realm);

        try {
            dog.addChangeListener((RealmChangeListener) null);
            fail("adding null change listener must throw an exception.");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            dog.addChangeListener((RealmObjectChangeListener) null);
            fail("adding null change listener must throw an exception.");
        } catch (IllegalArgumentException ignore) {
        }

        looperThread.testComplete();
    }

    @Test
    public void addChangeListener_throwOnAddingNullListenerFromNonLooperThread() throws Throwable {
        final Dog dog = createManagedDogObjectFromRealmInstance(realm);

        try {
            dog.addChangeListener((RealmChangeListener) null);
            fail("adding null change listener must throw an exception.");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            dog.addChangeListener((RealmObjectChangeListener) null);
            fail("adding null change listener must throw an exception.");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    @RunTestInLooperThread
    public void changeListener_triggeredWhenObjectIsDeleted() {
        final Realm realm = looperThread.getRealm();
        realm.beginTransaction();
        AllTypes obj = realm.createObject(AllTypes.class);
        realm.commitTransaction();

        obj.addChangeListener(new RealmChangeListener<AllTypes>() {
            @Override
            public void onChange(AllTypes obj) {
                assertFalse(obj.isValid());
                looperThread.testComplete();
            }
        });

        realm.beginTransaction();
        obj.deleteFromRealm();
        realm.commitTransaction();
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
        }

        try {
            dog.addChangeListener(new RealmObjectChangeListener<Dog>() {
                @Override
                public void onChange(Dog object, ObjectChangeSet changeSet) {
                }
            });
            fail("adding change listener on unmanaged object must throw an exception.");
        } catch (IllegalArgumentException ignore) {
        }

        looperThread.testComplete();
    }

    // Object Store will throw when adding change listener inside a transaction.
    @Test
    @RunTestInLooperThread
    public void addChangeListener_throwInsiderTransaction() {
        Realm realm = looperThread.getRealm();

        realm.beginTransaction();
        Dog dog = realm.createObject(Dog.class);
        try {
            dog.addChangeListener(new RealmChangeListener<Dog>() {
                @Override
                public void onChange(Dog element) {
                    fail();
                }
            });
        } catch (IllegalStateException ignored) {
        }

        try {
            dog.addChangeListener(new RealmObjectChangeListener<Dog>() {
                @Override
                public void onChange(Dog object, ObjectChangeSet changeSet) {
                    fail();
                }
            });
        } catch (IllegalStateException ignored) {
        }
        realm.cancelTransaction();

        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void removeChangeListener_throwOnRemovingNullListenerFromLooperThread() {
        final Realm realm = looperThread.getRealm();
        Dog dog = createManagedDogObjectFromRealmInstance(realm);

        try {
            dog.removeChangeListener((RealmChangeListener) null);
            fail("removing null change listener must throw an exception.");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            dog.removeChangeListener((RealmObjectChangeListener) null);
            fail("removing null change listener must throw an exception.");
        } catch (IllegalArgumentException ignore) {
        }

        looperThread.testComplete();
    }

    @Test
    public void removeChangeListener_throwOnRemovingNullListenerFromNonLooperThread() throws Throwable {
        final Dog dog = createManagedDogObjectFromRealmInstance(realm);

        try {
            dog.removeChangeListener((RealmChangeListener) null);
            fail("removing null change listener must throw an exception.");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            dog.removeChangeListener((RealmObjectChangeListener) null);
            fail("removing null change listener must throw an exception.");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    @RunTestInLooperThread
    public void removeChangeListener_insideTransaction() {
        Realm realm = looperThread.getRealm();
        final Dog dog = createManagedDogObjectFromRealmInstance(realm);
        RealmChangeListener<Dog> realmChangeListener = new RealmChangeListener<Dog>() {
            @Override
            public void onChange(Dog element) {
            }
        };
        RealmObjectChangeListener<Dog> realmObjectChangeListener = new RealmObjectChangeListener<Dog>() {
            @Override
            public void onChange(Dog object, ObjectChangeSet changeSet) {
            }
        };

        dog.addChangeListener(realmChangeListener);
        dog.addChangeListener(realmObjectChangeListener);

        realm.beginTransaction();
        dog.removeChangeListener(realmChangeListener);
        dog.removeChangeListener(realmObjectChangeListener);
        realm.cancelTransaction();
        looperThread.testComplete();
    }

    /**
     * This test is to see if RealmObject.removeChangeListeners() works as it is intended.
     */
    @Test
    @RunTestInLooperThread
    public void removeAllChangeListeners() {
        final Realm realm = looperThread.getRealm();
        realm.beginTransaction();
        Dog dog = realm.createObject(Dog.class);
        dog.setAge(13);
        realm.commitTransaction();
        dog.addChangeListener(new RealmChangeListener<Dog>() {
            @Override
            public void onChange(Dog object) {
                fail();
            }
        });
        dog.addChangeListener(new RealmObjectChangeListener<Dog>() {
            @Override
            public void onChange(Dog object, ObjectChangeSet changeSet) {
                fail();
            }
        });
        dog.removeAllChangeListeners();

        realm.beginTransaction();
        Dog sameDog = realm.where(Dog.class).equalTo(Dog.FIELD_AGE, 13).findFirst();
        sameDog.setName("Jesper");
        realm.commitTransaction();
        // Try to trigger the listeners.
        realm.sharedRealm.refresh();
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void removeAllChangeListeners_thenAdd() {
        final Realm realm = looperThread.getRealm();
        realm.beginTransaction();
        Dog dog = realm.createObject(Dog.class);
        dog.setAge(13);
        realm.commitTransaction();
        dog.addChangeListener(new RealmChangeListener<Dog>() {
            @Override
            public void onChange(Dog object) {
                fail();
            }
        });
        dog.removeAllChangeListeners();

        dog.addChangeListener(new RealmChangeListener<Dog>() {
            @Override
            public void onChange(Dog dog) {
                assertEquals(14, dog.getAge());
                looperThread.testComplete();
            }
        });

        realm.beginTransaction();
        dog.setAge(14);
        realm.commitTransaction();
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
        RealmObjectChangeListener objectChangeListener = new RealmObjectChangeListener<Dog>() {
            @Override
            public void onChange(Dog object, ObjectChangeSet changeSet) {
            }
        };

        try {
            dog.removeChangeListener(listener);
            fail("Failed to remove a listener from null Realm.");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            dog.removeChangeListener(objectChangeListener);
            fail("Failed to remove a listener from null Realm.");
        } catch (IllegalArgumentException ignore) {
        }

        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void removeAllChangeListeners_throwOnUnmanagedObject() {
        Dog dog = new Dog();

        try {
            dog.removeAllChangeListeners();
            fail("Failed to remove null listener.");
        } catch (IllegalArgumentException ignore) {
            looperThread.testComplete();
        }
    }

    // Bug https://github.com/realm/realm-java/issues/2569
    @Test
    @RunTestInLooperThread
    public void addChangeListener_returnedObjectOfCopyToRealmOrUpdate() {
        Realm realm = looperThread.getRealm();
        realm.beginTransaction();
        realm.createObject(AllTypesPrimaryKey.class, 1);

        AllTypesPrimaryKey allTypesPrimaryKey = new AllTypesPrimaryKey();
        allTypesPrimaryKey.setColumnLong(1);
        allTypesPrimaryKey.setColumnFloat(0f);
        allTypesPrimaryKey = realm.copyToRealmOrUpdate(allTypesPrimaryKey);
        realm.commitTransaction();

        looperThread.keepStrongReference(allTypesPrimaryKey);
        allTypesPrimaryKey.addChangeListener(new RealmChangeListener<AllTypesPrimaryKey>() {
            @Override
            public void onChange(AllTypesPrimaryKey element) {
                assertEquals(42.0f, element.getColumnFloat(), 0f);
                looperThread.testComplete();
            }
        });

        // Change the object to trigger the listener.
        realm.beginTransaction();
        allTypesPrimaryKey.setColumnFloat(42f);
        realm.commitTransaction();
    }

    // step 1: findFirstAsync
    // step 2: async query returns, change the object in the listener
    // step 3: listener gets called again
    @Test
    @RunTestInLooperThread
    public void addChangeListener_listenerShouldBeCalledIfObjectChangesAfterAsyncReturn() {
        final AtomicInteger listenerCounter = new AtomicInteger(0);
        final Realm realm = looperThread.getRealm();
        realm.beginTransaction();
        realm.createObject(AllTypesPrimaryKey.class, 1);
        realm.commitTransaction();

        // Step 1
        final AllTypesPrimaryKey allTypesPrimaryKey = realm.where(AllTypesPrimaryKey.class).findFirstAsync();
        looperThread.keepStrongReference(allTypesPrimaryKey);
        allTypesPrimaryKey.addChangeListener(new RealmChangeListener<AllTypesPrimaryKey>() {
            @Override
            public void onChange(AllTypesPrimaryKey element) {
                int count = listenerCounter.getAndAdd(1);
                if (count == 0) {
                    // Step 2
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.where(AllTypesPrimaryKey.class).findFirst().setColumnFloat(42f);
                        }
                    });
                } else if (count == 1) {
                    // Step 3
                    assertEquals(allTypesPrimaryKey.getColumnFloat(), 42f, 0);
                    looperThread.testComplete();
                } else {
                    fail();
                }
            }
        });
    }

    @Test
    public void getRealm_managedRealmObject() {
        realm.beginTransaction();
        AllTypes object = realm.createObject(AllTypes.class);
        realm.commitTransaction();

        assertSame(realm, object.getRealm());
        assertSame(realm, RealmObject.getRealm(object));
    }

    @Test
    public void getRealm_managedRealmModel() {
        realm.beginTransaction();
        AllTypesRealmModel object = realm.createObject(AllTypesRealmModel.class, 1L);
        realm.commitTransaction();

        assertSame(realm, RealmObject.getRealm(object));
    }

    @Test
    public void getRealm_DynamicRealmObject() {
        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(realmConfig);
        //noinspection TryFinallyCanBeTryWithResources
        try {
            dynamicRealm.beginTransaction();
            DynamicRealmObject object = dynamicRealm.createObject("AllTypesRealmModel", 1L);
            dynamicRealm.commitTransaction();

            try {
                object.getRealm();
                fail();
            } catch (IllegalStateException expected) {
                assertEquals(RealmObject.MSG_DYNAMIC_OBJECT, expected.getMessage());
            }
            try {
                RealmObject.getRealm(object);
                fail();
            } catch (IllegalStateException expected) {
                assertEquals(RealmObject.MSG_DYNAMIC_OBJECT, expected.getMessage());
            }
        } finally {
            dynamicRealm.close();
        }
    }

    @Test
    public void getRealm_unmanagedRealmObjectReturnsNull() {
        assertNull(new AllTypes().getRealm());
        assertNull(RealmObject.getRealm(new AllTypes()));
    }

    @Test
    public void getRealm_unmanagedRealmModelReturnsNull() {
        assertNull(RealmObject.getRealm(new AllTypesRealmModel()));
    }

    @Test
    public void getRealm_null() {
        try {
            RealmObject.getRealm(null);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals(RealmObject.MSG_NULL_OBJECT, expected.getMessage());
        }
    }

    @Test
    public void getRealm_closedObjectThrows() {
        realm.beginTransaction();
        AllTypes object = realm.createObject(AllTypes.class);
        realm.commitTransaction();

        realm.close();
        realm = null;

        try {
            object.getRealm();
            fail();
        } catch (IllegalStateException e) {
            assertEquals(BaseRealm.CLOSED_REALM_MESSAGE, e.getMessage());
        }
        try {
            RealmObject.getRealm(object);
            fail();
        } catch (IllegalStateException e) {
            assertEquals(BaseRealm.CLOSED_REALM_MESSAGE, e.getMessage());
        }
    }

    @Test
    public void getRealmConfiguration_deletedObjectThrows() {
        realm.beginTransaction();
        AllTypes object = realm.createObject(AllTypes.class);
        object.deleteFromRealm();
        realm.commitTransaction();

        try {
            object.getRealm();
            fail();
        } catch (IllegalStateException e) {
            assertEquals(RealmObject.MSG_DELETED_OBJECT, e.getMessage());
        }
        try {
            RealmObject.getRealm(object);
            fail();
        } catch (IllegalStateException e) {
            assertEquals(RealmObject.MSG_DELETED_OBJECT, e.getMessage());
        }
    }

    @Test
    public void getRealm_illegalThreadThrows() throws Throwable {
        realm.beginTransaction();
        final AllTypes object = realm.createObject(AllTypes.class);
        realm.commitTransaction();

        final CountDownLatch threadFinished = new CountDownLatch(1);
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    object.getRealm();
                    fail();
                } catch (IllegalStateException e) {
                    assertEquals(BaseRealm.INCORRECT_THREAD_MESSAGE, e.getMessage());
                }
                try {
                    RealmObject.getRealm(object);
                    fail();
                } catch (IllegalStateException e) {
                    assertEquals(BaseRealm.INCORRECT_THREAD_MESSAGE, e.getMessage());
                } finally {
                    threadFinished.countDown();
                }
            }
        });
        thread.start();
        TestHelper.awaitOrFail(threadFinished);
    }

    @Test
    public void setter_binary_long_values() {
        byte[] longBinary = new byte[Table.MAX_BINARY_SIZE];
        byte[] tooLongBinary = new byte[Table.MAX_BINARY_SIZE + 1];

        realm.beginTransaction();
        AllTypes allTypes = realm.createObject(AllTypes.class);
        allTypes.setColumnBinary(longBinary);
        realm.commitTransaction();
        assertEquals(longBinary.length, allTypes.getColumnBinary().length);

        realm.beginTransaction();
        try {
            allTypes.setColumnBinary(tooLongBinary);
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), CoreMatchers.containsString("which exceeds the max binary size"));
        }
    }

    @Test
    public void setter_string_long_values() {
        byte[] tooLongBinary = new byte[Table.MAX_STRING_SIZE + 1];
        Arrays.fill(tooLongBinary, (byte) 'a');
        String longString = new String(tooLongBinary, 0, Table.MAX_STRING_SIZE, Charset.forName("US-ASCII"));
        String tooLongString = new String(tooLongBinary, 0, Table.MAX_STRING_SIZE + 1, Charset.forName("US-ASCII"));

        realm.beginTransaction();
        AllTypes allTypes = realm.createObject(AllTypes.class);
        allTypes.setColumnString(longString);
        realm.commitTransaction();
        assertEquals(longString.length(), allTypes.getColumnString().length());

        realm.beginTransaction();
        try {
            allTypes.setColumnString(tooLongString);
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), CoreMatchers.containsString("which exceeds the max string length"));
        }
    }

    @Test
    public void setter_nonLatinFieldName() {
        // Reproduces https://github.com/realm/realm-java/pull/5346
        realm.beginTransaction();
        NonLatinFieldNames obj = realm.createObject(NonLatinFieldNames.class);
        obj.setΔέλτα(42);
        realm.commitTransaction();
    }
}
