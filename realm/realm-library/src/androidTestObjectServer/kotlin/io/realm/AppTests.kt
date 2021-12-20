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
import io.realm.entities.SyncStringOnly
import io.realm.exceptions.RealmFileException
import io.realm.internal.network.OkHttpNetworkTransport
import io.realm.internal.objectstore.OsJavaNetworkTransport
import io.realm.kotlin.syncSession
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.mongodb.*
import io.realm.mongodb.sync.SyncConfiguration
import io.realm.mongodb.sync.SyncSession
import io.realm.mongodb.sync.testSchema
import io.realm.rule.BlockingLooperThread
import org.bson.codecs.StringCodec
import org.bson.codecs.configuration.CodecRegistries
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.CountDownLatch
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
        RealmLog.setLevel(LogLevel.DEBUG)
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
            assertEquals(ErrorCode.INVALID_EMAIL_PASSWORD, ex.errorCode)
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
            assertEquals(ErrorCode.INVALID_EMAIL_PASSWORD, result.error.errorCode)
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
    fun currentUser_availableIfJustExpired() {
        app.close()
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        app = TestApp(object: OsJavaNetworkTransport() {
            override fun sendRequestAsync(method: String,
                                          url: String,
                                          timeoutMs: Long,
                                          headers: MutableMap<String, String>,
                                          body: String,
                                          completionPtr: Long) {
                val response = executeRequest(method, url, timeoutMs, headers, body)
                handleResponse(response, completionPtr)
            }

            override fun executeRequest(
                method: String,
                url: String,
                timeoutMs: Long,
                headers: MutableMap<String, String>,
                body: String
            ): Response {
                var result = ""
                when {
                    url.endsWith("/providers/${Credentials.Provider.ANONYMOUS.id}/login") -> {
                        // This token expires on Sunday 10. May 2020 22:23:28
                        result = """
                                    {
                                        "access_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjVlNjk2M2RmYWZlYTYzMjU0NTgxYzAyNiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1ODM5NjcyMDgsImlhdCI6MTU4Mzk2NTQwOCwiaXNzIjoiNWU2OTY0ZTBhZmVhNjMyNTQ1ODFjMWEzIiwic3RpdGNoX2RldklkIjoiMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwIiwic3RpdGNoX2RvbWFpbklkIjoiNWU2OTYzZGVhZmVhNjMyNTQ1ODFjMDI1Iiwic3ViIjoiNWU2OTY0ZTBhZmVhNjMyNTQ1ODFjMWExIiwidHlwIjoiYWNjZXNzIn0.J4mp8LnlsxTQRV_7W2Er4qY0tptR76PJGG1k6HSMmUYqgfpJC2Fnbcf1VCoebzoNolH2-sr8AHDVBBCyjxRjqoY9OudFHmWZKmhDV1ysxPP4XmID0nUuN45qJSO8QEAqoOmP1crXjrUZWedFw8aaCZE-bxYfvcDHyjBcbNKZqzawwUw2PyTOlrNjgs01k2J4o5a5XzYkEsJuzr4_8UqKW6zXvYj24UtqnqoYatW5EzpX63m2qig8AcBwPK4ZHb5wEEUdf4QZxkRY5QmTgRHP8SSqVUB_mkHgKaizC_tSB3E0BekaDfLyWVC1taAstXJNfzgFtLI86AzuXS2dCiCfqQ",
                                        "refresh_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjVlNjk2M2RmYWZlYTYzMjU0NTgxYzAyNiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1ODkxNDk0MDgsImlhdCI6MTU4Mzk2NTQwOCwic3RpdGNoX2RhdGEiOm51bGwsInN0aXRjaF9kZXZJZCI6IjAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMCIsInN0aXRjaF9kb21haW5JZCI6IjVlNjk2M2RlYWZlYTYzMjU0NTgxYzAyNSIsInN0aXRjaF9pZCI6IjVlNjk2NGUwYWZlYTYzMjU0NTgxYzFhMyIsInN0aXRjaF9pZGVudCI6eyJpZCI6IjVlNjk2NGUwYWZlYTYzMjU0NTgxYzFhMC1oaWF2b3ZkbmJxbGNsYXBwYnl1cmJpaW8iLCJwcm92aWRlcl90eXBlIjoiYW5vbi11c2VyIiwicHJvdmlkZXJfaWQiOiI1ZTY5NjNlMGFmZWE2MzI1NDU4MWMwNGEifSwic3ViIjoiNWU2OTY0ZTBhZmVhNjMyNTQ1ODFjMWExIiwidHlwIjoicmVmcmVzaCJ9.FhLdpmL48Mw0SyUKWuaplz3wfeS8TCO8S7I9pIJenQww9nPqQ7lIvykQxjCCtinGvsZIJKt_7R31xYCq4Jp53Nw81By79IwkXtO7VXHPsXXZG5_2xV-s0u44e85sYD5su_H-xnx03sU2piJbWJLSB8dKu3rMD4mO-S0HNXCCAty-JkYKSaM2-d_nS8MNb6k7Vfm7y69iz_uwHc-bb_1rPg7r827K6DEeEMF41Hy3Nx1kCdAUOM9-6nYv3pZSU1PFrGYi2uyTXPJ7R7HigY5IGHWd0hwONb_NUr4An2omqfvlkLEd77ut4V9m6mExFkoKzRz7shzn-IGkh3e4h7ECGA",
                                        "user_id": "5e6964e0afea63254581c1a1",
                                        "device_id": "000000000000000000000000"
                                    }                    
                                """.trimIndent()
                    }
                    url.endsWith("/auth/profile") -> {
                        result = """
                                    {
                                        "user_id": "5e6964e0afea63254581c1a1",
                                        "domain_id": "000000000000000000000000",
                                        "identities": [
                                            {
                                                "id": "5e68f51ade5ba998bb17500d",
                                                "provider_type": "local-userpass",
                                                "provider_id": "000000000000000000000003",
                                                "provider_data": {
                                                    "email": "unique_user@domain.com"
                                                }
                                            }
                                        ],
                                        "data": {
                                            "email": "unique_user@domain.com"
                                        },
                                        "type": "normal",
                                        "roles": [
                                            {
                                                "role_name": "GROUP_OWNER",
                                                "group_id": "5e68f51e087b1b33a53f56d5"
                                            }
                                        ]
                                    }
                                """.trimIndent()
                    }
                    url.endsWith("/location") -> {
                        result = """
                                    { "deployment_model" : "GLOBAL",
                                      "location": "US-VA", 
                                      "hostname": "http://localhost:9090",
                                      "ws_hostname": "ws://localhost:9090"
                                    }
                                    """.trimIndent()
                    }
                    else -> {
                        fail("Unexpected request url: $url")
                    }
                }
                val successHeaders: Map<String, String> = mapOf(Pair("Content-Type", "application/json"))
                return OkHttpNetworkTransport.Response.httpResponse(200, successHeaders, result)
            }

            override fun sendStreamingRequest(request: Request): Response {
                throw IllegalAccessError()
            }
        })

        val creds = Credentials.anonymous()
        val user: User = app.login(creds)
        assertEquals(User.State.LOGGED_IN, user.state) // TODO: Should ideally be LOGGED_OUT, but only happens after interaction with server
    }

    @Test
    fun currentUser_nullWhenSyncAuthenticationFails() = looperThread.runBlocking {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        admin.disableUser(user)
        val waiter = CountDownLatch(1)
        val syncConfig = configFactory.createSyncConfigurationBuilder(user)
            .testSchema(SyncStringOnly::class.java)
            .errorHandler { session: SyncSession, error: AppException ->
                RealmLog.error(error.toString())
                assertEquals(User.State.LOGGED_OUT, session.user.state)
                assertNull(app.currentUser())
                looperThread.testComplete()
            }
            .build()
        val realm = Realm.getInstance(syncConfig)
        looperThread.closeAfterTest(realm)
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

            val syncConfig = SyncConfiguration
                    .Builder(user, "foo")
                    .testSchema(SyncStringOnly::class.java)
                    .build()

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
