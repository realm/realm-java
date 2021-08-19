/*
 * Copyright 2021 Realm Inc.
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

import io.realm.internal.network.OkHttpNetworkTransport
import io.realm.internal.objectstore.OsJavaNetworkTransport
import io.realm.mongodb.log.obfuscator.HttpLogObfuscator

/**
 * This class intercepts the response of an HTTP request instead of passing it on to ObjectStore.
 * This enables us to query it later.
 */

typealias Observer = (response: OsJavaNetworkTransport.Response) -> Unit

class HttpNetworkTransportInterceptor(private val passOnToObjectStore: Boolean = false,
                                      obfuscator: HttpLogObfuscator?) : OkHttpNetworkTransport(obfuscator) {

    private var observer: Observer? = null
    var preExecuteAction : (() -> Unit)? = null

    override fun handleResponse(
        response: OsJavaNetworkTransport.Response,
        completionBlockPtr: Long
    ) {
        observer?.let {
            it(response)
        }
        if (passOnToObjectStore) {
            super.handleResponse(response, completionBlockPtr)
        }
    }

    override fun executeRequest(
        method: String,
        url: String,
        timeoutMs: Long,
        headers: MutableMap<String, String>,
        body: String
    ): OsJavaNetworkTransport.Response {
        preExecuteAction?.let { it() }
        return super.executeRequest(method, url, timeoutMs, headers, body)
    }



    fun observeResponses(callback: (response: OsJavaNetworkTransport.Response) -> Unit) {
        observer = callback
    }
}
