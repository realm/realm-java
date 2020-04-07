package io.realm

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.rule.BlockingLooperThread
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class KotlinSyncedRealmTests { // FIXME: Rename to SyncedRealmTests once remaining Java tests have been moved

    private val looperThread = BlockingLooperThread()
    private lateinit var app: TestRealmApp

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        RealmLog.setLevel(LogLevel.TRACE)
        app = TestRealmApp()
    }

    @After
    fun tearDown() {
        app.close()
        RealmLog.setLevel(LogLevel.WARN)
    }

    // Smoke test for sync. Waiting for proper
    @Test
    fun syncRoundTrip() {
        val user: RealmUser = app.login(RealmCredentials.anonymous())
        val realm: Realm = Realm.getInstance(SyncConfiguration.defaultConfig(user))

        app.syncManager.getAllSyncSessions(user)[0].downloadAllServerChanges()

        assertTrue(realm.isEmpty)
        // How to verify it has been uploaded?
    }

    @Test
    fun session() {
        val user: RealmUser = app.login(RealmCredentials.anonymous())
        val realm = Realm.getInstance(SyncConfiguration.defaultConfig(user))
        assertNotNull(realm.session)
        assertEquals(SyncSession.State.ACTIVE, realm.session.state)
        assertEquals(user, realm.session.user)
        realm.close()
    }
}