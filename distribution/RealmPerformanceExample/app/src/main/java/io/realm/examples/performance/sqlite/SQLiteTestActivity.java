package io.realm.examples.performance.sqlite;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.realm.examples.performance.R;


public class SQLiteTestActivity extends Activity {

    public static final String TAG = SQLiteTestActivity.class.getName();

    private LinearLayout rootLayout = null;

    private EmployeeDatabaseHelper database = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_example);

        database = new EmployeeDatabaseHelper(this);

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
            android.database.sqlite.SQLiteDatabase db = database.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(database.COLUMN_NAME, "Name");
            values.put(database.COLUMN_AGE, 100);
            values.put(database.COLUMN_HIRED, 1);
            for(int i=0; i< NUM_TESTS; i++) {
                db.insert(database.TABLE_EMPLOYEES, null, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "testInserts " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    private String testQueries() {
        long startTime = System.currentTimeMillis();
        try {
            android.database.sqlite.SQLiteDatabase db = database.getWritableDatabase();
            String query = "SELECT * FROM "
                    + EmployeeDatabaseHelper.TABLE_EMPLOYEES
                    + " WHERE name = 'Name' AND age >= 500 AND age <= 50000 AND hired = 1";
            Cursor cursor = db.rawQuery(query, null);
            cursor.getCount();
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "testQueries " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    private String testCounts() {
        long startTime = System.currentTimeMillis();

        try {
            android.database.sqlite.SQLiteDatabase db = database.getWritableDatabase();
            String count = "SELECT count(*) FROM "
                    + EmployeeDatabaseHelper.TABLE_EMPLOYEES
                    + " WHERE name = 'Name' AND age >= 500 AND age <= 50000 AND hired = 1";
            Cursor mcursor = db.rawQuery(count, null);
            mcursor.moveToFirst();
            mcursor.getInt(0);
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
