package io.realm.examples.performance.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import io.realm.Realm;
import io.realm.examples.performance.PerformanceTest;

public class GreenDAOTests extends PerformanceTest {

    private Realm realm = null;

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;

    public GreenDAOTests() {
        testName = "GreenDAO";
    }

    public void clearDevice() {

    }

    public String testInserts() {
        DaoMaster.DevOpenHelper helper
                = new DaoMaster.DevOpenHelper(getActivity(), "Employee-db", null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);

        long startTime = System.currentTimeMillis();

        daoSession = daoMaster.newSession();
        EmployeeDao employeeDao = daoSession.getEmployeeDao();

        for(int row=0; row < getNumInserts(); row++) {
            Employee employee = new Employee();
            employee.setName(getEmployeeName(row));
            employee.setAge(getEmployeeAge(row));
            employee.setHired(getHiredBool(row));
            employeeDao.insert(employee);
        }

        long duration = (System.currentTimeMillis() - startTime);
        String status = "testInserts " + duration + " ms.";

        Cursor cursor = db.query(employeeDao.getTablename(),
                employeeDao.getAllColumns(), null, null, null, null, null);

        status += "...Found " + cursor.getCount() + " inserts\n";
        timings.put("testInserts", (getNumInserts() / (double)duration));

        return status;
    }

    public String testQueries() {
        DaoMaster.DevOpenHelper helper
                = new DaoMaster.DevOpenHelper(getActivity(), "Employee-db", null);
        db = helper.getWritableDatabase();

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

        long duration = (System.currentTimeMillis() - startTime);
        timings.put("testQueries", (4 / (double)duration));

        return "testQueries " + duration + " ms.";
    }

    private void loopCursor(Cursor cursor) {
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            cursor.moveToNext();
        }
    }

    public String testCounts() {
        DaoMaster.DevOpenHelper helper
                = new DaoMaster.DevOpenHelper(getActivity(), "Employee-db", null);
        db = helper.getWritableDatabase();

        //Skip the first as a "warmup"
        String query = QUERY1;
        Cursor cursor = db.rawQuery(query, null);
        cursor.getCount();
        cursor.close();

        long startTime = System.currentTimeMillis();

        query = QUERY2;
        cursor = db.rawQuery(query, null);
        cursor.getCount();
        cursor.close();

        query = QUERY3;
        cursor = db.rawQuery(query, null);
        cursor.getCount();
        cursor.close();

        query = QUERY4;
        cursor = db.rawQuery(query, null);
        cursor.getCount();
        cursor.close();

        query = QUERY5;
        cursor = db.rawQuery(query, null);
        loopCursor(cursor);
        cursor.getCount();
        db.close();

        long duration = (System.currentTimeMillis() - startTime);
        timings.put("testCounts", (4 / (double)duration));

        return "testCounts " + duration + " ms.";
    }

}
