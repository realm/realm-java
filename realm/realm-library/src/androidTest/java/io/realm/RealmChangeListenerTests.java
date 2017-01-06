/*
 * Copyright 2016 Realm Inc.
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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.entities.AllTypes;
import io.realm.entities.Cat;
import io.realm.entities.pojo.AllTypesRealmModel;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class RealmChangeListenerTests {
    @Rule
    public final UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private Realm realm;
    private RealmConfiguration realmConfig;

    @Before
    public void setUp() {
        realmConfig = configFactory.createConfiguration();
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    @Test
    @RunTestInLooperThread
    public void returnedRealmIsNotNull() {
        Realm realm = looperThread.realm;
        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm realm) {
                assertNotNull(realm);
                assertFalse(realm.isClosed());
                looperThread.testComplete();
            }
        });
        realm.beginTransaction();
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void returnedDynamicRealmIsNotNull() {
        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(looperThread.realmConfiguration);
        dynamicRealm.addChangeListener(new RealmChangeListener<DynamicRealm>() {
            @Override
            public void onChange(DynamicRealm dynRealm) {
                assertNotNull(dynRealm);
                assertFalse(dynRealm.isClosed());
                dynRealm.close();
                looperThread.testComplete();
            }
        });
        dynamicRealm.beginTransaction();
        dynamicRealm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void returnedRealmResultsIsNotNull() {
        Realm realm = looperThread.realm;
        RealmResults<Cat> cats = realm.where(Cat.class).findAll();
        looperThread.keepStrongReference.add(cats);
        cats.addChangeListener(new RealmChangeListener<RealmResults<Cat>>() {
            @Override
            public void onChange(RealmResults<Cat> result) {
                assertEquals("cat1", result.first().getName());
                looperThread.testComplete();
            }
        });
        realm.beginTransaction();
        Cat cat = realm.createObject(Cat.class);
        cat.setName("cat1");
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void returnedRealmResultsOfModelIsNotNull() {
        Realm realm = looperThread.realm;
        RealmResults<AllTypesRealmModel> alltypes = realm.where(AllTypesRealmModel.class).findAll();
        looperThread.keepStrongReference.add(alltypes);
        alltypes.addChangeListener(new RealmChangeListener<RealmResults<AllTypesRealmModel>>() {
            @Override
            public void onChange(RealmResults<AllTypesRealmModel> result) {
                assertEquals("data 1", result.first().columnString);
                looperThread.testComplete();
            }
        });

        realm.beginTransaction();
        AllTypesRealmModel model = realm.createObject(AllTypesRealmModel.class, 0);
        model.columnString = "data 1";
        realm.commitTransaction();
    }


    @Test
    @RunTestInLooperThread
    public void returnedRealmObjectIsNotNull() {
        Realm realm = looperThread.realm;
        realm.beginTransaction();
        Cat cat = looperThread.realm.createObject(Cat.class);
        realm.commitTransaction();

        looperThread.keepStrongReference.add(cat);
        cat.addChangeListener(new RealmChangeListener<Cat>() {
            @Override
            public void onChange(Cat object) {
                assertEquals("cat1", object.getName());
                looperThread.testComplete();
            }
        });

        realm.beginTransaction();
        cat.setName("cat1");
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void returnedRealmModelIsNotNull() {
        Realm realm = looperThread.realm;
        realm.beginTransaction();
        AllTypesRealmModel model = realm.createObject(AllTypesRealmModel.class, 0);
        realm.commitTransaction();

        looperThread.keepStrongReference.add(model);
        RealmObject.addChangeListener(model, new RealmChangeListener<AllTypesRealmModel>() {
            @Override
            public void onChange(AllTypesRealmModel object) {
                assertEquals("model1", object.columnString);
                looperThread.testComplete();
            }
        });

        realm.beginTransaction();
        model.columnString = "model1";
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void returnedDynamicRealmObjectIsNotNull() {
        Realm realm = Realm.getInstance(looperThread.realmConfiguration);
        realm.close();

        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(looperThread.realmConfiguration);
        dynamicRealm.beginTransaction();
        DynamicRealmObject allTypes = dynamicRealm.createObject(AllTypes.CLASS_NAME);
        dynamicRealm.commitTransaction();

        looperThread.keepStrongReference.add(allTypes);
        allTypes.addChangeListener(new RealmChangeListener<DynamicRealmObject>() {
            @Override
            public void onChange(DynamicRealmObject object) {
                assertEquals("test data 1", object.getString(AllTypes.FIELD_STRING));
                dynamicRealm.close();
                looperThread.testComplete();
            }
        });
        dynamicRealm.beginTransaction();
        allTypes.setString(AllTypes.FIELD_STRING, "test data 1");
        dynamicRealm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void returnedDynamicRealmResultsIsNotNull() {
        Realm realm = Realm.getInstance(looperThread.realmConfiguration);
        realm.close();

        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(looperThread.realmConfiguration);
        RealmResults<DynamicRealmObject> all = dynamicRealm.where(AllTypes.CLASS_NAME).findAll();
        looperThread.keepStrongReference.add(all);
        all.addChangeListener(new RealmChangeListener<RealmResults<DynamicRealmObject>>() {
            @Override
            public void onChange(RealmResults<DynamicRealmObject> result) {
                assertEquals("test data 1", result.first().getString(AllTypes.FIELD_STRING));
                dynamicRealm.close();
                looperThread.testComplete();
            }
        });

        dynamicRealm.beginTransaction();
        DynamicRealmObject allTypes = dynamicRealm.createObject(AllTypes.CLASS_NAME);
        allTypes.setString(AllTypes.FIELD_STRING, "test data 1");
        dynamicRealm.commitTransaction();
    }
}
