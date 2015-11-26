package io.realm.examples.realmmultiprocessaexample;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import io.realm.Realm;

public class AnotherProcessService extends Service {

    Realm realm;
    Handler handler;

    public AnotherProcessService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (realm == null) {
            realm = Realm.getDefaultInstance();
        }

        handler = new Handler(Looper.myLooper());
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(Utils.createStandaloneProcessInfo(AnotherProcessService.this));
                realm.commitTransaction();
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
            realm = null;
            Realm.disableInterprocessNotification();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
