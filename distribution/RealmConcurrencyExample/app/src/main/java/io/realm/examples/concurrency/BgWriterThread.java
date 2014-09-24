package io.realm.examples.concurrency;

import android.util.Log;

import java.io.File;
import java.io.IOException;

import io.realm.Realm;
import io.realm.examples.concurrency.model.Dog;
import io.realm.examples.concurrency.model.Person;

public class BgWriterThread extends Thread implements KillableThread {

    public static final String TAG = BgWriterThread.class.getName();

    private Realm realm = null;
    private File realmDir = null;

    public BgWriterThread(File realmDir) {
        this.realmDir = realmDir;
    }

    public void run() {
        try {
            realm = new Realm(realmDir);
            int iterCount = 0;
            while (iterCount < 1000000 && running == true) {
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
        } catch (IOException e) {
            e.printStackTrace();
            terminate();
        }
    }

    private boolean running = true;

    @Override
    public void terminate() {
        running = false;
    }
}
