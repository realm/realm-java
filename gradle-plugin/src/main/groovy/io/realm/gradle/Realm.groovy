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
import io.realm.gradle.RealmPluginExtension
import io.realm.transformer.RealmTransformer
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.UnknownConfigurationException

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

        def syncEnabledDefault = false
        def usesAptPlugin = project.plugins.findPlugin('com.neenbedankt.android-apt') != null
        def isKotlinProject = project.plugins.findPlugin('kotlin-android') != null
        def useKotlinExtensionsDefault = isKotlinProject
        def hasAnnotationProcessorConfiguration = project.getConfigurations().findByName('annotationProcessor') != null
        // TODO add a parameter in 'realm' block if this should be specified by users
        def preferAptOnKotlinProject = false
        def dependencyConfigurationName = getDependencyConfigurationName(project)
        def extension = project.extensions.create('realm', RealmPluginExtension)
        extension.addPropertyListener(RealmPluginExtension.KEY_KOTLIN_EXTENSIONS_ENABLED, new RealmPluginExtension.PropertyChangedListener<Boolean>() {
            @Override
            void onChange(Boolean checked) {
                setDependencies(project, dependencyConfigurationName, extension.syncEnabled, extension.kotlinExtensionsEnabled)
            }
        })
        extension.addPropertyListener(RealmPluginExtension.KEY_SYNC_ENABLED, new RealmPluginExtension.PropertyChangedListener<Boolean>() {
            @Override
            void onChange(Boolean checked) {
                setDependencies(project, dependencyConfigurationName, extension.syncEnabled, extension.kotlinExtensionsEnabled)
            }
        })
        extension.kotlinExtensionsEnabled = useKotlinExtensionsDefault

        if (shouldApplyAndroidAptPlugin(usesAptPlugin, isKotlinProject,
                                        hasAnnotationProcessorConfiguration, preferAptOnKotlinProject)) {
            project.plugins.apply(AndroidAptPlugin)
            usesAptPlugin = true
        }

        project.android.registerTransform(new RealmTransformer(project))

        project.repositories.add(project.getRepositories().jcenter())
        project.dependencies.add(dependencyConfigurationName, "io.realm:realm-annotations:${Version.VERSION}")
        if (usesAptPlugin) {
            project.dependencies.add("apt", "io.realm:realm-annotations-processor:${Version.VERSION}")
            project.dependencies.add("androidTestApt", "io.realm:realm-annotations-processor:${Version.VERSION}")
        } else if (isKotlinProject && !preferAptOnKotlinProject) {
            project.dependencies.add("kapt", "io.realm:realm-annotations-processor:${Version.VERSION}")
            project.dependencies.add("kaptAndroidTest", "io.realm:realm-annotations-processor:${Version.VERSION}")
        } else {
            assert hasAnnotationProcessorConfiguration
            project.dependencies.add("annotationProcessor", "io.realm:realm-annotations-processor:${Version.VERSION}")
            project.dependencies.add("androidTestAnnotationProcessor", "io.realm:realm-annotations-processor:${Version.VERSION}")
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

    private static String getDependencyConfigurationName(Project project) {
        /*
         * Dependency configuration name for android gradle plugin 3.0.0-*.
         * We need to use 'api' instead of 'implementation' since user's model class
         * might be using Realm's classes and annotations.
         */
        def newDependencyName = "api"
        def oldDependencyName = "compile"
        try {
            project.getConfigurations().getByName(newDependencyName)
            return newDependencyName
        } catch (UnknownConfigurationException ignored) {
            oldDependencyName
        }
    }

    private static boolean shouldApplyAndroidAptPlugin(boolean usesAptPlugin, boolean isKotlinProject,
                                                       boolean hasAnnotationProcessorConfiguration,
                                                       boolean preferAptOnKotlinProject) {
        if (usesAptPlugin) {
            // for any projects that uses android-apt plugin already. No need to apply it twice.
            return false
        }
        if (isKotlinProject) {
            // for any Kotlin projects where user did not apply 'android-apt' plugin manually.
            return preferAptOnKotlinProject && !hasAnnotationProcessorConfiguration
        }
        // for any Java Projects where user did not apply 'android-apt' plugin manually.
        return !hasAnnotationProcessorConfiguration
    }

    // This will setup the required dependencies.
    // Due to how Gradle works, we have no choice but to run this code every time any of the parameters
    // in the Realm extension is changed.
    private static void setDependencies(Project project, String dependencyConfigurationName, boolean syncEnabled, boolean kotlinExtensionsEnabled) {
        // remove libraries first
        def iterator = project.getConfigurations().getByName(dependencyConfigurationName).getDependencies().iterator()
        while (iterator.hasNext()) {
            def item = iterator.next()
            if (item.group == 'io.realm') {
                if (item.name.startsWith('realm-android-library')) {
                    iterator.remove()
                }
                if (item.name.startsWith('realm-android-kotlin-extensions')) {
                    iterator.remove()
                }
            }
        }

        // then add again
        def syncArtifactName = "realm-android-library${syncEnabled ? '-object-server' : ''}"
        project.dependencies.add(dependencyConfigurationName, "io.realm:${syncArtifactName}:${Version.VERSION}")

        if (kotlinExtensionsEnabled) {
            def kotlinExtArtifactName = "realm-android-kotlin-extensions${syncEnabled ? '-object-server' : ''}"
            project.dependencies.add(dependencyConfigurationName, "io.realm:${kotlinExtArtifactName}:${Version.VERSION}")
        }
    }
}
