package io.realm.kotlin

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.*
import io.realm.objectserver.utils.Constants
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KotlinSyncedRealmTests {

    @get:Rule
    val configFactory = TestSyncConfigurationFactory()


    private lateinit var realm: Realm

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        val user = SyncTestUtils.createTestUser()
        realm = Realm.getInstance(configFactory.createSyncConfigurationBuilder(user, Constants.DEFAULT_REALM).build())
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Ignore("FIXME")
    @Test
    fun syncSession() {
        assertEquals(SyncManager.getSession(realm.configuration as SyncConfiguration), realm.syncSession)
    }

    @Ignore("FIXME")
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

}
