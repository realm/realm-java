package io.realm.realmintroexample;

import android.app.Activity;
import android.util.Log;

import io.realm.Realm;
import io.realm.RealmList;

public class IntroExample {
    private Activity activity;
    private static final String TAG = "REALM";

    public IntroExample(Activity activity) {
        this.activity = activity;
    }

    public void WriteAndRead() {
        // open a default realm
        Realm realm;
        try {
            realm = new Realm(activity.getFilesDir());
        } catch (java.io.IOException e) {
            return;
        }

        // Add ten persons in small transactions
        for (int i = 0; i<10; i++) {
            realm.beginWrite();
            Person person = realm.create(Person.class);
            person.setName("Person no. " + i);
            person.setAge(i);
            realm.commit();
        }

        // Implicit read transactions allow you to access your objects
        Log.i(TAG, "Number of persons: " + realm.allObjects(Person.class).size());

        // Iterate over all objects
        for (Person p : realm.allObjects(Person.class)) {
            Log.i(TAG, p.getName() + ":" + p.getAge());
        }
    }


    public void QueryYourObjects() {
        Realm realm;
        try {
            realm = new Realm(activity.getFilesDir());
        } catch (Exception e) {
            return;
        }

        Log.i(TAG, "Number of persons: " + realm.allObjects(Person.class).size());

        // Find all objects where age > 5
        RealmList<Person> result = realm.where(Person.class).greaterThan("age", 5).findAll();
        Log.i(TAG, "Size of result set: " + result.size());

        // Remove oldest persons
        realm.beginWrite();
        result.clear();
        realm.commit();

        Log.i(TAG, "Number of persons: " + realm.allObjects(Person.class).size());
    }

    public void ListenForUpdates() {
        Realm realm;
        try {
            realm = new Realm(activity.getFilesDir());
        } catch (Exception e) {
            return;
        }
    }
}