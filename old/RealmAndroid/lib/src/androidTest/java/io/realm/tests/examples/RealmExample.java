package io.realm.tests.examples;

import android.test.AndroidTestCase;

import java.io.IOException;

import io.realm.tests.examples.entities.Dog;
import io.realm.tests.examples.entities.Person;
import io.realm.typed.Realm;
import io.realm.typed.RealmList;

public class RealmExample extends AndroidTestCase {


    public void testExample() {


        // Set & read properties

        // Realms are used to group data together
        Realm realm = null;
        try {
            realm = new Realm(getContext().getFilesDir());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        realm.beginWrite();
        // Create a standalone object
        Dog myDog = realm.create(Dog.class);
        myDog.setName("Rex");
        System.out.println(myDog.getName());

        // Save your object
        realm.commit();

        // Query
        RealmList<Dog> results = realm.where(Dog.class).contains("name", "x").findAll();

        // Queries are chainable!
        results.where().greaterThan("age", 8);

        // Link objects

        realm.beginWrite();

        Person person = realm.create(Person.class);

        person.setName("Tim");

        person.getDogs().add(myDog);

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
