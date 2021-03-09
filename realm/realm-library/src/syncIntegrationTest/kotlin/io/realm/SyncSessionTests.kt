package io.realm

import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.admin.ServerAdmin
import io.realm.entities.*
import io.realm.exceptions.DownloadingRealmInterruptedException
import io.realm.internal.OsRealmConfig
import io.realm.kotlin.syncSession
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.mongodb.*
import io.realm.mongodb.sync.*
import io.realm.rule.BlockingLooperThread
import io.realm.util.ResourceContainer
import io.realm.util.assertFailsWithErrorCode
import io.realm.util.assertFailsWithMessage
import org.bson.BsonInt32
import org.bson.BsonInt64
import org.bson.BsonString
import org.bson.types.ObjectId
import org.hamcrest.CoreMatchers
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.Closeable
import java.lang.Thread
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

typealias SessionCallback = (SyncSession) -> Unit

private val SECRET_PASSWORD = "123456"

@RunWith(AndroidJUnit4::class)
class SyncSessionTests {

    @get:Rule
    private val looperThread = BlockingLooperThread()

    private lateinit var app: App
    private lateinit var user: User
    private lateinit var syncConfiguration: SyncConfiguration
    private lateinit var admin: ServerAdmin

    private val configFactory: TestSyncConfigurationFactory = TestSyncConfigurationFactory()

    private fun getSession(callback: SessionCallback) {
        // Work-around for a race condition happening when shutting down a Looper test and
        // Resetting the SyncManager
        // The problem is the `@After` block which runs as soon as the test method has completed.
        // For integration tests this will attempt to reset the SyncManager which will fail
        // if Realms are still open as they hold a reference to a session object.
        // By moving this into a Looper callback we ensure that a looper test can shutdown as
        // intended.
        // Generally it seems that using calling `RunInLooperThread.testComplete()` in a synchronous
        looperThread.postRunnable(Runnable {
            val user = app.registerUserAndLogin(TestHelper.getRandomEmail(), SECRET_PASSWORD)
            val syncConfiguration = configFactory
                    .createSyncConfigurationBuilder(user)
                    .testSchema(SyncStringOnly::class.java)
                    .build()
            val realm = Realm.getInstance(syncConfiguration)
            looperThread.closeAfterTest(realm)
            callback(realm.syncSession)
        })
    }

    private fun getActiveSession(callback: SessionCallback) {
        getSession { session ->
            if (session.isConnected) {
                callback(session)
            } else {
                session.addConnectionChangeListener(object : ConnectionListener {
                    override fun onChange(oldState: ConnectionState, newState: ConnectionState) {
                        if (newState == ConnectionState.CONNECTED) {
                            session.removeConnectionChangeListener(this)
                            callback(session)
                        }
                    }
                })
            }
        }
    }

    @Before
    fun setup() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        RealmLog.setLevel(LogLevel.ALL)

        app = TestApp()
        admin = ServerAdmin(app)
        user = app.registerUserAndLogin(TestHelper.getRandomEmail(), SECRET_PASSWORD)
        syncConfiguration = configFactory
                // TODO We generate new partition value for each test to avoid overlaps in data. We
                //  could make test booting with a cleaner state by somehow flushing data between
                //  tests.
                .createSyncConfigurationBuilder(user, BsonString(UUID.randomUUID().toString()))
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
    fun partitionValue_string() {
        val partitionValue = "123464652"
        val syncConfiguration = configFactory
                .createSyncConfigurationBuilder(user, BsonString(partitionValue))
                .modules(DefaultSyncSchema())
                .build()
        Realm.getInstance(syncConfiguration).use { realm ->
            realm.executeTransaction {
                realm.createObject(SyncDog::class.java, ObjectId())
            }
            realm.syncSession.uploadAllLocalChanges()
        }
    }

