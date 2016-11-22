package io.realm;

import java.util.Collection;

/**
 * Created by Nabil on 22/11/2016.
 */

//TODO use user identity as default KEY
class ObjectStoreUserStore implements UserStore {
    //TODO expose a constructor that can accept an AES key so we can enable encryption
    // User is responsible of creating a single instance of this
    ObjectStoreUserStore (String path, byte[] aesKey) {
        ObjectStoreSyncManager.configureMetaDataSystem(path, aesKey);
    }

    @Override
    public SyncUser put(SyncUser user) {
        String userJson = user.toJson();
        // create or update token (userJson) using identity
        ObjectStoreSyncManager.updateOrCreateUser(user.getIdentity(), userJson);
        return user;
    }

    @Override
    public SyncUser get() {
        String userJson = ObjectStoreSyncManager.getCurrentUser();
        SyncUser currentUser = SyncUser.fromJson(userJson);
        return currentUser;
    }

    @Override
    public void remove() {
        ObjectStoreSyncManager.logoutCurrentUser();
    }

    @Override
    public Collection<SyncUser> allUsers() {
        return null;
    }

    @Override
    public void clear() {

    }
}
