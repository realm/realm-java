package io.realm.examples.performance.sqlite;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;

import io.realm.examples.performance.PerformanceTest;
import io.realm.examples.performance.PerformanceTestException;

public class SQLiteTests extends PerformanceTest {

    private EmployeeDatabaseHelper databaseHelper = null;
    private android.database.sqlite.SQLiteDatabase db = null;

    public SQLiteTests() {
        testName = "SQLite";
    }

    public void clearDatabase() throws PerformanceTestException {
        databaseHelper.onUpgrade(databaseHelper.getWritableDatabase(), 2, 3);
    }

    public void testBootstrap() throws PerformanceTestException {
        databaseHelper = new EmployeeDatabaseHelper(getActivity());
        db = databaseHelper.getWritableDatabase();
        databaseHelper.onCreate(db);
    }

    public void testInserts() throws PerformanceTestException {
        ContentValues values = new ContentValues();
        db.beginTransaction();
        for (int row = 0; row < getNumInserts(); row++) {
            values.put(databaseHelper.COLUMN_NAME, getEmployeeName(row));
            values.put(databaseHelper.COLUMN_AGE, getEmployeeAge(row));
            values.put(databaseHelper.COLUMN_HIRED, getEmployeeHiredStatus(row));
            db.insert(databaseHelper.TABLE_EMPLOYEES, null, values);
        }
        db.setTransactionSuccessful();
        db.endTransaction();

        //Verify writes were successful
        String query = "SELECT * FROM " + EmployeeDatabaseHelper.TABLE_EMPLOYEES;
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.getCount() < getNumInserts()) {
            throw new PerformanceTestException();
        }

        db.close();
    }

    public void testQueries() throws PerformanceTestException {
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
    }

    private void loopCursor(Cursor cursor) {
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            cursor.moveToNext();
        }
    }

    public void testCounts() throws PerformanceTestException {
        android.database.sqlite.SQLiteDatabase db = databaseHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery(COUNT_QUERY1, null);
        cursor.moveToFirst();
        cursor.close();

        cursor = db.rawQuery(COUNT_QUERY2, null);
        cursor.moveToFirst();
        cursor.close();

        cursor = db.rawQuery(COUNT_QUERY3, null);
        cursor.moveToFirst();
        cursor.close();

        cursor = db.rawQuery(COUNT_QUERY4, null);
        cursor.moveToFirst();
        cursor.close();

        cursor = db.rawQuery(COUNT_QUERY5, null);
        cursor.moveToFirst();
        cursor.close();
        db.close();
    }
}
