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

package io.realm.tests.examples;

import android.test.AndroidTestCase;

import java.io.IOException;

import io.realm.tests.examples.entities.Dog;
import io.realm.tests.examples.entities.Person;
import io.realm.Realm;
import io.realm.RealmList;

public class RealmExample extends AndroidTestCase {


    public void testExample() throws IOException {

        // Realms are used to group data together
        Realm realm = new Realm(getContext().getFilesDir());

        // Update your object in a transaction
        realm.beginWrite();
        Dog myDog = realm.create(Dog.class);
        myDog.setName("Rex");
        realm.commit();

        // Read properties
        System.out.println(myDog.getName());

        // Query
        RealmList<Dog> results = realm.where(Dog.class).contains("name", "x").findAll();

        // Queries are chainable!
        results.where().greaterThan("age", 8);

        // Link objects
        Person person = new Person("Tim");


        //TODO:  person.getDogs().add(myDog);

        realm.beginWrite();
        //TODO: realm.add(person);
        realm.commit();

        // Query across links
        // RealmList<Person> persons = realm.where(Person.class).with("dogs").contains("name", "x").findAll();

        // Query from another thread
        new Thread() {
            public void run() {

                Realm realm = null;
                try {
                    realm = new Realm(getContext().getFilesDir());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                realm.where(Dog.class).contains("name", "x").findAll();

            }
        }.run();

    }

}
