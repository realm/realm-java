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
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.entities.DefaultSyncSchema
import io.realm.entities.SyncStringOnly
import io.realm.exceptions.DownloadingRealmInterruptedException
import io.realm.exceptions.RealmMigrationNeededException
import io.realm.internal.OsRealmConfig
import io.realm.kotlin.syncSession
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.log.RealmLogger
import io.realm.mongodb.*
import io.realm.mongodb.sync.*
import io.realm.rule.BlockingLooperThread
import io.realm.util.assertFailsWithErrorCode
import org.bson.BsonObjectId
import org.bson.types.ObjectId
import org.junit.*
import org.junit.runner.RunWith
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.assertFailsWith

private const val SECRET_PASSWORD = "123456"
private const val CUSTOM_HEADER_NAME = "Foo"
private const val CUSTOM_HEADER_VALUE = "bar"
private const val AUTH_HEADER_NAME = "RealmAuth"

/**
 * Catch all class for tests that not naturally fit anywhere else.
 */
@RunWith(AndroidJUnit4::class)
class SyncedRealmIntegrationTests {

    private val looperThread = BlockingLooperThread()

    private lateinit var app: App
    private lateinit var user: User
    private lateinit var syncConfiguration: SyncConfiguration

    private val configurationFactory: TestSyncConfigurationFactory = TestSyncConfigurationFactory()

