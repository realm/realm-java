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
import io.realm.entities.StringOnly
import io.realm.entities.StringOnlyModule
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import io.realm.rule.RunInLooperThread
import io.realm.util.expectException
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.io.File
import java.lang.IllegalArgumentException

@RunWith(AndroidJUnit4::class)
class SyncConfigurationTests {

    companion object {
        // FIXME Partition key must be Extended Json formatted. We need to fix the public API's
        // to automatically format these
        private const val DEFAULT_PARTITION = "\"default\""
    }

    @get:Rule
    val configFactory = TestSyncConfigurationFactory()

    @get:Rule
    val looperThread = RunInLooperThread()

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var app: TestRealmApp

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        app = TestRealmApp()
    }

    @After
    fun tearDown() {
        app.close()
    }

    @Test
    fun errorHandler() {
        val builder: SyncConfiguration.Builder = SyncConfiguration.Builder(createTestUser(app), DEFAULT_PARTITION)
        val errorHandler: SyncSession.ErrorHandler = object : SyncSession.ErrorHandler {
            override fun onError(session: SyncSession, error: ObjectServerError) {}
        }
        val config = builder.errorHandler(errorHandler).build()
        Assert.assertEquals(errorHandler, config.errorHandler)
    }

    @Test
    fun errorHandler_fromSyncManager() {
        val user: RealmUser = createTestUser(app)
        val config: SyncConfiguration = SyncConfiguration.defaultConfig(user, DEFAULT_PARTITION)
        Assert.assertEquals(app.configuration.defaultErrorHandler, config.errorHandler)
    }

    @Test
    fun errorHandler_nullThrows() {
        val user: RealmUser = createTestUser(app)
        val builder = SyncConfiguration.Builder(user, DEFAULT_PARTITION)
        expectException<java.lang.IllegalArgumentException> { builder.errorHandler(TestHelper.getNull())  }
    }

    @Test
    fun equals() {
        val user: RealmUser = createTestUser(app)
        val config: SyncConfiguration = SyncConfiguration.defaultConfig(user, DEFAULT_PARTITION)
        Assert.assertTrue(config == config)
    }

    @Test
    fun equals_same() {
        val user: RealmUser = createTestUser(app)
        val config1: SyncConfiguration = SyncConfiguration.Builder(user, DEFAULT_PARTITION).build()
        val config2: SyncConfiguration = SyncConfiguration.Builder(user, DEFAULT_PARTITION).build()
        Assert.assertTrue(config1 == config2)
    }

    @Test
    fun equals_not() {
        val user1: RealmUser = createTestUser(app)
        val user2: RealmUser = createTestUser(app)
        val config1: SyncConfiguration = SyncConfiguration.Builder(user1, DEFAULT_PARTITION).build()
        val config2: SyncConfiguration = SyncConfiguration.Builder(user2, DEFAULT_PARTITION).build()
        Assert.assertFalse(config1 == config2)
    }

    @Test
    fun hashCode_equal() {
        val user: RealmUser = createTestUser(app)
        val config: SyncConfiguration = SyncConfiguration.defaultConfig(user, DEFAULT_PARTITION)
        Assert.assertEquals(config.hashCode(), config.hashCode())
    }

    @Test
    fun hashCode_notEquals() {
        val user1: RealmUser = createTestUser(app)
        val user2: RealmUser = createTestUser(app)
        val config1: SyncConfiguration = SyncConfiguration.defaultConfig(user1, DEFAULT_PARTITION)
        val config2: SyncConfiguration = SyncConfiguration.defaultConfig(user2, DEFAULT_PARTITION)
        Assert.assertNotEquals(config1.hashCode(), config2.hashCode())
    }

    @Test
    fun get_syncSpecificValues() {
        val user: RealmUser = createTestUser(app)
        val config: SyncConfiguration = SyncConfiguration.defaultConfig(user, DEFAULT_PARTITION)
        Assert.assertTrue(user == config.user)
        Assert.assertEquals("ws://127.0.0.1:9090/", config.serverUrl.toString()) // FIXME: Figure out exactly what to return here
        Assert.assertFalse(config.shouldDeleteRealmOnLogout())
        Assert.assertTrue(config.isSyncConfiguration)
    }

    @Test
    fun encryption() {
        val user: RealmUser = createTestUser(app)
        val config: SyncConfiguration = SyncConfiguration.Builder(user, DEFAULT_PARTITION)
                .encryptionKey(TestHelper.getRandomKey())
                .build()
        Assert.assertNotNull(config.encryptionKey)
    }

    @Test
    fun encryption_invalid_null() {
        val user: RealmUser = createTestUser(app)
        val builder = SyncConfiguration.Builder(user, DEFAULT_PARTITION)
        expectException<IllegalArgumentException> { builder.encryptionKey(TestHelper.getNull()) }
    }

    fun encryption_invalid_wrong_length() {
        val user: RealmUser = createTestUser(app)
        val builder = SyncConfiguration.Builder(user, DEFAULT_PARTITION)
        expectException<IllegalArgumentException> { builder.encryptionKey(byteArrayOf(1, 2, 3)) }
    }

    @Test
    fun initialData() {
        val user: RealmUser = createTestUser(app)
        val config = configFactory.createSyncConfigurationBuilder(user)
                .schema(StringOnly::class.java)
                .initialData(object : Realm.Transaction {
                    override fun execute(realm: Realm) {
                        val stringOnly: StringOnly = realm.createObject<StringOnly>()
                        stringOnly.setChars("TEST 42")
                    }
                })
                .build()
        Assert.assertNotNull(config.initialDataTransaction)

        // open the first time - initialData must be triggered
        val realm1: Realm = Realm.getInstance(config)
        val results: RealmResults<StringOnly> = realm1.where<StringOnly>().findAll()
        assertEquals(1, results.size)
        assertEquals("TEST 42", results.first()!!.getChars())
        realm1.close()

        // open the second time - initialData must not be triggered
        val realm2: Realm = Realm.getInstance(config)
        assertEquals(1, realm2.where<StringOnly>().count())
        realm2.close()
    }

    @Test
    fun defaultRxFactory() {
        val user: RealmUser = createTestUser(app)
        val config: SyncConfiguration = SyncConfiguration.defaultConfig(user, DEFAULT_PARTITION)
        Assert.assertNotNull(config.rxFactory)
    }

    @Test
    fun toString_nonEmpty() {
        val user: RealmUser = createTestUser(app)
        val config: SyncConfiguration = SyncConfiguration.defaultConfig(user, DEFAULT_PARTITION)
        val configStr = config.toString()
        assertTrue(configStr.isNotEmpty())
    }

    // Check that it is possible for multiple users to reference the same Realm URL while each user still use their
    // own copy on the filesystem. This is e.g. what happens if a Realm is shared using a PermissionOffer.
    @Test
    @Ignore("FIXME: Enable this once Sync is working.")
    fun multipleUsersReferenceSameRealm() {
        val user1: RealmUser = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        val user2: RealmUser = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        val config1: SyncConfiguration = SyncConfiguration.Builder(user1, DEFAULT_PARTITION)
                .modules(StringOnlyModule())
                .build()
        val realm1: Realm = Realm.getInstance(config1)
        val config2: SyncConfiguration = SyncConfiguration.Builder(user2, DEFAULT_PARTITION)
                .modules(StringOnlyModule())
                .build()
        var realm2: Realm? = null

        // Verify that two different configurations can be used for the same URL
        realm2 = try {
            Realm.getInstance(config1)
        } finally {
            realm1.close()
            if (realm2 != null) {
                realm2.close()
            }
        }

        // Verify that we actually save two different files
        Assert.assertNotEquals(config1.path, config2.path)
    }
    @Test
    fun defaultConfiguration_throwsIfNotLoggedIn() {
        val user: RealmUser = createTestUser(app)
        user.osUser.invalidate()
        expectException<IllegalArgumentException> { SyncConfiguration.defaultConfig(user, DEFAULT_PARTITION) }
    }

    @Test
    fun clientResyncMode() {
        val user: RealmUser = createTestUser(app)

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
        val user: RealmUser = createTestUser(app)
        val config: SyncConfiguration.Builder = SyncConfiguration.Builder(user, DEFAULT_PARTITION)
        try {
            config.clientResyncMode(TestHelper.getNull())
            Assert.fail()
        } catch (ignore: IllegalArgumentException) {
        }
    }
}
