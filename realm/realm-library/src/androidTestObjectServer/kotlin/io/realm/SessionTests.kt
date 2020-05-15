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
package io.realm

import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.TestHelper.TestLogger
import io.realm.entities.StringOnly
import io.realm.entities.StringOnlyModule
import io.realm.exceptions.RealmFileException
import io.realm.exceptions.RealmMigrationNeededException
import io.realm.log.RealmLog
import io.realm.rule.BlockingLooperThread
import io.realm.util.ResourceContainer
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

@RunWith(AndroidJUnit4::class)
class SessionTests {
    private lateinit var configuration: SyncConfiguration
    private lateinit var app: TestRealmApp
    private lateinit var user: RealmUser

    @get:Rule
    val configFactory = TestSyncConfigurationFactory()

    private val looperThread = BlockingLooperThread()

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        app = TestRealmApp()
        // TODO We could potentially work without a fully functioning user to speed up tests, but
        //  seems like the old  way of "faking" it, does now work for now, so using a real user.
        // user = SyncTestUtils.createTestUser(app)
        user = createNewUser(app)
        configuration = SyncConfiguration.defaultConfig(user, "default")
    }

    @After
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
    }

    @Test
    fun get_syncValues() {
        val session = SyncSession(configuration)
        Assert.assertEquals("ws://127.0.0.1:9090/", session.serverUrl.toString())
        Assert.assertEquals(user, session.user)
        Assert.assertEquals(configuration, session.configuration)
    }

    @Test
    fun addDownloadProgressListener_nullThrows() {
        val session = app.sync.getOrCreateSession(configuration)
        try {
            session.addDownloadProgressListener(ProgressMode.CURRENT_CHANGES, TestHelper.getNull())
            Assert.fail()
        } catch (ignored: IllegalArgumentException) {
        }
    }

    @Test
    fun addUploadProgressListener_nullThrows() {
        val session = app.sync.getOrCreateSession(configuration)
        try {
            session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES, TestHelper.getNull())
            Assert.fail()
        } catch (ignored: IllegalArgumentException) {
        }
    }

    @Test
    fun removeProgressListener() {
        val realm = Realm.getInstance(configuration)
        val session = app.sync.getOrCreateSession(configuration)
        val listeners = arrayOf(
                null,
                ProgressListener { progress: Progress? -> },
                ProgressListener { progress: Progress? -> }
        )
        session.addDownloadProgressListener(ProgressMode.CURRENT_CHANGES, TestHelper.allowNull(listeners[2]))
        session.addDownloadProgressListener(ProgressMode.CURRENT_CHANGES, TestHelper.allowNull(listeners[2]))

        // Check that remove works unconditionally for all input
        for (listener in listeners) {
            session.removeProgressListener(TestHelper.allowNull(listener))
        }
        realm.close()
    }

    // Check that a Client Reset is correctly reported.
    @Test
    fun errorHandler_clientResetReported() = looperThread.runBlocking {
        val config = configFactory.createSyncConfigurationBuilder(user)
                .clientResyncMode(ClientResyncMode.MANUAL)
                .errorHandler { session: SyncSession, error: ObjectServerError ->
                    if (error.errorCode != ErrorCode.CLIENT_RESET) {
                        Assert.fail("Wrong error $error")
                        return@errorHandler
                    }
                    val handler = error as ClientResetRequiredError
                    val filePathFromError = handler.originalFile.absolutePath
                    val filePathFromConfig = session.configuration.path
                    Assert.assertEquals(filePathFromError, filePathFromConfig)
                    Assert.assertFalse(handler.backupFile.exists())
                    Assert.assertTrue(handler.originalFile.exists())
                    looperThread.testComplete()
                }
                .build()

        val realm = Realm.getInstance(config)
        looperThread.closeAfterTest(realm)

        // Trigger error
        val syncService = user.app.sync
        syncService.simulateClientReset(syncService.getSession(config))
    }

    // Check that we can manually execute the Client Reset.
    @Test
    fun errorHandler_manualExecuteClientReset() = looperThread.runBlocking {
        val resources = ResourceContainer()

        val config = configFactory.createSyncConfigurationBuilder(user)
                .clientResyncMode(ClientResyncMode.MANUAL)
                .errorHandler { session: SyncSession?, error: ObjectServerError ->
                    if (error.errorCode != ErrorCode.CLIENT_RESET) {
                        Assert.fail("Wrong error $error")
                        return@errorHandler
                    }
                    val handler = error as ClientResetRequiredError
                    try {
                        handler.executeClientReset()
                        Assert.fail("All Realms should be closed before executing Client Reset can be allowed")
                    } catch (ignored: IllegalStateException) {
                    }

                    // Execute Client Reset
                    resources.close()
                    handler.executeClientReset()

                    // Validate that files have been moved
                    Assert.assertFalse(handler.originalFile.exists())
                    Assert.assertTrue(handler.backupFile.exists())
                    looperThread.testComplete()
                }
                .build()
        val realm = Realm.getInstance(config)
        resources.add(realm)

        // Trigger error
        user.app.sync.simulateClientReset(app.sync.getSession(configuration))
    }

    // Check that we can use the backup SyncConfiguration to open the Realm.
    @Test
    fun errorHandler_useBackupSyncConfigurationForClientReset() = looperThread.runBlocking {
        val resources = ResourceContainer()
        val config = configFactory.createSyncConfigurationBuilder(user)
                .clientResyncMode(ClientResyncMode.MANUAL)
                .schema(StringOnly::class.java)
                .errorHandler { session: SyncSession?, error: ObjectServerError ->
                    if (error.errorCode != ErrorCode.CLIENT_RESET) {
                        Assert.fail("Wrong error $error")
                        return@errorHandler
                    }
                    val handler = error as ClientResetRequiredError
                    // Execute Client Reset
                    resources.close()
                    handler.executeClientReset()

                    // Validate that files have been moved
                    Assert.assertFalse(handler.originalFile.exists())
                    Assert.assertTrue(handler.backupFile.exists())
                    val backupRealmConfiguration = handler.backupRealmConfiguration
                    Assert.assertNotNull(backupRealmConfiguration)
                    Assert.assertFalse(backupRealmConfiguration.isSyncConfiguration)
                    Assert.assertTrue(backupRealmConfiguration.isRecoveryConfiguration)
                    val backupRealm = Realm.getInstance(backupRealmConfiguration)
                    Assert.assertFalse(backupRealm.isEmpty)
                    Assert.assertEquals(1, backupRealm.where(StringOnly::class.java).count())
                    backupRealm.where(StringOnly::class.java).findAll().first()?.let { first ->
                        Assert.assertEquals("Foo", first.chars)
                    } ?: fail()
                    backupRealm.close()

                    // opening a Dynamic Realm should also work
                    val dynamicRealm = DynamicRealm.getInstance(backupRealmConfiguration)
                    dynamicRealm.schema.checkHasTable(StringOnly.CLASS_NAME, "Dynamic Realm should contains " + StringOnly.CLASS_NAME)
                    val all = dynamicRealm.where(StringOnly.CLASS_NAME).findAll()
                    Assert.assertEquals(1, all.size.toLong())
                    all.first()?.let { first ->
                        Assert.assertEquals("Foo", first.getString(StringOnly.FIELD_CHARS))
                    } ?: fail()
                    dynamicRealm.close()
                    looperThread.testComplete()
                }
                .modules(StringOnlyModule())
                .build()
        val realm = Realm.getInstance(config)
        realm.beginTransaction()
        realm.createObject(StringOnly::class.java).chars = "Foo"
        realm.commitTransaction()
        resources.add(realm)

        // Trigger error
        user.app.sync.simulateClientReset(app.sync.getSession(configuration))
    }

    // Check that we can open the backup file without using the provided SyncConfiguration,
    // this might be the case if the user decide to act upon the client reset later (providing s/he
    // persisted the location of the file)
    @Test
    fun errorHandler_useBackupSyncConfigurationAfterClientReset() = looperThread.runBlocking {
        val resources = ResourceContainer()
        val config = configFactory.createSyncConfigurationBuilder(user)
                .clientResyncMode(ClientResyncMode.MANUAL)
                .errorHandler { session: SyncSession?, error: ObjectServerError ->
                    if (error.errorCode != ErrorCode.CLIENT_RESET) {
                        Assert.fail("Wrong error $error")
                        return@errorHandler
                    }
                    val handler = error as ClientResetRequiredError
                    // Execute Client Reset
                    resources.close()
                    handler.executeClientReset()

                    // Validate that files have been moved
                    Assert.assertFalse(handler.originalFile.exists())
                    Assert.assertTrue(handler.backupFile.exists())
                    val backupFile = handler.backupFile.absolutePath

                    // this SyncConf doesn't specify any module, it will throw a migration required
                    // exception since the backup Realm contain only StringOnly table
                    var backupRealmConfiguration = SyncConfiguration.forRecovery(backupFile)
                    try {
                        Realm.getInstance(backupRealmConfiguration)
                        Assert.fail("Expected to throw a Migration required")
                    } catch (expected: RealmMigrationNeededException) {
                    }

                    // opening a DynamicRealm will work though
                    val dynamicRealm = DynamicRealm.getInstance(backupRealmConfiguration)
                    dynamicRealm.schema.checkHasTable(StringOnly.CLASS_NAME, "Dynamic Realm should contains " + StringOnly.CLASS_NAME)
                    val all = dynamicRealm.where(StringOnly.CLASS_NAME).findAll()
                    Assert.assertEquals(1, all.size.toLong())
                    all.first()?.let { first ->
                        Assert.assertEquals("Foo", first.getString(StringOnly.FIELD_CHARS))
                    } ?: fail()
                    // make sure we can't write to it (read-only Realm)
                    try {
                        dynamicRealm.beginTransaction()
                        Assert.fail("Can't perform transactions on read-only Realms")
                    } catch (expected: IllegalStateException) {
                    }
                    dynamicRealm.close()
                    try {
                        SyncConfiguration.forRecovery(backupFile, null, StringOnly::class.java)
                        Assert.fail("Expected to throw java.lang.Class is not a RealmModule")
                    } catch (expected: IllegalArgumentException) {
                    }

                    // specifying the module will allow to open the typed Realm
                    backupRealmConfiguration = SyncConfiguration.forRecovery(backupFile, null, StringOnlyModule())
                    val backupRealm = Realm.getInstance(backupRealmConfiguration)
                    Assert.assertFalse(backupRealm.isEmpty)
                    Assert.assertEquals(1, backupRealm.where(StringOnly::class.java).count())
                    val allSorted = backupRealm.where(StringOnly::class.java).findAll()
                    allSorted[0]?.let { allSorted0 ->
                        Assert.assertEquals("Foo", allSorted0.chars)
                    } ?: fail()
                    backupRealm.close()
                    looperThread.testComplete()
                }
                .modules(StringOnlyModule())
                .build()
        val realm = Realm.getInstance(config)
        realm.beginTransaction()
        realm.createObject(StringOnly::class.java).chars = "Foo"
        realm.commitTransaction()
        resources.add(realm)

        // Trigger error
        user.app.sync.simulateClientReset(app.sync.getSession(configuration))
    }

    // make sure the backup file Realm is encrypted with the same key as the original synced Realm.
    @Test
    fun errorHandler_useClientResetEncrypted() = looperThread.runBlocking {
        val resources = ResourceContainer()

        val randomKey = TestHelper.getRandomKey()
        val config = configFactory.createSyncConfigurationBuilder(user)
                .clientResyncMode(ClientResyncMode.MANUAL)
                .encryptionKey(randomKey)
                .modules(StringOnlyModule())
                .errorHandler { session: SyncSession?, error: ObjectServerError ->
                    if (error.errorCode != ErrorCode.CLIENT_RESET) {
                        Assert.fail("Wrong error $error")
                        return@errorHandler
                    }
                    val handler = error as ClientResetRequiredError
                    // Execute Client Reset
                    resources.close()
                    handler.executeClientReset()
                    var backupRealmConfiguration = handler.backupRealmConfiguration

                    // can open encrypted backup Realm
                    var backupEncryptedRealm = Realm.getInstance(backupRealmConfiguration)
                    Assert.assertEquals(1, backupEncryptedRealm.where(StringOnly::class.java).count())
                    var allSorted = backupEncryptedRealm.where(StringOnly::class.java).findAll()
                    allSorted[0]?.let { allSorted0 ->
                        Assert.assertEquals("Foo", allSorted0.chars)
                    } ?: fail()
                    backupEncryptedRealm.close()
                    val backupFile = handler.backupFile.absolutePath
                    // build a conf to open a DynamicRealm
                    backupRealmConfiguration = SyncConfiguration.forRecovery(backupFile, randomKey, StringOnlyModule())
                    backupEncryptedRealm = Realm.getInstance(backupRealmConfiguration)
                    Assert.assertEquals(1, backupEncryptedRealm.where(StringOnly::class.java).count())
                    allSorted = backupEncryptedRealm.where(StringOnly::class.java).findAll()
                    allSorted[0]?.let { allSorted0 ->
                        Assert.assertEquals("Foo", allSorted0.chars)
                    }
                    backupEncryptedRealm.close()

                    // using wrong key throw
                    try {
                        Realm.getInstance(SyncConfiguration.forRecovery(backupFile, TestHelper.getRandomKey(), StringOnlyModule()))
                        Assert.fail("Expected to throw when using wrong encryption key")
                    } catch (expected: RealmFileException) {
                    }
                    looperThread.testComplete()
                }
                .build()
        val realm = Realm.getInstance(config)
        realm.beginTransaction()
        realm.createObject(StringOnly::class.java).chars = "Foo"
        realm.commitTransaction()
        resources.add(realm)

        // Trigger error
        user.app.sync.simulateClientReset(app.sync.getSession(configuration))
    }

    @Test
    @UiThreadTest
    @Throws(InterruptedException::class)
    fun uploadAllLocalChanges_throwsOnUiThread() {
        val realm = Realm.getInstance(configuration)
        try {
            app.sync.getOrCreateSession(configuration).uploadAllLocalChanges()
            Assert.fail("Should throw an IllegalStateException on Ui Thread")
        } catch (ignored: IllegalStateException) {
        } finally {
            realm.close()
        }
    }

    @Test
    @UiThreadTest
    @Throws(InterruptedException::class)
    fun uploadAllLocalChanges_withTimeout_throwsOnUiThread() {
        val realm = Realm.getInstance(configuration)
        try {
            app.sync.getOrCreateSession(configuration).uploadAllLocalChanges(30, TimeUnit.SECONDS)
            Assert.fail("Should throw an IllegalStateException on Ui Thread")
        } catch (ignored: IllegalStateException) {
        } finally {
            realm.close()
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun uploadAllLocalChanges_withTimeout_invalidParametersThrows() {
        val realm = Realm.getInstance(configuration)
        val session = app.sync.getOrCreateSession(configuration)
        try {
            try {
                session.uploadAllLocalChanges(-1, TimeUnit.SECONDS)
                Assert.fail()
            } catch (ignored: IllegalArgumentException) {
            }
            try {
                session.uploadAllLocalChanges(1, TestHelper.getNull())
                Assert.fail()
            } catch (ignored: IllegalArgumentException) {
            }
        } finally {
            realm.close()
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun uploadAllLocalChanges_returnFalseWhenTimedOut() {
        val realm = Realm.getInstance(configuration)
        val session = app.sync.getSession(configuration)
        try {
            Assert.assertFalse(session.uploadAllLocalChanges(100, TimeUnit.MILLISECONDS))
        } finally {
            realm.close()
        }
    }

    @Test
    @UiThreadTest
    @Throws(InterruptedException::class)
    fun downloadAllServerChanges_throwsOnUiThread() {
        val realm = Realm.getInstance(configuration)
        try {
            app.sync.getSession(configuration).downloadAllServerChanges()
            Assert.fail("Should throw an IllegalStateException on Ui Thread")
        } catch (ignored: IllegalStateException) {
        } finally {
            realm.close()
        }
    }

    @Test
    @UiThreadTest
    @Throws(InterruptedException::class)
    fun downloadAllServerChanges_withTimeout_throwsOnUiThread() {
        val realm = Realm.getInstance(configuration)
        try {
            app.sync.getSession(configuration).downloadAllServerChanges(30, TimeUnit.SECONDS)
            Assert.fail("Should throw an IllegalStateException on Ui Thread")
        } catch (ignored: IllegalStateException) {
        } finally {
            realm.close()
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun downloadAllServerChanges_withTimeout_invalidParametersThrows() {
        val realm = Realm.getInstance(configuration)
        val session = app.sync.getSession(configuration)
        try {
            try {
                session.downloadAllServerChanges(-1, TimeUnit.SECONDS)
                Assert.fail()
            } catch (ignored: IllegalArgumentException) {
            }
            try {
                session.downloadAllServerChanges(1, TestHelper.getNull())
                Assert.fail()
            } catch (ignored: IllegalArgumentException) {
            }
        } finally {
            realm.close()
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun downloadAllServerChanges_returnFalseWhenTimedOut() {
        val realm = Realm.getInstance(configuration)
        val session = app.sync.getSession(configuration)
        try {
            // FIXME Have to add this to make it pass. Has something changed in initial sync?
//             assertTrue(session.downloadAllServerChanges(100, TimeUnit.MILLISECONDS))
            assertFalse(session.downloadAllServerChanges(100, TimeUnit.MILLISECONDS))
        } finally {
            realm.close()
        }
    }

    @Test
    @UiThreadTest
    fun unrecognizedErrorCode_errorHandler() {
        val errorHandlerCalled = AtomicBoolean(false)
        configuration = configFactory.createSyncConfigurationBuilder(user)
                .errorHandler { session: SyncSession?, error: ObjectServerError ->
                    errorHandlerCalled.set(true)
                    assertEquals(ErrorCode.UNKNOWN, error.errorCode)
                    assertEquals(ErrorCode.Category.FATAL, error.category)
                }
                .build()
        val realm = Realm.getInstance(configuration)
        val session = app.sync.getSession(configuration)
        val testLogger = TestLogger()
        RealmLog.add(testLogger)
        session.notifySessionError("unknown", 3, "Unknown Error")
        RealmLog.remove(testLogger)
        assertTrue(errorHandlerCalled.get())
        assertEquals("Unknown error code: 'unknown:3'", testLogger.message)
        realm.close()
    }

    // Closing the Realm should remove the session
    @Test
    fun getSessionThrowsOnNonExistingSession() {
        val realm = Realm.getInstance(configuration)
        val session = app.sync.getSession(configuration)
        assertEquals(configuration, session.configuration)

        // Closing the Realm should remove the session
        realm.close()
        try {
            app.sync.getSession(configuration)
            fail("getSession should throw an ISE")
        } catch (expected: IllegalStateException) {
            assertThat(expected.message, CoreMatchers.containsString(
                    "No SyncSession found using the path : "))
        }
    }


    @Test
    fun isConnected_falseForInvalidUser() {
        val realm = Realm.getInstance(configuration)
        val session = app.sync.getSession(configuration)
        try {
            assertFalse(session.isConnected)
        } finally {
            realm.close()
        }
    }

    @Test
    fun stop_doesNotThrowIfCalledWhenRealmIsClosed() {
        val realm = Realm.getInstance(configuration)
        val session = app.sync.getSession(configuration)
        realm.close()
        session.stop()
    }
    
    private fun createNewUser(app: RealmApp): RealmUser {
        val email = TestHelper.getRandomEmail()
        val password = "123456"
        app.emailPasswordAuth.registerUser(email, password)
        return app.login(RealmCredentials.emailPassword(email, password))
    }

}
