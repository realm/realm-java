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

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.entities.AllTypes;
import io.realm.entities.BacklinksSource;
import io.realm.entities.BacklinksTarget;
import io.realm.entities.Cat;
import io.realm.entities.StringOnly;
import io.realm.entities.pojo.AllTypesRealmModel;
import io.realm.log.RealmLog;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
        Realm realm = looperThread.getRealm();
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
        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(looperThread.getConfiguration());
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
        Realm realm = looperThread.getRealm();
        RealmResults<Cat> cats = realm.where(Cat.class).findAll();
        looperThread.keepStrongReference(cats);
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
        Realm realm = looperThread.getRealm();
        RealmResults<AllTypesRealmModel> alltypes = realm.where(AllTypesRealmModel.class).findAll();
        looperThread.keepStrongReference(alltypes);
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
        Realm realm = looperThread.getRealm();
        realm.beginTransaction();
        Cat cat = realm.createObject(Cat.class);
        realm.commitTransaction();

        looperThread.keepStrongReference(cat);
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
        Realm realm = looperThread.getRealm();
        realm.beginTransaction();
        AllTypesRealmModel model = realm.createObject(AllTypesRealmModel.class, 0);
        realm.commitTransaction();

        looperThread.keepStrongReference(model);
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
        Realm realm = Realm.getInstance(looperThread.getConfiguration());
        realm.close();

        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(looperThread.getConfiguration());
        dynamicRealm.beginTransaction();
        DynamicRealmObject allTypes = dynamicRealm.createObject(AllTypes.CLASS_NAME);
        dynamicRealm.commitTransaction();

        looperThread.keepStrongReference(allTypes);
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
        Realm realm = Realm.getInstance(looperThread.getConfiguration());
        realm.close();

        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(looperThread.getConfiguration());
        RealmResults<DynamicRealmObject> all = dynamicRealm.where(AllTypes.CLASS_NAME).findAll();
        looperThread.keepStrongReference(all);
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

    // 1. adding a listener on the children
    // 2. modify parent
    // 3. at least one child is modified
    // 4. listener is not triggered (backlink)
    // FIXME: will break when https://github.com/realm/realm-java/issues/4875 is solved
    @Test
    @RunTestInLooperThread
    public void listenerOnChildChangeParent() {
        final long[] nCalls = {0};
        final Realm realm = Realm.getInstance(looperThread.getConfiguration());
        TestHelper.populateLinkedDataSet(realm);

        RealmResults<BacklinksTarget> backlinksTargets = realm.where(BacklinksTarget.class).findAll();
        assertEquals(3, backlinksTargets.size());
        assertTrue(backlinksTargets.last().getParents().isEmpty());
        assertEquals(2, backlinksTargets.first().getParents().size());

        looperThread.keepStrongReference(backlinksTargets);

        backlinksTargets.addChangeListener(new RealmChangeListener<RealmResults<BacklinksTarget>>() {
            @Override
            public void onChange(RealmResults<BacklinksTarget> backlinksTargets) {
                nCalls[0]++;
            }
        });

        realm.beginTransaction();
        BacklinksTarget target = backlinksTargets.last();
        realm.where(BacklinksSource.class).findFirst().setChild(target);
        realm.commitTransaction();

        // backlinks are updated
        assertEquals(1, backlinksTargets.last().getParents().size());
        assertEquals(1, backlinksTargets.first().getParents().size());
        assertEquals(0, nCalls[0]);
        realm.close();
        looperThread.testComplete();
    }

    // 1. adding a listener if on the parent
    // 2. modify child
    // 3. listener is triggered (forward link)
    @Test
    @RunTestInLooperThread
    public void listenerOnParentChangeChild() {
        final long[] nCalls = {0};
        final Realm realm = Realm.getInstance(looperThread.getConfiguration());
        TestHelper.populateLinkedDataSet(realm);

        RealmResults<BacklinksSource> backlinksSources = realm.where(BacklinksSource.class).findAll();
        assertEquals(4, backlinksSources.size());

        looperThread.keepStrongReference(backlinksSources);
        backlinksSources.addChangeListener(new RealmChangeListener<RealmResults<BacklinksSource>>() {
            @Override
            public void onChange(RealmResults<BacklinksSource> backlinksSources) {
                nCalls[0]++;
            }
        });

        realm.beginTransaction();
        BacklinksTarget backlinksTarget = realm.where(BacklinksTarget.class).findFirst();
        backlinksTarget.setId(42);
        realm.commitTransaction();

        assertEquals(42, backlinksSources.first().getChild().getId());
        assertEquals(1, nCalls[0]);

        realm.close();
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void removeListenerOnInvalidObjectShouldWarn() {
        realm = Realm.getInstance(realmConfig);
        RealmChangeListener<StringOnly> listener = realmModel -> {
        };
        RealmChangeListener<RealmResults<StringOnly>> listenerAll = realmModel -> {
        };

        realm.beginTransaction();
        StringOnly stringOnly = realm.createObject(StringOnly.class);
        realm.commitTransaction();

        stringOnly.addChangeListener(listener);

        RealmResults<StringOnly> all = realm.where(StringOnly.class).findAll();
        all.addChangeListener(listenerAll);

        realm.close();

        // add a custom logger to capture expected warning message
        TestHelper.TestLogger testLogger = new TestHelper.TestLogger();
        RealmLog.add(testLogger);

        stringOnly.removeChangeListener(listener);
        assertThat(testLogger.message, CoreMatchers.containsString(
                "Calling removeChangeListener on a closed Realm " + realm.getPath() + ", make sure to close all listeners before closing the Realm."));

        testLogger.message = "";
        stringOnly.removeAllChangeListeners();
        assertThat(testLogger.message, CoreMatchers.containsString(
                "Calling removeChangeListener on a closed Realm " + realm.getPath() + ", make sure to close all listeners before closing the Realm."));


        testLogger.message = "";
        all.removeChangeListener(listenerAll);
        assertThat(testLogger.message, CoreMatchers.containsString(
                "Calling removeChangeListener on a closed Realm " + realm.getPath() + ", make sure to close all listeners before closing the Realm."));

        testLogger.message = "";
        all.removeAllChangeListeners();
        assertThat(testLogger.message, CoreMatchers.containsString(
                "Calling removeChangeListener on a closed Realm " + realm.getPath() + ", make sure to close all listeners before closing the Realm."));

        RealmLog.remove(testLogger);

        looperThread.testComplete();
    }
}
