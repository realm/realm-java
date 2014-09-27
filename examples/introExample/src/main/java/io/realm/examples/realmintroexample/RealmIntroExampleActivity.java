/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    private void basicReadWrite() throws IOException {
        showStatus("Performing basic Read/Write operation...");

        // Open a default realm
        Realm realm = Realm.getInstance(this);

        // Add a person in a write transaction
        realm.beginTransaction();
        Person person = realm.createObject(Person.class);
        person.setName("Happy Person");
        person.setAge(14);
        realm.commitTransaction();

        // Find first person
        person = realm.where(Person.class).findFirst();
        showStatus(person.getName() + ":" + person.getAge());
    }

    private void basicQuery() throws IOException {
        showStatus("\nPerforming basic Query operation...");

        Realm realm = Realm.getInstance(this);
        showStatus("Number of persons: " + realm.allObjects(Person.class).size());
        RealmResults<Person> results = realm.where(Person.class).equalTo("age", 99).findAll();
        showStatus("Size of result set: " + results.size());
    }

    private void basicUpdate() throws IOException {
        showStatus("\nPerforming basic Update operation...");

        // Open a default realm
        Realm realm = Realm.getInstance(this);

        // Get the first object
        Person person = realm.where(Person.class).findFirst();

        // Update person in a write transaction
        realm.beginTransaction();
        person.setName("Senior Person");
        person.setAge(99);
        realm.commitTransaction();

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
        Realm realm = Realm.getInstance(this);

        // Add ten persons in one write transaction
        realm.beginTransaction();
        Dog fido = realm.createObject(Dog.class);
        fido.setName("fido");
        for (int i = 0; i < 10; i++) {
            Person person = realm.createObject(Person.class);
            person.setName("Person no. " + i);
            person.setAge(i);
            person.setDog(fido);

            // The field tempReference is annotated with @Ignore.
            // This means setTempReference sets the Person tempReference
            // field directly. The tempReference is NOT saved as part of
            // the RealmObject:
            person.setTempReference(42);

            for (int j = 0; j < i; j++) {
                Cat cat = realm.createObject(Cat.class);
                cat.setName("Cat_" + j);
                person.getCats().add(cat);
            }
        }
        realm.commitTransaction();

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

            // The field tempReference is annotated with @Ignore
            // Though we initially set its value to 42, it has
            // not been saved as part of the Person RealmObject:
            assert(pers.getTempReference() == 0);

        }

        return status;
    }

    private String complexQuery() throws IOException {
        String status = "\n\nPerforming complex Query operation...";

        Realm realm = Realm.getInstance(this);
        status += "\nNumber of persons: " + realm.allObjects(Person.class).size();

        // Find all persons where age between 7 and 9 and name begins with "Person".
        RealmResults<Person> results = realm.where(Person.class)
                .between("age", 7, 9)       // Notice implicit "and" operation
                .beginsWith("name", "Person").findAll();
        status += "\nSize of result set: " + results.size();
        return status;
    }
}
