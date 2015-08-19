//package io.realm;
//
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.os.Message;
//
//import java.lang.ref.WeakReference;
//import java.util.Map;
//
///**
// * Created by Nabil on 04/08/15.
// */
//public class WorkerThread extends HandlerThread implements Handler.Callback, RealmChangeListener {
//    private final Handler handler;
//    private final Realm realm;
//
//    // I can't use the original handover query pointer (because it was imported from a different
//    // thread than this one). If we use the original query then I need to export from the original thread
//
//    public WorkerThread(RealmConfiguration realmConfiguration) {
//        super("Async Queries Worker Thread");
//        start();
//        handler = new Handler(this);
//        realm = Realm.getInstance(realmConfiguration);
//        realm.addChangeListener(this);
//    }
//
//
//    @Override
//    public boolean handleMessage(Message msg) {
//        switch (msg.what) {
//
//        }
//        return false;
//    }
//
//    @Override
//    public void onChange() {
//        // update registered RealmResults per Thread
//        for (Map.Entry<Handler, Map<WeakReference<RealmResults<?>>, Long>> entry : Realm.asyncQueries.entrySet()) {
//            updateRealmResultsForThread(entry.getKey(), entry.getValue());
//        }
//    }
//
//    private void updateRealmResultsForThread(Handler thread, Map<WeakReference<RealmResults<?>>, Long> queries) {
//
//    }
//}