    @Test
    fun partitionValue_int32_failsWhenServerConfiguredWithStringPartition() {
        val int = 123536462
        val syncConfiguration = configFactory
                .createSyncConfigurationBuilder(user, BsonInt32(int))
                .modules(DefaultSyncSchema())
                .build()

        looperThread.runBlocking {
            Realm.getInstance(syncConfiguration).use { realm ->
                realm.executeTransaction {
                    realm.createObject(SyncDog::class.java, ObjectId())
                }

                assertFailsWith<AppException> {
                    // This throws as the server has NOT been configured to have int partitions!
                    realm.syncSession.uploadAllLocalChanges()
                }.also {
                    looperThread.testComplete()
                }
            }
        }
    }

    @Test
    fun partitionValue_int64_failsWhenServerConfiguredWithStringPartition() {
        val long = 1243513244L
        val syncConfiguration = configFactory
                .createSyncConfigurationBuilder(user, BsonInt64(long))
                .modules(DefaultSyncSchema())
                .build()

        looperThread.runBlocking {
            Realm.getInstance(syncConfiguration).use { realm ->
                realm.executeTransaction {
                    realm.createObject(SyncDog::class.java, ObjectId())
                }

                assertFailsWith<AppException> {
                    // This throws as the server has NOT been configured to have long partitions!
                    realm.syncSession.uploadAllLocalChanges()
                }.also {
                    looperThread.testComplete()
                }
            }
        }
    }

    @Test
    fun partitionValue_objectId() {
        val syncConfiguration = configFactory
                .createSyncConfigurationBuilder(user, BsonString(UUID.randomUUID().toString()))
                .modules(DefaultSyncSchema())
                .build()
        Realm.getInstance(syncConfiguration).use { realm ->
            realm.executeTransaction {
                realm.createObject(SyncDog::class.java, ObjectId())
            }
            realm.syncSession.uploadAllLocalChanges()
        }
    }

    @Test(timeout = 3000)
    fun getState_active() {
        Realm.getInstance(syncConfiguration).use { realm ->
            val session: SyncSession = realm.syncSession

            // make sure the `access_token` is acquired. otherwise we can still be
            // in WAITING_FOR_ACCESS_TOKEN state
            while (session.state != SyncSession.State.ACTIVE) {
                SystemClock.sleep(200)
            }
        }
    }

    @Test
    fun getState_throwOnClosedSession() {
        var session: SyncSession? = null
        Realm.getInstance(syncConfiguration).use { realm ->
            session = realm.syncSession
        }
        user.logOut()

        assertFailsWithMessage<java.lang.IllegalStateException>(CoreMatchers.equalTo("Could not find session, Realm was probably closed")) {
            session!!.state
        }
    }

    @Test
    fun getState_loggedOut() {
        Realm.getInstance(syncConfiguration).use { realm ->
            val session = realm.syncSession
            user.logOut()
            assertEquals(SyncSession.State.INACTIVE, session.state)
        }
    }

    @Test
    fun session_throwOnLogoutUser() {
        user.logOut()
        assertFailsWith<IllegalStateException> {
            Realm.getInstance(syncConfiguration).use { }
        }
    }

    @Test
    fun uploadDownloadAllChanges() {
        Realm.getInstance(syncConfiguration).use { realm ->
            realm.executeTransaction {
                realm.createObject(SyncAllTypes::class.java, ObjectId())
            }
            realm.syncSession.uploadAllLocalChanges()
        }

        // New user but same Realm as configuration has the same partition value
        val user2 = app.registerUserAndLogin(TestHelper.getRandomEmail(), SECRET_PASSWORD)
        val config2 = configFactory
                .createSyncConfigurationBuilder(user2, syncConfiguration.partitionValue)
                .modules(DefaultSyncSchema())
                .build()

        Realm.getInstance(config2).use { realm ->
            realm.syncSession.downloadAllServerChanges()
            realm.refresh()
            assertEquals(1, realm.where(SyncAllTypes::class.java).count())
        }
    }

