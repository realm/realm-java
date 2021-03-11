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

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.entities.DefaultSyncSchema
import io.realm.entities.SyncDog
import io.realm.entities.SyncStringOnly
import io.realm.internal.OsRealmConfig
import io.realm.kotlin.syncSession
import io.realm.kotlin.where
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.mongodb.User
import io.realm.mongodb.close
import io.realm.mongodb.registerUserAndLogin
import io.realm.mongodb.sync.Progress
import io.realm.mongodb.sync.ProgressListener
import io.realm.mongodb.sync.ProgressMode
import io.realm.mongodb.sync.SyncConfiguration
import io.realm.mongodb.sync.SyncSession
import io.realm.mongodb.sync.testRealmExists
import io.realm.mongodb.sync.testSchema
import io.realm.mongodb.sync.testSessionStopPolicy
import io.realm.rule.BlockingLooperThread
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertFailsWith

@RunWith(AndroidJUnit4::class)
class ProgressListenerTests {

    companion object {
        private const val TEST_SIZE: Long = 10
    }

    private val looperThread = BlockingLooperThread()
    private val configurationFactory = TestSyncConfigurationFactory()

    private lateinit var app: TestApp
    private lateinit var partitionValue: String

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        RealmLog.setLevel(LogLevel.TRACE)
        partitionValue = UUID.randomUUID().toString()
        app = TestApp()
    }

    @After
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
        RealmLog.setLevel(LogLevel.WARN)
    }

    @Test
    fun downloadProgressListener_changesOnly() {
        val allChangesDownloaded = CountDownLatch(1)
        val user1: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        val user1Config = createSyncConfig(user1)
        createRemoteData(user1Config)
        val user2: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        val user2Config = createSyncConfig(user2)
        Realm.getInstance(user2Config).use { realm ->
            realm.syncSession.addDownloadProgressListener(ProgressMode.CURRENT_CHANGES) { progress ->
                if (progress.isTransferComplete) {
                    assertTransferComplete(progress, true)
                    assertEquals(TEST_SIZE, getStoreTestDataSize(user2Config))
                    allChangesDownloaded.countDown()
                }
            }
            TestHelper.awaitOrFail(allChangesDownloaded)
        }
    }

    @Test
    fun downloadProgressListener_indefinitely() {
        val transferCompleted = AtomicInteger(0)
        val allChangesDownloaded = CountDownLatch(1)
        val startWorker = CountDownLatch(1)
        val user1: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456") // login(Credentials.anonymous())
        val user1Config: SyncConfiguration = createSyncConfig(user1)

        // Create worker thread that puts data into another Realm.
        // This is to avoid blocking one progress listener while waiting for another to complete.
        val worker = Thread(Runnable {
            TestHelper.awaitOrFail(startWorker)
            createRemoteData(user1Config)
        })
        worker.start()
        val user2: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456") // login(Credentials.anonymous())
        val user2Config: SyncConfiguration = createSyncConfig(user2)
        Realm.getInstance(user2Config).use { user2Realm ->
            val session: SyncSession = user2Realm.syncSession
            session.addDownloadProgressListener(ProgressMode.INDEFINITELY) { progress ->
                val objectCounts = getStoreTestDataSize(user2Config)
                // The downloading progress listener could be triggered at the db version where only contains the meta
                // data. So we start checking from when the first 10 objects downloaded.
                RealmLog.warn(String.format(
                        Locale.ENGLISH, "downloadProgressListener_indefinitely download %d/%d objects count:%d",
                        progress.transferredBytes, progress.transferableBytes, objectCounts))
                if (objectCounts != 0L && progress.isTransferComplete) {
                    when (transferCompleted.incrementAndGet()) {
                        1 -> {
                            assertEquals(TEST_SIZE, objectCounts)
                            assertTransferComplete(progress, true)
                            startWorker.countDown()
                        }
                        2 -> {
                            assertTransferComplete(progress, true)
                            assertEquals(TEST_SIZE * 2, objectCounts)
                            allChangesDownloaded.countDown()
                        }
                        else -> fail("Transfer complete called too many times:" + transferCompleted.get())
                    }
                }
            }
            writeSampleData(user2Realm) // Write first batch of sample data
            TestHelper.awaitOrFail(allChangesDownloaded)
        }
        // worker thread will hang if logout happens before listener triggered.
        worker.join()
        user1.logOut()
        user2.logOut()
    }

    // Make sure that a ProgressListener continues to report the correct thing, even if it crashed
    @Test
    fun uploadListener_worksEvenIfCrashed() {
        val transferCompleted = AtomicInteger(0)
        val testDone = CountDownLatch(1)
        val config = createSyncConfig()
        Realm.getInstance(config).use { realm ->
            val session: SyncSession = realm.syncSession
            session.addUploadProgressListener(ProgressMode.INDEFINITELY) { progress ->
                if (progress.isTransferComplete) {
                    when (transferCompleted.incrementAndGet()) {
                        1 -> {
                            Realm.getInstance(config).use { realm ->
                            writeSampleData(realm)
                            }
                            throw RuntimeException("Crashing the changelistener")
                        }
                        2 -> {
                            assertTransferComplete(progress, true)
                            testDone.countDown()
                        }
                        else -> fail("Unsupported number of transfers completed: " + transferCompleted.get())
                    }
                }
            }
            writeSampleData(realm) // Write first batch of sample data
            TestHelper.awaitOrFail(testDone)
        }
    }

    @Test
    fun uploadProgressListener_changesOnly() {
        val allChangeUploaded = CountDownLatch(1)
        val config = createSyncConfig()
        Realm.getInstance(config).use { realm ->
            val session: SyncSession = realm.syncSession
            assertEquals(SyncSession.State.ACTIVE, session.state)
            writeSampleData(realm)
            session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES) { progress ->
                if (progress.isTransferComplete) {
                    assertTransferComplete(progress, true)
                    allChangeUploaded.countDown()
                }
            }
            TestHelper.awaitOrFail(allChangeUploaded)
        }
    }

    @Test
    fun uploadProgressListener_indefinitely() {
        val transferCompleted = AtomicInteger(0)
        val testDone = CountDownLatch(1)
        val config = createSyncConfig()
        Realm.getInstance(config).use { realm ->
            val session: SyncSession = realm.syncSession
            session.addUploadProgressListener(ProgressMode.INDEFINITELY) { progress ->
                if (progress.isTransferComplete) {
                    when (transferCompleted.incrementAndGet()) {
                        1 -> {
                            Realm.getInstance(config).use { realm ->
                                writeSampleData(realm)
                            }
                        }
                        2 -> {
                            assertTransferComplete(progress, true)
                            testDone.countDown()
                        }
                        else -> fail("Unsupported number of transfers completed: " + transferCompleted.get())
                    }
                }
            }
            writeSampleData(realm) // Write first batch of sample data
            TestHelper.awaitOrFail(testDone)
        }
    }

    @Test
    fun addListenerInsideCallback() {
        val allChangeUploaded = CountDownLatch(1)
        val config = createSyncConfig()
        Realm.getInstance(config).use { realm ->
            val session: SyncSession = realm.syncSession
            writeSampleData(realm)
            session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES) { progress ->
                if (progress.isTransferComplete) {
                    Realm.getInstance(config).use { realm ->
                        writeSampleData(realm)
                    }
                    session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES) { progress ->
                        if (progress.isTransferComplete) {
                            allChangeUploaded.countDown()
                        }
                    }
                }
            }
            TestHelper.awaitOrFail(allChangeUploaded)
        }
    }

    @Test
    fun addListenerInsideCallback_mixProgressModes() {
        val allChangeUploaded = CountDownLatch(3)
        val progressCompletedReported = AtomicBoolean(false)
        val config = createSyncConfig()
        Realm.getInstance(config).use { realm ->
            val session: SyncSession = realm.syncSession
            session.addUploadProgressListener(ProgressMode.INDEFINITELY) { progress ->
                if (progress.isTransferComplete) {
                    allChangeUploaded.countDown()
                    if (progressCompletedReported.compareAndSet(false, true)) {
                        Realm.getInstance(config).use { realm ->
                            writeSampleData(realm)
                        }
                        session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES) { progress ->
                            if (progress.isTransferComplete) {
                                allChangeUploaded.countDown()
                            }
                        }
                    }
                }
            }
            writeSampleData(realm)
            TestHelper.awaitOrFail(allChangeUploaded)
        }
    }

    @Test
    fun addProgressListener_triggerImmediatelyWhenRegistered() {
        val config = createSyncConfig()
        Realm.getInstance(config).use { realm ->
            val session: SyncSession = realm.syncSession
            checkDownloadListener(session, ProgressMode.INDEFINITELY)
            checkUploadListener(session, ProgressMode.INDEFINITELY)
            checkDownloadListener(session, ProgressMode.CURRENT_CHANGES)
            checkUploadListener(session, ProgressMode.CURRENT_CHANGES)
        }
    }

    @Test
    fun addProgressListener_triggerImmediatelyWhenRegistered_waitForInitialRemoteData() {
        val user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        val config = SyncConfiguration.Builder(user, getTestPartitionValue())
                .waitForInitialRemoteData()
                .modules(DefaultSyncSchema())
                .build()
        Realm.getInstance(config).use { realm ->
            val session: SyncSession = realm.syncSession
            checkDownloadListener(session, ProgressMode.INDEFINITELY)
            checkUploadListener(session, ProgressMode.INDEFINITELY)
            checkDownloadListener(session, ProgressMode.CURRENT_CHANGES)
            checkUploadListener(session, ProgressMode.CURRENT_CHANGES)
        }
    }

    @Test
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

        assertFailsWith<IllegalStateException> {
            app.sync.getSession(configOld)
        }

        // 2. Local state should now be completely reset. Open the same sync Realm but different local name again with
        // a new configuration which should download the uploaded changes (pray it managed to do so within the time frame).
        // Use different user to trigger different path
        val user2 = app.registerUserAndLogin(TestHelper.getRandomEmail(), password)
        val config: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user2, user.id)
                .testSchema(SyncStringOnly::class.java)
                .waitForInitialRemoteData()
                .build()

        assertFalse(config.testRealmExists())

        val countDownLatch = CountDownLatch(2)

        val indefiniteListenerComplete = AtomicBoolean(false)
        val currentChangesListenerComplete = AtomicBoolean(false)
        val task = Realm.getInstanceAsync(config, object : Realm.Callback() {
            override fun onSuccess(realm: Realm) {
                realm.syncSession.addDownloadProgressListener(ProgressMode.INDEFINITELY, object : ProgressListener {
                    override fun onChange(progress: Progress) {
                        if (progress.isTransferComplete()) {
                            indefiniteListenerComplete.set(true)
                            countDownLatch.countDown()
                        }
                    }
                })
                realm.syncSession.addDownloadProgressListener(ProgressMode.CURRENT_CHANGES, object : ProgressListener {
                    override fun onChange(progress: Progress) {
                        if (progress.isTransferComplete()) {
                            currentChangesListenerComplete.set(true)
                            countDownLatch.countDown()
                        }
                    }
                })
                countDownLatch.await(100, TimeUnit.SECONDS)
                realm.close()
                if (!indefiniteListenerComplete.get()) {
                    fail("Indefinite progress listener did not report complete.")
                }
                if (!currentChangesListenerComplete.get()) {
                    fail("Current changes progress listener did not report complete.")
                }
                looperThread.testComplete()
            }

            override fun onError(exception: Throwable) {
                fail(exception.toString())
            }
        })
        looperThread.keepStrongReference(task)
    }

    @Test
    fun uploadListener_keepIncreasingInSize() {
        val config = createSyncConfig()
        Realm.getInstance(config).use { realm ->
            val session: SyncSession = realm.syncSession
            for (i in 0..9) {
                val changesUploaded = CountDownLatch(1)
                writeSampleData(realm)
                session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES) { progress ->
                    if (progress.isTransferComplete) {
                        assertTransferComplete(progress, true)
                        changesUploaded.countDown()
                    }
                }
                TestHelper.awaitOrFail(changesUploaded)
            }
        }
    }

    private fun checkDownloadListener(session: SyncSession, progressMode: ProgressMode) {
        val listenerCalled = CountDownLatch(1)
        session.addDownloadProgressListener(progressMode) { progress ->
            listenerCalled.countDown()
        }
        TestHelper.awaitOrFail(listenerCalled, 30)
    }
    private fun checkUploadListener(session: SyncSession, progressMode: ProgressMode) {
        val listenerCalled = CountDownLatch(1)
        session.addUploadProgressListener(progressMode) { progress ->
            listenerCalled.countDown()
        }
        TestHelper.awaitOrFail(listenerCalled, 30)
    }


    private fun writeSampleData(realm: Realm, partitionValue: String = getTestPartitionValue()) {
        realm.executeTransaction {
            for (i in 0 until TEST_SIZE) {
                val obj = SyncDog()
                obj.name = "Object $i"
                realm.insert(obj)
            }
        }
    }

    private fun assertTransferComplete(progress: Progress, nonZeroChange: Boolean) {
        assertTrue(progress.isTransferComplete)
        assertEquals(1.0, progress.fractionTransferred, 0.0)
        assertEquals(progress.transferableBytes, progress.transferredBytes)
        if (nonZeroChange) {
            assertTrue(progress.transferredBytes > 0)
        }
    }

    // Create remote data for a given user.
    private fun createRemoteData(config: SyncConfiguration) {
        Realm.getInstance(config).use { realm ->
            val changesUploaded = CountDownLatch(1)
            val session: SyncSession = realm.syncSession
            writeSampleData(realm)
            session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES, object : ProgressListener {
                override fun onChange(progress: Progress) {
                    if (progress.isTransferComplete) {
                        session.removeProgressListener(this)
                        changesUploaded.countDown()
                    }
                }
            })
            TestHelper.awaitOrFail(changesUploaded)
        }
    }

    private fun getStoreTestDataSize(config: RealmConfiguration): Long {
        Realm.getInstance(config).use { realm ->
            return realm.where<SyncDog>().count()
        }
    }

    private fun createSyncConfig(user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456"), partitionValue: String = getTestPartitionValue()): SyncConfiguration {
        return SyncConfiguration.Builder(user, partitionValue)
                .modules(DefaultSyncSchema())
                .build()
    }

    private fun getTestPartitionValue(): String {
        if (!this::partitionValue.isInitialized) {
            fail("Test not setup correctly. Partition value is missing");
        }
        return partitionValue
    }
}
