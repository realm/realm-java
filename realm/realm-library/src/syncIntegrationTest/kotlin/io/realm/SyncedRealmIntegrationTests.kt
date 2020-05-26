/*
 * Copyright 2020 Realm Inc.
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
package io.realm

import android.os.SystemClock
import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.realm.SyncTestUtils.Companion.createTestUser
import io.realm.entities.StringOnly
import io.realm.exceptions.DownloadingRealmInterruptedException
import io.realm.exceptions.RealmMigrationNeededException
import io.realm.internal.OsRealmConfig
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.log.RealmLogger
import io.realm.objectserver.utils.Constants
import io.realm.rule.RunTestInLooperThread
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Catch all class for tests that not naturally fit anywhere else.
 */
@RunWith(AndroidJUnit4::class)
class SyncedRealmIntegrationTests : StandardIntegrationTest() {
    @Test
    @RunTestInLooperThread
    @Throws(InterruptedException::class)
    fun loginLogoutResumeSyncing() {
        val username = UUID.randomUUID().toString()
        val password = "password"
        var user: SyncUser = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL)
        val config: SyncConfiguration = user.createConfiguration(Constants.USER_REALM)
                .schema(StringOnly::class.java)
                .sessionStopPolicy(OsRealmConfig.SyncSessionStopPolicy.IMMEDIATELY)
                .build()
        val realm = Realm.getInstance(config)
        realm.beginTransaction()
        realm.createObject(StringOnly::class.java).chars = "Foo"
        realm.commitTransaction()
        SyncManager.getSession(config).uploadAllLocalChanges()
        user.logOut()
        realm.close()
        try {
            Assert.assertTrue(Realm.deleteRealm(config))
        } catch (e: IllegalStateException) {
            // FIXME: We don't have a way to ensure that the Realm instance on client thread has been
            // closed for now https://github.com/realm/realm-java/issues/5416
            if (e.message!!.contains("It's not allowed to delete the file")) {
                // retry after 1 second
                SystemClock.sleep(1000)
                Assert.assertTrue(Realm.deleteRealm(config))
            }
        }
        user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, false), Constants.AUTH_URL)
        val config2: SyncConfiguration = user.createConfiguration(Constants.USER_REALM)
                .schema(StringOnly::class.java)
                .build()
        val realm2 = Realm.getInstance(config2)
        SyncManager.getSession(config2).downloadAllServerChanges()
        realm2.refresh()
        Assert.assertEquals(1, realm2.where(StringOnly::class.java).count())
        realm2.close()
        looperThread.testComplete()
    }

    @Test
    @UiThreadTest
    fun waitForInitialRemoteData_mainThreadThrows() {
        val user: SyncUser = createTestUser(Constants.AUTH_URL)
        val config: SyncConfiguration = user.createConfiguration(Constants.USER_REALM)
                .waitForInitialRemoteData()
                .build()
        var realm: Realm? = null
        try {
            realm = Realm.getInstance(config)
            Assert.fail()
        } catch (ignore: IllegalStateException) {
        } finally {
            realm?.close()
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun waitForInitialRemoteData() {
        val username = UUID.randomUUID().toString()
        val password = "password"
        var user: SyncUser = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL)

        // 1. Copy a valid Realm to the server (and pray it does it within 10 seconds)
        val configOld: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .schema(StringOnly::class.java)
                .sessionStopPolicy(OsRealmConfig.SyncSessionStopPolicy.IMMEDIATELY)
                .build()
        var realm = Realm.getInstance(configOld)
        realm.executeTransaction { realm ->
            for (i in 0..9) {
                realm.createObject(StringOnly::class.java).chars = "Foo$i"
            }
        }
        SyncManager.getSession(configOld).uploadAllLocalChanges()
        realm.close()
        user.logOut()

        // 2. Local state should now be completely reset. Open the same sync Realm but different local name again with
        // a new configuration which should download the uploaded changes (pray it managed to do so within the time frame).
        user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password), Constants.AUTH_URL)
        val config: SyncConfiguration = user.createConfiguration(Constants.USER_REALM)
                .name("newRealm")
                .schema(StringOnly::class.java)
                .waitForInitialRemoteData()
                .build()
        realm = Realm.getInstance(config)
        realm.executeTransaction { realm ->
            for (i in 0..9) {
                realm.createObject(StringOnly::class.java).chars = "Foo 1$i"
            }
        }
        try {
            Assert.assertEquals(20, realm.where(StringOnly::class.java).count())
        } finally {
            realm.close()
        }
    }

    // This tests will start and cancel getting a Realm 10 times. The Realm should be resilient towards that
    // We cannot do much better since we cannot control the order of events internally in Realm which would be
    // needed to correctly test all error paths.
    @Test
    @Ignore("Sync somehow keeps a Realm alive, causing the Realm.deleteRealm to throw " +
            " https://github.com/realm/realm-java/issues/5416")
    @Throws(InterruptedException::class)
    fun waitForInitialData_resilientInCaseOfRetries() {
        val credentials = SyncCredentials.usernamePassword(UUID.randomUUID().toString(), "password", true)
        val user: SyncUser = SyncUser.logIn(credentials, Constants.AUTH_URL)
        val config: SyncConfiguration = user.createConfiguration(Constants.USER_REALM)
                .waitForInitialRemoteData()
                .build()
        for (i in 0..9) {
            val t = Thread(Runnable {
                var realm: Realm? = null
                try {
                    // This will cause the download latch called later to immediately throw an InterruptedException.
                    Thread.currentThread().interrupt()
                    realm = Realm.getInstance(config)
                } catch (ignored: DownloadingRealmInterruptedException) {
                    Assert.assertFalse(File(config.path).exists())
                } finally {
                    if (realm != null) {
                        realm.close()
                        Realm.deleteRealm(config)
                    }
                }
            })
            t.start()
            t.join()
        }
    }

    // This tests will start and cancel getting a Realm 10 times. The Realm should be resilient towards that
    // We cannot do much better since we cannot control the order of events internally in Realm which would be
    // needed to correctly test all error paths.
    @Test
    @RunTestInLooperThread
    @Ignore("See https://github.com/realm/realm-java/issues/5373")
    fun waitForInitialData_resilientInCaseOfRetriesAsync() {
        val credentials = SyncCredentials.usernamePassword(UUID.randomUUID().toString(), "password", true)
        val user: SyncUser = SyncUser.logIn(credentials, Constants.AUTH_URL)
        val config: SyncConfiguration = user.createConfiguration(Constants.USER_REALM)
                .sessionStopPolicy(OsRealmConfig.SyncSessionStopPolicy.IMMEDIATELY)
                .directory(configurationFactory.getRoot())
                .waitForInitialRemoteData()
                .build()
        val randomizer = Random()
        for (i in 0..9) {
            val task = Realm.getInstanceAsync(config, object : Realm.Callback() {
                override fun onSuccess(realm: Realm) {
                    Assert.fail()
                }

                override fun onError(exception: Throwable) {
                    Assert.fail(exception.toString())
                }
            })
            SystemClock.sleep(randomizer.nextInt(5).toLong())
            task.cancel()
        }
        looperThread.testComplete()
    }

    @Test
    @Throws(InterruptedException::class)
    fun waitForInitialRemoteData_readOnlyTrue() {
        val username = UUID.randomUUID().toString()
        val password = "password"
        var user: SyncUser = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL)

        // 1. Copy a valid Realm to the server (and pray it does it within 10 seconds)
        val configOld: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .schema(StringOnly::class.java)
                .build()
        var realm = Realm.getInstance(configOld)
        realm.executeTransaction { realm ->
            for (i in 0..9) {
                realm.createObject(StringOnly::class.java).chars = "Foo$i"
            }
        }
        SyncManager.getSession(configOld).uploadAllLocalChanges()
        realm.close()
        user.logOut()

        // 2. Local state should now be completely reset. Open the Realm again with a new configuration which should
        // download the uploaded changes (pray it managed to do so within the time frame).
        user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, false), Constants.AUTH_URL)
        val configNew: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .name("newRealm")
                .waitForInitialRemoteData()
                .readOnly()
                .schema(StringOnly::class.java)
                .build()
        Assert.assertFalse(configNew.realmExists())
        realm = Realm.getInstance(configNew)
        Assert.assertEquals(10, realm.where(StringOnly::class.java).count())
        realm.close()
        user.logOut()
    }

    @Test
    fun waitForInitialRemoteData_readOnlyTrue_throwsIfWrongServerSchema() {
        val credentials = SyncCredentials.usernamePassword(UUID.randomUUID().toString(), "password", true)
        val user: SyncUser = SyncUser.logIn(credentials, Constants.AUTH_URL)
        val configNew: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .waitForInitialRemoteData()
                .readOnly()
                .schema(StringOnly::class.java)
                .build()
        Assert.assertFalse(configNew.realmExists())
        var realm: Realm? = null
        try {
            // This will fail, because the server Realm is completely empty and the Client is not allowed to write the
            // schema.
            realm = Realm.getInstance(configNew)
            Assert.fail()
        } catch (ignore: RealmMigrationNeededException) {
        } finally {
            realm?.close()
            user.logOut()
        }
    }

    @Test
    fun waitForInitialRemoteData_readOnlyFalse_upgradeSchema() {
        val credentials = SyncCredentials.usernamePassword(UUID.randomUUID().toString(), "password", true)
        val user: SyncUser = SyncUser.logIn(credentials, Constants.AUTH_URL)
        val config: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .waitForInitialRemoteData() // Not readonly so Client should be allowed to write schema
                .schema(StringOnly::class.java) // This schema should be written when opening the empty Realm.
                .schemaVersion(2)
                .build()
        Assert.assertFalse(config.realmExists())
        val realm = Realm.getInstance(config)
        try {
            Assert.assertEquals(0, realm.where(StringOnly::class.java).count())
        } finally {
            realm.close()
            user.logOut()
        }
    }

    @Ignore("FIXME: Re-enable this once we can test againt a proper Stitch server")
    @Test
    @Throws(InterruptedException::class)
    fun defaultRealm() {
        val credentials = SyncCredentials.usernamePassword(UUID.randomUUID().toString(), "test", true)
        val user: SyncUser = SyncUser.logIn(credentials, Constants.AUTH_URL)
        val config: SyncConfiguration = user.getDefaultConfiguration()
        val realm = Realm.getInstance(config)
        SyncManager.getSession(config).downloadAllServerChanges()
        realm.refresh()
        try {
            Assert.assertTrue(realm.isEmpty)
        } finally {
            realm.close()
            user.logOut()
        }
    }

    // Check that custom headers and auth header renames are correctly used for HTTP requests
    // performed from Java.
    @Test
    @RunTestInLooperThread
    fun javaRequestCustomHeaders() {
        SyncManager.addCustomRequestHeader("Foo", "bar")
        SyncManager.setAuthorizationHeaderName("RealmAuth")
        runJavaRequestCustomHeadersTest()
    }

    // Check that custom headers and auth header renames are correctly used for HTTP requests
    // performed from Java.
    @Test
    @RunTestInLooperThread
    fun javaRequestCustomHeaders_specificHost() {
        SyncManager.addCustomRequestHeader("Foo", "bar", Constants.HOST)
        SyncManager.setAuthorizationHeaderName("RealmAuth", Constants.HOST)
        runJavaRequestCustomHeadersTest()
    }

    private fun runJavaRequestCustomHeadersTest() {
        val credentials = SyncCredentials.usernamePassword(UUID.randomUUID().toString(), "test", true)
        val headerSet = AtomicBoolean(false)
        RealmLog.setLevel(LogLevel.ALL)
        val logger = RealmLogger { level: Int, tag: String?, throwable: Throwable?, message: String? ->
            if (level == LogLevel.TRACE && message!!.contains("Foo: bar")
                    && message.contains("RealmAuth: ")) {
                headerSet.set(true)
            }
        }
        looperThread.runAfterTest({ RealmLog.remove(logger) })
        RealmLog.add(logger)
        val user: SyncUser = SyncUser.logIn(credentials, Constants.AUTH_URL)
        try {
            user.changePassword("foo")
        } catch (e: ObjectServerError) {
            if (e.errorCode != ErrorCode.INVALID_CREDENTIALS) {
                throw e
            }
        }
        Assert.assertTrue(headerSet.get())
        looperThread.testComplete()
    }

    // Test that auth header renaming, custom headers and url prefix are all propagated correctly
    // to Sync. There really isn't a way to create a proper integration test since ROS used for testing
    // isn't configured to accept such requests. Instead we inspect the log from Sync which will
    // output the headers in TRACE mode.
    @Test
    @RunTestInLooperThread
    fun syncAuthHeaderAndUrlPrefix() {
        SyncManager.setAuthorizationHeaderName("TestAuth")
        SyncManager.addCustomRequestHeader("Test", "test")
        runSyncAuthHeadersAndUrlPrefixTest()
    }

    // Test that auth header renaming, custom headers and url prefix are all propagated correctly
    // to Sync. There really isn't a way to create a proper integration test since ROS used for testing
    // isn't configured to accept such requests. Instead we inspect the log from Sync which will
    // output the headers in TRACE mode.
    @Test
    @RunTestInLooperThread
    fun syncAuthHeaderAndUrlPrefix_specificHost() {
        SyncManager.setAuthorizationHeaderName("TestAuth", Constants.HOST)
        SyncManager.addCustomRequestHeader("Test", "test", Constants.HOST)
        runSyncAuthHeadersAndUrlPrefixTest()
    }

    private fun runSyncAuthHeadersAndUrlPrefixTest() {
        val credentials = SyncCredentials.usernamePassword(UUID.randomUUID().toString(), "test", true)
        val user: SyncUser = SyncUser.logIn(credentials, Constants.AUTH_URL)
        val config: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .urlPrefix("/foo")
                .errorHandler(SyncSession.ErrorHandler { session, error -> RealmLog.error(error.toString()) })
                .build()
        RealmLog.setLevel(LogLevel.ALL)
        val logger = RealmLogger { level: Int, tag: String, throwable: Throwable?, message: String? ->
            if (tag == "REALM_SYNC" && message!!.contains("GET /foo/")
                    && message.contains("TestAuth: Realm-Access-Token version=1")
                    && message.contains("Test: test")) {
                looperThread.testComplete()
            }
        }
        looperThread.runAfterTest({ RealmLog.remove(logger) })
        RealmLog.add(logger)
        val realm = Realm.getInstance(config)
        looperThread.closeAfterTest(realm)
    }

    @Test
    @RunTestInLooperThread
    @Throws(InterruptedException::class)
    fun progressListenersWorkWhenUsingWaitForInitialRemoteData() {
        val username = UUID.randomUUID().toString()
        val password = "password"
        var user: SyncUser = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL)

        // 1. Copy a valid Realm to the server (and pray it does it within 10 seconds)
        val configOld: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .schema(StringOnly::class.java)
                .sessionStopPolicy(OsRealmConfig.SyncSessionStopPolicy.IMMEDIATELY)
                .build()
        val realm = Realm.getInstance(configOld)
        realm.executeTransaction { realm ->
            for (i in 0..9) {
                realm.createObject(StringOnly::class.java).chars = "Foo$i"
            }
        }
        SyncManager.getSession(configOld).uploadAllLocalChanges()
        realm.close()
        user.logOut()
        Assert.assertTrue(SyncManager.getAllSessions(user).isEmpty())

        // 2. Local state should now be completely reset. Open the same sync Realm but different local name again with
        // a new configuration which should download the uploaded changes (pray it managed to do so within the time frame).
        user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password), Constants.AUTH_URL)
        val config: SyncConfiguration = user.createConfiguration(Constants.USER_REALM)
                .name("newRealm")
                .schema(StringOnly::class.java)
                .waitForInitialRemoteData()
                .build()
        Assert.assertFalse(config.realmExists())
        val indefineteListenerComplete = AtomicBoolean(false)
        val currentChangesListenerComplete = AtomicBoolean(false)
        val task = Realm.getInstanceAsync(config, object : Realm.Callback() {
            override fun onSuccess(realm: Realm) {
                realm.close()
                if (!indefineteListenerComplete.get()) {
                    Assert.fail("Indefinete progress listener did not report complete.")
                }
                if (!currentChangesListenerComplete.get()) {
                    Assert.fail("Current changes progress listener did not report complete.")
                }
                looperThread.testComplete()
            }

            override fun onError(exception: Throwable) {
                Assert.fail(exception.toString())
            }
        })
        looperThread.keepStrongReference(task)
        SyncManager.getSession(config).addDownloadProgressListener(ProgressMode.INDEFINITELY, ProgressListener { progress ->
            if (progress.isTransferComplete) {
                indefineteListenerComplete.set(true)
            }
        })
        SyncManager.getSession(config).addDownloadProgressListener(ProgressMode.CURRENT_CHANGES, ProgressListener { progress ->
            if (progress.isTransferComplete) {
                currentChangesListenerComplete.set(true)
            }
        })
    }
}
