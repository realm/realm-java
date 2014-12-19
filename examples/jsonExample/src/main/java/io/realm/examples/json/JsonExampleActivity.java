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

package io.realm.examples.json;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;

public class JsonExampleActivity extends Activity {

    public static final String TAG = JsonExampleActivity.class.getName();

    private GridView mGridView;
    private CityAdapter mAdapter;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_example);

        Realm.deleteRealmFile(this);
        realm = Realm.getInstance(this);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    public List<City> loadCities() {

        loadJsonFromStream();
        loadJsonFromJsonObject();
        loadJsonFromString();

        return realm.allObjects(City.class);
    }

    private void loadJsonFromStream() {
        // Use streams if you are worried about the size of the JSON whether it was persisted on disk
        // or received from the network.
        InputStream stream = null;
        try {
            stream = getAssets().open("cities.json");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Open a transaction to store items into the realm
        realm.beginTransaction();
        try {
            realm.createAllFromJson(City.class, stream);
            realm.commitTransaction();
        } catch (IOException e) {
            // Remember to cancel the transaction if anything goes wrong.
            realm.cancelTransaction();
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException ignore) {
                // Ignore
            }
        }
    }

    private void loadJsonFromJsonObject() {
        Map<String, String> city = new HashMap<String, String>();
        city.put("name", "København");
        city.put("votes", "9");
        JSONObject json = new JSONObject(city);

        realm.beginTransaction();
        realm.createObjectFromJson(City.class, json);
        realm.commitTransaction();
    }

    private void loadJsonFromString() {
        String json = "{ city: \"Aarhus\", votes: 99 }";

        realm.beginTransaction();
        realm.createObjectFromJson(City.class, json);
        realm.commitTransaction();
    }
}
