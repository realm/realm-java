package io.realm.sync;

import io.realm.internal.SharedGroup;
import io.realm.internal.SyncSessionImpl;

public class SyncManager {

    private SyncManager() {
    }

    public static SyncSession apply(SyncConfiguration config) {

        SharedGroup realm = new SharedGroup(config);
        long nativeSessionPointer = realm.startSession(config.getServer());
        SyncSession session = new SyncSessionImpl(config, nativeSessionPointer);
        config.getSyncPolicy().apply(session);
        return session;
    }

}
//
//    // Public
//    SyncConfiguration syncConfig = SyncConfiguration.from(realmConfig)
//            .server("")
//            .syncPolicy(new Manual())
//            .userToken(new UserTokenRequest() {
//
//            });
//.build();
//
////        SyncSession SyncManager.register(SyncConfiguration);
//        SyncInterface syncmanager  = SyncManager.apply(SyncConfiguration);
////        SyncManager.shutDownAll();
////        SyncManager.add(SyncConfiguration);
////        SyncManagerInterface syncmanager = new SyncManager(manualSyncConfig2);
////        SyncManager.init();
//
////        SyncManager.add(SyncConfiguration);
//
//        // do some work with ync conf
////        SyncInterface syncmanager = SyncManager.from(manualSyncConfig2);
//
////        static RealmSync syncmanager = RealmSync.with(syncConfig, syncConfig1);
////        SyncInterface syncmanager.get(manualSyncConfig2);
//
//        //syncmanager.start();
//
//
//        // Private
//        SyncConfiguration  manualSyncConfig2;
//        manager.register(syncConfig)
//        manager.register(manualSyncConfig2);
//
//
//
//
////        SyncManager manager = SyncManager.getInstance(syncConfig);
////        manager.startSync();
//