    // TODO This test is only for tracking failure when uploading SyncAllTypes including float field.
    //  Once this test fails (meaning that the full schema can be uploaded) the test can be removed
    //  and we can include the float field in SyncAllTypes
    @Test
    fun uploadDownloadAllChangesWithFloatFails() {
        val config = configFactory
                .createSyncConfigurationBuilder(user, syncConfiguration.partitionValue)
                .testSchema(SyncAllTypesWithFloat::class.java, SyncDog::class.java, SyncPerson::class.java)
                .build()

        Realm.getInstance(config).use { realm ->
            realm.executeTransaction {
                realm.createObject(SyncAllTypesWithFloat::class.java, ObjectId())
            }
            assertFailsWithErrorCode(ErrorCode.INVALID_SCHEMA_CHANGE) {
                realm.syncSession.uploadAllLocalChanges()
            }
        }
    }

    @Test
    fun differentPartitionValue_allTypes() {
        Realm.getInstance(syncConfiguration).use { realm ->
            realm.executeTransaction {
                realm.createObject(SyncAllTypes::class.java, ObjectId())
            }
            realm.syncSession.uploadAllLocalChanges()
        }

        // New user and different partition value
        val user2 = app.registerUserAndLogin(TestHelper.getRandomEmail(), SECRET_PASSWORD)
        val config2 = configFactory
                .createSyncConfigurationBuilder(user2, BsonString(UUID.randomUUID().toString()))
                .modules(DefaultSyncSchema())
                .build()

        Realm.getInstance(config2).use { realm ->
            realm.executeTransaction {
                realm.createObject(SyncAllTypes::class.java, ObjectId())
            }
            realm.syncSession.uploadAllLocalChanges()
        }
    }

    @Test
    fun differentPartitionValue_noCrosstalk() {
        Realm.getInstance(syncConfiguration).use { realm ->
            realm.executeTransaction {
                realm.createObject(SyncAllTypes::class.java, ObjectId())
            }
            realm.syncSession.uploadAllLocalChanges()
        }

        // New user and different partition value
        val user2 = app.registerUserAndLogin(TestHelper.getRandomEmail(), SECRET_PASSWORD)
        val config2 = configFactory
                .createSyncConfigurationBuilder(user2, BsonString(UUID.randomUUID().toString()))
                .modules(DefaultSyncSchema())
                .build()

        Realm.getInstance(config2).use { realm ->
            realm.syncSession.downloadAllServerChanges()
            // We should not have any data here
            assertEquals(0, realm.where(SyncAllTypes::class.java).count())
        }
    }

    @Test
    fun interruptWaits() {
        Realm.getInstance(syncConfiguration).use { userRealm ->
            userRealm.executeTransaction {
                userRealm.createObject(SyncAllTypes::class.java, ObjectId())
            }
            val userSession = userRealm.syncSession
            try {
                // 1. Start download (which will be interrupted)
                Thread.currentThread().interrupt()
                userSession.downloadAllServerChanges()
                fail()
            } catch (ignored: InterruptedException) {
                assertFalse(Thread.currentThread().isInterrupted)
            }
            try {
                // 2. Upload all changes
                userSession.uploadAllLocalChanges()
            } catch (e: InterruptedException) {
                fail("Upload interrupted")
            }
        }

        // New user but same Realm as configuration has the same partition value
        val user2 = app.registerUserAndLogin(TestHelper.getRandomEmail(), SECRET_PASSWORD)
        val config2 = configFactory
                .createSyncConfigurationBuilder(user2, syncConfiguration.partitionValue)
                .modules(DefaultSyncSchema())
                .build()

        Realm.getInstance(config2).use { adminRealm ->
            val adminSession: SyncSession = adminRealm.syncSession
            try {
                // 3. Start upload (which will be interrupted)
                Thread.currentThread().interrupt()
                adminSession.uploadAllLocalChanges()
                fail()
            } catch (ignored: InterruptedException) {
                assertFalse(Thread.currentThread().isInterrupted) // clear interrupted flag
            }
            try {
                // 4. Download all changes
                adminSession.downloadAllServerChanges()
            } catch (e: InterruptedException) {
                fail("Download interrupted")
            }
            adminRealm.refresh()

            assertEquals(1, adminRealm.where(SyncAllTypes::class.java).count())
        }
    }

