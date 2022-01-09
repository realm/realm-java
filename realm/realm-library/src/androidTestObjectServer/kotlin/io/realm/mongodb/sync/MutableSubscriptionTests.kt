package io.realm.mongodb.sync

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.Realm
import io.realm.TEST_APP_3
import io.realm.TestApp
import io.realm.TestHelper
import io.realm.TestSyncConfigurationFactory
import io.realm.admin.ServerAdmin
import io.realm.entities.SyncColor
import io.realm.kotlin.syncSession
import io.realm.kotlin.where
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.mongodb.close
import io.realm.mongodb.registerUserAndLogin
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertNull
import junit.framework.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Class wrapping tests for SubscriptionSets
 */
@RunWith(AndroidJUnit4::class)
class MutableSubscriptionTests {

    @get:Rule
    val configFactory = TestSyncConfigurationFactory()

    private lateinit var app: TestApp
    private lateinit var realm: Realm

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        RealmLog.setLevel(LogLevel.ALL)
        app = TestApp(appName = TEST_APP_3)
        ServerAdmin(app).enableFlexibleSync() // Currrently required because importing doesn't work
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
        val subscriptions = realm.subscriptions.update { mutableSubs ->
            assertEquals(0, mutableSubs.size())
            assertEquals(SubscriptionSet.State.UNCOMMITTED, mutableSubs.state)
        }
    }

    @Test
    fun insertNamedSubscription() {
        val updatedSubs = realm.subscriptions.update { mutableSubs ->
            mutableSubs.addOrUpdate(Subscription.create("test", realm.where<SyncColor>()))
        }
        assertEquals(1, updatedSubs.size())
        assertEquals(SubscriptionSet.State.PENDING, updatedSubs.state)
        val sub: Subscription = updatedSubs.first()
        assertEquals(sub.name, "test")
        assertEquals(sub.query, "TRUEPREDICATE ")
        assertEquals(sub.objectType, "SyncColor")
        assertTrue(sub.createdAt!!.time > 0)
        assertTrue(sub.updatedAt == sub.createdAt)
    }

    @Test
    fun insertAnonymousSubscription() {
        val updatedSubs = realm.subscriptions.update { mutableSubs ->
            mutableSubs.addOrUpdate(Subscription.create(realm.where<SyncColor>()))
        }
        assertEquals(1, updatedSubs.size())
        assertEquals(SubscriptionSet.State.PENDING, updatedSubs.state)
        val sub: Subscription = updatedSubs.first()
        assertNull(sub.name)
        assertEquals(sub.query, "TRUEPREDICATE ")
        assertEquals(sub.objectType, "SyncColor")
        assertTrue(sub.createdAt!!.time > 0)
        assertTrue(sub.updatedAt == sub.createdAt)
    }

    @Test
    fun removeNamed() {
        var updatedSubs = realm.subscriptions.update { mutableSubs ->
            mutableSubs.addOrUpdate(Subscription.create("test", realm.where<SyncColor>()))
        }
        assertEquals(1, updatedSubs.size())
        updatedSubs = updatedSubs.update { mutableSubs ->
            assertTrue(mutableSubs.remove("test"))
            assertEquals(0, mutableSubs.size());
        }
        assertEquals(0, updatedSubs.size());
    }

    @Test
    fun removeSubscription() {
        var updatedSubs = realm.subscriptions.update { mutableSubs ->
            mutableSubs.addOrUpdate(Subscription.create("test", realm.where<SyncColor>()))
        }
        assertEquals(1, updatedSubs.size())
        updatedSubs = updatedSubs.update { mutableSubs ->
            assertTrue(mutableSubs.remove(mutableSubs.first()))
            assertEquals(0, mutableSubs.size());
        }
        assertEquals(0, updatedSubs.size());
    }

    @Test
    fun removeAllStringTyped() {
        var updatedSubs = realm.subscriptions.update { mutableSubs ->
            mutableSubs.addOrUpdate(Subscription.create(realm.where<SyncColor>()))
        }
        assertEquals(1, updatedSubs.size())
        updatedSubs = updatedSubs.update { mutableSubs ->
            assertTrue(mutableSubs.removeAll("SyncColor"))
            assertEquals(0, mutableSubs.size());
        }
        assertEquals(0, updatedSubs.size());
    }

    @Test
    fun removeAllClazzTyped() {
        var updatedSubs = realm.subscriptions.update { mutableSubs ->
            mutableSubs.addOrUpdate(Subscription.create(realm.where<SyncColor>()))
        }
        assertEquals(1, updatedSubs.size())
        updatedSubs = updatedSubs.update { mutableSubs ->
            assertTrue(mutableSubs.removeAll(SyncColor::class.java))
            assertEquals(0, mutableSubs.size());
        }
        assertEquals(0, updatedSubs.size());
    }

    @Test
    fun removeAll() {
        var updatedSubs = realm.subscriptions.update { mutableSubs ->
            mutableSubs.addOrUpdate(Subscription.create("test", realm.where<SyncColor>()))
        }
        assertEquals(1, updatedSubs.size())
        updatedSubs = updatedSubs.update { mutableSubs ->
            assertTrue(mutableSubs.removeAll())
            assertEquals(0, mutableSubs.size());
        }
        assertEquals(0, updatedSubs.size());
    }

    @Test
    fun waitForSynchronizationAfterInsert() {
        var updatedSubs = realm.subscriptions.update { mutableSubs ->
            mutableSubs.addOrUpdate(Subscription.create("test", realm.where<SyncColor>()))
        }
        assertTrue(updatedSubs.waitForSynchronization())
        assertEquals(SubscriptionSet.State.COMPLETE, updatedSubs.state)
    }

    @Test
    fun waitForSynchronizationError() {
        var updatedSubs = realm.subscriptions.update { mutableSubs ->
            mutableSubs.addOrUpdate(Subscription.create("test", realm.where<SyncColor>().limit(1)))
        }
        assertFalse(updatedSubs.waitForSynchronization())
        assertEquals(SubscriptionSet.State.ERROR, updatedSubs.state)
        assertTrue(updatedSubs.errorMessage!!.contains("Client provided query with bad syntax"))
    }
}