    @Before
    fun setup() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        RealmLog.setLevel(LogLevel.ALL)
        app = TestApp()
        user = app.registerUserAndLogin(TestHelper.getRandomEmail(), SECRET_PASSWORD)
        syncConfiguration = configurationFactory
                // TODO We generate new partition value for each test to avoid overlaps in data. We
                //  could make test booting with a cleaner state by somehow flushing data between
                //  tests.
                .createSyncConfigurationBuilder(user, BsonObjectId(ObjectId()))
                .modules(DefaultSyncSchema())
                .build()
    }

    @After
    fun teardown() {
        if (this::app.isInitialized) {
            app.close()
        }
        RealmLog.setLevel(LogLevel.WARN)
    }

    @Test
    fun loginLogoutResumeSyncing() = looperThread.runBlocking {
        val config: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, user.id)
                .testSchema(SyncStringOnly::class.java)
                .testSessionStopPolicy(OsRealmConfig.SyncSessionStopPolicy.IMMEDIATELY)
                .build()
        Realm.getInstance(config).use { realm ->
            realm.executeTransaction {
                realm.createObject(SyncStringOnly::class.java, ObjectId()).chars = "Foo"
            }
            realm.syncSession.uploadAllLocalChanges()
            user.logOut()
        }
        try {
            Assert.assertTrue(Realm.deleteRealm(config))
        } catch (e: IllegalStateException) {
            // FIXME: We don't have a way to ensure that the Realm instance on client thread has been
            //  closed for now https://github.com/realm/realm-java/issues/5416
            if (e.message!!.contains("It's not allowed to delete the file")) {
                // retry after 1 second
                SystemClock.sleep(1000)
                Assert.assertTrue(Realm.deleteRealm(config))
            }
        }

        // FIXME Is this sufficient to test "loginLogoutResumeSynching"-case
        user = app.login(Credentials.emailPassword(user.email, SECRET_PASSWORD))
        val config2: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, user.id)
                .testSchema(SyncStringOnly::class.java)
                .build()
        Realm.getInstance(config2).use { realm ->
            realm.syncSession.downloadAllServerChanges()
            realm.refresh()
            Assert.assertEquals(1, realm.where(SyncStringOnly::class.java).count())
        }
        looperThread.testComplete()
    }

    @Test
    @UiThreadTest
    fun waitForInitialRemoteData_mainThreadThrows() {
        val user: User = SyncTestUtils.createTestUser(app)
        val config: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, user.id)
                .waitForInitialRemoteData()
                .build()
        assertFailsWith<java.lang.IllegalStateException> {
            Realm.getInstance(config).close()
        }
    }

    @Test
    fun waitForInitialRemoteData() {
        // 1. Copy a valid Realm to the server (and pray it does it within 10 seconds)
        val configOld: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, user.id)
                .testSchema(SyncStringOnly::class.java)
                .testSessionStopPolicy(OsRealmConfig.SyncSessionStopPolicy.IMMEDIATELY)
                .build()
        Realm.getInstance(configOld).use { realm ->
            realm.executeTransaction { realm ->
                for (i in 0..9) {
                    realm.createObject(SyncStringOnly::class.java, ObjectId()).chars = "Foo$i"
                }
            }
            realm.syncSession.uploadAllLocalChanges()
        }
        user.logOut()

        // 2. Local state should now be completely reset. Open the same sync Realm but different local name again with
        // a new configuration which should download the uploaded changes (pray it managed to do so within the time frame).
        // FIXME Is this sufficient for testing. I guess we use the same local file when we cannot
        //  set the name
        user = app.login(Credentials.emailPassword(user.email, SECRET_PASSWORD))
        val config: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, user.id)
                // FIXME How does this work for sync?
                // .name("newRealm")
                .testSchema(SyncStringOnly::class.java)
                .waitForInitialRemoteData()
                .build()
        Realm.getInstance(config).use { realm ->
            realm.executeTransaction { realm ->
                for (i in 0..9) {
                    realm.createObject(SyncStringOnly::class.java, ObjectId()).chars = "Foo 1$i"
                }
            }
            Assert.assertEquals(20, realm.where(SyncStringOnly::class.java).count())
        }
    }

    // This tests will start and cancel getting a Realm 10 times. The Realm should be resilient towards that
    // We cannot do much better since we cannot control the order of events internally in Realm which would be
    // needed to correctly test all error paths.
    @Test
    @Ignore("Sync somehow keeps a Realm alive, causing the Realm.deleteRealm to throw " +
            " https://github.com/realm/realm-java/issues/5416")
    fun waitForInitialData_resilientInCaseOfRetries() {
        val config: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, user.id)
                .waitForInitialRemoteData()
                .build()
        for (i in 0..9) {
            val t = Thread(Runnable {
                var realm: Realm? = null
                assertFailsWith<DownloadingRealmInterruptedException> {
                    Thread.currentThread().interrupt()
                    Realm.getInstance(config).close()
                }
                Assert.assertFalse(File(config.getPath()).exists())
                Realm.deleteRealm(config)
            })
            t.start()
            t.join()
        }
    }

    // This tests will start and cancel getting a Realm 10 times. The Realm should be resilient towards that
    // We cannot do much better since we cannot control the order of events internally in Realm which would be
    // needed to correctly test all error paths.
    @Test
    // FIXME This does not throw anymore as described in issue. But do the test still make sense
    //  with new sync?
    //@Ignore("See https://github.com/realm/realm-java/issues/5373")
    fun waitForInitialData_resilientInCaseOfRetriesAsync() = looperThread.runBlocking {
        val config: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, user.id)
                .testSessionStopPolicy(OsRealmConfig.SyncSessionStopPolicy.IMMEDIATELY)
                // FIXME Is this important for the test
                //.directory(configurationFactory.getRoot())
                .waitForInitialRemoteData()
                .build()
        val randomizer = Random()
        for (i in 0..9) {
            val task = Realm.getInstanceAsync(config, object : Realm.Callback() {
                override fun onSuccess(realm: Realm) { Assert.fail() }
                override fun onError(exception: Throwable) { Assert.fail(exception.toString()) }
            })
            SystemClock.sleep(randomizer.nextInt(5).toLong())
            task.cancel()
        }
        looperThread.testComplete()
    }

    @Test
    // FIXME Investigate
    fun waitForInitialRemoteData_readOnlyTrue() {
        // 1. Copy a valid Realm to the server (and pray it does it within 10 seconds)
        val configOld: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, user.id)
                .testSchema(SyncStringOnly::class.java)
                .build()
        Realm.getInstance(configOld).use { realm ->
            realm.executeTransaction { realm ->
                for (i in 0..9) {
                    realm.createObject(SyncStringOnly::class.java, ObjectId()).chars = "Foo$i"
                }
            }
            realm.syncSession.uploadAllLocalChanges()
        }
        user.logOut()

        // 2. Local state should now be completely reset. Open the Realm again with a new configuration which should
        // download the uploaded changes (pray it managed to do so within the time frame).
        // FIXME Is this sufficient for test. Guess we need a separate file to be able to test it!?
        user = app.login(Credentials.emailPassword(user.email, SECRET_PASSWORD))
        val configNew: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, user.id)
                // FIXME IS this essential for tests
                // .name("newRealm")
                .waitForInitialRemoteData()
                .readOnly()
                .testSchema(SyncStringOnly::class.java)
                .build()
        // FIXME This assert fails...due to commented .name above
        Assert.assertFalse(configNew.testRealmExists())
        Realm.getInstance(configNew).use { realm ->
            Assert.assertEquals(10, realm.where(SyncStringOnly::class.java).count())
        }
        user.logOut()
    }

    @Test
    // FIXME Investigate
    fun waitForInitialRemoteData_readOnlyTrue_throwsIfWrongServerSchema() {
        val configNew: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, user.id)
                .waitForInitialRemoteData()
                .readOnly()
                .testSchema(SyncStringOnly::class.java)
                .build()
        Assert.assertFalse(configNew.testRealmExists())
        assertFailsWith<RealmMigrationNeededException> {
            // This will fail, because the server Realm is completely empty and the Client is not allowed to write the
            // schema.
            // FIXME Does not throw. Has something changed around this
            Realm.getInstance(configNew).close()
        }
        user.logOut()
    }

    @Test
    fun waitForInitialRemoteData_readOnlyFalse_upgradeSchema() {
        val config: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, user.id)
                .waitForInitialRemoteData() // Not readonly so Client should be allowed to write schema
                .testSchema(SyncStringOnly::class.java) // This schema should be written when opening the empty Realm.
                .schemaVersion(2)
                .build()
        Assert.assertFalse(config.testRealmExists())
        Realm.getInstance(config).use { realm ->
            Assert.assertEquals(0, realm.where(SyncStringOnly::class.java).count())
        }
        user.logOut()
    }

    @Test
    fun defaultRealm() {
        val config: SyncConfiguration = SyncConfiguration.defaultConfig(user, user.id)
        Realm.getInstance(config).use { realm ->
            realm.syncSession.downloadAllServerChanges()
            realm.refresh()
            Assert.assertTrue(realm.isEmpty)
        }
        user.logOut()
    }

    // Check that custom headers and auth header renames are correctly used for HTTP requests
    // performed from Java.
    // FIXME Move to AppConfigurationTestt
    @Test
    fun javaRequestCustomHeaders() {
        looperThread.runBlocking {
            // FIXME Hack to overcome that we cannot have multiple apps and needs to adjust
            //  configuration of TestApp for this test. Would not be required in AppConfigurationTest
            app.close()
            Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
            val app = TestApp(builder = { builder ->
                builder.addCustomRequestHeader(CUSTOM_HEADER_NAME, CUSTOM_HEADER_VALUE)
                builder.authorizationHeaderName(AUTH_HEADER_NAME)
            })
            runJavaRequestCustomHeadersTest(app)
        }
    }

    // FIXME Seems to be outdated...cannot find an option for setting headers for a specific host
