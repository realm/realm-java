package io.realm.internal.android;

import android.os.Handler;
import android.os.Looper;

import io.realm.internal.Capabilities;
import io.realm.internal.Keep;
import io.realm.internal.RealmNotifier;
import io.realm.internal.SharedRealm;


/**
 * {@link RealmNotifier} implementation for Android.
 */
@Keep
public class AndroidRealmNotifier extends RealmNotifier {
    private Handler handler;

    public AndroidRealmNotifier(SharedRealm sharedRealm, Capabilities capabilities) {
        super(sharedRealm);
        if (capabilities.canDeliverNotification()) {
            handler = new Handler(Looper.myLooper());
        } else {
            handler = null;
        }
    }

    @Override
    public boolean post(Runnable runnable) {
        return handler != null && handler.post(runnable);
    }
}