    // check that logging out a SyncUser used by different Realm will
    // affect all associated sessions.
    @Test(timeout = 5000)
    fun logout_sameSyncUserMultipleSessions() {
        Realm.getInstance(syncConfiguration).use { realm1 ->
            // New partitionValue to differentiate sync session
            val syncConfiguration2 = configFactory
                    .createSyncConfigurationBuilder(user, BsonString(UUID.randomUUID().toString()))
                    .modules(DefaultSyncSchema())
                    .build()

            Realm.getInstance(syncConfiguration2).use { realm2 ->
                val session1: SyncSession = realm1.syncSession
                val session2: SyncSession = realm2.syncSession

                // make sure the `access_token` is acquired. otherwise we can still be
                // in WAITING_FOR_ACCESS_TOKEN state
                // FIXME Reavaluate with new sync states
                while (session1.state != SyncSession.State.ACTIVE || session2.state != SyncSession.State.ACTIVE) {
                    SystemClock.sleep(200)
                }

                assertEquals(SyncSession.State.ACTIVE, session1.state)
                assertEquals(SyncSession.State.ACTIVE, session2.state)
                assertNotEquals(realm1, realm2)
                assertNotEquals(session1, session2)
                assertEquals(session1.user, session2.user)
                user.logOut()
                assertEquals(SyncSession.State.INACTIVE, session1.state)
                assertEquals(SyncSession.State.INACTIVE, session2.state)

                // Login again
                app.login(Credentials.emailPassword(user.profile.email!!, SECRET_PASSWORD))

                // reviving the sessions. The state could be changed concurrently.
                assertTrue(
                        //session1.state == SyncSession.State.WAITING_FOR_ACCESS_TOKEN ||
                        session1.state == SyncSession.State.ACTIVE)
                assertTrue(
                        //session2.state == SyncSession.State.WAITING_FOR_ACCESS_TOKEN ||
                        session2.state == SyncSession.State.ACTIVE)
            }
        }
    }

    // A Realm that was opened before a user logged out should be able to resume uploading if the user logs back in.
    @Test
    fun logBackResumeUpload() {
        val config1 = configFactory
                .createSyncConfigurationBuilder(user, UUID.randomUUID().toString())
                .modules(SyncStringOnlyModule())
                .waitForInitialRemoteData()
                .build()
        Realm.getInstance(config1).use { realm1 ->
            realm1.executeTransaction { realm -> realm.createObject(SyncStringOnly::class.java, ObjectId()).chars = "1" }
            val session1: SyncSession = realm1.syncSession
            session1.uploadAllLocalChanges()
            user.logOut()

            // add a commit while we're still offline
            realm1.executeTransaction { realm -> realm.createObject(SyncStringOnly::class.java, ObjectId()).chars = "2" }
            val testCompleted = CountDownLatch(1)
            val handlerThread = HandlerThread("HandlerThread")
            handlerThread.start()
            val looper = handlerThread.looper
            val handler = Handler(looper)
            val allResults = AtomicReference<RealmResults<SyncStringOnly>>() // notifier could be GC'ed before it get a chance to trigger the second commit, so declaring it outside the Runnable
            handler.post { // access the Realm from an different path on the device (using admin user), then monitor
                // when the offline commits get synchronized
                val user2 = app.registerUserAndLogin(TestHelper.getRandomEmail(), SECRET_PASSWORD)
                val config2: SyncConfiguration = configFactory.createSyncConfigurationBuilder(user2, config1.partitionValue)
                        .modules(SyncStringOnlyModule())
                        .build()
                val realm2 = Realm.getInstance(config2)

                allResults.set(realm2.where(SyncStringOnly::class.java).sort(SyncStringOnly.FIELD_CHARS).findAll())
                val realmChangeListener: RealmChangeListener<RealmResults<SyncStringOnly>> = object : RealmChangeListener<RealmResults<SyncStringOnly>> {
                    override fun onChange(stringOnlies: RealmResults<SyncStringOnly>) {
                        if (stringOnlies.size == 2) {
                            assertEquals("1", stringOnlies[0]!!.chars)
                            assertEquals("2", stringOnlies[1]!!.chars)
                            handler.post {

                                // Closing a Realm from inside a listener doesn't seem to remove the
                                // active session reference in Object Store
                                realm2.close()
                                testCompleted.countDown()
                                handlerThread.quitSafely()
                            }
                        }
                    }
                }
                allResults.get().addChangeListener(realmChangeListener)

                // login again to re-activate the user
                val credentials = Credentials.emailPassword(user.profile.email!!, SECRET_PASSWORD)
                // this login will re-activate the logged out user, and resume all it's pending sessions
                // the OS will trigger bindSessionWithConfig with the new refresh_token, in order to obtain
                // a new access_token.
                app.login(credentials)
            }
            TestHelper.awaitOrFail(testCompleted)
        }
    }

