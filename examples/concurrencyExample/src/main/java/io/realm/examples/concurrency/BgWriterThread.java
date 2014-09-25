package io.realm.examples.concurrency;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import io.realm.Realm;
import io.realm.examples.concurrency.model.Dog;
import io.realm.examples.concurrency.model.Person;

public class BgWriterThread extends Thread implements KillableThread {

    public static final String TAG = BgWriterThread.class.getName();

    private Realm   realm   = null;
    private Context context = null;

    public BgWriterThread(Context context) {
        this.context = context;
    }

    public void run() {
        realm = Realm.getInstance(context);
        int iterCount = 0;

        realm.beginTransaction();
        while (iterCount < 1000000 && running == true) {
            if ((iterCount % 1000) == 0) {
                Log.d(TAG, "WR_OPERATION#: " + iterCount + "," + Thread.currentThread().getName());
            }

            Person person = realm.createObject(Person.class);
            person.setName("New person");
//            person.setDog(realm.createObject(Dog.class));
            iterCount++;
        }
        realm.commitTransaction();
    }

    private boolean running = true;

    @Override
    public void terminate() {
        running = false;
    }
}
