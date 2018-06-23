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

import io.realm.transformer.RealmTransformer

import com.android.build.api.transform.Transform
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.internal.artifacts.dsl.dependencies.DefaultDependencyHandler
import org.gradle.api.internal.plugins.PluginApplicationException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue
import static org.junit.Assert.fail

class PluginTest {

    private Project project
    private String currentVersion

    @Before
    void setUp() {
        project = ProjectBuilder.builder().build()
        currentVersion = new File("../version.txt").text.trim()
    }

    @Test
    void pluginAddsRightDependencies() {
        project.buildscript {
            repositories {
                mavenLocal()
                google()
                jcenter()
            }
            dependencies {
                classpath 'com.android.tools.build:gradle:3.1.0-alpha03'
                classpath 'com.jakewharton.sdkmanager:gradle-plugin:0.12.0'
            }
        }

        def manifest = project.file("src/main/AndroidManifest.xml")
        manifest.parentFile.mkdirs()
        manifest.text = '<manifest xmlns:android="http://schemas.android.com/apk/res/android"  package="com.realm.test"></manifest>'

        project.apply plugin: 'com.android.application'
        project.apply plugin: 'realm-android'

        project.android {
            compileSdkVersion 27

            defaultConfig {
                minSdkVersion 16
                targetSdkVersion 27
            }
        }

        project.evaluate()

        assertTrue(containsDependency(project.dependencies, 'io.realm', 'realm-android-library', currentVersion))
        assertTrue(containsDependency(project.dependencies, 'io.realm', 'realm-annotations', currentVersion))
        assertTrue(containsTransform(project.android.transforms, RealmTransformer.class))
    }

    @Test
    void pluginFailsWithoutAndroidPlugin() {
        project.buildscript {
            repositories {
                mavenLocal()
                jcenter()
            }
            dependencies {
                classpath 'com.android.tools.build:gradle:3.1.0-alpha03'
                classpath 'com.jakewharton.sdkmanager:gradle-plugin:0.12.0'
            }
        }

        try {
            project.apply plugin: 'realm-android'
            fail()
        } catch (PluginApplicationException e) {
            assertEquals(e.getCause().class, GradleException.class)
            assertTrue(e.getCause().getMessage().contains("'com.android.application' or 'com.android.library' plugin required."))
        }
    }

    private static boolean containsUrl(RepositoryHandler repositories, String url) {
        for (repo in repositories) {
            if (repo.properties.get('url').toString() == url) {
                return true
            }
        }
        return false
    }

    private static boolean containsDependency(DependencyHandler dependencies,
                                              String group, String name, String version) {
        def configurationContainerField = DefaultDependencyHandler.class.getDeclaredField("configurationContainer")
        configurationContainerField.setAccessible(true)
        def configurationContainer = configurationContainerField.get(dependencies)
        def compileConfiguration = configurationContainer.findByName("api")

        def DependencySet dependencySet = compileConfiguration.getDependencies()
        for (Dependency dependency in dependencySet) {
            if (dependency.properties.group == group
                    && dependency.properties.name == name
                    && dependency.properties.version == version) {
                return true
            }
        }
        return false
    }

    private static boolean containsTransform(List<Transform> transformList, Class<? extends Transform> targetClass) {
        for (Transform t : transformList) {
            if (t.getClass() == targetClass) {
                return true
            }
        }
        return false
    }
}
