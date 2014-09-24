package io.realm.internal.android;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This thread continuously receives and dispatches messages regarding changes in realms.
 */
public class LooperThread extends Thread {
    // Message types
    public static final int REALM_CHANGED  = 3;

    public static final Map<Handler, Integer> handlers = new ConcurrentHashMap<Handler, Integer>();
    public Handler handler;
    private static LooperThread instance;

    // private because it's a singleton
    private LooperThread() {}

    // thread safe static constructor
    public static LooperThread getInstance() {
        if (instance == null) {
            synchronized (LooperThread.class) {
                if (instance == null) {
                    instance = new LooperThread();
                }
            }
        }
        return instance;
    }

    @Override
    public void run() {
        Looper.prepare();

        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if(message.arg1 == REALM_CHANGED) {
                    for (Map.Entry<Handler, Integer> entry : handlers.entrySet()) {
                        if (entry.getValue() == message.arg2) {
                            if (!entry.getKey().hasMessages(REALM_CHANGED)) {
                                entry.getKey().sendEmptyMessage(REALM_CHANGED);
                            }
                        }
                    }
                }
            }
        };

        Looper.loop();
    }
}
