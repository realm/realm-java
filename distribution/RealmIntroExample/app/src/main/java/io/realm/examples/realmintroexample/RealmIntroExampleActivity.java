package io.realm.examples.realmintroexample;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.examples.realmintroexample.model.Cat;
import io.realm.examples.realmintroexample.model.Dog;
import io.realm.examples.realmintroexample.model.Person;


public class RealmIntroExampleActivity extends Activity {

    public static final String TAG = RealmIntroExampleActivity.class.getName();

    private LinearLayout rootLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_basic_example);

        rootLayout = ((LinearLayout) findViewById(R.id.container));
        rootLayout.removeAllViews();

        try {
            //These operations are small enough that
            //we can generally safely run them on the UI thread.
            basicReadWrite();
            basicUpdate();
            basicQuery();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
    }

    private void basicReadWrite() throws java.io.IOException {
        showStatus("Performing basic Read/Write operation...");

        // Open a default realm
        Realm realm = new Realm(this);

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

        Realm realm = new Realm(this);
        showStatus("Number of persons: " + realm.allObjects(Person.class).size());
        RealmResults<Person> results = realm.where(Person.class).equalTo("age", 99).findAll();
        showStatus("Size of result set: " + results.size());
    }

    private void basicUpdate() throws java.io.IOException {
        showStatus("\nPerforming basic Update operation...");

        // Open a default realm
        Realm realm = new Realm(this);

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
        TextView tv = new TextView(this);
        tv.setText(txt);
        rootLayout.addView(tv);
    }

    private String complexReadWrite() throws IOException {
        String status = "\nPerforming complex Read/Write operation...";

        // Open a default realm
        Realm realm = new Realm(this);

        // Add ten persons in one write transaction
        realm.beginWrite();
        Dog fido = realm.create(Dog.class);
        fido.setName("fido");
        for (int i = 0; i < 10; i++) {
            Person person = realm.create(Person.class);
            person.setName("Person no. " + i);
            person.setAge(i);
            person.setDog(fido);
            person.setTempReference(42);
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

            // Note that the tempReference field has been annotated with @Ignore
            // It is therefore not persisted:
            assert(pers.getTempReference() == 0);

        }

        return status;
    }

    private String complexQuery() throws IOException {
        String status = "\n\nPerforming complex Query operation...";

        Realm realm = new Realm(this);
        status += "\nNumber of persons: " + realm.allObjects(Person.class).size();

        // Find all persons where age between 7 and 9 and name begins with "Person".
        RealmResults<Person> results = realm.where(Person.class)
                .between("age", 7, 9)       // Notice implicit "and" operation
                .beginsWith("name", "Person").findAll();
        status += "\nSize of result set: " + results.size();
        return status;
    }
}
