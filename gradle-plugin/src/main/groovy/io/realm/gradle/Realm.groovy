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

package io.realm.gradle

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.neenbedankt.gradle.androidapt.AndroidAptPlugin
import io.realm.transformer.RealmTransformer
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class Realm implements Plugin<Project> {

    @Override
    void apply(Project project) {
        // Make sure the project is either an Android application or library
        def isAndroidApp = project.plugins.withType(AppPlugin)
        def isAndroidLib = project.plugins.withType(LibraryPlugin)
        if (!isAndroidApp && !isAndroidLib) {
            throw new GradleException("'com.android.application' or 'com.android.library' plugin required.")
        }

        if (!isTransformAvailable()) {
            throw new GradleException('Realm gradle plugin only supports android gradle plugin 1.5.0 or later.')
        }

        def usesKotlinPlugin = project.plugins.findPlugin('kotlin-android') != null
        def usesAptPlugin = project.plugins.findPlugin('com.neenbedankt.android-apt') != null

        def isKaptProject = usesKotlinPlugin && !usesAptPlugin

        if (!isKaptProject) {
            project.plugins.apply(AndroidAptPlugin)
        }

        project.android.registerTransform(new RealmTransformer(project))
        project.repositories.add(project.getRepositories().jcenter())
        project.dependencies.add("compile", "io.realm:realm-android-library:${Version.VERSION}")
        project.dependencies.add("compile", "io.realm:realm-annotations:${Version.VERSION}")
        if (isKaptProject) {
            project.dependencies.add("kapt", "io.realm:realm-annotations:${Version.VERSION}")
            project.dependencies.add("kapt", "io.realm:realm-annotations-processor:${Version.VERSION}")
        } else {
            project.dependencies.add("apt", "io.realm:realm-annotations:${Version.VERSION}")
            project.dependencies.add("apt", "io.realm:realm-annotations-processor:${Version.VERSION}")
            project.dependencies.add("androidTestApt", "io.realm:realm-annotations:${Version.VERSION}")
            project.dependencies.add("androidTestApt", "io.realm:realm-annotations-processor:${Version.VERSION}")
        }
    }

    private static boolean isTransformAvailable() {
        try {
            Class.forName('com.android.build.api.transform.Transform')
            return true
        } catch (Exception ignored) {
            return false
        }
    }
}
