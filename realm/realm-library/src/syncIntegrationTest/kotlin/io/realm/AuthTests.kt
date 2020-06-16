package io.realm

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.realm.AuthenticationListener
import io.realm.ErrorCode
import io.realm.ObjectServerError
import io.realm.StandardIntegrationTest
import io.realm.SyncConfiguration
import io.realm.SyncManager
import io.realm.SyncSession
import io.realm.SyncTestUtils
import io.realm.SyncUser
import io.realm.SyncUserInfo
import io.realm.internal.objectserver.Token
import io.realm.objectserver.utils.Constants
import io.realm.objectserver.utils.StringOnlyModule
import io.realm.objectserver.utils.UserFactory
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import java.util.*
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class AuthTests : StandardIntegrationTest() {
    @Test
    @Throws(InterruptedException::class)
    fun cachedInstanceShouldNotThrowIfRefreshTokenExpires() {
        val username = UUID.randomUUID().toString()
        val password = "password"
        val credentials: SyncCredentials = SyncCredentials.usernamePassword(username, password, true)
        // TODO: A bit unsure how to make the user invalid ... maybe delete the user on the server using the admin SDK?
        val user: SyncUser = Mockito.spy(SyncUser.logIn(credentials, Constants.AUTH_URL))
        Mockito.`when`(user.isValid()).thenReturn(true, true, false)
        val configuration: RealmConfiguration = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM).build()
        val realm = Realm.getInstance(configuration)
        Assert.assertFalse(user.isValid())
        Mockito.verify<Any>(user, Mockito.times(3)).isValid()
        val backgroundThread = CountDownLatch(1)
        // Should not throw when using an expired refresh_token form a different thread
        // It should be able to open a Realm with an expired token
        object : Thread() {
            override fun run() {
                val instance = Realm.getInstance(configuration)
                instance.close()
                backgroundThread.countDown()
            }
        }.start()
        backgroundThread.await()

        // It should be possible to open a cached Realm with expired token
        val cachedInstance = Realm.getInstance(configuration)
        junit.framework.Assert.assertNotNull(cachedInstance)
        realm.close()
        cachedInstance.close()
        user.logOut()
    }

    // TODO: Not sure if we already have tests for this....This sounds like it should be in SyncConfigurationTests
    @Test
    fun buildingSyncConfigurationShouldThrowIfInvalidUser() {
        val username = UUID.randomUUID().toString()
        val password = "password"
        val credentials: SyncCredentials = SyncCredentials.usernamePassword(username, password, true)
        val user: SyncUser = SyncUser.logIn(credentials, Constants.AUTH_URL)
        val currentUser: SyncUser = SyncUser.current()
        user.logOut()
        Assert.assertFalse(user.isValid())
        try {
            // We should not be able to build a configuration with an invalid/logged out user
            configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM).build()
            junit.framework.Assert.fail("Invalid user, it should not be possible to create a SyncConfiguration")
        } catch (expected: IllegalStateException) {
            // User not authenticated or authentication expired.
        }
        try {
            // We should not be able to build a configuration with an invalid/logged out user
            configurationFactory.createSyncConfigurationBuilder(currentUser, Constants.USER_REALM).build()
            junit.framework.Assert.fail("Invalid currentUser, it should not be possible to create a SyncConfiguration")
        } catch (expected: IllegalStateException) {
            // User not authenticated or authentication expired.
        }
    }

    // Same as above test
    // using a logout user should not throw
    @Test
    fun usingConfigurationWithInvalidUserShouldNotThrow() {
        val username = UUID.randomUUID().toString()
        val password = "password"
        val credentials: SyncCredentials = SyncCredentials.usernamePassword(username, password, true)
        val user: SyncUser = SyncUser.logIn(credentials, Constants.AUTH_URL)
        val configuration: RealmConfiguration = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM).build()
        user.logOut()
        Assert.assertFalse(user.isValid())
        val instance = Realm.getInstance(configuration)
        instance.close()
    }

    // TODO: Check if this test is in UserTests
    @Test
    fun logout_currentUserMoreThanOne() {
        UserFactory.createUniqueUser(Constants.AUTH_URL)
        SyncUser.current().logOut()
        val user: SyncUser = UserFactory.createUniqueUser(Constants.AUTH_URL)
        assertEquals(user, SyncUser.current())
    }

    // TODO: Check if this test is in UserTests
    // logging out 'user' should have the same impact on other instance(s) of the same user
    @Test
    @Throws(InterruptedException::class)
    fun loggingOutUserShouldImpactOtherInstances() {
        val username = UUID.randomUUID().toString()
        val password = "password"
        val credentials: SyncCredentials = SyncCredentials.usernamePassword(username, password, true)
        val user: SyncUser = SyncUser.logIn(credentials, Constants.AUTH_URL)
        val currentUser: SyncUser = SyncUser.current()
        junit.framework.Assert.assertTrue(user.isValid())
        assertEquals(user, currentUser)
        user.logOut()
        Assert.assertFalse(user.isValid())
        Assert.assertFalse(currentUser.isValid())
    }

    // TODO: Check if this test is in UserTests
    // logging out 'current' should have the same impact on other instance(s) of the user
    @Test
    @Throws(InterruptedException::class)
    fun loggingOutCurrentUserShouldImpactOtherInstances() {
        val username = UUID.randomUUID().toString()
        val password = "password"
        val credentials: SyncCredentials = SyncCredentials.usernamePassword(username, password, true)
        val user: SyncUser = SyncUser.logIn(credentials, Constants.AUTH_URL)
        val currentUser: SyncUser = SyncUser.current()
        junit.framework.Assert.assertTrue(user.isValid())
        assertEquals(user, currentUser)
        SyncUser.current().logOut()
        Assert.assertFalse(user.isValid())
        Assert.assertFalse(currentUser.isValid())
        Assert.assertNull(SyncUser.current())
    }

    // TODO: Check if this test is in UserTests
    // verify that a single user can be logged out and back in.
    @Test
    fun singleUserCanBeLoggedInAndOutRepeatedly() {
        val username = UUID.randomUUID().toString()
        val password = "password"

        // register the user the first time
        var credentials: SyncCredentials? = SyncCredentials.usernamePassword(username, password, true)
        var user: SyncUser = SyncUser.logIn(credentials, Constants.AUTH_URL)
        junit.framework.Assert.assertTrue(user.isValid())
        user.logOut()
        Assert.assertFalse(user.isValid())

        // on subsequent logins, the user is already registered.
        credentials = SyncCredentials.usernamePassword(username, password, false)
        credentials = credentials
        for (i in 0..2) {
            user = SyncUser.logIn(credentials, Constants.AUTH_URL)
            junit.framework.Assert.assertTrue(user.isValid())
            user.logOut()
            Assert.assertFalse(user.isValid())
        }
    }

    // TODO: Check if this test is in UserTests
    @Test
    @Throws(InterruptedException::class)
    fun revokedRefreshTokenIsNotSameAfterLogin() {
        val userLoggedInAgain = CountDownLatch(1)
        val uniqueName = UUID.randomUUID().toString()
        val credentials: SyncCredentials = SyncCredentials.usernamePassword(uniqueName, "password", true)
        val user: SyncUser = SyncUser.logIn(credentials, Constants.AUTH_URL)
        val revokedRefreshToken: Token = SyncTestUtils.getRefreshToken(user)
        SyncManager.addAuthenticationListener(object : AuthenticationListener() {
            fun loggedIn(user: SyncUser?) {}
            fun loggedOut(user: SyncUser?) {
                val credentials: SyncCredentials = SyncCredentials.usernamePassword(uniqueName, "password", false)
                val loggedInUser: SyncUser = SyncUser.logIn(credentials, Constants.AUTH_URL)
                val token: Token = SyncTestUtils.getRefreshToken(loggedInUser)
                // still comparing the same user
                junit.framework.Assert.assertEquals(revokedRefreshToken.identity(), token.identity())

                // different tokens
                Assert.assertNotEquals(revokedRefreshToken.value(), token.value())
                SyncManager.removeAuthenticationListener(this)
                userLoggedInAgain.countDown()
            }
        })
        user.logOut()
        TestHelper.awaitOrFail(userLoggedInAgain)
    }
}
