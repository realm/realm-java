package io.realm.examples.concurrency;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.examples.concurrency.model.Person;

public class BgReaderThread extends Thread implements KillableThread {

    public static final String TAG = BgReaderThread.class.getName();

    private Context context = null;
    private Realm realm = null;

    public BgReaderThread(Context context) {
        this.context = context;
    }

    public void run() {
        realm = Realm.getInstance(context);

        while (running) {
            try {
                RealmQuery realmQuery = realm.where(Person.class);
                List<Person> list = realmQuery.findAll();
                Log.d(TAG, "First item: " + realmQuery.findFirst());
            } catch (Exception e) {
                e.printStackTrace();
                terminate();
            }
        }
    }

    private boolean running = true;

    @Override
    public void terminate() {
        running = false;
    }
}
