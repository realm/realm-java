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
import io.realm.examples.performance.R;
import io.realm.examples.performance.model.Employee;
import io.realm.examples.performance.sqlite.EmployeeDatabaseHelper;


public class RealmTestActivity extends Activity {

    public static final String TAG = RealmTestActivity.class.getName();

    private LinearLayout rootLayout = null;

    private EmployeeDatabaseHelper database = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_example);

        rootLayout = ((LinearLayout) findViewById(R.id.container));
        rootLayout.removeAllViews();

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

    public static final int NUM_TESTS = 150000;

    private String testInserts() {
        long startTime = System.currentTimeMillis();

        try {
            Realm realm = new Realm(getFilesDir());
            realm.beginWrite();
            for(int i = 0; i<NUM_TESTS; i++) {
                Employee employee = realm.create(Employee.class);
                employee.setName("Name");
                employee.setAge(14);
                employee.setHired(1);
            }
            realm.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "testInserts " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    private String testQueries() {
        long startTime = System.currentTimeMillis();
        try {
            Realm realm = new Realm(getFilesDir());
            List<Employee> results
                    = realm.where(Employee.class)
                    .equalTo("hired", 1)
                    .between("age", 500, 50000)
                    .equalTo("name", "Name").findAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "testQueries " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    private String testCounts() {
        long startTime = System.currentTimeMillis();

        try {
            Realm realm = new Realm(getFilesDir());
            List<Employee> results
                    = realm.where(Employee.class)
                    .equalTo("hired", 1)
                    .between("age", 500, 50000)
                    .equalTo("name", "Name").findAll();
            results.size();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "testCounts " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    private void showStatus(String txt) {
        Log.i(TAG, txt);
        TextView tv = new TextView(this);
        tv.setText(txt);
        rootLayout.addView(tv);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.realm_example, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
