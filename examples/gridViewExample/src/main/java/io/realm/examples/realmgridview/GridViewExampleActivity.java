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
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class GridViewExampleActivity extends Activity implements AdapterView.OnItemClickListener {

    private GridView mGridView;
    private CityAdapter mAdapter;

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_example);

        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this).build();

        // Clear the realm from last time
        Realm.deleteRealm(realmConfiguration);

        // Create a new empty instance of Realm
        realm = Realm.getInstance(realmConfiguration);
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
            mGridView.setOnItemClickListener(GridViewExampleActivity.this);
            mAdapter.notifyDataSetChanged();
            mGridView.invalidate();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close(); // Remember to close Realm when done.
    }

    private List<City> loadCities() {
        // In this case we're loading from local assets.
        // NOTE: could alternatively easily load from network
        InputStream stream;
        try {
            stream = getAssets().open("cities.json");
        } catch (IOException e) {
            return null;
        }

        // GSON can parse the data.
        // Note there is a bug in GSON 2.3.1 that can cause it to StackOverflow when working with RealmObjects.
        // To work around this, use the ExclusionStrategy below or downgrade to 1.7.1
        // See more here: https://code.google.com/p/google-gson/issues/detail?id=440
        Gson gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create();

        JsonElement json = new JsonParser().parse(new InputStreamReader(stream));
        List<City> cities = gson.fromJson(json, new TypeToken<List<City>>() {}.getType());

        // Open a transaction to store items into the realm
        // Use copyToRealm() to convert the objects into proper RealmObjects managed by Realm.
        realm.beginTransaction();
        Collection<City> realmCities = realm.copyToRealm(cities);
        realm.commitTransaction();

        return new ArrayList<City>(realmCities);
    }

    public void updateCities() {
        Realm realm = Realm.getInstance(this);

        // Pull all the cities from the realm
        RealmResults<City> cities = realm.where(City.class).findAll();

        // Put these items in the Adapter
        mAdapter.setData(cities);
        mAdapter.notifyDataSetChanged();
        mGridView.invalidate();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        City modifiedCity = (City)mAdapter.getItem(position);

        // Update the realm object affected by the user
        Realm realm = Realm.getInstance(this);

        // Acquire the list of realm cities matching the name of the clicked City.
        City city = realm.where(City.class).equalTo("name", modifiedCity.getName()).findFirst();

        // Create a transaction to increment the vote count for the selected City in the realm
        realm.beginTransaction();
        city.setVotes(city.getVotes() + 1);
        realm.commitTransaction();

        updateCities();
    }
}
