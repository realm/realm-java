package io.realm.internal.android;

import android.os.Handler;
import android.os.Looper;

import io.realm.internal.Capabilities;
import io.realm.internal.RealmNotifier;

public class AndroidRealmNotifier extends RealmNotifier {
    private Handler handler;

    public AndroidRealmNotifier(Capabilities capabilities) {
        if (capabilities.canDeliverNotification()) {
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

    @Override
    public void post(Runnable runnable) {
        if (handler != null) {
            handler.post(runnable);
        }
    }

    @Override
    public void close() {
        super.close();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
