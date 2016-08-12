package io.realm.internal;

import io.realm.objectserver.ObjectServerConfiguration;
import io.realm.objectserver.SyncSession;

public class SyncSessionImpl implements SyncSession {

    private final ObjectServerConfiguration config;
    private final long nativeSyncSessionPtr;

    public SyncSessionImpl(ObjectServerConfiguration config, long nativeSyncSessionPtr) {
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
    //TODO just delete the pointer nativeSyncSessionPtr to stop syncing
    private native void nativeStopSync(long nativeSyncSessionPtr);
}
