package io.realm.kotlin

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.*
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KotlinSyncedRealmTests {

    @get:Rule
    val configFactory = TestSyncConfigurationFactory()

    private lateinit var app: RealmApp
    private lateinit var realm: Realm

    @Before
    fun setUp() {
        // FIXME
//        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
//        app = RealmApp("foo")
//        val user = SyncTestUtils.createTestUser(app)
//        realm = Realm.getInstance(configFactory.createSyncConfigurationBuilder(user).build())
    }

    @After
    fun tearDown() {
        // FIXME
//        if (this::realm.isInitialized) {
//            realm.close()
//        }
//        if (this::app.isInitialized) {
//            RealmApp.CREATED = false
//        }
    }

    @Ignore("FIXME")
    @Test
    fun syncSession() {
        assertEquals(app.sync.getSession(realm.configuration as SyncConfiguration), realm.syncSession)
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
