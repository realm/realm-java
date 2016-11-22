package io.realm;

/**
 * Created by Nabil on 22/11/2016.
 */

class ObjectStoreSyncManager {
    // synchronized already in OS ?

    static native String getCurrentUser ();
        // return json data (token) of the current logged in user

    public static long getAllUsers() {
        // SyncManager::shared().all_users()
        return 0;
    }

    static native void updateOrCreateUser(String identity, String jsonToken);
        // SyncManager::shared().get_user([model.refreshToken.tokenData.identity UTF8String],
        // [model.refreshToken.token UTF8String],
        // std::move(server_url));

    static native void logoutCurrentUser ();
        // SyncUser#logout


    static native void configureMetaDataSystem(String baseFile, byte[] aesKey);
        // SyncManager::shared().configure_file_system(rootDirectory.path.UTF8String, mode);
}
