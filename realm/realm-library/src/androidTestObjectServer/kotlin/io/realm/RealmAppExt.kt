package io.realm

import io.realm.admin.ServerAdmin

/**
 * Resets the Realm Application and delete all local state.
 *
 * Trying to access any Sync or Realm App API's after this has been called has undefined
 * behavior.
 */
fun RealmApp.close() {
    ServerAdmin().deleteAllUsers()
    SyncManager.reset()
    BaseRealm.applicationContext = null // Required for Realm.init() to work
}

/**
 * Helper function for quickly logging in test users.
 * This only works if users in the Realm Application are configured to be automatically confirmed.
 */
fun RealmApp.registerUserAndLogin(email: String, password: String): RealmUser {
    emailPasswordAuthProvider.registerUser(email, password)
    return login(RealmCredentials.emailPassword(email, password))
}
