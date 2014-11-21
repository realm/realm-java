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

import android.test.AndroidTestCase;
import android.view.View;
import android.widget.TextView;

import io.realm.entities.AllTypes;
import io.realm.entities.RealmAdapter;

public class RealmAdapterTest extends AndroidTestCase {

    private final static String FIELD_STRING = "columnString";

    private final static int TEST_DATA_SIZE = 47;

    private boolean SORT_ORDER_DECENDING = false;
    private boolean automaticUpdate = true;

    private Realm testRealm;

    public RealmAdapterTest() {
        super();
    }

    protected void setUp() throws Exception {
        super.setUp();
        Realm.deleteRealmFile(getContext());
        testRealm = Realm.getInstance(getContext());

        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        for (int i = 0; i < TEST_DATA_SIZE; ++i) {
            AllTypes allTypes = testRealm.createObject(AllTypes.class);
            allTypes.setColumnString("test data " + i);
        }
        testRealm.commitTransaction();

    }

    public void testUpdateRealmResultInAdapter() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll()
                .sort(FIELD_STRING);
        RealmAdapter realmAdapter = new RealmAdapter(getContext(), resultList, automaticUpdate);
        assertEquals(resultList.first(), realmAdapter.getRealmResults().first());
        assertEquals(resultList.size(),realmAdapter.getRealmResults().size());

        realmAdapter.updateRealmResults(realmAdapter.getRealmResults()
                .sort(FIELD_STRING, SORT_ORDER_DECENDING));
        assertEquals(resultList.first(), realmAdapter.getRealmResults().last());
        assertEquals(resultList.size(), realmAdapter.getRealmResults().size());

        realmAdapter.updateRealmResults(realmAdapter.getRealmResults().sort(FIELD_STRING));
        assertEquals(resultList.first(), realmAdapter.getRealmResults().first());
        assertEquals(resultList.size(),realmAdapter.getRealmResults().size());
    }

    public void testSortWithAdapter() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll()
                .sort(FIELD_STRING, SORT_ORDER_DECENDING);
        RealmAdapter realmAdapter = new RealmAdapter(getContext(), resultList, automaticUpdate);
        assertEquals(resultList.first(), realmAdapter.getRealmResults().first());
        assertEquals(resultList.size(), realmAdapter.getRealmResults().size());

        realmAdapter.updateRealmResults(realmAdapter.getRealmResults()
                .sort(FIELD_STRING));
        assertEquals(resultList.first(), realmAdapter.getRealmResults().first());
        assertEquals(resultList.size(), realmAdapter.getRealmResults().size());

    }

    public void testGetItem() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmAdapter realmAdapter = new RealmAdapter(getContext(), resultList, automaticUpdate);

        assertEquals(resultList.get(0),realmAdapter.getItem(0));
        assertEquals(resultList.size(), realmAdapter.getRealmResults().size());
        assertEquals(resultList.last(), realmAdapter.getRealmResults().last());
    }

    public void testGetItemId() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmAdapter realmAdapter = new RealmAdapter(getContext(), resultList, automaticUpdate);
        for (int i = 0; i < resultList.size(); i++) {
            assertEquals(i, realmAdapter.getItemId(i));
        }
    }

    public void testGetCount() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmAdapter realmAdapter = new RealmAdapter(getContext(), resultList, automaticUpdate);
        assertEquals(47, realmAdapter.getCount());
    }

    public void testGetView() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmAdapter realmAdapter = new RealmAdapter(getContext(), resultList, automaticUpdate);
        View view = realmAdapter.getView(0, null, null);

        TextView name = (TextView) view.findViewById(android.R.id.text1);

        assertNotNull(view);
        assertNotNull(name);
        assertEquals(resultList.get(0).getColumnString(), name.getText());
    }
}
