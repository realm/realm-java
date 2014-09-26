package io.realm.examples.performance.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import io.realm.examples.performance.PerformanceTest;
import io.realm.examples.performance.PerformanceTestException;

public class GreenDAOTests extends PerformanceTest {

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;

    public GreenDAOTests() {
        testName = "GreenDAO";
    }

    public void clearDatabase() throws PerformanceTestException {
        DaoMaster.DevOpenHelper helper
                = new DaoMaster.DevOpenHelper(getActivity(), "Employee-db", null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        EmployeeDao employeeDao = daoSession.getEmployeeDao();
        employeeDao.dropTable(db, true);
    }

    public void testBootstrap() throws PerformanceTestException {
        DaoMaster.DevOpenHelper helper
                = new DaoMaster.DevOpenHelper(getActivity(), "Employee-db", null);
        db = helper.getWritableDatabase();

        //Skip the first as a "warmup"
        String query = QUERY1;
        Cursor cursor = db.rawQuery(query, null);
        cursor.getCount();
        cursor.close();
    }

    public void testInserts() throws PerformanceTestException {
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        EmployeeDao employeeDao = daoSession.getEmployeeDao();

        for(int row=0; row < getNumInserts(); row++) {
            Employee employee = new Employee();
            employee.setName(getEmployeeName(row));
            employee.setAge(getEmployeeAge(row));
            employee.setHired(getHiredBool(row));
            employeeDao.insert(employee);
        }

        Cursor cursor = db.query(employeeDao.getTablename(),
                employeeDao.getAllColumns(), null, null, null, null, null);
        cursor.getCount();

        db.close();
    }

    public void testQueries() throws PerformanceTestException {
        long startTime = System.currentTimeMillis();
        String query;
        Cursor cursor;

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
        String query;
        Cursor cursor;

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
    }

}
