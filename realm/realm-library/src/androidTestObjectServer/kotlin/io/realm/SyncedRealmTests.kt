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
import io.realm.kotlin.syncSession
import io.realm.kotlin.where
import io.realm.entities.*
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File
import java.util.*

/**
 * Testing sync specific methods on [Realm].
 */
@RunWith(AndroidJUnit4::class)
class SyncedRealmTests {

    @get:Rule
    val configFactory = TestSyncConfigurationFactory()

    private lateinit var app: RealmApp
    private lateinit var partitionValue: String

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        RealmLog.setLevel(LogLevel.TRACE)
        app = TestRealmApp()
        partitionValue = UUID.randomUUID().toString()
    }

    @After
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
        RealmLog.setLevel(LogLevel.WARN)
    }

    // Smoke test for Sync. Waiting for working Sync support.
    @Test
    fun connectWithInitialSchema() {
        val user: RealmUser = createNewUser()
        val config = createDefaultConfig(user)
        Realm.getInstance(config).use { realm ->
            with(realm.syncSession) {
                uploadAllLocalChanges()
                downloadAllServerChanges()
            }
            assertTrue(realm.isEmpty)
        }
    }

    // Smoke test for Sync
    @Test
    fun roundTripObjectsNotInServerSchemaObject() {
        // User 1 creates an object an uploads it to MongoDB Realm
        val user1: RealmUser = createNewUser()
        val config1: SyncConfiguration = createCustomConfig(user1, partitionValue)
        Realm.getInstance(config1).use { realm ->
            realm.executeTransaction {
                for (i in 1..10) {
                    it.insert(SyncColor())
                }
            }
            realm.syncSession.uploadAllLocalChanges()
            assertEquals(10, realm.where<SyncColor>().count())
        }

        // User 2 logs and using the same partition key should see the object
        val user2: RealmUser = createNewUser()
        val config2 = createCustomConfig(user2, partitionValue)
        Realm.getInstance(config2).use { realm ->
            realm.syncSession.downloadAllServerChanges()
            realm.refresh()
            assertEquals(10, realm.where<SyncColor>().count())
        }
    }

    // Smoke test for sync
    // Insert different types with no links between them
    @Test
    fun roundTripSimpleObjectsInServerSchema() {
        // User 1 creates an object an uploads it to MongoDB Realm
        val user1: RealmUser = createNewUser()
        val config1: SyncConfiguration = createDefaultConfig(user1, partitionValue)
        Realm.getInstance(config1).use { realm ->
            realm.executeTransaction {
                val person = SyncPerson()
                person.firstName = "Jane"
                person.lastName = "Doe"
                person.age = 42
                realm.insert(person);
                for (i in 0..9) {
                    val dog = SyncDog()
                    dog.name = "Fido $i"
                    it.insert(dog)
                }
            }
            realm.syncSession.uploadAllLocalChanges()
            assertEquals(10, realm.where<SyncDog>().count())
            assertEquals(1, realm.where<SyncPerson>().count())
        }

        // User 2 logs and using the same partition key should see the object
        val user2: RealmUser = createNewUser()
        val config2 = createDefaultConfig(user2, partitionValue)
        Realm.getInstance(config2).use { realm ->
            realm.syncSession.downloadAllServerChanges()
            realm.refresh()
            assertEquals(10, realm.where<SyncDog>().count())
            assertEquals(1, realm.where<SyncPerson>().count())
        }
    }

    // Smoke test for sync
    // Insert objects with links between them
    @Test
    fun roundTripObjectsWithLists() {
        // User 1 creates an object an uploads it to MongoDB Realm
        val user1: RealmUser = createNewUser()
        val config1: SyncConfiguration = createDefaultConfig(user1, partitionValue)
        Realm.getInstance(config1).use { realm ->
            realm.executeTransaction {
                val person = SyncPerson()
                person.firstName = "Jane"
                person.lastName = "Doe"
                person.age = 42
                for (i in 0..9) {
                    val dog = SyncDog()
                    dog.name = "Fido $i"
                    person.dogs.add(dog)
                }
                realm.insert(person)
            }
            realm.syncSession.uploadAllLocalChanges()
            assertEquals(10, realm.where<SyncDog>().count())
            assertEquals(1, realm.where<SyncPerson>().count())
        }

        // User 2 logs and using the same partition key should see the object
        val user2: RealmUser = createNewUser()
        val config2 = createDefaultConfig(user2, partitionValue)
        Realm.getInstance(config2).use { realm ->
            realm.syncSession.downloadAllServerChanges()
            realm.refresh()
            assertEquals(10, realm.where<SyncDog>().count())
            assertEquals(1, realm.where<SyncPerson>().count())
        }
    }

    @Test
    fun session() {
        val user: RealmUser = app.login(RealmCredentials.anonymous())
        Realm.getInstance(createDefaultConfig(user)).use { realm ->
            assertNotNull(realm.syncSession)
            assertEquals(SyncSession.State.ACTIVE, realm.syncSession.state)
            assertEquals(user, realm.syncSession.user)
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

    private fun createDefaultConfig(user: RealmUser, partitionValue: String = defaultPartitionValue): SyncConfiguration {
        return SyncConfiguration.Builder(user, partitionValue)
                .modules(DefaultSyncSchema())
                .build()
    }

    private fun createCustomConfig(user: RealmUser, partitionValue: String = defaultPartitionValue): SyncConfiguration {
        return SyncConfiguration.Builder(user, partitionValue)
                .schema(SyncColor::class.java)
                .build()
    }

    private fun createNewUser(): RealmUser {
        val email = TestHelper.getRandomEmail()
        val password = "123456"
        app.emailPasswordAuth.registerUser(email, password)
        return app.login(RealmCredentials.emailPassword(email, password))
    }

}
