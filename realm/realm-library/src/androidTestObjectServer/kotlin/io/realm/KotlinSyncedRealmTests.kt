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
import io.realm.entities.*
import io.realm.kotlin.syncSession
import io.realm.kotlin.where
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
@Ignore("FIXME This stalls Trying to bypass sync test to see if this is blocking CI")
class KotlinSyncedRealmTests { // FIXME: Rename to SyncedRealmTests once remaining Java tests have been moved

    private lateinit var app: TestRealmApp
    private lateinit var realm: Realm
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
        if (this::realm.isInitialized) {
            realm.close()
        }
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
        realm = Realm.getInstance(config)
        app.syncManager.getSession(config).uploadAllLocalChanges()
        app.syncManager.getSession(config).downloadAllServerChanges()
        assertTrue(realm.isEmpty)
    }

    // Smoke test for Sync
    @Test
    fun roundTripObjectsNotInServerSchemaObject() {
        // User 1 creates an object an uploads it to MongoDB Realm
        val user1: RealmUser = createNewUser()
        val config1: SyncConfiguration = createCustomConfig(user1, partitionValue)
        realm = Realm.getInstance(config1)
        realm.executeTransaction {
            for (i in 1..10) {
                it.insert(SyncColor())
            }
        }
        app.syncManager.getSession(config1).uploadAllLocalChanges()
        assertEquals(10, realm.where<SyncColor>().count())
        realm.close()

        // User 2 logs and using the same partition key should see the object
        val user2: RealmUser = createNewUser()
        val config2 = createCustomConfig(user2, partitionValue)
        realm = Realm.getInstance(config2)
        realm.syncSession.downloadAllServerChanges()
        realm.refresh()
        assertEquals(10, realm.where<SyncColor>().count())
    }

    // Smoke test for sync
    // Insert different types with no links between them
    @Test
    fun roundTripSimpleObjectsInServerSchema() {
        // User 1 creates an object an uploads it to MongoDB Realm
        val user1: RealmUser = createNewUser()
        val config1: SyncConfiguration = createDefaultConfig(user1, partitionValue)
        realm = Realm.getInstance(config1)
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
        realm.close()

        // User 2 logs and using the same partition key should see the object
        val user2: RealmUser = createNewUser()
        val config2 = createDefaultConfig(user2, partitionValue)
        realm = Realm.getInstance(config2)
        realm.syncSession.downloadAllServerChanges()
        realm.refresh()
        assertEquals(10, realm.where<SyncDog>().count())
        assertEquals(1, realm.where<SyncPerson>().count())
    }


    // Smoke test for sync
    // Insert objects with links between them
    @Ignore("Crashes server currently")
    @Test
    fun roundTripObjectsWithLists() {
        // User 1 creates an object an uploads it to MongoDB Realm
        val user1: RealmUser = createNewUser()
        val config1: SyncConfiguration = createDefaultConfig(user1, partitionValue)
        realm = Realm.getInstance(config1)
        realm.executeTransaction {
            val person = SyncPerson()
            person.firstName = "Jane"
            person.lastName = "Doe"
            person.age = 42
            for (i in 0..9) {
                val dog = SyncDog()
                dog.name = "Fido $i"
                it.insert(dog)
                person.dogs.add(dog.id)
            }
            realm.insert(person)
        }
        realm.syncSession.uploadAllLocalChanges()
        assertEquals(10, realm.where<SyncDog>().count())
        assertEquals(1, realm.where<SyncPerson>().count())
        realm.close()

        // User 2 logs and using the same partition key should see the object
        val user2: RealmUser = createNewUser()
        val config2 = createDefaultConfig(user2, partitionValue)
        realm = Realm.getInstance(config2)
        realm.syncSession.downloadAllServerChanges()
        realm.refresh()
        assertEquals(10, realm.where<SyncDog>().count())
        assertEquals(1, realm.where<SyncPerson>().count())
    }

    @Test
    fun session() {
        val user: RealmUser = app.login(RealmCredentials.anonymous())
        realm = Realm.getInstance(createDefaultConfig(user))
        assertNotNull(realm.syncSession)
        assertEquals(SyncSession.State.ACTIVE, realm.syncSession.state)
        assertEquals(user, realm.syncSession.user)
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
