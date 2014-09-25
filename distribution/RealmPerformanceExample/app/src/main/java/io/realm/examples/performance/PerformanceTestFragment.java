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

    protected View         rootView   = null;
    protected LinearLayout rootLayout = null;

    public PerformanceTestFragment() {
        // Required empty public constructor
    }

    protected List<PerformanceTest> tests = new ArrayList<PerformanceTest>();

    private AsyncTask<Void, String, Void> bgTask = null;

    protected AsyncTask<Void, String, Void> getTask() {
        bgTask = null;
        bgTask = new AsyncTask<Void, String, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                for(PerformanceTest t : tests) {
                    t.setActivity(getActivity());
                    t.initNames();

                    publishProgress("Executing for Insert Count: " + t.getNumInserts());

                    publishProgress(t.testInserts());
                    publishProgress(t.testQueries());
                    publishProgress(t.testCounts());
                }
                return null;
            }

            protected void onProgressUpdate(String... progress) {
                showStatus(progress[0]);
            }
        };
        return bgTask;
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
                String txt = ((EditText)rootView.findViewById(R.id.input_field)).getText().toString();
                try {
                    for(PerformanceTest t : tests) {
                        t.setNumInserts(new Integer(txt));
                    }
                } catch (Exception e) {
                    showStatus("Entry: " + txt + " not a valid integer");
                }
                getTask().execute();
            }
        });

        rootView.findViewById(R.id.clear_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(PerformanceTest t : tests) {
                    t.clearDevice();
                }
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

    protected void showStatus(String txt) {
        Log.i(TAG, txt);
        TextView tv = new TextView(getActivity());
        tv.setText(txt);
        rootLayout.addView(tv);
    }
}
