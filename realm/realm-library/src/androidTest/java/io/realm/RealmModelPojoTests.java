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

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Date;

import io.realm.entities.pojo.AllTypesPojo;
import io.realm.entities.pojo.InvalidModelPojo;
import io.realm.exceptions.RealmException;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static io.realm.internal.test.ExtraTests.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class RealmModelPojoTests {
    private final static int TEST_DATA_SIZE = 10;

    private Context context;
    private Realm realm;
    private RealmConfiguration realmConfig;

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    @Before
    public void setUp() {
        // Injecting the Instrumentation instance is required
        // for your test to run with AndroidJUnitRunner.
        context = InstrumentationRegistry.getInstrumentation().getContext();
        realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    private void populateTestRealm(Realm realm, int objects) {
        realm.beginTransaction();
        realm.allObjects(AllTypesPojo.class).deleteAllFromRealm();
        for (int i = 0; i < objects; ++i) {
            AllTypesPojo allTypes = new AllTypesPojo();
            allTypes.columnLong = i;
            allTypes.columnBoolean = (i % 3) == 0;
            allTypes.columnBinary = new byte[]{1, 2, 3};
            allTypes.columnDate = new Date();
            allTypes.columnDouble = 3.1415 + i;
            allTypes.columnFloat = 1.234567f;
            allTypes.columnString = "test data ";
            allTypes.columnByte = 0b0010_1010;
            realm.copyToRealm(allTypes);
        }
        realm.commitTransaction();
    }

    @Test
    public void createObject() {
        for (int i = 1; i < 43; i++) { // using i = 0 as PK will crash subsequent createObject
                                       // since createObject uses default values
            realm.beginTransaction();
            AllTypesPojo allTypesPojo = realm.createObject(AllTypesPojo.class);
            allTypesPojo.columnLong = i;
            realm.commitTransaction();
        }

        RealmResults<AllTypesPojo> resultList = realm.allObjects(AllTypesPojo.class);
        assertEquals("Realm.get is returning wrong result set", 42, resultList.size());
    }

    @Test
    public void copyToRealm() {
        populateTestRealm(realm, TEST_DATA_SIZE);
        RealmResults<AllTypesPojo> resultList = realm.allObjects(AllTypesPojo.class);
        assertEquals("Realm.get is returning wrong result set", TEST_DATA_SIZE, resultList.size());
    }


    @Test
    public void copyFromRealm() {
        populateTestRealm(realm, TEST_DATA_SIZE);

        AllTypesPojo realmObject = realm.where(AllTypesPojo.class).findAllSorted(AllTypesPojo.FIELD_LONG).first();
        AllTypesPojo standaloneObject = realm.copyFromRealm(realmObject);
        assertArrayEquals(realmObject.columnBinary, standaloneObject.columnBinary);
        assertEquals(realmObject.columnString, standaloneObject.columnString);
        assertEquals(realmObject.columnLong, standaloneObject.columnLong);
        assertEquals(realmObject.columnFloat, standaloneObject.columnFloat, 0.00000000001);
        assertEquals(realmObject.columnDouble, standaloneObject.columnDouble, 0.00000000001);
        assertEquals(realmObject.columnBoolean, standaloneObject.columnBoolean);
        assertEquals(realmObject.columnDate, standaloneObject.columnDate);
        assertEquals(realmObject.hashCode(), standaloneObject.hashCode());

    }

    @Test
    public void copyToRealmOrUpdate() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                AllTypesPojo obj = new AllTypesPojo();
                obj.columnLong = 1;
                realm.copyToRealm(obj);

                AllTypesPojo obj2 = new AllTypesPojo();
                obj2.columnLong = 1;
                obj2.columnString = "Foo";
                realm.copyToRealmOrUpdate(obj2);
            }
        });

        assertEquals(1, realm.allObjects(AllTypesPojo.class).size());

        AllTypesPojo obj = realm.allObjects(AllTypesPojo.class).first();
        assertEquals("Foo", obj.columnString);
    }

    @Test
    public void createOrUpdateAllFromJson() throws IOException {
        realm.beginTransaction();
        realm.createOrUpdateAllFromJson(AllTypesPojo.class, TestHelper.loadJsonFromAssets(context, "list_alltypes_primarykey.json"));
        realm.commitTransaction();

        assertEquals(1, realm.allObjects(AllTypesPojo.class).size());
        AllTypesPojo obj = realm.allObjects(AllTypesPojo.class).first();
        assertEquals("Bar", obj.columnString);
        assertEquals(2.23F, obj.columnFloat, 0.000000001);
        assertEquals(2.234D, obj.columnDouble, 0.000000001);
        assertEquals(true, obj.columnBoolean);
        assertArrayEquals(new byte[]{1, 2, 3}, obj.columnBinary);
        assertEquals(new Date(2000), obj.columnDate);
        assertEquals("Dog4", obj.columnRealmObject.getName());
        assertEquals(2, obj.columnRealmList.size());
        assertEquals("Dog5", obj.columnRealmList.get(0).getName());
    }

    // where with filed selection
    @Test
    public void query() {
        populateTestRealm(realm, TEST_DATA_SIZE);

        assertEquals(5, realm.where(AllTypesPojo.class).greaterThanOrEqualTo(AllTypesPojo.FIELD_DOUBLE, 8.1415).count());
    }

    // async where with filed selection
    @Test
    @RunTestInLooperThread
    public void async_query() {
        populateTestRealm(looperThread.realm, TEST_DATA_SIZE);

        final RealmResults<AllTypesPojo> allTypesPojos = looperThread.realm.distinctAsync(AllTypesPojo.class, AllTypesPojo.FIELD_STRING);
        allTypesPojos.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                assertEquals(1, allTypesPojos.size());
                looperThread.testComplete();
            }
        });
    }

    @Test
    public void dynamicObject() {
        populateTestRealm(realm, TEST_DATA_SIZE);

        AllTypesPojo typedObj = realm.allObjects(AllTypesPojo.class).first();
        DynamicRealmObject dObj = new DynamicRealmObject(typedObj);

        realm.beginTransaction();
        dObj.setLong(AllTypesPojo.FIELD_LONG, 42L);
        assertEquals(42, dObj.getLong(AllTypesPojo.FIELD_LONG));
        assertEquals(42, typedObj.columnLong);

        dObj.setBlob(AllTypesPojo.FIELD_BINARY, new byte[]{1, 2, 3});
        Assert.assertArrayEquals(new byte[]{1, 2, 3}, dObj.getBlob(AllTypesPojo.FIELD_BINARY));
        Assert.assertArrayEquals(new byte[]{1, 2, 3}, typedObj.columnBinary);
        realm.cancelTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void dynamicRealm() {
        populateTestRealm(looperThread.realm, TEST_DATA_SIZE);
        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(looperThread.realmConfiguration);

        dynamicRealm.beginTransaction();
        DynamicRealmObject dog = dynamicRealm.createObject(AllTypesPojo.CLASS_NAME, 42);
        assertEquals(42, dog.getLong(AllTypesPojo.FIELD_LONG));
        dynamicRealm.commitTransaction();


        RealmResults<DynamicRealmObject> allAsync = dynamicRealm.where(AllTypesPojo.CLASS_NAME).equalTo(AllTypesPojo.FIELD_LONG, 42).findAll();
        allAsync.load();
        assertTrue(allAsync.isLoaded());
        assertEquals(1, allAsync.size());

        dynamicRealm.beginTransaction();
        allAsync.deleteAllFromRealm();
        dynamicRealm.commitTransaction();

        RealmResults<DynamicRealmObject> results = dynamicRealm.where(AllTypesPojo.CLASS_NAME).findAll();
        assertEquals(TEST_DATA_SIZE, results.size());
        for (int i = 0; i < TEST_DATA_SIZE; i++) {
            assertEquals(3.1415 + i, results.get(i).getDouble(AllTypesPojo.FIELD_DOUBLE), 0.0000001);
            assertEquals((i % 3) == 0, results.get(i).getBoolean(AllTypesPojo.FIELD_BOOLEAN));
        }
        dynamicRealm.close();
        looperThread.testComplete();
    }

    // exception expected when using in schema model not annotated
    // a valid model need to implement the interface RealmModel and annotate the class with @RealmClass
    // we expect in this test a runtime exception 'InvalidModelPojo is not part of the schema for this Realm.'
    @Test(expected = RealmException.class)
    public void invalidModelDefinition() {
        realm.beginTransaction();
        realm.createObject(InvalidModelPojo.class);
        realm.commitTransaction();
    }
}
