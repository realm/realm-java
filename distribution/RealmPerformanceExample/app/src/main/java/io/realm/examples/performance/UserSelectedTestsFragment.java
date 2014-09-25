package io.realm.examples.performance;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;

public class UserSelectedTestsFragment extends PerformanceTestFragment {

    public static final String TAG = UserSelectedTestsFragment.class.getName();

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
            RadioButton button = new RadioButton(getActivity());
            button.setText(c.getSimpleName());
            ((LinearLayout)rootView.findViewById(R.id.selected_tests)).addView(button);
        }

        rootView.findViewById(R.id.executeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int numButtons = ((LinearLayout)rootView.findViewById(R.id.selected_tests)).getChildCount();

                for(int i=0; i<numButtons; i++) {
                      RadioButton button = (RadioButton)((LinearLayout)rootView
                            .findViewById(R.id.selected_tests)).getChildAt(i);
                      if(button.isChecked()) {
                        try {
                            PerformanceTest test = (PerformanceTest)possibleTests[i].newInstance();
                            Log.d(TAG, "Adding test: " + test.getName());
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

                //Clear the buttons
                for(int i=0; i<numButtons; i++) {
                    RadioButton button = (RadioButton)((LinearLayout)rootView
                            .findViewById(R.id.selected_tests)).getChildAt(i);
                    button.setChecked(false);
                }
            }
        });

        rootView.findViewById(R.id.clear_all_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllDatabases();
            }
        });

        return rootView;
    }
}
