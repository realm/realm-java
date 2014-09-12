package io.realm.realmintroexample;

import android.app.Activity;
import android.util.Log;

import java.util.Arrays;

import io.realm.Realm;
import io.realm.RealmList;

public class IntroExample {
    private Activity activity;
    private static final String TAG = "REALM";

    public IntroExample(Activity activity) {
        this.activity = activity;
    }

    public void WriteAndRead() throws java.io.IOException {
        // open a default realm
        Realm realm = new Realm(activity.getFilesDir());

        // Add ten persons in one write transaction
        realm.beginWrite();
        Dog dog = realm.create(Dog.class);
        dog.setName("Fido");

        for (int i = 0; i<10; i++) {
            Person person = realm.create(Person.class);
            person.setName("Person no. " + i);
            person.setAge(i);
            person.setDog(dog);
            person.setData(new byte[] {1,2,3});
        }
        realm.commit();

        // Implicit read transactions allow you to access your objects
        Log.i(TAG, "Number of persons: " + realm.allObjects(Person.class).size());

        // Iterate over all objects
        for (Person p : realm.allObjects(Person.class)) {
            Log.i(TAG, String.format("%s : %d : %s : %b", p.getName(), p.getAge(), p.getDog().getName(), Arrays.equals(p.getData(), new byte[] {1,2,3})));
        }
    }


    public void QueryYourObjects() throws java.io.IOException {
        Realm realm = new Realm(activity.getFilesDir());

        Log.i(TAG, "Number of persons: " + realm.allObjects(Person.class).size());

        // Find all persons where age > 5
        RealmList<Person> result = realm.where(Person.class).greaterThan("age", 5).findAll();
        Log.i(TAG, "Size of result set: " + result.size());

        // Remove oldest persons
        realm.beginWrite();
        result.clear();
        realm.commit();

        Log.i(TAG, "Number of persons: " + realm.allObjects(Person.class).size());
    }
}