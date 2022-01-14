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
import io.realm.TestSyncConfigurationFactory
import io.realm.mongodb.SyncTestUtils.Companion.createTestUser
import io.realm.mongodb.User
import io.realm.mongodb.close
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertFailsWith

@RunWith(AndroidJUnit4::class)
class FlexibleSyncConfigurationTests {

    @get:Rule
    val configFactory = TestSyncConfigurationFactory()

    private lateinit var app: TestApp

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        app = TestApp(appName = TEST_APP_3)
    }

    @After
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
    }

    @Test
    fun equals() {
        val user: User = createTestUser(app)
        val config: SyncConfiguration = SyncConfiguration.defaultConfig(user)
        assertTrue(config == config)
    }

    @Test
    fun equals_same() {
        val user: User = createTestUser(app)
        val config1: SyncConfiguration = SyncConfiguration.Builder(user).build()
        val config2: SyncConfiguration = SyncConfiguration.Builder(user).build()
        assertTrue(config1 == config2)
    }

    @Test
    fun equals_not() {
        val user1: User = createTestUser(app)
        val user2: User = createTestUser(app)
        val config1: SyncConfiguration = SyncConfiguration.Builder(user1).build()
        val config2: SyncConfiguration = SyncConfiguration.Builder(user2).build()
        assertFalse(config1 == config2)
    }

    @Test
    fun hashCode_equal() {
        val user: User = createTestUser(app)
        val config: SyncConfiguration = SyncConfiguration.defaultConfig(user)
        assertEquals(config.hashCode(), config.hashCode())
    }

    @Test
    fun hashCode_notEquals() {
        val user1: User = createTestUser(app)
        val user2: User = createTestUser(app)
        val config1: SyncConfiguration = SyncConfiguration.defaultConfig(user1)
        val config2: SyncConfiguration = SyncConfiguration.defaultConfig(user2)
        assertNotEquals(config1.hashCode(), config2.hashCode())
    }

    @Test
    fun get_syncSpecificValues() {
        val user: User = createTestUser(app)
        val config: SyncConfiguration = SyncConfiguration.defaultConfig(user)
        assertTrue(user == config.user)
        assertEquals("ws://127.0.0.1:9090/", config.serverUrl.toString()) // FIXME: Figure out exactly what to return here
        assertFalse(config.shouldDeleteRealmOnLogout())
        assertTrue(config.isSyncConfiguration)
        assertTrue(config.isFlexibleSyncConfiguration())
    }

    @Test
    fun toString_nonEmpty() {
        val user: User = createTestUser(app)
        val config: SyncConfiguration = SyncConfiguration.defaultConfig(user)
        assertTrue(config.isFlexibleSyncConfiguration)
        assertFalse(config.isPartitionBasedSyncConfiguration)
    }

    @Test
    fun getPartitionValueThrows() {
        val user: User = createTestUser(app)
        val config: SyncConfiguration = SyncConfiguration.defaultConfig(user)
        assertFailsWith<IllegalStateException> { config.partitionValue }
    }

    @Test
    fun defaultPath() {
        val user: User = createTestUser(app)
        val config: SyncConfiguration = SyncConfiguration.defaultConfig(user)
        assertTrue("Path is: ${config.path}", config.path.endsWith("/default.realm"))
    }

    @Test
    fun initialSubscriptions() {
        val user: User = createTestUser(app)
        val handler = SyncConfiguration.InitialFlexibleSyncSubscriptions { realm, subscriptions ->
            // Do nothing
        }
        val config: SyncConfiguration = SyncConfiguration.Builder(user)
            .initialSubscriptions(handler)
            .build()

        assertEquals(handler, config.initialSubscriptionsHandler)
    }

    @Test
    fun defaultClientResetStrategy() {
        val user: User = createTestUser(app)
        val handler = SyncConfiguration.InitialFlexibleSyncSubscriptions { realm, subscriptions ->
            // Do nothing
        }
        val config: SyncConfiguration = SyncConfiguration.defaultConfig(user)
        assertTrue(config.syncClientResetStrategy is ManuallyRecoverUnsyncedChangesStrategy)
    }
}
