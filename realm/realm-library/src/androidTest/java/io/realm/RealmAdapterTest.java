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

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.annotation.UiThreadTest;
import android.view.View;
import android.widget.TextView;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.entities.AllTypes;
import io.realm.entities.RealmAdapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmAdapterTest {
    @Rule
    public final UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();
    private Context context;

    private final static String FIELD_STRING = "columnString";
    private final static int TEST_DATA_SIZE = 47;

    private boolean automaticUpdate = true;
    private Realm testRealm;

    @Before
    public void setUp() throws Exception {
        // Injecting the Instrumentation instance is required
        // for your test to run with AndroidJUnitRunner.
        context = InstrumentationRegistry.getInstrumentation().getContext();

        RealmConfiguration realmConfig = TestHelper.createConfiguration(context);
        Realm.deleteRealm(realmConfig);
        testRealm = Realm.getInstance(realmConfig);

        testRealm.beginTransaction();
        for (int i = 0; i < TEST_DATA_SIZE; ++i) {
            AllTypes allTypes = testRealm.createObject(AllTypes.class);
            allTypes.setColumnString("test data " + i);
        }
        testRealm.commitTransaction();
    }

    @After
    public void tearDown() throws Exception {
        testRealm.close();
    }

    @Test
    public void testAdapterParameterExceptions() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        try {
            RealmAdapter realmAdapter = new RealmAdapter(null, resultList, automaticUpdate);
            fail("Should throw exception if context is null");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testUpdateRealmResultInAdapter() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        resultList.sort(FIELD_STRING);
        RealmAdapter realmAdapter = new RealmAdapter(context, resultList, false);
        assertEquals(resultList.first().getColumnString(), realmAdapter.getRealmResults().first().getColumnString());
        assertEquals(resultList.size(), realmAdapter.getRealmResults().size());

        testRealm.beginTransaction();
        AllTypes allTypes = testRealm.createObject(AllTypes.class);
        allTypes.setColumnString("test data " + TEST_DATA_SIZE);
        testRealm.commitTransaction();
        assertEquals(resultList.last().getColumnString(), realmAdapter.getRealmResults().last().getColumnString());
        assertEquals(resultList.size(), realmAdapter.getRealmResults().size());

        RealmResults<AllTypes> emptyResultList = testRealm.where(AllTypes.class).equalTo(FIELD_STRING, "Not there").findAll();
        realmAdapter.updateRealmResults(emptyResultList);
        assertEquals(emptyResultList.size(), realmAdapter.getRealmResults().size());
    }

    @Test
    @UiThreadTest
    public void testClearFromAdapter() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmAdapter realmAdapter = new RealmAdapter(context, resultList, automaticUpdate);

        testRealm.beginTransaction();
        realmAdapter.getRealmResults().clear();
        testRealm.commitTransaction();

        assertEquals(0, realmAdapter.getCount());
        assertEquals(0, resultList.size());
    }

    @Test
    @UiThreadTest
    public void testRemoveFromAdapter() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmAdapter realmAdapter = new RealmAdapter(context, resultList, automaticUpdate);

        testRealm.beginTransaction();
        realmAdapter.getRealmResults().remove(0);
        testRealm.commitTransaction();
        assertEquals(TEST_DATA_SIZE - 1, realmAdapter.getCount());

        resultList = testRealm.where(AllTypes.class).equalTo(FIELD_STRING, "test data 0").findAll();
        assertEquals(0, resultList.size());
    }

    @Test
    @UiThreadTest
    public void testSortWithAdapter() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        resultList.sort(FIELD_STRING, Sort.DESCENDING);
        RealmAdapter realmAdapter = new RealmAdapter(context, resultList, automaticUpdate);
        assertEquals(resultList.first().getColumnString(), realmAdapter.getRealmResults().first().getColumnString());
        assertEquals(resultList.size(), realmAdapter.getRealmResults().size());

        resultList.sort(FIELD_STRING);

        assertEquals(resultList.last().getColumnString(), realmAdapter.getRealmResults().last().getColumnString());
        assertEquals(resultList.get(TEST_DATA_SIZE / 2).getColumnString(), realmAdapter.getRealmResults().get(TEST_DATA_SIZE / 2).getColumnString());
        assertEquals(resultList.size(), realmAdapter.getRealmResults().size());
    }

    @Test
    @UiThreadTest
    public void testEmptyRealmResult() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).equalTo(FIELD_STRING, "Not there").findAll();
        RealmAdapter realmAdapter = new RealmAdapter(context, resultList, automaticUpdate);
        assertEquals(0, realmAdapter.getRealmResults().size());
        assertEquals(0, realmAdapter.getCount());
    }

    @Test
    @UiThreadTest
    public void testGetItem() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmAdapter realmAdapter = new RealmAdapter(context, resultList, automaticUpdate);

        assertEquals(resultList.get(0).getColumnString(), realmAdapter.getItem(0).getColumnString());
        assertEquals(resultList.size(), realmAdapter.getRealmResults().size());
        assertEquals(resultList.last().getColumnString(), realmAdapter.getRealmResults().last().getColumnString());
    }

    @Test
    @UiThreadTest
    public void testGetItemId() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmAdapter realmAdapter = new RealmAdapter(context, resultList, automaticUpdate);
        for (int i = 0; i < resultList.size(); i++) {
            assertEquals(i, realmAdapter.getItemId(i));
        }
    }

    @Test
    @UiThreadTest
    public void testGetCount() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmAdapter realmAdapter = new RealmAdapter(context, resultList, automaticUpdate);
        assertEquals(TEST_DATA_SIZE, realmAdapter.getCount());
    }

    @Test
    @UiThreadTest
    public void testGetView() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmAdapter realmAdapter = new RealmAdapter(context, resultList, automaticUpdate);
        View view = realmAdapter.getView(0, null, null);

        TextView name = (TextView) view.findViewById(android.R.id.text1);

        assertNotNull(view);
        assertNotNull(name);
        assertEquals(resultList.get(0).getColumnString(), name.getText());
    }

    @Test
    public void testNullResults() {
        RealmAdapter realmAdapter = new RealmAdapter(context, null, automaticUpdate);

        assertEquals(0, realmAdapter.getCount());
    }

    @Test
    @UiThreadTest
    public void testNonNullToNullResults() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmAdapter realmAdapter = new RealmAdapter(context, resultList, automaticUpdate);
        realmAdapter.updateRealmResults(null);

        assertEquals(0, realmAdapter.getCount());
    }

    @Test
    @UiThreadTest
    public void testNullToNonNullResults() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmAdapter realmAdapter = new RealmAdapter(context, null, automaticUpdate);
        realmAdapter.updateRealmResults(resultList);

        assertEquals(TEST_DATA_SIZE, realmAdapter.getCount());
    }
}
