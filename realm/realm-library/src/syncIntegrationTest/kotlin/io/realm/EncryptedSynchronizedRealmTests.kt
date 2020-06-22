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

import io.realm.ObjectServerError
import io.realm.StandardIntegrationTest
import io.realm.SyncConfiguration
import io.realm.SyncManager
import io.realm.SyncSession
import io.realm.SyncTestUtils
import io.realm.SyncUser
import io.realm.entities.StringOnly
import io.realm.exceptions.RealmFileException
import io.realm.objectserver.utils.Constants
import io.realm.objectserver.utils.StringOnlyModule
import io.realm.objectserver.utils.UserFactory
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import java.util.*

class EncryptedSynchronizedRealmTests : StandardIntegrationTest() {
    @Rule
    var globalTimeout = Timeout.seconds(30)

    // Make sure the encryption is local, i.e after deleting a synced Realm
    // re-open it again with no (or different) key, should be possible.
    @Test
    @Throws(InterruptedException::class)
    fun setEncryptionKey_canReOpenRealmWithoutKey() {

        // STEP 1: open a synced Realm using a local encryption key
        val username = UUID.randomUUID().toString()
        val password = "password"
        var user: SyncUser = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL)
        val randomKey = TestHelper.getRandomKey()
        val configWithEncryption: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .modules(StringOnlyModule())
                .waitForInitialRemoteData()
                .errorHandler(object : ErrorHandler() {
                    fun onError(session: SyncSession?, error: ObjectServerError) {
                        Assert.fail(error.getErrorMessage())
                    }
                })
                .encryptionKey(randomKey)
                .build()
        var realm = Realm.getInstance(configWithEncryption)
        Assert.assertTrue(realm.isEmpty)
        realm.beginTransaction()
        realm.createObject(StringOnly::class.java).chars = "Hi Alice"
        realm.commitTransaction()

        // STEP 2:  make sure the changes gets to the server
        SyncManager.getSession(configWithEncryption).uploadAllLocalChanges()
        realm.close()
        user.logOut()

