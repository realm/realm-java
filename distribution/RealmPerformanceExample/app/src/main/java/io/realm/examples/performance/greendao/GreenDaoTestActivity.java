package io.realm.examples.performance.greendao;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import io.realm.examples.performance.R;

public class GreenDaoTestActivity extends Activity {

    public static final String TAG = GreenDaoTestActivity.class.getName();

    private LinearLayout rootLayout = null;

//    private DaoMaster daoMaster;
//    private DaoSession daoSession;
//    private EmployeeDao employeeDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_example);

//        DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "notes-db", null);
//        db = helper.getWritableDatabase();
//        daoMaster   = new DaoMaster(db);
//        daoSession  = daoMaster.newSession();
//        employeeDao = daoSession.getNoteDao();

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

        String ret = "";
        try {

            ret += "testInserts " + (System.currentTimeMillis() - startTime) + " ms.\n";
        } catch(Exception e) {}

        try {

            ret += "Completed "  + " inserts\n";
        } catch(Exception e) {}
        return ret;
    }

    private String testQueries() {
        long startTime = System.currentTimeMillis();
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "testQueries " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    private String testCounts() {
        long startTime = System.currentTimeMillis();

        try {

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
