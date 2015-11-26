package io.realm.examples.realmmultiprocessserviceonlyexample;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class AnotherAPKService extends Service {

    Realm realm;
    Handler handler;
    final String targetPackageName = "io.realm.examples.realmmultiprocessaexample";

    public AnotherAPKService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (realm == null) {
            Realm.enableInterprocessNotification(this, targetPackageName);
            Context extContext;
            try {
                extContext = createPackageContext(targetPackageName, Context.CONTEXT_IGNORE_SECURITY);
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
            RealmConfiguration configuration = new RealmConfiguration.Builder(extContext).build();
            realm = Realm.getInstance(configuration);
        }

        handler = new Handler(Looper.myLooper());
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(Utils.createStandaloneProcessInfo(AnotherAPKService.this));
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
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
