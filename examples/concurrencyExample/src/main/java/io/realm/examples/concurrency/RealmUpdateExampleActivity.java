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

package io.realm.examples.concurrency;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.examples.concurrency.adapters.RealmExampleAdapter;
import io.realm.examples.concurrency.model.Cat;
import io.realm.examples.concurrency.model.Dog;
import io.realm.examples.concurrency.model.Person;
import io.realm.examples.concurrency.services.SpawningService;
import io.realm.examples.concurrency.services.TransactionService;

public class RealmUpdateExampleActivity extends Activity implements View.OnClickListener {

    @SuppressWarnings("UnusedDeclaration")
    public static final String TAG = RealmUpdateExampleActivity.class.getName();

    private Realm realm = null;

    private RealmExampleAdapter<Person> mAdapter = null;
    private ListView mListView = null;

    private TextView mTextUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_updateexample);

        findViewById(R.id.insert_record_button).setOnClickListener(this);
        findViewById(R.id.quit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTransactionTests();
            }
        });

        mListView   = (ListView)findViewById(R.id.items_list);
        mTextUpdate = (TextView)findViewById(R.id.size_status);
        // Reset the realm data before starting the tests
        Realm.deleteRealmFile(this);

        // Acquire a realm object
        realm = Realm.getInstance(this);

        realm.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                updateList();
            }
        });

        mAdapter = new RealmExampleAdapter<Person>(this, R.layout.simplelistitem);
        mListView.setAdapter(mAdapter);
    }

    private void updateList() {
        RealmResults<Person> realmResults = realm.allObjects(Person.class);
        if(realmResults != null) {
            //Log.d(TAG, "OUTCOME: " + realmResults.size());
            mTextUpdate.setText(realmResults.size()+"");
        }
        mAdapter.setData(realmResults);
        mListView.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();

	    //Alternatively can use transaction tests here...
        startTransactionTests();

        //startSpawnTests();
    }

    @Override
    public void onStop() {
        super.onStop();

	    //Alternatively can use transaction tests here...
        stopTransactionTests();

        //stopSpawnTests();
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

        realm.beginTransaction();
        Person person = realm.createObject(Person.class);
        person.setName(personName);
        person.setAge(parseAge);

        int checkedId = ((RadioGroup) findViewById(R.id.petType)).getCheckedRadioButtonId();
        if (checkedId == R.id.hasCat) {
            Cat cat = realm.createObject(Cat.class);
            cat.setName(petName);
            RealmList<Cat> cats = person.getCats();
            cats.add(cat);
        } else if (checkedId == R.id.hasDog) {
            Dog dog = realm.createObject(Dog.class);
            dog.setName(petName);
            person.setDog(dog);
        }

        realm.commitTransaction();
    }

    // The transaction tests are one IntentService running multiple Writes
    // to a Realm in either a multiple transaction or single transaction loop
    private void startTransactionTests() {
        Intent serviceIntent = new Intent(this, TransactionService.class);
        serviceIntent.putExtra(TransactionService.REALM_TESTTYPE_EXTRA,
                TransactionService.TestType.MANY_TRANSACTIONS);
        serviceIntent.putExtra(TransactionService.ITERATION_COUNT, 10000000);
        this.startService(serviceIntent);
    }

    private void stopTransactionTests() {
        Intent serviceIntent = new Intent(this, TransactionService.class);
        this.stopService(serviceIntent);
    }

    // The Spawned tests create multiple threads of type Reader or Writer
    // which loop for a specified count
    private void startSpawnTests() {
        Intent serviceIntent = new Intent(this, SpawningService.class);
        serviceIntent.putExtra(SpawningService.REALM_INSERTCOUNT_EXTRA, 10000000);
        serviceIntent.putExtra(SpawningService.REALM_READCOUNT_EXTRA, 10000000);
        this.startService(serviceIntent);
    }

    private void stopSpawnTests() {
        Intent serviceIntent = new Intent(this, SpawningService.class);
        this.stopService(serviceIntent);
    }

    @SuppressWarnings("UnusedDeclaration")
    private void restartTests() {
        stopTransactionTests();
        startTransactionTests();
    }
}
