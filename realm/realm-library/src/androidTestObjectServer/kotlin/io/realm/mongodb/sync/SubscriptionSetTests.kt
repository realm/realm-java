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
import io.realm.kotlin.where
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.mongodb.close
import io.realm.mongodb.registerUserAndLogin
import io.realm.rule.BlockingLooperThread
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit
import kotlin.test.assertFailsWith

/**
 * Class wrapping tests for SubscriptionSets
 */
@RunWith(AndroidJUnit4::class)
class SubscriptionSetTests {

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
        ServerAdmin(app).enableFlexibleSync() // Currrently required because importing doesn't work
        val user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        realmConfig = configFactory.createFlexibleSyncConfigurationBuilder(user)
            .schema(FlexSyncColor::class.java)
            .build()
        realm = Realm.getInstance(realmConfig)
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

    @Test
    fun find() {
        val subscriptions = realm.subscriptions
        assertNull(subscriptions.find(realm.where<FlexSyncColor>()))
        subscriptions.update { mutableSubs ->
            mutableSubs.addOrUpdate(Subscription.create(realm.where<FlexSyncColor>()))
        }
        val sub: Subscription? = subscriptions.find(realm.where<FlexSyncColor>())
        assertNotNull(sub)
        assertEquals("FlexSyncColor", sub!!.objectType)
    }

    @Test
    fun find_byName() {
        val subscriptions = realm.subscriptions
        assertNull(subscriptions.find("foo"))
        subscriptions.update { mutableSubs ->
            mutableSubs.addOrUpdate(Subscription.create("foo", realm.where<FlexSyncColor>()))
        }

        val sub: Subscription? = subscriptions.find("foo")
        assertNotNull(sub)
        assertEquals("foo", sub!!.name)
    }

    @Test
    fun getState() {
        val subscriptions = realm.subscriptions
        assertEquals(SubscriptionSet.State.UNCOMMITTED, subscriptions.state)
        subscriptions.update { mutableSubs ->
            mutableSubs.addOrUpdate(Subscription.create("test", realm.where<FlexSyncColor>()))
        }
        assertEquals(SubscriptionSet.State.PENDING, subscriptions.state)
        subscriptions.waitForSynchronization()
        assertEquals(SubscriptionSet.State.COMPLETE, subscriptions.state)
        subscriptions.update { mutableSubs ->
            mutableSubs.addOrUpdate(Subscription.create("test", realm.where<FlexSyncColor>().limit(1)))
        }
        subscriptions.waitForSynchronization()
        assertEquals(SubscriptionSet.State.ERROR, subscriptions.state)
    }

    @Test
    fun size() {
        val subscriptions = realm.subscriptions
        assertEquals(0, subscriptions.size())
        subscriptions.update { mutableSubs ->
            mutableSubs.addOrUpdate(Subscription.create(realm.where<FlexSyncColor>()))
        }
        assertEquals(1, subscriptions.size())
        subscriptions.update { mutableSubs ->
            mutableSubs.removeAll()
        }
        assertEquals(0, subscriptions.size())
    }

    @Test
    fun errorMessage() {
        val subscriptions = realm.subscriptions
        assertNull(subscriptions.errorMessage)
        subscriptions.update { mutableSubs ->
            mutableSubs.addOrUpdate(Subscription.create(realm.where<FlexSyncColor>().limit(1)))
        }
        subscriptions.waitForSynchronization()
        assertTrue(subscriptions.errorMessage!!.contains("Client provided query with bad syntax"))
        subscriptions.update { mutableSubs ->
            mutableSubs.removeAll() // TODO Removing all queries seems to provoke an error on the server
            mutableSubs.addOrUpdate(
                Subscription.create(realm.where<FlexSyncColor>())
            )
        }
        subscriptions.waitForSynchronization()
        assertNull(subscriptions.errorMessage)
    }

    @Test
    fun iterator_zeroSize() {
        val subscriptions = realm.subscriptions
        val iterator: MutableIterator<Subscription> = subscriptions.iterator()
        assertFalse(iterator.hasNext())
        assertFailsWith<NoSuchElementException> { iterator.next() }
        assertFailsWith<UnsupportedOperationException> { iterator.remove() }
    }

    @Test
    fun iterator() {
        val subscriptions = realm.subscriptions
        subscriptions.update { mutableSub ->
            mutableSub.addOrUpdate(Subscription.create("sub1", realm.where<FlexSyncColor>()))
        }
        val iterator: MutableIterator<Subscription> = subscriptions.iterator()
        assertTrue(iterator.hasNext())
        assertEquals("sub1", iterator.next().name)
        assertFalse(iterator.hasNext())
        assertFailsWith<NoSuchElementException> { iterator.next() }
        assertFailsWith<UnsupportedOperationException> { iterator.remove() }
    }

    @Test
    fun subscriptions_throwsOnClosedRealm() {
        realm.close()
        assertFailsWith<IllegalStateException> { realm.subscriptions }
    }

