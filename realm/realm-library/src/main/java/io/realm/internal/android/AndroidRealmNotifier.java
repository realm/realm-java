package io.realm.internal.android;

import android.os.Handler;
import android.os.Looper;

import io.realm.internal.RealmNotifier;
import io.realm.internal.SharedRealm;

public class AndroidRealmNotifier extends RealmNotifier {
    private final Handler handler;

    public AndroidRealmNotifier() {
        if (SharedRealm.getCapabilities().canDeliverNotification()) {
            handler = new Handler(Looper.myLooper());
        } else {
            handler = null;
        }
    }

    @Override
    public void postAtFrontOfQueue(Runnable runnable) {
        if (handler != null) {
            handler.postAtFrontOfQueue(runnable);
        }
    }
}
