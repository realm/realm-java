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
 */

package io.realm.transformer.ext

import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import java.io.File

/**
 * Attempts to determine the best possible unique AppId for this project.
 */
fun Project.getAppId(): String {
    // Use the Root project name, usually set in `settings.gradle`
    // This means that we don't treat apps with multiple flavours as different, nor
    // if a project contains more than one app (probably unlikely).
    // This seems acceptable. These cases would just show up as more builds for the
    // same AppId.
    return this.rootProject.name
}

/**
 * Returns the `targetSdk` property for this project if it is available.
 */
fun Project.getTargetSdk(): String {
    return getAndroidExtension(this).defaultConfig.targetSdkVersion?.apiString ?: "unknown"
}

/**
 * Returns the `minSdk` property for this project if it is available.
 */
fun Project.getMinSdk(): String {
    return getAndroidExtension(this).defaultConfig.minSdkVersion?.apiString ?: "unknown"
}

/**
 * Returns the version of the Android Gradle Plugin that is used.
 */
fun Project.getAgpVersion(): String {
    // This API is only available from AGP 7.0.0. And it is a bit unclear exactly which part of
    // this is actually stable. Also, there appear to be problems with depending on AGP 7.* on
    // the compile classpath (it cannot load BaseExtension).
    //
    // So for now, this code assumes that we are compiling against AGP 4.1 and uses reflection
    // to try to grap the AGP version.
    //
    // This is done with a best-effort, but we just
    // accept finding the version isn't possible if anything goes wrong.
    return try {
        val extension = this.extensions.getByName("androidComponents") as Object
        val method = extension.`class`.getMethod("getPluginVersion")
        val version = method.invoke(extension)
        if (version != null) {
            return version.toString()
        } else {
            return "unknown"
        }
    } catch (e: Exception) {
        "unknown"
    }
}

/**
 * Returns the `bootClasspath` for this project
 */
fun Project.getBootClasspath(): List<File> {
    return getAndroidExtension(this).bootClasspath
}

private fun getAndroidExtension(project: Project): BaseExtension {
    // This will always be present, otherwise the android build would not be able to
    // trigger the transformer code in the first place.
    return project.extensions.getByName("android") as BaseExtension
}
