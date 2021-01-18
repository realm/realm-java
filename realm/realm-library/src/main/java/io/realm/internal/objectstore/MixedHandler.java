package io.realm.internal.objectstore;

import io.realm.Mixed;


public interface MixedHandler {
    void handleItem(long listPtr, Mixed mixed);
}
