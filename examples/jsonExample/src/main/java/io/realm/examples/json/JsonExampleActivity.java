/*
 * Copyright 2018 Realm Inc.
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
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * This example demonstrates how to import RealmObjects as JSON. Realm supports JSON represented
 * as Strings, JSONObject, JSONArray or InputStreams (from API 11+)
 */
public class JsonExampleActivity extends Activity {

    private GridView gridView;
    private CityAdapter adapter;

    private Realm realm;
    private RealmResults<City> cities;
    private RealmChangeListener<RealmResults<City>> realmChangeListener = (cities) -> {
        adapter.setData(cities);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_example);

        Realm.deleteRealm(Realm.getDefaultConfiguration());

        realm = Realm.getDefaultInstance();

        gridView = findViewById(R.id.cities_list);

        cities = realm.where(City.class).findAllAsync();
        cities.addChangeListener(realmChangeListener);

        adapter = new CityAdapter();
        gridView.setAdapter(adapter);

        // Load from file "cities.json" first time
        loadCities();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        cities.removeAllChangeListeners();
        realm.close();
    }

    public void loadCities() {
        try {
            loadJsonFromStream();
            loadJsonFromJsonObject();
            loadJsonFromString();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadJsonFromStream() throws IOException {
        // Use streams if you are worried about the size of the JSON whether it was persisted on disk
        // or received from the network.
        try(InputStream stream = getAssets().open("cities.json")) {
            try {
                // Open a transaction to store items into the realm
                realm.beginTransaction();
                realm.createAllFromJson(City.class, stream);
                realm.commitTransaction();
            } catch (IOException e) {
                // Remember to cancel the transaction if anything goes wrong.
                if(realm.isInTransaction()) {
                    realm.cancelTransaction();
                }
                throw new RuntimeException(e);
            }
        }
    }

    private void loadJsonFromJsonObject() {
        Map<String, String> city = new HashMap<String, String>();
        city.put("name", "København");
        city.put("votes", "9");
        final JSONObject json = new JSONObject(city);

        realm.executeTransaction(realm -> realm.createObjectFromJson(City.class, json));
    }

    private void loadJsonFromString() {
        final String json = "{ name: \"Aarhus\", votes: 99 }";

        realm.executeTransaction(realm -> realm.createObjectFromJson(City.class, json));
    }
}
