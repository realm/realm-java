package io.realm.typed;

import android.test.AndroidTestCase;

import io.realm.typed.entities.Dog;
import io.realm.typed.entities.Person;

public class RealmExample extends AndroidTestCase {


    public void testExample() {

        // Create a standalone object
        Dog myDog = new Dog();

        // Set & read properties
        myDog.setName("Rex");
        System.out.println(myDog.getName());

        // Realms are used to group data together
        Realm realm = new Realm(getContext());

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

                Realm realm = new Realm(getContext());
                realm.where(Dog.class).contains("name", "x").findAll();

            }
        }.run();

    }

}
