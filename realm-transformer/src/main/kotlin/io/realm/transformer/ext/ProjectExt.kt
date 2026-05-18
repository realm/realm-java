/*
 * Copyright 2018 Realm Inc.
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
 *
 * --
 * Modified by Butterfly Network for AGP 9 compatibility. The legacy
 * `com.android.build.gradle.BaseExtension` API was removed in AGP 9; this file now
 * reads `bootClasspath` from `AndroidComponentsExtension.sdkComponents` and treats the
 * SDK-version analytics fields as best-effort (returning "unknown" if the modern DSL
 * shape ever changes again).
 */

package io.realm.transformer.ext

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Project
import java.io.File

/**
 * Attempts to determine the best possible unique AppId for this project.
 */
fun Project.getAppId(): String {
    // Use the Root project name, usually set in `settings.gradle`
    return this.rootProject.name
}

/**
 * Returns the `targetSdk` property for this project if it is available.
 * Reflectively reads `defaultConfig.targetSdk` so the same code works against both
 * `ApplicationExtension` (which has it) and `LibraryExtension` (which does not).
 */
fun Project.getTargetSdk(): String = readDefaultConfigInt("getTargetSdk")

/**
 * Returns the `minSdk` property for this project if it is available.
 */
fun Project.getMinSdk(): String = readDefaultConfigInt("getMinSdk")

private fun Project.readDefaultConfigInt(getter: String): String =
    runCatching {
        @Suppress("UnstableApiUsage")
        val ext = extensions.getByType(CommonExtension::class.java)
        val defaultConfig: Any = ext.defaultConfig
        val method = defaultConfig.javaClass.methods.firstOrNull { it.name == getter && it.parameterCount == 0 }
        method?.invoke(defaultConfig)?.toString() ?: "unknown"
    }.getOrDefault("unknown")

/**
 * Returns the version of the Android Gradle Plugin that is used.
 */
fun Project.getAgpVersion(): String {
    return try {
        val extension = this.extensions.getByName("androidComponents") as Object
        val method = extension.`class`.getMethod("getPluginVersion")
        val version = method.invoke(extension)
        version?.toString() ?: "unknown"
    } catch (e: Exception) {
        "unknown"
    }
}

/**
 * Returns the `bootClasspath` for this project. AGP 9 exposes this via the modern
 * `AndroidComponentsExtension.sdkComponents.bootClasspath` provider; we resolve it
 * eagerly here because the transformer task needs a `List<File>`.
 */
fun Project.getBootClasspath(): List<File> {
    val androidComponents = extensions.getByType(AndroidComponentsExtension::class.java)
    return androidComponents.sdkComponents.bootClasspath.get().map { it.asFile }
}

fun Project.areIncrementalBuildsDisabled() =
    if (extensions.extraProperties.has("io.realm.disableIncrementalBuilds")) {
        extensions.extraProperties["io.realm.disableIncrementalBuilds"] == "true"
    } else {
        false
    }

fun Project.targetType(): String = with(project.plugins) {
    when {
        findPlugin("com.android.application") != null -> "app"
        findPlugin("com.android.library") != null -> "library"
        else -> "unknown"
    }
}

fun Project.usesKotlin(): Boolean {
    return project.pluginManager.hasPlugin("kotlin-kapt")
}
