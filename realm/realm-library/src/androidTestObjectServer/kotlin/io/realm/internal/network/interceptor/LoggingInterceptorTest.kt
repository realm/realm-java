///*
// * Copyright 2020 Realm Inc.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package io.realm.internal.network.interceptor
//
//import androidx.test.platform.app.InstrumentationRegistry
//import io.realm.Realm
//import io.realm.internal.network.OkHttpNetworkTransport
//import io.realm.internal.objectstore.OsJavaNetworkTransport
//import org.junit.Assert
//import org.junit.Before
//import org.junit.Test
//
//class LoggingInterceptorTest {
//
//    private lateinit var transport: OkHttpNetworkTransport
//    private val baseUrl = "http://127.0.0.1:8888" // URL to command server
//
//    enum class HTTPMethod(val nativeKey: String) {
//        GET("get"), POST("post"), PATCH("patch"), PUT("put"), DELETE("delete");
//    }
//
//    @Before
//    fun setUp() {
//        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
//        transport = OkHttpNetworkTransport(LoggingInterceptor.interceptor(null))
//    }
//
//    @Test
//    fun requestSuccessful() {
//        val url = "$baseUrl/okhttp?success=true"
//        for (method in HTTPMethod.values()) {
//            val body = if (method == HTTPMethod.GET) "" else "{ \"body\" : \"some content\" }"
//            val headers = mapOf(
//                    Pair("Content-Type", "application/json;charset=utf-8"),
//                    Pair("Accept", "application/json")
//            )
//
//            val response: OsJavaNetworkTransport.Response = transport.sendRequest(method.nativeKey,
//                    url,
//                    5000,
//                    headers,
//                    body)
//            Assert.assertEquals(200, response.httpResponseCode)
//            Assert.assertEquals(0, response.customResponseCode)
//            Assert.assertEquals("${method.name}-success", response.body)
//        }
//    }
//}
