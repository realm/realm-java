/*
 * Copyright 2022 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.realm.mongodb.sync

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.Realm
import io.realm.TEST_APP_3
import io.realm.TestApp
import io.realm.TestHelper
import io.realm.TestSyncConfigurationFactory
import io.realm.admin.ServerAdmin
import io.realm.entities.FlexSyncColor
import io.realm.entities.SyncDog
import io.realm.kotlin.where
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.mongodb.close
import io.realm.mongodb.registerUserAndLogin
import io.realm.rule.BlockingLooperThread
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertNull
import junit.framework.Assert.assertTrue
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertFailsWith

/**
 * Class wrapping tests for modifying a subscription set.
 */
@RunWith(AndroidJUnit4::class)
class MutableSubscriptionSetTests {


    @get:Rule
    val configFactory = TestSyncConfigurationFactory()
    private val looperThread = BlockingLooperThread()

    private lateinit var app: TestApp
    private lateinit var realm: Realm
    private lateinit var config: SyncConfiguration

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        RealmLog.setLevel(LogLevel.ALL)
        app = TestApp(appName = TEST_APP_3)
        ServerAdmin(app).enableFlexibleSync() // Currrently required because importing doesn't work
        val user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        config = configFactory.createFlexibleSyncConfigurationBuilder(user)
            .schema(FlexSyncColor::class.java)
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
        realm.subscriptions.update { mutableSubs ->
            assertEquals(0, mutableSubs.size())
            assertEquals(SubscriptionSet.State.UNCOMMITTED, mutableSubs.state)
        }
    }

    @Test
    fun addNamedSubscription() {
        val updatedSubs = realm.subscriptions.update { mutableSubs ->
            mutableSubs.add(Subscription.create("test", realm.where<FlexSyncColor>()))
        }
        assertEquals(1, updatedSubs.size())
        assertEquals(SubscriptionSet.State.PENDING, updatedSubs.state)
        val sub: Subscription = updatedSubs.first()
        assertEquals("test", sub.name)
        assertEquals("TRUEPREDICATE ", sub.query)
        assertEquals("FlexSyncColor", sub.objectType)
        assertTrue(sub.createdAt!!.time > 0)
        assertTrue(sub.updatedAt == sub.createdAt)
    }

    @Test
    fun addAnonymousSubscription() {
        val updatedSubs = realm.subscriptions.update { mutableSubs ->
            mutableSubs.add(Subscription.create(realm.where<FlexSyncColor>()))
        }
        assertEquals(1, updatedSubs.size())
        assertEquals(SubscriptionSet.State.PENDING, updatedSubs.state)
        val sub: Subscription = updatedSubs.first()
        assertNull(sub.name)
        assertEquals(sub.query, "TRUEPREDICATE ")
        assertEquals(sub.objectType, "FlexSyncColor")
        assertTrue(sub.createdAt!!.time > 0)
        assertTrue(sub.updatedAt == sub.createdAt)
    }

    @Test
    fun addExistingAnonymous_throws() {
        realm.subscriptions.update { mutableSubs ->
            mutableSubs.add(Subscription.create(realm.where<FlexSyncColor>()))
            assertFailsWith<IllegalArgumentException> {
                mutableSubs.add(Subscription.create(realm.where<FlexSyncColor>()))
            }
        }
    }

    @Test
    fun addExistingNamed_throws() {
        realm.subscriptions.update { mutableSubs ->
            mutableSubs.add(Subscription.create("sub1", realm.where<FlexSyncColor>()))
            assertFailsWith<IllegalArgumentException> {
                mutableSubs.add(Subscription.create("sub1", realm.where<FlexSyncColor>()))
            }
        }
    }

    @Test
    fun addOrUpdate_managedThrows() {
        realm.subscriptions.update {
            val managedSub = it.add(Subscription.create(realm.where<FlexSyncColor>()))
            assertFailsWith<java.lang.IllegalArgumentException> {  it.addOrUpdate(managedSub) }
        }
    }

    @Test
    fun update() {
        val subs = realm.subscriptions
        subs.update { mutableSubs ->
            mutableSubs.add(Subscription.create("sub1", realm.where<FlexSyncColor>()))
        }
        subs.update { mutableSubs ->
            mutableSubs.addOrUpdate(Subscription.create("sub1", realm.where<FlexSyncColor>().equalTo("color", "red")))
        }
        val sub = subs.first()
        assertEquals("sub1", sub.name)
        assertEquals("FlexSyncColor", sub.objectType)
        assertEquals("color == \"red\" ", sub.query)
        assertTrue(sub.createdAt!! < sub.updatedAt!!)
    }

    @Test
    fun updateAsync() = looperThread.runBlocking {
        val realm = Realm.getInstance(config)
        looperThread.closeAfterTest(realm)
        val subs = realm.subscriptions
        subs.update {
            it.add(Subscription.create("sub1", realm.where<FlexSyncColor>()))
        }

        subs.updateAsync(object: SubscriptionSet.UpdateAsyncCallback {
            override fun update(subscriptions: MutableSubscriptionSet) {
                Realm.getInstance(realm.configuration).use { bgRealm ->
                    subscriptions.addOrUpdate(Subscription.create(
                        "sub1",
                        bgRealm.where<FlexSyncColor>()
                            .equalTo("color", "red"))
                    )
                }
            }

            override fun onSuccess(subscriptions: SubscriptionSet) {
                val sub = subscriptions.first()
                assertEquals("sub1", sub.name)
                assertEquals("FlexSyncColor", sub.objectType)
                assertEquals("color == \"red\" ", sub.query)
                assertTrue(sub.createdAt!! < sub.updatedAt!!)
                looperThread.testComplete()
            }

            override fun onError(exception: Throwable) {
                fail(exception.toString())
            }
        })
    }

    @Test
    fun updateAsync_throws() = looperThread.runBlocking {
        val realm = Realm.getInstance(config)
        looperThread.closeAfterTest(realm)
        val subs = realm.subscriptions
        subs.updateAsync(object: SubscriptionSet.UpdateAsyncCallback {
            override fun update(subscriptions: MutableSubscriptionSet) {
                throw RuntimeException("Boom")
            }

            override fun onSuccess(subscriptions: SubscriptionSet) {
                fail()
            }

            override fun onError(exception: Throwable) {
                assertTrue(exception is RuntimeException)
                assertEquals("Boom", exception.message)
                looperThread.testComplete()
            }
        })
    }

    @Test
    fun removeNamed() {
        var updatedSubs = realm.subscriptions.update { mutableSubs ->
            mutableSubs.addOrUpdate(Subscription.create("test", realm.where<FlexSyncColor>()))
        }
        assertEquals(1, updatedSubs.size())
        updatedSubs = updatedSubs.update { mutableSubs ->
            assertTrue(mutableSubs.remove("test"))
            assertEquals(0, mutableSubs.size());
        }
        assertEquals(0, updatedSubs.size());
    }

    @Test
    fun removeNamed_fails() {
        var updatedSubs = realm.subscriptions.update { mutableSubs ->
            assertFalse(mutableSubs.remove("dont-exists"))
        }
    }

    @Test
    fun removeSubscription() {
        var updatedSubs = realm.subscriptions.update { mutableSubs ->
            mutableSubs.addOrUpdate(Subscription.create("test", realm.where<FlexSyncColor>()))
        }
        assertEquals(1, updatedSubs.size())
        updatedSubs = updatedSubs.update { mutableSubs ->
            assertTrue(mutableSubs.remove(mutableSubs.first()))
            assertEquals(0, mutableSubs.size());
        }
        assertEquals(0, updatedSubs.size());
    }

    @Test
    fun removeSubscription_fails() {
        realm.subscriptions.update { mutableSubs ->
            val managedSub = mutableSubs.add(Subscription.create(realm.where<FlexSyncColor>()))
            assertTrue(mutableSubs.remove(managedSub))
            assertFalse(mutableSubs.remove(managedSub))
        }
    }

    @Test
    fun removeSubscription_unManagedthrows() {
        realm.subscriptions.update { mutableSubs ->
            val managedSub = mutableSubs.add(Subscription.create("sub", realm.where<FlexSyncColor>()))
            assertFailsWith<IllegalArgumentException> {
                mutableSubs.remove(Subscription.create("sub", realm.where<FlexSyncColor>()))
            }
        }
    }

    @Test
    fun removeAllStringTyped() {
        var updatedSubs = realm.subscriptions.update { mutableSubs ->
            mutableSubs.addOrUpdate(Subscription.create(realm.where<FlexSyncColor>()))
        }
        assertEquals(1, updatedSubs.size())
        updatedSubs = updatedSubs.update { mutableSubs ->
            assertTrue(mutableSubs.removeAll("FlexSyncColor"))
            assertEquals(0, mutableSubs.size());
        }
        assertEquals(0, updatedSubs.size());
    }

    @Test
    fun removeAllStringTyped_fails() {
        // Not part of schema
        realm.subscriptions.update { mutableSubs ->
            assertFalse(mutableSubs.removeAll("DontExists"))
        }

        // part of schema
        realm.subscriptions.update { mutableSubs ->
            assertFalse(mutableSubs.removeAll("FlexSyncColor"))
        }
    }

    @Test
    fun removeAllClassTyped() {
        var updatedSubs = realm.subscriptions.update { mutableSubs ->
            mutableSubs.addOrUpdate(Subscription.create(realm.where<FlexSyncColor>()))
        }
        assertEquals(1, updatedSubs.size())
        updatedSubs = updatedSubs.update { mutableSubs ->
            assertTrue(mutableSubs.removeAll(FlexSyncColor::class.java))
            assertEquals(0, mutableSubs.size());
        }
        assertEquals(0, updatedSubs.size());
    }

    @Test
    fun removeAllClassTyped_fails() {
        // Not part of schema
        realm.subscriptions.update { mutableSubs ->
            assertFailsWith<IllegalArgumentException> {
                mutableSubs.removeAll(SyncDog::class.java)
            }
        }

        // part of schema
        realm.subscriptions.update { mutableSubs ->
            assertFalse(mutableSubs.removeAll(FlexSyncColor::class.java))
        }
    }

    @Test
    fun removeAll() {
        var updatedSubs = realm.subscriptions.update { mutableSubs ->
            mutableSubs.add(Subscription.create("test", realm.where<FlexSyncColor>()))
            mutableSubs.add(Subscription.create("test2", realm.where<FlexSyncColor>()))
        }
        assertEquals(2, updatedSubs.size())
        updatedSubs = updatedSubs.update { mutableSubs ->
            assertTrue(mutableSubs.removeAll())
            assertEquals(0, mutableSubs.size());
        }
        assertEquals(0, updatedSubs.size());
    }

    @Test
    fun removeAll_fails() {
        realm.subscriptions.update { mutableSubs ->
            assertFalse(mutableSubs.removeAll())
        }
    }

}
