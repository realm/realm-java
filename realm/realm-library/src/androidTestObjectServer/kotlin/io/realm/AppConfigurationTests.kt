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
import io.realm.internal.network.LoggingInterceptor.LOGIN_FEATURE
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.log.RealmLogger
import io.realm.mongodb.*
import io.realm.mongodb.log.obfuscator.HttpLogObfuscator
import io.realm.mongodb.sync.SyncSession
import io.realm.rule.BlockingLooperThread
import io.realm.util.assertFailsWithErrorCode
import org.bson.codecs.StringCodec
import org.bson.codecs.configuration.CodecRegistries
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.io.File
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.LinkedHashMap
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

private const val CUSTOM_HEADER_NAME = "Foo"
private const val CUSTOM_HEADER_VALUE = "bar"
private const val AUTH_HEADER_NAME = "RealmAuth"

@RunWith(AndroidJUnit4::class)
class AppConfigurationTests {

    val looperThread = BlockingLooperThread()

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Test
    fun authorizationHeaderName_illegalArgumentsThrows() {
        val builder: AppConfiguration.Builder = AppConfiguration.Builder("app-id")
        assertFailsWith<IllegalArgumentException> { builder.authorizationHeaderName(TestHelper.getNull()) }
        assertFailsWith<IllegalArgumentException> { builder.authorizationHeaderName("") }
    }

    @Test
    fun authorizationHeaderName() {
        val config1 = AppConfiguration.Builder("app-id").build()
        assertEquals("Authorization", config1.authorizationHeaderName)

        val config2 = AppConfiguration.Builder("app-id")
                .authorizationHeaderName("CustomAuth")
                .build()
        assertEquals("CustomAuth", config2.authorizationHeaderName)
    }

    @Test
    fun addCustomRequestHeader_illegalArgumentThrows() {
        val builder: AppConfiguration.Builder = AppConfiguration.Builder("app-id")
        assertFailsWith<IllegalArgumentException> { builder.addCustomRequestHeader("", "val") }
        assertFailsWith<IllegalArgumentException> { builder.addCustomRequestHeader(TestHelper.getNull(), "val") }
        assertFailsWith<IllegalArgumentException> { builder.addCustomRequestHeader("header", TestHelper.getNull()) }
    }

    @Test
    fun addCustomRequestHeader() {
        val config = AppConfiguration.Builder("app-id")
                .addCustomRequestHeader("header1", "val1")
                .addCustomRequestHeader("header2", "val2")
                .build()
        val headers: Map<String, String> = config.customRequestHeaders
        assertEquals(2, headers.size.toLong())
        assertTrue(headers.any { it.key == "header1" && it.value == "val1" })
        assertTrue(headers.any { it.key == "header2" && it.value == "val2" })
    }

    @Test
    fun addCustomRequestHeaders() {
        val inputHeaders: MutableMap<String, String> = LinkedHashMap()
        inputHeaders["header1"] = "value1"
        inputHeaders["header2"] = "value2"
        val config = AppConfiguration.Builder("app-id")
                .addCustomRequestHeaders(TestHelper.getNull())
                .addCustomRequestHeaders(inputHeaders)
                .build()
        val outputHeaders: Map<String, String> = config.customRequestHeaders
        assertEquals(2, outputHeaders.size.toLong())
        assertTrue(outputHeaders.any { it.key == "header1" && it.value == "value1" })
        assertTrue(outputHeaders.any { it.key == "header2" && it.value == "value2" })
    }

    @Test
    fun addCustomHeader_combinesSingleAndMultiple() {
        val config = AppConfiguration.Builder("app-id")
                .addCustomRequestHeader("header3", "val3")
                .addCustomRequestHeaders(mapOf(Pair("header1", "val1")))
                .build()
        val headers: Map<String, String> = config.customRequestHeaders
        assertEquals(2, headers.size)
        assertTrue(headers.any { it.key == "header3" && it.value == "val3" })
        assertTrue(headers.any { it.key == "header1" && it.value == "val1" })
    }

    @Test
    fun syncRootDirectory_default() {
        val config = AppConfiguration.Builder("app-id").build()
        val expectedDefaultRoot = File(InstrumentationRegistry.getInstrumentation().targetContext.filesDir, "mongodb-realm")
        assertEquals(expectedDefaultRoot, config.syncRootDirectory)
    }

