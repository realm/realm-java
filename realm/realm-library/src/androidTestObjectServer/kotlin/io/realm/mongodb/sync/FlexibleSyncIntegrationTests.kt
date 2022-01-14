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
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.mongodb.registerUserAndLogin
import io.realm.rule.BlockingLooperThread
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import io.realm.kotlin.where
import io.realm.mongodb.close
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import kotlin.random.Random

/**
 * Integration smoke tests for Flexible Sync. This is not intended to cover all cases, but just
 * test common scenarios.
 */
@RunWith(AndroidJUnit4::class)
class FlexibleSyncIntegrationTests {

    @get:Rule
    val configFactory = TestSyncConfigurationFactory()
    private val looperThread = BlockingLooperThread()

    private lateinit var app: TestApp
    private lateinit var realmConfig: SyncConfiguration
    private lateinit var realm: Realm

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        RealmLog.setLevel(LogLevel.ALL)
        app = TestApp(appName = TEST_APP_3)
        ServerAdmin(app).enableFlexibleSync() // Currently required because importing doesn't work
    }

    @After
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
    }

    @Test
    fun downloadInitialData() {
        val section: Int = Random.nextInt() // Generate random section to allow replays of unit tests

        // Upload data from user 1
        val user1 = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        val config1 = configFactory.createFlexibleSyncConfigurationBuilder(user1)
            .schema(SyncColor::class.java)
            .build()
        val realm1 = Realm.getInstance(config1)
        val subs = realm1.subscriptions.update {
            it.add(Subscription.create(realm1.where<SyncColor>().equalTo("section", section)))
        }
        assertTrue(subs.waitForSynchronization())
        realm1.executeTransaction {
            it.insert(SyncColor(section).apply { color = "red" })
            it.insert(SyncColor(section).apply { color = "blue" })
        }
        realm1.syncSession.uploadAllLocalChanges()
        realm1.close()

        // Download data from user 2
        val user2 = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        val config2 = configFactory.createFlexibleSyncConfigurationBuilder(user2)
            .schema(SyncColor::class.java)
            .initialSubscriptions { realm, subscriptions ->
                subscriptions.add(Subscription.create(realm.where<SyncColor>()
                    .equalTo("section", section)
                    .equalTo("color", "blue")))
            }
            .waitForInitialRemoteData()
            .build()
        val realm2 = Realm.getInstance(config2)
        assertEquals(1, realm2.where<SyncColor>().equalTo("color", "blue").count())
        realm2.close()
    }

    @Test
    fun clientResetIfNoSubscriptionWhenWriting() = looperThread.runBlocking {
        val user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        val config = configFactory.createFlexibleSyncConfigurationBuilder(user)
            .schema(SyncColor::class.java)
            .syncClientResetStrategy { session, error ->
                assertTrue(error.toString(), error.message!!.contains("Client attempted a write that is outside of permissions or query filters"))
                looperThread.testComplete()
            }
            .build()
        val realm = Realm.getInstance(config)
        looperThread.closeAfterTest(realm)
        realm.executeTransaction {
            it.insert(SyncColor().apply { color = "red" })
        }
    }

    @Test
    fun dataIsDeletedWhenSubscriptionIsRemoved() {
        val section: Int = Random.nextInt() // Generate random section to allow replays of unit tests

        val user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        val config = configFactory.createFlexibleSyncConfigurationBuilder(user)
            .schema(SyncColor::class.java)
            .initialSubscriptions { realm, subscriptions ->
                subscriptions.add(
                    Subscription.create("sub", realm.where<SyncColor>()
                        .equalTo("section", section)
                        .beginGroup()
                            .equalTo("color", "red")
                            .or()
                            .equalTo("color", "blue")
                        .endGroup()
                    )
                )
            }
            .build()
        val realm = Realm.getInstance(config)
        realm.executeTransaction {
            it.insert(SyncColor(section).apply { color = "red" })
            it.insert(SyncColor(section).apply { color = "blue" })
        }
        assertEquals(2, realm.where<SyncColor>().count())
        val subscriptions = realm.subscriptions
        subscriptions.update {
            it.addOrUpdate(Subscription.create("sub", realm.where<SyncColor>()
                .equalTo("section", section)
                .equalTo("color", "red")
            ))
        }
        assertTrue(subscriptions.waitForSynchronization())
        realm.refresh()
        assertEquals(1, realm.where<SyncColor>().count())
        realm.close()
    }
}