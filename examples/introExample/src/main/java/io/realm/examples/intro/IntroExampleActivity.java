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

package io.realm.examples.intro;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.examples.intro.model.Cat;
import io.realm.examples.intro.model.Dog;
import io.realm.examples.intro.model.Person;


public class IntroExampleActivity extends Activity {

    public static final String TAG = IntroExampleActivity.class.getName();
    private LinearLayout rootLayout = null;

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_basic_example);
        rootLayout = ((LinearLayout) findViewById(R.id.container));
        rootLayout.removeAllViews();

        // These operations are small enough that
        // we can generally safely run them on the UI thread.

        // Open the default realm ones for the UI thread.
        realm = Realm.getInstance(this);

        basicCRUD(realm);
        basicQuery(realm);
        basicLinkQuery(realm);

        // More complex operations can be executed on another thread.
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String info = null;
                info = complexReadWrite();
                info += complexQuery();
                return info;
            }

            @Override
            protected void onPostExecute(String result) {
                showStatus(result);
            }
        }.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close(); // Remember to close Realm when done.
    }

    private void showStatus(String txt) {
        Log.i(TAG, txt);
        TextView tv = new TextView(this);
        tv.setText(txt);
        rootLayout.addView(tv);
    }

    private void basicCRUD(Realm realm) {
        showStatus("Perform basic Create/Read/Update/Delete (CRUD) operations...");

        // All writes must be wrapped in a transaction to facilitate safe multi threading
        realm.beginTransaction();

        // Add a person
        Person person = realm.createObject(Person.class);
        person.setId(1);
        person.setName("Young Person");
        person.setAge(14);

        // When the write transaction is committed, all changes a synced to disk.
        realm.commitTransaction();

        // Find the first person (no query conditions) and read a field
        person = realm.where(Person.class).findFirst();
        showStatus(person.getName() + ":" + person.getAge());

        // Update person in a write transaction
        realm.beginTransaction();
        person.setName("Senior Person");
        person.setAge(99);
        showStatus(person.getName() + " got older: " + person.getAge());
        realm.commitTransaction();

        // Delete all persons
        realm.beginTransaction();
        realm.allObjects(Person.class).clear();
        realm.commitTransaction();
    }

    private void basicQuery(Realm realm) {
        showStatus("\nPerforming basic Query operation...");
        showStatus("Number of persons: " + realm.allObjects(Person.class).size());

        RealmResults<Person> results = realm.where(Person.class).equalTo("age", 99).findAll();

        showStatus("Size of result set: " + results.size());
    }

    private void basicLinkQuery(Realm realm) {
        showStatus("\nPerforming basic Link Query operation...");
        showStatus("Number of persons: " + realm.allObjects(Person.class).size());

        RealmResults<Person> results = realm.where(Person.class).equalTo("cats.name", "Tiger").findAll();

        showStatus("Size of result set: " + results.size());
    }

    private String complexReadWrite() {
        String status = "\nPerforming complex Read/Write operation...";

        // Open the default realm. All threads must use it's own reference to the realm.
        // Those can not be transferred across threads.
        Realm realm = Realm.getInstance(this);

        // Add ten persons in one write transaction
        realm.beginTransaction();
        Dog fido = realm.createObject(Dog.class);
        fido.setName("fido");
        for (int i = 0; i < 10; i++) {
            Person person = realm.createObject(Person.class);
            person.setId(i);
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

        // Sorting
        RealmResults<Person> sortedPersons = realm.allObjects(Person.class);
        sortedPersons.sort("age", false);
        assert(realm.allObjects(Person.class).last().getName() == sortedPersons.first().getName());
        status += "\nSorting " + sortedPersons.last().getName() + " == " + realm.allObjects(Person.class).first().getName();

        realm.close();
        return status;
    }

    private String complexQuery() {
        String status = "\n\nPerforming complex Query operation...";

        Realm realm = Realm.getInstance(this);
        status += "\nNumber of persons: " + realm.allObjects(Person.class).size();

        // Find all persons where age between 7 and 9 and name begins with "Person".
        RealmResults<Person> results = realm.where(Person.class)
                .between("age", 7, 9)       // Notice implicit "and" operation
                .beginsWith("name", "Person").findAll();
        status += "\nSize of result set: " + results.size();

        realm.close();
        return status;
    }
}
