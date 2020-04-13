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
import io.realm.util.expectException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.IllegalArgumentException

@RunWith(AndroidJUnit4::class)
class RealmAppConfigurationTests {

    // FIXME: Add tests for remaining builder methods
    //    builder.appName()
    //    builder.appVersion()
    //    builder.baseUrl()
    //    builder.defaultSyncErrorHandler()
    //    builder.encryptionKey()
    //    builder.logLevel()
    //    builder.requestTimeout()

    @Test
    fun authorizationHeaderName_illegalArgumentsThrows() {
        val builder: RealmAppConfiguration.Builder = RealmAppConfiguration.Builder("app-id")
        expectException<IllegalArgumentException> { builder.authorizationHeaderName(TestHelper.getNull()) }
        expectException<IllegalArgumentException> { builder.authorizationHeaderName("") }
    }

    @Test
    fun authorizationHeaderName() {
        val config1 = RealmAppConfiguration.Builder("app-id").build()
        assertEquals("Authorization", config1.authorizationHeaderName)

        val config2 = RealmAppConfiguration.Builder("app-id")
                .authorizationHeaderName("CustomAuth")
                .build()
        assertEquals("CustomAuth", config2.authorizationHeaderName)
    }

    @Test
    fun addCustomRequestHeader_illegalArgumentThrows() {
        val builder: RealmAppConfiguration.Builder = RealmAppConfiguration.Builder("app-id")
        expectException<IllegalArgumentException> { builder.addCustomRequestHeader("", "val") }
        expectException<IllegalArgumentException> { builder.addCustomRequestHeader("header", "") }
        expectException<IllegalArgumentException> { builder.addCustomRequestHeader(TestHelper.getNull(), "val") }
        expectException<IllegalArgumentException> { builder.addCustomRequestHeader("header", TestHelper.getNull()) }
    }

    @Test
    fun addCustomRequestHeader() {
        val config = RealmAppConfiguration.Builder("app-id")
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
        val config = RealmAppConfiguration.Builder("app-id")
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
        val app = TestRealmApp()
        val config = RealmAppConfiguration.Builder("app-id")
                .addCustomRequestHeader("header3", "val3")
                .addCustomRequestHeaders(mapOf(Pair("header1", "val1")))
                .build()
        val headers: Map<String, String> = config.customRequestHeaders
        assertEquals(2, headers.size)
        assertTrue(headers.any { it.key == "header3" && it.value == "val3" })
        assertTrue(headers.any { it.key == "header1" && it.value == "val1" })
    }
}
