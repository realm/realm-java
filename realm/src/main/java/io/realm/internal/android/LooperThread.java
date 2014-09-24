package io.realm.internal.android;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * This thread continuously receives and dispatches messages regarding changes in realms.
 */
public class LooperThread extends Thread {
    // Message types
    public static final int ADD_HANDLER    = 1;
    public static final int REMOVE_HANDLER = 2;
    public static final int REALM_CHANGED  = 3;

    private final Map<Handler, Integer> handlers = new HashMap<Handler, Integer>();
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
                if (message.obj instanceof Handler) {
                    // message.obj contains the handler
                    // message.arg1 contains the operation type (add/remove)
                    // message.arg2 contains the realm id
                    Handler messageHandler = (Handler) message.obj;
                    switch (message.arg1) {
                        case ADD_HANDLER:
                            handlers.put(messageHandler, message.arg2);
                            break;
                        case REMOVE_HANDLER:
                            handlers.remove(messageHandler);
                            break;
                        default:
                            break;
                    }
                } else if(message.arg1 == REALM_CHANGED) {
                    for (Map.Entry<Handler, Integer> entry : handlers.entrySet()) {
                        if (entry.getValue() == message.arg2) {
                            entry.getKey().sendEmptyMessage(REALM_CHANGED);
                        }
                    }
                }
            }
        };

        Looper.loop();
    }
}
