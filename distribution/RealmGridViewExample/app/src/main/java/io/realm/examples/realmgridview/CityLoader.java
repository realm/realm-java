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

        //Store the retrieved items to the Realm
        try {
            Realm realm = new Realm(context.getFilesDir());
            realm.clear(); //Delete the Realm (in the event of a Reset)

            realm.beginWrite();
            for (City city : items) {
                City realmCity = realm.create(City.class);
                realmCity.setName(city.getName());
                realmCity.setVotes(city.getVotes());
            }
            realm.commit();
        } catch (IOException ioe) {
            Log.e(TAG, "Realm failed to commit the retrieved info from JSON");
        }

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