package io.realm.examples.performance.ormlite;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import io.realm.examples.performance.Globals;
import io.realm.examples.performance.R;


public class ORMLiteTestActivity extends OrmLiteBaseActivity<OrmLiteDatabaseHelper> {

    public static final String TAG = ORMLiteTestActivity.class.getName();

    private LinearLayout rootLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_example);

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

        String ret = "";

        final RuntimeExceptionDao<OrmLiteEmployee, Integer> employeeDao = getHelper().getEmployeeDao();

        employeeDao.callBatchTasks(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                for (int row = 0; row < Globals.NUM_INSERTS; row++) {
                    OrmLiteEmployee employee = new OrmLiteEmployee();
                    employee.setName(Globals.getName(row));
                    employee.setAge(Globals.getAge(row));
                    employee.setHired(Globals.getHiredBool(row));
                    employeeDao.create(employee);
                }
                return null;
            }
        });

        ret += "testInserts " + (System.currentTimeMillis() - startTime) + " ms.\n";

        //Verify writes were successful
        GenericRawResults<String[]> rawResults =
                employeeDao.queryRaw(
                        "SELECT * from Employee");
        List<String[]> results = null;
        try {
            results = rawResults.getResults();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        ret += "...Completed " + results.size() + " inserts\n";
        return ret;
    }

    private String testQueries() {
        long startTime = System.currentTimeMillis();

        final RuntimeExceptionDao<OrmLiteEmployee, Integer> employeeDao = getHelper().getEmployeeDao();

        GenericRawResults<String[]> rawResults =
                employeeDao.queryRaw(
                        "SELECT * from Employee " +
                                "WHERE name = 'Foo0' " +
                                "AND age >= 20 AND age <= 50 " +
                                "AND hired = 0");

        return "testQueries " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    private String testCounts() {
        long startTime = System.currentTimeMillis();

        final RuntimeExceptionDao<OrmLiteEmployee, Integer> employeeDao = getHelper().getEmployeeDao();

            GenericRawResults<String[]> rawResults =
                    employeeDao.queryRaw(
                            "SELECT * from Employee " +
                                    "WHERE name = 'Foo0' " +
                                    "AND age >= 20 AND age <= 50 " +
                                    "AND hired = 0");

        List<String[]> results = null;
        try {
            results = rawResults.getResults();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        results.size();

        return "testCounts " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    private String testCountsBuilder() {
        long startTime = System.currentTimeMillis();

        final RuntimeExceptionDao<OrmLiteEmployee, Integer> employeeDao = getHelper().getEmployeeDao();
        QueryBuilder<OrmLiteEmployee, Integer> queryBuilder =
                employeeDao.queryBuilder();

        PreparedQuery<OrmLiteEmployee> preparedQuery = null;
        try {
            queryBuilder.where().eq("name", "Name").and().between("age", 500, 50000).and().eq("hired", true);
            preparedQuery = queryBuilder.prepare();
        } catch(SQLException e) {
            e.printStackTrace();
        }

        List<OrmLiteEmployee> employeeList = employeeDao.query(preparedQuery);
        employeeList.size();

        return "testCounts " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    private void showStatus(String txt) {
        Log.i(TAG, txt);
        TextView tv = new TextView(this);
        tv.setText(txt);
        rootLayout.addView(tv);
    }
}
