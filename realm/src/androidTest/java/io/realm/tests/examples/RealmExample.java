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


    public void testExample() {

        // Create a standalone object
        Dog myDog = new Dog();

        // Set & read properties
        myDog.setName("Rex");
        System.out.println(myDog.getName());

        // Realms are used to group data together
        Realm realm = null;
        try {
            realm = new Realm(getContext().getFilesDir());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Save your object
        realm.beginWrite();
        realm.add(myDog);
        realm.commit();

        // Query
        RealmList<Dog> results = realm.where(Dog.class).contains("name", "x").findAll();

        // Queries are chainable!
        results.where().greaterThan("age", 8);

        // Link objects
        Person person = new Person("Tim");


        person.getDogs().add(myDog);

        realm.beginWrite();
        realm.add(person);
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
                    return;
                }
                realm.where(Dog.class).contains("name", "x").findAll();

            }
        }.run();

    }

}
