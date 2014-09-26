package io.realm.examples.performance;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.examples.performance.greendao.GreenDAOTests;
import io.realm.examples.performance.ormlite.ORMLiteTests;
import io.realm.examples.performance.realm.RealmTests;
import io.realm.examples.performance.sqlite.SQLiteTests;
import io.realm.examples.performance.sugar_orm.SugarORMTests;

public abstract class PerformanceTestFragment extends Fragment {

    public static final String TAG = PerformanceTestFragment.class.getName();

    protected View rootView = null;
    protected LinearLayout rootLayout = null;

    protected List<PerformanceTest> tests = new ArrayList<PerformanceTest>();

    private AsyncTask<Void, String, Void> bgTask = null;

    protected Class[] possibleTests = new Class[]{RealmTests.class,
            SQLiteTests.class, ORMLiteTests.class, SugarORMTests.class, GreenDAOTests.class};

    public PerformanceTestFragment() {
        // Required empty public constructor
    }

    protected AsyncTask<Void, String, Void> getTask() {
        bgTask = null;
        bgTask = new AsyncTask<Void, String, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                //Required by SugarORM message call to SQLite layer
                if(Looper.myLooper() != null) {
                    Looper.myLooper().quit();
                }
                Looper.prepare();
                //TODO:  We need to disable execute button or otherwise restart

                for (PerformanceTest t : tests) {
                    t.setActivity(getActivity());
                    t.initNames();
                    t.timings = new HashMap<String, Double>();

                    publishProgress("---<br><b>Executing " + t.getName() + " for Insert Count: " + t.getNumInserts() + "</b>...");

                    try {
                        t.testBootstrap();
                        long startTime = System.currentTimeMillis();
                        t.testInserts();
                        long duration = System.currentTimeMillis() - startTime;
                        publishProgress("Inserts: " + duration);
                        t.timings.put("Inserts", (t.getNumInserts() * 1.0 / duration));
                    } catch(PerformanceTestException e) {

                    }

                    try {
                        t.testBootstrap();
                        long startTime = System.currentTimeMillis();
                        t.testQueries();
                        long duration = System.currentTimeMillis() - startTime;
                        publishProgress("Queries: " + duration);
                        t.timings.put("Queries", (t.getNumQueries() * 1.0 / duration));
                    } catch(PerformanceTestException e) {

                    }

                    try {
                        t.testBootstrap();
                        long startTime = System.currentTimeMillis();
                        t.testCounts();
                        long duration = System.currentTimeMillis() - startTime;
                        publishProgress("Counts: " + duration);
                        t.timings.put("Counts", (t.getNumQueries() * 1.0 / duration));
                    } catch(PerformanceTestException e) {

                    }

//                    try {
//                        t.clearDatabase();
//                    } catch (PerformanceTestException e) {
//                        e.printStackTrace();
//                    }
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(String... progress) {
                showStatus(progress[0]);
            }

            @Override
            protected void onPostExecute(Void result) {
                printResults();
            }
        };
        return bgTask;
    }

    private void printResults() {
        showStatus("->>");
        showStatus("<b>Combined Results...</b>");
        showStatus("<b>Inserts per Second:</b>");
        //Multiply by 1000 to move from operations/ms to operations/second
        for(PerformanceTest t : tests) {
            showStatus(String.format(t.getName() + " %.2f", t.timings.get("Inserts") * 1000));
        }
        showStatus("---<br><b>Queries per Second:</b>");
        for(PerformanceTest t : tests) {
            showStatus(String.format(t.getName() + " %.2f", t.timings.get("Queries") * 1000));
        }
        showStatus("---<br><b>Counts per Second:</b>");
        for(PerformanceTest t : tests) {
            showStatus(String.format(t.getName() + " %.2f", t.timings.get("Counts") * 1000));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_single_test, null);

        rootLayout = (LinearLayout) rootView.findViewById(R.id.message_container);

        rootView.findViewById(R.id.executeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = ((EditText) rootView.findViewById(R.id.input_field)).getText().toString();
                try {
                    for (PerformanceTest t : tests) {
                        t.setNumInserts(new Integer(txt));
                    }
                } catch (Exception e) {
                    showStatus("User entry for Inserts: " + txt + " not a valid integer...using default");
                }
                getTask().execute();
            }
        });

        return rootView;
    }

    protected void clearDatabase() {
        for (PerformanceTest t : tests) {
            try {
                t.clearDatabase();
            } catch (PerformanceTestException e) {
                e.printStackTrace();
            }
        }
    }

    protected void clearAllDatabases() {
        for (Class c : possibleTests) {
            PerformanceTest t = null;
            try {
                t = (PerformanceTest) c.newInstance();
                t.setActivity(getActivity());
            } catch (java.lang.InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            try {
                t.clearDatabase();
            } catch (PerformanceTestException e) {
                e.printStackTrace();
            }
        }
        tests = new ArrayList<PerformanceTest>();
    }

    @Override
    public void onPause() {
        super.onStop();
        Log.d(TAG, "Cancelling tests");
        showStatus("Cancelling test due to screen Pause");
        if(bgTask != null)
            bgTask.cancel(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "Cancelling tests");
        showStatus("Cancelling test due to screen Stop");
        if(bgTask != null)
            bgTask.cancel(true);
    }

    protected void showStatus(String txt) {
        Log.i(TAG, txt);
        TextView tv = new TextView(getActivity());
        tv.setText(Html.fromHtml(txt));
        rootLayout.addView(tv);
    }
}
