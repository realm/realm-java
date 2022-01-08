package io.realm.mongodb.sync

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.Realm
import io.realm.TEST_APP_3
import io.realm.TestApp
import io.realm.TestHelper
import io.realm.TestSyncConfigurationFactory
import io.realm.entities.SyncColor
import io.realm.entities.SyncDog
import io.realm.mongodb.Credentials
import io.realm.mongodb.User
import io.realm.mongodb.close
import io.realm.mongodb.registerUserAndLogin
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Class wrapping tests for SubscriptionSets
 */
@RunWith(AndroidJUnit4::class)
class SubscriptionSetTests {

    @get:Rule
    val configFactory = TestSyncConfigurationFactory()

    private lateinit var app: TestApp
    private lateinit var realm: Realm

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        app = TestApp(appName = TEST_APP_3)
        val user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        val config = configFactory.createFlexibleSyncConfiguationBuilder(user)
            .schema(SyncColor::class.java)
            .build()
        realm = Realm.getInstance(config)
    }

    @After
    fun tearDown() {
        realm.close()
        if (this::app.isInitialized) {
            app.close()
        }
    }

    @Test
    fun initialSubscriptions() {
        val subscriptions = realm.subscriptions
        assertEquals(0, subscriptions.size())
        assertEquals(SubscriptionSet.State.UNCOMMITTED, subscriptions.state)
    }
}