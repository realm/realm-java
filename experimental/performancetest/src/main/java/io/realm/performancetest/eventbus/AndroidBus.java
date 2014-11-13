package io.realm.performancetest.eventbus;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Added support for posting events on non UI thread.
 * See https://github.com/square/otto/issues/38
 */
public class AndroidBus extends Bus {

    private final Handler mainThread = new Handler(Looper.getMainLooper());

    public AndroidBus(ThreadEnforcer enforcer) {
        super(enforcer);
    }

    public void postOnUiThread(final Object event) {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                post(event);
            }
        });
    }
}