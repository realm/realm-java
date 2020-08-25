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
import io.realm.mongodb.auth.ApiKey
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

    private val checkNullInApiKeyCallback = App.Callback<ApiKey> { result ->
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
        provider = user.apiKeys
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
        val key: ApiKey = provider.create("my-key")
        assertEquals("my-key", key.name)
        assertNotNull("my-key", key.value)
        assertNotNull("my-key", key.id)
        assertTrue("my-key", key.isEnabled)
    }

    @Test
    fun createApiKey_invalidServerArgsThrows() {
        try {
            provider.create("%s")
            fail()
        } catch (e: AppException) {
            assertEquals(ErrorCode.INVALID_PARAMETER, e.errorCode)
        }
    }

    @Test
    fun createApiKey_invalidArgumentThrows() {
        testNullArg { provider.create(TestHelper.getNull()) }
        testNullArg { provider.create("") }
        looperThread.runBlocking {
            provider.createAsync(TestHelper.getNull(), checkNullInApiKeyCallback)
        }
        looperThread.runBlocking {
            provider.createAsync("", checkNullInApiKeyCallback)
        }
    }

    @Test
    fun createApiKeyAsync() = looperThread.runBlocking {
        provider.createAsync("my-key") { result ->
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
        provider.createAsync("%s") { result ->
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
        val key1: ApiKey = provider.create("my-key")
        val key2: ApiKey = provider.fetch(key1.id)

        assertEquals(key1.id, key2.id)
        assertEquals(key1.name, key2.name)
        assertNull(key2.value)
        assertEquals(key1.isEnabled, key2.isEnabled)
    }

    @Test
    fun fetchApiKey_nonExistingKey() {
        try {
            provider.fetch(ObjectId())
            fail()
        } catch (e: AppException) {
            assertEquals(ErrorCode.API_KEY_NOT_FOUND, e.errorCode)
        }
    }

    @Test
    fun fetchApiKey_invalidArgumentThrows() {
        testNullArg { provider.fetch(TestHelper.getNull()) }
        looperThread.runBlocking {
            provider.fetchAsync(TestHelper.getNull(), checkNullInApiKeyCallback)
        }
    }

    @Test
    fun fetchApiKeyAsync() {
        val key1: ApiKey = provider.create("my-key")
        looperThread.runBlocking {
            provider.fetchAsync(key1.id) { result ->
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
        val key1: ApiKey = provider.create("my-key")
        val key2: ApiKey = provider.create("other-key")
        val allKeys: List<ApiKey> = provider.fetchAll()
        assertEquals(2, allKeys.size)
        assertTrue(allKeys.any { it.id == key1.id })
        assertTrue(allKeys.any { it.id == key2.id })
    }

    @Test
    fun fetchAllApiKeysAsync() {
        val key1: ApiKey = provider.create("my-key")
        val key2: ApiKey = provider.create("other-key")
        looperThread.runBlocking {
            provider.fetchAll() { result ->
                val keys: List<ApiKey> = result.orThrow
                assertEquals(2, keys.size)
                assertTrue(keys.any { it.id == key1.id })
                assertTrue(keys.any { it.id == key2.id })
                looperThread.testComplete()
            }
        }
    }

    @Test
    fun deleteApiKey() {
        val key1: ApiKey = provider.create("my-key")
        assertNotNull(provider.fetch(key1.id))
        provider.delete(key1.id)
        try {
            provider.fetch(key1.id)
            fail()
        } catch (e: AppException) {
            assertEquals(ErrorCode.API_KEY_NOT_FOUND, e.errorCode)
        }
    }

    @Test
    fun deleteApiKey_invalidServerArgsThrows() {
        try {
            provider.delete(ObjectId())
            fail()
        } catch (e: AppException) {
            assertEquals(ErrorCode.API_KEY_NOT_FOUND, e.errorCode)
        }
    }

    @Test
    fun deleteApiKey_invalidArgumentThrows() {
        testNullArg { provider.delete(TestHelper.getNull()) }
        looperThread.runBlocking {
            provider.deleteAsync(TestHelper.getNull(), checkNullInVoidCallback)
        }
    }

    @Test
    fun deleteApiKeyAsync() {
        val key: ApiKey = provider.create("my-key")
        assertNotNull(provider.fetch(key.id))
        looperThread.runBlocking {
            provider.deleteAsync(key.id) { result ->
                if (result.isSuccess) {
                    try {
                        provider.fetch(key.id)
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
        provider.deleteAsync(ObjectId()) { result ->
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
        val key: ApiKey = provider.create("my-key")
        provider.disable(key.id)
        assertFalse(provider.fetch(key.id).isEnabled)
        provider.enable(key.id)
        assertTrue(provider.fetch(key.id).isEnabled)
    }

    @Test
    fun enableApiKey_alreadyEnabled() {
        val key: ApiKey = provider.create("my-key")
        provider.disable(key.id)
        assertFalse(provider.fetch(key.id).isEnabled)
        provider.enable(key.id)
        assertTrue(provider.fetch(key.id).isEnabled)
        provider.enable(key.id)
        assertTrue(provider.fetch(key.id).isEnabled)
    }

    @Test
    fun enableApiKey_invalidServerArgsThrows() {
        try {
            provider.enable(ObjectId())
            fail()
        } catch (e: AppException) {
            assertEquals(ErrorCode.API_KEY_NOT_FOUND, e.errorCode)
        }
    }

    @Test
    fun enableApiKey_invalidArgumentThrows() {
        testNullArg { provider.enable(TestHelper.getNull()) }
        looperThread.runBlocking {
            provider.enableAsync(TestHelper.getNull(), checkNullInVoidCallback)
        }
    }

    @Test
    fun enableApiKeyAsync() {
        val key: ApiKey = provider.create("my-key")
        provider.disable(key.id)
        assertFalse(provider.fetch(key.id).isEnabled)
        looperThread.runBlocking {
            provider.enableAsync(key.id) { result ->
                if (result.isSuccess) {
                    assertTrue(provider.fetch(key.id).isEnabled)
                    looperThread.testComplete()
                } else {
                    fail(result.error.toString())
                }
            }
        }
    }

    @Test
    fun enableApiKeyAsync_invalidServerArgsThrows() = looperThread.runBlocking {
        provider.disableAsync(ObjectId()) { result ->
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
        val key: ApiKey = provider.create("my-key")
        provider.disable(key.id)
        assertFalse(provider.fetch(key.id).isEnabled)
    }

    @Test
    fun disableApiKey_alreadyDisabled() {
        val key: ApiKey = provider.create("my-key")
        provider.disable(key.id)
        assertFalse(provider.fetch(key.id).isEnabled)
        provider.disable(key.id)
        assertFalse(provider.fetch(key.id).isEnabled)
    }

    @Test
    fun disableApiKey_invalidServerArgsThrows() {
        try {
            provider.disable(ObjectId())
            fail()
        } catch (e: AppException) {
            assertEquals(ErrorCode.API_KEY_NOT_FOUND, e.errorCode)
        }
    }

    @Test
    fun disableApiKey_invalidArgumentThrows() {
        testNullArg { provider.disable(TestHelper.getNull()) }
        looperThread.runBlocking {
            provider.disableAsync(TestHelper.getNull(), checkNullInVoidCallback)
        }
    }

    @Test
    fun disableApiKeyAsync() {
        val key: ApiKey = provider.create("my-key")
        assertTrue(key.isEnabled)
        looperThread.runBlocking {
            provider.disableAsync(key.id) { result ->
                if (result.isSuccess) {
                    assertFalse(provider.fetch(key.id).isEnabled)
                    looperThread.testComplete()
                } else {
                    fail(result.error.toString())
                }
            }
        }
    }

    @Test
    fun disableApiKeyAsync_invalidServerArgsThrows() = looperThread.runBlocking {
        provider.disableAsync(ObjectId()) { result ->
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
                    Method.CREATE -> provider.create("name")
                    Method.FETCH_SINGLE -> provider.fetch(ObjectId())
                    Method.FETCH_ALL -> provider.fetchAll()
                    Method.DELETE -> provider.delete(ObjectId())
                    Method.ENABLE -> provider.enable(ObjectId())
                    Method.DISABLE -> provider.disable(ObjectId())
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
                    Method.CREATE -> provider.createAsync("key") { fail() }
                    Method.FETCH_SINGLE -> provider.fetchAsync(ObjectId()) { fail() }
                    Method.FETCH_ALL -> provider.fetchAll { fail() }
                    Method.DELETE -> provider.deleteAsync(ObjectId()) { fail() }
                    Method.ENABLE -> provider.enableAsync(ObjectId()) { fail() }
                    Method.DISABLE -> provider.disableAsync(ObjectId()) { fail() }
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
                    Method.CREATE -> provider.create("name")
                    Method.FETCH_SINGLE -> provider.fetch(ObjectId())
                    Method.FETCH_ALL -> provider.fetchAll()
                    Method.DELETE -> provider.delete(ObjectId())
                    Method.ENABLE -> provider.enable(ObjectId())
                    Method.DISABLE -> provider.disable(ObjectId())
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

