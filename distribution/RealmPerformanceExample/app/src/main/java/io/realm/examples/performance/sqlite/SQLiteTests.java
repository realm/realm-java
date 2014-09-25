package io.realm.examples.performance.sqlite;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;

import io.realm.examples.performance.PerformanceTest;

public class SQLiteTests extends PerformanceTest {

    private EmployeeDatabaseHelper databaseHelper = null;

    public void setActivity(Activity activity) {
        databaseHelper = new EmployeeDatabaseHelper(activity);
    }

    public void clearDevice() {
        databaseHelper.onUpgrade(databaseHelper.getWritableDatabase(), 2, 3);
    }

    public String testInserts() {
        android.database.sqlite.SQLiteDatabase db = databaseHelper.getWritableDatabase();

        long startTime = System.currentTimeMillis();

        ContentValues values = new ContentValues();
        db.beginTransaction();
        for(int row=0;row < getNumInserts(); row++) {
            values.put(databaseHelper.COLUMN_NAME, getName(row));
            values.put(databaseHelper.COLUMN_AGE, getAge(row));
            values.put(databaseHelper.COLUMN_HIRED, getHired(row));
            db.insert(databaseHelper.TABLE_EMPLOYEES, null, values);
        }
        db.setTransactionSuccessful();
        db.endTransaction();

        String status = "testInserts " + (System.currentTimeMillis() - startTime) + " ms.\n";

        //Verify writes were successful
        String query = "SELECT * FROM " + EmployeeDatabaseHelper.TABLE_EMPLOYEES;
        Cursor cursor = db.rawQuery(query, null);
        cursor.getCount();

        status += "...Completed " + cursor.getCount() + " inserts\n";

        db.close();

        return status;
    }

    public String testQueries() {
        android.database.sqlite.SQLiteDatabase db = databaseHelper.getWritableDatabase();

        //Skip the first as a "warmup"
        String query = QUERY1;
        Cursor cursor = db.rawQuery(query, null);
        loopCursor(cursor);
        cursor.close();

        long startTime = System.currentTimeMillis();

        query = QUERY2;
        cursor = db.rawQuery(query, null);
        loopCursor(cursor);
        cursor.close();

        query = QUERY3;
        cursor = db.rawQuery(query, null);
        loopCursor(cursor);
        cursor.close();

        query = QUERY4;
        cursor = db.rawQuery(query, null);
        loopCursor(cursor);
        cursor.close();

        query = QUERY5;
        cursor = db.rawQuery(query, null);
        loopCursor(cursor);
        cursor.close();
        db.close();

        return "testQueries " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    private void loopCursor(Cursor cursor) {
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            cursor.moveToNext();
        }
    }

    public String testCounts() {
        android.database.sqlite.SQLiteDatabase db = databaseHelper.getWritableDatabase();

        //Skip the first as a "warmup"
        Cursor cursor = db.rawQuery(COUNT_QUERY1, null);
        cursor.moveToFirst();
        String status = "...Count Acquired: " + cursor.getInt(0) + " inserts\n";
        cursor.close();

        long startTime = System.currentTimeMillis();

        cursor = db.rawQuery(COUNT_QUERY2, null);
        cursor.moveToFirst();
        status += "...Count Acquired: " + cursor.getInt(0) + " inserts\n";
        cursor.close();

        cursor = db.rawQuery(COUNT_QUERY3, null);
        cursor.moveToFirst();
        status += "...Count Acquired: " + cursor.getInt(0) + " inserts\n";
        cursor.close();

        cursor = db.rawQuery(COUNT_QUERY4, null);
        cursor.moveToFirst();
        status += "...Count Acquired: " + cursor.getInt(0) + " inserts\n";
        cursor.close();

        cursor = db.rawQuery(COUNT_QUERY5, null);
        cursor.moveToFirst();
        status += "...Count Acquired: " + cursor.getInt(0) + " inserts\n";
        cursor.close();
        db.close();

        status += "testCounts " + (System.currentTimeMillis() - startTime) + " ms.\n";
        return status;
    }
}
