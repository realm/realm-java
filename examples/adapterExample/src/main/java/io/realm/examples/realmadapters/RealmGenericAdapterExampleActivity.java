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

package io.realm.examples.realmadapters;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.examples.realmadapters.adapters.RealmGenericExampleAdapter;
import io.realm.examples.realmadapters.model.SimpleRecord;

public class RealmGenericAdapterExampleActivity extends Activity implements View.OnClickListener {

    @SuppressWarnings("UnusedDeclaration")
    public static final String TAG = RealmGenericAdapterExampleActivity.class.getName();

    private Realm realm = null;

    private RealmGenericExampleAdapter<SimpleRecord> mAdapter = null;
    private ListView mListView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_example);

        findViewById(R.id.insert_record_button).setOnClickListener(this);

        mListView = (ListView)findViewById(R.id.items_list);

        // Acquire a realm object
        realm = Realm.getInstance(this);

        RealmResults<SimpleRecord> rList = realm.where(SimpleRecord.class).findAll();

        mAdapter = new RealmGenericExampleAdapter<SimpleRecord>(this, R.layout.simplelistitem, rList, true);
        mListView.setAdapter(mAdapter);

        realm.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    // Using the screen form the user can inject into the Realm
    // even if other threads are creating transactions.
    @Override
    public void onClick(View v) {
        realm.beginTransaction();
        SimpleRecord record = realm.createObject(SimpleRecord.class);
        record.setDescriptor("Record: " + new Date());
        realm.commitTransaction();
    }
}
