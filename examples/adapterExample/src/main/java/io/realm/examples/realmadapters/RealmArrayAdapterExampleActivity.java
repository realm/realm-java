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

package io.realm.examples.realmadapters;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmArrayAdapter;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.examples.realmadapters.model.Cat;
import io.realm.examples.realmadapters.model.Dog;
import io.realm.examples.realmadapters.model.Person;

public class RealmArrayAdapterExampleActivity extends Activity implements View.OnClickListener {

    @SuppressWarnings("UnusedDeclaration")
    public static final String TAG = RealmArrayAdapterExampleActivity.class.getName();

    private Realm realm = null;

    private RealmArrayAdapter<Person> mAdapter = null;
    private ListView mListView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_example);

        findViewById(R.id.insert_record_button).setOnClickListener(this);

        mListView   = (ListView)findViewById(R.id.items_list);

        // Reset the realm data before starting the tests
//        Realm.deleteRealmFile(this);

        // Acquire a realm object
        realm = Realm.getInstance(this);

        RealmResults<Person> results = realm.where(Person.class).findAll();

        mAdapter = new RealmArrayAdapter<Person>(this, R.layout.simplelistitem, results);
        mListView.setAdapter(mAdapter);

        realm.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    // Using the screen form the user can inject into the Realm
    // even if other threads are creating transactions.
    @Override
    public void onClick(View v) {
        String personName = ((TextView) findViewById(R.id.name)).getText().toString();
        String personAge = ((TextView) findViewById(R.id.age)).getText().toString();
        String petName = ((TextView) findViewById(R.id.pets_name)).getText().toString();

        Integer parseAge = 0;
        try {
            parseAge = Integer.parseInt(personAge);
        } catch (NumberFormatException ignored) {
	        Log.d(TAG, "Age for a person invalid");
	        return;
        }

        Person person = new Person();
        person.setName(personName);
        person.setAge(parseAge);

        int checkedId = ((RadioGroup) findViewById(R.id.petType)).getCheckedRadioButtonId();
        if (checkedId == R.id.hasCat) {
            Cat cat = realm.createObject(Cat.class);
            cat.setName(petName);
            RealmList<Cat> cats = person.getCats();
            cats.add(cat);
        } else if (checkedId == R.id.hasDog) {
            Dog dog = new Dog();
            dog.setName(petName);
            person.setDog(dog);
        }

        //TODO: This method is not supported by realm so we can't realistically run this example.
        mAdapter.add(person);
    }
}
