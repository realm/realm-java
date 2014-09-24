package io.realm.examples.performance.realm;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import io.realm.Realm;
import io.realm.examples.performance.Globals;
import io.realm.examples.performance.R;
import io.realm.examples.performance.sqlite.EmployeeDatabaseHelper;


public class RealmTestActivity extends Activity {

    public static final String TAG = RealmTestActivity.class.getName();

    private LinearLayout rootLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_example);

        rootLayout = ((LinearLayout) findViewById(R.id.container));
        rootLayout.removeAllViews();

        Globals.initNames();

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String results = "";
                results += testInserts();
                results += testQueries();
                results += testCounts();
                return results;
            }

            @Override
            protected void onPostExecute(String results) {
                showStatus(results);
            }
        }.execute();
    }

    private String testInserts() {
        long startTime = System.currentTimeMillis();

        Realm realm = new Realm(getFilesDir());
        realm.beginWrite();
        for(int row = 0; row < Globals.NUM_INSERTS; row++) {
            Employee employee = realm.create(Employee.class);
            employee.setName(Globals.getName(row));
            employee.setAge(Globals.getAge(row));
            employee.setHired(Globals.getHired(row));
        }
        realm.commit();

        return "testInserts " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    private String testQueries() {
        long startTime = System.currentTimeMillis();

        Realm realm = new Realm(getFilesDir());
        List<Employee> results
                = realm.where(Employee.class)
                .equalTo("hired", 0)
                .between("age", 20, 50)
                .equalTo("name", "Foo0").findAll();

        return "testQueries " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    private String testCounts() {
        long startTime = System.currentTimeMillis();

        Realm realm = new Realm(getFilesDir());
        List<Employee> results
                = realm.where(Employee.class)
                .equalTo("hired", 0)
                .between("age", 20, 50)
                .equalTo("name", "Foo0").findAll();
        results.size();

        return "testCounts " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    private void showStatus(String txt) {
        Log.i(TAG, txt);
        TextView tv = new TextView(this);
        tv.setText(txt);
        rootLayout.addView(tv);
    }
}
