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

import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.admin.ServerAdmin
import io.realm.mongodb.*
import io.realm.mongodb.auth.ApiKeyAuth
import io.realm.mongodb.auth.UserApiKey
import io.realm.rule.BlockingLooperThread
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ApiKeyAuthTests {
    private val looperThread = BlockingLooperThread()
    private lateinit var app: TestApp
    private lateinit var admin: ServerAdmin
    private lateinit var user: User
    private lateinit var provider: ApiKeyAuth

    // Callback use to verify that an Illegal Argument was thrown from async methods
    private val checkNullInVoidCallback = App.Callback<Void> { result ->
        if (result.isSuccess) {
            fail()
        } else {
            assertEquals(ErrorCode.UNKNOWN, result.error.errorCode)
            looperThread.testComplete()
        }
    }

    private val checkNullInApiKeyCallback = App.Callback<UserApiKey> { result ->
        if (result.isSuccess) {
            fail()
        } else {
            assertEquals(ErrorCode.UNKNOWN, result.error.errorCode)
            looperThread.testComplete()
        }
    }

    // Methods exposed by the EmailPasswordAuthProvider
    enum class Method {
        CREATE,
        FETCH_SINGLE,
        FETCH_ALL,
        DELETE,
        ENABLE,
        DISABLE
    }

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        app = TestApp()
        admin = ServerAdmin(app)
        user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        provider = user.apiKeyAuth
    }

    @After
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
        admin.deleteAllUsers()
    }

    inline fun testNullArg(method: () -> Unit) {
        try {
            method()
            fail()
        } catch (ignore: IllegalArgumentException) {
        }
    }

    @Test
    fun createApiKey() {
        val key: UserApiKey = provider.createApiKey("my-key")
        assertEquals("my-key", key.name)
        assertNotNull("my-key", key.value)
        assertNotNull("my-key", key.id)
        assertTrue("my-key", key.isEnabled)
    }

    @Test
    fun createApiKey_invalidServerArgsThrows() {
        try {
            provider.createApiKey("%s")
            fail()
        } catch (e: AppException) {
            assertEquals(ErrorCode.INVALID_PARAMETER, e.errorCode)
        }
    }

    @Test
    fun createApiKey_invalidArgumentThrows() {
        testNullArg { provider.createApiKey(TestHelper.getNull()) }
        testNullArg { provider.createApiKey("") }
        looperThread.runBlocking {
            provider.createApiKeyAsync(TestHelper.getNull(), checkNullInApiKeyCallback)
        }
        looperThread.runBlocking {
            provider.createApiKeyAsync("", checkNullInApiKeyCallback)
        }
    }

    @Test
    fun createApiKeyAsync() = looperThread.runBlocking {
        provider.createApiKeyAsync("my-key") { result ->
            val key = result.orThrow
            assertEquals("my-key", key.name)
            assertNotNull("my-key", key.value)
            assertNotNull("my-key", key.id)
            assertTrue("my-key", key.isEnabled)
            looperThread.testComplete()
        }
    }

    @Test
    fun createApiKeyAsync_invalidServerArgsThrows() = looperThread.runBlocking {
        provider.createApiKeyAsync("%s") { result ->
            if (result.isSuccess) {
                fail()
            } else {
                assertEquals(ErrorCode.INVALID_PARAMETER, result.error.errorCode)
                looperThread.testComplete()
            }
        }
    }

    @Test
    fun fetchApiKey() {
        val key1: UserApiKey = provider.createApiKey("my-key")
        val key2: UserApiKey = provider.fetchApiKey(key1.id)

        assertEquals(key1.id, key2.id)
        assertEquals(key1.name, key2.name)
        assertNull(key2.value)
        assertEquals(key1.isEnabled, key2.isEnabled)
    }

    @Test
    fun fetchApiKey_nonExistingKey() {
        try {
            provider.fetchApiKey(ObjectId())
            fail()
        } catch (e: AppException) {
            assertEquals(ErrorCode.API_KEY_NOT_FOUND, e.errorCode)
        }
    }

    @Test
    fun fetchApiKey_invalidArgumentThrows() {
        testNullArg { provider.fetchApiKey(TestHelper.getNull()) }
        looperThread.runBlocking {
            provider.fetchApiKeyAsync(TestHelper.getNull(), checkNullInApiKeyCallback)
        }
    }

    @Test
    fun fetchApiKeyAsync() {
        val key1: UserApiKey = provider.createApiKey("my-key")
        looperThread.runBlocking {
            provider.fetchApiKeyAsync(key1.id) { result ->
                val key2 = result.orThrow
                assertEquals(key1.id, key2.id)
                assertEquals(key1.name, key2.name)
                assertNull(key2.value)
                assertEquals(key1.isEnabled, key2.isEnabled)
                looperThread.testComplete()
            }
        }
    }

    @Test
    fun fetchAllApiKeys() {
        val key1: UserApiKey = provider.createApiKey("my-key")
        val key2: UserApiKey = provider.createApiKey("other-key")
        val allKeys: List<UserApiKey> = provider.fetchAllApiKeys()
        assertEquals(2, allKeys.size)
        assertTrue(allKeys.any { it.id == key1.id })
        assertTrue(allKeys.any { it.id == key2.id })
    }

    @Test
    fun fetchAllApiKeysAsync() {
        val key1: UserApiKey = provider.createApiKey("my-key")
        val key2: UserApiKey = provider.createApiKey("other-key")
        looperThread.runBlocking {
            provider.fetchAllApiKeys() { result ->
                val keys: List<UserApiKey> = result.orThrow
                assertEquals(2, keys.size)
                assertTrue(keys.any { it.id == key1.id })
                assertTrue(keys.any { it.id == key2.id })
                looperThread.testComplete()
            }
        }
    }

    @Test
    fun deleteApiKey() {
        val key1: UserApiKey = provider.createApiKey("my-key")
        assertNotNull(provider.fetchApiKey(key1.id))
        provider.deleteApiKey(key1.id)
        try {
            provider.fetchApiKey(key1.id)
            fail()
        } catch (e: AppException) {
            assertEquals(ErrorCode.API_KEY_NOT_FOUND, e.errorCode)
        }
    }

    @Test
    fun deleteApiKey_invalidServerArgsThrows() {
        try {
            provider.deleteApiKey(ObjectId())
            fail()
        } catch (e: AppException) {
            assertEquals(ErrorCode.API_KEY_NOT_FOUND, e.errorCode)
        }
    }

    @Test
    fun deleteApiKey_invalidArgumentThrows() {
        testNullArg { provider.deleteApiKey(TestHelper.getNull()) }
        looperThread.runBlocking {
            provider.deleteApiKeyAsync(TestHelper.getNull(), checkNullInVoidCallback)
        }
    }

    @Test
    fun deleteApiKeyAsync() {
        val key: UserApiKey = provider.createApiKey("my-key")
        assertNotNull(provider.fetchApiKey(key.id))
        looperThread.runBlocking {
            provider.deleteApiKeyAsync(key.id) { result ->
                if (result.isSuccess) {
                    try {
                        provider.fetchApiKey(key.id)
                        fail()
                    } catch (e: AppException) {
                        assertEquals(ErrorCode.API_KEY_NOT_FOUND, e.errorCode)
                    }
                    looperThread.testComplete()
                } else {
                    fail(result.error.toString())
                }
            }
        }
    }

    @Test
    fun deleteApiKeyAsync_invalidServerArgsThrows() = looperThread.runBlocking {
        provider.deleteApiKeyAsync(ObjectId()) { result ->
            if (result.isSuccess) {
                fail()
            } else {
                assertEquals(ErrorCode.API_KEY_NOT_FOUND, result.error.errorCode)
                looperThread.testComplete()
            }
        }
    }

    @Test
    fun enableApiKey() {
        val key: UserApiKey = provider.createApiKey("my-key")
        provider.disableApiKey(key.id)
        assertFalse(provider.fetchApiKey(key.id).isEnabled)
        provider.enableApiKey(key.id)
        assertTrue(provider.fetchApiKey(key.id).isEnabled)
    }

    @Test
    fun enableApiKey_alreadyEnabled() {
        val key: UserApiKey = provider.createApiKey("my-key")
        provider.disableApiKey(key.id)
        assertFalse(provider.fetchApiKey(key.id).isEnabled)
        provider.enableApiKey(key.id)
        assertTrue(provider.fetchApiKey(key.id).isEnabled)
        provider.enableApiKey(key.id)
        assertTrue(provider.fetchApiKey(key.id).isEnabled)
    }

    @Test
    fun enableApiKey_invalidServerArgsThrows() {
        try {
            provider.enableApiKey(ObjectId())
            fail()
        } catch (e: AppException) {
            assertEquals(ErrorCode.API_KEY_NOT_FOUND, e.errorCode)
        }
    }

    @Test
    fun enableApiKey_invalidArgumentThrows() {
        testNullArg { provider.enableApiKey(TestHelper.getNull()) }
        looperThread.runBlocking {
            provider.enableApiKeyAsync(TestHelper.getNull(), checkNullInVoidCallback)
        }
    }

    @Test
    fun enableApiKeyAsync() {
        val key: UserApiKey = provider.createApiKey("my-key")
        provider.disableApiKey(key.id)
        assertFalse(provider.fetchApiKey(key.id).isEnabled)
        looperThread.runBlocking {
            provider.enableApiKeyAsync(key.id) { result ->
                if (result.isSuccess) {
                    assertTrue(provider.fetchApiKey(key.id).isEnabled)
                    looperThread.testComplete()
                } else {
                    fail(result.error.toString())
                }
            }
        }
    }

    @Test
    fun enableApiKeyAsync_invalidServerArgsThrows() = looperThread.runBlocking {
        provider.disableApiKeyAsync(ObjectId()) { result ->
            if (result.isSuccess) {
                fail()
            } else {
                assertEquals(ErrorCode.API_KEY_NOT_FOUND, result.error.errorCode)
                looperThread.testComplete()
            }
        }
    }

    @Test
    fun disableApiKey() {
        val key: UserApiKey = provider.createApiKey("my-key")
        provider.disableApiKey(key.id)
        assertFalse(provider.fetchApiKey(key.id).isEnabled)
    }

    @Test
    fun disableApiKey_alreadyDisabled() {
        val key: UserApiKey = provider.createApiKey("my-key")
        provider.disableApiKey(key.id)
        assertFalse(provider.fetchApiKey(key.id).isEnabled)
        provider.disableApiKey(key.id)
        assertFalse(provider.fetchApiKey(key.id).isEnabled)
    }

    @Test
    fun disableApiKey_invalidServerArgsThrows() {
        try {
            provider.disableApiKey(ObjectId())
            fail()
        } catch (e: AppException) {
            assertEquals(ErrorCode.API_KEY_NOT_FOUND, e.errorCode)
        }
    }

    @Test
    fun disableApiKey_invalidArgumentThrows() {
        testNullArg { provider.disableApiKey(TestHelper.getNull()) }
        looperThread.runBlocking {
            provider.disableApiKeyAsync(TestHelper.getNull(), checkNullInVoidCallback)
        }
    }

    @Test
    fun disableApiKeyAsync() {
        val key: UserApiKey = provider.createApiKey("my-key")
        assertTrue(key.isEnabled)
        looperThread.runBlocking {
            provider.disableApiKeyAsync(key.id) { result ->
                if (result.isSuccess) {
                    assertFalse(provider.fetchApiKey(key.id).isEnabled)
                    looperThread.testComplete()
                } else {
                    fail(result.error.toString())
                }
            }
        }
    }

    @Test
    fun disableApiKeyAsync_invalidServerArgsThrows() = looperThread.runBlocking {
        provider.disableApiKeyAsync(ObjectId()) { result ->
            if (result.isSuccess) {
                fail()
            } else {
                assertEquals(ErrorCode.API_KEY_NOT_FOUND, result.error.errorCode)
                looperThread.testComplete()
            }
        }
    }

    @Test
    @UiThreadTest
    fun callMethodsOnMainThreadThrows() {
        for (method in Method.values()) {
            try {
                when(method) {
                    Method.CREATE -> provider.createApiKey("name")
                    Method.FETCH_SINGLE -> provider.fetchApiKey(ObjectId())
                    Method.FETCH_ALL -> provider.fetchAllApiKeys()
                    Method.DELETE -> provider.deleteApiKey(ObjectId())
                    Method.ENABLE -> provider.enableApiKey(ObjectId())
                    Method.DISABLE -> provider.disableApiKey(ObjectId())
                }
                fail("$method should have thrown an exception")
            } catch (error: AppException) {
                assertEquals(ErrorCode.NETWORK_UNKNOWN, error.errorCode)
            }
        }
    }

    @Test
    fun callAsyncMethodsOnNonLooperThreadThrows() {
        for (method in Method.values()) {
            try {
                when(method) {
                    Method.CREATE -> provider.createApiKeyAsync("key") { fail() }
                    Method.FETCH_SINGLE -> provider.fetchApiKeyAsync(ObjectId()) { fail() }
                    Method.FETCH_ALL -> provider.fetchAllApiKeys { fail() }
                    Method.DELETE -> provider.deleteApiKeyAsync(ObjectId()) { fail() }
                    Method.ENABLE -> provider.enableApiKeyAsync(ObjectId()) { fail() }
                    Method.DISABLE -> provider.disableApiKeyAsync(ObjectId()) { fail() }
                }
                fail("$method should have thrown an exception")
            } catch (ignore: IllegalStateException) {
            }
        }
    }

    @Test
    fun callMethodWithLoggedOutUser() {
        user.logOut()
        for (method in Method.values()) {
            try {
                when(method) {
                    Method.CREATE -> provider.createApiKey("name")
                    Method.FETCH_SINGLE -> provider.fetchApiKey(ObjectId())
                    Method.FETCH_ALL -> provider.fetchAllApiKeys()
                    Method.DELETE -> provider.deleteApiKey(ObjectId())
                    Method.ENABLE -> provider.enableApiKey(ObjectId())
                    Method.DISABLE -> provider.disableApiKey(ObjectId())
                }
                fail("$method should have thrown an exception")
            } catch (error: AppException) {
                assertEquals(ErrorCode.INVALID_SESSION, error.errorCode)
            }
        }
    }

    @Test
    fun getUser() {
        assertEquals(app.currentUser(), provider.user)
    }

    @Test
    fun getApp() {
        assertEquals(app, provider.app)
    }
}

