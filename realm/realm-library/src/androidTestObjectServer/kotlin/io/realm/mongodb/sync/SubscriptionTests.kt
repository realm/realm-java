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
import io.realm.entities.SyncColor
import io.realm.mongodb.close
import io.realm.mongodb.registerUserAndLogin
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import io.realm.kotlin.where
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import java.lang.IllegalArgumentException
import kotlin.test.assertFailsWith

/**
 * Class wrapping tests for SubscriptionSets
 */
@RunWith(AndroidJUnit4::class)
class SubscriptionTests {

    @get:Rule
    val configFactory = TestSyncConfigurationFactory()

    private lateinit var app: TestApp
    private lateinit var realm: Realm

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        app = TestApp(appName = TEST_APP_3)
        val user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        val config = configFactory.createFlexibleSyncConfigurationBuilder(user)
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
    fun unmanagedSubscription() {
        var sub = Subscription.create("sub", realm.where<SyncColor>())
        assertEquals("sub", sub.name)
        assertEquals("SyncColor", sub.objectType)
        assertEquals("TRUEPREDICATE ", sub.query)
        assertNull(sub.createdAt)
        assertNull(sub.updatedAt)

        sub = Subscription.create(realm.where<SyncColor>())
        assertNull(sub.name)
        assertEquals("SyncColor", sub.objectType)
        assertEquals("TRUEPREDICATE ", sub.query)
        assertNull(sub.createdAt)
        assertNull(sub.updatedAt)
    }

    @Test
    fun create_emptyNameThrows() {
        val query = realm.where<SyncColor>()
        assertFailsWith<IllegalArgumentException> {
            Subscription.create("", query)
        }
    }

}