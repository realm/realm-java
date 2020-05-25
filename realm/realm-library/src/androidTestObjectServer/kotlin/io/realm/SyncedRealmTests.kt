/*
 * Copyright 2020 Realm Inc.
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
package io.realm

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.SyncTestUtils.Companion.createTestUser
import io.realm.entities.AllJavaTypes
import io.realm.entities.AllTypes
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException

/**
 * Testing sync specific methods on [Realm].
 */
@RunWith(AndroidJUnit4::class)
class SyncedRealmTests {
    @get:Rule
    val configFactory = TestSyncConfigurationFactory()

    private lateinit var app: RealmApp

    @Before
    fun setUp() {
        app = TestRealmApp()
    }

    @After
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
    }

    @Test
    @Ignore("Flaky, seems like Realm.compactRealm(config) sometimes returns false")
    fun compactRealm_populatedRealm() {
        val config = configFactory.createSyncConfigurationBuilder(createTestUser(app)).build()
        Realm.getInstance(config).use { realm ->
            realm.executeTransaction { r: Realm ->
                for (i in 0..9) {
                    r.insert(AllJavaTypes(i.toLong()))
                }
            }
        }
        assertTrue(Realm.compactRealm(config))

        Realm.getInstance(config).use { realm ->
            assertEquals(10, realm.where(AllJavaTypes::class.java).count())
        }
    }

    @Test
    fun compactOnLaunch_shouldCompact() {
        val user = createTestUser(app)

        // Fill Realm with data and record size
        val config1 = configFactory.createSyncConfigurationBuilder(user).build()
        var originalSize : Long? = null
        Realm.getInstance(config1).use { realm ->
            val oneMBData = ByteArray(1024 * 1024)
            realm.executeTransaction {
                for (i in 0..9) {
                    realm.createObject(AllTypes::class.java).columnBinary = oneMBData
                }
            }
            originalSize = File(realm.path).length()
        }

        // Open Realm with CompactOnLaunch
        val config2 = configFactory.createSyncConfigurationBuilder(user)
                .compactOnLaunch { totalBytes, usedBytes -> true }
                .build()
        Realm.getInstance(config2).use { realm ->
            val compactedSize = File(realm.path).length()
            assertTrue(originalSize!! > compactedSize)
        }
    }
}
