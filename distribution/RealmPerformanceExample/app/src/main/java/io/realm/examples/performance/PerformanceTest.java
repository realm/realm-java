package io.realm.examples.performance;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class PerformanceTest {

    public static final int MAX_AGE = 50;
    public static final int MIN_AGE = 20;
    public static final int NUM_TEST_NAMES = 1000;
    public static final int MIN_NUM_INSERTS = 10000;

    public static final String QUERY1 =
            "SELECT * from Employee " +
                    "WHERE name = 'Foo0' " +
                    "AND age >= 20 AND age <= 50 " +
                    "AND hired = 0";

    public static final String QUERY2 =
            "SELECT * from Employee " +
                    "WHERE name = 'Foo1' " +
                    "AND age >= 20 AND age <= 50 " +
                    "AND hired = 1";

    public static final String QUERY3 =
            "SELECT * from Employee " +
                    "WHERE name = 'Foo3' " +
                    "AND age >= 20 AND age <= 50 " +
                    "AND hired = 1";

    public static final String QUERY4 =
            "SELECT * from Employee " +
                    "WHERE name = 'Foo2' " +
                    "AND age >= 20 AND age <= 50 " +
                    "AND hired = 0";

    public static final String QUERY5 =
            "SELECT * from Employee " +
                    "WHERE name = 'Foo330' " +
                    "AND age >= 20 AND age <= 50 " +
                    "AND hired = 0";

    public static final String QUERY6 =
            "SELECT * from Employee " +
                    "WHERE name = 'Foo20' " +
                    "AND age >= 20 AND age <= 50 " +
                    "AND hired = 0";

    public static final String QUERY7 =
            "SELECT * from Employee " +
                    "WHERE name = 'Foo90' " +
                    "AND age >= 20 AND age <= 50 " +
                    "AND hired = 0";

    public static final String QUERY8 =
            "SELECT * from Employee " +
                    "WHERE name = 'Foo20' " +
                    "AND age >= 20 AND age <= 50 " +
                    "AND hired = 0";

    public static final String QUERY9 =
            "SELECT * from Employee " +
                    "WHERE name = 'Foo111' " +
                    "AND age >= 20 AND age <= 50 " +
                    "AND hired = 1";

    public static final String QUERY10 =
            "SELECT * from Employee " +
                    "WHERE name = 'Foo99' " +
                    "AND age >= 20 AND age <= 50 " +
                    "AND hired = 1";

    //Count queries

    public static final String COUNT_QUERY1 =
            "SELECT COUNT(*) from Employee " +
                    "WHERE name = 'Foo0' " +
                    "AND age >= 20 AND age <= 50 " +
                    "AND hired = 0";

    public static final String COUNT_QUERY2 =
            "SELECT COUNT(*) from Employee " +
                    "WHERE name = 'Foo1' " +
                    "AND age >= 20 AND age <= 50 " +
                    "AND hired = 1";

    public static final String COUNT_QUERY3 =
            "SELECT COUNT(*) from Employee " +
                    "WHERE name = 'Foo3' " +
                    "AND age >= 20 AND age <= 50 " +
                    "AND hired = 1";

    public static final String COUNT_QUERY4 =
            "SELECT COUNT(*) from Employee " +
                    "WHERE name = 'Foo2' " +
                    "AND age >= 20 AND age <= 50 " +
                    "AND hired = 0";

    public static final String COUNT_QUERY5 =
            "SELECT COUNT(*) from Employee " +
                    "WHERE name = 'Foo330' " +
                    "AND age >= 20 AND age <= 50 " +
                    "AND hired = 0";

    public static final String COUNT_QUERY6 =
            "SELECT COUNT(*) from Employee " +
                    "WHERE name = 'Foo20' " +
                    "AND age >= 20 AND age <= 50 " +
                    "AND hired = 0";

    public static final String COUNT_QUERY7 =
            "SELECT COUNT(*) from Employee " +
                    "WHERE name = 'Foo90' " +
                    "AND age >= 20 AND age <= 50 " +
                    "AND hired = 0";

    public static final String COUNT_QUERY8 =
            "SELECT COUNT(*) from Employee " +
                    "WHERE name = 'Foo20' " +
                    "AND age >= 20 AND age <= 50 " +
                    "AND hired = 0";

    public static final String COUNT_QUERY9 =
            "SELECT COUNT(*) from Employee " +
                    "WHERE name = 'Foo111' " +
                    "AND age >= 20 AND age <= 50 " +
                    "AND hired = 1";

    public static final String COUNT_QUERY10 =
            "SELECT COUNT(*) from Employee " +
                    "WHERE name = 'Foo99' " +
                    "AND age >= 20 AND age <= 50 " +
                    "AND hired = 1";

    private int numInserts = MIN_NUM_INSERTS;

    protected String testName;

    private List<String> employeeNames = null;

    //Timings are stored as events/ms
    protected HashMap<String, Double> timings;

    private Activity activity = null;
    private View rootView = null;
    private LinearLayout rootLayout = null;

    public int getNumInserts() {
        return numInserts;
    }

    public void setNumInserts(int numInserts) {
        this.numInserts = numInserts;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void initNames() {
        employeeNames = new ArrayList<String>();
        for (int i = 0; i < NUM_TEST_NAMES; i++) {
            employeeNames.add("Foo" + i);
        }
    }

    public String getEmployeeName(int row) {
        return employeeNames.get(row % NUM_TEST_NAMES);
    }

    public int getEmployeeAge(int row) {
        return row % MAX_AGE + MIN_AGE;
    }

    public int getEmployeeHiredStatus(int row) {
        return row % 2;
    }

    public boolean getHiredBool(int row) {
        if (row % 2 == 0) return false;
        return true;
    }

    public abstract void clearDevice();

    public abstract String testQueries();

    public abstract String testInserts();

    public abstract String testCounts();

    public String getName() {
        return testName;
    }

    public void setName(String name) {
        this.testName = name;
    }

}
