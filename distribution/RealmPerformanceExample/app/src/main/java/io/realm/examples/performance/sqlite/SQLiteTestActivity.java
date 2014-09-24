package io.realm.examples.performance.sqlite;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.realm.examples.performance.Globals;
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

        android.database.sqlite.SQLiteDatabase db = database.getWritableDatabase();
        ContentValues values = new ContentValues();

        db.beginTransaction();
        for(int row=0;row< Globals.NUM_INSERTS; row++) {
            values.put(database.COLUMN_NAME, Globals.getName(row));
            values.put(database.COLUMN_AGE, Globals.getAge(row));
            values.put(database.COLUMN_HIRED, Globals.getHired(row));
            db.insert(database.TABLE_EMPLOYEES, null, values);
        }
        db.endTransaction();

        return "testInserts " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    private String testQueries() {
        long startTime = System.currentTimeMillis();

        android.database.sqlite.SQLiteDatabase db = database.getWritableDatabase();
        String query = "SELECT * FROM "
                + EmployeeDatabaseHelper.TABLE_EMPLOYEES
                + " WHERE name = 'Foo0' AND age >= 20 AND age <= 50 AND hired = 0";
        Cursor cursor = db.rawQuery(query, null);
        cursor.getCount();
        db.close();

        return "testQueries " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    private String testCounts() {
        long startTime = System.currentTimeMillis();

        android.database.sqlite.SQLiteDatabase db = database.getWritableDatabase();
        String count = "SELECT count(*) FROM "
                + EmployeeDatabaseHelper.TABLE_EMPLOYEES
                + " WHERE name = 'Foo0' AND age >= 20 AND age <= 50 AND hired = 0";
        Cursor mcursor = db.rawQuery(count, null);
        mcursor.moveToFirst();
        mcursor.getInt(0);

        return "testCounts " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    private void showStatus(String txt) {
        Log.i(TAG, txt);
        TextView tv = new TextView(this);
        tv.setText(txt);
        rootLayout.addView(tv);
    }
}
