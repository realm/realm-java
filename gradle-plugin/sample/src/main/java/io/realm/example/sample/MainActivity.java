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

package io.realm.example.sample;


import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.example.sample.models.Person;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Realm realm = Realm.getInstance(this);
        RealmResults<Person> persons = realm.allObjects(Person.class);
        if (persons.isEmpty()) {
            Toast toast = Toast.makeText(this, "No persons in the Realm file", Toast.LENGTH_SHORT);
            toast.show();
        }
        realm.beginTransaction();
        Person person  = realm.createObject(Person.class);
        person.note = "Test";
        realm.commitTransaction();
        realm.close();
    }
}
