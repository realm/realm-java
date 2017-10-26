package io.realm.examples.realmmultiprocessexample;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import io.realm.Realm;

public class AnotherProcessService extends Service {

    Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();

        handler = new Handler(Looper.myLooper());
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(Utils.createStandaloneProcessInfo(AnotherProcessService.this));
                realm.commitTransaction();
                realm.close();
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
