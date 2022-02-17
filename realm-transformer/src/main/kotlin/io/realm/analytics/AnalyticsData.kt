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
import io.realm.transformer.Version
import java.net.SocketException
import java.security.NoSuchAlgorithmException

inline class PublicAppId(val id: String) {
    fun anonymize(): String {
        val idBytes: ByteArray = id.toByteArray()
        return Utils.hexStringify(Utils.sha256Hash(idBytes))
    }
}

/**
 * Class wrapping data we want to send as analytics data.
 */
data class AnalyticsData(
    val appId: PublicAppId,
    val usesKotlin: Boolean,
    val usesSync: Boolean,
    val targetSdk: String,
    val minSdk: String,
    val target:String,
    val gradleVersion: String,
    val agpVersion: String
) {

    private val TOKEN = "ce0fac19508f6c8f20066d345d360fd0"
    private val EVENT_NAME = "Run"
    private val JSON_TEMPLATE = """
        {
          "event": "%EVENT%",
          "properties": {
              "token": "%TOKEN%",
              "distinct_id": "%USER_ID%",
              "Anonymized MAC Address": "%USER_ID%",
              "Anonymized Bundle ID": "%APP_ID%",
              "Binding": "java",
              "Target": "%TARGET%",
              "Language": "%LANGUAGE%",
              "Sync Version": %SYNC_VERSION%,
              "Realm Version": "%REALM_VERSION%",
              "Host OS Type": "%OS_TYPE%",
              "Host OS Version": "%OS_VERSION%",
              "Target OS Type": "android",
              "Target OS Version": "%TARGET_SDK%",
              "Target OS Minimum Version": "%MIN_SDK%",
              "Gradle version": "%GRADLE_VERSION%",
              "Android Gradle Plugin Version": "%AGP_VERSION%"
          }
        }
    """.trimIndent()

    @Throws(SocketException::class, NoSuchAlgorithmException::class)
    fun generateJson(): String {
        return JSON_TEMPLATE
            .replace("%EVENT%".toRegex(), EVENT_NAME)
            .replace("%TOKEN%".toRegex(), TOKEN)
            .replace("%USER_ID%".toRegex(), ComputerIdentifierGenerator.get())
            .replace("%APP_ID%".toRegex(), appId.anonymize())
            .replace("%TARGET%".toRegex(), target)
            .replace("%LANGUAGE%".toRegex(), if (usesKotlin) "kotlin" else "java")
            .replace(
                "%SYNC_VERSION%".toRegex(),
                if (usesSync) "\"" + Version.SYNC_VERSION + "\"" else "null"
            )
            .replace("%REALM_VERSION%".toRegex(), Version.VERSION)
            .replace("%OS_TYPE%".toRegex(), System.getProperty("os.name"))
            .replace("%OS_VERSION%".toRegex(), System.getProperty("os.version"))
            .replace("%TARGET_SDK%".toRegex(), targetSdk)
            .replace("%MIN_SDK%".toRegex(), minSdk)
            .replace("%GRADLE_VERSION%".toRegex(), gradleVersion)
            .replace("%AGP_VERSION%".toRegex(), agpVersion)
    }
}