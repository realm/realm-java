package io.realm.kotlin

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import io.realm.Realm
import io.realm.SyncConfiguration
import io.realm.SyncManager
import io.realm.TestSyncConfigurationFactory
import io.realm.entities.SimpleClass
import io.realm.objectserver.utils.Constants
import io.realm.SyncTestUtils
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KotlinSyncedRealmTests {

    @get:Rule
    val configFactory = TestSyncConfigurationFactory()


    private lateinit var realm: Realm

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getTargetContext())
        val user = SyncTestUtils.createTestUser()
        realm = Realm.getInstance(configFactory.createSyncConfigurationBuilder(user, Constants.DEFAULT_REALM).build())
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun syncSession() {
        assertEquals(SyncManager.getSession(realm.configuration as SyncConfiguration), realm.syncSession)
    }

    @Test
    fun syncSession_throwsForNonSyncRealm() {
        realm.close()
        realm = Realm.getInstance(configFactory.createConfiguration())
        try {
            realm.syncSession
            fail()
        } catch (ignored: IllegalStateException) {
        }
    }

    @Test
    fun classPermissions() {
        assertNotNull(realm.classPermissions<SimpleClass>())
    }

    @Test
    fun classPermissions_throwsForNonSyncRealm() {
        realm.close()
        realm = Realm.getInstance(configFactory.createConfiguration())
        try {
            realm.classPermissions<SimpleClass>()
            fail()
        } catch (ignored: IllegalStateException) {
        }
    }
}
