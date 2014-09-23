package io.realm.examples.performance.sugarorm;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.orm.query.Condition;
import com.orm.query.Select;

import io.realm.examples.performance.R;
import io.realm.examples.performance.model.SugarEmployee;


public class SugarORMTestActivity extends Activity {

    public static final String TAG = SugarORMTestActivity.class.getName();

    private LinearLayout rootLayout = null;

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
            for (int i = 0; i < NUM_TESTS; i++) {
                SugarEmployee book = new SugarEmployee(SugarORMTestActivity.this, "Name", i, 1);
                book.save();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "testInserts " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    private String testQueries() {
        long startTime = System.currentTimeMillis();
        try {
            Select outcome = Select.from(SugarEmployee.class)
                    .where(Condition.prop("name").eq("Name"),
                            Condition.prop("age").gt(500).lt(50000),
                            Condition.prop("hired").eq(1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "testQueries " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    private String testCounts() {
        long startTime = System.currentTimeMillis();

        try {
            Select outcome = Select.from(SugarEmployee.class)
                    .where(Condition.prop("name").eq("Name"),
                            Condition.prop("age").gt(500).lt(50000),
                            Condition.prop("hired").eq(1));
            outcome.list().size();
            //SugarEmployee.count(SugarEmployee.class, "WHERE hired = 1 AND age >= 500 AND age <= 50000 AND name = 'Name'", null);
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