    // A Realm that was opened before a user logged out should be able to resume uploading if the user logs back in.
    // this test validate the behaviour of SyncSessionStopPolicy::AfterChangesUploaded
    @Test
    fun uploadChangesWhenRealmOutOfScope() = looperThread.runBlocking {
        val strongRefs: MutableList<Any> = ArrayList()
        val chars = CharArray(1000000) // 2MB
        Arrays.fill(chars, '.')
        val twoMBString = String(chars)
        val config1 = configFactory
                .createSyncConfigurationBuilder(user, UUID.randomUUID().toString())
                .testSessionStopPolicy(OsRealmConfig.SyncSessionStopPolicy.AFTER_CHANGES_UPLOADED)
                .modules(SyncStringOnlyModule())
                .build()
        Realm.getInstance(config1).use { realm ->
            realm.executeTransaction {
                // upload 10MB
                for (i in 0..4) {
                    realm.createObject(SyncStringOnly::class.java, ObjectId()).chars = twoMBString
                }
            }
        }

        val testCompleted = CountDownLatch(1)
        val handlerThread = HandlerThread("HandlerThread")
        handlerThread.start()
        val looper = handlerThread.looper
        val handler = Handler(looper)
        handler.post { // using an other user to open the Realm on different path on the device to monitor when all the uploads are done
            val user2 = app.registerUserAndLogin(TestHelper.getRandomEmail(), SECRET_PASSWORD)
            val config2: SyncConfiguration = configFactory.createSyncConfigurationBuilder(user2, config1.partitionValue)
                    .modules(SyncStringOnlyModule())
                    .build()
            val realm2 = Realm.getInstance(config2)
            val all = realm2.where(SyncStringOnly::class.java).findAll()
            if (all.size == 5) {
                realm2.close()
                testCompleted.countDown()
                handlerThread.quit()
            } else {
                strongRefs.add(all)
                val realmChangeListener = OrderedRealmCollectionChangeListener { results: RealmResults<SyncStringOnly?>, changeSet: OrderedCollectionChangeSet? ->
                    if (results.size == 5) {
                        realm2.close()
                        testCompleted.countDown()
                        handlerThread.quit()
                    }
                }
                all.addChangeListener(realmChangeListener)
            }
        }
        TestHelper.awaitOrFail(testCompleted, TestHelper.STANDARD_WAIT_SECS)
        handlerThread.join()
        user.logOut()
        looperThread.testComplete()
    }

