package io.realm.examples.concurrency;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import io.realm.Realm;
import io.realm.examples.concurrency.model.Dog;
import io.realm.examples.concurrency.model.Person;

import java.io.File;
import java.io.IOException;

public class BgService extends IntentService {

    public static final String TAG = BgService.class.getName();

    private Boolean serviceQuitting = false;

    public static final String REALM_FILE_EXTRA = "RealmFileExtra";

    private Realm realm = null;
    private File realmPath = null;

    public BgService() {
        super(BgService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Starting intent...");

        this.realmPath = (File) intent.getSerializableExtra(REALM_FILE_EXTRA);
        try {
            realm = new Realm(realmPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            int iterCount = 0;
            while (iterCount < 1000000 && serviceQuitting == false) {
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
        } catch (Exception e) {
            Log.d(TAG, "Quitting Service");
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        this.serviceQuitting = true;
    }

}
