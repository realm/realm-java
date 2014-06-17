package io.realm.typed;

import android.os.Handler;

import java.lang.ref.WeakReference;

public class RealmEventHandler implements Runnable {

    private static int count = 0;
    private int myCount;
    private WeakReference<Realm> realmRef;

    public RealmEventHandler(Realm realm) {
        realmRef = new WeakReference<Realm>(realm);
        myCount = count++;
    }

    @Override
    public void run() {

        Realm realm = realmRef.get();

        if(realm != null && realm.runEventHandler) {

            if (realm.hasChanged()) {
                realm.sendNotifications();
            }
            new Handler().postDelayed(this, 100);

            System.out.println("check for changes " + myCount);
        }

    }
}
