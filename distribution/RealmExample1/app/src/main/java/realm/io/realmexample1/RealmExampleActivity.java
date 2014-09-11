package realm.io.realmexample1;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmTableOrViewList;

public class RealmExampleActivity extends Activity implements LoaderManager.LoaderCallbacks<List<City>>, AdapterView.OnItemClickListener {

    public static final String TAG = RealmExampleActivity.class.getName();

    private GridView    mGrid;
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
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    /*** Loader Callback Methods ***/

    @Override
    public Loader<List<City>> onCreateLoader(int id, Bundle args) {
        return new CityLoader(this, null);
    }

    //TODO:  Add onSavedInstance handling and context switch handling

    @Override
    public void onLoadFinished(Loader<List<City>> loader, List<City> response) {
        if(response == null) {
            Log.e(TAG, "Loader did not acquire any useful information");
        } else {
            mAdapter = new CityAdapter(this);
            mGrid = (GridView)findViewById(R.id.cities_list);
            mGrid.setAdapter(mAdapter);
            mGrid.setOnItemClickListener(RealmExampleActivity.this);

            //Pull the acquired JSON information from the Realm
            updateCities();
        }
    }

    public void updateCities() {
        try {
            //For performance reasons you may not want to requery
            //the Realm for each onscreen update in your "real app".
            Realm r = new Realm(getFilesDir());
            RealmTableOrViewList<City> rList = r.where(City.class).findAll();
            ArrayList cities = new ArrayList<City>();
            for(City c : rList) {
                Log.d(TAG, "CITY ACQUIRED: " + c.getName() + "," + c.getVotes());
                cities.add(c);
            }
            mAdapter.setData(cities);
        } catch(IOException ioe) {
            Log.e(TAG, "Realm failed to retrieve and update the User list");
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<City>> loader) {

    }

    /*** Options Menu ***/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_reload) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*** Listeners ***/

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        City modifiedCity = (City)mAdapter.getItem(position);

        try {
            //Update the realm object affected by the user
            Realm r = new Realm(RealmExampleActivity.this.getFilesDir());
            RealmQuery<City> q = r.where(City.class).beginsWith("name", modifiedCity.getName());
            City realmCity = q.findFirst();
            r.beginWrite();
            realmCity.setVotes(realmCity.getVotes()+1);
            r.commit();
        } catch(IOException ioe) {
            Log.e(TAG, "Realm failed to store a user vote");
        }

        updateCities();
    }
}
