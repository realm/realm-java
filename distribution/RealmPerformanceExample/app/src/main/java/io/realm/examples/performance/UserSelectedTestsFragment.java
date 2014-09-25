package io.realm.examples.performance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;

import io.realm.examples.performance.ormlite.ORMLiteTests;
import io.realm.examples.performance.realm.RealmTests;
import io.realm.examples.performance.sqlite.SQLiteTests;
import io.realm.examples.performance.sugar_orm.SugarORMTests;

public class UserSelectedTestsFragment extends PerformanceTestFragment {

    public static final String TAG = UserSelectedTestsFragment.class.getName();

    private Class[] possibleTests = new Class[] { RealmTests.class,
            SQLiteTests.class, ORMLiteTests.class, SugarORMTests.class };

    private View rootView = null;

    public static UserSelectedTestsFragment newInstance() {
        UserSelectedTestsFragment fragment = new UserSelectedTestsFragment();
        return fragment;
    }

    public UserSelectedTestsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        rootView   = inflater.inflate(R.layout.fragment_multiple_test, null);
        rootLayout = (LinearLayout) rootView.findViewById(R.id.message_container);

        for(Class c : possibleTests) {
            Switch switchButton = new Switch(getActivity());
            switchButton.setText(c.getName());
            ((LinearLayout)rootView.findViewById(R.id.selected_tests)).addView(switchButton);
        }

        rootView.findViewById(R.id.executeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int switchCount = ((LinearLayout)rootView.findViewById(R.id.selected_tests)).getChildCount();

                for(int i=0; i<switchCount; i++) {
                    Switch switchButton = (Switch)((LinearLayout)rootView.findViewById(R.id.selected_tests)).getChildAt(0);
                    if(switchButton.isChecked()) {
                        try {
                            PerformanceTest test = (PerformanceTest)possibleTests[i].newInstance();
                            tests.add(test);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

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
                for (PerformanceTest t : tests) {
                    t.clearDevice();
                }
            }
        });

        return rootView;
    }
}
