/*
 * Copyright 2019 Realm Inc.
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

package io.realm.processor

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic


class RealmVersionChecker private constructor(private val processingEnvironment: ProcessingEnvironment) {

    private val REALM_ANDROID_DOWNLOAD_URL = "https://static.realm.io/downloads/java/latest"
    private val VERSION_URL = "https://static.realm.io/update/java?"
    private val REALM_VERSION = Version.VERSION
    private val REALM_VERSION_PATTERN = "\\d+\\.\\d+\\.\\d+"
    private val READ_TIMEOUT = 2000
    private val CONNECT_TIMEOUT = 4000

    fun executeRealmVersionUpdate() {
        val backgroundThread = Thread(Runnable { launchRealmCheck() })
        backgroundThread.start()
        try {
            backgroundThread.join((CONNECT_TIMEOUT + READ_TIMEOUT).toLong())
        } catch (ignore: InterruptedException) {
            // We ignore this exception on purpose not to break the build system if this class fails
        }
    }

    private fun launchRealmCheck() {
        //Check Realm version server
        val latestVersionStr = checkLatestVersion()
        if (latestVersionStr != REALM_VERSION) {
            printMessage("Version $latestVersionStr of Realm is now available: $REALM_ANDROID_DOWNLOAD_URL")
        }
    }

    private fun checkLatestVersion(): String {
        var result = REALM_VERSION
        try {
            val url = URL(VERSION_URL + REALM_VERSION)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = CONNECT_TIMEOUT
            conn.readTimeout = READ_TIMEOUT
            val rd = BufferedReader(InputStreamReader(conn.inputStream))
            val latestVersion = rd.readLine()
            // if the obtained string does not match the pattern, we are in a separate network.
            if (latestVersion.matches(REALM_VERSION_PATTERN.toRegex())) {
                result = latestVersion
            }
            rd.close()
        } catch (e: IOException) {
            // We ignore this exception on purpose not to break the build system if this class fails
        }

        return result
    }

    private fun printMessage(message: String) {
        processingEnvironment.messager.printMessage(Diagnostic.Kind.OTHER, message)
    }

    companion object {
        private var instance: RealmVersionChecker? = null
        fun getInstance(env: ProcessingEnvironment): RealmVersionChecker {
            if (instance == null) {
                synchronized(RealmVersionChecker::class.java) {
                    if (instance == null) {
                        instance = RealmVersionChecker(env)
                    }
                }
            }
            return instance!!
        }
    }
}
