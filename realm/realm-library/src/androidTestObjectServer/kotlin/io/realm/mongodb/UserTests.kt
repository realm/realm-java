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
package io.realm.mongodb

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.*
import io.realm.admin.ServerAdmin
import io.realm.mongodb.auth.ApiKey
import io.realm.mongodb.auth.ApiKeyAuth
import io.realm.rule.BlockingLooperThread
import org.bson.Document
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertFails
import kotlin.test.assertFailsWith

val CUSTOM_USER_DATA_FIELD = "custom_field"
val CUSTOM_USER_DATA_VALUE = "custom_data"

@RunWith(AndroidJUnit4::class)
class UserTests {

    val looperThread = BlockingLooperThread()

    private lateinit var app: App
    private lateinit var anonUser: User
    private lateinit var admin: ServerAdmin

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        app = TestApp()
        admin = ServerAdmin(app)
        anonUser = app.login(Credentials.anonymous())
    }

    @After
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
    }

    @Test
    fun getApp() {
        assertEquals(app, anonUser.app)
    }

    @Test
    fun getState_anonymousUser() {
        assertEquals(User.State.LOGGED_IN, anonUser.state)
        anonUser.logOut()
        assertEquals(User.State.REMOVED, anonUser.state)
    }

    @Test
    fun getState_emailUser() {
        val emailUser = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals(User.State.LOGGED_IN, emailUser.state)
        emailUser.logOut()
        assertEquals(User.State.LOGGED_OUT, emailUser.state)
        app.removeUser(emailUser)
        assertEquals(User.State.REMOVED, emailUser.state)
    }

    @Test
    fun logOut() {
        // Anonymous users are removed upon log out
        assertEquals(anonUser, app.currentUser())
        anonUser.logOut()
        assertEquals(User.State.REMOVED, anonUser.state)
        assertNull(app.currentUser())

        // Users registered with Email/Password will register as Logged Out
        val user2: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        val current: User = app.currentUser()!!
        assertEquals(user2, current)
        user2.logOut()
        assertEquals(User.State.LOGGED_OUT, user2.state)
        // Same effect on all instances
        assertEquals(User.State.LOGGED_OUT, current.state)
        // And no current user anymore
        assertNull(app.currentUser())
    }

    @Test
    fun logOutAsync() = looperThread.runBlocking {
        assertEquals(anonUser, app.currentUser())
        anonUser.logOutAsync() { result ->
            val callbackUser: User = result.orThrow
            assertNull(app.currentUser())
            assertEquals(anonUser, callbackUser)
            assertEquals(User.State.REMOVED, anonUser.state)
            assertEquals(User.State.REMOVED, callbackUser.state)
            looperThread.testComplete()
        }
    }

    @Test
    fun logOutUserInstanceImpactsCurrentUser() {
        val currentUser = app.currentUser()!!
        assertEquals(User.State.LOGGED_IN, currentUser.state)
        assertEquals(User.State.LOGGED_IN, anonUser.state)
        assertEquals(currentUser, anonUser)

        anonUser!!.logOut()

        assertNotEquals(User.State.LOGGED_OUT, currentUser.state)
        assertNotEquals(User.State.LOGGED_OUT, anonUser.state)
        assertNull(app.currentUser())
    }

    @Test
    fun logOutCurrentUserImpactsOtherInstances() {
        val currentUser = app.currentUser()!!
        assertEquals(User.State.LOGGED_IN, currentUser.state)
        assertEquals(User.State.LOGGED_IN, anonUser.state)
        assertEquals(currentUser, anonUser)

        currentUser!!.logOut()

        assertNotEquals(User.State.LOGGED_OUT, currentUser.state)
        assertNotEquals(User.State.LOGGED_OUT, anonUser.state)
        assertNull(app.currentUser())
    }

    @Test
    fun repeatedLogInAndOut() {
        val password = "123456"
        val initialUser = app.registerUserAndLogin(TestHelper.getRandomEmail(), password)
        assertEquals(User.State.LOGGED_IN, initialUser.state)
        initialUser.logOut()
        assertEquals(User.State.LOGGED_OUT, initialUser.state)

        repeat(3) {
            val user = app.login(Credentials.emailPassword(initialUser.profile.email, password))
            assertEquals(User.State.LOGGED_IN, user.state)
            user.logOut()
            assertEquals(User.State.LOGGED_OUT, user.state)
        }
    }

    @Test
    fun logOutAsync_throwsOnNonLooperThread() {
        try {
            anonUser.logOutAsync { fail() }
            fail()
        } catch (ignore: IllegalStateException) {
        }
    }

    @Test
    fun linkUser_emailPassword() {
        assertEquals(1, anonUser.identities.size)

        val email = TestHelper.getRandomEmail()
        val password = "123456"
        app.emailPassword.registerUser(email, password) // TODO: Test what happens if auto-confirm is enabled
        var linkedUser: User = anonUser.linkCredentials(Credentials.emailPassword(email, password))

        assertTrue(anonUser === linkedUser)
        assertEquals(2, linkedUser.identities.size)
        assertEquals(Credentials.Provider.EMAIL_PASSWORD, linkedUser.identities[1].provider)

        // Validate that we cannot link a second set of credentials
        val otherEmail = TestHelper.getRandomEmail()
        val otherPassword = "123456"
        app.emailPassword.registerUser(otherEmail, otherPassword)

        val credentials = Credentials.emailPassword(otherEmail, otherPassword)

        assertFails {
            linkedUser = anonUser.linkCredentials(credentials)
        }
    }

    @Test
    fun linkUser_userApiKey() {
        // Generate API key
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        val apiKey: ApiKey = user.apiKeys.create("my-key");
        user.logOut()

        anonUser = app.login(Credentials.anonymous())

        assertEquals(1, anonUser.identities.size)

        // Linking with another user's API key is not allowed and must raise an AppException
        val exception = assertFailsWith<AppException> {
            anonUser.linkCredentials(Credentials.apiKey(apiKey.value))
        }

        assertEquals("invalid user link request", exception.errorMessage);
        assertEquals(ErrorCode.Category.FATAL, exception.errorCode.category);
        assertEquals("realm::app::ServiceError", exception.errorCode.type);
        assertEquals(6, exception.errorCode.intValue());
    }

    @Test
    fun linkUser_customFunction() {
        assertEquals(1, anonUser.identities.size)

        val document = Document(mapOf(
                "mail" to TestHelper.getRandomEmail(),
                "id" to TestHelper.getRandomId() + 666
        ))

        val credentials = Credentials.customFunction(document)

        val linkedUser = anonUser.linkCredentials(credentials)

        assertTrue(anonUser === linkedUser)
        assertEquals(2, linkedUser.identities.size)
        assertEquals(Credentials.Provider.CUSTOM_FUNCTION, linkedUser.identities[1].provider)
    }

    @Test
    fun linkUser_existingCredentialsThrows() {
        val email = TestHelper.getRandomEmail()
        val password = "123456"
        val emailUser: User = app.registerUserAndLogin(email, password)
        try {
            anonUser.linkCredentials(Credentials.emailPassword(email, password))
            fail()
        } catch (ex: AppException) {
            assertEquals(ErrorCode.INVALID_SESSION, ex.errorCode)
        }
    }

    @Test
    fun linkUser_invalidArgsThrows() {
        try {
            anonUser.linkCredentials(TestHelper.getNull())
            fail()
        } catch (ignore: IllegalArgumentException) {
        }
    }

    @Test
    fun linkUserAsync() = looperThread.runBlocking {
        assertEquals(1, anonUser.identities.size)
        val email = TestHelper.getRandomEmail()
        val password = "123456"
        app.emailPassword.registerUser(email, password) // TODO: Test what happens if auto-confirm is enabled

        anonUser.linkCredentialsAsync(Credentials.emailPassword(email, password)) { result ->
            val linkedUser: User = result.orThrow
            assertTrue(anonUser === linkedUser)
            assertEquals(2, linkedUser.identities.size)
            assertEquals(Credentials.Provider.EMAIL_PASSWORD, linkedUser.identities[1].provider)
            looperThread.testComplete()
        }
    }

    @Test
    fun linkUserAsync_throwsOnNonLooperThread() {
        try {
            anonUser.linkCredentialsAsync(Credentials.emailPassword(TestHelper.getRandomEmail(), "123456")) { fail() }
            fail()
        } catch (ignore: java.lang.IllegalStateException) {
        }
    }

    @Test
    fun removeUser() {
        anonUser.logOut() // Remove user used by other tests

        // Removing logged in user
        val user1 = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals(user1, app.currentUser())
        assertEquals(1, app.allUsers().size)
        app.removeUser(user1)
        assertEquals(User.State.REMOVED, user1.state)
        assertNull(app.currentUser())
        assertEquals(0, app.allUsers().size)

        // Remove logged out user
        val user2 = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        user2.logOut()
        assertNull(app.currentUser())
        assertEquals(1, app.allUsers().size)
        app.removeUser(user2)
        assertEquals(User.State.REMOVED, user2.state)
        assertEquals(0, app.allUsers().size)
    }

    @Test
    fun removeUserAsync() {
        anonUser.logOut() // Remove user used by other tests

        // Removing logged in user
        looperThread.runBlocking {
            val user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
            assertEquals(user, app.currentUser())
            assertEquals(1, app.allUsers().size)
            user.removeAsync { result ->
                assertEquals(User.State.REMOVED, result.orThrow.state)
                assertNull(app.currentUser())
                assertEquals(0, app.allUsers().size)
                looperThread.testComplete()
            }
        }

        // Removing logged out user
        looperThread.runBlocking {
            val user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
            user.logOut()
            assertNull(app.currentUser())
            assertEquals(1, app.allUsers().size)
            user.removeAsync { result ->
                assertEquals(User.State.REMOVED, result.orThrow.state)
                assertEquals(0, app.allUsers().size)
                looperThread.testComplete()
            }
        }
    }

    @Test
    fun removeUserAsync_nonLooperThreadThrows() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "1234567")
        try {
            user.removeAsync { fail() }
        } catch (ignore: IllegalStateException) {
        }
    }

    @Test
    fun getApiKeyAuthProvider() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        val provider1: ApiKeyAuth = user.apiKeys
        assertEquals(user, provider1.user)

        user.logOut()

        try {
            user.apiKeys
            fail()
        } catch (ex: IllegalStateException) {
        }
    }

    @Test
    fun revokedRefreshTokenIsNotSameAfterLogin() = looperThread.runBlocking {
        val password = "password"
        val user = app.registerUserAndLogin(TestHelper.getRandomEmail(), password)
        val refreshToken = user.refreshToken

        app.addAuthenticationListener(object : AuthenticationListener {
            override fun loggedIn(user: User) {}

            override fun loggedOut(loggerOutUser: User) {
                app.loginAsync(Credentials.emailPassword(loggerOutUser.profile.email, password)) {
                    val loggedInUser = it.orThrow
                    assertTrue(loggerOutUser !== loggedInUser)
                    assertNotEquals(refreshToken, loggedInUser.refreshToken)
                    looperThread.testComplete()
                }
            }
        })
        user.logOut()
    }

    @Test
    fun getLocalId() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertNotNull(user.localId)
    }

    fun isLoggedIn() {
        var anonUser = app.login(Credentials.anonymous())
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")


        assertTrue(anonUser.isLoggedIn)
        assertTrue(user.isLoggedIn)

        anonUser.logOut()
        assertFalse(anonUser.isLoggedIn)
        assertTrue(user.isLoggedIn)

        user.logOut()
        assertFalse(user.isLoggedIn)
    }

    @Test
    fun equals() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals(user, user)
        assertNotEquals(user, app)
        user.logOut()

        val sameUserNewLogin = app.login(Credentials.emailPassword(user.profile.email!!, "123456"))
        // Verify that it is not same object but uses underlying OSSyncUser equality on identity
        assertFalse(user === sameUserNewLogin)
        assertEquals(user, sameUserNewLogin)

        val differentUser: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertNotEquals(user, differentUser)
    }

    @Test
    fun hashCode_user() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        user.logOut()

        val sameUserNewLogin = app.login(Credentials.emailPassword(user.profile.email!!, "123456"))
        // Verify that two equal users also returns same hashCode
        assertFalse(user === sameUserNewLogin)
        assertEquals(user.hashCode(), sameUserNewLogin.hashCode())
    }

    @Test
    @Ignore("Cannot automate custom user data cluster setup yet due to missing CLI support " +
            "https://github.com/realm/realm-java/issues/6942")
    fun customData_initiallyEmpty() {
        val user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        // Newly registered users do not have any custom data with current test server setup
        assertEquals(Document(), user.customData)
    }

    @Test
    @Ignore("Cannot automate custom user data cluster setup yet due to missing CLI support " +
            "https://github.com/realm/realm-java/issues/6942")
    fun customData_refresh() {
        val user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        // Newly registered users do not have any custom data with current test server setup
        assertEquals(Document(), user.customData)

        updateCustomData(user, Document(CUSTOM_USER_DATA_FIELD, CUSTOM_USER_DATA_VALUE))

        val updatedCustomData = user.refreshCustomData()
        assertEquals(CUSTOM_USER_DATA_VALUE, updatedCustomData[CUSTOM_USER_DATA_FIELD])
        assertEquals(CUSTOM_USER_DATA_VALUE, user.customData[CUSTOM_USER_DATA_FIELD])
    }

    @Test
    @Ignore("Cannot automate custom user data cluster setup yet due to missing CLI support " +
            "https://github.com/realm/realm-java/issues/6942")
    fun customData_refreshAsync() = looperThread.runBlocking {
        val user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        // Newly registered users do not have any custom data with current test server setup
        assertEquals(Document(), user.customData)

        updateCustomData(user, Document(CUSTOM_USER_DATA_FIELD, CUSTOM_USER_DATA_VALUE))

        val updatedCustomData = user.refreshCustomData { result ->
            val updatedCustomData = result.orThrow
            assertEquals(CUSTOM_USER_DATA_VALUE, updatedCustomData[CUSTOM_USER_DATA_FIELD])
            assertEquals(CUSTOM_USER_DATA_VALUE, user.customData[CUSTOM_USER_DATA_FIELD])
            looperThread.testComplete()
        }
    }

    @Test
    @Ignore("Cannot automate custom user data cluster setup yet due to missing CLI support " +
            "https://github.com/realm/realm-java/issues/6942")
    fun customData_refreshByLogout() {
        val password = "123456"
        val user = app.registerUserAndLogin(TestHelper.getRandomEmail(), password)
        // Newly registered users do not have any custom data with current test server setup
        assertEquals(Document(), user.customData)

        updateCustomData(user, Document(CUSTOM_USER_DATA_FIELD, CUSTOM_USER_DATA_VALUE))

        // But will be updated when authorization token is refreshed
        user.logOut()
        app.login(Credentials.emailPassword(user.profile.email, password))
        assertEquals(CUSTOM_USER_DATA_VALUE, user.customData.get(CUSTOM_USER_DATA_FIELD))
    }

    @Test
    @Ignore("Cannot automate custom user data cluster setup yet due to missing CLI support " +
            "https://github.com/realm/realm-java/issues/6942")
    fun customData_refreshAsyncThrowsOnNonLooper() {
        val password = "123456"
        val user = app.registerUserAndLogin(TestHelper.getRandomEmail(), password)

        assertFailsWith<java.lang.IllegalStateException> {
            user.refreshCustomData { }
        }
    }

    private fun updateCustomData(user: User, data: Document) {
        // Name of collection and property used for storing custom user data. Must match server config.json
        val COLLECTION_NAME = "custom_user_data"
        val USER_ID_FIELD = "userid"

        val client = user.getMongoClient(SERVICE_NAME)
        client.getDatabase(DATABASE_NAME).let {
            it.getCollection(COLLECTION_NAME).also { collection ->
                collection.insertOne(data.append(USER_ID_FIELD, user.id)).get()
            }
        }
    }

}
