package io.realm.examples.performance.ormlite;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.util.List;
import java.util.concurrent.Callable;

import io.realm.examples.performance.R;
import io.realm.examples.performance.model.OrmLiteEmployee;


public class ORMLiteTestActivity extends OrmLiteBaseActivity<OrmLiteDatabaseHelper> {

    public static final String TAG = ORMLiteTestActivity.class.getName();

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

        String ret = "";

        final RuntimeExceptionDao<OrmLiteEmployee, Integer> employeeDao = getHelper().getEmployeeDao();

        employeeDao.callBatchTasks(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                for (int i = 0; i < NUM_TESTS; i++) {
                    OrmLiteEmployee employee = new OrmLiteEmployee();
                    employee.setName("Name");
                    employee.setAge(i);
                    employee.setHired(true);
                    employeeDao.create(employee);
                }
                return null;
            }
        });

        ret += "testInserts " + (System.currentTimeMillis() - startTime) + " ms.\n";

        try {
            QueryBuilder<OrmLiteEmployee, Integer> queryBuilder =
                    employeeDao.queryBuilder();
            queryBuilder.where().eq("name", "Name");
            PreparedQuery<OrmLiteEmployee> preparedQuery = queryBuilder.prepare();
            List<OrmLiteEmployee> employeeList = employeeDao.query(preparedQuery);

            ret += "Completed " + employeeList.size() + " inserts\n";
        } catch(Exception e) {}
        return ret;
    }

    private String testQueries() {
        long startTime = System.currentTimeMillis();
        try {
            final RuntimeExceptionDao<OrmLiteEmployee, Integer> employeeDao = getHelper().getEmployeeDao();

            QueryBuilder<OrmLiteEmployee, Integer> queryBuilder =
                    employeeDao.queryBuilder();
            queryBuilder.where().eq("name", "Name").and().between("age",500,50000).and().eq("hired",true);
            PreparedQuery<OrmLiteEmployee> preparedQuery = queryBuilder.prepare();
            List<OrmLiteEmployee> employeeList = employeeDao.query(preparedQuery);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "testQueries " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    private String testCounts() {
        long startTime = System.currentTimeMillis();

        try {
            final RuntimeExceptionDao<OrmLiteEmployee, Integer> employeeDao = getHelper().getEmployeeDao();

            QueryBuilder<OrmLiteEmployee, Integer> queryBuilder =
                    employeeDao.queryBuilder();
            queryBuilder.where().eq("name", "Name").and().between("age",500,50000).and().eq("hired",true);
            PreparedQuery<OrmLiteEmployee> preparedQuery = queryBuilder.prepare();
            List<OrmLiteEmployee> employeeList = employeeDao.query(preparedQuery);
            employeeList.size();
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
