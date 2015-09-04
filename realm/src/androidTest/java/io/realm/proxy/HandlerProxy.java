package io.realm.proxy;

import android.os.Handler;
import android.os.Message;

/**
 * Created by Nabil on 21/08/15.
 */
public abstract class HandlerProxy extends Handler {
    private final Handler handler;

    public HandlerProxy(Handler handler) {
        this.handler = handler;
    }

//    @Override
//    public void handleMessage(Message msg) {
//        onPreHandleMessage ();
//        handler.handleMessage(msg);
//    }


    public void postAtFront (Runnable runnable) {
        handler.postAtFrontOfQueue(runnable);
    }

    @Override
    public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        boolean eventConsumed = onInterceptMessage(msg.what);
        return !eventConsumed && handler.sendMessageAtTime(msg, uptimeMillis);

    }

//    @Override
//    public void dispatchMessage(Message msg) {
//        if (msg.what == 39088169) { //Realm.REALM_COMPLETED_ASYNC_QUERY
//            onPreHandleMessage();
//        } else if (msg.what == 24157817) { //Realm.REALM_UPDATE_ASYNC_QUERIES
//            onPreHandleMessage();
//        }
//        handler.dispatchMessage(msg);
//    }


    // called on the Handler's Thread
    public abstract boolean onInterceptMessage(int what);
}
