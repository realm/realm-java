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
import io.realm.kotlin.where
import io.realm.kotlin.syncSession
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.rule.BlockingLooperThread
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@Ignore("These are generally flaky. We need to investigate further.")
@RunWith(AndroidJUnit4::class)
class ProgressListenerTests {

    companion object {
        private const val TEST_SIZE: Long = 10
    }

    private val looperThread = BlockingLooperThread()
    private lateinit var app: TestRealmApp
    private lateinit var realm: Realm
    private lateinit var partitionValue: String

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        RealmLog.setLevel(LogLevel.TRACE)
        partitionValue = UUID.randomUUID().toString()
        app = TestRealmApp()
    }

    @After
    fun tearDown() {
        if (this::realm.isInitialized) {
            realm.close()
            app.close()
        }
        RealmLog.setLevel(LogLevel.WARN)
    }

    @Ignore("See https://mongodb.slack.com/archives/CQLDYRJ3V/p1587563930459100")
    @Test
    fun downloadProgressListener_changesOnly() {
        val allChangesDownloaded = CountDownLatch(1)
        val user1: RealmUser = app.login(RealmCredentials.anonymous())
        val user1Config = createSyncConfig(user1)
        createRemoteData(user1Config)
        val user2: RealmUser = app.login(RealmCredentials.anonymous())
        val user2Config = createSyncConfig(user2)
        val realm = Realm.getInstance(user2Config)
        val session: SyncSession = realm.syncSession
        session.addDownloadProgressListener(ProgressMode.CURRENT_CHANGES) { progress ->
            RealmLog.error(progress.toString())
            if (progress.isTransferComplete) {
                assertTransferComplete(progress, true)
                assertEquals(TEST_SIZE, getStoreTestDataSize(user2Config))
                allChangesDownloaded.countDown()
            }
        }
        TestHelper.awaitOrFail(allChangesDownloaded)
        realm.close()
    }

    @Test
    fun downloadProgressListener_indefinitely() {
        val transferCompleted = AtomicInteger(0)
        val allChangesDownloaded = CountDownLatch(1)
        val startWorker = CountDownLatch(1)
        val user1: RealmUser = app.login(RealmCredentials.anonymous())
        val user1Config: SyncConfiguration = createSyncConfig(user1)

        // Create worker thread that puts data into another Realm.
        // This is to avoid blocking one progress listener while waiting for another to complete.
        val worker = Thread(Runnable {
            TestHelper.awaitOrFail(startWorker)
            createRemoteData(user1Config)
        })
        worker.start()
        val user2: RealmUser = app.login(RealmCredentials.anonymous())
        val user2Config: SyncConfiguration = createSyncConfig(user2)
        val user2Realm = Realm.getInstance(user2Config)
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
        TestHelper.awaitOrFail(allChangesDownloaded)
        user2Realm.close()
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
        val realm = Realm.getInstance(config)
        writeSampleData(realm) // Write first batch of sample data
        val session: SyncSession = realm.syncSession
        session.addUploadProgressListener(ProgressMode.INDEFINITELY) { progress ->
            if (progress.isTransferComplete) {
                when (transferCompleted.incrementAndGet()) {
                    1 -> {
                        val realm = Realm.getInstance(config)
                        writeSampleData(realm)
                        realm.close()
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
        TestHelper.awaitOrFail(testDone)
        realm.close()
    }

    @Test
    fun uploadProgressListener_changesOnly() {
        val allChangeUploaded = CountDownLatch(1)
        val config = createSyncConfig()
        val realm = Realm.getInstance(config)
        writeSampleData(realm)
        val session: SyncSession = realm.syncSession
        assertEquals(SyncSession.State.ACTIVE, session.state)
        session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES) { progress ->
            RealmLog.error(progress.toString());
            if (progress.isTransferComplete) {
                assertTransferComplete(progress, true)
                allChangeUploaded.countDown()
            }
        }
        TestHelper.awaitOrFail(allChangeUploaded)
        realm.close()
    }

    @Test
    fun uploadProgressListener_indefinitely() {
        val transferCompleted = AtomicInteger(0)
        val testDone = CountDownLatch(1)
        val config = createSyncConfig()
        val realm = Realm.getInstance(config)
        writeSampleData(realm) // Write first batch of sample data
        val session: SyncSession = realm.syncSession
        session.addUploadProgressListener(ProgressMode.INDEFINITELY) { progress ->
            if (progress.isTransferComplete) {
                when (transferCompleted.incrementAndGet()) {
                    1 -> {
                        val realm = Realm.getInstance(config)
                        writeSampleData(realm)
                        realm.close()
                    }
                    2 -> {
                        assertTransferComplete(progress, true)
                        testDone.countDown()
                    }
                    else -> fail("Unsupported number of transfers completed: " + transferCompleted.get())
                }
            }
        }
        TestHelper.awaitOrFail(testDone)
        realm.close()
    }

    @Test
    fun addListenerInsideCallback() {
        val allChangeUploaded = CountDownLatch(1)
        val config = createSyncConfig()
        val realm = Realm.getInstance(config)
        writeSampleData(realm)
        val session: SyncSession = realm.syncSession
        session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES) { progress ->
            if (progress.isTransferComplete) {
                val realm = Realm.getInstance(config)
                writeSampleData(realm)
                realm.close()
                session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES) { progress ->
                    if (progress.isTransferComplete) {
                        allChangeUploaded.countDown()
                    }
                }
            }
        }
        TestHelper.awaitOrFail(allChangeUploaded)
        realm.close()
    }

    @Test
    fun addListenerInsideCallback_mixProgressModes() {
        val allChangeUploaded = CountDownLatch(3)
        val progressCompletedReported = AtomicBoolean(false)
        val config = createSyncConfig()
        val realm = Realm.getInstance(config)
        writeSampleData(realm)
        val session: SyncSession = realm.syncSession
        session.addUploadProgressListener(ProgressMode.INDEFINITELY) { progress ->
            if (progress.isTransferComplete) {
                allChangeUploaded.countDown()
                if (progressCompletedReported.compareAndSet(false, true)) {
                    val realm = Realm.getInstance(config)
                    writeSampleData(realm)
                    realm.close()
                    session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES) { progress ->
                        if (progress.isTransferComplete) {
                            allChangeUploaded.countDown()
                        }
                    }
                }
            }
        }
        TestHelper.awaitOrFail(allChangeUploaded)
        realm.close()
    }

    @Test
    fun addProgressListener_triggerImmediatelyWhenRegistered() {
        val config = createSyncConfig()
        val realm = Realm.getInstance(config)
        val session: SyncSession = realm.syncSession
        checkListener(session, ProgressMode.INDEFINITELY)
        checkListener(session, ProgressMode.CURRENT_CHANGES)
        realm.close()
    }

    @Test
    fun uploadListener_keepIncreasingInSize() {
        val config = createSyncConfig()
        val realm = Realm.getInstance(config)
        val session: SyncSession = realm.syncSession
        for (i in 0..9) {
            val changesUploaded = CountDownLatch(1)
            writeSampleData(realm)
            session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES) { progress ->
                RealmLog.info("Test %s -> %s", Integer.toString(i), progress.toString())
                if (progress.isTransferComplete) {
                    assertTransferComplete(progress, true)
                    changesUploaded.countDown()
                }
            }
            TestHelper.awaitOrFail(changesUploaded)
        }
        realm.close()
    }

    private fun checkListener(session: SyncSession, progressMode: ProgressMode) {
        val listenerCalled = CountDownLatch(1)
        session.addDownloadProgressListener(progressMode) { listenerCalled.countDown() }
        TestHelper.awaitOrFail(listenerCalled)
    }

    private fun writeSampleData(realm: Realm, partitionValue: String = getTestPartitionValue()) {
        realm.beginTransaction()
        for (i in 0 until TEST_SIZE) {
            val obj = SyncDog()
            obj.realmId = partitionValue
            obj.name = "Object $i"
            realm.insert(obj)
        }
        realm.commitTransaction()
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
        val realm = Realm.getInstance(config)
        val changesUploaded = CountDownLatch(1)
        writeSampleData(realm)
        val session: SyncSession = realm.syncSession
        session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES, object : ProgressListener {
            override fun onChange(progress: Progress) {
                if (progress.isTransferComplete) {
                    session.removeProgressListener(this)
                    changesUploaded.countDown()
                }
            }
        })
        TestHelper.awaitOrFail(changesUploaded)
        realm.close()
    }

    private fun getStoreTestDataSize(config: RealmConfiguration): Long {
        val realm: Realm = Realm.getInstance(config)
        val objectCounts: Long = realm.where<SyncDog>().count()
        realm.close()
        return objectCounts
    }

    private fun createSyncConfig(user: RealmUser = app.login(RealmCredentials.anonymous()), partitionValue: String = getTestPartitionValue()): SyncConfiguration {
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