    @Test
    fun syncRootDirectory() {
        val builder: AppConfiguration.Builder = AppConfiguration.Builder("app-id")
        val expectedRoot = tempFolder.newFolder()
        val config = builder
                .syncRootDirectory(expectedRoot)
                .build()
        assertEquals(expectedRoot, config.syncRootDirectory)
    }

    @Test
    fun syncRootDirectory_null() {
        val builder: AppConfiguration.Builder = AppConfiguration.Builder("app-id")
        assertFailsWith<IllegalArgumentException> { builder.syncRootDirectory(TestHelper.getNull()) }
    }

    @Test
    fun syncRootDirectory_writeProtectedDir() {
        val builder: AppConfiguration.Builder = AppConfiguration.Builder("app-id")
        val dir = File("/")
        assertFailsWith<IllegalArgumentException> { builder.syncRootDirectory(dir) }
    }

    @Test
    fun syncRootDirectory_dirIsAFile() {
        val builder: AppConfiguration.Builder = AppConfiguration.Builder("app-id")
        val file = File(tempFolder.newFolder(), "dummyfile")
        assertTrue(file.createNewFile())
        assertFailsWith<IllegalArgumentException> { builder.syncRootDirectory(file) }
    }

    @Test
    fun appName() {
        val config = AppConfiguration.Builder("app-id")
                .appName("app-name")
                .build()
        assertEquals("app-name", config.appName)
    }

    @Test
    fun appName_invalidValuesThrows() {
        val builder = AppConfiguration.Builder("app-id")

        assertFailsWith<java.lang.IllegalArgumentException> { builder.appName(TestHelper.getNull()) }
        assertFailsWith<java.lang.IllegalArgumentException> { builder.appName("") }
    }

    @Test
    fun appVersion() {
        val config = AppConfiguration.Builder("app-id")
                .appVersion("app-version")
                .build()
        assertEquals("app-version", config.appVersion)
    }

    @Test
    fun appVersion_invalidValuesThrows() {
        val builder = AppConfiguration.Builder("app-id")

        assertFailsWith<java.lang.IllegalArgumentException> { builder.appVersion(TestHelper.getNull()) }
        assertFailsWith<java.lang.IllegalArgumentException> { builder.appVersion("") }
    }

    @Test
    fun baseUrl() {
        val url = "http://myurl.com"
        val config = AppConfiguration.Builder("foo").baseUrl(url).build()
        assertEquals(URL(url), config.baseUrl)
    }

    @Test
    fun baseUrl_defaultValue() {
        val url = "https://realm.mongodb.com"
        val config = AppConfiguration.Builder("foo").build()
        assertEquals(URL(url), config.baseUrl)
    }

    @Test
    fun baseUrl_invalidValuesThrows() {
        val configBuilder = AppConfiguration.Builder("foo")
        assertFailsWith<IllegalArgumentException> { configBuilder.baseUrl("") }
        assertFailsWith<IllegalArgumentException> { configBuilder.baseUrl(TestHelper.getNull()) }
        assertFailsWith<IllegalArgumentException> { configBuilder.baseUrl("invalid-url") }
    }

    @Test
    fun defaultSyncErrorHandler() {
        val errorHandler = SyncSession.ErrorHandler { _, _ -> }

        val config = AppConfiguration.Builder("app-id")
                .defaultSyncErrorHandler(errorHandler)
                .build()
        assertEquals(config.defaultErrorHandler, errorHandler)
    }

    @Test
    fun defaultSyncErrorHandler_invalidValuesThrows() {
        assertFailsWith<IllegalArgumentException> {
            AppConfiguration.Builder("app-id")
                    .defaultSyncErrorHandler(TestHelper.getNull())
        }

    }

    @Test
    fun encryptionKey() {
        val key = TestHelper.getRandomKey()

        val config = AppConfiguration.Builder("app-id")
                .encryptionKey(key)
                .build()

        assertArrayEquals(key, config.encryptionKey)
    }

