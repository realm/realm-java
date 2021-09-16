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
package io.realm.analytics

import io.realm.transformer.Utils
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.SocketException
import java.net.URL
import java.security.NoSuchAlgorithmException

class UrlEncodedAnalytics private constructor(private val prefix: String, private val suffix: String) {

    /**
     * Send the analytics event to the server.
     */
    fun execute(analytics: AnalyticsData) {
        try {
            val url = getUrl(analytics)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()
            connection.responseCode
        } catch (ignored: Exception) {
        }
    }

    @Throws(
        MalformedURLException::class,
        SocketException::class,
        NoSuchAlgorithmException::class,
        UnsupportedEncodingException::class
    )
    private fun getUrl(analytics: AnalyticsData): URL {
        logger.error("Sending: \n${analytics.generateJson()}")
        return URL(prefix + Utils.base64Encode(analytics.generateJson()) + suffix)
    }

    companion object {
        fun create(): UrlEncodedAnalytics {
            val ADDRESS_PREFIX =
                "https://webhooks.mongodb-realm.com/api/client/v2.0/app/realmsdkmetrics-zmhtm/service/metric_webhook/incoming_webhook/metric?data="
            val ADDRESS_SUFFIX = ""
            return UrlEncodedAnalytics(ADDRESS_PREFIX, ADDRESS_SUFFIX)
        }
    }
}