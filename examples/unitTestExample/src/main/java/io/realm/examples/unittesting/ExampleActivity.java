/*
 * Copyright 2015 Realm Inc.
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

package io.realm.examples.unittesting;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.examples.unittesting.model.Person;

public class ExampleActivity extends AppCompatActivity {

    public static final String TAG = ExampleActivity.class.getName();
    private LinearLayout rootLayout = null;

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Realm.init(getApplicationContext());
        setContentView(R.layout.activity_example);
        rootLayout = findViewById(R.id.container);
        rootLayout.removeAllViews();

        // Open the default Realm for the UI thread.
        realm = Realm.getDefaultInstance();

        // Clean up from previous run
        cleanUp();

        // Small operation that is ok to run on the main thread
        basicCRUD(realm);

        // More complex operations can be executed on another thread.
        AsyncTask<Void, Void, String> foo = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String info = "";
                info += complexQuery();
                return info;
            }

            @Override
            protected void onPostExecute(String result) {
                showStatus(result);
            }
        };

        foo.execute();

        findViewById(R.id.clean_up).setOnClickListener(view -> {
            view.setEnabled(false);
            Log.d("TAG", "clean up");
            cleanUp();
            view.setEnabled(true);
        });
    }

    private void cleanUp() {
        // Delete all persons
        realm.executeTransaction(r -> r.delete(Person.class));
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
        realm.executeTransaction(r -> {
            // Add a person
            Person person = r.createObject(Person.class);
            person.setId(1);
            person.setName("John Young");
            person.setAge(14);
        });

        // Find the first person (no query conditions) and read a field
        final Person person = realm.where(Person.class).findFirst();
        showStatus(person.getName() + ":" + person.getAge());

        // Update person in a transaction
        realm.executeTransaction(r -> {
            person.setName("John Senior");
            person.setAge(89);
        });

        showStatus(person.getName() + " got older: " + person.getAge());

        // Add two more people
        realm.executeTransaction(r -> {
            Person jane = r.createObject(Person.class);
            jane.setName("Jane");
            jane.setAge(27);

            Person doug = r.createObject(Person.class);
            doug.setName("Robert");
            doug.setAge(42);
        });

        RealmResults<Person> people = realm.where(Person.class).findAll();
        showStatus(String.format("Found %s people", people.size()));
        for (Person p : people) {
            showStatus("Found " + p.getName());
        }
    }

    private String complexQuery() {
        String status = "\n\nPerforming complex Query operation...";

        Realm realm = Realm.getDefaultInstance();
        status += "\nNumber of people in the DB: " + realm.where(Person.class).count();

        // Find all persons where age between 1 and 99 and name begins with "J".
        RealmResults<Person> results = realm.where(Person.class)
                .between("age", 1, 99)       // Notice implicit "and" operation
                .beginsWith("name", "J")
                .findAll();
        status += "\nNumber of people aged between 1 and 99 who's name start with 'J': " + results.size();

        realm.close();
        return status;
    }
}