    @Test
    fun encryptionKey_invalidValuesThrows() {
        val builder = AppConfiguration.Builder("app-id")

        assertFailsWith<IllegalArgumentException> {
            builder.encryptionKey(TestHelper.getNull())
        }

        assertFailsWith<IllegalArgumentException> {
            builder.encryptionKey(byteArrayOf(0, 0, 0, 0))
        }
    }

    @Test
    fun requestTimeout() {
        val config = AppConfiguration.Builder("app-id")
                .requestTimeout(1, TimeUnit.MILLISECONDS)
                .build()
        assertEquals(1000L, config.requestTimeoutMs)
    }

    @Test
    fun requestTimeout_invalidValuesThrows() {
        val builder = AppConfiguration.Builder("app-id")

        assertFailsWith<IllegalArgumentException> { builder.requestTimeout(-1, TimeUnit.MILLISECONDS) }
        assertFailsWith<IllegalArgumentException> { builder.requestTimeout(1, TestHelper.getNull()) }
    }

    @Test
    fun codecRegistry_null() {
        val builder: AppConfiguration.Builder = AppConfiguration.Builder("app-id")
        assertFailsWith<IllegalArgumentException> {
            builder.codecRegistry(TestHelper.getNull())
        }
    }

    @Test
    fun defaultFunctionsCodecRegistry() {
        val config: AppConfiguration = AppConfiguration.Builder("app-id").build()
        assertEquals(AppConfiguration.DEFAULT_BSON_CODEC_REGISTRY, config.defaultCodecRegistry)
    }

    @Test
    fun customCodecRegistry() {
        val configCodecRegistry = CodecRegistries.fromCodecs(StringCodec())
        val config: AppConfiguration = AppConfiguration.Builder("app-id")
                .codecRegistry(configCodecRegistry)
                .build()
        assertEquals(configCodecRegistry, config.defaultCodecRegistry)
    }

    @Test
    fun httpLogObfuscator_null() {
        val config = AppConfiguration.Builder("app-id")
                .httpLogObfuscator(TestHelper.getNull())
                .build()
        assertNull(config.httpLogObfuscator)
    }

    @Test
    fun defaultLoginInfoObfuscator() {
        val config = AppConfiguration.Builder("app-id").build()

        val defaultHttpLogObfuscator = HttpLogObfuscator(LOGIN_FEATURE, AppConfiguration.loginObfuscators)
        assertEquals(defaultHttpLogObfuscator, config.httpLogObfuscator)
    }
    // Check that custom headers and auth header renames are correctly used for HTTP requests
    // performed from Java.
    @Test
    fun javaRequestCustomHeaders() {
        var app: App? = null
        try {
            looperThread.runBlocking {
                app = TestApp(builder = { builder ->
                    builder.addCustomRequestHeader(CUSTOM_HEADER_NAME, CUSTOM_HEADER_VALUE)
                    builder.authorizationHeaderName(AUTH_HEADER_NAME)
                })
                runJavaRequestCustomHeadersTest(app!!)
            }
        } finally {
            app?.close()
        }
    }

    private fun runJavaRequestCustomHeadersTest(app: App) {
        val username = UUID.randomUUID().toString()
        val password = "password"
        val headerSet = AtomicBoolean(false)

        // Setup logger to inspect that we get a log message with the custom headers
        val level = RealmLog.getLevel()
        RealmLog.setLevel(LogLevel.ALL)
        val logger = RealmLogger { level: Int, tag: String?, throwable: Throwable?, message: String? ->
            if (level > LogLevel.TRACE && message!!.contains(CUSTOM_HEADER_NAME) && message.contains(CUSTOM_HEADER_VALUE)
                    && message.contains("RealmAuth: ")) {
                headerSet.set(true)
            }
        }
        RealmLog.add(logger)
        assertFailsWithErrorCode(ErrorCode.SERVICE_UNKNOWN) {
            app.registerUserAndLogin(username, password)
        }
        // FIXME Guess it would be better to reset logger on Realm.init, but not sure of impact
        //  ...or is the logger intentionally shared to enable full trace of a full test run?
        RealmLog.remove(logger)
        RealmLog.setLevel(level)

        assertTrue(headerSet.get())
        looperThread.testComplete()
    }


}