        // STEP 3: try to open again the same sync Realm but different local name without the encryption key should not
        // fail
        user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, false), Constants.AUTH_URL)
        val configWithoutEncryption: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .name("newName")
                .modules(StringOnlyModule())
                .waitForInitialRemoteData()
                .errorHandler(object : ErrorHandler() {
                    fun onError(session: SyncSession?, error: ObjectServerError) {
                        Assert.fail(error.getErrorMessage())
                    }
                })
                .build()
        realm = Realm.getInstance(configWithoutEncryption)
        val all = realm.where(StringOnly::class.java).findAll()
        Assert.assertEquals(1, all.size.toLong())
        Assert.assertEquals("Hi Alice", all[0]!!.chars)
        realm.close()
        user.logOut()
    }

    // If an encrypted synced Realm is re-opened with the wrong key, throw an exception.
    @Test
    @Throws(InterruptedException::class)
    fun setEncryptionKey_shouldCrashIfKeyNotProvided() {
        // STEP 1: open a synced Realm using a local encryption key
        val username = UUID.randomUUID().toString()
        val password = "password"
        var user: SyncUser = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL)
        val randomKey = TestHelper.getRandomKey()
        val configWithEncryption: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .modules(StringOnlyModule())
                .waitForInitialRemoteData()
                .errorHandler(object : ErrorHandler() {
                    fun onError(session: SyncSession?, error: ObjectServerError) {
                        Assert.fail(error.getErrorMessage())
                    }
                })
                .encryptionKey(randomKey)
                .build()
        var realm = Realm.getInstance(configWithEncryption)
        Assert.assertTrue(realm!!.isEmpty)
        realm.beginTransaction()
        realm.createObject(StringOnly::class.java).chars = "Hi Alice"
        realm.commitTransaction()

        // STEP 2: Close the Realm and log the user out to forget about it.
        realm.close()
        user.logOut()

        // STEP 3: try to open again the Realm without the encryption key should fail
        user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, false), Constants.AUTH_URL)
        val configWithoutEncryption: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .modules(StringOnlyModule())
                .waitForInitialRemoteData()
                .errorHandler(object : ErrorHandler() {
                    fun onError(session: SyncSession?, error: ObjectServerError) {
                        Assert.fail(error.getErrorMessage())
                    }
                })
                .build()
        try {
            realm = Realm.getInstance(configWithoutEncryption)
            Assert.fail("It should not be possible to open the Realm without the encryption key set previously.")
        } catch (ignored: RealmFileException) {
        } finally {
            realm?.close()
        }
    }

    // If client B encrypts its synced Realm, client A should be able to access that Realm with a different encryption key.
    @Test
    @Throws(InterruptedException::class)
    fun setEncryptionKey_differentClientsWithDifferentKeys() {
        // STEP 1: prepare a synced Realm for client A
        val username = UUID.randomUUID().toString()
        val password = "password"
        val user: SyncUser = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL)
        val randomKey = TestHelper.getRandomKey()
        val configWithEncryption: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .modules(StringOnlyModule())
                .waitForInitialRemoteData()
                .errorHandler(object : ErrorHandler() {
                    fun onError(session: SyncSession?, error: ObjectServerError) {
                        Assert.fail(error.getErrorMessage())
                    }
                })
                .encryptionKey(randomKey)
                .build()
        var realm = Realm.getInstance(configWithEncryption)
        Assert.assertTrue(realm.isEmpty)
        realm.beginTransaction()
        realm.createObject(StringOnly::class.java).chars = "Hi Alice"
        realm.commitTransaction()

        // STEP 2: make sure the changes gets to the server
        SyncManager.getSession(configWithEncryption).uploadAllLocalChanges()
        realm.close()

        // STEP 3: prepare a synced Realm for client B (admin user)
        val admin: SyncUser = UserFactory.createAdminUser(Constants.AUTH_URL)
        val credentials: SyncCredentials = SyncCredentials.accessToken(SyncTestUtils.getRefreshToken(admin).value(), "custom-admin-user")
        val adminUser: SyncUser = SyncUser.logIn(credentials, Constants.AUTH_URL)
        val adminRandomKey = TestHelper.getRandomKey()
        val adminConfigWithEncryption: SyncConfiguration = configurationFactory.createSyncConfigurationBuilder(adminUser, configWithEncryption.getServerUrl().toString())
                .modules(StringOnlyModule())
                .waitForInitialRemoteData()
                .errorHandler(object : ErrorHandler() {
                    fun onError(session: SyncSession?, error: ObjectServerError) {
                        Assert.fail(error.getErrorMessage())
                    }
                })
                .encryptionKey(adminRandomKey)
                .build()
        var adminRealm = Realm.getInstance(adminConfigWithEncryption)
        val all = adminRealm.where(StringOnly::class.java).findAll()
        Assert.assertEquals(1, all.size.toLong())
        Assert.assertEquals("Hi Alice", all[0]!!.chars)
        adminRealm.beginTransaction()
        adminRealm.createObject(StringOnly::class.java).chars = "Hi Bob"
        adminRealm.commitTransaction()
        SyncManager.getSession(adminConfigWithEncryption).uploadAllLocalChanges()
        adminRealm.close()

        // STEP 4: client A can see changes from client B (although they're using different encryption keys)
        realm = Realm.getInstance(configWithEncryption)
        SyncManager.getSession(configWithEncryption).downloadAllServerChanges() // force download latest commits from ROS
        realm.refresh() // Not calling refresh will still point to the previous version of the Realm without the latest admin commit  "Hi Bob"
        Assert.assertEquals(2, realm.where(StringOnly::class.java).count())
        adminRealm = Realm.getInstance(adminConfigWithEncryption)
        val allSorted = realm.where(StringOnly::class.java).sort(StringOnly.FIELD_CHARS).findAll()
        val allSortedAdmin = adminRealm.where(StringOnly::class.java).sort(StringOnly.FIELD_CHARS).findAll()
        Assert.assertEquals("Hi Alice", allSorted[0]!!.chars)
        Assert.assertEquals("Hi Bob", allSorted[1]!!.chars)
        Assert.assertEquals("Hi Alice", allSortedAdmin[0]!!.chars)
        Assert.assertEquals("Hi Bob", allSortedAdmin[1]!!.chars)
        adminRealm.close()
        adminUser.logOut()
        realm.close()
        user.logOut()
    }
}
