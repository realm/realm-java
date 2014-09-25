package io.realm.examples.performance;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public abstract class PerformanceTestFragment extends Fragment {

    public static final String TAG = PerformanceTestFragment.class.getName();

    public static final int MAX_AGE         = 50;
    public static final int MIN_AGE         = 20;
    public static final int NUM_TEST_NAMES  = 1000;
    public static final int MIN_NUM_INSERTS = 10000;

    //Queries

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

    public int getNumInserts() {
        return numInserts;
    }

    public void setNumInserts(int numInserts) {
        this.numInserts = numInserts;
    }

    private List<String> employeeNames = null;

    private View         rootView   = null;
    private LinearLayout rootLayout = null;

    public void initNames() {
        employeeNames = new ArrayList<String>();
        for (int i = 0; i < NUM_TEST_NAMES; i++) {
            employeeNames.add("Foo" + i);
        }
    }

    public String getName(int row) {
        return employeeNames.get(row % NUM_TEST_NAMES);
    }

    public int getAge(int row) {
        return row % MAX_AGE + MIN_AGE;
    }

    public int getHired(int row) {
        return row % 2;
    }

    public boolean getHiredBool(int row) {
        if (row % 2 == 0) return false;
        return true;
    }

    public PerformanceTestFragment() {
        // Required empty public constructor
    }

    AsyncTask<Void, Void, String> bgTask = null;

    private AsyncTask<Void, Void, String> getTask() {
        bgTask = null;
        bgTask = new AsyncTask<Void, Void, String>() {
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
        };
        return bgTask;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        rootView = inflater.inflate(R.layout.fragment_basic_example, null);

        rootLayout = (LinearLayout) rootView.findViewById(R.id.container);

        initNames();

        rootView.findViewById(R.id.executeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = ((EditText)rootView.findViewById(R.id.input_field)).getText().toString();
                try {
                    PerformanceTestFragment.this.numInserts = new Integer(txt);
                } catch (Exception e) {
                    showStatus("Entry: " + txt + " not a valid integer");
                    PerformanceTestFragment.this.numInserts = MIN_NUM_INSERTS;
                }
                showStatus("Executing for Insert Count: " + numInserts);
                getTask().execute();
            }
        });

        rootView.findViewById(R.id.clear_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearDevice();
            }
        });

        return rootView;
    }

    @Override
    public void onPause() {
        super.onStop();
        Log.d(TAG, "Cancelling tests");
        showStatus("Cancelling test due to screen Pause");
        bgTask.cancel(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "Cancelling tests");
        showStatus("Cancelling test due to screen Stop");
        bgTask.cancel(true);
    }

    public abstract void clearDevice();

    public abstract String testQueries();

    public abstract String testInserts();

    public abstract String testCounts();

    private void showStatus(String txt) {
        Log.i(TAG, txt);
        TextView tv = new TextView(getActivity());
        tv.setText(txt);
        rootLayout.addView(tv);
    }
}
