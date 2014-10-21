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

package io.realm.examples.realmgridview;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.realm.Realm;

public class RealmJsonExampleActivity extends Activity {

    public static final String TAG = RealmJsonExampleActivity.class.getName();

    private GridView mGridView;
    private CityAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_example);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Load from file "cities.json" first time
        if(mAdapter == null) {
            List<City> cities = loadCities();

            //This is the GridView adapter
            mAdapter = new CityAdapter(this);
            mAdapter.setData(cities);

            //This is the GridView which will display the list of cities
            mGridView = (GridView) findViewById(R.id.cities_list);
            mGridView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            mGridView.invalidate();
        }
    }

    public List<City> loadCities() {

        // In this case we're loading from local assets.
        // NOTE: could alternatively easily load from network
        InputStream stream = null;
        try {
            stream = getAssets().open("cities.json");
        } catch (IOException e) {
            return null;
        }

        Realm.deleteRealmFile(this);

        // Store the retrieved items to the Realm
        Realm realm = Realm.getInstance(this);

        // Open a transaction to store items into the realm
        try {
            realm.beginTransaction();
            realm.createAllFromJson(City.class, stream);
            realm.commitTransaction();
            stream.close();
        } catch (IOException e) {
            // Ignore
        }

        return realm.allObjects(City.class);
    }
}
