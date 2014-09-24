package io.realm.examples.concurrency;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BgSpawningService extends Service {

    public static final String TAG = BgSpawningService.class.getName();

    private Boolean serviceQuitting = false;

    public static final String REALM_FILE_EXTRA = "RealmFileExtra";

    private File realmPath = null;

    private List<KillableThread> allThreads = null;

    BgWriterThread wT = null;
    BgReaderThread rT = null;

    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.realmPath = (File)intent.getSerializableExtra(REALM_FILE_EXTRA);
        try {
            allThreads = new ArrayList<KillableThread>();
            wT = new BgWriterThread(realmPath);
            allThreads.add(wT);
            wT.start();
            rT = new BgReaderThread(realmPath);
            allThreads.add(rT);
            rT.start();
        } catch (Exception e) {
            e.printStackTrace();
            quit();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        quit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void quit() {
        this.serviceQuitting = true;
        for (KillableThread t : allThreads) {
            t.terminate();
        }
    }

}