    // A Realm that was opened before a user logged out should be able to resume downloading if the user logs back in.
    @Test
    fun downloadChangesWhenRealmOutOfScope() {
        val uniqueName = UUID.randomUUID().toString()
        app.emailPassword.registerUser(uniqueName, "password")
        val config1 = configFactory
                .createSyncConfigurationBuilder(user, UUID.randomUUID().toString())
                .modules(SyncStringOnlyModule())
                .build()
        Realm.getInstance(config1).use { realm ->
            realm.executeTransaction {
                realm.createObject(SyncStringOnly::class.java, ObjectId()).chars = "1"
            }
            val session: SyncSession = realm.syncSession
            session.uploadAllLocalChanges()

            // Log out the user.
            user.logOut()

            // Log the user back in.
            val credentials = Credentials.emailPassword(user.profile.email!!, SECRET_PASSWORD)
            app.login(credentials)

            // Write updates from a different user
            val backgroundUpload = CountDownLatch(1)
            val handlerThread = HandlerThread("HandlerThread")
            handlerThread.start()
            val looper = handlerThread.looper
            val handler = Handler(looper)
            handler.post { // Using a different user to open the Realm on different path on the device then some commits
                val user2 = app.registerUserAndLogin(TestHelper.getRandomEmail(), SECRET_PASSWORD)
                val config2: SyncConfiguration = configFactory.createSyncConfigurationBuilder(user2, config1.partitionValue)
                        .modules(SyncStringOnlyModule())
                        .waitForInitialRemoteData()
                        .build()
                Realm.getInstance(config2).use { realm2 ->
                    realm2.executeTransaction {
                        realm2.createObject(SyncStringOnly::class.java, ObjectId()).chars = "2"
                        realm2.createObject(SyncStringOnly::class.java, ObjectId()).chars = "3"
                    }
                    realm2.syncSession.uploadAllLocalChanges()
                }
                backgroundUpload.countDown()
                handlerThread.quit()
            }
            TestHelper.awaitOrFail(backgroundUpload, 60)
            // Resume downloading
            session.downloadAllServerChanges()
            realm.refresh() //FIXME not calling refresh will still point to the previous version of the Realm count == 1
            assertEquals(3, realm.where(SyncStringOnly::class.java).count())
        }
    }

    // Check that if we manually trigger a Client Reset, then it should be possible to start
    // downloading the Realm immediately after.
    @Test
    // TODO Seems to align with tests in SessionTests, should we move them to same location
    fun clientReset_manualTriggerAllowSessionToRestart() = looperThread.runBlocking {
        val resources = ResourceContainer()

        val configRef = AtomicReference<SyncConfiguration?>(null)
        val config: SyncConfiguration = configFactory.createSyncConfigurationBuilder(user)
                .testSchema(SyncStringOnly::class.java)
                // ClientResyncMode is currently hidden, but MANUAL is the default
                // .clientResyncMode(ClientResyncMode.MANUAL)
                // FIXME Is this critical for the test
                //.directory(looperThread.getRoot())
                .clientResetHandler { session, error ->
                    // Execute Client Reset
                    resources.close()
                    error.executeClientReset()

                    // Try to re-open Realm and download it again
                    looperThread.postRunnable(Runnable { // Validate that files have been moved
                        assertFalse(error.originalFile.exists())
                        assertTrue(error.backupFile.exists())
                        val config = configRef.get()
                        Realm.getInstance(config!!).use { realm ->
                            realm.syncSession.downloadAllServerChanges()
                            looperThread.testComplete()
                        }
                    })
                }
                .build()
        configRef.set(config)
        val realm = Realm.getInstance(config)
        resources.add(realm)
        // Trigger error
        user.app.sync.simulateClientReset(realm.syncSession)
    }

    @Test
    fun registerConnectionListener() = looperThread.runBlocking {
        getSession { session: SyncSession ->
            session.addConnectionChangeListener { oldState: ConnectionState?, newState: ConnectionState ->
                if (newState == ConnectionState.DISCONNECTED) {
                    // Closing a Realm inside a connection listener doesn't work: https://github.com/realm/realm-java/issues/6249
                    looperThread.postRunnable(Runnable { looperThread.testComplete() })
                }
            }
            session.stop()
        }
    }

