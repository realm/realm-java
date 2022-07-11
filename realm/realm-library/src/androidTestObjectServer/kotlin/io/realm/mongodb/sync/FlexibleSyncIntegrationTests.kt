package io.realm.mongodb.sync

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.*
import io.realm.admin.ServerAdmin
import io.realm.entities.ColorSyncSchema
import io.realm.entities.FlexSyncColor
import io.realm.entities.SyncColor
import io.realm.kotlin.syncSession
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.mongodb.User
import io.realm.mongodb.registerUserAndLogin
import io.realm.rule.BlockingLooperThread
import org.junit.runner.RunWith
import io.realm.kotlin.where
import io.realm.mongodb.close
import org.junit.*
import org.junit.Assert.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
import kotlin.test.assertFailsWith

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
    private lateinit var user: User
    private var section: Int = 0

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        RealmLog.setLevel(LogLevel.ALL)
        app = TestApp(appName = TEST_APP_3)
        user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "SECRET_PASSWORD")
        ServerAdmin(app).enableFlexibleSync() // Currently required because importing doesn't work

        section = Random.nextInt() // Generate random section to allow replays of unit tests
    }

    @After
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
    }

    @Test
    fun downloadInitialData() {
        // Upload data from user 1
        val user1 = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        val config1 = configFactory.createFlexibleSyncConfigurationBuilder(user1)
            .schema(FlexSyncColor::class.java)
            .build()
        val realm1 = Realm.getInstance(config1)
        val subs = realm1.subscriptions.update {
            it.add(Subscription.create(realm1.where<FlexSyncColor>().equalTo("section", section)))
        }
        assertTrue(subs.waitForSynchronization())
        realm1.executeTransaction {
            it.insert(FlexSyncColor(section).apply { color = "red" })
            it.insert(FlexSyncColor(section).apply { color = "blue" })
        }
        realm1.syncSession.uploadAllLocalChanges()
        realm1.close()

        // Download data from user 2
        val user2 = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        val config2 = configFactory.createFlexibleSyncConfigurationBuilder(user2)
            .schema(FlexSyncColor::class.java)
            .initialSubscriptions { realm, subscriptions ->
                subscriptions.add(
                    Subscription.create(
                        realm.where<FlexSyncColor>()
                            .equalTo("section", section)
                            .equalTo("color", "blue")
                    )
                )
            }
            .waitForInitialRemoteData()
            .build()
        val realm2 = Realm.getInstance(config2)
        assertEquals(1, realm2.where<FlexSyncColor>().equalTo("color", "blue").count())
        realm2.close()
    }

    @Test
    fun clientResetIfNoSubscriptionWhenWriting() = looperThread.runBlocking {
        val user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        val config = configFactory.createFlexibleSyncConfigurationBuilder(user)
            .schema(FlexSyncColor::class.java)
            .build()
        val realm = Realm.getInstance(config)
        looperThread.closeAfterTest(realm)
        realm.executeTransaction {
            assertFailsWith<RuntimeException> { it.insert(FlexSyncColor().apply { color = "red" }) }
            looperThread.testComplete()
        }
    }

    @Test
    fun dataIsDeletedWhenSubscriptionIsRemoved() {
        val user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        val config = configFactory.createFlexibleSyncConfigurationBuilder(user)
            .schema(FlexSyncColor::class.java)
            .initialSubscriptions { realm, subscriptions ->
                subscriptions.add(
                    Subscription.create(
                        "sub", realm.where<FlexSyncColor>()
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
            it.insert(FlexSyncColor(section).apply { color = "red" })
            it.insert(FlexSyncColor(section).apply { color = "blue" })
        }
        assertEquals(2, realm.where<FlexSyncColor>().count())
        val subscriptions = realm.subscriptions
        subscriptions.update {
            it.addOrUpdate(
                Subscription.create(
                    "sub", realm.where<FlexSyncColor>()
                        .equalTo("section", section)
                        .equalTo("color", "red")
                )
            )
        }
        assertTrue(subscriptions.waitForSynchronization())
        realm.refresh()
        assertEquals(1, realm.where<FlexSyncColor>().count())
        realm.close()
    }

    @Test
    fun errorHandler_discardUnsyncedChangesStrategyReported() = looperThread.runBlocking {
        val counter = AtomicInteger()

        val incrementAndValidate = {
            if (2 == counter.incrementAndGet()) {
                looperThread.testComplete()
            }
        }

        val config = configFactory.createFlexibleSyncConfigurationBuilder(user)
            .schema(FlexSyncColor::class.java)
            .initialSubscriptions { realm, subscriptions ->
                subscriptions.add(
                    Subscription.create(
                        "sub",
                        realm.where<FlexSyncColor>().equalTo("section", section)
                    )
                )
            }
            .syncClientResetStrategy(object : DiscardUnsyncedChangesStrategy {
                override fun onBeforeReset(realm: Realm) {
                    assertTrue(realm.isFrozen)
                    assertEquals(1, realm.where<FlexSyncColor>().count())
                    incrementAndValidate()
                }

                override fun onAfterReset(before: Realm, after: Realm) {
                    assertTrue(before.isFrozen)
                    assertFalse(after.isFrozen)

                    assertEquals(1, before.where<FlexSyncColor>().count())
                    assertEquals(0, after.where<FlexSyncColor>().count())

                    //Validate we can move data to the reset Realm.
                    after.executeTransaction {
                        it.insert(before.where<FlexSyncColor>().findFirst()!!)
                    }
                    assertEquals(1, after.where<FlexSyncColor>().count())
                    incrementAndValidate()
                }

                override fun onError(session: SyncSession, error: ClientResetRequiredError) {
                    fail("This test case was not supposed to trigger DiscardUnsyncedChangesStrategy::onError()")
                }

            })
            .modules(ColorSyncSchema())
            .build()

        val realm = Realm.getInstance(config)

        realm.triggerClientReset(user) {
            realm.executeTransaction {
                realm.copyToRealm(FlexSyncColor())
            }

            assertEquals(1, realm.where<FlexSyncColor>().count())
        }

        looperThread.closeAfterTest(realm)
    }

    @Test
    fun errorHandler_automaticRecoveryStrategy() {
        val config = configFactory.createFlexibleSyncConfigurationBuilder(user)
            .schema(FlexSyncColor::class.java)
            .initialSubscriptions { realm, subscriptions ->
                subscriptions.add(
                    Subscription.create(
                        "sub",
                        realm.where<FlexSyncColor>().equalTo("section", section)
                    )
                )
            }
            .syncClientResetStrategy(object :
                RecoverUnsyncedChangesStrategy {
                override fun onError(session: SyncSession, error: ClientResetRequiredError) {
                    fail("This test case was not supposed to trigger AutomaticRecoveryStrategy::onError()")
                }
            })
            .modules(ColorSyncSchema())
            .build()

        Realm.getInstance(config).use { realm ->
            // Downloading would trigger the client reset if any
            realm.syncSession.downloadAllServerChanges()
            assertEquals(1, realm.where<FlexSyncColor>().count())
        }
    }

    @Test
    fun errorHandler_automaticRecoveryOrDiscardStrategy() {
        val config = configFactory.createFlexibleSyncConfigurationBuilder(user)
            .schema(FlexSyncColor::class.java)
            .initialSubscriptions { realm, subscriptions ->
                subscriptions.add(
                    Subscription.create(
                        "sub",
                        realm.where<FlexSyncColor>().equalTo("section", section)
                    )                )
            }
            .syncClientResetStrategy(object :
                RecoverOrDiscardUnsyncedChangesStrategy {
                override fun onBeforeReset(realm: Realm) {
                    fail("This test case was not supposed to trigger AutomaticRecoveryOrDiscardUnsyncedChangesStrategy::onBeforeReset()")
                }

                override fun onAfterReset(before: Realm, after: Realm) {
                    fail("This test case was not supposed to trigger AutomaticRecoveryOrDiscardUnsyncedChangesStrategy::onAfterReset()")
                }

                override fun onError(session: SyncSession, error: ClientResetRequiredError) {
                    fail("This test case was not supposed to trigger AutomaticRecoveryStrategy::onError()")
                }
            })
            .modules(ColorSyncSchema())
            .build()

        Realm.getInstance(config).use { realm ->
            // Downloading would trigger the client reset if any
            realm.syncSession.downloadAllServerChanges()
            assertEquals(1, realm.where<FlexSyncColor>().count())
        }
    }

    @Test
    fun errorHandler_automaticRecoveryOrDiscardStrategy_discardsLocal() = looperThread.runBlocking {
        val counter = AtomicInteger()

        val incrementAndValidate = {
            if (2 == counter.incrementAndGet()) {
                looperThread.testComplete()
            }
        }
        val config = configFactory.createFlexibleSyncConfigurationBuilder(user)
            .schema(FlexSyncColor::class.java)
            .initialSubscriptions { realm, subscriptions ->
                subscriptions.add(
                    Subscription.create(
                        "sub",
                        realm.where<FlexSyncColor>().equalTo("section", section)
                    )
                )
            }
            .syncClientResetStrategy(object :
                RecoverOrDiscardUnsyncedChangesStrategy {
                override fun onBeforeReset(realm: Realm) {
                    assertTrue(realm.isFrozen)
                    assertEquals(1, realm.where<FlexSyncColor>().count())
                    incrementAndValidate()
                }

                override fun onAfterReset(before: Realm, after: Realm) {
                    assertTrue(before.isFrozen)
                    assertFalse(after.isFrozen)

                    assertEquals(1, before.where<FlexSyncColor>().count())
                    assertEquals(0, after.where<FlexSyncColor>().count())

                    //Validate we can move data to the reset Realm.
                    after.executeTransaction {
                        it.insert(before.where<FlexSyncColor>().findFirst()!!)
                    }
                    assertEquals(1, after.where<FlexSyncColor>().count())
                    incrementAndValidate()
                }

                override fun onError(session: SyncSession, error: ClientResetRequiredError) {
                    fail("This test case was not supposed to trigger AutomaticRecoveryOrDiscardUnsyncedChangesStrategy::onError()")
                }
            })
            .modules(ColorSyncSchema())
            .build()

        val realm = Realm.getInstance(config)

        realm.triggerClientReset(user, true) {
            realm.executeTransaction {
                realm.copyToRealm(FlexSyncColor())
            }

            assertEquals(1, realm.where<FlexSyncColor>().count())
        }

        looperThread.closeAfterTest(realm)
    }

    fun Realm.triggerClientReset(
        user: User,
        isRecoveryModeDisabled: Boolean = false,
        block: () -> Unit
    ) {
        syncSession.downloadAllServerChanges()
        syncSession.stop()

        block()

        // Trigger client reset here
        app.triggerClientReset(user.id, isRecoveryModeDisabled)

        syncSession.start()
    }
}