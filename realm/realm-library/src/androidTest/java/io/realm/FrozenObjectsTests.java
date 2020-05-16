/*
 * Copyright 2019 Realm Inc.
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
import org.junit.runner.RunWith;

import java.util.Arrays;

import javax.annotation.Nullable;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.AllTypes;
import io.realm.entities.Dog;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Class testing that the frozen objects feature works correctly.
 */
@RunWith(AndroidJUnit4.class)
public class FrozenObjectsTests {

    private static final int DATA_SIZE = 10;

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    private RealmConfiguration realmConfig;
    private Realm realm;
    private Realm frozenRealm;

    @Before
    public void setUp() {
        realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);
        frozenRealm = realm.freeze();
    }

    @After
    public void tearDown() {
        realm.close(); // This also closes the frozen Realm
    }

    @Test
    public void deleteFrozenRealm() {
        RealmConfiguration config = configFactory.createConfigurationBuilder().name("deletable.realm").build();
        Realm realm = Realm.getInstance(config);
        frozenRealm = realm.freeze();
        try {
            Realm.deleteRealm(config);
        } catch (IllegalStateException ignore) {
        }
        realm.close();
        assertTrue(Realm.deleteRealm(config));
    }

    @Test
    public void freezeRealm() {
        assertFalse(realm.isFrozen());
        Realm frozenRealm = realm.freeze();
        assertEquals(realm.getPath(), frozenRealm.getPath());
        assertTrue(frozenRealm.isFrozen());
        frozenRealm.close();
    }

    @Test
    public void freezeDynamicRealm() {
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(realmConfig);
        DynamicRealm frozenDynamicRealm = dynamicRealm.freeze();
        assertEquals(dynamicRealm.getPath(), frozenDynamicRealm.getPath());
        assertTrue(frozenRealm.isFrozen());
        dynamicRealm.close();
        assertFalse(frozenDynamicRealm.isClosed());
        frozenDynamicRealm.close();
    }

    @Test
    public void frozenRealmsCannotStartTransactions() {
        try {
            frozenRealm.beginTransaction();
            fail();
        } catch (IllegalStateException ignore) {
        }
    }

    @Test
    @RunTestInLooperThread
    public void addingRealmChangeListenerThrows() {
        try {
            frozenRealm.addChangeListener(new RealmChangeListener<Realm>() {
                @Override
                public void onChange(Realm realm) {
                }
            });
            fail();
        } catch (IllegalStateException ignore) {
            looperThread.testComplete();
        }
    }

    @Test
    @RunTestInLooperThread
    public void addingResultsChangeListenerThrows() {
        RealmResults<AllTypes> results = frozenRealm.where(AllTypes.class).findAll();
        try {
            results.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<AllTypes>>() {
                @Override
                public void onChange(RealmResults<AllTypes> allTypes, OrderedCollectionChangeSet changeSet) {
                }
            });
            fail();
        } catch (IllegalStateException ignore) {
            looperThread.testComplete();
        }
    }

    @Test
    @RunTestInLooperThread
    public void addingListChangeListenerThrows() {
        realm.executeTransaction(r -> {
            r.createObject(AllTypes.class);
        });

        Realm frozenRealm = realm.freeze();
        AllTypes obj = frozenRealm.where(AllTypes.class).findFirst();
        try {
            obj.getColumnStringList().addChangeListener(new OrderedRealmCollectionChangeListener<RealmList<String>>() {
                @Override
                public void onChange(RealmList<String> strings, OrderedCollectionChangeSet changeSet) {
                }
            });
            fail();
        } catch (IllegalStateException ignore) {
        }

        try {
            obj.getColumnRealmList().addChangeListener(new OrderedRealmCollectionChangeListener<RealmList<Dog>>() {
                @Override
                public void onChange(RealmList<Dog> dogs, OrderedCollectionChangeSet changeSet) {
                }
            });
            fail();
        } catch (IllegalStateException ignore) {
        }
        frozenRealm.close();
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void addingObjectChangeListenerThrows() {
        realm.executeTransaction(r -> {
            r.createObject(AllTypes.class);
        });
        Realm frozenRealm = realm.freeze();
        AllTypes obj = frozenRealm.where(AllTypes.class).findFirst();
        try {
            obj.addChangeListener(new RealmObjectChangeListener<RealmModel>() {
                @Override
                public void onChange(RealmModel realmModel, @Nullable ObjectChangeSet changeSet) {

                }
            });
            fail();
        } catch (IllegalStateException ignore) {
            frozenRealm.close();
            looperThread.testComplete();
        }
    }

    @Test
    public void removingChangeListeners() {
        frozenRealm.removeAllChangeListeners();
        frozenRealm.removeChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm realm) {
            }
        });
    }

    @Test
    public void refreshThrows() {
        try {
            frozenRealm.refresh();
            fail();
        } catch (IllegalStateException ignore) {
        }
    }

    @Test
    public void writeToFrozenObjectThrows() {
        realm.beginTransaction();
        try {
            frozenRealm.createObject(AllTypes.class);
            fail();
        } catch (IllegalStateException ignore) {
        } finally {
            realm.cancelTransaction();
        }

        realm.beginTransaction();
        try {
            frozenRealm.insert(new AllTypes());
            fail();
        } catch (IllegalStateException ignore) {
        } finally {
            realm.cancelTransaction();
        }

        realm.beginTransaction();
        try {
            frozenRealm.copyToRealm(new AllTypes());
            fail();
        } catch (IllegalStateException ignore) {
        } finally {
            realm.cancelTransaction();
        }

        realm.executeTransaction(r -> {
            r.createObject(AllTypes.class);
        });
        Realm frozenRealm = realm.freeze();
        AllTypes obj = frozenRealm.where(AllTypes.class).findFirst();
        realm.beginTransaction();
        try {
            obj.setColumnString("Foo");
            fail();
        } catch (IllegalStateException ignore) {
        } finally {
            realm.cancelTransaction();
        }

        try {
            frozenRealm.executeTransactionAsync(r -> {  /* Do nothing */ });
        } catch (IllegalStateException ignore) {
        } finally {
            frozenRealm.close();
        }
    }

    @Test
    public void freezingPinsRealmVersion() {
        assertTrue(frozenRealm.isEmpty());
        assertTrue(realm.isEmpty());

        realm.executeTransaction(r -> {
            r.createObject(AllTypes.class);
        });

        assertTrue(frozenRealm.isEmpty());
        assertFalse(realm.isEmpty());
    }

    @Test
    public void readFrozenRealmAcrossThreads() throws InterruptedException {
        Thread t = new Thread(() -> {
            assertTrue(frozenRealm.isEmpty());
            assertTrue(frozenRealm.isFrozen());
        });
        t.start();
        t.join();
    }

    @Test
    public void queryFrozenRealmAcrossThreads() throws InterruptedException {
        final Realm frozenRealm = createDataForFrozenRealm(DATA_SIZE);
        Thread t = new Thread(() -> {
            RealmResults<AllTypes> results = frozenRealm.where(AllTypes.class).findAll();
            assertEquals(DATA_SIZE, results.size());
        });
        t.start();
        t.join();
    }

    @Test
    public void canReadFrozenResultsAcrossThreads() throws InterruptedException {
        Realm frozenRealm = createDataForFrozenRealm(DATA_SIZE);
        RealmResults<AllTypes> results = frozenRealm.where(AllTypes.class).findAll();
        Thread t = new Thread(() -> {
            assertEquals(DATA_SIZE, results.size());
            assertTrue(results.isFrozen());
        });
        t.start();
        t.join();
    }

    @Test
    public void canReadFrozenListsAcrossThreads() throws InterruptedException {
        Realm frozenRealm = createDataForFrozenRealm(DATA_SIZE);
        RealmList<Dog> list = frozenRealm.where(AllTypes.class).findFirst().getColumnRealmList();
        Thread t = new Thread(() -> {
            assertEquals(5, list.size());
            assertTrue(list.isFrozen());
        });
        t.start();
        t.join();
    }

    @Test
    public void canReadFrozenObjectsAcrossThreads() throws InterruptedException {
        Realm frozenRealm = createDataForFrozenRealm(DATA_SIZE);
        AllTypes obj = frozenRealm.where(AllTypes.class).sort(AllTypes.FIELD_LONG).findFirst();
        Thread t = new Thread(() -> {
            assertEquals(0, obj.getColumnLong());
            assertEquals("String 0", obj.getColumnString());
            assertTrue(obj.isFrozen());
        });
        t.start();
        t.join();
    }

    @Test
    public void frozenObjectsReturnsFrozenRealms() {
        Realm frozenRealm = createDataForFrozenRealm(DATA_SIZE);
        RealmResults<AllTypes> results = frozenRealm.where(AllTypes.class).findAll();
        AllTypes obj = results.first();
        RealmList<Dog> list = obj.getColumnRealmList();

        assertTrue(results.getRealm().isFrozen());
        assertTrue(obj.getRealm().isFrozen());
        assertTrue(list.getRealm().isFrozen());
    }

    @Test
    public void freezeResults() throws InterruptedException {
        Realm realm = createDataForLiveRealm(DATA_SIZE);
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> frozenResults = results.freeze();
        assertEquals(DATA_SIZE, frozenResults.size());
        assertTrue(frozenResults.isFrozen());
        assertTrue(frozenResults.isValid());
        assertTrue(frozenResults.isLoaded());

        Thread t = new Thread(() -> {
            assertEquals(DATA_SIZE, frozenResults.size());
            assertTrue(frozenResults.isFrozen());
            assertEquals(1, frozenResults.where().equalTo(AllTypes.FIELD_LONG, 1).findAll().size());
        });
        t.start();
        t.join();
    }

    @Test
    public void freezeDynamicResults() throws InterruptedException {
        Realm realm = createDataForLiveRealm(DATA_SIZE);
        DynamicRealm dynRealm = DynamicRealm.getInstance(realm.getConfiguration());
        RealmResults<DynamicRealmObject> results = dynRealm.where(AllTypes.CLASS_NAME).findAll();
        RealmResults<DynamicRealmObject> frozenResults = results.freeze();
        assertEquals(DATA_SIZE, frozenResults.size());
        assertTrue(frozenResults.isFrozen());
        assertTrue(frozenResults.isValid());
        assertTrue(frozenResults.isLoaded());

        Thread t = new Thread(() -> {
            assertEquals(DATA_SIZE, frozenResults.size());
            assertTrue(frozenResults.isFrozen());
            assertEquals(1, frozenResults.where().equalTo(AllTypes.FIELD_LONG, 1).findAll().size());
        });
        t.start();
        t.join();
        dynRealm.close();
    }

    @Test
    public void freezeSnapshot() {
        Realm realm = createDataForLiveRealm(DATA_SIZE);
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();
        OrderedRealmCollectionSnapshot<AllTypes> snapshot = results.createSnapshot();
        try {
            snapshot.freeze();
            fail();
        } catch(UnsupportedOperationException ignored) {
        }
    }

    @Test
    public void freezeDynamicSnapshot() {
        Realm realm = createDataForLiveRealm(DATA_SIZE);
        DynamicRealm dynRealm = DynamicRealm.getInstance(realm.getConfiguration());
        RealmResults<DynamicRealmObject> results = dynRealm.where(AllTypes.CLASS_NAME).findAll();
        OrderedRealmCollectionSnapshot<DynamicRealmObject> snapshot = results.createSnapshot();
        try {
            snapshot.freeze();
            fail();
        } catch(UnsupportedOperationException ignored) {
        } finally {
            dynRealm.close();
        }
    }

    @Test
    public void freezeLists() throws InterruptedException {
        Realm realm = createDataForLiveRealm(DATA_SIZE);
        AllTypes obj = realm.where(AllTypes.class).findFirst();
        RealmList<Dog> frozenObjectList = obj.getColumnRealmList().freeze();
        RealmList<String> frozenStringList = obj.getColumnStringList().freeze();
        Thread t = new Thread(() -> {
            assertEquals(5, frozenObjectList.size());
            assertTrue(frozenObjectList.isFrozen());
            assertEquals(1, frozenObjectList.where().equalTo(Dog.FIELD_NAME, "Dog 1").findAll().size());

            assertEquals(3, frozenStringList.size());
            assertTrue(frozenStringList.isFrozen());
            assertEquals("Foo", frozenStringList.first());
        });
        t.start();
        t.join();
    }

    @Test
    public void freezeDynamicList() throws InterruptedException {
        Realm realm = createDataForLiveRealm(DATA_SIZE);
        DynamicRealm dynRealm = DynamicRealm.getInstance(realm.getConfiguration());
        DynamicRealmObject obj = dynRealm.where(AllTypes.CLASS_NAME).findFirst();
        RealmList<DynamicRealmObject> frozenObjectList = obj.getList(AllTypes.FIELD_REALMLIST).freeze();
        RealmList<String> frozenStringList = obj.getList(AllTypes.FIELD_STRING_LIST, String.class).freeze();
        Thread t = new Thread(() -> {
            assertEquals(5, frozenObjectList.size());
            assertTrue(frozenObjectList.isFrozen());
            assertEquals(1, frozenObjectList.where().equalTo(Dog.FIELD_NAME, "Dog 1").findAll().size());

            assertEquals(3, frozenStringList.size());
            assertTrue(frozenStringList.isFrozen());
            assertEquals("Foo", frozenStringList.first());
        });
        t.start();
        t.join();
        dynRealm.close();
    }

    @Test
    public void freezeObject() throws InterruptedException {
        Realm realm = createDataForLiveRealm(DATA_SIZE);
        AllTypes obj = realm.where(AllTypes.class).sort(AllTypes.FIELD_LONG).findFirst();
        AllTypes frozenObj = obj.freeze();
        Thread t = new Thread(() -> {
            assertTrue(frozenObj.isFrozen());
            assertEquals(0, frozenObj.getColumnLong());
            assertTrue(frozenObj.getColumnRealmList().isFrozen());
            assertTrue(frozenObj.getColumnRealmObject().isFrozen());
        });
        t.start();
        t.join();
    }

    @Test
    public void freezeDynamicObject() throws InterruptedException {
        Realm realm = createDataForLiveRealm(DATA_SIZE);
        DynamicRealm dynRealm = DynamicRealm.getInstance(realm.getConfiguration());
        DynamicRealmObject obj = dynRealm.where(AllTypes.CLASS_NAME).sort(AllTypes.FIELD_LONG).findFirst();
        DynamicRealmObject frozenObj = obj.freeze();
        Thread t = new Thread(() -> {
            assertTrue(frozenObj.isFrozen());
            assertEquals(0, frozenObj.getLong(AllTypes.FIELD_LONG));
            assertTrue(frozenObj.getList(AllTypes.FIELD_REALMLIST).isFrozen());
            assertTrue(frozenObj.getObject(AllTypes.FIELD_REALMOBJECT).isFrozen());
        });
        t.start();
        t.join();
        dynRealm.close();
        assertTrue(Realm.getGlobalInstanceCount(realm.getConfiguration()) > 0);
    }

    @Test
    public void freezeDeletedObject() {
        Realm realm = createDataForLiveRealm(DATA_SIZE);
        AllTypes obj = realm.where(AllTypes.class).sort(AllTypes.FIELD_LONG).findFirst();
        realm.executeTransaction(r -> {
            obj.deleteFromRealm();
        });
        AllTypes frozenObj = obj.freeze();
        assertFalse(frozenObj.isValid());
        assertTrue(frozenObj.isFrozen());
        assertTrue(frozenObj.isLoaded());
    }

    @Test
    @RunTestInLooperThread
    public void freezePendingObject() {
        Realm realm = createDataForLiveRealm(DATA_SIZE);
        AllTypes obj = realm.where(AllTypes.class).sort(AllTypes.FIELD_LONG).findFirstAsync();

        AllTypes frozenObj = obj.freeze();
        assertFalse(frozenObj.isValid());
        assertFalse(frozenObj.isLoaded());
        assertTrue(frozenObj.isFrozen());
        looperThread.testComplete();
    }

    @Test
    public void frozenRealms_notEqualToLiveRealm() {
        assertNotEquals(realm, frozenRealm);
    }

    @Test
    public void frozenRealm_notEqualToFrozenRealmAtOtherVersion() {
        realm.beginTransaction();
        realm.commitTransaction();
        Realm otherFrozenRealm = realm.freeze();
        try {
            assertNotEquals(frozenRealm, otherFrozenRealm);
        } finally {
            otherFrozenRealm.close();
        }
    }

    @Test
    public void frozenRealm_equalToFrozenRealmAtSameVersion() throws InterruptedException {
        Realm otherFrozenRealm = realm.freeze();
        assertEquals(frozenRealm, otherFrozenRealm); // Same thread

        Thread t = new Thread(() -> {
            Realm bgRealm = Realm.getInstance(realmConfig);
            Realm otherThreadFrozenRealm = bgRealm.freeze();
            try {
                assertEquals(frozenRealm, otherThreadFrozenRealm);
            } finally {
                bgRealm.close();
            }
        });
        t.start();
        t.join();
    }

    @Test
    public void frozenRealm_closeFromOtherThread() throws InterruptedException {
        assertFalse(frozenRealm.isClosed());
        Thread t = new Thread(() -> {
            frozenRealm.close();
            assertTrue(frozenRealm.isClosed());
        });
        t.start();
        t.join();
    }

    @Test
    public void copyToRealm() throws InterruptedException {
        Realm realm = createDataForLiveRealm(DATA_SIZE);
        AllTypes frozenObject = realm.where(AllTypes.class).sort(AllTypes.FIELD_LONG).findFirst().freeze();

        Thread t = new Thread(() -> {
            Realm bgRealm = Realm.getInstance(realm.getConfiguration());
            bgRealm.beginTransaction();
            AllTypes copiedObject = bgRealm.copyToRealm(frozenObject);
            bgRealm.commitTransaction();

            assertEquals(DATA_SIZE + 1, bgRealm.where(AllTypes.class).count());
            assertEquals(frozenObject.getColumnLong(), copiedObject.getColumnLong());
            assertEquals(frozenObject.getColumnString(), copiedObject.getColumnString());
            assertEquals(frozenObject.getColumnRealmList().size(), copiedObject.getColumnRealmList().size());
            bgRealm.close();
        });
        t.start();
        t.join();
    }

    @Test
    public void copyToRealmOrUpdate() throws InterruptedException {
        realm.executeTransaction(r -> {
            r.createObject(AllJavaTypes.class, 42);
        });
        AllJavaTypes frozenObject = realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_ID, 42).findFirst().freeze();

        Thread t = new Thread(() -> {
            Realm bgRealm = Realm.getInstance(realm.getConfiguration());
            bgRealm.beginTransaction();
            AllJavaTypes copiedObject = bgRealm.copyToRealmOrUpdate(frozenObject);
            bgRealm.commitTransaction();

            assertEquals(1, bgRealm.where(AllJavaTypes.class).count());
            assertEquals(frozenObject.getFieldLong(), copiedObject.getFieldLong());
            bgRealm.close();
        });
        t.start();
        t.join();
    }

    @Test
    public void insert() throws InterruptedException {
        Realm realm = createDataForLiveRealm(DATA_SIZE);
        AllTypes frozenObject = realm.where(AllTypes.class).sort(AllTypes.FIELD_LONG).findFirst().freeze();

        Thread t = new Thread(() -> {
            Realm bgRealm = Realm.getInstance(realm.getConfiguration());
            bgRealm.beginTransaction();
            bgRealm.insert(frozenObject);
            bgRealm.commitTransaction();

            assertEquals(DATA_SIZE + 1, bgRealm.where(AllTypes.class).count());
            bgRealm.close();
        });
        t.start();
        t.join();
    }

    @Test
    public void insertList() throws InterruptedException {
        Realm realm = createDataForLiveRealm(DATA_SIZE);
        AllTypes frozenObject1 = realm.where(AllTypes.class).sort(AllTypes.FIELD_LONG, Sort.ASCENDING).findFirst().freeze();
        AllTypes frozenObject2 = realm.where(AllTypes.class).sort(AllTypes.FIELD_LONG, Sort.DESCENDING).findFirst().freeze();

        Thread t = new Thread(() -> {
            Realm bgRealm = Realm.getInstance(realm.getConfiguration());
            bgRealm.beginTransaction();
            bgRealm.insert(Arrays.asList(frozenObject1, frozenObject2));
            bgRealm.commitTransaction();

            assertEquals(DATA_SIZE + 2, bgRealm.where(AllTypes.class).count());
            bgRealm.close();
        });
        t.start();
        t.join();
    }

    @Test
    public void insertOrUpdate() throws InterruptedException {
        realm.executeTransaction(r -> {
            r.createObject(AllJavaTypes.class, 42);
        });
        AllJavaTypes frozenObject = realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_ID, 42).findFirst().freeze();

        Thread t = new Thread(() -> {
            Realm bgRealm = Realm.getInstance(realm.getConfiguration());
            bgRealm.beginTransaction();
            bgRealm.insertOrUpdate(frozenObject);
            bgRealm.commitTransaction();
            assertEquals(1, bgRealm.where(AllJavaTypes.class).count());
            bgRealm.close();
        });
        t.start();
        t.join();
    }

    @Test
    public void insertOrUpdateList() throws InterruptedException {
        realm.executeTransaction(r -> {
            r.createObject(AllJavaTypes.class, 42);
            r.createObject(AllJavaTypes.class, 43);
        });

        // Create two Java objects pointing to the same underlying Realm object in order to verify
        // that insertOrUpdate works correctly both for the same Java object but also for two
        // different Java objects representing the same Realm Object.
        AllJavaTypes frozenObject1 = realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_ID, 42).findFirst().freeze();
        AllJavaTypes frozenObject2 = realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_ID, 42).findFirst().freeze();

        Thread t = new Thread(() -> {
            Realm bgRealm = Realm.getInstance(realm.getConfiguration());
            bgRealm.beginTransaction();
            bgRealm.insertOrUpdate(Arrays.asList(frozenObject1, frozenObject1, frozenObject2));
            bgRealm.commitTransaction();
            assertEquals(2, bgRealm.where(AllJavaTypes.class).count());
            bgRealm.close();
        });
        t.start();
        t.join();
    }

    @Test
    public void realmObject_equals() throws InterruptedException {
        realm = createDataForLiveRealm(DATA_SIZE);
        AllTypes obj1 = realm.where(AllTypes.class).sort(AllTypes.FIELD_LONG).findFirst();
        AllTypes obj2 = realm.where(AllTypes.class).sort(AllTypes.FIELD_LONG).findFirst();
        AllTypes obj1Frozen = obj1.freeze();
        AllTypes obj2Frozen = obj2.freeze();

        assertEquals(obj1, obj2);
        assertEquals(obj1Frozen, obj2Frozen);
        assertFalse(obj1.equals(obj1Frozen));
        Thread t = new Thread(() -> {
            Realm bgRealm = Realm.getInstance(realm.getConfiguration());
            AllTypes bgObj1 = bgRealm.where(AllTypes.class).sort(AllTypes.FIELD_LONG).findFirst();
            AllTypes bgObj1Frozen = bgObj1.freeze();
            assertEquals(obj1Frozen, obj2Frozen);
            assertEquals(obj1Frozen, bgObj1Frozen);
            bgRealm.close();
        });
        t.start();
        t.join();
    }

    @Test
    public void realmObject_returnsFrozenRealm() {
        realm = createDataForLiveRealm(DATA_SIZE);
        AllTypes obj = realm.where(AllTypes.class).sort(AllTypes.FIELD_LONG).findFirst().freeze();
        assertTrue(obj.getRealm().isFrozen());
    }

    @Test
    public void realmList_returnsFrozenRealm() {
        realm = createDataForLiveRealm(DATA_SIZE);
        RealmResults<AllTypes> results = realm.where(AllTypes.class).sort(AllTypes.FIELD_LONG).findAll().freeze();
        assertTrue(results.getRealm().isFrozen());
    }

    @Test
    public void realmResults_returnsFrozenRealm() {
        realm = createDataForLiveRealm(DATA_SIZE);
        RealmList<Dog> list = realm.where(AllTypes.class).sort(AllTypes.FIELD_LONG).findFirst().getColumnRealmList().freeze();
        assertTrue(list.getRealm().isFrozen());
    }

    private Realm createDataForFrozenRealm(int dataSize) {
        return createDataForLiveRealm(dataSize).freeze();
    }

    private Realm createDataForLiveRealm(int dataSize) {
        realm.executeTransaction(r -> {

            RealmList<Dog> list = new RealmList<>();
            for (int i = 0; i < 5; i++) {
                list.add(r.copyToRealm(new Dog("Dog " + i)));
            }
            for (int i = 0; i < dataSize; i++) {
                AllTypes obj = new AllTypes();
                obj.setColumnString("String " + i);
                obj.setColumnLong(i);
                obj.setColumnRealmList(list);
                obj.setColumnStringList(new RealmList<String>("Foo", "Bar", "Baz"));
                obj.setColumnRealmObject(r.copyToRealm(new Dog("Dog 42")));
                r.insert(obj);
            }
        });
        return realm;
    }
}
