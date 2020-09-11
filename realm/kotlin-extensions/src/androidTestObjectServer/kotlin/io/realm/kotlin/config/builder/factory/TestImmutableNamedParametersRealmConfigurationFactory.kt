/*
 * Copyright 2016 Realm Inc.
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
package io.realm.kotlin.config.builder.factory

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.kotlin.config.builder.ImmutableNamedParametersRealmConfigBuilder
import org.junit.rules.TemporaryFolder
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertTrue

/**
 * Rule that creates the [RealmConfiguration] in a temporary directory and deletes the Realm created with that
 * configuration once the test finishes. Be sure to close all Realm instances before finishing the test. Otherwise
 * [Realm.deleteRealm] will throw an exception in the [.after] method.
 * The temp directory will be deleted regardless if the [Realm.deleteRealm] fails or not.
 */
class TestImmutableNamedParametersRealmConfigurationFactory : TemporaryFolder() {

    private val map: Map<RealmConfiguration, Boolean> = ConcurrentHashMap()
    private val configurations = Collections.newSetFromMap(map)

    @get:Synchronized
    private var isUnitTestFailed = false
    private var testName = ""
    private var tempFolder: File? = null

    private lateinit var context: Context

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                setTestName(description)
                before()
                try {
                    base.evaluate()
                } catch (throwable: Throwable) {
                    setUnitTestFailed()
                    throw throwable
                } finally {
                    after()
                }
            }
        }
    }

    @Throws(Throwable::class)
    override fun before() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        Realm.init(context)
        super.before()
    }

    override fun after() {
        try {
            for (configuration in configurations) {
                Realm.deleteRealm(configuration!!)
            }
        } catch (e: IllegalStateException) {
            // Only throws the exception caused by deleting the opened Realm if the test case itself doesn't throw.
            if (!isUnitTestFailed) {
                throw e
            }
        } finally {
            // This will delete the temp directory.
            super.after()
        }
    }

    @Throws(IOException::class)
    override fun create() {
        super.create()
        tempFolder = File(super.getRoot(), testName)
        check(!(tempFolder!!.exists() && !tempFolder!!.delete())) { "Could not delete folder: " + tempFolder!!.absolutePath }
        check(tempFolder!!.mkdir()) { "Could not create folder: " + tempFolder!!.absolutePath }
    }

    override fun getRoot(): File {
        return checkNotNull(tempFolder) { "the temporary folder has not yet been created" }
    }

    /**
     * To be called in the [.apply].
     */
    protected fun setTestName(description: Description) {
        testName = description.displayName
    }

    @Synchronized
    fun setUnitTestFailed() {
        isUnitTestFailed = true
    }

    // This builder creates a configuration that is *NOT* managed.
    // You have to delete it yourself.
    fun createConfigurationBuilder(): ImmutableNamedParametersRealmConfigBuilder {
        return ImmutableNamedParametersRealmConfigBuilder(
                context = context
        )
    }

    fun createConfiguration(): RealmConfiguration {
        return ImmutableNamedParametersRealmConfigBuilder(
                context = context
        ).build()
    }

    fun createConfiguration(name: String): RealmConfiguration {
        return ImmutableNamedParametersRealmConfigBuilder(
                context = context,
                fileName = name
        ).build()
    }

    fun createConfiguration(name: String, key: ByteArray): RealmConfiguration {
        return ImmutableNamedParametersRealmConfigBuilder(
                context = context,
                fileName = name,
                key = key
        ).build()
    }

    fun createConfiguration(name: String, module: Any): RealmConfiguration {
        return ImmutableNamedParametersRealmConfigBuilder(
                context = context,
                fileName = name,
                providedModules = listOf(module)
        ).build()
    }

    fun createConfiguration(subDir: String, name: String, module: Any, key: ByteArray): RealmConfiguration? {
        var folder = root
        folder = File(folder, subDir)
        assertTrue(folder.mkdirs())

        val configuration = ImmutableNamedParametersRealmConfigBuilder(
                context = context,
                fileName = name,
                key = key,
                directory = folder,
                providedModules = listOf(module)
        ).build()

        configurations.add(configuration)
        return configuration
    }

    // Copies a Realm file from assets to temp dir
    @Throws(IOException::class)
    fun copyRealmFromAssets(context: Context, realmPath: String, newName: String) {
        val config: RealmConfiguration = ImmutableNamedParametersRealmConfigBuilder(
                context = context,
                directory = root,
                fileName = newName
        ).build()
        copyRealmFromAssets(context, realmPath, config)
    }

    @Throws(IOException::class)
    fun copyRealmFromAssets(context: Context, realmPath: String?, config: RealmConfiguration) {
        check(!File(config.path).exists()) { String.format(Locale.ENGLISH, "%s exists!", config.path) }
        val outFile = File(config.realmDirectory, config.realmFileName)
        copyFileFromAssets(context, realmPath, outFile)
    }

    @Throws(IOException::class)
    fun copyFileFromAssets(context: Context, assetPath: String?, outFile: File?) {
        var `is`: InputStream? = null
        var os: FileOutputStream? = null
        try {
            `is` = context.assets.open(assetPath!!)
            os = FileOutputStream(outFile)
            val buf = ByteArray(1024)
            var bytesRead: Int
            while (`is`.read(buf).also { bytesRead = it } > -1) {
                os.write(buf, 0, bytesRead)
            }
        } finally {
            if (`is` != null) {
                try {
                    `is`.close()
                } catch (ignore: IOException) {
                }
            }
            if (os != null) {
                try {
                    os.close()
                } catch (ignore: IOException) {
                }
            }
        }
    }
}
