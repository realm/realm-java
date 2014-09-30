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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class RealmExampleActivity extends Activity implements AdapterView.OnItemClickListener {

    public static final String TAG = RealmExampleActivity.class.getName();

    private GridView mGrid;
    private CityAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_example);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(mAdapter == null) {
            List<City> cities = loadCities();
            mAdapter = new CityAdapter(this);
            mAdapter.setData(cities);

            mGrid = (GridView) findViewById(R.id.cities_list);
            mGrid.setAdapter(mAdapter);
            mGrid.setOnItemClickListener(RealmExampleActivity.this);
            mAdapter.notifyDataSetChanged();
            mGrid.invalidate();
        }
    }

    public List<City> loadCities() {
        List<City> items = new ArrayList<City>();

        //In this case we're loading from local assets.
        //NOTE: could alternatively easily load from network
        InputStream stream = null;
        try {
            stream = getAssets().open("cities.json");
        } catch (Exception e) {
            return null;
        }

        JsonParser parser = new JsonParser();
        JsonArray array = parser.parse(new InputStreamReader(stream)).getAsJsonArray();

        // Clear the realm from last time
        Realm.deleteRealmFile(this);

        // Store the retrieved items to the Realm
        Realm realm = Realm.getInstance(this);

        realm.beginTransaction();
        for (JsonElement e : array) {
            City realmCity = realm.createObject(City.class);
            realmCity.setName(e.getAsJsonObject().get("name").getAsString());
            realmCity.setVotes(e.getAsJsonObject().get("votes").getAsInt());
            items.add(realmCity);
        }
        realm.commitTransaction();

        return items;
    }

    public void updateCities() {
        Realm realm = Realm.getInstance(this);
        RealmResults<City> cities = realm.where(City.class).findAll();
        mAdapter.setData(cities);
        mAdapter.notifyDataSetChanged();
        mGrid.invalidate();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        City modifiedCity = (City)mAdapter.getItem(position);

        //Update the realm object affected by the user
        Realm realm = Realm.getInstance(this);
        City city = realm.where(City.class).equalTo("name", modifiedCity.getName()).findFirst();

        realm.beginTransaction();
        city.setVotes(city.getVotes() + 1);
        realm.commitTransaction();

        updateCities();
    }
}
