package io.realm.examples.intro.fragments;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
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
import io.realm.examples.intro.model.Cat;
import io.realm.examples.intro.model.Dog;
import io.realm.examples.intro.model.Person;

public class ComplexExampleFragment extends Fragment {

    public static final String TAG = ComplexExampleFragment.class.getName();

    LinearLayout rootLayout = null;

    public static ComplexExampleFragment newInstance() {
        ComplexExampleFragment fragment = new ComplexExampleFragment();
        return fragment;
    }

    public ComplexExampleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_basic_example, null);

        rootLayout = (LinearLayout) rootView.findViewById(R.id.container);

        //More complex operations should not be
        //executed on the UI thread.
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String info = null;
                try {
                    info = complexReadWrite();
                    info += complexQuery();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return info;
            }

            @Override
            protected void onPostExecute(String result) {
                showStatus(result);
            }
        }.execute();

        return rootView;
    }

    private void showStatus(String txt) {
        Log.i(TAG, txt);
        TextView tv = new TextView(getActivity());
        tv.setText(txt);
        rootLayout.addView(tv);
    }

    private String complexReadWrite() throws IOException {
        String status = "\nPerforming complex Read/Write operation...";

        // Open a default realm
        Realm realm = new Realm(getActivity());

        // Add ten persons in one write transaction
        realm.beginWrite();
        Dog fido = realm.create(Dog.class);
        fido.setName("fido");
        for (int i = 0; i < 10; i++) {
            Person person = realm.create(Person.class);
            person.setName("Person no. " + i);
            person.setAge(i);
            person.setDog(fido);
            for (int j = 0; j < i; j++) {
                Cat cat = realm.create(Cat.class);
                cat.setName("Cat_" + j);
                person.getCats().add(cat);
            }
        }
        realm.commit();

        // Implicit read transactions allow you to access your objects
        status += "\nNumber of persons: " + realm.allObjects(Person.class).size();

        // Iterate over all objects
        for (Person pers : realm.allObjects(Person.class)) {
            String dogName;
            if (pers.getDog() == null) {
                dogName = "None";
            } else {
                dogName = pers.getDog().getName();
            }
            status += "\n" + pers.getName() + ":" + pers.getAge() + " : " + dogName + " : " + pers.getCats().size();
        }

        return status;
    }

    private String complexQuery() throws IOException {
        String status = "\n\nPerforming complex Query operation...";

        Realm realm = new Realm(getActivity());
        status += "\nNumber of persons: " + realm.allObjects(Person.class).size();

        // Find all persons where age between 7 and 9 and name begins with "Person".
        RealmResults<Person> results = realm.where(Person.class)
                .between("age", 7, 9)       // Notice implicit "and" operation
                .beginsWith("name", "Person").findAll();
        status += "\nSize of result set: " + results.size();
        return status;
    }
}
