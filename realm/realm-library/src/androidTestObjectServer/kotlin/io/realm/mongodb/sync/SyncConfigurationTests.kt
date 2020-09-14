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
package io.realm.mongodb.sync

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.*
import io.realm.entities.StringOnly
import io.realm.entities.StringOnlyModule
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import io.realm.mongodb.AppException
import io.realm.mongodb.SyncTestUtils.Companion.createTestUser
import io.realm.mongodb.User
import io.realm.mongodb.close
import io.realm.mongodb.registerUserAndLogin
import org.bson.BsonString
import org.bson.types.ObjectId
import org.junit.*
import org.junit.runner.RunWith
import kotlin.test.*

@RunWith(AndroidJUnit4::class)
class SyncConfigurationTests {

    companion object {
        private const val DEFAULT_PARTITION = "default"
    }

    @get:Rule
    val configFactory = TestSyncConfigurationFactory()

    private lateinit var app: TestApp

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        app = TestApp()
    }

    @After
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
    }

    @Test
    fun errorHandler() {
        val builder: SyncConfiguration.Builder = SyncConfiguration.Builder(createTestUser(app), DEFAULT_PARTITION)
        val errorHandler: SyncSession.ErrorHandler = object : SyncSession.ErrorHandler {
            override fun onError(session: SyncSession, error: AppException) {}
        }
        val config = builder.errorHandler(errorHandler).build()
        assertEquals(errorHandler, config.errorHandler)
    }

    @Test
    fun errorHandler_fromAppConfiguration() {
        val user: User = createTestUser(app)
        val config: SyncConfiguration = SyncConfiguration.defaultConfig(user, DEFAULT_PARTITION)
        assertEquals(app.configuration.defaultErrorHandler, config.errorHandler)
    }

    @Test
    fun errorHandler_nullThrows() {
        val user: User = createTestUser(app)
        val builder = SyncConfiguration.Builder(user, DEFAULT_PARTITION)
        assertFailsWith<IllegalArgumentException> { builder.errorHandler(TestHelper.getNull())  }
    }

    @Test
    fun clientResetHandler() {
        val builder: SyncConfiguration.Builder = SyncConfiguration.Builder(createTestUser(app), DEFAULT_PARTITION)
        val handler = object : SyncSession.ClientResetHandler {
            override fun onClientReset(session: SyncSession, error: ClientResetRequiredError) {}
        }
        val config = builder.clientResetHandler(handler).build()
        assertEquals(handler, config.clientResetHandler)
    }

    @Test
    fun clientResetHandler_fromAppConfiguration() {
        val user: User = createTestUser(app)
        val config: SyncConfiguration = SyncConfiguration.defaultConfig(user, DEFAULT_PARTITION)
        assertEquals(app.configuration.defaultClientResetHandler, config.clientResetHandler)
    }

    @Test
    fun clientResetHandler_nullThrows() {
        val user: User = createTestUser(app)
        val builder = SyncConfiguration.Builder(user, DEFAULT_PARTITION)
        assertFailsWith<IllegalArgumentException> { builder.clientResetHandler(TestHelper.getNull())  }
    }

    @Test
    fun equals() {
        val user: User = createTestUser(app)
        val config: SyncConfiguration = SyncConfiguration.defaultConfig(user, DEFAULT_PARTITION)
        assertTrue(config == config)
    }

    @Test
    fun equals_same() {
        val user: User = createTestUser(app)
        val config1: SyncConfiguration = SyncConfiguration.Builder(user, DEFAULT_PARTITION).build()
        val config2: SyncConfiguration = SyncConfiguration.Builder(user, DEFAULT_PARTITION).build()
        assertTrue(config1 == config2)
    }

    @Test
    // FIXME Tests are not exhaustive
    fun equals_not() {
        val user1: User = createTestUser(app)
        val user2: User = createTestUser(app)
        val config1: SyncConfiguration = SyncConfiguration.Builder(user1, DEFAULT_PARTITION).build()
        val config2: SyncConfiguration = SyncConfiguration.Builder(user2, DEFAULT_PARTITION).build()
        assertFalse(config1 == config2)
    }

    @Test
    fun hashCode_equal() {
        val user: User = createTestUser(app)
        val config: SyncConfiguration = SyncConfiguration.defaultConfig(user, DEFAULT_PARTITION)
        assertEquals(config.hashCode(), config.hashCode())
    }

    @Test
    fun hashCode_notEquals() {
        val user1: User = createTestUser(app)
        val user2: User = createTestUser(app)
        val config1: SyncConfiguration = SyncConfiguration.defaultConfig(user1, DEFAULT_PARTITION)
        val config2: SyncConfiguration = SyncConfiguration.defaultConfig(user2, DEFAULT_PARTITION)
        assertNotEquals(config1.hashCode(), config2.hashCode())
    }

    @Test
    fun get_syncSpecificValues() {
        val user: User = createTestUser(app)
        val config: SyncConfiguration = SyncConfiguration.defaultConfig(user, DEFAULT_PARTITION)
        assertTrue(user == config.user)
        assertEquals("ws://127.0.0.1:9090/", config.serverUrl.toString()) // FIXME: Figure out exactly what to return here
        assertFalse(config.shouldDeleteRealmOnLogout())
        assertTrue(config.isSyncConfiguration)
    }

    @Test
    fun name() {
        val user: User = createTestUser(app)
        val filename = "my-file-name.realm"
        val config: SyncConfiguration = SyncConfiguration.Builder(user, DEFAULT_PARTITION)
                .name(filename)
                .build()
        val suffix = "/mongodb-realm/${user.app.configuration.appId}/${user.id}/$filename"
        assertTrue(config.path.endsWith(suffix))
    }

    @Test
    fun name_illegalValuesThrows() {
        val user: User = createTestUser(app)
        val builder = SyncConfiguration.Builder(user, DEFAULT_PARTITION)

        assertFailsWith<IllegalArgumentException> { builder.name(TestHelper.getNull()) }
        assertFailsWith<IllegalArgumentException> { builder.name(".realm") }
    }

    @Test
    fun encryption() {
        val user: User = createTestUser(app)
        val config: SyncConfiguration = SyncConfiguration.Builder(user, DEFAULT_PARTITION)
                .encryptionKey(TestHelper.getRandomKey())
                .build()
        assertNotNull(config.encryptionKey)
    }

    @Test
    fun encryption_invalid_null() {
        val user: User = createTestUser(app)
        val builder = SyncConfiguration.Builder(user, DEFAULT_PARTITION)
        assertFailsWith<IllegalArgumentException> { builder.encryptionKey(TestHelper.getNull()) }
    }

    @Test
    fun encryption_invalid_wrong_length() {
        val user: User = createTestUser(app)
        val builder = SyncConfiguration.Builder(user, DEFAULT_PARTITION)
        assertFailsWith<IllegalArgumentException> { builder.encryptionKey(byteArrayOf(1, 2, 3)) }
    }

    @Test
    fun initialData() {
        val user: User = createTestUser(app)
        val config = configFactory.createSyncConfigurationBuilder(user)
                .schema(StringOnly::class.java)
                .initialData(object : Realm.Transaction {
                    override fun execute(realm: Realm) {
                        val stringOnly: StringOnly = realm.createObject<StringOnly>()
                        stringOnly.setChars("TEST 42")
                    }
                })
                .build()
        assertNotNull(config.initialDataTransaction)

        // open the first time - initialData must be triggered
        Realm.getInstance(config).use { realm ->
            val results: RealmResults<StringOnly> = realm.where<StringOnly>().findAll()
            assertEquals(1, results.size)
            assertEquals("TEST 42", results.first()!!.getChars())
        }

        // open the second time - initialData must not be triggered
        Realm.getInstance(config).use { realm ->
            assertEquals(1, realm.where<StringOnly>().count())
        }
    }

    @Test
    fun defaultRxFactory() {
        val user: User = createTestUser(app)
        val config: SyncConfiguration = SyncConfiguration.defaultConfig(user, DEFAULT_PARTITION)
        assertNotNull(config.rxFactory)
    }

    @Test
    fun toString_nonEmpty() {
        val user: User = createTestUser(app)
        val config: SyncConfiguration = SyncConfiguration.defaultConfig(user, DEFAULT_PARTITION)
        val configStr = config.toString()
        assertTrue(configStr.isNotEmpty())
    }

    // Check that it is possible for multiple users to reference the same Realm URL while each user still use their
    // own copy on the filesystem. This is e.g. what happens if a Realm is shared using a PermissionOffer.
    @Test
    fun multipleUsersReferenceSameRealm() {
        val user1: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        val user2: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")

        val config1: SyncConfiguration = SyncConfiguration.Builder(user1, DEFAULT_PARTITION)
                .modules(StringOnlyModule())
                .build()
        val config2: SyncConfiguration = SyncConfiguration.Builder(user2, DEFAULT_PARTITION)
                .modules(StringOnlyModule())
                .build()

        // Verify that two different configurations can be used for the same URL
        val realm1: Realm = Realm.getInstance(config1)
        val realm2: Realm = Realm.getInstance(config2)
        assertNotEquals(realm1, realm2)

        realm1.close()
        realm2.close()

        // Verify that we actually save two different files
        assertNotEquals(config1.path, config2.path)
    }

    @Test
    fun defaultConfiguration_throwsIfNotLoggedIn() {
        // TODO Maybe we could avoid registering a real user
        val user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        user.logOut()
        assertFailsWith<IllegalArgumentException> { SyncConfiguration.defaultConfig(user, DEFAULT_PARTITION) }
    }

    @Test
    @Ignore("Not implemented yet")
    fun shouldWaitForInitialRemoteData() { }

    @Test
    @Ignore("Not implemented yet")
    fun getInitialRemoteDataTimeout() { }

    @Test
    @Ignore("Not implemented yet")
    fun getSessionStopPolicy () { }

    @Test
    @Ignore("Not implemented yet")
    fun getUrlPrefix () { }

    @Test
    fun getPartitionValue () {
        val user: User = createTestUser(app)
        val config: SyncConfiguration = SyncConfiguration.defaultConfig(user, DEFAULT_PARTITION)
        assertEquals(BsonString(DEFAULT_PARTITION), config.partitionValue)
    }

    @Test
    fun clientResyncMode() {
        val user: User = createTestUser(app)

        // Default mode for full Realms
        var config: SyncConfiguration = SyncConfiguration.defaultConfig(user, DEFAULT_PARTITION)
        assertEquals(ClientResyncMode.MANUAL, config.clientResyncMode)

        // Manually set the mode
        config = SyncConfiguration.Builder(user, DEFAULT_PARTITION)
                .clientResyncMode(ClientResyncMode.RECOVER_LOCAL_REALM)
                .build()
        assertEquals(ClientResyncMode.RECOVER_LOCAL_REALM, config.clientResyncMode)
    }

    @Test
    fun clientResyncMode_throwsOnNull() {
        val user: User = createTestUser(app)
        val config: SyncConfiguration.Builder = SyncConfiguration.Builder(user, DEFAULT_PARTITION)
        try {
            config.clientResyncMode(TestHelper.getNull())
            fail()
        } catch (ignore: IllegalArgumentException) {
        }
    }

    // If the same user create two configurations with different partition values they must
    // resolve to different paths on disk.
    @Test
    fun differentPartitionValuesAreDifferentRealms() {
        val user: User = createTestUser(app)
        val config1 = SyncConfiguration.defaultConfig(user, "realm1")
        val config2 = SyncConfiguration.defaultConfig(user, "realm2")
        assertNotEquals(config1.path, config2.path)

         assertTrue(config1.path.endsWith("${app.configuration.appId}/${user.id}/s_realm1.realm"))
         assertTrue(config2.path.endsWith("${app.configuration.appId}/${user.id}/s_realm2.realm"))

        // Check for https://github.com/realm/realm-java/issues/6882
        val realm1 = Realm.getInstance(config1)
        try {
            val realm2 = Realm.getInstance(config2)
            realm2.close()
        } finally {
            realm1.close()
        }
    }

    @Test
    fun nullPartitionValue() {
        val user: User = createTestUser(app)

        val configs = listOf<SyncConfiguration>(
            SyncConfiguration.defaultConfig(user, null as String?),
            SyncConfiguration.defaultConfig(user, null as Int?),
            SyncConfiguration.defaultConfig(user, null as Long?),
            SyncConfiguration.defaultConfig(user, null as ObjectId?),
            SyncConfiguration.Builder(user, null as String?).build(),
            SyncConfiguration.Builder(user, null as Int?).build(),
            SyncConfiguration.Builder(user, null as Long?).build(),
            SyncConfiguration.Builder(user, null as ObjectId?).build()
        )

        configs.forEach { config ->
            assertTrue(config.path.endsWith("/null.realm"))
        }
    }

    @Test
    fun loggedOutUsersThrows() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        user.logOut()
        assertFailsWith<java.lang.IllegalArgumentException> {
            SyncConfiguration.defaultConfig(user, ObjectId())
        }
        assertFailsWith<java.lang.IllegalArgumentException> {
            SyncConfiguration.defaultConfig(app.currentUser(), ObjectId())
        }
    }

}
