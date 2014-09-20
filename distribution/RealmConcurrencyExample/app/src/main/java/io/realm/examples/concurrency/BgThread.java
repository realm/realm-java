package io.realm.examples.concurrency;

import android.util.Log;

import java.io.File;
import java.io.IOException;

import io.realm.Realm;
import io.realm.examples.concurrency.model.Dog;
import io.realm.examples.concurrency.model.Person;

public class BgThread extends Thread {

    public static final String TAG = BgThread.class.getName();

    private Boolean threadQuitting = false;

    private Realm realm = null;
    private File realmDir = null;

    public BgThread(File realmDir) {
        this.realmDir = realmDir;
    }

    public void run() {
        try {
            realm = new Realm(realmDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int iterCount = 0;
        while (iterCount < 1000000 && threadQuitting == false) {
            if ((iterCount % 1000) == 0) {
                Log.d(TAG, "WR_OPERATION#: " + iterCount + "," + Thread.currentThread().getName());
            }

            realm.beginWrite();
            Person person = realm.create(Person.class);
            person.setName("New person");
            person.setDog(realm.create(Dog.class));
            realm.commit();
            iterCount++;
        }
    }
}
