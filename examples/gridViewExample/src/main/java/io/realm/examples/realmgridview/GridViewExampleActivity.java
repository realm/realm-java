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

package io.realm.examples.realmgridview;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class GridViewExampleActivity extends Activity implements AdapterView.OnItemClickListener {

    private GridView gridView;
    private CityAdapter adapter;

    private Realm realm;
    private RealmResults<City> cities;
    private RealmChangeListener<RealmResults<City>> realmChangeListener = cities -> {
        // Set the cities to the adapter only when async query is loaded.
        // It will also be called for any future writes made to the Realm.
        adapter.setData(cities);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_example);

        // This is the GridView adapter
        adapter = new CityAdapter();

        //This is the GridView which will display the list of cities
        gridView = findViewById(R.id.cities_list);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(GridViewExampleActivity.this);

        // Clear the realm from last time
        //noinspection ConstantConditions
        Realm.deleteRealm(Realm.getDefaultConfiguration());

        // Create a new empty instance of Realm
        realm = Realm.getDefaultInstance();

        // Obtain the cities in the Realm with asynchronous query.
        cities = realm.where(City.class).findAllAsync();

        // The RealmChangeListener will be called when the results are asynchronously loaded, and available for use.
        cities.addChangeListener(realmChangeListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cities.removeAllChangeListeners(); // Remove change listeners to prevent updating views not yet GCed.
        realm.close(); // Remember to close Realm when done.
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        City modifiedCity = adapter.getItem(position);

        // Acquire the name of the clicked City, in order to be able to query for it.
        final String name = modifiedCity.getName();

        // Create an asynchronous transaction to increment the vote count for the selected City in the Realm.
        // The write will happen on a background thread, and the RealmChangeListener will update the GridView automatically.
        realm.executeTransactionAsync(bgRealm -> {
            // We need to find the City we want to modify from the background thread's Realm
            City city = bgRealm.where(City.class).equalTo("name", name).findFirst();
            if (city != null) {
                // Let's increase the votes of the selected city!
                city.setVotes(city.getVotes() + 1);
            }
        });
    }
}