    @Test
    @Ignore
    fun subscriptions_accessAfterRealmClosed() {
        val subscriptions = realm.subscriptions
        realm.close()
        // FIXME: Results in native crash. Must check if Realm is closed.
        subscriptions.update { mutableSubs ->
            mutableSubs.addOrUpdate(Subscription.create(realm.where<FlexSyncColor>()))
        }
        subscriptions.waitForSynchronization()
        assertEquals(SubscriptionSet.State.COMPLETE, subscriptions.state)
    }

    @Test
    fun waitForSynchronizationInitialSubscriptions() {
        val subscriptions = realm.subscriptions
         assertTrue(subscriptions.waitForSynchronization())
         assertEquals(SubscriptionSet.State.COMPLETE, subscriptions.state)
         assertEquals(0, subscriptions.size())
    }

    @Test
    fun waitForSynchronizationInitialEmptySubscriptionSet() {
        val subscriptions = realm.subscriptions
        subscriptions.update { /* Do nothing */ }

        assertTrue(subscriptions.waitForSynchronization())
        assertEquals(SubscriptionSet.State.COMPLETE, subscriptions.state)
        assertEquals(0, subscriptions.size())
    }

    @Test
    fun waitForSynchronizationAfterInsert() {
        var updatedSubs = realm.subscriptions.update { mutableSubs ->
            mutableSubs.add(Subscription.create("test", realm.where<FlexSyncColor>()))
        }
        assertFalse(updatedSubs.state == SubscriptionSet.State.COMPLETE)
        assertTrue(updatedSubs.waitForSynchronization())
        assertEquals(SubscriptionSet.State.COMPLETE, updatedSubs.state)
    }

    @Test
    fun waitForSynchronizationError() {
        var updatedSubs = realm.subscriptions.update { mutableSubs ->
            mutableSubs.addOrUpdate(Subscription.create("test", realm.where<FlexSyncColor>().limit(1)))
        }
        assertFalse(updatedSubs.waitForSynchronization())
        assertEquals(SubscriptionSet.State.ERROR, updatedSubs.state)
        assertTrue(updatedSubs.errorMessage!!.contains("Client provided query with bad syntax"))
    }

    @Test
    fun waitForSynchronization_timeOut() {
        var updatedSubs = realm.subscriptions.update { mutableSubs ->
            mutableSubs.add(Subscription.create("sub", realm.where<FlexSyncColor>()))
        }
        assertTrue(updatedSubs.waitForSynchronization(1, TimeUnit.MINUTES))
    }

    @Test
    fun waitForSynchronizationAsync() = looperThread.runBlocking {
        val realm = Realm.getInstance(realmConfig)
        looperThread.closeAfterTest(realm)
        val subs = realm.subscriptions
        subs.update {
            it.add(Subscription.create(realm.where<FlexSyncColor>()))
        }
        subs.waitForSynchronizationAsync(object:
            SubscriptionSet.StateChangeCallback {
            override fun onStateChange(subscriptions: SubscriptionSet) {
                assertEquals(1, subscriptions.size())
                assertEquals(SubscriptionSet.State.COMPLETE, subscriptions.state)
                looperThread.testComplete()
            }
            override fun onError(e: Throwable) {
                fail(e.toString())
            }
        })
    }

    @Test
    fun waitForSynchronizationAsync_error() = looperThread.runBlocking {
        // Server errors are not reported as Exceptions but as state changes
        val realm = Realm.getInstance(realmConfig)
        looperThread.closeAfterTest(realm)
        val subs = realm.subscriptions
        subs.update { mutableSubs ->
            mutableSubs.add(Subscription.create("test", realm.where<FlexSyncColor>().limit(1)))
        }
        subs.waitForSynchronizationAsync(object:
            SubscriptionSet.StateChangeCallback {
            override fun onStateChange(subscriptions: SubscriptionSet) {
                assertEquals(SubscriptionSet.State.ERROR, subscriptions.state)
                assertTrue(subscriptions.errorMessage!!.contains("Client provided query with bad syntax"))
                looperThread.testComplete()
            }
            override fun onError(e: Throwable) {
                fail(e.toString())
            }
        })
    }

    @Test
    fun waitForSynchronizationAsync_timeOut() = looperThread.runBlocking {
        val realm = Realm.getInstance(realmConfig)
        looperThread.closeAfterTest(realm)
        val subs = realm.subscriptions
        subs.update {
            it.add(Subscription.create(realm.where<FlexSyncColor>()))
        }
        subs.waitForSynchronizationAsync(1, TimeUnit.MINUTES, object:
            SubscriptionSet.StateChangeCallback {
            override fun onStateChange(subscriptions: SubscriptionSet) {
                assertEquals(1, subscriptions.size())
                assertEquals(SubscriptionSet.State.COMPLETE, subscriptions.state)
                looperThread.testComplete()
            }
            override fun onError(e: Throwable) {
                fail(e.toString())
            }
        })
    }
}
