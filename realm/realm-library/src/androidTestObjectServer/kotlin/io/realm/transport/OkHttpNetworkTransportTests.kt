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
package io.realm.transport

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.Realm
import io.realm.internal.network.OkHttpNetworkTransport
import io.realm.internal.objectstore.OsJavaNetworkTransport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This class is responsible for testing the OkHttp implementation of the network layer.
 * Any behavior happening after the network request has executed are not covered by this class,
 * but instead in [OsJavaNetworkTransportTests].
 *
 * This class uses a simple custom webserver written in Node that must be running when
 * executing these tests.
 */
@RunWith(AndroidJUnit4::class)
class OkHttpNetworkTransportTests {

    private lateinit var transport: OkHttpNetworkTransport
    private val baseUrl = "http://127.0.0.1:8888" // URL to command server

    enum class HTTPMethod(val nativeKey: String) {
        GET("get"), POST("post"), PATCH("patch"), PUT("put"), DELETE("delete");
    }

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        transport = OkHttpNetworkTransport(null)
    }

    @Test
    fun requestSuccessful() {
        val url = "$baseUrl/okhttp?success=true"
        for (method in HTTPMethod.values()) {
            val body = if (method == HTTPMethod.GET) "" else "{ \"body\" : \"some content\" }"
            val headers = mapOf(
                    Pair("Content-Type", "application/json;charset=utf-8"),
                    Pair("Accept", "application/json")
            )

            val response: OsJavaNetworkTransport.Response = transport.sendRequest(method.nativeKey,
                    url,
                    5000,
                    headers,
                    body)
            assertEquals(200, response.httpResponseCode)
            assertEquals(0, response.customResponseCode)
            assertEquals("${method.name}-success", response.body)
        }
    }

    @Test
    fun requestFailedOnServer() {
        val url = "$baseUrl/okhttp?success=false"
        for (method in HTTPMethod.values()) {
            val body = if (method == HTTPMethod.GET) "" else "{ \"body\" : \"some content\" }"
            val headers = mapOf(
                    Pair("Content-Type", "application/json;charset=utf-8"),
                    Pair("Accept", "application/json")
            )

            val response: OsJavaNetworkTransport.Response = transport.sendRequest(method.nativeKey,
                    url,
                    5000,
                    headers,
                    body)
            assertEquals(500, response.httpResponseCode)
            assertEquals(0, response.customResponseCode)
            assertEquals("${method.name}-failure", response.body)
        }
    }

    // Make sure that the client doesn't crash if attempting to send invalid JSON
    // This is mostly a guard against Java crashing if ObjectStore serializes the wrong
    // way by accident.
    @Test
    fun requestSendsIllegalJson() {
        val url = "$baseUrl/okhttp?success=true"
        for (method in HTTPMethod.values()) {
            val body = if (method == HTTPMethod.GET) "" else "Boom!"
            val headers = mapOf(
                    Pair("Content-Type", "application/json;charset=utf-8"),
                    Pair("Accept", "application/json")
            )

            val response: OsJavaNetworkTransport.Response = transport.sendRequest(method.nativeKey,
                    url,
                    5000,
                    headers,
                    body)
            assertEquals(200, response.httpResponseCode)
            assertEquals(0, response.customResponseCode)
            assertEquals("${method.name}-success", response.body)
        }
    }


    // Validate that we can access a text/event-stream streamed response
    @Test
    fun streamRequest() {
        val url = "$baseUrl/watcher"

        val headers = mapOf(
                Pair("Accept", "text/event-stream")
        )

        val request = OsJavaNetworkTransport.Request("get", url, headers, "")
        val response = transport.sendStreamingRequest(request)

        assertEquals(200, response.httpResponseCode)
        assertEquals(0, response.customResponseCode)
        assertEquals("hello world 1", response.readBodyLine())
        assertEquals("hello world 2", response.readBodyLine())

        response.close()
    }


    @Test
    fun requestInterrupted() {
        val url = "$baseUrl/okhttp?success=true"
        for (method in HTTPMethod.values()) {
            val body = if (method == HTTPMethod.GET) "" else "{ \"body\" : \"some content\" }"
            val headers = mapOf(
                    Pair("Content-Type", "application/json;charset=utf-8"),
                    Pair("Accept", "application/json")
            )

            Thread.currentThread().interrupt()
            val response: OsJavaNetworkTransport.Response = transport.sendRequest(method.nativeKey,
                    url,
                    5000,
                    headers,
                    body)
            assertEquals(0, response.httpResponseCode)
            assertEquals(OsJavaNetworkTransport.ERROR_IO, response.customResponseCode)
            assertTrue(response.body.contains("interrupted"))
        }
    }

    @Ignore("Add test for this")
    @Test
    fun customAuthorizationHeader() {
        TODO("FIXME")
    }

    @Ignore("Add test for this")
    @Test
    fun customHeaders() {
        TODO("FIXME")
    }
    
}
