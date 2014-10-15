package io.realm.examples.realmadapters;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.examples.realmadapters.models.TimeStamp;


public class MyActivity extends Activity {

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        Realm.deleteRealmFile(this);
        realm = Realm.getInstance(this);
        RealmResults<TimeStamp> timeStamps = realm.where(TimeStamp.class).findAll();
        final MyAdapter adapter = new MyAdapter(this, R.id.listView, timeStamps, true);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    realm.beginTransaction();
                    adapter.getRealmResults().remove(i);
                    realm.commitTransaction();
                    return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            realm.beginTransaction();
            TimeStamp timeStamp = realm.createObject(TimeStamp.class);
            timeStamp.setTimeStamp(Long.toString(System.currentTimeMillis()));
            realm.commitTransaction();
        }
        return true;
    }
}
