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
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.NetworkInterface
import java.net.SocketException
import java.security.NoSuchAlgorithmException
import java.util.*

/**
 * Generate a unique identifier for a computer. The method being used depends on the platform:
 * - OS X:  Mac address of en0
 * - Windows:  BIOS identifier
 * - Linux: Machine ID provided by the OS
 */
class ComputerIdentifierGenerator {
    companion object {
        private const val UNKNOWN = "unknown"
        private val OS: String = System.getProperty("os.name").toLowerCase()
        private val isWindows: Boolean = OS.contains("win")
        private val isMac: Boolean = OS.contains("mac")
        private val isLinux: Boolean = OS.contains("inux")

        fun get(): String {
            return try {
                when {
                    isWindows -> getWindowsIdentifier()
                    isMac -> getMacOsIdentifier()
                    isLinux -> getLinuxMacAddress()
                    else -> UNKNOWN
                }
            } catch (e: Exception) {
                UNKNOWN
            }
        }

        @Throws(FileNotFoundException::class, NoSuchAlgorithmException::class)
        private fun  getLinuxMacAddress(): String {
            var machineId = File("/var/lib/dbus/machine-id")
            if (!machineId.exists()) {
                machineId = File("/etc/machine-id")
            }
            if (!machineId.exists()) {
                return UNKNOWN
            }
            var scanner: Scanner? = null
            return try {
                scanner = Scanner(machineId)
                val id = scanner.useDelimiter("\\A").next()
                Utils.hexStringify(Utils.sha256Hash(id.toByteArray()))
            } finally {
                scanner?.close()
            }
        }

        @Throws(SocketException::class, NoSuchAlgorithmException::class)
        private fun getMacOsIdentifier(): String {
            val networkInterface = NetworkInterface.getByName("en0")
            val hardwareAddress = networkInterface.hardwareAddress
            return Utils.hexStringify(Utils.sha256Hash(hardwareAddress))
        }

        @Throws(IOException::class, NoSuchAlgorithmException::class)
        private fun getWindowsIdentifier(): String {
            val runtime = Runtime.getRuntime()
            val process = runtime.exec(arrayOf("wmic", "csproduct", "get", "UUID"))
            var result: String? = null
            val `is` = process.inputStream
            val sc = Scanner(process.inputStream)
            try {
                while (sc.hasNext()) {
                    val next = sc.next()
                    if (next.contains("UUID")) {
                        result = sc.next().trim { it <= ' ' }
                        break
                    }
                }
            } finally {
                `is`.close()
            }
            return if (result == null) UNKNOWN else Utils.hexStringify(Utils.sha256Hash(result.toByteArray()))
        }
    }
}