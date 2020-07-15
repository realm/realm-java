package io.realm.kotlin

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.realm.*
import io.realm.mongodb.App
import io.realm.mongodb.sync.SyncConfiguration
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KotlinSyncedRealmTests {

    @get:Rule
    val configFactory = TestSyncConfigurationFactory()

    private lateinit var app: App
    private lateinit var realm: Realm

    @Before
    fun setUp() {
        // FIXME
//        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
//        app = App("foo")
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
//            App.CREATED = false
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