    @Test
    fun removeConnectionListener() = looperThread.runBlocking {
        Realm.getInstance(syncConfiguration).use { realm ->
            val session: SyncSession = realm.syncSession
            val listener1 = ConnectionListener { oldState: ConnectionState?, newState: ConnectionState ->
                if (newState == ConnectionState.DISCONNECTED) {
                    fail("Listener should have been removed")
                }
            }
            val listener2 = ConnectionListener { oldState, newState ->
                if (newState == ConnectionState.DISCONNECTED) {
                    looperThread.testComplete()
                }
            }
            session.addConnectionChangeListener(listener1)
            session.addConnectionChangeListener(listener2)
            session.removeConnectionChangeListener(listener1)
        }
    }

    @Test
    fun getIsConnected() = looperThread.runBlocking {
        getActiveSession { session: SyncSession ->
            assertEquals(session.connectionState, ConnectionState.CONNECTED)
            assertTrue(session.isConnected)
            looperThread.testComplete()
        }
    }

    @Test
    fun stopStartSession() = looperThread.runBlocking {
        getActiveSession { session: SyncSession ->
            assertEquals(SyncSession.State.ACTIVE, session.state)
            session.stop()
            assertEquals(SyncSession.State.INACTIVE, session.state)
            session.start()
            assertNotEquals(SyncSession.State.INACTIVE, session.state)
            looperThread.testComplete()
        }
    }

    @Test
    fun start_multipleTimes() = looperThread.runBlocking {
        getActiveSession { session ->
            session.start()
            assertEquals(SyncSession.State.ACTIVE, session.state)
            session.start()
            assertEquals(SyncSession.State.ACTIVE, session.state)
            looperThread.testComplete()
        }
    }

    @Test
    fun stop_multipleTimes() = looperThread.runBlocking {
        getActiveSession { session ->
            session.stop()
            assertEquals(SyncSession.State.INACTIVE, session.state)
            session.stop()
            assertEquals(SyncSession.State.INACTIVE, session.state)
            looperThread.testComplete()
        }
    }

    @Test
    // FIXME Investigate
    @Ignore("Asserts with no_session when tearing down, meaning that all session are not " +
            "closed, but realm seems to be closed, so further investigation is needed " +
            "seems to be caused by https://github.com/realm/realm-java/issues/5416")
    fun waitForInitialRemoteData_throwsOnTimeout() = looperThread.runBlocking {
        val syncConfiguration = configFactory
                .createSyncConfigurationBuilder(user)
                .modules(DefaultSyncSchema())
                .initialData { bgRealm: Realm ->
                    for (i in 0..99) {
                        bgRealm.createObject(SyncAllTypes::class.java, ObjectId())
                    }
                }
                .waitForInitialRemoteData(1, TimeUnit.MILLISECONDS)
                .build()
        assertFailsWith<DownloadingRealmInterruptedException> {
            val instance = Realm.getInstance(syncConfiguration)
            looperThread.closeAfterTest(Closeable {
                instance.syncSession.testClose()
                instance.close()
            })
        }
        looperThread.testComplete()
    }

    @Test
    fun cachedInstanceShouldNotThrowIfUserTokenIsInvalid() {
        val configuration: RealmConfiguration = configFactory.createSyncConfigurationBuilder(user)
                .testSchema(SyncStringOnly::class.java)
                .errorHandler { session, error ->
                    RealmLog.debug("error", error)
                }
                .build()

        Realm.getInstance(configuration).close()

        admin.disableUser(user)

        // It should be possible to open a cached Realm with expired token
        Realm.getInstance(configuration).close()

        // It should also be possible to open a Realm with an expired token from a different thread
        looperThread.runBlocking {
            val instance = Realm.getInstance(configuration)
            instance.close()
            looperThread.testComplete()
        }

        // TODO We cannot currently easily verify that token is actually invalid and triggering
        //  refresh. If OS includes support for reacting on this we should verify that it is
        //  refreshed.
        //Realm.getInstance(configuration).use { realm ->
        //     realm.syncSession.downloadAllServerChanges()
        //}
    }

}
