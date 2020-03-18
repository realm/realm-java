package io.realm

import androidx.test.platform.app.InstrumentationRegistry

/**
 * Resets the Realm Application and delete all local state.
 *
 * Trying to access any Sync or Realm App API's after this has been called has undefined
 * behavior.
 */
fun RealmApp.close() {
    // TODO Do we need to log out users?
    SyncManager.reset()
    BaseRealm.applicationContext = null // Required for Realm.init() to work
    Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
}
