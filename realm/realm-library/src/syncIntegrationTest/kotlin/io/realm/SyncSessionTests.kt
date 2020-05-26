package io.realm

import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.realm.SyncTestUtils
import io.realm.entities.AllTypes
import io.realm.entities.StringOnly
import io.realm.exceptions.DownloadingRealmInterruptedException
import io.realm.internal.OsRealmConfig
import io.realm.objectserver.utils.Constants
import io.realm.objectserver.utils.StringOnlyModule
import io.realm.objectserver.utils.UserFactory
import io.realm.rule.RunTestInLooperThread
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class SyncSessionTests : StandardIntegrationTest() {
    @Rule
    var configFactory = TestSyncConfigurationFactory()

    private interface SessionCallback {
        fun onReady(session: SyncSession?)
    }

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
            val user: SyncUser = UserFactory.createUniqueUser(Constants.AUTH_URL)
            val syncConfiguration = configFactory
                    .createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                    .build()
            looperThread.closeAfterTest(Realm.getInstance(syncConfiguration))
            callback.onReady(SyncManager.getSession(syncConfiguration))
        })
    }

    private fun getActiveSession(callback: SessionCallback) {
        getSession(SessionCallback { session: SyncSession ->
            if (session.isConnected) {
                callback.onReady(session)
            } else {
                session.addConnectionChangeListener(object : ConnectionListener {
                    override fun onChange(oldState: ConnectionState, newState: ConnectionState) {
                        if (newState == ConnectionState.CONNECTED) {
                            session.removeConnectionChangeListener(this)
                            callback.onReady(session)
                        }
                    }
                })
            }
        })
    }

    // make sure the `access_token` is acquired. otherwise we can still be
    // in WAITING_FOR_ACCESS_TOKEN state
    @get:Test(timeout = 3000)
    val state_active: Unit
        get() {
            val user: SyncUser = UserFactory.createUniqueUser(Constants.AUTH_URL)
            val syncConfiguration = configFactory
                    .createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                    .build()
            val realm = Realm.getInstance(syncConfiguration)
            val session: SyncSession = SyncManager.getSession(syncConfiguration)

            // make sure the `access_token` is acquired. otherwise we can still be
            // in WAITING_FOR_ACCESS_TOKEN state
            while (session.state != SyncSession.State.ACTIVE) {
                SystemClock.sleep(200)
            }
            realm.close()
        }

    @get:Test
    val state_throwOnClosedSession: Unit
        get() {
            val user: SyncUser = UserFactory.createUniqueUser(Constants.AUTH_URL)
            val syncConfiguration = configFactory
                    .createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                    .build()
            val realm = Realm.getInstance(syncConfiguration)
            val session: SyncSession = SyncManager.getSession(syncConfiguration)
            realm.close()
            user.logOut()
            thrown.expect(IllegalStateException::class.java)
            thrown.expectMessage("Could not find session, Realm was probably closed")
            session.state
        }

    @get:Test
    val state_loggedOut: Unit
        get() {
            val user: SyncUser = UserFactory.createUniqueUser(Constants.AUTH_URL)
            val syncConfiguration = configFactory
                    .createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                    .build()
            val realm = Realm.getInstance(syncConfiguration)
            val session: SyncSession = SyncManager.getSession(syncConfiguration)
            user.logOut()
            val state = session.state
            Assert.assertEquals(SyncSession.State.INACTIVE, state)
            realm.close()
        }

    @Test
    @Throws(InterruptedException::class)
    fun uploadDownloadAllChanges() {
        val user: SyncUser = UserFactory.createUniqueUser(Constants.AUTH_URL)
        val adminUser: SyncUser = UserFactory.createAdminUser(Constants.AUTH_URL)
        val userConfig = configFactory
                .createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .build()
        val adminConfig = configFactory
                .createSyncConfigurationBuilder(adminUser, userConfig.serverUrl.toString())
                .build()
        val userRealm = Realm.getInstance(userConfig)
        userRealm.beginTransaction()
        userRealm.createObject(AllTypes::class.java)
        userRealm.commitTransaction()
        SyncManager.getSession(userConfig).uploadAllLocalChanges()
        userRealm.close()
        val adminRealm = Realm.getInstance(adminConfig)
        SyncManager.getSession(adminConfig).downloadAllServerChanges()
        adminRealm.refresh()
        Assert.assertEquals(1, adminRealm.where(AllTypes::class.java).count())
        adminRealm.close()
    }

    @Test
    @Throws(InterruptedException::class)
    fun interruptWaits() {
        val user: SyncUser = UserFactory.createUniqueUser(Constants.AUTH_URL)
        val adminUser: SyncUser = UserFactory.createAdminUser(Constants.AUTH_URL)
        val userConfig = configFactory
                .createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .build()
        val adminConfig = configFactory
                .createSyncConfigurationBuilder(adminUser, userConfig.serverUrl.toString())
                .build()
        val t = Thread(Runnable {
            val userRealm = Realm.getInstance(userConfig)
            userRealm.beginTransaction()
            userRealm.createObject(AllTypes::class.java)
            userRealm.commitTransaction()
            val userSession: SyncSession = SyncManager.getSession(userConfig)
            try {
                // 1. Start download (which will be interrupted)
                Thread.currentThread().interrupt()
                userSession.downloadAllServerChanges()
            } catch (ignored: InterruptedException) {
                Assert.assertFalse(Thread.currentThread().isInterrupted)
            }
            try {
                // 2. Upload all changes
                userSession.uploadAllLocalChanges()
            } catch (e: InterruptedException) {
                Assert.fail("Upload interrupted")
            }
            userRealm.close()
            val adminRealm = Realm.getInstance(adminConfig)
            val adminSession: SyncSession = SyncManager.getSession(adminConfig)
            try {
                // 3. Start upload (which will be interrupted)
                Thread.currentThread().interrupt()
                adminSession.uploadAllLocalChanges()
            } catch (ignored: InterruptedException) {
                Assert.assertFalse(Thread.currentThread().isInterrupted) // clear interrupted flag
            }
            try {
                // 4. Download all changes
                adminSession.downloadAllServerChanges()
            } catch (e: InterruptedException) {
                Assert.fail("Download interrupted")
            }
            adminRealm.refresh()
            Assert.assertEquals(1, adminRealm.where(AllTypes::class.java).count())
            adminRealm.close()
        })
        t.start()
        t.join()
    }

    // check that logging out a SyncUser used by different Realm will
    // affect all associated sessions.
    @Test(timeout = 5000)
    fun logout_sameSyncUserMultipleSessions() {
        val uniqueName = UUID.randomUUID().toString()
        var credentials = SyncCredentials.usernamePassword(uniqueName, "password", true)
        val user: SyncUser = SyncUser.logIn(credentials, Constants.AUTH_URL)
        val syncConfiguration1 = configFactory
                .createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .build()
        val realm1 = Realm.getInstance(syncConfiguration1)
        val syncConfiguration2 = configFactory
                .createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL_2)
                .build()
        val realm2 = Realm.getInstance(syncConfiguration2)
        val session1: SyncSession = SyncManager.getSession(syncConfiguration1)
        val session2: SyncSession = SyncManager.getSession(syncConfiguration2)

        // make sure the `access_token` is acquired. otherwise we can still be
        // in WAITING_FOR_ACCESS_TOKEN state
        while (session1.state != SyncSession.State.ACTIVE || session2.state != SyncSession.State.ACTIVE) {
            SystemClock.sleep(200)
        }
        Assert.assertEquals(SyncSession.State.ACTIVE, session1.state)
        Assert.assertEquals(SyncSession.State.ACTIVE, session2.state)
        Assert.assertNotEquals(session1, session2)
        Assert.assertEquals(session1.user, session2.user)
        user.logOut()
        Assert.assertEquals(SyncSession.State.INACTIVE, session1.state)
        Assert.assertEquals(SyncSession.State.INACTIVE, session2.state)
        credentials = SyncCredentials.usernamePassword(uniqueName, "password", false)
        SyncUser.logIn(credentials, Constants.AUTH_URL)

        // reviving the sessions. The state could be changed concurrently.
        Assert.assertTrue(session1.state == SyncSession.State.WAITING_FOR_ACCESS_TOKEN ||
                session1.state == SyncSession.State.ACTIVE)
        Assert.assertTrue(session2.state == SyncSession.State.WAITING_FOR_ACCESS_TOKEN ||
                session2.state == SyncSession.State.ACTIVE)
        realm1.close()
        realm2.close()
    }

    // A Realm that was opened before a user logged out should be able to resume uploading if the user logs back in.
    @Test
    @Throws(InterruptedException::class)
    fun logBackResumeUpload() {
        val uniqueName = UUID.randomUUID().toString()
        val credentials = SyncCredentials.usernamePassword(uniqueName, "password", true)
        val user: SyncUser = SyncUser.logIn(credentials, Constants.AUTH_URL)
        val syncConfiguration = configFactory
                .createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .modules(StringOnlyModule())
                .waitForInitialRemoteData()
                .build()
        val realm = Realm.getInstance(syncConfiguration)
        realm.executeTransaction { realm -> realm.createObject(StringOnly::class.java).chars = "1" }
        val session: SyncSession = SyncManager.getSession(syncConfiguration)
        session.uploadAllLocalChanges()
        user.logOut()

        // add a commit while we're still offline
        realm.executeTransaction { realm -> realm.createObject(StringOnly::class.java).chars = "2" }
        val testCompleted = CountDownLatch(1)
        val handlerThread = HandlerThread("HandlerThread")
        handlerThread.start()
        val looper = handlerThread.looper
        val handler = Handler(looper)
        val allResults = AtomicReference<RealmResults<StringOnly>>() // notifier could be GC'ed before it get a chance to trigger the second commit, so declaring it outside the Runnable
        handler.post { // access the Realm from an different path on the device (using admin user), then monitor
            // when the offline commits get synchronized
            val admin: SyncUser = UserFactory.createAdminUser(Constants.AUTH_URL)
            val credentialsAdmin = SyncCredentials.accessToken(SyncTestUtils.getRefreshToken(admin).value(), "custom-admin-user")
            val adminUser: SyncUser = SyncUser.logIn(credentialsAdmin, Constants.AUTH_URL)
            val adminConfig: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(adminUser, syncConfiguration.serverUrl.toString())
                    .modules(StringOnlyModule())
                    .waitForInitialRemoteData()
                    .build()
            val adminRealm = Realm.getInstance(adminConfig)
            allResults.set(adminRealm.where(StringOnly::class.java).sort(StringOnly.FIELD_CHARS).findAll())
            val realmChangeListener: RealmChangeListener<RealmResults<StringOnly>> = object : RealmChangeListener<RealmResults<StringOnly?>?> {
                override fun onChange(stringOnlies: RealmResults<StringOnly>) {
                    if (stringOnlies.size == 2) {
                        Assert.assertEquals("1", stringOnlies[0]!!.chars)
                        Assert.assertEquals("2", stringOnlies[1]!!.chars)
                        handler.post {

                            // Closing a Realm from inside a listener doesn't seem to remove the
                            // active session reference in Object Store
                            adminRealm.close()
                            testCompleted.countDown()
                            handlerThread.quitSafely()
                        }
                    }
                }
            }
            allResults.get().addChangeListener(realmChangeListener)

            // login again to re-activate the user
            val credentials = SyncCredentials.usernamePassword(uniqueName, "password", false)
            // this login will re-activate the logged out user, and resume all it's pending sessions
            // the OS will trigger bindSessionWithConfig with the new refresh_token, in order to obtain
            // a new access_token.
            SyncUser.logIn(credentials, Constants.AUTH_URL)
        }
        TestHelper.awaitOrFail(testCompleted)
        realm.close()
    }

    // A Realm that was opened before a user logged out should be able to resume uploading if the user logs back in.
    // this test validate the behaviour of SyncSessionStopPolicy::AfterChangesUploaded
    @Test
    @Throws(InterruptedException::class)
    fun uploadChangesWhenRealmOutOfScope() {
        val strongRefs: MutableList<Any> = ArrayList()
        val uniqueName = UUID.randomUUID().toString()
        val credentials = SyncCredentials.usernamePassword(uniqueName, "password", true)
        val user: SyncUser = SyncUser.logIn(credentials, Constants.AUTH_URL)
        val chars = CharArray(1000000) // 2MB
        Arrays.fill(chars, '.')
        val twoMBString = String(chars)
        val syncConfiguration = configFactory
                .createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .sessionStopPolicy(OsRealmConfig.SyncSessionStopPolicy.AFTER_CHANGES_UPLOADED)
                .modules(StringOnlyModule())
                .build()
        val realm = Realm.getInstance(syncConfiguration)
        realm.beginTransaction()
        // upload 10MB
        for (i in 0..4) {
            realm.createObject(StringOnly::class.java).chars = twoMBString
        }
        realm.commitTransaction()
        realm.close()
        val testCompleted = CountDownLatch(1)
        val handlerThread = HandlerThread("HandlerThread")
        handlerThread.start()
        val looper = handlerThread.looper
        val handler = Handler(looper)
        handler.post { // using an admin user to open the Realm on different path on the device to monitor when all the uploads are done
            val admin: SyncUser = UserFactory.createAdminUser(Constants.AUTH_URL)
            val adminConfig: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(admin, syncConfiguration.serverUrl.toString())
                    .modules(StringOnlyModule())
                    .build()
            val adminRealm = Realm.getInstance(adminConfig)
            val all = adminRealm.where(StringOnly::class.java).findAll()
            if (all.size == 5) {
                adminRealm.close()
                testCompleted.countDown()
                handlerThread.quit()
            } else {
                strongRefs.add(all)
                val realmChangeListener = OrderedRealmCollectionChangeListener { results: RealmResults<StringOnly?>, changeSet: OrderedCollectionChangeSet? ->
                    if (results.size == 5) {
                        adminRealm.close()
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
    }

    // A Realm that was opened before a user logged out should be able to resume downloading if the user logs back in.
    @Test
    @Throws(InterruptedException::class)
    fun downloadChangesWhenRealmOutOfScope() {
        val uniqueName = UUID.randomUUID().toString()
        var credentials = SyncCredentials.usernamePassword(uniqueName, "password", true)
        val user: SyncUser = SyncUser.logIn(credentials, Constants.AUTH_URL)
        val syncConfiguration = configFactory
                .createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .modules(StringOnlyModule())
                .build()
        val realm = Realm.getInstance(syncConfiguration)
        realm.beginTransaction()
        realm.createObject(StringOnly::class.java).chars = "1"
        realm.commitTransaction()
        val session: SyncSession = SyncManager.getSession(syncConfiguration)
        session.uploadAllLocalChanges()

        // Log out the user.
        user.logOut()

        // Log the user back in.
        credentials = SyncCredentials.usernamePassword(uniqueName, "password", false)
        SyncUser.logIn(credentials, Constants.AUTH_URL)

        // now let the admin upload some commits
        val backgroundUpload = CountDownLatch(1)
        val handlerThread = HandlerThread("HandlerThread")
        handlerThread.start()
        val looper = handlerThread.looper
        val handler = Handler(looper)
        handler.post { // using an admin user to open the Realm on different path on the device then some commits
            val admin: SyncUser = UserFactory.createAdminUser(Constants.AUTH_URL)
            val credentialsAdmin = SyncCredentials.accessToken(SyncTestUtils.getRefreshToken(admin).value(), "custom-admin-user")
            val adminUser: SyncUser = SyncUser.logIn(credentialsAdmin, Constants.AUTH_URL)
            val adminConfig: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(adminUser, syncConfiguration.serverUrl.toString())
                    .modules(StringOnlyModule())
                    .waitForInitialRemoteData()
                    .build()
            val adminRealm = Realm.getInstance(adminConfig)
            adminRealm.beginTransaction()
            adminRealm.createObject(StringOnly::class.java).chars = "2"
            adminRealm.createObject(StringOnly::class.java).chars = "3"
            adminRealm.commitTransaction()
            try {
                SyncManager.getSession(adminConfig).uploadAllLocalChanges()
            } catch (e: InterruptedException) {
                e.printStackTrace()
                Assert.fail(e.message)
            }
            adminRealm.close()
            backgroundUpload.countDown()
            handlerThread.quit()
        }
        TestHelper.awaitOrFail(backgroundUpload, 60)
        // Resume downloading
        session.downloadAllServerChanges()
        realm.refresh() //FIXME not calling refresh will still point to the previous version of the Realm count == 1
        Assert.assertEquals(3, realm.where(StringOnly::class.java).count())
        realm.close()
    }

    // Check that if we manually trigger a Client Reset, then it should be possible to start
    // downloading the Realm immediately after.
    @Test
    @RunTestInLooperThread
    fun clientReset_manualTriggerAllowSessionToRestart() {
        val uniqueName = UUID.randomUUID().toString()
        val credentials = SyncCredentials.usernamePassword(uniqueName, "password", true)
        val user: SyncUser = SyncUser.logIn(credentials, Constants.AUTH_URL)
        val configRef = AtomicReference<SyncConfiguration?>(null)
        val config: SyncConfiguration = configFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .clientResyncMode(ClientResyncMode.MANUAL)
                .directory(looperThread.getRoot())
                .errorHandler(SyncSession.ErrorHandler { session, error ->
                    val handler = error as ClientResetRequiredError
                    // Execute Client Reset
                    looperThread.closeTestRealms()
                    handler.executeClientReset()

                    // Try to re-open Realm and download it again
                    looperThread.postRunnable(Runnable { // Validate that files have been moved
                        Assert.assertFalse(handler.originalFile.exists())
                        Assert.assertTrue(handler.backupFile.exists())
                        val config = configRef.get()
                        val instance = Realm.getInstance(config!!)
                        looperThread.addTestRealm(instance)
                        try {
                            SyncManager.getSession(config).downloadAllServerChanges()
                            looperThread.testComplete()
                        } catch (e: InterruptedException) {
                            Assert.fail(e.toString())
                        }
                    })
                })
                .build()
        configRef.set(config)
        val realm = Realm.getInstance(config)
        looperThread.addTestRealm(realm)
        // Trigger error
        SyncManager.simulateClientReset(SyncManager.getSession(config))
    }

    @Test
    @RunTestInLooperThread
    fun registerConnectionListener() {
        getSession(SessionCallback { session: SyncSession ->
            session.addConnectionChangeListener { oldState: ConnectionState?, newState: ConnectionState ->
                if (newState == ConnectionState.DISCONNECTED) {
                    // Closing a Realm inside a connection listener doesn't work: https://github.com/realm/realm-java/issues/6249
                    looperThread.postRunnable({ looperThread.testComplete() })
                }
            }
            session.stop()
        })
    }

    @Test
    @RunTestInLooperThread
    fun removeConnectionListener() {
        val user: SyncUser = UserFactory.createUniqueUser(Constants.AUTH_URL)
        val syncConfiguration = configFactory
                .createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .build()
        val realm = Realm.getInstance(syncConfiguration)
        val session: SyncSession = SyncManager.getSession(syncConfiguration)
        val listener1 = ConnectionListener { oldState: ConnectionState?, newState: ConnectionState ->
            if (newState == ConnectionState.DISCONNECTED) {
                Assert.fail("Listener should have been removed")
            }
        }
        val listener2 = ConnectionListener { oldState: ConnectionState?, newState: ConnectionState ->
            if (newState == ConnectionState.DISCONNECTED) {
                looperThread.testComplete()
            }
        }
        session.addConnectionChangeListener(listener1)
        session.addConnectionChangeListener(listener2)
        session.removeConnectionChangeListener(listener1)
        realm.close()
    }

    @get:RunTestInLooperThread
    @get:Test
    val isConnected: Unit
        get() {
            getActiveSession(SessionCallback { session: SyncSession ->
                Assert.assertEquals(session.connectionState, ConnectionState.CONNECTED)
                Assert.assertTrue(session.isConnected)
                looperThread.testComplete()
            })
        }

    @Test
    @RunTestInLooperThread
    fun stopStartSession() {
        getActiveSession(SessionCallback { session: SyncSession ->
            Assert.assertEquals(SyncSession.State.ACTIVE, session.state)
            session.stop()
            Assert.assertEquals(SyncSession.State.INACTIVE, session.state)
            session.start()
            Assert.assertNotEquals(SyncSession.State.INACTIVE, session.state)
            looperThread.testComplete()
        })
    }

    @Test
    @RunTestInLooperThread
    fun start_multipleTimes() {
        getActiveSession(SessionCallback { session: SyncSession ->
            session.start()
            Assert.assertEquals(SyncSession.State.ACTIVE, session.state)
            session.start()
            Assert.assertEquals(SyncSession.State.ACTIVE, session.state)
            looperThread.testComplete()
        })
    }

    @Test
    @RunTestInLooperThread
    fun stop_multipleTimes() {
        getSession(SessionCallback { session: SyncSession ->
            session.stop()
            Assert.assertEquals(SyncSession.State.INACTIVE, session.state)
            session.stop()
            Assert.assertEquals(SyncSession.State.INACTIVE, session.state)
            looperThread.testComplete()
        })
    }

    @Test
    @RunTestInLooperThread
    fun waitForInitialRemoteData_throwsOnTimeout() {
        val user: SyncUser = UserFactory.createUniqueUser(Constants.AUTH_URL)
        val syncConfiguration = configFactory
                .createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .initialData { bgRealm: Realm ->
                    for (i in 0..99) {
                        bgRealm.createObject(AllTypes::class.java)
                    }
                }
                .waitForInitialRemoteData(1, TimeUnit.MILLISECONDS)
                .build()
        try {
            Realm.getInstance(syncConfiguration)
            Assert.fail("This should have timed out")
        } catch (ignore: DownloadingRealmInterruptedException) {
        }
        looperThread.testComplete()
    }
}
