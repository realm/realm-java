/*
 * Copyright 2018 Realm Inc.
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

import java.lang.ref.WeakReference;
import java.util.Arrays;

import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.examples.intro.model.Cat;
import io.realm.examples.intro.model.Dog;
import io.realm.examples.intro.model.Person;

public class IntroExampleActivity extends Activity {
    public static final String TAG = "IntroExampleActivity";

    private LinearLayout rootLayout;

    private Realm realm;

    // Results obtained from a Realm are live, and can be observed on looper threads (like the UI thread).
    // Note that if you want to observe the RealmResults for a long time, then it should be a field reference.
    // Otherwise, the RealmResults can no longer be notified if the GC has cleared the reference to it.
    private RealmResults<Person> persons;

    // OrderedRealmCollectionChangeListener receives fine-grained changes - insertions, deletions, and changes.
    // If the change set isn't needed, then RealmChangeListener can also be used.
    private final OrderedRealmCollectionChangeListener<RealmResults<Person>> realmChangeListener = (people, changeSet) -> {
        String insertions = changeSet.getInsertions().length == 0 ? "" : "\n - Insertions: " + Arrays.toString(changeSet.getInsertions());
        String deletions = changeSet.getDeletions().length == 0 ? "" : "\n - Deletions: " + Arrays.toString(changeSet.getDeletions());
        String changes = changeSet.getChanges().length == 0 ? "" : "\n - Changes: " + Arrays.toString(changeSet.getChanges());
        showStatus("Person was loaded, or written to. " + insertions + deletions + changes);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_basic_example);
        rootLayout = findViewById(R.id.container);
        rootLayout.removeAllViews();

        // Clear the Realm if the example was previously run.
        Realm.deleteRealm(Realm.getDefaultConfiguration());

        // Create the Realm instance
        realm = Realm.getDefaultInstance();

        // Asynchronous queries are evaluated on a background thread,
        // and passed to the registered change listener when it's done.
        // The change listener is also called on any future writes that change the result set.
        persons = realm.where(Person.class).findAllAsync();

        // The change listener will be notified when the data is loaded,
        // or the Realm is written to from any threads (and the result set is modified).
        persons.addChangeListener(realmChangeListener);

        // These operations are small enough that
        // we can generally safely run them on the UI thread.
        basicCRUD(realm);
        basicQuery(realm);
        basicLinkQuery(realm);

        // More complex operations can be executed on another thread.
        new ComplexBackgroundOperations(this).execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        persons.removeAllChangeListeners(); // Remove the change listener when no longer needed.
        realm.close(); // Remember to close Realm when done.
    }

    private void showStatus(String text) {
        Log.i(TAG, text);
        TextView textView = new TextView(this);
        textView.setText(text);
        rootLayout.addView(textView);
    }

    private void basicCRUD(Realm realm) {
        showStatus("Perform basic Create/Read/Update/Delete (CRUD) operations...");

        // All writes must be wrapped in a transaction to facilitate safe multi threading
        realm.executeTransaction(r -> {
            // Add a person. 
            // RealmObjects with primary keys created with `createObject()` must specify the primary key value as an argument.
            Person person = r.createObject(Person.class, 1);
            person.setName("Young Person");
            person.setAge(14);

            // Even young people have at least one phone in this day and age.
            // Please note that this is a RealmList that contains primitive values.
            person.getPhoneNumbers().add("+1 123 4567");
        });

        // Find the first person (no query conditions) and read a field
        final Person person = realm.where(Person.class).findFirst();
        showStatus(person.getName() + ":" + person.getAge());

        // Update person in a transaction
        realm.executeTransaction(r -> {
            // Managed objects can be modified inside transactions.
            person.setName("Senior Person");
            person.setAge(99);
            showStatus(person.getName() + " got older: " + person.getAge());
        });

        // Delete all persons
        showStatus("Deleting all persons");
        realm.executeTransaction(r -> r.delete(Person.class));
    }

    private void basicQuery(Realm realm) {
        showStatus("\nPerforming basic Query operation...");

        // Let's add a person so that the query returns something.
        realm.executeTransaction(r -> {
            Person oldPerson = new Person();
            oldPerson.setId(99);
            oldPerson.setAge(99);
            oldPerson.setName("George");
            realm.insertOrUpdate(oldPerson);
        });

        showStatus("Number of persons: " + realm.where(Person.class).count());

        RealmResults<Person> results = realm.where(Person.class).equalTo("age", 99).findAll();

        showStatus("Size of result set: " + results.size());
    }

    private void basicLinkQuery(Realm realm) {
        showStatus("\nPerforming basic Link Query operation...");

        // Let's add a person with a cat so that the query returns something.
        realm.executeTransaction(r -> {
            Person catLady = realm.createObject(Person.class, 24);
            catLady.setAge(52);
            catLady.setName("Mary");

            Cat tiger = realm.createObject(Cat.class);
            tiger.name = "Tiger";
            catLady.getCats().add(tiger);
        });

        showStatus("Number of persons: " + realm.where(Person.class).count());

        RealmResults<Person> results = realm.where(Person.class).equalTo("cats.name", "Tiger").findAll();

        showStatus("Size of result set: " + results.size());
    }

    // This AsyncTask shows how to use Realm in background thread operations.
    //
    // AsyncTasks should be static inner classes to avoid memory leaks.
    // In this example, WeakReference is used for the sake of simplicity.
    private static class ComplexBackgroundOperations extends AsyncTask<Void, Void, String> {
        private WeakReference<IntroExampleActivity> weakReference;

        public ComplexBackgroundOperations(IntroExampleActivity introExampleActivity) {
            this.weakReference = new WeakReference<>(introExampleActivity);
        }

        @Override
        protected void onPreExecute() {
            IntroExampleActivity activity = weakReference.get();
            if (activity == null) {
                return;
            }
            activity.showStatus("\n\nBeginning complex operations on background thread.");
        }

        @Override
        protected String doInBackground(Void... voids) {
            IntroExampleActivity activity = weakReference.get();
            if (activity == null) {
                return "";
            }
            // Open the default realm. Uses `try-with-resources` to automatically close Realm when done.
            // All threads must use their own reference to the realm.
            // Realm instances, RealmResults, and managed RealmObjects can not be transferred across threads.
            try (Realm realm = Realm.getDefaultInstance()) {
                String info;
                info = activity.complexReadWrite(realm);
                info += activity.complexQuery(realm);
                return info;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            IntroExampleActivity activity = weakReference.get();
            if (activity == null) {
                return;
            }
            activity.showStatus(result);
        }
    }

    private String complexReadWrite(Realm realm) {
        String status = "\nPerforming complex Read/Write operation...";

        // Add ten persons in one transaction
        realm.executeTransaction(r -> {
            Dog fido = r.createObject(Dog.class);
            fido.name = "fido";
            for (int i = 0; i < 10; i++) {
                Person person = r.createObject(Person.class, i);
                person.setName("Person no. " + i);
                person.setAge(i);
                person.setDog(fido);

                // The field tempReference is annotated with @Ignore.
                // This means setTempReference sets the Person tempReference
                // field directly. The tempReference is NOT saved as part of
                // the RealmObject:
                person.setTempReference(42);

                for (int j = 0; j < i; j++) {
                    Cat cat = r.createObject(Cat.class);
                    cat.name = "Cat_" + j;
                    person.getCats().add(cat);
                }
            }
        });

        // Implicit read transactions allow you to access your objects
        status += "\nNumber of persons: " + realm.where(Person.class).count();

        // Iterate over all objects, with an iterator
        for (Person person : realm.where(Person.class).findAll()) {
            String dogName;
            if (person.getDog() == null) {
                dogName = "None";
            } else {
                dogName = person.getDog().name;
            }
            status += "\n" + person.getName() + ":" + person.getAge() + " : " + dogName + " : " + person.getCats().size();
        }

        // Sorting
        RealmResults<Person> sortedPersons = realm.where(Person.class).sort("age", Sort.DESCENDING).findAll();
        status += "\nSorting " + sortedPersons.last().getName() + " == " + realm.where(Person.class).findFirst()
                .getName();

        return status;
    }

    private String complexQuery(Realm realm) {
        String status = "\n\nPerforming complex Query operation...";
        status += "\nNumber of persons: " + realm.where(Person.class).count();

        // Find all persons where age between 7 and 9 and name begins with "Person".
        RealmResults<Person> results = realm.where(Person.class)
                .between("age", 7, 9)       // Notice implicit "and" operation
                .beginsWith("name", "Person").findAll();

        status += "\nSize of result set: " + results.size();

        return status;
    }
}
