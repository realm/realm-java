package io.realm.internal;

import io.realm.sync.SyncConfiguration;
import io.realm.sync.SyncSession;

public class SyncSessionImpl implements SyncSession {

    private final SyncConfiguration config;
    private final long nativeSyncSessionPtr;

    public SyncSessionImpl(SyncConfiguration config, long nativeSyncSessionPtr) {
        this.config = config;
        this.nativeSyncSessionPtr = nativeSyncSessionPtr;
    }

    @Override
    public void start() {
       // nativeStartSync(nativeSyncSessionPtr);
    }

    @Override
    public void stop() {
        nativeStopSync(nativeSyncSessionPtr);
    }

    private native void nativeStartSync(long nativeSyncSessionPtr);
    private native void nativeStopSync(long nativeSyncSessionPtr);
}
