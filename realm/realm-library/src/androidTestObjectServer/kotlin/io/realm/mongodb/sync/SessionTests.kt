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
package io.realm.mongodb.sync

import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.*
import io.realm.TestHelper.TestLogger
import io.realm.entities.DefaultSyncSchema
import io.realm.entities.SyncColor
import io.realm.entities.SyncStringOnly
import io.realm.entities.SyncStringOnlyModule
import io.realm.exceptions.RealmFileException
import io.realm.exceptions.RealmMigrationNeededException
import io.realm.kotlin.syncSession
import io.realm.kotlin.where
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.mongodb.*
import io.realm.rule.BlockingLooperThread
import io.realm.util.ResourceContainer
import io.realm.util.assertFailsWithMessage
import org.bson.types.ObjectId
import org.hamcrest.CoreMatchers
import org.junit.*
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.*

@RunWith(AndroidJUnit4::class)
class SessionTests {
    private lateinit var configuration: SyncConfiguration
    private lateinit var app: TestApp
    private lateinit var user: User

    @get:Rule
    val configFactory = TestSyncConfigurationFactory()

    private val looperThread = BlockingLooperThread()

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        app = TestApp()
        // TODO We could potentially work without a fully functioning user to speed up tests, but
        //  seems like the old  way of "faking" it, does now work for now, so using a real user.
        // user = SyncTestUtils.createTestUser(app)
        user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        configuration = SyncConfiguration.Builder(user, "default")
                .modules(DefaultSyncSchema())
                .build()
    }

    @After
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
    }

    @Test
    fun get_syncValues() {
        Realm.getInstance(configuration).use { realm ->
            val session = realm.syncSession
            assertEquals("ws://127.0.0.1:9090/", session.serverUrl.toString())
            assertEquals(user, session.user)
            assertEquals(configuration, session.configuration)
        }
    }

    @Test
    fun addDownloadProgressListener_nullThrows() {
        Realm.getInstance(configuration).use { realm ->
        val session = realm.syncSession
            assertFailsWith<IllegalArgumentException> {
                session.addDownloadProgressListener(ProgressMode.CURRENT_CHANGES, TestHelper.getNull())
            }
        }
    }

    @Test
    fun addUploadProgressListener_nullThrows() {
        Realm.getInstance(configuration).use { realm ->
            val session = realm.syncSession
            assertFailsWith<IllegalArgumentException> {
                session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES, TestHelper.getNull())
            }
        }
    }

    @Test
    fun removeProgressListener() {
        Realm.getInstance(configuration).use { realm ->
            val session = realm.syncSession
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
        }
    }

    // Check that a Seamless Client Reset is correctly reported.
    @Test
    @Ignore
    fun errorHandler_discardUnsyncedChangesStrategyReported() = looperThread.runBlocking {
        val counter = AtomicInteger()

        val incrementAndValidate = {
            if (2 == counter.incrementAndGet()) {
                looperThread.testComplete()
            }
        }

        val config = configFactory.createSyncConfigurationBuilder(user, "e873fb25-11ef-498f-9782-3c8e1cd2a12c")
                .assetFile("synced_realm_e873fb25-11ef-498f-9782-3c8e1cd2a12c_no_client_id.realm")
                .syncClientResetStrategy(object: DiscardUnsyncedChangesStrategy{
                    override fun onBeforeReset(realm: Realm) {
                        assertTrue(realm.isFrozen)
                        Assert.assertEquals(1, realm.where<SyncColor>().count())
                        incrementAndValidate()
                    }

                    override fun onAfterReset(before: Realm, after: Realm) {
                        assertTrue(before.isFrozen)
                        assertFalse(after.isFrozen)

                        Assert.assertEquals(1, before.where<SyncColor>().count())
                        Assert.assertEquals(0, after.where<SyncColor>().count())

                        //Validate we can move data to the reset Realm.
                        after.executeTransaction{
                            it.insert(before.where<SyncColor>().findFirst()!!)
                        }
                        Assert.assertEquals(1, after.where<SyncColor>().count())
                        incrementAndValidate()
                    }

                    override fun onError(session: SyncSession, error: ClientResetRequiredError) {
                        fail("This test case was not supposed to trigger DiscardUnsyncedChangesStrategy::onError()")
                    }

                })
                .schema(SyncColor::class.java)
                .build()

        val realm = Realm.getInstance(config)
        Assert.assertEquals(1, realm.where<SyncColor>().count())
        looperThread.closeAfterTest(realm)
    }

    // Check that a if Seamless loss Client Reset fails the error is correctly reported.
    @Test
    fun errorHandler_discardUnsyncedChangesStrategy_resetErrorHandled() = looperThread.runBlocking {
        val email = TestHelper.getRandomEmail()
        val user: User = app.registerUserAndLogin(email, "123456")

        val config = configFactory.createSyncConfigurationBuilder(user)
            .testSchema(SyncStringOnly::class.java)
                .syncClientResetStrategy(object: DiscardUnsyncedChangesStrategy {
                    override fun onBeforeReset(realm: Realm) {
                        fail("This test case was not supposed to trigger DiscardUnsyncedChangesStrategy::onBeforeReset()")
                    }

                    override fun onAfterReset(before: Realm, after: Realm) {
                        fail("This test case was not supposed to trigger DiscardUnsyncedChangesStrategy::onAfterReset()")
                    }

                    override fun onError(session: SyncSession, error: ClientResetRequiredError) {
                        val filePathFromError = error.originalFile.absolutePath
                        val filePathFromConfig = session.configuration.path
                        assertEquals(filePathFromError, filePathFromConfig)
                        assertFalse(error.backupFile.exists())
                        assertTrue(error.originalFile.exists())
                        // Note, this error message is just the one created by ObjectStore for testing
                        // The server will send a different message. This just ensures that we don't
                        // accidentially modify or remove the message.
                        assertEquals("Simulate Client Reset", error.message)
                        looperThread.testComplete()
                    }
                })
            .build()

        val realm = Realm.getInstance(config)
        looperThread.closeAfterTest(realm)
        user.app.sync.simulateClientReset(realm.syncSession, ErrorCode.AUTO_CLIENT_RESET_FAILURE)
    }

    // Check that a Client Reset is correctly reported.
    @Test
    @Deprecated("clientResetHandler deprecated in favor of syncClientResetStrategy")
    fun errorHandler_manualClientResetReported() = looperThread.runBlocking {
        val config = configFactory.createSyncConfigurationBuilder(user)
                .testSchema(SyncStringOnly::class.java)
                .clientResetHandler { session: SyncSession, error: ClientResetRequiredError ->
                    val filePathFromError = error.originalFile.absolutePath
                    val filePathFromConfig = session.configuration.path
                    assertEquals(filePathFromError, filePathFromConfig)
                    assertFalse(error.backupFile.exists())
                    assertTrue(error.originalFile.exists())
                    // Note, this error message is just the one created by ObjectStore for testing
                    // The server will send a different message. This just ensures that we don't
                    // accidentially modify or remove the message.
                    assertEquals("Simulate Client Reset", error.message)
                    looperThread.testComplete()
                }
                .build()

        val realm = Realm.getInstance(config)
        looperThread.closeAfterTest(realm)

        // Trigger error
        user.app.sync.simulateClientReset(realm.syncSession)
    }

    // Check that we can manually execute the Client Reset.
    @Test
    @Deprecated("clientResetHandler deprecated in favor of syncClientResetStrategy")
    fun errorHandler_manualExecuteClientReset() = looperThread.runBlocking {
        val resources = ResourceContainer()

        val config = configFactory.createSyncConfigurationBuilder(user)
                .testSchema(SyncStringOnly::class.java)
                .clientResetHandler { _: SyncSession, error: ClientResetRequiredError ->
                    try {
                        error.executeClientReset()
                        fail("All Realms should be closed before executing Client Reset can be allowed")
                    } catch (ignored: IllegalStateException) {
                    }

                    // Execute Client Reset
                    resources.close()
                    error.executeClientReset()

                    // Validate that files have been moved
                    assertFalse(error.originalFile.exists())
                    assertTrue(error.backupFile.exists())
                    looperThread.testComplete()
                }
                .build()
        val realm = Realm.getInstance(config)
        resources.add(realm)

        // Trigger error
        user.app.sync.simulateClientReset(realm.syncSession)
    }

    // Check that we can use the backup SyncConfiguration to open the Realm.
    @Test
    @Deprecated("clientResetHandler deprecated in favor of syncClientResetStrategy")
    fun errorHandler_useBackupSyncConfigurationForClientReset() = looperThread.runBlocking {
        val resources = ResourceContainer()
        val config = configFactory.createSyncConfigurationBuilder(user)
                .schema(SyncStringOnly::class.java)
                .clientResetHandler { _: SyncSession?, error: ClientResetRequiredError ->
                    // Execute Client Reset
                    resources.close()
                    error.executeClientReset()

                    // Validate that files have been moved
                    assertFalse(error.originalFile.exists())
                    assertTrue(error.backupFile.exists())
                    val backupRealmConfiguration = error.backupRealmConfiguration
                    assertNotNull(backupRealmConfiguration)
                    assertFalse(backupRealmConfiguration is SyncConfiguration)
                    assertTrue(backupRealmConfiguration.isRecoveryConfiguration)
                    Realm.getInstance(backupRealmConfiguration).use { backupRealm ->
                        assertFalse(backupRealm.isEmpty)
                        assertEquals(1, backupRealm.where(SyncStringOnly::class.java).count())
                        assertEquals("Foo", backupRealm.where(SyncStringOnly::class.java).findAll().first()!!.chars)
                    }

                    // opening a Dynamic Realm should also work
                    DynamicRealm.getInstance(backupRealmConfiguration).use { dynamicRealm ->
                        assertNotNull(dynamicRealm.schema.get(SyncStringOnly.CLASS_NAME))
                        val all = dynamicRealm.where(SyncStringOnly.CLASS_NAME).findAll()
                        assertEquals(1, all.size.toLong())
                        assertEquals("Foo", all.first()!!.getString(SyncStringOnly.FIELD_CHARS))
                    }
                    looperThread.testComplete()
                }
                .build()
        val realm = Realm.getInstance(config)
        realm.executeTransaction {
            realm.createObject(SyncStringOnly::class.java, ObjectId()).chars = "Foo"
        }
        resources.add(realm)

        // Trigger error
        user.app.sync.simulateClientReset(realm.syncSession)
    }

    // Check that we can open the backup file without using the provided SyncConfiguration,
    // this might be the case if the user decide to act upon the client reset later (providing s/he
    // persisted the location of the file)
    @Test
    @Deprecated("clientResetHandler deprecated in favor of syncClientResetStrategy")
    fun errorHandler_useBackupSyncConfigurationAfterClientReset() = looperThread.runBlocking {
        val resources = ResourceContainer()
        val config = configFactory.createSyncConfigurationBuilder(user)
                .modules(SyncStringOnlyModule())
                .clientResetHandler { session: SyncSession?, error: ClientResetRequiredError ->
                    // Execute Client Reset
                    resources.close()
                    error.executeClientReset()

                    // Validate that files have been moved
                    assertFalse(error.originalFile.exists())
                    assertTrue(error.backupFile.exists())
                    val backupFile = error.backupFile.absolutePath

                    // this SyncConf doesn't specify any module, it will throw a migration required
                    // exception since the backup Realm contain only StringOnly table
                    var backupRealmConfiguration = SyncConfiguration.forRecovery(backupFile)
                    assertFailsWith<RealmMigrationNeededException> {
                        Realm.getInstance(backupRealmConfiguration)
                    }

                    // opening a DynamicRealm will work though
                    DynamicRealm.getInstance(backupRealmConfiguration).use { dynamicRealm ->
                        assertNotNull(dynamicRealm.schema.get(SyncStringOnly.CLASS_NAME))
                        val all = dynamicRealm.where(SyncStringOnly.CLASS_NAME).findAll()
                        assertEquals(1, all.size.toLong())
                        assertEquals("Foo", all.first()!!.getString(SyncStringOnly.FIELD_CHARS))
                        // make sure we can't write to it (read-only Realm)
                        assertFailsWith<java.lang.IllegalStateException> {
                            dynamicRealm.beginTransaction()
                        }
                    }

                    assertFailsWith<IllegalArgumentException> {
                        SyncConfiguration.forRecovery(backupFile, null, SyncStringOnly::class.java)
                    }

                    // specifying the module will allow to open the typed Realm
                    backupRealmConfiguration = SyncConfiguration.forRecovery(backupFile, null, SyncStringOnlyModule())
                    Realm.getInstance(backupRealmConfiguration).use { backupRealm ->
                        assertFalse(backupRealm.isEmpty)
                        assertEquals(1, backupRealm.where(SyncStringOnly::class.java).count())
                        val allSorted = backupRealm.where(SyncStringOnly::class.java).findAll()
                        assertEquals("Foo", allSorted[0]!!.chars)
                    }
                    looperThread.testComplete()
                }
                .build()

        val realm = Realm.getInstance(config)
        realm.executeTransaction {
            it.createObject(SyncStringOnly::class.java, ObjectId()).chars = "Foo"
        }
        resources.add(realm)

        // Trigger error
        user.app.sync.simulateClientReset(realm.syncSession)
    }

    // make sure the backup file Realm is encrypted with the same key as the original synced Realm.
    @Test
    @Deprecated("clientResetHandler deprecated in favor of syncClientResetStrategy")
    fun errorHandler_useClientResetEncrypted() = looperThread.runBlocking {
        val resources = ResourceContainer()

        val randomKey = TestHelper.getRandomKey()
        val config = configFactory.createSyncConfigurationBuilder(user)
                .encryptionKey(randomKey)
                .modules(SyncStringOnlyModule())
                .clientResetHandler { session: SyncSession?, error: ClientResetRequiredError ->
                    // Execute Client Reset
                    resources.close()
                    error.executeClientReset()
                    var backupRealmConfiguration = error.backupRealmConfiguration

                    // can open encrypted backup Realm
                    Realm.getInstance(backupRealmConfiguration).use { backupEncryptedRealm ->
                        assertEquals(1, backupEncryptedRealm.where(SyncStringOnly::class.java).count())
                        val allSorted = backupEncryptedRealm.where(SyncStringOnly::class.java).findAll()
                        assertEquals("Foo", allSorted[0]!!.chars)
                    }
                    val backupFile = error.backupFile.absolutePath

                    // build a conf to open a DynamicRealm
                    backupRealmConfiguration = SyncConfiguration.forRecovery(backupFile, randomKey, SyncStringOnlyModule())
                    Realm.getInstance(backupRealmConfiguration).use { backupEncryptedRealm ->
                        assertEquals(1, backupEncryptedRealm.where(SyncStringOnly::class.java).count())
                        val allSorted = backupEncryptedRealm.where(SyncStringOnly::class.java).findAll()
                        assertEquals("Foo", allSorted[0]!!.chars)
                    }

                    // using wrong key throw
                    assertFailsWith<RealmFileException> {
                        Realm.getInstance(SyncConfiguration.forRecovery(backupFile, TestHelper.getRandomKey(), SyncStringOnlyModule()))
                    }
                    looperThread.testComplete()
                }
                .build()

        val realm = Realm.getInstance(config)
        realm.executeTransaction {
            realm.createObject(SyncStringOnly::class.java, ObjectId()).chars = "Foo"
        }
        resources.add(realm)

        // Trigger error
        user.app.sync.simulateClientReset(realm.syncSession)
    }


    // Check that a Client Reset is correctly reported.
    @Test
    fun errorHandler_manuallyRecoverClientResetReported() = looperThread.runBlocking {
        val config = configFactory.createSyncConfigurationBuilder(user)
            .testSchema(SyncStringOnly::class.java)
            .syncClientResetStrategy { session: SyncSession, error: ClientResetRequiredError ->
                val filePathFromError = error.originalFile.absolutePath
                val filePathFromConfig = session.configuration.path
                assertEquals(filePathFromError, filePathFromConfig)
                assertFalse(error.backupFile.exists())
                assertTrue(error.originalFile.exists())
                // Note, this error message is just the one created by ObjectStore for testing
                // The server will send a different message. This just ensures that we don't
                // accidentially modify or remove the message.
                assertEquals("Simulate Client Reset", error.message)
                looperThread.testComplete()
            }
            .build()

        val realm = Realm.getInstance(config)
        looperThread.closeAfterTest(realm)

        // Trigger error
        user.app.sync.simulateClientReset(realm.syncSession)
    }

    // Check that we can manually execute the Client Reset.
    @Test
    fun errorHandler_manuallyRecoverExecuteClientReset() = looperThread.runBlocking {
        val resources = ResourceContainer()

        val config = configFactory.createSyncConfigurationBuilder(user)
            .testSchema(SyncStringOnly::class.java)
            .syncClientResetStrategy { _: SyncSession, error: ClientResetRequiredError ->
                try {
                    error.executeClientReset()
                    fail("All Realms should be closed before executing Client Reset can be allowed")
                } catch (ignored: IllegalStateException) {
                }

                // Execute Client Reset
                resources.close()
                error.executeClientReset()

                // Validate that files have been moved
                assertFalse(error.originalFile.exists())
                assertTrue(error.backupFile.exists())
                looperThread.testComplete()
            }
            .build()
        val realm = Realm.getInstance(config)
        resources.add(realm)

        // Trigger error
        user.app.sync.simulateClientReset(realm.syncSession)
    }

    // Check that we can use the backup SyncConfiguration to open the Realm.
    @Test
    fun errorHandler_manuallyUseBackupSyncConfigurationForClientReset() = looperThread.runBlocking {
        val resources = ResourceContainer()
        val config = configFactory.createSyncConfigurationBuilder(user)
            .schema(SyncStringOnly::class.java)
            .syncClientResetStrategy { _: SyncSession?, error: ClientResetRequiredError ->
                // Execute Client Reset
                resources.close()
                error.executeClientReset()

                // Validate that files have been moved
                assertFalse(error.originalFile.exists())
                assertTrue(error.backupFile.exists())
                val backupRealmConfiguration = error.backupRealmConfiguration
                assertNotNull(backupRealmConfiguration)
                assertFalse(backupRealmConfiguration is SyncConfiguration)
                assertTrue(backupRealmConfiguration.isRecoveryConfiguration)
                Realm.getInstance(backupRealmConfiguration).use { backupRealm ->
                    assertFalse(backupRealm.isEmpty)
                    assertEquals(1, backupRealm.where(SyncStringOnly::class.java).count())
                    assertEquals("Foo", backupRealm.where(SyncStringOnly::class.java).findAll().first()!!.chars)
                }

                // opening a Dynamic Realm should also work
                DynamicRealm.getInstance(backupRealmConfiguration).use { dynamicRealm ->
                    assertNotNull(dynamicRealm.schema.get(SyncStringOnly.CLASS_NAME))
                    val all = dynamicRealm.where(SyncStringOnly.CLASS_NAME).findAll()
                    assertEquals(1, all.size.toLong())
                    assertEquals("Foo", all.first()!!.getString(SyncStringOnly.FIELD_CHARS))
                }
                looperThread.testComplete()
            }
            .build()
        val realm = Realm.getInstance(config)
        realm.executeTransaction {
            realm.createObject(SyncStringOnly::class.java, ObjectId()).chars = "Foo"
        }
        resources.add(realm)

        // Trigger error
        user.app.sync.simulateClientReset(realm.syncSession)
    }

    // Check that we can open the backup file without using the provided SyncConfiguration,
    // this might be the case if the user decide to act upon the client reset later (providing s/he
    // persisted the location of the file)
    @Test
    fun errorHandler_manuallyUseBackupSyncConfigurationAfterClientReset() = looperThread.runBlocking {
        val resources = ResourceContainer()
        val config = configFactory.createSyncConfigurationBuilder(user)
            .modules(SyncStringOnlyModule())
            .syncClientResetStrategy { session: SyncSession?, error: ClientResetRequiredError ->
                // Execute Client Reset
                resources.close()
                error.executeClientReset()

                // Validate that files have been moved
                assertFalse(error.originalFile.exists())
                assertTrue(error.backupFile.exists())
                val backupFile = error.backupFile.absolutePath

                // this SyncConf doesn't specify any module, it will throw a migration required
                // exception since the backup Realm contain only StringOnly table
                var backupRealmConfiguration = SyncConfiguration.forRecovery(backupFile)
                assertFailsWith<RealmMigrationNeededException> {
                    Realm.getInstance(backupRealmConfiguration)
                }

                // opening a DynamicRealm will work though
                DynamicRealm.getInstance(backupRealmConfiguration).use { dynamicRealm ->
                    assertNotNull(dynamicRealm.schema.get(SyncStringOnly.CLASS_NAME))
                    val all = dynamicRealm.where(SyncStringOnly.CLASS_NAME).findAll()
                    assertEquals(1, all.size.toLong())
                    assertEquals("Foo", all.first()!!.getString(SyncStringOnly.FIELD_CHARS))
                    // make sure we can't write to it (read-only Realm)
                    assertFailsWith<java.lang.IllegalStateException> {
                        dynamicRealm.beginTransaction()
                    }
                }

                assertFailsWith<IllegalArgumentException> {
                    SyncConfiguration.forRecovery(backupFile, null, SyncStringOnly::class.java)
                }

                // specifying the module will allow to open the typed Realm
                backupRealmConfiguration = SyncConfiguration.forRecovery(backupFile, null, SyncStringOnlyModule())
                Realm.getInstance(backupRealmConfiguration).use { backupRealm ->
                    assertFalse(backupRealm.isEmpty)
                    assertEquals(1, backupRealm.where(SyncStringOnly::class.java).count())
                    val allSorted = backupRealm.where(SyncStringOnly::class.java).findAll()
                    assertEquals("Foo", allSorted[0]!!.chars)
                }
                looperThread.testComplete()
            }
            .build()

        val realm = Realm.getInstance(config)
        realm.executeTransaction {
            it.createObject(SyncStringOnly::class.java, ObjectId()).chars = "Foo"
        }
        resources.add(realm)

        // Trigger error
        user.app.sync.simulateClientReset(realm.syncSession)
    }

    // make sure the backup file Realm is encrypted with the same key as the original synced Realm.
    @Test
    fun errorHandler_manuallyUseClientResetEncrypted() = looperThread.runBlocking {
        val resources = ResourceContainer()

        val randomKey = TestHelper.getRandomKey()
        val config = configFactory.createSyncConfigurationBuilder(user)
            .encryptionKey(randomKey)
            .modules(SyncStringOnlyModule())
            .syncClientResetStrategy { session: SyncSession?, error: ClientResetRequiredError ->
                // Execute Client Reset
                resources.close()
                error.executeClientReset()
                var backupRealmConfiguration = error.backupRealmConfiguration

                // can open encrypted backup Realm
                Realm.getInstance(backupRealmConfiguration).use { backupEncryptedRealm ->
                    assertEquals(1, backupEncryptedRealm.where(SyncStringOnly::class.java).count())
                    val allSorted = backupEncryptedRealm.where(SyncStringOnly::class.java).findAll()
                    assertEquals("Foo", allSorted[0]!!.chars)
                }
                val backupFile = error.backupFile.absolutePath

                // build a conf to open a DynamicRealm
                backupRealmConfiguration = SyncConfiguration.forRecovery(backupFile, randomKey, SyncStringOnlyModule())
                Realm.getInstance(backupRealmConfiguration).use { backupEncryptedRealm ->
                    assertEquals(1, backupEncryptedRealm.where(SyncStringOnly::class.java).count())
                    val allSorted = backupEncryptedRealm.where(SyncStringOnly::class.java).findAll()
                    assertEquals("Foo", allSorted[0]!!.chars)
                }

                // using wrong key throw
                assertFailsWith<RealmFileException> {
                    Realm.getInstance(SyncConfiguration.forRecovery(backupFile, TestHelper.getRandomKey(), SyncStringOnlyModule()))
                }
                looperThread.testComplete()
            }
            .build()

        val realm = Realm.getInstance(config)
        realm.executeTransaction {
            realm.createObject(SyncStringOnly::class.java, ObjectId()).chars = "Foo"
        }
        resources.add(realm)

        // Trigger error
        user.app.sync.simulateClientReset(realm.syncSession)
    }

    @Test
    @UiThreadTest
    fun uploadAllLocalChanges_throwsOnUiThread() {
        Realm.getInstance(configuration).use { realm ->
            assertFailsWith<java.lang.IllegalStateException> {
                realm.syncSession.uploadAllLocalChanges()
            }
        }
    }

    @Test
    @UiThreadTest
    fun uploadAllLocalChanges_withTimeout_throwsOnUiThread() {
        Realm.getInstance(configuration).use { realm ->
            assertFailsWith<IllegalStateException> {
                realm.syncSession.uploadAllLocalChanges(30, TimeUnit.SECONDS)
            }
        }
    }

    @Test
    fun uploadAllLocalChanges_withTimeout_invalidParametersThrows() {
        Realm.getInstance(configuration). use { realm ->
            val session = realm.syncSession
            assertFailsWith<IllegalArgumentException> {
                session.uploadAllLocalChanges(-1, TimeUnit.SECONDS)
            }
            assertFailsWith<IllegalArgumentException> {
                session.uploadAllLocalChanges(1, TestHelper.getNull())
            }
        }
    }

    @Test
    fun uploadAllLocalChanges_returnFalseWhenTimedOut() {
        Realm.getInstance(configuration).use { realm ->
            val session = realm.syncSession
            // We never assume to be able to download changes with one 1ms
            assertFalse(session.uploadAllLocalChanges(1, TimeUnit.MILLISECONDS))
        }
    }

    @Test
    @UiThreadTest
    fun downloadAllServerChanges_throwsOnUiThread() {
        Realm.getInstance(configuration).use { realm ->
            assertFailsWith<IllegalStateException> {
                realm.syncSession.downloadAllServerChanges()
            }
        }
    }

    @Test
    @UiThreadTest
    fun downloadAllServerChanges_withTimeout_throwsOnUiThread() {
        Realm.getInstance(configuration).use { realm ->
            assertFailsWith<IllegalStateException> {
                realm.syncSession.downloadAllServerChanges(30, TimeUnit.SECONDS)
            }
        }
    }

    @Test
    fun downloadAllServerChanges_withTimeout_invalidParametersThrows() {
        Realm.getInstance(configuration).use { realm ->
            val session = realm.syncSession
            assertFailsWith<IllegalArgumentException> {
                session.downloadAllServerChanges(-1, TimeUnit.SECONDS)
            }
            assertFailsWith<IllegalArgumentException> {
                session.downloadAllServerChanges(1, TestHelper.getNull())
            }
        }
    }

    @Test
    fun downloadAllServerChanges_returnFalseWhenTimedOut() {
        Realm.getInstance(configuration).use { realm ->
            val session = realm.syncSession
            // We never assume to be able to download changes within one 1ms
            assertFalse(session.downloadAllServerChanges(1, TimeUnit.MILLISECONDS))
        }
    }

    @Test
    @UiThreadTest
    fun unrecognizedErrorCode_errorHandler() {
        val errorHandlerCalled = AtomicBoolean(false)
        configuration = configFactory.createSyncConfigurationBuilder(user)
                .testSchema(SyncStringOnly::class.java)
                .errorHandler { session: SyncSession?, error: AppException ->
                    errorHandlerCalled.set(true)
                    assertEquals(ErrorCode.UNKNOWN, error.errorCode)
                    assertEquals(ErrorCode.Category.FATAL, error.category)
                }
                .build()

        Realm.getInstance(configuration).use { realm ->
            val session = realm.syncSession
            // TODO This test requires errors to be reported, when running full test suite
            //  some test running before leaves it at FATAL. Do we have conventions about it? For
            //  now just lowering while triggering the actual test
            val level = RealmLog.getLevel()
            RealmLog.setLevel(LogLevel.WARN)
            val testLogger = TestLogger()
            RealmLog.add(testLogger)
            session.notifySessionError("unknown", 3, "Unknown Error", "")
            RealmLog.remove(testLogger)
            // TODO See comment above
            RealmLog.setLevel(level)
            assertTrue(errorHandlerCalled.get())
            assertEquals("Unknown error code: 'unknown:3'", testLogger.message)
        }
    }

    // Closing the Realm should remove the session
    @Test
    fun getSessionThrowsOnNonExistingSession() {
        Realm.getInstance(configuration).use { realm ->
            val session = realm.syncSession
            assertEquals(configuration, session.configuration)
        // Exiting the scope closes the Realm and should remove the session
        }
        assertFailsWithMessage<IllegalStateException>(
                CoreMatchers.containsString( "No SyncSession found using the path : ")
        ) {
            app.sync.getSession(configuration)
        }
    }

    @Test
    fun stop_doesNotThrowIfCalledWhenRealmIsClosed() {
        val realm = Realm.getInstance(configuration)
        val session = realm.syncSession
        realm.close()
        session.stop()
    }

    // Smoke test of discouraged method of retrieving session
    @Test
    fun getOrCreateSession() {
        assertNotNull(app.sync.getOrCreateSession(configuration))
    }

    @Test
    fun getAllSessions(){
        val realm = Realm.getInstance(configuration)
        val sessions = app.sync.allSessions

        assertNotNull(sessions)
        assertEquals(1, sessions.size)

        realm.close()
    }
    
}
