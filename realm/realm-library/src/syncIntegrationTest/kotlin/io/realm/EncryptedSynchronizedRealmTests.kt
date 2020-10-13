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

import androidx.test.platform.app.InstrumentationRegistry
import io.realm.entities.SyncStringOnly
import io.realm.exceptions.RealmFileException
import io.realm.kotlin.syncSession
import io.realm.mongodb.App
import io.realm.mongodb.Credentials
import io.realm.mongodb.close
import io.realm.mongodb.registerUserAndLogin
import io.realm.mongodb.sync.SyncConfiguration
import io.realm.mongodb.sync.testSchema
import org.bson.BsonString
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.util.*
import kotlin.test.assertFailsWith

private val SECRET_PASSWORD = "123456"

class EncryptedSynchronizedRealmTests {

    private lateinit var app: App

    private val configurationFactory: TestSyncConfigurationFactory = TestSyncConfigurationFactory()

    @Before
    fun setup() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        app = TestApp()
    }

    @After
    fun teardown() {
        if (this::app.isInitialized) {
            app.close()
        }
    }

    // Make sure the encryption is local, i.e after deleting a synced Realm
    // re-open it again with no (or different) key, should be possible.
    @Test
    fun setEncryptionKey_canReOpenRealmWithoutKey() {

        // STEP 1: open a synced Realm using a local encryption key
        val user = app.registerUserAndLogin(TestHelper.getRandomEmail(), SECRET_PASSWORD)
        val randomKey = TestHelper.getRandomKey()
        val configWithEncryption: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, BsonString(UUID.randomUUID().toString()))
                .testSchema(SyncStringOnly::class.java)
                .waitForInitialRemoteData()
                .errorHandler { _, error -> fail(error.errorMessage) }
                .encryptionKey(randomKey)
                .build()

        Realm.getInstance(configWithEncryption).use { realm ->
            assertTrue(realm.isEmpty)
            realm.executeTransaction {
                realm.createObject(SyncStringOnly::class.java, ObjectId()).chars = "Hi Alice"
            }

            // STEP 2:  make sure the changes gets to the server
            realm.syncSession.uploadAllLocalChanges()
        }
        user.logOut()

        // STEP 3: try to open again the same sync Realm but different local name without the encryption key should not
        // fail
        val user2 = app.registerUserAndLogin(TestHelper.getRandomEmail(), SECRET_PASSWORD)
        val configWithoutEncryption: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user2, configWithEncryption.partitionValue)
                // Using different user with same partition value to trigger a different path instead of
                // .name("newName")
                .testSchema(SyncStringOnly::class.java)
                .waitForInitialRemoteData()
                .errorHandler { _, error -> fail(error.errorMessage) }
                .build()

        Realm.getInstance(configWithoutEncryption).use { realm ->
            val all = realm.where(SyncStringOnly::class.java).findAll()
            assertEquals(1, all.size.toLong())
            assertEquals("Hi Alice", all[0]!!.chars)
        }
        user.logOut()
    }

    // FIXME: ignore until https://github.com/realm/realm-java/issues/7028 is fixed
    // If an encrypted synced Realm is re-opened with the wrong key, throw an exception.
    @Test
    @Ignore("Crashes at random - https://github.com/realm/realm-java/issues/7028")
    fun setEncryptionKey_shouldCrashIfKeyNotProvided() {
        // STEP 1: open a synced Realm using a local encryption key
        var user = app.registerUserAndLogin(TestHelper.getRandomEmail(), SECRET_PASSWORD)
        val randomKey = TestHelper.getRandomKey()
        val configWithEncryption: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, BsonString(UUID.randomUUID().toString()))
                .testSchema(SyncStringOnly::class.java)
                .waitForInitialRemoteData()
                .errorHandler { session, error -> fail(error.getErrorMessage()) }
                .encryptionKey(randomKey)
                .build()

        Realm.getInstance(configWithEncryption).use { realm ->
            assertTrue(realm.isEmpty)
            realm.executeTransaction {
                realm.createObject(SyncStringOnly::class.java, ObjectId()).chars = "Hi Alice"
            }
            // STEP 2: Close the Realm and log the user out to forget about it.
        }
        user.logOut()

        // STEP 3: try to open again the Realm without the encryption key should fail
        user = app.login(Credentials.emailPassword(user.profile.email, SECRET_PASSWORD))
        val configWithoutEncryption: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, configWithEncryption.partitionValue)
                .testSchema(SyncStringOnly::class.java)
                .waitForInitialRemoteData()
                .errorHandler { session, error -> fail(error.getErrorMessage()) }
                .build()

        assertFailsWith<RealmFileException> {
            Realm.getInstance(configWithoutEncryption).close()
        }
    }

    // If client B encrypts its synced Realm, client A should be able to access that Realm with a different encryption key.
    @Test
    fun setEncryptionKey_differentClientsWithDifferentKeys() {
        // STEP 1: prepare a synced Realm for client A
        val user = app.registerUserAndLogin(TestHelper.getRandomEmail(), SECRET_PASSWORD)
        val randomKey = TestHelper.getRandomKey()
        val configWithEncryption: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, BsonString(UUID.randomUUID().toString()))
                .testSchema(SyncStringOnly::class.java)
                .waitForInitialRemoteData()
                .errorHandler { session, error -> fail(error.getErrorMessage()) }
                .encryptionKey(randomKey)
                .build()

        Realm.getInstance(configWithEncryption).use { realm ->
            assertTrue(realm.isEmpty)
            realm.executeTransaction {
                realm.createObject(SyncStringOnly::class.java, ObjectId()).chars = "Hi Alice"
            }
            // STEP 2: make sure the changes gets to the server
            realm.syncSession.uploadAllLocalChanges()
        }

        // STEP 3: prepare a synced Realm for client B
        val user2 = app.registerUserAndLogin(TestHelper.getRandomEmail(), SECRET_PASSWORD)
        val key2 = TestHelper.getRandomKey()
        val configWithEncryption2: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user2, configWithEncryption.partitionValue)
                .testSchema(SyncStringOnly::class.java)
                .waitForInitialRemoteData()
                .errorHandler { session, error -> fail(error.getErrorMessage()) }
                .encryptionKey(key2)
                .build()

        Realm.getInstance(configWithEncryption2).use { realm ->
            val all = realm.where(SyncStringOnly::class.java).findAll()
            assertEquals(1, all.size.toLong())
            assertEquals("Hi Alice", all[0]!!.chars)
            realm.executeTransaction {
                realm.createObject(SyncStringOnly::class.java, ObjectId()).chars = "Hi Bob"
            }
            realm.syncSession.uploadAllLocalChanges()
        }

        // STEP 4: client A can see changes from client B (although they're using different encryption keys)
        Realm.getInstance(configWithEncryption).use { realm ->
            realm.syncSession.downloadAllServerChanges() // force download latest commits from remote realm
            realm.refresh() // Not calling refresh will still point to the previous version of the Realm without the latest admin commit  "Hi Bob"
            assertEquals(2, realm.where(SyncStringOnly::class.java).count())
            val allSorted = realm.where(SyncStringOnly::class.java).sort(SyncStringOnly.FIELD_CHARS).findAll()
            val allSortedAdmin = realm.where(SyncStringOnly::class.java).sort(SyncStringOnly.FIELD_CHARS).findAll()
            assertEquals("Hi Alice", allSorted[0]!!.chars)
            assertEquals("Hi Bob", allSorted[1]!!.chars)
            assertEquals("Hi Alice", allSortedAdmin[0]!!.chars)
            assertEquals("Hi Bob", allSortedAdmin[1]!!.chars)
        }

        user.logOut()
        user2.logOut()
    }
}
