package io.realm.sync;

import java.net.URL;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class SyncConfiguration {

    private final SyncPolicy syncPolicy;
    private final String userToken;
    final RealmConfiguration configuration;

    public RealmConfiguration getConfiguration() {
        return configuration;
    }

    public String getServer() {
        return server;
    }

    private final String server;

    //TODO have a builder
    public SyncConfiguration(RealmConfiguration configuration, String server) {
        this.configuration = configuration;
        this.server = server;
        this.syncPolicy = new RealtimeSyncPolicy();
        this.userToken = "boom";
    }

    public SyncPolicy getSyncPolicy() {
        return syncPolicy;
    }

    public String getUserToken() {
        return userToken;
    }
}
