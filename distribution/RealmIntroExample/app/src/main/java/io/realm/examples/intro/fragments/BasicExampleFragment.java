package io.realm.examples.intro.fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.examples.intro.R;
import io.realm.examples.intro.model.Person;

public class BasicExampleFragment extends Fragment {

    public static final String TAG = BasicExampleFragment.class.getName();

    LinearLayout rootLayout = null;

    public static BasicExampleFragment newInstance() {
        BasicExampleFragment fragment = new BasicExampleFragment();
        return fragment;
    }

    public BasicExampleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_basic_example, null);

        rootLayout = (LinearLayout) rootView.findViewById(R.id.container);

        try {
            //These operations are small enough that
            //we can generally safely run them on the UI thread.
            basicReadWrite();
            basicUpdate();
            basicQuery();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rootView;
    }

    private void basicReadWrite() throws java.io.IOException {
        showStatus("Performing basic Read/Write operation...");

        // Open a default realm
        Realm realm = new Realm(getActivity());

        // Add a person in a write transaction
        realm.beginWrite();
        Person person = realm.create(Person.class);
        person.setName("Happy Person");
        person.setAge(14);
        realm.commit();

        // Find first person
        person = realm.where(Person.class).findFirst();
        showStatus(person.getName() + ":" + person.getAge());
    }

    private void basicQuery() throws java.io.IOException {
        showStatus("\nPerforming basic Query operation...");

        Realm realm = new Realm(getActivity());
        showStatus("Number of persons: " + realm.allObjects(Person.class).size());
        RealmResults<Person> results = realm.where(Person.class).equalTo("age", 99).findAll();
        showStatus("Size of result set: " + results.size());
    }

    private void basicUpdate() throws java.io.IOException {
        showStatus("\nPerforming basic Update operation...");

        // Open a default realm
        Realm realm = new Realm(getActivity());

        // Get the first object
        Person person = realm.where(Person.class).findFirst();

        // Update person in a write transaction
        realm.beginWrite();
        person.setName("Senior Person");
        person.setAge(99);
        realm.commit();

        showStatus(person.getName() + ":" + person.getAge());
    }

    private void showStatus(String txt) {
        Log.i(TAG, txt);
        TextView tv = new TextView(getActivity());
        tv.setText(txt);
        rootLayout.addView(tv);
    }
}
