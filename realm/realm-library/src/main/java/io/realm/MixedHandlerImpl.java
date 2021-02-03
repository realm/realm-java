package io.realm;

import io.realm.internal.objectstore.MixedHandler;
import io.realm.internal.objectstore.OsObjectBuilder;


public class MixedHandlerImpl implements MixedHandler {

    @Override
    public void handleItem(long listPtr, Mixed mixed) {
        OsObjectBuilder.nativeAddMixedListItem(listPtr, mixed.getNativePtr());
    }
}
