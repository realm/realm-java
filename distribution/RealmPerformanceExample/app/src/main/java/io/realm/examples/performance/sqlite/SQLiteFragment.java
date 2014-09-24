package io.realm.examples.performance.sqlite;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;

import io.realm.examples.performance.PerformanceTestFragment;

public class SQLiteFragment extends PerformanceTestFragment {

    public static final String TAG = SQLiteFragment.class.getName();

    private EmployeeDatabaseHelper databaseHelper = null;

    public static SQLiteFragment newInstance() {
        SQLiteFragment fragment = new SQLiteFragment();
        return fragment;
    }

    public SQLiteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        databaseHelper = new EmployeeDatabaseHelper(activity);
    }

    public String testInserts() {
        android.database.sqlite.SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        long startTime = System.currentTimeMillis();

        db.beginTransaction();
        for(int row=0;row < NUM_INSERTS; row++) {
            values.put(databaseHelper.COLUMN_NAME, getName(row));
            values.put(databaseHelper.COLUMN_AGE, getAge(row));
            values.put(databaseHelper.COLUMN_HIRED, getHired(row));
            db.insert(databaseHelper.TABLE_EMPLOYEES, null, values);
        }
        db.endTransaction();

        return "testInserts " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    public String testQueries() {
        android.database.sqlite.SQLiteDatabase db = databaseHelper.getWritableDatabase();

        long startTime = System.currentTimeMillis();

        String query = "SELECT * FROM "
                + EmployeeDatabaseHelper.TABLE_EMPLOYEES
                + " WHERE name = 'Foo0' AND age >= 20 AND age <= 50 AND hired = 0";
        Cursor cursor = db.rawQuery(query, null);
        cursor.getCount();
        db.close();

        return "testQueries " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    public String testCounts() {
        android.database.sqlite.SQLiteDatabase db = databaseHelper.getWritableDatabase();

        long startTime = System.currentTimeMillis();

        String count = "SELECT count(*) FROM "
                + EmployeeDatabaseHelper.TABLE_EMPLOYEES
                + " WHERE name = 'Foo0' AND age >= 20 AND age <= 50 AND hired = 0";
        Cursor mcursor = db.rawQuery(count, null);
        mcursor.moveToFirst();
        mcursor.getInt(0);

        return "testCounts " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }
}
