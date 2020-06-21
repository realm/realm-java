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
import io.realm.log.obfuscator.EmailPasswordObfuscator
import io.realm.log.obfuscator.LoginInfoObfuscator
import io.realm.log.obfuscator.TokenObfuscator
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.Credentials
import io.realm.mongodb.PatternObfuscatorFactory
import org.bson.codecs.StringCodec
import org.bson.codecs.configuration.CodecRegistries
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.io.File
import java.net.URL
import kotlin.test.assertFailsWith

@RunWith(AndroidJUnit4::class)
class AppConfigurationTests {

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

        // FIXME Add network check

        // FIXME Add sync session check
    }

    @Test
    fun addCustomRequestHeader_illegalArgumentThrows() {
        val builder: AppConfiguration.Builder = AppConfiguration.Builder("app-id")
        assertFailsWith<IllegalArgumentException> { builder.addCustomRequestHeader("", "val") }
        assertFailsWith<IllegalArgumentException> { builder.addCustomRequestHeader(TestHelper.getNull(), "val") }
        assertFailsWith<IllegalArgumentException> { builder.addCustomRequestHeader("header", TestHelper.getNull()) }
        // FIXME: Add tests for illegally formatted headers. Figure out what legal headers look like.
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

        // FIXME Add network check

        // FIXME Add sync session check
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

        // FIXME Add check when opening Realm
    }

    @Test
    fun syncRootDirectory() {
        val builder: AppConfiguration.Builder = AppConfiguration.Builder("app-id")
        val expectedRoot = tempFolder.newFolder()
        val config = builder
                .syncRootDirectory(expectedRoot)
                .build()
        assertEquals(expectedRoot, config.syncRootDirectory)

        // FIXME Add check when opening Realm
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
    @Ignore("FIXME")
    fun appName() {
        TODO("FIXME: When support has been added in ObjectStore")
    }

    @Test
    @Ignore("FIXME")
    fun appName_invalidValuesThrows() {
        TODO()
    }

    @Test
    @Ignore("FIXME")
    fun appVersion() {
        TODO("FIXME: When support has been added in ObjectStore")
    }

    @Test
    @Ignore("FIXME")
    fun appVersion_invalidValuesThrows() {
        TODO()
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
    @Ignore("FIXME")
    fun defaultSyncErrorHandler() {
        TODO()
    }

    @Test
    @Ignore("FIXME")
    fun defaultSyncErrorHandler_invalidValuesThrows() {
        TODO()
    }

    @Test
    @Ignore("FIXME")
    fun encryptionKey() {
        TODO()
    }

    @Test
    @Ignore("FIXME")
    fun encryptionKey_invalidValuesThrows() {
        TODO()
    }

    @Test
    @Ignore("FIXME")
    fun requestTimeout() {
        TODO()
    }

    @Test
    @Ignore("FIXME")
    fun requestTimeout_invalidValuesThrows() {
        TODO()
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
    fun loginInfoObfuscator_null() {
        AppConfiguration.Builder("app-id").let {
            assertFailsWith<IllegalArgumentException> {
                it.loginInfoObfuscator(null)
            }
        }
    }

    @Test
    fun defaultLoginInfoObfuscator() {
        AppConfiguration.Builder("app-id")
                .build()
                .let {
                    val defaultLoginInfoObfuscator = LoginInfoObfuscator(PatternObfuscatorFactory.getObfuscators())
                    assertEquals(defaultLoginInfoObfuscator, it.loginInfoObfuscator)
                }
    }
}
