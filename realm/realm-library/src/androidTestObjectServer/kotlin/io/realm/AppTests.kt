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
import io.realm.admin.ServerAdmin
import io.realm.entities.DefaultSyncSchema
import io.realm.exceptions.RealmFileException
import io.realm.kotlin.syncSession
import io.realm.mongodb.*
import io.realm.mongodb.sync.SyncConfiguration
import io.realm.rule.BlockingLooperThread
import org.bson.BsonString
import org.bson.codecs.StringCodec
import org.bson.codecs.configuration.CodecRegistries
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.assertFailsWith

@RunWith(AndroidJUnit4::class)
class AppTests {

    @get:Rule
    val configFactory = TestSyncConfigurationFactory()

    private val looperThread = BlockingLooperThread()
    private lateinit var app: TestApp
    private lateinit var admin: ServerAdmin

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        app = TestApp()
        admin = ServerAdmin(app)
    }

    @After
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
    }

    @Test
    fun login() {
        val creds = Credentials.anonymous()
        var user = app.login(creds)
        assertNotNull(user)
    }

    @Test
    fun login_invalidUserThrows() {
        val credentials = Credentials.emailPassword("foo", "bar")
        try {
            app.login(credentials)
            fail()
        } catch(ex: AppException) {
            assertEquals(ErrorCode.SERVICE_UNKNOWN, ex.errorCode)
        }
    }

    @Test
    fun login_invalidArgsThrows() {
        assertFailsWith<IllegalArgumentException> { app.login(TestHelper.getNull()) }
    }

    @Test
    fun loginAsync() = looperThread.runBlocking {
        app.loginAsync(Credentials.anonymous()) { result ->
            assertNotNull(result.orThrow)
            looperThread.testComplete()
        }
    }

    @Test
    fun loginAsync_invalidUserThrows() = looperThread.runBlocking {
        app.loginAsync(Credentials.emailPassword("foo", "bar")) { result ->
            assertFalse(result.isSuccess)
            assertEquals(ErrorCode.SERVICE_UNKNOWN, result.error.errorCode)
            looperThread.testComplete()
        }
    }

    @Test
    fun loginAsync_throwsOnNonLooperThread() {
        try {
            app.loginAsync(Credentials.anonymous()) { fail() }
            fail()
        } catch (ignore: IllegalStateException) {
        }
    }

    @Test
    fun currentUser() {
        assertNull(app.currentUser())
        val user: User = app.login(Credentials.anonymous())
        assertEquals(user, app.currentUser())
        user.logOut()
        assertNull(app.currentUser())
    }

    @Test
    fun allUsers() {
        assertEquals(0, app.allUsers().size)
        val user1 = app.login(Credentials.anonymous())
        var allUsers = app.allUsers()
        assertEquals(1, allUsers.size)
        assertTrue(allUsers.containsKey(user1.id))
        assertEquals(user1, allUsers[user1.id])

        // Only 1 anonymous user exists, so logging in again just returns the old one
        val user2 = app.login(Credentials.anonymous())
        allUsers = app.allUsers()
        assertEquals(1, allUsers.size)
        assertTrue(allUsers.containsKey(user2.id))

        val user3: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        allUsers = app.allUsers()
        assertEquals(2, allUsers.size)
        assertTrue(allUsers.containsKey(user3.id))

        // Logging out users that registered with email/password will just put them in LOGGED_OUT state
        user3.logOut();
        allUsers = app.allUsers()
        assertEquals(2, allUsers.size)
        assertTrue(allUsers.containsKey(user3.id))
        assertEquals(User.State.LOGGED_OUT, allUsers[user3.id]!!.state)

        // Logging out anonymous users will remove them completely
        user1.logOut()
        allUsers = app.allUsers()
        assertEquals(1, allUsers.size)
        assertFalse(allUsers.containsKey(user1.id))
    }

    @Test
    fun allUsers_retrieveRemovedUser() {
        val user1: User = app.login(Credentials.anonymous())
        val allUsers: Map<String, User> = app.allUsers()
        assertEquals(1, allUsers.size)
        user1.logOut()
        assertEquals(1, allUsers.size)
        val userCopy: User = allUsers[user1.id] ?: error("Could not find user")
        assertEquals(user1, userCopy)
        assertEquals(User.State.REMOVED, userCopy.state)
        assertTrue(app.allUsers().isEmpty())
    }

    @Test
    fun switchUser() {
        val user1: User = app.login(Credentials.anonymous())
        assertEquals(user1, app.currentUser())
        val user2: User = app.login(Credentials.anonymous())
        assertEquals(user2, app.currentUser())

        assertEquals(user1, app.switchUser(user1))
        assertEquals(user1, app.currentUser())
    }

    @Test
    fun switchUser_throwIfUserNotLoggedIn() {
        val user1: User = app.login(Credentials.anonymous())
        val user2: User = app.login(Credentials.anonymous())
        assertEquals(user2, app.currentUser())

        user1.logOut()
        try {
            app.switchUser(user1)
            fail()
        } catch (ignore: IllegalArgumentException) {
        }
    }

    @Test
    fun currentUser_FallbackToNextValidUser() {
        val user1: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        val user2: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals(user2, app.currentUser())
        user2.logOut()
        assertEquals(user1, app.currentUser())
        user1.logOut()
        assertNull(app.currentUser())
    }

    @Test
    fun switchUser_nullThrows() {
        try {
            app.switchUser(TestHelper.getNull())
            fail()
        } catch (ignore: IllegalArgumentException) {
        }
    }

    @Ignore("Add this test once we have support for both EmailPassword and ApiKey Auth Providers")
    @Test
    fun switchUser_authProvidersLockUsers() {
        TODO("FIXME")
    }

    @Test
    fun authListener() {
        val userRef = AtomicReference<User>(null)
        looperThread.runBlocking {
            val authenticationListener = object : AuthenticationListener {
                override fun loggedIn(user: User) {
                    userRef.set(user)
                    user.logOutAsync { /* Ignore */ }
                }

                override fun loggedOut(user: User) {
                    assertEquals(userRef.get(), user)
                    looperThread.testComplete()
                }
            }
            app.addAuthenticationListener(authenticationListener)
            app.login(Credentials.anonymous())
        }
    }

    @Test
    fun authListener_nullThrows() {
        assertFailsWith<IllegalArgumentException> { app.addAuthenticationListener(TestHelper.getNull()) }
    }

    @Test
    fun authListener_remove() = looperThread.runBlocking {
        val failListener = object : AuthenticationListener {
            override fun loggedIn(user: User) { fail() }
            override fun loggedOut(user: User) { fail() }
        }
        val successListener = object : AuthenticationListener {
            override fun loggedOut(user: User) { fail() }
            override fun loggedIn(user: User) { looperThread.testComplete() }
        }
        // This test depends on listeners being executed in order which is an
        // implementation detail, but there isn't a sure fire way to do this
        // without depending on implementation details or assume a specific timing.
        app.addAuthenticationListener(failListener)
        app.addAuthenticationListener(successListener)
        app.removeAuthenticationListener(failListener)
        app.login(Credentials.anonymous())
    }

    @Test
    fun functions_defaultCodecRegistry() {
        var user = app.login(Credentials.anonymous())
        assertEquals(app.configuration.defaultCodecRegistry, app.getFunctions(user).defaultCodecRegistry)
    }

    @Test
    fun functions_customCodecRegistry() {
        var user = app.login(Credentials.anonymous())
        val registry = CodecRegistries.fromCodecs(StringCodec())
        assertEquals(registry, app.getFunctions(user, registry).defaultCodecRegistry)
    }

    @Test()
    fun encryption() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // Create new test app with a random encryption key
        val testApp = TestApp(appName = TEST_APP_2, builder = {
            it.encryptionKey(TestHelper.getRandomKey())
        })

        try {
            // Create Realm in order to create the sync metadata Realm
            var user = testApp.login(Credentials.anonymous())
            val syncConfig = SyncConfiguration.defaultConfig(user, "foo")
            Realm.getInstance(syncConfig).close()

            // Create a configuration pointing to the metadata Realm for that app
            val metadataDir = File(context.filesDir, "mongodb-realm/${testApp.configuration.appId}/server-utility/metadata/")
            val config = RealmConfiguration.Builder()
                    .name("sync_metadata.realm")
                    .directory(metadataDir)
                    .build()
            assertTrue(File(config.path).exists())

            // Open the metadata realm file without a valid encryption key
            assertFailsWith<RealmFileException> {
                DynamicRealm.getInstance(config)
            }
        } finally {
            testApp.close()
        }
    }

    // Check that it is possible to have two Java instances of an App class, but they will
    // share the underlying App state.
    @Test
    fun multipleInstancesSameApp() {
        // Create a second copy of the test app
        val app2 = TestApp()
        try {
            // User handling are shared between each app
            val user = app.login(Credentials.anonymous());
            assertEquals(user, app2.currentUser())
            assertEquals(user, app.allUsers().values.first())
            assertEquals(user, app2.allUsers().values.first())

            user.logOut();

            assertNull(app.currentUser())
            assertNull(app2.currentUser())
        } finally {
            app2.close()
        }
    }
}
