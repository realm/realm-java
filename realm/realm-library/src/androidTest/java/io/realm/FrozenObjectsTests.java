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
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import javax.annotation.Nullable;

import io.realm.entities.AllTypes;
import io.realm.entities.Dog;
import io.realm.internal.Util;
import io.realm.log.RealmLog;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Class testing various aspects of the frozen objects features
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
        if (realm != null) {
            realm.close();
        }
        if (frozenRealm != null) {
            frozenRealm.close();
        }

        // FIXME: Work-around for https://github.com/realm/realm-core/issues/3435
        deleteRealm(realmConfig);
    }

    private void deleteRealm(RealmConfiguration configuration) {
        String canonicalPath = configuration.getPath();
        File realmFolder = configuration.getRealmDirectory();
        String realmFileName = configuration.getRealmFileName();
        assertTrue(Util.deleteRealm(canonicalPath, realmFolder, realmFileName));
    }

    @Test
    public void freezeRealm() {
        Realm frozenRealm = realm.freeze();
        assertEquals(realm.getPath(), frozenRealm.getPath());
        assertTrue(frozenRealm.isFrozen());
        frozenRealm.close();

        DynamicRealm dynamicRealm = DynamicRealm.getInstance(realmConfig);
        DynamicRealm frozenDynamicRealm = dynamicRealm.freeze();
        assertEquals(dynamicRealm.getPath(), frozenDynamicRealm.getPath());
        assertTrue(frozenRealm.isFrozen());
        dynamicRealm.close();
        assertFalse(frozenDynamicRealm.isClosed());
        frozenDynamicRealm.close();
    }

    @Test
    public void freezeRealmInsideWriteTransactionsThrows() {
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
            RealmLog.error(ignore.toString());
        }

        try {
            obj.getColumnRealmList().addChangeListener(new OrderedRealmCollectionChangeListener<RealmList<Dog>>() {
                @Override
                public void onChange(RealmList<Dog> dogs, OrderedCollectionChangeSet changeSet) {
                }
            });
            fail();
        } catch (IllegalStateException ignore) {
            RealmLog.error(ignore.toString());
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
        frozenRealm.close();
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
        frozenRealm.close();
    }

    @Test
    public void canReadFrozenListsAcrossThreads() throws InterruptedException {
        Realm frozenRealm = createDataForFrozenRealm(DATA_SIZE);
        RealmList<Dog> list = frozenRealm.where(AllTypes.class).findFirst().getColumnRealmList();
        Thread t = new Thread(() -> {
            assertEquals(0, list.size());
            assertTrue(list.isFrozen());
        });
        t.start();
        t.join();
        frozenRealm.close();
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
        frozenRealm.close();
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
        frozenRealm.close();
    }

    @Test
    public void freezeResults() throws InterruptedException {
        Realm realm = createDataForLiveRealm(DATA_SIZE);
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> frozenResults = results.freeze();
        Thread t = new Thread(() -> {
            assertEquals(DATA_SIZE, frozenResults.size());
            assertTrue(frozenResults.isFrozen());
            assertEquals(1, frozenResults.where().equalTo(AllTypes.FIELD_LONG, 1).findAll().size());
        });
        t.start();
        t.join();
        frozenResults.getRealm().close(); // FIXME: What to do about frozen Realm lifecycles?
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
        frozenObjectList.getRealm().close(); // FIXME: What to do about frozen Realm lifecycles?
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
        frozenObj.getRealm().close(); // FIXME: What to do about frozen Realm lifecycles?
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
