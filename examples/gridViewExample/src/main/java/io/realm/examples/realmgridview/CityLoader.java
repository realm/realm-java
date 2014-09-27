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

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import io.realm.Realm;

public class CityLoader extends AsyncTaskLoader<List<City>> {

    public static final String TAG = CityLoader.class.getName();

    private Context context = null;
    private Bundle args = null;

    //Added this field to make Loaders work under support package.
    private boolean dataIsReady = false;

    public CityLoader(final Context context, Bundle bundle) {
        super(context);

        this.context = context;
        this.args = bundle;
    }

    //Added this method to make loaders from support package work
    @Override
    public void onStartLoading() {
        if (dataIsReady) {
            deliverResult(null);
        } else {
            forceLoad();
        }
    }

    @Override
    public List<City> loadInBackground() throws RuntimeException {
        List<City> items = loadCities();

        if (items == null) {
            Log.e(TAG, "No cities found");
            return null;
        }

        dataIsReady = true;
        
        // Delete previous database in case it's there from a previous run.
        Realm.deleteRealmFile(context);

        // Store the retrieved items to the Realm
        Realm realm = Realm.getInstance(context);

        realm.beginTransaction();
        for (City city : items) {
            City realmCity = realm.createObject(City.class);
            realmCity.setName(city.getName());
            realmCity.setVotes(city.getVotes());
        }
        realm.commitTransaction();

        return items;
    }

    public Gson getGson() {
        return new Gson();
    }

    public List<City> loadCities() {
        List<City> items = null;

        //In this case we're loading from local assets.
        //NOTE: could alternatively easily load from network

        try {
            InputStream stream = null;
            stream = context.getAssets().open("cities.json");
            items = getGson().fromJson(new InputStreamReader(stream), new TypeToken<List<City>>() {
            }.getType());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            Log.e(TAG, "Could not load cities information");
        }

        return items;
    }
}
