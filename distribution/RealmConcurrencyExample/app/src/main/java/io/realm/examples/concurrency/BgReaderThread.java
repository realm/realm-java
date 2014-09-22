package io.realm.examples.concurrency;

import android.util.Log;

import java.io.File;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.examples.concurrency.model.Person;

public class BgReaderThread extends Thread implements KillableThread {

    public static final String TAG = BgReaderThread.class.getName();

    private File realmDir = null;
    private Realm realm = null;

    public BgReaderThread(File realmDir) {
        this.realmDir = realmDir;
    }

    public void run() {
        try {
            realm = new Realm(realmDir);
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (running) {
            try {
                RealmQuery realmQuery = realm.where(Person.class);
                List<Person> list = realmQuery.findAll();
                Log.d(TAG, "First item: " + realmQuery.findFirst());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean running = true;

    @Override
    public void terminate() {
        running = false;
    }
}