//    // Check that custom headers and auth header renames are correctly used for HTTP requests
//    // performed from Java.
//    @Test
//    @RunTestInLooperThread
//    fun javaRequestCustomHeaders_specificHost() {
//        SyncManager.addCustomRequestHeader("Foo", "bar", Constants.HOST)
//        SyncManager.setAuthorizationHeaderName("RealmAuth", Constants.HOST)
//        runJavaRequestCustomHeadersTest()
//    }

    private fun runJavaRequestCustomHeadersTest(app: App) {
        val username = UUID.randomUUID().toString()
        val password = "password"
        val headerSet = AtomicBoolean(false)

        // Setup logger to inspect that we get a log message with the custom headers
        RealmLog.setLevel(LogLevel.ALL)
        val logger = RealmLogger { level: Int, tag: String?, throwable: Throwable?, message: String? ->
            if (level > LogLevel.TRACE && message!!.contains(CUSTOM_HEADER_NAME) && message.contains(CUSTOM_HEADER_VALUE)
                    && message.contains("RealmAuth: ")) {
                headerSet.set(true)
            }
        }
        RealmLog.add(logger)
        assertFailsWithErrorCode(ErrorCode.SERVICE_UNKNOWN) {
            app.registerUserAndLogin(username, password)
        }
        // FIXME Guess it would be better to reset logger on Realm.init, but not sure of impact
        //  ...or is the logger intentionally shared to enable full trace of a full test run?
        RealmLog.remove(logger)

        Assert.assertTrue(headerSet.get())
        looperThread.testComplete()
    }

    @Test
    // FIXME Investigate
    fun progressListenersWorkWhenUsingWaitForInitialRemoteData() = looperThread.runBlocking {
        val username = UUID.randomUUID().toString()
        val password = "password"
        var user: User = app.registerUserAndLogin(username, password)

        // 1. Copy a valid Realm to the server (and pray it does it within 10 seconds)
        val configOld: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, user.id)
                .testSchema(SyncStringOnly::class.java)
                .testSessionStopPolicy(OsRealmConfig.SyncSessionStopPolicy.IMMEDIATELY)
                .build()
        Realm.getInstance(configOld).use { realm ->
            realm.executeTransaction { realm ->
                for (i in 0..9) {
                    realm.createObject(SyncStringOnly::class.java, ObjectId()).chars = "Foo$i"
                }
            }
            realm.syncSession.uploadAllLocalChanges()
        }
        user.logOut()

        // FIXME Is this equivalent to the old
        // Assert.assertTrue(SyncManager.getAllSessions(user).isEmpty())
        assertFailsWith<java.lang.IllegalStateException> {
            app.sync.getSession(configOld)
        }

        // 2. Local state should now be completely reset. Open the same sync Realm but different local name again with
        // a new configuration which should download the uploaded changes (pray it managed to do so within the time frame).
        // FIXME This whole part is maybe not applicable for new sync. Maybe use a new users to
        //  force using a new file??
        user = app.login(Credentials.emailPassword(username, password))
        val config: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, user.id)
                // FIXME Is this essential for tests
                // .name("newRealm")
                .testSchema(SyncStringOnly::class.java)
                // FIXME I guess we should not wair for initial remote data, as we want to test progress listeners
                // .waitForInitialRemoteData()
                .build()

        // FIXME Reintroduce?
        // Assert.assertFalse(config.testRealmExists())

        val indefiniteListenerComplete = AtomicBoolean(false)
        val currentChangesListenerComplete = AtomicBoolean(false)
        val task = Realm.getInstanceAsync(config, object : Realm.Callback() {
            override fun onSuccess(realm: Realm) {
                // FIXME Is it acceptable to register the listeners here as we cannot access the
                //  session before the Realm is opened?
                realm.syncSession.addDownloadProgressListener(ProgressMode.INDEFINITELY, object : ProgressListener {
                    override fun onChange(progress: Progress) {
                        if (progress.isTransferComplete()) {
                            indefiniteListenerComplete.set(true)
                        }
                    }
                })
                realm.syncSession.addDownloadProgressListener(ProgressMode.CURRENT_CHANGES, object : ProgressListener {
                    override fun onChange(progress: Progress) {
                        if (progress.isTransferComplete()) {
                            currentChangesListenerComplete.set(true)
                        }
                    }
                })
                realm.close()
                if (!indefiniteListenerComplete.get()) {
                    Assert.fail("Indefinite progress listener did not report complete.")
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
    }

}
