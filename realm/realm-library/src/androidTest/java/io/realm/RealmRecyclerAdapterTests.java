/*
 * Copyright 2016 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *gio
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.FrameLayout;

import io.realm.entities.AllTypes;
import io.realm.entities.RealmRecyclerAdapter;
import io.realm.entities.UnsupportedCollection;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmRecyclerAdapterTests {

    @Rule
    public final UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    private Context context;

    private static final int TEST_DATA_SIZE = 47;
    private static final boolean AUTOMATIC_UPDATE = true;

    private Realm realm;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getContext();
        RealmConfiguration realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);

        populateTestRealm(realm, TEST_DATA_SIZE);
    }

    private void populateTestRealm(Realm testRealm, int objects) {
        testRealm.beginTransaction();
        for (int i = 0; i < objects; i++) {
            AllTypes allTypes = testRealm.createObject(AllTypes.class);
            allTypes.setColumnString("test data " + i);
        }
        testRealm.commitTransaction();
    }

    @After
    public void tearDown() throws Exception {
        realm.close();
    }

    @Test
    public void constructor_testRecyclerAdapterParameterExceptions() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        try {
            new RealmRecyclerAdapter(null, resultList, AUTOMATIC_UPDATE);
            fail("Should throw exception if context is null");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    @UiThreadTest
    public void clear() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, AUTOMATIC_UPDATE);
        realm.beginTransaction();
        resultList.deleteAllFromRealm();
        realm.commitTransaction();

        assertEquals(0, realmAdapter.getItemCount());
        assertEquals(0, resultList.size());
    }

    @Test
    @UiThreadTest
    public void updateData_realmResultInAdapter() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        resultList.sort(AllTypes.FIELD_STRING);
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, false);
        assertEquals(resultList.first().getColumnString(), realmAdapter.getData().first().getColumnString());
        assertEquals(resultList.size(), realmAdapter.getData().size());

        realm.beginTransaction();
        AllTypes allTypes = realm.createObject(AllTypes.class);
        allTypes.setColumnString("test data " + TEST_DATA_SIZE);
        realm.commitTransaction();
        assertEquals(resultList.last().getColumnString(), realmAdapter.getData().last().getColumnString());
        assertEquals(resultList.size(), realmAdapter.getData().size());

        RealmResults<AllTypes> emptyResultList = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "Not there").findAll();
        realmAdapter.updateData(emptyResultList);
        assertEquals(emptyResultList.size(), realmAdapter.getData().size());
    }

    @Test
    @UiThreadTest
    public void updateData_realmUnsupportedCollectionInAdapter() {
        try {
            RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, null, AUTOMATIC_UPDATE);
            realmAdapter.updateData(new UnsupportedCollection<AllTypes>());
            fail("Should throw exception if there is unsupported collection");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    @UiThreadTest
    public void getItemCount_emptyRealmResult() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "Not there").findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, AUTOMATIC_UPDATE);
        assertEquals(0, resultList.size());
        assertEquals(0, realmAdapter.getData().size());
        assertEquals(0, realmAdapter.getItemCount());
    }

    @Test
    @UiThreadTest
    public void getItem_testGettingData() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, AUTOMATIC_UPDATE);

        assertEquals(resultList.get(0).getColumnString(), realmAdapter.getItem(0).getColumnString());
        assertEquals(resultList.size(), realmAdapter.getData().size());
        assertEquals(resultList.last().getColumnString(), realmAdapter.getData().last().getColumnString());
    }

    @Test
    @UiThreadTest
    public void getItem_testGettingNullData() {
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, null, AUTOMATIC_UPDATE);
        assertNull(realmAdapter.getItem(0));
    }

    @Test
    @UiThreadTest
    public void getItemId_testGetItemId() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, AUTOMATIC_UPDATE);
        for (int i = 0; i < resultList.size(); i++) {
            assertEquals(i, realmAdapter.getItemId(i));
        }
    }

    @Test
    @UiThreadTest
    public void getItemCount_testGetCount() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, AUTOMATIC_UPDATE);
        assertEquals(TEST_DATA_SIZE, realmAdapter.getItemCount());
    }

    @Test
    @UiThreadTest
    public void getItemCount_testNullResults() {
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, null, AUTOMATIC_UPDATE);
        assertEquals(0, realmAdapter.getItemCount());
    }

    @Test
    @UiThreadTest
    public void getItemCount_testNotValidResults() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, AUTOMATIC_UPDATE);

        realm.close();
        assertEquals(0, realmAdapter.getItemCount());
    }

    @Test
    @UiThreadTest
    public void getItemCount_testNonNullToNullResults() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, AUTOMATIC_UPDATE);
        realmAdapter.updateData(null);

        assertEquals(0, realmAdapter.getItemCount());
    }

    @Test
    @UiThreadTest
    public void getItemCount_testNullToNonNullResults() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, null, AUTOMATIC_UPDATE);
        realmAdapter.updateData(resultList);

        assertEquals(TEST_DATA_SIZE, realmAdapter.getItemCount());
    }

    @Test
    @UiThreadTest
    public void viewHolderTestForSimpleView() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, AUTOMATIC_UPDATE);

        RealmRecyclerAdapter.ViewHolder holder = realmAdapter.onCreateViewHolder(new FrameLayout(context), 0);
        assertNotNull(holder.textView);

        realmAdapter.onBindViewHolder(holder, 0);
        assertEquals(resultList.get(0).getColumnString(), holder.textView.getText());
    }
}
