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
import io.realm.entities.AllTypes
import io.realm.entities.SyncColor
import io.realm.kotlin.where
import io.realm.kotlin.syncSession
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.rule.BlockingLooperThread
import org.junit.After
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URI
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

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
            if (progress.isTransferComplete) {
                assertTransferComplete(progress, true)
                assertEquals(TEST_SIZE, getStoreTestDataSize(user2Config))
                allChangesDownloaded.countDown()
            }
        }
        TestHelper.awaitOrFail(allChangesDownloaded)
        realm.close()
    }
//
//    @Test
//    @Throws(InterruptedException::class)
//    fun downloadProgressListener_indefinitely() {
//        val transferCompleted = AtomicInteger(0)
//        val allChangesDownloaded = CountDownLatch(1)
//        val startWorker = CountDownLatch(1)
//        val userWithData: SyncUser = UserFactory.createUniqueUser(Constants.AUTH_URL)
//        val userWithDataConfig: SyncConfiguration = configFactory.createSyncConfigurationBuilder(userWithData, Constants.USER_REALM)
//                .name("remote")
//                .build()
//        val serverUrl = createRemoteData(userWithDataConfig)
//
//        // Create worker thread that puts data into another Realm.
//        // This is to avoid blocking one progress listener while waiting for another to complete.
//        val worker = Thread(Runnable {
//            TestHelper.awaitOrFail(startWorker)
//            createRemoteData(userWithDataConfig)
//        })
//        worker.start()
//        val adminUser: SyncUser = UserFactory.createAdminUser(Constants.AUTH_URL)
//        val adminConfig: SyncConfiguration = configFactory.createSyncConfigurationBuilder(adminUser, serverUrl.toString())
//                .name("local")
//                .build()
//        val adminRealm = Realm.getInstance(adminConfig)
//        val session: SyncSession = SyncManager.getSession(adminConfig)
//        session.addDownloadProgressListener(ProgressMode.INDEFINITELY) { progress ->
//            val objectCounts = getStoreTestDataSize(adminConfig)
//            // The downloading progress listener could be triggered at the db version where only contains the meta
//            // data. So we start checking from when the first 10 objects downloaded.
//            RealmLog.warn(String.format(
//                    Locale.ENGLISH, "downloadProgressListener_indefinitely download %d/%d objects count:%d",
//                    progress.transferredBytes, progress.transferableBytes, objectCounts))
//            if (objectCounts != 0L && progress.isTransferComplete) {
//                when (transferCompleted.incrementAndGet()) {
//                    1 -> {
//                        Assert.assertEquals(TEST_SIZE, objectCounts)
//                        assertTransferComplete(progress, true)
//                        startWorker.countDown()
//                    }
//                    2 -> {
//                        assertTransferComplete(progress, true)
//                        Assert.assertEquals(TEST_SIZE * 2, objectCounts)
//                        allChangesDownloaded.countDown()
//                    }
//                    else -> Assert.fail("Transfer complete called too many times:" + transferCompleted.get())
//                }
//            }
//        }
//        TestHelper.awaitOrFail(allChangesDownloaded)
//        adminRealm.close()
//        // worker thread will hang if logout happens before listener triggered.
//        worker.join()
//        userWithData.logOut()
//        adminUser.logOut()
//    }
//
//    // Make sure that a ProgressListener continues to report the correct thing, even if it crashed
//    @Test
//    @Throws(InterruptedException::class)
//    fun uploadListener_worksEvenIfCrashed() {
//        val transferCompleted = AtomicInteger(0)
//        val testDone = CountDownLatch(1)
//        val config = createSyncConfig()
//        val realm = Realm.getInstance(config)
//        writeSampleData(realm) // Write first batch of sample data
//        val session: SyncSession = SyncManager.getSession(config)
//        session.addUploadProgressListener(ProgressMode.INDEFINITELY) { progress ->
//            if (progress.isTransferComplete) {
//                when (transferCompleted.incrementAndGet()) {
//                    1 -> {
//                        val realm = Realm.getInstance(config)
//                        writeSampleData(realm)
//                        realm.close()
//                        throw RuntimeException("Crashing the changelistener")
//                    }
//                    2 -> {
//                        assertTransferComplete(progress, true)
//                        testDone.countDown()
//                    }
//                    else -> Assert.fail("Unsupported number of transfers completed: " + transferCompleted.get())
//                }
//            }
//        }
//        TestHelper.awaitOrFail(testDone)
//        realm.close()
//    }

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
//
//    @Test
//    fun addListenerInsideCallback() {
//        val allChangeUploaded = CountDownLatch(1)
//        val config = createSyncConfig()
//        val realm = Realm.getInstance(config)
//        writeSampleData(realm)
//        val session: SyncSession = SyncManager.getSession(config)
//        session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES) { progress ->
//            if (progress.isTransferComplete) {
//                val realm = Realm.getInstance(config)
//                writeSampleData(realm)
//                realm.close()
//                session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES) { progress ->
//                    if (progress.isTransferComplete) {
//                        allChangeUploaded.countDown()
//                    }
//                }
//            }
//        }
//        TestHelper.awaitOrFail(allChangeUploaded)
//        realm.close()
//    }
//
//    @Test
//    fun addListenerInsideCallback_mixProgressModes() {
//        val allChangeUploaded = CountDownLatch(3)
//        val progressCompletedReported = AtomicBoolean(false)
//        val config = createSyncConfig()
//        val realm = Realm.getInstance(config)
//        writeSampleData(realm)
//        val session: SyncSession = SyncManager.getSession(config)
//        session.addUploadProgressListener(ProgressMode.INDEFINITELY) { progress ->
//            if (progress.isTransferComplete) {
//                allChangeUploaded.countDown()
//                if (progressCompletedReported.compareAndSet(false, true)) {
//                    val realm = Realm.getInstance(config)
//                    writeSampleData(realm)
//                    realm.close()
//                    session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES) { progress ->
//                        if (progress.isTransferComplete) {
//                            allChangeUploaded.countDown()
//                        }
//                    }
//                }
//            }
//        }
//        TestHelper.awaitOrFail(allChangeUploaded)
//        realm.close()
//    }
//
//    @Test
//    fun addProgressListener_triggerImmediatelyWhenRegistered() {
//        val config = createSyncConfig()
//        val realm = Realm.getInstance(config)
//        val session: SyncSession = SyncManager.getSession(config)
//        checkListener(session, ProgressMode.INDEFINITELY)
//        checkListener(session, ProgressMode.CURRENT_CHANGES)
//        realm.close()
//    }
//
//    @Test
//    fun uploadListener_keepIncreasingInSize() {
//        val config = createSyncConfig()
//        val realm = Realm.getInstance(config)
//        val session: SyncSession = SyncManager.getSession(config)
//        for (i in 0..9) {
//            val changesUploaded = CountDownLatch(1)
//            writeSampleData(realm)
//            session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES) { progress ->
//                RealmLog.info("Test %s -> %s", Integer.toString(i), progress.toString())
//                if (progress.isTransferComplete) {
//                    assertTransferComplete(progress, true)
//                    changesUploaded.countDown()
//                }
//            }
//            TestHelper.awaitOrFail(changesUploaded)
//        }
//        realm.close()
//    }

    private fun checkListener(session: SyncSession, progressMode: ProgressMode) {
        val listenerCalled = CountDownLatch(1)
        session.addDownloadProgressListener(progressMode) { listenerCalled.countDown() }
        TestHelper.awaitOrFail(listenerCalled)
    }

    private fun writeSampleData(realm: Realm, partitionValue: String = getTestPartitionValue()) {
        realm.beginTransaction()
        for (i in 0 until TEST_SIZE) {
            val obj = SyncColor()
            obj.realmId = partitionValue
            obj.color = "Object $i"
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
        val session: SyncSession = realm.syncSession
        val beforeAdd = realm.where<SyncColor>().count()
        writeSampleData(realm)
        val threadId = Thread.currentThread().id
        session.addUploadProgressListener(ProgressMode.INDEFINITELY, object : ProgressListener {
            override fun onChange(progress: Progress) {
                // FIXME: This check is to make sure before this method returns, all the uploads has been done.
                // See https://github.com/realm/realm-object-store/issues/581#issuecomment-339353832
                if (threadId == Thread.currentThread().id) {
                    return
                }
                if (progress.isTransferComplete) {
                    val realm = Realm.getInstance(config)
                    val afterAdd = realm.where<SyncColor>().count()
                    realm.close()
                    RealmLog.warn(String.format(Locale.ENGLISH, "createRemoteData upload %d/%d objects count:%d",
                            progress.transferredBytes, progress.transferableBytes, afterAdd))
                    // FIXME: Remove this after https://github.com/realm/realm-object-store/issues/581
                    if (afterAdd == TEST_SIZE + beforeAdd) {
                        session.removeProgressListener(this)
                        changesUploaded.countDown()
                    } else if (afterAdd < TEST_SIZE + beforeAdd) {
                        Assert.fail("The added objects are more than expected.")
                    }
                }
            }
        })
        TestHelper.awaitOrFail(changesUploaded)
        realm.close()
    }

    private fun getStoreTestDataSize(config: RealmConfiguration): Long {
        val realm: Realm = Realm.getInstance(config)
        val objectCounts: Long = realm.where<AllTypes>().count()
        realm.close()
        return objectCounts
    }

    private fun createSyncConfig(user: RealmUser = app.login(RealmCredentials.anonymous()), partitionValue: String = getTestPartitionValue()): SyncConfiguration {
        return SyncConfiguration.Builder(user, partitionValue)
                .schema(SyncColor::class.java)
                .build()
    }

    private fun getTestPartitionValue(): String {
        if (!this::partitionValue.isInitialized) {
            fail("Test not setup correctly. Partition value is missing");
        }
        return partitionValue
    }
}