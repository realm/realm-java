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
import io.realm.*
import io.realm.internal.network.OkHttpNetworkTransport
import io.realm.internal.objectstore.OsJavaNetworkTransport
import io.realm.mongodb.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This class is responsible for testing the general network transport layer, i.e. that
 * requests can round trip correctly through all layers and that exceptions/errors are reported
 * correctly.
 *
 * This class should _NOT_ test any real network logic. See [OkHttpNetworkTransportTests] for
 * tests using the actual network implementation.
 */
@RunWith(AndroidJUnit4::class)
class OsJavaNetworkTransportTests {

    private lateinit var app: App
    private val successHeaders: Map<String, String> = mapOf(Pair("Content-Type", "application/json"))

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @After
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
    }

    // Test that the round trip works in case of a successful HTTP request.
    @Test
    fun requestSuccess() {
        app = TestApp(object: OsJavaNetworkTransport() {
            override fun sendRequest(method: String, url: String, timeoutMs: Long, headers: MutableMap<String, String>, body: String): Response {
                var result = ""
                if (url.endsWith("/providers/${Credentials.IdentityProvider.ANONYMOUS.id}/login")) {
                    result = """
                        {
                            "access_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjVlNjk2M2RmYWZlYTYzMjU0NTgxYzAyNiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1ODM5NjcyMDgsImlhdCI6MTU4Mzk2NTQwOCwiaXNzIjoiNWU2OTY0ZTBhZmVhNjMyNTQ1ODFjMWEzIiwic3RpdGNoX2RldklkIjoiMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwIiwic3RpdGNoX2RvbWFpbklkIjoiNWU2OTYzZGVhZmVhNjMyNTQ1ODFjMDI1Iiwic3ViIjoiNWU2OTY0ZTBhZmVhNjMyNTQ1ODFjMWExIiwidHlwIjoiYWNjZXNzIn0.J4mp8LnlsxTQRV_7W2Er4qY0tptR76PJGG1k6HSMmUYqgfpJC2Fnbcf1VCoebzoNolH2-sr8AHDVBBCyjxRjqoY9OudFHmWZKmhDV1ysxPP4XmID0nUuN45qJSO8QEAqoOmP1crXjrUZWedFw8aaCZE-bxYfvcDHyjBcbNKZqzawwUw2PyTOlrNjgs01k2J4o5a5XzYkEsJuzr4_8UqKW6zXvYj24UtqnqoYatW5EzpX63m2qig8AcBwPK4ZHb5wEEUdf4QZxkRY5QmTgRHP8SSqVUB_mkHgKaizC_tSB3E0BekaDfLyWVC1taAstXJNfzgFtLI86AzuXS2dCiCfqQ",
                            "refresh_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjVlNjk2M2RmYWZlYTYzMjU0NTgxYzAyNiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1ODkxNDk0MDgsImlhdCI6MTU4Mzk2NTQwOCwic3RpdGNoX2RhdGEiOm51bGwsInN0aXRjaF9kZXZJZCI6IjAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMCIsInN0aXRjaF9kb21haW5JZCI6IjVlNjk2M2RlYWZlYTYzMjU0NTgxYzAyNSIsInN0aXRjaF9pZCI6IjVlNjk2NGUwYWZlYTYzMjU0NTgxYzFhMyIsInN0aXRjaF9pZGVudCI6eyJpZCI6IjVlNjk2NGUwYWZlYTYzMjU0NTgxYzFhMC1oaWF2b3ZkbmJxbGNsYXBwYnl1cmJpaW8iLCJwcm92aWRlcl90eXBlIjoiYW5vbi11c2VyIiwicHJvdmlkZXJfaWQiOiI1ZTY5NjNlMGFmZWE2MzI1NDU4MWMwNGEifSwic3ViIjoiNWU2OTY0ZTBhZmVhNjMyNTQ1ODFjMWExIiwidHlwIjoicmVmcmVzaCJ9.FhLdpmL48Mw0SyUKWuaplz3wfeS8TCO8S7I9pIJenQww9nPqQ7lIvykQxjCCtinGvsZIJKt_7R31xYCq4Jp53Nw81By79IwkXtO7VXHPsXXZG5_2xV-s0u44e85sYD5su_H-xnx03sU2piJbWJLSB8dKu3rMD4mO-S0HNXCCAty-JkYKSaM2-d_nS8MNb6k7Vfm7y69iz_uwHc-bb_1rPg7r827K6DEeEMF41Hy3Nx1kCdAUOM9-6nYv3pZSU1PFrGYi2uyTXPJ7R7HigY5IGHWd0hwONb_NUr4An2omqfvlkLEd77ut4V9m6mExFkoKzRz7shzn-IGkh3e4h7ECGA",
                            "user_id": "5e6964e0afea63254581c1a1",
                            "device_id": "000000000000000000000000"
                        }                    
                    """.trimIndent()
                } else if (url.endsWith("/auth/profile")) {
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
                } else if (url.endsWith("/location")) {
                    return OkHttpNetworkTransport.Response.httpResponse(200, mapOf(), """
                        { "deployment_model" : "GLOBAL",
                          "location": "US-VA", 
                          "hostname": "http://localhost:9090",
                          "ws_hostname": "ws://localhost:9090"
                        }
                        """.trimIndent())
                } else {
                    fail("Unexpected request url: $url")
                }
                return OkHttpNetworkTransport.Response.httpResponse(200, successHeaders, result)
            }

            override fun sendStreamingRequest(request: Request): Response {
                throw IllegalAccessError()
            }
        })

        val creds = Credentials.anonymous()
        val user: User = app.login(creds)
        assertNotNull(user)
    }

    // Test that the server accepting the result but returns an error is succesfully reported back
    // to the user as an exception.
    @Test
    fun requestFailWithServerError() {
        app = TestApp(object: OsJavaNetworkTransport() {
            override fun sendRequest(method: String, url: String, timeoutMs: Long, headers: MutableMap<String, String>, body: String): Response {
                val result = """
                    {
                        "error": "invalid username/password",
                        "error_code": "AuthError",
                        "link": "http://localhost:9090/some_link"
                    }                
                    """.trimIndent()
                return OkHttpNetworkTransport.Response.httpResponse(200, successHeaders, result)
            }

            override fun sendStreamingRequest(request: Request): Response {
                throw IllegalAccessError()
            }
        })

        val creds = Credentials.emailPassword("foo", "bar")
        try {
            app.login(creds)
            fail()
        } catch (ex: AppException) {
            assertEquals(ErrorCode.AUTH_ERROR, ex.errorCode)
            assertEquals(ErrorCode.Type.SERVICE, ex.errorType)
        }
    }

    // Test that the server failing to respond with a non-200 status code returns a proper exception
    // to the user.
    @Test
    fun requestFailWithHttpError() {
        app = TestApp(object: OsJavaNetworkTransport() {
            override fun sendRequest(method: String, url: String, timeoutMs: Long, headers: MutableMap<String, String>, body: String): Response {
                return OkHttpNetworkTransport.Response.httpResponse(500, mapOf(), "Boom!")
            }

            override fun sendStreamingRequest(request: Request): Response {
                throw IllegalAccessError()
            }
        })

        val creds = Credentials.anonymous()
        try {
            app.login(creds)
            fail()
        } catch (ex: AppException) {
            assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, ex.errorCode)
            assertEquals(ErrorCode.Type.HTTP, ex.errorType)
        }
    }

    // Test that custom error codes thrown from the Java transport are correctly reported back to the user.
    @Test
    fun requestFailWithCustomError() {
        app = TestApp(object: OsJavaNetworkTransport() {
            override fun sendRequest(method: String, url: String, timeoutMs: Long, headers: MutableMap<String, String>, body: String): Response {
                return OkHttpNetworkTransport.Response.ioError("Boom!")
            }

            override fun sendStreamingRequest(request: Request): Response {
                throw IllegalAccessError()
            }
        })

        val creds = Credentials.anonymous()
        try {
            app.login(creds)
            fail()
        } catch (ex: AppException) {
            assertEquals(ErrorCode.NETWORK_IO_EXCEPTION, ex.errorCode)
            assertEquals(ErrorCode.Type.JAVA, ex.errorType)
        }
    }


    // Test that if the Java transport throws an uncaught exception it is correctly returned
    // to the user.
    @Test
    fun requestFailWithTransportException() {
        app = TestApp(object: OsJavaNetworkTransport() {
            override fun sendRequest(method: String, url: String, timeoutMs: Long, headers: MutableMap<String, String>, body: String): Response {
                throw IllegalStateException("Boom!")
            }

            override fun sendStreamingRequest(request: Request): Response {
                throw IllegalAccessError()
            }
        })

        val creds = Credentials.anonymous()
        try {
            app.login(creds)
            fail()
        } catch (ex: IllegalStateException) {
            assertEquals("Boom!", ex.message)
        }
    }

    // Test that if the Java transport throws a fatal error it is correctly returned to the user.
    @Test
    fun requestFailWithTransportError() {
        app = TestApp(object: OsJavaNetworkTransport() {
            override fun sendRequest(method: String, url: String, timeoutMs: Long, headers: MutableMap<String, String>, body: String): Response {
                throw Error("Boom!")
            }

            override fun sendStreamingRequest(request: Request): Response {
                throw IllegalAccessError()
            }
        })

        val creds = Credentials.anonymous()
        try {
            app.login(creds)
            fail()
        } catch (ex: Error) {
            assertEquals("Boom!", ex.message)
        }
    }
}

