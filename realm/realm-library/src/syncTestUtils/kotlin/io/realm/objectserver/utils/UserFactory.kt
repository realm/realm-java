/*
 * Copyright 2016 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.realm.objectserver.utils

import android.os.Handler
import android.os.HandlerThread
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.TestHelper
import io.realm.log.RealmLog

// Helper class to retrieve users with same IDs even in multi-processes.
// Must be in `io.realm.objectserver` to work around package protected methods.
// This require Realm.init() to be called before using this class.
class UserFactory private constructor(// Since the integration tests need to use the same user for different processes, we create a new user name when the
        // test starts and store it in a Realm. Then it can be retrieved for every process.
        private val userName: String) {

    companion object {
        const val PASSWORD = "myPassw0rd"
        private var instance: UserFactory? = null
        private var configuration: RealmConfiguration? = null

        // Run initializer here to make it possible to ensure that Realm.init has been called.
        // It is unpredictable when the static initializer is running
        @kotlin.jvm.Synchronized
        private fun initFactory(forceReset: Boolean) {
            if (configuration == null || forceReset) {
                val builder: RealmConfiguration.Builder = Builder().name("user-factory.realm")
                configuration = builder.build()
            }
        }

        // The @Before method will be called before the looper tests finished. We need to find a better place to call this.
        fun clearInstance() {
            val realm: Realm = Realm.getInstance(configuration)
            realm.beginTransaction()
            realm.delete(UserFactoryStore::class.java)
            realm.commitTransaction()
            realm.close()
        }

        @kotlin.jvm.Synchronized
        fun getInstance(): UserFactory? {
            if (instance == null) {
                initFactory(false)
                val realm: Realm = Realm.getInstance(configuration)
                val store: UserFactoryStore = realm.where(UserFactoryStore::class.java).findFirst()
                if (store == null || store.getUserName() == null) {
                    throw IllegalStateException("Current user has not been set. Call resetInstance() first.")
                }
                instance = UserFactory(store.getUserName())
                realm.close()
            }
            RealmLog.debug("UserFactory.getInstance, the default user is " + instance!!.userName + " .")
            return instance
        }

        /**
         * Blocking call that logs out all users
         */
        fun logoutAllUsers() {
            val allUsersLoggedOut = CountDownLatch(1)
            val ht = HandlerThread("LoggingOutUsersThread")
            ht.start()
            val handler = Handler(ht.getLooper())
            handler.post(object : Runnable() {
                @Override
                fun run() {
//                Map<String, User> users = App.allUsers();
//                for (User user : users.values()) {
//                    App.logout(user);
//                }
                    TestHelper.waitForNetworkThreadExecutorToFinish()
                    allUsersLoggedOut.countDown()
                }
            })
            TestHelper.awaitOrFail(allUsersLoggedOut)
            ht.quit()
            try {
                ht.join(TimeUnit.SECONDS.toMillis(TestHelper.SHORT_WAIT_SECS))
            } catch (e: InterruptedException) {
                throw AssertionError("LoggingOutUsersThread failed to finish in time")
            }
        }
    }

}
